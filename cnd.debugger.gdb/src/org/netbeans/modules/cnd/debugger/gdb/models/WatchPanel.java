/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.cnd.debugger.gdb.models;

import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.util.*;
import java.awt.BorderLayout;

/**
 * A GUI panel for customizing a Watch.
 *
 * @author Maros Sandor
 */
public class WatchPanel {

    private JPanel panel;
    private JTextField textField;
    private String expression;

    public WatchPanel(String expression) {
        this.expression = expression;
    }

    public JComponent getPanel() {
        if (panel != null) {
            return panel;
        }

        panel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);

        panel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_WatchPanel")); // NOI18N
        JLabel textLabel = new JLabel(bundle.getString("CTL_Watch_Name")); // NOI18N
        textLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(11, 12, 1, 11));
        panel.add("West", textLabel); // NOI18N
        panel.add("Center", textField = new JTextField(25)); // NOI18N
        textField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Watch_Name")); // NOI18N
        textField.setBorder(new CompoundBorder(textField.getBorder(), new EmptyBorder(2, 0, 2, 0)));
        textLabel.setDisplayedMnemonic(bundle.getString("CTL_Watch_Name_Mnemonic").charAt(0)); // NOI18N
        String t = Utils.getIdentifier();
        if (t != null) {
            textField.setText(t);
        } else {
            textField.setText(expression);
        }
        textField.selectAll();

        textLabel.setLabelFor(textField);
        textField.requestFocus();
        return panel;
    }

    public String getExpression() {
        return textField.getText().trim();
    }
}
