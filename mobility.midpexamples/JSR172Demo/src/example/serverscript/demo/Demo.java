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
package example.serverscript.demo;


// local
import example.serverscript.connector.Interface;
import example.serverscript.connector.Interface_Stub;

// standard
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

// jax-rpc
import java.rmi.RemoteException;

import java.util.Hashtable;
import java.util.Stack;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Screen;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

// micro edition
import javax.microedition.midlet.MIDlet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

// xml
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * JSR 172 demo.
 * Almost all screens displayed by the demo comes from server in xml format.
 * It's up to server, what the demo will do.
 */
public class Demo extends MIDlet implements Runnable, CommandListener {
    /**
     * Exception with this string is thrown in parser when no need to continue
     * parsing as Exit screen received.
     */
    private static final String EXIT_STRING = "Exiting..";

    /** Service connector jax-rpc stub for connecting to server. */
    private Interface_Stub service;

    /** SAX XML parser handler class. */
    private ParserHandler parser;

    /** Operation in progress flag. Disables command listener when true. */
    private boolean busy = false;

    /** Current active screen. */
    private Screen screen;

    /** This midlet's display object. */
    private Display display;

    /** Table of commands names. */
    private Hashtable commandTypes;

    /** Table of alert types names. */
    private Hashtable alertTypes;

    /** Table of constraints names. */
    private Hashtable constraints;

    /** Info parameter in request to server. */
    private String requestInfo;

    /** Command parameter in request to server. */
    private String requestCommand;

    /** Instance of XML parser engine. */
    private SAXParser saxParser;

    /** Initialize midlet data, service, parsers */
    public void startApp() {
        service = new Interface_Stub();
        service._setProperty(Interface_Stub.SESSION_MAINTAIN_PROPERTY, new Boolean(true));
        parser = new ParserHandler();
        display = Display.getDisplay(this);

        // init command types
        commandTypes = new Hashtable(8);
        commandTypes.put("back", new Integer(Command.BACK));
        commandTypes.put("cancel", new Integer(Command.CANCEL));
        commandTypes.put("exit", new Integer(Command.EXIT));
        commandTypes.put("help", new Integer(Command.HELP));
        commandTypes.put("item", new Integer(Command.ITEM));
        commandTypes.put("ok", new Integer(Command.OK));
        commandTypes.put("screen", new Integer(Command.SCREEN));
        commandTypes.put("stop", new Integer(Command.STOP));

        alertTypes = new Hashtable(5);
        alertTypes.put("alarm", AlertType.ALARM);
        alertTypes.put("confirmation", AlertType.CONFIRMATION);
        alertTypes.put("error", AlertType.ERROR);
        alertTypes.put("info", AlertType.INFO);
        alertTypes.put("warning", AlertType.WARNING);

        constraints = new Hashtable(6);
        constraints.put("any", new Integer(TextField.ANY));
        constraints.put("emailaddr", new Integer(TextField.EMAILADDR));
        constraints.put("numeric", new Integer(TextField.NUMERIC));
        constraints.put("password", new Integer(TextField.PASSWORD));
        constraints.put("phonenumber", new Integer(TextField.PHONENUMBER));
        constraints.put("url", new Integer(TextField.URL));

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            saxParser = factory.newSAXParser();
        } catch (Exception e) {
            error("Error initializing JSR172 features.");

            return;
        }

        retrieveScreen(null, null);
    }

    /** Pause the midlet. */
    public void pauseApp() {
    }

    /**
     * Destroy midlet.
     * @param unconditional Unconditional flag.
     */
    public void destroyApp(boolean unconditional) {
    }

    /** Initiate thread retrieving next screen from server. */
    private void retrieveScreen(String info, String command) {
        System.out.println("retrieve screen: info = " + info + ", command = " + command);
        busy = true;
        requestCommand = command;
        requestInfo = info;
        new Thread(this).start();
    }

    /** Retrieving and parsing thread. */
    public void run() {
        try {
            String str = service.request(requestInfo, requestCommand);
            System.out.println("screen = (((" + str + ")))");
            parser.reset();
            saxParser.parse(new ByteArrayInputStream(str.getBytes("UTF-8")), parser);

            if (screen == null) {
                error("Server error or incompatibility.\n");
            } else {
                screen.setCommandListener(this);
                display.setCurrent(screen);
            }

            busy = false;
        } catch (Exception e) {
            if (!EXIT_STRING.equals(e.getMessage())) {
                e.printStackTrace();
                error("Connection problems.\n" + "Check your internet/proxy settings.");
            }
        }
    }

    /** Display error screen. */
    public void error(String msg) {
        // error initializing xml parser
        Alert connectionError = new Alert("Error", msg, null, AlertType.ERROR);
        connectionError.setTimeout(Alert.FOREVER);
        connectionError.setCommandListener(this);
        display.setCurrent(connectionError);
        busy = false;
        screen = null;
    }

    /** Handle users commands. */
    public void commandAction(Command c, Displayable d) {
        if (busy) {
            return;
        }

        String info = null;

        if (screen == null) {
            destroyApp(false);
            notifyDestroyed();

            return;
        }

        if (screen instanceof List) {
            List list = (List)screen;
            info = list.getString(list.getSelectedIndex());
        } else if (screen instanceof TextBox) {
            info = ((TextBox)screen).getString();
        }

        retrieveScreen(info, c.getLabel());
    }

    /**
     * Parser handler class to parse screen information received from server.
     */
    class ParserHandler extends DefaultHandler {
        /** Stack of document elements.  */
        Stack stack;

        /** Current document element. */
        Object current;

        /** Reset parser. */
        public void reset() {
            stack = new Stack();
            screen = null;
        }

        /** Decode element and create corresponding objects. */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
            if ("Exit".equals(qName)) {
                screen = null;
                destroyApp(false);
                notifyDestroyed();
                throw new SAXException(EXIT_STRING);
            } else if ("Alert".equals(qName)) {
                // get type, default INFO
                AlertType type = AlertType.INFO;
                String typeStr = attributes.getValue("type");

                if (typeStr != null) {
                    type = (AlertType)alertTypes.get(typeStr);
                }

                // create alert
                Alert alert = new Alert(attributes.getValue("title"), null, null, type);

                // get timeout,default FOREVER
                int timeout = Alert.FOREVER;
                String timeoutStr = attributes.getValue("timeout");

                if (timeoutStr != null) {
                    timeout = Integer.parseInt(timeoutStr);
                }

                alert.setTimeout(Alert.FOREVER);
                current = screen = alert;
            } else if ("List".equals(qName)) {
                List list = new List(attributes.getValue("title"), List.IMPLICIT);
                current = screen = list;
            } else if ("TextBox".equals(qName)) {
                // get constraints, default ANY
                int constr = TextField.ANY;
                String constrStr = attributes.getValue("constraints");

                if (constrStr != null) {
                    constr = ((Integer)constraints.get(constrStr)).intValue();
                }

                // get size, default 20
                int size = 20;
                String sizeStr = attributes.getValue("size");

                if (sizeStr != null) {
                    size = Integer.parseInt(sizeStr);
                }

                TextBox box = new TextBox(attributes.getValue("title"), null, size, constr);
                current = screen = box;
            } else if ("Item".equals(qName)) {
                current = "Item";
            } else if ("Command".equals(qName)) {
                // get type, default OK
                int type = Command.OK;
                String typeStr = attributes.getValue("type");

                if (typeStr != null) {
                    type = ((Integer)commandTypes.get(typeStr)).intValue();
                }

                //get priority, default 1
                int priority = 1;
                String priorityStr = attributes.getValue("priority");

                if (priorityStr != null) {
                    priority = Integer.parseInt(priorityStr);
                }

                // create and add button
                Command command = new Command(attributes.getValue("title"), type, priority);
                screen.addCommand(command);

                if ("true".equals(attributes.getValue("select")) && screen instanceof List) {
                    ((List)screen).setSelectCommand(command);
                }

                current = command;
            } else {
                current = new Object();
            }

            stack.push(current);
        }

        /** Handles document character data. */
        public void characters(char[] ch, int start, int length) {
            Object current = stack.peek();

            if (current instanceof Alert) {
                ((Alert)stack.peek()).setString(new String(ch, start, length));
            } else if ("Item".equals(current)) {
                if (screen instanceof List) {
                    ((List)screen).append(new String(ch, start, length), null);
                }
            } else if (current instanceof TextBox) {
                ((TextBox)stack.peek()).setString(new String(ch, start, length));
            }
        }

        /** Handles closing tags. */
        public void endElement(String uri, String localName, String qName) {
            stack.pop();
        }
    }
}
