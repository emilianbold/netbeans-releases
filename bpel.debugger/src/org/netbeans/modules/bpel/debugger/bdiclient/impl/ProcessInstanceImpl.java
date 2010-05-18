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
import java.util.LinkedList;
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
import org.netbeans.modules.bpel.debugger.BreakPosition;
import org.netbeans.modules.bpel.debugger.api.CorrelationSet;
import org.netbeans.modules.bpel.debugger.api.Fault;
import org.netbeans.modules.bpel.debugger.api.RuntimePartnerLink;
import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel.Branch;
import org.netbeans.modules.bpel.debugger.eventlog.ActivityTerminatedRecord;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELPartnerLink;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessRef;
import org.w3c.dom.Element;

/**
 *
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class ProcessInstanceImpl implements ProcessInstance {
    
    private final String myId;
    private final BpelProcessImpl myBpelProcess;
    private final ProcessInstancesModelImpl myModel;
    private final BPELProcessInstanceRef myProcessInstanceRef;
    
    private BreakPosition myBreakPosition;
    private BreakPosition myLastPosition;
    private Object myWaitLock = new Object();
    private Object mySyncLock = new Object();
    private AtomicInteger myState = new AtomicInteger(STATE_RUNNING);

    private boolean myIsUndeployed;
    private boolean myStepIntoFlag;
    private boolean myStepOverFlag;
    private boolean myStepOutFlag;
    private boolean myPauseFlag;
    private final List<BDIDebugFrame> myFrames = 
            Collections.synchronizedList(new ArrayList<BDIDebugFrame>());
    private EventLog myEventLog;
    private WeakReference<ProcessExecutionModelImpl> myPemRef;
    
    private List<Fault> myFaults = new LinkedList<Fault>();
    
    
    /** Creates a new instance of ProcessInstanceImpl */
    public ProcessInstanceImpl(
            final ProcessInstancesModelImpl model,
            final BPELProcessInstanceRef processInstanceRef) {
        
        myModel = model;
        myProcessInstanceRef = processInstanceRef;
        myBpelProcess = myModel.getProcess(myProcessInstanceRef.template());
        myId = processInstanceRef.globalID();
    }
    
    public String getName() {
        return myBpelProcess.getName() + " #" + myId;
    }

    public int getState() {
        return myState.get();
    }

    public BreakPosition getCurrentPosition() {
        return myBreakPosition;
    }
    
    public void pause() {
        myPauseFlag = true;
    }
    
    public void resume() {
        synchronized (myWaitLock) {
            myWaitLock.notifyAll();
        }
    }
    
    public void stepInto() {
        myStepIntoFlag = true;
        resume();
    }
    
    public void stepOver() {
        myStepOverFlag = true;
        myLastPosition = myBreakPosition;
        
        resume();
    }
    
    public void stepOut() {
        myStepOutFlag = true;
        myLastPosition = myBreakPosition;
        
        resume();
    }
    
    public void terminate() {
        final BDIDebugger debugger = getDebugger().getBDIDebugger();
        
        if (debugger != null) {
            final VirtualBPELEngine engine = 
                    debugger.getVirtualBPELEngine();
            
            if (engine != null) {
                engine.terminatePI(myProcessInstanceRef.globalGUID());
            }
        }
    }

    public String getId() {
        return myId;
    }
    
    public BpelProcessImpl getProcess() {
        return myBpelProcess;
    }
    
    public QName getProcessQName() {
        return myBpelProcess.getQName();
    }

    public Variable[] getVariables() {
        final BreakPosition position = myBreakPosition;
        
        if (position != null) {
            final BDIDebugFrame frame = position.getFrame();
            final DebuggableEngine engine = frame.getDebuggableEngine();
            
            if (engine != null) {
                final String[] names = engine.getVariables();
                final Variable[] variables = new Variable[names.length];
                
                for (int i = 0; i < names.length; i++) {
                    final String name = names[i];
                    final BPELVariable engineVar = engine.getVariable(name);
                    
                    if (engineVar == null) {
                        continue;
                    }
                    
                    if (engineVar.isWSDLMessage()) {
                        variables[i] = new WsdlMessageVariableImpl(
                                name, position, engineVar);
                    } else if (engineVar.isSimpleType()) {
                        variables[i] = new SimpleVariableImpl(
                                name, position, engineVar);
                    } else {
                        variables[i] = new XmlElementVariableImpl(
                                name, position, engineVar);
                    }
                }
                
                return variables;
            }
        }
        
        return new Variable[0];
    }
    
    public RuntimePartnerLink[] getRuntimePartnerLinks() {
        final BreakPosition position = myBreakPosition;
        
        if (position != null) {
            final BDIDebugFrame frame = position.getFrame();
            final DebuggableEngine engine = frame.getDebuggableEngine();
            
            if (engine != null) {
                final String[] names = engine.getPartnerLinks();
                final RuntimePartnerLink[] pLinks = new RuntimePartnerLink[names.length];
                
                for (int i = 0; i < names.length; i++) {
                    final BPELPartnerLink enginePLink = 
                            engine.getPartnerLink(names[i]);
                    
                    if (enginePLink == null) {
                        continue;
                    }
                    
                    pLinks[i] = new RuntimePartnerLinkImpl(enginePLink);
                }
                
                return pLinks;
            }
        }
        
        return new RuntimePartnerLink[0];
    }
    
    public CorrelationSet[] getCorrelationSets() {
        final List<CorrelationSet> list = new LinkedList<CorrelationSet>();
        
        final BPELProcessRef processRef = myBpelProcess.getProcessRef();
        
        for (String name: processRef.allCorrelationSetsNames()) {
            final String value = 
                    myProcessInstanceRef.getCorrelationSetValue(name);
            
            if (value == null) {
                list.add(new CorrelationSetImpl(
                        name, 
                        processRef.getCorrelationSetId(name), 
                        null,
                        new QName[0],
                        new QName[0],
                        new String[0]));
            } else {
                String[] names = myProcessInstanceRef.getCorrelationSetPropertyNames(name);
                String[] types = myProcessInstanceRef.getCorrelationSetPropertyTypes(name);
                String[] values = myProcessInstanceRef.getCorrelationSetPropertyValues(name);
                
                QName[] realNames = new QName[names.length];
                QName[] realTypes = new QName[names.length];
                
                for (int i = 0; i < names.length; i++) {
                    String[] temp;
                    
                    temp = names[i].split("\n");
                    realNames[i] = new QName(temp[0], temp[2], temp[1]);
                    
                    temp = types[i].split("\n");
                    realTypes[i] = new QName(temp[0], temp[2], temp[1]);
                }
                
                list.add(new CorrelationSetImpl(
                        name, 
                        processRef.getCorrelationSetId(name), 
                        value,
                        realNames,
                        realTypes,
                        values));
            }
        }
        
        return list.toArray(new CorrelationSet[list.size()]);
    }
    
    public Fault[] getFaults() {
        return myFaults.toArray(new Fault[myFaults.size()]);
    }
    
    public Value evaluate(
            final String expression) 
            throws InvalidStateException, EvaluationException {
        
        if (expression == null || expression.trim().equals("")) {
            return null;
        }
        
        final BreakPosition position = myBreakPosition;
        if (position != null) {
            final BDIDebugFrame frame = position.getFrame();
            final DebuggableEngine engine = frame.getDebuggableEngine();
            
            if (engine != null) {
                try {
                    final String string = engine.evaluate(expression);
                    
                    if (string != null) {
                        final Element element = Util.parseXmlElement(string);
                        
                        if (element != null) {
                            return new XmlElementValueImpl(element);
                        } else {
                            return new SimpleValueImpl(string);
                        }
                    }
                } catch (XpathExpressionException ex) {
                    //TODO:change to NLS
                    throw new EvaluationException(
                            "Can not evaluate the expression", ex);
                }
            }
        } else {
            //TODO:change to NLS
            throw new InvalidStateException(
                    "Process instance is not suspended");
        }
        
        return null;
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
    
    // Protected ///////////////////////////////////////////////////////////////
    protected void setUndeployed(
            final boolean isUndeployed) {
        
        myIsUndeployed = isUndeployed;
    }
    
    protected void onActivityStarted(
            final BreakPosition position) {
        
        //TODO:why do we need this check?
        if (getDebugger().getState() != BpelDebugger.STATE_RUNNING) {
            return;
        }
        
        if (myIsUndeployed) {
            return;
        }
        
        synchronized (mySyncLock) {
            if (myEventLog != null) {
                myEventLog.addRecord(new ActivityStartedRecord(
                        position.getFrame().getId(),
                        position.getXpath()));
            }
            
            final String path = getDebugger().getSourcePath().getSourcePath(
                    position.getProcessQName());
            if (path == null) {
                return;
            }
            
            if (myPauseFlag ||
                    stepIntoSatisfied(position) || 
                    stepOverSatisfied(position) ||
                    stepOutSatisfied(position) ||
                    getDebugger().hasBreakpoint(path, position.getXpath())) {
                
                myPauseFlag = false;
                myStepIntoFlag = false;
                myStepOverFlag = false;
                myStepOutFlag = false;
                getDebugger().clearRunToCursorBreakpoint();
                
                getProcessExecutionModel().
                        setCurrentBranchWithoutResume(position.getBranchId());
                
                myBreakPosition = position;
                setState(STATE_SUSPENDED);
                doWait();
                myBreakPosition = null;
                setState(STATE_RUNNING);
            } else {
                // This call mught seem redundant, but it triggers a bunch of 
                // important updates, like BpelDebugger.setCurrentPosition(null)
                // which causes the annotations to be redrawn.
                setState(STATE_RUNNING);
            }
        }
    }
    
    protected void onActivityCompleted(
            final BreakPosition position) {
        
        if (myEventLog != null) {
            myEventLog.addRecord(new ActivityCompletedRecord(
                    position.getFrame().getId(),
                    position.getXpath()));
        }
    }
    
    protected void onFault(
            final BreakPosition position, 
            final String strFaultQName, 
            final BPELVariable faultData) {
        
        //TODO:why do we need this check?
        if (getDebugger().getState() != BpelDebugger.STATE_RUNNING) {
            return;
        }
        
        if (myIsUndeployed) {
            return;
        }
        
        synchronized (mySyncLock) {
            final QName faultQName;
            if ((strFaultQName == null) || strFaultQName.trim().equals("")) {
                faultQName = null;
            } else {
                final String[] temp = strFaultQName.split("\n");
                
                faultQName = new QName(temp[0], temp[2], temp[1]);
            }
            
            // add the fault to the list, so it can be displayed by the 
            // processes view
            if (faultData == null) {
                myFaults.add(new FaultImpl(
                        faultQName, 
                        position.getXpath(), 
                        null));
            } else if (faultData.isWSDLMessage()) {
                myFaults.add(new FaultImpl(
                        faultQName, 
                        position.getXpath(), 
                        new WsdlMessageVariableImpl(
                                "faultData", position, faultData)));
            } else if (faultData.isSimpleType()) {
                myFaults.add(new FaultImpl(
                        faultQName, 
                        position.getXpath(), 
                        new SimpleVariableImpl(
                                "faultData", position, faultData)));
            } else {
                myFaults.add(new FaultImpl(
                        faultQName, 
                        position.getXpath(), 
                        new XmlElementVariableImpl(
                                "faultData", position, faultData)));
            }
            
            final String path = getDebugger().getSourcePath().getSourcePath(
                    position.getProcessQName());
            if (path == null) {
                return;
            }
            
            //TODO:search for Fault Breakpoint is inconsistent with
            //search for Line (Activity) breakpoint - the later uses NB
            //breakpoints
            final Breakpoint[] nbBreakpoints =
                    DebuggerManager.getDebuggerManager().getBreakpoints();
            
            boolean foundfb = false;
            for (Breakpoint nbBreakpoint : nbBreakpoints) {
                if (nbBreakpoint instanceof BpelFaultBreakpoint) {
                    final BpelFaultBreakpoint fb = 
                            (BpelFaultBreakpoint) nbBreakpoint;
                    
                    if (!nbBreakpoint.isEnabled()) {
                        continue;
                    }
                    
                    if (fb.getProcessQName().equals(position.getProcessQName())) {
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
                myStepIntoFlag = false;
                getDebugger().clearRunToCursorBreakpoint();
                myBreakPosition = position;
                setState(STATE_SUSPENDED);
                doWait();
                myBreakPosition = null;
                setState(STATE_RUNNING);
            }
        }
    }

    protected void onXpathException(
            final DebugFrame frame) {
        
        if (getRootFrame() == frame) {
            setState(STATE_FAILED);
        }
    }
    
    protected void onProcessInstanceStarted() {
        myEventLog = new EventLog();
        myEventLog.addRecord(new ProcessInstanceStartedRecord());
    }
    
    protected void onProcessInstanceDied() {
        setState(STATE_COMPLETED);
        
        if (myEventLog != null) {
            myEventLog.addRecord(new ProcessInstanceCompletedRecord());
        }
        
        myModel.processInstanceCompleted(this);
    }
    
    protected void onTerminate(
            final BreakPosition position) {
        if (myEventLog != null) {
            myEventLog.addRecord(new ActivityTerminatedRecord(
                    position.getFrame().getId(),
                    position.getXpath()));
        }
    }
    
    protected void onExit(
            final BDIDebugFrame frame) {
        
        if (myEventLog != null) {
            myEventLog.addRecord(new BranchCompletedRecord(frame.getId()));
        }
    }
    
    protected BDIDebugFrame addFrame(
            final String id, 
            final String parentFrameId) {
        final BDIDebugFrame frame = new BDIDebugFrame(this, id);
        
        myFrames.add(frame);
        
        if (myEventLog != null) {
            myEventLog.addRecord(new BranchStartedRecord(id, parentFrameId));
        }
        
        return frame;
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void setState(
            final int newState) {
        int oldState = myState.getAndSet(newState);
        myModel.processInstanceStateChanged(this, oldState, newState);
    }
    
    private void doWait() {
        try {
            synchronized (myWaitLock) {
                //TODO:Always put wait() in a condition loop
                //since wait() can sometimes stop waiting for some reason - see
                //wait() javadoc.
                myWaitLock.wait();
            }
        } catch (InterruptedException ie) {
            // process interrupted
        }
    }
    
    private boolean stepIntoSatisfied(
            final BreakPosition position) {
        if (!myStepIntoFlag) {
            return false;
        }
        
        // If the current branch is not that of the position -- we should not 
        // stop
        final Branch branch = getProcessExecutionModel().getCurrentBranch();
        if ((branch == null) || 
                !position.getBranchId().equals(branch.getId())) {
            return false;
        }
        
        return true;
    }
    
    private boolean stepOverSatisfied(
            final BreakPosition position) {
        if (!myStepOverFlag) {
            return false;
        }
        
        // If the current branch is not that of the position -- we should not 
        // stop
        final Branch branch = getProcessExecutionModel().getCurrentBranch();
        if ((branch == null) || 
                !position.getBranchId().equals(branch.getId())) {
            return false;
        }
        
        final String lastXpath = myLastPosition.getXpath();
        final String currentXpath = position.getXpath();
        
        // We stop after step over if the current element is not "inside" the 
        // last element
        if (!currentXpath.startsWith(lastXpath) || 
                currentXpath.equals(lastXpath)) {
            return true;
        }
        
        return false;
    }
    
    private boolean stepOutSatisfied(
            final BreakPosition position) {
        if (!myStepOutFlag) {
            return false;
        }
        
        // If the current branch is not that of the position -- we should not 
        // stop
        final Branch branch = getProcessExecutionModel().getCurrentBranch();
        if ((branch == null) || 
                !position.getBranchId().equals(branch.getId())) {
            return false;
        }
        
        final String lastXpath = myLastPosition.getXpath();
        final String currentXpath = position.getXpath();
        
        final String lastXpathParent = 
                lastXpath.substring(0, lastXpath.lastIndexOf("/"));
        
        // We stop after step out if the current element "contains" the last 
        // one
        if (!currentXpath.startsWith(lastXpathParent)) {
            return true;
        }
        
        return false;
    }
    
    //TODO:get rid of the notion of "root frame", since there may be more
    //than one "root frame" e.g. main activity frame and event frame
    private BDIDebugFrame getRootFrame() {
        return myFrames.get(0);
    }
    
    private BpelDebuggerImpl getDebugger() {
        return myModel.getDebugger();
    }
}
