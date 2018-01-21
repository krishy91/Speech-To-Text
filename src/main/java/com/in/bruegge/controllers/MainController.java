package com.in.bruegge.controllers;

import com.in.bruegge.service.TranscribeService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import com.in.bruegge.util.AudioRecorder;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

public class MainController {
    @FXML
    private Label transcribedText;

    // record duration, in milliseconds
    static final long RECORD_TIME = 5000;  // 1 minute

    AudioRecorder audioRecorder;

    TranscribeService transcribeService = TranscribeService.getService();

    public MainController() {
        audioRecorder = AudioRecorder.getRecorder();
    }


    @FXML protected void handleRecordButtonAction(ActionEvent event) {
        Thread stopper = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(RECORD_TIME);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                audioRecorder.stopRecording();
            }
        });

        stopper.start();

        audioRecorder.startRecording();
    }

    @FXML protected void handleTranscribeButtonAction(ActionEvent event) {
        transcribedText.setText("Transcribing...");

        ListenableFuture<Response> whenResponse = transcribeService.transcribeAudio(audioRecorder.getWavFile());

        java.util.concurrent.Executor executor = Executors.newFixedThreadPool(1);

        //TODO Move threading related stuff to util class
        whenResponse.addListener(() -> {
            try  {
                Response response = whenResponse.get();
                Platform.runLater(() -> {
                        transcribedText.setText(response.getResponseBody());
                    }
                );
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, executor);
    }

}
