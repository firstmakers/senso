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
        mListener = new ArrayList<>();
        sensorManager = new SensorManager();
        setProductID(productID);
        setVendorID(vendorID);   
        setInterval(2000);
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
        
        //System.out.println("device is detached " + device + " listeners "+ mListener.size());
        synchronized(this){
            for(FmSensoListener l : mListener)
                l.onDetachedDevice(device);
        }
        stop();      
    }

    @Override
    public void deviceAttached(HidDevice device) {
         
        //System.out.println("device is attached " + device);
        synchronized (this) {
            for (FmSensoListener l : mListener) {
                l.onAttachedDevice(device);
            }
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
            synchronized (this) {
                for (FmSensoListener l : mListener) {
                    l.onStart();
                }
                running = true;
            }
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
            default:
                ExternalSensorStatus(data);
                //System.out.println("REPORTE DE SENSORES CONECTADOS "+Arrays.toString(data));
        }         
    }

    public int writeCommand(byte command) {
        int value = super.write(command);
        //if(value > 0) //System.out.println("Success " + value);
            //success write
        return value;
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
                for(FmSensoListener l : mListener)
                    l.onSensorDetach(new ArrayList<>(removed));
            }
            sensorManager.removeSensor(removed);
        } else{
            List<Sensor> added = sensorManager.getAttachedSensor(data);
            synchronized(this){
                for(FmSensoListener l : mListener)
                    l.onSensorAttach(new ArrayList<>(added));
            }
            sensorManager.addSensor(added);
        } 
    }
   

    public void stop() {
        running = false;
        /*
         writer.terminate();
          
         thread.join();
         writer = null;
         thread = null;*/
        writer.cancel();
        synchronized (this) {
            for (FmSensoListener l : mListener) {
                l.onClose();
            }
        }

        stopReporter();
        sensorManager.clear();
        writer = null;

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
                writeCommand(Commands.EXTERNAL_SENSORS);
                writeCommand(Commands.INTERNAL_SENSORS);
            }
        }, 0, interval);
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
}




