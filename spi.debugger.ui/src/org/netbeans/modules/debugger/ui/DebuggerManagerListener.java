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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.ui;

import java.beans.PropertyChangeEvent;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;

import org.openide.awt.ToolbarPool;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;


/**
 * This listener notifies about changes in the
 * {@link DebuggerManager}.
 *
 * @author Jan Jancura
 */
public class DebuggerManagerListener extends DebuggerManagerAdapter {
    
    private boolean isOpened = false;
    
    public void propertyChange (PropertyChangeEvent evt) {
        if ( (DebuggerManager.getDebuggerManager ().getCurrentEngine () 
               != null) &&
             (!isOpened)
        ) {
            // Open debugger TopComponentGroup.
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    TopComponentGroup group = WindowManager.getDefault ().
                        findTopComponentGroup ("debugger"); // NOI18N
                    if (group != null) {
                        group.open ();
                        if (ToolbarPool.getDefault ().
                            getConfiguration ().equals 
                            (ToolbarPool.DEFAULT_CONFIGURATION)
                        )
                            ToolbarPool.getDefault ().setConfiguration 
                                ("Debugging"); // NOI18N
                    }
                }
            });
            isOpened = true;
        }
        if ( (evt.getPropertyName () == DebuggerManager.PROP_DEBUGGER_ENGINES) 
                &&
             ((DebuggerEngine[]) evt.getNewValue ()).length == 0
        ) {
            closeDebuggerUI();
            isOpened = false;
        }
    }
    
    static void closeDebuggerUI() {
        /*
        java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.jpda").fine("CLOSING TopComponentGroup...");
        StringWriter sw = new StringWriter();
        new Exception("Stack Trace").fillInStackTrace().printStackTrace(new java.io.PrintWriter(sw));
        java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.jpda").fine(sw.toString());
         */
        // Close debugger TopComponentGroup.
        if (SwingUtilities.isEventDispatchThread()) {
            doCloseDebuggerUI();
        } else {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    doCloseDebuggerUI();
                }
            });
        }
        //java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.jpda").fine("TopComponentGroup closed.");
    }
    
    private static void doCloseDebuggerUI() {
        TopComponentGroup group = WindowManager.getDefault ().
                findTopComponentGroup ("debugger"); // NOI18N
        if (group != null) {
            group.close ();
            if (ToolbarPool.getDefault().getConfiguration().equals("Debugging")) { // NOI18N
                ToolbarPool.getDefault().setConfiguration(ToolbarPool.DEFAULT_CONFIGURATION);
            }
        }
    }
    
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_DEBUGGER_ENGINES,
            DebuggerManager.PROP_CURRENT_ENGINE
        };
    }
    
}
