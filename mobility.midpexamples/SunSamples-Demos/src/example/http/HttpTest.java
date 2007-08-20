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

import java.io.*;

import java.util.*;

import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.pki.*;


/**
 * An example MIDlet to fetch a page using an HttpConnection.
 * Refer to the startApp, pauseApp, and destroyApp
 * methods so see how it handles each requested transition.
 *
 * Note: if you run this inside POSE using a multi-homed PC (with more
 * than one network connections), POSE doesn't know how to resolve
 * host names not connected to the first network card. To solve this,
 * add a line like this in your c:/WINNT/system32/drivers/etc/hosts
 * file:
 *
 * XXX.XXX.XXX.XXX  www.sun.com
 * where XXX.XXX.XXX.XXX == IP address 
 */
public class HttpTest extends MIDlet implements CommandListener, Runnable {
    /** User interface command to exit the current application. */
    private Command exitCommand = new Command("Exit", Command.EXIT, 2);

    /** User interface command to issue an HTTP GET request. */
    private Command getCommand = new Command("Get", Command.SCREEN, 1);

    /** User interface command to issue an HTTP POST request. */
    private Command postCommand = new Command("Post", Command.SCREEN, 1);

    /** User interface command to issue an HTTP HEAD request. */
    private Command headCommand = new Command("Head", Command.SCREEN, 1);

    /** User interface command to choose a test. */
    private Command chooseCommand = new Command("Choose", Command.SCREEN, 2);

    /** User interface command to Add a new location. */
    private Command addCommand = new Command("Add", Command.SCREEN, 1);

    /** User interface command to save a new location. */
    private Command addSaveCommand = new Command("OK", Command.SCREEN, 1);

    /** User interface command to confirm current operation. */
    private Command okCommand = new Command("OK", Command.OK, 1);

    /** User interface command to abort current operation. */
    private Command cancelCommand = new Command("Cancel", Command.CANCEL, 1);

    /** The current display object. */
    private Display display;

    /** The url to GET from the network. */
    private String url;

    /** Array of target locations. */
    private Vector urls;

    /** User interface list for selection. */
    private List list;

    /** Message area for user entered URL. */
    private TextBox addTextBox;

    /** Current command to process. */
    private Command currentCommand;

    /** The current command processing thread. */
    private Thread commandThread;

    /** Current attempt count. */
    private int attempt;
    private TextBox t;
    private boolean firstTime;

    /** Initialize the MIDlet with a handle to the current display */
    public HttpTest() {
        urls = new Vector();
        urls.addElement("http://cds.cmsg.sun.com:80/serverscript/serverscript");
        urls.addElement("http://www.sun.com/");
        urls.addElement("https://java.com");
        urls.addElement("-----------------------");
        urls.addElement("http://localhost:8080/");
        urls.addElement("-----------------------");
        urls.addElement("shttp://host/notsupportedprotocol");
        urls.addElement("http://:8080/missinghost");
        urls.addElement("http://mal\\formed:axyt/url???");
        urls.addElement("http://www.sun.com/no/such/page/");
        urls.addElement("http://www.sun.com:29999/no/such/port/");
        urls.addElement("http://no.such.site/");
        urls.addElement("http://www.sun.com/bad_proxy/");

        url = (String)urls.elementAt(0);
        display = Display.getDisplay(this);
        firstTime = true;
    }

    /**
     * Debug output routine.
     * @param s string to be printed.
     */
    static final void DEBUG(String s) {
        if (true) {
            System.out.println(s);
        }
    }

    /**
     * Converts a time to a string containing the corresponding
     * date.<br />
     * <b>NOTE:</b> This is here only because the J2ME date class does not
     * implement toString() in any meaningful way.
     * <p />
     * @param time time to be converted
     * @return a string representation of the time in
     *         the form "dayOfWeek, day mon year hour:min:sec GMT"
     */
    private String time2str(long time) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(new Date(time));

        return c.toString();
    }

    /**
     * Start creates the thread to do the timing.
     * It should return immediately to keep the dispatcher
     * from hanging.
     */
    public void startApp() {
        if (firstTime) {
            // Use the specified URL is overriden in the descriptor
            String u = getAppProperty("HttpTest-Url");

            if (u != null) {
                url = u;
            }

            mainScreen();
            firstTime = false;
        } else {
            display.setCurrent(t);
        }
    }

    /**
     * Display the main screen.
     */
    void mainScreen() {
        String s =
            "URL = " + url + ". Press Get or Post to fetch it, or Choose to " + "use another URL";
        t = new TextBox("Http Test", s, s.length(), 0);
        setCommands(t, false);
        display.setCurrent(t);
    }

    /**
     * Pick a screen.
     */
    void chooseScreen() {
        list = new List("Choose URL", Choice.EXCLUSIVE);

        for (int i = 0; i < urls.size(); i++) {
            list.append((String)urls.elementAt(i), null);
        }

        setCommands(list, true);
        display.setCurrent(list);
    }

    /**
     * Add another screen.
     */
    void addScreen() {
        addTextBox = new TextBox("New URL", "http://", 200, 0);
        addTextBox.addCommand(addSaveCommand);
        addTextBox.addCommand(cancelCommand);
        addTextBox.setCommandListener(this);
        display.setCurrent(addTextBox);
    }

    /**
     * Read the content of the page. Don't care about the response
     * headers.
     * @param request type of HTTP request (GET or POST)
     */
    private void readContents(String request) {
        StringBuffer b = new StringBuffer();
        ++attempt;
        b.append("attempt " + attempt + " content of " + request + " " + url + "\n");

        HttpConnection c = null;
        OutputStream os = null;
        InputStream is = null;
        TextBox t = null;

        try {
            long len = -1;
            int ch = 0;
            long count = 0;
            int rc;

            DEBUG(request + " Page: " + url);
            c = (HttpConnection)Connector.open(url);
            DEBUG("c= " + c);

            c.setRequestMethod(request);

            c.setRequestProperty("foldedField", "first line\r\n second line\r\n third line");

            if (request == HttpConnection.POST) {
                String m = "Test POST text.";
                DEBUG("Posting: " + m);
                os = c.openOutputStream();
                os.write(m.getBytes());
                os.close();
            }

            rc = c.getResponseCode();

            if (rc != HttpConnection.HTTP_OK) {
                b.append("Response Code: " + c.getResponseCode() + "\n");
                b.append("Response Message: " + c.getResponseMessage() + "\n\n");
            }

            is = c.openInputStream();

            DEBUG("is = " + is);

            if (c instanceof HttpConnection) {
                len = ((HttpConnection)c).getLength();
            }

            DEBUG("len = " + len);

            if (len != -1) {
                // Read exactly Content-Length bytes
                DEBUG("Content-Length: " + len);

                for (int i = 0; i < len; i++) {
                    if ((ch = is.read()) != -1) {
                        if (ch <= ' ') {
                            ch = ' ';
                        }

                        b.append((char)ch);
                        count++;

                        if (count > 200) {
                            break;
                        }
                    }
                }
            } else {
                byte[] data = new byte[100];
                int n = is.read(data, 0, data.length);

                for (int i = 0; i < n; i++) {
                    ch = data[i] & 0x000000ff;
                    b.append((char)ch);
                }
            }

            try {
                if (is != null) {
                    is.close();
                }

                if (c != null) {
                    c.close();
                }
            } catch (Exception ce) {
                DEBUG("Error closing connection");
            }

            try {
                len = is.available();
                DEBUG("Inputstream failed to throw IOException after close");
            } catch (IOException io) {
                DEBUG("expected IOException (available())");
                io.printStackTrace();

                // Test to make sure available() is only valid while
                // the connection is still open.,
            }

            t = new TextBox("Http Test", b.toString(), b.length(), 0);
            is = null;
            c = null;
        } catch (IOException ex) {
            ex.printStackTrace();
            DEBUG(ex.getClass().toString());
            DEBUG(ex.toString());
            DEBUG("Exception reading from http");

            if (c != null) {
                try {
                    String s = null;

                    if (c instanceof HttpConnection) {
                        s = ((HttpConnection)c).getResponseMessage();
                    }

                    DEBUG(s);

                    if (s == null) {
                        s = "No Response message";
                    }

                    t = new TextBox("Http Error", s, s.length(), 0);
                } catch (IOException e) {
                    e.printStackTrace();

                    String s = e.toString();
                    DEBUG(s);

                    if (s == null) {
                        s = ex.getClass().getName();
                    }

                    t = new TextBox("Http Error", s, s.length(), 0);
                }

                try {
                    c.close();
                } catch (IOException ioe) {
                    // do not over throw current exception
                }
            } else {
                t = new TextBox("Http Error", "Could not open URL", 128, 0);
            }
        } catch (IllegalArgumentException ille) {
            // Check if an invalid proxy web server was detected.
            t = new TextBox("Illegal Argument", ille.getMessage(), 128, 0);
        } catch (Exception e) {
            t = new TextBox("Error", e.toString(), 128, 0);
        }

        if (is != null) {
            try {
                is.close();
            } catch (Exception ce) {
                ;
            }
        }

        if (c != null) {
            try {
                c.close();
            } catch (Exception ce) {
                ;
            }
        }

        setCommands(t, false);
        display.setCurrent(t);
    }

    /**
     * Read the header of an HTTP connection. Don't care about
     * the actual data.
     *
     * All response header fields are displayed in a TextBox screen.
     * @param request type of HTTP request (GET or POST)
     */
    private void readHeaders(String request) {
        HttpConnection c;
        TextBox t;
        StringBuffer b;

        try {
            try {
                c = (HttpConnection)Connector.open(url);
            } catch (IllegalArgumentException e) {
                String m = e.getMessage();
                t = new TextBox("Illegal argument", e.getMessage(), 128, 0);
                setCommands(t, false);
                display.setCurrent(t);

                return;
            } catch (ConnectionNotFoundException e) {
                t = new TextBox("Error", "Protocol not supported", 128, 0);
                setCommands(t, false);
                display.setCurrent(t);

                return;
            } catch (Exception e) {
                t = new TextBox("Error", e.toString(), 128, 0);
                setCommands(t, false);
                display.setCurrent(t);

                return;
            }

            try {
                c.setRequestMethod(request);

                b = new StringBuffer();
                b.append("URL: ");
                b.append(c.getURL());
                b.append("\nProtocol: ");
                b.append(c.getProtocol());
                b.append("\nHost: " + c.getHost());
                b.append("\nFile: " + c.getFile());
                b.append("\nRef: " + c.getRef());
                b.append("\nQuery: ");
                b.append(c.getQuery());
                b.append("\nPort: ");
                b.append(c.getPort());
                b.append("\nMethod: ");
                b.append(c.getRequestMethod());

                if (c instanceof HttpsConnection) {
                    // getSecurityInfo should connect
                    SecurityInfo sslInfo = ((HttpsConnection)c).getSecurityInfo();
                    Certificate cert = sslInfo.getServerCertificate();

                    b.append("\nSecure protocol: ");
                    b.append(sslInfo.getProtocolName());
                    b.append("\nSecure protocol version: ");
                    b.append(sslInfo.getProtocolVersion());
                    b.append("\nCipher suite: ");
                    b.append(sslInfo.getCipherSuite());

                    if (cert == null) {
                        b.append("\nNo server Certificate.");
                    } else {
                        b.append("\nServer certificate \n\t Type: ");
                        b.append(cert.getType());
                        b.append("\n\t Version: ");
                        b.append(cert.getVersion());
                        b.append("\n\t Serial number: ");
                        b.append(cert.getSerialNumber());
                        b.append("\n\t Issuer: ");
                        b.append(cert.getIssuer());
                        b.append("\n\t Subject: ");
                        b.append(cert.getSubject());
                        b.append("\n\t Signature algorithm: ");
                        b.append(cert.getSigAlgName());
                        b.append("\n\t Not valid before: ");
                        b.append(time2str(cert.getNotBefore()));
                        b.append("\n\t Not valid after:  ");
                        b.append(time2str(cert.getNotAfter()));
                    }
                }

                // if not connected getResponseCode should connect
                b.append("\nResponseCode: ");
                b.append(c.getResponseCode());
                b.append("\nResponseMessage:");
                b.append(c.getResponseMessage());
                b.append("\nContentLength: ");
                b.append(c.getLength());
                b.append("\nContentType: ");
                b.append(c.getType());
                b.append("\nContentEncoding: ");
                b.append(c.getEncoding());
                b.append("\nContentExpiration: ");
                b.append(c.getExpiration());
                b.append("\nDate: ");
                b.append(c.getDate());
                b.append("\nLast-Modified: ");
                b.append(c.getLastModified());
                b.append("\n\n");

                int h = 0;

                while (true) {
                    try {
                        String key = c.getHeaderFieldKey(h);

                        if (key == null) {
                            break;
                        }

                        String value = c.getHeaderField(h);
                        b.append(key);
                        b.append(": ");
                        b.append(value);
                        b.append("\n");
                        h++;

                    } catch (Exception e) {
                        //     "Exception while fetching headers");
                        break;
                    }
                }

                t = new TextBox("Http Test", b.toString(), b.length(), 0);
                setCommands(t, false);
                display.setCurrent(t);
            } finally {
                c.close();
            }
        } catch (ConnectionNotFoundException e) {
            t = new TextBox("Error", "Could not Connect.", 128, 0);
            setCommands(t, false);
            display.setCurrent(t);

            return;
        } catch (CertificateException e) {
            StringBuffer m = new StringBuffer(256);
            String s;
            Certificate cert = e.getCertificate();

            m.append(e.getMessage());

            if (cert != null) {
                m.append("\nServer certificate \n\t Type: ");
                m.append(cert.getType());
                m.append("\n\t Version: ");
                m.append(cert.getVersion());
                m.append("\n\t Serial number: ");
                m.append(cert.getSerialNumber());
                m.append("\n\t Issuer: ");
                m.append(cert.getIssuer());
                m.append("\n\t Subject: ");
                m.append(cert.getSubject());
                m.append("\n\t Signature algorithm: ");
                m.append(cert.getSigAlgName());
                m.append("\n\t Not valid before: ");
                m.append(time2str(cert.getNotBefore()));
                m.append("\n\t Not valid after:  ");
                m.append(time2str(cert.getNotAfter()));
            }

            s = m.toString();
            t = new TextBox("Certificate Error", s, s.length(), 0);
            setCommands(t, false);
            display.setCurrent(t);

            return;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set the funtion to perform based on commands selected.
     * @param d Displayable object
     * @param islist flag to indicate list processing
     */
    void setCommands(Displayable d, boolean islist) {
        if (islist) {
            d.addCommand(addCommand);
            d.addCommand(okCommand);
        } else {
            d.addCommand(exitCommand);
            d.addCommand(chooseCommand);
            d.addCommand(getCommand);
            d.addCommand(postCommand);
            d.addCommand(headCommand);
        }

        d.setCommandListener(this);
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
     * @param unconditional Flag to indicate that forced shutdown
     * is requested
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * Respond to commands, including exit
     * @param c command to perform
     * @param s Screen displayable object
     */
    public void commandAction(Command c, Displayable s) {
        synchronized (this) {
            if (commandThread != null) {
                // process only one command at a time
                return;
            }

            currentCommand = c;
            commandThread = new Thread(this);
            commandThread.start();
        }
    }

    /**
     * Perform the current command set by the method commandAction.
     */
    public void run() {
        if (currentCommand == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        } else if (currentCommand == getCommand) {
            readContents(HttpConnection.GET);
        } else if (currentCommand == postCommand) {
            readContents(HttpConnection.POST);
        } else if (currentCommand == headCommand) {
            readHeaders(HttpConnection.HEAD);
        } else if (currentCommand == chooseCommand) {
            chooseScreen();
        } else if (currentCommand == okCommand) {
            int i = list.getSelectedIndex();

            if (i >= 0) {
                url = list.getString(i);
            }

            mainScreen();
        } else if (currentCommand == addSaveCommand) {
            urls.addElement(addTextBox.getString().trim());
            chooseScreen();
        } else if (currentCommand == addCommand) {
            addScreen();
        } else if (currentCommand == cancelCommand) {
            chooseScreen();
        }

        synchronized (this) {
            // signal that another command can be processed
            commandThread = null;
        }
    }
}
