import api.HttpServer;
import database.DBManager;
import services.PaymentService;
import transaction.TransactionProcessor;

public class Main {

    private static HttpServer httpServer;
    private static TransactionProcessor processor;

    public static void main(String[] args) {
        System.out.println("Payment Gateway Server starting...");
        try {
            // Initialize connection with SQL Database
            DBManager.initializeConnection();

            // Initialize the Services
            PaymentService ps = new PaymentService();
            processor = new TransactionProcessor(ps);

            // Start HTTP server
            httpServer = new HttpServer(ps, 8080);
            httpServer.start();

            System.out.println("Payment Gateway Server running on Port 8000");

            // Keep server running
            Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));
            Thread.currentThread().join();
        }
        catch (Exception e) {
            System.err.println("Error starting Server: "+e.getMessage());
            shutdown();
        }
    }
    public static void shutdown() {
        System.out.println("Shutting Down Payment Gateway Server...");

        try {

            if (httpServer != null)
                httpServer.stop();

            if (processor != null)
                processor.shutdown();

            DBManager.closeConnection();
            System.out.println("Server shutdown complete");
        }
        catch (Exception e) {
            System.out.println("Error during shutdown: "+e.getMessage());
        }
    }
}