/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.tour;

import cl.tide.fm.components.Control;
import cl.tide.fm.components.LigthView;
import cl.tide.fm.components.SensorView;
import cl.tide.fm.model.Ligth;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author eDelgado
 */
public final class TourViewController extends VBox {

    FXMLLoader fxmlLoader;
    @FXML
    VBox componentContainer;
    @FXML
    TextFlow textcontainer;
    Text description;
    Text title;
    List<Node> nodes;
    int index = 0;
    private static final Logger LOG = Logger.getLogger(TourViewController.class.getName());
    ContextMenu cMenu;

    public TourViewController() {
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/TourView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        cMenu = setContextMenu();
        loadNodes();
    }

    public void add(Node node) {
        
        componentContainer.getChildren().add(node);

    }
    public void remove(Node node){
        componentContainer.getChildren().remove(node);
    }

    public void loadNodes() {
        nodes = new ArrayList<>();
        description = new Text();
        description.setId("description-text");
        title = new Text();
        title.setId("title-text");
        description.setText("Posiciona el mouse sobre los elementos para ver la descripción");

        textcontainer.getChildren().addAll(title, description);
        nodes.add(getControlView());
        nodes.add(getSensorView());
        nodes.add(getChart());
    }

    /*
     * Retorna la vista del control
     */
    private Control getControlView() {
        Control ctr = new Control();
        /*ctr.samples.setNumber(new BigDecimal(300));
        ctr.interval.setNumber(BigDecimal.ONE);
        ctr.interval.setOnMouseEntered((MouseEvent e) -> {
            animateText(title);
            title.setText("Intervalo\n");
            description.setText("Es el valor en segundos entre cada muestra.");
        });
        ctr.samples.setOnMouseEntered((MouseEvent e) -> {
            animateText(title);
            title.setText("Muestras\n");
            description.setText("Representa al total de mediciones por cada sensor.");
        });*/
        ctr.btnStart.setOnMouseEntered((MouseEvent event) -> {
            animateText(title);
            title.setText("Botón Iniciar\n");
            description.setText("Al hacer click en este botón, inicia "
                    + "la captura de datos de los sensores conectados.");
        });
        ctr.btnPause.setOnMouseEntered((MouseEvent event) -> {
            animateText(title);
            title.setText("Botón Pausar\n");
            description.setText("Al hacer Click en este botón, pausa "
                    + "la captura de datos y se detiene hasta pulsar "
                    + "nuevamente el botón iniciar.");
        });
        ctr.btnStop.setOnMouseEntered((MouseEvent event) -> {
            animateText(title);
            title.setText("Botón Detener\n");
            description.setText("Al hacer Click en este botón, detiene "
                    + "la captura de datos.");
        });
        /*ctr.btnSave.setOnMouseEntered((MouseEvent event) -> {
            animateText(title);
            title.setText("Botón guardar\n");
            description.setText("Al hacer Click en este botón, se"
                    + "genera un archivo con las mediciones capturadas.");
        });*/
        ctr.progressBar.setOnMouseEntered((MouseEvent event) -> {
            animateText(title);
            title.setText("Barra de Progreso\n");
            description.setText("Indica el progreso de una medición en curso");
        });
        return ctr;
    }

    /*
     * Crea y retorna una vista de un sensor
     * @return vista del sensor de luminosidad
     */
    private SensorView getSensorView() {
        Ligth l = new Ligth("l");
        LigthView sensorview = new LigthView(l);
        sensorview.decimal.setText("8");
        sensorview.integer.setText("462.");
        sensorview.integer.setOnMouseEntered((MouseEvent e) -> {
            animateText(title);
            title.setText("Valor Actual\n");
            description.setText("Muestra el valor actual del sensor.");
        });
       /* sensorview.name.setOnMouseEntered((MouseEvent e) -> {
            animateText(title);
            title.setText("Nombre del sensor\n");
            description.setText("Muestra el Nombre actual del sensor. Es editable"
                    + " y se puede cambiar. Haga clic sobre el texto para editar.");
        });
        sensorview.cbx.setOnMouseEntered((MouseEvent e) -> {
            animateText(title);
            title.setText("Ver Sensor\n");
            description.setText("Habilita y deshabilita la serie del sensor en el gráfico.");
        });
        sensorview.colorPicker.setOnMouseEntered((MouseEvent e) -> {
            animateText(title);
            title.setText("Selector de color\n");
            description.setText("Escoje un color para distinguir cada sensor.");
        });*/
        sensorview.setSensor(new Ligth("1"));
        return (SensorView) sensorview;
    }

    private LineChart getChart() {
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        //creating the chart
        final LineChart<Number, Number> lineChart
                = new LineChart<>(xAxis, yAxis);

        XYChart.Series series = new XYChart.Series();
        series.setName("Luminosidad");
        series.getData().add(new XYChart.Data(1, 400));
        series.getData().add(new XYChart.Data(2, 535));
        series.getData().add(new XYChart.Data(3, 566));
        series.getData().add(new XYChart.Data(4, 200));
        series.getData().add(new XYChart.Data(5, 100));
        series.getData().add(new XYChart.Data(6, 36));
        lineChart.getData().add(series);
        lineChart.setId("linechart");
        lineChart.setOnMouseEntered((MouseEvent e) -> {
            animateText(title);
            title.setText("Gráfico\n");
            description.setText("Muestra en tiempo real los valores de los sensores. También puede hacer clic derecho para aceder a la configuración del gráfico.");
        });

        lineChart.setOnMouseClicked((MouseEvent event) -> {
            if (MouseButton.SECONDARY.equals(event.getButton())) {
                cMenu.show(lineChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });
        return lineChart;
    }

    private ContextMenu setContextMenu() {

        MenuItem clearChart = new MenuItem("Borrar gráfico");
        //GlyphsDude.setIcon(clearChart, FontAwesomeIcon.TRASH, "1.5em");
        MenuItem chartManager = new MenuItem("Detener gráfico");
        //GlyphsDude.setIcon(chartManager, FontAwesomeIcon.STOP, "1.5em");

        CheckMenuItem fastUpdate = new CheckMenuItem("Graficar cada 1 segundo");
        CheckMenuItem normalUpdate = new CheckMenuItem("Graficar cada 5 segundos");
        CheckMenuItem slowUpdate = new CheckMenuItem("Graficar cada 10 segundos");
        normalUpdate.setSelected(true);
        clearChart.setOnAction(e -> {

        });

        chartManager.setOnAction(e -> {
            if (chartManager.getText().equals("Detener gráfico")) {

                chartManager.setText("Iniciar gráfico");
                //GlyphsDude.setIcon(chartManager, FontAwesomeIcon.PLAY, "1.5em");
            } else {

                chartManager.setText("Detener gráfico");
                //GlyphsDude.setIcon(chartManager, FontAwesomeIcon.STOP, "1.5em");
            }
        });

        /*fastUpdate.setOnAction(e -> {
            if (fastUpdate.isSelected()) {

                slowUpdate.setSelected(false);
                normalUpdate.setSelected(false);

            }
        });
        normalUpdate.setOnAction(e -> {
            if (normalUpdate.isSelected()) {

                slowUpdate.setSelected(false);
                fastUpdate.setSelected(false);
            }
        });

        slowUpdate.setOnAction(e -> {
            if (slowUpdate.isSelected()) {

                fastUpdate.setSelected(false);
                normalUpdate.setSelected(false);

            }
        });*/

        ContextMenu menu = new ContextMenu(
                chartManager,
                clearChart,
                new SeparatorMenuItem()/*,
                fastUpdate,
                normalUpdate,
                slowUpdate*/
        );
        return menu;
    }

    /*
     * Muestra el siguiente componente 
     */
    public void showNext(ActionEvent e) {
        if (index < nodes.size() - 1) {           
            remove(nodes.get(index)); 
            index++;
            animateNext(nodes.get(index));
            add(nodes.get(index));
            description.setText("");
            title.setText("");     
        } 
    }
    
    public void animateNext(Node node){
        TranslateTransition tt = new TranslateTransition(new Duration(500));
        tt.setNode(node);
        tt.setFromX(500);
        tt.setToX(0.0);
        tt.play();
    }
    public void animatePreview(Node node){
        TranslateTransition tt = new TranslateTransition(new Duration(500));
        tt.setNode(node);
        tt.setFromX(-500);
        tt.setToX(0);
        tt.play();
    }
    public void animateText(Text text){
        FadeTransition ft = new FadeTransition(new Duration(500));
        ft.setNode(text);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
    /*
     * Muestra el componente anterior
     */
    public void showPreview(ActionEvent e) {
        if (index < nodes.size() && index > 0) {
            remove(nodes.get(index));
            index--;
            animatePreview(nodes.get(index));
            add(nodes.get(index));
            title.setText("");
            description.setText("");
        }
    }

    /*
     * Devuelve el primer elemento de los componentes
     */
    public Node getFirstNode() {
        if (nodes.size() > 0) {
            return nodes.get(0);
        } else {
            throw new NullPointerException();
        }
    }

}
