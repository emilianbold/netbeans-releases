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

package org.netbeans.modules.debugger.jpda.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.api.debugger.Breakpoint;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.project.Project;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.project.ActionProvider;
import org.openide.ErrorManager;


/**
*
* @author   Jan Jancura
*/
public class StepIntoActionProvider extends ActionsProviderSupport {

//    private MethodBreakpoint breakpoint;
    
    {
        Listener listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener (listener);
//        DebuggerManager.getDebuggerManager ().addDebuggerListener (listener);
        
        setEnabled (
            ActionsManager.ACTION_STEP_INTO,
            shouldBeEnabled ()
        );
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_STEP_INTO);
    }
    
    public void doAction (final Object action) {
        // start debugging of project
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        invokeAction();
                    }
                });
            } catch (InterruptedException iex) {
                // Procceed
            } catch (java.lang.reflect.InvocationTargetException itex) {
                ErrorManager.getDefault().notify(itex);
            }
        } else {
            invokeAction();
        }
    }
    
    public void postAction(Object action, Runnable actionPerformedNotifier) {
        // start debugging of project
        invokeAction();
        actionPerformedNotifier.run();
    }
    
    private void invokeAction() {
        ((ActionProvider) MainProjectManager.getDefault ().
            getMainProject ().getLookup ().lookup (
                ActionProvider.class
            )).invokeAction (
                ActionProvider.COMMAND_DEBUG_STEP_INTO, 
                MainProjectManager.getDefault ().getMainProject ().getLookup ()
            );
    }
    
    private boolean shouldBeEnabled () {
        
        // check if current project supports this action
        Project p = MainProjectManager.getDefault ().getMainProject ();
        if (p == null) return false;
        ActionProvider actionProvider = (ActionProvider) p.getLookup ().
            lookup (ActionProvider.class);
        if (actionProvider == null) return false;
        String[] sa = actionProvider.getSupportedActions ();
        int i, k = sa.length;
        for (i = 0; i < k; i++)
            if (ActionProvider.COMMAND_DEBUG_STEP_INTO.equals (sa [i]))
                break;
        if (i == k) return false;
        
        // check if this action should be enabled
        return actionProvider.isActionEnabled (
            ActionProvider.COMMAND_DEBUG_STEP_INTO,
            MainProjectManager.getDefault ().getMainProject ().getLookup ()
        );
    }
    
    
    private class Listener implements PropertyChangeListener/*, 
    DebuggerManagerListener */{
        public void propertyChange (PropertyChangeEvent e) {
            setEnabled (
                ActionsManager.ACTION_STEP_INTO,
                shouldBeEnabled ()
            );
        }
//        public void sessionRemoved (Session session) {
//            if (breakpoint != null) {
//                DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
//                breakpoint = null;
//            }
//        }
//        
//        public void breakpointAdded (Breakpoint breakpoint) {}
//        public void breakpointRemoved (Breakpoint breakpoint) {}
//        public Breakpoint[] initBreakpoints () {
//            return new Breakpoint [0];
//        }
//        public void initWatches () {}
//        public void sessionAdded (Session session) {}
//        public void watchAdded (Watch watch) {}
//        public void watchRemoved (Watch watch) {}
    }
}
