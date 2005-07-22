/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.jconsole;

import java.io.PrintWriter;

import org.netbeans.api.xml.cookies.*;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.windows.IOProvider;

/**
 * Create a new pane in the IDE output tab panel.
 *
 * To output a message, use the message method.
 *
 */
public class OutputConsole implements CookieObserver
{
    /** output tab */
    private InputOutput io;

    /** writer to that tab */
    private OutputWriter ow = null;

    private void initInputOutput (String name)
    {
        if (ow != null)
            return;

        // reuse the existing tab with same name if possible
        io = IOProvider.getDefault().getIO(name, false);
        io.setFocusTaken (false);
        ow = io.getOut();
            try {
                // clear the output pane
                ow.reset();
            } catch (java.io.IOException ex) {
                //bad luck
            }
    }

    /** Creates a new instance of OutputConsole */
    public OutputConsole (String name)
    {
        initInputOutput(name);
    }

    // get the embedded PrintWriter interface
    public PrintWriter getPrintWriter()
    {
        if (ow != null)
            return ow;
        else
            return null;
    }

    public void receive (CookieMessage msg)
    {
        if (ow != null)
            ow.println(msg.getMessage());
    }

    public synchronized void message(String message)
    {
        ow.println(message);
    }

    /**
     * Try to move InputOutput to front. Suitable for last message.
     */
    public final void moveToFront()
    {
        boolean wasFocusTaken = io.isFocusTaken();
        io.select();
        io.setFocusTaken(true);
        ow.write("\r");// NOI18N
        io.setFocusTaken(wasFocusTaken);
    }
    
    public void close() {
        io.closeInputOutput();
    }

}