/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.api.build.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class AdvancedTaskPanel extends JPanel {

    private final String buildToolExec;

    // GuardedBy("EDT")
    DialogDescriptor descriptor;


    private AdvancedTaskPanel(String tasksLabel, String buildToolExec, List<String> tasks) {
        assert EventQueue.isDispatchThread();
        assert tasksLabel != null;
        assert buildToolExec != null;
        assert tasks != null;

        this.buildToolExec = buildToolExec;

        initComponents();
        init(tasksLabel, tasks);
    }

    @CheckForNull
    public static Pair<Boolean, String> open(String title, String tasksLabel, String buildToolExec, List<String> tasks) {
        assert EventQueue.isDispatchThread();
        AdvancedTaskPanel panel = new AdvancedTaskPanel(tasksLabel, buildToolExec, tasks);
        DialogDescriptor descriptor = new DialogDescriptor(panel, title, true, null);
        panel.descriptor = descriptor;
        // ui
        panel.taskChanged();
        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION) {
            return Pair.of(panel.isSharedTask(), panel.getTask());
        }
        return null;
    }

    private void init(String tasksLabel, List<String> tasks) {
        Mnemonics.setLocalizedText(this.tasksLabel, tasksLabel);
        tasksComboBox.setModel(new DefaultComboBoxModel(tasks.toArray(new String[tasks.size()])));
        // listeners
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        optionsTextField.getDocument().addDocumentListener(defaultDocumentListener);
        tasksComboBox.addActionListener(new DefaultActionListener());
        parametersTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    String getTask() {
        StringBuilder sb = new StringBuilder();
        String options = optionsTextField.getText();
        if (StringUtilities.hasText(options)) {
            sb.append(options);
        }
        String tasks = (String) tasksComboBox.getSelectedItem();
        if (StringUtilities.hasText(tasks)) {
            if (sb.length() > 0) {
                sb.append(" "); // NOI18N
            }
            sb.append(tasks);
        }
        String parameters = parametersTextField.getText();
        if (StringUtilities.hasText(parameters)) {
            if (sb.length() > 0) {
                sb.append(" "); // NOI18N
            }
            sb.append(parameters);
        }

        return sb.toString();
    }

    boolean isSharedTask() {
        return sharedCheckBox.isSelected();
    }

    void taskChanged() {
        validateTask();
        setPreview();
    }

    private void validateTask() {
        assert EventQueue.isDispatchThread();
        descriptor.setValid(StringUtilities.hasText(getTask()));
    }

    private void setPreview() {
        previewTextField.setText(buildToolExec + " " + getTask()); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        optionsLabel = new JLabel();
        optionsTextField = new JTextField();
        tasksLabel = new JLabel();
        tasksComboBox = new JComboBox<String>();
        parametersLabel = new JLabel();
        parametersTextField = new JTextField();
        previewLabel = new JLabel();
        previewTextField = new JTextField();
        sharedCheckBox = new JCheckBox();

        optionsLabel.setLabelFor(optionsTextField);
        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(AdvancedTaskPanel.class, "AdvancedTaskPanel.optionsLabel.text")); // NOI18N

        tasksLabel.setLabelFor(tasksComboBox);
        Mnemonics.setLocalizedText(tasksLabel, "TASKS:"); // NOI18N

        tasksComboBox.setEditable(true);

        parametersLabel.setLabelFor(parametersTextField);
        Mnemonics.setLocalizedText(parametersLabel, NbBundle.getMessage(AdvancedTaskPanel.class, "AdvancedTaskPanel.parametersLabel.text")); // NOI18N

        previewLabel.setLabelFor(previewTextField);
        Mnemonics.setLocalizedText(previewLabel, NbBundle.getMessage(AdvancedTaskPanel.class, "AdvancedTaskPanel.previewLabel.text")); // NOI18N

        previewTextField.setEditable(false);

        sharedCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(sharedCheckBox, NbBundle.getMessage(AdvancedTaskPanel.class, "AdvancedTaskPanel.sharedCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(parametersLabel)
                    .addComponent(tasksLabel)
                    .addComponent(optionsLabel)
                    .addComponent(previewLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(optionsTextField)
                    .addComponent(parametersTextField)
                    .addComponent(previewTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sharedCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(tasksComboBox, 0, 487, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(optionsLabel)
                    .addComponent(optionsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(tasksLabel)
                    .addComponent(tasksComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(parametersLabel)
                    .addComponent(parametersTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(previewTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(previewLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sharedCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel optionsLabel;
    private JTextField optionsTextField;
    private JLabel parametersLabel;
    private JTextField parametersTextField;
    private JLabel previewLabel;
    private JTextField previewTextField;
    private JCheckBox sharedCheckBox;
    private JComboBox<String> tasksComboBox;
    private JLabel tasksLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            taskChanged();
        }

    }

    private final class DefaultActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            taskChanged();
        }

    }

}
