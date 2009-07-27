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
package org.netbeans.modules.cnd.ui.options;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;
import org.openide.windows.WindowManager;

/** Display the "Tools Default" panel */
public final class ToolsPanel extends JPanel implements ActionListener, DocumentListener,
        ListSelectionListener, ItemListener {

    // The following are constants so I can do == rather than "equals"
    private final String MAKE_NAME = "make"; // NOI18N
    private final String DEBUGGER_NAME = "debugger"; // NOI18N
    private final String C_NAME = "C"; // NOI18N
    private final String CPP_NAME = "C++"; // NOI18N
    private final String FORTRAN_NAME = "Fortran"; // NOI18N
    private final String ASSEMBLER_NAME = "Assembler"; // NOI18N
    private final String QMAKE_NAME = "QMake"; // NOI18N
    private final String CMAKE_NAME = "CMake"; // NOI18N
    public static final String PROP_VALID = "valid"; // NOI18N
    private boolean initialized = false;
    private boolean changed;
    private boolean changingCompilerSet;
    private boolean updating;
    private boolean valid;
    private ToolsPanelModel model = null;
    private Color tfColor = null;
    private boolean customizeDebugger;
    private ExecutionEnvironment execEnv;
    private static ToolsPanel instance = null;
    private CompilerSetManager csm;
    private CompilerSet currentCompilerSet;
    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    private final ToolsCacheManager cacheManager = new ToolsCacheManager();

    /** Creates new form ToolsPanel */
    public ToolsPanel() {
        initComponents();
        setName("TAB_ToolsTab"); // NOI18N (used as a pattern...)
        cbDebuggerRequired.setName("debugger"); // NOI18N
        cbCRequired.setName("c"); // NOI18N
        cbCppRequired.setName("c++"); // NOI18N
        cbFortranRequired.setName("fortran"); // NOI18N
        cbAsRequired.setName("assembler"); // NOI18N
        changed = false;
        instance = this;
        currentCompilerSet = null;
        if (cacheManager.isRemoteAvailable()) {
            execEnv = cacheManager.getDefaultHostEnvironment();
            btEditDevHost.setEnabled(true);
            cbDevHost.setEnabled(true);
        } else {
            execEnv = ExecutionEnvironmentFactory.getLocal();
        }

        lstDirlist.setCellRenderer(new MyCellRenderer());

        if ("Windows".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            setOpaque(false);
        }

        HelpCtx.setHelpIDString(this, "ResolveBuildTools"); // NOI18N
    }

    public ToolsPanel(ToolsPanelModel model) {
        this();
        this.model = model;
    }

    private void initialize() {
        if (instance == null) {
            instance = this;
        }
        changingCompilerSet = true;
        if (model == null) {
            model = new GlobalToolsPanelModel();
        }
        if (!model.showRequiredTools()) {
            requiredToolsLabel.setVisible(false); // Required Tools label!
            requiredToolsPanel.setVisible(false); // Required Tools panel!
        }
        cbDevHost.removeItemListener(this);

        ExecutionEnvironment selectedEnv = model.getSelectedDevelopmentHost();
        ServerRecord selectedRec = null;

        Collection<? extends ServerRecord> hostList = cacheManager.getHosts();
        if (hostList != null) {
            cbDevHost.removeAllItems();
            for (ServerRecord rec : hostList) {
                if (rec.getExecutionEnvironment().equals(selectedEnv)) {
                    selectedRec = rec;
                }
                cbDevHost.addItem(rec);
            }
        } else {
            cbDevHost.addItem(ServerList.get(ExecutionEnvironmentFactory.getLocal()));
        }

        if (selectedRec != null) {
            cbDevHost.setSelectedItem(selectedRec);
        } else {
            cbDevHost.setSelectedItem(cacheManager.getDefaultHostRecord());
        }

        cbDevHost.setRenderer(new MyDevHostListCellRenderer());
        cbDevHost.addItemListener(this);
        cbDevHost.setEnabled(model.getEnableDevelopmentHostChange());
        btEditDevHost.setEnabled(model.getEnableDevelopmentHostChange());
        execEnv = getSelectedRecord().getExecutionEnvironment();

        btBaseDirectory.setEnabled(false);
        btCBrowse.setEnabled(false);
        btCppBrowse.setEnabled(false);
        btFortranBrowse.setEnabled(false);
        btAsBrowse.setEnabled(false);
        btMakeBrowse.setEnabled(false);
        btDebuggerBrowse.setEnabled(false);
        btVersions.setEnabled(false);
        tfMakePath.setEditable(false);
        tfDebuggerPath.setEditable(false);
        tfQMakePath.setEditable(false);
        tfCMakePath.setEditable(false);
        btVersions.setEnabled(false);

        if (model.enableRequiredCompilerCB()) {
            cbCRequired.setEnabled(true);
            cbCppRequired.setEnabled(true);
            cbFortranRequired.setEnabled(true);
            cbAsRequired.setEnabled(true);
        } else {
            cbCRequired.setEnabled(false);
            cbCppRequired.setEnabled(false);
            cbFortranRequired.setEnabled(false);
            cbAsRequired.setEnabled(false);
        }
        csm = cacheManager.getCompilerSetManagerCopy(execEnv, true);

        customizeDebugger = isCustomizableDebugger();

        // Initialize Required tools. Can't do it in constructor because there is no model then.
        cbMakeRequired.setSelected(model.isMakeRequired());
        cbDebuggerRequired.setSelected(model.isDebuggerRequired());
        cbCRequired.setSelected(model.isCRequired());
        cbCppRequired.setSelected(model.isCppRequired());
        cbFortranRequired.setSelected(model.isFortranRequired());
        cbAsRequired.setSelected(model.isAsRequired());
    }

    private void addCompilerSet() {
        AddCompilerSetPanel panel = new AddCompilerSetPanel(csm);
        String title = isRemoteHostSelected() ? getString("NEW_TOOL_SET_TITLE_REMOTE", ExecutionEnvironmentFactory.toUniqueID(csm.getExecutionEnvironment())) : getString("NEW_TOOL_SET_TITLE");
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, title);
        panel.setDialogDescriptor(dialogDescriptor);
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }

        CompilerSet cs = panel.getCompilerSet();
        csm.add(cs);
        changed = true;
        update(false, cs);
    }

    private void duplicateCompilerSet() {
        CompilerSet selectedCompilerSet = (CompilerSet) lstDirlist.getSelectedValue();
        DuplicateCompilerSetPanel panel = new DuplicateCompilerSetPanel(csm, selectedCompilerSet);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, getString("COPY_TOOL_SET_TITLE"));
        panel.setDialogDescriptor(dialogDescriptor);
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }
        String compilerSetName = panel.getCompilerSetName().trim();
        CompilerSet cs = selectedCompilerSet.createCopy();
        cs.setName(compilerSetName);
        cs.unsetDefault();
        cs.setAutoGenerated(false);
        csm.add(cs);
        changed = true;
        update(false, cs);
    }

    private void onCompilerSetChanged() {
        boolean cbRemoveEnabled;
        if (model.showRequiredTools()) {
            cbRemoveEnabled = lstDirlist.getSelectedIndex() >= 0;
        } else {
            cbRemoveEnabled = csm.getCompilerSets().size() > 1 && lstDirlist.getSelectedIndex() >= 0;
        }
        changeCompilerSet((CompilerSet) lstDirlist.getSelectedValue());
        btAdd.setEnabled(isHostValidForEditing());
        btRemove.setEnabled(cbRemoveEnabled && isHostValidForEditing());
        btDuplicate.setEnabled(lstDirlist.getSelectedIndex() >= 0 && isHostValidForEditing());
        btDefault.setEnabled(lstDirlist.getSelectedIndex() >= 0 && !((CompilerSet) lstDirlist.getSelectedValue()).isDefault());
    }

    private void onNewDevHostSelected() {
        if (!execEnv.equals(getSelectedRecord().getExecutionEnvironment())) {
            log.fine("TP.itemStateChanged: About to update");
            changed = true;
            if (!cacheManager.hasCache()) {
                List<ServerRecord> nulist = new ArrayList<ServerRecord>(cbDevHost.getItemCount());
                for (int i = 0; i < cbDevHost.getItemCount(); i++) {
                    nulist.add((ServerRecord) cbDevHost.getItemAt(i));
                }
                cacheManager.setHosts(nulist);
            }
            cacheManager.setDefaultRecord((ServerRecord) cbDevHost.getSelectedItem());
            execEnv = getSelectedRecord().getExecutionEnvironment();
            model.setSelectedDevelopmentHost(execEnv);
            update(true);
        } else {
            update(false);
        }
    }

    private void removeCompilerSet() {
        CompilerSet cs = (CompilerSet) lstDirlist.getSelectedValue();
        if (cs != null) {
            int index = csm.getCompilerSets().indexOf(cs);
            csm.remove(cs);
            if (cs.isDefault()) {
                if (csm.getCompilerSets().size() > 0) {
                    csm.setDefault(csm.getCompilerSet(0));
                }
            }
            if (index >= 0 && index < csm.getCompilerSets().size()) {
                update(false, csm.getCompilerSets().get(index));
            } else if (index > 0) {
                update(false, csm.getCompilerSets().get(index - 1));
            } else {
                tfBaseDirectory.setText(""); // NOI18N
                btBaseDirectory.setEnabled(false);
                lbFamilyValue.setText(""); // NOI18N
                tfCPath.setText(""); // NOI18N
                tfCppPath.setText(""); // NOI18N
                tfFortranPath.setText(""); // NOI18N
                tfAsPath.setText(""); // NOI18N
                tfMakePath.setText(""); // NOI18N
                tfDebuggerPath.setText(""); // NOI18N
                tfQMakePath.setText(""); // NOI18N
                tfCMakePath.setText(""); // NOI18N
                update(false);
            }
            changed = true;
        }
    }

    private void setSelectedAsDefault() {
        CompilerSet cs = (CompilerSet) lstDirlist.getSelectedValue();
        csm.setDefault(cs);
        changed = true;
        update(false);

    }

    private void setMakePathField(String path) {
        tfMakePath.setText(path); // Validation happens automatically
    }

    private void validateMakePathField() {
        setPathFieldValid(tfMakePath, isPathFieldValid(tfMakePath) && supportedMake(tfMakePath));
        dataValid();
    }

    private void setGdbPathField(String path) {
        tfDebuggerPath.setText(path); // Validation happens automatically
    }

    private void validateGdbPathField() {
        setPathFieldValid(tfDebuggerPath, isPathFieldValid(tfDebuggerPath));
        dataValid();
    }

    private void setCPathField(String path) {
        tfCPath.setText(path); // Validation happens automatically
    }

    private void validateCPathField() {
        setPathFieldValid(tfCPath, isPathFieldValid(tfCPath));
        dataValid();
    }

    private void setCppPathField(String path) {
        tfCppPath.setText(path); // Validation happens automatically
    }

    private void validateCppPathField() {
        setPathFieldValid(tfCppPath, isPathFieldValid(tfCppPath));
        dataValid();
    }

    private void setFortranPathField(String path) {
        tfFortranPath.setText(path); // Validation happens automatically
    }

    private void validateFortranPathField() {
        setPathFieldValid(tfFortranPath, isPathFieldValid(tfFortranPath));
        dataValid();
    }

    private void setAsPathField(String path) {
        tfAsPath.setText(path); // Validation happens automatically
    }

    private void validateAsPathField() {
        setPathFieldValid(tfAsPath, isPathFieldValid(tfAsPath));
        dataValid();
    }

    private void setQMakePathField(String path) {
        tfQMakePath.setText(path); // Validation happens automatically
    }

    private void validateQMakePathField() {
        setPathFieldValid(tfQMakePath, isPathFieldValid(tfQMakePath));
        dataValid();
    }

    private void setCMakePathField(String path) {
        tfCMakePath.setText(path); // Validation happens automatically
    }

    private void validateCMakePathField() {
        setPathFieldValid(tfCMakePath, isPathFieldValid(tfCMakePath));
        dataValid();
    }

    public static boolean supportedMake(String name) {
        name = IpeUtils.getBaseName(name);
        return !name.toLowerCase().equals("mingw32-make.exe"); // NOI18N
    }

    private boolean supportedMake(JTextField field) {
        String txt = field.getText();
        if (txt.length() == 0) {
            return false;
        }
        return supportedMake(txt);
    }

    private boolean isPathFieldValid(JTextField field) {
        String txt = field.getText();
        if (txt.length() == 0) {
            return false;
        }

        if (execEnv.isLocal()) {
            File file = new File(txt);
            boolean ok = false;
            if (Utilities.isWindows()) {
                if (txt.endsWith(".lnk")) { // NOI18N
                    ok = false;
                } else {
                    ok = (file.exists() || new File(txt + ".lnk").exists()) && !file.isDirectory(); // NOI18N
                }
            } else {
                ok = file.exists() && !file.isDirectory();
            }
            if (!ok) {
                // try users path
                ArrayList<String> paths = Path.getPath();
                for (String p : paths) {
                    file = new File(p + File.separatorChar + txt);
                    ok = file.exists() && !file.isDirectory();
                    if (ok) {
                        break;
                    }
                }
            }
            return ok;
        } else {
            // TODO this method must be called out of EDT, because it's time consuming
            // we need to remember once calculated "valid" state and reuse it
            // instead of check on each unrelated action
            // with remote support it became even more visible in UI freezing
            return true;
//            if (SwingUtilities.isEventDispatchThread()) {
//                log.fine("ToolsPanel.isPathFieldValid from EDT"); // NOI18N
//                // always return true in remote mode, instead of call to very expensive operation
//                return true;
//            } else {
//                return serverList.isValidExecutable(execEnv, txt);
//            }
        }
    }

    private void setPathFieldValid(JTextField field, boolean valid) {
        if (valid) {
            field.setForeground(tfColor);
        } else {
            field.setForeground(Color.RED);
        }
    }

    /** Update the display */
    public void update() {
        update(true, null);
    }

    private void update(boolean doInitialize) {
        update(doInitialize, null);
    }

    /** Update the display */
    public void update(boolean doInitialize, CompilerSet selectedCS) {

        updating = true;
        if (!initialized || doInitialize) {
            initialize();
        }

        lbDebuggerCommand.setVisible(customizeDebugger);
        tfDebuggerPath.setVisible(customizeDebugger);
        btDebuggerBrowse.setVisible(customizeDebugger);

        cbMakeRequired.setVisible(model.showRequiredBuildTools());
        cbDebuggerRequired.setVisible(model.showRequiredDebugTools() && customizeDebugger);
        cbCppRequired.setVisible(model.showRequiredBuildTools());
        cbCRequired.setVisible(model.showRequiredBuildTools());
        cbFortranRequired.setVisible(model.showRequiredBuildTools());
        cbAsRequired.setVisible(model.showRequiredBuildTools());

        if (doInitialize) {
            // Set Default
            if (!csm.getCompilerSets().isEmpty()) {
                if (csm.getDefaultCompilerSet() == null) {
                    String name = model.getCompilerSetName(); // the default set
                    if (name.length() == 0 || csm.getCompilerSet(name) == null) {
                        csm.setDefault(csm.getCompilerSet(0));
                    } else {
                        csm.setDefault(csm.getCompilerSet(name));
                    }
                }
                String selectedName = model.getSelectedCompilerSetName(); // The selected set
                if (selectedName != null) {
                    selectedCS = csm.getCompilerSet(selectedName);

                }
                if (selectedCS == null) {
                    selectedCS = csm.getDefaultCompilerSet();
                }
            }
        }

        if (selectedCS == null) {
            selectedCS = (CompilerSet) lstDirlist.getSelectedValue();
        }
        lstDirlist.setListData(csm.getCompilerSets().toArray());
        if (selectedCS != null) {
            lstDirlist.setSelectedValue(selectedCS, true); // FIXUP: should use name
        }
        if (lstDirlist.getSelectedIndex() < 0) {
            lstDirlist.setSelectedIndex(0);
        }
        lstDirlist.invalidate();
        lstDirlist.repaint();
        onCompilerSetChanged();
        updating = false;
        dataValid();
        initialized = true;
    }

    private boolean isRemoteHostSelected() {
        return ((ServerRecord) cbDevHost.getSelectedItem()).isRemote();
    }

    private ServerRecord getSelectedRecord() {
        return (ServerRecord) cbDevHost.getSelectedItem();
    }

    private boolean isHostValidForEditing() {
        return true; //serverList == null ? true : serverList.get((String)cbDevHost.getSelectedItem()).isOnline();
    }

    private void changeCompilerSet(CompilerSet cs) {
        if (cs != null) {
            tfBaseDirectory.setText(cs.getDirectory());
            btBaseDirectory.setEnabled(!isRemoteHostSelected());
            lbFamilyValue.setText(cs.getCompilerFlavor().toString());
        } else {
            lbFamilyValue.setText(""); // NOI18N
            String errorMsg = "";
            if (!cacheManager.isDevHostValid(execEnv)) {
                errorMsg = NbBundle.getMessage(ToolsPanel.class, "TP_ErrorMessage_BadDevHost", execEnv.toString());
            }
            lblErrors.setText("<html>" + errorMsg + "</html>"); //NOI18N
            updateToolsControls(false, false, false, true);
            return;
        }
        if (currentCompilerSet != null && currentCompilerSet != cs) {
            Tool tool;
            tool = currentCompilerSet.findTool(Tool.CCompiler);
            tool.setPath(tfCPath.getText());
            tool = currentCompilerSet.findTool(Tool.CCCompiler);
            tool.setPath(tfCppPath.getText());
            tool = currentCompilerSet.findTool(Tool.FortranCompiler);
            tool.setPath(tfFortranPath.getText());
            tool = currentCompilerSet.findTool(Tool.Assembler);
            tool.setPath(tfAsPath.getText());
            tool = currentCompilerSet.findTool(Tool.MakeTool);
            tool.setPath(tfMakePath.getText());
            tool = currentCompilerSet.findTool(Tool.DebuggerTool);
            tool.setPath(tfDebuggerPath.getText());
            tool = currentCompilerSet.findTool(Tool.QMakeTool);
            tool.setPath(tfQMakePath.getText());
            tool = currentCompilerSet.findTool(Tool.CMakeTool);
            tool.setPath(tfCMakePath.getText());
        }


        changingCompilerSet = true;

        Tool cSelection = cs.getTool(Tool.CCompiler);
        Tool cppSelection = cs.getTool(Tool.CCCompiler);
        Tool fortranSelection = cs.getTool(Tool.FortranCompiler);
        Tool asSelection = cs.getTool(Tool.Assembler);
        Tool makeToolSelection = cs.getTool(Tool.MakeTool);
        Tool debuggerToolSelection = cs.getTool(Tool.DebuggerTool);
        Tool qmakeToolSelection = cs.getTool(Tool.QMakeTool);
        Tool cmakeToolSelection = cs.getTool(Tool.CMakeTool);
        if (cSelection != null) {
            setCPathField(cSelection.getPath());
        } else {
            tfCPath.setText("");
        }
        if (cppSelection != null) {
            setCppPathField(cppSelection.getPath());
        } else {
            tfCppPath.setText("");
        }
        if (fortranSelection != null) {
            setFortranPathField(fortranSelection.getPath());
        } else {
            tfFortranPath.setText("");
        }
        if (asSelection != null) {
            setAsPathField(asSelection.getPath());
        } else {
            tfAsPath.setText("");
        }
        if (qmakeToolSelection != null) {
            setQMakePathField(qmakeToolSelection.getPath());
        } else {
            tfQMakePath.setText("");
        }
        if (cmakeToolSelection != null) {
            setCMakePathField(cmakeToolSelection.getPath());
        } else {
            tfCMakePath.setText("");
        }
        setMakePathField(makeToolSelection.getPath());
        setGdbPathField(debuggerToolSelection.getPath());
        changingCompilerSet = false;
        currentCompilerSet = cs;
        fireCompilerSetChange();
        dataValid();
    }

    public void applyChanges(boolean force) {
        changed = force;
        applyChanges();
    }

    /** Apply changes */
    public void applyChanges() {
        if (changed || isChangedInOtherPanels()) {

            CompilerSet cs = (CompilerSet) lstDirlist.getSelectedValue();
            changed = false;
            if (cs != null) {
                cs.getTool(Tool.MakeTool).setPath(tfMakePath.getText());
                cs.getTool(Tool.DebuggerTool).setPath(tfDebuggerPath.getText());
                cs.getTool(Tool.CCompiler).setPath(tfCPath.getText());
                cs.getTool(Tool.CCCompiler).setPath(tfCppPath.getText());
                cs.getTool(Tool.FortranCompiler).setPath(tfFortranPath.getText());
                cs.getTool(Tool.Assembler).setPath(tfAsPath.getText());
                cs.getTool(Tool.QMakeTool).setPath(tfQMakePath.getText());
                cs.getTool(Tool.CMakeTool).setPath(tfCMakePath.getText());
                model.setCompilerSetName(csm.getDefaultCompilerSet().getName());
                model.setSelectedCompilerSetName(cs.getName());
            }
            currentCompilerSet = cs;
            cacheManager.applyChanges((ServerRecord) cbDevHost.getSelectedItem());
        }

        if (model != null) { // model is null for Tools->Options if we don't look at C/C++ panel
            // the following don't set changed if changed
            if (model.isDebuggerRequired() != cbDebuggerRequired.isSelected()) {
                model.setDebuggerRequired(cbDebuggerRequired.isSelected());
            }
            if (model.isCRequired() != cbCRequired.isSelected()) {
                model.setCRequired(cbCRequired.isSelected());
            }
            if (model.isCppRequired() != cbCppRequired.isSelected()) {
                model.setCppRequired(cbCppRequired.isSelected());
            }
            if (model.isFortranRequired() != cbFortranRequired.isSelected()) {
                model.setFortranRequired(cbFortranRequired.isSelected());
            }
            if (model.isAsRequired() != cbAsRequired.isSelected()) {
                model.setAsRequired(cbAsRequired.isSelected());
            }
        }
        instance = null; // remove the global instance
    }

    /** What to do if user cancels the dialog (nothing) */
    public void cancel() {
        cacheManager.clear();
        changed = false;
        instance = null; // remove the global instance
    }

    //TODO: get rid of this...
    public static ToolsPanel getToolsPanel() {
        return instance;
    }

    public ToolsCacheManager getToolsCacheManager() {
        return cacheManager;
    }

    public CompilerSet getCurrentCompilerSet() {
        return currentCompilerSet;
    }

    /**
     * Lets NB know if the data in the panel is valid and OK should be enabled
     *
     * @return Returns true if all data is valid
     */
    public boolean dataValid() {
        if (csm.getCompilerSets().size() == 0) {
            valid = false;
            firePropertyChange(PROP_VALID, !valid, valid);
            return false;
        }
        if (updating || changingCompilerSet) {
            return true;
        } else {
            boolean csmValid = csm.getCompilerSets().size() > 0;
            boolean makeValid = cbMakeRequired.isSelected() ? isPathFieldValid(tfMakePath) && supportedMake(tfMakePath) : true;
            boolean debuggerValid = cbDebuggerRequired.isSelected() ? isPathFieldValid(tfDebuggerPath) : true;
            boolean cValid = cbCRequired.isSelected() ? isPathFieldValid(tfCPath) : true;
            boolean cppValid = cbCppRequired.isSelected() ? isPathFieldValid(tfCppPath) : true;
            boolean fortranValid = cbFortranRequired.isSelected() ? isPathFieldValid(tfFortranPath) : true;
            boolean asValid = cbAsRequired.isSelected() ? isPathFieldValid(tfAsPath) : true;

            boolean devhostValid = cacheManager.isDevHostValid(execEnv);

            if (!initialized) {
                valid = !(csmValid && makeValid && debuggerValid && cValid && cppValid && fortranValid && asValid && devhostValid);
            }

            if (valid != (csmValid && makeValid && debuggerValid && cValid && cppValid && fortranValid && asValid && devhostValid)) {
                valid = !valid;
                firePropertyChange(PROP_VALID, !valid, valid);
            }

            // post errors in error text area
            lblErrors.setText("<html>"); // NOI18N
            if (!valid) {
                ArrayList<String> errors = new ArrayList<String>();
                if (!devhostValid) {
                    errors.add(NbBundle.getMessage(ToolsPanel.class, "TP_ErrorMessage_BadDevHost", execEnv.toString()));
                }
                if (cbMakeRequired.isSelected() && !makeValid) {
                    if (!isPathFieldValid(tfMakePath)) {
                        errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedMake"));
                    } else {
                        errors.add(NbBundle.getMessage(ToolsPanel.class, "TP_ErrorMessage_UnsupportedMake", "mingw32-make")); // NOI18N
                    }
                }
                if (cbCRequired.isSelected() && !cValid) {
                    errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedCCompiler"));
                }
                if (cbCppRequired.isSelected() && !cppValid) {
                    errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedCppCompiler"));
                }
                if (cbDebuggerRequired.isSelected() && !debuggerValid && customizeDebugger) {
                    errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedDebugger"));
                }
                if (cbFortranRequired.isSelected() && !fortranValid) {
                    errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedFortranCompiler"));
                }
                if (cbAsRequired.isSelected() && !asValid) {
                    errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedAssembler"));
                }
                StringBuilder errorString = new StringBuilder();
                for (int i = 0; i < errors.size(); i++) {
                    errorString.append(errors.get(i));
                    if (i < errors.size() - 1) {
                        errorString.append("<br>"); // NOI18N
                    } // NOI18N
                }
                lblErrors.setText("<html>" + errorString.toString() + "</html>"); //NOI18N

                validate();
                repaint();
            }

            boolean baseDirValid = new File(tfBaseDirectory.getText()).exists();
            boolean enableText = baseDirValid || (isRemoteHostSelected() && isHostValidForEditing());
            boolean enableBrowse = baseDirValid && !isRemoteHostSelected();
            boolean enableVersions = (baseDirValid || isRemoteHostSelected()) && isHostValidForEditing();
            updateToolsControls(enableText, enableBrowse, enableVersions, false);

            return valid;
        }
    }

    private void updateToolsControls(boolean enableText, boolean enableBrowse, boolean enableVersions, boolean cleanText) {
        btCBrowse.setEnabled(enableBrowse);
        btCppBrowse.setEnabled(enableBrowse);
        btFortranBrowse.setEnabled(enableBrowse);
        btAsBrowse.setEnabled(enableBrowse);
        btMakeBrowse.setEnabled(enableBrowse);
        btDebuggerBrowse.setEnabled(enableBrowse);
        btQMakeBrowse.setEnabled(enableBrowse);
        btCMakeBrowse.setEnabled(enableBrowse);
        btVersions.setEnabled(enableVersions);
        updateTextField(tfMakePath, enableText, cleanText);
        updateTextField(tfDebuggerPath, enableText, cleanText);
        updateTextField(tfBaseDirectory, enableText, cleanText);
        updateTextField(tfCPath, enableText, cleanText);
        updateTextField(tfCppPath, enableText, cleanText);
        updateTextField(tfFortranPath, enableText, cleanText);
        updateTextField(tfAsPath, enableText, cleanText);
        updateTextField(tfQMakePath, enableText, cleanText);
        updateTextField(tfCMakePath, enableText, cleanText);
    }

    private void updateTextField(JTextField tf, boolean editable, boolean cleanText) {
        if (cleanText) {
            tf.setText("");
        }
        tf.setEditable(editable);
    }

    /**
     * Lets caller know if any data has been changed.
     *
     * @return True if anything has been changed
     */
    public boolean isChanged() {
        return changed;
    }

    String getToolVersion(Tool tool, JTextField tf) {
        StringBuilder version = new StringBuilder();
        version.append(tool.getDisplayName() + ": "); // NOI18N
        if (isPathFieldValid(tf)) {
            String path = tf.getText();
            if (!IpeUtils.isPathAbsolute(path)) {
                path = Path.findCommand(path);
            }
            String v = postVersionInfo(tool, path);
            if (v != null) {
                version.append(v);
            } else {
                version.append(getString("TOOL_VERSION_NOT_FOUND")); // NOI18N
            }
        } else {
            version.append(getString("TOOL_NOT_FOUND")); // NOI18N
        }
        return version.toString();
    }

    /**
     * Display version information for a program pointed to by "path".
     *
     * @param tool  tool description
     * @param path  absolute path of the tool
     */
    private String postVersionInfo(Tool tool, String path) {
        if (path == null) {
            return null;
        }
        return new VersionCommand(tool, path).getVersion();
    }
    static Set<ChangeListener> listenerChanged = new HashSet<ChangeListener>();

    public static void addCompilerSetChangeListener(ChangeListener l) {
        listenerChanged.add(l);
    }

    public static void removeCompilerSetChangeListener(ChangeListener l) {
        listenerChanged.remove(l);
    }

    public void fireCompilerSetChange() {
        ChangeEvent ev = new ChangeEvent(currentCompilerSet);
        for (ChangeListener l : listenerChanged) {
            l.stateChanged(ev);
        }
    }
    private final static Set<ChangeListener> listenerModified = new WeakSet<ChangeListener>();

    public static void addCompilerSetModifiedListener(ChangeListener l) {
        synchronized (listenerModified) {
            listenerModified.add(l);
        }
    }

    public static void removeCompilerSetModifiedListener(ChangeListener l) {
        synchronized (listenerModified) {
            listenerModified.remove(l);
        }
    }

    public void fireCompilerSetModified() {
        ChangeEvent ev = new ChangeEvent(currentCompilerSet);
        synchronized (listenerModified) {
            for (ChangeListener l : listenerModified) {
                l.stateChanged(ev);
            }
        }
    }
    private static final Set<IsChangedListener> listenerIsChanged = new WeakSet<IsChangedListener>();

    public static void addIsChangedListener(IsChangedListener l) {
        synchronized (listenerIsChanged) {
            listenerIsChanged.add(l);
        }
    }

    public static void removeIsChangedListener(IsChangedListener l) {
        synchronized (listenerIsChanged) {
            listenerIsChanged.remove(l);
        }
    }

    private boolean isChangedInOtherPanels() {
        boolean isChanged = false;
        synchronized (listenerIsChanged) {
            for (IsChangedListener l : listenerIsChanged) {
                if (l.isChanged()) {
                    isChanged = true;
                    break;
                }
            }
        }
        return isChanged;
    }

    // implement ActionListener
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o instanceof JButton) {
            if (o == btAdd) {
                addCompilerSet();
            } else if (o == btRemove) {
                removeCompilerSet();
            } else if (o == btDuplicate) {
                duplicateCompilerSet();
            } else if (o == btEditDevHost) {
                editDevHosts();
            } else if (o == btDefault) {
                setSelectedAsDefault();
            } else {
                if (o == btMakeBrowse) {
                } else if (o == btDebuggerBrowse) {
                } else if (o == btCBrowse) {
                } else if (o == btCppBrowse) {
                } else if (o == btFortranBrowse) {
                }
            }
        }
    }

    // implemet ItemListener
    public void itemStateChanged(ItemEvent ev) {
        Object o = ev.getSource();
        if (!updating) {
            if (o == cbDevHost && ev.getStateChange() == ItemEvent.SELECTED) {
                onNewDevHostSelected();
            } else if (o instanceof JCheckBox && !changingCompilerSet) {
                dataValid();
            }
        }
    }

    // implement DocumentListener
    public void changedUpdate(DocumentEvent ev) {
    }

    public void insertUpdate(DocumentEvent ev) {
        if (!updating) {
            changed = true;
        }
        Document doc = ev.getDocument();
        String title = (String) doc.getProperty(Document.TitleProperty);
        if (title.equals(MAKE_NAME)) {
            validateMakePathField();
        } else if (title.equals(DEBUGGER_NAME)) {
            validateGdbPathField();
        } else if (title.equals(C_NAME)) {
            validateCPathField();
        } else if (title.equals(CPP_NAME)) {
            validateCppPathField();
        } else if (title.equals(FORTRAN_NAME)) {
            validateFortranPathField();
        } else if (title.equals(ASSEMBLER_NAME)) {
            validateAsPathField();
        } else if (title.equals(QMAKE_NAME)) {
            validateQMakePathField();
        } else if (title.equals(CMAKE_NAME)) {
            validateCMakePathField();
        }
    }

    public void removeUpdate(DocumentEvent ev) {
        insertUpdate(ev);
    }

    // Implement List SelectionListener
    public void valueChanged(ListSelectionEvent ev) {

        if (!ev.getValueIsAdjusting() && !updating) { // we don't want the event until its finished
            if (ev.getSource() == lstDirlist) {
                onCompilerSetChanged();
            }
        }
    }

    /**
     * Show the Development Host Manager. Note that we assume serverList is non-null as the Edit
     * button should <b>never</b> be enabled if its null.
     */
    private void editDevHosts() {
        // Show the Dev Host Manager dialog
        if (ServerListDisplayerEx.showServerListDialog(cacheManager)) {
            changed = true;
            cbDevHost.removeItemListener(this);
            log.fine("TP.editDevHosts: Removing all items from cbDevHost");
            cbDevHost.removeAllItems();
            log.fine("TP.editDevHosts: Adding " + cacheManager.getHosts().size() + " items to cbDevHost");
            for (ServerRecord rec : cacheManager.getHosts()) {
                log.fine("    Adding " + rec);
                cbDevHost.addItem(rec);
            }
            log.fine("TP.editDevHosts: cbDevHost has " + cbDevHost.getItemCount() + " items");
            log.fine("TP.editDevHosts: getDefaultHostRecord returns " + cacheManager.getDefaultHostRecord());
            cbDevHost.setSelectedItem(cacheManager.getDefaultHostRecord());
            cacheManager.ensureHostSetup(getSelectedRecord().getExecutionEnvironment());
            cbDevHost.addItemListener(this);
            onNewDevHostSelected();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lbToolCollections = new javax.swing.JLabel();
        lbMakeCommand = new javax.swing.JLabel();
        tfMakePath = new javax.swing.JTextField();
        tfMakePath.getDocument().putProperty(Document.TitleProperty, MAKE_NAME);
        tfMakePath.getDocument().addDocumentListener(this);
        btMakeBrowse = new javax.swing.JButton();
        btMakeBrowse.addActionListener(this);
        lbDebuggerCommand = new javax.swing.JLabel();
        tfDebuggerPath = new javax.swing.JTextField();
        tfDebuggerPath.getDocument().putProperty(Document.TitleProperty, DEBUGGER_NAME);
        tfDebuggerPath.getDocument().addDocumentListener(this);
        btDebuggerBrowse = new javax.swing.JButton();
        btDebuggerBrowse.addActionListener(this);
        lbCCommand = new javax.swing.JLabel();
        tfCPath = new javax.swing.JTextField();
        tfCPath.getDocument().putProperty(Document.TitleProperty, C_NAME);
        tfCPath.getDocument().addDocumentListener(this);
        btCBrowse = new javax.swing.JButton();
        btCBrowse.addActionListener(this);
        lbCppCommand = new javax.swing.JLabel();
        tfCppPath = new javax.swing.JTextField();
        tfCppPath.getDocument().putProperty(Document.TitleProperty, CPP_NAME);
        tfCppPath.getDocument().addDocumentListener(this);
        btCppBrowse = new javax.swing.JButton();
        btCppBrowse.addActionListener(this);
        lbFortranCommand = new javax.swing.JLabel();
        tfFortranPath = new javax.swing.JTextField();
        tfFortranPath.getDocument().putProperty(Document.TitleProperty, FORTRAN_NAME);
        tfFortranPath.getDocument().addDocumentListener(this);
        btFortranBrowse = new javax.swing.JButton();
        btFortranBrowse.addActionListener(this);
        lbFamily = new javax.swing.JLabel();
        requiredToolsLabel = new javax.swing.JLabel();
        requiredToolsPanel = new javax.swing.JPanel();
        cbMakeRequired = new javax.swing.JCheckBox();
        cbDebuggerRequired = new javax.swing.JCheckBox();
        cbDebuggerRequired.addItemListener(this);
        cbCRequired = new javax.swing.JCheckBox();
        cbCRequired.addItemListener(this);
        cbCppRequired = new javax.swing.JCheckBox();
        cbCppRequired.addItemListener(this);
        cbFortranRequired = new javax.swing.JCheckBox();
        cbFortranRequired.addItemListener(this);
        cbAsRequired = new javax.swing.JCheckBox();
        cbFortranRequired.addItemListener(this);
        lbBaseDirectory = new javax.swing.JLabel();
        tfBaseDirectory = new javax.swing.JTextField();
        btBaseDirectory = new javax.swing.JButton();
        buttomPanel = new javax.swing.JPanel();
        lblErrors = new javax.swing.JLabel();
        btVersions = new javax.swing.JButton();
        btRestore = new javax.swing.JButton();
        ToolSetPanel = new javax.swing.JPanel();
        spDirlist = new JScrollPane(lstDirlist);
        lstDirlist = new javax.swing.JList();
        buttonPanel = new javax.swing.JPanel();
        btAdd = new javax.swing.JButton();
        btAdd.addActionListener(this);
        btRemove = new javax.swing.JButton();
        btRemove.addActionListener(this);
        btDuplicate = new javax.swing.JButton();
        btDuplicate.addActionListener(this);
        btDefault = new javax.swing.JButton();
        btDefault.addActionListener(this);
        lbDevHost = new javax.swing.JLabel();
        cbDevHost = new javax.swing.JComboBox();
        cbDevHost.addItemListener(this);
        btEditDevHost = new javax.swing.JButton();
        btEditDevHost.addActionListener(this);
        lbAsCommand = new javax.swing.JLabel();
        tfAsPath = new javax.swing.JTextField();
        tfAsPath.getDocument().putProperty(Document.TitleProperty, ASSEMBLER_NAME);
        tfAsPath.getDocument().addDocumentListener(this);
        btAsBrowse = new javax.swing.JButton();
        btFortranBrowse.addActionListener(this);
        lbFamilyValue = new javax.swing.JLabel();
        lbQMakePath = new javax.swing.JLabel();
        lbCMakePath = new javax.swing.JLabel();
        tfQMakePath = new javax.swing.JTextField();
        tfQMakePath.getDocument().putProperty(Document.TitleProperty, QMAKE_NAME);
        tfQMakePath.getDocument().addDocumentListener(this);
        tfCMakePath = new javax.swing.JTextField();
        tfCMakePath.getDocument().putProperty(Document.TitleProperty, CMAKE_NAME);
        tfCMakePath.getDocument().addDocumentListener(this);
        btQMakeBrowse = new javax.swing.JButton();
        btCMakeBrowse = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(600, 400));
        setLayout(new java.awt.GridBagLayout());

        lbToolCollections.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DirlistLabel").charAt(0));
        lbToolCollections.setLabelFor(spDirlist);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle"); // NOI18N
        lbToolCollections.setText(bundle.getString("LBL_DirlistLabel")); // NOI18N
        lbToolCollections.setToolTipText(bundle.getString("HINT_DirListLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 4);
        add(lbToolCollections, gridBagConstraints);
        lbToolCollections.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_DirlistLabel")); // NOI18N
        lbToolCollections.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DirlistLabel")); // NOI18N

        lbMakeCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_MakeCommand").charAt(0));
        lbMakeCommand.setLabelFor(tfMakePath);
        lbMakeCommand.setText(bundle.getString("LBL_MakeCommand")); // NOI18N
        lbMakeCommand.setToolTipText(bundle.getString("HINT_MakeCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 0);
        add(lbMakeCommand, gridBagConstraints);
        lbMakeCommand.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_MakeCommand")); // NOI18N
        lbMakeCommand.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MakeCommand")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 2, 0, 0);
        add(tfMakePath, gridBagConstraints);
        tfMakePath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.tfMakePath.AccessibleContext.accessibleDescription")); // NOI18N

        btMakeBrowse.setText(bundle.getString("LBL_MakeVersion")); // NOI18N
        btMakeBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btMakeBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 6);
        add(btMakeBrowse, gridBagConstraints);
        btMakeBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btMakeVersion.AccessibleContext.accessibleDescription")); // NOI18N

        lbDebuggerCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_GdbCommand").charAt(0));
        lbDebuggerCommand.setLabelFor(tfDebuggerPath);
        lbDebuggerCommand.setText(bundle.getString("LBL_GdbCommand")); // NOI18N
        lbDebuggerCommand.setToolTipText(bundle.getString("HINT_GdbCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbDebuggerCommand, gridBagConstraints);
        lbDebuggerCommand.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_GdbCommand")); // NOI18N
        lbDebuggerCommand.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_GdbCommand")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfDebuggerPath, gridBagConstraints);
        tfDebuggerPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.tfGdbPath.AccessibleContext.accessibleDescription")); // NOI18N

        btDebuggerBrowse.setText(bundle.getString("LBL_GdbVersion")); // NOI18N
        btDebuggerBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDebuggerBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btDebuggerBrowse, gridBagConstraints);
        btDebuggerBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btGdbVersion.AccessibleContext.accessibleDescription")); // NOI18N

        lbCCommand.setLabelFor(tfCPath);
        lbCCommand.setText(bundle.getString("LBL_CCommand")); // NOI18N
        lbCCommand.setToolTipText(bundle.getString("HINT_CCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbCCommand, gridBagConstraints);
        lbCCommand.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CCommand")); // NOI18N
        lbCCommand.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CCommand")); // NOI18N

        tfCPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCPath, gridBagConstraints);

        btCBrowse.setText(bundle.getString("LBL_CVersion")); // NOI18N
        btCBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btCBrowse, gridBagConstraints);
        btCBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btCVersion.AccessibleContext.accessibleDescription")); // NOI18N

        lbCppCommand.setLabelFor(tfCppPath);
        lbCppCommand.setText(bundle.getString("LBL_CppCommand")); // NOI18N
        lbCppCommand.setToolTipText(bundle.getString("HINT_CppCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbCppCommand, gridBagConstraints);
        lbCppCommand.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CppCommand")); // NOI18N
        lbCppCommand.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CppCommand")); // NOI18N

        tfCppPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCppPath, gridBagConstraints);

        btCppBrowse.setText(bundle.getString("LBL_CppVersion")); // NOI18N
        btCppBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCppBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btCppBrowse, gridBagConstraints);
        btCppBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btCppVersion.AccessibleContext.accessibleDescription")); // NOI18N

        lbFortranCommand.setLabelFor(tfFortranPath);
        lbFortranCommand.setText(bundle.getString("LBL_FortranCommand")); // NOI18N
        lbFortranCommand.setToolTipText(bundle.getString("HINT_FortranCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbFortranCommand, gridBagConstraints);
        lbFortranCommand.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_FortranCommand")); // NOI18N
        lbFortranCommand.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_FortranCommand")); // NOI18N

        tfFortranPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfFortranPath, gridBagConstraints);

        btFortranBrowse.setText(bundle.getString("LBL_FortranVersion")); // NOI18N
        btFortranBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFortranBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btFortranBrowse, gridBagConstraints);
        btFortranBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btFortranVersion.AccessibleContext.accessibleDescription")); // NOI18N

        lbFamily.setText(bundle.getString("LBL_CompilerCollection")); // NOI18N
        lbFamily.setToolTipText(bundle.getString("HINT_CompilerCollection")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(lbFamily, gridBagConstraints);
        lbFamily.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CompilerCollection")); // NOI18N
        lbFamily.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CompilerCollection")); // NOI18N

        requiredToolsLabel.setLabelFor(cbMakeRequired);
        requiredToolsLabel.setText(bundle.getString("LBL_RequiredTools")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 0);
        add(requiredToolsLabel, gridBagConstraints);

        requiredToolsPanel.setLayout(new java.awt.GridBagLayout());

        cbMakeRequired.setSelected(true);
        cbMakeRequired.setText(bundle.getString("LBL_RequiredMake")); // NOI18N
        cbMakeRequired.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        requiredToolsPanel.add(cbMakeRequired, gridBagConstraints);

        cbDebuggerRequired.setText(bundle.getString("LBL_RequiredGdb")); // NOI18N
        cbDebuggerRequired.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        requiredToolsPanel.add(cbDebuggerRequired, gridBagConstraints);

        cbCRequired.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CCompiler_CB").charAt(0));
        cbCRequired.setText(bundle.getString("LBL_RequiredCompiler_C")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        requiredToolsPanel.add(cbCRequired, gridBagConstraints);

        cbCppRequired.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CppCompiler_CB").charAt(0));
        cbCppRequired.setText(bundle.getString("LBL_RequiredCompiler_Cpp")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        requiredToolsPanel.add(cbCppRequired, gridBagConstraints);

        cbFortranRequired.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_FortranCompiler_CB").charAt(0));
        cbFortranRequired.setText(bundle.getString("LBL_RequiredCompiler_Fortran")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        requiredToolsPanel.add(cbFortranRequired, gridBagConstraints);

        cbAsRequired.setText(bundle.getString("ToolsPanel.cbAsRequired.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        requiredToolsPanel.add(cbAsRequired, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 0, 6);
        add(requiredToolsPanel, gridBagConstraints);

        lbBaseDirectory.setLabelFor(tfBaseDirectory);
        lbBaseDirectory.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.lbBaseDirectory.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbBaseDirectory, gridBagConstraints);

        tfBaseDirectory.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 0, 0);
        add(tfBaseDirectory, gridBagConstraints);
        tfBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.tfBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N

        btBaseDirectory.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btBaseDirectory.text")); // NOI18N
        btBaseDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBaseDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 6);
        add(btBaseDirectory, gridBagConstraints);
        btBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N

        buttomPanel.setOpaque(false);
        buttomPanel.setLayout(new java.awt.GridBagLayout());

        lblErrors.setForeground(new java.awt.Color(255, 51, 51));
        lblErrors.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblErrors.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.lblErrors.text")); // NOI18N
        lblErrors.setEnabled(false);
        lblErrors.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        buttomPanel.add(lblErrors, gridBagConstraints);
        lblErrors.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.lblErrors.AccessibleContext.accessibleName")); // NOI18N

        btVersions.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_Versions").charAt(0));
        btVersions.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btVersions.text")); // NOI18N
        btVersions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btVersionsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        buttomPanel.add(btVersions, gridBagConstraints);
        btVersions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btVersions.AccessibleContext.accessibleDescription")); // NOI18N

        btRestore.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_RestoreDefault_BT").charAt(0));
        btRestore.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btRestore.text")); // NOI18N
        btRestore.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        btRestore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRestoreActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        buttomPanel.add(btRestore, gridBagConstraints);
        btRestore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btRestore.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(buttomPanel, gridBagConstraints);

        ToolSetPanel.setOpaque(false);
        ToolSetPanel.setLayout(new java.awt.GridBagLayout());

        spDirlist.setMinimumSize(new java.awt.Dimension(180, 20));
        spDirlist.setPreferredSize(new java.awt.Dimension(180, 20));

        lstDirlist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstDirlist.setAutoscrolls(false);
        lstDirlist.addListSelectionListener(this);
        spDirlist.setViewportView(lstDirlist);
        lstDirlist.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.lstDirlist.AccessibleContext.accessibleName")); // NOI18N
        lstDirlist.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.lstDirlist.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        ToolSetPanel.add(spDirlist, gridBagConstraints);

        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new java.awt.GridBagLayout());

        btAdd.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_AddButton").charAt(0));
        btAdd.setText(bundle.getString("LBL_AddButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonPanel.add(btAdd, gridBagConstraints);
        btAdd.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_AddButton")); // NOI18N
        btAdd.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_AddButton")); // NOI18N

        btRemove.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_RemoveButton").charAt(0));
        btRemove.setText(bundle.getString("LBL_RemoveButton")); // NOI18N
        btRemove.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonPanel.add(btRemove, gridBagConstraints);
        btRemove.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_RemoveButton")); // NOI18N
        btRemove.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_RemoveButton")); // NOI18N

        btDuplicate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_UpButton").charAt(0));
        btDuplicate.setText(bundle.getString("LBL_UpButton")); // NOI18N
        btDuplicate.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        buttonPanel.add(btDuplicate, gridBagConstraints);
        btDuplicate.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_UpButton")); // NOI18N
        btDuplicate.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_UpButton")); // NOI18N

        btDefault.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DownButton").charAt(0));
        btDefault.setText(bundle.getString("LBL_DownButton")); // NOI18N
        btDefault.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        buttonPanel.add(btDefault, gridBagConstraints);
        btDefault.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_DownButton")); // NOI18N
        btDefault.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DownButton")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        ToolSetPanel.add(buttonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(ToolSetPanel, gridBagConstraints);

        lbDevHost.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DevelopmentHosts").charAt(0));
        lbDevHost.setLabelFor(cbDevHost);
        lbDevHost.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "LBL_DevelopmentHosts")); // NOI18N
        lbDevHost.setToolTipText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "HINT_DevelopmentHosts")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        add(lbDevHost, gridBagConstraints);

        cbDevHost.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(cbDevHost, gridBagConstraints);

        btEditDevHost.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_AddDevHost").charAt(0));
        btEditDevHost.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "Lbl_AddDevHost")); // NOI18N
        btEditDevHost.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btEditDevHost, gridBagConstraints);

        lbAsCommand.setLabelFor(tfFortranPath);
        lbAsCommand.setText(bundle.getString("ToolsPanel.lbAsCommand.text")); // NOI18N
        lbAsCommand.setToolTipText(bundle.getString("ToolsPanel.lbAsCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbAsCommand, gridBagConstraints);

        tfAsPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfAsPath, gridBagConstraints);

        btAsBrowse.setText(bundle.getString("ToolsPanel.btAsBrowse.text")); // NOI18N
        btAsBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAsBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btAsBrowse, gridBagConstraints);

        lbFamilyValue.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.lbFamilyValue.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        add(lbFamilyValue, gridBagConstraints);

        lbQMakePath.setLabelFor(tfQMakePath);
        lbQMakePath.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "LBL_QMakeCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbQMakePath, gridBagConstraints);

        lbCMakePath.setLabelFor(tfCMakePath);
        lbCMakePath.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "LBL_CMakeCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbCMakePath, gridBagConstraints);

        tfQMakePath.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.tfQMakePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfQMakePath, gridBagConstraints);

        tfCMakePath.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.tfCMakePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCMakePath, gridBagConstraints);

        btQMakeBrowse.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btQMakeBrowse.text")); // NOI18N
        btQMakeBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btQMakeBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btQMakeBrowse, gridBagConstraints);

        btCMakeBrowse.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btCMakeBrowse.text")); // NOI18N
        btCMakeBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCMakeBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btCMakeBrowse, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void btVersionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btVersionsActionPerformed
    btVersions.setEnabled(false);

    RequestProcessor.getDefault().post(new Runnable() {

        public void run() {
            ProgressHandle handle = ProgressHandleFactory.createHandle(getString("LBL_VersionInfo_Progress")); // NOI18N
            handle.start(customizeDebugger ? 8 : 7);

            StringBuilder versions = new StringBuilder();
            int i = 0;
            versions.append("\n"); // NOI18N
            versions.append(getToolVersion(currentCompilerSet.findTool(Tool.CCompiler), tfCPath)).append("\n"); // NOI18N
            handle.progress(++i);
            versions.append(getToolVersion(currentCompilerSet.findTool(Tool.CCCompiler), tfCppPath)).append("\n"); // NOI18N
            handle.progress(++i);
            versions.append(getToolVersion(currentCompilerSet.findTool(Tool.FortranCompiler), tfFortranPath)).append("\n"); // NOI18N
            handle.progress(++i);
            versions.append(getToolVersion(currentCompilerSet.findTool(Tool.Assembler), tfAsPath)).append("\n"); // NOI18N
            handle.progress(++i);
            versions.append(getToolVersion(currentCompilerSet.findTool(Tool.MakeTool), tfMakePath)).append("\n"); // NOI18N
            if (customizeDebugger) {
                handle.progress(++i);
                versions.append(getToolVersion(currentCompilerSet.findTool(Tool.DebuggerTool), tfDebuggerPath)).append("\n"); // NOI18N
            }
            handle.progress(++i);
            versions.append(getToolVersion(currentCompilerSet.findTool(Tool.QMakeTool), tfQMakePath)).append("\n"); // NOI18N
            handle.progress(++i);
            versions.append(getToolVersion(currentCompilerSet.findTool(Tool.CMakeTool), tfCMakePath)).append("\n"); // NOI18N
            handle.finish();

            NotifyDescriptor nd = new NotifyDescriptor.Message(versions.toString());
            nd.setTitle(getString("LBL_VersionInfo_Title")); // NOI18N
            DialogDisplayer.getDefault().notify(nd);

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    btVersions.setEnabled(true);
                }
            });
        }
    });
}//GEN-LAST:event_btVersionsActionPerformed

private void btBaseDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBaseDirectoryActionPerformed
    String seed = null;
    if (tfBaseDirectory.getText().length() > 0) {
        seed = tfBaseDirectory.getText();
    } else if (FileChooser.getCurrectChooserFile() != null) {
        seed = FileChooser.getCurrectChooserFile().getPath();
    } else {
        seed = System.getProperty("user.home"); // NOI18N
    }
    FileChooser fileChooser = new FileChooser(getString("SELECT_BASE_DIRECTORY_TITLE"), null, JFileChooser.DIRECTORIES_ONLY, null, seed, true);
    int ret = fileChooser.showOpenDialog(this);
    if (ret == JFileChooser.CANCEL_OPTION) {
        return;
    }
    String dirPath = fileChooser.getSelectedFile().getPath();
    tfBaseDirectory.setText(dirPath);

    CompilerSet cs = (CompilerSet) lstDirlist.getSelectedValue();
    csm.reInitCompilerSet(cs, dirPath);
    changed = true;
    update(false);

}//GEN-LAST:event_btBaseDirectoryActionPerformed

    private boolean selectCompiler(JTextField tf, Tool tool) {
        String seed = tfBaseDirectory.getText();
        FileChooser fileChooser = new FileChooser(getString("SELECT_TOOL_TITLE"), null, JFileChooser.FILES_ONLY, null, seed, false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return false;
        }
        if (!new File(new File(tfBaseDirectory.getText()), fileChooser.getSelectedFile().getName()).exists()) {
            NotifyDescriptor nb = new NotifyDescriptor.Message(getString("COMPILER_BASE_ERROR"), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nb);
            return false;
        }
        String aPath = fileChooser.getSelectedFile().getPath();
        if (Utilities.isWindows()) {
            if (aPath.endsWith(".lnk")) { // NOI18N
                aPath = aPath.substring(0, aPath.length() - 4);
            }
        }
        tf.setText(aPath);
        tool.setPath(tf.getText());
        fireCompilerSetChange();
        fireCompilerSetModified();
        return true;
    }

    private boolean selectTool(JTextField tf) {
        String seed = tf.getText();
        FileChooser fileChooser = new FileChooser(getString("SELECT_TOOL_TITLE"), null, JFileChooser.FILES_ONLY, null, seed, false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return false;
        }
        String aPath = fileChooser.getSelectedFile().getPath();
        if (Utilities.isWindows()) {
            if (aPath.endsWith(".lnk")) { // NOI18N
                aPath = aPath.substring(0, aPath.length() - 4);
            }
        }
        tf.setText(aPath);
        return true;
    }

    private boolean isCustomizableDebugger() {
        ToolsPanelGlobalCustomizer customizer = Lookup.getDefault().lookup(ToolsPanelGlobalCustomizer.class);
        return customizer == null ? true : customizer.isDebuggerCustomizable();
    }

    static class MyCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            CompilerSet cs = (CompilerSet) value;
            if (cs.isDefault()) {
                comp.setFont(comp.getFont().deriveFont(Font.BOLD));
            }
            return comp;
        }
    }

    class MyDevHostListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ServerRecord rec = (ServerRecord) value;
            label.setText(rec.getDisplayName());
            if (value != null && value.equals(cacheManager.getDefaultHostRecord())) {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            }
            return label;
        }
    }

private void btCBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCBrowseActionPerformed
    selectCompiler(tfCPath, currentCompilerSet.getTool(Tool.CCompiler));
}//GEN-LAST:event_btCBrowseActionPerformed

private void btCppBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCppBrowseActionPerformed
    selectCompiler(tfCppPath, currentCompilerSet.getTool(Tool.CCCompiler));
}//GEN-LAST:event_btCppBrowseActionPerformed

private void btFortranBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFortranBrowseActionPerformed
    selectCompiler(tfFortranPath, currentCompilerSet.getTool(Tool.FortranCompiler));
}//GEN-LAST:event_btFortranBrowseActionPerformed

private void btMakeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btMakeBrowseActionPerformed
    selectTool(tfMakePath);
}//GEN-LAST:event_btMakeBrowseActionPerformed

private void btDebuggerBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDebuggerBrowseActionPerformed
    selectTool(tfDebuggerPath);
}//GEN-LAST:event_btDebuggerBrowseActionPerformed

private void btRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRestoreActionPerformed
    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
            getString("RESTORE_TXT"),
            getString("RESTORE_TITLE"),
            NotifyDescriptor.OK_CANCEL_OPTION);
    Object ret = DialogDisplayer.getDefault().notify(nd);
    if (ret != NotifyDescriptor.OK_OPTION) {
        return;
    }
    final CompilerSet selectedCS[] = new CompilerSet[]{(CompilerSet) lstDirlist.getSelectedValue()};
    final AtomicBoolean cancelled = new AtomicBoolean(false);
    Runnable longTask = new Runnable() {

        public void run() {
            log.finest("Restoring defaults\n");
            ServerRecord record = ServerList.get(execEnv);
            if (record.isOffline()) {
                record.validate(true);
                if (record.isOffline()) {
                    cancelled.set(true);
                    return;
                }
            }
//            try {
//                ConnectionManager.getInstance().connectTo(execEnv);
//            } catch (IOException ex) {
//                //TODO: report it!
//                cancelled.set(true);
//                return;
//            } catch (CancellationException ex) {
//                cancelled.set(true);
//                return;
//            }
            CompilerSetManager newCsm = CompilerSetManager.create(execEnv);
            newCsm.initialize(false, true);
            while (newCsm.isPending()) {
                log.finest("\twaiting for compiler manager to initialize...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    cancelled.set(true);
                    return;
                }
            }

            cacheManager.addCompilerSetManager(newCsm);
            List<CompilerSet> list = csm.getCompilerSets();
            for (CompilerSet cs : list) {
                if (!cs.isAutoGenerated()) {
                    String name = cs.getName();
                    String newName = newCsm.getUniqueCompilerSetName(name);
                    if (!name.equals(newName)) {
                        // FIXUP: show a dialog with renamed custom sets. Can't do now because of UI freeze.
                        cs.setName(newName);
                    }
                    newCsm.add(cs);
                }
            }
            String defaultName = null;
            CompilerSet defaultCS = csm.getDefaultCompilerSet();
            if (defaultCS != null) {
                defaultName = defaultCS.getName();
            }
            String selectedName = null;
            if (selectedCS[0] != null) {
                selectedName = selectedCS[0].getName();
            }
            csm = newCsm;
            CompilerSet defaultCompilerSet = csm.getCompilerSet(defaultName);
            if (defaultCompilerSet != null) {
                csm.setDefault(defaultCompilerSet);
            }
            if (selectedName != null) {
                selectedCS[0] = csm.getCompilerSet(selectedName);
            }
            log.finest("Restored defaults\n");
        }
    };
    Runnable postWork = new Runnable() {

        public void run() {
            if (!cancelled.get()) {
                changed = true;
                if (selectedCS[0] != null) {
                    update(false, selectedCS[0]);
                } else {
                    update(false);
                }
            }
        }
    };
    final Frame mainWindow = WindowManager.getDefault().getMainWindow();
    String title = getString("TITLE_Configure");
    String msg = getString("MSG_Configure_Compiler_Sets", execEnv.toString());
    ModalMessageDlg.runLongTask(mainWindow, longTask, postWork, null, title, msg);
}//GEN-LAST:event_btRestoreActionPerformed

private void btAsBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAsBrowseActionPerformed
    selectCompiler(tfAsPath, currentCompilerSet.getTool(Tool.Assembler));
}//GEN-LAST:event_btAsBrowseActionPerformed

private void btQMakeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btQMakeBrowseActionPerformed
    selectTool(tfQMakePath);
}//GEN-LAST:event_btQMakeBrowseActionPerformed

private void btCMakeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCMakeBrowseActionPerformed
    selectTool(tfCMakePath);
}//GEN-LAST:event_btCMakeBrowseActionPerformed

    private static String getString(String key) {
        return NbBundle.getMessage(ToolsPanel.class, key);
    }

    private static String getString(String key, Object param) {
        return NbBundle.getMessage(ToolsPanel.class, key, param);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ToolSetPanel;
    private javax.swing.JButton btAdd;
    private javax.swing.JButton btAsBrowse;
    private javax.swing.JButton btBaseDirectory;
    private javax.swing.JButton btCBrowse;
    private javax.swing.JButton btCMakeBrowse;
    private javax.swing.JButton btCppBrowse;
    private javax.swing.JButton btDebuggerBrowse;
    private javax.swing.JButton btDefault;
    private javax.swing.JButton btDuplicate;
    private javax.swing.JButton btEditDevHost;
    private javax.swing.JButton btFortranBrowse;
    private javax.swing.JButton btMakeBrowse;
    private javax.swing.JButton btQMakeBrowse;
    private javax.swing.JButton btRemove;
    private javax.swing.JButton btRestore;
    private javax.swing.JButton btVersions;
    private javax.swing.JPanel buttomPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JCheckBox cbAsRequired;
    private javax.swing.JCheckBox cbCRequired;
    private javax.swing.JCheckBox cbCppRequired;
    private javax.swing.JCheckBox cbDebuggerRequired;
    private javax.swing.JComboBox cbDevHost;
    private javax.swing.JCheckBox cbFortranRequired;
    private javax.swing.JCheckBox cbMakeRequired;
    private javax.swing.JLabel lbAsCommand;
    private javax.swing.JLabel lbBaseDirectory;
    private javax.swing.JLabel lbCCommand;
    private javax.swing.JLabel lbCMakePath;
    private javax.swing.JLabel lbCppCommand;
    private javax.swing.JLabel lbDebuggerCommand;
    private javax.swing.JLabel lbDevHost;
    private javax.swing.JLabel lbFamily;
    private javax.swing.JLabel lbFamilyValue;
    private javax.swing.JLabel lbFortranCommand;
    private javax.swing.JLabel lbMakeCommand;
    private javax.swing.JLabel lbQMakePath;
    private javax.swing.JLabel lbToolCollections;
    private javax.swing.JLabel lblErrors;
    private javax.swing.JList lstDirlist;
    private javax.swing.JLabel requiredToolsLabel;
    private javax.swing.JPanel requiredToolsPanel;
    private javax.swing.JScrollPane spDirlist;
    private javax.swing.JTextField tfAsPath;
    private javax.swing.JTextField tfBaseDirectory;
    private javax.swing.JTextField tfCMakePath;
    private javax.swing.JTextField tfCPath;
    private javax.swing.JTextField tfCppPath;
    private javax.swing.JTextField tfDebuggerPath;
    private javax.swing.JTextField tfFortranPath;
    private javax.swing.JTextField tfMakePath;
    private javax.swing.JTextField tfQMakePath;
    // End of variables declaration//GEN-END:variables
}
