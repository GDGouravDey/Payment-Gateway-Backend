package transaction;

import models.Transaction;
import services.PaymentService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TransactionProcessor {
    private final PaymentService paymentService;
    private final TransactionQueue queue;
    private final ExecutorService executorService;
    private boolean isRunning = false;

    public TransactionProcessor(PaymentService paymentService) {
        this.paymentService = paymentService;
        this.queue = TransactionQueue.getInstance();
        this.executorService = Executors.newFixedThreadPool(5);
        startProcessing();
    }

    public void startProcessing() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        System.out.println("Transaction processor started");

        // Start multiple worker threads
        for (int i = 0; i < 5; i++) {
            executorService.submit(this::processTransactions);
        }
    }

    private void processTransactions() {
        while (isRunning) {
            try {
                Transaction transaction = queue.dequeue();

                if (transaction != null) {
                    try {
                        System.out.println("Processing transaction: " + transaction.getTransactionId());
                        paymentService.executeTransaction(transaction);
                        System.out.println("Transaction completed: " + transaction.getTransactionId());
                    } catch (Exception e) {
                        System.err.println("Transaction failed: " + transaction.getTransactionId() + " - " + e.getMessage());
                        transaction.setStatus(Transaction.TransactionStatus.FAILED);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error in transaction processing: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        System.out.println("Shutting down transaction processor...");
        isRunning = false;

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Transaction processor shutdown complete");
    }

    public boolean isRunning() {
        return isRunning;
    }
}