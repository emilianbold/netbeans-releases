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
package datagram;

import java.io.*;

import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;


public class Client implements Runnable, CommandListener {
    private DatagramMIDlet parent;
    private Display display;
    private Form f;
    private StringItem si;
    private TextField tf;
    private Command sendCommand = new Command("Send", Command.ITEM, 1);
    private Command exitCommand = new Command("Exit", Command.EXIT, 1);
    Sender sender;

    public Client(DatagramMIDlet m) {
        parent = m;
        display = Display.getDisplay(parent);
        f = new Form("Datagram Client");
        si = new StringItem("Status:", " ");
        tf = new TextField("Send:", "", 30, TextField.ANY);
        f.append(si);
        f.append(tf);
        f.addCommand(sendCommand);
        f.addCommand(exitCommand);
        f.setCommandListener(this);
        display.setCurrent(f);
    }

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        try {
            DatagramConnection dc = (DatagramConnection)Connector.open("datagram://localhost:5555");

            si.setText("Connected to server");

            sender = new Sender(dc);

            while (true) {
                Datagram dg = dc.newDatagram(100);
                dc.receive(dg);

                // Have we actually received something or
                // is this just a timeout ?
                if (dg.getLength() > 0) {
                    si.setText("Message received - " + new String(dg.getData(), 0, dg.getLength()));
                }
            }
        } catch (ConnectionNotFoundException cnfe) {
            Alert a = new Alert("Client", "Please run Server MIDlet first", null, AlertType.ERROR);
            a.setTimeout(Alert.FOREVER);
            display.setCurrent(a);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void commandAction(Command c, Displayable s) {
        if ((c == sendCommand) && !parent.isPaused()) {
            sender.send(null, tf.getString());
        }

        if (c == exitCommand) {
            parent.destroyApp(true);
            parent.notifyDestroyed();
        }
    }

    public void stop() {
    }
}
