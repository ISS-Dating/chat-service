package com.knu.service.chat.service;

import com.google.protobuf.Timestamp;
import com.knu.service.chat.manager.ClientManager;
import com.knu.service.chat.manager.DBManager;
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

    public ChatServiceImpl() throws IOException {
        dbManager = new DBManager();
        clientManager = new ClientManager();
    }

    @Override
    public void login(ChatInfoOuterClass.ChatInfo request, StreamObserver<ChatMessage.ChatResponse> responseObserver) {

        if (!clientManager.isLogged(request)) {
            clientManager.addNewClient(request, responseObserver);
        } else {
            responseObserver.onError(new Exception("This client already logged"));
        }

        List<ChatMessage.ChatResponse> list = dbManager.getAllChatHistory(request);

        for (ChatMessage.ChatResponse response : list) {
            responseObserver.onNext(response);
        }
    }

    @Override
    public void request(ChatMessage.ChatRequest request, StreamObserver<ChatMessage.ChatStatus> responseObserver) {

        if (clientManager.isLogged(request.getChatInfo())) {

            logger.info("Received ChatInfo request on chat server:\n" + request.toString());

            ChatMessage.ChatResponse response = dbManager.addNewMessage(request);

            if (response != null) {

                clientManager.boardCast(response);

                responseObserver.onNext(ChatMessage.ChatStatus.newBuilder()
                        .setStatus(ChatStatus.Status.SUCCESS)
                        .build());
            } else {
                responseObserver.onNext(ChatMessage.ChatStatus.newBuilder()
                        .setStatus(ChatStatus.Status.ERROR)
                        .build());
            }

        } else {
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
