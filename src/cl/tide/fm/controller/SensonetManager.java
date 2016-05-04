/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.controller;

import java.io.IOException;
import javafx.fxml.FXMLLoader;

/**
 *
 * @author edisondelgado
 */
public class SensonetManager {
    FXMLLoader fxmlLoader;
    
    /**
     */
    public SensonetManager() {
        
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/interval.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
}
