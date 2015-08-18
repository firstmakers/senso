/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.device;

import com.sun.javafx.PlatformUtil;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.hid4java.HidDevice;
import org.hid4java.HidException;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.event.HidServicesEvent;

/**
 *
 * @author Edison Delgado
 */
public abstract class FmDevice implements HidServicesListener {

    private short vendorID;
    private short productID;
    private static final int PACKET_LENGHT = 64;
    private static final byte REPORT_ID = (byte) 0;
    protected HidServices hidServices;
    protected ArrayList<HidDevice> devices;
    protected HidDevice currentDevice;
    //Reporter reporter; 
    //Thread thread;
    private boolean connected = false;
    Timer timer;

    public FmDevice() throws HidException {
        hidServices = HidManager.getHidServices();
        hidServices.addHidServicesListener(this);
        devices = new ArrayList<>();
    }

    public void initialize() {
        getCompatibleDevices(productID, vendorID);
        if (devices.size() > 0) {
            connected = true;
            currentDevice = devices.get(0);
            deviceAttached(currentDevice);

        }
    }

    @Override
    public void hidDeviceAttached(HidServicesEvent event) {
        HidDevice hidDevice = event.getHidDevice();
        if (hidDevice.getProductId() == productID
                && hidDevice.getVendorId() == vendorID) {
            devices.add(hidDevice);
            if (currentDevice == null) {
                connected = true;
                currentDevice = hidDevice;
                deviceAttached(currentDevice);

            }
        }
    }

    @Override
    public void hidDeviceDetached(HidServicesEvent event) {
        HidDevice hidDevice = event.getHidDevice();
        if (hidDevice.equals(currentDevice)) {
            connected = false;
            deviceDetached(hidDevice);
            currentDevice = null;
        } else {
            System.out.println("Other devices is detached " + hidDevice);
        }
        devices.remove(hidDevice);
    }

    @Override
    public void hidFailure(HidServicesEvent event) {
        System.out.println("HID FAILURE " + event.toString());
    }
    /*
     */

    public int write(byte command) {
        byte[] data;
        if (connected) {
            if (PlatformUtil.isWindows()) {
                data = new byte[PACKET_LENGHT];
                data[0] = REPORT_ID;
                data[1] = command;
            } else {
                data = new byte[PACKET_LENGHT];
                data[0] = command;
            }
            return currentDevice.write(data, PACKET_LENGHT, REPORT_ID);
        } else {
            return 0;
        }
    }

    protected void reportData(byte[] data) {
    }

    protected void deviceDetached(HidDevice device) {
    }

    protected void deviceAttached(HidDevice device) {
    }

    public short getVendorID() {
        return vendorID;
    }

    public void setVendorID(short vendorID) {
        this.vendorID = vendorID;
    }

    public short getProductID() {
        return productID;
    }

    public void setProductID(short productID) {
        this.productID = productID;
    }

    public void startReporter() {
        timer = new Timer();
        report();
    }

    public void stopReporter() {
        if(timer != null)
            timer.cancel();
        timer = null;
    }

    /*
     Get all devices by product and vendor ID 
     *@param pid this is the product ID (device)
     *@param vid this is the vender ID (device)
     */
    public ArrayList<HidDevice> getCompatibleDevices(short pid, short vid) {
        hidServices.getAttachedHidDevices().stream().filter((hidDevice) -> 
                (hidDevice.getProductId() == pid && hidDevice.getVendorId() == vid))
                .forEach((hidDevice) -> {
                    devices.add(hidDevice);
                });
        return this.devices;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void report() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                byte inBuffer[] = new byte[PACKET_LENGHT];
                int val = currentDevice.read(inBuffer);
                switch (val) {
                    case -1:
                        //System.err.println(currentDevice.getLastErrorMessage()); always throw null
                        break;
                    case 0:
                        break;
                    default:
                        if (inBuffer != null) {
                            reportData(inBuffer);
                        }
                        break;
                }
            }
        }, 0, 100);
    }


    /*public class Reporter implements Runnable {
     boolean running = true;
        
     public void terminate(){
     running = false;
     }

     @Override
     public void run() {
     try {
     while (currentDevice.isOpen() && running) {               
     byte inBuffer[] = new byte[PACKET_LENGHT];
     int val = currentDevice.read(inBuffer);
     switch (val) {
     case -1:
     //System.err.println(currentDevice.getLastErrorMessage()); always throw null
     break;
     case 0:
     break;
     default:
     if(inBuffer != null)
     reportData(inBuffer);
     break;
     }
     }
            
     } catch (Exception e) {
     Logger.getLogger(FmDevice.class.getName()).log(Level.INFO,e.getMessage(), e);
     }
       
     }
     }*/
}
