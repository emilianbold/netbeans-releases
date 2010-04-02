/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * ToolCollectionPanel.java
 *
 * Created on Oct 13, 2009, 10:55:01 AM
 */

package org.netbeans.modules.cnd.toolchain.ui.options;

import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelModel;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.toolchain.compilerset.APIAccessor;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolUtils;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
/*package-local*/ class ToolCollectionPanel extends javax.swing.JPanel implements DocumentListener, ItemListener  {

    private final String MAKE_NAME = "make"; // NOI18N
    private final String DEBUGGER_NAME = "debugger"; // NOI18N
    private final String C_NAME = "C"; // NOI18N
    private final String CPP_NAME = "C++"; // NOI18N
    private final String FORTRAN_NAME = "Fortran"; // NOI18N
    private final String ASSEMBLER_NAME = "Assembler"; // NOI18N
    private final String QMAKE_NAME = "QMake"; // NOI18N
    private final String CMAKE_NAME = "CMake"; // NOI18N
    private Color tfColor = null;
    private boolean isUrl = false;

    private final ToolsPanel manager;

    /** Creates new form ToolCollectionPanel */
    public ToolCollectionPanel(ToolsPanel manager) {
        this.manager = manager;
        initComponents();
        tpInstall.setContentType("text/html"); // NOI18N
        btInstall.setVisible(isUrl);
        scrollPane.setVisible(isUrl);
        cbDebuggerRequired.setName("debugger"); // NOI18N
        cbCRequired.setName("c"); // NOI18N
        cbCppRequired.setName("c++"); // NOI18N
        cbFortranRequired.setName("fortran"); // NOI18N
        cbQMakeRequired.setName("qmake"); // NOI18N
        cbAsRequired.setName("assembler"); // NOI18N
    }

    void initializeUI() {
        if (!manager.getModel().showRequiredTools()) {
            requiredToolsLabel.setVisible(false); // Required Tools label!
            requiredToolsPanel.setVisible(false); // Required Tools panel!
        }
        tfMakePath.setEditable(false);
        tfDebuggerPath.setEditable(false);
        tfQMakePath.setEditable(false);
        tfCMakePath.setEditable(false);

        if (manager.getModel().enableRequiredCompilerCB()) {
            cbCRequired.setEnabled(true);
            cbCppRequired.setEnabled(true);
            cbFortranRequired.setEnabled(true);
            cbQMakeRequired.setEnabled(true);
            cbAsRequired.setEnabled(true);
        } else {
            cbCRequired.setEnabled(false);
            cbCppRequired.setEnabled(false);
            cbFortranRequired.setEnabled(false);
            cbQMakeRequired.setEnabled(false);
            cbAsRequired.setEnabled(false);
        }

        // Initialize Required tools. Can't do it in constructor because there is no model then.
        cbMakeRequired.setSelected(manager.getModel().isMakeRequired());
        cbDebuggerRequired.setSelected(manager.getModel().isDebuggerRequired());
        cbCRequired.setSelected(manager.getModel().isCRequired());
        cbCppRequired.setSelected(manager.getModel().isCppRequired());
        cbFortranRequired.setSelected(manager.getModel().isFortranRequired());
        cbQMakeRequired.setSelected(manager.getModel().isQMakeRequired());
        cbAsRequired.setSelected(manager.getModel().isAsRequired());
    }

    void updateUI(boolean doInitialize, CompilerSet selectedCS){
        lbDebuggerCommand.setVisible(manager.isCustomizableDebugger());
        tfDebuggerPath.setVisible(manager.isCustomizableDebugger());
        btDebuggerBrowse.setVisible(manager.isCustomizableDebugger());

        cbMakeRequired.setVisible(manager.getModel().showRequiredBuildTools());
        cbDebuggerRequired.setVisible(manager.getModel().showRequiredDebugTools() && manager.isCustomizableDebugger());
        cbCppRequired.setVisible(manager.getModel().showRequiredBuildTools());
        cbCRequired.setVisible(manager.getModel().showRequiredBuildTools());
        cbFortranRequired.setVisible(manager.getModel().showRequiredBuildTools());
        cbQMakeRequired.setVisible(manager.getModel().showRequiredBuildTools());
        cbAsRequired.setVisible(manager.getModel().showRequiredBuildTools());
    }

    void removeCompilerSet() {
        lbFamilyValue.setText(""); // NOI18N
        tfBaseDirectory.setText(""); // NOI18N
        tfCPath.setText(""); // NOI18N
        tfCppPath.setText(""); // NOI18N
        tfFortranPath.setText(""); // NOI18N
        tfAsPath.setText(""); // NOI18N
        tfMakePath.setText(""); // NOI18N
        tfDebuggerPath.setText(""); // NOI18N
        tfQMakePath.setText(""); // NOI18N
        tfCMakePath.setText(""); // NOI18N
    }

    void updateCompilerSet(CompilerSet cs, boolean force) {
        if (cs.isUrlPointer()) {
            return;
        }
        if (force) {
            APIAccessor.get().setToolPath(cs.getTool(PredefinedToolKind.CCompiler),tfCPath.getText());
            APIAccessor.get().setToolPath(cs.getTool(PredefinedToolKind.CCCompiler),tfCppPath.getText());
            APIAccessor.get().setToolPath(cs.getTool(PredefinedToolKind.FortranCompiler),tfFortranPath.getText());
            APIAccessor.get().setToolPath(cs.getTool(PredefinedToolKind.Assembler),tfAsPath.getText());
            APIAccessor.get().setToolPath(cs.getTool(PredefinedToolKind.MakeTool),tfMakePath.getText());
            APIAccessor.get().setToolPath(cs.getTool(PredefinedToolKind.DebuggerTool),tfDebuggerPath.getText());
            APIAccessor.get().setToolPath(cs.getTool(PredefinedToolKind.QMakeTool),tfQMakePath.getText());
            APIAccessor.get().setToolPath(cs.getTool(PredefinedToolKind.CMakeTool),tfCMakePath.getText());
        } else {
            APIAccessor.get().setToolPath(cs.findTool(PredefinedToolKind.CCompiler),tfCPath.getText());
            APIAccessor.get().setToolPath(cs.findTool(PredefinedToolKind.CCCompiler),tfCppPath.getText());
            APIAccessor.get().setToolPath(cs.findTool(PredefinedToolKind.FortranCompiler),tfFortranPath.getText());
            APIAccessor.get().setToolPath(cs.findTool(PredefinedToolKind.Assembler),tfAsPath.getText());
            APIAccessor.get().setToolPath(cs.findTool(PredefinedToolKind.MakeTool),tfMakePath.getText());
            APIAccessor.get().setToolPath(cs.findTool(PredefinedToolKind.DebuggerTool),tfDebuggerPath.getText());
            APIAccessor.get().setToolPath(cs.findTool(PredefinedToolKind.QMakeTool),tfQMakePath.getText());
            APIAccessor.get().setToolPath(cs.findTool(PredefinedToolKind.CMakeTool),tfCMakePath.getText());
        }
    }

    void applyChanges() {
        ToolsPanelModel model = manager.getModel();
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
            if (model.isQMakeRequired() != cbQMakeRequired.isSelected()) {
                model.setFortranRequired(cbQMakeRequired.isSelected());
            }
        }
    }

    void preChangeCompilerSet(CompilerSet cs) {
        if (cs == null) {
            lbFamilyValue.setText(""); // NOI18N
            updateToolsControls(false, false, true);
            return;
        }
        if (cs.isUrlPointer()) {
            isUrl = true;
            String selected = cs.getCompilerFlavor().toString();
            String name = cs.getDisplayName();
            String uc = cs.getCompilerFlavor().getToolchainDescriptor().getUpdateCenterUrl();
            String message = ToolsPanel.getString("ToolsPanel.UpdateCenterMessage", selected, name, uc);
            tpInstall.setText(message);
            tpInstall.setBackground(getBackground());
            tpInstall.select(tpInstall.getDocument().getLength()-1, tpInstall.getDocument().getLength()-1);
        } else {
            isUrl = false;
            tfBaseDirectory.setText(cs.getDirectory());
        }
        scrollPane.setVisible(isUrl);
        btInstall.setVisible(isUrl);

        lbFamily.setVisible(!isUrl);
        lbFamilyValue.setVisible(!isUrl);
        lbAsCommand.setVisible(!isUrl);
        lbBaseDirectory.setVisible(!isUrl);
        lbCCommand.setVisible(!isUrl);
        lbCMakePath.setVisible(!isUrl);
        lbCppCommand.setVisible(!isUrl);
        lbDebuggerCommand.setVisible(!isUrl && manager.isCustomizableDebugger());
        lbFortranCommand.setVisible(!isUrl);
        lbMakeCommand.setVisible(!isUrl);
        lbQMakePath.setVisible(!isUrl);

        tfAsPath.setVisible(!isUrl);
        tfBaseDirectory.setVisible(!isUrl);
        tfCMakePath.setVisible(!isUrl);
        tfCPath.setVisible(!isUrl);
        tfCppPath.setVisible(!isUrl);
        tfDebuggerPath.setVisible(!isUrl && manager.isCustomizableDebugger());
        tfFortranPath.setVisible(!isUrl);
        tfMakePath.setVisible(!isUrl);
        tfQMakePath.setVisible(!isUrl);

        btAsBrowse.setVisible(!isUrl);
        btCBrowse.setVisible(!isUrl);
        btCMakeBrowse.setVisible(!isUrl);
        btCppBrowse.setVisible(!isUrl);
        btDebuggerBrowse.setVisible(!isUrl && manager.isCustomizableDebugger());
        btFortranBrowse.setVisible(!isUrl);
        btMakeBrowse.setVisible(!isUrl);
        btQMakeBrowse.setVisible(!isUrl);

        lbFamilyValue.setText(cs.getCompilerFlavor().toString());
    }

    void changeCompilerSet(CompilerSet cs) {
        Tool cSelection = null;
        Tool cppSelection = null;
        Tool fortranSelection = null;
        Tool asSelection = null;
        Tool makeToolSelection = null;
        Tool debuggerToolSelection = null;
        Tool qmakeToolSelection = null;
        Tool cmakeToolSelection = null;
        if (!cs.isUrlPointer()) {
            cSelection = cs.getTool(PredefinedToolKind.CCompiler);
            cppSelection = cs.getTool(PredefinedToolKind.CCCompiler);
            fortranSelection = cs.getTool(PredefinedToolKind.FortranCompiler);
            asSelection = cs.getTool(PredefinedToolKind.Assembler);
            makeToolSelection = cs.getTool(PredefinedToolKind.MakeTool);
            debuggerToolSelection = cs.getTool(PredefinedToolKind.DebuggerTool);
            qmakeToolSelection = cs.getTool(PredefinedToolKind.QMakeTool);
            cmakeToolSelection = cs.getTool(PredefinedToolKind.CMakeTool);
        }
        if (cSelection != null) {
            setCPathField(cSelection.getPath());
        } else {
            tfCPath.setText(""); // NOI18N
        }
        if (cppSelection != null) {
            setCppPathField(cppSelection.getPath());
        } else {
            tfCppPath.setText(""); // NOI18N
        }
        if (fortranSelection != null) {
            setFortranPathField(fortranSelection.getPath());
        } else {
            tfFortranPath.setText(""); // NOI18N
        }
        if (asSelection != null) {
            setAsPathField(asSelection.getPath());
        } else {
            tfAsPath.setText(""); // NOI18N
        }
        if (qmakeToolSelection != null) {
            setQMakePathField(qmakeToolSelection.getPath());
        } else {
            tfQMakePath.setText(""); // NOI18N
        }
        if (cmakeToolSelection != null) {
            setCMakePathField(cmakeToolSelection.getPath());
        } else {
            tfCMakePath.setText(""); // NOI18N
        }
        if (makeToolSelection != null) {
            setMakePathField(makeToolSelection.getPath());
        } else {
            tfMakePath.setText(""); // NOI18N
        }
        if (debuggerToolSelection != null) {
            setGdbPathField(debuggerToolSelection.getPath());
        } else {
            tfDebuggerPath.setText(""); // NOI18N
        }
    }

    private void setMakePathField(String path) {
        tfMakePath.setText(path); // Validation happens automatically
    }

    private void validateMakePathField() {
        setPathFieldValid(tfMakePath, isPathFieldValid(tfMakePath) && supportedMake(tfMakePath));
        manager.dataValid();
    }

    private void setGdbPathField(String path) {
        tfDebuggerPath.setText(path); // Validation happens automatically
    }

    private void validateGdbPathField() {
        setPathFieldValid(tfDebuggerPath, isPathFieldValid(tfDebuggerPath));
        manager.dataValid();
    }

    private void setCPathField(String path) {
        tfCPath.setText(path); // Validation happens automatically
    }

    private void validateCPathField() {
        setPathFieldValid(tfCPath, isPathFieldValid(tfCPath));
        manager.dataValid();
    }

    private void setCppPathField(String path) {
        tfCppPath.setText(path); // Validation happens automatically
    }

    private void validateCppPathField() {
        setPathFieldValid(tfCppPath, isPathFieldValid(tfCppPath));
        manager.dataValid();
    }

    private void setFortranPathField(String path) {
        tfFortranPath.setText(path); // Validation happens automatically
    }

    private void validateFortranPathField() {
        setPathFieldValid(tfFortranPath, isPathFieldValid(tfFortranPath));
        manager.dataValid();
    }

    private void setAsPathField(String path) {
        tfAsPath.setText(path); // Validation happens automatically
    }

    private void validateAsPathField() {
        setPathFieldValid(tfAsPath, isPathFieldValid(tfAsPath));
        manager.dataValid();
    }

    private void setQMakePathField(String path) {
        tfQMakePath.setText(path); // Validation happens automatically
    }

    private void validateQMakePathField() {
        setPathFieldValid(tfQMakePath, isPathFieldValid(tfQMakePath));
        manager.dataValid();
    }

    private void setCMakePathField(String path) {
        tfCMakePath.setText(path); // Validation happens automatically
    }

    private void validateCMakePathField() {
        setPathFieldValid(tfCMakePath, isPathFieldValid(tfCMakePath));
        manager.dataValid();
    }

    private void setPathFieldValid(JTextField field, boolean valid) {
        if (valid) {
            field.setForeground(tfColor);
        } else {
            field.setForeground(Color.RED);
        }
    }

    private boolean supportedMake(JTextField field) {
        String txt = field.getText();
        if (txt.length() == 0) {
            return false;
        }
        return !ToolsPanelSupport.isUnsupportedMake(txt);
    }

    boolean isToolsValid() {
        boolean makeValid = cbMakeRequired.isSelected() ? isPathFieldValid(tfMakePath) && supportedMake(tfMakePath) : true;
        boolean debuggerValid = cbDebuggerRequired.isSelected() ? isPathFieldValid(tfDebuggerPath) : true;
        boolean cValid = cbCRequired.isSelected() ? isPathFieldValid(tfCPath) : true;
        boolean cppValid = cbCppRequired.isSelected() ? isPathFieldValid(tfCppPath) : true;
        boolean fortranValid = cbFortranRequired.isSelected() ? isPathFieldValid(tfFortranPath) : true;
        boolean qmakeValid = cbQMakeRequired.isSelected() ? isPathFieldValid(tfQMakePath) : true;
        boolean asValid = cbAsRequired.isSelected() ? isPathFieldValid(tfAsPath) : true;
        return makeValid && debuggerValid && cValid && cppValid && fortranValid && asValid && qmakeValid;
    }

    void getErrors(List<String> errors) {
        boolean makeValid = cbMakeRequired.isSelected() ? isPathFieldValid(tfMakePath) && supportedMake(tfMakePath) : true;
        boolean debuggerValid = cbDebuggerRequired.isSelected() ? isPathFieldValid(tfDebuggerPath) : true;
        boolean cValid = cbCRequired.isSelected() ? isPathFieldValid(tfCPath) : true;
        boolean cppValid = cbCppRequired.isSelected() ? isPathFieldValid(tfCppPath) : true;
        boolean fortranValid = cbFortranRequired.isSelected() ? isPathFieldValid(tfFortranPath) : true;
        boolean qmakeValid = cbQMakeRequired.isSelected() ? isPathFieldValid(tfQMakePath) : true;
        boolean asValid = cbAsRequired.isSelected() ? isPathFieldValid(tfAsPath) : true;
        if (cbMakeRequired.isSelected() && !makeValid) {
            if (!isPathFieldValid(tfMakePath)) {
                errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedMake")); // NOI18N
            } else {
                errors.add(ToolsPanel.getString("TP_ErrorMessage_UnsupportedMake", "mingw32-make")); // NOI18N
            }
        }
        if (cbCRequired.isSelected() && !cValid) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedCCompiler")); // NOI18N
        }
        if (cbCppRequired.isSelected() && !cppValid) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedCppCompiler")); // NOI18N
        }
        if (cbDebuggerRequired.isSelected() && !debuggerValid && manager.isCustomizableDebugger()) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedDebugger")); // NOI18N
        }
        if (cbFortranRequired.isSelected() && !fortranValid) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedFortranCompiler")); // NOI18N
        }
        if (cbQMakeRequired.isSelected() && !qmakeValid) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedQMake")); // NOI18N
        }
        if (cbAsRequired.isSelected() && !asValid) {
            errors.add(ToolsPanel.getString("TP_ErrorMessage_MissedAssembler")); // NOI18N
        }
    }

    void updateToolsControls(boolean enableText, boolean enableVersions, boolean cleanText) {
        updateTextField(tfMakePath, enableText, cleanText);
        updateTextField(tfDebuggerPath, enableText, cleanText);
        updateTextField(tfBaseDirectory, false, cleanText);
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

    private boolean isPathFieldValid(JTextField field) {
        String txt = field.getText();
        if (txt.length() == 0) {
            return false;
        }

        if (manager.getExecutionEnvironment().isLocal()) {
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
                for (String p : Path.getPath()) {
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

    private boolean selectCompiler(JTextField tf) {
        String seed = tf.getText();
        if (seed.length() > 0 && ! seed.endsWith("/")) { //NOI18N
            int pos = seed.lastIndexOf("/"); //NOI18N
            if (pos > 0) {
                seed = seed.substring(0, pos);
            }
        }
        JFileChooser fileChooser = new FileChooserBuilder(manager.getExecutionEnvironment()).createFileChooser(seed);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(ToolsPanel.getString("SELECT_TOOL_TITLE"));
        //fileChooser.setApproveButtonMnemonic(KeyEvent.VK_ENTER);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return false;
        }
        if (!new File(new File(tfBaseDirectory.getText()), fileChooser.getSelectedFile().getName()).exists()) {
            NotifyDescriptor nb = new NotifyDescriptor.Message(ToolsPanel.getString("COMPILER_BASE_ERROR"), NotifyDescriptor.ERROR_MESSAGE); // NOI18N
            DialogDisplayer.getDefault().notify(nb);
            return false;
        }
        String aPath = fileChooser.getSelectedFile().getPath();
        if (Utilities.isWindows()) {
            if (aPath.endsWith(".lnk")) { // NOI18N
                aPath = aPath.substring(0, aPath.length() - 4);
            }
        }
        tf.setText(aPath); // compiler set is updated by textfield's listener
        return true;
    }

    private boolean selectTool(JTextField tf) {
        String seed = tf.getText();
        FileChooser fileChooser = new FileChooser(ToolsPanel.getString("SELECT_TOOL_TITLE"), null, JFileChooser.FILES_ONLY, null, seed, false); // NOI18N
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
        tf.setText(aPath); // compiler set is updated by textfield's listener
        return true;
    }

    // implement DocumentListener
    @Override
    public void changedUpdate(DocumentEvent ev) {
    }

    @Override
    public void insertUpdate(DocumentEvent ev) {
        boolean userChange = !manager.isUpdatindOrChangingCompilerSet();
        if (userChange) {
            manager.setChanged(true);
        }
        Document doc = ev.getDocument();
        String title = (String) doc.getProperty(Document.TitleProperty);
        PredefinedToolKind toolKind = PredefinedToolKind.UnknownTool;
        String toolPath = null;
        if (title.equals(MAKE_NAME)) {
            validateMakePathField();
            toolKind = PredefinedToolKind.MakeTool;
            toolPath = tfMakePath.getText();
        } else if (title.equals(DEBUGGER_NAME)) {
            validateGdbPathField();
            toolKind = PredefinedToolKind.DebuggerTool;
            toolPath = tfDebuggerPath.getText();
        } else if (title.equals(C_NAME)) {
            validateCPathField();
            toolKind = PredefinedToolKind.CCompiler;
            toolPath = tfCPath.getText();
        } else if (title.equals(CPP_NAME)) {
            validateCppPathField();
            toolKind = PredefinedToolKind.CCCompiler;
            toolPath = tfCppPath.getText();
        } else if (title.equals(FORTRAN_NAME)) {
            validateFortranPathField();
            toolKind = PredefinedToolKind.FortranCompiler;
            toolPath = tfFortranPath.getText();
        } else if (title.equals(ASSEMBLER_NAME)) {
            validateAsPathField();
            toolKind = PredefinedToolKind.Assembler;
            toolPath = tfAsPath.getText();
        } else if (title.equals(QMAKE_NAME)) {
            validateQMakePathField();
            toolKind = PredefinedToolKind.QMakeTool;
            toolPath = tfQMakePath.getText();
        } else if (title.equals(CMAKE_NAME)) {
            validateCMakePathField();
            toolKind = PredefinedToolKind.CMakeTool;
            toolPath = tfCMakePath.getText();
        }
        if (userChange && toolKind != PredefinedToolKind.UnknownTool) {
            APIAccessor.get().setToolPath(manager.getCurrentCompilerSet().getTool(toolKind),toolPath);
            manager.fireCompilerSetChange();
            manager.fireCompilerSetModified();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent ev) {
        insertUpdate(ev);
    }

    @Override
    public void itemStateChanged(ItemEvent ev) {
        Object o = ev.getSource();
        if (o instanceof JCheckBox) {
            if (!manager.isUpdatindOrChangingCompilerSet()) {
                manager.dataValid();
            }
        }
    }

    private String getToolVersion(Tool tool, JTextField tf) {
        StringBuilder version = new StringBuilder();
        version.append(tool.getDisplayName()).append(": "); // NOI18N
        if (isPathFieldValid(tf)) {
            String path = tf.getText();
            if (!ToolUtils.isPathAbsolute(path)) {
                path = Path.findCommand(path);
            }
            String v = postVersionInfo(tool, path);
            if (v != null) {
                version.append(v);
            } else {
                version.append(ToolsPanel.getString("TOOL_VERSION_NOT_FOUND")); // NOI18N
            }
        } else {
            version.append(ToolsPanel.getString("TOOL_NOT_FOUND")); // NOI18N
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

    String getVersion(CompilerSet cs){
        ProgressHandle handle = ProgressHandleFactory.createHandle(ToolsPanel.getString("LBL_VersionInfo_Progress")); // NOI18N
        handle.start(manager.isCustomizableDebugger() ? 8 : 7);

        StringBuilder versions = new StringBuilder();
        int i = 0;
        versions.append('\n'); // NOI18N
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.CCompiler), tfCPath)).append('\n'); // NOI18N
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.CCCompiler), tfCppPath)).append('\n'); // NOI18N
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.FortranCompiler), tfFortranPath)).append('\n'); // NOI18N
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.Assembler), tfAsPath)).append('\n'); // NOI18N
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.MakeTool), tfMakePath)).append('\n'); // NOI18N
        if (manager.isCustomizableDebugger()) {
            handle.progress(++i);
            versions.append(getToolVersion(cs.findTool(PredefinedToolKind.DebuggerTool), tfDebuggerPath)).append('\n'); // NOI18N
        }
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.QMakeTool), tfQMakePath)).append('\n'); // NOI18N
        handle.progress(++i);
        versions.append(getToolVersion(cs.findTool(PredefinedToolKind.CMakeTool), tfCMakePath)).append('\n'); // NOI18N
        handle.finish();
        String upgradeUrl = cs.getCompilerFlavor().getToolchainDescriptor().getUpgradeUrl();
        if (upgradeUrl != null) {
            versions.append('\n').append(ToolsPanel.getString("TOOL_UPGRADE", upgradeUrl)).append('\n'); // NOI18N
        }
        return versions.toString();
    }

    boolean isBaseDirValid(){
        return !isUrl && new File(tfBaseDirectory.getText()).exists();
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

        lbMakeCommand = new javax.swing.JLabel();
        tfMakePath = new javax.swing.JTextField();
        tfMakePath.getDocument().putProperty(Document.TitleProperty, MAKE_NAME);
        tfMakePath.getDocument().addDocumentListener(this);
        btMakeBrowse = new javax.swing.JButton();
        lbDebuggerCommand = new javax.swing.JLabel();
        tfDebuggerPath = new javax.swing.JTextField();
        tfDebuggerPath.getDocument().putProperty(Document.TitleProperty, DEBUGGER_NAME);
        tfDebuggerPath.getDocument().addDocumentListener(this);
        btDebuggerBrowse = new javax.swing.JButton();
        lbCCommand = new javax.swing.JLabel();
        tfCPath = new javax.swing.JTextField();
        tfCPath.getDocument().putProperty(Document.TitleProperty, C_NAME);
        tfCPath.getDocument().addDocumentListener(this);
        btCBrowse = new javax.swing.JButton();
        lbCppCommand = new javax.swing.JLabel();
        tfCppPath = new javax.swing.JTextField();
        tfCppPath.getDocument().putProperty(Document.TitleProperty, CPP_NAME);
        tfCppPath.getDocument().addDocumentListener(this);
        btCppBrowse = new javax.swing.JButton();
        lbFortranCommand = new javax.swing.JLabel();
        tfFortranPath = new javax.swing.JTextField();
        tfFortranPath.getDocument().putProperty(Document.TitleProperty, FORTRAN_NAME);
        tfFortranPath.getDocument().addDocumentListener(this);
        btFortranBrowse = new javax.swing.JButton();
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
        cbQMakeRequired = new javax.swing.JCheckBox();
        lbBaseDirectory = new javax.swing.JLabel();
        tfBaseDirectory = new javax.swing.JTextField();
        lbAsCommand = new javax.swing.JLabel();
        tfAsPath = new javax.swing.JTextField();
        tfAsPath.getDocument().putProperty(Document.TitleProperty, ASSEMBLER_NAME);
        tfAsPath.getDocument().addDocumentListener(this);
        btAsBrowse = new javax.swing.JButton();
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
        lbFamilyValue = new javax.swing.JLabel();
        btInstall = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        tpInstall = new javax.swing.JTextPane();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle"); // NOI18N
        lbMakeCommand.setText(bundle.getString("ToolCollectionPanel.lbMakeCommand.text")); // NOI18N
        lbMakeCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbMakeCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 0);
        add(lbMakeCommand, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 2, 0, 0);
        add(tfMakePath, gridBagConstraints);

        btMakeBrowse.setText(bundle.getString("ToolCollectionPanel.btMakeBrowse.text")); // NOI18N
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

        lbDebuggerCommand.setText(bundle.getString("ToolCollectionPanel.lbDebuggerCommand.text")); // NOI18N
        lbDebuggerCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbDebuggerCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbDebuggerCommand, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfDebuggerPath, gridBagConstraints);

        btDebuggerBrowse.setText(bundle.getString("ToolCollectionPanel.btDebuggerBrowse.text")); // NOI18N
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

        lbCCommand.setText(bundle.getString("ToolCollectionPanel.lbCCommand.text")); // NOI18N
        lbCCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbCCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbCCommand, gridBagConstraints);

        tfCPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCPath, gridBagConstraints);

        btCBrowse.setText(bundle.getString("ToolCollectionPanel.btCBrowse.text")); // NOI18N
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

        lbCppCommand.setText(bundle.getString("ToolCollectionPanel.lbCppCommand.text")); // NOI18N
        lbCppCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbCppCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbCppCommand, gridBagConstraints);

        tfCppPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCppPath, gridBagConstraints);

        btCppBrowse.setText(bundle.getString("ToolCollectionPanel.btCppBrowse.text")); // NOI18N
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

        lbFortranCommand.setText(bundle.getString("ToolCollectionPanel.lbFortranCommand.text")); // NOI18N
        lbFortranCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbFortranCommand.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbFortranCommand, gridBagConstraints);

        tfFortranPath.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfFortranPath, gridBagConstraints);

        btFortranBrowse.setText(bundle.getString("ToolCollectionPanel.btFortranBrowse.text")); // NOI18N
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

        lbFamily.setText(bundle.getString("ToolCollectionPanel.lbFamily.text")); // NOI18N
        lbFamily.setToolTipText(bundle.getString("ToolCollectionPanel.lbFamily.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(lbFamily, gridBagConstraints);

        requiredToolsLabel.setText(bundle.getString("ToolCollectionPanel.requiredToolsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 0);
        add(requiredToolsLabel, gridBagConstraints);

        requiredToolsPanel.setLayout(new java.awt.GridBagLayout());

        cbMakeRequired.setSelected(true);
        cbMakeRequired.setText(bundle.getString("ToolCollectionPanel.cbMakeRequired.text")); // NOI18N
        cbMakeRequired.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        requiredToolsPanel.add(cbMakeRequired, gridBagConstraints);

        cbDebuggerRequired.setText(bundle.getString("ToolCollectionPanel.cbDebuggerRequired.text")); // NOI18N
        cbDebuggerRequired.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        requiredToolsPanel.add(cbDebuggerRequired, gridBagConstraints);

        cbCRequired.setText(bundle.getString("ToolCollectionPanel.cbCRequired.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        requiredToolsPanel.add(cbCRequired, gridBagConstraints);

        cbCppRequired.setText(bundle.getString("ToolCollectionPanel.cbCppRequired.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        requiredToolsPanel.add(cbCppRequired, gridBagConstraints);

        cbFortranRequired.setText(bundle.getString("ToolCollectionPanel.cbFortranRequired.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        requiredToolsPanel.add(cbFortranRequired, gridBagConstraints);

        cbAsRequired.setText(bundle.getString("ToolCollectionPanel.cbAsRequired.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        requiredToolsPanel.add(cbAsRequired, gridBagConstraints);

        cbQMakeRequired.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.cbQMakeRequired.text")); // NOI18N
        requiredToolsPanel.add(cbQMakeRequired, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 0, 6);
        add(requiredToolsPanel, gridBagConstraints);

        lbBaseDirectory.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.lbBaseDirectory.text")); // NOI18N
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

        lbAsCommand.setText(bundle.getString("ToolCollectionPanel.lbAsCommand.text")); // NOI18N
        lbAsCommand.setToolTipText(bundle.getString("ToolCollectionPanel.lbAsCommand.toolTipText")); // NOI18N
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

        btAsBrowse.setText(bundle.getString("ToolCollectionPanel.btAsBrowse.text")); // NOI18N
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

        lbQMakePath.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.lbQMakePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbQMakePath, gridBagConstraints);

        lbCMakePath.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.lbCMakePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbCMakePath, gridBagConstraints);

        tfQMakePath.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.tfQMakePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfQMakePath, gridBagConstraints);

        tfCMakePath.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.tfCMakePath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCMakePath, gridBagConstraints);

        btQMakeBrowse.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.btQMakeBrowse.text")); // NOI18N
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

        btCMakeBrowse.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.btCMakeBrowse.text")); // NOI18N
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

        lbFamilyValue.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolCollectionPanel.lbFamilyValue.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        add(lbFamilyValue, gridBagConstraints);

        btInstall.setText(org.openide.util.NbBundle.getMessage(ToolCollectionPanel.class, "ToolsPanel.UpdateCenterInstallButton")); // NOI18N
        btInstall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInstallActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        add(btInstall, gridBagConstraints);

        scrollPane.setBorder(null);
        scrollPane.setMinimumSize(new java.awt.Dimension(200, 200));
        scrollPane.setPreferredSize(new java.awt.Dimension(200, 200));
        scrollPane.setViewportView(tpInstall);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(scrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void btMakeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btMakeBrowseActionPerformed
        selectTool(tfMakePath);
}//GEN-LAST:event_btMakeBrowseActionPerformed

    private void btDebuggerBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDebuggerBrowseActionPerformed
        selectTool(tfDebuggerPath);
}//GEN-LAST:event_btDebuggerBrowseActionPerformed

    private void btCBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCBrowseActionPerformed
        selectCompiler(tfCPath);
}//GEN-LAST:event_btCBrowseActionPerformed

    private void btCppBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCppBrowseActionPerformed
        selectCompiler(tfCppPath);
}//GEN-LAST:event_btCppBrowseActionPerformed

    private void btFortranBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFortranBrowseActionPerformed
        selectCompiler(tfFortranPath);
}//GEN-LAST:event_btFortranBrowseActionPerformed

    private void btAsBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAsBrowseActionPerformed
        selectCompiler(tfAsPath);
}//GEN-LAST:event_btAsBrowseActionPerformed

    private void btQMakeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btQMakeBrowseActionPerformed
        selectTool(tfQMakePath);
}//GEN-LAST:event_btQMakeBrowseActionPerformed

    private void btCMakeBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCMakeBrowseActionPerformed
        selectTool(tfCMakePath);
}//GEN-LAST:event_btCMakeBrowseActionPerformed

    private void btInstallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInstallActionPerformed
        CompilerSet cs = manager.getCurrentCompilerSet();
        DownloadUtils.downloadCompilerSet(cs);
    }//GEN-LAST:event_btInstallActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAsBrowse;
    private javax.swing.JButton btCBrowse;
    private javax.swing.JButton btCMakeBrowse;
    private javax.swing.JButton btCppBrowse;
    private javax.swing.JButton btDebuggerBrowse;
    private javax.swing.JButton btFortranBrowse;
    private javax.swing.JButton btInstall;
    private javax.swing.JButton btMakeBrowse;
    private javax.swing.JButton btQMakeBrowse;
    private javax.swing.JCheckBox cbAsRequired;
    private javax.swing.JCheckBox cbCRequired;
    private javax.swing.JCheckBox cbCppRequired;
    private javax.swing.JCheckBox cbDebuggerRequired;
    private javax.swing.JCheckBox cbFortranRequired;
    private javax.swing.JCheckBox cbMakeRequired;
    private javax.swing.JCheckBox cbQMakeRequired;
    private javax.swing.JLabel lbAsCommand;
    private javax.swing.JLabel lbBaseDirectory;
    private javax.swing.JLabel lbCCommand;
    private javax.swing.JLabel lbCMakePath;
    private javax.swing.JLabel lbCppCommand;
    private javax.swing.JLabel lbDebuggerCommand;
    private javax.swing.JLabel lbFamily;
    private javax.swing.JLabel lbFamilyValue;
    private javax.swing.JLabel lbFortranCommand;
    private javax.swing.JLabel lbMakeCommand;
    private javax.swing.JLabel lbQMakePath;
    private javax.swing.JLabel requiredToolsLabel;
    private javax.swing.JPanel requiredToolsPanel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextField tfAsPath;
    private javax.swing.JTextField tfBaseDirectory;
    private javax.swing.JTextField tfCMakePath;
    private javax.swing.JTextField tfCPath;
    private javax.swing.JTextField tfCppPath;
    private javax.swing.JTextField tfDebuggerPath;
    private javax.swing.JTextField tfFortranPath;
    private javax.swing.JTextField tfMakePath;
    private javax.swing.JTextField tfQMakePath;
    private javax.swing.JTextPane tpInstall;
    // End of variables declaration//GEN-END:variables

}
