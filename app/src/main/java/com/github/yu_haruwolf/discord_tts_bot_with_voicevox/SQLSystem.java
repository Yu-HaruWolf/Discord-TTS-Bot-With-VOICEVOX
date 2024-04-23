package com.github.yu_haruwolf.discord_tts_bot_with_voicevox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SQLSystem {
    Connection connection;
    Statement statement;
    Logger logger;
    Map<String, Integer> cacheSpeakerMap = new HashMap<>();
    Map<String, Integer> cacheVolumeMap = new HashMap<>();

    public SQLSystem() {
        logger = LoggerFactory.getLogger(SQLSystem.class);
        cleanCache();
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
                query = "ALTER TABLE servers ADD COLUMN ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, column);
                preparedStatement.executeQuery();
            }
        }
    }

    public int getVolume(String id) throws SQLException {
        if(!cacheVolumeMap.containsKey(id)) {
            return cacheVolumeMap.get(id);
        }
        String query = "SELECT volume FROM servers WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            cacheVolumeMap.put(id, resultSet.getInt(1));
            return resultSet.getInt(1);
        }
        updateVolume(id, 10);
        return 10;
    }

    public void updateVolume(String id, int volume) throws SQLException {
        String query = "REPLACE INTO servers(id, volume) VALUES (?, "+ volume + ")";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1,id);
        preparedStatement.execute();
        cacheVolumeMap.put(id, volume);
    }

    public int getSpeakerId(String userId) throws SQLException {
        if(cacheSpeakerMap.containsKey(userId)) {
            return cacheSpeakerMap.get(userId);
        }
        String query = "SELECT speaker FROM users WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, userId);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            int result = Integer.parseInt(resultSet.getString(1));
            cacheSpeakerMap.put(userId, result);
            return result;
        }
        return 3;
    }
    public void updateSpeakerId(String userId, int speakerId) throws SQLException {
        String query = "REPLACE INTO users(id, speaker) VALUES (?, " + speakerId + ")";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, userId);
        preparedStatement.execute();
        cacheSpeakerMap.put(userId, speakerId);
    }

    public void cleanCache() {
        cacheSpeakerMap = new HashMap<>();
        cacheVolumeMap = new HashMap<>();
    }
}
