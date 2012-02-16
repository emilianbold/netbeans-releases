/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.ui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import org.netbeans.modules.web.inspect.ElementHandle;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.util.NbBundle;

/**
 * Panel that displays CSS/style rules matching an element.
 *
 * @author Jan Stola
 */
public class MatchedRulesElementPanel extends JPanel {
    /** Collapsed icon (used in title). */
    static final Icon ICON_COLLAPSED;
    /** Expanded icon (used in title). */
    static final Icon ICON_EXPANDED;
    /** Sub-panel showing the rule information. */
    private JPanel rulesPanel;

    static {
        boolean gtk = "GTK".equals(UIManager.getLookAndFeel().getID()); // NOI18N
        ICON_COLLAPSED = UIManager.getIcon(gtk ? "Tree.gtk_collapsedIcon" : "Tree.collapsedIcon"); // NOI18N
        ICON_EXPANDED = UIManager.getIcon(gtk ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon"); // NOI18N
    }

    /**
     * Creates a new {@code MatchedRulesElementPanel}.
     * 
     * @param element element whose matched rules should be shown.
     * @param rules rules to show.
     * @param title title for this set of rules.
     * @param expanded determines whether the rules should be shown or hidden at the beginning.
     */
    public MatchedRulesElementPanel(ElementHandle element, List<PageModel.RuleInfo> rules, String title, boolean expanded) {
        setBackground(UIManager.getColor("TextArea.background")); // NOI18N
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        rulesPanel = new JPanel();
        rulesPanel.setAlignmentX(0);
        rulesPanel.setBackground(getBackground());
        rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.PAGE_AXIS));
        JComponent titleComponent = createTitleComponent(title, expanded);
        add(titleComponent);
        if (rules.isEmpty()) {
            rulesPanel.add(createNoRulesLabel());
        } else {
            for (PageModel.RuleInfo rule : rules) {
                JPanel rulePanel = new MatchedRulesRulePanel(rule);
                rulesPanel.add(rulePanel);
            }
        }
        add(rulesPanel);
    }

    /**
     * Creates a label that is shown when there are no rules to display.
     * 
     * @return label that is shown when there are no rules to display.
     */
    private JLabel createNoRulesLabel() {
        String text = NbBundle.getMessage(MatchedRulesElementPanel.class, "MatchedRulesElementPanel.noRules"); // NOI18N
        JLabel label = new JLabel(text);
        LayoutStyle style = LayoutStyle.getInstance();
        label.setBorder(BorderFactory.createEmptyBorder(
                style.getContainerGap(label, SwingConstants.NORTH, null),
                style.getContainerGap(label, SwingConstants.WEST, null),
                style.getContainerGap(label, SwingConstants.SOUTH, null),
                style.getContainerGap(label, SwingConstants.EAST, null)
        ));
        label.setEnabled(false);
        Dimension dim = label.getMaximumSize();
        label.setMaximumSize(new Dimension(Short.MAX_VALUE, dim.height));
        return label;
    }

    /**
     * Creates the component that serves as the title for the rules.
     * 
     * @param title text to show in the title.
     * @param expanded determines whether the rules should be shown or hidden
     * at the beginning (the title component is responsible for showing/hiding
     * the rules).
     * @return component that serves as the title for the rules.
     */
    private JComponent createTitleComponent(String title, boolean expanded) {
        return new TitleCheckBox(title, expanded);
    }

    /**
     * Component that serves as the title for the rules.
     */
    class TitleCheckBox extends JCheckBox {

        /**
         * Creates a new {@code TitleCheckBox}.
         * 
         * @param text text to show in the title.
         * @param selected determines if the checkbox should be selected
         * (and the set of rules shown).
         */
        TitleCheckBox(String text, boolean selected) {
            setText(text);
            setSelected(selected);
            setMargin(new Insets(0, 0, 0, 0));
            setFocusPainted(false);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    update();
                }
            });
            update();
        }

        /**
         * Updates the component - updates the icon and collapses/expands
         * the panel with the rules.
         */
        private void update() {
            boolean selected = isSelected();
            setIcon(selected ? ICON_EXPANDED : ICON_COLLAPSED);
            rulesPanel.setVisible(selected);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension dim = super.getMaximumSize();
            return new Dimension(Short.MAX_VALUE, dim.height);
        }
        
    }

}
