package com.knu.service.chat.service;

import io.grpc.stub.StreamObserver;
import service.chat.ChatMessageOuterClass;

import java.util.TimerTask;

public class NewMessageChecker extends TimerTask {

    private StreamObserver<ChatMessageOuterClass.ChatMessage> responseObserver;

    public NewMessageChecker(StreamObserver<ChatMessageOuterClass.ChatMessage> responseObserver) {
        this.responseObserver = responseObserver;
    }

    @Override
    public void run() {

    }
}
