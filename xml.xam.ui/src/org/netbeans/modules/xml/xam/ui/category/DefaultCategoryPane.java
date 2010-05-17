/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.xam.ui.category;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.modules.xml.xam.ui.search.DefaultSearchControlPanel;

/**
 * The default implementation of CategoryPane.
 *
 * @author Nathan Fiedler
 */
public class DefaultCategoryPane extends AbstractCategoryPane {
    /** The top component of our interface. */
    private JPanel visualComponent;
    /** Component for search interface. */
    private DefaultSearchControlPanel searchComponent;
    /** Where the Category components reside. */
    private JPanel categoryPanel;
    /** List of actions to show the categories. */
    private List<ShowCategoryAction> categoryActions;
    /** Makes the buttons act like radio buttons. */
    private ButtonGroup buttonGroup;

    /**
     * Creates a new instance of DefaultCategoryPane.
     */
    public DefaultCategoryPane() {
        categoryActions = new ArrayList<ShowCategoryAction>();
        initComponents();
    }

    public void addCategory(Category category) {
        categoryActions.add(new ShowCategoryAction(category, this));
    }

    public Component getComponent() {
        return visualComponent;
    }

    public SearchComponent getSearchComponent() {
        return searchComponent;
    }

    /**
     * Construct our user interface.
     */
    private void initComponents() {
        // Button group for our category buttons.
        buttonGroup = new ButtonGroup();

        // Build our overall container.
        visualComponent = new JPanel(new GridBagLayout()) {
            /** silence compiler warnings */
            private static final long serialVersionUID = 1L;

            @Override
            public void requestFocus() {
                super.requestFocus();
                // Pass focus to category component to ease keyboard navigation.
                Category cat = getCategory();
                if (cat != null) {
                    cat.getComponent().requestFocus();
                }
            }

            @Override
            public boolean requestFocusInWindow() {
                boolean retVal = super.requestFocusInWindow();
                // Pass focus to category component to ease keyboard navigation.
                Category cat = getCategory();
                if (cat != null) {
                    return cat.getComponent().requestFocusInWindow();
                }
                return retVal;
            }
        };

        // Build the primary view component.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0d;
        gbc.weighty = 1.0d;
        categoryPanel = new JPanel(new BorderLayout());
        visualComponent.add(categoryPanel, gbc);

        // Build the search component.
        searchComponent = new DefaultSearchControlPanel(this);
        searchComponent.hideComponent();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0d;
        gbc.weighty = 0.0d;
        visualComponent.add(searchComponent.getComponent(), gbc);
    }

    public void populateToolbar(JToolBar toolbar) {
        Category selected = getCategory();
        // Get the NetBeans button border, if available.
        Border border = UIManager.getBorder("nb.tabbutton.border"); //NOI18N
        for (ShowCategoryAction action : categoryActions) {
            JToggleButton button = new JToggleButton(action);
            // Action has a name for accessibility purposes, but we do
            // not want that to appear in the button label.
            button.setText(null);
            button.getAccessibleContext().setAccessibleName(action.getCategory().getTitle());
            button.setRolloverEnabled(true);
            if (border != null) {
                button.setBorder(border);
            }
            buttonGroup.add(button);
            if (selected == null) {
                if (buttonGroup.getButtonCount() == 1) {
                    // Make the first button the chosen one.
                    button.setSelected(true);
                    // Select the category so it is visible.
                    action.actionPerformed(null);
                }
            } else {
                if (action.getCategory().equals(selected)) {
                    button.setSelected(true);
                }
            }
            toolbar.add(button);
        }
    }

    @Override
    public void setCategory(Category category) {
        Category oldcat = getCategory();
        super.setCategory(category);
        if (oldcat != null) {
            Component oldcomp = oldcat.getComponent();
            categoryPanel.remove(oldcomp);
            oldcat.componentHidden();
        }
        Component newcomp = category.getComponent();
        categoryPanel.add(newcomp, BorderLayout.CENTER);
        category.componentShown();
        categoryPanel.validate();
        categoryPanel.repaint();
        searchComponent.updateSearchProviders();
    }

    /**
     * An action to show a category.
     */
    private static class ShowCategoryAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        /** The Category we show. */
        private transient Category category;
        /** The CategoryPane containing the Category. */
        private transient CategoryPane pane;
        
        /**
         * Creates a new instance of ShowCategoryAction.
         *
         * @param  category  the Category to be shown.
         * @param  pane      the CategoryPane for category.
         */
        public ShowCategoryAction(Category category, CategoryPane pane) {
            this.category = category;
            this.pane = pane;
            putValue(Action.NAME, category.getTitle());
            putValue(Action.SHORT_DESCRIPTION, category.getDescription());
            putValue(Action.SMALL_ICON, category.getIcon());
        }

        public void actionPerformed(ActionEvent e) {
            pane.setCategory(category);
        }

        /**
         * Return the Category associated with this action.
         *
         * @return  Category this action displays.
         */
        public Category getCategory() {
            return category;
        }
    }

    public void close() {
        //super.setCategory(null);
        visualComponent.removeAll();
        categoryPanel.removeAll();
        categoryActions.clear();
        visualComponent = null;
        categoryPanel = null;
        categoryActions = null;
    }
}
