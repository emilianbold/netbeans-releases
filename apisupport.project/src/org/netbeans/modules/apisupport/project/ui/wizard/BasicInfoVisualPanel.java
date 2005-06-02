/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.io.File;
import java.io.IOException;
import javax.swing.ButtonModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ModuleList;
import org.netbeans.modules.apisupport.project.NbPlatform;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.ui.ComponentFactory;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.windows.WindowManager;

/**
 * First panel of <code>NewNbModuleWizardIterator</code>. Allow user to enter
 * basic module information:
 *
 * <ul>
 *  <li>Project name</li>
 *  <li>Project Location</li>
 *  <li>Project Folder</li>
 *  <li>If should be set as a Main Project</li>
 *  <li>NetBeans Platform (for standalone modules)</li>
 *  <li>Module Suite (for suite modules)</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
public class BasicInfoVisualPanel extends BasicVisualPanel {
    
    private NewModuleProjectData data;

    private boolean isNetBeansOrg;

    private ButtonModel lastSelectedType;
    
    /** Creates new form BasicInfoVisualPanel */
    public BasicInfoVisualPanel(WizardDescriptor setting, boolean showModuleTypeChooser) {
        super(setting);
        initComponents();
        this.data = (NewModuleProjectData) getSetting().getProperty(
                NewModuleProjectData.DATA_PROPERTY_NAME);
        typeChooserPanel.setVisible(showModuleTypeChooser);
        nameValue.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { nameUpdated(); }
            public void removeUpdate(DocumentEvent e) { nameUpdated(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        locationValue.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { locationUpdated(); }
            public void removeUpdate(DocumentEvent e) { locationUpdated(); }
            public void changedUpdate(DocumentEvent e) {}
        });
    }
    
    private String getNameValue() {
        return nameValue.getText().trim();
    }
    
    private String getLocationValue() {
        return locationValue.getText().trim();
    }
    
    private void nameUpdated() {
        // check module name
        String name = getNameValue();
        if ("".equals(name)) { // NOI18N
            setErrorMessage(getMessage("MSG_NameCannotBeEmpty")); // NOI18N
            return;
        }
        updateFolder(true);
    }
    
    private void locationUpdated() {
        // check module location
        File fLocation = new File(getLocationValue());
        if (!fLocation.exists()) {
            setErrorMessage(getMessage("MSG_LocationMustExist")); // NOI18N
            return;
        }
        if (!fLocation.canWrite()) {
            updateFolder(false);
            setErrorMessage(getMessage("MSG_LocationNotWritable")); // NOI18N
            return;
        } else {
            updateFolder(true);
        }
    }
    
    private void updateFolder(boolean alsoCheck) {
        if ("".equals(getLocationValue()) || "".equals(getNameValue())) { // NOI18N
            folderValue.setText(""); // NOI18N
            setErrorMessage(null, false);
            return;
        }
        String path = getLocationValue() + File.separator + getNameValue();
        File fFolder = FileUtil.normalizeFile(new File(path));
        folderValue.setText(fFolder.getPath());
        boolean isNBOrg = ModuleList.findNetBeansOrg(fFolder) != null;
        if (isNBOrg != isNetBeansOrg) {
            isNetBeansOrg = isNBOrg;
            if (isNetBeansOrg) {
                lastSelectedType = moduleTypeGroup.getSelection();
                moduleTypeGroup.remove(standAloneModule);
                moduleTypeGroup.remove(suiteModule);
                standAloneModule.setSelected(false);
                suiteModule.setSelected(false);
            } else {
                moduleTypeGroup.add(standAloneModule);
                moduleTypeGroup.add(suiteModule);
                moduleTypeGroup.setSelected(lastSelectedType, true);
            }
            standAloneModule.setEnabled(!isNetBeansOrg);
            suiteModule.setEnabled(!isNetBeansOrg);
            typeChanged(null);
        }
        if (!checkModuleSuite()) {
            return;
        }
        if (alsoCheck) {
            if (fFolder.exists()) {
                setErrorMessage(getMessage("MSG_ProjectFolderExists")); // NOI18N
                return;
            }
            setErrorMessage(null);
        }
    }
    
    private boolean checkModuleSuite() {
        if (suiteModule.isSelected() && moduleSuiteValue.getSelectedItem() == null) {
            setErrorMessage(getMessage("MSG_ChooseRegularSuite")); // NOI18N
            return false;
        } else {
            setErrorMessage(null);
            return true;
        }
    }
    
    /** Stores collected data into model. */
    void storeData() {
        data.setProjectName(getNameValue());
        data.setProjectLocation(getLocationValue());
        data.setProjectFolder(folderValue.getText());
        data.setMainProject(mainProject.isSelected());
        data.setNetBeansOrg(isNetBeansOrg);
        data.setStandalone(standAloneModule.isSelected());
        data.setPlatform(((NbPlatform) platformValue.getSelectedItem()).getID());
        data.setSuiteRoot((String) moduleSuiteValue.getSelectedItem());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        moduleTypeGroup = new javax.swing.ButtonGroup();
        infoPanel = new javax.swing.JPanel();
        nameLbl = new javax.swing.JLabel();
        locationLbl = new javax.swing.JLabel();
        folderLbl = new javax.swing.JLabel();
        nameValue = new javax.swing.JTextField();
        locationValue = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        filler = new javax.swing.JLabel();
        folderValue = new javax.swing.JTextField();
        separator2 = new javax.swing.JSeparator();
        mainProject = new javax.swing.JCheckBox();
        typeChooserPanel = new javax.swing.JPanel();
        standAloneModule = new javax.swing.JRadioButton();
        platform = new javax.swing.JLabel();
        platformValue = ComponentFactory.getNbPlatformsComboxBox();
        suiteModule = new javax.swing.JRadioButton();
        moduleSuite = new javax.swing.JLabel();
        moduleSuiteValue = ComponentFactory.getSuitesComboBox();
        browseSuiteButton = new javax.swing.JButton();
        chooserFiller = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        infoPanel.setLayout(new java.awt.GridBagLayout());

        nameLbl.setLabelFor(nameValue);
        org.openide.awt.Mnemonics.setLocalizedText(nameLbl, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("LBL_ProjectName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        infoPanel.add(nameLbl, gridBagConstraints);

        locationLbl.setLabelFor(locationValue);
        org.openide.awt.Mnemonics.setLocalizedText(locationLbl, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("LBL_ProjectLocation"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        infoPanel.add(locationLbl, gridBagConstraints);

        folderLbl.setLabelFor(folderValue);
        org.openide.awt.Mnemonics.setLocalizedText(folderLbl, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("LBL_ProjectFolder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        infoPanel.add(folderLbl, gridBagConstraints);

        nameValue.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        infoPanel.add(nameValue, gridBagConstraints);

        locationValue.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        infoPanel.add(locationValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("CTL_BrowseButton_o"));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocation(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        infoPanel.add(browseButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 1.0;
        infoPanel.add(filler, gridBagConstraints);

        folderValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        infoPanel.add(folderValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(infoPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(separator2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mainProject, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("CTL_SetAsMainProject"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(mainProject, gridBagConstraints);

        typeChooserPanel.setLayout(new java.awt.GridBagLayout());

        moduleTypeGroup.add(standAloneModule);
        standAloneModule.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(standAloneModule, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_StandaloneModule"));
        standAloneModule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        typeChooserPanel.add(standAloneModule, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_NetBeansPlatform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        typeChooserPanel.add(platform, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        typeChooserPanel.add(platformValue, gridBagConstraints);

        moduleTypeGroup.add(suiteModule);
        org.openide.awt.Mnemonics.setLocalizedText(suiteModule, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_AddToModuleSuite"));
        suiteModule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        typeChooserPanel.add(suiteModule, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(moduleSuite, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_ModuleSuite"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        typeChooserPanel.add(moduleSuite, gridBagConstraints);

        moduleSuiteValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moduleSuiteChosen(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        typeChooserPanel.add(moduleSuiteValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseSuiteButton, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_BrowseButton_w"));
        browseSuiteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseModuleSuite(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        typeChooserPanel.add(browseSuiteButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 1.0;
        typeChooserPanel.add(chooserFiller, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(typeChooserPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void moduleSuiteChosen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moduleSuiteChosen
        checkModuleSuite();
    }//GEN-LAST:event_moduleSuiteChosen
    
    private void browseModuleSuite(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseModuleSuite
        JFileChooser chooser = ProjectChooser.projectChooser();
        int option = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (option == JFileChooser.APPROVE_OPTION) {
            File projectDir = chooser.getSelectedFile();
            try {
                Project suite = ProjectManager.getDefault().findProject(
                        FileUtil.toFileObject(projectDir));
                if (suite != null) {
                    SuiteProvider sp = (SuiteProvider) suite.
                            getLookup().lookup(SuiteProvider.class);
                    if (sp != null && sp.getSuiteDirectory() != null) {
                        String suiteDir = sp.getSuiteDirectory().getAbsolutePath();
                        // register for this session
                        ComponentFactory.addUserSuite(suiteDir);
                        // add to current combobox
                        moduleSuiteValue.addItem(suiteDir);
                        moduleSuiteValue.setSelectedItem(suiteDir);
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }//GEN-LAST:event_browseModuleSuite
    
    private void typeChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeChanged
        boolean standalone = standAloneModule.isSelected();
        boolean suiteModuleSelected = suiteModule.isSelected();
        platform.setEnabled(standalone);
        platformValue.setEnabled(standalone);
        moduleSuite.setEnabled(suiteModuleSelected);
        moduleSuiteValue.setEnabled(suiteModuleSelected);
        browseSuiteButton.setEnabled(suiteModuleSelected);
        checkModuleSuite();
    }//GEN-LAST:event_typeChanged
    
    private void browseLocation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocation
        JFileChooser chooser = new JFileChooser(locationValue.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            locationValue.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseLocation
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JButton browseSuiteButton;
    private javax.swing.JLabel chooserFiller;
    private javax.swing.JLabel filler;
    private javax.swing.JLabel folderLbl;
    private javax.swing.JTextField folderValue;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JLabel locationLbl;
    private javax.swing.JTextField locationValue;
    private javax.swing.JCheckBox mainProject;
    private javax.swing.JLabel moduleSuite;
    private javax.swing.JComboBox moduleSuiteValue;
    private javax.swing.ButtonGroup moduleTypeGroup;
    private javax.swing.JLabel nameLbl;
    private javax.swing.JTextField nameValue;
    private javax.swing.JLabel platform;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JSeparator separator2;
    private javax.swing.JRadioButton standAloneModule;
    private javax.swing.JRadioButton suiteModule;
    private javax.swing.JPanel typeChooserPanel;
    // End of variables declaration//GEN-END:variables
}
