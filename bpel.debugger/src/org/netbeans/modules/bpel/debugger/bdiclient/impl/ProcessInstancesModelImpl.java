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


package org.netbeans.modules.bpel.debugger.bdiclient.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.ProcessInstancesModel;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessInstanceRef;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;
import org.netbeans.modules.bpel.debugger.api.BpelProcess;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessRef;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.VirtualBPELEngine;

/**
 * Model for maintaining the list of process instances.
 * Supports notification of process state changes.
 *
 * @author Sun Microsystems
 */
public class ProcessInstancesModelImpl implements ProcessInstancesModel {

    private final Vector<Listener> myListeners = new Vector<Listener>();
    private final BpelDebuggerImpl myDebugger;
    
    private final Map<String, ProcessInstanceImpl> myProcessInstances = 
            Collections.synchronizedMap(
                    new LinkedHashMap<String, ProcessInstanceImpl>());
    
    private final Map<QName, BpelProcessImpl> myProcesses =
            new HashMap<QName, BpelProcessImpl>();
    
    public ProcessInstancesModelImpl(
            final BpelDebuggerImpl debugger) {
        
        myDebugger = debugger;
    }
    
    public BpelDebuggerImpl getDebugger() {
        return myDebugger;
    }
    
    public void clear() {
        myProcessInstances.clear();
        myListeners.clear();
    }
    
    public ProcessInstanceImpl[] getProcessInstances() {
        return myProcessInstances.values().toArray(
                new ProcessInstanceImpl[myProcessInstances.size()]);
    }
    
    public ProcessInstanceImpl[] getProcessInstances(
            final BpelProcess process) {
        
        final List<ProcessInstance> filtered = 
                new LinkedList<ProcessInstance>();
        
        for (ProcessInstance instance: myProcessInstances.values()) {
            if (instance.getProcess().equals(process)) {
                filtered.add(instance);
            }
        }
        
        return filtered.toArray(
                new ProcessInstanceImpl[filtered.size()]);
    }
    
    public BpelProcessImpl[] getProcesses() {
        return myProcesses.values().toArray(
                new BpelProcessImpl[myProcesses.size()]);
    }
    
    public BpelProcessImpl getProcess(
            final BPELProcessRef processRef) {
        
        final QName processQName = makeProcessQName(processRef.uri());
        
        BpelProcessImpl process = myProcesses.get(processQName);
        
        if (process == null) {
            process = new BpelProcessImpl(processRef, myDebugger);
            myProcesses.put(processQName, process);
        }
        
        return process;
    }
    
    public void addListener(
            final Listener listener) {
        
        myListeners.add(listener);
    }
    
    public void removeListener(
            final Listener listener) {
        
        myListeners.remove(listener);
    }
    
    protected void processUndeployed(
            final String uri) {
        
        final QName processQName = makeProcessQName(uri);
        for (ProcessInstanceImpl processInstance : getProcessInstances()) {
            if (processInstance.getProcessQName().equals(processQName)) {
                processInstance.setUndeployed(true);
                
                if (processInstance.getState() 
                        == ProcessInstance.STATE_SUSPENDED) {
                    processInstance.resume();
                }
                
                myProcessInstances.remove(processInstance.getId());
                
                fireProcessInstanceRemoved(processInstance);
            }
        }
        
        myProcesses.remove(processQName);
    }
    
    protected BDIDebugFrame frameCreated(
            final String id, 
            final String processInstanceId, 
            final String parentFrameId, 
            final String bpelFile, 
            final String uri) {
        
        ProcessInstanceImpl processInstance = 
                myProcessInstances.get(processInstanceId);
        
        // If no instance with this id is registered -- we should register 
        // it (apply for the BPELProcessInstanceRef). If it's not 
        // available -- ignore the event altogether.
        if (processInstance == null) {
            final VirtualBPELEngine engine = 
                    myDebugger.getBDIDebugger().getVirtualBPELEngine();
                        
            for (String procId: engine.allDeployedBPELs()) {
                final BPELProcessRef processRef = 
                        engine.getBPELProcess(procId);
                
                getProcess(processRef);
                
                final List<String> ids = 
                        Arrays.asList(processRef.allProcessInstanceIDs());
                
                if (ids.contains(processInstanceId)) {
                    final BPELProcessInstanceRef instanceRef = 
                            processRef.getProcessInstance(processInstanceId);
                            
                    processInstanceStarted(instanceRef);
                    break;
                }
            }
            
            processInstance = myProcessInstances.get(processInstanceId);
            
            // If it's still null -- ignore the event
            if (processInstance == null) {
                return null;
            }
        }
        
        final BDIDebugFrame frame = 
                processInstance.addFrame(id, parentFrameId);
        
        return frame;
    }
    
    protected void processInstanceStarted(
            final BPELProcessInstanceRef processInstanceRef) {
        
        final ProcessInstanceImpl processInstance = 
                new ProcessInstanceImpl(this, processInstanceRef);
        
        myProcessInstances.put(processInstance.getId(), processInstance);
        processInstance.onProcessInstanceStarted();
        
        fireProcessInstanceAdded(processInstance);
    }
    
    protected void processInstanceDied(
            final BPELProcessInstanceRef processInstanceRef) {
        
        final String processInstanceId = processInstanceRef.globalID();
        
        final ProcessInstanceImpl processInstance = 
                myProcessInstances.get(processInstanceId);
        
        processInstance.onProcessInstanceDied();
    }
    
    protected void processInstanceCompleted(
            final ProcessInstanceImpl processInstance) {
        
        myProcessInstances.remove(processInstance.getId());
        
        fireProcessInstanceRemoved(processInstance);
    }
    
    protected void processInstanceStateChanged(
            final ProcessInstanceImpl processInstance, 
            final int oldState, 
            final int newState) {
        
        fireProcessInstanceStateChanged(processInstance, oldState, newState);
    }
    
    protected static QName makeProcessQName(String uri) {
        final int pos = uri.lastIndexOf('/');
        
        return new QName(uri.substring(0, pos), uri.substring(pos + 1));
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void fireProcessInstanceAdded(
            final ProcessInstanceImpl processInstance) {
        
        final Listener[] listeners = new Listener[myListeners.size()];
        myListeners.copyInto(listeners);
        
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].processInstanceAdded(processInstance);
        }
    }
    
    private void fireProcessInstanceRemoved(
            final ProcessInstanceImpl processInstance) {
        
        final Listener[] listeners = new Listener[myListeners.size()];
        myListeners.copyInto(listeners);
        
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].processInstanceRemoved(processInstance);
        }
    }
    
    private void fireProcessInstanceStateChanged(
            final ProcessInstanceImpl processInstance, 
            final int oldState, 
            final int newState) {
        
        final Listener[] listeners = new Listener[myListeners.size()];
        myListeners.copyInto(listeners);
        
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].processInstanceStateChanged(
                    processInstance, oldState, newState);
        }
    }
}
