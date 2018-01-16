package com.in.bruegge;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXMLStarter extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("views/main.fxml"));
        Scene scene = new Scene(root, 1280, 720);
        stage.setTitle("Speech-To-Text");
        stage.setScene(scene);
        stage.show();
    }
}