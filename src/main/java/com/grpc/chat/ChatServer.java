package com.grpc.chat;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ChatServer {
    public static void main(String[] args) throws InterruptedException, IOException {
        Server server = ServerBuilder.forPort(9090).addService(new ChatServiceImpl()).build();

        server.start();
        System.out.println("Server started successfully");
        server.awaitTermination();
    }
}
