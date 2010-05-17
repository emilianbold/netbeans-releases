/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.projectimport.jbuilder.ui;
import java.awt.Color;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.projectimport.j2seimport.AbstractProject;
import org.netbeans.modules.projectimport.j2seimport.ui.BasicWizardIterator;
import org.netbeans.modules.projectimport.jbuilder.parsing.ProjectBuilder;
import org.netbeans.modules.projectimport.jbuilder.parsing.UserLibrarySupport;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Represent "Selection" step(panel) in the JBuilder importer wizard.
 *
 * @author  Radek Matous
 */
public final class JBWizardPanel extends BasicWizardIterator.Panel {
    private JBWizardData    data = null;
    
    private boolean projectFileValid = false;
    private Collection allPrjDefs = null;
    private DocumentListener documentListener;
    
    /** Creates new form ProjectSelectionPanel */
    JBWizardPanel(WizardDescriptor wiz, JBWizardData data) {
        super(wiz);
        this.data = data;
        initComponents();
        initAccessibility();
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(JBWizardPanel.class, key);
    }
    
    private void initAccessibility() {
        prjFileBrowse.getAccessibleContext().setAccessibleDescription(getMessage("ACS_BrowseProjectFile"));
        prjFileTextField.getAccessibleContext().setAccessibleDescription(getMessage("ACS_Project"));
        destDirTextField.getAccessibleContext().setAccessibleDescription(getMessage("ACS_ProjectDestination"));
        userHomeTextField.getAccessibleContext().setAccessibleDescription(getMessage("ACS_UserHome"));
        userHomeBrowse.getAccessibleContext().setAccessibleDescription(getMessage("ACS_BrowseUserHome"));
        destDirBrowse.getAccessibleContext().setAccessibleDescription(getMessage("ACS_BrowseDestDir"));
        jbDirBrowse.getAccessibleContext().setAccessibleDescription(getMessage("ACS_BrowseInstallDir"));
        jbDirTextField.getAccessibleContext().setAccessibleDescription(getMessage("ACS_InstallDir"));
    }
        
    private void addListeners() {
        if (documentListener == null) {
            documentListener = new DocumentListener() {
                public void removeUpdate(DocumentEvent e) { insertUpdate(null); }
                public void changedUpdate(DocumentEvent e) { insertUpdate(null); }                
                public void insertUpdate(DocumentEvent e) { fieldUpdate(); }
            };
            
            prjFileTextField.getDocument().addDocumentListener(documentListener);
            destDirTextField.getDocument().addDocumentListener(documentListener);
            jbDirTextField.getDocument().addDocumentListener(documentListener);
            userHomeTextField.getDocument().addDocumentListener(documentListener);
        }
    }
    
    private void removeListeners() {
        if (documentListener != null) {
            prjFileTextField.getDocument().removeDocumentListener(documentListener);
            destDirTextField.getDocument().removeDocumentListener(documentListener);
            jbDirTextField.getDocument().removeDocumentListener(documentListener);
            userHomeTextField.getDocument().removeDocumentListener(documentListener);
        }
    }
    
    public void addNotify() {
        super.addNotify();
        addListeners();
        fieldUpdate();
        Color lblBgr = UIManager.getColor("Label.background"); // NOI18N
        prjFileTextArea.setBackground(lblBgr);
        jSpanTextArea.setBackground(lblBgr);
    }
    
    public void removeNotify() {
        super.removeNotify();
        removeListeners();
    }
            
    private void changedInstallDir() {
        File libFolder = null;
        if (UserLibrarySupport.getInstallDirLib() == null) {
            File installDir = getInstallationDir();
            if (installDir.exists()) {
                if (!installDir.getName().equals("lib")) { //NOI18N
                    libFolder = new File(installDir,"lib");//NOI18N
                    libFolder = (libFolder.exists()) ? libFolder : null;
                }
            }
        }
        
        if (libFolder != null) {
            Iterator it = Utils.getInvalidUserLibraries(allPrjDefs).iterator();
            while (it.hasNext()) {
                AbstractProject.UserLibrary uLib = (AbstractProject.UserLibrary)it.next();
                UserLibrarySupport usInstance;
                
                if (UserLibrarySupport.getInstance(uLib.getName(), libFolder) != null) {
                    UserLibrarySupport.setInstallDirLib(libFolder);
                    if (projectFileValid) {
                        parseProjectFile(getProjectFile());
                    }
                    break;
                }
            }
        }
    }
    
    private void fieldUpdate() {
        String errorMessage = null;
        boolean isUserHomeInvalid = false;
        File destFolder = (isEmpty(destDirTextField)) ? null : getFile(destDirTextField);
        boolean isDesttDirValid = false;
        for(File tempFolder = destFolder; tempFolder != null ; tempFolder = tempFolder.getParentFile()) {
            if (tempFolder == null || tempFolder.isFile()) {
                break;
            }
            if (tempFolder.canWrite()) {
                isDesttDirValid = true;
                break;
            }
        }
        if (!isDesttDirValid) {
            errorMessage = NbBundle.getMessage(JBWizardPanel.class, "MSG_NotRegularDestinationDir",destDirTextField.getText()); // NOI18N;
        }
        File projectFile = (isEmpty(prjFileTextField)) ? null : getFile(prjFileTextField);
        boolean unresolvedReferences = Utils.checkNotFoundUserLibraries(allPrjDefs);
        if (projectFile != null && ProjectBuilder.isProjectFile(projectFile)) {
            if (!projectFileValid) {
                projectFileValid = true;
                parseProjectFile(projectFile);
                return;
            } else {
                if (unresolvedReferences) {
                    File userHomeDir = UserLibrarySupport.getUserHomeLib();
                    File f = getFile(userHomeTextField);
                    if (isDesttDirValid) {
                        if (!isEmpty(userHomeTextField) && f.exists() && !f.equals(userHomeDir)) {
                            UserLibrarySupport.setUserHomeLib(f);
                            parseProjectFile(projectFile);
                            return;
                        } else if (isEmpty(userHomeTextField) || !f.exists() || f.getName().indexOf("jbuilder") == -1 ) {
                            //errorMessage = NbBundle.getMessage(JBWizardPanel.class, "MSG_NotRegularUserHome", userHomeTextField.getText()); // NOI18N
                            isUserHomeInvalid = true;
                        } else if (jbDirTextField.isEnabled()) {
                            changedInstallDir();
                        }
                    }
                }
            }
            enableAdditionalComponents(unresolvedReferences);
        } else {
            projectFileValid = false;
            enableAdditionalComponents(false);
            errorMessage = NbBundle.getMessage(JBWizardPanel.class, "MSG_NotRegularProject",prjFileTextField.getText()); // NOI18N;
        }
        
        if (errorMessage == null) {
            markValid();
            if (jbDirTextField.isEnabled() || isUserHomeInvalid) {
                setWarning(getMessage("LBL_Warning"));
            }            
        } else {
            setError(errorMessage);
        }                
    }
    
    protected String getPanelName() {
        return NbBundle.getMessage(JBWizardPanel.class, "CTL_FirstStep");
    }
    
    protected void storeToDataModel() {
        removeListeners();
        fieldUpdate();
        
        this.data.setDestinationDir(getFile(destDirTextField));
        this.data.setProjectFile(getFile(prjFileTextField));
        this.data.setProjectDefinition(this.allPrjDefs);
        this.data.setIncludeDependencies((Utils.getDependencyErrors(allPrjDefs,getDestinationDir()) == null));
    }
    
    protected void readFromDataModel() {
        addListeners();
        enableAdditionalComponents(false);
        if (data.getDestinationDir() != null) {
            destDirTextField.setText(data.getDestinationDir().getAbsolutePath());
        } else {
            destDirTextField.setText("");//NOPI18N
        }
        if (data.getProjectFile() != null) {
            prjFileTextField.setText(data.getProjectFile().getAbsolutePath());
        } else {
            prjFileTextField.setText("");//NOPI18N
        }
        File uLib = UserLibrarySupport.getUserHomeLib();
        if (uLib != null && uLib.exists()) {
            userHomeTextField.setText(uLib.getAbsolutePath());
        }
        
        File iLib = UserLibrarySupport.getInstallDirLib();
        if (iLib != null && iLib.exists()) {
            if (iLib.getName().equals("lib") && iLib.getParentFile() != null) {//NOI18N
                iLib = iLib.getParentFile();
            }
            jbDirTextField.setText(iLib.getAbsolutePath());
        }
        
        this.allPrjDefs = data.getProjectDefinition();
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(JBWizardPanel.class);
    }
    
    private void parseProjectFile(final File projectFile) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        Collection result = ProjectBuilder.buildProjectModels(projectFile);
                        if (result != null && result.size() > 0 && projectFileValid &&
                                projectFile.equals(getFile(prjFileTextField))) {
                            allPrjDefs =  result;
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    fieldUpdate();
                                }
                            });
                        }
                    }
                });
            }
        });
    }
                    
    private void enableAdditionalComponents(boolean enable) {
        jbDirTextField.setEnabled(enable);
        jbDirBrowse.setEnabled(enable);
        jbDirLabel.setEnabled(enable);
    }    
    
    public static File getFile(JTextField textField) {
        return new File(textField.getText().trim());
    }
    
    public static boolean isEmpty(JTextField textField) {
        return textField.getText().trim().length() == 0;
    }
    
    public File getDestinationDir() {
        return getFile(destDirTextField);
    }
    
    
    public File getProjectFile() {
        return getFile(prjFileTextField);
    }
    
    public File getInstallationDir() {
        return getFile(jbDirTextField);
    }
    
    private void browseActionPerformed(JTextField field, boolean fileHidingEnabled) {
        String initValue = field.getText().trim();
        JFileChooser chooser = new JFileChooser(initValue);        
        FileUtil.preventFileChooserSymlinkTraversal(chooser, chooser.getCurrentDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileHidingEnabled(fileHidingEnabled);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        prjFileTextField = new javax.swing.JTextField();
        prjFileBrowse = new javax.swing.JButton();
        prjFileLabel = new javax.swing.JLabel();
        destDirLabel = new javax.swing.JLabel();
        destDirTextField = new javax.swing.JTextField();
        destDirBrowse = new javax.swing.JButton();
        prjFileTextArea = new javax.swing.JTextArea();
        jbDirTextField = new javax.swing.JTextField();
        jbDirBrowse = new javax.swing.JButton();
        jSpanTextArea = new javax.swing.JTextArea();
        jbDirLabel = new javax.swing.JLabel();
        userHomeLabel = new javax.swing.JLabel();
        userHomeTextField = new javax.swing.JTextField();
        userHomeBrowse = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(500, 380));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(prjFileTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(prjFileBrowse, java.util.ResourceBundle.getBundle("org/netbeans/modules/projectimport/jbuilder/ui/Bundle").getString("CTL_BrowseProjectFile"));
        prjFileBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prjFileBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 10);
        add(prjFileBrowse, gridBagConstraints);

        prjFileLabel.setLabelFor(prjFileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(prjFileLabel, org.openide.util.NbBundle.getMessage(JBWizardPanel.class, "LBL_Project"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(prjFileLabel, gridBagConstraints);

        destDirLabel.setLabelFor(destDirTextField);
        org.openide.awt.Mnemonics.setLocalizedText(destDirLabel, org.openide.util.NbBundle.getMessage(JBWizardPanel.class, "LBL_ProjectDestination"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(destDirLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(destDirTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(destDirBrowse, java.util.ResourceBundle.getBundle("org/netbeans/modules/projectimport/jbuilder/ui/Bundle").getString("CTL_BrowseDestDir"));
        destDirBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                destDirBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 10);
        add(destDirBrowse, gridBagConstraints);

        prjFileTextArea.setEditable(false);
        prjFileTextArea.setLineWrap(true);
        prjFileTextArea.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/projectimport/jbuilder/ui/Bundle").getString("LBL_SpecifyProjectDescription"));
        prjFileTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        add(prjFileTextArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jbDirTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jbDirBrowse, java.util.ResourceBundle.getBundle("org/netbeans/modules/projectimport/jbuilder/ui/Bundle").getString("CTL_BrowseInstallDir"));
        jbDirBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbDirBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 10);
        add(jbDirBrowse, gridBagConstraints);

        jSpanTextArea.setEditable(false);
        jSpanTextArea.setLineWrap(true);
        jSpanTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 10, 0);
        add(jSpanTextArea, gridBagConstraints);

        jbDirLabel.setLabelFor(jbDirTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jbDirLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/projectimport/jbuilder/ui/Bundle").getString("LBL_InstallDir"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(jbDirLabel, gridBagConstraints);

        userHomeLabel.setLabelFor(userHomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userHomeLabel, org.openide.util.NbBundle.getMessage(JBWizardPanel.class, "LBL_UserHome"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(userHomeLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(userHomeTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(userHomeBrowse, java.util.ResourceBundle.getBundle("org/netbeans/modules/projectimport/jbuilder/ui/Bundle").getString("CTL_BrowseUserHome"));
        userHomeBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userHomeBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 10);
        add(userHomeBrowse, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void userHomeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userHomeBrowseActionPerformed
        browseActionPerformed(userHomeTextField, false);
    }//GEN-LAST:event_userHomeBrowseActionPerformed
        
    private void jbDirBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbDirBrowseActionPerformed
        browseActionPerformed(jbDirTextField, true);
    }//GEN-LAST:event_jbDirBrowseActionPerformed
    
    private void destDirBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destDirBrowseActionPerformed
        browseActionPerformed(destDirTextField, true);
    }//GEN-LAST:event_destDirBrowseActionPerformed
    
    
    private void prjFileBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prjFileBrowseActionPerformed
        String initValue = prjFileTextField.getText().trim();
        JFileChooser chooser = new JFileChooser(initValue);
        chooser.setFileFilter(new FileFilter(){
            public boolean accept(File f) {
                return f.isDirectory() || ProjectBuilder.isProjectFile(f);
            }
            
            public String getDescription() {
                return NbBundle.getMessage(JBWizardPanel.class, "ProjectFileDescription"); // NOI18N
            }
            
        });
        FileUtil.preventFileChooserSymlinkTraversal(chooser, chooser.getCurrentDirectory());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            prjFileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_prjFileBrowseActionPerformed
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JButton destDirBrowse;
    private javax.swing.JLabel destDirLabel;
    private javax.swing.JTextField destDirTextField;
    private javax.swing.JTextArea jSpanTextArea;
    private javax.swing.JButton jbDirBrowse;
    private javax.swing.JLabel jbDirLabel;
    private javax.swing.JTextField jbDirTextField;
    private javax.swing.JButton prjFileBrowse;
    private javax.swing.JLabel prjFileLabel;
    private javax.swing.JTextArea prjFileTextArea;
    private javax.swing.JTextField prjFileTextField;
    private javax.swing.JButton userHomeBrowse;
    private javax.swing.JLabel userHomeLabel;
    private javax.swing.JTextField userHomeTextField;
    // End of variables declaration//GEN-END:variables
}
