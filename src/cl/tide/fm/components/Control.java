/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.components;

import de.thomasbolz.javafx.NumberSpinner;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

/**
 *
 * @author Edison Delgado
 */
public class Control extends AnchorPane{
    public Text sampleText;
    public Button btnStart;
    public Button btnStop;
    public Button btnSave;
    public Button btnPause;
    public ProgressBar progressBar;
    public NumberSpinner samples;
    public NumberSpinner interval;
    private FXMLLoader fxmlLoader;

    public Control() {
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Control.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }       
    }
     

}
