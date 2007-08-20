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

import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.midlet.*;


public class MixTest extends MIDlet implements CommandListener, Runnable {
    private static final String[] mcases = { "Tone+Wav", "Tone+ToneSeq", "ToneSeq+Wav" };
    static String wavUrl;
    private static MixCanvas soundObj = null;
    private static List theList;
    private static MixTest instance = null;
    private Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private Command playCommand = new Command("Play", Command.ITEM, 1);
    private Display display;

    // if this MIDlet's startApp method is started
    // for the first time
    private boolean firstTime = true;

    // pause/resume support
    private boolean restartOnResume = false;

    public MixTest() {
        instance = this;
        display = Display.getDisplay(this);
        theList = new List("Lists", Choice.IMPLICIT);

        for (int i = 0; i < mcases.length; i++) {
            theList.append(mcases[i], null);
        }

        wavUrl = getAppProperty("MixTestURL");
        theList.addCommand(playCommand);
        theList.addCommand(exitCommand);
        theList.setCommandListener(this);
        soundObj = new MixCanvas(display);
    }

    public static MixTest getInstance() {
        return instance;
    }

    public static List getList() {
        return theList;
    }

    /**
     * Called when this MIDlet is started for the first time,
     * or when the MIDlet returns from paused mode.
     *
     * If it is the first time, display the menu list.
     *
     * Otherwise, if music was playing when the MIDlet
     * was paused, call the demo's playSound method.
     */
    public void startApp() {
        if (firstTime) {
            display.setCurrent(theList);
            firstTime = false;
        } else {
            if ((soundObj != null) && restartOnResume) {
                soundObj.playSound();
            }

            restartOnResume = false;
        }
    }

    /**
     * Called when this MIDlet is paused.
     * Pause the thread.
     * If the demo is playing, call its pause method.
     * For consistency across different VM's,
     * it's a good idea to stop the player if
     * it's currently playing.
     */
    public void pauseApp() {
        restartOnResume = ((soundObj != null) && soundObj.isPlaying());

        if (restartOnResume) {
            soundObj.pauseSound();
        }
    }

    /**
     * Destroy must cleanup everything not handled
     * by the garbage collector.
     */
    public void destroyApp(boolean unconditional) {
        if (soundObj != null) {
            soundObj.stopSound();
        }

        soundObj = null;
    }

    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        } else if (((s == theList) && (c == List.SELECT_COMMAND)) || (c == playCommand)) {
            int i = theList.getSelectedIndex();
            soundObj.setIndex(i);
            display.setCurrent(soundObj);
            soundObj.serviceRepaints();

            // method playSound() should not be invoked on GUI thread. Manager.createPlayer()
            // will potentially invoke a blocking I/O. This is not good
            // practice recommended by MIDP programming style. So here we
            // will create the Player in a separate thread.
            new Thread(this).start();
        }
    }

    public void run() {
        if (soundObj != null) {
            soundObj.playSound();
        }
    }
}
