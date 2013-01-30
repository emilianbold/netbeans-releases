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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.wizards.CommonUtilities;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.ui.DocumentAdapter;
import org.netbeans.modules.cnd.utils.ui.EditableComboBox;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

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
    private static final String BINARY_FILE_KEY = "binaryField"; // NOI18N
    private final List<AtomicBoolean> cancelable = new ArrayList<AtomicBoolean>();
    private static final class Lock {}
    private final Object lock = new Lock();
    private final AtomicBoolean searching = new AtomicBoolean(false);
    private ExecutionEnvironment env;
    private FileSystem fileSystem;

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
        ((EditableComboBox)binaryField).addChangeListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = ((EditableComboBox)binaryField).getText().trim();
                controller.getWizardStorage().setBinaryPath(new FSPath(fileSystem, path));
                updateRoot();
            }
        });
        sourcesField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void update(DocumentEvent e) {
                String path = sourcesField.getText().trim();
                controller.getWizardStorage().setSourceFolderPath(new FSPath(fileSystem, path));
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedLine = table.rowAtPoint(e.getPoint());

                if (clickedLine != -1) {
                    if ((e.getModifiers() == InputEvent.BUTTON1_MASK)){
                        if (e.getClickCount() == 1){
                            onClickAction(e);
                        }
                    }
                }
            }
        });
        dependeciesComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                validateController();
            }
        });
        updateRoot();
    }

    private void validateController() {
        controller.getWizardStorage().validate();
    }

    private FileObject findProjectCreator() {
        for(CompilerSet set : CompilerSetManager.get(env).getCompilerSets()) {
            if (set.getCompilerFlavor().isSunStudioCompiler()) {
                String directory = set.getDirectory();
                FileObject creator = fileSystem.findResource(directory+"/../lib/ide_project/bin/ide_project");
                if (creator != null && creator.isValid()) {
                    return creator;
                }
            }
        }
        return null;
    }

    private void updateRoot(){
        sourcesField.setEnabled(false);
        sourcesButton.setEnabled(false);
        dependeciesComboBox.setEnabled(false);
        viewComboBox.setEnabled(false);
        table.setModel(new DefaultTableModel(0, 0));
        if (validBinary()) {
            if (env.isRemote() && findProjectCreator() == null) {
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("ERROR_FIND_PROJECT_CREATOR", env.getDisplayName()));  // NOI18N
                return;
            }
            controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "");
            checking.incrementAndGet();
            validateController();
            final IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("DW:buildResult", controller.getWizardStorage().getBinaryPath().getPath()); // NOI18N
            if (env.isRemote()) {
                map.put("DW:fileSystem", fileSystem); // NOI18N
            }
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
                        @SuppressWarnings("unchecked")
                        List<String> searchPaths = (List<String>) map.get("DW:searchPaths"); // NOI18N
                        final Map<String, String> resolvedDlls = searchingTable(dlls);
                        updateArtifacts(root, map, resolvedDlls);
                        checkDll(resolvedDlls, root, searchPaths, controller.getWizardStorage().getBinaryPath());
                    }
                });
            }
        } else {
            String path = ((EditableComboBox)binaryField).getText().trim();
            if (!path.isEmpty() && controller.getWizardDescriptor() != null) {
                if (CndPathUtilitities.isPathAbsolute(path)) {
                    FileObject fo = CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(path));
                    if (fo == null || !fo.isValid()) {
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("SelectBinaryPanelVisual.FileNotFound"));  // NOI18N
                    } else {
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("SelectBinaryPanelVisual.Unsupported.Binary"));  // NOI18N
                    }
                } else {
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, getString("SelectBinaryPanelVisual.FileNotFound"));  // NOI18N
                }
            }
        }
    }

    private void updateArtifacts(final String root, final Map<String, Object> map, final Map<String, String> dlls){
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (env.isLocal()) {
                    CompilerSet compiler = detectCompilerSet((String) map.get("DW:compiler")); // NOI18N
                    if (compiler != null) {
                        controller.getWizardDescriptor().putProperty(WizardConstants.PROPERTY_TOOLCHAIN, compiler);
                        controller.getWizardDescriptor().putProperty(WizardConstants.PROPERTY_HOST_UID, ExecutionEnvironmentFactory.getLocal().getHost());
                        // allow user to select right tool collection if discovery detected wrong one
                        controller.getWizardDescriptor().putProperty(WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN, Boolean.FALSE);
                    } else {
                        controller.getWizardDescriptor().putProperty(WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN, Boolean.FALSE);
                    }
                    sourcesField.setText(root);
                    int i = checking.decrementAndGet();
                    if (i == 0) {
                        boolean validBinary = validBinary();
                        String validBinaryPath = getValidBinaryPath();
                        sourcesField.setEnabled(validBinary);
                        sourcesButton.setEnabled(validBinary);
                        dependeciesComboBox.setEnabled(validBinary);
                        viewComboBox.setEnabled(validBinary);
                        if (validBinary && validBinaryPath != null) {
                            String binaryRoot = CndPathUtilitities.getDirName(validBinaryPath);
                            if (binaryRoot != null) {
                                if (binaryRoot.startsWith(root) || root.startsWith(binaryRoot)) {
                                    binaryRoot = null;
                                }
                            }
                            updateTableModel(dlls, root, binaryRoot, true);
                        } else {
                            updateTableModel(Collections.<String, String>emptyMap(), root, null, true);
                        }
                    }
                    @SuppressWarnings("unchecked")
                    List<String> errors = (List<String>) map.get("DW:errors"); // NOI18N
                    if (errors != null && errors.size() > 0) {
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errors.get(0));
                    } else {
                        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "");
                    }
                } else {
                    sourcesField.setText(root);
                    int i = checking.decrementAndGet();
                    if (i == 0) {
                        boolean validBinary = validBinary();
                        String validBinaryPath = getValidBinaryPath();
                        sourcesField.setEnabled(validBinary);
                        sourcesButton.setEnabled(validBinary);
                        dependeciesComboBox.setEnabled(false);
                        viewComboBox.setEnabled(validBinary);
                        if (validBinary && validBinaryPath != null) {
                            String binaryRoot = CndPathUtilitities.getDirName(validBinaryPath);
                            if (binaryRoot != null) {
                                if (binaryRoot.startsWith(root) || root.startsWith(binaryRoot)) {
                                    binaryRoot = null;
                                }
                            }
                            updateTableModel(dlls, root, binaryRoot, true);
                        } else {
                            updateTableModel(Collections.<String, String>emptyMap(), root, null, true);
                        }
                    }
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "");
                }
                validateController();
            }
        });
    }

    private void updateDllArtifacts(final String root, final Map<String, String> checkDll, final boolean searching){
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                int i = checking.get();
                if (i == 0) {
                    boolean validBinary = validBinary();
                    String validBinaryPath = getValidBinaryPath();
                    if (validBinary && validBinaryPath != null) {
                        String binaryRoot = CndPathUtilitities.getDirName(validBinaryPath);
                        if (binaryRoot != null) {
                            if (binaryRoot.startsWith(root) || root.startsWith(binaryRoot)) {
                                binaryRoot = null;
                            }
                        }
                        updateTableModel(checkDll, root, binaryRoot, searching);
                    } else {
                        updateTableModel(Collections.<String, String>emptyMap(), root, null, searching);
                    }
                }
                validateController();
            }
        });
    }

    private void updateTableModel(Map<String, String> dlls, String root, String binaryRoot, boolean searching) {
        tableModel = new MyDefaultTableModel(this, dlls, root, binaryRoot, searching);
        table.setModel(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        table.getColumnModel().getColumn(0).setMinWidth(15);
        table.getColumnModel().getColumn(0).setCellRenderer(new CheckBoxCellRenderer());
        table.getColumnModel().getColumn(0).setCellEditor(new CheckBoxTableCellEditor());
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setMinWidth(50);
        if (table.getWidth() > 200) {
            table.getColumnModel().getColumn(2).setPreferredWidth(table.getWidth()-100);
        } else {
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
        }
        table.getColumnModel().getColumn(2).setCellRenderer(new PathCellRenderer(fileSystem));
    }

    private void cancelSearch() {
        for(AtomicBoolean cancel : cancelable) {
            cancel.set(true);
        }
    }

    private Map<String,String> searchingTable(List<String> dlls) {
        Map<String,String> dllPaths = new TreeMap<String, String>();
        if (dlls != null) {
            for(String dll : dlls) {
               dllPaths.put(dll, null);
            }
        }
        return dllPaths; 
    }

    private void checkDll(Map<String, String> dllPaths, String root, List<String> searchPaths, FSPath binary) {
        cancelSearch();
        if (validBinary()) {
            searching.set(true);
            validateController();
            synchronized (lock) {
                final AtomicBoolean cancel = new AtomicBoolean(false);
                cancelable.add(cancel);
                ActionListener actionListener = new ActionListener(){
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         cancel.set(true);
                     }
                 };
                cancelSearch.addActionListener(actionListener);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        cancelSearch.setEnabled(true);
                    }
                });
                processDlls(searchPaths, binary, dllPaths, cancel, root);
                cancelSearch.removeActionListener(actionListener);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        cancelSearch.setEnabled(false);
                    }
                });
            }
            searching.set(false);
            validateController();
        }
    }
    
    private void processDlls(List<String> searchPaths, FSPath binary, Map<String, String> dllPaths, final AtomicBoolean cancel, String root) {
        Set<String> checkedDll = new HashSet<String>();
        checkedDll.add(binary.getPath());
        String ldLibPath = CommonUtilities.getLdLibraryPath(env);
        ldLibPath = CommonUtilities.addSearchPaths(ldLibPath, searchPaths, binary.getPath());
        for(String dll : dllPaths.keySet()) {
            if (cancel.get()) {
                break;
            }
            String p = findLocation(dll, ldLibPath);
            if (p != null) {
                dllPaths.put(dll, p);
            } else {
                dllPaths.put(dll, null);
            }
        }
        while(true) {
            List<String> secondary = new ArrayList<String>();
            for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                if (cancel.get()) {
                    break;
                }
                if (entry.getValue() != null) {
                    if (!checkedDll.contains(entry.getValue())) {
                        checkedDll.add(entry.getValue());
                        final IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
                        final Map<String, Object> map = new HashMap<String, Object>();
                        map.put("DW:buildResult", entry.getValue()); // NOI18N
                        if (env.isRemote()) {
                            map.put("DW:fileSystem", fileSystem); // NOI18N
                        }
                        if (extension != null) {
                            extension.discoverArtifacts(map);
                            @SuppressWarnings("unchecked")
                            List<String> dlls = (List<String>) map.get("DW:dependencies"); // NOI18N
                            if (dlls != null) {
                                for(String so : dlls) {
                                    if (!dllPaths.containsKey(so)) {
                                        secondary.add(so);
                                    }
                                }
                                //@SuppressWarnings("unchecked")
                                //List<String> searchPaths = (List<String>) map.get("DW:searchPaths"); // NOI18N
                            }
                        }
                    }
                }
            }
            for(String so : secondary) {
                if (cancel.get()) {
                    break;
                }
                dllPaths.put(so, findLocation(so, ldLibPath));
            }
            int search = 0;
            for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                if (entry.getValue() == null) {
                    search++;
                }
            }
            updateDllArtifacts(root, dllPaths, search > 0);
            if (!cancel.get() && search > 0 && root.length() > 1) {
                ProgressHandle progress = ProgressHandleFactory.createHandle(getString("SearchForUnresolvedDLL")); //NOI18N
                progress.start();
                try {
                    gatherSubFolders(fileSystem.findResource(root), new HashSet<String>(), dllPaths, cancel);
                } finally {
                    progress.finish();
                }
                updateDllArtifacts(root, dllPaths, false);
            }
            int newSearch = 0;
            for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                if (entry.getValue() == null) {
                    newSearch++;
                }
            }
            if (newSearch == search && secondary.isEmpty()) {
                break;
            }
        }
    }

    private void gatherSubFolders(FileObject d, HashSet<String> set, Map<String,String> result, AtomicBoolean cancel){
        if (cancel.get()) {
            return;
        }
        if (d != null && d.isFolder() && d.canRead()){
            //String path = d.getPath();
            String path;
            try {
                path = FileSystemProvider.getCanonicalPath(d);
            } catch (IOException ex) {
                return;
            }
            path = path.replace('\\', '/'); // NOI18N
            if (!set.contains(path)){
                set.add(path);
                FileObject[] ff = d.getChildren();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (cancel.get()) {
                            return;
                        }
                        String ffPath = ff[i].getPath();
                        if (set.contains(ffPath)){
                            continue;
                        }
                        String name = ff[i].getNameExt();
                        if (result.containsKey(name)) {
                           result.put(name, ffPath);
                            boolean finished = true;
                            for (Map.Entry<String,String> entry : result.entrySet()) {
                                if (entry.getValue() == null) {
                                    finished = false;
                                    break;
                                }
                            }
                            if (finished) {
                                return;
                            }
                        }
                        gatherSubFolders(ff[i], set, result, cancel);
                    }
                }
            }
        }
    }

    private String findLocation(String dll, String ldPath){
        if (ldPath != null) {
            String pathSepararor = ":"; // NOI18N
            if (ldPath.indexOf(';')>0) {
                pathSepararor = ";"; // NOI18N
            }
            for(String search :  ldPath.split(pathSepararor)) {  // NOI18N
                FileObject file = fileSystem.findResource(search+"/"+dll);
                if (file != null && file.isValid() && file.isData()) {
                    String path = file.getPath();
                    return path.replace('\\', '/');
                }
            }
        }
        return null;
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
        binaryField = new EditableComboBox();
        cancelSearch = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.GridBagLayout());

        binaryLabel.setLabelFor(binaryField);
        org.openide.awt.Mnemonics.setLocalizedText(binaryLabel, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.binaryLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(binaryLabel, gridBagConstraints);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(binaryField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cancelSearch, org.openide.util.NbBundle.getMessage(SelectBinaryPanelVisual.class, "SelectBinaryPanelVisual.cancelSearch.text")); // NOI18N
        cancelSearch.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        add(cancelSearch, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void binaryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_binaryButtonActionPerformed
        String path = selectBinaryFile(((EditableComboBox)binaryField).getText());
        if (path == null) {
            return;
        }
        ((EditableComboBox)binaryField).setText(path);
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
        env = (ExecutionEnvironment) wizardDescriptor.getProperty(WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV);
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        } else {
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_HOST_UID, ExecutionEnvironmentFactory.toUniqueID(env));
        }
        fileSystem = FileSystemProvider.getFileSystem(env);

        ((EditableComboBox)binaryField).setStorage(BINARY_FILE_KEY, NbPreferences.forModule(SelectBinaryPanelVisual.class));
        String binary = (String)wizardDescriptor.getProperty(WizardConstants.PROPERTY_BUILD_RESULT);
        if (binary == null) {
            binary = ""; // NOI18N
        }
        ((EditableComboBox)binaryField).read(binary);
    }

    void store(WizardDescriptor wizardDescriptor) {
        cancelSearch();
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_BUILD_RESULT,  ((EditableComboBox)binaryField).getText().trim());
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_PREFERED_PROJECT_NAME,   new File(((EditableComboBox)binaryField).getText().trim()).getName());
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_SOURCE_FOLDER_PATH,  sourcesField.getText().trim());
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_DEPENDENCY_KIND,  ((ProjectKindItem)dependeciesComboBox.getSelectedItem()).kind);
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_DEPENDENCIES,  getDlls());
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_TRUE_SOURCE_ROOT,  ((ProjectView)viewComboBox.getSelectedItem()).isSourceRoot);
        ((EditableComboBox)binaryField).setStorage(BINARY_FILE_KEY, NbPreferences.forModule(SelectBinaryPanelVisual.class));
        ((EditableComboBox)binaryField).store();
        if (wizardDescriptor.getProperty(WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV) != null) {
            // forbid tool collection selection
            // project creator detect real tool collection
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN, Boolean.TRUE);
        }
        // TODO should be inited
        wizardDescriptor.putProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH,  ""); // NOI18N
    }

    private ArrayList<String> getDlls(){
        ArrayList<String> dlls = new ArrayList<String>();
        if (((ProjectKindItem)dependeciesComboBox.getSelectedItem()).kind == IteratorExtension.ProjectKind.Minimal) {
            return dlls;
        }
        for(int i = 0; i < table.getModel().getRowCount(); i++) {
            if ((Boolean)table.getModel().getValueAt(i, 0)){
                dlls.add((String)table.getModel().getValueAt(i, 2));
            }
        }
        return dlls;
    }

    boolean valid() {
        return !searching.get() && checking.get()==0 && validBinary() && validSourceRoot() && validDlls();
    }

    private String getValidBinaryPath() {
        String path = ((EditableComboBox) binaryField).getText().trim();
        if (path.isEmpty()) {
            return null;
        }
        if (CndPathUtilitities.isPathAbsolute(path)) {
            return CndFileUtils.normalizeAbsolutePath(fileSystem, path);
        } else {
            return null;
        }   
    }
    
    private boolean validBinary() {
        String validBinaryPath = getValidBinaryPath();
        if (validBinaryPath != null) {
            FileObject fo = fileSystem.findResource(validBinaryPath); // can be null
            if (fo != null && fo.isValid()) {
                return MIMENames.isBinary(fo.getMIMEType());
            }
        }
            return false;
    }

    private boolean validSourceRoot() {
        String path = sourcesField.getText().trim();
        if (path.isEmpty()) {
            return false;
        }
        if (CndPathUtilitities.isPathAbsolute(path)) {
            FileObject fo = fileSystem.findResource(CndFileUtils.normalizeAbsolutePath(path));
            if (fo == null || !fo.isValid()) {
                return false;
            }
            return fo.isFolder();
        } else {
            return false;
        }
    }

    private boolean validDlls() {
        for(String dll : getDlls()) {
            FileObject fo = fileSystem.findResource(dll);
            if(fo == null || !fo.isValid()) {
                return false;
            }
        }
        return true;
    }

    private void onClickAction(MouseEvent e) {
        int rowIndex = table.rowAtPoint(e.getPoint());
        if (rowIndex >= 0) {
            TableColumnModel columnModel = table.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int col = table.convertColumnIndexToModel(viewColumn);
            if (col == 2){
                Rectangle rect = table.getCellRect(rowIndex, viewColumn, false);
                Point point = new Point(e.getPoint().x - rect.x, e.getPoint().y - rect.y);
                //System.err.println("Action for row "+rowIndex+" rect "+rect+" point "+point);
                if (rect.width - BUTTON_WIDTH <= point.x && point.x <= rect.width ) {
                    tableButtonActionPerformed(rowIndex);
                }
            }
        }
    }

    private String selectBinaryFile(String path) {
        FileFilter[] filters = FileFilterFactory.getBinaryFilters();

        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                controller.getWizardDescriptor(),
                getString("SelectBinaryPanelVisual.Browse.Title"), // NOI18N
                getString("SelectBinaryPanelVisual.Browse.Select"), // NOI18N
                JFileChooser.FILES_ONLY,
                filters,
                path,
                false
                );
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return null;
        }
        return fileChooser.getSelectedFile().getPath();
    }

    private void tableButtonActionPerformed(int row) {
        String path = selectBinaryFile((String) table.getModel().getValueAt(row, 2));
        if (path == null) {
            return;
        }
        table.getModel().setValueAt(path, row, 2);
    }

    private static String getString(String key) {
        return NbBundle.getMessage(SelectBinaryPanelVisual.class, key);
    }

    private static String getString(String key, String arg) {
        return NbBundle.getMessage(SelectBinaryPanelVisual.class, key, arg);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton binaryButton;
    private javax.swing.JComboBox binaryField;
    private javax.swing.JLabel binaryLabel;
    private javax.swing.JButton cancelSearch;
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

    private static final class ProjectKindItem {
        private final IteratorExtension.ProjectKind kind;
        ProjectKindItem(IteratorExtension.ProjectKind kind) {
            this.kind = kind;
        }

        @Override
        public String toString() {
            return getString("ProjectItemKind_"+kind);
        }
    }

    private static final class ProjectView {
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

    private static final class CheckBoxCellRenderer extends JCheckBox implements TableCellRenderer {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private final JLabel emptyLabel = new JLabel();

	public CheckBoxCellRenderer() {
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

    private static final int BUTTON_WIDTH = 20;
    private static final class PathCellRenderer extends JPanel implements TableCellRenderer {
        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private static final Border noFocusButtonBorder = new LineBorder(Color.GRAY, 1);
        private final JTextField field = new JTextField();
        private final JButton button = new JButton("..."); // NOI18N
        private final Color textFieldColor;
        private final Color redTextFieldColor;
        private final FileSystem fileSystem;

	public PathCellRenderer(FileSystem fileSystem) {
	    super();
            setLayout(new BorderLayout());
            add(field, BorderLayout.CENTER);
            field.setBorder(noFocusBorder);
            textFieldColor = field.getForeground();
            redTextFieldColor = new Color(field.getBackground().getRed(), textFieldColor.getGreen(), textFieldColor.getBlue());
            add(button, BorderLayout.EAST);
            button.setPreferredSize(new Dimension(BUTTON_WIDTH,5));
            button.setMaximumSize(new Dimension(BUTTON_WIDTH,20));
            button.setBorder(noFocusButtonBorder);
            this.fileSystem = fileSystem;
	}

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, final int row, final int column) {
            field.setText(value.toString());
            if (table.getModel().isCellEditable(row, column)) {
                field.setEnabled(true);
                button.setEnabled(true);
            } else {
                field.setEnabled(false);
                button.setEnabled(false);
            }
            FileObject dll = fileSystem.findResource(value.toString());
            if (dll != null && dll.isValid()) {
                field.setForeground(textFieldColor);
            } else {
                field.setForeground(redTextFieldColor);
            }
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setBorder(hasFocus ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder); // NOI18N
            return this;
        }
    }

    private static final class CheckBoxTableCellEditor extends DefaultCellEditor {

        private CheckBoxTableCellEditor() {
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

    private static final class MyDefaultTableModel extends DefaultTableModel {
        private List<Boolean> uses = new ArrayList<Boolean>();
        private List<String> names = new ArrayList<String>();
        private List<String> paths = new ArrayList<String>();
        private final SelectBinaryPanelVisual parent;
        private final boolean searching;
        private MyDefaultTableModel(SelectBinaryPanelVisual parent, Map<String, String> dlls, String root, String binaryRoot, boolean searching){
            super(new String[] {
                SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col0"), //NOI18N
                SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col1"), //NOI18N
                SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col2"), //NOI18N
            }, 0);
            this.searching = searching;
            for(Map.Entry<String,String> entry : dlls.entrySet()) {
                String dll = entry.getKey();
                names.add(dll);
                String path = entry.getValue();
                if (path == null) {
                    uses.add(Boolean.FALSE);
                    if (searching) {
                        paths.add(SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col.searching")); //NOI18N
                    } else {
                        paths.add(SelectBinaryPanelVisual.getString("SelectBinaryPanelVisual.col.notfound")); //NOI18N
                    }
                } else {
                    if (isMyDll(path, root) || isMyDll(path, binaryRoot)) {
                        uses.add(Boolean.TRUE);
                    } else {
                        uses.add(Boolean.FALSE);
                    }
                    paths.add(path);
                }
            }
            this.parent = parent;
        }

        private boolean isMyDll(String path, String root) {
            if (root == null) {
                return false;
            }
            path = path.replace('\\','/'); //NOI18N
            root = root.replace('\\','/'); //NOI18N
            if (path.startsWith("/usr/lib/")) { //NOI18N
                return false;
            } else if (path.startsWith("/lib/")) { //NOI18N
                return false;
            } else if (path.startsWith("/usr/local/lib/")) { //NOI18N
                return false;
            } else if (path.startsWith(root)) {
                return true;
            } else {
                String[] p1 = path.split("/");  // NOI18N
                String[] p2 = root.split("/");  // NOI18N
                for(int i = 0; i < Math.min(p1.length - 1, p2.length); i++) {
                    if (!p1[i].equals(p2[i])) {
                        if (i > 3) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    if (i > 3) {
                        return true;
                    }
                }
            }
            return false;
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
                    parent.validateController();
                    return;
                case 1:
                    names.set(row, (String)value);
                    return;
                case 2:
                    paths.set(row, (String)value);
                    parent.validateController();
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
            if (searching) {
                return false;
            }
            if (col == 1) {
                return false;
            } else {
                return true;
            }
        }
    }
}
