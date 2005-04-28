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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;

import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;


/**
 *
 * @author   Jan Jancura
 */
public abstract class DebuggerAction extends AbstractAction {
    
    private static RequestProcessor actionRequestProcessor;
    
    private static WeakSet debuggeeActions;

    public DebuggerAction () {
        new Listener (this);
        setEnabled (getCurrentActionsManager ().isEnabled (getAction ()));
        if (isDebuggeeAction()) {
            synchronized (DebuggerAction.class) {
                if (debuggeeActions == null) {
                    debuggeeActions = new WeakSet();
                }
                debuggeeActions.add(this);
            }
        }
    }
    
    public abstract Object getAction ();
    
    /**
     * Test whether the action can be run synchronously or not.
     * Actions that contact the debugged process (debuggee) should be performed
     * asynchronously, otherwise AWT can be blocked.
     */
    private boolean isDebuggeeAction() {
        Object action = getAction();
        return ActionsManager.ACTION_CONTINUE == action ||
               ActionsManager.ACTION_PAUSE == action ||
               ActionsManager.ACTION_RESTART == action ||
               ActionsManager.ACTION_RUN_INTO_METHOD == action ||
               ActionsManager.ACTION_RUN_TO_CURSOR == action ||
               ActionsManager.ACTION_STEP_INTO == action ||
               ActionsManager.ACTION_STEP_OUT == action ||
               ActionsManager.ACTION_STEP_OVER == action ||
               ActionsManager.ACTION_POP_TOPMOST_CALL == action;
    }
    
    public void actionPerformed (ActionEvent evt) {
        // Perform in sync with AWT if it does not contact debuggee.
        if (!isDebuggeeAction()) {
            getCurrentActionsManager().doAction (
                getAction ()
            );
            return ;
        }
        // Otherwise, spawn a task for it in RP and disable all actions
        // that would also contact the debuggee.
        synchronized (DebuggerAction.class) {
            if (actionRequestProcessor == null) {
                actionRequestProcessor = new RequestProcessor("DebuggerAction", 1); // NOI18N
            }
        }
        final Set actions = disableAllDebuggeeActions();
        RequestProcessor rp;
        if (getAction() == ActionsManager.ACTION_KILL) {
            rp = RequestProcessor.getDefault(); // Always process the kill action
        } else {
            rp = actionRequestProcessor;
        }
        rp.post(new Runnable() {
            public void run() {
                getCurrentActionsManager().doAction (
                    getAction ()
                );
                enableActions(actions);
            }
        });
    }
    
    /**
     * @return the set of actions that were disabled.
     */
    private static Set disableAllDebuggeeActions() {
        Set actions = new HashSet();
        synchronized (DebuggerAction.class) {
            for (Iterator it = debuggeeActions.iterator(); it.hasNext(); ) {
                DebuggerAction da = (DebuggerAction) it.next();
                if (da == null) continue;
                actions.add(da);
                da.setEnabled(false);
            }
        }
        return actions;
    }
    
    private static void enableActions(final Set actions) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    for (Iterator it = actions.iterator(); it.hasNext(); ) {
                        DebuggerAction da = (DebuggerAction) it.next();
                        da.setEnabled (getCurrentActionsManager ().isEnabled (
                            da.getAction ()
                        ));
                    }
                }
            });
        } catch (InterruptedException iex) {
        } catch (InvocationTargetException itex) {
            ErrorManager.getDefault().notify(itex);
        }
    }
        
    private static ActionsManager getCurrentActionsManager () {
        return DebuggerManager.getDebuggerManager ().
            getCurrentEngine () == null ? 
            DebuggerManager.getDebuggerManager ().getActionsManager () :
            DebuggerManager.getDebuggerManager ().getCurrentEngine ().
                getActionsManager ();
    }

    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE and on current engine
     * on PROP_ACTION_STATE and updates state of this action instance.
     */
    static class Listener extends DebuggerManagerAdapter 
    implements ActionsManagerListener {
        
        private ActionsManager  currentActionsManager;
        private WeakReference   ref;

        
        Listener (DebuggerAction da) {
            ref = new WeakReference (da);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
            updateCurrentActionsManager ();
        }
        
        public void propertyChange (PropertyChangeEvent evt) {
            final DebuggerAction da = getDebuggerAction ();
            if (da == null) return;
            updateCurrentActionsManager ();
            final boolean en = currentActionsManager.isEnabled (da.getAction ());
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    da.setEnabled (en);
                }
            });
        }
        
        public void actionPerformed (Object action) {
        }
        public void actionStateChanged (
            final Object action, 
            final boolean enabled
        ) {
            final DebuggerAction da = getDebuggerAction ();
            if (da == null) return;
            if (action != da.getAction ()) return;
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    da.setEnabled (enabled);
                }
            });
        }
        
        private void updateCurrentActionsManager () {
            ActionsManager newActionsManager = getCurrentActionsManager ();
            if (currentActionsManager == newActionsManager) return;
            
            if (currentActionsManager != null)
                currentActionsManager.removeActionsManagerListener
                    (ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            if (newActionsManager != null)
                newActionsManager.addActionsManagerListener
                    (ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            currentActionsManager = newActionsManager;
        }
        
        private DebuggerAction getDebuggerAction () {
            DebuggerAction da = (DebuggerAction) ref.get ();
            if (da == null) {
                DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                    DebuggerManager.PROP_CURRENT_ENGINE,
                    this
                );
                if (currentActionsManager != null)
                    currentActionsManager.removeActionsManagerListener 
                        (ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
                currentActionsManager = null;
                return null;
            }
            return da;
        }
    }
}

