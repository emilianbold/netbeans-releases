/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.debugger;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.bpel.debugger.api.AttachingCookie;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.DebugException;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.ProcessInstancesModel;
import org.netbeans.modules.bpel.debugger.api.SourcePath;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.bdiclient.BreakPosition;
import org.netbeans.modules.bpel.debugger.bdiclient.IDebugEngineConnector;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.BDIDebugConnector;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.ProcessInstanceImpl;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.ProcessInstancesModelImpl;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * This class provides the central debugging control for the BPEL debugger.
 * It delegates to a DebugEngineConnector for the implementation of how
 * a particular BPEL debugger is actually controlled.
 * 
 * @author Sun Microsystems
 */
public class BpelDebuggerImpl extends BpelDebugger {

    private static Logger LOGGER = Logger.getLogger(BpelDebuggerImpl.class.getName());
    
    private Thread mStartingThread;
    protected BDIDebugConnector mConnector;
    private int mState = STATE_DISCONNECTED;
    private Session mSession;
    private AtomicReference<BreakPosition> mCurrentPositionRef =
            new AtomicReference<BreakPosition>();
    private AtomicReference<ProcessInstanceImpl> mCurrentProcessInstanceRef =
            new AtomicReference<ProcessInstanceImpl>();
    
    private Map myBreakpointsMap = Collections.synchronizedMap(new IdentityHashMap());
    
    private SourcePath mSourcePath;
    private ProcessInstancesModelImpl mProcessInstancesModel;
    private ProcessInstancesModelListener mProcessInstancesModelListener;
    private DebugException mException;

    public BpelDebuggerImpl(ContextProvider lookupProvider) {
        super(lookupProvider);
        mSession = getEngineProvider().getSession();
        mProcessInstancesModel = new ProcessInstancesModelImpl(this);
        mProcessInstancesModelListener = new ProcessInstancesModelListener();
        mProcessInstancesModel.addListener(mProcessInstancesModelListener);
    }
    
    public SourcePath getSourcePath() {
        
        if (mSourcePath == null) {
            mSourcePath = (SourcePath)getLookupProvider().
                lookupFirst(null, SourcePath.class);
        }
        return mSourcePath;
    }
   
    /**
     * It should be called from an action as a part of a session temination.
     * Don't call it from anywhere except KillActionProvider.
     * Use Session.kill() instead.
     */
    public void finish() {
        int oldState = getState();
        if (oldState == STATE_DISCONNECTED) {
            return;
        }
        setState(STATE_DISCONNECTED);
        
        mProcessInstancesModel.removeListener(mProcessInstancesModelListener);
        for (ProcessInstanceImpl processInstance : mProcessInstancesModel.getProcessInstances()) {
            if (processInstance.getState() == processInstance.STATE_SUSPENDED) {
                processInstance.resume();
            }
        }
        //Just a back-up
        mProcessInstancesModel.clear();
        
        if (mStartingThread != null) {
            mStartingThread.interrupt();
            mStartingThread = null;
        }

        if (mConnector != null) {
            mConnector.detach();
        }
        
        if (oldState == BpelDebugger.STATE_STARTING) {
            if (getException() != null) {
                // TODO:change to NLS
                traceDebugException("Unable to start a debug session", getException());
            } else {
                //TODO:change to NLS
                getTracer().println("Stop connecting");
            }
        } else {
            if (getException() != null) {
                // TODO:change to NLS
                traceDebugException("Debug session terminated", getException());
            } else {
                // TODO:change to NLS
                getTracer().println("Debug session finished"); 
            }
        }

        if (getEngineProvider().getDestructor() != null) {
            getEngineProvider().getDestructor().killEngine();
        }
    }

    public void setException(DebugException e) {
        if (getState() == STATE_DISCONNECTED) {
            return;
        }
        
        mException = e;
        mSession.kill();
}

    private void setState(int state) {
        if (state == mState) {
            return;
        }
        int oldState = mState;
        mState = state;
        firePropertyChange(PROP_STATE, new Integer(oldState), new Integer(state));
    }

    public int getState() {
        return mState;
    }

    public void setStartingThread(Thread startingThread) {
        mStartingThread = startingThread;
        setState(STATE_STARTING);
    }

    public void unsetStartingThread() {
        mStartingThread = null;
    }

    public void setRunning(AttachingCookie attachCookie) {
        String host = attachCookie.getHost();
        int port = attachCookie.getPort();
        if (BDIDebugConnector.getDebugConnector(attachCookie) != null) {
            //TODO:change to NLS
            setException(new DebugException("Already connected to " + host + ":" + port));
            return;
        }
        
        //TODO:change to NLS
        getTracer().println("Connecting to " + host + ":" + port);
        mConnector = new BDIDebugConnector(this);
        if (!mConnector.isInitialized()) {
            setException(new DebugException(mConnector.getException()));
            return;
        }
        
        mConnector.attach(attachCookie);
        if (!mConnector.isAttached()) {
            //TODO: change to NLS
            setException(new DebugException("Unable to connect to " + host + ":" + port, mConnector.getException()));
            return;
        }
        
        synchronizeBreakpoints();
        
        //TODO:change to NLS
        getTracer().println("Debug session started");
        setState(STATE_RUNNING);
    }
    
    public ProcessInstancesModelImpl getProcessInstancesModel() {
        return mProcessInstancesModel;
    }
    
    public DebugException getException() {
        return mException;
    }
    
    /** 
     * Registers added breakpoint at the target BPEL engine.<br>
     * This method would normally be called from
     * <code>DebuggerManagerListener.breakpointAdded()</code> event handler.
     *
     * @param breakpoint breakpoint that has been added
     *
     * @see #breakpointRemoved
     * @see #getBreakpoints
     */
    public void breakpointAdded(BpelBreakpoint breakpoint) {
        if (breakpoint instanceof LineBreakpoint) {
            myBreakpointsMap.put(breakpoint, new SBYNActivityBreakpoint(
                    (LineBreakpoint) breakpoint, this));
        }
    }
    
    /** 
     * Removes registered breakpoint from the target BPEL engine.<br>
     * This method would normally be called from
     * <code>DebuggerManagerListener.breakpointRemoved()</code> event handler.
     *
     * @param breakpoint breakpoint that has been removed
     *
     * @see #breakpointAdded
     * @see #getBreakpoints
     */
    public void breakpointRemoved(BpelBreakpoint breakpoint) {
        myBreakpointsMap.remove(breakpoint);
    }
    
    public SBYNBreakpoint findBreakpoint(QName processQName, String xpath) {
        SBYNBreakpoint[] bps = getBreakpoints();
        for (SBYNBreakpoint bp : bps) {
            if (bp.isAt(processQName, xpath)) {
                return bp;
            }
        }
        return null;
    }
    
    private void synchronizeBreakpoints() {
        Breakpoint[] nbBreakpoints =
                DebuggerManager.getDebuggerManager().getBreakpoints();

        for (Breakpoint nbBreakpoint : nbBreakpoints) {
            if (nbBreakpoint instanceof LineBreakpoint) {
                LineBreakpoint lbp = (LineBreakpoint)nbBreakpoint;
                myBreakpointsMap.put(lbp, new SBYNActivityBreakpoint(lbp, this));
            }
        }
    }
    
    private void activateSession() {
        if (mSession != DebuggerManager.getDebuggerManager().getCurrentSession()) {
            DebuggerManager.getDebuggerManager().setCurrentSession(mSession);
        }
    }
    
    /**
     * Returns breakpoints that have been registered at the target BPEL engine.
     *
     * @return breakpoints that have been registered at the target BPEL engine
     *
     * @see #breakpointAdded
     * @see #breakpointRemoved
     */
    public SBYNBreakpoint[] getBreakpoints() {
        synchronized (myBreakpointsMap) {
            return (SBYNBreakpoint[]) myBreakpointsMap.values().toArray(new SBYNBreakpoint[myBreakpointsMap.size()]);
        }
    }
    
    private void setCurrentPosition(BreakPosition newPosition) {
        BreakPosition oldPosition =
                mCurrentPositionRef.getAndSet(newPosition);
        
        firePropertyChange(PROP_CURRENT_POSITION, oldPosition, newPosition);
        
        if (newPosition != null) {
            activateSession();
        }
    }
    
//  BpelDebugger interface methods
//  BpelDebugger interface methods
//  BpelDebugger interface methods

    
    /**
     * Causes current process instance to do a step into or does nothing if
     * there's no current process instance or it's not in the suspended state.
     */
    public void stepInto() {
        ProcessInstanceImpl processInstance = getCurrentProcessInstance();
        if (processInstance != null) {
            processInstance.stepInto();
        }
    }

    /**
     * Resumes the execution of the current process instance or does nothing if
     * there's no current process instance or it's not in the suspended state.
     */
    public void resume() {
        ProcessInstanceImpl processInstance = getCurrentProcessInstance();
        if (processInstance != null) {
            processInstance.resume();
        }
    }
    
    public BreakPosition getCurrentPosition() {
        return mCurrentPositionRef.get();
    }
    
    public ProcessInstanceImpl getCurrentProcessInstance() {
        return mCurrentProcessInstanceRef.get();
    }

    public int getCurrentProcessInstanceState() {
        ProcessInstanceImpl processInstance = mCurrentProcessInstanceRef.get();
        if (processInstance != null) {
            return processInstance.getState();
        } else {
            return ProcessInstance.STATE_UNKNOWN;
        }
    }

    public void setCurrentProcessInstance(ProcessInstance processInstance) {
        ProcessInstanceImpl newProcessInstance = (ProcessInstanceImpl)processInstance;
        ProcessInstanceImpl oldProcessInstance =
                mCurrentProcessInstanceRef.getAndSet(newProcessInstance);
        
        firePropertyChange(PROP_CURRENT_PROCESS_INSTANCE, oldProcessInstance, newProcessInstance);
        
        if (newProcessInstance != null) {
            setCurrentPosition(newProcessInstance.getCurrentPosition());
        } else {
            setCurrentPosition(null);
        }
    }
    
    private void traceDebugException(String message, DebugException dex) {
        if (dex == null) {
            getTracer().println(message);
            return;
        }
        
        StringBuffer sb = new StringBuffer(200);
        if (message != null) {
            sb.append(message);
        }
        if (dex.getMessage() != null) {
            if (sb.length() > 0) {
                sb.append(" : ");
            }
            sb.append(dex.getMessage());
        }
        
        if (dex.getCause() != null) {
            Throwable cause;
            if (dex.getCause() instanceof UndeclaredThrowableException) {
                cause = dex.getCause().getCause();
            } else {
                cause = dex.getCause();
            }
            
            if (cause.getMessage() != null) {
                if (sb.length() > 0) {
                    sb.append(" : ");
                }
                sb.append(cause.getMessage());
            }
        }
        
        getTracer().println(sb.toString());
    }
    
    private class ProcessInstancesModelListener implements ProcessInstancesModel.Listener {
        public void processInstanceRemoved(ProcessInstance processInstance) {
            if (processInstance == getCurrentProcessInstance()) {
                setCurrentProcessInstance(null);
            }
        }

        public void processInstanceAdded(ProcessInstance processInstance) {
            //Nothing to do
        }

        public void processInstanceStateChanged(ProcessInstance processInstance, int oldState, int newState) {
            if (newState == ProcessInstance.STATE_SUSPENDED) {
                setCurrentProcessInstance(processInstance);
            } else if (processInstance == getCurrentProcessInstance()) {
                setCurrentPosition(null);
            }
        }
    }
}
