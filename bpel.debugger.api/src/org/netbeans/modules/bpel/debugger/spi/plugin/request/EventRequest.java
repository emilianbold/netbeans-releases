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

package org.netbeans.modules.bpel.debugger.spi.plugin.request;

/**
 * Represents a request for notification of an event.
 * Examples include
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.BreakpointReachedRequest}
 * and
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.ProcessInstanceCreatedRequest}.
 * When an event occurs for which an enabled request is present,
 * an
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.EventSet}
 * will be placed on the
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.EventQueue}.
 * The collection of existing event requests is managed
 * by the
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequestManager}.
 * <br><br>
 * Any method on EventRequest or which takes EventRequest as an parameter may
 * throw
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.BpelEngineDisconnectedException}
 * if the target BPEL Engine is disconnected and the
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.BpelEngineDisconnectedEvent}
 * has been or is available to be read from the
 * {@link org.netbeans.modules.bpel.debugger.spi.plugin.event.EventQueue}.
 *
 * @author Alexander Zgursky
 */
public interface EventRequest {
    
    /**
     * Enables or disables this event request.
     * While this event request is disabled, the event request will be ignored
     * by the target BPEL Engine. Disabled event requests still exist, and are
     * included in
     * {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.EventRequestManager#getAllRequests}.
     *
     * @param val true if the request is to be enabled; false otherwise.
     *
     * @throws InvalidRequestStateException
     *  if this request has been deleted
     * @throws org.netbeans.modules.bpel.debugger.spi.plugin.IncompatibleProcessInstanceStateException
     *  if this is a
     *  {@link org.netbeans.modules.bpel.debugger.spi.plugin.request.StepCompletedRequest},
     *  val is <code>true</code>, and the process instance named in
     *  the request has completed
     */
    void setEnabled(boolean val);
    
    /**
     * Determines if this event request is currently enabled.
     *
     * @return <code>true</code> if enabled; <code>false</code> otherwise
     */
    boolean isEnabled();
    
    /**
     * Add an arbitrary key/value "property" to this request.
     * The property can be used by the BPEL Debugger Core to associate
     * its information with the request; these properties are not used
     * internally by the BPEL Debugger Plugin.
     * <br><br>
     * The get/putProperty methods provide access to a small per-instance map.
     * This is not to be confused with {@link java.util.Properties}.
     * <br><br>
     * If value is <code>null</code> this method will remove the property. 
     */
    void putProperty(Object key, Object value);
    
    /**
     * Returns the value of the property with the specified key.
     * Only properties added with
     * {@link #putProperty} will return a non-null value.
     *
     * @return the value of this property or <code>null</code>
     */
    Object getProperty(Object key);
    
    /**
     * Determines the process instance to suspend when the requested event
     * occurs in the target BPEL Engine.
     * 
     * Use {@link SuspendPolicy#SUSPEND_ALL}
     * to suspend all process instances in the target BPEL Engine.
     * Use {@link SuspendPolicy#SUSPEND_EVENT_PROCESS_INSTANCE} to suspend only
     * the process instance which generated the event (the default).
     * Use {@link SuspendPolicy#SUSPEND_NONE} to suspend no process instances.
     *
     * @throws InvalidRequestStateException
     *  if this request is currently enabled or has been deleted.
     *  Suspend policy may only be set in disabled requests.
     */
    void setSuspendPolicy(SuspendPolicy policy);
    
    /**
     * Returns a value which describes the process instances to suspend when
     * the requested event occurs in the target BPEL Engine.
     *
     * @return the current suspend mode for this request
     */
    SuspendPolicy getSuspendPolicy();
    
    /**
     * Defines the possible values for suspend policy.
     */
    enum SuspendPolicy {
        
        /**
         * Suspend all process instances when the event occurs.
         */
        SUSPEND_ALL,
        
        /**
         * Suspend only the process instance which generated
         * the event when the event occurs.
         */
        SUSPEND_EVENT_PROCESS_INSTANCE,
        
        /**
         * Suspend no process instances when the event occurs.
         */
        SUSPEND_NONE
    }
}
