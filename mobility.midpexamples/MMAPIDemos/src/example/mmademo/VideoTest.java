/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package example.mmademo;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Vector;


/**
 * An example MIDlet to demo MMAPI video features
 *
 */
public class VideoTest extends MIDlet implements CommandListener, Runnable {

    private static VideoCanvas videoCanvas = null;
    private static VideoPlayer videoPlayer = null;
    private static Vector  videoClips;

    private Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private Command playCommand = new Command("Play", Command.ITEM, 1);
    //private Command helpCommand = new Command("Help", Command.HELP, 1);

    //private Alert helpScreen  = null;
    private Display display;
    private static List theList;
    private static VideoTest instance = null;

    private static Vector urls;

    static public VideoTest getInstance() {
        return instance;
    }

    static public List getList() {
        return theList;
    }

    public VideoTest() {

        instance = this;
        display  = Display.getDisplay(this);
        theList  = new List("MMAPI Video Player", Choice.IMPLICIT);
        fillList();

        theList.addCommand(playCommand);
        theList.addCommand(exitCommand);
        theList.setCommandListener(this);
        display.setCurrent(theList);
    }

    private void fillList() {
        videoClips = new Vector();
        urls = new Vector();
        for (int n = 1; n < 100; n++) {
            String nthURL = "VideoTest-URL"+ n;
            String url = getAppProperty(nthURL);
            if (url == null || url.length() == 0) {
                break;
            }
	    if (!SimplePlayer.isSupported(url))
		continue;
            String nthTitle = "VideoTest-" + n;
            String title = getAppProperty(nthTitle);
            if (title == null || title.length() == 0) {
                title = url;
            }
            videoClips.addElement(title);
            urls.addElement(url);
            theList.append(title, null);
        }
    }


    /**
     * Called when this MIDlet is started for the first time,
     * or when it returns from paused mode.
     * If there is currently a Form or Canvas displaying
     * video, call its startApp() method.
     */
    public void startApp() {
        //try {
            if (videoPlayer != null)
                videoPlayer.startApp();
            if (videoCanvas != null)
                videoCanvas.startApp();
        //} catch (Exception e) {
        //    System.out.println("DEBUG: GOT EXCEPTION in VideoTest.startApp()!");
        //    e.printStackTrace();
        //}
    }


    /**
     * Called when this MIDlet is paused.
     * If there is currently a Form or Canvas displaying
     * video, call its startApp() method.
     */
    public void pauseApp() {
        //try {
            if (videoPlayer != null)
                videoPlayer.pauseApp();
            if (videoCanvas != null)
                videoCanvas.pauseApp();
        //} catch (Exception e) {
        //    System.out.println("DEBUG: GOT EXCEPTION in VideoTest.pauseApp()!");
        //    e.printStackTrace();
        //}
    }


    /**
     * Destroy must cleanup everything not handled
     * by the garbage collector.
     */
    public synchronized void destroyApp(boolean unconditional) {
        //try {
            if (videoPlayer != null)
                videoPlayer.close();
            if (videoCanvas != null)
                videoCanvas.close();
            nullPlayer();
        //} catch (Exception e) {
        //    System.out.println("DEBUG: GOT EXCEPTION in VideoTest.destroyApp("+unconditional+")!");
        //    e.printStackTrace();
        //}
    }

    public synchronized void nullPlayer() {
        videoPlayer = null;
        videoCanvas = null;
    }

    int index = 0;

    public void run() {
        //try {
            if (index % 2 == 0) {
                videoPlayer = new VideoPlayer(display);
                videoPlayer.open((String) urls.elementAt(index));
                if (videoPlayer != null) {
                    display.setCurrent(videoPlayer);
                    videoPlayer.start();
                }
            } else {
                videoCanvas = new VideoCanvas(display);
                videoCanvas.open((String) urls.elementAt(index));
                if(videoCanvas != null) {
                    display.setCurrent(videoCanvas);
                    videoCanvas.start();
                }
            }
        //} catch (Exception e) {
        //    System.out.println("DEBUG: GOT EXCEPTION in VideoTest.run()!");
        //    e.printStackTrace();
        //}
    }

    /*
     * Respond to commands, including exit
     * On the exit command, cleanup and notify that the MIDlet has
     * been destroyed.
     */
    public void commandAction(Command c, Displayable s) {
        //try {
            if (c == exitCommand) {
                synchronized (this) {
                    if (videoPlayer != null || videoCanvas != null) {
                         new ExitThread().start();
                    } else {
                        destroyApp(false);
                        notifyDestroyed();
                    }
                }
            } else if ((s == theList && c == List.SELECT_COMMAND) || c == playCommand) {
                synchronized (this) {
                    if (videoPlayer != null || videoCanvas != null) {
                        return;
                    }
                    int i = theList.getSelectedIndex();
                    index = i;
                    // need to start the players in a separate thread to
                    // not block the command listener thread during
                    // Player.realize: if it requires a security
                    // dialog (like "is it OK to use airtime?"),
                    // it would block the VM
                    (new Thread(this)).start();
                }
            }
        //} catch (Exception e) {
        //    System.out.println("DEBUG: GOT EXCEPTION in VideoTest.commandAction("+c.toString()+","+s.toString()+")!");
        //    e.printStackTrace();
        //}
    }
    
    class ExitThread extends Thread {
        public void run() {
            //try {
                // this is stop()+deallocate(), but not close(), 
                //which is done in destroyApp() ...
                if (videoPlayer != null) {
                    videoPlayer.stopVideoPlayer();
                    //videoPlayer = null;
                } else { //videoCanvas != null
                    videoCanvas.stopVideoCanvas();
                    //videoCanvas = null;
                }
                destroyApp(false);
                notifyDestroyed();
            //} catch (Exception e) {
            //    System.out.println("DEBUG: GOT EXCEPTION in VideoTest.ExitThread.run()!");
            //    e.printStackTrace();
            //}
        }
    }
}

