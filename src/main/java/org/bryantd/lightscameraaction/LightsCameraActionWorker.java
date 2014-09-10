/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bryantd.lightscameraaction;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class LightsCameraActionWorker extends SwingWorker<Integer, String> {
    private final Camera camera_;
    private final Lights lights_;
    private final JTextArea jTextAreaStatus_;
    private final Integer lightsToImageDelayMS_;
    private final String setWLCommand_;
    private final ArrayList<DateTime> imageJobSchedule_;
    private final Boolean requireRunningSchedule_;
    private final Iterator imageJobScheduleIterator_;
    private final Timer timer_ = new Timer(true);    
    
    public LightsCameraActionWorker(final Camera camera, final Lights lights, final JTextArea jTextAreaStatus, final ArrayList<DateTime> imageJobSchedule, final Integer lightsToImageDelay, final String setWLcommand, final Boolean requireRunningSchedule) {
        this.camera_ = camera;
        this.lights_ = lights;
        this.jTextAreaStatus_ = jTextAreaStatus;
        this.lightsToImageDelayMS_ = lightsToImageDelay;
        this.setWLCommand_ = setWLcommand;
        this.imageJobSchedule_ = imageJobSchedule;
        this.requireRunningSchedule_ = requireRunningSchedule;
        imageJobScheduleIterator_ = imageJobSchedule_.iterator();
    }
    
    private static void failIfInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Inturrupted while running image acquisition job.");
        }
    }
    
    @Override
    protected Integer doInBackground() throws Exception {
        final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd '-' HH:mm:ss.SSSS");
        String fileName;
        while(imageJobScheduleIterator_.hasNext()) {
            LightsCameraActionWorker.failIfInterrupted();
            DateTime nextScheduledEvent = (DateTime)imageJobScheduleIterator_.next();
            process(Arrays.asList(Utilities.timeStamp("Next snapshot scheduled for " + dtf.print(nextScheduledEvent)).toString()));
            if(nextScheduledEvent.isAfterNow()) {
                waitUntil(nextScheduledEvent);
                process(Arrays.asList(Utilities.timeStamp("Begining scheduled image acquisition.").toString()));
                
                Boolean okToExecuteJob = true;
                if(requireRunningSchedule_) {
                    process(Arrays.asList(Utilities.timeStamp("Checking lights for running schedule:").toString()));
                    try {
                        okToExecuteJob = lights_.issueCommands(Arrays.asList(LightsTask.GETSCHEDULERUNNING), null, null);
                    } catch (InterruptedException e) {
                        throw(e);
                    } catch(Exception e) {
                        okToExecuteJob = false;
                        process(Arrays.asList(Utilities.timeStamp("Error: " + e.getMessage()).toString()));
                        process(Arrays.asList(Utilities.timeStamp("Error type: " + e.getCause()).toString()));
                        process(Arrays.asList(Utilities.timeStamp("Could not issue lights command. Skipping this scheduled job.").toString()));
                    }
                    if(okToExecuteJob) {
                        process(Arrays.asList(Utilities.timeStamp("Schedule is running on lights, proceeding.").toString()));
                    } else {
                        process(Arrays.asList(Utilities.timeStamp("Schedule is not running on lights! Skipping job.").toString()));
                        okToExecuteJob = false;
                    }
                }

                if(okToExecuteJob) {
                    boolean success = true;
                    fileName = nextScheduledEvent.toLocalDateTime().toString().replace(':', '-');
                    process(Arrays.asList(Utilities.timeStamp("Executing image acquisition routine.").toString()));
                    
                    lights_.issueCommands(Arrays.asList(LightsTask.SCHEDULESTOP, LightsTask.SETWL), null, setWLCommand_);
                    DateTime takeImageEvent = DateTime.now().plusMillis(lightsToImageDelayMS_);
                    waitUntil(takeImageEvent);
                    
                    try {
                        success = camera_.snapImage(fileName);
                        failIfInterrupted();
                    } catch (Exception e) {
                        process(Arrays.asList(Utilities.timeStamp("Error snapping image: " + e.getMessage()).toString()));
                        process(Arrays.asList(Utilities.timeStamp("Could not issue camera command. Skipping this scheduled job.").toString()));
                    }
                    if(success) { process(Arrays.asList(Utilities.timeStamp("Successfully acquired image.").toString())); }
                    else { process(Arrays.asList(Utilities.timeStamp("Something went wrong with this job.").toString())); }
                    
                    lights_.issueCommands(Arrays.asList(LightsTask.SCHEDULESTART), null, null);
                }
            } else {
                process(Arrays.asList(Utilities.timeStamp("Scheduled job is in the past. Skipping.").toString()));
                System.out.println("event skipped");
            }            
        }
        process(Arrays.asList(Utilities.timeStamp("Done processing full schedule.").toString()));
        return 0;
    }
    
    private void waitUntil(DateTime scheduledEvent) throws InterruptedException {
        final Object o = new Object();
        TimerTask tt = new TimerTask() {
            public void run() {
                synchronized (o) {
                    o.notify();
                }
            }
        };
        Date date = scheduledEvent.toDate();
        timer_.schedule(tt, date);
        synchronized(o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                LightsCameraActionWorker.failIfInterrupted();
                throw(e);
            }
        }
    }
 
    @Override
    protected void process(final List<String> chunks) {
        for (final String string : chunks) {
            jTextAreaStatus_.append(string);
            jTextAreaStatus_.append("\n");
        }
    }
}
