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


package org.netbeans.modules.bpel.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.bpel.debugger.*;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Listener for breakpoint events from NB Debugger Framework.
 *
 * @author Sun Microsystems
 */
public class BreakpointsEngineListener
        extends LazyActionsManagerListener 
        implements PropertyChangeListener, DebuggerManagerListener
{
    private BpelDebuggerImpl myDebugger;
    private boolean myIsStarted;
    
    
    /**
     * Creates a new instance of BreakpointsEngineListener.
     *
     * @param lookupProvider debugger engine context
     */
    public BreakpointsEngineListener(ContextProvider lookupProvider) {
        myDebugger = (BpelDebuggerImpl)lookupProvider.lookupFirst 
                (null, BpelDebugger.class);
    
        myDebugger.addPropertyChangeListener(BpelDebugger.PROP_STATE, this);
    }
    
    protected void destroy() {
        myDebugger.removePropertyChangeListener(BpelDebugger.PROP_STATE, this);
        DebuggerManager.getDebuggerManager().removeDebuggerListener(
                DebuggerManager.PROP_BREAKPOINTS, this);
    }

    /** {@inheritDoc} */
    public String[] getProperties() {
        return new String[] {"asd"};    //NOI18N
    }

    /** {@inheritDoc} */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == myDebugger ) {
            if (myDebugger.getState() == BpelDebugger.STATE_RUNNING) {
                if (myIsStarted) {
                    return;
                }
                myIsStarted = true;
                DebuggerManager.getDebuggerManager().addDebuggerListener(
                    DebuggerManager.PROP_BREAKPOINTS, this);
                //TODO:why do we need a PROP_CURRENT_SESSION listener?
                DebuggerManager.getDebuggerManager().addDebuggerListener(
                        DebuggerManager.PROP_CURRENT_SESSION, this);
               
            }
    
            if (myDebugger.getState () == BpelDebugger.STATE_DISCONNECTED) {
                //removeBreakpointImpls();
                myIsStarted = false;
                DebuggerManager.getDebuggerManager().removeDebuggerListener(
                    DebuggerManager.PROP_BREAKPOINTS, this);
                //TODO:why do we need a PROP_CURRENT_SESSION listener?
                DebuggerManager.getDebuggerManager().removeDebuggerListener(
                        DebuggerManager.PROP_CURRENT_SESSION, this);                
            }
        }        
//        else if (evt.getSource() == DebuggerManager.getDebuggerManager()) {
//            Session oldSession = (Session) evt.getOldValue();
//            Session newSession = (Session) evt.getNewValue();
//            
//            if (myDebugger.getSession() == oldSession) {
//                myDebugger.clearOldPosition();
//            } else if (myDebugger.getSession() == newSession) {
//                //Reactivate the current position
//                myDebugger.setCurrentProcessInstance(myDebugger.getCurrentProcessInstance());
//            }
//        }
    }

    /** {@inheritDoc} */
    public Breakpoint[] initBreakpoints() {
        return new Breakpoint[0];
    }

    /** {@inheritDoc} */
    public void breakpointAdded(Breakpoint breakpoint) {
        if (breakpoint instanceof BpelBreakpoint) {
            myDebugger.breakpointAdded((BpelBreakpoint)breakpoint);
        }
    }

    /** {@inheritDoc} */
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (breakpoint instanceof BpelBreakpoint) {
            myDebugger.breakpointRemoved((BpelBreakpoint)breakpoint);
        }
    }

    /** {@inheritDoc} */
    public void initWatches() {
    }

    /** {@inheritDoc} */
    public void watchAdded(Watch watch) {
    }

    /** {@inheritDoc} */
    public void watchRemoved(Watch watch) {
    }

    /** {@inheritDoc} */
    public void sessionAdded(Session session) {
    }

    /** {@inheritDoc} */
    public void sessionRemoved(Session session) {
    }

    /** {@inheritDoc} */
    public void engineAdded(DebuggerEngine debuggerEngine) {
    }

    /** {@inheritDoc} */
    public void engineRemoved(DebuggerEngine debuggerEngine) {
    }
}
