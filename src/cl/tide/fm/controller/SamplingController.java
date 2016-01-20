/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.controller;

import cl.tide.fm.utilities.IntegerFieldSample;
import java.io.IOException;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author edisondelgado
 */
public class SamplingController extends AnchorPane {

    /**
     * Initializes the controller class.
     */
    FXMLLoader fxmlLoader;
  
    @FXML
    public IntegerFieldSample sample;
    @FXML
    public Button cancel, ok;
    @FXML
    public Text  validator;
    @FXML
    public AnchorPane container;
 
    private ArrayList<SettingListener> listener;
    

    public SamplingController() {

        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sampling.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        listener = new ArrayList<>();


        
        sample.setMaxValue(99999999);
        sample.setMinValue(1);
        sample.setValue(1000);

    }


    /**
     *
     * @param event
     */
    
    public void clickOk(ActionEvent event){
        int num = sample.getValue();
        if(num < 1){
            validator.setText("El número de muestras debe ser mayor que 1");
            return;
        }
        else
           listener.stream().forEach((l)->{
                l.onValuesChange(num);
           });
        
        
    }
    
    
    public boolean addListener(SettingListener l){
        if(!listener.contains(l)){
            listener.add(l);
            return true;
        }
        return false; 
    }
    
    public boolean removeListener(SettingListener l){
        return listener.remove(l);
    }
    
    public interface SettingListener{
           public void onValuesChange(int samples);
    }
    
    public void setCurrentSetting(int samples){ 
        sample.setValue(samples);
    }
    

}
