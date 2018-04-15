package com.in.bruegge.service;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

import java.io.File;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class FeedbackService {
    static FeedbackService service;

    //TODO Move to HTTPConnector
    AsyncHttpClient asyncHttpClient;

    //TODO Move to config
    static String SERVER_URL = "http://127.0.0.1:5000/feedback";

    private FeedbackService(){
        asyncHttpClient = asyncHttpClient();
    }

    public ListenableFuture<Response> sendFeedback(File wavFile, String wavText){

        String postBody = "{\"wav_url\":\"" + wavFile.getAbsolutePath()
                + "\",\"wav_text\":\"" + wavText  + "\"}" ;

        System.out.println(postBody);

        return asyncHttpClient.preparePost(SERVER_URL)
                .setHeader("Content-Type","application/json")
                .setBody(postBody)
                .execute();
    }

    public static FeedbackService getService(){
        if(service == null){
            service = new FeedbackService();
        }
        return service;
    }
}
