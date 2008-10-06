/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;

import org.netbeans.modules.debugger.ui.Utils;

import org.netbeans.spi.project.ui.support.FileSensitiveActions;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


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
        return super.getValue(key);
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
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/Continue.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createFixAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_FIX);
        action.putValue (Action.NAME, "CTL_Fix_action_name");
        action.putValue (
            "iconBase",
            "org/netbeans/modules/debugger/resources/actions/Fix.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createKillAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_KILL);
        action.putValue (Action.NAME, "CTL_KillAction_name");
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
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/GoToCalledMethod.gif" // NOI18N
        );
        return action;
    }

    public static DebuggerAction createMakeCallerCurrentAction() {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_MAKE_CALLER_CURRENT);
        action.putValue (Action.NAME, "CTL_MakeCallerCurrentAction_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/GoToCallingMethod.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createPauseAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_PAUSE);
        action.putValue (Action.NAME, "CTL_Pause_action_name");
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
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/RunToCursor.gif" // NOI18N
        );
        return action;
    }

    public static DebuggerAction createStepIntoAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_INTO);
        action.putValue (Action.NAME, "CTL_Step_into_action_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepInto.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createStepIntoNextMethodAction () {
        DebuggerAction action = new DebuggerAction("stepIntoNextMethod"); // NOI18N [TODO] add constant
        action.putValue (Action.NAME, "CTL_Step_into_next_method_action_name");
        return action;
    }
    
    public static DebuggerAction createStepOutAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_OUT);
        action.putValue (Action.NAME, "CTL_Step_out_action_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepOut.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createStepOverAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_OVER);
        action.putValue (Action.NAME, "CTL_Step_over_action_name");
        action.putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/StepOver.gif" // NOI18N
        );
        return action;
    }
    
    public static DebuggerAction createStepOperationAction () {
        DebuggerAction action = new DebuggerAction(ActionsManager.ACTION_STEP_OPERATION);
        action.putValue (Action.NAME, "CTL_Step_operation_action_name");
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
    
    // Debug File Actions:
    
    public static Action createDebugFileAction() {
        Action a = FileSensitiveActions.fileCommandAction(
            "debug.single", // XXX Define standard
            NbBundle.getMessage(DebuggerAction.class, "LBL_DebugSingleAction_Name"), // NOI18N
            new ImageIcon( ImageUtilities.loadImage( "org/netbeans/modules/debugger/resources/debugSingle.png" ) )); //NOI18N
        a.putValue("iconBase","org/netbeans/modules/debugger/resources/debugSingle.png"); //NOI18N
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        return a;
    }
    
    public static Action createDebugTestFileAction()  {
        Action a = FileSensitiveActions.fileCommandAction(
            "debug.test.single", // XXX Define standard
            NbBundle.getMessage(DebuggerAction.class, "LBL_DebugTestSingleAction_Name" ),// NOI18N
            new ImageIcon( ImageUtilities.loadImage( "org/netbeans/modules/debugger/resources/debugTestSingle.png" ) )); //NOI18N
        a.putValue("iconBase","org/netbeans/modules/debugger/resources/debugTestSingle.png"); //NOI18N
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        return a;
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

