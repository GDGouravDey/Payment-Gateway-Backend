package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.User;
import services.PaymentService;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

public class HttpServer {
    private final PaymentService paymentService;
    private final int port;
    private com.sun.net.httpserver.HttpServer server;

    public HttpServer(PaymentService paymentService, int port) {
        this.paymentService = paymentService;
        this.port = port;
    }

    public void start() throws Exception {
        server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);

        // Set up endpoints
        server.createContext("/users", new UserHandler());
        server.createContext("/deposit", new DepositHandler());
        server.createContext("/withdraw", new WithdrawHandler());
        server.createContext("/balance", new BalanceHandler());
        server.createContext("/health", new HealthHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("HTTP Server started on port " + port);
        System.out.println("Available endpoints:");
        System.out.println("  POST /users - Create user");
        System.out.println("  GET /users/{userId} - Get user by ID");
        System.out.println("  POST /deposit - Process deposit");
        System.out.println("  POST /withdraw - Process withdrawal");
        System.out.println("  GET /balance/{userId} - Get user balance");
        System.out.println("  GET /health - Health check");
    }

    public void stop() {
        if (server != null) {
            server.stop(5);
            System.out.println("HTTP Server stopped");
        }
    }

    // User management handler
    class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            try {
                if ("POST".equals(method)) {
                    handleCreateUser(exchange);
                } else if ("GET".equals(method)) {
                    String userId = extractUserIdFromPath(path);
                    handleGetUser(exchange, userId);
                } else {
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private void handleCreateUser(HttpExchange exchange) throws Exception {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseJsonToMap(requestBody);

            String userId = params.get("userId");
            String username = params.get("username");
            BigDecimal balance = new BigDecimal(params.getOrDefault("balance", "0.00"));
            User.Currency currency = User.Currency.valueOf(params.getOrDefault("currency", "USD"));

            paymentService.createUser(userId, username, balance, currency);
            sendResponse(exchange, 201, "{\"message\":\"User created successfully\",\"userId\":\"" + userId + "\"}");
        }

        private void handleGetUser(HttpExchange exchange, String userId) throws Exception {
            User user = paymentService.getUserById(userId);
            if (user != null) {
                String response = String.format(
                        "{\"userId\":\"%s\",\"username\":\"%s\",\"balance\":%s,\"currency\":\"%s\"}",
                        user.getUserId(), user.getUsername(), user.getBalance().toString(), user.getCurrency()
                );
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
            }
        }
    }

    // Deposit handler
    class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseJsonToMap(requestBody);

                String userId = params.get("userId");
                BigDecimal amount = new BigDecimal(params.get("amount"));
                String idempotencyKey = params.get("idempotencyKey");

                String transactionId = paymentService.processDeposit(userId, amount, idempotencyKey);

                String response = String.format(
                        "{\"transactionId\":\"%s\",\"message\":\"Deposit request processed\"}",
                        transactionId
                );
                sendResponse(exchange, 202, response);

            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // Withdrawal handler
    class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseJsonToMap(requestBody);

                String userId = params.get("userId");
                BigDecimal amount = new BigDecimal(params.get("amount"));
                String idempotencyKey = params.get("idempotencyKey");

                String transactionId = paymentService.processWithdrawal(userId, amount, idempotencyKey);

                String response = String.format(
                        "{\"transactionId\":\"%s\",\"message\":\"Withdrawal request processed\"}",
                        transactionId
                );
                sendResponse(exchange, 202, response);

            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // Balance handler
    class BalanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                String path = exchange.getRequestURI().getPath();
                String userId = extractUserIdFromPath(path);

                User user = paymentService.getUserById(userId);
                if (user != null) {
                    String response = String.format(
                            "{\"userId\":\"%s\",\"balance\":%s,\"currency\":\"%s\"}",
                            user.getUserId(), user.getBalance().toString(), user.getCurrency()
                    );
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"User not found\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // Health check handler
    class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendResponse(exchange, 200, "{\"status\":\"healthy\",\"timestamp\":\"" + java.time.Instant.now() + "\"}");
        }
    }

    // Utility methods
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String extractUserIdFromPath(String path) {
        String[] parts = path.split("/");
        return parts.length > 2 ? parts[2] : "";
    }

    private Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new java.util.HashMap<>();
        json = json.trim().substring(1, json.length() - 1); // Remove { }

        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }
}
