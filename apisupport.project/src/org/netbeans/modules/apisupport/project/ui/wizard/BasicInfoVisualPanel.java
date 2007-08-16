/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.ButtonModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory;
import org.netbeans.modules.apisupport.project.ui.platform.NbPlatformCustomizer;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

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
public class BasicInfoVisualPanel extends BasicVisualPanel.NewTemplatePanel {
    
    private ButtonModel lastSelectedType;
    private static String lastSelectedSuite;
    private boolean locationUpdated;
    private boolean nameUpdated;
    private boolean moduleTypeGroupAttached = true;
    private boolean mainProjectTouched;
    
    /** Creates new form BasicInfoVisualPanel */
    BasicInfoVisualPanel(final NewModuleProjectData data) {
        super(data);
        initComponents();
        initAccessibility();
        initPlatformCombos();
        setComponentsVisibility();
        switch (data.getWizardType()) {
            case NewNbModuleWizardIterator.TYPE_SUITE:
                detachModuleTypeGroup();
                break;
            case NewNbModuleWizardIterator.TYPE_MODULE:
            case NewNbModuleWizardIterator.TYPE_SUITE_COMPONENT:
                if (moduleSuiteValue.getItemCount() > 0) {
                    restoreSelectedSuite();
                    suiteComponent.setSelected(true);
                    mainProject.setSelected(false);
                }
                break;
            case NewNbModuleWizardIterator.TYPE_LIBRARY_MODULE:
                moduleSuite.setText(getMessage("LBL_Add_to_Suite")); // NOI18N
                suiteComponent.setSelected(true);
                if (moduleSuiteValue.getItemCount() > 0) {
                    restoreSelectedSuite();
                }
                break;
            default:
                assert false : "Unknown wizard type = " + data.getWizardType();
        }
        attachDocumentListeners();
        setInitialLocation();
        setInitialProjectName();
        updateEnabled();
    }
    
    private void setInitialLocation() {
        if (isSuiteComponent()) {
            computeAndSetLocation((String) moduleSuiteValue.getSelectedItem(), true);
        } else { // suite or standalone module
            String location = computeLocationValue(ProjectChooser.getProjectsFolder().getAbsolutePath());
            File locationF = new File(location);
            if (SuiteUtils.isSuite(locationF)) {
                computeAndSetLocation(locationF.getParent(), true);
            } else {
                setLocation(location, true);
            }
        }
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_BasicInfoVisualPanel"));
        browseButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_BrowseButton"));
        browseSuiteButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_BrowseSuiteButton"));
        folderValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_FolderValue"));
        locationValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_LocationValue"));
        mainProject.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_MainProject"));
        managePlatform.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ManagePlatform"));
        manageSuitePlatform.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ManageSuitePlatform"));
        moduleSuiteValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ModuleSuiteValue"));
        nameValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_NameValue"));
        platformValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_PlatformValue"));
        standAloneModule.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_StandAloneModule"));
        suiteComponent.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_SuiteModule"));
        suitePlatformValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_SuitePlatformValue"));
    }
    
    private void setComponentsVisibility() {
        boolean isSuiteWizard = isSuiteWizard();
        boolean isSuiteComponentWizard = isSuiteComponentWizard();
        boolean isLibraryWizard = isLibraryWizard();
        
        typeChooserPanel.setVisible(!isSuiteWizard);
        suitePlatform.setVisible(isSuiteWizard);
        suitePlatformValue.setVisible(isSuiteWizard);
        manageSuitePlatform.setVisible(isSuiteWizard);
        mainProject.setVisible(!isLibraryWizard);
        
        suiteComponent.setVisible(!isLibraryWizard);
        platform.setVisible(!isLibraryWizard);
        platformValue.setVisible(!isLibraryWizard);
        managePlatform.setVisible(!isLibraryWizard);
        standAloneModule.setVisible(!isLibraryWizard);
        mainProject.setSelected(!isLibraryWizard);
        
        standAloneModule.setVisible(!isSuiteComponentWizard && !isLibraryWizard);
        platform.setVisible(!isSuiteComponentWizard && !isLibraryWizard);
        platformValue.setVisible(!isSuiteComponentWizard && !isLibraryWizard);
        managePlatform.setVisible(!isSuiteComponentWizard && !isLibraryWizard);
        suiteComponent.setVisible(!isSuiteComponentWizard && !isLibraryWizard);
    }
    
    private void restoreSelectedSuite() {
        String preferredSuiteDir  = getPreferredSuiteDir();
        if (preferredSuiteDir != null) {
            lastSelectedSuite = preferredSuiteDir;
        }
        if (lastSelectedSuite != null) {
            int max = moduleSuiteValue.getModel().getSize();
            for (int i=0; i < max; i++) {
                if (lastSelectedSuite.equals(moduleSuiteValue.getModel().getElementAt(i))) {
                    moduleSuiteValue.setSelectedItem(lastSelectedSuite);
                    break;
                }
            }
        }
    }
    
    private String getPreferredSuiteDir() {
        return (String) getSettings().getProperty(NewNbModuleWizardIterator.PREFERRED_SUITE_DIR);
    }
    
    private boolean isOneSuiteDedicatedMode() {
        Boolean b = (Boolean) getSettings().getProperty(
                NewNbModuleWizardIterator.ONE_SUITE_DEDICATED_MODE);
        return b != null ? b.booleanValue() : false;
    }
    
    private String getNameValue() {
        return nameValue.getText().trim();
    }
    
    private String getLocationValue() {
        return locationValue.getText().trim();
    }
    
    private File getLocationFile() {
        return new File(getLocationValue());
    }
    
    private void updateEnabled() {
        boolean isNetBeansOrg = isNetBeansOrgFolder();
        standAloneModule.setEnabled(!isNetBeansOrg);
        suiteComponent.setEnabled(!isNetBeansOrg);
        
        boolean standalone = isStandAlone();
        boolean suiteModuleSelected = isSuiteComponent();
        platform.setEnabled(standalone);
        platformValue.setEnabled(standalone);
        managePlatform.setEnabled(standalone);
        moduleSuite.setEnabled(suiteModuleSelected);
        moduleSuiteValue.setEnabled(suiteModuleSelected && !isOneSuiteDedicatedMode());
        browseSuiteButton.setEnabled(suiteModuleSelected && !isOneSuiteDedicatedMode());
    }
    
    void updateAndCheck() {
        updateGUI();
        
        if ("".equals(getNameValue())) {
            setError(getMessage("MSG_NameCannotBeEmpty"));
        } else if ("".equals(getLocationValue())) {
            setError(getMessage("MSG_LocationCannotBeEmpty"));
        } else if (isLibraryWizard() && isNetBeansOrgFolder()) {
            setError(getMessage("MSG_LibraryWrapperForNBOrgUnsupported"));
        } else if (isSuiteComponent() && moduleSuiteValue.getSelectedItem() == null) {
            setError(getMessage("MSG_ChooseRegularSuite"));
        } else if (isStandAlone() &&
                (platformValue.getSelectedItem() == null || !((NbPlatform) platformValue.getSelectedItem()).isValid())) {
            setError(getMessage("MSG_ChosenPlatformIsInvalid"));
        } else if (isSuiteWizard() &&
                (suitePlatformValue.getSelectedItem() == null || !((NbPlatform) suitePlatformValue.getSelectedItem()).isValid())) {
            setError(getMessage("MSG_ChosenPlatformIsInvalid"));
        } else if (getFolder().exists()) {
            setError(getMessage("MSG_ProjectFolderExists"));
        } else if (!getLocationFile().exists()) {
            setError(getMessage("MSG_LocationMustExist"));
        } else if (!getLocationFile().canWrite()) {
            setError(getMessage("MSG_LocationNotWritable"));
        } else {
            markValid();
        }
    }
    
    private void updateGUI() {
        // update project folder
        folderValue.setText(getFolder().getPath());
        
        if (isSuiteWizard() || isNetBeansOrgFolder()) {
            detachModuleTypeGroup();
        } else {
            attachModuleTypeGroup();
        }
        updateEnabled();
    }
    
    private void detachModuleTypeGroup() {
        if (moduleTypeGroupAttached) {
            lastSelectedType = moduleTypeGroup.getSelection();
            moduleTypeGroup.remove(standAloneModule);
            moduleTypeGroup.remove(suiteComponent);
            standAloneModule.setSelected(false);
            suiteComponent.setSelected(false);
            moduleTypeGroupAttached = false;
        }
    }
    
    private void attachModuleTypeGroup() {
        if (!moduleTypeGroupAttached) {
            moduleTypeGroup.add(standAloneModule);
            moduleTypeGroup.add(suiteComponent);
            if (isLibraryWizard()) {
                suiteComponent.setSelected(true);
            } else {
                moduleTypeGroup.setSelected(lastSelectedType, true);
            }
            moduleTypeGroupAttached = true;
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
        getData().setProjectName(getNameValue());
        getData().setProjectLocation(getLocationValue());
        getData().setProjectFolder(folderValue.getText());
        getData().setMainProject(mainProject.isSelected());
        getData().setNetBeansOrg(isNetBeansOrgFolder());
        getData().setStandalone(isStandAlone());
        getData().setSuiteRoot((String) moduleSuiteValue.getSelectedItem());
        if (isSuiteWizard() && suitePlatformValue.getSelectedItem() != null) {
            getData().setPlatformID(((NbPlatform) suitePlatformValue.getSelectedItem()).getID());
        } else if (platformValue.getSelectedItem() != null) {
            getData().setPlatformID(((NbPlatform) platformValue.getSelectedItem()).getID());
        }
    }
    
    void refreshData() {
        if (getData().getProjectName() != null) {
            nameValue.setText(getData().getProjectName());
        } else {
            setInitialProjectName();
        }
    }
    
    private void setInitialProjectName() {
        String bundlekey = null;
        int counter = 0;
        switch (getData().getWizardType()) {
            case NewNbModuleWizardIterator.TYPE_SUITE:
                counter = ModuleUISettings.getDefault().getNewSuiteCounter() + 1;
                bundlekey = "TXT_Suite"; //NOI18N
                getData().setSuiteCounter(counter);
                break;
            case NewNbModuleWizardIterator.TYPE_MODULE:
            case NewNbModuleWizardIterator.TYPE_SUITE_COMPONENT:
                counter = ModuleUISettings.getDefault().getNewModuleCounter() + 1;
                bundlekey = "TXT_Module"; //NOI18N
                getData().setModuleCounter(counter);
                break;
            case NewNbModuleWizardIterator.TYPE_LIBRARY_MODULE:
                counter = ModuleUISettings.getDefault().getNewModuleCounter() + 1;
                bundlekey = "TXT_Library"; //NOI18N
                getData().setModuleCounter(counter);
                break;
            default:
                assert false : "Unknown wizard type = " + getData().getWizardType();
        }
        setProjectName(getMessage(bundlekey), counter);
        nameValue.select(0, nameValue.getText().length());
        nameUpdated = false;
    }
    
    private void attachDocumentListeners() {
        DocumentListener fieldsDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { updateAndCheck(); }
        };
        nameValue.getDocument().addDocumentListener(fieldsDL);
        nameValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { nameUpdated = true; }
        });
        locationValue.getDocument().addDocumentListener(fieldsDL);
        locationValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { locationUpdated = true; }
        });
        ActionListener plafAL = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateAndCheck();
            }
        };
        platformValue.addActionListener(plafAL);
        suitePlatformValue.addActionListener(plafAL);
    }
    
    private File getFolder() {
        StringBuffer destFolder = new StringBuffer(getLocationValue());
        if (destFolder.length() != 0) {
            destFolder.append(File.separator);
        }
        destFolder.append(getNameValue());
        return FileUtil.normalizeFile(new File(destFolder.toString()));
    }
    
    private boolean isNetBeansOrgFolder() {
        return ModuleList.findNetBeansOrg(getFolder()) != null;
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(BasicInfoVisualPanel.class, key);
    }
    
    private void initPlatformCombos() {
        if (platformValue.getItemCount() <= 0) {
            return;
        }
        boolean set = false;
        String idToSelect = ModuleUISettings.getDefault().getLastUsedPlatformID();
        for (int i = 0; i < platformValue.getItemCount(); i++) {
            if (((NbPlatform) platformValue.getItemAt(i)).getID().equals(idToSelect)) {
                platformValue.setSelectedIndex(i);
                suitePlatformValue.setSelectedIndex(i);
                set = true;
                break;
            }
        }
        if (!set) {
            NbPlatform defPlaf = NbPlatform.getDefaultPlatform();
            platformValue.setSelectedItem(defPlaf == null ? platformValue.getItemAt(0) : defPlaf);
            suitePlatformValue.setSelectedItem(defPlaf == null ? suitePlatformValue.getItemAt(0) : defPlaf);
        }
    }
    
    private void setLocation(String location, boolean silently) {
        boolean revert = silently && !locationUpdated;
        locationValue.setText(location);
        locationUpdated = revert ^ true;
    }
    
    private void computeAndSetLocation(String value, boolean silently) {
        setLocation(computeLocationValue(value), silently);
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
    
    private boolean isStandAlone() {
        return standAloneModule.isSelected();
    }
    
    private boolean isSuiteComponent() {
        return suiteComponent.isSelected();
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
        suitePlatform = new javax.swing.JLabel();
        suitePlatformValue = PlatformComponentFactory.getNbPlatformsComboxBox();
        manageSuitePlatform = new javax.swing.JButton();
        separator3 = new javax.swing.JSeparator();
        mainProject = new javax.swing.JCheckBox();
        typeChooserPanel = new javax.swing.JPanel();
        standAloneModule = new javax.swing.JRadioButton();
        platform = new javax.swing.JLabel();
        platformValue = PlatformComponentFactory.getNbPlatformsComboxBox();
        managePlatform = new javax.swing.JButton();
        suiteComponent = new javax.swing.JRadioButton();
        moduleSuite = new javax.swing.JLabel();
        moduleSuiteValue = PlatformComponentFactory.getSuitesComboBox();
        browseSuiteButton = new javax.swing.JButton();
        chooserFiller = new javax.swing.JLabel();
        pnlThouShaltBeholdLayout = new javax.swing.JPanel();

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
        gridBagConstraints.gridy = 5;
        gridBagConstraints.weighty = 1.0;
        infoPanel.add(filler, gridBagConstraints);

        folderValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        infoPanel.add(folderValue, gridBagConstraints);

        suitePlatform.setLabelFor(suitePlatformValue);
        org.openide.awt.Mnemonics.setLocalizedText(suitePlatform, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "LBL_NetBeansPlatform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 12);
        infoPanel.add(suitePlatform, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        infoPanel.add(suitePlatformValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(manageSuitePlatform, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_ManagePlatforms_g"));
        manageSuitePlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageSuitePlatformActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        infoPanel.add(manageSuitePlatform, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 6, 0);
        infoPanel.add(separator3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(infoPanel, gridBagConstraints);

        mainProject.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mainProject, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_SetAsMainProject"));
        mainProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainProjectActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        typeChooserPanel.add(platformValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(managePlatform, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_ManagePlatforms_g"));
        managePlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                managePlatformActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        typeChooserPanel.add(managePlatform, gridBagConstraints);

        moduleTypeGroup.add(suiteComponent);
        org.openide.awt.Mnemonics.setLocalizedText(suiteComponent, org.openide.util.NbBundle.getMessage(BasicInfoVisualPanel.class, "CTL_AddToModuleSuite"));
        suiteComponent.addActionListener(new java.awt.event.ActionListener() {
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
        typeChooserPanel.add(suiteComponent, gridBagConstraints);

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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
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
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(typeChooserPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pnlThouShaltBeholdLayout, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void manageSuitePlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageSuitePlatformActionPerformed
        managePlatform(suitePlatformValue);
    }//GEN-LAST:event_manageSuitePlatformActionPerformed
    
    private void mainProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainProjectActionPerformed
        mainProjectTouched = true;
    }//GEN-LAST:event_mainProjectActionPerformed
    
    private void managePlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managePlatformActionPerformed
        managePlatform(platformValue);
    }//GEN-LAST:event_managePlatformActionPerformed
    
    private void managePlatform(final JComboBox platformCombo) {
        NbPlatformCustomizer.showCustomizer();
        platformCombo.setModel(new PlatformComponentFactory.NbPlatformListModel()); // refresh
        platformCombo.requestFocus();
        updateAndCheck();
    }
    
    private void platformChosen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformChosen
        updateAndCheck();
    }//GEN-LAST:event_platformChosen
    
    private void moduleSuiteChosen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moduleSuiteChosen
        if (!locationUpdated) {
            String suite = (String) moduleSuiteValue.getSelectedItem();
            computeAndSetLocation(suite, true);
            lastSelectedSuite = suite;
        }
        updateAndCheck();
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
                    String suiteDir = SuiteUtils.getSuiteDirectoryPath(suite);
                    if (suiteDir != null) {
                        // register for this session
                        PlatformComponentFactory.addUserSuite(suiteDir);
                        // add to current combobox
                        moduleSuiteValue.addItem(suiteDir);
                        moduleSuiteValue.setSelectedItem(suiteDir);
                    } else {
                        DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(
                                NbBundle.getMessage(BasicInfoVisualPanel.class, "MSG_NotRegularSuite",
                                ProjectUtils.getInformation(suite).getDisplayName())));
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
    }//GEN-LAST:event_browseModuleSuite
    
    private void typeChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeChanged
        if (!mainProjectTouched) {
            mainProject.setSelected(isStandAlone());
        }
        if (!locationUpdated) {
            setInitialLocation();
        }
        if (!nameUpdated) {
            setInitialProjectName();
        }
        updateAndCheck();
    }//GEN-LAST:event_typeChanged
    
    private void browseLocation(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocation
        JFileChooser chooser = new JFileChooser(getLocationValue());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            computeAndSetLocation(chooser.getSelectedFile().getAbsolutePath(), false);
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
    private javax.swing.JButton managePlatform;
    private javax.swing.JButton manageSuitePlatform;
    private javax.swing.JLabel moduleSuite;
    private javax.swing.JComboBox moduleSuiteValue;
    private javax.swing.ButtonGroup moduleTypeGroup;
    private javax.swing.JLabel nameLbl;
    javax.swing.JTextField nameValue;
    private javax.swing.JLabel platform;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JPanel pnlThouShaltBeholdLayout;
    private javax.swing.JSeparator separator3;
    private javax.swing.JRadioButton standAloneModule;
    private javax.swing.JRadioButton suiteComponent;
    private javax.swing.JLabel suitePlatform;
    private javax.swing.JComboBox suitePlatformValue;
    private javax.swing.JPanel typeChooserPanel;
    // End of variables declaration//GEN-END:variables
    
}
