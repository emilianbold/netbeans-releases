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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.debugger.ui.Utils;
import org.openide.util.NbBundle;


/**
 *
 * @author   Jan Jancura
 */
public class DebuggerAction extends AbstractAction {
    
    private Object action;

    private DebuggerAction (Object action) {
        this.action = action;
        new Listener (this);
        setEnabled (isEnabled (getAction ()));
    }
    
    public Object getAction () {
        return action;
    }
    
    public Object getValue(String key) {
        if (key == Action.NAME) {
            return NbBundle.getMessage (DebuggerAction.class, (String) super.getValue(key));
        }
        Object value = super.getValue(key);
        if (key == Action.SMALL_ICON) {
            if (value instanceof String) {
                value = Utils.getIcon ((String) value);
            }
        }
        return value;
    }
    
    public void actionPerformed (ActionEvent evt) {
        // Post the action asynchronously, since we're on AWT
        getActionsManager(action).postAction(action);
    }
    
    /**
     * Get the actions manager of the current engine (if any).
     * @return The actions manager or <code>null</code>.
     */
    private static ActionsManager getCurrentEngineActionsManager() {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            return engine.getActionsManager();
        } else {
            return null;
        }
    }
    
    /**
     * Test whether the given action is enabled in either the current engine's
     * action manager, or the default action manager.
     * We need to take the default actions into account so that actions provided
     * by other debuggers are not ignored.
     */
    private static boolean isEnabled(Object action) {
        ActionsManager manager = getCurrentEngineActionsManager();
        if (manager != null) {
            if (manager.isEnabled(action)) {
                return true;
            }
        }
        return DebuggerManager.getDebuggerManager().getActionsManager().isEnabled(action);
    }
    
    /**
     * Get the actions manager for which the action is enabled.
     * It returns either the current engine's manager, or the default one.
     * @param the action
     * @return the actions manager
     */
    private static ActionsManager getActionsManager(Object action) {
        ActionsManager manager = getCurrentEngineActionsManager();
        if (manager != null) {
            if (manager.isEnabled(action)) {
                return manager;
            }
        }
        return DebuggerManager.getDebuggerManager().getActionsManager();
    }
    
    public static DebuggerAction createContinueAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_CONTINUE);
        action.putValue (Action.NAME, "CTL_Continue_action_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/Continue" // NOI18N
        );
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/Continue.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createFixAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_FIX);
        action.putValue (Action.NAME, "CTL_Fix_action_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/Fix" // NOI18N
        );
        action.putValue (
            "iconBase",
            "org/netbeans/modules/debugger/resources/actions/Fix.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createKillAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_KILL);
        action.putValue (Action.NAME, "CTL_KillAction_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/Kill" // NOI18N
        );
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/Kill.gif" // NOI18N
        );
        action.setEnabled (false);
        return action;
    }
    
    public static DebuggerAction createMakeCalleeCurrentAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_MAKE_CALLEE_CURRENT);
        action.putValue (Action.NAME, "CTL_MakeCalleeCurrentAction_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/GoToCalledMethod" // NOI18N
        );
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/GoToCalledMethod.gif" // NOI18N
        );
        return action;
    }

    public static DebuggerAction createMakeCallerCurrentAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_MAKE_CALLER_CURRENT);
        action.putValue (Action.NAME, "CTL_MakeCallerCurrentAction_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/GoToCallingMethod" // NOI18N
        );
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/GoToCallingMethod.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createPauseAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_PAUSE);
        action.putValue (Action.NAME, "CTL_Pause_action_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/Pause" // NOI18N
        );
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/Pause.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createPopTopmostCallAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_POP_TOPMOST_CALL);
        action.putValue (Action.NAME, "CTL_PopTopmostCallAction_name");
        return action;
    }
    
    public static DebuggerAction createRunIntoMethodAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_RUN_INTO_METHOD);
        action.putValue (Action.NAME, "CTL_Run_into_method_action_name");
        return action;
    }
    
    public static DebuggerAction createRunToCursorAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_RUN_TO_CURSOR);
        action.putValue (Action.NAME, "CTL_Run_to_cursor_action_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/RunToCursor" // NOI18N
        );
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/RunToCursor.gif" // NOI18N
        );
        return action;
    }

    public static DebuggerAction createStepIntoAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_INTO);
        action.putValue (Action.NAME, "CTL_Step_into_action_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/StepInto" // NOI18N
        );
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepInto.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createStepOutAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_OUT);
        action.putValue (Action.NAME, "CTL_Step_out_action_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/StepOut" // NOI18N
        );
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepOut.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createStepOverAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_OVER);
        action.putValue (Action.NAME, "CTL_Step_over_action_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/StepOver" // NOI18N
        );
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepOver.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createStepOperationAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_OPERATION);
        action.putValue (Action.NAME, "CTL_Step_operation_action_name");
        action.putValue (Action.SMALL_ICON, 
                "org/netbeans/modules/debugger/resources/actions/StepOverOperation" // NOI18N
        );
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepOverOperation.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createToggleBreakpointAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
        action.putValue (Action.NAME, "CTL_Toggle_breakpoint");
        return action;
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
            DebuggerManager.getDebuggerManager ().getActionsManager().addActionsManagerListener(
                ActionsManagerListener.PROP_ACTION_STATE_CHANGED,
                this
            );
            updateCurrentActionsManager ();
        }
        
        public void propertyChange (PropertyChangeEvent evt) {
            final DebuggerAction da = getDebuggerAction ();
            if (da == null) return;
            updateCurrentActionsManager ();
            final boolean en = DebuggerAction.isEnabled (da.getAction ());
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
            // ignore the enabled argument, check it with respect to the proper
            // actions manager.
            final boolean en = DebuggerAction.isEnabled (da.getAction ());
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    da.setEnabled (en);
                }
            });
        }
        
        private void updateCurrentActionsManager () {
            ActionsManager newActionsManager = getCurrentEngineActionsManager ();
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
                DebuggerManager.getDebuggerManager ().getActionsManager().removeActionsManagerListener(
                    ActionsManagerListener.PROP_ACTION_STATE_CHANGED,
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

