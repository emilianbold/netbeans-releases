/*
 * Copyright (c) 2010, Oracle.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Oracle nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
