package transaction;

import models.Transaction;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TransactionQueue {
    private static TransactionQueue instance;
    private final BlockingQueue<Transaction> queue;

    private TransactionQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public static synchronized TransactionQueue getInstance() {
        if (instance == null) {
            instance = new TransactionQueue();
        }
        return instance;
    }

    public void enqueue(Transaction transaction) throws Exception {
        try {
            transaction.setStatus(Transaction.TransactionStatus.PENDING);
            queue.put(transaction);
            System.out.println();
        }
        catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new Exception("Failed to enqueue transaction: " + e.getMessage());
        }
    }

    public Transaction dequeue() throws InterruptedException {
        Transaction transaction = queue.take();
        transaction.setStatus(Transaction.TransactionStatus.PROCESSING);
        return transaction;
    }

    public int size() {
        return queue.size();
    }
}