/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.debug.actions;

import java.beans.*;
import java.util.*;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.debugger.*;
import org.netbeans.modules.web.debug.Context;
import org.netbeans.modules.web.debug.util.*;
import org.netbeans.modules.web.debug.breakpoints.*;

import org.openide.windows.TopComponent;


/**
 *
 * @author Martin Grebac
 */
public class JspGoToCursorActionProvider extends ActionsProviderSupport implements PropertyChangeListener {

    private JPDADebugger debugger;
    private Session session;
    private JspLineBreakpoint breakpoint;
    
    
    public JspGoToCursorActionProvider(LookupProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.lookupFirst(JPDADebugger.class);
        session = (Session) lookupProvider.lookupFirst(Session.class);
        debugger.addPropertyChangeListener(debugger.PROP_STATE, this);
        Context.addPropertyChangeListener(this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        setEnabled (
            DebuggerManager.ACTION_RUN_TO_CURSOR,
            (debugger.getState () == debugger.STATE_STOPPED) &&
            (Utils.isJsp(Context.getCurrentURL()))
        );
        if ((debugger.getState () != debugger.STATE_RUNNING) && (breakpoint != null)) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
    }
    
    public Set getActions () {
        return Collections.singleton (DebuggerManager.ACTION_RUN_TO_CURSOR);
    }
    
    public void doAction (Object action) {
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
        breakpoint = JspLineBreakpoint.create (
            Context.getCurrentURL(),
            Context.getCurrentLineNumber()
        );
        breakpoint.setHidden(true);
        DebuggerManager.getDebuggerManager().addBreakpoint (breakpoint);
        session.getEngineForLanguage ("JSP").getActionsManager ().doAction (
            DebuggerManager.ACTION_CONTINUE
        );
    }
}
