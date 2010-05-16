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


package org.netbeans.modules.bpel.debugger;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.DebugException;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.ProcessInstancesModel;
import org.netbeans.modules.bpel.debugger.api.SourcePath;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.BDIDebugConnector;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.BDIDebugger;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.ProcessInstanceImpl;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.ProcessInstancesModelImpl;
import org.netbeans.modules.bpel.debugger.breakpoints.SBYNActivityBreakpoint;
import org.netbeans.modules.bpel.debugger.breakpoints.SBYNBreakpoint;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.util.NbBundle;

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
    private BpelDebuggerEngineProvider    myDebuggerEngineProvider;
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
    private SBYNActivityBreakpoint mRunToCursorBreakpoint;

    public BpelDebuggerImpl(ContextProvider lookupProvider) {
        super(lookupProvider);
        
        //TODO:why not just call lookupFirst?
        List<? extends DebuggerEngineProvider> l = lookupProvider.lookup(null, DebuggerEngineProvider.class);
        int i;
        int k = l.size();
        
        for (i = 0; i < k; i++) {
            if (l.get(i) instanceof BpelDebuggerEngineProvider) {
                myDebuggerEngineProvider = (BpelDebuggerEngineProvider) l.get(i);
            }
        }
        
        if (myDebuggerEngineProvider == null) {
            throw new IllegalArgumentException
                    ("BpelDebuggerEngineProvider have to be used " + // NOI18N
                    "to start BpelDebugger!");  // NOI18N
        }
        
//        myDebuggerEngineProvider = (DebuggerEngineProvider)lookupProvider.lookupFirst(null, DebuggerEngineProvider.class);
        mSession = myDebuggerEngineProvider.getSession();
        mProcessInstancesModel = new ProcessInstancesModelImpl(this);
        mProcessInstancesModelListener = new ProcessInstancesModelListener();
        mProcessInstancesModel.addListener(mProcessInstancesModelListener);
    }
    
    public SourcePath getSourcePath() {
        
        if (mSourcePath == null) {
            mSourcePath = getLookupProvider().lookupFirst(null, SourcePath.class);
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
            if (processInstance.getState() == ProcessInstance.STATE_SUSPENDED) {
                processInstance.resume();
            }
        }
        //Just a back-up
        mProcessInstancesModel.clear();
        
        if (mStartingThread != null) {
            mStartingThread.interrupt();
            mStartingThread = null;
        }
        
        Exception disconnectionException = null;
        if (mConnector != null) {
            try {
                mConnector.detach();
            } catch (Exception e) {
                disconnectionException = e;
            }
        }
        
        if (oldState == BpelDebugger.STATE_STARTING) {
            if (getException() != null) {
                traceDebugException(NbBundle.getMessage(
                        BpelDebuggerImpl.class, 
                        "ERR_UnableToStartSession"), getException());
            } else {
                getTracer().println(NbBundle.getMessage(
                        BpelDebuggerImpl.class, 
                        "MSG_StopConnecting"));
            }
        } else {
            if (getException() != null) {
                traceDebugException(NbBundle.getMessage(
                        BpelDebuggerImpl.class, 
                        "ERR_SessionTerminated"), getException());
            } else {
                getTracer().println(NbBundle.getMessage(
                        BpelDebuggerImpl.class, 
                        "MSG_SessionFinished")); 
            }
        }
        
        if (disconnectionException != null) {
            traceDebugException(NbBundle.getMessage(
                    BpelDebuggerImpl.class, 
                    "ERR_ErrorWhileDisconnecting"), disconnectionException);
        }
        
        if (myDebuggerEngineProvider.getDestructor() != null) {
            myDebuggerEngineProvider.getDestructor().killEngine();
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

    public void setRunning(String host, int port) {
        if (BDIDebugConnector.getDebugConnector(host, port) != null) {
            setException(new DebugException(NbBundle.getMessage(
                        BpelDebuggerImpl.class, 
                        "ERR_AlreadyConnected",
                        host,
                        "" + port)));
            return;
        }
        
        getTracer().println(NbBundle.getMessage(
                    BpelDebuggerImpl.class, 
                    "MSG_Connecting",
                    host,
                    "" + port));
        mConnector = new BDIDebugConnector(this);
        if (!mConnector.isInitialized()) {
            setException(new DebugException(mConnector.getException()));
            return;
        }
        
        mConnector.attach(host, port);
        if (!mConnector.isAttached()) {
            setException(new DebugException(NbBundle.getMessage(
                    BpelDebuggerImpl.class, 
                    "ERR_UnableToConnect",
                    host,
                    "" + port), mConnector.getException()));
            return;
        }
        
        synchronizeBreakpoints();
        
        getTracer().println(NbBundle.getMessage(
                    BpelDebuggerImpl.class, 
                    "MSG_SessionStarted"));
        setState(STATE_RUNNING);
    }
    
    public ProcessInstancesModelImpl getProcessInstancesModel() {
        return mProcessInstancesModel;
    }
    
    public DebugException getException() {
        return mException;
    }
    
    public BDIDebugger getBDIDebugger() {
        if (mConnector != null) {
            return mConnector.getBDIDebugger();
        } else {
            return null;
        }
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
    
    public boolean hasBreakpoint(String path, String xpath) {
        SBYNBreakpoint[] bps = getBreakpoints();
        for (SBYNBreakpoint bp : bps) {
            if (    bp.isEnabled() &&
                    new File(path).equals(new File(bp.getURL())) &&
                    xpath.equals(bp.getXpath()))
            {
                return true;
            }
        }
        
        if (    mRunToCursorBreakpoint != null &&
                new File(path).equals(new File(mRunToCursorBreakpoint.getURL())) &&
                xpath.equals(mRunToCursorBreakpoint.getXpath()))
        {
            return true;
        }
        
        return false;
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
    
    public void stepInto() {
        final ProcessInstanceImpl processInstance = getCurrentProcessInstance();
        
        if (processInstance != null) {
            processInstance.stepInto();
        }
    }

    public void stepOver() {
        final ProcessInstanceImpl processInstance = getCurrentProcessInstance();
        
        if (processInstance != null) {
            processInstance.stepOver();
        }
    }

    public void stepOut() {
        final ProcessInstanceImpl processInstance = getCurrentProcessInstance();
        
        if (processInstance != null) {
            processInstance.stepOut();
        }
    }
    
    public void pause() {
        for (ProcessInstanceImpl instance: 
                mProcessInstancesModel.getProcessInstances()) {
                    
            instance.pause();
        }
    }
    
    public void resume() {
        final ProcessInstanceImpl processInstance = getCurrentProcessInstance();
        
        if (processInstance != null) {
            processInstance.resume();
        }
        
        setCurrentPosition(null);
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
        ProcessInstanceImpl newProcessInstance = 
                (ProcessInstanceImpl) processInstance;
        
        // Automatically switch oto the first suspended process instance in
        // case we're about to lose the current one
        if (newProcessInstance == null) {
            for (ProcessInstanceImpl instance: 
                    mProcessInstancesModel.getProcessInstances()) {
                if (instance.getState() == ProcessInstance.STATE_SUSPENDED) {
                    newProcessInstance = instance;
                    break;
                }
            }
        }
        
        final ProcessInstanceImpl oldProcessInstance =
                mCurrentProcessInstanceRef.getAndSet(newProcessInstance);
        
        firePropertyChange(
                PROP_CURRENT_PROCESS_INSTANCE, 
                oldProcessInstance, 
                newProcessInstance);
        
        if (newProcessInstance != null) {
            setCurrentPosition(newProcessInstance.getCurrentPosition());
        } else {
            setCurrentPosition(null);
        }
    }
    
    private void traceDebugException(String message, Exception exception) {
        if (exception == null) {
            getTracer().println(message); // the message is already localized
            return;
        }
        
        final String sep = NbBundle.getMessage(
                BpelDebuggerImpl.class, "ERR_Separator");
        
        StringBuffer sb = new StringBuffer(200);
        if (message != null) {
            sb.append(message);
        }
        if (exception.getMessage() != null) {
            if (sb.length() > 0) {
                sb.append(sep);
            }
            sb.append(exception.getMessage());
        }
        
        if (exception.getCause() != null) {
            Throwable cause;
            if (exception.getCause() instanceof UndeclaredThrowableException) {
                cause = exception.getCause().getCause();
            } else {
                cause = exception.getCause();
            }
            
            if (cause.getMessage() != null) {
                if (sb.length() > 0) {
                    sb.append(sep);
                }
                sb.append(cause.getMessage());
            }
        }
        
        getTracer().println(sb.toString());
    }
    
    public void clearRunToCursorBreakpoint() {
        mRunToCursorBreakpoint = null;
    }

    public SBYNActivityBreakpoint getRunToCursorBreakpoint() {
        return mRunToCursorBreakpoint;
    }

    public void runToCursor(LineBreakpoint breakpoint) {
        mRunToCursorBreakpoint = new SBYNActivityBreakpoint(breakpoint, this);
        resume();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private class ProcessInstancesModelListener 
            implements ProcessInstancesModel.Listener {
        
        public void processInstanceRemoved(
                final ProcessInstance processInstance) {
            
            if (processInstance == getCurrentProcessInstance()) {
                setCurrentProcessInstance(null);
            }
        }
        
        public void processInstanceAdded(
                final ProcessInstance processInstance) {
            
            // Does nothing
        }

        public void processInstanceStateChanged(
                final ProcessInstance processInstance, 
                final int oldState, 
                final int newState) {
            
            if (newState == ProcessInstance.STATE_SUSPENDED) {
                setCurrentProcessInstance(processInstance);
            } else if (processInstance == getCurrentProcessInstance()) {
                setCurrentPosition(null);
            }
        }
    }
}
