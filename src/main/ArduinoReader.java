package main;

import jssc.*;

import java.util.ArrayList;
import java.util.Stack;

public class ArduinoReader implements SerialPortEventListener {
    private SerialPort serialPort;
    private Stack<String> data = new Stack<>();
    private ArrayList<String> output = new ArrayList<>();
    private StringBuilder message = new StringBuilder();
    String out2 = "";

    public synchronized void serialEvent (SerialPortEvent event){
        if (event.isRXCHAR()) {
            //System.out.println("rxchar");
            try {
                String out = serialPort.readString();
                if (out != null) {
                    if (out.indexOf("\n") > 0) {
                        out2 += out;
                        this.data.push(out2.trim());
                        //System.out.println(out2.trim());
                        out2 = "";
                    } else {
                        out2 += out;
                    }
                }
            } catch (SerialPortException ex) {
                System.out.println(ex);
            }
        }
    }

    void initialize() {
        String[] portNames = SerialPortList.getPortNames();
        for (String port : portNames) {
            serialPort = new SerialPort(port);
            try {
                serialPort.openPort();
                if (serialPort.isOpened()) {
                    System.out.println(serialPort.isOpened());
                    serialPort.setParams(115200, 8, 1, 0);
                    int mask = SerialPort.MASK_RXCHAR;
                    //Set the prepared mask
                    serialPort.setEventsMask(mask);
                    //Add an interface through which we will receive information about events
                    serialPort.addEventListener(this);
                }
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }

    public Stack<String> getData() {
        return data;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public static void main(String[] args) {
        ArduinoReader main = new ArduinoReader();
        main.initialize();
        Thread t = new Thread() {
            public void run() {
                //the following line will keep this app alive for 1000 seconds,
                //waiting for events to occur and responding to them (printing incoming messages to console).
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ie) {
                }
            }
        };
        t.start();
        //System.out.println("Started");
    }
}
