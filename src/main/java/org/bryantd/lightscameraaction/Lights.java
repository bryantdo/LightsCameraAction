/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bryantd.lightscameraaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import static org.bryantd.lightscameraaction.LightsTask.GETSCHEDULERUNNING;
import static org.bryantd.lightscameraaction.LightsTask.SETWL;
import static org.bryantd.lightscameraaction.LightsTask.SCHEDULESTART;
import static org.bryantd.lightscameraaction.LightsTask.SCHEDULESTOP;
import static org.bryantd.lightscameraaction.LightsTask.TEST;

public class Lights {
    private static String ipAddress_;
    private static Integer port_;
    private static Socket socket_;
    private static PrintWriter streamToLights_;
    private static BufferedReader streamFromLights_;
    private static Integer timeOutDelay_;
        
    public Lights(String ipAddress, Integer port) {
        Lights.ipAddress_ = ipAddress;
        Lights.port_ = port;
        Lights.socket_ = new Socket();
        Lights.timeOutDelay_ = 2500;
    }
    
    private static Boolean connectToLights() throws Exception {
        Boolean success = true;
        try {
            socket_ = new Socket();
            socket_.connect(new InetSocketAddress(ipAddress_, port_), timeOutDelay_);
            streamToLights_ = new PrintWriter(socket_.getOutputStream(), true);
            streamFromLights_ = new BufferedReader(new InputStreamReader(socket_.getInputStream()));
        } catch(Exception e) {
            success = false;
        }
        return success;
    }
    
    // This is the main lights command passing method. If you want to read the lights' reply, pass a stringbuilder as second arg, 
    // otherwise pass null. If you want to send a set wavelengths command (setall 0 or etc.) pass the command as the third arg,
    // otherwise pass null.
    public static Boolean issueCommands(List<LightsTask> tasks, StringBuilder lightsReplyReturn, String setWLCommand) throws Exception {
        Boolean success = false;
        Integer numConnectionTries = 5, connectionSleep = 1000;
        Boolean canConnect = connectToLights();
        while(!canConnect) {
            if(numConnectionTries <= 0) { throw(new IOException("Unable to connect to lights over 5 attempts.")); }
            Thread.sleep(connectionSleep);
            numConnectionTries--;
            canConnect = connectToLights();
        }
        
        for(LightsTask task : tasks) {
            String toLightsCommand = "", okResultToken = "", failResultToken = "";
            switch(task) {
                case TEST:
                    toLightsCommand = "hello";
                    okResultToken = "OK";
                    failResultToken = "Error";
                    break;
                case GETWL:
                    toLightsCommand = "getwl";
                    okResultToken = "OK";
                    failResultToken = "Error";
                    break;
                case GETSCHEDULERUNNING:
                    toLightsCommand = "getScheduleRunning";
                    okResultToken = "OK\t1";
                    failResultToken = "OK\t0";
                    break;
                case SCHEDULESTART:
                    toLightsCommand = "startSchedule";
                    okResultToken = "OK";
                    failResultToken = "Error";
                    break;
                case SCHEDULESTOP:
                    toLightsCommand = "stopSchedule";
                    okResultToken = "OK";
                    failResultToken = "Error";
                    break;
                case SETWL:                    
                    if(setWLCommand == null) {
                        toLightsCommand = "setAll 0";
                    } else {
                        toLightsCommand = setWLCommand;
                    }
                    okResultToken = "OK";
                    failResultToken = "Error";
                    break;
            }

            Boolean done = false;
            try {
                streamToLights_.println(toLightsCommand);
                String fromLightsLine;
                while(!done && (fromLightsLine = streamFromLights_.readLine()) != null) {
                    if(fromLightsLine.contains(okResultToken)) {
                        if(lightsReplyReturn != null) {
                            lightsReplyReturn.append(fromLightsLine);
                        }
                        done = true;
                        success = true;
                    } else if(fromLightsLine.contains(failResultToken)) {
                        done = true;
                        success = false;
                    }
                    System.out.println(fromLightsLine);
                }
            } catch (Exception e) {
                System.out.println("Error in lights: " + e.getMessage());
                throw(e);
            }
        }        
        streamToLights_.println("bye");
        socket_.close();
        return success;
        
    }
    
    public String getIpAddress_() {
        return ipAddress_;
    }

    public void setIpAddress_(String ipAddress_) {
        this.ipAddress_ = ipAddress_;
    }

    public Integer getPort_() {
        return port_;
    }

    public void setPort_(Integer port_) {
        this.port_ = port_;
    }
}
