/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.options;

import java.io.File;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.classpath.GlobalIncludePathSupport;
import org.netbeans.modules.php.project.ui.IncludePathUiSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author  Tomas Mysik
 */
public class PhpOptionsPanel extends JPanel {
    private static final long serialVersionUID = 1092136352492432078L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public PhpOptionsPanel() {
        initComponents();

        initPhpGlobalIncludePath();

        // listeners
        DocumentListener documentListener = new DefaultDocumentListener();
        phpInterpreterTextField.getDocument().addDocumentListener(documentListener);
        debuggerPortTextField.getDocument().addDocumentListener(documentListener);
    }

    private void initPhpGlobalIncludePath() {
        DefaultListModel listModel = IncludePathUiSupport.createListModel(
                GlobalIncludePathSupport.getInstance().itemsIterator());
        includePathList.setModel(listModel);
        includePathList.setCellRenderer(new IncludePathUiSupport.ClassPathListCellRenderer());
        IncludePathUiSupport.EditMediator.register(includePathList,
                                               addFolderButton.getModel(),
                                               removeButton.getModel(),
                                               moveUpButton.getModel(),
                                               moveDownButton.getModel());
    }

    public String getPhpInterpreter() {
        return phpInterpreterTextField.getText();
    }

    public void setPhpInterpreter(String phpInterpreter) {
        phpInterpreterTextField.setText(phpInterpreter);
    }

    public boolean isOpenResultInOutputWindow() {
        return outputWindowCheckBox.isSelected();
    }

    public void setOpenResultInOutputWindow(boolean openResultInOutputWindow) {
        outputWindowCheckBox.setSelected(openResultInOutputWindow);
    }

    public boolean isOpenResultInBrowser() {
        return webBrowserCheckBox.isSelected();
    }

    public void setOpenResultInBrowser(boolean openResultInBrowser) {
        webBrowserCheckBox.setSelected(openResultInBrowser);
    }

    public boolean isOpenResultInEditor() {
        return editorCheckBox.isSelected();
    }

    public void setOpenResultInEditor(boolean openResultInEditor) {
        editorCheckBox.setSelected(openResultInEditor);
    }

    public Integer getDebuggerPort() {
        Integer port = null;
        try {
            port = Integer.parseInt(debuggerPortTextField.getText());
        } catch (NumberFormatException exc) {
            // ignored
        }
        return port;
    }

    public void setDebuggerPort(int debuggerPort) {
        debuggerPortTextField.setText(String.valueOf(debuggerPort));
    }

    public boolean isDebuggerStoppedAtTheFirstLine() {
        return stopAtTheFirstLineCheckBox.isSelected();
    }

    public void setDebuggerStoppedAtTheFirstLine(boolean debuggerStoppedAtTheFirstLine) {
        stopAtTheFirstLineCheckBox.setSelected(debuggerStoppedAtTheFirstLine);
    }

    public String getPhpGlobalIncludePath() {
        String[] paths = GlobalIncludePathSupport.getInstance().encodeToStrings(
                IncludePathUiSupport.getIterator((DefaultListModel) includePathList.getModel()));
        StringBuilder path = new StringBuilder();
        for (String s : paths) {
            path.append(s);
        }
        return path.toString();
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        commandLineLabel = new javax.swing.JLabel();
        phpInterpreterLabel = new javax.swing.JLabel();
        phpInterpreterTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        openResultInLabel = new javax.swing.JLabel();
        outputWindowCheckBox = new javax.swing.JCheckBox();
        webBrowserCheckBox = new javax.swing.JCheckBox();
        editorCheckBox = new javax.swing.JCheckBox();
        debuggingLabel = new javax.swing.JLabel();
        debuggerPortLabel = new javax.swing.JLabel();
        debuggerPortTextField = new javax.swing.JTextField();
        stopAtTheFirstLineCheckBox = new javax.swing.JCheckBox();
        globalIncludePathLabel = new javax.swing.JLabel();
        includePathScrollPane = new javax.swing.JScrollPane();
        includePathList = new javax.swing.JList();
        addFolderButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        useTheFollowingPathByDefaultLabel = new javax.swing.JLabel();
        commandLineSeparator = new javax.swing.JSeparator();
        debuggingSeparator = new javax.swing.JSeparator();
        globalIncludePathSeparator = new javax.swing.JSeparator();
        errorLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(commandLineLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_CommandLine")); // NOI18N

        phpInterpreterLabel.setLabelFor(phpInterpreterTextField);
        org.openide.awt.Mnemonics.setLocalizedText(phpInterpreterLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpInterpreter")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Browse")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Search")); // NOI18N
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        openResultInLabel.setLabelFor(outputWindowCheckBox);
        org.openide.awt.Mnemonics.setLocalizedText(openResultInLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_OpenResultIn")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(outputWindowCheckBox, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_OutputWindow")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(webBrowserCheckBox, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_WebBrowser")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(editorCheckBox, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Editor")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(debuggingLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Debugging")); // NOI18N

        debuggerPortLabel.setLabelFor(debuggerPortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(debuggerPortLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_DebuggerPort")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(stopAtTheFirstLineCheckBox, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_StopAtTheFirstLine")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(globalIncludePathLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_GlobalIncludePath")); // NOI18N

        includePathScrollPane.setViewportView(includePathList);

        org.openide.awt.Mnemonics.setLocalizedText(addFolderButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_AddFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Remove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_MoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_MoveDown")); // NOI18N

        useTheFollowingPathByDefaultLabel.setLabelFor(includePathList);
        org.openide.awt.Mnemonics.setLocalizedText(useTheFollowingPathByDefaultLabel, org.openide.util.NbBundle.getMessage(PhpOptionsPanel.class, "LBL_UseTheFollowingPathByDefault")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, "ERROR");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(phpInterpreterLabel)
                    .add(openResultInLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(phpInterpreterTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(searchButton))
                    .add(layout.createSequentialGroup()
                        .add(outputWindowCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(webBrowserCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editorCheckBox)
                        .addContainerGap())))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(commandLineLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(commandLineSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                .add(0, 0, 0))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(useTheFollowingPathByDefaultLabel)
                .addContainerGap(239, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(includePathScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(addFolderButton)
                    .add(removeButton)
                    .add(moveUpButton)
                    .add(moveDownButton)))
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(errorLabel)
                        .addContainerGap())
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .add(debuggingLabel)
                            .add(12, 12, 12)
                            .add(debuggingSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE))
                        .add(layout.createSequentialGroup()
                            .add(12, 12, 12)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(stopAtTheFirstLineCheckBox)
                                .add(layout.createSequentialGroup()
                                    .add(debuggerPortLabel)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(debuggerPortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap())
                        .add(layout.createSequentialGroup()
                            .add(globalIncludePathLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(globalIncludePathSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)))))
        );

        layout.linkSize(new java.awt.Component[] {addFolderButton, moveDownButton, moveUpButton, removeButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {browseButton, searchButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(commandLineLabel)
                    .add(commandLineSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phpInterpreterLabel)
                    .add(searchButton)
                    .add(browseButton)
                    .add(phpInterpreterTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(openResultInLabel)
                    .add(outputWindowCheckBox)
                    .add(webBrowserCheckBox)
                    .add(editorCheckBox))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(debuggingSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(debuggingLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(debuggerPortLabel)
                    .add(debuggerPortTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(stopAtTheFirstLineCheckBox)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(globalIncludePathLabel)
                    .add(globalIncludePathSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(useTheFollowingPathByDefaultLabel)
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(includePathScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(errorLabel))
                    .add(layout.createSequentialGroup()
                        .add(addFolderButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(moveUpButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(moveDownButton)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(PhpOptionsPanel.class, "LBL_SelectPhpInterpreter"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File phpInterpreter = FileUtil.normalizeFile(chooser.getSelectedFile());
            phpInterpreterTextField.setText(phpInterpreter.getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        SelectPhpInterpreterPanel panel = new SelectPhpInterpreterPanel();
        if (panel.open()) {
            if (panel.getSelectedPhpInterpreter() != null) {
                phpInterpreterTextField.setText(panel.getSelectedPhpInterpreter());
            }
        }
    }//GEN-LAST:event_searchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFolderButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel commandLineLabel;
    private javax.swing.JSeparator commandLineSeparator;
    private javax.swing.JLabel debuggerPortLabel;
    private javax.swing.JTextField debuggerPortTextField;
    private javax.swing.JLabel debuggingLabel;
    private javax.swing.JSeparator debuggingSeparator;
    private javax.swing.JCheckBox editorCheckBox;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel globalIncludePathLabel;
    private javax.swing.JSeparator globalIncludePathSeparator;
    private javax.swing.JList includePathList;
    private javax.swing.JScrollPane includePathScrollPane;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JLabel openResultInLabel;
    private javax.swing.JCheckBox outputWindowCheckBox;
    private javax.swing.JLabel phpInterpreterLabel;
    private javax.swing.JTextField phpInterpreterTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JCheckBox stopAtTheFirstLineCheckBox;
    private javax.swing.JLabel useTheFollowingPathByDefaultLabel;
    private javax.swing.JCheckBox webBrowserCheckBox;
    // End of variables declaration//GEN-END:variables

    private final class DefaultDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChange();
        }
    }
}
