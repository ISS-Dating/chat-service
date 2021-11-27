package com.knu.service.chat.service;

import io.grpc.stub.StreamObserver;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessageOuterClass;
import service.chat.ChatServiceGrpc;

import java.util.logging.Logger;

public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {
    private static final Logger logger = Logger.getLogger(ChatServiceImpl.class.getName());

    @Override
    public void startChatService(ChatInfoOuterClass.ChatInfo request, StreamObserver<ChatMessageOuterClass.ChatMessage> responseObserver) {

        logger.info("Received ChatInfo request on chat server:\n" + request.toString());

        //Received messages from DB

        //Sent messages to client

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ChatMessageOuterClass.ChatMessage> openMessageStream(StreamObserver<ChatMessageOuterClass.ChatMessage> responseObserver) {

        logger.info("Received client stream on chat server");

        return new StreamObserver<ChatMessageOuterClass.ChatMessage>() {
            @Override
            public void onNext(ChatMessageOuterClass.ChatMessage chatMessage) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }
}
