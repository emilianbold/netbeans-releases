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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.apisupport.project.ui.wizard.spi.ModuleTypePanel;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

class CustomComponentVisualPanel extends JPanel {

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

    public static final String ACSN_PROJECT_PANEL = "ACSN_ProjectPanel";    // NOI18N
    public static final String ACSD_PROJECT_PANEL = "ACSD_ProjectPanel";    // NOI18N

    public CustomComponentVisualPanel(CustomComponentWizardPanel panel) {
        initComponents();
        this.myPanel = panel;
        
        putClientProperty("NewProjectWizard_Title", NbBundle.getMessage(CustomComponentVisualPanel.class, "TXT_MobileDesigner"));
        initDocumentListeners();
        attachDocumentListeners();

        initAccessibility();
    }

    @Override 
    public void addNotify() {
        super.addNotify();
        attachDocumentListeners();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    @Override 
    public void removeNotify() {
        // prevent checking when the panel is not "active"
        removeDocumentListeners();
        super.removeNotify();
    }

    void store(WizardDescriptor d) {
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        Boolean setAsMain = mainProject.isSelected();

        d.putProperty(CustomComponentWizardIterator.PROJECT_DIR, new File(folder));
        d.putProperty(CustomComponentWizardIterator.PROJECT_NAME, name);
        d.putProperty(CustomComponentWizardIterator.SET_AS_MAIN, setAsMain);
    }

    void read(WizardDescriptor settings) {
        if (mySettings == null){
            mySettings = settings;
            mySettings.addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (ModuleTypePanel.isPanelUpdated(evt)){
                        moduleTypePanelUpdated();
                    }
                }
            });
            initPanels(mySettings);
        }

        //typeChooserPanel.read(mySettings);
        // invoke store to have changes in mySettings
        //typeChooserPanel.store(mySettings);

        if (getIsMainProject() != null){
            this.mainProject.setSelected(getIsMainProject());
        }
        setLocation(getProjectLocation().getAbsolutePath());

        this.projectNameTextField.setText(getProjectName());
        this.projectNameTextField.selectAll();
    }

    protected HelpCtx getHelp() {
        return new HelpCtx(CustomComponentVisualPanel.class);
    }
    
    private void initDocumentListeners() {
        nameDL = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateTexts(e);
                checkValidity();
            }
        };
        locationDL = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                locationUpdated = true;
                updateTexts(e);
                checkValidity();
            }
        };
        isMainAL = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainProjectTouched = true;
            }
        };
    }
    
    private void attachDocumentListeners() {
        if (!listenersAttached) {
            projectNameTextField.getDocument().addDocumentListener(nameDL);
            projectLocationTextField.getDocument().addDocumentListener(locationDL);
            mainProject.addActionListener(isMainAL);
            listenersAttached = true;
        }
    }
    
    private void removeDocumentListeners() {
        if (listenersAttached) {
            projectNameTextField.getDocument().removeDocumentListener(nameDL);
            projectLocationTextField.getDocument().removeDocumentListener(locationDL);
            mainProject.removeActionListener(isMainAL);
            listenersAttached = false;
        }
    }
    
    private void initPanels(WizardDescriptor settings){
        if (typeChooserPanel != null){
            typeChooserPanelContainer.removeAll();
            typeChooserPanel = null;
        }
        typeChooserPanel = ModuleTypePanel.createComponent(settings);
        typeChooserPanelContainer.add(typeChooserPanel, BorderLayout.CENTER);
        typeChooserPanelContainer.validate();
        validate();
    }
    
    private void moduleTypePanelUpdated() {
        boolean isStandAlone = ModuleTypePanel.isStandalone(getSettings());
        boolean isSuiteComponent = ModuleTypePanel.isSuiteComponent(getSettings());
        // both radio buttons are deselected and disaled
        if (!isStandAlone && !isSuiteComponent){
            return;
        }
        
        if (!mainProjectTouched) {
            mainProject.setSelected(isStandAlone);
        }
        if (!locationUpdated) {
            setLocation(computeInitialLocationValue());
        }
        checkValidity();
    }

    private void setLocation(String location) {
        boolean revert = !locationUpdated;
        projectLocationTextField.setText(location);
        locationUpdated = revert ^ true;
    }
    
    boolean checkValidity() {
        if (!isProjectNameValid()){
            return false;
        } else if (!isProjectLocationValid()){
            return false;
        } else if (!isCreatedFolderValid()){
            return false;
        } else if (!ModuleTypePanel.validate(getSettings())){
            return false;
        }

        markValid();
        return true;
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
        getSettings().putProperty(
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
        File projectLocation = (File) getSettings()
                .getProperty(CustomComponentWizardIterator.PROJECT_DIR);
        // project directory
        if (projectLocation == null
                || projectLocation.getParentFile() == null
                || !projectLocation.getParentFile().isDirectory())
        {
            projectLocation = new File(computeInitialLocationValue());
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        return projectLocation;
    }

    private String computeInitialLocationValue(){
        if (typeChooserPanel != null && isSuiteComponent()) {
            return computeLocationValue(getSelectedSuite());
        } else {
            String path = ProjectChooser.getProjectsFolder().getAbsolutePath();
            return computeLocationValue(path);
        }
    }
    
    private String computeLocationValue(String value) {
        if (value == null) {
            value = System.getProperty("user.home"); // NOI18N
        }
        File file = new File(value);
        if (!file.exists() && file.getParent() != null) {
            return computeLocationValue(file.getParent());
        } else {
            return file.exists() ? value : System.getProperty("user.home"); // NOI18N
        }
    }
    
    private Boolean isSuiteComponent(){
        if (getSettings() != null){
            return ModuleTypePanel.isSuiteComponent(getSettings());
        }
        return false;
    }
    
    private String getSelectedSuite(){
        return ModuleTypePanel.getSuiteRoot(getSettings());
    }
    
    /**
     * Returns project name value stored in WizardDescriptor, or
     * default value if it wasn't stored yet
     * @param settings WizardDescriptor
     * @return String project name loaded from WizardDescriptor or default
     * name wich is not used as directory name in project location directory yet.
     */
    String getProjectName(){
        String projectName = (String) getSettings()
                .getProperty(CustomComponentWizardIterator.PROJECT_NAME);
        // project name
        if (projectName == null) {
            projectName = getDefaultFreeName(getProjectLocation());
        }
        return projectName;
    }

    Boolean getIsMainProject(){
        Boolean isMain = (Boolean) getSettings()
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
        java.awt.GridBagConstraints gridBagConstraints;

        mainProject = new javax.swing.JCheckBox();
        infoPanel = new javax.swing.JPanel();
        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        createdFolderTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        createdFolderLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        typeChooserPanelContainer = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        mainProject.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mainProject, org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "LBL_SetAsMainProject")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(mainProject, gridBagConstraints);
        mainProject.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_SetAsMainProject")); // NOI18N
        mainProject.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSD_SetAsMainProject")); // NOI18N

        infoPanel.setLayout(new java.awt.GridBagLayout());

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "LBL_ProjectName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        infoPanel.add(projectNameLabel, gridBagConstraints);
        projectNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_ProjectName")); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSD_ProjectName")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        infoPanel.add(projectNameTextField, gridBagConstraints);

        createdFolderTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        infoPanel.add(createdFolderTextField, gridBagConstraints);

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "LBL_ProjectLocation")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 12);
        infoPanel.add(projectLocationLabel, gridBagConstraints);
        projectLocationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_ProjectLocation")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSD_ProjectLocation")); // NOI18N

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "LBL_ProjectFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        infoPanel.add(createdFolderLabel, gridBagConstraints);
        createdFolderLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_ProjectFolder")); // NOI18N
        createdFolderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSD_ProjectFolder")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        infoPanel.add(projectLocationTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "LBL_Browse_Button")); // NOI18N
        browseButton.setActionCommand(BROWSE);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        infoPanel.add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_Browse_Button")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomComponentVisualPanel.class, "ACSN_Browse_Button")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(infoPanel, gridBagConstraints);

        typeChooserPanelContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(typeChooserPanelContainer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fillerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String command = evt.getActionCommand();
        if (BROWSE.equals(command)) {//GEN-HEADEREND:event_browseButtonActionPerformed
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
                File projectDir = chooser.getSelectedFile();//GEN-LAST:event_browseButtonActionPerformed
                String projectDirPath = FileUtil.normalizeFile(projectDir).getAbsolutePath();
                projectLocationTextField.setText(computeLocationValue(projectDirPath));
            }
            //myPanel.fireChangeEvent();
        }

    }

    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(
                CustomComponentVisualPanel.class, ACSN_PROJECT_PANEL));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                CustomComponentVisualPanel.class, ACSD_PROJECT_PANEL));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JCheckBox mainProject;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JPanel typeChooserPanelContainer;
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

            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
            projectFolderChanged(getCreatedFolderValue());

        }
    }
    
    private void projectFolderChanged(String projectFolder){
        File folder = FileUtil.normalizeFile(new File(projectFolder));
        ModuleTypePanel.setProjectFolder(getSettings(), folder);
    }

    private WizardDescriptor getSettings(){
        return mySettings;
    }

    private WizardDescriptor mySettings;
    private CustomComponentWizardPanel myPanel;
    private JComponent typeChooserPanel;
    private boolean locationUpdated;
    private boolean mainProjectTouched;

    private boolean listenersAttached;
    private DocumentListener nameDL;
    private DocumentListener locationDL;
    private ActionListener isMainAL;
}
