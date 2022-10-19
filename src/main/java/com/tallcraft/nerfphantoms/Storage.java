package com.tallcraft.nerfphantoms;

import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

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

    public void init(JavaPlugin plugin) throws SQLException {
        // Setup database connection
        
        if (type.equals("sqlite")) {
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + plugin.getDataFolder() + "/" + name + ".db",
                    username, password);
        } else {
            connection = DriverManager.getConnection(
                    "jdbc:" + type + "://" + host + ":" + port + "/" + name,
                    username, password);
        }

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

        String sql = "DELETE FROM nerfphantoms_phantom_disabled WHERE uuid = ?";
        PreparedStatement statement = this.connection.prepareStatement(sql);
        statement.setString(1, uuid.toString());
        statement.executeUpdate();

        if (isDisabled) {
            sql = "INSERT INTO nerfphantoms_phantom_disabled (uuid) VALUES (?)";
            statement = this.connection.prepareStatement(sql);
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
            return;
        }
    }
}
