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
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.TextAction;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * SearchProvider for all actions. 
 * @author  Jan Becicka
 */
public class ActionsSearchProvider implements SearchProvider {

    /**
     * Iterates through all found KeymapManagers and their sets of actions
     * and fills response object with proper actions that are enabled
     * and can be run meaningfully on current actions context.
     */
    public void evaluate(SearchRequest request, SearchResponse response) {
        // iterate over all found KeymapManagers
        for (KeymapManager m : Lookup.getDefault().lookupAll(KeymapManager.class)) {
            for (Entry<String, Set<ShortcutAction>> entry : m.getActions().entrySet()) {
                for (ShortcutAction sa : entry.getValue()) {
                    // check action and obtain only meaningful ones
                    Object[] actAndEvent = getActionAndEvent(sa);
                    if (actAndEvent == null) {
                        continue;
                    }
                    if (sa.getDisplayName().toLowerCase().indexOf(request.getText().toLowerCase()) != -1) {
                        Object shortcut = ((Action)actAndEvent[0]).getValue(Action.ACCELERATOR_KEY);
                        KeyStroke stroke = null;
                        if (shortcut instanceof KeyStroke) {
                            stroke = (KeyStroke)shortcut;
                        }
                        if (!response.addResult(
                                new ActionResult((Action)actAndEvent[0], (ActionEvent)actAndEvent[1]),
                                sa.getDisplayName(), stroke, null)) {
                            // return immediatelly if no further result is needed
                            return;
                        }
                    }
                }
            }
        }
    }
    
    private Object[] getActionAndEvent(ShortcutAction sa) {
        Class clazz = sa.getClass();
        Field f = null;
        try {
            f = clazz.getDeclaredField("action");
            f.setAccessible(true);
            Action action = (Action) f.get(sa);
            
            if (!action.isEnabled()) {
                return null;
            }
            
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
            
            return new Object[] {action, new ActionEvent(evSource, evId, null)};
            
        } catch (NoSuchFieldException ex) {
            System.out.println("no field action for: " + sa.getDisplayName());
            //Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        // fallback
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
            command.actionPerformed(event);
        }
    }

}
