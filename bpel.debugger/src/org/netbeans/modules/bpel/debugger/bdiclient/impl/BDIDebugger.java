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

import org.netbeans.modules.bpel.debugger.api.DebugException;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELDebugger;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessInstanceRef;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessRef;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebugFrame;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.VirtualBPELEngine;

/**
 * This is a remote object, the BPEL service engine makes callbacks
 * into this. The methods called are defined in the BPELDebugger interface.
 * We receive enterFrame events for each thread entered.
 * 
 * <p>
 * <b>Note:</b> Javadocs for this class are adapted from the (somewhat) cryptic
 * javadocs of the interface, thus may get evetually outdated.
 * 
 * @author Alexander Zgursky
 */
public class BDIDebugger implements BPELDebugger {
    
    private BpelDebuggerImpl myDebugger;
    private VirtualBPELEngine myVirtualBpelEngine;
    
    /** Creates a new instance of BDIDebugger */
    public BDIDebugger(
            final BpelDebuggerImpl debugger) {
        myDebugger = debugger;
    }
    
    /**
     * BPEL Debugger Server MUST implement this method to pass the information 
     * of parent debug frame: Id, BPEL file name and target URI the 
     * <code>DebuggableEngine</code> belongs and returns its local 
     * <code>DebugFrame</code> that encapsulates the remote 
     * <code>DebugFrame</code> that will be used to inform the remote debugger 
     * client on runtime activities.
     * 
     * <p>
     * This method is invoked from debugger server to debugger client.
     * 
     * @param id Id of the <code>DebugFrame</code>
     * @param parentFrameId Id of parent debug frame
     * @param bpelFile The name of bpel file
     * @param uri The targetNameSpace uri
     *
     * @return DOCUMENT ME!
     */    
    public DebugFrame enterFrame(
            final String frameId, 
            final String processInstanceId, 
            final String parentFrameId, 
            final String bpelFile, 
            final String uri) {
        System.out.println("- enterFrame(" + frameId + ", " + 
                processInstanceId + ", " + parentFrameId + ", " + 
                bpelFile + ", " + uri + ")");
        
        return getProcessInstancesModel().frameCreated(
                frameId, 
                processInstanceId, 
                parentFrameId, 
                bpelFile, 
                uri);
    }
    
    /**
     * Called when detached from the other end of self-initiated, both can be 
     * intentional or accidental.
     * 
     * <p>
     * The implementation should account for both situations, the result 
     * should be the debugger client and server are both disconnected cleanly.
     * 
     * @return <code>true</code> if detach succeeds.
     */
    public boolean detach() {
        System.out.println("- detach()");
        
        //TODO:change to NLS
        myDebugger.setException(new DebugException("Target disconnected"));
        return true;
    }
    
    /**
     * Invoked when a BPEL process is added to the BPEL engine.
     * 
     * @param process The BPEL process reference.
     */
    public void processAdded(
            final BPELProcessRef process) {
        System.out.println("- processAdded(" + process + ")");
        
        // Does nothing
    }
    
    /**
     * Invoked when a BPEL process is removed from the BPEL engine.
     * 
     * @param process The BPEL process reference.
     */
    public void processRemoved(
            final BPELProcessRef process) {
        System.out.println("- processRemoved(" + process + ")");
        
        getProcessInstancesModel().processUndeployed(process.uri());
    }
    
    /**
     * Invoked when a BPEL process instance is created.
     * 
     * @param instance The process instance reference.
     */
    public void processInstanceStarted(
            final BPELProcessInstanceRef instance) {
        System.out.println("- processInstanceStarted(" + instance + ")");
        
        getProcessInstancesModel().processInstanceStarted(instance);
    }
    
    /**
     * Invoked when a BPEL process instance is completed.
     * 
     * @param instance The process instance reference.
     */
    public void processInstanceDied(
            final BPELProcessInstanceRef instance) {
        System.out.println("- processInstanceDied(" + instance + ")");
        
        getProcessInstancesModel().processInstanceDied(instance);
    }
    
    /**
     * Debugger server set the instance of <code>VirtualBPELEngine</code> to 
     * debugger client.
     * 
     * @param engine The <code>VirtualBPELEngine</code> object.
     */
    public void setVirtualBPELEngine(
            final VirtualBPELEngine engine) {
        System.out.println("- setVirtualBPELEngine(" + engine.toString() + ")");
        
        myVirtualBpelEngine = engine;
        
        final ProcessInstancesModelImpl model = getProcessInstancesModel();
        
        for (String procName: engine.allDeployedBPELs()) {
            final BPELProcessRef processRef = engine.getBPELProcess(procName);
            
            model.getProcess(processRef);
            
            for (String instName: processRef.allProcessInstanceIDs()) {
                final BPELProcessInstanceRef instanceRef = 
                        processRef.getProcessInstance(instName);
                
                model.processInstanceStarted(instanceRef);
            }
        }
    }
    
    public VirtualBPELEngine getVirtualBPELEngine() {
        return myVirtualBpelEngine;
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private ProcessInstancesModelImpl getProcessInstancesModel() {
        return myDebugger.getProcessInstancesModel();
    }
}
