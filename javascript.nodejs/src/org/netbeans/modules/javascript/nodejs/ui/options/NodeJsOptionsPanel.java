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
package org.netbeans.modules.javascript.nodejs.ui.options;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public final class NodeJsOptionsPanel extends JPanel {

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public NodeJsOptionsPanel() {
        initComponents();
        init();
    }

    @NbBundle.Messages({
        "# {0} - node.js file name",
        "NodeJsOptionsPanel.node.hint=Full path of node file (typically {0}).",
    })
    private void init() {
        errorLabel.setText(" "); // NOI18N
        nodeHintLabel.setText(Bundle.NodeJsOptionsPanel_node_hint(NodeExecutable.NODE_NAME));

        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        nodeTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    public String getNode() {
        return nodeTextField.getText();
    }

    public void setNode(String node) {
        nodeTextField.setText(node);
    }

    public boolean isUseNodePath() {
        return nodePathCheckBox.isSelected();
    }

    public void setUseNodePath(boolean useNodePath) {
        nodePathCheckBox.setSelected(useNodePath);
    }

    public boolean isUseNpmGlobalRoot() {
        return npmGlobalRootCheckBox.isSelected();
    }

    public void setUseNpmGlobalRoot(boolean useNpmGlobalRoot) {
        npmGlobalRootCheckBox.setSelected(useNpmGlobalRoot);
    }

    public void setError(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void setWarning(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nodeLabel = new JLabel();
        nodeTextField = new JTextField();
        nodeBrowseButton = new JButton();
        nodeSearchButton = new JButton();
        nodeHintLabel = new JLabel();
        foldersLabel = new JLabel();
        nodePathCheckBox = new JCheckBox();
        npmGlobalRootCheckBox = new JCheckBox();
        errorLabel = new JLabel();

        Mnemonics.setLocalizedText(nodeLabel, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.nodeLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(nodeBrowseButton, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.nodeBrowseButton.text")); // NOI18N
        nodeBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                nodeBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(nodeSearchButton, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.nodeSearchButton.text")); // NOI18N
        nodeSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                nodeSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(nodeHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(foldersLabel, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.foldersLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(nodePathCheckBox, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.nodePathCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(npmGlobalRootCheckBox, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.npmGlobalRootCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(nodeLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nodeHintLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nodeTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nodeBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nodeSearchButton))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(errorLabel)
                    .addComponent(nodePathCheckBox)
                    .addComponent(npmGlobalRootCheckBox)
                    .addComponent(foldersLabel))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nodeLabel)
                    .addComponent(nodeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(nodeBrowseButton)
                    .addComponent(nodeSearchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodeHintLabel)
                .addGap(18, 18, 18)
                .addComponent(foldersLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodePathCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(npmGlobalRootCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("NodeJsOptionsPanel.node.browse.title=Select node")
    private void nodeBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_nodeBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(NodeJsOptionsPanel.class)
                .setFilesOnly(true)
                .setTitle(Bundle.NodeJsOptionsPanel_node_browse_title())
                .showOpenDialog();
        if (file != null) {
            nodeTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_nodeBrowseButtonActionPerformed

    @NbBundle.Messages("NodeJsOptionsPanel.node.none=No node executable was found.")
    private void nodeSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_nodeSearchButtonActionPerformed
        assert EventQueue.isDispatchThread();
        for (String node : FileUtils.findFileOnUsersPath(NodeExecutable.NODE_NAME)) {
            nodeTextField.setText(new File(node).getAbsolutePath());
            return;
        }
        // no node found
        StatusDisplayer.getDefault().setStatusText(Bundle.NodeJsOptionsPanel_node_none());
    }//GEN-LAST:event_nodeSearchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel errorLabel;
    private JLabel foldersLabel;
    private JButton nodeBrowseButton;
    private JLabel nodeHintLabel;
    private JLabel nodeLabel;
    private JCheckBox nodePathCheckBox;
    private JButton nodeSearchButton;
    private JTextField nodeTextField;
    private JCheckBox npmGlobalRootCheckBox;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChange();
        }

    }

}
