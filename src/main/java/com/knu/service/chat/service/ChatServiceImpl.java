package com.knu.service.chat.service;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessageOuterClass;
import service.chat.ChatServiceGrpc;

import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    private static final Logger logger = Logger.getLogger(ChatServiceImpl.class.getName());
    private ChatInfoOuterClass.ChatInfo chatInfo;

    @Override
    public void startChatService(ChatInfoOuterClass.ChatInfo request, StreamObserver<ChatMessageOuterClass.ChatMessage> responseObserver) {

        logger.info("Received ChatInfo request on chat server:\n" + request.toString());

        chatInfo = request;

        //Received messages from DB

        //Sent messages to client

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ChatMessageOuterClass.ChatMessage> openMessageStream(StreamObserver<ChatMessageOuterClass.ChatMessage> responseObserver) {

        if (chatInfo != null) {

            Timer timer = new Timer();
            NewMessageChecker checker = new NewMessageChecker(responseObserver);
            timer.schedule(checker, 0, 1000);

            return new StreamObserver<ChatMessageOuterClass.ChatMessage>() {
                @Override
                public void onNext(ChatMessageOuterClass.ChatMessage chatMessage) {

                    ChatMessageOuterClass.ChatMessage message = ChatMessageOuterClass.ChatMessage.newBuilder()
                            .setSenderId(chatMessage.getSenderId())
                            .setRecipientId(chatInfo.getRecipientId())
                            .setBody(chatMessage.getBody())
                            .setTimestamp(Timestamp.newBuilder()
                                    .setSeconds(System.currentTimeMillis())
                                    .build())
                            .build();

                    //Add message to BD

                    responseObserver.onNext(message);
                }

                @Override
                public void onError(Throwable throwable) {
                    responseObserver.onCompleted();
                }

                @Override
                public void onCompleted() {

                }
            };
        } else {
            return null;
        }
    }
}
