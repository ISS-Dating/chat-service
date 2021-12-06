package com.knu.service.chat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.knu.service.chat.grpcweb.GrpcPortNumRelay;
import com.knu.service.chat.grpcweb.JettyWebserverForGrpcwebTraffic;
import com.knu.service.chat.service.ChatServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ChatServer {
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    Server server;

    public void start(int grpcPort, int grpcWebPort) throws IOException {
        server = ServerBuilder.forPort(grpcPort)
                .addService(new ChatServiceImpl())
                .build()
                .start();

        logger.info("****  started gRPC Service on port# " + grpcPort);

        JettyWebserverForGrpcwebTraffic traffic = new JettyWebserverForGrpcwebTraffic(grpcWebPort);
        traffic.start();
        GrpcPortNumRelay.setGrpcPortNum(grpcPort);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                ChatServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }
}
