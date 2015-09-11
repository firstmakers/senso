/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.tour;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author eDelgado
 */
public class Tour {
    Stage dialog;
    FXMLLoader fXMLLoader;
    TourViewController tvc;
  

    public Tour(Window win) {
        System.out.println("***********Init TouR**********");
        dialog = new Stage();
        //ventana modal
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(win);
        dialog.setTitle("Ayuda");
        tvc = new TourViewController();
        
        Scene dialogScene = new Scene(tvc);
        dialogScene.getStylesheets().add("/styles/Styles.css");
        dialog.setScene(dialogScene);  
        dialog.setMinWidth(600.0);
        dialog.setMinHeight(400.0);
        dialog.setMaxWidth(800);
        dialog.setMaxHeight(600);
    }

    public void showTour(){
        tvc.add(tvc.getFirstNode());
        dialog.show();
    }
    
   
    
}
