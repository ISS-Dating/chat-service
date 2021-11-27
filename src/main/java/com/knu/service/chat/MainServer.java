package com.knu.service.chat;
import java.io.IOException;

public class MainServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        ChatServer server = new ChatServer();
        server.start(5555);
        server.blockUntilShutdown();
    }
}
