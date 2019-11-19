package com.grpc.chat;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {
    private static Set<StreamObserver<Chat.ChatMessageFromServer>> observers = ConcurrentHashMap.newKeySet();

    @Override
    /*
        Need to use a StreamObserver to stream data back to the client.
        The return type is actually what the client sends to the server
        and the argument is what the server sends to the client.
     */
    public StreamObserver<Chat.ChatMessage> chat(StreamObserver<Chat.ChatMessageFromServer> responseObserver) {
        observers.add(responseObserver);

        /*
            This listens to the callback of the client and responds.
            It is a listener and a stream producer.
         */
        return new StreamObserver<Chat.ChatMessage>() {

            @Override
            public void onNext(Chat.ChatMessage value) { // Called when a client sends a message to the server
                System.out.println(value);
                Chat.ChatMessageFromServer message = Chat.ChatMessageFromServer.newBuilder()
                        .setMessage(value)
                        .setTimestamp(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000))
                        .build();

                /* Stream the message to all registered observers
                    or if you prefer, just some particular ones.
                 */
                for (StreamObserver<Chat.ChatMessageFromServer> observer : observers) {
                    observer.onNext(message);
                }
            }

            @Override
            public void onError(Throwable t) {
                // do something (see onComplete);
            }

            @Override
            public void onCompleted() {
                observers.remove(responseObserver); // If client is finished streaming, remove connection.
            }
        };
    }
}
