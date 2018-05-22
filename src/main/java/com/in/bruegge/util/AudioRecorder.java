package com.in.bruegge.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioRecorder {

    static AudioRecorder MANAGER;

    File wavFile;

    private AudioRecorder(){
        try{
            wavFile = new File( "recording.wav");

            if (wavFile.createNewFile()){
                System.out.println("File is created!");
            }else{
                System.out.println("File already exists.");
            }

        }catch(IOException e){
            System.out.println("Could not find");
        }
    }

    public static AudioRecorder getRecorder(){
        if(MANAGER == null){
            MANAGER = new AudioRecorder();
        }


        return MANAGER;
    }

    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // path of the wav file



    TargetDataLine line;

    AudioFormat getAudioFormat() {
        float sampleRate     = 16000;
        int sampleSizeInBits = 8;
        int channels         = 1;
        boolean signed       = true;
        boolean bigEndian    = true;

        AudioFormat format   = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);

        return format;
    }

    public void startRecording(){
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            System.out.println("Started recording..");

            AudioInputStream ais = new AudioInputStream(line);

            AudioSystem.write(ais, fileType, wavFile);

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void stopRecording(){
        line.stop();
        line.close();
        System.out.println("Stopped Recording");
    }

    public File getWavFile() {
        return wavFile;
    }
}
