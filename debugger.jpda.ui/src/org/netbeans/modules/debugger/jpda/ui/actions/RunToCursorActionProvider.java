/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.debugger.jpda.ui.actions;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.request.EventRequestManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.Context;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
 *
 * @author  Jan Jancura
 */
public class RunToCursorActionProvider extends ActionsProviderSupport implements PropertyChangeListener {

    private JPDADebugger debugger;
    private Session session;
    private LineBreakpoint breakpoint;
    
    
    public RunToCursorActionProvider (LookupProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.lookupFirst 
                (JPDADebugger.class);
        session = (Session) lookupProvider.lookupFirst 
                (Session.class);
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
        Context.addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (debugger.PROP_STATE, this);
        Context.removePropertyChangeListener (this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        setEnabled (
            DebuggerManager.ACTION_RUN_TO_CURSOR,
            (debugger.getState () == debugger.STATE_STOPPED) &&
            (Context.getCurrentLineNumber () >= 0) && 
            (Context.getCurrentURL ().endsWith (".java"))
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
        return Collections.singleton (DebuggerManager.ACTION_RUN_TO_CURSOR);
    }
    
    public void doAction (Object action) {
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
        breakpoint = LineBreakpoint.create (
            Context.getCurrentURL (),
            Context.getCurrentLineNumber ()
        );
        breakpoint.setHidden (true);
        DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
        session.getEngineForLanguage ("Java").getActionsManager ().doAction (
            DebuggerManager.ACTION_CONTINUE
        );
    }
}
