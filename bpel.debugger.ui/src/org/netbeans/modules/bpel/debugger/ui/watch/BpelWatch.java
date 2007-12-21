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
package org.netbeans.modules.bpel.debugger.ui.watch;

import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.InvalidStateException;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.variables.Value;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Zgursky
 */
public class BpelWatch {
    private BpelDebugger myDebugger;
    private Watch myNbWatch;
    private Value myValue;
    private Exception myException;
    
    /** Creates a new instance of BpelWatchImpl. */
    public BpelWatch(
            final BpelDebugger debugger, 
            final Watch nbWatch) {
        
        myDebugger = debugger;
        myNbWatch = nbWatch;
        evaluate();
    }

    public String getExpression() {
        return myNbWatch.getExpression();
    }

    public void setExpression(
            final String expression) {
        
        myNbWatch.setExpression(expression);
        
        //don't need to re-evaluate since BPELWatchImpl will be re-created
        //evaluate();
    }

    public void remove() {
        myNbWatch.remove();
        
        //TODO:implement
    }
    
    public Exception getException() {
        return myException;
    }
    
    public Value getValue() {
        return myValue;
    }
    
    /** 
     * Returns ths original NetBeans {@link Watch} object associated with this
     * instance.
     * 
     * @return {@link Watch} object instance.
     */
    public Watch getNbWatch() {
        return myNbWatch;
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void evaluate() {
        final ProcessInstance processInstance = 
                myDebugger.getCurrentProcessInstance();
        
        if (processInstance != null) {
            try {
                myValue = processInstance.evaluate(myNbWatch.getExpression());
            } catch (Exception e) {
                myException = e;
            }
        } else {
            myException = new InvalidStateException(
                    NbBundle.getMessage(BpelWatch.class, "BW_NoPI")); // NOI18N
        }
    }
}
