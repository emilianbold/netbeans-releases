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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.namespace.QName;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.EvaluationException;
import org.netbeans.modules.bpel.debugger.api.InvalidStateException;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelFaultBreakpoint;
import org.netbeans.modules.bpel.debugger.api.variables.Value;
import org.netbeans.modules.bpel.debugger.api.variables.Variable;
import org.netbeans.modules.bpel.debugger.eventlog.ActivityCompletedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.ActivityStartedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.BranchCompletedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.BranchStartedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.EventLog;
import org.netbeans.modules.bpel.debugger.eventlog.EventRecord;
import org.netbeans.modules.bpel.debugger.eventlog.ProcessInstanceCompletedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.ProcessInstanceStartedRecord;
import org.netbeans.modules.bpel.debugger.pem.ProcessExecutionModelImpl;
import org.netbeans.modules.bpel.debugger.variables.SimpleValueImpl;
import org.netbeans.modules.bpel.debugger.variables.SimpleVariableImpl;
import org.netbeans.modules.bpel.debugger.variables.Util;
import org.netbeans.modules.bpel.debugger.variables.WsdlMessageVariableImpl;
import org.netbeans.modules.bpel.debugger.variables.XmlElementValueImpl;
import org.netbeans.modules.bpel.debugger.variables.XmlElementVariableImpl;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessInstanceRef;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELVariable;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebugFrame;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebuggableEngine;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.VirtualBPELEngine;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.XpathExpressionException;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;
import org.netbeans.modules.bpel.debugger.breakpoints.SBYNBreakpoint;
import org.netbeans.modules.bpel.debugger.BreakPosition;
import org.w3c.dom.Element;

/**
 *
 * @author Alexander Zgursky
 */
public class ProcessInstanceImpl implements ProcessInstance {
    
    private final String mId;
//    private final QName mProcessQName;
    private final BpelProcessImpl mBpelProcess;
    private final ProcessInstancesModelImpl mModel;
    private final BPELProcessInstanceRef mProcessInstanceRef;
    
    private BreakPosition mBreakPosition;
    private Object mWaitLock = new Object();
    private Object mSyncLock = new Object();
    private AtomicInteger mState = new AtomicInteger(STATE_RUNNING);

    private boolean mIsUndeployed;
    private boolean mBreakFlag;
    private final List<BDIDebugFrame> mFrames = Collections.synchronizedList(
            new ArrayList<BDIDebugFrame>());
    private EventLog myEventLog;
    private WeakReference<ProcessExecutionModelImpl> myPemRef;
    
    
    /** Creates a new instance of ProcessInstanceImpl */
    public ProcessInstanceImpl(
            ProcessInstancesModelImpl model,
            BPELProcessInstanceRef processInstanceRef)
    {
        mModel = model;
        mProcessInstanceRef = processInstanceRef;
        mBpelProcess = mModel.getProcess(mProcessInstanceRef.template());
//        String uri = processInstanceRef.template().uri();
//        mProcessQName = ProcessInstancesModelImpl.makeProcessQName(uri);
        mId = processInstanceRef.globalID();
    }
    
    public String getName() {
        return mBpelProcess.getName() + " #" + mId;
    }

    public int getState() {
        return mState.get();
    }

    public BreakPosition getCurrentPosition() {
        return mBreakPosition;
    }
    
    public void resume() {
        synchronized (mWaitLock) {
            mWaitLock.notifyAll();
        }
    }
    
    public void stepInto() {
        mBreakFlag = true;
        resume();
    }
    
    public void terminate() {
        BDIDebugger bdiDebugger = getDebugger().getBDIDebugger();
        if (bdiDebugger != null) {
            VirtualBPELEngine virtualBPELEngine = bdiDebugger.getVirtualBPELEngine();
            if (virtualBPELEngine != null) {
                virtualBPELEngine.terminatePI(mProcessInstanceRef);
            }
        }
    }

    public String getId() {
        return mId;
    }
    
    public BpelProcessImpl getProcess() {
        return mBpelProcess;
    }
    
    public QName getProcessQName() {
        return mBpelProcess.getQName();
    }

    public Variable[] getVariables() {
        BreakPosition activePosition = mBreakPosition;
        if (activePosition != null) {
            BDIDebugFrame currentFrame = activePosition.getFrame();
            DebuggableEngine engine = currentFrame.getDebuggableEngine();
            if (engine != null) {
                String[] engineVarNames = engine.getVariables();
                Variable[] vars = new Variable[engineVarNames.length];
                for (int i=0; i < engineVarNames.length; i++) {
                    String varName = engineVarNames[i];
                    BPELVariable engineVar = engine.getVariable(varName);
                    if (engineVar == null) {
                        System.out.println("engineVar is null for variable " + varName);
                        continue;
                    }
                    if (engineVar.isWSDLMessage()) {
                        vars[i] = new WsdlMessageVariableImpl(varName, activePosition, engineVar);
                    } else if (engineVar.isSimpleType()) {
                        vars[i] = new SimpleVariableImpl(varName, activePosition, engineVar);
                    } else {
                        vars[i] = new XmlElementVariableImpl(varName, activePosition, engineVar);
                    }
                }
                return vars;
            }
        }
        return new Variable[0];
    }

    public Value evaluate(String xpathExpression)
            throws InvalidStateException, EvaluationException
    {
        if (xpathExpression == null || xpathExpression.trim().equals("")) {
            return null;
        }
        
        Value result = null;
        BreakPosition activePosition = mBreakPosition;
        if (activePosition != null) {
            BDIDebugFrame currentFrame = activePosition.getFrame();
            DebuggableEngine engine = currentFrame.getDebuggableEngine();
            if (engine != null) {
                try {
                    String strResult = engine.evaluate(xpathExpression);
                    if (strResult != null) {
                        Element element = Util.parseXmlElement(strResult);
                        if (element != null) {
                            result = new XmlElementValueImpl(element);
                        } else {
                            result = new SimpleValueImpl(strResult);
                        }
                    }
                } catch (XpathExpressionException ex) {
                    //TODO:change to NLS
                    throw new EvaluationException("Can not evaluate the expression", ex);
                }
            }
        } else {
            //TODO:change to NLS
            throw new InvalidStateException("Process instance is not suspended");
        }
        return result;
    }
    
    public EventLog getEventLog() {
        return myEventLog;
    }
    
    public ProcessExecutionModelImpl getProcessExecutionModel() {
        ProcessExecutionModelImpl pem = null;
        if (myPemRef != null) {
            pem = myPemRef.get();
        }
        
        if (pem == null) {
            pem = ProcessExecutionModelImpl.build(this);
            myPemRef = new WeakReference<ProcessExecutionModelImpl>(pem);
        }
        
        return pem;
    }
    
    protected void setUndeployed(boolean isUndeployed) {
        mIsUndeployed = isUndeployed;
    }
    
    protected void onActivityStarted(BreakPosition breakPosition) {
        //TODO:why do we need this check?
        if (getDebugger().getState() != BpelDebugger.STATE_RUNNING) {
            return;
        }
        
        if (mIsUndeployed) {
            return;
        }
        
        synchronized (mSyncLock) {
            if (myEventLog != null) {
                EventRecord record = new ActivityStartedRecord(
                        breakPosition.getFrame().getId(),
                        breakPosition.getXpath());
                myEventLog.addRecord(record);
            }
            
            String path = getDebugger().getSourcePath().getSourcePath(breakPosition.getProcessQName());
            if (path == null) {
                return;
            }
            
            if (    mBreakFlag ||
                    getDebugger().hasBreakpoint(path, breakPosition.getXpath()))
            {
                mBreakFlag = false;
                getDebugger().clearRunToCursorBreakpoint();

                mBreakPosition = breakPosition;
                setState(STATE_SUSPENDED);
                doWait();
                mBreakPosition = null;
                setState(STATE_RUNNING);
            }
        }
    }
    
    protected void onActivityCompleted(BreakPosition breakPosition) {
        if (myEventLog != null) {
            EventRecord record = new ActivityCompletedRecord(
                    breakPosition.getFrame().getId(),
                    breakPosition.getXpath());
            myEventLog.addRecord(record);
        }
    }
    
    protected void onFault(BreakPosition breakPosition, String strFaultQName, BPELVariable faultData) {
        //TODO:why do we need this check?
        if (getDebugger().getState() != BpelDebugger.STATE_RUNNING) {
            return;
        }
        
        if (mIsUndeployed) {
            return;
        }
        
        synchronized (mSyncLock) {
            
            String path = getDebugger().getSourcePath().getSourcePath(breakPosition.getProcessQName());
            if (path == null) {
                return;
            }
            
            //TODO:search for Fault Breakpoint is inconsistent with
            //search for Line (Activity) breakpoint - the later uses NB
            //breakpoints
            Breakpoint[] nbBreakpoints =
                    DebuggerManager.getDebuggerManager().getBreakpoints();
            
            QName faultQName = ((strFaultQName == null) || (strFaultQName.trim().equals(""))) ?
                null : QName.valueOf(strFaultQName);
                
            boolean foundfb = false;
            for (Breakpoint nbBreakpoint : nbBreakpoints) {
                if (nbBreakpoint instanceof BpelFaultBreakpoint) {
                    BpelFaultBreakpoint fb = (BpelFaultBreakpoint)nbBreakpoint;
                    if (!nbBreakpoint.isEnabled()) {
                        continue;
                    }
                    if (fb.getProcessQName().equals(breakPosition.getProcessQName())) {
                        if (fb.getFaultQName() == null) {
                            foundfb = true;
                            break;
                        } else if (fb.getFaultQName().equals(faultQName)) {
                            foundfb = true;
                            break;
                        }
                    }
                }
            }
            
            if (foundfb) {
                mBreakFlag = false;
                getDebugger().clearRunToCursorBreakpoint();
                mBreakPosition = breakPosition;
                setState(STATE_SUSPENDED);
                doWait();
                mBreakPosition = null;
                setState(STATE_RUNNING);
            }
        }
    }

    protected void onXpathException(DebugFrame frame) {
        if (getRootFrame() == frame) {
            setState(STATE_FAILED);
        }
    }
    
    protected void onProcessInstanceStarted() {
        myEventLog = new EventLog();
        EventRecord record = new ProcessInstanceStartedRecord();
        myEventLog.addRecord(record);
    }
    
    protected void onProcessInstanceDied() {
        setState(STATE_COMPLETED);
        if (myEventLog != null) {
            EventRecord record = new ProcessInstanceCompletedRecord();
            myEventLog.addRecord(record);
        }
        mModel.processInstanceCompleted(this);
    }

    protected void onTerminate(DebugFrame frame) {
//        if (getRootFrame() == frame) {
//            setState(STATE_TERMINATED);
//            mModel.processInstanceCompleted(this);
//        }
    }

    protected void onExit(BDIDebugFrame frame) {
        if (myEventLog != null) {
            EventRecord record = new BranchCompletedRecord(frame.getId());
            myEventLog.addRecord(record);
        }
    }
    
    protected BDIDebugFrame addFrame(String id, String parentFrameId) {
        BDIDebugFrame frame = new BDIDebugFrame(this, id);
        mFrames.add(frame);
        if (myEventLog != null) {
            EventRecord record = new BranchStartedRecord(id, parentFrameId);
            myEventLog.addRecord(record);
        }
        return frame;
    }
    
    private void setState(int newState) {
        int oldState = mState.getAndSet(newState);
        mModel.processInstanceStateChanged(this, oldState, newState);
    }
    
    private void doWait() {
        try {
            synchronized (mWaitLock) {
                //TODO:Always put wait() in a condition loop
                //since wait() can sometimes stop waiting for some reason - see
                //wait() javadoc.
                mWaitLock.wait();
            }
        } catch (InterruptedException ie) {
            // process interrupted
        }
    }
    
    //TODO:get rid of the notion of "root frame", since there may be more
    //than one "root frame" e.g. main activity frame and event frame
    private BDIDebugFrame getRootFrame() {
        return mFrames.get(0);
    }
    
    private BpelDebuggerImpl getDebugger() {
        return mModel.getDebugger();
    }
}
