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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;

/**
 * @author mkrauskopf
 */
final class BasicConfVisualPanel extends javax.swing.JPanel {
    
    private static final String EXAMPLE_BASE_NAME = "org.yourorghere."; // NOI18N
    
    private WizardDescriptor settings;
    private NewModuleProjectData data;
    private Boolean valid = Boolean.FALSE;
    
    /** Creates new form BasicConfVisualPanel */
    public BasicConfVisualPanel(WizardDescriptor setting) {
        initComponents();
        this.settings = setting;
        this.data = (NewModuleProjectData) settings.
                getProperty("moduleProjectData"); // XXX should be constant
        platformValue.addItem(getDefaultPlatform());
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
        
    }
    
    // TODO this whole method is nonsense ant will be probably removed in the 
    // future when thinks around NB Platforms will be clear
    private String getDefaultPlatform() {
        File userDirProps = new File(System.getProperty("netbeans.user"),
                "build.properties");
        Properties props = new Properties();
        InputStream is = null;
        String plf = null;
        try {
            is = new FileInputStream(userDirProps);
            props.load(is);
            plf = props.getProperty("netbeans.dest.dir");
        } catch (IOException e) {
            System.err.println("Cannot load default platform: " + e); // NOI18N
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return plf;
    }
    
    private void codeNameBaseUpdated() {
        String dotName = getCodeNameBaseValue();
        String slashName = dotName.replace('.', '/');
        bundleValue.setText(slashName + "/Bundle.properties"); // NOI18N
        layerValue.setText(slashName + "/layer.xml"); // NOI18N
        setErrorMessage(null);
    }
    
    private void bundleUpdated() {
        String bundle = getBundleValue();
        if (bundle.length() == 0) {
            setErrorMessage("Bundle cannot be empty."); // NOI18N
            return;
        }
        if (bundle.indexOf('/') == -1) {
            setErrorMessage("Cannot use default package for a Bundle.");
            return;
        }
        if (!bundle.endsWith(".properties")) {
            setErrorMessage("Bundle must have \".properties\" extension.");
            return;
        }
        setErrorMessage(null);
    }
    
    /** Set error message and always update panel validity. */
    private void setErrorMessage(String errorMessage) {
        setErrorMessage(errorMessage, true);
    }
    
    /** Set error message and eventually update panel validity. */
    private void setErrorMessage(String errorMessage, boolean fireChange) {
        settings.putProperty("WizardPanel_errorMessage", errorMessage); // NOI18N
        if (fireChange) {
            setValid(Boolean.valueOf(errorMessage == null));
        }
    }
    
    private void setValid(Boolean newValid) {
        Boolean oldValid = valid;
        valid = newValid;
        firePropertyChange("valid", oldValid, newValid); // NOI18N
    }
    
    void refreshData() {
        String dotName = EXAMPLE_BASE_NAME + data.getProjectName();
        codeNameBaseValue.setText(dotName);
        codeNameBaseValue.select(0, EXAMPLE_BASE_NAME.length() - 1);
        displayNameValue.setText(data.getProjectName());
        codeNameBaseUpdated();
    }
    
    /** Stores collected data into model. */
    void storeData() {
        // change will be fired -> update data
        NewModuleProjectData data = (NewModuleProjectData) settings.
                getProperty("moduleProjectData"); // XXX should be constant
        data.setCodeNameBase(getCodeNameBaseValue());
        data.setPlatform((String) platformValue.getSelectedItem());
        data.setProjectDisplayName(displayNameValue.getText());
        data.setBundle(getBundleValue());
    }

    private String getCodeNameBaseValue() {
        return codeNameBaseValue.getText().trim();
    }
    
    private String getBundleValue() {
        return bundleValue.getText().trim();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLocation = new javax.swing.JLabel();
        separator1 = new javax.swing.JSeparator();
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
        platformValue = new javax.swing.JComboBox();
        filler = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        nameLocation.setText("Basic Module Configuration");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(nameLocation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(separator1, gridBagConstraints);

        confPanel.setLayout(new java.awt.GridBagLayout());

        codeNameBase.setText("Code Name Base:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 12);
        confPanel.add(codeNameBase, gridBagConstraints);

        displayName.setText("Module Display Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 12);
        confPanel.add(displayName, gridBagConstraints);

        bundle.setText("Localizing Bundle:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 12);
        confPanel.add(bundle, gridBagConstraints);

        layer.setText("XML Layer:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 12);
        confPanel.add(layer, gridBagConstraints);

        platform.setText("NetBeans Platform:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        confPanel.add(platform, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        confPanel.add(codeNameBaseValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        confPanel.add(displayNameValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        confPanel.add(bundleValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        confPanel.add(layerValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        confPanel.add(platformValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.weighty = 1.0;
        confPanel.add(filler, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(confPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    private javax.swing.JLabel nameLocation;
    private javax.swing.JLabel platform;
    private javax.swing.JComboBox platformValue;
    private javax.swing.JSeparator separator1;
    // End of variables declaration//GEN-END:variables
    
}
