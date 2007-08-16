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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

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
final class BasicConfVisualPanel extends BasicVisualPanel.NewTemplatePanel {
    
    static final String EXAMPLE_BASE_NAME = "org.yourorghere."; // NOI18N
    
    private boolean wasLayerUpdated;
    private boolean wasBundleUpdated;
    
    private boolean listenersAttached;
    private final DocumentListener cnbDL;
    private final DocumentListener layerDL;
    private final DocumentListener bundleDL;
    
    public BasicConfVisualPanel(final NewModuleProjectData data) {
        super(data);
        initComponents();
        initAccessibility();
        cnbDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { checkCodeNameBase(); }
        };
        if (isLibraryWizard()) {
            // for library modules, don't generate any layer.
            layer.setVisible(false);
            layerValue.setVisible(false);
            layerDL = null;
        } else {
            layerDL = new UIUtil.DocumentAdapter() {
                public void insertUpdate(DocumentEvent e) { wasLayerUpdated = true; checkLayer(); }
            };
        }
        bundleDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { wasBundleUpdated = true; checkBundle(); }
        };
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_BasicConfVisualPanel"));
        bundleValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_BundleValue"));
        codeNameBaseValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_CodeNameBaseValue"));
        displayNameValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_DisplayNameValue"));
        layerValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_LayerValue"));
    }
    
    private void checkCodeNameBase() {
        if (!Util.isValidJavaFQN(getCodeNameBaseValue())) {
            setError(getMessage("MSG_InvalidCNB"));
        } else if (getData().isSuiteComponent() && cnbIsAlreadyInSuite(getData().getSuiteRoot(), getCodeNameBaseValue())) {
            setError(NbBundle.getMessage(BasicConfVisualPanel.class, "MSG_ComponentWithSuchCNBAlreadyInSuite",
                    getCodeNameBaseValue()));
        } else {
            markValid();
            // update layer and bundle from the cnb
            String dotName = getCodeNameBaseValue();
            String slashName = dotName.replace('.', '/');
            if (!wasBundleUpdated) {
                bundleValue.setText(slashName + "/Bundle.properties"); // NOI18N
                wasBundleUpdated = false;
            }
            if (!wasLayerUpdated && !isLibraryWizard()) {
                layerValue.setText(slashName + "/layer.xml"); // NOI18N
                wasLayerUpdated = false;
            }
        }
    }
    
    private void checkBundle() {
        checkEntry(getBundleValue(), "bundle", ".properties"); // NOI18N
    }
    
    private void checkLayer() {
        checkEntry(getLayerValue(), "layer", ".xml"); // NOI18N
    }
    
    /** Used for Layer and Bundle entries. */
    private void checkEntry(String path, String resName, String extension) {
        if (path.length() == 0) {
            setError(NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel_err_" + resName + "_empty"));
            return;
        }
        if (path.indexOf('/') == -1) {
            setError(NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel_err_" + resName + "_def_pkg"));
            return;
        }
        if (!path.endsWith(extension)) {
            setError(NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel_err_" + resName + "_ext", extension));
            return;
        }
        markValid();
    }
    
    void refreshData() {
        String cnb = getData().getCodeNameBase();
        codeNameBaseValue.setText(cnb);
        if (cnb.startsWith(EXAMPLE_BASE_NAME)) {
            codeNameBaseValue.select(0, EXAMPLE_BASE_NAME.length() - 1);
        }
        String dn = getData().getProjectDisplayName();
        displayNameValue.setText(dn);
        checkCodeNameBase();
    }
    
    /** Stores collected data into model. */
    void storeData() {
        // change will be fired -> update data
        getData().setCodeNameBase(getCodeNameBaseValue());
        getData().setProjectDisplayName(displayNameValue.getText());
        getData().setBundle(getBundleValue());
        if (!isLibraryWizard()) {
            getData().setLayer(getLayerValue());
        }
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
    
    private boolean cnbIsAlreadyInSuite(String suiteDir, String cnb) {
        boolean result = false;
        FileObject suiteDirFO = FileUtil.toFileObject(new File(suiteDir));
        try {
            Project suite = ProjectManager.getDefault().findProject(suiteDirFO);
            for (Iterator it = SuiteUtils.getSubProjects(suite).iterator(); it.hasNext();) {
                Project p = (Project) it.next();
                if (ProjectUtils.getInformation(p).getName().equals(cnb)) {
                    result = true;
                    break;
                }
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return result;
    }
    
    public @Override void addNotify() {
        super.addNotify();
        attachDocumentListeners();
    }
    
    public @Override void removeNotify() {
        // prevent checking when the panel is not "active"
        removeDocumentListeners();
        super.removeNotify();
    }
    
    private void attachDocumentListeners() {
        if (!listenersAttached) {
            codeNameBaseValue.getDocument().addDocumentListener(cnbDL);
            bundleValue.getDocument().addDocumentListener(bundleDL);
            if (!isLibraryWizard()) {
                layerValue.getDocument().addDocumentListener(layerDL);
            }
            listenersAttached = true;
        }
    }
    
    private void removeDocumentListeners() {
        if (listenersAttached) {
            codeNameBaseValue.getDocument().removeDocumentListener(cnbDL);
            bundleValue.getDocument().removeDocumentListener(bundleDL);
            if (!isLibraryWizard()) {
                layerValue.getDocument().removeDocumentListener(layerDL);
            }
            listenersAttached = false;
        }
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(BasicConfVisualPanel.class, key);
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
