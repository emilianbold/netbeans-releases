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
package org.netbeans.modules.bpel.nodes.actions;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.openide.DialogDescriptor;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.04.07
 */
final class JavaScriptMap extends Dialog {

    JavaScriptMap(JTextField textField, boolean isInput, String[] bpelVariables) {
        myIsInput = isInput;
        myTextField = textField;
        myBpelVariables = bpelVariables;
    }

    @Override
    protected final DialogDescriptor createDescriptor() {
        myDescriptor = new DialogDescriptor(
            createPanel(),
            i18n("LBL_JavaScript_Map"), // NOI18N
            true,
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (myDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
                        return;
                    }
                    myTextField.setText(myTable.getValue());
                }
            }
        );
        return myDescriptor;
    }

    @Override
    protected final void opened() {
        myCreateButton.requestFocus();
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(LARGE_SIZE, TINY_SIZE, TINY_SIZE, SMALL_SIZE);
        myTable = new JavaScriptTable(myTextField.getText(), myIsInput);

        // left
        c.gridy++;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(HUGE_SIZE, MEDIUM_SIZE, TINY_SIZE, MEDIUM_SIZE);
        c.fill = GridBagConstraints.NONE;
        JLabel leftLabel = createLabel(getLeftLabel());
        panel.add(leftLabel, c);

        // variables
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(HUGE_SIZE, MEDIUM_SIZE, TINY_SIZE, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        myLeftVariables = createLeftVariables();
        leftLabel.setLabelFor(myLeftVariables);
        panel.add(myLeftVariables, c);

        // create button
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(HUGE_SIZE, HUGE_SIZE, TINY_SIZE, MEDIUM_SIZE);
        c.fill = GridBagConstraints.HORIZONTAL;
        myCreateButton = createButton(
            new ButtonAction(
            i18n("LBL_Create_Map"), // NOI18N
            i18n("TLT_Create_Map")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    createMap();
                }
            }
        );
        panel.add(myCreateButton, c);

        // right
        c.gridy++;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(MEDIUM_SIZE, MEDIUM_SIZE, TINY_SIZE, MEDIUM_SIZE);
        c.fill = GridBagConstraints.NONE;
        JLabel rightLabel = createLabel(getRightLabel());
        panel.add(rightLabel, c);

        // variables
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(MEDIUM_SIZE, MEDIUM_SIZE, TINY_SIZE, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        myRightVariables = createRightVariables();
        rightLabel.setLabelFor(myRightVariables);
        panel.add(myRightVariables, c);

        // table
        c.gridy++;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        c.insets = new Insets(HUGE_SIZE, MEDIUM_SIZE, TINY_SIZE, 0);
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(myTable), c);

        // delete button
        c.gridwidth = 1;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.insets = new Insets(HUGE_SIZE, HUGE_SIZE, TINY_SIZE, MEDIUM_SIZE);
        c.fill = GridBagConstraints.HORIZONTAL;
        JButton deleteButton = createButton(
            new ButtonAction(
            i18n("LBL_Delete_Map"), // NOI18N
            i18n("TLT_Delete_Map")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                    deleteMap();
                }
            }
        );
        panel.add(deleteButton, c);

        return panel;
    }

    private String getLeftLabel() {
        return myIsInput ? JAVA_SCRIPT_VARIABLE : BPEL_VARIABLE;
    }

    private String getRightLabel() {
        return myIsInput ? BPEL_VARIABLE : JAVA_SCRIPT_VARIABLE;
    }

    private JComboBox createLeftVariables() {
        return myIsInput ? createJavaScriptVariables() : createBpelVariables();
    }

    private JComboBox createRightVariables() {
        return myIsInput ? createBpelVariables() : createJavaScriptVariables();
    }

    private JComboBox createJavaScriptVariables() {
        JComboBox comboBox = new JComboBox(myTable.getJavaScriptVariables(myIsInput));
        comboBox.setEditable(true);
        return comboBox;
    }

    private JComboBox createBpelVariables() {
        JComboBox comboBox = new JComboBox(myBpelVariables);
        comboBox.setEditable(false);
        return comboBox;
    }

    private void createMap() {
        Object left = myLeftVariables.getSelectedItem();

        if (left == null) {
            return;
        }
        Object right = myRightVariables.getSelectedItem();

        if (right == null) {
            return;
        }
        myTable.add(left.toString(), right.toString());
    }

    private void deleteMap() {
        myTable.delete();
    }

    private boolean myIsInput;
    private JButton myCreateButton;
    private JTextField myTextField;
    private JavaScriptTable myTable;
    private String[] myBpelVariables;
    private JComboBox myLeftVariables;
    private JComboBox myRightVariables;
    private DialogDescriptor myDescriptor;

    private static final String BPEL_VARIABLE = org.netbeans.modules.xml.misc.UI.i18n(JavaScriptMap.class, "LBL_BPEL_Variable"); // NOI18N
    private static final String JAVA_SCRIPT_VARIABLE = org.netbeans.modules.xml.misc.UI.i18n(JavaScriptMap.class, "LBL_JavaScript_Variable"); // NOI18N
}
