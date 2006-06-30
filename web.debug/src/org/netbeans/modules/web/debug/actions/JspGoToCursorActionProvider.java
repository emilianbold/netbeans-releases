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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


/**
 *
 * @author Martin Grebac
 */
public class JspGoToCursorActionProvider extends ActionsProviderSupport implements PropertyChangeListener {

    private JPDADebugger debugger;
    private Session session;
    private JspLineBreakpoint breakpoint;
    
    
    public JspGoToCursorActionProvider(ContextProvider contextProvider) {
        debugger = (JPDADebugger) contextProvider.lookupFirst(null, JPDADebugger.class);
        session = (Session) contextProvider.lookupFirst(null, Session.class);
        debugger.addPropertyChangeListener(debugger.PROP_STATE, this);
        Context.addPropertyChangeListener(this);
    }

    private void destroy () {
        debugger.removePropertyChangeListener (debugger.PROP_STATE, this);
        Context.removePropertyChangeListener (this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        setEnabled (
            ActionsManager.ACTION_RUN_TO_CURSOR,
            (debugger.getState () == debugger.STATE_STOPPED) &&
            (Utils.isJsp(Context.getCurrentURL()) || Utils.isTag(Context.getCurrentURL()))
        );
        if ((debugger.getState () != debugger.STATE_RUNNING) && (breakpoint != null)) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
        if (debugger.getState () == debugger.STATE_DISCONNECTED) {
            destroy ();
        }
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_RUN_TO_CURSOR);
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
            ActionsManager.ACTION_CONTINUE
        );
    }
}
