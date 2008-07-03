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
package org.netbeans.modules.bpel.samples;

import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class SampleWizardPanelVisual extends JPanel implements DocumentListener {
    public static final String PROP_PROJECT_NAME = "projectName"; // NOI18N

    private SampleWizardPanel myPanel;
    private int myType;

    public SampleWizardPanelVisual(SampleWizardPanel panel) {
        initComponents();
        myPanel = panel;
        projectNameTextField.getDocument().addDocumentListener( this );
        projectLocationTextField.getDocument().addDocumentListener( this );
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(SampleWizardPanelVisual.class).getString("ACS_SampleWizardPanel_Description")); // NOI18N
    }

    protected String getDefaultProjectName() {
        return "SoaSampleProject"; // NOI18N
    }

    public String getProjectName() {
        return this.projectNameTextField.getText();
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        projectNameLabel.setLabelFor(projectNameTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2*2*2 + 2*2, 0);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel,NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_ProjectName_Label")); // NOI18N
        add(projectNameLabel, gridBagConstraints);
        projectNameTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SampleWizardPanelVisual.class, "ACS_ProjectName_Description")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2*2*2 + 2*2, 2*2*2 + 2*2, 0);
        add(projectNameTextField, gridBagConstraints);

        projectLocationLabel.setLabelFor(projectLocationTextField);
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SampleWizardPanelVisual.class, "ACS_ProjectLocation_Description")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2+2+2, 0);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_ProjectLocation_Label")); // NOI18N
        add(projectLocationLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2*2*2 + 2*2, 2+2+2, 0);
        add(projectLocationTextField, gridBagConstraints);

        browseButton.setActionCommand(BROWSE);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocationAction(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 2+2+2, 2+2+2, 0);
        org.openide.awt.Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_BrowseLocation_Button")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SampleWizardPanelVisual.class, "ACS_BrowseLocation__Description")); // NOI18N
        add(browseButton, gridBagConstraints);

        createdFolderLabel.setLabelFor(createdFolderTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_CreatedProjectFolder_Lablel")); // NOI18N
        add(createdFolderLabel, gridBagConstraints);

        createdFolderTextField.setEditable(false);
        createdFolderTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SampleWizardPanelVisual.class, "ACS_CreatedProjectFolder_Description")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2*2*2 + 2*2, 2*2*2 + 2*2, 0);
        add(createdFolderTextField, gridBagConstraints);

        initAdditionalComponents();
    }

    protected void initAdditionalComponents() {
        JLabel specialLabel = new javax.swing.JLabel();
        specialLabel.setFocusable(false);

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1+2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2+2+2, 0, 0);
        add(specialLabel, gridBagConstraints);
    }

    private void browseLocationAction(java.awt.event.ActionEvent evt) {
        String command = evt.getActionCommand();
        if (BROWSE.equals(command)) {
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogTitle(NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_SelectProjectLocation")); // NOI18N
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText( FileUtil.normalizeFile(projectDir).getAbsolutePath() );
            }
            myPanel.fireChangeEvent();
        }
    }

    public void addNotify() {
        super.addNotify();
        projectNameTextField.requestFocus();
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
//System.out.println();
        String projectName = projectNameTextField.getText();
//System.out.println("projectName: " + projectName + " " + isValidName(projectName));

        if (projectName.length() == 0
                || projectName.indexOf('/')  >= 0 // NOI18N
                || projectName.indexOf("\\") >= 0 // NOI18N
                || projectName.indexOf(':')  >= 0 // NOI18N
                || !isValidName(projectName)) {
//System.out.println(" 1: " + projectName + " " + isValidName(projectName));
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(getClass(), "MSG_IllegalProjectName")); // NOI18N
            return false;
        }
        if (projectName.indexOf(' ')  >= 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(getClass(), "MSG_IllegalProjectNameWithWhiteSpace")); // NOI18N
            return false;
        }
        File f = new File(projectLocationTextField.getText()).getAbsoluteFile();

        if (getCanonicalFile(f) == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(getClass(), "MSG_IllegalProjectLocation")); // NOI18N
            return false;
        }
        final File destFolder = new File(createdFolderTextField.getText()).getAbsoluteFile();

        if (getCanonicalFile(destFolder) == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(getClass(), "MSG_IllegalProjectName")); // NOI18N
            return false;
        }
        File projLoc = FileUtil.normalizeFile(destFolder);

        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(getClass(), "MSG_ProjectFolderReadOnly")); // NOI18N
            return false;
        }
        if (FileUtil.toFileObject(projLoc) == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(getClass(), "MSG_IllegalProjectLocation")); // NOI18N
            return false;
        }
        File[] kids = destFolder.listFiles();

        if (destFolder.exists() && kids != null && kids.length > 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(getClass(), "MSG_ProjectFolderExists")); // NOI18N
            return false;
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        return true;
    }

    private boolean isValidName(String fileName) {
      try {
          boolean isValid = true;
          File tempFile =  new File(fileName);
          String tempFileName ="00" + fileName; // NOI18N
          File actualTempFile = File.createTempFile(tempFileName, null);

          if ( !FileUtil.normalizeFile(tempFile).equals(tempFile.getCanonicalFile())) {
            isValid = false;
          }
          actualTempFile.delete();
          actualTempFile = null;
          tempFile = null;
          return isValid;
      }
      catch (IOException e) {
        return false;
      }
    }

    protected void store(WizardDescriptor d) {
      String name = projectNameTextField.getText().trim();

      d.putProperty(SampleWizardIterator.PROJECT_DIR, new File(createdFolderTextField.getText().trim()));
      d.putProperty(SampleWizardIterator.NAME, name);

      File projectsDir = new File(this.projectLocationTextField.getText());

      if (projectsDir.isDirectory()) {
          ProjectChooser.setProjectsFolder (projectsDir);
      }
    }

    protected void read(WizardDescriptor settings) {
      File projectLocation = (File) settings.getProperty(SampleWizardIterator.PROJECT_DIR);

      if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
          projectLocation = ProjectChooser.getProjectsFolder();
      }
      else {
          projectLocation = projectLocation.getParentFile();
      }
      projectLocationTextField.setText(projectLocation.getAbsolutePath());
      String projectName = (String) settings.getProperty(SampleWizardIterator.NAME);

      if (projectName == null) {
          projectName = getDefaultProjectName();
          File file = new File(projectLocation, projectName);
          int index1 = 1;

          if(file.exists()) {
              while(file.exists()) {
                  file = new File(projectLocation, projectName + String.valueOf(index1));
                  index1++;
              }
              projectName = projectName + String.valueOf(index1 -1);
          }

      }
      projectNameTextField.setText(projectName);
      projectNameTextField.selectAll();
    }

    protected void validate(WizardDescriptor d) throws WizardValidationException {}

    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;

    private static JFileChooser createChooser() {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        chooser.setAcceptAllFileFilterUsed( false );
        chooser.setName( NbBundle.getMessage(SampleWizardPanelVisual.class, "LBL_SelectProjectDirectory")); // NOI18N
        return chooser;
    }

    private String validFreeProjectName(final File parentFolder, final String formater, final int index) {
        String name = MessageFormat.format(formater, new Object[]{new Integer(index)});
        File file = new File(parentFolder, name);
        return file.exists() ? null : name;
    }

    public void changedUpdate( DocumentEvent e ) {
        updateTexts( e );
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }

    public void insertUpdate( DocumentEvent e ) {
        updateTexts( e );
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }

    public void removeUpdate( DocumentEvent e ) {
        updateTexts( e );
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME,null,this.projectNameTextField.getText());
        }
    }

    private void updateTexts(DocumentEvent e) {
        Document doc = e.getDocument();

        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();
            String projFolderPath = FileUtil.normalizeFile(new File(projectFolder)).getAbsolutePath();
            
            if (projFolderPath.endsWith(File.separator)) {
                createdFolderTextField.setText(projFolderPath + projectName);
            }
            else {
                createdFolderTextField.setText(projFolderPath + File.separator + projectName);
            }
        }                
        myPanel.fireChangeEvent();
    }

    private File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
    }

    private static final String BROWSE = "BROWSE"; // NOI18N
}
