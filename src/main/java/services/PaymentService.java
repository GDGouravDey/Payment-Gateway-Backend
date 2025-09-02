package services;

import database.UserDataAccess;
import models.Transaction;
import models.User;
import transaction.TransactionQueue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentService {
    private final UserDataAccess userDA;
    private final TransactionQueue queue;
    private final ConcurrentHashMap<String, String> idCache;

    public PaymentService() {
        this.userDA = new UserDataAccess();
        this.queue = TransactionQueue.getInstance();
        this.idCache = new ConcurrentHashMap<>();
    }

    public void createUser(String userId, String username, BigDecimal initialBalance,
                           User.Currency currency) throws Exception {
        validateUserId(userId);
        validateUsername(username);
        validateAmount(initialBalance);

        userDA.createUser(userId, username, initialBalance, currency);
    }

    public User getUserById(String userId) throws Exception {
        try {
            validateUserId(userId);
            return userDA.findByUserId(userId);
        }
        catch (Exception e) {
            throw new Exception("Error fetching user: " + e.getMessage());
        }
    }

    public User getUserByUsername(String username) throws Exception {
        try {
            validateUsername(username);
            return userDA.findByUsername(username);
        }
        catch (Exception e) {
            throw new Exception("Error fetching user: " + e.getMessage());
        }
    }

    public String processDeposit(String userId, BigDecimal amount, String idKey) throws Exception {
        try {
            if(null == getUserById(userId))
                throw new Exception("User does not exist in DB");
            validateTransaction(userId, amount, idKey);

            if (idCache.containsKey(idKey)) {
                String existingTxId = idCache.get(idKey);
                System.out.println("Duplicate request detected: " + idKey);
                return existingTxId;
            }

            getUserById(userId);

            String transactionId = UUID.randomUUID().toString();
            Transaction transaction = createTransaction(transactionId, userId, amount, Transaction.TransactionType.DEPOSIT, idKey);

            queue.enqueue(transaction);
            idCache.put(idKey, transactionId);

            System.out.println("Enqueued deposit transaction: " + transactionId);
            return transactionId;
        }
        catch (Exception e) {
            throw new Exception("Error validating transaction: " + e.getMessage());
        }

    }

    public String processWithdrawal(String userId, BigDecimal amount, String idKey) throws Exception {
        try {
            if(null == getUserById(userId))
                throw new Exception("User does not exist in DB");
            validateTransaction(userId, amount, idKey);

            if (idCache.containsKey(idKey)) {
                String existingTxId = idCache.get(idKey);
                System.out.println("Duplicate request detected: " + idKey);
                return existingTxId;
            }

            User user = getUserById(userId);
            if (user.getBalance().compareTo(amount) < 0) {
                throw new Exception("Insufficient funds for withdrawal");
            }

            String transactionId = UUID.randomUUID().toString();
            Transaction transaction = createTransaction(transactionId, userId, amount, Transaction.TransactionType.WITHDRAW, idKey);

            queue.enqueue(transaction);
            idCache.put(idKey, transactionId);

            System.out.println("Enqueued withdrawal transaction: " + transactionId);
            return transactionId;
        }
        catch (Exception e) {
            throw new Exception("Error validating transaction: " + e.getMessage());
        }
    }

    public void executeTransaction(Transaction transaction) throws Exception {
        User user = getUserById(transaction.getAccountId());
        transaction.setPrevBalance(user.getBalance());

        BigDecimal newBalance;
        if (transaction.getType() == Transaction.TransactionType.DEPOSIT) {
            newBalance = user.getBalance().add(transaction.getAmount());
        }
        else {
            if (user.getBalance().compareTo(transaction.getAmount()) < 0) {
                throw new Exception("Insufficient balance during processing");
            }
            newBalance = user.getBalance().subtract(transaction.getAmount());
        }

        userDA.updateBalance(user.getUserId(), newBalance);
        transaction.setNewBalance(newBalance);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setProcessedTime(Instant.now());
    }

    private Transaction createTransaction(String transactionId, String accountId, BigDecimal amount, Transaction.TransactionType type, String idempotencyKey) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setCreationTime(Instant.now());
        return transaction;
    }

    private void validateTransaction(String userId, BigDecimal amount, String idKey) throws Exception {
        validateUserId(userId);
        validateAmount(amount);

        if (idKey == null || idKey.trim().isEmpty()) {
            throw new Exception("Idempotency key is required");
        }
    }

    private void validateUserId(String userId) throws Exception {
        if (userId == null || userId.trim().isEmpty()) {
            throw new Exception("User ID cannot be empty");
        }
    }

    private void validateUsername(String username) throws Exception {
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("Username cannot be empty");
        }
    }

    private void validateAmount(BigDecimal amount) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Amount must be positive");
        }
    }
}
