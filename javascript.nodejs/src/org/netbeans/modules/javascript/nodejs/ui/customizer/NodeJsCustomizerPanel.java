/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.ui.customizer;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferences;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferencesValidator;
import org.netbeans.modules.javascript.nodejs.ui.options.NodeJsOptionsPanelController;
import org.netbeans.modules.javascript.nodejs.util.ValidationResult;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;

public final class NodeJsCustomizerPanel extends JPanel {

    private final ProjectCustomizer.Category category;
    private final NodeJsPreferences preferences;

    volatile boolean enabled;
    volatile boolean defaultNode;
    volatile String node;


    public NodeJsCustomizerPanel(ProjectCustomizer.Category category, Project project) {
        assert category != null;
        assert project != null;

        this.category = category;
        preferences = NodeJsSupport.forProject(project).getPreferences();

        initComponents();
        init();
    }

    private void init() {
        // init
        enabled = preferences.isEnabled();
        enabledCheckBox.setSelected(enabled);
        node = preferences.getNode();
        nodeTextField.setText(node);
        defaultNode = preferences.isDefaultNode();
        defaultNodeCheckBox.setSelected(defaultNode);
        // ui
        enableAllFields();
        validateData();
        // listeners
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });
        enabledCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enabled = e.getStateChange() == ItemEvent.SELECTED;
                validateData();
                enableAllFields();
            }
        });
        nodeTextField.getDocument().addDocumentListener(new DocumentListener() {
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
                node = nodeTextField.getText();
                validateData();
            }
        });
        defaultNodeCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                defaultNode = e.getStateChange() == ItemEvent.SELECTED;
                validateData();
                enableNodeFields();
            }
        });
    }

    void enableAllFields() {
        enableNodeFields();
        defaultNodeCheckBox.setEnabled(enabled);
        configureNodeButton.setEnabled(enabled);
    }

    void enableNodeFields() {
        nodeTextField.setEnabled(enabled && !defaultNode);
        nodeBrowseButton.setEnabled(enabled && !defaultNode);
    }

    void validateData() {
        ValidationResult result = new NodeJsPreferencesValidator()
                .validate(enabled, defaultNode, node)
                .getResult();
        if (result.hasErrors()) {
            category.setErrorMessage(result.getFirstErrorMessage());
            category.setValid(false);
            return;
        }
        if (result.hasWarnings()) {
            category.setErrorMessage(result.getFirstWarningMessage());
            category.setValid(true);
            return;
        }
        category.setErrorMessage(null);
        category.setValid(true);
    }

    void saveData() {
        preferences.setEnabled(enabled);
        preferences.setNode(node);
        preferences.setDefaultNode(defaultNode);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        enabledCheckBox = new JCheckBox();
        nodeLabel = new JLabel();
        nodeTextField = new JTextField();
        nodeBrowseButton = new JButton();
        defaultNodeCheckBox = new JCheckBox();
        configureNodeButton = new JButton();

        Mnemonics.setLocalizedText(enabledCheckBox, NbBundle.getMessage(NodeJsCustomizerPanel.class, "NodeJsCustomizerPanel.enabledCheckBox.text")); // NOI18N

        nodeLabel.setLabelFor(nodeTextField);
        Mnemonics.setLocalizedText(nodeLabel, NbBundle.getMessage(NodeJsCustomizerPanel.class, "NodeJsCustomizerPanel.nodeLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(nodeBrowseButton, NbBundle.getMessage(NodeJsCustomizerPanel.class, "NodeJsCustomizerPanel.nodeBrowseButton.text")); // NOI18N
        nodeBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                nodeBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(defaultNodeCheckBox, NbBundle.getMessage(NodeJsCustomizerPanel.class, "NodeJsCustomizerPanel.defaultNodeCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(configureNodeButton, NbBundle.getMessage(NodeJsCustomizerPanel.class, "NodeJsCustomizerPanel.configureNodeButton.text")); // NOI18N
        configureNodeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configureNodeButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(nodeLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(defaultNodeCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(configureNodeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nodeTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nodeBrowseButton))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {configureNodeButton, nodeBrowseButton});

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nodeLabel)
                    .addComponent(nodeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(nodeBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultNodeCheckBox)
                    .addComponent(configureNodeButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("NodeJsCustomizerPanel.node.browse.title=Select node")
    private void nodeBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodeBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(NodeJsCustomizerPanel.class)
                .setFilesOnly(true)
                .setTitle(Bundle.NodeJsCustomizerPanel_node_browse_title())
                .showOpenDialog();
        if (file != null) {
            nodeTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_nodeBrowseButtonActionPerformed

    private void configureNodeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configureNodeButtonActionPerformed
        assert EventQueue.isDispatchThread();
        OptionsDisplayer.getDefault().open(NodeJsOptionsPanelController.OPTIONS_PATH);
    }//GEN-LAST:event_configureNodeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton configureNodeButton;
    private JCheckBox defaultNodeCheckBox;
    private JCheckBox enabledCheckBox;
    private JButton nodeBrowseButton;
    private JLabel nodeLabel;
    private JTextField nodeTextField;
    // End of variables declaration//GEN-END:variables
}
