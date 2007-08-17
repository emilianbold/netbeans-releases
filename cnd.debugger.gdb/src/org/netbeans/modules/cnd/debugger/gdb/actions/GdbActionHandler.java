/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.util.ArrayList;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.CustomProjectActionHandler;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.profiles.GdbProfile;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

public class GdbActionHandler implements CustomProjectActionHandler {
    
    private ArrayList listeners = new ArrayList();
    
    public void execute(final ProjectActionEvent ev, InputOutput io) {
        GdbProfile profile = (GdbProfile) ev.getConfiguration().getAuxObject(GdbProfile.GDB_PROFILE_ID);
        if (profile != null) { // profile can be null if dbxgui is enabled
        String gdb = profile.getGdbPath(profile.getGdbCommand(), ev.getProfile().getRunDirectory());
        if (gdb != null) {
            executionStarted();
            Runnable loadProgram = new Runnable() {
                public void run() {
                    if (ev.getID() == ProjectActionEvent.DEBUG) {
                        DebuggerManager.getDebuggerManager().startDebugging(
                                DebuggerInfo.create(GdbDebugger.SESSION_PROVIDER_ID, new Object[] {ev}));
                    } else if (ev.getID() == ProjectActionEvent.DEBUG_STEPINTO) {
                        DebuggerManager.getDebuggerManager().startDebugging(
                                DebuggerInfo.create(GdbDebugger.SESSION_PROVIDER_ID, new Object[] {ev}));
                    }
                }
            };
            SwingUtilities.invokeLater(loadProgram);

            executionFinished(0);
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                NbBundle.getMessage(GdbActionHandler.class, "Err_NoGdbFound"))); // NOI18N

        }
    }
    }
    
    public void addExecutionListener(ExecutionListener l) {
        listeners.add(l);
    }
    
    public void removeExecutionListener(ExecutionListener l) {
        listeners.remove(listeners.indexOf(l));
    }
    
    public void executionStarted() {
        for (int i = 0; i < listeners.size(); i++) {
            ExecutionListener listener = (ExecutionListener) listeners.get(i);
            listener.executionStarted();
        }
    }
    
    public void executionFinished(int rc) {
        for (int i = 0; i < listeners.size(); i++) {
            ExecutionListener listener = (ExecutionListener) listeners.get(i);
            listener.executionFinished(rc);
        }
    }
}
