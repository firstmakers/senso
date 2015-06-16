package cl.tide.fm.components;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author eDelgado
 */
public class DeviceInfo extends VBox{
    @FXML private Text name;
    @FXML private Text firmware;
    @FXML private Text status;
    @FXML private ImageView icon;
    private FXMLLoader fxmlLoader;

    /**
     * Initializes the controller class.
     */
    
    public DeviceInfo() {
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DeviceInfo.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    } 

    public String getName() {
        return name.getText();
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public String getFirmware() {
        return firmware.getText();
    }

    public void setFirmware(String firmware) {
        this.firmware.setText(firmware);
    }

    public String getStatus() {
        return status.getText();
    }

    public void setStatus(String status) {
        this.status.setText(status);
    }

    public Image getIcon() {
        return icon.getImage();
    }

    public void setIcon(Image icon) {
        this.icon.setImage(icon);
    }    
    
}
