package com.in.bruegge.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import com.in.bruegge.util.AudioRecorder;

public class MainController {
    @FXML
    private Label transcribedText;

    // record duration, in milliseconds
    static final long RECORD_TIME = 5000;  // 1 minute

    AudioRecorder audioRecorder;

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
        try{
            sendingPostRequest();
        }catch(Exception e){
            transcribedText.setText("There was an error in transcribing");
        }
    }

    private void sendingPostRequest() throws Exception {

        String USER_AGENT = "Mozilla/5.0";

        String url = "http://127.0.0.1:5000/transcribe";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Setting basic post request
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type","application/json");

        String postJsonData = "{\"wav_url\":\"" + audioRecorder.getWavFile().getAbsolutePath() + "\"}" ;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postJsonData);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("nSending 'POST' request to URL : " + url);
        System.out.println("Post Data : " + postJsonData);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();

        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();

        transcribedText.setText(response.toString());
    }
}
