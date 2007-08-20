/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package example.mmademo; 
 
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.IOException; 
import javax.microedition.media.*;

/**
 * Demonstrates how to properly respond to pauseApp()
 */ 
public class PausingAudioTest extends MIDlet implements CommandListener {
	
    private Command behaveCommand = new Command("Behave", Command.SCREEN, 2);
    private Command misbehaveCommand = 
        new Command("Misbehave", Command.SCREEN, 2);
    private Command exitCommand = new Command("Exit", Command.BACK, 1);
    
    private Form screen = null;

    private Player p = null;
    
    private boolean isWellBehaved = true;
    
    public PausingAudioTest() {
    }

    public void startApp() {
        if (screen == null) {
            screen = new Form("Pausing Audio Test");
            screen.addCommand(exitCommand);
            screen.setCommandListener(this);
            Display.getDisplay(this).setCurrent(screen);
        }
        setupScreen();
        if (p == null) {
            String url = getAppProperty("PauseAudioURL");
            try {
                p = Manager.createPlayer(url);
            } catch (IOException ioe) {
                screen.append(new StringItem("Could not open URL:", url));
                screen.append(new StringItem("Exception:", ioe.toString()));
                return;
            } catch (MediaException me) {
                screen.append(new StringItem("Manager.createPlayer(" + url +
                    " threw:", me.toString()));
                return;
            }
            p.setLoopCount(-1);            
        }
        try {
            p.start();
        } catch (MediaException me) {
            screen.append(new StringItem("Player.start() threw:", 
                me.toString()));
        }          
    }

    private void setupScreen() {
        screen.deleteAll();
        screen.removeCommand(behaveCommand);
        screen.removeCommand(misbehaveCommand);
        
        if (isWellBehaved) {
            screen.addCommand(misbehaveCommand);            
            screen.append(new StringItem("Current State:", "Well-Behaved"));
        } else {
            screen.addCommand(behaveCommand);
            screen.append(new StringItem("Current State:", "Not Well-Behaved"));        
        }                
    }
    
    public void pauseApp() {
        if (isWellBehaved && p != null) {
            try {
                p.stop();
            } catch (MediaException me) {
                screen.append(new StringItem("Player.stop() threw:", 
                    me.toString()));
            }
        }
    }

    public void destroyApp(boolean unconditional) {
        if (p != null) {
            p.close();
        }
    }
    
    public void commandAction(Command c, Displayable s) {
        if (c == behaveCommand || c == misbehaveCommand) {
            isWellBehaved = !isWellBehaved;
            setupScreen();
        } else if (c == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        }
    }
}
