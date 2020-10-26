package com.tallcraft.nerfphantoms;

import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;
import java.util.UUID;

public class Storage {
    private final String type;
    private final String host;
    private final int port;
    private final String name;
    private final String username;
    private final String password;

    private Connection connection;

    public Storage(ConfigurationSection config) {
        this.type = config.getString("type");
        this.host = config.getString("host");
        this.port = config.getInt("port");
        this.name = config.getString("name");
        this.username = config.getString("username");
        this.password = config.getString("password");
    }

    public void init() throws SQLException {
        // Setup database connection
        connection = DriverManager.getConnection(
                "jdbc:" + type + "://" + host + ":" + port + "/" + name,
                username, password);

        // Initialize table if it doesn't exist
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS nerfphantoms_phantom_disabled (uuid VARCHAR(36) PRIMARY KEY)");
    }

    boolean getPhantomDisabled(UUID uuid) throws SQLException {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID can't be null");
        }
        final String sql = "SELECT EXISTS(SELECT uuid FROM nerfphantoms_phantom_disabled WHERE uuid = ?)";
        PreparedStatement statement = this.connection.prepareStatement(sql);
        statement.setString(1, uuid.toString());
        ResultSet result = statement.executeQuery();
        result.next();
        return result.getBoolean(1);
    }

    void setPhantomDisabled(UUID uuid, boolean isDisabled) throws SQLException {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID can't be null");
        }

        if (isDisabled) {
            final String sql = "INSERT IGNORE INTO nerfphantoms_phantom_disabled (uuid) VALUES (?)";
            PreparedStatement statement = this.connection.prepareStatement(sql);
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
            return;
        }

        final String sql = "DELETE FROM nerfphantoms_phantom_disabled WHERE uuid = ?";
        PreparedStatement statement = this.connection.prepareStatement(sql);
        statement.setString(1, uuid.toString());
        statement.executeUpdate();
    }
}
