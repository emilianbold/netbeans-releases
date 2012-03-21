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
import org.netbeans.modules.web.inspect.CSSUtils;
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
    private PageModel pageModel;

    /**
     * Creates a new {@code MatchedRulesPanel}.
     */
    public MatchedRulesPanel(PageModel pageModel) {
        this.pageModel = pageModel;
        setBackground(UIManager.getColor("TextArea.background")); // NOI18N
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        if (pageModel != null) {
            pageModel.addPropertyChangeListener(createModelListener());
        }
        update();
    }

    /**
     * Updates the data shown in the panel.
     */
    private void update() {
        Collection<ElementHandle> selection;
        if (pageModel == null) {
            selection = Collections.EMPTY_LIST;
        } else {
            selection = pageModel.getSelectedElements();
        }
        final int selectionSize = selection.size();
        final Map<ElementHandle, List<PageModel.RuleInfo>> ruleData;
        final ElementHandle selectedElement;
        if (selectionSize == 1) {
            selectedElement = selection.iterator().next();
            ruleData = collectRuleData(selectedElement);
        } else {
            selectedElement = null;
            ruleData = null;
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
                            String selector = CSSUtils.selectorFor(element, false);
                            title = NbBundle.getMessage(MatchedRulesPanel.class, "MatchedRulesPanel.inheritedFrom", tagName, selector); // NOI18N
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

    /**
     * Collects the rules that affect the specified element.
     * 
     * @param element element whose style information should be returned.
     * @return map with rules that affect the specified element,
     * the map maps parent elements to set of rules that match the
     * (parent) element and affect the specified element.
     */
    private Map<ElementHandle, List<PageModel.RuleInfo>> collectRuleData(ElementHandle element) {
        Map<ElementHandle, List<PageModel.RuleInfo>> ruleData = new IdentityHashMap<ElementHandle, List<PageModel.RuleInfo>>();
        boolean first = true;
        Set<String> inheritedExplicitly = new HashSet<String>();
        while (element != null) {
            List<PageModel.RuleInfo> matchedRules = pageModel.getMatchedRules(element);
            matchedRules = filterRules(matchedRules);
            List<PageModel.RuleInfo> rules;
            if (first) {
                // Marking all properties of the given element as inherited explicitly.
                // This minor hack allows us not to keep the branch for the 'first'
                // case as small as possible.
                for (PageModel.RuleInfo rule : matchedRules) {
                    for (Map.Entry<String,String> property : rule.getStyle().entrySet()) {
                        String propertyName = property.getKey();
                        propertyName = convertPropertyName(propertyName);
                        inheritedExplicitly.add(propertyName);
                    }
                }
                first = false;
            }
            
            // Properties that the processed element inherits explicitly from the parent
            Set<String> newInheritedExplicitly = new HashSet<String>();

            // Rules that affect the deepest element - all matched rules
            // of the deepest element and inherited rules of its parents.
            rules = new LinkedList<PageModel.RuleInfo>();
            for (PageModel.RuleInfo rule : matchedRules) {
                Map<String,String> inherited = new HashMap<String,String>();
                for (Map.Entry<String,String> property : rule.getStyle().entrySet()) {
                    String propertyName = property.getKey();
                    propertyName = convertPropertyName(propertyName);
                    String propertyValue = property.getValue();
                    if (!isDisplayedProperty(propertyName)) {
                        continue;
                    }
                    // Consider unknown properties as inherited, better to show
                    // them then not (despite they probably are not inherited)
                    boolean explicitlyInherited = inheritedExplicitly.contains(propertyName);
                    if (explicitlyInherited
                            || !CSSUtils.isKnownProperty(propertyName)
                            || CSSUtils.isInheritedProperty(propertyName)) {
                        inherited.put(propertyName, propertyValue);
                    }
                    if (explicitlyInherited && CSSUtils.isInheritValue(propertyValue)) {
                        newInheritedExplicitly.add(propertyName);
                    }
                }
                // Do not include the rule if none of its properties affect the element
                if (!inherited.isEmpty()) {
                    rules.add(new PageModel.RuleInfo(
                            rule.getSourceURL(),
                            rule.getSelector(),
                            inherited));
                }
            }
            ruleData.put(element, rules);
            inheritedExplicitly = newInheritedExplicitly;
            element = element.getParent();
        }
        return ruleData;
    }

    /**
     * Filters matched rules, i.e., excludes the ones that shouldn't
     * be shown to the user.
     * 
     * @param rules list of rules to filter.
     * @return list that contains only the rules that should be displayed.
     */
    private List<PageModel.RuleInfo> filterRules(List<PageModel.RuleInfo> rules) {
        List<PageModel.RuleInfo> filteredRules = new LinkedList<PageModel.RuleInfo>();
        for (PageModel.RuleInfo rule : rules) {
            if (isDisplayedRule(rule)) {
                filteredRules.add(rule);
            }
        }
        return filteredRules;
    }

    /**
     * Determines whether the specified rule should be shown to the user or not.
     * 
     * @param rule rule to check.
     * @return {@code true} when the rule should be displayed,
     * returns {@code false} otherwise.
     */
    private boolean isDisplayedRule(PageModel.RuleInfo rule) {
        String sourceURL = rule.getSourceURL();
        // Do not show Firefox internal stylesheets like
        // about:PreferenceStyleSheet or resource://gre-resources/ua.css
        return (sourceURL == null)
                || (!sourceURL.startsWith("about:") // NOI18N
                && !sourceURL.startsWith("resource:")); // NOI18N
    }

    /**
     * Converts the given property name to another (more suitable).
     * 
     * @param propertyName property name to convert.
     * @return more suitable name of the property.
     */
    private String convertPropertyName(String propertyName) {
        // Get rid of some bidi-related garbage generated by Firefox
        // (e.g., replace 'border-left-width-value' by 'border-left-width')
        if (propertyName.endsWith("-value") // NOI18N
                && (propertyName.contains("-left-") // NOI18N
                || propertyName.contains("-right-"))) { // NOI18N
            // Remove the -value suffix
            propertyName = propertyName.substring(0, propertyName.length()-6);
        }
        return propertyName;
    }
    
    /**
     * Determines whether the specified property should be shown to the user or not.
     * 
     * @param propertyName name of the property to check.
     * @return {@code true} when the property should be displayed,
     * returns {@code false} otherwise.
     */
    private boolean isDisplayedProperty(String propertyName) {
        // Do not show some garbage produced by Firefox
        // like border-left-color-ltr-source
        return !propertyName.endsWith("-ltr-source") && !propertyName.endsWith("-rtl-source"); // NOI18N
    }

}
