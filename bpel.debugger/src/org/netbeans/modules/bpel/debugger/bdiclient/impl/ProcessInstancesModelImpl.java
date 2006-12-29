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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.debugger.bdiclient.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.ProcessInstancesModel;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;

/**
 * Model for maintaining the list of process instances.
 * Supports notification of process state changes.
 *
 * @author Sun Microsystems
 */
public class ProcessInstancesModelImpl implements ProcessInstancesModel {

    private static Logger LOGGER = Logger.getLogger(ProcessInstancesModelImpl.class.getName());
    
    private final Vector mListeners = new Vector();
    private final BpelDebuggerImpl mDebugger;
    
    private final Map<String, ProcessInstanceImpl> mProcessInstances = Collections.synchronizedMap(
            new LinkedHashMap<String, ProcessInstanceImpl>());
    
    public ProcessInstancesModelImpl(BpelDebuggerImpl debugger) {
        mDebugger = debugger;
    }
    
    public BpelDebuggerImpl getDebugger() {
        return mDebugger;
    }
    
    public void clear() {
        mProcessInstances.clear();
        mListeners.clear();
    }
    
    public ProcessInstanceImpl[] getProcessInstances() {
        return mProcessInstances.values().toArray(new ProcessInstanceImpl[mProcessInstances.size()]);
    }
    
    public void addListener(ProcessInstancesModel.Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(ProcessInstancesModel.Listener listener) {
        mListeners.remove(listener);
    }
    
    protected void processUndeployed(String uri) {
        QName processQName = makeProcessQName(uri);
        for (ProcessInstanceImpl processInstance : getProcessInstances()) {
            if (processInstance.getProcessQName().equals(processQName)) {
                processInstance.setUndeployed(true);
                if (processInstance.getState() == ProcessInstance.STATE_SUSPENDED) {
                    processInstance.resume();
                }
                mProcessInstances.remove(processInstance.getRootFrameId());
                fireProcessInstanceRemoved(processInstance);
            }
        }
    }

    protected BDIDebugFrame frameCreated(String rootFrameId, String id, String bpelFile, String uri) {
        if (id.equals(rootFrameId)) {
            // New distinct process instance.
            QName processQName = makeProcessQName(uri);
            ProcessInstanceImpl processInstance = new ProcessInstanceImpl(this, processQName, rootFrameId);
            mProcessInstances.put(id, processInstance);
            fireProcessInstanceAdded(processInstance);
            
            BDIDebugFrame frame = processInstance.addFrame(id);
            return frame;
        } else {
            ProcessInstanceImpl processInstance = mProcessInstances.get(rootFrameId);
            BDIDebugFrame frame = processInstance.addFrame(id);
            return frame;
        }
    }
    
    protected void processInstanceCompleted(ProcessInstanceImpl processInstance) {
        mProcessInstances.remove(processInstance.getRootFrameId());
        fireProcessInstanceRemoved(processInstance);
    }
    
    protected void processInstanceStateChanged(ProcessInstanceImpl processInstance, int oldState, int newState) {
        fireProcessInstanceStateChanged(processInstance, oldState, newState);
    }
    
    private void fireProcessInstanceAdded(ProcessInstanceImpl processInstance) {
        ProcessInstancesModel.Listener[] listeners = new ProcessInstancesModel.Listener[mListeners.size()];
        mListeners.copyInto(listeners);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].processInstanceAdded(processInstance);
        }
    }
    
    private void fireProcessInstanceRemoved(ProcessInstanceImpl processInstance) {
        ProcessInstancesModel.Listener[] listeners = new ProcessInstancesModel.Listener[mListeners.size()];
        mListeners.copyInto(listeners);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].processInstanceRemoved(processInstance);
        }
    }
    
    private void fireProcessInstanceStateChanged(ProcessInstanceImpl processInstance, int oldState, int newState) {
        ProcessInstancesModel.Listener[] listeners = new ProcessInstancesModel.Listener[mListeners.size()];
        mListeners.copyInto(listeners);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].processInstanceStateChanged(processInstance, oldState, newState);
        }
    }
    
    private static QName makeProcessQName(String uri) {
        int pos = uri.lastIndexOf('/');
        return new QName(uri.substring(0, pos), uri.substring(pos + 1));
    }
    
}
