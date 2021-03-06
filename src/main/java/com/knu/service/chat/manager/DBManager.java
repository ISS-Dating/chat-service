package com.knu.service.chat.manager;

import com.knu.service.chat.tools.Converter;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessage;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class DBManager {

    private static final String allChatMessages = "SELECT * FROM chat_messages WHERE chat_id=?";
    private static final String addNewMessage = "INSERT INTO chat_messages  (chat_id, sender_id, recipient_id, body, timestamp) VALUES (?, ?, ?, ?, ?)";

    private static final Logger logger = Logger.getLogger(DBManager.class.getName());
    private Connection connection = null;

    public DBManager(String url, String user, String password) throws IOException {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.warning("PostgreSQL JDBC Driver is not found.");
            e.printStackTrace();
            return;
        }

        logger.info("PostgreSQL JDBC Driver successfully connected");

        try {

            connection = DriverManager.getConnection(url, user, password);
            logger.info("Connected to DB");

        } catch (SQLException e) {
            logger.warning("Connection Failed");
            e.printStackTrace();
            return;
        }
    }

    public List<ChatMessage.ChatResponse> getAllChatHistory(ChatInfoOuterClass.ChatInfo chatInfo) {
        List<ChatMessage.ChatResponse> list = new LinkedList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(allChatMessages, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, chatInfo.getChatId());

            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                ChatMessage.ChatResponse response = Converter.getUserFromResultSet(result);

                if ((chatInfo.getSenderId().equals(response.getChatInfo().getSenderId()) && chatInfo.getRecipientId().equals(response.getChatInfo().getRecipientId()))
                        || (chatInfo.getSenderId().equals(response.getChatInfo().getRecipientId()) && chatInfo.getRecipientId().equals(response.getChatInfo().getSenderId()))) {
                    list.add(response);
                }
            }
        } catch (SQLException throwables) {
            logger.warning(throwables.getMessage());
            return null;
        }
        logger.info("messages successfully found");
        return list;
    }

    public ChatMessage.ChatResponse addNewMessage(ChatMessage.ChatRequest request) {

        long timestamp = System.currentTimeMillis();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(addNewMessage, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, request.getChatInfo().getChatId());
            preparedStatement.setString(2, request.getChatInfo().getSenderId());
            preparedStatement.setString(3, request.getChatInfo().getRecipientId());
            preparedStatement.setString(4, request.getBody());
            preparedStatement.setLong(5, timestamp);

            preparedStatement.executeUpdate();

            logger.info("Message successfully added");
        } catch (SQLException throwables) {
            logger.warning("Cound not add message: " + throwables.getMessage());
            return null;
        }
        return ChatMessage.ChatResponse.newBuilder()
                .setChatInfo(request.getChatInfo())
                .setBody(request.getBody())
                .setTimestampInMillis(timestamp)
                .build();
    }
}
