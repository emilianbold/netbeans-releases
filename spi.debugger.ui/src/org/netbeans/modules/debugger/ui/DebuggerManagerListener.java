/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.Session;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;

import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;


/**
 * This listener notifies about changes in the 
 * {@link DebuggerManager}.
 *
 * @author Jan Jancura
 */
public class DebuggerManagerListener extends DebuggerManagerAdapter {
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName ().equals (DebuggerManager.PROP_SESSIONS)) {
            if (((Session[]) evt.getOldValue ()).length == 0
            ) {
                // Open debugger TopComponentGroup.
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        TopComponentGroup group = WindowManager.getDefault ().
                            findTopComponentGroup ("debugger"); // NOI18N
                        if (group != null) {
                            try {
                            group.open ();
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
            if (DebuggerManager.getDebuggerManager ().getSessions ().length == 0
            ) {
//                if (DebuggerManager.getDebuggerManager ().getSessions ().
//                    length < 1)
//                    StatusDisplayer.getDefault ().setStatusText (
//                        NbBundle.getMessage (
//                            DebuggerManagerListener.class, 
//                            "CTL_DebuggerManager_end"   //NOI18N
//                    ));
            
                // Close debugger TopComponentGroup.
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        TopComponentGroup group = WindowManager.getDefault ().
                            findTopComponentGroup ("debugger"); // NOI18N
                        if (group != null) {
                            group.close ();
                        }
                    }
                });
            }
        }
    }
    
    public String[] getProperties () {
        return new String [] {DebuggerManager.PROP_SESSIONS};
    }
    
}
