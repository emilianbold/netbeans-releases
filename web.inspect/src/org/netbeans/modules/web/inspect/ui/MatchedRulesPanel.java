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
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import org.netbeans.modules.web.inspect.ElementHandle;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.util.NbBundle;

/**
 * Panel that displays CSS/style rules influencing an element.
 *
 * @author Jan Stola
 */
public class MatchedRulesPanel extends JPanel {
    /** Page model used by this panel. */
    private PageModel pageModel = PageModel.getDefault();

    /**
     * Creates a new {@code MatchedRulesPanel}.
     */
    public MatchedRulesPanel() {
        setBackground(UIManager.getColor("TextArea.background")); // NOI18N
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        pageModel.addPropertyChangeListener(createModelListener());
        update();
    }

    /**
     * Updates the data shown in the panel.
     */
    private void update() {
        Collection<ElementHandle> selection = pageModel.getSelectedElements();
        final int selectionSize = selection.size();
        final Map<ElementHandle, List<PageModel.RuleInfo>> ruleData = new IdentityHashMap<ElementHandle, List<PageModel.RuleInfo>>();
        final ElementHandle selectedElement;
        if (selectionSize == 1) {
            // Pre-compute matched rules
            selectedElement = selection.iterator().next();
            ElementHandle element = selectedElement;
            while (element != null) {
                List<PageModel.RuleInfo> rules = pageModel.getMatchedRules(element);
                ruleData.put(element, rules);
                element = element.getParent();
            }
        } else {
            selectedElement = null;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                removeAll();
                if (selectionSize == 1) {
                    ElementHandle element = selectedElement;
                    boolean first = true;
                    while (element != null) {
                        String title;
                        if (first) {
                            title = NbBundle.getMessage(MatchedRulesPanel.class, "MatchedRulesPanel.matchedRules"); // NOI18N
                        } else {
                            String tagName = element.getTagName().toLowerCase();
                            title = NbBundle.getMessage(MatchedRulesPanel.class, "MatchedRulesPanel.inheritedFrom", tagName); // NOI18N
                        }
                        List<PageModel.RuleInfo> rules = ruleData.get(element);
                        if (first || !rules.isEmpty()) {
                            JPanel elementPanel = new MatchedRulesElementPanel(element, rules, title, true);
                            elementPanel.setAlignmentX(0);
                            add(elementPanel);
                        }
                        element = element.getParent();
                        first = false;
                    }
                } else {
                    String key = (selectionSize == 0) ? "MatchedRulesPanel.emptySelection" : "MatchedRulesPanel.multiSelection";
                    String message = NbBundle.getMessage(MatchedRulesPanel.class, key); // NOI18N
                    JLabel label = createMessageLabel(message);
                    add(label);
                }
                revalidate();
                repaint();
            }
        });
    }

    /**
     * Creates {@code PageModel} listener.
     * 
     * @return {@code PageModel} listener.
     */
    private PropertyChangeListener createModelListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (PageModel.PROP_SELECTED_ELEMENTS.equals(propName)) {
                    update();
                }
            }
        };
    }

    /**
     * Creates a label for the given message.
     * 
     * @param message message to show in the label.
     * @return label for the given message.
     */
    private JLabel createMessageLabel(String message) {
        JLabel label = new JLabel(message);
        label.setEnabled(false);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        return label;
    }

}
