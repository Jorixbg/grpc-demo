package com.grpc.chat;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {
    //clients that are subscribed
    private static Set<StreamObserver<ChatMessageFromServer>> observers = ConcurrentHashMap.newKeySet();

    @Override
    /*since we have bi- directional streaming we have StreamObserver as return type and input param
    StreamObserver<ChatMessageFromServer> is what the server needs to send to the client and the return type is what the client send to the server
    You are returning an object that is actually getting data from the client to the server radther than sedning data back from
    the server to the client this is important and I will show you why this is done this way  */
    public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessageFromServer> responseObserver) {
        observers.add(responseObserver);

        /*
            This listens to the callback of the client and responds.
            It is a listener and a stream producer.
         */
        return new StreamObserver<ChatMessage>() {

           // When the client sends something to the server this onNext method is where we are recieving the data from client to the server,
           // and when we receive a chat message what we are going to do is to send it to all of the existing connections that we have
            @Override
            public void onNext(ChatMessage value) { // Called when a client sends a message to the server
                System.out.println(value);
                ChatMessageFromServer message = ChatMessageFromServer.newBuilder()//builder pattern is used everywhere in gRPC
                        .setMessage(value)
                        .setTimestamp(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000))
                        .build();

                /* Stream the message to all registered observers
                    or if you prefer, just some particular ones.
                 */
                for (StreamObserver<ChatMessageFromServer> observer : observers) {
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
