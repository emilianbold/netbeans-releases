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

package org.netbeans.modules.vmd.componentssupport.ui.wizard;


import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.modules.vmd.componentssupport.ui.UIUtils;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.BaseHelper;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ads
 */
final class BasicModuleConfVisualPanel extends JPanel {

    private static final String BASIC_CONF_ERR_PREFIX 
                                              = "BasicConfVisualPanel_err_";// NOI18N 
    private static final String XML           = ".xml";                    // NOI18N 
    private static final String LAYER         = "layer";                   // NOI18N 
    private static final String PROPS         = ".properties";             // NOI18N 
    private static final String BUNDLE        = "bundle";                  // NOI18N 
    
    private static final String MSG_INVALID_CNB 
                                              = "MSG_InvalidCNB";          // NOI18N 
    private static final String MSG_INVALID_NBORG_CNB 
                                 = "BasicConfVisualPanel_err_wrong_nborg_name"; // NOI18N 
    private static final String ACS_LAYER_VALUE 
                                              = "ACS_CTL_LayerValue";      // NOI18N 
    private static final String ACS_DISPLAY_NAME_VALUE 
                                              = "ACS_CTL_DisplayNameValue";// NOI18N
    private static final String ACS_CODE_NAME_BASE_VALUE 
                                              = "ACS_CTL_CodeNameBaseValue";// NOI18N
    private static final String ACS_BUNDLE_VALUE 
                                              = "ACS_CTL_BundleValue";      // NOI18N
    private static final String ACS_DESC      = "ACS_BasicConfVisualPanel"; // NOI18N

    private static final long serialVersionUID = -7699370587627049750L;
    
    public BasicModuleConfVisualPanel( BasicModuleConfWizardPanel panel) {
        myPanel = panel;
        initComponents();
        initAccessibility();
        myCodeBaseNameListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { 
                updateValuesOnCNBUpdate();
                checkValidity();
                }
        };
        myLayerListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                isLayerUpdated = true;
                checkValidity();
            }
        };
        myBundleListener = new DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { 
                isBundleUpdated = true; 
                checkValidity();
                }
        };
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(BasicModuleConfVisualPanel.class);
    }
    
    private boolean checkValidity(){
        if (!checkCodeNameBase()){
            return false;
        } else if (!checkLayer()){
            return false;
        } else if (!checkBundle()){
            return false;
        }
        markValid();
        return true;
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(
                getMessage(ACS_DESC));
        bundleValue.getAccessibleContext().setAccessibleDescription(
                getMessage(ACS_BUNDLE_VALUE));
        codeNameBaseValue.getAccessibleContext().setAccessibleDescription(
                getMessage(ACS_CODE_NAME_BASE_VALUE));
        displayNameValue.getAccessibleContext().setAccessibleDescription(
                getMessage(ACS_DISPLAY_NAME_VALUE));
        layerValue.getAccessibleContext().setAccessibleDescription(
                getMessage(ACS_LAYER_VALUE));
        
        bundleValue.getAccessibleContext().setAccessibleName(
                getMessage(ACS_BUNDLE_VALUE));
        codeNameBaseValue.getAccessibleContext().setAccessibleName(
                getMessage(ACS_CODE_NAME_BASE_VALUE));
        displayNameValue.getAccessibleContext().setAccessibleName(
                getMessage(ACS_DISPLAY_NAME_VALUE));
        layerValue.getAccessibleContext().setAccessibleName(
                getMessage(ACS_LAYER_VALUE));
    }
    
    private boolean checkCodeNameBase() {
        String dotName = getCodeNameBaseValue();
        if (!UIUtils.isValidJavaFQN(dotName)) {
            setError(getMessage(MSG_INVALID_CNB));
            return false;
        }
        if (BaseHelper.isNetBeansOrg(mySettings)) {
            // Ensure that official naming conventions are respected.
            String cnbShort = abbreviateCNB(dotName);
            String name = (String)mySettings.getProperty( 
                    CustomComponentWizardIterator.PROJECT_NAME);
            if (!name.equals(cnbShort)) {
                setError(getMessage(MSG_INVALID_NBORG_CNB, cnbShort));
                return false;
            }
        }
        return true;
    }
    
    // copied from org.netbeans.modules.apisupport.project.universe.ModuleList
    private static String abbreviateCNB(String cnb) {
        return cnb.replaceFirst("^org\\.netbeans\\.modules\\.", ""). // NOI18N
                   replaceFirst("^org\\.netbeans\\.(libs|lib|api|spi|core)\\.", "$1."). // NOI18N
                   replaceFirst("^org\\.netbeans\\.", "o.n."). // NOI18N
                   replaceFirst("^org\\.openide\\.", "openide."). // NOI18N
                   replaceFirst("^org\\.", "o."). // NOI18N
                   replaceFirst("^com\\.sun\\.", "c.s."). // NOI18N
                   replaceFirst("^com\\.", "c."); // NOI18N
    }

    private void updateValuesOnCNBUpdate(){
        String dotName = getCodeNameBaseValue();
        // update layer and bundle from the cnb
        String slashName = dotName.replace('.', '/');
        if (!isBundleUpdated) {
            bundleValue.setText(
                slashName + "/" + CustomComponentWizardIterator.BUNDLE_PROPERTIES); // NOI18N
            isBundleUpdated = false;
        }
        if (!isLayerUpdated) {
            layerValue.setText(
                slashName + "/" + CustomComponentWizardIterator.LAYER_XML); // NOI18N
            isLayerUpdated = false;
        }
    }
    
    private boolean checkBundle() {
        return checkEntry(getBundleValue(), BUNDLE, PROPS); // NOI18N
    }
    
    private boolean checkLayer() {
        return checkEntry(getLayerValue(), LAYER, XML); // NOI18N
    }
    
    /** Used for Layer and Bundle entries. */
    private boolean checkEntry(String path, String resName, String extension) {
        if (path.length() == 0) {
            setError(NbBundle.getMessage(BasicModuleConfVisualPanel.class, 
                    BASIC_CONF_ERR_PREFIX + resName + "_empty"));
            return false;
        }
        if (path.indexOf('/') == -1) {
            setError(NbBundle.getMessage(BasicModuleConfVisualPanel.class, 
                    BASIC_CONF_ERR_PREFIX + resName + "_def_pkg"));
            return false;
        }
        if (!path.endsWith(extension)) {
            setError(NbBundle.getMessage(BasicModuleConfVisualPanel.class, 
                    BASIC_CONF_ERR_PREFIX + resName + "_ext", extension));
            return false;
        }
        return true;
    }
    
    void refreshData( WizardDescriptor settings) {
        mySettings = settings;
        
        String cnb = getCodeNameBase();
        codeNameBaseValue.setText(cnb);
        if (cnb.startsWith(BaseHelper.EXAMPLE_BASE_NAME)) {
            codeNameBaseValue.select(0, BaseHelper.EXAMPLE_BASE_NAME.length() - 1);
        }
        displayNameValue.setText(getProjectDisplayName());
        
        updateValuesOnCNBUpdate();
        checkValidity();
    }
    
    private String getProjectDisplayName() {
        String projectName = (String)mySettings.getProperty( 
                CustomComponentWizardIterator.PROJECT_NAME);
        String displayName = (String)mySettings.getProperty( 
                CustomComponentWizardIterator.DISPLAY_NAME);
        if ( displayName == null ){
            displayName = projectName;
        }
        return displayName;
    }

    private String getCodeNameBase() {
        String codeBaseName = (String)mySettings.getProperty( 
                CustomComponentWizardIterator.CODE_BASE_NAME);
        String projectName = (String)mySettings.getProperty( 
                CustomComponentWizardIterator.PROJECT_NAME);
        if ( codeBaseName == null ){
            codeBaseName = BaseHelper.getDefaultCodeNameBase(projectName);
        }
        return codeBaseName;
    }

    /** Stores collected data into model. */
    void storeData( WizardDescriptor descriptor ) {
        descriptor.putProperty( CustomComponentWizardIterator.CODE_BASE_NAME, 
                getCodeNameBaseValue() );
        descriptor.putProperty( CustomComponentWizardIterator.DISPLAY_NAME, 
                displayNameValue.getText() );
        descriptor.putProperty( CustomComponentWizardIterator.BUNDLE_PATH, 
                getBundleValue() );
        descriptor.putProperty( CustomComponentWizardIterator.LAYER_PATH, 
                getLayerValue() );
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
    
    protected final void setError(String message) {
        assert message != null;
        setMessage(message);
        setValid(false);
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
            codeNameBaseValue.getDocument().addDocumentListener(
                    myCodeBaseNameListener);
            bundleValue.getDocument().addDocumentListener(myBundleListener);
            layerValue.getDocument().addDocumentListener( myLayerListener );
            listenersAttached = true;
        }
    }
    
    private final void setValid(boolean valid) {
        myPanel.setValid(valid);
    }
    
    private void markValid() {
        setMessage(null);
        setValid(true);
    }
    
    private final void setMessage(String message) {
        mySettings.putProperty(
                CustomComponentWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, 
                message);
    }
    
    private void removeDocumentListeners() {
        if (listenersAttached) {
            codeNameBaseValue.getDocument().removeDocumentListener(myCodeBaseNameListener);
            bundleValue.getDocument().removeDocumentListener(myBundleListener);
            layerValue.getDocument().removeDocumentListener( myLayerListener );
            listenersAttached = false;
        }
    }
    
    private static String getMessage(String key, Object... params) {
        return NbBundle.getMessage(BasicModuleConfVisualPanel.class, key, params);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        generateLayer = new javax.swing.JCheckBox();
        cnbHint = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        confPanel.setLayout(new java.awt.GridBagLayout());

        codeNameBase.setLabelFor(codeNameBaseValue);
        org.openide.awt.Mnemonics.setLocalizedText(codeNameBase, org.openide.util.NbBundle.getMessage(BasicModuleConfVisualPanel.class, "LBL_CodeNameBase")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 6, 12);
        confPanel.add(codeNameBase, gridBagConstraints);

        displayName.setLabelFor(displayNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(displayName, org.openide.util.NbBundle.getMessage(BasicModuleConfVisualPanel.class, "LBL_ModuleDisplayName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 12);
        confPanel.add(displayName, gridBagConstraints);

        bundle.setLabelFor(bundleValue);
        org.openide.awt.Mnemonics.setLocalizedText(bundle, org.openide.util.NbBundle.getMessage(BasicModuleConfVisualPanel.class, "LBL_LocalizingBundle")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 5, 0, 12);
        confPanel.add(bundle, gridBagConstraints);

        layer.setLabelFor(layerValue);
        org.openide.awt.Mnemonics.setLocalizedText(layer, org.openide.util.NbBundle.getMessage(BasicModuleConfVisualPanel.class, "LBL_XMLLayer")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 20, 0, 12);
        confPanel.add(layer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 5, 0);
        confPanel.add(codeNameBaseValue, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        confPanel.add(displayNameValue, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        confPanel.add(bundleValue, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        confPanel.add(layerValue, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        confPanel.add(filler, gridBagConstraints);

        generateLayer.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateLayer, getMessage("CTL_GenerateLayer")); // NOI18N
        generateLayer.setEnabled(false);
        generateLayer.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = -2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        confPanel.add(generateLayer, gridBagConstraints);
        generateLayer.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BasicModuleConfVisualPanel.class, "ACS_CTL_GenerateLayer")); // NOI18N
        generateLayer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BasicModuleConfVisualPanel.class, "ACS_CTL_GenerateLayer")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cnbHint, getMessage("LBL_CodeNameBaseHint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        confPanel.add(cnbHint, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(confPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bundle;
    private javax.swing.JTextField bundleValue;
    private javax.swing.JLabel cnbHint;
    private javax.swing.JLabel codeNameBase;
    private javax.swing.JTextField codeNameBaseValue;
    private javax.swing.JPanel confPanel;
    private javax.swing.JLabel displayName;
    private javax.swing.JTextField displayNameValue;
    private javax.swing.JLabel filler;
    private javax.swing.JCheckBox generateLayer;
    private javax.swing.JLabel layer;
    private javax.swing.JTextField layerValue;
    // End of variables declaration//GEN-END:variables
    
    private boolean isBundleUpdated;
    private boolean isLayerUpdated;
    private boolean listenersAttached;
    
    private final DocumentListener myCodeBaseNameListener;
    private final DocumentListener myLayerListener;
    private final DocumentListener myBundleListener;
    
    private WizardDescriptor mySettings;
    private BasicModuleConfWizardPanel myPanel;
    
}
