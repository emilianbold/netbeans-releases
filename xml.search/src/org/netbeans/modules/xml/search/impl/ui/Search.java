/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.search.impl.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openide.DialogDescriptor;
import org.netbeans.modules.xml.search.api.SearchException;
import org.netbeans.modules.xml.search.api.SearchMatch;
import org.netbeans.modules.xml.search.api.SearchOption;
import org.netbeans.modules.xml.search.api.SearchTarget;
import org.netbeans.modules.xml.search.spi.SearchEngine;
import org.netbeans.modules.xml.search.spi.SearchProvider;
import org.netbeans.modules.xml.search.impl.output.View;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.23
 */
public final class Search extends Dialog {

    public void show(SearchEngine engine, SearchProvider provider) {
        if (engine == null) {
            return;
        }
        myTargets = provider.getTargets();
        myProvider = provider;
        mySearchEngine = engine;

        mySearchEngine.removeSearchListeners();
        mySearchEngine.addSearchListener(new View());
        mySearchEngine.addSearchListener(new Progress());

        show();

        a11y(getUIComponent(), i18n("ACS_Advanced_Search")); // NOI18N
    }

    @Override
    protected void updated() {
//out("UPDATED");
        setItems(myTarget, myTargets);
        myTarget.init();
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;

        // text
        c.insets = new Insets(LARGE_SIZE, 0, 0, 0);
        panel.add(createTextPanel(), c);

        // option
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(createSeparator(i18n("LBL_Options")), c); // NOI18N
        panel.add(createOptionPanel(), c);

        return panel;
    }

    private JComponent createTextPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;

        // text
        c.gridy++;
        c.insets = new Insets(TINY_SIZE, 0, TINY_SIZE, 0);
        JLabel label = createLabel(i18n("LBL_Name")); // NOI18N
        panel.add(label, c);

        c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        myName = new Field(ASTERISK);
        setWidth(myName.getUIComponent(), TEXT_WIDTH);
        label.setLabelFor(myName.getUIComponent());
        panel.add(myName.getUIComponent(), c);

        // type
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.insets = new Insets(TINY_SIZE, 0, TINY_SIZE, 0);
        label = createLabel(i18n("LBL_Target")); // NOI18N
        panel.add(label, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        c.weightx = 1.0;
        myTarget = createComboBox(myTargets);
        a11y(myTarget, i18n("ACS_Target")); // NOI18N
        label.setLabelFor(myTarget);
        panel.add(myTarget, c);

        return panel;
    }

    private JComponent createOptionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, LARGE_SIZE, 0, 0);
        c.weightx = 1.0;

        c.gridy++;
        myMatchCase = createCheckBox(
            new ButtonAction(i18n("LBL_Match_Case")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                }
            }
        );
        panel.add(myMatchCase, c);

        c.gridy++;
        myPatternMatch = createCheckBox(
            new ButtonAction(i18n("LBL_Match_Pattern")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    exclusion(myPatternMatch, myRegularExpression);
                }
            }
        );
        myPatternMatch.setSelected(true);
        myPatternMatch.setEnabled(true);
        panel.add(myPatternMatch, c);

        c.gridy++;
        myRegularExpression = createCheckBox(
            new ButtonAction(i18n("LBL_Regular_Expression")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    exclusion(myRegularExpression, myPatternMatch);
                }
            }
        );
        myRegularExpression.setSelected(false);
        myRegularExpression.setEnabled(false);
        panel.add(myRegularExpression, c);

        return panel;
    }

    private void exclusion(JCheckBox checkBox1, JCheckBox checkBox2) {
        checkBox1.setEnabled(!checkBox2.isSelected());

        if (checkBox2.isSelected()) {
            checkBox1.setSelected(false);
        }
        checkBox2.setEnabled(!checkBox1.isSelected());

        if (checkBox1.isSelected()) {
            checkBox2.setSelected(false);
        }
    }

    private SearchMatch getMatch() {
        if (myPatternMatch.isSelected()) {
            return SearchMatch.PATTERN;
        }
        if (myRegularExpression.isSelected()) {
            return SearchMatch.REGULAR_EXPRESSION;
        }
        return null;
    }

    private void search() {
        myDescriptor.setClosingOptions(new Object[] { mySearchButton, DialogDescriptor.CANCEL_OPTION });
        SearchOption option = new SearchOption.Adapter(
            myName.getText().trim(),
            myProvider,
            (SearchTarget) myTarget.getSelectedItem(),
            getMatch(),
            myMatchCase.isSelected(),
            false // use selection
        );
        try {
            mySearchEngine.search(option);
        } catch (SearchException e) {
            myDescriptor.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
            printError(i18n("ERR_Pattern_Error", e.getMessage())); // NOI18N
        }
    }

    private void close() {
        myName.save();
        mySearchEngine = null;
        myProvider = null;
        myTargets = null;
    }

    @Override
    protected DialogDescriptor createDescriptor() {
        Object[] buttons = getButtons();
        myDescriptor = new DialogDescriptor(
            getResizableX(createPanel()),
            i18n("LBL_Advanced_Search"), // NOI18N
            true, // modal
            buttons,
            mySearchButton,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (mySearchButton == event.getSource()) {
                        search();
                        close();
                    }
                }
            }
        );
        return myDescriptor;
    }

    private Object[] getButtons() {
        mySearchButton = createButton(
            new ButtonAction(i18n("LBL_Search"), i18n("TLT_Search")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                }
            }
        );
        return new Object[] { mySearchButton, DialogDescriptor.CANCEL_OPTION };
    }

    private Field myName;
    private JButton mySearchButton;
    private JCheckBox myMatchCase;
    private JCheckBox myPatternMatch;
    private JCheckBox myRegularExpression;
    private MyComboBox myTarget;
    private SearchProvider myProvider;
    private SearchTarget[] myTargets;
    private SearchEngine mySearchEngine;
    private DialogDescriptor myDescriptor;
    private static final int TEXT_WIDTH = 200;
    private static final String ASTERISK = "*"; // NOI18N
}
