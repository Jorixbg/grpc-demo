package com.grpc.chat;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ChatClient extends Application {
    private ObservableList<String> messages = FXCollections.observableArrayList();
    private ListView<String> messagesView = new ListView<>();
    private TextField name = new TextField("name");
    private TextField message = new TextField();
    private Button send = new Button();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        messagesView.setItems(messages);

        send.setText("Send");

        BorderPane pane = new BorderPane();
        pane.setLeft(name);
        pane.setCenter(message);
        pane.setRight(send);

        BorderPane root = new BorderPane();
        root.setCenter(messagesView);
        root.setBottom(pane);

        primaryStage.setTitle("gRPC Chat");
        primaryStage.setScene(new Scene(root, 480, 320));

        primaryStage.show();

        // --- INTERESTING STUFF --- //

        // Setup connection channel to server
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext(true).build();

        /* Stub uses the channel to send RPCs to the service.
            Can also be blocking stub for synchronous (unary) services.
            In this case no observers are returned, but ordinary responses
         */

        ChatServiceGrpc.ChatServiceStub chatService = ChatServiceGrpc.newStub(channel);
        /*
            Opposite to the server, below is a listener, listening to messages from the server.
            Parameter listens to server, type responds to server.
         */
        StreamObserver<ChatMessage> chatClient = chatService.chat(new StreamObserver<ChatMessageFromServer>() {
            @Override
            public void onNext(ChatMessageFromServer value) { // Listens to server's onNext

                // Below code displays newly received message in the UI.
                Platform.runLater(() -> {
                    messages.add(value.getMessage().getFrom() + ": " + value.getMessage().getMessage());
                    messagesView.scrollTo(messages.size());
                });
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace(); // Display exception
                System.out.println("Disconnected");
            }

            @Override
            public void onCompleted() { // Means the server has disconnected that client
                System.out.println("Disconnected");
            }
        });

        /*
            ChatService needs to return a StreamObserver.
         */
        send.setOnAction(e -> {

            // The onNext callback that the server is listening to.
            chatClient.onNext(ChatMessage.newBuilder().setFrom(name.getText()).setMessage(message.getText()).build());
            message.setText("");
        });
        primaryStage.setOnCloseRequest(e -> {chatClient.onCompleted(); channel.shutdown(); });
    }
}
