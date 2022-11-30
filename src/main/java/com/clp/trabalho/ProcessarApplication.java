package com.clp.trabalho;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProcessarApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Status.connectPort();

        FXMLLoader fxmlLoader = new FXMLLoader(ProcessarApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("CLP Programmer");
        stage.setScene(scene);
        stage.show();

        ProcessarController.lerInputs();
    }

    public static void main(String[] args) {
        launch();
    }
}