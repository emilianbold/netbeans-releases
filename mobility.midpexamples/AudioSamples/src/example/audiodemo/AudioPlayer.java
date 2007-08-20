/*
 *
 * Copyright (c) 2007, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package example.audiodemo;

import java.io.*;

import java.util.*;

import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.midlet.*;


/**
 * This is a demo midlet to show the basic audio functionalities, to
 * play wave file, tone, tone sequence from http, resource jar file
 * and record store.
 *
 * @version
 *
 */
public class AudioPlayer extends MIDlet implements CommandListener {
    private static PlayerCanvas playerGUI = null;
    private static List theList;
    private static Vector urls;
    private Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private Command playCommand = new Command("Play", Command.ITEM, 1);
    private Display display;

    // pause/resume support
    private boolean restartOnResume = false;

    public AudioPlayer() {
        super();
        display = Display.getDisplay(this);
        initPlayList();
        display.setCurrent(theList);
    }

    public static List getList() {
        return theList;
    }

    /**
     * Called when this MIDlet is started for the first time,
     * or when it returns from paused mode.
     * If a player is visible, and it was playing
     * when the MIDlet was paused, call its playSound method.
     */
    public void startApp() {
        if ((playerGUI != null) && restartOnResume) {
            playerGUI.playSound();
        }

        restartOnResume = false;
    }

    /**
     * Called when this MIDlet is paused.
     * If the player GUI is visible, call its pauseSound method.
     * For consistency across different VM's
     * it's a good idea to stop the player if it's currently
     * playing.
     */
    public void pauseApp() {
        restartOnResume = ((playerGUI != null) && playerGUI.isPlaying());

        if (restartOnResume) {
            playerGUI.pauseSound();
        }
    }

    /**
     * Destroy must cleanup everything not handled
     * by the garbage collector.
     */
    public void destroyApp(boolean unconditional) {
        if (playerGUI != null) {
            playerGUI.stopSound();
            playerGUI = null;
        }

        display.setCurrent(null);
    }

    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        } else if (((s == theList) && (c == List.SELECT_COMMAND)) || (c == playCommand)) {
            int i = theList.getSelectedIndex();

            if (i == 0) { // Simple tone

                try {
                    Manager.playTone(60, 200, 90);
                } catch (MediaException ex) {
                    System.out.println("can't play tone");
                }
            } else if (i > 0) {
                if (playerGUI == null) {
                    playerGUI = new PlayerCanvas(display);
                } else {
                    playerGUI.stopSound();
                }

                playerGUI.setParam((String)urls.elementAt(i));
                playerGUI.playSound();
                display.setCurrent(playerGUI);
            }
        }
    }

    /**
     * load the play list from thd jad file
     *
     **/
    private void initPlayList() {
        urls = new Vector();

        theList = new List("MIDP Audio Player", Choice.IMPLICIT);

        for (int n = 1; n < 32; n++) {
            String nthURL = "PlayerURL-" + n;
            String url = getAppProperty(nthURL);

            if ((url == null) || (url.length() == 0)) {
                break;
            }

            String nthTitle = "PlayerTitle-" + n;
            String title = getAppProperty(nthTitle);

            if ((title == null) || (title.length() == 0)) {
                title = url;
            }

            urls.addElement(url);
            theList.append(title, null);
        }

        theList.addCommand(exitCommand);
        theList.addCommand(playCommand);
        theList.setCommandListener(this);
    }
}
