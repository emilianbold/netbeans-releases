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

import java.awt.event.ActionEvent;
import java.beans.*;
import java.util.*;
import javax.swing.*;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.project.Project;
import org.netbeans.spi.debugger.*;
import org.netbeans.spi.debugger.jpda.ContextProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.*;

import org.openide.windows.TopComponent;

import org.netbeans.modules.web.debug.util.*;
import org.netbeans.modules.web.debug.breakpoints.JspLineBreakpoint;

/**
*
* @author Martin Grebac
*/
public class JspRunToCursorActionProvider extends ActionsProviderSupport {
    
    private ContextProvider contextProvider;
    private JspLineBreakpoint breakpoint;
        
    {
        contextProvider = (ContextProvider) DebuggerManager.
            getDebuggerManager().lookupFirst(ContextProvider.class);
        
        Listener listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener (listener);
        TopComponent.getRegistry ().addPropertyChangeListener (listener);
        DebuggerManager.getDebuggerManager ().addDebuggerListener (listener);

        setEnabled(DebuggerManager.ACTION_RUN_TO_CURSOR, shouldBeEnabled());
    }
    
    public Set getActions() {
        return Collections.singleton (DebuggerManager.ACTION_RUN_TO_CURSOR);
    }
    
    public void doAction (Object action) {
        // 1) set breakpoint
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(breakpoint);
            breakpoint = null;
        }
        breakpoint = JspLineBreakpoint.create (
            contextProvider.getCurrentURL(),
            contextProvider.getCurrentLineNumber()
        );
        breakpoint.setHidden(true);
        DebuggerManager.getDebuggerManager().addBreakpoint(breakpoint);
        
        // 2) start debugging of project
        ((ActionProvider) MainProjectManager.getDefault().
            getMainProject().getLookup().lookup(
                ActionProvider.class
            )).invokeAction (
                "debug", MainProjectManager.getDefault ().getMainProject ().getLookup ()
            );
    }
    
    private boolean shouldBeEnabled () {
        Project p = MainProjectManager.getDefault().getMainProject ();
        if (p == null) return false;
        
        ActionProvider ap = ((ActionProvider)p.getLookup().lookup(ActionProvider.class));
        if (ap == null) return false;
        
        if (ap.isActionEnabled("debug", MainProjectManager.getDefault().getMainProject().getLookup())) {
            return Utils.isJsp(contextProvider.getCurrentURL());
        }
        return false;
    }
    
    
    private class Listener implements PropertyChangeListener, DebuggerManagerListener {
        public void propertyChange (PropertyChangeEvent e) {
            setEnabled (
                DebuggerManager.ACTION_RUN_TO_CURSOR,
                shouldBeEnabled ()
            );
        }
        public void sessionRemoved (Session session) {
            if (breakpoint != null) {
                DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
                breakpoint = null;
            }
        }
        
        public void breakpointAdded (Breakpoint breakpoint) {}
        public void breakpointRemoved (Breakpoint breakpoint) {}
        public Breakpoint[] initBreakpoints () {
            return new Breakpoint [0];
        }
        public void initWatches () {}
        public void sessionAdded (Session session) {}
        public void watchAdded (Watch watch) {}
        public void watchRemoved (Watch watch) {}
    }
}
