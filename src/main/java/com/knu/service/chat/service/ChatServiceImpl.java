package com.knu.service.chat.service;

import com.knu.service.chat.manager.ClientManager;
import com.knu.service.chat.manager.DBManager;
import com.knu.service.chat.manager.PropertiesManager;
import com.knu.service.chat.tools.Converter;
import com.sproutsocial.nsq.Publisher;
import io.grpc.stub.StreamObserver;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessage;
import service.chat.ChatServiceGrpc;
import service.chat.ChatStatus;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    private static final Logger logger = Logger.getLogger(ChatServiceImpl.class.getName());
    private final ClientManager clientManager;
    private final DBManager dbManager;
    private final Publisher publisher1;
    private final Publisher publisher2;
    private final String nsqTopic;

    public ChatServiceImpl() throws IOException {

        PropertiesManager propertiesManager = new PropertiesManager("application.properties");

        dbManager = new DBManager(propertiesManager.getProperty("db.postgres.url"), propertiesManager.getProperty("db.postgres.user"), propertiesManager.getProperty("db.postgres.password"));

        clientManager = new ClientManager();

        publisher1 = new Publisher("nsqd");
        publisher2 = new Publisher("nsqlookupd");
        nsqTopic = propertiesManager.getProperty("nsq.topic");
    }

    @Override
    public void login(ChatInfoOuterClass.ChatInfo request, StreamObserver<ChatMessage.ChatResponse> responseObserver) {
        logger.info("login called");
        if (!clientManager.isLogged(request)) {
            logger.info("add client");
            clientManager.addNewClient(request, responseObserver);
            logger.info("Client: " + request + " - added");
        } else {
            logger.info("already logged");
            responseObserver.onError(new Exception("This client already logged"));
            return;
        }

        List<ChatMessage.ChatResponse> list = dbManager.getAllChatHistory(request);

        for (ChatMessage.ChatResponse response : list) {
            responseObserver.onNext(response);
        }
    }

    @Override
    public void request(ChatMessage.ChatRequest request, StreamObserver<ChatMessage.ChatStatus> responseObserver) {

        logger.info("Received ChatInfo request on chat server:\n" + request.toString());

        if (clientManager.isLogged(request.getChatInfo())) {

            ChatMessage.ChatResponse response = dbManager.addNewMessage(request);

            if (response != null) {

                clientManager.boardCast(response);

                responseObserver.onNext(ChatMessage.ChatStatus.newBuilder()
                        .setStatus(ChatStatus.Status.SUCCESS)
                        .build());

                try {
                    publisher1.publishBuffered(nsqTopic, Converter.convert(response));
                } catch (Exception e) {}
                try {
                    publisher2.publishBuffered(nsqTopic, Converter.convert(response));
                } catch (Exception e) {}
            } else {
                responseObserver.onNext(ChatMessage.ChatStatus.newBuilder()
                        .setStatus(ChatStatus.Status.ERROR)
                        .build());
            }

        } else {
            logger.warning("CLIENT_NOT_LOGGED");
            responseObserver.onNext(ChatMessage.ChatStatus.newBuilder()
                    .setStatus(ChatStatus.Status.CLIENT_NOT_LOGGED)
                    .build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void logout(ChatInfoOuterClass.ChatInfo request, StreamObserver<ChatMessage.ChatStatus> responseObserver) {

        if (clientManager.removeClient(request)) {
            responseObserver.onNext(ChatMessage.ChatStatus.newBuilder()
                    .setStatus(ChatStatus.Status.SUCCESS)
                    .build());
        } else {
            responseObserver.onNext(ChatMessage.ChatStatus.newBuilder()
                    .setStatus(ChatStatus.Status.UNKNOWN_CHAT_INFO)
                    .build());
        }

        responseObserver.onCompleted();
    }
}
