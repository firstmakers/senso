package cl.tide.fm.senso;

import cl.tide.fm.controller.FXMLController;
import com.sun.javafx.PlatformUtil;
import de.codecentric.centerdevice.platform.osx.NSMenuBarAdapter;
import java.net.URL;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class MainApp extends Application {
    
    private FXMLLoader fxmlLoader;
    private String version;
    private String name;
    private Stage st;

    @Override
    public void start(Stage stage) throws Exception {
        st = stage;
        name = "Senso";
        version = "1.1";
        URL location = getClass().getResource("/fxml/Scene.fxml");
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        Parent root = (Parent) fxmlLoader.load(location.openStream());

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        stage.getIcons().add(new Image("/images/senso.png"));
        stage.setTitle(name.concat(" ").concat(version));
        stage.setScene(scene);
        //min sizes
        stage.setMinWidth(960.0);
        stage.setMinHeight(640.0);
        stage.show();
        //tweak menu mac
        if (PlatformUtil.isMac()) {
            NSMenuBarAdapter creator = new NSMenuBarAdapter();
            creator.renameMenuItem(0, 0, "Ocultar Senso");
            creator.renameMenuItem(0, 1, "Ocultar otros");
            creator.renameMenuItem(0, 2, "Mostar todos");
            creator.renameMenuItem(0, -1, "Salir de Senso");
            creator.renameApplicationMenu("Senso");

            /*Menu help = new Menu("Ayuda");
            MenuItem item = new MenuItem("Ver ayuda");
            item.setOnAction((ActionEvent event) -> {
                showHelp(event);
            });
            help.getItems().add(item);
            creator.addMenu(help);*/
            FXMLController controller = (FXMLController)fxmlLoader.getController();
            controller.setStage(st);
        }

    }
    

    @Override
    public void stop() throws Exception {      
        FXMLController controller = (FXMLController)fxmlLoader.getController();
        controller.close();
        super.stop(); 
    } 
    
    public void showHelp(ActionEvent e){
        FXMLController controller = (FXMLController)fxmlLoader.getController();
        controller.showTour(st);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }



}
