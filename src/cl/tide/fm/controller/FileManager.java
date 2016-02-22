/*
 *
 */
package cl.tide.fm.controller;


import cl.tide.fm.components.SensorView;
import cl.tide.fm.utilities.AsyncTask;
import com.ubidots.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 * Esta Clase se encarga de almacenar los datos de las mediciones de la tarjeta 
 * senso, mediante el método @streamrow(), cada vez que se realiza una medición 
 * se guardan los datos a un archivo temporal del disco duro al terminar se llama a la 
 * función @flush() para cerrar el archivo de salida y guardarlo en un ruta 
 * especifica.
 * Los datos son almacenados directamente en el Disco duro para minimizar el 
 * consumo de memoría RAM, Se Recomienda esctrictamente no desconectar los 
 * sensores externos durante una medición.
 * 
 * @author Edison Delgado
 */
public class FileManager {
    
    private ApiClient ubidots;
    private DataSource dataSource;
    /*Libro Excel*/
    SXSSFWorkbook workbook;
    /*Hoja de cálculo*/
    Sheet sheet;
    /*Fila de hoja de cálculo */
    Row row;
    /* Archivo de salida */
    FileOutputStream out ;
    /* Puntero, indica el numero de la fila actual*/
    int indexRow;
    /* Lista que contiene los sensores conectados, (no se usa)*/
    List<SensorView> samples;
    public String workspace; 
    public Boolean saveAll = false;
    String [] header; 
    private final FXMLController controller;

    private boolean isOnline = false;
    private String lastFile = "";
    /*
    * Constructor
    */
    public FileManager(ArrayList<SensorView> s, FXMLController controller){
        this.controller = controller;
        this.samples = s;
        create();
       // boolean allowUbidots = SettingsController.getAllowUbidots();
        //String apikey = SettingsController.getUbidotsApiKey();
        /*if (allowUbidots && !apikey.isEmpty()) 
            ubidots = new ApiClient(apikey);*/
        
        
    }
    private Variable[] helperVariable;
   
    /*
    * Crea una nueva instancia de un documento excel, reinicia el contador 
    * de filas
    */
    public void create(){
        workbook= new SXSSFWorkbook(10);
        sheet = workbook.createSheet("Senso");  
        indexRow = 0; 
        

    }
    /*
    * Exporta un objeto lista a excel, recomendado para archivos con pocas filas
    * (datos), actualmente no se usa.
    */
    public  void exportToExcel(String path) throws FileNotFoundException, IOException{
        int numRow = 0;
        FileOutputStream out = new FileOutputStream(path);
        SXSSFWorkbook wb = new SXSSFWorkbook(1);
        Sheet s =  wb.createSheet();
        wb.setSheetName(0, "Senso Medición");
        Row r = s.createRow(numRow);
        //set header
        int cellnum = 0;
        for(SensorView v: samples){
          
            String name = v.getName();
            Cell c = r.createCell(cellnum);
            c.setCellStyle(getHeaderStyle(wb));  
            c.setCellValue(name);
            s.autoSizeColumn(cellnum);
            cellnum++;
            System.out.print(name+ " " +cellnum );
        } 

        int totalSensor = samples.size();
        int length = samples.get(0).getSensor().getData().size();
        for(int i = 0 ; i < length; i++){
            r = s.createRow(i+1);
            for(int j=0; j < totalSensor;j++){
                double value = samples.get(j).getSensor().getData().get(i);
                Cell c = r.createCell(j);
                c.setCellValue(value);
                c.setCellStyle(getBodyStyle(wb));
            }
        }
        wb.write(out);
        out.close();  
        wb.dispose();
    }
    
    /*
    * Devuelve el estilo de la hoja de calculo para los datos de 
    * la cabecera del documento
    */
    private static CellStyle getHeaderStyle(Workbook wb){
        CellStyle cs = wb.createCellStyle();
        Font f = wb.createFont();
        f.setColor((short)0x34);
        f.setBoldweight(Font.BOLDWEIGHT_BOLD);
        f.setFontHeightInPoints((short) 12);
        cs.setFont(f);
        cs.setAlignment(CellStyle.ALIGN_CENTER);

        return cs;
    }
    
    private static XSSFColor getColor(Color color){
        XSSFColor myColor = new XSSFColor(
                new java.awt.Color(
                        (int)color.getRed(), 
                        (int)color.getGreen(),
                        (int)color.getBlue()
                )
        );
        return myColor;
    }
    /*
    * Devuelve el estilo de la hoja de calculo para los datos del 
    * cuerpo del documento
    */
    private static CellStyle getBodyStyle(Workbook wb){
        CellStyle cs = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        cs.setFillBackgroundColor((short) 0x00);
        cs.setFont(f);
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setBorderBottom(CellStyle.BORDER_THIN);
        cs.setBorderLeft(CellStyle.BORDER_THIN);
        cs.setBorderRight(CellStyle.BORDER_THIN);    
        return cs;
    }
    

    /*
    * Sobre el archivo creado crea una nueva fila de datos
    */
    public void streamRow(Object[] ob, CellStyle style){           
            int length = ob.length;    
            row = sheet.createRow(indexRow);
            for(int i=0;i<length;i++){
                Cell c = row.createCell(i);
                if(ob[i] instanceof Double){
                    Double value = new Double(ob[i].toString());
                    c.setCellValue(value);
                }
                else{
                    c.setCellValue(ob[i].toString());
                }
                if(style != null){
                    c.setCellStyle(style);
                    sheet.autoSizeColumn(i);
                }
                else{
                    c.setCellStyle(getBodyStyle(workbook));
                }
            }
            indexRow++; 
    }
    
    /*
    * Cierra el archivo y lo guarda en la ruta seleccionada por el usuario.
    * @params path: ruta absoluta del archivo.
    */
    public void save(String path) {
    if(indexRow == 0 && saveAll){
        try{
            Files.copy(new File(lastFile).toPath(), new File(path).toPath());
        }catch(IOException e){    
        }
    }
    else{
    try {
        out = new FileOutputStream(path);
        workbook.write(out);
        out.close();
        create();
        } catch (Exception ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } 
    }
    lastFile = path;
    }


    public void newWorkbook(String[] header) {
        helperVariable = null;
        this.header = header;
        if (indexRow == 0) {
            streamRow(header, getHeaderStyle(workbook));
            //Ubidots     
        }
        /*if (SettingsController.getAllowUbidots()) {
            PrepareUbidots task = new PrepareUbidots(controller);
            task.execute();
            System.err.println("Lanzando ubidots");
        }*/
    }

    public void automaticSave(){
        if(saveAll && indexRow > 0){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
            String dateString = LocalDateTime.now().format(formatter);
            new File(workspace).mkdir();
            save(workspace+File.separator+dateString+".xlsx");
        }
    }  
    
    
    class PrepareUbidots extends AsyncTask{
        FXMLController ctrl;
        public PrepareUbidots(FXMLController ctrl){
            this.ctrl = ctrl;
        }

        @Override
        public void onPreExecute() {
            ctrl.info("Preparando estructura de datos para ubidots", 0);
            
        }

        @Override
        public void doInBackground() {  
            isOnline = false;
            if(ubidots == null)
                ubidots = new ApiClient(SettingsController.getUbidotsApiKey());
            dataSource = ubidots.createDataSource("Senso");
                helperVariable = new Variable[header.length-1];
                for (int i = 0; i < helperVariable.length; i++) {
                    helperVariable[i] = dataSource.createVariable(header[i+1]);
                    //ctrl.info("Preparando variable de "+header[i], 0);
            }
        }

        @Override
        public void onPostExecute() {
            //ctrl.info("Configuración Ubidots lista", 0);
            if( helperVariable!= null && helperVariable.length > 0)
                isOnline = true;
            else
                isOnline = false;
            
            System.out.println("Post Execute");
        }

        @Override
        public void progressCallback(Object... params) {
            
        }
    }
}
