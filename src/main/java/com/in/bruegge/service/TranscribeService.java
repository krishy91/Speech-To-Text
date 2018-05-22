package com.in.bruegge.service;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import com.typesafe.config.ConfigFactory;
import java.awt.*;
import java.io.File;
import java.util.concurrent.Future;

import static org.asynchttpclient.Dsl.*;

public class TranscribeService {

    static TranscribeService service;

    //TODO Move to HTTPConnector
    AsyncHttpClient asyncHttpClient;

    //TODO Move to config
    static String SERVER_URL = "http://127.0.0.1:5000/transcribe";

    private TranscribeService(){
         asyncHttpClient = asyncHttpClient();
         try{
             Config conf   = ConfigFactory.load();
             String server = conf.getString("app.server");
             SERVER_URL = server + "/transcribe";
             //System.out.println("Got some config");

         }catch(ConfigException e){
             e.printStackTrace();
             //System.out.println("No custom config");
         }

    }

    public ListenableFuture<Response> transcribeAudio(File wavFile){

        String postBody = "{\"wav_url\":\"" + wavFile.getAbsolutePath() + "\"}" ;

        return asyncHttpClient.preparePost(SERVER_URL)
                .setHeader("Content-Type","application/json")
                .setBody(postBody)
                .execute();
    }

    public static TranscribeService getService(){
        if(service == null){
            service = new TranscribeService();
        }
        return service;
    }
}
