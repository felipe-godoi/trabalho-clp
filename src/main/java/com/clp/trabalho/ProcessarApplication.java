package com.clp.trabalho;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

public class ProcessarApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            Status.connectPort();

            FXMLLoader fxmlLoader = new FXMLLoader(ProcessarApplication.class.getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 780, 500);
            stage.setTitle("CLP Programmer");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e){
            JOptionPane.showMessageDialog(null,
                    "A comunicação com Arduino não pode ser estabelecida com sucesso! Tente novamente!.",
                    "ERRO",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}