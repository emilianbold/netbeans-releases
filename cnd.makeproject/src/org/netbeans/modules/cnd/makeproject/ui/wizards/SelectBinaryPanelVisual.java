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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.ui.DocumentAdapter;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
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
    private final AtomicInteger checking = new AtomicInteger(0);
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.cnd.discovery.projectimport.ImportExecutable"); // NOI18N
    private DefaultTableModel tableModel;


    /** Creates new form SelectBinaryPanelVisual */
    public SelectBinaryPanelVisual(SelectBinaryPanel controller) {
        this.controller = controller;
        initComponents();
        dependeciesComboBox.removeAllItems();
        dependeciesComboBox.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.Minimal));
        dependeciesComboBox.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.IncludeDependencies));
        dependeciesComboBox.addItem(new ProjectKindItem(IteratorExtension.ProjectKind.CreateDependencies));
        dependeciesComboBox.setSelectedIndex(1);
        viewComboBox.removeAllItems();
        viewComboBox.addItem(new ProjectView(false));
        viewComboBox.addItem(new ProjectView(true));
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
        sourcesField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void update(DocumentEvent e) {
                String path = sourcesField.getText().trim();
                controller.getWizardStorage().setSourceFolderPath(path);
            }
        });
        updateRoot();
    }

    private void updateRoot(){
        sourcesField.setEnabled(false);
        sourcesButton.setEnabled(false);
        dependeciesComboBox.setEnabled(false);
        viewComboBox.setEnabled(false);
        table.setModel(new DefaultTableModel(0, 0));
        if (validBinary()) {
            checking.incrementAndGet();
            controller.getWizardStorage().validate();
            final IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("DW:buildResult", controller.getWizardStorage().getBinaryPath()); // NOI18N
            if (extension != null) {
                RP.post(new Runnable() {

                    @Override
                    public void run() {
                        extension.discoverArtifacts(map);
                        @SuppressWarnings("unchecked")
                        List<String> dlls = (List<String>) map.get("DW:dependencies"); // NOI18N
                        String root = (String) map.get("DW:rootFolder"); // NOI18N
                        if (root == null) {
                            root = "";
                        }
                        final Map<String, String> checkDll = checkDll(dlls, root);
                        updateArtifacts(root, map, checkDll);
                    }
                });
            }
        }
    }

    private void updateArtifacts(final String root, final Map<String, Object> map, final Map<String, String> checkDll){
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                CompilerSet compiler = detectCompilerSet((String) map.get("DW:compiler")); // NOI18N
                if (compiler != null) {
                    controller.getWizardDescriptor().putProperty(WizardConstants.PROPERTY_TOOLCHAIN, compiler);
                    controller.getWizardDescriptor().putProperty(WizardConstants.PROPERTY_HOST_UID, ExecutionEnvironmentFactory.getLocal().getHost());
                    controller.getWizardDescriptor().putProperty(WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN, Boolean.TRUE);
                } else {
                    controller.getWizardDescriptor().putProperty(WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN, Boolean.FALSE);
                }
                sourcesField.setText(root);
                int i = checking.decrementAndGet();
                controller.getWizardStorage().validate();
                if (i == 0) {
                    boolean validBinary = validBinary();
                    sourcesField.setEnabled(validBinary);
                    sourcesButton.setEnabled(validBinary);
                    dependeciesComboBox.setEnabled(validBinary);
                    viewComboBox.setEnabled(validBinary);
                    if (validBinary) {
                        updateTableModel(checkDll, root);
                    } else {
                        updateTableModel(Collections.<String, String>emptyMap(), root);
                    }
                }
            }
        });
    }

    private void updateTableModel(Map<String, String> dlls, String root) {
        tableModel = new MyDefaultTableModel(dlls, root);
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        table.getColumnModel().getColumn(0).setMinWidth(15);
        table.getColumnModel().getColumn(0).setCellRenderer(new MyTableCellRenderer());
        table.getColumnModel().getColumn(0).setCellEditor(new MyTableCellEditor());
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setMinWidth(50);
        if (table.getWidth() > 200) {
            table.getColumnModel().getColumn(2).setPreferredWidth(table.getWidth()-100);
        } else {
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
        }
    }

    private Map<String,String> checkDll(List<String> dlls, String root){
        Map<String,String> dllPaths = new TreeMap<String, String>();
        if (validBinary()) {
            String ldLibPath = getLdLibraryPath();
            boolean search = false;
            for(String dll : dlls) {
                String p = findLocation(dll, ldLibPath);
                if (p != null) {
                    dllPaths.put(dll, p);
                } else {
                    search = true;
                    dllPaths.put(dll, null);
                }
            }
            if (search && root.length() > 0) {
                gatherSubFolders(new File(root), new HashSet<String>(), dllPaths);
            }
        }
        return dllPaths;
    }

    private void gatherSubFolders(File d, HashSet<String> set, Map<String,String> result){
        if (d.exists() && d.isDirectory() && d.canRead()){
            String path = d.getAbsolutePath();
            path = path.replace('\\', '/'); // NOI18N
            if (!set.contains(path)){
                set.add(path);
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    try {
                        String canPath = ff[i].getCanonicalPath();
                        String absPath = ff[i].getAbsolutePath();
                        if (!absPath.equals(canPath) && absPath.startsWith(canPath)) {
                            continue;
                        }
                    } catch (IOException ex) {
                        //Exceptions.printStackTrace(ex);
                    }
                    String name = ff[i].getName();
                    if (result.containsKey(name)) {
                       result.put(name, ff[i].getAbsolutePath());
                    }
                    gatherSubFolders(ff[i], set, result);
                }
            }
        }
    }

    private String findLocation(String dll, String ldPath){
        if (ldPath != null) {
            for(String search :  ldPath.split(":")) {  // NOI18N
                File file = new File(search, dll);
                if (file.isFile() && file.exists()) {
                    String path = file.getAbsolutePath();
                    return path.replace('\\', '/');
                }
            }
        }
        return null;
    }

    private String getLdLibraryPath() {
        ExecutionEnvironment eenv = ExecutionEnvironmentFactory.getLocal();
        String ldLibraryPathName = getLdLibraryPathName(eenv);
        return HostInfoProvider.getEnv(eenv).get(ldLibraryPathName);
    }

    private static String getLdLibraryPathName(ExecutionEnvironment eenv) {
        PlatformInfo platformInfo = PlatformInfo.getDefault(eenv);
        switch (platformInfo.getPlatform()) {
            case PlatformTypes.PLATFORM_WINDOWS:
                return platformInfo.getPathName();
            case PlatformTypes.PLATFORM_MACOSX:
                return "DYLD_LIBRARY_PATH"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
            case PlatformTypes.PLATFORM_LINUX:
            default:
                return "LD_LIBRARY_PATH"; // NOI18N
        }
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
        dependenciesLabel = new javax.swing.JLabel();
        dependeciesComboBox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        viewLabel = new javax.swing.JLabel();
        viewComboBox = new javax.swing.JComboBox();

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

        dependenciesLabel.setLabelFor(dependeciesComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(dependenciesLabel, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.dependenciesLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(dependenciesLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(dependeciesComboBox, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 200));

        table.setModel(new DefaultTableModel());
        jScrollPane1.setViewportView(table);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(jScrollPane1, gridBagConstraints);

        viewLabel.setLabelFor(viewComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(viewLabel, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.viewLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(viewLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(viewComboBox, gridBagConstraints);
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

        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                controller.getWizardDescriptor(),
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
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_BUILD_RESULT,  binaryField.getText().trim());
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_PREFERED_PROJECT_NAME,   new File(binaryField.getText().trim()).getName());
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_SOURCE_FOLDER_PATH,  sourcesField.getText().trim());
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_DEPENDENCY_KIND,  ((ProjectKindItem)dependeciesComboBox.getSelectedItem()).kind);
        ArrayList<String> dlls = new ArrayList<String>();
        for(int i = 0; i < table.getModel().getRowCount(); i++) {
            if ((Boolean)table.getModel().getValueAt(i, 0)){
                dlls.add((String)table.getModel().getValueAt(i, 2));
            }
        }
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_DEPENDENCIES,  dlls);
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_TRUE_SOURCE_ROOT,  ((ProjectView)viewComboBox.getSelectedItem()).isSourceRoot);
        // TODO should be inited
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH,  ""); // NOI18N
    }

    boolean valid() {
        return checking.get()==0 && validBinary() && validSourceRoot();
    }

    private boolean validBinary() {
        String path = binaryField.getText().trim();
        if (path.isEmpty()) {
            return false;
        }
        FileObject fo = CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(path));
        if (fo == null || !fo.isValid()) {
            return false;
        }
        return MIMENames.isBinary(fo.getMIMEType());
    }

    private boolean validSourceRoot() {
        String path = sourcesField.getText().trim();
        if (path.isEmpty()) {
            return false;
        }
        FileObject fo = CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(path));
        if (fo == null || !fo.isValid()) {
            return false;
        }
        return fo.isFolder();
    }

    private static String getString(String key) {
        return NbBundle.getBundle(SelectBinaryPanelVisual.class).getString(key);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton binaryButton;
    private javax.swing.JTextField binaryField;
    private javax.swing.JLabel binaryLabel;
    private javax.swing.JComboBox dependeciesComboBox;
    private javax.swing.JLabel dependenciesLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton sourcesButton;
    private javax.swing.JTextField sourcesField;
    private javax.swing.JLabel sourcesLabel;
    private javax.swing.JTable table;
    private javax.swing.JComboBox viewComboBox;
    private javax.swing.JLabel viewLabel;
    // End of variables declaration//GEN-END:variables

    private final class ProjectKindItem {
        private final IteratorExtension.ProjectKind kind;
        ProjectKindItem(IteratorExtension.ProjectKind kind) {
            this.kind = kind;
        }

        @Override
        public String toString() {
            return getString("ProjectItemKind_"+kind);
        }
    }

    private final class ProjectView {
        private boolean isSourceRoot;
        ProjectView(boolean isSourceRoot) {
            this.isSourceRoot = isSourceRoot;
        }

        @Override
        public String toString() {
            if (isSourceRoot) {
                return getString("ProjectViewSource");
            } else {
                return getString("ProjectViewLogical");
            }
        }
    }

    private static class MyTableCellRenderer extends JCheckBox implements TableCellRenderer {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private final JLabel emptyLabel = new JLabel();

	public MyTableCellRenderer() {
	    super();
	    setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
            emptyLabel.setBorder(noFocusBorder);
            emptyLabel.setOpaque(true);
	}

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JComponent result;
            if (value == null) {
                result = emptyLabel;
            } else {
                setSelected(((Boolean)value).booleanValue());
                setEnabled(table.getModel().isCellEditable(row, column));
                result = this;
            }
            result.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            result.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            result.setBorder(hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder); // NOI18N
            return result;
        }
    }

    private static class MyTableCellEditor extends DefaultCellEditor {

        private MyTableCellEditor() {
            super(new JCheckBox());
	    ((JCheckBox)getEditorComponent()).setHorizontalAlignment(JLabel.CENTER);
            ((JCheckBox)getEditorComponent()).setBorderPainted(true);
        }

        public final JComponent getEditorComponent() {
            return editorComponent;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }

    private static boolean isMyDll(String path, String root) {
        if (path.startsWith(root)) {
            return true;
        } else {
            String[] p1 = path.replace('\\','/').split("/");  // NOI18N
            String[] p2 = root.replace('\\','/').split("/");  // NOI18N
            for(int i = 0; i < Math.min(p1.length - 1, p2.length); i++) {
                if (!p1[i].equals(p2[i])) {
                    if (i > 2) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private static class MyDefaultTableModel extends DefaultTableModel {
        private List<Boolean> inited = new ArrayList<Boolean>();
        private List<Boolean> uses = new ArrayList<Boolean>();
        private List<String> names = new ArrayList<String>();
        private List<String> paths = new ArrayList<String>();
        private MyDefaultTableModel(Map<String, String> dlls, String root){
            super(new String[] {
                SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col0"),
                SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col1"),
                SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col2"),
            }, 0);
            for(Map.Entry<String,String> entry : dlls.entrySet()) {
                String dll = entry.getKey();
                names.add(dll);
                String path = entry.getValue();
                if (path == null) {
                    inited.add(Boolean.FALSE);
                    uses.add(Boolean.FALSE);
                    paths.add(SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col.notfound"));
                } else {
                    inited.add(Boolean.TRUE);
                    if (isMyDll(path, root)) {
                        uses.add(Boolean.TRUE);
                    } else {
                        uses.add(Boolean.FALSE);
                    }
                    paths.add(path);
                }
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch(column) {
                case 0: return uses.get(row);
                case 1: return names.get(row);
                case 2: return paths.get(row);
            }
            return super.getValueAt(row, column);
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            switch(column) {
                case 0:
                    uses.set(row, (Boolean)value);
                    return;
                case 1:
                    names.set(row, (String)value);
                    return;
                case 2:
                    paths.set(row, (String)value);
                    return;
            }
            super.setValueAt(value, row, column);
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch(column) {
                case 0: return Boolean.class;
                case 1: return String.class;
                case 2: return String.class;
            }
            return super.getColumnClass(column);
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            if (uses == null) {
                return 0;
            } else {
                return uses.size();
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 1) {
                return false;
            } else {
                if (inited.get(row)) {
                    return true;
                }
            }
            return false;
        }
    }
}
