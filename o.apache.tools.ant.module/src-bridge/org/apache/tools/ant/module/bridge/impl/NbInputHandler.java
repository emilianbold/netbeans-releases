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

package org.apache.tools.ant.module.bridge.impl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author David Konecny, Dusan Balek, Jesse Glick
 */
final class NbInputHandler implements InputHandler {

    private JComboBox combo = null;
    private JTextField input = null;
    private final Runnable interestingOutputCallback;

    public NbInputHandler(Runnable interestingOutputCallback) {
        this.interestingOutputCallback = interestingOutputCallback;
    }

    public void handleInput(InputRequest request) throws BuildException {
        interestingOutputCallback.run();

        // #30196 - for one Ant script containing several <input> tasks there will be created
        // just one instance of the NbInputHandler. So it is necessary to cleanup the instance
        // used by the previous <input> task first.
        combo = null;
        input = null;

        JPanel panel = createPanel(request);
        DialogDescriptor dlg = new DialogDescriptor(panel,
        NbBundle.getMessage(NbInputHandler.class, "TITLE_input_handler")); //NOI18N
        do {
            DialogDisplayer.getDefault().createDialog(dlg).setVisible(true);
            if (dlg.getValue() != NotifyDescriptor.OK_OPTION) {
                throw new BuildException(NbBundle.getMessage(NbInputHandler.class, "MSG_input_aborted")); //NOI18N
            }
            String value;
            if (combo != null) {
                value = (String) combo.getSelectedItem();
            } else {
                value = input.getText();
            }
            request.setInput(value);
        } while (!request.isInputValid());
    }

    private JPanel createPanel(InputRequest request) {

        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());

        JLabel jLabel1 = new JLabel(request.getPrompt());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(12, 12, 11, 6);
        pane.add(jLabel1, gridBagConstraints);

        JComponent comp = null;
        if (request instanceof MultipleChoiceInputRequest) {
            combo = new JComboBox(((MultipleChoiceInputRequest)request).getChoices());
            if (defaultValue != null && defaultValue.length() > 0) {
                combo.setSelectedItem(getDefaultValue(request));
            }
            comp = combo;
        } else {
            input = new JTextField(getDefaultValue(request), 25);
            comp = input;
        }

        comp.getAccessibleContext().setAccessibleDescription(
        NbBundle.getMessage(NbInputHandler.class, "ACSD_input_handler")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(12, 6, 11, 6);
        pane.add(comp, gridBagConstraints);

        jLabel1.setLabelFor(comp);
        if (request.getPrompt().length() > 0)
            jLabel1.setDisplayedMnemonic(request.getPrompt().charAt(0));

        pane.getAccessibleContext().setAccessibleName(
        NbBundle.getMessage(NbInputHandler.class, "TITLE_input_handler")); // NOI18N
        pane.getAccessibleContext().setAccessibleDescription(
        NbBundle.getMessage(NbInputHandler.class, "ACSD_input_handler")); // NOI18N

        HelpCtx.setHelpIDString(pane, "org.apache.tools.ant.module.run.NBInputHandler"); // NOI18N

        return pane;
    }

    private static String defaultValue;
    static void setDefaultValue(String d) {
        defaultValue = d;
    }
    private static String getDefaultValue(InputRequest req) {
        try {
            // Ant 1.7.0+
            return (String) InputRequest.class.getMethod("getDefaultValue").invoke(req); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK, Ant 1.6.5 or earlier
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return defaultValue;
    }

}
