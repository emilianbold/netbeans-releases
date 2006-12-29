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

package org.netbeans.modules.bpel.debugger.bdiclient.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebugFrame;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebuggableEngine;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.XpathExpressionException;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;
import org.netbeans.modules.bpel.debugger.SBYNBreakpoint;
import org.netbeans.modules.bpel.debugger.bdiclient.BreakPosition;

/**
 *
 * @author Alexander Zgursky
 */
public class ProcessInstanceImpl implements ProcessInstance {
    
    private final String mRootFrameId;
    private final QName mProcessQName;
    private final ProcessInstancesModelImpl mModel;
    
    private BreakPosition mBreakPosition;
    private Object mWaitLock = new Object();
    private Object mSyncLock = new Object();
    private AtomicInteger mState = new AtomicInteger(STATE_RUNNING);

    private boolean mIsUndeployed;
    private boolean mBreakFlag;
    private final List<BDIDebugFrame> mFrames = Collections.synchronizedList(
            new ArrayList<BDIDebugFrame>());
    
    
    /** Creates a new instance of ProcessInstanceImpl */
    public ProcessInstanceImpl(ProcessInstancesModelImpl model, QName processQName, String rootFrameId) {
        mModel = model;
        mProcessQName = processQName;
        mRootFrameId = rootFrameId;
    }
    
    public String getName() {
        return mProcessQName.getLocalPart();
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
    
    public String getRootFrameId() {
        return mRootFrameId;
    }
    
    public QName getProcessQName() {
        return mProcessQName;
    }

    public String[] getVariableNames() {
        BreakPosition activePosition = mBreakPosition;
        if (activePosition != null) {
            BDIDebugFrame currentFrame = activePosition.getFrame();
            DebuggableEngine engine = currentFrame.getDebuggableEngine();
            if (engine != null) {
                return engine.getVariables();
            }
        }
        return new String[0];
    }
    
    public String getVariableData(String variableName) {
        BreakPosition activePosition = mBreakPosition;
        if (activePosition != null) {
            BDIDebugFrame currentFrame = activePosition.getFrame();
            DebuggableEngine engine = currentFrame.getDebuggableEngine();
            if (engine != null) {
                return engine.getContainerDataAsString(variableName);
            }
        }
        return null;
    }

    public String evaluate(String xpathExpression) {
        BreakPosition activePosition = mBreakPosition;
        String result = "";
        if (xpathExpression == null || xpathExpression.equals("")) {
            return "";
        }
        if (activePosition != null) {
            BDIDebugFrame currentFrame = activePosition.getFrame();
            DebuggableEngine engine = currentFrame.getDebuggableEngine();
            if (engine != null) {
                try {
                    result = engine.evaluate(xpathExpression);
                } catch (XpathExpressionException ex) {
                    //TODO:maybe better to rethrow?
                    //TODO:change to NLS
                    result = "<Can not evaluate the expression>";
                }
            }
        } else {
            //TODO:change to NLS
            result = "<Current process instance is not suspended>";
        }
        return result;
    }
    
    protected void setUndeployed(boolean isUndeployed) {
        mIsUndeployed = isUndeployed;
    }
    
    protected void onPositionChanged(BreakPosition breakPosition) {
        if (getDebugger().getState() != BpelDebugger.STATE_RUNNING) {
            return;
        }
        if (mIsUndeployed) {
            return;
        }
        
        synchronized (mSyncLock) {
            SBYNBreakpoint lb = getDebugger().findBreakpoint(breakPosition.getProcessQName(), breakPosition.getXpath());
            boolean foundlb = false;
            if (lb != null && lb.isEnabled()) {
                foundlb = true;
            }
            if (foundlb || mBreakFlag) {
                mBreakFlag = false;

                if (lb != null) {
                    getDebugger().getSourcePath().setURL(mProcessQName, lb.getURL());
                }

                mBreakPosition = breakPosition;
                setState(STATE_SUSPENDED);
                doWait();
                mBreakPosition = null;
                setState(STATE_RUNNING);
            }
        }
    }
    
    protected void onFault(DebugFrame frame) {
        if (getRootFrame() == frame) {
            setState(STATE_FAILED);
        }
    }

    protected void onXpathException(DebugFrame frame) {
        if (getRootFrame() == frame) {
            setState(STATE_FAILED);
        }
    }

    protected void onTerminate(DebugFrame frame) {
        if (getRootFrame() == frame) {
            setState(STATE_TERMINATED);
            mModel.processInstanceCompleted(this);
        }
    }

    protected void onExit(DebugFrame frame) {
        if (getRootFrame() == frame) {
            setState(STATE_COMPLETED);
            mModel.processInstanceCompleted(this);
        }
    }
    
    protected BDIDebugFrame addFrame(String id) {
        BDIDebugFrame frame = new BDIDebugFrame(this, id);
        mFrames.add(frame);
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
    
    private BDIDebugFrame getRootFrame() {
        return mFrames.get(0);
    }
    
    private BpelDebuggerImpl getDebugger() {
        return mModel.getDebugger();
    }
    
}
