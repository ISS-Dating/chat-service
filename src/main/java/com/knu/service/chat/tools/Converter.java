package com.knu.service.chat.tools;

import com.google.protobuf.Timestamp;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Converter {

    public static ChatMessage.ChatResponse getUserFromResultSet(ResultSet resultSet) throws SQLException {
        return ChatMessage.ChatResponse.newBuilder()
                .setChatInfo(ChatInfoOuterClass.ChatInfo.newBuilder()
                    .setChatId(resultSet.getString("chat_id"))
                    .setSenderId(resultSet.getString("sender_id"))
                    .setRecipientId(resultSet.getString("recipient_id"))
                    .build())
                .setBody(resultSet.getString("body"))
                .setTimestampInMillis(resultSet.getLong("timestamp"))
                .build();
    }

    public static byte[] convert(ChatMessage.ChatResponse response) {
        String s = "{\"chat_id\": \"" + response.getChatInfo().getChatId() + "\", "+
                "\"sender_id\": \"" + response.getChatInfo().getSenderId() + "\", " +
                "\"recipient_id\": \"" + response.getChatInfo().getRecipientId() + "\", " +
                "\"message\": \"" + response.getBody() + "\", " +
                "\"time_in_millis\": \"" + response.getTimestampInMillis() + "\"}";
        return s.getBytes();
    }
}
