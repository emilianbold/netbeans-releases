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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.project.ActionProvider;
import org.openide.windows.TopComponent;
import org.netbeans.modules.web.debug.breakpoints.JspLineBreakpoint;
import org.netbeans.modules.web.debug.util.Utils;
import org.netbeans.spi.debugger.ActionsProviderSupport;

/**
*
* @author Martin Grebac, Libor Kotouc
*/
public class JspRunToCursorActionProvider extends ActionsProviderSupport {
    
    private EditorContext editorContext;
    private JspLineBreakpoint breakpoint;
        
    {
        editorContext = (EditorContext) DebuggerManager.
            getDebuggerManager().lookupFirst(null, EditorContext.class);
        
        Listener listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener (listener);
        TopComponent.getRegistry ().addPropertyChangeListener (listener);
        DebuggerManager.getDebuggerManager ().addDebuggerListener (listener);

        setEnabled(ActionsManager.ACTION_RUN_TO_CURSOR, shouldBeEnabled());
    }
    
    public Set getActions() {
        return Collections.singleton (ActionsManager.ACTION_RUN_TO_CURSOR);
    }
    
    public void doAction (Object action) {
        
        // 1) set breakpoint
        removeBreakpoint();
        createBreakpoint();
        
        // 2) start debugging of project
        ((ActionProvider) MainProjectManager.getDefault().
            getMainProject().getLookup().lookup(
                ActionProvider.class
            )).invokeAction (
                ActionProvider.COMMAND_DEBUG, 
                MainProjectManager.getDefault ().getMainProject ().getLookup ()
            );
    }
    
    private boolean shouldBeEnabled () {

        if (/* some module disabled? */ editorContext == null || !Utils.isJsp(editorContext.getCurrentURL())) {
            return false;
        }
        
        // check if current project supports this action
        Project p = MainProjectManager.getDefault ().getMainProject ();
        if (p == null) return false;
        ActionProvider actionProvider = (ActionProvider)p.getLookup ().lookup (ActionProvider.class);
        if (actionProvider == null) return false;

        String[] sa = actionProvider.getSupportedActions ();
        int i, k = sa.length;
        for (i = 0; i < k; i++) {
            if (ActionProvider.COMMAND_DEBUG.equals (sa [i])) {
                break;
            }
        }
        if (i == k) {
            return false;
        }

        // check if this action should be enabled
        return ((ActionProvider) p.getLookup ().lookup (
                ActionProvider.class
            )).isActionEnabled (
                ActionProvider.COMMAND_DEBUG, 
                p.getLookup ()
            );
    }

    private void createBreakpoint() {
        breakpoint = JspLineBreakpoint.create (
            editorContext.getCurrentURL (),
            editorContext.getCurrentLineNumber ()
        );
        breakpoint.setHidden (true);
        DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
    }
    
    private void removeBreakpoint() {
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
    }
    
    private class Listener implements PropertyChangeListener, DebuggerManagerListener {
        public void propertyChange (PropertyChangeEvent e) {
            if ((e == null) || (TopComponent.Registry.PROP_OPENED.equals(e.getPropertyName())))
                return;
            if (e.getPropertyName () == JPDADebugger.PROP_STATE) {
                int state = ((Integer) e.getNewValue ()).intValue ();
                if (state == JPDADebugger.STATE_DISCONNECTED || state == JPDADebugger.STATE_STOPPED)
                    removeBreakpoint ();
                return;
            }

            setEnabled (
                ActionsManager.ACTION_RUN_TO_CURSOR,
                shouldBeEnabled ()
            );
        }
        
        public void sessionRemoved (Session session) {
            removeBreakpoint();
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

        public void engineAdded (DebuggerEngine engine) {
            JPDADebugger debugger = (JPDADebugger) engine.lookupFirst 
                (null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.addPropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
        }
        
        public void engineRemoved (DebuggerEngine engine) {
            JPDADebugger debugger = (JPDADebugger) engine.lookupFirst 
                (null, JPDADebugger.class);
            if (debugger == null) return;
            debugger.removePropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
        }

    }
}
