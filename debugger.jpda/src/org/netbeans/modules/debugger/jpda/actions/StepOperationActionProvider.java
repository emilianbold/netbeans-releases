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
package org.netbeans.modules.debugger.jpda.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
 *
 * @author  Martin Entlicher
 */
public class StepOperationActionProvider extends ActionsProviderSupport
                                       implements PropertyChangeListener,
                                                  ActionsManagerListener {

    private JPDADebugger debugger;
    private Session session;
    private ActionsManager lastActionsManager;
    
    
    public StepOperationActionProvider (ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.lookupFirst 
                (null, JPDADebugger.class);
        session = (Session) lookupProvider.lookupFirst 
                (null, Session.class);
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (debugger.PROP_STATE, this);
    }
    
    static ActionsManager getCurrentActionsManager () {
        return DebuggerManager.getDebuggerManager ().
            getCurrentEngine () == null ? 
            DebuggerManager.getDebuggerManager ().getActionsManager () :
            DebuggerManager.getDebuggerManager ().getCurrentEngine ().
                getActionsManager ();
    }
    
    private ActionsManager getActionsManager () {
        ActionsManager current = getCurrentActionsManager();
        if (current != lastActionsManager) {
            if (lastActionsManager != null) {
                lastActionsManager.removeActionsManagerListener(
                        ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            }
            current.addActionsManagerListener(
                    ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            lastActionsManager = current;
        }
        return current;
    }

    public void propertyChange (PropertyChangeEvent evt) {
        setEnabled (
            ActionsManager.ACTION_STEP_OPERATION,
            getActionsManager().isEnabled(ActionsManager.ACTION_CONTINUE) &&
            (debugger.getState () == debugger.STATE_STOPPED)
        );
        if (debugger.getState () == debugger.STATE_DISCONNECTED) 
            destroy ();
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_STEP_OPERATION);
    }
    
    public void doAction (Object action) {
        JPDAStep step = debugger.createJPDAStep(JPDAStep.STEP_OPERATION, JPDAStep.STEP_OVER);
        step.addStep(debugger.getCurrentThread());
        if (debugger.getSuspend() == JPDADebugger.SUSPEND_EVENT_THREAD) {
            //debugger.getCurrentThread().resume();
            ((JPDADebuggerImpl) debugger).resumeCurrentThread();
        } else {
            ((JPDADebuggerImpl) debugger).resume();
        }
    }

    public void actionPerformed(Object action) {
        // Is never called
    }

    /** Sync up with continue action state. */
    public void actionStateChanged(Object action, boolean enabled) {
        if (ActionsManager.ACTION_CONTINUE == action) {
            setEnabled (
                ActionsManager.ACTION_STEP_OPERATION,
                enabled &&
                (debugger.getState () == debugger.STATE_STOPPED)
            );
        }
    }
}
