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


package org.netbeans.modules.bpel.debugger.api;

/**
 * Model for maintaining the list of process instances.
 * Supports notification of process state changes.
 *
 * @author Sun Microsystems
 */
public interface ProcessInstancesModel {

    /**
     * Returns an array containing one process instance 
     * per process. Similar to getProcessInstanceThreads
     * except only one process instance is selected to
     * be in the array for any given process, 
     * and that selection is either the first suspended
     * instance or the first instance of the process.
     * 
     * @return 
     */
    ProcessInstance[] getProcessInstances();
    
    ProcessInstance[] getProcessInstances(BpelProcess process);
    
    BpelProcess[] getProcesses();
    
    /**
     * Adds a listener to the model.
     * 
     * @param listener 
     */
    void addListener(Listener listener);
    
    /**
     * Removes a listener from the model.
     * 
     * @param listener 
     */
    void removeListener(Listener listener);
    
    /**
     * Listener for changes to the process instance model.
     * The listener will be informed of when process instances
     * have been created/exited. State changes are also fired to listeners.
     *
     * @author Sun Microsystems
     */
    interface Listener {

        /**
         * Process instance exited.
         * 
         * @param processInstance 
         */
        void processInstanceRemoved(ProcessInstance processInstance);

        /**
         * New process isntance created.
         * 
         * @param processInstance 
         */
        void processInstanceAdded(ProcessInstance processInstance);

        /**
         * The state of the process instance changed.
         * 
         * @param processInstance
         * @param oldState
         * @param newState 
         */
        void processInstanceStateChanged(
                ProcessInstance processInstance, 
                int oldState, 
                int newState);

    }
}
