/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.jpda.ui.actions;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.request.EventRequestManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;


import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
 *
 * @author  Jan Jancura
 */
public class RunToCursorActionProvider extends ActionsProviderSupport
                                       implements PropertyChangeListener,
                                                  ActionsManagerListener {

    private JPDADebugger debugger;
    private Session session;
    private LineBreakpoint breakpoint;
    private ActionsManager lastActionsManager;
    
    
    public RunToCursorActionProvider (ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.lookupFirst 
                (null, JPDADebugger.class);
        session = (Session) lookupProvider.lookupFirst 
                (null, Session.class);
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
        EditorContextBridge.addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (debugger.PROP_STATE, this);
        EditorContextBridge.removePropertyChangeListener (this);
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
            ActionsManager.ACTION_RUN_TO_CURSOR,
            getActionsManager().isEnabled(ActionsManager.ACTION_CONTINUE) &&
            (debugger.getState () == debugger.STATE_STOPPED) &&
            (EditorContextBridge.getCurrentLineNumber () >= 0) && 
            (EditorContextBridge.getCurrentURL ().endsWith (".java"))
        );
        if ( (debugger.getState () != debugger.STATE_RUNNING) &&
             (breakpoint != null)
        ) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
        if (debugger.getState () == debugger.STATE_DISCONNECTED) 
            destroy ();
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_RUN_TO_CURSOR);
    }
    
    public void doAction (Object action) {
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
        breakpoint = LineBreakpoint.create (
            EditorContextBridge.getCurrentURL (),
            EditorContextBridge.getCurrentLineNumber ()
        );
        breakpoint.setHidden (true);
        DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
        session.getEngineForLanguage ("Java").getActionsManager ().doAction (
            ActionsManager.ACTION_CONTINUE
        );
    }

    public void actionPerformed(Object action) {
        // Is never called
    }

    /** Sync up with continue action state. */
    public void actionStateChanged(Object action, boolean enabled) {
        if (ActionsManager.ACTION_CONTINUE == action) {
            setEnabled (
                ActionsManager.ACTION_RUN_TO_CURSOR,
                enabled &&
                (debugger.getState () == debugger.STATE_STOPPED) &&
                (EditorContextBridge.getCurrentLineNumber () >= 0) && 
                (EditorContextBridge.getCurrentURL ().endsWith (".java"))
            );
        }
    }
}
