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
public class HttpView extends MIDlet implements CommandListener, Runnable {
    /** user interface command for indicating Exit request. */
    Command exitCommand = new Command("Exit", Command.EXIT, 2);

    /** user interface command for indicating a page reload request. */
    Command reloadCommand = new Command("Reload", Command.SCREEN, 1);

    /** user interface command to request an HTTP HEAD transaction. */
    Command headCommand = new Command("Head", Command.SCREEN, 1);

    /** user interface command to request an HTTP POST transaction. */
    Command postCommand = new Command("Post", Command.SCREEN, 1);

    /** user interface command to request an HTTP GET transaction. */
    Command getCommand = new Command("Get", Command.SCREEN, 1);

    /** user interface command to cancel the current screen. */
    Command cancelCommand = new Command("Cancel", Command.SCREEN, 1);

    /** user interface command to return back to previous screen. */
    Command backCommand = new Command("Back", Command.BACK, 1);

    /** user interface command to request current HTTP headers. */
    Command headersCommand = new Command("Headers", Command.SCREEN, 1);

    /** user interface command to display current HTTP request headers. */
    Command requestsCommand = new Command("Requests", Command.SCREEN, 1);

    /** user interface command to display errors from current request. */
    Command errorsCommand = new Command("Errors", Command.SCREEN, 1);

    /** user interface command to enter a new URL */
    Command newURLCommand = new Command("New URL", Command.SCREEN, 10);

    /** user interface command to remove the current URL */
    Command removeURLCommand = new Command("Remove", Command.SCREEN, 11);

    /** user interface command to confirm current screen. */
    Command okCommand = new Command("Ok", Command.SCREEN, 1);

    /** user interface command to display help message. */
    Command helpCommand = new Command("Help", Command.HELP, 1);

    /** user interface component containing a list of URLs */
    List urlList;

    /** array of current URLs */
    Vector urls;

    /** user interface alert component. */
    Alert alert;

    /** user interface text box for the contents of the fetched URL. */
    TextBox content;

    /** current display. */
    Display display;

    /** instance of a thread for asynchronous networking and user interface. */
    Thread thread;

    /** current requested url. */
    String url;

    /** current HTTP request type - GET, HEAD, or POST */
    Command requestCommand;

    /** user interface form to hold progress results. */
    Form progressForm;

    /** user interface progress indicator. */
    Gauge progressGauge;

    /** user interface screen for HTTP headers */
    Form headerForm;

    /** form to display request including parsing */
    Form requestForm;

    /** form to display exceptions */
    Form errorsForm;

    /** data entry text box for inputting URLs */
    TextBox urlbox;

    /** initialize the MIDlet with the current display object. */
    public HttpView() {
        display = Display.getDisplay(this);
        setupList();
        alert = new Alert("Warning");
        alert.setTimeout(2000);

        headerForm = new Form("Headers");
        headerForm.addCommand(backCommand);
        headerForm.addCommand(requestsCommand);
        headerForm.setCommandListener(this);

        requestForm = new Form("Request headers");
        requestForm.addCommand(backCommand);
        requestForm.addCommand(errorsCommand);
        requestForm.setCommandListener(this);

        progressForm = new Form("Progress");
        progressForm.addCommand(cancelCommand);
        progressForm.setCommandListener(this);

        progressGauge = new javax.microedition.lcdui.Gauge(url, false, 9, 0);
        progressForm.append(progressGauge);

        errorsForm = new Form("Errors");
        errorsForm.addCommand(backCommand);
        errorsForm.addCommand(headersCommand);
        errorsForm.setCommandListener(this);

        urlbox = new TextBox("Enter Url", "http://", 400, TextField.URL);
        urlbox.addCommand(okCommand);
        urlbox.setCommandListener(this);
    }

    /**
     * Start creates the thread to do the timing.
     * It should return immediately to keep the dispatcher
     * from hanging.
     */
    public void startApp() {
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
                    e.printStackTrace();
                }

                /*
                 * After successfully receiving a socket posted URL
                 * register a datagram connection, too.
                 */
                try {
                    PushRegistry.registerConnection("datagram://:40080", "example.http.HttpView",
                        "129.148.*.*");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (ConnectionNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (connections[0].startsWith("datagram://")) {
                /* Must be a datagram connection. */
                try {
                    UDPDatagramConnection udc =
                        (UDPDatagramConnection)Connector.open(connections[0]);
                    Datagram dg = udc.newDatagram(256);

                    udc.receive(dg);
                    udc.close();

                    byte[] buf = dg.getData();

                    int endofline = 0;
                    count = buf.length;

                    for (int i = 0; i < count; i++) {
                        if (buf[i] == '\n') {
                            endofline = i;

                            break;
                        }
                    }

                    newurl = new String(buf, 0, endofline);

                    /* Unregister the datagram connection. */
                    PushRegistry.unregisterConnection("datagram://:40080");
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //unknown connection type
            }

            urlList.append(newurl, null);
            urls.addElement(newurl);
        } else {
            connections = PushRegistry.listConnections(false);

            /*
             * If the MIDlet was started manually, set an alarm
             * to restart automatically int one minute.
             */
            try {
                Date alarm = new Date();
                PushRegistry.registerAlarm("example.http.HttpView", alarm.getTime() + 60000);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (ConnectionNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (urlList.size() > 0) {
            display.setCurrent(urlList);
        } else {
            alert.setString("No url's configured.");
            display.setCurrent(alert, urlList);
        }
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
        thread = null;
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
        urlList = new List("URLs", List.IMPLICIT);
        urlList.addCommand(headCommand);
        urlList.addCommand(getCommand);
        urlList.addCommand(postCommand);
        urlList.addCommand(exitCommand);
        urlList.addCommand(newURLCommand);
        urlList.addCommand(removeURLCommand);
        urlList.addCommand(helpCommand);
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

        urls.addElement("http://jse.east/Telco/HttpTest.txt");
        // urls.addElement(
        //         "http://dhcp-70-219:8080/examples/servlet/httpdbexport");
        // urls.addElement(
        //       "http://jse.east.sun.com/~kfinn/proxy.jar");
        // urls.addElement(
        //       "http://dhcp-70-219:8080/examples/servlet/HelloWorldKerry");
        urlList.append("Test URL", null);
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
            } else if ((c == headCommand) || (c == getCommand) || (c == postCommand) ||
                    (c == List.SELECT_COMMAND)) {
                if (c == List.SELECT_COMMAND) {
                    c = getCommand;
                }

                requestCommand = c;

                // Display the progress screen and
                // start the thread to read the url
                int i = urlList.getSelectedIndex();

                url = (String)urls.elementAt(i);
                genProgressForm("Progress", url);
                display.setCurrent(progressForm);
                thread = new Thread(this);
                thread.start();
            } else if (c == headersCommand) {
                display.setCurrent(headerForm);
            } else if (c == requestsCommand) {
                display.setCurrent(requestForm);
            } else if (c == errorsCommand) {
                display.setCurrent(errorsForm);
            } else if (c == backCommand) {
                if ((s == headerForm) || (s == requestForm) || (s == errorsForm)) {
                    display.setCurrent(content);
                } else {
                    // Display the list of urls.
                    display.setCurrent(urlList);
                }
            } else if (c == cancelCommand) {
                // Signal thread to stop and put an alert.
                thread = null;
                alert.setString("Loading cancelled.");
                display.setCurrent(alert, urlList);
            } else if (c == newURLCommand) {
                display.setCurrent(urlbox);
            } else if (c == removeURLCommand) {
                int i = urlList.getSelectedIndex();
                urlList.delete(i);
                urls.removeElementAt(i);
            } else if ((c == okCommand) && (s == urlbox)) {
                String newurl = urlbox.getString();
                urlList.append(newurl, null);
                urls.addElement(newurl);
                display.setCurrent(urlList);
            } else if (c == helpCommand) {
                String helpString =
                    "Use Head, Get or Post to download a URL.\n\n" +
                    "Use 'New URL' to enter a new URL.";
                Alert alert = new Alert(null, helpString, null, null);
                alert.setTimeout(Alert.FOREVER);
                display.setCurrent(alert);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Fetch the specified url in a separate thread and update the
     * progress bar as it goes.
     * If the user cancels the fetch, the thread be changed from this thread.
     * If this happens no further updates should be made to the
     * displayable forms. Those shared objects may be re-used
     * by the next fetch.
     */
    public void run() {
        long start = 0;
        long end = 0;
        int bytecode_count_start = 0;
        int bytecode_count_end = 0;

        Thread mythread = Thread.currentThread();
        String method = HttpConnection.GET;

        if (requestCommand == headCommand) {
            method = HttpConnection.HEAD;
        } else if (requestCommand == postCommand) {
            method = HttpConnection.POST;
        }

        if (content == null) {
            content = new TextBox("Content", "", 4096, 0);
            content.addCommand(backCommand);
            content.addCommand(headersCommand);
            content.setCommandListener(this);
        }

        // Clear the buffers and forms so then can be displayed
        // even if an exception terminates reading early.
        content.setTitle("Body len = 0");
        content.setString("");
        genErrorsForm("Errors", null);
        clearForm(requestForm);
        clearForm(headerForm);
        progressGauge.setValue(1);

        HttpConnection conn = null;
        InputStream input = null;
        OutputStream output = null;
        StringBuffer b;
        String string = null;

        try {
            long len = 0;

            conn = (HttpConnection)Connector.open(url);
            conn.setRequestMethod(method);
            setConfig(conn);

            if (mythread != thread) {
                return;
            }

            progressGauge.setValue(2);

            for (int hops = 0; hops < 2; hops++) {
                // Send data to the server (if necessary). Then, see if
                // we're redirected. If so, hop to the new URL
                // specified by the server.
                //
                // You can choose how many hops to make by changing the
                // exit condition of this loop.
                //
                // To see an example of this, try the link
                // "http://www.sun.com/products" link, which will
                // redirect you to a link with a session ID.
                if (method == HttpConnection.POST) {
                    output = conn.openOutputStream();

                    if (mythread != thread) {
                        return;
                    }

                    output.write("hello midlet world".getBytes());
                    output.close();
                    output = null;
                }

                HttpConnection newConn = handleRedirects(conn);

                if (conn != newConn) {
                    conn = newConn;
                } else {
                    break;
                }
            }

            genRequestForm(conn);

            input = conn.openInputStream();

            if (mythread != thread) {
                return;
            }

            content.setTitle(conn.getResponseMessage() + " (" + conn.getResponseCode() + ")");
            genHeaderForm(conn);
            progressGauge.setValue(5);

            if (mythread != thread) {
                return;
            }

            // Download the content of the URL. We limit our download
            // to 4096 bytes (content.getMaxSize()), as most small
            // devices may not be able to handler larger size.
            //
            // A "real program", of course, needs to handle large
            // downloads intelligently. If possible, it should work
            // with the server to limit downloads to small sizes. If
            // this is not possible, it should download only part of
            // the data and allow the user to specify which part to
            // download.
            len = conn.getLength();
            b = new StringBuffer((len >= 0) ? (int)len : 1000);

            int max = content.getMaxSize();

            if (len != -1) {
                // Read content-Length bytes, or until max is reached.
                int ch = 0;

                for (int i = 0; i < len; i++) {
                    if ((ch = input.read()) != -1) {
                        if (ch <= ' ') {
                            ch = ' ';
                        }

                        b.append((char)ch);

                        if (b.length() >= max) {
                            break;
                        }
                    }
                }
            } else {
                // Read til the connection is closed, or til max is reached.
                // (Typical HTTP/1.0 script generated output)
                int ch = 0;
                len = 0;

                while ((ch = input.read()) != -1) {
                    if (ch <= ' ') {
                        ch = ' ';
                    }

                    b.append((char)ch);

                    if (b.length() >= max) {
                        break;
                    }
                }
            }

            string = b.toString();

            if (mythread != thread) {
                return;
            }

            progressGauge.setValue(8);

            content.setTitle("Body len = " + b.length());

            if (b.length() > 0) {
                content.setString(string);
            } else {
                content.setString("no data");
            }

            display.setCurrent(content);
            progressGauge.setValue(9);
        } catch (OutOfMemoryError mem) {
            // Mmm, we still run out of memory, even after setting
            // max download to 4096 bytes. Tell user about the error.
            //
            // A "real program" should decide on the max download
            // size depending on available heap space, or perhaps
            // allow the user to set the max size
            b = null;
            content = null; // free memory to print error

            mem.printStackTrace();

            if (mythread != thread) {
                genErrorsForm("Memory", mem);
                display.setCurrent(errorsForm);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            genErrorsForm("Errors", ex);
            display.setCurrent(errorsForm);
        } finally {
            cleanUp(conn, input, output);

            if (mythread == thread) {
                progressGauge.setValue(10);
            }
        }
    }

    /**
     * Clean up all objects used by the HttpConnection. We must
     * close the InputStream, OutputStream objects, as well as the
     * HttpConnection object, to reclaim system resources. Otherwise,
     * we may not be able to make new connections on some platforms.
     *
     * @param conn the HttpConnection
     * @param input the InputStream of the HttpConnection, may be null
     *              if it's not yet opened.
     * @param output the OutputStream the HttpConnection, may be null
     *              if it's not yet opened.
     */
    void cleanUp(HttpConnection conn, InputStream input, OutputStream output) {
        Thread mythread = Thread.currentThread();

        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            if (mythread == thread) {
                genErrorsForm("InputStream close error", e);
            }
        }

        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException e) {
            if (mythread == thread) {
                genErrorsForm("OutStream close error", e);
            }
        }

        try {
            if (conn != null) {
                conn.close();
            }
        } catch (IOException e) {
            if (mythread == thread) {
                genErrorsForm("HttpConnection close error", e);
            }
        }
    }

    /**
     * Check for redirect response codes and handle
     * the redirect by getting the new location and
     * opening a new connection to it.  The original
     * connection is closed.
     * The process repeats until there are no more redirects.
     * @param c the initial HttpConnection
     * @return the final HttpConnection
     */
    HttpConnection handleRedirects(HttpConnection c) throws IOException {
        while (true) {
            int code = c.getResponseCode();

            switch (code) {
            case HttpConnection.HTTP_TEMP_REDIRECT:
            case HttpConnection.HTTP_MOVED_TEMP:
            case HttpConnection.HTTP_MOVED_PERM:

                String loc = c.getHeaderField("location");
                c.close();
                showAlert("Redirecting to " + loc, null);
                progressGauge.setLabel(loc);

                c = (HttpConnection)Connector.open(loc);

                continue;

            default:
                return c;
            }
        }
    }

    /**
     * Add request properties for the configuration, profiles,
     * and locale of this system.
     * @param c current HttpConnection to receive user agent header
     */
    void setConfig(HttpConnection c) throws IOException {
        String conf = System.getProperty("microedition.configuration");
        String prof = System.getProperty("microedition.profiles");
        int space = prof.indexOf(' ');

        if (space != -1) {
            prof = prof.substring(0, space - 1);
        }

        String platform = System.getProperty("microedition.platform");
        String locale = System.getProperty("microedition.locale");
        String ua = "Profile/" + prof + " Configuration/" + conf + " Platform/" + platform;

        c.setRequestProperty("User-Agent", ua);

        if (locale != null) {
            c.setRequestProperty("Content-Language", locale);
        }
    }

    /**
     * Generate and fill in the Form with the header fields.
     * @param c the open connection with the result headers.
     */
    void genHeaderForm(HttpConnection c) throws IOException {
        clearForm(headerForm);
        headerForm.append(new StringItem("response message: ", c.getResponseMessage()));
        headerForm.append(new StringItem("response code: ", c.getResponseCode() + ""));

        for (int i = 0;; i++) {
            String key = c.getHeaderFieldKey(i);

            if (key == null) {
                break;
            }

            String value = c.getHeaderField(i);
            StringItem item = new StringItem(key + ": ", value);
            headerForm.append(item);
        }
    }

    /**
     * Generate the form with the request attributes and values.
     * @param c the open connection with the request headers.
     */
    void genRequestForm(HttpConnection c) throws IOException {
        clearForm(requestForm);

        requestForm.append(new StringItem("URL: ", c.getURL()));
        requestForm.append(new StringItem("Method: ", c.getRequestMethod()));
        requestForm.append(new StringItem("Protocol: ", c.getProtocol()));
        requestForm.append(new StringItem("Host: ", c.getHost()));
        requestForm.append(new StringItem("File: ", c.getFile()));
        requestForm.append(new StringItem("Ref: ", c.getRef()));
        requestForm.append(new StringItem("Query: ", c.getQuery()));
        requestForm.append(new StringItem("Port: ", Integer.toString(c.getPort())));
        requestForm.append(new StringItem("User-Agent: ", c.getRequestProperty("User-Agent")));
        requestForm.append(new StringItem("Content-Language: ",
                c.getRequestProperty("Content-Language")));
    }

    /**
     * Generate the options form with URL title and progress gauge.
     * @param name the title of the URL to be loaded.
     * @param url label for the progress gauge
     */
    void genProgressForm(String name, String url) {
        progressGauge.setValue(0);
        progressGauge.setLabel(url);
        progressForm.setTitle(name);
    }

    /**
     * Set the Alert to the exception message and display it.
     * @param s the Exception title string
     * @param ex the Exception
     */
    void genErrorsForm(String s, Throwable ex) {
        clearForm(errorsForm);

        if (s != null) {
            errorsForm.setTitle(s);
        } else {
            errorsForm.setTitle("Exception");
        }

        if (ex != null) {
            ex.printStackTrace(); 
            errorsForm.append(ex.getClass().getName());
            errorsForm.append("\n");

            String m = ex.getMessage();

            if (m != null) {
                errorsForm.append(m);
            }
        } else {
            errorsForm.append("None");
        }
    }

    /**
     * Set the alert string and display it.
     * @param s the error message
     * @param next the screen to be shown after the Alert.
     */
    void showAlert(String s, Screen next) {
        alert.setString(s);

        if (next == null) {
            display.setCurrent(alert);
        } else {
            display.setCurrent(alert, next);
        }
    }

    /**
     * Clear out all items in a Form.
     * @param form the Form to clear.
     */
    void clearForm(Form form) {
        int s = form.size();

        for (int i = s - 1; i >= 0; i--) {
            form.delete(i);
        }
    }
}
