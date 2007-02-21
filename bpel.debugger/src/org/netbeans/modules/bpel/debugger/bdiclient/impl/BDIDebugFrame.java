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
    
    private ProcessInstanceImpl mProcessInstance;
    private String mId;
    
    private DebuggableEngine mEngine;

    public BDIDebugFrame(ProcessInstanceImpl processInstance, String id) {
        mProcessInstance = processInstance;
        mId = id;
    }

    public String getId() {
        return mId;
    }

    //TODO:avoid storing DebuggableEngine in DebugFrame.
    //Store it in BreakPosition instead
    public DebuggableEngine getDebuggableEngine() {
        return mEngine;
    }
    
    public ProcessInstanceImpl getProcessInstance() {
        return mProcessInstance;
    }
    
//  DebugFrame interface methods
//  DebugFrame interface methods
//  DebugFrame interface methods

    public void onLineChange(String bpelFile, String uri, int lineNumber, String xpath, DebuggableEngine engine) {
        if (ignoreEvent(xpath)) {
            return;
        }
        //System.out.println("   Activity started in frame " + mId + ": " + xpath);
        mEngine = engine;
        BreakPosition breakPosition = new BreakPosition(this, xpath);
        mProcessInstance.onActivityStarted(breakPosition);
    }
    
    public void onFault(String bpelFile, String uri, int lineNumber, String xpath) {
        //mProcessInstance.onFault(this);
    }

    public void onXPathException(String bpelFile, String uri, int lineNumber, String message, String xpath) {
        mProcessInstance.onXpathException(this);
    }

    public void onTerminate(String bpelFile, String uri, int lineNumber, String xpath) {
        if (ignoreEvent(xpath)) {
            return;
        }
        //System.out.println("   Activity terminated in frame " + mId + ": " + xpath);
        mProcessInstance.onTerminate(this);
    }

    public void onExit(String bpelFile, String uri) {
        //System.out.println("Exit frame: " + mId);
        mProcessInstance.onExit(this);
    }
    
    public void onFault(String bpelFile, String uri, int lineNumber, String xpath, String faultQName, BPELVariable faultData, DebuggableEngine engine) {
        if (ignoreEvent(xpath)) {
            return;
        }
        //System.out.println("   !!!Fault in frame " + mId + ": " + xpath);
        mEngine = engine;
        BreakPosition breakPosition = new BreakPosition(this, xpath);
        mProcessInstance.onFault(breakPosition, faultQName, faultData);
    }
    
    public void onActivityComplete(String bpelFile, String uri, int lineNumber, String xpath) {
        if (ignoreEvent(xpath)) {
            return;
        }
        //System.out.println("   Activity completed in frame " + mId + ": " + xpath);
        BreakPosition breakPosition = new BreakPosition(this, xpath);
        mProcessInstance.onActivityCompleted(breakPosition);
    }
    
    private boolean ignoreEvent(String xpath) {
        return  xpath.endsWith("onAlarm") ||
                xpath.endsWith("onAlarm[1]") ||
                xpath.endsWith("onEvent") ||
                xpath.endsWith("onEvent[1]");
    }
}
