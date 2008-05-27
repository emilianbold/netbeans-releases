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


package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

class CustomComponentVisualPanel extends JPanel implements DocumentListener {

    public static final String PROP_PROJECT_NAME = "projectName";
    public static final String BROWSE = "BROWSE";
    public static final String LBL_SELECT_LOCATION_DLG = "LBL_SelectProjectLocation";
    //messages
    public static final String MSG_NAME_CANNOT_BE_EMPTY = "MSG_NameCannotBeEmpty";
    public static final String MSG_LOCATION_CANNOT_BE_EMPTY = "MSG_LocationCannotBeEmpty";
    public static final String MSG_LOCATION_MUST_EXIST = "MSG_LocationMustExist";
    public static final String MSG_IS_NOT_DIRECTORY = "MSG_IsNotAFolder";
    public static final String MSG_CANT_CREATE_FOLDER = "MSG_CanNotCreateFolder";
    public static final String MSG_ILLEGAL_FOLDER_PATH = "MSG_IllegalFolderPath";
    public static final String MSG_FOLDER_EXISTS = "MSG_ProjectFolderExists";
    //default values
    public static final String TXT_DEFAULT_PROJECT_NAME = "TXT_DefaultProjectName";

    public CustomComponentVisualPanel(CustomComponentWizardPanel panel) {
        initComponents();
        this.myPanel = panel;
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    // Implementation of DocumentListener --------------------------------------
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
        checkValidity();
    }

    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }
    // -------------
    
    void store(WizardDescriptor d) {
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        Boolean setAsMain = mainProject.isSelected();

        d.putProperty(CustomComponentWizardIterator.PROJECT_DIR, new File(folder));
        d.putProperty(CustomComponentWizardIterator.PROJECT_NAME, name);
        d.putProperty(CustomComponentWizardIterator.SET_AS_MAIN, setAsMain);
    }

    void read(WizardDescriptor settings) {
        mySettings = settings;

        if (getIsMainProject() != null){
            this.mainProject.setSelected(getIsMainProject());
        }
        this.projectLocationTextField.setText(
                getProjectLocation().getAbsolutePath());
        
        this.projectNameTextField.setText(getProjectName());
        this.projectNameTextField.selectAll();
    }
    
    private boolean isProjectNameValid(){
        if (getProjectNameValue().trim().length() == 0) {
            setError(getMessage(MSG_NAME_CANNOT_BE_EMPTY));
            return false;
        }
        return true;
    }
    
    private boolean isProjectLocationValid(){
        String projectLocation = getProjectLocationValue().trim();
        File f = FileUtil.normalizeFile(
                new File(projectLocation).getAbsoluteFile());
        if (projectLocation.length() == 0){
            setError(getMessage(MSG_LOCATION_CANNOT_BE_EMPTY));
            return false;
        } else if (!f.exists()){
            setError(getMessage(MSG_LOCATION_MUST_EXIST));
            return false;
        }else if (!f.isDirectory()) {
            setError(getMessage(MSG_IS_NOT_DIRECTORY));
            return false;
        }
        return true;
    }

    private boolean isCreatedFolderValid(){
        String createdFolder = getCreatedFolderValue();
        final File destFolder = FileUtil.normalizeFile(
                new File(createdFolder).getAbsoluteFile());

        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            setError(getMessage(MSG_CANT_CREATE_FOLDER));
            return false;
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            setError(getMessage(MSG_ILLEGAL_FOLDER_PATH));
            return false;
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            setError(getMessage(MSG_FOLDER_EXISTS));
            return false;
        }
        return true;
    }

    // TODO
    boolean checkValidity() {
        if (!isProjectNameValid()){
            return false;
        } else if (!isProjectLocationValid()){
            return false;
        } else if (!isCreatedFolderValid()){
            return false;
        }
        
        
        markValid();
        return true;
    }

    private String getProjectNameValue(){
        return projectNameTextField.getText();
    }
    
    private String getProjectLocationValue(){
        return projectLocationTextField.getText();
    }

    private String getCreatedFolderValue() {
        return createdFolderTextField.getText();
    }

    private static String getMessage(String key, Object... args) {
        return NbBundle.getMessage(CustomComponentVisualPanel.class, key, args);
    }
    
    /**
     * Set an error message and mark the myPanel as invalid.
     */
    protected final void setError(String message) {
        assert message != null;
        setMessage(message);
        setValid(false);
    }

    private final void setValid(boolean valid) {
        myPanel.setValid(valid);
    }
    
    private final void setMessage(String message) {
        mySettings.putProperty(
                CustomComponentWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, 
                message);
    }

    /**
     * Mark the panel as valid and clear any error or warning message.
     */
    protected final void markValid() {
        setMessage(null);
        setValid(true);
    }

    /**
     * Returns project location value stored in WizardDescriptor, or
     * default value if it wasn't stored yet
     * @param settings WizardDescriptor
     * @return File Directory that will contain project folder
     */
    File getProjectLocation(){
        File projectLocation = (File) mySettings
                .getProperty(CustomComponentWizardIterator.PROJECT_DIR);
        // project directory
        if (projectLocation == null 
                || projectLocation.getParentFile() == null 
                || !projectLocation.getParentFile().isDirectory()) 
        {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        return projectLocation;
    }

    /**
     * Returns project name value stored in WizardDescriptor, or
     * default value if it wasn't stored yet
     * @param settings WizardDescriptor
     * @return String project name loaded from WizardDescriptor or default 
     * name wich is not used as directory name in project location directory yet.
     */
    String getProjectName(){
        String projectName = (String) mySettings
                .getProperty(CustomComponentWizardIterator.PROJECT_NAME);
        // project name
        if (projectName == null) {
            projectName = getDefaultFreeName(getProjectLocation());
        }
        return projectName;
    }
    
    Boolean getIsMainProject(){
        Boolean isMain = (Boolean) mySettings
                .getProperty(CustomComponentWizardIterator.SET_AS_MAIN);
        return isMain;
    }
    
    /* 
     * is invoked from myPanel.validate()
     * which implements WizardDescriptor.ValidatingPanel 
     */
    void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        mainProject = new javax.swing.JCheckBox();

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "LBL_ProjectName")); // NOI18N

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "LBL_ProjectLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "LBL_Browse_Button")); // NOI18N
        browseButton.setActionCommand(BROWSE);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "LBL_ProjectFolder")); // NOI18N

        createdFolderTextField.setEditable(false);

        mainProject.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mainProject, org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "LBL_SetAsMainProject")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectLocationLabel)
                    .add(createdFolderLabel)
                    .add(projectNameLabel))
                .add(19, 19, 19)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, projectLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, createdFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE))
                .add(18, 18, 18)
                .add(browseButton)
                .add(0, 0, 0))
            .add(layout.createSequentialGroup()
                .add(mainProject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectNameLabel)
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectLocationLabel)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(createdFolderLabel))
                .add(18, 18, 18)
                .add(mainProject)
                .addContainerGap(183, Short.MAX_VALUE))
        );

        projectNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_ProjectName")); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSD_ProjectName")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_ProjectLocation")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSD_ProjectLocation")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_Browse_Button")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_Browse_Button")); // NOI18N
        createdFolderLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_ProjectFolder")); // NOI18N
        createdFolderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSD_ProjectFolder")); // NOI18N
        mainProject.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_SetAsMainProject")); // NOI18N
        mainProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSD_SetAsMainProject")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String command = evt.getActionCommand();
        if (BROWSE.equals(command)) {
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogTitle(LBL_SELECT_LOCATION_DLG);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(
                        FileUtil.normalizeFile(projectDir).getAbsolutePath());
            }
            //myPanel.fireChangeEvent();
        }

    }//GEN-LAST:event_browseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JCheckBox mainProject;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables

    // TODO: use FileUtil.findFreeFolderName here
    private String getDefaultFreeName(File projectLocation) {
        int i = 1;
        String projectName;
        do {
            projectName = validFreeProjectName(projectLocation, i++);
        } while (projectName == null);
        return projectName;
    }

    private String validFreeProjectName(File parentFolder, int index) {
        String name = NbBundle.getMessage(CustomComponentVisualPanel.class,
                    TXT_DEFAULT_PROJECT_NAME, new Object[] {index}  );
        File file = new File(parentFolder, name);
        if (file.exists()) {
            return null;
        }
        return name;
    }

    /** Handles changes in the Project name and project directory, */
    private void updateTexts(DocumentEvent e) {

        Document doc = e.getDocument();

        if (doc == projectNameTextField.getDocument() 
                || doc == projectLocationTextField.getDocument()) 
        {
            // Change in the project name

            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();

            //if (projectFolder.trim().length() == 0 || projectFolder.equals(oldName)) {
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
        //}

        }
        //myPanel.fireChangeEvent(); // Notify that the myPanel changed

    }

    private WizardDescriptor mySettings;
    private CustomComponentWizardPanel myPanel;
}
