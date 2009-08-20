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

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.ui.LastUsedFolders;
import org.netbeans.modules.php.project.ui.ProjectNameProvider;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ProjectFolder extends JPanel implements ActionListener, DocumentListener, ChangeListener {
    private static final long serialVersionUID = 7976754658427748L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ProjectNameProvider projectNameProvider;
    private final SourcesFolderProvider sourcesFolderProvider;

    public ProjectFolder(ProjectNameProvider projectNameProvider, SourcesFolderProvider sourcesFolderProvider) {
        this.projectNameProvider = projectNameProvider;
        this.sourcesFolderProvider = sourcesFolderProvider;

        initComponents();

        init();
        setWarning(false);
    }

    private void init() {
        projectFolderCheckBox.addActionListener(this);
        projectFolderTextField.getDocument().addDocumentListener(this);
        sourcesFolderProvider.addChangeListener(this);
    }

    void addProjectFolderListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    void removeProjectFolderListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    boolean isProjectFolderUsed() {
        return projectFolderCheckBox.isSelected();
    }

    void setProjectFolderUsed(boolean used) {
        projectFolderCheckBox.setSelected(used);
        setState(used);
    }

    String getProjectFolder() {
        return projectFolderTextField.getText().trim();
    }

    void setProjectFolder(String projectFolder) {
        projectFolderTextField.setText(projectFolder);
    }

    void setState(boolean enabled) {
        projectFolderLabel.setEnabled(enabled);
        projectFolderTextField.setEnabled(enabled);
        projectFolderBrowseButton.setEnabled(enabled);
        // warning
        setWarning(enabled);
    }

    private void setWarning(boolean enabled) {
        projectFolderScrollPane.setVisible(enabled && isProjectDifferentFromSources());
    }

    // #169784
    private boolean isProjectDifferentFromSources() {
        File sources = FileUtil.normalizeFile(sourcesFolderProvider.getSourcesFolder());
        File project = FileUtil.normalizeFile(new File(getProjectFolder()));
        return !Utils.subdirectories(sources.getAbsolutePath(), project.getAbsolutePath());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {


        projectFolderCheckBox = new JCheckBox();
        projectFolderLabel = new JLabel();
        projectFolderTextField = new JTextField();
        projectFolderBrowseButton = new JButton();
        projectFolderScrollPane = new JScrollPane();
        projectFolderTextArea = new JTextArea();

        Mnemonics.setLocalizedText(projectFolderCheckBox, NbBundle.getMessage(ProjectFolder.class, "LBL_SeparateProjectFolder")); // NOI18N
        projectFolderLabel.setLabelFor(projectFolderTextField);
        Mnemonics.setLocalizedText(projectFolderLabel, NbBundle.getMessage(ProjectFolder.class, "LBL_MetadataFolder")); // NOI18N
        projectFolderLabel.setEnabled(false);

        projectFolderTextField.setEnabled(false);

        Mnemonics.setLocalizedText(projectFolderBrowseButton, NbBundle.getMessage(ProjectFolder.class, "LBL_BrowseProject")); // NOI18N
        projectFolderBrowseButton.setEnabled(false);
        projectFolderBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                projectFolderBrowseButtonActionPerformed(evt);
            }
        });

        projectFolderScrollPane.setBorder(null);

        projectFolderTextArea.setBackground(UIManager.getDefaults().getColor("Label.background"));
        projectFolderTextArea.setEditable(false);
        projectFolderTextArea.setFont(projectFolderTextArea.getFont().deriveFont(projectFolderTextArea.getFont().getStyle() | Font.BOLD));
        projectFolderTextArea.setLineWrap(true);
        projectFolderTextArea.setText(NbBundle.getMessage(ProjectFolder.class, "TXT_MetadataInfo")); // NOI18N
        projectFolderTextArea.setToolTipText(NbBundle.getMessage(ProjectFolder.class, "TXT_MetadataInfo")); // NOI18N
        projectFolderTextArea.setWrapStyleWord(true);
        projectFolderTextArea.setBorder(null);
        projectFolderTextArea.setDisabledTextColor(UIManager.getDefaults().getColor("nb.warningForeground"));
        projectFolderTextArea.setEnabled(false);
        projectFolderScrollPane.setViewportView(projectFolderTextArea);

        projectFolderTextArea.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderTextArea.AccessibleContext.accessibleName")); // NOI18N
        projectFolderTextArea.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderTextArea.AccessibleContext.accessibleDescription")); // NOI18N
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, projectFolderScrollPane)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(projectFolderCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(projectFolderLabel)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(projectFolderTextField, GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(projectFolderBrowseButton)))
                .add(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(projectFolderCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(projectFolderTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(projectFolderLabel)
                    .add(projectFolderBrowseButton))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(projectFolderScrollPane, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE))
        );

        projectFolderCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderCheckBox.AccessibleContext.accessibleName")); // NOI18N
        projectFolderCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        projectFolderLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderLabel.AccessibleContext.accessibleName")); // NOI18N
        projectFolderLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectFolderTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        projectFolderTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N
        projectFolderBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        projectFolderBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        projectFolderScrollPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderScrollPane.AccessibleContext.accessibleName")); // NOI18N
        projectFolderScrollPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.projectFolderScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ProjectFolder.class, "ProjectFolder.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void projectFolderBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_projectFolderBrowseButtonActionPerformed
        File newLocation = Utils.browseLocationAction(this, LastUsedFolders.getProject(), NbBundle.getMessage(ProjectFolder.class, "LBL_SelectProjectFolder"));
        if (newLocation != null) {
            setProjectFolder(new File(newLocation, projectNameProvider.getProjectName()).getAbsolutePath());
            LastUsedFolders.setProject(newLocation);
        }
    }//GEN-LAST:event_projectFolderBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton projectFolderBrowseButton;
    private JCheckBox projectFolderCheckBox;
    private JLabel projectFolderLabel;
    private JScrollPane projectFolderScrollPane;
    private JTextArea projectFolderTextArea;
    private JTextField projectFolderTextField;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed(ActionEvent e) {
        setState(projectFolderCheckBox.isSelected());
        changeSupport.fireChange();
    }

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
        changeSupport.fireChange();
    }

    public void stateChanged(ChangeEvent e) {
        setState(projectFolderCheckBox.isSelected());
    }
}
