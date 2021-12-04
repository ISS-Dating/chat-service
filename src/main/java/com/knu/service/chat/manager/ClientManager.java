package com.knu.service.chat.manager;

import com.google.common.collect.Sets;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.tuple.MutablePair;
import service.chat.ChatInfoOuterClass;
import service.chat.ChatMessage;

import java.util.Set;

public class ClientManager {

    private Set<MutablePair<ChatInfoOuterClass.ChatInfo, StreamObserver<ChatMessage.ChatResponse>>> clients = Sets.newConcurrentHashSet();

    public void addNewClient(ChatInfoOuterClass.ChatInfo chatInfo, StreamObserver<ChatMessage.ChatResponse> streamObserver) {
        clients.add(new MutablePair<>(chatInfo, streamObserver));
    }

    public boolean removeClient(ChatInfoOuterClass.ChatInfo chatInfo) {

        for (MutablePair<ChatInfoOuterClass.ChatInfo, StreamObserver<ChatMessage.ChatResponse>> pair : clients) {

            ChatInfoOuterClass.ChatInfo temp = pair.getKey();

            if (temp.getChatId().equals(chatInfo.getChatId())) {
                clients.remove(pair);

                return true;
            }
        }

        return false;
    }

    public void boardCast(ChatMessage.ChatResponse response) {

        for (MutablePair<ChatInfoOuterClass.ChatInfo, StreamObserver<ChatMessage.ChatResponse>> pair : clients) {

            ChatInfoOuterClass.ChatInfo chatInfo = pair.getKey();

            if (response.getChatInfo().getChatId().equals(chatInfo.getChatId())) {
                pair.getValue().onNext(response);
            }
        }
    }

    public boolean isLogged(ChatInfoOuterClass.ChatInfo chatInfo) {

        for (MutablePair<ChatInfoOuterClass.ChatInfo, StreamObserver<ChatMessage.ChatResponse>> pair : clients) {

            ChatInfoOuterClass.ChatInfo temp = pair.getKey();

            if (temp.getChatId().equals(chatInfo.getChatId()) && temp.getSenderId().equals(chatInfo.getSenderId())) {
                return true;
            }
        }

        return false;
    }
}
