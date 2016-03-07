/*
 * 
 */
package cl.tide.fm.device;
import cl.tide.fm.model.Sensor;
import cl.tide.fm.utilities.Commands;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hid4java.*;



/**
 * @author Edison Delgado
 */
public class FmSenso extends FmDevice {
    
    private final short vendorID = 0x4d8;
    private final short productID = 0x3f; 
    private SensorManager sensorManager;
    private ArrayList<FmSensoListener> mListener;
    private boolean running = false;
    private long interval;
    private Timer writer;


    
    public  FmSenso()throws HidException{
        super();
        setProductID(productID);
        setVendorID(vendorID);
        setInterval(1000);
        mListener = new ArrayList<>();
        sensorManager = new SensorManager();
        initialize();
    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public ArrayList<HidDevice> getDevices() {
        return devices;
    }

    public void setDevices(ArrayList<HidDevice> devices) {
        this.devices = devices;
    }

    public HidDevice getCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(HidDevice currentDevice) {
        this.currentDevice = currentDevice;
    }
    
    public synchronized void addFmSensoListener(FmSensoListener listener){
        if(!mListener.contains(listener))
            mListener.add(listener);
    }
      
    @Override
    public void deviceDetached(HidDevice device){
        synchronized(this){
            mListener.stream().forEach((l) -> {l.onDetachedDevice(device);});
        }
        stop();      
    }

    @Override
    public void deviceAttached(HidDevice device) {
        synchronized (this) {
            mListener.stream().forEach((l) -> {
                l.onAttachedDevice(device);
            });
        }
    }
    
    
public void start() {
        if (currentDevice.open()) {
            startReporter();
            /*writer = new Writer();
            thread = new Thread(writer);
            thread.start();*/
            writer = new Timer();
            write();
            running = true;
            writeCommand(Commands.FIRMWARE);
            synchronized (this) {
                for (FmSensoListener l : mListener) {
                    l.onStart();   
                }
            }
        }else{
            System.out.println("el dispositivo no está listo... intentando nuevamente");
            start();
        }
    }

    @Override
    public void reportData(byte[] data) {      
  
        switch(data[0]){
            case Commands.EXTERNAL_SENSORS:
                processExternalSensor(data);
                //System.out.println("MEDICION EXTERNA "+Arrays.toString(data));
                break;
            case Commands.INTERNAL_SENSORS:
                processInternalSensor(data);
                //System.out.println("MEDICION INTERNA"+Arrays.toString(data));
                break;
            case Commands.FIRMWARE:
                setFirmware(data);
            default:
                ExternalSensorStatus(data);
                //System.out.println("REPORTE DE SENSORES CONECTADOS "+Arrays.toString(data));
        }         
    }

    public int writeCommand(byte command) {
        System.out.println("write command "+ command);
        return super.write(command);
    }
    
    /*
    Get number of sensor connected to senso board
    */
    public int getSensorCount(){
        return sensorManager.getSensorCount();
    }
    
    private void ExternalSensorStatus(byte[] data) {
        int oldCount = getSensorCount();
        int count = (int) data[0];
        if(count == oldCount)
            return;
        
        if (oldCount > count) {
            List<Sensor> removed = sensorManager.getDetachedSensor(data);
            synchronized(this){
                if(mListener!=null)
                mListener.stream().forEach((l) -> {l.onSensorDetach(new ArrayList<>(removed));});
            }
            sensorManager.removeSensor(removed);
        } else{
            List<Sensor> added = sensorManager.getAttachedSensor(data);
            synchronized(this){      
                if(mListener!=null)
                    mListener.stream().forEach((l) -> {l.onSensorAttach(new ArrayList<>(added));});
            }
            sensorManager.addSensor(added);
        } 
    }
   
    public void stop() {
        if (running) {
            running = false;
            writer.cancel();

            synchronized (this) {
                mListener.stream().forEach((l) -> {
                    l.onClose();
                });
            }

            stopReporter();
            sensorManager.clear();
            writer = null;
            if(currentDevice!=null && currentDevice.isOpen())
                currentDevice.close();
            
        }
    }

    private void processExternalSensor(byte[] data) {
        byte[] id = new byte[8];
        byte[] sample = new byte[2];
        sample[0]= data[9];
        sample[1]= data[10];
        //byte header = data[0];
        for(int i= 0;i< 8; i++){
            id[i]= data[i+1];
        }
        String mId = Arrays.toString(id);
        Sensor s = sensorManager.getSensorByID(mId);
        if(s!=null){
            s.setValue(sample);
            //System.out.println(mId + " Value = "+ s.getValue());
        }
        
    }
    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }
  
    public boolean isRunning() {
        return running;
    }
    
    private void write() {
        writer.schedule(new TimerTask() {          
            @Override          
            public void run() {
                try {
                    writeCommand(Commands.INTERNAL_SENSORS);
                    Thread.sleep(500);              
                    writeCommand(Commands.EXTERNAL_SENSORS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FmSenso.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 300, interval);
    }

    private void processInternalSensor(byte[] data) {
        Sensor s = sensorManager.getInternalSensors().get(0);
        byte[] d = new byte[2];
        d[0] = data[2];
        d[1] = data[3];
        s.setValue(d);
    }
    

   /* public class Writer implements Runnable{
        boolean running = true;
        public long interval = 1000; //ms       
        public void terminate(){
            running = false;
        }
        @Override
        public void run() {
            while(running){
                
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FmSenso.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }*/

    private void setFirmware(byte[] data) {
        StringBuilder b = new StringBuilder();
       
        b.append(data[1]);
        b.append(".");
        b.append(data[2]);
        setVersion(b.toString());
        mListener.forEach((l)->{
            l.onFirmwareChange(b.toString());
        });
    }
}




