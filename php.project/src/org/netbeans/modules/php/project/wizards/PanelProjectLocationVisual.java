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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.php.project.wizards;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

class PanelProjectLocationVisual extends JPanel {

    private static final long serialVersionUID = 946804714147466826L;
    
    public static final String PROP_PROJECT_DIR 
                                        = "projectDir";                 // NOI18N
    
    private static final String BROWSE  = "BROWSE";                      // NOI18N
    
    private static final String SELECT_PROJECT_LOCATION 
                                         = "LBL_SelectProjectLocation";  // NOI18N
    
    
    private static final String MSG_ILLEGAL_PROJECT_NAME 
                                         = "MSG_IllegalProjectName";     // NOI18N
    
    private static final String MSG_ILLEGAL_PROJECT_LOCATION 
                                         = "MSG_IllegalProjectLocation"; // NOI18N
    
    private static final String MSG_PROJECT_FOLDER_READ_ONLY 
                                         = "MSG_ProjectFolderReadOnly";  // NOI18N
    
    private static final String MSG_PROJECT_FOLDER_EXISTS 
                                         = "MSG_ProjectFolderExists";    // NOI18N

    //private static final String WEB_SERVER_MODE 
    //                                    = "TXT_WebServerMode";           // NOI18N

    //private static final String COMMAND_LINE_MODE 
    //                                    = "TXT_CommandLineMode";         // NOI18N

    PanelProjectLocationVisual( PhpProjectConfigurePanel panel ) {
        initComponents();

        init(panel);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }
    

    boolean dataIsValid(WizardDescriptor wizardDescriptor) {
        return validate(wizardDescriptor);
    }
    
    void store(WizardDescriptor descriptor) {
        String name = projectNameTextField.getText().trim();

        descriptor.putProperty(NewPhpProjectWizardIterator.PROJECT_DIR, 
                new File(createdFolderTextField.getText().trim()));
        
        descriptor.putProperty(NewPhpProjectWizardIterator.NAME, name);
        
        File projectsDir = new File(projectLocationTextField.getText());
        if (projectsDir.isDirectory()) {
            ProjectChooser.setProjectsFolder(projectsDir);
        }

    }

    void read(WizardDescriptor settings) {
        initProjectLocation(settings);
    }
    
    private void init( PhpProjectConfigurePanel panel ) {
        myPanel = panel;
        // Register listener on the textFields to make the automatic updates
        myListener = new Listener();
        projectNameTextField.getDocument().addDocumentListener( myListener );
        projectLocationTextField.getDocument().addDocumentListener( myListener );

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
        myBrowseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();

        projectNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_ProjectName_Label")); // NOI18N
        projectNameLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_ProjectLocation_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myBrowseButton, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_BrowseLocation_Button")); // NOI18N
        myBrowseButton.setActionCommand(BROWSE);
        myBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocationAction(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_CreatedProjectFolder_Lablel")); // NOI18N

        createdFolderTextField.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectLocationLabel)
                    .add(projectNameLabel)
                    .add(createdFolderLabel))
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, createdFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(myBrowseButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectNameLabel)
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectLocationLabel)
                    .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myBrowseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(createdFolderLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/php/project/wizards/Bundle"); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleName(bundle.getString("A11_Project_Name")); // NOI18N
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_ProjectName_A11YDesc")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleName(bundle.getString("A11_Project_Location")); // NOI18N
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_ProjectLocation_A11YDesc")); // NOI18N
        myBrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_BrowseLocation_A11YDesc")); // NOI18N
        createdFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACS_LBL_CreatedProjectFolder_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    private void browseLocationAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocationAction
        String command = evt.getActionCommand();

        if ( command.equals(BROWSE)) 
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle( NbBundle.getMessage( 
                    NewPhpProjectWizardIterator.class,SELECT_PROJECT_LOCATION));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(projectDir.getAbsolutePath());
            }
            //getPanel().fireChangeEvent();
        }
    }//GEN-LAST:event_browseLocationAction

    private void initProjectLocation( WizardDescriptor settings ) {
        File projectLocation = getPanel().getProjectLocation(settings);
        projectLocationTextField.setText(projectLocation.getAbsolutePath());

        String projectName = getPanel().getProjectName(settings);
        projectNameTextField.setText(projectName);
        projectNameTextField.selectAll();
    }

    private PhpProjectConfigurePanel getPanel() {
        return myPanel;
    }

    /** Handles changes in the project name and project directory
     */
    private void updateTexts(DocumentEvent event) {
        if ( projectNameTextField.getDocument() == event.getDocument() ||
                projectLocationTextField.getDocument() == event.getDocument() )
        {
            String oldPath = createdFolderTextField.getText();
            String newPath = getCreatedFolderPath();
            createdFolderTextField.setText(newPath);
            
            firePropertyChange(PROP_PROJECT_DIR, oldPath, newPath);
        }
        getPanel().fireChangeEvent(); // Notify that the panel changed
    }

    private String getCreatedFolderPath() {
        String location = projectLocationTextField.getText().trim();
        String name = projectNameTextField.getText().trim();
        
        File locationFile = (new File(location)).getAbsoluteFile();
        File resultDir = new File(locationFile, name);
        return resultDir.getAbsolutePath();
    }
    

    private boolean validate( WizardDescriptor wizardDescriptor ) {
        
        boolean isLocationCorrect = validateProjectLocation(wizardDescriptor);
        if( !isLocationCorrect ) {
            return isLocationCorrect;
        }
        
        return true;
    }
    
    private boolean validateProjectLocation( WizardDescriptor wizardDescriptor ) {
        String projectName = projectNameTextField.getText();
        if (projectName.length() == 0  ||
                projectName.indexOf('/')  >= 0 || 
                projectName.indexOf('\\') >= 0 ) 
        {
            String message = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, MSG_ILLEGAL_PROJECT_NAME);
            wizardDescriptor.putProperty(
                    NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, message);
            return false; // Display name not specified
        }

        File f = new File( projectName ).getAbsoluteFile();
        if (PhpProjectConfigurePanel.getCanonicalFile(f)==null) {
            String message = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, MSG_ILLEGAL_PROJECT_LOCATION);
            wizardDescriptor.putProperty(
                    NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, message);
            return false;
        }
        
        File destFolder = new File(createdFolderTextField.getText() ).
                getAbsoluteFile();
        if (PhpProjectConfigurePanel.getCanonicalFile(destFolder) == null) {
            String message = NbBundle.getMessage(NewPhpProjectWizardIterator.class, 
                    MSG_ILLEGAL_PROJECT_NAME);
            wizardDescriptor.putProperty(
                    NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, message);
            return false;
        }

        File projLoc = FileUtil.normalizeFile(destFolder);
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            String message = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, MSG_PROJECT_FOLDER_READ_ONLY);
            wizardDescriptor.putProperty( 
                    NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, message);
            return false;
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, 
                    MSG_ILLEGAL_PROJECT_LOCATION);
            wizardDescriptor.putProperty(
                    NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, message);
            return false;
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            String message = NbBundle.getMessage(
                    PanelProjectLocationVisual.class, MSG_PROJECT_FOLDER_EXISTS);
            wizardDescriptor.putProperty( 
                    NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, message);
            return false;
        }
        return true;
    }
    
    private void performUpdate( DocumentEvent event ) {
        updateTexts(event);
    }
    
    private class Listener implements DocumentListener {
        public void changedUpdate(DocumentEvent event ) {
            performUpdate(event);
        }

        public void insertUpdate(DocumentEvent event) {
            performUpdate(event);
        }

        public void removeUpdate(DocumentEvent event) {
            performUpdate(event);
        }
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JButton myBrowseButton;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    protected javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables
    
    private PhpProjectConfigurePanel myPanel;
    
    private DocumentListener myListener;

}
