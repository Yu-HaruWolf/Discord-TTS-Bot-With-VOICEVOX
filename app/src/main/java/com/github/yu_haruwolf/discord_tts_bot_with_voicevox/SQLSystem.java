package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;

public class SQLSystem {
    Connection connection;
    Statement statement;
    Logger logger;
    public SQLSystem() {
        logger = LoggerFactory.getLogger(SQLSystem.class);
        try {
            // Connect to the databases.
            connection = DriverManager.getConnection("jdbc:sqlite:data.db");
            statement = connection.createStatement();
            initDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void initDatabase() throws SQLException {
        // Check existence of table
        String query = "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='servers'";
        ResultSet result = statement.executeQuery(query);
        result.next();
        if (Integer.parseInt(result.getString(1)) <= 0) {
            // If the table doesn't exist, create the table.
            statement.execute("CREATE TABLE servers(id PRIMARY KEY, volume INTEGER)");
        } else {
            // If the table exists, check the columns.
            String[] columns = {"id", "volume"};
            HashSet<String> set = new HashSet<>(Arrays.asList(columns));
            query = "SELECT name FROM pragma_table_info('servers')";
            result = statement.executeQuery(query);
            while(result.next()) {
                set.remove(result.getString(1));
            }
            // If there are lack of column(s), add it.
            for(String column : set) {
                statement.execute("ALTER TABLE servers ADD COLUMN " + column);
            }
        }
    }

    public int getVolume(String id) throws SQLException{
        ResultSet resultSet = statement.executeQuery("SELECT volume FROM servers WHERE id="+ id);
        if(resultSet.next()) {
            return Integer.parseInt(resultSet.getString(1));
        }
        updateVolume(id, 10);
        return 10;
    }

    public void updateVolume(String id, int volume) throws SQLException {
        statement.execute("REPLACE INTO servers(id, volume) VALUES (" + id + "," + volume + ")");
    }

}
