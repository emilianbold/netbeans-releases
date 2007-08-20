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


public class MixCanvas extends Canvas implements CommandListener {
    private static final String TITLE_TEXT = "Mix Demo";
    private static final int[] notes = { 69, 70, 71, 72, 73, 74, 75, 76 };
    static Player wavPlayer = null;
    static Player tonePlayer = null;
    static Image logo = null;
    private int idx = 0;
    private int ip = 0;
    Display parentDisplay;
    private Command backCommand = new Command("Back", Command.BACK, 1);
    private Command playCommand = new Command("Play", Command.ITEM, 1);
    private Command pauseCommand = new Command("Pause", Command.ITEM, 1);
    private Command toneCommand = new Command("Tone", Command.ITEM, 1);
    private Alert alert;

    //In case the user ended the player using the back command
    //or the 'End' button (the red one), if the WavPlayer did not
    //start yet there is a possibility that it will start after a while.
    //stopSound is here to catch this case and avoid playing the sound.
    private boolean stopSound = false;

    public MixCanvas(Display parentDisplay) {
        super();
        this.idx = 0;
        this.parentDisplay = parentDisplay;
        initialize();
    }

    void initialize() {
        addCommand(backCommand);
        setCommandListener(this);

        try {
            logo = Image.createImage("/icons/Duke.png");
        } catch (Exception ex) {
            logo = null;
        }

        if (logo == null) {
            System.out.println("can not load Duke.png");
        }

        alert = new Alert("Warning", "Can not create player", null, null);
        alert.setTimeout(1000);
    }

    /*
     * Respond to commands, including back
     */
    public void commandAction(Command c, Displayable s) {
        if (s == this) {
            if (c == backCommand) {
                stopSound();
                parentDisplay.setCurrent(MixTest.getList());
            } else if (c == toneCommand) {
                try {
                    Manager.playTone(notes[ip], 1000, 100);
                    ip++;

                    if (ip >= 8) {
                        ip = 0;
                    }
                } catch (Exception ex) {
                    System.out.println("get an exception for tone");
                }
            } else if (c == playCommand) {
                playSound();
            } else if (c == pauseCommand) {
                pauseSound();
            }
        }
    }

    public void setIndex(int idx) {
        this.idx = idx;
    }

    private void createWavPlayer() {
        try {
            if (wavPlayer == null) {
                if (MixTest.wavUrl.startsWith("resource")) {
                    int idx = MixTest.wavUrl.indexOf(':');
                    String loc = MixTest.wavUrl.substring(idx + 1);
                    InputStream is = getClass().getResourceAsStream(loc);
                    String ctype = guessContentType(MixTest.wavUrl);
                    wavPlayer = Manager.createPlayer(is, ctype);
                } else {
                    wavPlayer = Manager.createPlayer(MixTest.wavUrl);
                }

                wavPlayer.setLoopCount(-1);
            }

            if (stopSound) {
                return;
            }

            wavPlayer.start();
        } catch (Exception ex) {
            // ex.printStackTrace();
            if (wavPlayer != null) {
                wavPlayer.close();
                wavPlayer = null;
            }

            parentDisplay.setCurrent(alert);
        }
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

    private void createTonePlayer() {
        byte d = 8;
        byte C4 = ToneControl.C4;
        byte D4 = ToneControl.C4 + 2; // a whole step
        byte E4 = ToneControl.C4 + 4; // a major third
        byte G4 = ToneControl.C4 + 7; // a fifth
        byte rest = ToneControl.SILENCE; // eighth-note rest

        byte[] mySequence =
            new byte[] {
                ToneControl.VERSION, 1, ToneControl.TEMPO, 30, ToneControl.BLOCK_START, 0, E4, d, D4,
                d, C4, d, D4, d, E4, d, E4, d, E4, d, rest, d, ToneControl.BLOCK_END, 0,
                ToneControl.PLAY_BLOCK, 0, D4, d, D4, d, D4, d, rest, d, E4, d, G4, d, G4, d, rest,
                d, //play "B" section
                ToneControl.PLAY_BLOCK, 0, // content of "A" section
                D4, d, D4, d, E4, d, D4, d, C4, d, rest, d // play "C" section
            };

        try {
            if (tonePlayer == null) {
                tonePlayer = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
                tonePlayer.setLoopCount(-1);
                tonePlayer.realize();

                ToneControl tc =
                    (ToneControl)tonePlayer.getControl(
                        "javax.microedition.media.control.ToneControl");
                tc.setSequence(mySequence);
            }

            if ((tonePlayer != null) && !stopSound) {
                tonePlayer.start();
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
            if (tonePlayer != null) {
                tonePlayer.close();
                tonePlayer = null;
            }

            parentDisplay.setCurrent(alert);
        }
    }

    public void playSound() {
        stopSound = false;

        switch (idx) {
        case 0: // wave + tone
            addCommand(toneCommand);
            removeCommand(pauseCommand);
            removeCommand(playCommand);
            createWavPlayer();

            break;

        case 1: // toneseq + tone
            addCommand(toneCommand);
            removeCommand(pauseCommand);
            removeCommand(playCommand);
            createTonePlayer();

            break;

        case 2: // toneseq + wave
            removeCommand(playCommand);
            removeCommand(toneCommand);
            addCommand(pauseCommand);
            createWavPlayer();
            createTonePlayer();

            break;
        }
    }

    public void stopSound() {
        stopSound = true;

        if (tonePlayer != null) {
            tonePlayer.close();
            tonePlayer = null;
        }

        if (wavPlayer != null) {
            wavPlayer.close();
            wavPlayer = null;
        }

        removeCommand(toneCommand);
        removeCommand(pauseCommand);
        removeCommand(playCommand);
    }

    public void pauseSound() {
        removeCommand(pauseCommand);
        addCommand(playCommand);

        try {
            if (wavPlayer != null) {
                wavPlayer.stop();
            }
        } catch (MediaException me) {
            System.err.println(me);
        }

        try {
            if (tonePlayer != null) {
                tonePlayer.stop();
            }
        } catch (MediaException me) {
            System.err.println(me);
        }
    }

    public boolean isPlaying() {
        return ((tonePlayer != null) && (tonePlayer.getState() >= Player.STARTED)) ||
        ((wavPlayer != null) && (wavPlayer.getState() >= Player.STARTED));
    }

    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        String cname = "";

        switch (idx) {
        case 0:
            cname = "test-wav.wav";

            break;

        case 1:
            cname = "tone seq";

            break;

        case 2:
            cname = "wave+toneseq";

            break;
        }

        g.setColor(0);
        g.fillRect(0, 0, w, h);

        if (logo != null) {
            g.drawImage(logo, w / 2, 30, Graphics.TOP | Graphics.HCENTER);
        }

        g.setColor(0xFF7f00);
        g.drawString(TITLE_TEXT, w / 2, 8, Graphics.TOP | Graphics.HCENTER);
        g.drawString(cname, 0, 84, Graphics.TOP | Graphics.LEFT);
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

        if (wavPlayer != null) {
            vc = (VolumeControl)wavPlayer.getControl("VolumeControl");

            if (vc != null) {
                int cv = vc.getLevel();
                cv += diff;
                cv = vc.setLevel(cv);
            }
        }

        if (tonePlayer != null) {
            vc = (VolumeControl)tonePlayer.getControl("VolumeControl");

            if (vc != null) {
                int cv = vc.getLevel();
                cv += diff;
                cv = vc.setLevel(cv);
            }
        }
    }
}
