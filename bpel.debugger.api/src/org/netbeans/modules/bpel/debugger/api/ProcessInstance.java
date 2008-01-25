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

import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel;
import org.netbeans.modules.bpel.debugger.api.variables.Value;
import org.netbeans.modules.bpel.debugger.api.variables.Variable;

/**
 *
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public interface ProcessInstance {

    //TODO:change states to enum
    
    /** State is unknown. */
    int STATE_UNKNOWN       = 0;

    /** Process instance is currently running. */
    int STATE_RUNNING       = 1;

    /** Process instance has been suspended. */
    int STATE_SUSPENDED     = 2;

    /** Process instance completed its execution without fault. */
    int STATE_COMPLETED     = 3;
    
    /** Process instance completed its execution with fault. */
    int STATE_FAILED        = 4;
    
    /** Process instance has been terminated. */
    int STATE_TERMINATED    = 5;
    
    /**
     * Returns the name of the process instance.
     *
     * @return the name of the process instance;
     */
    String getName();
    
    /**
     * Returns the state of the process instance.
     *
     * @return the state of the process instance
     *
     * @see #STATE_UNKNOWN
     * @see #STATE_RUNNING
     * @see #STATE_SUSPENDED
     * @see #STATE_COMPLETED
     * @see #STATE_FAILED
     * @see #STATE_TERMINATED
     */
    int getState();
    
    /**
     * Returns position at which this process instance has been suspended.
     *
     * @return  current position at which this process instance has been
     *          suspended or <code>null</code> if it's not suspended
     *
     * @see #getState()
     */
    Position getCurrentPosition();
    
    BpelProcess getProcess();
    
    ProcessExecutionModel getProcessExecutionModel();
    
    void pause();
    
    /**
     * Resumes process instance if it's suspended or does nothing otherwise.
     */
    void resume();
    
    void stepInto();
    
    void stepOver();
    
    void stepOut();
    
    void terminate();
    
    Variable[] getVariables();
    
    RuntimePartnerLink[] getRuntimePartnerLinks();
    
    CorrelationSet[] getCorrelationSets();
    
    Fault[] getFaults();
    
    Value evaluate(String xpathExpression) throws InvalidStateException, EvaluationException;
}
