/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.editor.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.SideBarFactory;
import org.openide.util.NbBundle;

/*
 * Find (searchbar) actions
 */
public abstract class SearchAndReplaceBarHandler {

    public static final String INCREMENTAL_SEARCH_FORWARD = "incremental-search-forward";
    public static final String INCREMENTAL_SEARCH_BACKWARD = "incremental-search-backward";
    public static final String REPLACE_ACTION = "replace"; // NOI18N

    /**
     * Factory for creating the incremental search sidebar
     */
    public static final class Factory implements SideBarFactory {

        @Override
        public JComponent createSideBar(JTextComponent target) {
            SearchJPanel searchJPanel = new SearchJPanel();
            searchJPanel.setLayout(new BoxLayout(searchJPanel, BoxLayout.Y_AXIS));
            return searchJPanel;
        }
    }

    private static class SearchJPanel extends JPanel {
    }

    public static class SearchAction extends BaseAction {

        public SearchAction(String name, int updateMask) {
            super(name, updateMask);
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
                if (eui != null) {
                    //need to find if it has extended editor first, otherwise getExtComponent() will create all sidebars
                    //and other parts of full editor if action is assigned to just editor pane and broke later action logic.
                    JComponent comp = eui.hasExtComponent() ? eui.getExtComponent() : null;
                    if (comp != null) {
                        JPanel jp = findComponent(comp, SearchJPanel.class, 5);
                        if (jp != null) {
                            SearchBar searchBarInstance = SearchBar.getInstance(eui.getComponent());
                            jp.add(searchBarInstance);
                            ReplaceBar replaceBarInstance = ReplaceBar.getInstance(searchBarInstance);
                            if (replaceBarInstance.isVisible()) {
                                replaceBarInstance.looseFocus();
                            }
                            searchBarInstance.gainFocus();
                            makeSearchAndReplaceBarPersistent();
                        }
                    }
                }
            }
        }
    }

    public static class IncrementalSearchForwardAction extends SearchAction {

        static final long serialVersionUID = -1;

        public IncrementalSearchForwardAction() {
            super(INCREMENTAL_SEARCH_FORWARD, CLEAR_STATUS_TEXT);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IncrementalSearchForwardAction.class, INCREMENTAL_SEARCH_FORWARD));
        }
    }

    public static class IncrementalSearchBackwardAction extends SearchAction {

        static final long serialVersionUID = -1;

        public IncrementalSearchBackwardAction() {
            super(INCREMENTAL_SEARCH_BACKWARD, CLEAR_STATUS_TEXT);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IncrementalSearchBackwardAction.class, INCREMENTAL_SEARCH_BACKWARD));
        }
    }

    public static class ReplaceAction extends BaseAction {

        static final long serialVersionUID = -1;

        public ReplaceAction() {
            super(REPLACE_ACTION, CLEAR_STATUS_TEXT);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(ReplaceAction.class, REPLACE_ACTION));
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
                if (eui != null) {
                    JComponent comp = eui.hasExtComponent() ? eui.getExtComponent() : null;
                    if (comp != null) {
                        JPanel jp = findComponent(comp, SearchJPanel.class, 5);
                        if (jp != null) {
                            SearchBar searchBar = SearchBar.getInstance(eui.getComponent());
                            jp.add(searchBar);
                            jp.add(ReplaceBar.getInstance(searchBar));
                            ReplaceBar.getInstance(searchBar).gainFocus();
                            makeSearchAndReplaceBarPersistent();
                        }
                    }
                }
            }
        }
    }

    private static <T> T findComponent(Container container, Class<T> componentClass, int depth) {
        if (depth > 0) {
            for (Component c : container.getComponents()) {
                if (componentClass.isAssignableFrom(c.getClass())) {
                    @SuppressWarnings("unchecked")
                    T target = (T) c;
                    return target;
                } else if (c instanceof Container) {
                    T target = findComponent((Container) c, componentClass, depth - 1);
                    if (target != null) {
                        return target;
                    }
                }
            }
        }
        return null;
    }
    private static PropertyChangeListener searchAndReplaceBarPersistentListener = null;

    private static void makeSearchAndReplaceBarPersistent() {
        if (searchAndReplaceBarPersistentListener == null) {
            searchAndReplaceBarPersistentListener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(EditorRegistry.FOCUS_GAINED_PROPERTY)
                            && SearchBar.getInstance().getActualTextComponent() != EditorRegistry.lastFocusedComponent()
                            && SearchBar.getInstance().isVisible()) {
                        EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(EditorRegistry.lastFocusedComponent());
                        if (eui != null) {
                            JComponent comp = eui.hasExtComponent() ? eui.getExtComponent() : null;
                            if (comp != null) {
                                JPanel jp = findComponent(comp, SearchJPanel.class, 5);
                                if (jp != null) {
                                    SearchBar searchBarInstance = SearchBar.getInstance(eui.getComponent());
                                    ReplaceBar replaceBarInstance = ReplaceBar.getInstance(searchBarInstance);
                                    jp.add(searchBarInstance);
                                    if (replaceBarInstance.isVisible()) {
                                        jp.add(replaceBarInstance);
                                        if (replaceBarInstance.hadFocusOnTextField()) {
                                            replaceBarInstance.gainFocus();
                                        }
                                    }
                                    jp.revalidate();

                                    if (searchBarInstance.hadFocusOnTextField()) {
                                        searchBarInstance.gainFocus();
                                    }
                                }
                            }
                        }
                    }
                }
            };
            EditorRegistry.addPropertyChangeListener(searchAndReplaceBarPersistentListener);
        }
    }
}