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
package example.http;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.String;

import java.util.Date;
import java.util.Vector;

import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;


/**
 * An example MIDlet to fetch a page using an HttpConnection.
 * Refer to the startApp, pauseApp, and destroyApp
 * methods so see how it handles each requested transition.
 */
public class PushExample extends MIDlet implements CommandListener {
    // Wait for 2sec
    static final int DefaultTimeout = 2000;

    /** user interface command for indicating Exit request. */
    Command exitCommand = new Command("Exit", Command.EXIT, 2);

    /** user interface component containing a list of URLs */
    List urlList;

    /** array of current URLs */
    Vector urls;

    /** user interface alert component. */
    Alert alert;
    Image newsHoundImage;
    boolean imageLoaded;

    /** current display. */
    Display display;

    /** current requested url. */
    String url;

    /** initialize the MIDlet with the current display object. */
    public PushExample() {
        display = Display.getDisplay(this);
    }

    /**
     * Start creates the thread to do the timing.
     * It should return immediately to keep the dispatcher
     * from hanging.
     */
    public void startApp() {
        try {
            newsHoundImage = Image.createImage("/example/http/images/newshound.png");
            imageLoaded = true;
        } catch (java.io.IOException ex) {
            System.err.println("Image is not loaded :" + imageLoaded);
        }

        alert = new Alert("News Hound", "", newsHoundImage, AlertType.INFO);

        alert.setTimeout(DefaultTimeout);
        setupList();

        /* Bytes read from the URL update connection. */
        int count;

        /* Check for inbound async connection for sample Finger port. */
        String[] connections = PushRegistry.listConnections(true);

        /* HttpView was started to handle inbound request. */
        String pushProperty = getAppProperty("MIDlet-Push-1");

        if ((connections != null) && (connections.length > 0)) {
            String newurl = "Pushed URL Placeholder";

            /* Test basic get registry information interfaces. */
            try {
                String midlet = PushRegistry.getMIDlet(connections[0]);
                String filter = PushRegistry.getFilter(connections[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            /* Check for socket or datagram connection. */
            if (connections[0].startsWith("socket://")) {
                try {
                    /* Simple test assumes a server socket connection. */
                    ServerSocketConnection scn =
                        (ServerSocketConnection)Connector.open(connections[0]);
                    SocketConnection sc = (SocketConnection)scn.acceptAndOpen();

                    /* Read one line of text as a new URL to add to the list. */
                    DataInputStream dis = sc.openDataInputStream();
                    byte[] buf = new byte[256];
                    int endofline = 0;
                    count = dis.read(buf);

                    for (int i = 0; i < count; i++) {
                        if (buf[i] == '\n') {
                            endofline = i;

                            break;
                        }
                    }

                    newurl = new String(buf, 0, endofline);

                    dis.close();

                    sc.close();
                    scn.close();
                } catch (IOException e) {
                    System.err.println("******* io exception in push example");
                    e.printStackTrace();
                }
            } else {
                System.err.println("Unknown connection type");
            }

            urlList.append(newurl, null);
            urls.addElement(newurl);
        } else {
            connections = PushRegistry.listConnections(false);
        }

        display.setCurrent(alert, urlList);
    }

    /**
     * Pause signals the thread to stop by clearing the thread field.
     * If stopped before done with the iterations it will
     * be restarted from scratch later.
     */
    public void pauseApp() {
    }

    /**
     * Destroy must cleanup everything.  The thread is signaled
     * to stop and no result is produced.
     * @param unconditional true if a forced shutdown was requested
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * Check the attributes in the descriptor that identify
     * url's and titles and initialize the lists of urls
     * and urlList.
     * <P>
     * The attributes are named "ViewTitle-n" and "ViewURL-n".
     * The value "n" must start at "1" and increment by 1.
     */
    void setupList() {
        urls = new Vector();
        urlList = new List("News Headlines", List.IMPLICIT);
        urlList.setFitPolicy(Choice.TEXT_WRAP_OFF);
        urlList.addCommand(exitCommand);
        urlList.setCommandListener(this);

        for (int n = 1; n < 100; n++) {
            String nthURL = "ViewURL-" + n;
            String url = getAppProperty(nthURL);

            if ((url == null) || (url.length() == 0)) {
                break;
            }

            String nthTitle = "ViewTitle-" + n;
            String title = getAppProperty(nthTitle);

            if ((title == null) || (title.length() == 0)) {
                title = url;
            }

            urls.addElement(url);
            urlList.append(title, null);
        }

        urlList.append("Next InComing News: ", null);
    }

    /**
     * Respond to commands, including exit
     * @param c user interface command requested
     * @param s screen object initiating the request
     */
    public void commandAction(Command c, Displayable s) {
        try {
            if (c == exitCommand) {
                destroyApp(false);
                notifyDestroyed();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
