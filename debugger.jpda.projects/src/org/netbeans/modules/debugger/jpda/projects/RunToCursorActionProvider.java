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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;

import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDADebugger;

import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.project.ActionProvider;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

import org.openide.windows.TopComponent;


/**
*
* @author   Jan Jancura
*/
public class RunToCursorActionProvider extends ActionsProviderSupport {
    
    private EditorContext       editor;
    private LineBreakpoint      breakpoint;
    
    {
        editor = (EditorContext) DebuggerManager.
            getDebuggerManager ().lookupFirst (null, EditorContext.class);
        
        Listener listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener (listener);
        TopComponent.getRegistry ().addPropertyChangeListener (listener);
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_DEBUGGER_ENGINES,
            listener
        );

        //PATCH 57824: getOpenedPanes () calls from non AWT threads can
        // lead to deadlock.
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                setEnabled (
                    ActionsManager.ACTION_RUN_TO_CURSOR,
                    shouldBeEnabled ()
                );
            }
        });
        //PATCH 57824
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_RUN_TO_CURSOR);
    }
    
    public void doAction (Object action) {
        
        // 1) set breakpoint
        removeBreakpoint ();
        createBreakpoint (LineBreakpoint.create (
            editor.getCurrentURL (),
            editor.getCurrentLineNumber ()
        ));
        
        // 2) start debugging of project
        invokeAction();
    }
    
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        final LineBreakpoint newBreakpoint = LineBreakpoint.create (
            editor.getCurrentURL (),
            editor.getCurrentLineNumber ()
        );
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                // 1) set breakpoint
                removeBreakpoint ();
                createBreakpoint (newBreakpoint);
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
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    private void invokeAction() {
        ((ActionProvider) MainProjectManager.getDefault ().
            getMainProject ().getLookup ().lookup (
                ActionProvider.class
            )).invokeAction (
                ActionProvider.COMMAND_DEBUG, 
                MainProjectManager.getDefault ().getMainProject ().getLookup ()
            );
    }
    
    private boolean shouldBeEnabled () {
        if (editor.getCurrentLineNumber () < 0) return false;
        if (!editor.getCurrentURL ().endsWith (".java")) return false;
        
        // check if current project supports this action
        Project p = MainProjectManager.getDefault ().getMainProject ();
        if (p == null) return false;
        ActionProvider actionProvider = (ActionProvider) p.getLookup ().
            lookup (ActionProvider.class);
        if (actionProvider == null) return false;
        String[] sa = actionProvider.getSupportedActions ();
        int i, k = sa.length;
        for (i = 0; i < k; i++)
            if (ActionProvider.COMMAND_DEBUG.equals (sa [i]))
                break;
        if (i == k) return false;

        // check if this action should be enabled
        return ((ActionProvider) p.getLookup ().lookup (
                ActionProvider.class
            )).isActionEnabled (
                ActionProvider.COMMAND_DEBUG, 
                MainProjectManager.getDefault ().getMainProject ().getLookup ()
            );
    }
    
    private void createBreakpoint (LineBreakpoint breakpoint) {
        breakpoint.setHidden (true);
        DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
        this.breakpoint = breakpoint;
    }
    
    private void removeBreakpoint () {
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
            breakpoint = null;
        }
    }
    
    private class Listener extends DebuggerManagerAdapter {
        public void propertyChange (PropertyChangeEvent e) {
            if (e.getPropertyName () == JPDADebugger.PROP_STATE) {
                int state = ((Integer) e.getNewValue ()).intValue ();
                if ( (state == JPDADebugger.STATE_DISCONNECTED) ||
                     (state == JPDADebugger.STATE_STOPPED)
                ) removeBreakpoint ();
                return;
            }
            setEnabled (
                ActionsManager.ACTION_RUN_TO_CURSOR,
                shouldBeEnabled ()
            );
        }
        
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
