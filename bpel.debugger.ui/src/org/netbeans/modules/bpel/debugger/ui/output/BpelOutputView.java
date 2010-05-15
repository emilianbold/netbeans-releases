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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.output;

import java.awt.EventQueue;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.windows.WindowManager;
import org.openide.windows.TopComponent;

import org.netbeans.modules.bpel.debugger.api.Tracer;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.14
 */
public class BpelOutputView implements Tracer {

    BpelOutputView(String name) {
//System.out.println();
//System.out.println("INIT TRACER: " + name);
        
        myDateFormat = new SimpleDateFormat("HH:mm:ss"); // NOI18N
        final String title;

//      if (name == null) {
           title = NbBundle.getMessage(BpelOutputView.class, "CTL_DebuggerConsoleName"); // NOI18N
//      } else {
//         title = NbBundle.getMessage(BpelOutputView.class, "CTL_DebuggerConsole", name); // NOI18N
//      }
        if (EventQueue.isDispatchThread()) {
            prepareOutput(title);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    prepareOutput(title);
                }
            });
        }
    }
    
    /**{@inheritDoc}*/
    public void println(final String message) {
        if (EventQueue.isDispatchThread()) {
            myOutputWriter.println(myDateFormat.format(new Date())+" "+message); // NOI18N
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    myOutputWriter.println(myDateFormat.format(new Date())+" "+message); // NOI18N
                }
            });
        }
    }
    
    /**{@inheritDoc}*/
    public void println(final Throwable exception) {
        if (EventQueue.isDispatchThread()) {
            exception.printStackTrace(myOutputWriter);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    exception.printStackTrace(myOutputWriter);
                }
            });
        }
    }
    
    /**{@inheritDoc}*/
    public void println(String message, Throwable exception) {
        println(message);
        println(exception);
    }
    
    /**{@inheritDoc}*/
    public void debug(String message) {
        if (ourEnabled) {
            println(message);
        }
    }
    
    /**{@inheritDoc}*/
    public void debug(Throwable exception) {
        if (ourEnabled) {
            println(exception);
        }
    }
    
    /**{@inheritDoc}*/
    public void debug(String message, Throwable exception) {
        if (ourEnabled) {
            println(message, exception);
        }
    }
    
    private void prepareOutput(String title) {
        TopComponent topComponent =
            WindowManager.getDefault().findTopComponent("output"); // NOI18N
        
        if (topComponent != null) {
            topComponent.open();
            topComponent.requestActive();
        }
        
        InputOutput inputOutput = IOProvider.getDefault().getIO(title, false);
        inputOutput.setFocusTaken(false);
        inputOutput.select();
        myOutputWriter = inputOutput.getOut();
    }

    private DateFormat myDateFormat;
    private OutputWriter myOutputWriter;
    private static boolean ourEnabled = System.getProperty("org.netbeans.modules.bpel.debugger.debug") != null; // NOI18N
}
