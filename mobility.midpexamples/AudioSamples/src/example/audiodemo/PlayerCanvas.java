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

import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;


/**
 * The component for AudioPlayer.
 * It will create a player for the selected url, play and display
 * some properties of the player.
 *
 * Use star key to increase the volume, pound key to decrease the volume
 *
 **/
public class PlayerCanvas extends Canvas implements Runnable, CommandListener {
    private static final String RECORD_STORE_NAME = "adrms";
    private Player player;
    private Thread dThread;
    private Object dThreadLock = new Object();
    private Object pauseLock = new Object();
    private boolean interrupted;
    private boolean paused;
    private Image logo = null;
    private Display parentDisplay;
    private Command backCommand = new Command("Back", Command.BACK, 1);
    private Command playCommand = new Command("Play", Command.ITEM, 1);
    private Command pauseCommand = new Command("Pause", Command.ITEM, 10);
    private String title;
    private String url;
    private String mtime;

    public PlayerCanvas(Display parentDisplay) {
        super();
        this.parentDisplay = parentDisplay;
        initialize();
    }

    private void initialize() {
        this.addCommand(backCommand);
        this.addCommand(pauseCommand);

        setCommandListener(this);

        try {
            logo = Image.createImage("/icons/Duke.png");
        } catch (Exception ex) {
            logo = null;
        }

        if (logo == null) {
            System.out.println("can not load Duke.png");
        }
    }

    /*
     * simple implementation, not reflected actual state
     * of player.
     */
    public void commandAction(Command c, Displayable s) {
        if (s == this) {
            if (c == backCommand) {
                stopSound();
                removeCommand(playCommand);
                addCommand(pauseCommand);
                parentDisplay.setCurrent(AudioPlayer.getList());
            } else if (c == playCommand) {
                playSound();
                removeCommand(playCommand);
                addCommand(pauseCommand);
            } else if (c == pauseCommand) {
                pauseSound();
                removeCommand(pauseCommand);
                addCommand(playCommand);
            }
        }
    }

    public void setParam(String url) {
        this.url = url;

        int idx = url.lastIndexOf('/');
        title = url.substring(idx + 1);
    }

    public void playSound() {
        if ((title == null) || (url == null)) {
            return;
        }

        // player was paused
        if (player != null) {
            // wake up paused thread
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notify();
            }

            try {
                player.start();
            } catch (MediaException me) {
                me.printStackTrace();
            }

            return;
        }

        // start new player
        synchronized (dThreadLock) {
            stopSound();
            interrupted = false;
            paused = false;
            mtime = "";
            dThread = new Thread(this);
            dThread.start();
        }
    }

    public void stopSound() {
        synchronized (dThreadLock) {
            try {
                interrupted = true;

                // wake up thread if it is paused
                synchronized (pauseLock) {
                    pauseLock.notify();
                }

                if (dThread != null) {
                    dThreadLock.wait();
                }
            } catch (InterruptedException ie) {
                // nothing
            }
        }
    }

    void pauseSound() {
        try {
            if (player != null) {
                // pause player
                player.stop();
                paused = true;
            }
        } catch (MediaException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isPlaying() {
        return (player != null) && (player.getState() >= Player.STARTED);
    }

    private static String guessContentType(String url)
        throws Exception {
        String ctype;

        // some simple test for the content type
        if (url.endsWith("wav")) {
            ctype = "audio/x-wav";
        } else if (url.endsWith("jts")) {
            ctype = "audio/x-tone-seq";
        } else if (url.endsWith("mid")) {
            ctype = "audio/midi";
        } else {
            throw new Exception("Cannot guess content type from URL: " + url);
        }

        return ctype;
    }

    void createPlayer() {
        try {
            if (url.startsWith("http:")) {
                player = Manager.createPlayer(url);
            } else if (url.startsWith("resource")) {
                int idx = url.indexOf(':');
                String loc = url.substring(idx + 1);
                InputStream is = getClass().getResourceAsStream(loc);
                String ctype = guessContentType(url);
                player = Manager.createPlayer(is, ctype);
            } else if (url.startsWith("rms:")) {
                boolean created = false;
                InputStream stream = null;

                while (stream == null) {
                    try {
                        RecordStore rs = RecordStore.openRecordStore(RECORD_STORE_NAME, false);
                        byte[] adata = rs.getRecord(1);
                        rs.closeRecordStore();
                        stream = new ByteArrayInputStream(adata);

                        break; // exit while loop
                    } catch (Exception ex) {
                        // record store not found
                    }

                    if (created) {
                        // already tried to create record store!
                        throw new Exception("Could not create and open record store!");
                    }

                    created = true;
                    createMyRecordStore(url, RECORD_STORE_NAME);
                }

                String ctype = guessContentType(url);
                player = Manager.createPlayer(stream, ctype);
            }

            player.setLoopCount(-1);
        } catch (Exception ex) {
            if (player != null) {
                player.close();
                player = null;
            }

            Alert alert = new Alert("Warning", "Cannot create player", null, null);
            alert.setTimeout(1000);
            parentDisplay.setCurrent(alert);
        }
    }

    /**
     * Create a record store for the given url
     */
    private void createMyRecordStore(String url, String name) {
        try {
            int idx = url.indexOf(':');
            String loc = url.substring(idx + 1);
            InputStream is = getClass().getResourceAsStream(loc);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] tmp = new byte[1024];
            int nread;

            while ((nread = is.read(tmp, 0, 1024)) > 0) {
                baos.write(tmp, 0, nread);
            }

            byte[] data = baos.toByteArray();

            is.close();

            // create a RecordStore
            RecordStore rs = RecordStore.openRecordStore(name, true);
            rs.addRecord(data, 0, data.length);
            rs.closeRecordStore();
            System.out.println("created record store '" + name + "' with contents of " + loc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        g.setColor(0);
        g.fillRect(0, 0, w, h);

        g.setColor(0xFF7f00);
        g.drawString("Audio Player", w / 2, 8, Graphics.TOP | Graphics.HCENTER);

        if (logo != null) {
            g.drawImage(logo, w / 2, 30, Graphics.TOP | Graphics.HCENTER);
        }

        g.setColor(0xFF7f00);
        g.drawString("Audio Player", w / 2, 8, Graphics.TOP | Graphics.HCENTER);

        g.drawString(title, w / 2, 84, Graphics.TOP | Graphics.HCENTER);

        g.drawString(mtime, 0, 150, Graphics.TOP | Graphics.LEFT);
    }

    public void run() {
        /*
         * method playSound() runs on GUI thread.
         * Manager.createPlayer() will potentially invoke a blocking
         * I/O. This is not the good practice recommended by MIDP
         * programming style. So here we will create the
         * Player in a separate thread.
         */
        createPlayer();

        if (player == null) {
            // can't create player
            synchronized (dThreadLock) {
                dThread = null;
                dThreadLock.notify();

                return;
            }
        }

        try {
            player.realize();

            long dur = player.getDuration();

            if (dur != -1) {
                title = title + " [" + timeFM(dur) + "]";
            }

            player.start();
        } catch (Exception ex) {
        }

        // mtime update loop
        while (!interrupted) {
            try {
                mtime = timeFM(player.getMediaTime());
                repaint(0, 110, 100, 170);
                Thread.sleep(100);
            } catch (Exception ex) {
            }

            // pause the loop if player paused
            synchronized (pauseLock) {
                if (paused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException ie) {
                        // nothing
                    }
                }
            }
        }

        // terminating player and the thread
        player.close();
        player = null;

        synchronized (dThreadLock) {
            dThread = null;
            dThreadLock.notify();
        }
    }

    protected void keyPressed(int keycode) {
        switch (keycode) {
        case KEY_STAR:
            changeVolume(-10);

            break;

        case KEY_POUND:
            changeVolume(10);

            break;
        }
    }

    private void changeVolume(int diff) {
        VolumeControl vc;

        if (player != null) {
            vc = (VolumeControl)player.getControl("VolumeControl");

            if (vc != null) {
                int cv = vc.getLevel();
                cv += diff;
                cv = vc.setLevel(cv);
            }
        }
    }

    private String timeFM(long val) {
        String ret = "";
        int mval = (int)(val / 1000);
        int sec = mval / 1000;
        int min = sec / 60;

        if (min >= 10) {
            ret = ret + min + ":";
        } else if (min > 0) {
            ret = "0" + min + ":";
        } else {
            ret = "00:";
        }

        if (sec >= 10) {
            ret = ret + sec + ".";
        } else if (sec > 0) {
            ret = ret + "0" + sec + ".";
        } else {
            ret = ret + "00.";
        }

        mval = (mval % 1000) / 100;
        ret = ret + mval;

        return (ret);
    }
}
