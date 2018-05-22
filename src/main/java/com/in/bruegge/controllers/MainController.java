package com.in.bruegge.controllers;

import com.in.bruegge.service.FeedbackService;
import com.in.bruegge.service.TranscribeService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import com.in.bruegge.util.AudioRecorder;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import javafx.concurrent.Task;

public class MainController {
    @FXML
    private Label transcribedText;

    @FXML
    private ImageView recordImage;

    @FXML
    private JFXSpinner blueSpinner;

    @FXML
    private ImageView feedbackImage;

    @FXML
    private HBox feedbackBox;

    @FXML
    private StackPane rootPane;

    // record duration, in milliseconds
    static final long RECORD_TIME = 5000;  // 1 minute

    AudioRecorder audioRecorder;

    TranscribeService transcribeService = TranscribeService.getService();

    FeedbackService feedbackService = FeedbackService.getService();

    public MainController() {
        audioRecorder = AudioRecorder.getRecorder();
    }

    @FXML
    public void initialize() {


        Image feedbackImageObj   = new Image(getClass().getClassLoader().getResource("images/feedback.png").toString());
        Image startRecordingImage = new Image(getClass().getClassLoader().getResource("images/microphone_1.png").toString());
        Image stopRecordingImage = new Image(getClass().getClassLoader().getResource("images/microphone_2.png").toString());

        feedbackImage.setImage(feedbackImageObj);
        recordImage.setImage(startRecordingImage);

        recordImage.setOnMousePressed(x -> {
            recordImage.setImage(stopRecordingImage);

            Task<Integer> task = new Task<Integer>() {
                @Override protected Integer call() throws Exception {
                    audioRecorder.startRecording();
                    return 0;
                }
            };

            Thread audioThread = new Thread(task);
            audioThread.setDaemon(true);

            audioThread.start();

        });

        recordImage.setOnMouseReleased(y -> {
            recordImage.setImage(startRecordingImage);
            audioRecorder.stopRecording();
        });

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

        blueSpinner.setVisible(true);

        ListenableFuture<Response> whenResponse = transcribeService.transcribeAudio(audioRecorder.getWavFile());

        java.util.concurrent.Executor executor = Executors.newFixedThreadPool(1);

        //TODO Move threading related stuff to util class
        whenResponse.addListener(() -> {
            try  {
                Response response = whenResponse.get();
                Platform.runLater(() -> {
                        blueSpinner.setVisible(false);
                        transcribedText.setText(response.getResponseBody());
                        feedbackBox.setVisible(true);
                    }
                );
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    @FXML protected void openFeedbackForm(MouseEvent event){
        JFXDialog dialog = new JFXDialog();

        VBox parent = new VBox();
        parent.setSpacing(20);
        parent.setPadding(new Insets(20));
        parent.setAlignment(Pos.CENTER);
        parent.setPrefWidth(350.0);


        Label message = new Label();

        message.setText("For the recorded Speech sample, the transcription was '" + transcribedText.getText() + "'.\nPlease type the corrected version of the text below");
        message.setWrapText(true);
        message.setAlignment(Pos.CENTER);
        message.setTextAlignment(TextAlignment.CENTER);

        JFXTextField correctText = new JFXTextField();

        JFXButton sendFeedback = new JFXButton("Send");
        sendFeedback.setButtonType(JFXButton.ButtonType.RAISED);
        sendFeedback.setStyle("-fx-background-color: #4D66C9;-fx-text-fill: white;");
        sendFeedback.setPrefWidth(100);

        sendFeedback.setOnAction(x -> {
            if(!correctText.getText().isEmpty()){
                ListenableFuture<Response> whenResponse = feedbackService.sendFeedback(audioRecorder.getWavFile(), correctText.getText());

                java.util.concurrent.Executor executor = Executors.newFixedThreadPool(1);

                whenResponse.addListener(() -> {
                    try  {
                        Response response = whenResponse.get();
                        Platform.runLater(() -> {
                                    System.out.println("Feedback sent!");
                                }
                        );
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }, executor);

            }else{
                correctText.requestFocus();
            }



        });

        parent.getChildren().addAll(message, correctText, sendFeedback);

        dialog.setContent(parent);
        dialog.show(rootPane);
    }
}
