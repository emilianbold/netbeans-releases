/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

/*
 * SelectBinaryPanelVisual.java
 *
 * Created on Sep 22, 2010, 12:24:57 PM
 */

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.ui.DocumentAdapter;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class SelectBinaryPanelVisual extends javax.swing.JPanel {
    private final SelectBinaryPanel controller;
    private static final RequestProcessor RP = new RequestProcessor("Binary Artifact Discovery", 1); // NOI18N

    /** Creates new form SelectBinaryPanelVisual */
    public SelectBinaryPanelVisual(SelectBinaryPanel controller) {
        this.controller = controller;
        initComponents();
        addListeners();
    }

    private void addListeners(){
        binaryField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void update(DocumentEvent e) {
                String path = binaryField.getText().trim();
                controller.getWizardStorage().setBinaryPath(path);
                updateRoot();
            }
        });
        updateRoot();
    }

    private void updateRoot(){
        sourcesField.setEnabled(false);
        sourcesButton.setEnabled(false);
        if (valid()) {
            final IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("DW:buildResult", controller.getWizardStorage().getBinaryPath()); // NOI18N
            if (extension != null) {
                RP.post(new Runnable() {

                    @Override
                    public void run() {
                        extension.discoverArtifacts(map);
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                updateArtifacts(map);
                            }
                        });
                    }
                });
            }
        }
    }

    private void updateArtifacts(final Map<String, Object> map){
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                String root = (String) map.get("DW:rootFolder"); // NOI18N
                if (root != null) {
                    sourcesField.setText(root);
                }
                CompilerSet compiler = detectCompilerSet((String) map.get("DW:compiler")); // NOI18N
                if (compiler != null) {
                    controller.getWizardDescriptor().putProperty(NewMakeProjectWizardIterator.PROPERTY_TOOLCHAIN, compiler);
                    controller.getWizardDescriptor().putProperty(NewMakeProjectWizardIterator.PROPERTY_HOST_UID, ExecutionEnvironmentFactory.getLocal().getHost());
                    controller.getWizardDescriptor().putProperty(NewMakeProjectWizardIterator.PROPERTY_READ_ONLY_TOOLCHAIN, Boolean.TRUE);
                } else {
                    controller.getWizardDescriptor().putProperty(NewMakeProjectWizardIterator.PROPERTY_READ_ONLY_TOOLCHAIN, Boolean.FALSE);
                }
                @SuppressWarnings("unchecked")
                List<String> dlls = (List<String>) map.get("DW:dependencies"); // NOI18N
                sourcesField.setEnabled(true);
                sourcesButton.setEnabled(true);
            }
        });
    }

    private CompilerSet detectCompilerSet(String compiler){
        boolean isSunStudio = true;
        if (compiler != null) {
            isSunStudio = compiler.indexOf("Sun") >= 0; // NOI18N
        }
        CompilerSetManager manager = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal());
        if (isSunStudio) {
            CompilerSet def = manager.getDefaultCompilerSet();
            if (def != null && def.getCompilerFlavor().isSunStudioCompiler()) {
                return def;
            }
            def = null;
            for(CompilerSet set : manager.getCompilerSets()) {
                if (set.getCompilerFlavor().isSunStudioCompiler()) {
                    if ("OracleSolarisStudio".equals(set.getName())) { // NOI18N
                        def = set;
                    }
                    if (def == null) {
                        def = set;
                    }
                }
            }
            return def;
        } else {
            CompilerSet def = manager.getDefaultCompilerSet();
            if (def != null && !def.getCompilerFlavor().isSunStudioCompiler()) {
                return def;
            }
            def = null;
            for(CompilerSet set : manager.getCompilerSets()) {
                if (!set.getCompilerFlavor().isSunStudioCompiler()) {
                    if (def == null) {
                        def = set;
                    }
                }
            }
            return def;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        binaryLabel = new javax.swing.JLabel();
        binaryField = new javax.swing.JTextField();
        binaryButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        sourcesLabel = new javax.swing.JLabel();
        sourcesField = new javax.swing.JTextField();
        sourcesButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        binaryLabel.setLabelFor(binaryField);
        org.openide.awt.Mnemonics.setLocalizedText(binaryLabel, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.binaryLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(binaryLabel, gridBagConstraints);

        binaryField.setText(org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.binaryField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(binaryField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(binaryButton, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.binaryButton.text")); // NOI18N
        binaryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                binaryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(binaryButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jSeparator1, gridBagConstraints);

        sourcesLabel.setLabelFor(sourcesField);
        org.openide.awt.Mnemonics.setLocalizedText(sourcesLabel, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.sourcesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(sourcesLabel, gridBagConstraints);

        sourcesField.setText(org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.sourcesField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(sourcesField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(sourcesButton, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.sourcesButton.text")); // NOI18N
        sourcesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourcesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(sourcesButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void binaryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_binaryButtonActionPerformed
        FileFilter[] filters = null;
        if (Utilities.isWindows()) {
            filters = new FileFilter[]{FileFilterFactory.getPeExecutableFileFilter(),
                FileFilterFactory.getElfStaticLibraryFileFilter(),
                FileFilterFactory.getPeDynamicLibraryFileFilter()
            };
        } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            filters = new FileFilter[]{FileFilterFactory.getMacOSXExecutableFileFilter(),
                FileFilterFactory.getElfStaticLibraryFileFilter(),
                FileFilterFactory.getMacOSXDynamicLibraryFileFilter()
            };
        } else {
            filters = new FileFilter[]{FileFilterFactory.getElfExecutableFileFilter(),
                FileFilterFactory.getElfStaticLibraryFileFilter(),
                FileFilterFactory.getElfDynamicLibraryFileFilter()
            };
        }

        JFileChooser fileChooser = new FileChooser(
                getString("SelectBinaryPanelVisual.Browse.Title"), // NOI18N
                getString("SelectBinaryPanelVisual.Browse.Select"), // NOI18N
                JFileChooser.FILES_ONLY,
                filters,
                binaryField.getText(),
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String path = fileChooser.getSelectedFile().getPath();
        binaryField.setText(path);
    }//GEN-LAST:event_binaryButtonActionPerformed

    private void sourcesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourcesButtonActionPerformed
        String seed = sourcesField.getText();
        JFileChooser fileChooser;
        fileChooser = new FileChooser(
                getString("SelectBinaryPanelVisual.Source.Browse.Title"), // NOI18N
                getString("SelectBinaryPanelVisual.Source.Browse.Select"), // NOI18N
                JFileChooser.DIRECTORIES_ONLY,
                null,
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile != null) { // seems paranoidal, but once I've seen NPE otherwise 8-()
            String path = selectedFile.getPath();
            sourcesField.setText(path);
        }
    }//GEN-LAST:event_sourcesButtonActionPerformed

    void read(WizardDescriptor wizardDescriptor) {
    }

    void store(WizardDescriptor wizardDescriptor) {
        wizardDescriptor.putProperty("outputTextField",  binaryField.getText().trim()); // NOI18N
        wizardDescriptor.putProperty("sourceFolderPath",  sourcesField.getText().trim()); // NOI18N
        //wizardDescriptor.putProperty("displayName",   new File(binaryField.getText().trim()).getName()); // NOI18N
        // TODO should be inited
        wizardDescriptor.putProperty(NewMakeProjectWizardIterator.PROPERTY_MAKEFILE_NAME,  ""); // NOI18N
    }

    boolean valid() {
        String path = binaryField.getText().trim();
        if (path.isEmpty()) {
            return false;
        }
        FileObject fo = FileUtil.toFileObject(new File(path));
        if (fo == null) {
            return false;
        }
        return MIMENames.isBinary(fo.getMIMEType());
    }

    private String getString(String key) {
        return NbBundle.getBundle(SelectBinaryPanelVisual.class).getString(key);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton binaryButton;
    private javax.swing.JTextField binaryField;
    private javax.swing.JLabel binaryLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton sourcesButton;
    private javax.swing.JTextField sourcesField;
    private javax.swing.JLabel sourcesLabel;
    // End of variables declaration//GEN-END:variables

}
