package database;

import models.Transaction;
import java.sql.*;

public class TransactionDataAccess {

    public void insertTransaction(Transaction transaction) throws Exception {
        String query = """
            INSERT INTO transactions (
                transaction_id, account_id, amount, type, status,
                prev_balance, new_balance, failure_reason,
                creation_time, processed_time
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            Connection connection = DBManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);

            connection.setAutoCommit(false);

            ps.setString(1, transaction.getTransactionId());
            ps.setString(2, transaction.getAccountId());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setString(4, transaction.getType().name());
            ps.setString(5, transaction.getStatus().name());

            // Handle null values properly
            if (transaction.getPrevBalance() != null) {
                ps.setBigDecimal(6, transaction.getPrevBalance());
            } else {
                ps.setNull(6, java.sql.Types.DECIMAL);
            }

            if (transaction.getNewBalance() != null) {
                ps.setBigDecimal(7, transaction.getNewBalance());
            } else {
                ps.setNull(7, java.sql.Types.DECIMAL);
            }

            ps.setString(8, transaction.getFailureReason());

            // Convert Instant to Timestamp
            ps.setTimestamp(9, transaction.getCreationTime() != null ?
                    Timestamp.from(transaction.getCreationTime()) : null);
            ps.setTimestamp(10, transaction.getProcessedTime() != null ?
                    Timestamp.from(transaction.getProcessedTime()) : null);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new Exception("Failed to insert transaction");
            }

            connection.commit();

        } catch (Exception e) {
            throw new Exception("Error inserting transaction: " + e.getMessage(), e);
        }
    }
}