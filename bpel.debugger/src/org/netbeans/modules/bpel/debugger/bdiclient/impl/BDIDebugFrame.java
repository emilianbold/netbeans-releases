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

import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELVariable;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebugFrame;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebuggableEngine;
import org.netbeans.modules.bpel.debugger.BreakPosition;

/**
 * A DebugFrame represents one thread within a running BPEL process.
 * If a BPEL process has branched off multiple threads, there will be
 * one DebugFrame per thread.
 * 
 * This is a remote object, the BPEL service engine makes callbacks into
 * this. The methods called are defined in the DebugFrame interface.
 * We receive onLineChange events for each activity executed.
 * 
 * @author Sun Microsystems
 */
public class BDIDebugFrame implements DebugFrame {
    
    private ProcessInstanceImpl myProcessInstance;
    private String myId;
    
    private DebuggableEngine myDebuggableEngine;

    public BDIDebugFrame(
            final ProcessInstanceImpl processInstance, 
            final String id) {
        myProcessInstance = processInstance;
        myId = id;
    }

    public String getId() {
        return myId;
    }

    // TODO: avoid storing DebuggableEngine in DebugFrame.
    // Store it in BreakPosition instead
    public DebuggableEngine getDebuggableEngine() {
        return myDebuggableEngine;
    }
    
    public ProcessInstanceImpl getProcessInstance() {
        return myProcessInstance;
    }
    
    public void onLineChange(
            final String bpelFile, 
            final String uri, 
            final int lineNumber, 
            final String xpath, 
            final DebuggableEngine engine) {
        System.out.println("- onLineChange(" + lineNumber + ", " + 
                xpath + ")");
        
        myDebuggableEngine = engine;
        
        myProcessInstance.onActivityStarted(
                new BreakPosition(this, xpath, lineNumber));
    }

    public void onXPathException(
            final String bpelFile, 
            final String uri, 
            final int lineNumber, 
            final String message, 
            final String xpath) {
        System.out.println("- onXPathException(" + lineNumber + ", " + 
                message + ", " + xpath + ")");
        
        myProcessInstance.onXpathException(this);
    }
    
    public void onTerminate(
            final String bpelFile, 
            final String uri, 
            final int lineNumber, 
            final String xpath) {
        System.out.println("- onTerminate(" + lineNumber + ", " + 
                xpath + ")");
        
        myProcessInstance.onTerminate(
                new BreakPosition(this, xpath, lineNumber));
    }
    
    public void onExit(
            final String bpelFile, 
            final String uri) {
        System.out.println("- onExit(" + uri + ")");
        
        myProcessInstance.onExit(this);
    }
    
    public void onFault(
            final String bpelFile, 
            final String uri, 
            final int lineNumber, 
            final String xpath, 
            final String faultQName, 
            final BPELVariable faultData, 
            final DebuggableEngine engine) {
        System.out.println("- onFault(" + lineNumber + ", " + 
                xpath + ", " + faultQName.replace("\n", "|") + 
                ", " + faultData + ")");
        
        myDebuggableEngine = engine;
        
        myProcessInstance.onFault(
                new BreakPosition(this, xpath, lineNumber), 
                faultQName, 
                faultData);
    }
    
    public void onActivityComplete(
            final String bpelFile, 
            final String uri, 
            final int lineNumber, 
            final String xpath) {
        System.out.println("- onActivityComplete(" + lineNumber + ", " + 
                xpath + ")");
        
        myProcessInstance.onActivityCompleted(
                new BreakPosition(this, xpath, lineNumber));
    }
    
    public void onSubActivityComplete(
            final String bpelFile, 
            final String uri, 
            final int lineNumber, 
            final String xpath) {
        System.out.println("- onSubActivityComplete(" + lineNumber + ", " + 
                xpath + ")");
        
        // We do not differentiate between full-blown activities (<assign>, 
        // <if>, etc) and subactivities such as <copy>, <elseif>
        myProcessInstance.onActivityCompleted(
                new BreakPosition(this, xpath, lineNumber));
    }
}
