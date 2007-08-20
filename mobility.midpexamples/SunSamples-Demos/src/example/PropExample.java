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
package example;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;


/**
 * An example MIDlet shows the values of the system properties.
 * Refer to the startApp, pauseApp, and destroyApp
 * methods so see how it handles each requested transition.
 */
public class PropExample extends MIDlet implements CommandListener {
    private Display display;
    private Form props;
    private StringBuffer propbuf;
    private Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private boolean firstTime;

    /*
     * Construct a new PropExample.
     */
    public PropExample() {
        display = Display.getDisplay(this);
        firstTime = true;
        props = new Form("System Properties");
    }

    /**
     * Show the value of the properties
     */
    public void startApp() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();

        long free = runtime.freeMemory();

        if (firstTime) {
            long total = runtime.totalMemory();

            propbuf = new StringBuffer(50);

            props.append("Free Memory = " + free + "\n");
            props.append("Total Memory = " + total + "\n");

            props.append(showProp("microedition.configuration"));
            props.append(showProp("microedition.profiles"));

            props.append(showProp("microedition.platform"));
            props.append(showProp("microedition.locale"));
            props.append(showProp("microedition.encoding"));

            props.addCommand(exitCommand);
            props.setCommandListener(this);
            display.setCurrent(props);
            firstTime = false;
        } else {
            props.set(0, new StringItem("", "Free Memory = " + free + "\n"));
        }

        display.setCurrent(props);
    }

    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }

    /**
     * Show a property.
     */
    String showProp(String prop) {
        String value = System.getProperty(prop);
        propbuf.setLength(0);
        propbuf.append(prop);
        propbuf.append(" = ");

        if (value == null) {
            propbuf.append("<undefined>");
        } else {
            propbuf.append("\"");
            propbuf.append(value);
            propbuf.append("\"");
        }

        propbuf.append("\n");

        return propbuf.toString();
    }

    /**
     * Time to pause, free any space we don't need right now.
     */
    public void pauseApp() {
    }

    /**
     * Destroy must cleanup everything.
     */
    public void destroyApp(boolean unconditional) {
    }
}
