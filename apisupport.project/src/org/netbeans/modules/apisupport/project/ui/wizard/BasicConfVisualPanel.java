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
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbPlatform;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.ui.ComponentFactory;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.windows.WindowManager;

/**
 * Second UI panel of <code>NewNbModuleWizardIterator</code> for
 * <em>standalone</em> module creating mode. Allow user to enter basic
 * configuration:
 *
 * <ul>
 *  <li>Code Name Base</li>
 *  <li>Module Display Name</li>
 *  <li>Localizing Bundle</li>
 *  <li>XML Layer</li>
 *  <li>NetBeans Platform (for standalone modules)</li>
 *  <li>Module Suite (for suite modules)</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
final class BasicConfVisualPanel extends BasicVisualPanel {
    
    private static final String EXAMPLE_BASE_NAME = "org.yourorghere."; // NOI18N
    
    private NewModuleProjectData data;
    
    /** Creates new form BasicConfVisualPanel */
    public BasicConfVisualPanel(WizardDescriptor setting) {
        super(setting);
        initComponents();
        this.data = (NewModuleProjectData) getSetting().getProperty(
                "moduleProjectData"); // XXX should be constant
        codeNameBaseValue.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { codeNameBaseUpdated(); }
            public void removeUpdate(DocumentEvent e) { codeNameBaseUpdated(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        bundleValue.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { bundleUpdated(); }
            public void removeUpdate(DocumentEvent e) { bundleUpdated(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        layerValue.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { layerUpdated(); }
            public void removeUpdate(DocumentEvent e) { layerUpdated(); }
            public void changedUpdate(DocumentEvent e) {}
        });
    }
    
    private void codeNameBaseUpdated() {
        String dotName = getCodeNameBaseValue();
        String slashName = dotName.replace('.', '/');
        bundleValue.setText(slashName + "/Bundle.properties"); // NOI18N
        layerValue.setText(slashName + "/layer.xml"); // NOI18N
        setErrorMessage(null);
    }
    
    private void bundleUpdated() {
        checkEntry(getBundleValue(), "Bundle", ".properties"); // NOI18N
    }
    
    private void layerUpdated() {
        checkEntry(getLayerValue(), "Layer", ".xml"); // NOI18N
    }
    
    /** Used for Layer and Bundle entries. */
    private void checkEntry(String layer, String resName, String extension) {
        if (layer.length() == 0) {
            setErrorMessage(resName + " cannot be empty."); // NOI18N
            return;
        }
        if (layer.indexOf('/') == -1) {
            setErrorMessage("Cannot use default package for " + resName);
            return;
        }
        if (!layer.endsWith(extension)) {
            setErrorMessage(resName + " must have \"" + extension + "\" extension."); // NOI18N
            return;
        }
        setErrorMessage(null);
    }
    
    void refreshData() {
        String dotName = EXAMPLE_BASE_NAME + data.getProjectName();
        codeNameBaseValue.setText(dotName);
        codeNameBaseValue.select(0, EXAMPLE_BASE_NAME.length() - 1);
        displayNameValue.setText(data.getProjectName());
        if (data.isStandalone()) {
            standAloneModule.setSelected(true);
        } else {
            suiteModule.setSelected(true);
        }
        typeChanged(null);
        codeNameBaseUpdated();
    }
    
    /** Stores collected data into model. */
    void storeData() {
        // change will be fired -> update data
        NewModuleProjectData data = (NewModuleProjectData) getSetting().
                getProperty("moduleProjectData"); // XXX should be constant
        data.setStandalone(standAloneModule.isSelected());
        data.setCodeNameBase(getCodeNameBaseValue());
        data.setPlatform(((NbPlatform) platformValue.getSelectedItem()).getID());
        data.setSuiteRoot((String) moduleSuiteValue.getSelectedItem());
        data.setProjectDisplayName(displayNameValue.getText());
        data.setBundle(getBundleValue());
        data.setLayer(getLayerValue());
    }
    
    private String getCodeNameBaseValue() {
        return codeNameBaseValue.getText().trim();
    }
    
    private String getBundleValue() {
        return bundleValue.getText().trim();
    }
    
    private String getLayerValue() {
        return layerValue.getText().trim();
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
        confPanel = new javax.swing.JPanel();
        codeNameBase = new javax.swing.JLabel();
        displayName = new javax.swing.JLabel();
        bundle = new javax.swing.JLabel();
        layer = new javax.swing.JLabel();
        platform = new javax.swing.JLabel();
        codeNameBaseValue = new javax.swing.JTextField();
        displayNameValue = new javax.swing.JTextField();
        bundleValue = new javax.swing.JTextField();
        layerValue = new javax.swing.JTextField();
        platformValue = ComponentFactory.getNbPlatformsComboxBox();
        filler = new javax.swing.JLabel();
        standAloneModule = new javax.swing.JRadioButton();
        suiteModule = new javax.swing.JRadioButton();
        moduleSuite = new javax.swing.JLabel();
        browseSuiteButton = new javax.swing.JButton();
        moduleSuiteValue = ComponentFactory.getSuitesComboBox();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        confPanel.setLayout(new java.awt.GridBagLayout());

        codeNameBase.setLabelFor(codeNameBaseValue);
        org.openide.awt.Mnemonics.setLocalizedText(codeNameBase, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("LBL_CodeBaseName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 12);
        confPanel.add(codeNameBase, gridBagConstraints);

        displayName.setLabelFor(displayNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(displayName, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("LBL_ModuleDisplayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 12);
        confPanel.add(displayName, gridBagConstraints);

        bundle.setLabelFor(bundleValue);
        org.openide.awt.Mnemonics.setLocalizedText(bundle, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("LBL_LocalizingBundle"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 1, 12);
        confPanel.add(bundle, gridBagConstraints);

        layer.setLabelFor(layerValue);
        org.openide.awt.Mnemonics.setLocalizedText(layer, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("LBL_XMLLayer"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 12);
        confPanel.add(layer, gridBagConstraints);

        platform.setLabelFor(platformValue);
        org.openide.awt.Mnemonics.setLocalizedText(platform, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("LBL_NetBeansPlatform"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        confPanel.add(platform, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        confPanel.add(codeNameBaseValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        confPanel.add(displayNameValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 1, 0);
        confPanel.add(bundleValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        confPanel.add(layerValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        confPanel.add(platformValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.weighty = 1.0;
        confPanel.add(filler, gridBagConstraints);

        moduleTypeGroup.add(standAloneModule);
        standAloneModule.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(standAloneModule, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("CTL_StandaloneModule"));
        standAloneModule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        confPanel.add(standAloneModule, gridBagConstraints);

        moduleTypeGroup.add(suiteModule);
        org.openide.awt.Mnemonics.setLocalizedText(suiteModule, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("CTL_AddToModuleSuite"));
        suiteModule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        confPanel.add(suiteModule, gridBagConstraints);

        moduleSuite.setLabelFor(moduleSuiteValue);
        org.openide.awt.Mnemonics.setLocalizedText(moduleSuite, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("LBL_ModuleSuite"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        confPanel.add(moduleSuite, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseSuiteButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/Bundle").getString("CTL_BrowseButton_w"));
        browseSuiteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseModuleSuite(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        confPanel.add(browseSuiteButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        confPanel.add(moduleSuiteValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(confPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
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
        platform.setEnabled(standalone);
        platformValue.setEnabled(standalone);
        moduleSuite.setEnabled(!standalone);
        moduleSuiteValue.setEnabled(!standalone);
        browseSuiteButton.setEnabled(!standalone);
    }//GEN-LAST:event_typeChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseSuiteButton;
    private javax.swing.JLabel bundle;
    private javax.swing.JTextField bundleValue;
    private javax.swing.JLabel codeNameBase;
    private javax.swing.JTextField codeNameBaseValue;
    private javax.swing.JPanel confPanel;
    private javax.swing.JLabel displayName;
    private javax.swing.JTextField displayNameValue;
    private javax.swing.JLabel filler;
    private javax.swing.JLabel layer;
    private javax.swing.JTextField layerValue;
    private javax.swing.JLabel moduleSuite;
    private javax.swing.JComboBox moduleSuiteValue;
    private javax.swing.ButtonGroup moduleTypeGroup;
    private javax.swing.JLabel platform;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JRadioButton standAloneModule;
    private javax.swing.JRadioButton suiteModule;
    // End of variables declaration//GEN-END:variables
    
}
