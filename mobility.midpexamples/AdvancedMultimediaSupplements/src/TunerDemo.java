/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

import java.io.IOException;
import java.util.Date;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.amms.*;
import javax.microedition.amms.control.tuner.*;


public class TunerDemo extends MIDlet implements CommandListener, PlayerListener {
    private boolean initDone;
    private Player radioPlayer;
    private TunerControl tunerControl;
    private RDSControl rdsControl;
    private int freq;
    private String modulation;

    private Command cmdExit = new Command("Exit", Command.EXIT, 0);
    private Command cmdSearchUp = new Command("Search Up", Command.SCREEN, 1);
    private Command cmdSearchDown = new Command("Search Down", Command.SCREEN, 1);
    private Command cmdSwitchFM = new Command("Switch to FM", Command.SCREEN, 1);
    private Command cmdSwitchAM = new Command("Switch to AM", Command.SCREEN, 1);

    private TextBox textBox;
    private Display display;
    
    private Thread toRun;
    private String toSay;
    
    final static private String msgBusy = "Sorry, busy with previous request ...";
    final static private String msgSearchUp = "Searching up";
    final static private String msgSearchDown = "Searching down";
    final static private String msgSwitchToFM = "Switching to FM";
    final static private String msgSwitchToAM = "Switching to AM";
            
    private final static int MAX_TEXT_SIZE = 1024;

    public TunerDemo() {
        freq = 910000; //91.0 MHz
        modulation = TunerControl.MODULATION_FM;
        toSay = null;
        toRun = null;

        textBox = new TextBox("Radio Tuner", "starting...", MAX_TEXT_SIZE, TextField.ANY);

        textBox.addCommand(cmdSearchUp);
        textBox.addCommand(cmdSearchDown);
        textBox.addCommand(cmdSwitchFM);
        textBox.addCommand(cmdSwitchAM);

        textBox.addCommand(cmdExit);

        textBox.setCommandListener(this);

        display = Display.getDisplay(this);
        display.setCurrent(textBox);
    }

    public void startApp() {
        if (initDone) {
            startPlayer();
        } else {
            initializeRadio();
            initDone = true;
        }
    }

    public void pauseApp() {
        stopPlayer();
    }

    public void destroyApp(boolean unconditional) {
        closePlayer();
    }

    public void commandAction(Command c, Displayable s) {
        if (c == cmdExit) {
            // this method exists in CLDC 1.1 only
            //if (toRun != null)
            //    toRun.interrupt();
            closePlayer();
            notifyDestroyed();
        }
        else {
            if (toRun != null) {
                textBox.setString(msgBusy);
            } else {
                
                if (c == cmdSearchUp) {
                    toRun = new SearchUpThread();
                    toSay = msgSearchUp;
                } else if (c == cmdSearchDown) {
                    toRun = new SearchDownThread();
                    toSay = msgSearchDown;
                } else if (c == cmdSwitchFM) {
                    toRun = new SwitchToFMThread();
                    toSay = msgSwitchToFM;
                } else if (c == cmdSwitchAM) {
                    toRun = new SwitchToAMThread();
                    toSay = msgSwitchToAM;
                } else {
                    toRun = null;
                    toSay = null;
                }
                startAction();
            }
        }
    }
    
    private void startAction() {
        display.callSerially(new Runnable() {
            public void run() {
                textBox.setString(TunerDemo.this.toSay + 
                        " from \n" + 
                        (int) (TunerDemo.this.freq / 10) + 
                        "kHz...");
                TunerDemo.this.toRun.start();
            }
        });
    }

    private void startPlayer() {
        if (radioPlayer != null) {
            try {
                radioPlayer.start();
            } catch (MediaException e) {
            }
        }
    }

    private void stopPlayer() {
        if (radioPlayer != null) {
            try {
                radioPlayer.stop();
            } catch (MediaException e) {
            }
        }
    }

    private void closePlayer() {
        if (radioPlayer != null) {
            radioPlayer.close();
            radioPlayer = null;
        }
    }

    private void switchToFM() {
        if (tunerControl != null) {
            modulation =  TunerControl.MODULATION_FM;

            try {
                freq = tunerControl.getMinFreq(modulation);
                freq = tunerControl.seek(freq, modulation, true);
            } catch (MediaException e) {
                System.out.println("Failed to switch to FM station: " + e.getMessage());
            }
        }
    }

    private void switchToAM() {
        if (tunerControl != null) {
            modulation =  TunerControl.MODULATION_AM;

            try {
                freq = tunerControl.getMinFreq(modulation);
                freq = tunerControl.seek(freq, modulation, true);
            } catch (MediaException e) {
                System.out.println("Failed to switch to AM station: " + e.getMessage());
            }
        }
    }

    private void searchUp() {
        if (tunerControl != null) {
            try {
                freq = tunerControl.seek(freq + 1, modulation, true);
            } catch (Exception e) {
                System.out.println("Failed to search up: " + e.getMessage());
            }
        }
    }

    private void searchDown() {
        if (tunerControl != null) {
            try {
                freq = tunerControl.seek(freq - 1, modulation, false);
            } catch (Exception e) {
                System.out.println("Failed to search down: " + e.getMessage());
            }
        }
    }

    /**
     * Initializes and switched on the radio.
     */
    private void initializeRadio() {
        try {
            radioPlayer = Manager.createPlayer("capture://radio");
            radioPlayer.realize();

            radioPlayer.addPlayerListener(this);

            tunerControl = (TunerControl)
            radioPlayer.getControl("javax.microedition.amms.control.tuner.TunerControl");

            tunerControl.setStereoMode(TunerControl.STEREO);

            // Then, let's get the RDSControl:
            rdsControl = (RDSControl)
            radioPlayer.getControl("javax.microedition.amms.control.tuner.RDSControl");

            if (rdsControl != null) {
            //Let's turn on the automatic switching
            //to possible traffic announcements:
                    try {
                        rdsControl.setAutomaticTA(true); 
                    } catch (MediaException mex) {
                        // ignore if feature is not supported ...
                    }
            }

            radioPlayer.start();

            // Now that the radio is on let's first find a radio station
            // by seeking upwards from 91.0 MHz:
            commandAction(cmdSearchUp, textBox);

        } catch (MediaException me) {
            System.out.println("Failed to initialize radio: ");
            me.printStackTrace();
        } catch (IOException ioe) {
            System.out.println("Failed to initialize radio: ");
            ioe.printStackTrace();
        }
    }

    public void playerUpdate(Player player,
			     String event,
			     Object eventData) {
        if (event == RDSControl.RDS_NEW_DATA) {
            updateRDSDisplay();
        } 
    }

    /**
     * Shows some RDS data.
     */ 
    private void updateRDSDisplay() {
        if (rdsControl != null) {      
            String channelName = rdsControl.getPS();
            String radioText = rdsControl.getRT();
            String programmeType = rdsControl.getPTYString(true);
            Date date = rdsControl.getCT();	    
            String unit = "kHz";
            int val = (int) (freq / 10);

            String info = "station: " + channelName + ", " + radioText + "\n" +
                      "frequency: " + val + " " + unit + "\n" +
                      "modulation: " + modulation + "\n" +
                      "genre: " + programmeType + "\n" +
                      "local time: " + date;

            textBox.setString(info);
        }
    }
    
    class SearchUpThread extends Thread {
        public void run() {
            TunerDemo.this.searchUp();
            TunerDemo.this.toRun = null;
        }
    }

    class SearchDownThread extends Thread {
        public void run() {
            TunerDemo.this.searchDown();
            TunerDemo.this.toRun = null;
        }
    }

    class SwitchToFMThread extends Thread {
        public void run() {
            TunerDemo.this.switchToFM();
            TunerDemo.this.toRun = null;
        }
    }

    class SwitchToAMThread extends Thread {
        public void run() {
            TunerDemo.this.switchToAM();
            TunerDemo.this.toRun = null;
        }
    }
}
