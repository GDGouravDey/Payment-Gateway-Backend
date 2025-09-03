package database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

import models.Transaction;
import models.User;
import models.User.Currency;

public class UserDataAccess {
    public void createUser (String userId, String username, BigDecimal balance, Currency currency) throws Exception {
        String query = "INSERT INTO users (user_id, username, balance, currency, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection connection = DBManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            
            ps.setString(1, userId);
            ps.setString(2, username);
            ps.setBigDecimal(3, balance);
            ps.setString(4, currency.name());
            Timestamp now = Timestamp.from(Instant.now());
            ps.setTimestamp(5, now);
            ps.setTimestamp(6, now);

            ps.executeUpdate();
        }
        catch (Exception e) {
            throw new Exception("Error creating user: " + e.getMessage());
        }
    }

    public boolean userExists(String userId) throws Exception {
        String query = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try {
            Connection connection = DBManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            throw new Exception("Error checking user existence: " + e.getMessage());
        }
        return false;
    }

    public User findByUserId(String userId) throws Exception {
        return findUser(1, userId);
    }

    public User findByUsername(String username) throws Exception {
        return findUser(2, username);
    }

    public User findUser(int type, String userDetail) throws Exception{
        try {
            String query = "";
            if (type == 1)
                query = "SELECT * FROM users WHERE user_id = ?";
            else if (type == 2)
                query = "SELECT * FROM users WHERE username = ?";
            Connection connection = DBManager.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, userDetail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getString("user_id"));
                user.setUsername(rs.getString("username"));
                user.setBalance(rs.getBigDecimal("balance"));
                user.setCurrency(User.Currency.valueOf(rs.getString("currency")));
                user.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                user.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                return user;
            }
        }
        catch (Exception e) {
            throw new Exception("Error finding user: " + e.getMessage());
        }
        return null;
    }

    public void updateBalance(String userId, BigDecimal newBalance) throws Exception {
        String selectQuery = "SELECT * FROM users WHERE user_id = ? FOR UPDATE";
        String updateQuery = "UPDATE users SET balance = ?, updated_at = ? WHERE user_id = ?";

        Connection connection = null;
        try {
            connection = DBManager.getConnection();
            connection.setAutoCommit(false);

            PreparedStatement ps1 = connection.prepareStatement(selectQuery);

            ps1.setString(1, userId);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) {
                throw new Exception("User not found with ID: " + userId);
            }

            PreparedStatement ps2 = connection.prepareStatement(updateQuery);
            
            ps2.setBigDecimal(1, newBalance);
            ps2.setTimestamp(2, Timestamp.from(Instant.now()));
            ps2.setString(3, userId);

            int rowsAffected = ps2.executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("Failed to update balance for user: " + userId);
            }

        }
        catch (Exception e) {

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Error during transaction rollback: " + rollbackEx.getMessage());
                }
            }

            throw new Exception("Error updating user balance: " + e.getMessage());
        }
        finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (Exception ex) {
                    System.err.println("Error resetting auto-commit: " + ex.getMessage());
                }
            }
        }
    }

}
