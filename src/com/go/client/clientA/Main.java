package com.go.client.clientA;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application implements EventHandler<WindowEvent> {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("client.fxml"));
        primaryStage.setTitle("五子棋AI一号");
        primaryStage.setScene(new Scene(root, 400, 520));
        primaryStage.setResizable(true);
        primaryStage.show();
        primaryStage.setOnCloseRequest(this);
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void handle(WindowEvent event) {

        System.exit(0);

    }
}