/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
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
    
    private boolean wasBundleUpdated;
    private boolean wasLayerUpdated;
    
    private boolean listenersAttached;
    private final DocumentListener cnbDL;
    private final DocumentListener layerDL;
    private final DocumentListener bundleDL;
    private final ActionListener layerAL;
    
    public BasicConfVisualPanel(final NewModuleProjectData data) {
        super(data);
        initComponents();
        initAccessibility();
        cnbDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { 
                checkValues(true, false, false); 
            }
        };
        if (isLibraryWizard()) {
            // for library modules, don't generate any layer.
            layer.setVisible(false);
            layerValue.setVisible(false);
            generateLayer.setVisible(false);
            layerDL = null;
            layerAL = null;
            // We do not intend to support OSGi-style lib wrappers.
            // These would need to use Bundle-ClassPath etc.
            osgi.setVisible(false);
        } else {
            layerDL = new UIUtil.DocumentAdapter() {
                public void insertUpdate(DocumentEvent e) {
                    wasLayerUpdated = true;
                    checkValues(false, false, true);
                }
            };
            layerAL = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    checkValues(false, false, true);
                }
            };
            
        }
        bundleDL = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) { 
                wasBundleUpdated = true; 
                checkValues(false, true, false); 
            }
        };
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_BasicConfVisualPanel"));
        bundleValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_BundleValue"));
        codeNameBaseValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_CodeNameBaseValue"));
        displayNameValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_DisplayNameValue"));
        layerValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_LayerValue"));
        generateLayer.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_GenerateLayer"));
    }
    
    private boolean checkCodeNameBase() {
        String dotName = getCodeNameBaseValue();
        if (dotName.length() == 0) {
            setInfo(getMessage("MSG_EmptyCNB"), false);
        } else if (!Util.isValidJavaFQN(dotName)) {
            setError(getMessage("MSG_InvalidCNB"));
        } else if (getData().isSuiteComponent() && cnbIsAlreadyInSuite(getData().getSuiteRoot(), dotName)) {
            setError(NbBundle.getMessage(BasicConfVisualPanel.class, "MSG_ComponentWithSuchCNBAlreadyInSuite", dotName));
        } else {
            // update layer and bundle from the cnb
            String slashName = dotName.replace('.', '/');
            if (! wasBundleUpdated) {
                bundleValue.setText(slashName + "/Bundle.properties"); // NOI18N
                wasBundleUpdated = false;
            }
            if (! wasLayerUpdated) {
                layerValue.setText(slashName + "/layer.xml"); // NOI18N
                wasLayerUpdated = false;
            }
            if (getData().isNetBeansOrg()) {
                // Ensure that official naming conventions are respected.
                String cnbShort = ModuleList.abbreviate(dotName);
                String name = getData().getProjectName();
                if (!name.equals(cnbShort)) {
                    setError(NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel_err_wrong_nborg_name", cnbShort));
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean checkBundle() {
        return checkEntry(getBundleValue(), "bundle", ".properties"); // NOI18N
    }
    
    private boolean checkLayer() {
        String layerPath = getLayerValue();
        layerValue.setEnabled(layerPath != null);
        return (layerPath != null) ?
            checkEntry(layerPath, "layer", ".xml") // NOI18N
            : true;
    }
    
    /** Used for Layer and Bundle entries. */
    private boolean checkEntry(String path, String resName, String extension) {
        if (path.length() == 0) {
            setError(NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel_err_" + resName + "_empty"));
            return false;
        }
        if (path.indexOf('/') == -1) {
            setError(NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel_err_" + resName + "_def_pkg"));
            return false;
        }
        if (!path.endsWith(extension)) {
            setError(NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel_err_" + resName + "_ext", extension));
            return false;
        }
        return true;
    }
    
    private void checkValues(boolean preferCNB, boolean preferBundle, boolean preferLayer) {
        boolean valid = true;
        if (preferCNB && ! checkCodeNameBase())
            return; // invalid CNB
        if (preferBundle && ! checkBundle())
            return; // invalid Bundle
        if (preferLayer && ! checkLayer())
            return; // invalid layer
        if (! preferCNB && ! checkCodeNameBase())
            return;  // invalid CNB
        if (! preferBundle && ! checkBundle())
            return; // invalid Bundle
        if (! preferLayer && ! checkLayer())
            return; // invalid layer
        // all valid
        markValid();
    }
    
    void refreshData() {
        String dn = getData().getProjectDisplayName();
        displayNameValue.setText(dn);
        checkValues(true, false, false);
    }
    
    /** Stores collected data into model. */
    void storeData() {
        // change will be fired -> update data
        getData().setCodeNameBase(getCodeNameBaseValue());
        getData().setProjectDisplayName(displayNameValue.getText());
        getData().setBundle(getBundleValue());
        getData().setLayer(getLayerValue());
        getData().setOsgi(osgi.isSelected());
    }
    
    private String getCodeNameBaseValue() {
        return codeNameBaseValue.getText().trim();
    }
    
    private String getBundleValue() {
        return bundleValue.getText().trim();
    }
    
    private String getLayerValue() {
        String v = layerValue.getText().trim();
        return generateLayer.isSelected() ? v : null;
    }
    
    private boolean cnbIsAlreadyInSuite(String suiteDir, String cnb) {
        FileObject suiteDirFO = FileUtil.toFileObject(new File(suiteDir));
        try {
            Project suite = ProjectManager.getDefault().findProject(suiteDirFO);
            if (suite == null) { // #180644
                return false;
            }
            for (Project p : SuiteUtils.getSubProjects(suite)) {
                if (ProjectUtils.getInformation(p).getName().equals(cnb)) {
                    return true;
                }
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return false;
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
                generateLayer.addActionListener(layerAL);
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
                generateLayer.removeActionListener(layerAL);
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
        cnbHint = new javax.swing.JLabel();
        generateLayer = new javax.swing.JCheckBox();
        osgi = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        confPanel.setLayout(new java.awt.GridBagLayout());

        codeNameBase.setLabelFor(codeNameBaseValue);
        org.openide.awt.Mnemonics.setLocalizedText(codeNameBase, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_CodeNameBase")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 6, 12);
        confPanel.add(codeNameBase, gridBagConstraints);

        displayName.setLabelFor(displayNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(displayName, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_ModuleDisplayName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 12);
        confPanel.add(displayName, gridBagConstraints);

        bundle.setLabelFor(bundleValue);
        org.openide.awt.Mnemonics.setLocalizedText(bundle, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_LocalizingBundle")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 5, 0, 12);
        confPanel.add(bundle, gridBagConstraints);

        layer.setLabelFor(layerValue);
        org.openide.awt.Mnemonics.setLocalizedText(layer, org.openide.util.NbBundle.getMessage(BasicConfVisualPanel.class, "LBL_XMLLayer")); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 4, 0);
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
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        confPanel.add(layerValue, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        confPanel.add(filler, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cnbHint, getMessage("LBL_CodeNameBaseHint"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        confPanel.add(cnbHint, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(generateLayer, getMessage("CTL_GenerateLayer")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = -2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        confPanel.add(generateLayer, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(osgi, NbBundle.getMessage(BasicConfVisualPanel.class, "BasicConfVisualPanel.osgi")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        confPanel.add(osgi, gridBagConstraints);

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
    private javax.swing.JCheckBox osgi;
    // End of variables declaration//GEN-END:variables
    
}
