/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.controller;

import cl.tide.fm.utilities.IntegerField;
import de.thomasbolz.javafx.NumberSpinner;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.eclipse.swt.widgets.DateTime;

/**
 * FXML Controller class
 *
 * @author edisondelgado
 */
public class IntervalController extends AnchorPane {

    /**
     * Initializes the controller class.
     */
    FXMLLoader fxmlLoader;
    @FXML
    public IntegerField hour, minute, second;
    //@FXML
    /*public NumberSpinner sample;*/
    @FXML
    public Button cancel, ok;
    @FXML
    public Text  validator;
    @FXML
    public AnchorPane container;
 
    private ArrayList<SettingListener> listener;
    

    public IntervalController() {

        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/interval.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        listener = new ArrayList<>();

        //default config.
        hour.setMaxValue(23);
        minute.setMaxValue(59);
        second.setMaxValue(59);
        
        hour.setMinValue(0);
        minute.setMinValue(0);
        second.setMinValue(0);
        
        hour.setValue(0);
        minute.setValue(0);
        second.setValue(0);
        
        /*sample.setMin(BigDecimal.ONE);
        sample.setMax(new BigDecimal(999999999));
        sample.setNumber(new BigDecimal(1000));*/
        


    }

    /* Numeric Validation Limit the  characters to maxLengh AND to ONLY DigitS */
    public EventHandler<KeyEvent> numeric_Validation(final Integer max_Lengh) {
        return (KeyEvent e) -> {
            TextField txt_TextField = (TextField) e.getSource();
            if (txt_TextField.getText().length() >= max_Lengh) {
                e.consume();
            }
            if (e.getCharacter().matches("/^[0-9]+$/")) {
                if (txt_TextField.getText().length() == 0 && e.getCharacter().matches("[0]")) {
                    e.consume();
                }
            } else {
                e.consume();
            }
        };
    }

    final Callback<DatePicker, DateCell> dayCellFactory
            = new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(final DatePicker datePicker) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate item, boolean empty) {
                            super.updateItem(item, empty);

                            if (item.isBefore(LocalDate.now())) {
                                setDisable(true);
                                setStyle("-fx-background-color: #ffc0cb;");
                            }
                        }
                    };
                }
            };

    /**/

    /**
     *
     * @param event
     */
    
    public void clickOk(ActionEvent event){
        long i = hour.getValue()*3600000 + minute.getValue()*60000 + second.getValue()*1000;
        final long interval = i == 0? 86400000 : i;//  0 == 24 horas
        if(listener.size() > 0)
           listener.stream().forEach((l)->{
                l.onValuesChange(interval);
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
           public void onValuesChange(long interval);
    }
    
    public void setCurrentSetting(long interval){
        
        int hora = (int) (interval/3600000);
        long restoHora = interval%3600000;
        int minutos = (int) (restoHora/60000);
        long restoMinutos= restoHora%60000;
        int segundos = (int) (restoMinutos/1000);
        hour.setValue(hora);
        minute.setValue(minutos);
        second.setValue(segundos); 
    }
    

}
