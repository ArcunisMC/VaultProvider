package com.arcunis.vaultprovider.economy;

import com.arcunis.vaultprovider.Database;

import java.sql.*;
import java.util.*;

public class EconomyManager {

    public static void createAcc(UUID uuid) throws RuntimeException {
        if (hasAcc(uuid)) throw new RuntimeException("Player already has an account.");
        Database db = new Database();
        String insertQuery = "INSERT INTO accounts (uuid, balance) VALUES (?, ?)";
        try {
            PreparedStatement statement = db.conn.prepareStatement(insertQuery);
            statement.setString(1, uuid.toString());
            statement.setDouble(2, 0);
            statement.executeUpdate();
            db.close();
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static boolean hasAcc(UUID uuid) {
        Database db = new Database();
        String query = "SELECT 1 FROM accounts WHERE uuid = ?";
        try {
            PreparedStatement statement = db.conn.prepareStatement(query);
            statement.setString(1, uuid.toString());
            boolean hasResultSet = statement.execute();

            if (hasResultSet) {
                ResultSet resultSet = statement.getResultSet();
                boolean exists = resultSet.next();
                db.close();
                return exists; // returns true if there is at least one row

            } else {
                db.close();
                return false; // no ResultSet means no data
            }
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static double getAccBal(UUID uuid) {
        Database db = new Database();
        String query = "SELECT balance FROM accounts WHERE uuid = ?";
        try {
            PreparedStatement statement = db.conn.prepareStatement(query);
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            double bal = resultSet.getDouble("balance");
            db.close();
            return bal;
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException("Account not found for UUID: " + uuid);
        }
    }

    public static double depositAcc(UUID uuid, double amount) {
        Database db = new Database();
        String updateQuery = "UPDATE accounts SET balance = balance + ? WHERE uuid = ?";
        try {
            PreparedStatement statement = db.conn.prepareStatement(updateQuery);
            statement.setDouble(1, amount);
            statement.setString(2, uuid.toString());
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                db.close();
                throw new RuntimeException("Account not found for UUID: " + uuid);
            }
            db.close();
            return getAccBal(uuid); // Retrieve and return the updated balance
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static double withdrawAcc(UUID uuid, double amount) {
        Database db = new Database();
        String selectQuery = "SELECT balance FROM accounts WHERE uuid = ?";
        String updateQuery = "UPDATE accounts SET balance = balance - ? WHERE uuid = ?";

        try {
            PreparedStatement selectStatement = db.conn.prepareStatement(selectQuery);
            selectStatement.setString(1, uuid.toString());
            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                double currentBalance = resultSet.getDouble("balance");
                if (currentBalance < amount) {
                    db.close();
                    throw new RuntimeException("Insufficient funds for UUID: " + uuid);
                }
            } else {
                db.close();
                throw new RuntimeException("Account not found for UUID: " + uuid);
            }
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }

        try {
            PreparedStatement updateStatement = db.conn.prepareStatement(updateQuery);
            updateStatement.setDouble(1, amount);
            updateStatement.setString(2, uuid.toString());
            updateStatement.executeUpdate();
            db.close();
            return getAccBal(uuid); // Retrieve and return the updated balance
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static void createBank(String name, UUID owner) throws RuntimeException {
        if (hasBank(name)) throw new RuntimeException("Bank with name %s already exists.".formatted(name));
        Database db = new Database();
        String insertQuery = "INSERT INTO banks (name, owner, balance) VALUES (?, ?, ?)";
        try {
            PreparedStatement statement = db.conn.prepareStatement(insertQuery);
            statement.setString(1, name);
            statement.setString(2, owner.toString());
            statement.setDouble(3, 0);
            statement.executeUpdate();
            db.close();
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static void deleteBank(String name) {
        Database db = new Database();
        String deleteQuery = "DELETE FROM banks WHERE name = ?";
        try {
            PreparedStatement statement = db.conn.prepareStatement(deleteQuery);
            statement.setString(1, name);
            statement.executeUpdate();
            db.close();
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static boolean hasBank(String name) {
        Database db = new Database();
        String query = "SELECT 1 FROM banks WHERE name = ?";
        try {
            PreparedStatement statement = db.conn.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            boolean exists = resultSet.next();
            db.close();
            return exists;
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static double getBankBal(String name) {
        Database db = new Database();
        String query = "SELECT balance FROM banks WHERE name = ?";
        try {
            PreparedStatement statement = db.conn.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double bal = resultSet.getDouble("balance");
                db.close();
                return bal;
            } else {
                db.close();
                throw new RuntimeException("Bank not found with name: " + name);
            }
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static UUID getBankOwner(String name) {
        Database db = new Database();
        String query = "SELECT owner FROM banks WHERE name = ?";
        try {
            PreparedStatement statement = db.conn.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String owner = resultSet.getString("owner");
                db.close();
                return UUID.fromString(owner);
            } else {
                db.close();
                throw new RuntimeException("Bank not found with name: " + name);
            }
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static void setBankOwner(String name, UUID newOwner) {
        Database db = new Database();
        String updateQuery = "UPDATE banks SET owner = ? WHERE name = ?";
        try {
            PreparedStatement statement = db.conn.prepareStatement(updateQuery);
            statement.setString(1, newOwner.toString());
            statement.setString(2, name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<UUID> getBankMembers(String name) {
        Database db = new Database();
        String query = "SELECT member_uuid FROM bank_members WHERE bank_name = ?";
        Set<UUID> members = new HashSet<>();
        try {
            PreparedStatement statement = db.conn.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) members.add(UUID.fromString(resultSet.getString("member_uuid")));
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
        db.close();
        return members;
    }

    public static void addBankMember(String name, UUID uuid) {
        Database db = new Database();
        String insertQuery = "INSERT INTO bank_members (bank_name, member_uuid) VALUES (?, ?)";
        try {
            PreparedStatement statement = db.conn.prepareStatement(insertQuery);
            statement.setString(1, name);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
            db.close();
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static void removeBankMember(String name, UUID uuid) {
        Database db = new Database();
        String deleteQuery = "DELETE FROM bank_members WHERE bank_name = ? AND member_uuid = ?";
        try {
            PreparedStatement statement = db.conn.prepareStatement(deleteQuery);
            statement.setString(1, name);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
            db.close();
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static double depositBank(String name, double amount) {
        Database db = new Database();
        String updateQuery = "UPDATE banks SET balance = balance + ? WHERE name = ?";
        try {
            PreparedStatement statement = db.conn.prepareStatement(updateQuery);
            statement.setDouble(1, amount);
            statement.setString(2, name);
            statement.executeUpdate();
            db.close();
            return getBankBal(name);
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static double withdrawBank(String name, double amount) {
        Database db = new Database();
        String selectQuery = "SELECT balance FROM banks WHERE name = ?";
        String updateQuery = "UPDATE banks SET balance = balance - ? WHERE name = ?";

        try {
            PreparedStatement selectStatement = db.conn.prepareStatement(selectQuery);
            selectStatement.setString(1, name);
            ResultSet resultSet = selectStatement.executeQuery();
            if (resultSet.next()) {
                double currentBalance = resultSet.getDouble("balance");
                if (currentBalance < amount) {
                    db.close();
                    throw new RuntimeException("Insufficient funds for bank: " + name);
                }
            } else {
                db.close();
                throw new RuntimeException("Bank not found with name: " + name);
            }
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }

        try {
            PreparedStatement updateStatement = db.conn.prepareStatement(updateQuery);
            updateStatement.setDouble(1, amount);
            updateStatement.setString(2, name);
            updateStatement.executeUpdate();
            db.close();
            return getBankBal(name);
        } catch (SQLException e) {
            db.close();
            throw new RuntimeException(e);
        }
    }

    public static Set<String> getAllBankNames() {
        Database db = new Database();
        String query = "SELECT name FROM banks";
        Set<String> bankNames = new HashSet<>();
        try {
            PreparedStatement statement = db.conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                bankNames.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        db.close();
        return bankNames;
    }

}
