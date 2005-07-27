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

import java.util.StringTokenizer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.openide.WizardDescriptor;

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
 * </ul>
 *
 * @author Martin Krauskopf
 */
final class BasicConfVisualPanel extends BasicVisualPanel {
    
    private static final String EXAMPLE_BASE_NAME = "org.yourorghere."; // NOI18N
    
    private NewModuleProjectData data;
    private boolean wasLayerUpdated;
    private boolean wasBundleUpdated;
    
    private boolean listenersAttached;
    private DocumentListener cnbDL;
    private DocumentListener layerDL;
    private DocumentListener bundleDL;
    
    /** Creates new form BasicConfVisualPanel */
    public BasicConfVisualPanel(WizardDescriptor setting) {
        super(setting);
        initComponents();
        this.data = (NewModuleProjectData) getSettings().getProperty(
                NewModuleProjectData.DATA_PROPERTY_NAME);
        cnbDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { checkCodeNameBase(); }
        };
        layerDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { wasLayerUpdated = true; checkLayer(); }
        };
        bundleDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { wasBundleUpdated = true; checkBundle(); }
        };
    }
    
    private void checkCodeNameBase() {
        if (!isValidCodeNameBase(getCodeNameBaseValue())) {
            setErrorMessage(getMessage("MSG_InvalidCNB")); // NOI18N
        } else {
            setErrorMessage(null);
            // update layer and bundle from the cnb
            String dotName = getCodeNameBaseValue();
            String slashName = dotName.replace('.', '/');
            if (!wasBundleUpdated) {
                bundleValue.setText(slashName + "/Bundle.properties"); // NOI18N
                wasBundleUpdated = false;
            }
            if (!wasLayerUpdated) {
                layerValue.setText(slashName + "/layer.xml"); // NOI18N
                wasLayerUpdated = false;
            }
        }
    }
    
    private void checkBundle() {
        checkEntry(getBundleValue(), "Bundle", ".properties"); // NOI18N
    }
    
    private void checkLayer() {
        checkEntry(getLayerValue(), "Layer", ".xml"); // NOI18N
    }
    
    /** Used for Layer and Bundle entries. */
    private void checkEntry(String path, String resName, String extension) {
        if (path.length() == 0) {
            setErrorMessage(resName + " cannot be empty."); // NOI18N
            return;
        }
        if (path.indexOf('/') == -1) {
            setErrorMessage("Cannot use default package for " + resName);
            return;
        }
        if (!path.endsWith(extension)) {
            setErrorMessage(resName + " must have \"" + extension + "\" extension."); // NOI18N
            return;
        }
        setErrorMessage(null);
    }
    
    void refreshData() {
        String dotName = EXAMPLE_BASE_NAME + data.getProjectName();
        codeNameBaseValue.setText(Util.normalizeCNB(dotName));
        codeNameBaseValue.select(0, EXAMPLE_BASE_NAME.length() - 1);
        displayNameValue.setText(data.getProjectName());
        checkCodeNameBase();
    }
    
    /** Stores collected data into model. */
    void storeData() {
        // change will be fired -> update data
        data.setCodeNameBase(getCodeNameBaseValue());
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
    
    // Stolen from java/project PackageViewChildren.isValidPackageName()
    // XXX probably should be solved with some regular expression but spec. for
    // exact format is not probably known
    private static boolean isValidCodeNameBase(String name) {
        if (name.length() == 0) {
            //Fast check of default pkg
            return true;
        }
        StringTokenizer tk = new StringTokenizer(name,".",true); //NOI18N
        boolean delimExpected = false;
        while (tk.hasMoreTokens()) {
            String namePart = tk.nextToken();
            if (!delimExpected) {
                if (namePart.equals(".")) { //NOI18N
                    return false;
                }
                for (int i=0; i< namePart.length(); i++) {
                    char c = namePart.charAt(i);
                    if (i == 0) {
                        if (!Character.isJavaIdentifierStart(c)) {
                            return false;
                        }
                    } else {
                        if (!Character.isJavaIdentifierPart(c)) {
                            return false;
                        }
                    }
                }
            } else {
                if (!namePart.equals(".")) { //NOI18N
                    return false;
                }
            }
            delimExpected = !delimExpected;
        }
        return delimExpected;
    }
    
    public void addNotify() {
        super.addNotify();
        attachDocumentListeners();
    }
    
    public void removeNotify() {
        // prevent checking when the panel is not "active"
        removeDocumentListeners();
        super.removeNotify();
    }
    
    private void attachDocumentListeners() {
        if (!listenersAttached) {
            codeNameBaseValue.getDocument().addDocumentListener(cnbDL);
            bundleValue.getDocument().addDocumentListener(bundleDL);
            layerValue.getDocument().addDocumentListener(layerDL);
            listenersAttached = true;
        }
    }
    
    private void removeDocumentListeners() {
        if (listenersAttached) {
            codeNameBaseValue.getDocument().removeDocumentListener(cnbDL);
            bundleValue.getDocument().removeDocumentListener(bundleDL);
            layerValue.getDocument().removeDocumentListener(layerDL);
            listenersAttached = false;
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

        confPanel = new javax.swing.JPanel();
        codeNameBase = new javax.swing.JLabel();
        displayName = new javax.swing.JLabel();
        bundle = new javax.swing.JLabel();
        layer = new javax.swing.JLabel();
        codeNameBaseValue = new javax.swing.JTextField();
        displayNameValue = new javax.swing.JTextField();
        bundleValue = new javax.swing.JTextField();
        layerValue = new javax.swing.JTextField();
        filler = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        confPanel.setLayout(new java.awt.GridBagLayout());

        codeNameBase.setLabelFor(codeNameBaseValue);
        org.openide.awt.Mnemonics.setLocalizedText(codeNameBase, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_CodeNameBase"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 12);
        confPanel.add(codeNameBase, gridBagConstraints);

        displayName.setLabelFor(displayNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(displayName, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_ModuleDisplayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        confPanel.add(displayName, gridBagConstraints);

        bundle.setLabelFor(bundleValue);
        org.openide.awt.Mnemonics.setLocalizedText(bundle, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_LocalizingBundle"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        confPanel.add(bundle, gridBagConstraints);

        layer.setLabelFor(layerValue);
        org.openide.awt.Mnemonics.setLocalizedText(layer, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_XMLLayer"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        confPanel.add(layer, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 0);
        confPanel.add(codeNameBaseValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        confPanel.add(displayNameValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        confPanel.add(bundleValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        confPanel.add(layerValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        confPanel.add(filler, gridBagConstraints);

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
    // End of variables declaration//GEN-END:variables
    
}
