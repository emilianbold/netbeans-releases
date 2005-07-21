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
import java.text.MessageFormat;
import javax.swing.ButtonModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.platform.ComponentFactory;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;

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
    private boolean isSuiteWizard;
    
    private ButtonModel lastSelectedType;
    private boolean wasLocationUpdate;
    
    /** Creates new form BasicInfoVisualPanel */
    public BasicInfoVisualPanel(WizardDescriptor setting, boolean isSuiteWizard) {
        super(setting);
        this.isSuiteWizard = isSuiteWizard;
        initComponents();
        
        this.data = (NewModuleProjectData) getSetting().getProperty(
                NewModuleProjectData.DATA_PROPERTY_NAME);
        if (isSuiteWizard) {
            detachModuleTypeGroup();
            typeChooserPanel.setVisible(false);
            locationValue.setText(ModuleUISettings.getDefault().getLastUsedModuleLocation());
            int counter = ModuleUISettings.getDefault().getNewSuiteCounter() + 1;
            setProjectName(getMessage("TXT_Suite"), counter); // NOI18N
            data.setSuiteCounter(counter);
        } else {
            if (moduleSuiteValue.getItemCount() > 0) {
                suiteModule.setSelected(true);
                locationValue.setText((String) moduleSuiteValue.getSelectedItem());
            } else {
                locationValue.setText(ModuleUISettings.getDefault().getLastUsedModuleLocation());
            }
            int counter = ModuleUISettings.getDefault().getNewModuleCounter() + 1;
            setProjectName(getMessage("TXT_Module"), counter); // NOI18N
            data.setModuleCounter(counter);
        }
        
        
        attachDocumentListeners();
        updateEnabled();
    }
    
    private String getNameValue() {
        return nameValue.getText().trim();
    }
    
    private String getLocationValue() {
        return locationValue.getText().trim();
    }
    
    private void updateEnabled() {
        boolean standalone = standAloneModule.isSelected();
        boolean suiteModuleSelected = suiteModule.isSelected();
        platform.setEnabled(standalone);
        platformValue.setEnabled(standalone);
        moduleSuite.setEnabled(suiteModuleSelected);
        moduleSuiteValue.setEnabled(suiteModuleSelected);
        browseSuiteButton.setEnabled(suiteModuleSelected);
    }
    
    void checkForm() {
        // check module name
        String name = getNameValue();
        if ("".equals(name)) { // NOI18N
            setErrorMessage(getMessage("MSG_NameCannotBeEmpty")); // NOI18N
            return;
        }
        updateAndCheck(true);
        
        // check module location
        File fLocation = new File(getLocationValue());
        if (!fLocation.exists()) {
            setErrorMessage(getMessage("MSG_LocationMustExist")); // NOI18N
            return;
        }
        if (!fLocation.canWrite()) {
            updateAndCheck(false); // also update folder but doesn't check for existing folder
            setErrorMessage(getMessage("MSG_LocationNotWritable")); // NOI18N
            return;
        } else {
            updateAndCheck(true);
        }
    }
    
    private void detachModuleTypeGroup() {
        moduleTypeGroup.remove(standAloneModule);
        moduleTypeGroup.remove(suiteModule);
        standAloneModule.setSelected(false);
        suiteModule.setSelected(false);
    }
    
    /**
     * @param alsoCheckFolderExistence - if function should also check if the
     *        target location doesn't already exist
     */
    private void updateAndCheck(boolean checkFolderExistence) {
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
                detachModuleTypeGroup();
            } else {
                moduleTypeGroup.add(standAloneModule);
                moduleTypeGroup.add(suiteModule);
                moduleTypeGroup.setSelected(lastSelectedType, true);
            }
            standAloneModule.setEnabled(!isNetBeansOrg);
            suiteModule.setEnabled(!isNetBeansOrg);
            typeChanged(null);
        }
        // check for regular module suite in case of module component
        if (suiteModule.isSelected() && !checkModuleSuite()) {
            return;
        }
        // check for regular netbeans platform in case of standalone module
        if (standAloneModule.isSelected() && !checkNbPlatform()) {
            return;
        }
        if (checkFolderExistence) {
            if (fFolder.exists()) {
                setErrorMessage(getMessage("MSG_ProjectFolderExists")); // NOI18N
                return;
            }
        }
        setErrorMessage(null);
    }
    
    private boolean checkModuleSuite() {
        if (moduleSuiteValue.getSelectedItem() == null) {
            setErrorMessage(getMessage("MSG_ChooseRegularSuite")); // NOI18N
            return false;
        } else {
            setErrorMessage(null);
            return true;
        }
    }
    
    private boolean checkNbPlatform() {
        // always at least the default platform is selected
        NbPlatform plaf = (NbPlatform) platformValue.getSelectedItem();
        if (standAloneModule.isSelected() && !plaf.isValid()) {
            setErrorMessage(getMessage("MSG_ChosenPlatformIsInvalid")); // NOI18N
            return false;
        } else {
            setErrorMessage(null);
            return true;
        }
    }
    
    /** Set <em>next</em> free project name. */
    private void setProjectName(String formater, int counter) {
        String name;
        while ((name = validFreeModuleName(formater, counter)) == null) {
            counter++;
        }
        nameValue.setText(name);
    }
    
    // stolen (then adjusted) from j2seproject
    private String validFreeModuleName(String formater, int index) {
        String name = MessageFormat.format(formater, new Object[]{ new Integer(index) });
        File file = new File(getLocationValue(), name);
        return file.exists() ? null : name;
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
    
    private void attachDocumentListeners() {
        DocumentListener fieldsDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { checkForm(); }
        };
        nameValue.getDocument().addDocumentListener(fieldsDL);
        locationValue.getDocument().addDocumentListener(fieldsDL);
        locationValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { wasLocationUpdate = true; }
        });
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

        infoPanel.setLayout(new java.awt.GridBagLayout());

        nameLbl.setLabelFor(nameValue);
        org.openide.awt.Mnemonics.setLocalizedText(nameLbl, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_ProjectName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        infoPanel.add(nameLbl, gridBagConstraints);

        locationLbl.setLabelFor(locationValue);
        org.openide.awt.Mnemonics.setLocalizedText(locationLbl, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_ProjectLocation"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 12);
        infoPanel.add(locationLbl, gridBagConstraints);

        folderLbl.setLabelFor(folderValue);
        org.openide.awt.Mnemonics.setLocalizedText(folderLbl, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_ProjectFolder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        infoPanel.add(folderLbl, gridBagConstraints);

        nameValue.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
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
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        infoPanel.add(locationValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_BrowseButton_o"));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocation(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(infoPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(separator2, gridBagConstraints);

        mainProject.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mainProject, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_SetAsMainProject"));
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        typeChooserPanel.add(standAloneModule, gridBagConstraints);

        platform.setLabelFor(platformValue);
        org.openide.awt.Mnemonics.setLocalizedText(platform, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_NetBeansPlatform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 0, 12);
        typeChooserPanel.add(platform, gridBagConstraints);

        platformValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformChosen(evt);
            }
        });

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
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        typeChooserPanel.add(suiteModule, gridBagConstraints);

        moduleSuite.setLabelFor(moduleSuiteValue);
        org.openide.awt.Mnemonics.setLocalizedText(moduleSuite, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_ModuleSuite"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 12);
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
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
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
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
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
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(typeChooserPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void platformChosen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformChosen
        updateAndCheck(true);
    }//GEN-LAST:event_platformChosen
    
    private void moduleSuiteChosen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moduleSuiteChosen
        if (!wasLocationUpdate) {
            locationValue.setText((String) moduleSuiteValue.getSelectedItem());
            wasLocationUpdate = false;
        }
        checkModuleSuite();
    }//GEN-LAST:event_moduleSuiteChosen
    
    private void browseModuleSuite(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseModuleSuite
        JFileChooser chooser = ProjectChooser.projectChooser();
        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File projectDir = chooser.getSelectedFile();
            UIUtil.setProjectChooserDirParent(projectDir);
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
        updateEnabled();
        if (suiteModule.isSelected()) {
            checkModuleSuite();
        } else { // standalone module
            checkNbPlatform();
        }
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
