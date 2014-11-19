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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.netbeans.modules.javascript.nodejs.ui.NodeJsPathPanel;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public final class NodeJsOptionsPanel extends JPanel implements ChangeListener {

    final NodeJsPathPanel nodePanel;
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private NodeJsOptionsPanel() {
        assert EventQueue.isDispatchThread();
        initComponents();

        nodePanel = new NodeJsPathPanel();

        init();
    }

    public static NodeJsOptionsPanel create() {
        NodeJsOptionsPanel panel = new NodeJsOptionsPanel();
        panel.nodePanel.addChangeListener(panel);
        return panel;
    }

    @NbBundle.Messages({
        "# {0} - npm file name",
        "NodeJsOptionsPanel.npm.hint=Full path of npm file (typically {0}).",
    })
    private void init() {
        errorLabel.setText(" "); // NOI18N
        npmHintLabel.setText(Bundle.NodeJsOptionsPanel_npm_hint(NpmExecutable.NPM_NAME));
        nodePanelHolder.add(nodePanel, BorderLayout.CENTER);
        npmTextField.getDocument().addDocumentListener(new DefaultDocumentListener());
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
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

    public String getNode() {
        return nodePanel.getNode();
    }

    public void setNode(String node) {
        nodePanel.setNode(node);
    }

    public String getNpm() {
        return npmTextField.getText();
    }

    public void setNpm(String npm) {
        npmTextField.setText(npm);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nodePanelHolder = new JPanel();
        npmLabel = new JLabel();
        npmTextField = new JTextField();
        npmBrowseButton = new JButton();
        npmSearchButton = new JButton();
        npmHintLabel = new JLabel();
        errorLabel = new JLabel();

        nodePanelHolder.setLayout(new BorderLayout());

        Mnemonics.setLocalizedText(npmLabel, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.npmLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(npmBrowseButton, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.npmBrowseButton.text")); // NOI18N
        npmBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                npmBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(npmSearchButton, NbBundle.getMessage(NodeJsOptionsPanel.class, "NodeJsOptionsPanel.npmSearchButton.text")); // NOI18N
        npmSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                npmSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(npmHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(nodePanelHolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(npmLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(npmHintLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(npmTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(npmBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(npmSearchButton))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(errorLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {npmBrowseButton, npmSearchButton});

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(nodePanelHolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(npmLabel)
                    .addComponent(npmTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(npmSearchButton)
                    .addComponent(npmBrowseButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(npmHintLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("NodeJsOptionsPanel.npm.browse.title=Select npm")
    private void npmBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_npmBrowseButtonActionPerformed
        assert EventQueue.isDispatchThread();
        File file = new FileChooserBuilder(NodeJsOptionsPanel.class)
                .setFilesOnly(true)
                .setTitle(Bundle.NodeJsOptionsPanel_npm_browse_title())
                .showOpenDialog();
        if (file != null) {
            npmTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_npmBrowseButtonActionPerformed

    @NbBundle.Messages("NodeJsOptionsPanel.npm.none=No npm executable was found.")
    private void npmSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_npmSearchButtonActionPerformed
        assert EventQueue.isDispatchThread();
        for (String node : FileUtils.findFileOnUsersPath(NpmExecutable.NPM_NAME)) {
            npmTextField.setText(new File(node).getAbsolutePath());
            return;
        }
        // no node found
        StatusDisplayer.getDefault().setStatusText(Bundle.NodeJsOptionsPanel_npm_none());
    }//GEN-LAST:event_npmSearchButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel errorLabel;
    private JPanel nodePanelHolder;
    private JButton npmBrowseButton;
    private JLabel npmHintLabel;
    private JLabel npmLabel;
    private JButton npmSearchButton;
    private JTextField npmTextField;
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
