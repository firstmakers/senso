/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.controller;

import cl.tide.fm.utilities.IntegerField;
import de.thomasbolz.javafx.NumberSpinner;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import cl.tide.fm.utilities.*;

/**
 * FXML Controller class
 *
 * @author edisondelgado
 */
public class ProgramController extends AnchorPane {

    /**
     * Initializes the controller class.
     */
    FXMLLoader fxmlLoader;
    @FXML
    public IntegerField hour, minute, second;
    @FXML
    public NumberSpinner sample;
    @FXML
    public Button cancel, ok;
    @FXML
    public Text  validator;
    @FXML
    public AnchorPane container;
 
    @FXML
    public Button program;
    public long programTime = 0;
 
    private Timer timer;    
    private  ArrayList<ProgramListener> listener;
    

    public ProgramController() {

        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/program.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        listener = new ArrayList<>();

        hour.setMinValue(0);
        minute.setMinValue(0);
        second.setMinValue(0);
        
        hour.setMaxValue(23);
        minute.setMaxValue(59);
        second.setMaxValue(59);
        
        hour.setValue(0);
        minute.setValue(0);
        second.setValue(0);
    }
    /**
     *
     * @param event
     */
    public void program(ActionEvent event){
        validator.setText("");
       
        long ms = getHourToMillisecond(hour.getValue(), 
                minute.getValue(),
                second.getValue());
        
        long currentTime = getHourToMillisecond(LocalTime.now().getHour(), 
                LocalTime.now().getMinute(), 
                LocalTime.now().getSecond());
        if(ms == 0){
            ms = 86400000; 
            System.out.println(ms);
        }
        else if(ms < currentTime){
            ms = 86400000 + ms; 
            System.out.println(ms);
        }
            //notify
        programTime = ms;
        cancelTimer();
        listener.stream().forEach((l)->{l.onChangeProgram(programTime);});
        //Timer
        setTimer(programTime);
        
    }
    
    public static long getHourToMillisecond(int hour , int min, int sec){
         return hour*3600000 + 
                min*60000 + 
                sec*1000;
    }
    
    public static int[] getMsToHMS(long ms){
        int[] hms = new int[3];
        hms[0] = (int) (ms/3600000);
        long restoHora = ms%3600000;
        hms[1]= (int) (restoHora/60000);
        long restoMinutos= restoHora%60000;
        hms[2]= (int) (restoMinutos/1000);
        return hms;         
    }
    
    public void  cancelTimer(){
        if(timer != null){
            timer.cancel();
            timer.purge();
        }
    }
    
    public void setCurrentProgram(long ms){
        this.programTime = ms;
        int [] hms = getMsToHMS(ms);
        setTimer(programTime);
        hour.setValue(hms[0]);
        minute.setValue(hms[1]);
        second.setValue(hms[2]);
    }
    
    public boolean addListener(ProgramListener p){
        if(!listener.contains(p)){
            return listener.add(p); 
        }
        return false;      
    }
    
    public boolean removeListener(ProgramListener p){
        return listener.remove(p);
    }
    
    public void setTimer(long programTime){
            if(timer!= null)
                cancelTimer();
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    long current = getHourToMillisecond(LocalTime.now().getHour(), 
                          LocalTime.now().getMinute(), 
                          LocalTime.now().getSecond()); 
                    int[] remaining = getMsToHMS(programTime - current);
                    String hora = remaining[0]<10? "0"+remaining[0]:remaining[0]+"";
                    String minutos = remaining[1]<10? "0"+remaining[1]:remaining[1]+"";
                    String segundos = remaining[2]<10? "0"+remaining[2]:remaining[2]+"";
                    Platform.runLater(()->{
                        validator.setText("Comienzo en "+hora+":"+minutos+":"+segundos);
                    });
                    if(current >= programTime)
                        timer.cancel();
                }
            }, 0, 1000);
    }

    public interface ProgramListener{
           public void onChangeProgram(long programInMS);
    }
}
