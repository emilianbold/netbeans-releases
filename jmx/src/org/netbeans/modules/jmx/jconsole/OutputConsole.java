/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
        io.select();
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