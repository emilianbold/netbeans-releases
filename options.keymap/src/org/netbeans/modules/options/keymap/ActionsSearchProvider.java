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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */


package org.netbeans.modules.options.keymap;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.TextAction;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


    
/**
 * SearchProvider for all actions. 
 * @author  Jan Becicka, Dafe Simonek
 */
public class ActionsSearchProvider implements SearchProvider {

    /**
     * Iterates through all found KeymapManagers and their sets of actions
     * and fills response object with proper actions that are enabled
     * and can be run meaningfully on current actions context.
     */
    public void evaluate(SearchRequest request, SearchResponse response) {
        List<Object[]> possibleResults = new ArrayList<Object[]>(7);
        Map<ShortcutAction, Set<String>> curKeymap;
        // iterate over all found KeymapManagers
        for (KeymapManager m : Lookup.getDefault().lookupAll(KeymapManager.class)) {
            curKeymap = m.getKeymap(m.getCurrentProfile());
            for (Entry<String, Set<ShortcutAction>> entry : m.getActions().entrySet()) {
                for (ShortcutAction sa : entry.getValue()) {
                    // check action and obtain only meaningful ones
                    Object[] actAndEvent = getActionInfo(sa, curKeymap.get(sa));
                    if (actAndEvent == null) {
                        continue;
                    }
                    if (!doEvaluation(sa.getDisplayName(), request, actAndEvent, response, possibleResults)) {
                        return;
                    }
                }
            }
        }
        
        // try also actions of activated nodes
        Node[] actNodes = TopComponent.getRegistry().getActivatedNodes();
        for (int i = 0; i < actNodes.length; i++) {
            Action[] acts = actNodes[i].getActions(false);
            for (int j = 0; j < acts.length; j++) {
                Action action = checkNodeAction(acts[j]);
                if (action == null) {
                    continue;
                }
                Object[] actAndEvent = new Object[] {
                    action, createActionEvent(action), null, null
                };
                Object name = action.getValue(Action.NAME);
                if (!(name instanceof String)) {
                    // skip action without proper name
                    continue;
                }
                if (!doEvaluation((String)name, request, actAndEvent, response, possibleResults)) {
                    return;
                }
            }
        }

        // add results stored above, actions that contain typed text, but not as prefix
        for (Object[] actAndEvent : possibleResults) {
            if (!addAction(actAndEvent, response)) {
                return;
            }
        }
    }
    

    private boolean addAction(Object[] actAndEvent, SearchResponse response) {
        KeyStroke stroke = null;
        // obtaining shortcut, first try Keymaps
        Set<String> shortcuts = (Set<String>)actAndEvent[3];
        if (shortcuts != null && shortcuts.size() > 0) {
            String shortcut = shortcuts.iterator().next();
            stroke = Utilities.stringToKey(shortcut);
        }
        // try accelerator key property if Keymaps returned no shortcut
        Action action = (Action) actAndEvent[0];
        if (stroke == null) {
            Object shortcut = action.getValue(Action.ACCELERATOR_KEY);
            if (shortcut instanceof KeyStroke) {
                stroke = (KeyStroke)shortcut;
            }
        }
        
        /* uncomment if needed
         Object desc = ((Action) actAndEvent[0]).getValue(Action.SHORT_DESCRIPTION);
        String sDesc = null;
        if (sDesc instanceof String) {
            sDesc = (String) desc;
        }*/
        
        String displayName = null;
        ShortcutAction sa= (ShortcutAction)actAndEvent[2];
        if (sa != null) {
            displayName = sa.getDisplayName();
        } else {
            Object name = action.getValue(Action.NAME);
            if (name instanceof String) {
                displayName = (String)name;
            }
        }
        
        return response.addResult(new ActionResult(action, (ActionEvent) actAndEvent[1]),
                displayName, null, Collections.singletonList(stroke));
    }

    private boolean doEvaluation(String name, SearchRequest request,
            Object[] actAndEvent, SearchResponse response, List<Object[]> possibleResults) {
        int index = name.toLowerCase().indexOf(request.getText().toLowerCase());
        if (index == 0) {
            return addAction(actAndEvent, response);
        } else if (index != -1) {
            // typed text is contained in action name, but not as prefix,
            // store such actions if there are not enough "prefix" actions
            possibleResults.add(actAndEvent);
        }
        return true;
    }
    
    private Object[] getActionInfo(ShortcutAction sa, Set<String> shortcuts) {
        Class clazz = sa.getClass();
        Field f = null;
        try {
            f = clazz.getDeclaredField("action");
            f.setAccessible(true);
            Action action = (Action) f.get(sa);
            
            
            
            if (!action.isEnabled()) {
                return null;
            }
            
            return new Object[] {action, createActionEvent(action),
                                    sa, shortcuts};
            
        } catch (Throwable thr) {
            if (thr instanceof ThreadDeath) {
                throw (ThreadDeath)thr;
            }
            // just log problems, it is common that some actions may
            // complain
            Logger.getLogger(getClass().getName()).log(Level.FINE,
                    "Some problem getting action " + sa.getDisplayName(), thr);
        }
        // fallback
        return null;
    }
    
    
    private ActionEvent createActionEvent (Action action) {
        Object evSource = null;
        int evId = ActionEvent.ACTION_PERFORMED;

        // text (editor) actions
        if (action instanceof TextAction) {
            EditorCookie ec = Utilities.actionsGlobalContext().lookup(EditorCookie.class);
            if (ec == null) {
                return null;
            }

            JEditorPane[] editorPanes = ec.getOpenedPanes();
            if (editorPanes == null || editorPanes.length <= 0) {
                return null;
            }
            evSource = editorPanes[0];
        }

        if (evSource == null) {
            evSource = TopComponent.getRegistry().getActivated();
        }
        if (evSource == null) {
            evSource = WindowManager.getDefault().getMainWindow();
        }

        
        return new ActionEvent(evSource, evId, null);
    }
    
    private Action checkNodeAction (Action action) {
        if (action == null) {
            return null;
        }
        try {
            if (action.isEnabled()) {
                return action;
            }
        } catch (Throwable thr) {
            if (thr instanceof ThreadDeath) {
                throw (ThreadDeath)thr;
            }
            // just log problems, it is common that some actions may complain
            Logger.getLogger(getClass().getName()).log(Level.FINE,
                    "Problem asking isEnabled on action " + action, thr);
        }
        return null;
    }
    
    private static class ActionResult implements Runnable {
        private Action command;
        private ActionEvent event;

        public ActionResult(Action command, ActionEvent event) {
            this.command = command;
            this.event = event;
        }
        
        public void run() {
            // be careful, some actions throws assertions etc, because they
            // are not written to be invoked directly
            try {
                command.actionPerformed(event);
            } catch (Throwable thr) {
                if (thr instanceof ThreadDeath) {
                    throw (ThreadDeath)thr;
                }
                Object name = command.getValue(Action.NAME);
                String displayName = "";
                if (name instanceof String) {
                    displayName = (String)name;
                }
                
                Logger.getLogger(getClass().getName()).log(Level.FINE, 
                        displayName + " action can not be invoked.", thr);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                        getClass(), "MSG_ActionFailure", displayName));
            }
        }
    }

}
