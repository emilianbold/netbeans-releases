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

import java.awt.SystemColor;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.GroupLayout.*;
import javax.swing.LayoutStyle.*;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.actions.GoToRuleAction;
import org.openide.util.NbBundle;

/**
 * Panel that displays information about one rule.
 *
 * @author Jan Stola
 */
public class MatchedRulesRulePanel extends JPanel {

    /**
     * Creates a new {@code MatchedRulesRulePanel}.
     * 
     * @param rule rule to visualize.
     */
    public MatchedRulesRulePanel(PageModel.RuleInfo rule) {
        setBackground(UIManager.getColor("TextArea.background")); // NOI18N
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);

        SequentialGroup verticalGroup = layout.createSequentialGroup();
        ParallelGroup horizontalGroup = layout.createParallelGroup(Alignment.LEADING);

        // Style-sheet source
        JComponent styleSheetSourceComponent = createStyleSheetSourceComponent(rule);
        verticalGroup.addContainerGap();
        verticalGroup.addComponent(styleSheetSourceComponent);
        horizontalGroup.addComponent(styleSheetSourceComponent);

        // Rule start
        JComponent ruleStartComponent = createRuleStartComponent(rule.getSelector());
        verticalGroup.addPreferredGap(ComponentPlacement.RELATED);
        verticalGroup.addComponent(ruleStartComponent);
        horizontalGroup.addComponent(ruleStartComponent);

        // Rules
        SequentialGroup rulesHorizontalGroup = layout.createSequentialGroup();
        rulesHorizontalGroup.addGap(20);
        ParallelGroup ruleNamesHorizontalGroup = layout.createParallelGroup();
        ParallelGroup ruleValuesHorizontalGroup = layout.createParallelGroup();
        rulesHorizontalGroup.addGroup(ruleNamesHorizontalGroup);
        rulesHorizontalGroup.addPreferredGap(ComponentPlacement.RELATED);
        rulesHorizontalGroup.addGroup(ruleValuesHorizontalGroup);
        horizontalGroup.addGroup(rulesHorizontalGroup);
        // Extensions return low-level properties only and they do not
        // honor the order of the properties. So, there is probably
        // no reason not to sort the properties.
        Map<String,String> sortedStyle = new TreeMap<String,String>(rule.getStyle());
        for (Map.Entry<String,String> entry : sortedStyle.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            JComponent nameComponent = createPropertyNameComponent(name);
            JComponent valueComponent = createPropertyValueComponent(value);
            ruleNamesHorizontalGroup.addComponent(nameComponent);
            ruleValuesHorizontalGroup.addComponent(valueComponent);
            ParallelGroup rulesVerticalGroup = layout.createParallelGroup(Alignment.BASELINE);
            rulesVerticalGroup.addComponent(nameComponent);
            rulesVerticalGroup.addComponent(valueComponent);
            verticalGroup.addPreferredGap(ComponentPlacement.RELATED);
            verticalGroup.addGroup(rulesVerticalGroup);
        }

        // Rule end
        JComponent ruleEndComponent = createRuleEndComponent();
        verticalGroup.addPreferredGap(ComponentPlacement.RELATED);
        verticalGroup.addComponent(ruleEndComponent);
        horizontalGroup.addComponent(ruleEndComponent);

        verticalGroup.addContainerGap();

        // Set groups to layout
        layout.setVerticalGroup(verticalGroup);
        SequentialGroup group = layout.createSequentialGroup();
        group.addContainerGap();
        group.addGroup(horizontalGroup);
        group.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setHorizontalGroup(group);
    }

    /**
     * Creates a component that displays information about the source
     * of the rule.
     * 
     * @param rule rule whose source should be descrined by the returned component.
     * @return component that displays information about the source.
     */
    private JComponent createStyleSheetSourceComponent(PageModel.RuleInfo rule) {
        String sourceURL = rule.getSourceURL();
        JLabel label;
        if (sourceURL == null) {
            // embedded stylesheet
            label = new JLabel();
            label.setText(NbBundle.getMessage(MatchedRulesRulePanel.class,
                    "MatchedRulesRulePanel.embeddedStyleSheet")); // NOI18N
        } else if (sourceURL.startsWith("file://")) { // NOI18N
            Hyperlink hyperlink = new Hyperlink();
            hyperlink.setAction(new GoToRuleAction(rule));
            hyperlink.setText(sourceURL);
            label = hyperlink;
        } else {
            label = new JLabel();
            label.setText(sourceURL);
        }
        label.setForeground(SystemColor.textInactiveText);
        return label;
    }

    /**
     * Creates a component that displays the start of the rule.
     * 
     * @param selector selector of the rule.
     * @return component that displays the start of the rule.
     */
    private JComponent createRuleStartComponent(String selector) {
        String text = NbBundle.getMessage(MatchedRulesRulePanel.class,
                "MatchedRulesRulePanel.ruleStart", selector); // NOI18N
        JLabel label = new JLabel(text);
        return label;
    }

    /**
     * Creates a component that displays the end of the rule.
     * 
     * @return component that displays the end of the rule.
     */
    private JComponent createRuleEndComponent() {
        String text = NbBundle.getMessage(MatchedRulesRulePanel.class,
                "MatchedRulesRulePanel.ruleEnd"); // NOI18N
        return new JLabel(text);
    }

    /**
     * Creates a component that displays the name of the specified property.
     * 
     * @param name name of the property.
     * @return component that displays the name of the specified property.
     */
    private JComponent createPropertyNameComponent(String name) {
        String text = NbBundle.getMessage(MatchedRulesRulePanel.class,
                "MatchedRulesRulePanel.propertyName", name); // NOI18N
        JLabel label = new JLabel(text);
        return label;
    }

    /**
     * Creates a component that displays the value of the specified property.
     * 
     * @param value value of the property.
     * @return component that displays the value of the specified property.
     */
    private JComponent createPropertyValueComponent(String value) {
        String text = NbBundle.getMessage(MatchedRulesRulePanel.class,
                "MatchedRulesRulePanel.propertyValue", value); // NOI18N
        JLabel label = new JLabel(text);
        return label;
    }
    
}
