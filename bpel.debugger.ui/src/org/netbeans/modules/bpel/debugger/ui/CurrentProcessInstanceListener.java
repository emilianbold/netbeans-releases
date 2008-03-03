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

package org.netbeans.modules.bpel.debugger.ui;

import java.beans.PropertyChangeEvent;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.SourcePath;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.openide.util.RequestProcessor;

/**
 * Listens on {@link org.netbeans.api.debugger.DebuggerManager} on
 * {@link BpelDebugger#PROP_CURRENT_PROCESS_INSTANCE}
 * property and shows its source in the editor.
 *
 * @author Kirill Sorokin
 */
public class CurrentProcessInstanceListener extends DebuggerManagerAdapter {
    
    private transient Object myLock = new Object();
    
    private DebuggerEngine engine;
    private BpelDebugger debugger;
    private SourcePath sourcePath;
    
    public CurrentProcessInstanceListener() {
        boolean thing = true;
    }
    
    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_CURRENT_ENGINE};
    }

    @Override
    public void propertyChange(
            final PropertyChangeEvent e) {
        if (DebuggerManager.PROP_CURRENT_ENGINE.equals(e.getPropertyName())) {
            updateCurrentDebugger();
        } else if (BpelDebugger.PROP_CURRENT_PROCESS_INSTANCE.equals(
                e.getPropertyName())) {
            updateCurrentOpenFile();
        }
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void updateCurrentDebugger() {
        engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        
        if (engine == null) {
            return;
        }
        
        debugger = engine.lookupFirst(null, BpelDebugger.class);
        
        if (debugger == null) {
            return;
        }
        
        debugger.addPropertyChangeListener(this);
        
        sourcePath = engine.lookupFirst(null, SourcePath.class);
        
        if (sourcePath == null) {
            return;
        }
    }
    
    private void updateCurrentOpenFile() {
        if ((engine == null) || (debugger == null) || (sourcePath == null)) {
            return;
        }
        
        final ProcessInstance instance = debugger.getCurrentProcessInstance();
        
        if (instance == null) {
            return;
        }
        
        final String url = sourcePath.getSourcePath(
                instance.getProcess().getQName());
                        
        if (url == null) {
            return;
        }
        
        final PemEntity lastStarted = instance.
                getProcessExecutionModel().getLastStartedEntity();
        
        final String xpath;
        if (lastStarted == null) {
            xpath = "/bpws:process[1]";
        } else {
            xpath = lastStarted.getPsmEntity().getXpath();
        }
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                synchronized (myLock) {
                    EditorContextBridge.showSource(url, xpath, null);
                }
            }
        });
    }
}
