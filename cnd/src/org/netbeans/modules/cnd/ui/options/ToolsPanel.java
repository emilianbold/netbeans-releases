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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.modules.cnd.MIMENames;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** Display the "Tools Default" panel */
public class ToolsPanel extends JPanel implements ActionListener, DocumentListener,
        ListSelectionListener, ItemListener {
    
    // The following are constants so I can do == rather than "equals"
    private final String MAKE_NAME = "make"; // NOI18N
    private final String GDB_NAME = "gdb"; // NOI18N
    private final String C_NAME = "C"; // NOI18N
    private final String CPP_NAME = "C++"; // NOI18N
    private final String FORTRAN_NAME = "Fortran"; // NOI18N
    
//    private static final String DIRECTORY_MOVE_UP = "Up"; // NOI18N
//    private static final String DIRECTORY_MOVE_DOWN = "Down"; // NOI18N
    
    public static final String PROP_VALID = "valid"; // NOI18N
    
    private boolean initialized = false;
    private boolean changed;
    private boolean changingCompilerSet;
    private boolean updating;
    private boolean valid;
//    private static ArrayList<String> dirlist = null;
    private ToolsPanelModel model = null;
    private Color tfColor = null;
    private boolean gdbEnabled;
    
//    private Tool cCommandSelection = null;
//    private Tool cppCommandSelection = null;
//    private Tool fortranCommandSelection = null;
    
    private static ToolsPanel instance = null;
    
//    /** The default (or previously selected) C compiler for each CompilerSet */
//    private HashMap<String, String> cSelections;
//    
//    /** The default (or previously selected) C++ compiler for each CompilerSet */
//    private HashMap<String, String> cppSelections;
//    
//    /** The default (or previously selected) Fortran compiler for each CompilerSet */
//    private HashMap<String, String> fortranSelections;
    
//    private JFileChooser addDirectoryChooser;
    private CompilerSetManager csm;
    private CompilerSet currentCompilerSet;
    
    /** Creates new form ToolsPanel */
    public ToolsPanel() {
        initComponents();
        setName("TAB_ToolsTab"); // NOI18N (used as a pattern...)
        cbGdbRequired.setName("gdb"); // NOI18N
        cbCRequired.setName("c"); // NOI18N
        cbCppRequired.setName("c++"); // NOI18N
        cbFortranRequired.setName("fortran"); // NOI18N
        changed = false;
        instance = this;
        currentCompilerSet = null;
        
        errorTextArea.setText("");
        errorTextArea.setBackground(jPanel1.getBackground());
        
        lstDirlist.setCellRenderer(new MyCellRenderer());
        
    }
    
    class MyCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            CompilerSet cs = (CompilerSet)value;
            if (cs.isDefault()) {
                JLabel label = (JLabel) comp;
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            }
            return comp;
        }
        
    }
    
    public ToolsPanel(ToolsPanelModel model) {
        this();
        this.model = model;
    }
    
    private void initialize() {
        changingCompilerSet = true;
        if (model == null) {
            model = new GlobalToolsPanelModel();
        }
        if (!model.showRequiredTools()) {
            jLabel1.setVisible(false); // Required Tools label!
            jPanel1.setVisible(false); // Required Tools panel!
        }
        
        if (model.enableRequiredCompilerCB()) {
            cbCRequired.setEnabled(true);
            cbCppRequired.setEnabled(true);
            cbFortranRequired.setEnabled(true);
        }
        else {
            cbCRequired.setEnabled(false);
            cbCppRequired.setEnabled(false);
            cbFortranRequired.setEnabled(false);
        }
//        if (dirlist == null) {
//            // Take a copy ...
//            dirlist = new ArrayList<String>();
//            dirlist.addAll(Path.getPath());
//        }
////        dirlist = model.getPath();
//        if (csm == null) {
            csm = (CompilerSetManager)CompilerSetManager.getDefault().deepCopy(); // FIXUP: need a real deep copy...
            if (csm.getCompilerSets().size() == 1 && csm.getCompilerSets().get(0).getName() == CompilerSet.None) {
                csm.remove(csm.getCompilerSets().get(0));
            }
//        }
        gdbEnabled = IpeUtils.isGdbEnabled();
        
//        cSelections = new HashMap();
//        cppSelections = new HashMap();
//        fortrancSelectionsSelections = new HashMap();
//        addDirectoryChooser = null;
        
//        tfMakeCommand.setText(model.getMakeName());
//        tfGdbCommand.setText(model.getGdbName());
        
        // Assume fortran is enabled externally before the initial display of this dialog
        boolean fortran = CppSettings.getDefault().isFortranEnabled();
        lbFortranCommand.setVisible(fortran);
//        cbFortranCommand.setVisible(fortran);
        tfFortranPath.setVisible(fortran);
        btFortranVersion.setVisible(fortran);
        cbFortranRequired.setVisible(fortran);
        
        // Initialize Required tools. Can't do it in constructor because there is no model then.
        cbMakeRequired.setSelected(model.isMakeRequired());
        cbGdbRequired.setSelected(model.isGdbRequired());
        cbCRequired.setSelected(model.isCRequired());
        cbCppRequired.setSelected(model.isCppRequired());
        cbFortranRequired.setSelected(model.isFortranRequired());
        
//        cbCompilerSet.removeAllItems();
    } 
    
    private void addDirectory() {
        AddCompilerSetPanel panel = new AddCompilerSetPanel(csm);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, getString("NEW_TOOL_SET_TITLE"));
        panel.setDialogDescriptor(dialogDescriptor);
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION)
            return;
        String baseDirectory = panel.getBaseDirectory();
        CompilerSet.CompilerFlavor flavor = panel.getFamily();
        String compilerSetName = panel.getCompilerSetName().trim();
        
        CompilerSet cs = CompilerSet.getCustomCompilerSet(new File(baseDirectory).getAbsolutePath(), flavor, compilerSetName);
        CompilerSetManager.getDefault().initCompilerSet(cs);
        csm.add(cs);
        changed = true;
        update(false, cs);
    }
    
    private void removeDirectory() {
        CompilerSet cs = (CompilerSet)lstDirlist.getSelectedValue();
        if (cs != null) {
            int index = csm.getCompilerSets().indexOf(cs);
            csm.remove(cs);
            if (cs.isDefault()) {
                if (csm.getCompilerSets().size() > 0)
                    csm.getCompilerSet(0).setAsDefault(true);
            }
            if (index >= 0 && index < csm.getCompilerSets().size())
                update(false, csm.getCompilerSets().get(index));
            else if (index > 0)
                update(false, csm.getCompilerSets().get(index-1));
            else
                update(false);
        }
//        int idx = lstDirlist.getSelectedIndex();
//        assert idx != -1; // the button shouldn't be enabled
//        if (checkForCSRemoval(idx)) {
//            String dir = dirlist.remove(idx);
//            changed = true;
//            if (dirlist.size() > 0) {
//                if (idx == dirlist.size()) {
//                    idx--;
//                }
//            }
//
//            try {
//                for (CompilerSet cs : csm.getCompilerSets()) {
//                    StringTokenizer tok = new StringTokenizer(cs.getDirectory(), File.pathSeparator);
//                    while (tok.hasMoreTokens()) {
//                        String d = tok.nextToken();
//                        if (d.equals(dir)) {
//                            csm = new CompilerSetManager(dirlist);
//                            CompilerSet.removeCompilerSet(cs); // CompilerSet has it's own cache!!!!!!!!
//                            return;
//                        }
//                    }
//                }
//            } finally {
//                if (changed) {
//                    update(false);
//                }
//            }
//        }
    }
    
    private void setSelectedAsDefault() {
        CompilerSet cs = (CompilerSet)lstDirlist.getSelectedValue();
        csm.setDefault(cs);
        update(false);
        
    }
//    
//    private void moveDirectory(String direction) {
//        assert direction == DIRECTORY_MOVE_UP || direction == DIRECTORY_MOVE_DOWN;
//        int idx = lstDirlist.getSelectedIndex();
//        assert idx != -1; // the button shouldn't be enabled
//        String dir = dirlist.get(idx);
//        dirlist.remove(idx);
//        changed = true;
//        if (direction == DIRECTORY_MOVE_UP) {
//            idx--;
//        } else {
//            idx++; 
//        }
//        dirlist.add(idx, dir);
//        lstDirlist.setSelectedIndex(idx);
//        // Update compiler sets
//        csm = new CompilerSetManager(dirlist);
//        update(false);
//    }
//    
//    /**
//     * The user has pressed the Remove button id the directories list and the removal
//     * of the selected directory will result in the elimination of a compiler set. Ask
//     * the user if thats what they want and warn them some projects may be converted to
//     * another compiler collection. If the compiler set is the only one, tell the user
//     * its not allowed and stop the action.
//     *
//     * @param idx The index of the selected directory
//     * @returns True if the user OK's removal or removal doesn't cause elimination of a compiler collection
//     */
//    private boolean checkForCSRemoval(int idx) {
//        String dir = dirlist.get(idx);
//        int count = 0;
//        
//        for (String d : dirlist) {
//            if (d.equals(dir)) {
//                count++;
//            }
//        }
//        if (count > 1) {
//            return true; // its OK to remove a duplicate directory...
//        }
//        
//        for (CompilerSet cs : csm.getCompilerSets()) {
//            String csdirs = cs.getDirectory();
//            StringTokenizer tok = new StringTokenizer(csdirs, File.pathSeparator);
//            while (tok.hasMoreTokens()) {
//                String d = tok.nextToken();
//                if (d.equals(dir)) {
//                    if (csm.getCompilerSets().size() == 1) {
//                        return suppressCSRemoval(dir, cs.getDisplayName());
//                    } else {
//                        return warnAboutCSRemoval(dir, cs.getDisplayName());
//                    }
//                }
//            }
//        }
//        return true;
//    }
//    
//    /**
//     * Post a confirmation dialog about removing a compiler collection.
//     *
//     * @param dir The directory about to be removed
//     * @param csname The Compiler Collection about to be eliminated
//     * @returns True if the user pressed OK, false otherwise
//     */
//    private boolean warnAboutCSRemoval(String dir, String csname) {
//        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
//                NbBundle.getMessage(ToolsPanel.class, "WARN_CSRemovalPending", dir, csname), // NOI18N
//                NbBundle.getMessage(ToolsPanel.class, "LBL_PendingCSRemoval_Title"), // NOI18N
//                NotifyDescriptor.OK_CANCEL_OPTION);
//        Object ret = DialogDisplayer.getDefault().notify(nd);
//        return ret == NotifyDescriptor.OK_OPTION;
//    }
//    
//    /**
//     * Post a warning dialog telling the user they can't remove the last compiler collection.
//     *
//     * @param dir The directory about to be removed
//     * @param csname The Compiler Collection about to be eliminated
//     * @returns false
//     */
//    private boolean suppressCSRemoval(String dir, String csname) {
//        NotifyDescriptor nb = new NotifyDescriptor.Message(
//                NbBundle.getMessage(ToolsPanel.class, "WARN_CSRemovalDisallowed", dir, csname),
//                NotifyDescriptor.ERROR_MESSAGE); // NOI18N
//        nb.setTitle(NbBundle.getMessage(ToolsPanel.class, "LBL_CSRemovalDisallowed_Title")); // NOI18N
//        DialogDisplayer.getDefault().notify(nb);
//        return false;
//    }
    
    private void setMakePathField(String path) {
        tfMakePath.setText(path); // Validation happens automatically
    }
    
    private void validateMakePathField() {
        setPathFieldValid(tfMakePath, isPathFieldValid(tfMakePath));
        dataValid();
    }
    
    private void setGdbPathField(String path) {
        tfGdbPath.setText(path); // Validation happens automatically
    }
    
    private void validateGdbPathField() {
        setPathFieldValid(tfGdbPath, isPathFieldValid(tfGdbPath));
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
    
    private boolean isPathFieldValid(JTextField field) {
        String txt = field.getText();
        if (txt.length() == 0) {
            return false;
        }
        File file = new File(txt);
        boolean ok = false;
        ok = file.exists() && !file.isDirectory();
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
    }
    
    private void setPathFieldValid(JTextField field, boolean valid) {
        if (valid) {
            field.setForeground(tfColor);
        } else {
            field.setForeground(Color.RED);
        }
    }
    
    
//    private void setGdbPathField(String cmd) {
//        String path = findCommandFromPath(cmd);
//        if (path != null) {
//            tfGdbPath.setForeground(tfColor);
//            tfGdbPath.setText(path);
//        } else {
//            tfGdbPath.setForeground(Color.RED);
//            try {  // Want to output Red text and make data_valid return false
//                Document doc = tfGdbPath.getDocument();
//                doc.remove(0, doc.getLength());
//                doc.insertString(0, NbBundle.getMessage(ToolsPanel.class, "ERR_NotFound"),  // NOI18N
//                        SimpleAttributeSet.EMPTY);
//            } catch (BadLocationException ex) {
//            }
//        }
//        dataValid();
//    }
//    
//    private String findCommandFromPath(String cmd) {
//        File file;
//        String cmd2 = null;
//        
//        if (cmd.length() > 0) {
//            if (Utilities.isWindows() && !cmd.endsWith(".exe")) { // NOI18N
//                cmd2 = cmd + ".exe"; // NOI18N
//            }
//
//            for (String dir : dirlist) {
//                file = new File(dir, cmd);
//                if (file.exists()) {
//                    return file.getAbsolutePath();
//                }
//                if (cmd2 != null) {
//                    file = new File(dir, cmd2);
//                    if (file.exists()) {
//                        return file.getAbsolutePath();
//                    }
//                }
//            }
//        }
//        return null;
//    }
    
    /** Update the display */
    public void update() {
        update(true, null);
    }
    
    private void update(CompilerSet cs) {
        update(true, cs);
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
        
        lbGdbCommand.setVisible(gdbEnabled);
//        tfGdbCommand.setVisible(gdbEnabled);
        tfGdbPath.setVisible(gdbEnabled);
        btGdbVersion.setVisible(gdbEnabled);
//        cbGdbRequired.setVisible(gdbEnabled);
        
        cbMakeRequired.setVisible(model.showRequiredBuildTools());
        cbGdbRequired.setVisible(model.showRequiredDebugTools() && gdbEnabled);
        cbCppRequired.setVisible(model.showRequiredBuildTools());
        cbCRequired.setVisible(model.showRequiredBuildTools());
        cbFortranRequired.setVisible(model.showRequiredBuildTools() && CppSettings.getDefault().isFortranEnabled());
        
        if (doInitialize) {
            // Set Default
            if (!csm.getCompilerSets().isEmpty()) {
                String name = model.getCompilerSetName(); // the default set
                if (name == null) {
                    //Nothing
                }
                if (name.length() == 0 || csm.getCompilerSet(name) == null) {
                    csm.getCompilerSet(0).setAsDefault(true);
                } else {
                    csm.setDefault(csm.getCompilerSet(name));
                }
                String selectedName = model.getSelectedCompilerSetName(); // The selected set
                if (selectedName != null) {
                    selectedCS = csm.getCompilerSet(selectedName);
                    
                }
                if (selectedCS == null)
                    selectedCS = csm.getDefaultCompilerSet();
            }
        }
        
        if (selectedCS == null)
            selectedCS = (CompilerSet)lstDirlist.getSelectedValue();
        lstDirlist.setListData(csm.getCompilerSets().toArray());
        if (selectedCS != null) {
            lstDirlist.setSelectedValue(selectedCS, true); // FIXUP: should use name
        }
        if (lstDirlist.getSelectedIndex() < 0) {
            lstDirlist.setSelectedIndex(0);
        }
//        setMakePathField(tfMakeCommand.getText());
//        setGdbPathField(tfGdbCommand.getText());
//        updateCompilers();
        updating = false;
        dataValid();
        initialized = true;
    }
//    
//    private void updateCompilers() {
//        
//        if (!csm.getCompilerSets().isEmpty()) {
//            String name, dname;
//            
//            if (cbCompilerSet.getItemCount() > 0) {
//                name = ((CompilerSet) cbCompilerSet.getSelectedItem()).getName();
//                dname = ((CompilerSet) cbCompilerSet.getSelectedItem()).getDisplayName();
//            } else {
//                name = model.getCompilerSetName();
//                if (name.length() == 0 || csm.getCompilerSet(name) == null) {
//                    csm.getCompilerSet(0).setAsDefault(true);
//                    name = CompilerSetManager.getDefault().getCompilerSet(0).getName();
//                    dname = CompilerSetManager.getDefault().getCompilerSet(0).getDisplayName();
//                } else {
//                    csm.getCompilerSet(name).setAsDefault(true);
//                    name = csm.getCompilerSet(name).getName(); // Sun12 will fallback match any Sun release
//                    dname = csm.getCompilerSet(name).getDisplayName();
//                }
//            }
//            cbCompilerSet.removeAllItems();
//            for (CompilerSet cs : csm.getCompilerSets()) {
//                cbCompilerSet.addItem(cs);
//            }
//            
//            CompilerSet cs = csm.getCompilerSet(name, dname);
//            if (cs == null) {
//                cs = csm.getCompilerSet(0);
//            }
//            cbCompilerSet.setSelectedItem(cs);
//            changeCompilerSet(cs);
//        } else {
//            cbCompilerSet.removeAllItems();
//            changeCompilerSet(null);
//        }
//    }
    
    private void changeCompilerSet(CompilerSet cs) {
//        boolean fortran = CppSettings.getDefault().isFortranEnabled();
//        Tool fortranSelection = null;
        if (cs != null) {
            tfBaseDirectory.setText(cs.getDirectory());
            cbFamily.removeAllItems();
            List<CompilerFlavor> list = CompilerFlavor.getFlavors();
            for (CompilerFlavor cf : list)
                cbFamily.addItem(cf);
            cbFamily.setSelectedItem(cs.getCompilerFlavor());
        }
        else {
            tfBaseDirectory.setText(""); // NOI18N
            cbFamily.removeAllItems();
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
            tool = currentCompilerSet.findTool(Tool.MakeTool);
            tool.setPath(tfMakePath.getText());
            tool = currentCompilerSet.findTool(Tool.DebuggerTool);
            tool.setPath(tfGdbPath.getText());
        }
        
        
        changingCompilerSet = true;
        
//        cbCCommand.removeAllItems();
//        cbCppCommand.removeAllItems();
//        cbFortranCommand.removeAllItems();
        
        Tool cSelection = cs.getTool(Tool.CCompiler);
        Tool cppSelection = cs.getTool(Tool.CCCompiler);
        Tool fortranSelection = cs.getTool(Tool.FortranCompiler);
        Tool makeToolSelection = cs.getTool(Tool.MakeTool);
        Tool debuggerToolSelection = cs.getTool(Tool.DebuggerTool);
     
//        for (Tool tool : cs.getTools()) {
//            if (tool.getName().length() > 0) {
//                if (tool.getKind() == Tool.CCompiler) {
//                    cbCCommand.addItem(tool);
//                }
//                if (tool.getKind() == Tool.CCCompiler) {
//                    cbCppCommand.addItem(tool);
//                }
//                if (fortran && tool.getKind() == Tool.FortranCompiler) {
//                    cbFortranCommand.addItem(tool);
//                }
//            }
//        }
        if (cSelection != null) {
//            cbCCommand.setSelectedItem(cSelection);
//            cbCCommand.setToolTipText(cSelection.toString());
//            updateCPath(cSelection);
//            cCommandSelection = cSelection;
            setCPathField(cSelection.getPath());
        } else {
            tfCPath.setText("");
//            cbCCommand.setSelectedIndex(0);
        }
        if (cppSelection != null) {
//            cbCppCommand.setSelectedItem(cppSelection);
//            cbCppCommand.setToolTipText(cppSelection.toString());
            //updateCppPath(cppSelection);
//            cppCommandSelection = cppSelection;
            setCppPathField(cppSelection.getPath());
        } else {
            tfCppPath.setText("");
//            cbCppCommand.setSelectedIndex(0);
        }
        if (fortranSelection != null) {
//            cbFortranCommand.setSelectedItem(fortranSelection);
//            cbFortranCommand.setToolTipText(fortranSelection.toString());
//            updateFortranPath(fortranSelection);
//            fortranCommandSelection = fortranSelection;
            setFortranPathField(fortranSelection.getPath());
        } else {
            tfFortranPath.setText("");
//            cbFortranCommand.setSelectedIndex(0);
        }
        setMakePathField(makeToolSelection.getPath());
        setGdbPathField(debuggerToolSelection.getPath());
        changingCompilerSet = false;
        currentCompilerSet = cs;
        fireCompilerSetChange();
        dataValid();
    }
    
//    private void updatePath(Tool tool, int kind) {
//        if (kind == Tool.CCompiler) {
//            updateCPath(tool);
//        } else if (kind == Tool.CCCompiler) {
//            updateCppPath(tool);
//        } else if (kind == Tool.FortranCompiler) {
//            updateFortranPath(tool);
//        }
//    }
////    
//    private void updateCPath(Tool tool) {
//        if (tool != null) {
//            int toollen = tool.getPath().length();
//            int pathlen = tfCPath.getText().length();
//
//            if ((toollen > 0 && pathlen == 0) || (toollen == 0 && pathlen > 0)) {
//                dataValid(); // force fire prop changed on PROP_VALID
//            }
//            tfCPath.setText(tool.getPath());
//            tfCPath.setToolTipText(tool.getPath());
//        } else {
//            tfCPath.setText("");
//            tfCPath.setToolTipText("");
//        }
//    }
//    
//    private void updateCppPath(Tool tool) {
//        if (tool != null) {
//            int toollen = tool.getPath().length();
//            int pathlen = tfCppPath.getText().length();
//
//            if ((toollen > 0 && pathlen == 0) || (toollen == 0 && pathlen > 0)) {
//                dataValid(); // force fire prop changed on PROP_VALID
//            }
//            tfCppPath.setText(tool.getPath());
//            tfCppPath.setToolTipText(tool.getPath());
//        } else {
//            tfCppPath.setText("");
//            tfCppPath.setToolTipText("");
//        }
//    }
//    
//    private void updateFortranPath(Tool tool) {
//        if (tool != null) {
//            int toollen = tool.getPath().length();
//            int pathlen = tfFortranPath.getText().length();
//
//            if ((toollen > 0 && pathlen == 0) || (toollen == 0 && pathlen > 0)) {
//                dataValid(); // force fire prop changed on PROP_VALID
//            }
//            tfFortranPath.setText(tool.getPath());
//            tfFortranPath.setToolTipText(tool.getPath());
//        } else {
//            tfFortranPath.setText("");
//            tfFortranPath.setToolTipText("");
//        }
//    }
//    
//    /**
//     * Set the default compiler for each compiler type. This is useful when the user changes
//     * CompilerSets to one whose default compiler had been changed earlier. This lets it get
//     * restored to the earlier value.
//     *
//     * @param type The compiler type
//     * @param name The display name of the selected compiler
//     */
//    protected void setDefaultCompiler(int type, String name) {
//        CompilerSet cs = (CompilerSet) cbCompilerSet.getSelectedItem();
//        changed = true;
//        
//        if (type == Tool.CCompiler) {
//            cSelections.put(cs.getName(), name);
//        } else if (type == Tool.CCCompiler) {
//            cppSelections.put(cs.getName(), name);
//        } else if (type == Tool.FortranCompiler) {
//            fortranSelections.put(cs.getName(), name);
//        }
//    }
//    
//    /**
//     * Get the default compiler for each compiler type. This is useful when the user changes
//     * CompilerSets to one whose default compiler had been changed earlier. This lets it get
//     * restored to the earlier value.
//     *
//     * @param cs The CompilerSet for whom we want a tool
//     * @param kind The compiler type we want
//     * @returns The Tool for the requested compiler (or null)
//     */
//    protected Tool getDefaultCompiler(CompilerSet cs, int kind) {
//        String name = cs.getName();
//        
//        if (!name.equals(CompilerSet.None)) {
//            if (kind == Tool.CCompiler) {
//                if (cSelections.get(name) == null) {
//                    return cs.getTool(cs.isSunCompiler() ? "cc" : "gcc"); // NOI18N
//                } else {
//                    return cs.getTool(cSelections.get(name));
//                }
//            } else if (kind == Tool.CCCompiler) {
//                if (cppSelections.get(name) == null) {
//                    return cs.getTool(cs.isSunCompiler() ? "CC" : "g++"); // NOI18N
//                } else {
//                    Tool tool = cs.getTool(cppSelections.get(name), kind);
//                    if (tool == null) {
//                        File file = new File(cs.getDirectory(), name);
//                        if (file.exists()) {
//                            tool = cs.addTool(name, cs.getDirectory(), kind);
//                        }
//                    }
//                    return tool;
//                }
//            } else if (kind == Tool.FortranCompiler) {
//                if (fortranSelections.get(name) == null) {
//                    return cs.getTool(cs.isSunCompiler() ? "f90" : "g77"); // NOI18N
//                } else {
//                    return cs.getTool(fortranSelections.get(name));
//                }
//            }
//        }
//        return null;
//    }
    
    public void applyChanges(boolean force) {
        changed = force;
        applyChanges();
    }
    
    /** Apply changes */
    public void applyChanges() {
        if (changed) {
//            CompilerSet cs = (CompilerSet) cbCompilerSet.getSelectedItem();
            CompilerSet cs = (CompilerSet)lstDirlist.getSelectedValue();
            changed = false;
            
            CompilerSetManager.setDefault(csm);
//            model.setPath(dirlist);
//            model.setMakeName(tfMakeCommand.getText());
//            model.setMakePath(tfMakePath.getText());
            cs.getTool(Tool.MakeTool).setPath(tfMakePath.getText());
            cs.getTool(Tool.DebuggerTool).setPath(tfGdbPath.getText());
            cs.getTool(Tool.CCompiler).setPath(tfCPath.getText());
            cs.getTool(Tool.CCCompiler).setPath(tfCppPath.getText());
            cs.getTool(Tool.FortranCompiler).setPath(tfFortranPath.getText());
//            model.setGdbName(tfGdbCommand.getText());
//            model.setGdbPath(tfGdbPath.getText());
            model.setCompilerSetName(csm.getDefaultCompilerSet().getName());
            model.setSelectedCompilerSetName(cs.getName());
//            if (cSelections.get(cs.getName()) != null) {
//                model.setCCompilerName(cSelections.get(cs.getName()));
//            }
//            if (cppSelections.get(cs.getName()) != null) {
//                model.setCppCompilerName(cppSelections.get(cs.getName()));
//            }
//            if (fortranSelections.get(cs.getName()) != null) {
//                model.setFortranCompilerName(fortranSelections.get(cs.getName()));
//            }
            currentCompilerSet = cs;
            fireCompilerSetChange();
            fireCompilerSetModified();
            
            csm.saveToDisk();
        }
        
        if (model != null) { // model is null for Tools->Options if we don't look at C/C++ panel
            // the following don't set changed if changed
            if (model.isGdbRequired() != cbGdbRequired.isSelected()) {
                model.setGdbRequired(cbGdbRequired.isSelected());
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
        }
        instance = null; // remove the global instance
    }
    
    /** What to do if user cancels the dialog (nothing) */
    public void cancel() {
        changed = false;
    }
    
    public static ToolsPanel getToolsPanel() {
        return instance;
    }
    
    public CompilerSetManager getCompilerSetManager() {
        if (csm == null) {
            csm = CompilerSetManager.getDefault();
        }
        return csm;
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
        
        if (updating || changingCompilerSet) {
            return true;
        } else {
            boolean csmValid = csm.getCompilerSets().size() > 0;
            boolean makeValid = cbMakeRequired.isSelected() ? isPathFieldValid(tfMakePath) : true;
            boolean gdbValid = cbGdbRequired.isSelected() ? isPathFieldValid(tfGdbPath) : true;
            boolean cValid = cbCRequired.isSelected() ? isPathFieldValid(tfCPath) : true;
            boolean cppValid = cbCppRequired.isSelected() ? isPathFieldValid(tfCppPath) : true;
            boolean fortranValid = cbFortranRequired.isSelected() ? isPathFieldValid(tfFortranPath) : true;
            
            if (!initialized) {
                valid = !(csmValid && makeValid && gdbValid && cValid && cppValid && fortranValid);
            }

            if (valid != (csmValid && makeValid && gdbValid && cValid && cppValid && fortranValid)) {
                valid = !valid;
                firePropertyChange(PROP_VALID, !valid, valid);
            }
            
            // post errors in error text area
            errorTextArea.setText("");
            errorTextArea.setRows(0);
            if (!valid) {
                ArrayList<String> errors = new ArrayList<String>();
                if (cbMakeRequired.isSelected() && !makeValid) {
                    errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedMake"));
                }
                if (cbCRequired.isSelected() && !cValid) {
                    errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedCCompiler"));
                }
                if (cbCppRequired.isSelected() && !cppValid) {
                    errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedCppCompiler"));
                }
                if (cbGdbRequired.isSelected() && !gdbValid) {
                    errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedDebugger"));
                }
                if (cbFortranRequired.isSelected() && !fortranValid) {
                    errors.add(NbBundle.getBundle(ToolsPanel.class).getString("TP_ErrorMessage_MissedFortranCompiler"));
                }
                StringBuilder errorString = new StringBuilder();
                for (int i = 0; i < errors.size(); i++) {
                    errorString.append(errors.get(i));
                    if (i < errors.size() - 1)
                        errorString.append("\n"); // NOI18N
                }
                errorTextArea.setRows(errors.size());
                errorTextArea.setText(errorString.toString());
                
                validate();
                repaint();
            }
            if (new File(tfBaseDirectory.getText()).exists()) {
                btCVersion.setEnabled(true);
                btCppVersion.setEnabled(true);
                btFortranVersion.setEnabled(true);
                btMakeVersion.setEnabled(true);
                btGdbVersion.setEnabled(true);
                btVersions.setEnabled(true);
                tfMakePath.setEnabled(true);
                tfGdbPath.setEnabled(true);
            }
            else {
                btCVersion.setEnabled(false);
                btCppVersion.setEnabled(false);
                btFortranVersion.setEnabled(false);
                btMakeVersion.setEnabled(false);
                btGdbVersion.setEnabled(false);
                btVersions.setEnabled(false);
                tfMakePath.setEnabled(false);
                tfGdbPath.setEnabled(false);
            }
            
            return valid;
        }
    }
    
    /**
     * Lets caller know if any data has been changed.
     * 
     * @return True if anything has been changed
     */
    public boolean isChanged() {
        return changed;
    }
    
    /**
     * Post version information for tools not considered part of a CompilerSet (make and gdb).
     *
     * @param name The name of the tool (no directory information)
     * @param path The absolute path of the tool
     */
    private void postVersionInfo(String name, String path) {
        postVersionInfo(CompilerFlavor.Unknown, name, path);
    }
    
    /**
     * Display version information for a program pointed to by "path".
     *
     * @param flavor The type of CompilerSet (unknown if we don't care)
     * @param name The name of the tool (no directory information)
     * @param path The absolute path of the tool
     */
    private void postVersionInfo(CompilerFlavor flavor, String name, String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                FileObject fo = FileUtil.toFileObject(file.getCanonicalFile());
                if (fo != null) {
                    String mime = fo.getMIMEType();
                    if (mime.startsWith("application/x-exe") || mime.equals(MIMENames.SHELL_MIME_TYPE)) { // NOI18N
                        RequestProcessor.getDefault().post(new VersionCommand(flavor, name, path));
                    }
                }
            } catch (IOException ex) {
            }
        }
    }
    
//    private void addRemoveUserTool(JComboBox jc, Tool selected, int kind) {
//        CompilerSet cs = (CompilerSet) cbCompilerSet.getSelectedItem();
//        AddRemoveToolPanel panel = new AddRemoveToolPanel(jc, cs);
//        DialogDescriptor dd = new DialogDescriptor(panel, 
//                NbBundle.getMessage(ToolsPanel.class, "TITLE_AddRemoveCompiler" + kind));
//        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
//        dialog.setVisible(true);
//        jc.removeItemListener(this); // Remove or else we'll recurse setting the selection
//        if (dd.getValue() == NotifyDescriptor.OK_OPTION) {
//            List<String> addList = panel.getModel().getAddList();
//            List removeList = panel.getModel().getRemoveList();
//            ArrayList<Tool> rmlist = new ArrayList();
//            for (Object o : removeList) {
//                for (Tool tool : cs.getTools()) {
//                    if (o == tool) {
//                        if (tool == selected) {
//                            selected = null;
//                        }
//                        rmlist.add(tool);
//                        jc.removeItem(tool);
//                    }
//                }
//            }
//            for (Tool tool : rmlist) {
//                cs.getTools().remove(tool);
//            }
//            for (String name : addList) {
//                Tool tool = cs.addTool(name, cs.getDirectory(), kind);
//                jc.addItem(tool);
//                jc.setSelectedItem(tool);
//                selected = tool;
//            }
//        }
//        if (selected != null && cs.getTools().contains(selected)) {
//            jc.setSelectedItem(selected);
//            setDefaultCompiler(kind, selected.getName());
//            updatePath(selected, kind);
//        } else {
//            jc.setSelectedIndex(0);
//            if (jc.getItemAt(0) instanceof String) {
//                updatePath(null, kind);
//            } else {
//                updatePath((Tool) jc.getItemAt(0), kind);
//            }
//        }
//        jc.addItemListener(this);
//    }
    
    static Set<ChangeListener> listenerChanged = new HashSet();
    
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
    
    static Set<ChangeListener> listenerModified = new HashSet();
    
    public static void addCompilerSetModifiedListener(ChangeListener l) {
        listenerModified.add(l);
    }
    
    public static void removeCompilerSetModifiedListener(ChangeListener l) {
        listenerModified.remove(l);
    }
    
    public void fireCompilerSetModified() {
        ChangeEvent ev = new ChangeEvent(currentCompilerSet);
        for (ChangeListener l : listenerModified) {
            l.stateChanged(ev);
        }
    }
    
    // implement ActionListener
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if (o instanceof JButton) {
            changed = true;
            if (o == btAdd) {
                addDirectory();
            } else if (o == btRemove) {
                removeDirectory();
            } else if (o == btUp) {
//                moveDirectory(DIRECTORY_MOVE_UP);
            } else if (o == btDown) {
                setSelectedAsDefault();
            } else {
                if (o == btMakeVersion) {
                } else if (o == btGdbVersion) {
                } else if (o == btCVersion) {
                } else if (o == btCppVersion) {
                } else if (o == btFortranVersion) {
                }
            }
        }
    }
    
    // implemet ItemListener
    public void itemStateChanged(ItemEvent ev) {
        Object o = ev.getSource();
        
        if (!updating) {
            if (o instanceof JComboBox && ev.getStateChange() == ItemEvent.SELECTED) {
                Object item = ev.getItem();
                changed = true;

//                if (o == cbCompilerSet) {
//                    changeCompilerSet((CompilerSet) item);
//                } else if (o == cbCCommand) {
//                    if (item instanceof Tool) {
//                        cbCCommand.setToolTipText(((Tool) item).toString());
//                        setDefaultCompiler(Tool.CCompiler, ((Tool) item).getName());
//                        updateCPath((Tool) item);
//                        cCommandSelection = (Tool) item;
//                    } else if (!changingCompilerSet) {
//                        addRemoveUserTool(cbCCommand, cCommandSelection, Tool.CCompiler);
//                    }
//                } else if (o == cbCppCommand) {
//                    if (item instanceof Tool) {
//                        cbCppCommand.setToolTipText(((Tool) item).toString());
//                        setDefaultCompiler(Tool.CCCompiler, ((Tool) item).getName());
//                        updateCppPath((Tool) item);
//                        cppCommandSelection = (Tool) item;
//                    } else if (!changingCompilerSet) {
//                        addRemoveUserTool(cbCppCommand, cppCommandSelection, Tool.CCCompiler);
//                    }
//                } else if (o == cbFortranCommand) {
//                    if (item instanceof Tool) {
//                        cbFortranCommand.setToolTipText(((Tool) item).toString());
//                        setDefaultCompiler(Tool.FortranCompiler, ((Tool) item).getName());
//                        updateFortranPath((Tool) item);
//                        fortranCommandSelection = (Tool) item;
//                    } else if (!changingCompilerSet) {
//                        addRemoveUserTool(cbFortranCommand, fortranCommandSelection, Tool.FortranCompiler);
//                    }
//                }
            } else if (o instanceof JCheckBox && !changingCompilerSet) {
                dataValid();
            }
        }
    }
    
    // implement DocumentListener
    public void changedUpdate(DocumentEvent ev) {}
    
    public void insertUpdate(DocumentEvent ev) {
        String text;
        if (!updating) {
            changed = true;
        }
        Document doc = ev.getDocument();
        try {
            text = doc.getText(0, doc.getLength());
            String title = (String) doc.getProperty(Document.TitleProperty);
            if (title == MAKE_NAME) {
                validateMakePathField();
            } else if (title == GDB_NAME) {
                validateGdbPathField();
            } else if (title == C_NAME) {
                validateCPathField();
            } else if (title == CPP_NAME) {
                validateCppPathField();
            } else if (title == FORTRAN_NAME) {
                validateFortranPathField();
            }
        } catch (BadLocationException ex) {
        };
    }
    
    public void removeUpdate(DocumentEvent ev) {
        insertUpdate(ev);
    }
    
    // Implement List SelectionListener
    public void valueChanged(ListSelectionEvent ev) {
        
        if (!ev.getValueIsAdjusting()) { // we don't want the event until its finished
            if (ev.getSource() == lstDirlist) {
                changeCompilerSet((CompilerSet)lstDirlist.getSelectedValue());
                btRemove.setEnabled(csm.getCompilerSets().size() > 1 && lstDirlist.getSelectedIndex() >= 0);
                btUp.setEnabled(false /*lstDirlist.getSelectedIndex() >= 0*/);
                btDown.setEnabled(lstDirlist.getSelectedIndex() >= 0 && !((CompilerSet)lstDirlist.getSelectedValue()).isDefault());
            }
        }
    }
    
    /** Document which suppresses '/' characters */
    private static class NameOnlyDocument extends PlainDocument {
        
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str.indexOf("/") == -1) { // NOI18N
                super.insertString(offs, str, a);
            }
        }
    }
    
//    private class ToolsComboBox extends JComboBox {
//        
//        ToolsComboBox() {
//            super(new ToolsComboBoxModel(NbBundle.getMessage(ToolsPanel.class, "LBL_AddRemove")));
//        }
//        
//        public void removeAllItems() {
//            super.removeAllItems();
//            setSelectedIndex(0);
//        }
//        
//        public void removeItem(Object o) {
//            super.removeItem(o);
//            if (getItemAt(0) instanceof String) {
//                setSelectedIndex(0);
//            }
//        }
//    }
//    
//    private class ToolsComboBoxModel extends DefaultComboBoxModel {
//        
//        public ToolsComboBoxModel(String addRemove) {
//            super();
//            super.addElement("");
//            super.addElement(addRemove);
//        }
//        
//        public void addElement(Object o) {
//            if (getSize() == 2 && super.getElementAt(0) instanceof String && 
//                    super.getElementAt(0).toString().length() == 0) {
//                super.removeElementAt(0);
//            }
//            int size = getSize();
//            super.insertElementAt(o, getSize() - 1);
//        }
//        
//        public void removeElement(Object o) {
//            if (o instanceof Tool) {
//                super.removeElement(o);
//            }
//            if (getSize() == 1) {
//                super.insertElementAt("", 0);
//            }
//        }
//        
//        public void removeAllElements() {
//            int size = getSize();
//            for (int i = 1; i < size; i++) {
//                super.removeElementAt(0);
//            }
//            super.insertElementAt("", 0);
//        }
//    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lbDirlist = new javax.swing.JLabel();
        lbMakeCommand = new javax.swing.JLabel();
        tfMakePath = new javax.swing.JTextField();
        tfMakePath.getDocument().putProperty(Document.TitleProperty, MAKE_NAME);
        tfMakePath.getDocument().addDocumentListener(this);
        btMakeVersion = new javax.swing.JButton();
        btMakeVersion.addActionListener(this);
        lbGdbCommand = new javax.swing.JLabel();
        tfGdbPath = new javax.swing.JTextField();
        tfGdbPath.getDocument().putProperty(Document.TitleProperty, GDB_NAME);
        tfGdbPath.getDocument().addDocumentListener(this);
        btGdbVersion = new javax.swing.JButton();
        btGdbVersion.addActionListener(this);
        lbCCommand = new javax.swing.JLabel();
        tfCPath = new javax.swing.JTextField();
        tfCPath.getDocument().putProperty(Document.TitleProperty, C_NAME);
        tfCPath.getDocument().addDocumentListener(this);
        btCVersion = new javax.swing.JButton();
        btCVersion.addActionListener(this);
        lbCppCommand = new javax.swing.JLabel();
        tfCppPath = new javax.swing.JTextField();
        tfCppPath.getDocument().putProperty(Document.TitleProperty, CPP_NAME);
        tfCppPath.getDocument().addDocumentListener(this);
        btCppVersion = new javax.swing.JButton();
        btCppVersion.addActionListener(this);
        lbFortranCommand = new javax.swing.JLabel();
        tfFortranPath = new javax.swing.JTextField();
        tfFortranPath.getDocument().putProperty(Document.TitleProperty, FORTRAN_NAME);
        tfFortranPath.getDocument().addDocumentListener(this);
        btFortranVersion = new javax.swing.JButton();
        btFortranVersion.addActionListener(this);
        lbCompilerCollection = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cbMakeRequired = new javax.swing.JCheckBox();
        cbGdbRequired = new javax.swing.JCheckBox();
        cbGdbRequired.addItemListener(this);
        cbCRequired = new javax.swing.JCheckBox();
        cbCRequired.addItemListener(this);
        cbCppRequired = new javax.swing.JCheckBox();
        cbCppRequired.addItemListener(this);
        cbFortranRequired = new javax.swing.JCheckBox();
        cbFortranRequired.addItemListener(this);
        lbBaseDirectory = new javax.swing.JLabel();
        tfBaseDirectory = new javax.swing.JTextField();
        btBaseDirectory = new javax.swing.JButton();
        btVersions = new javax.swing.JButton();
        buttomPanel = new javax.swing.JPanel();
        errorScrollPane = new javax.swing.JScrollPane();
        errorTextArea = new javax.swing.JTextArea();
        btRestore = new javax.swing.JButton();
        ToolSetPanel = new javax.swing.JPanel();
        spDirlist = new JScrollPane(lstDirlist);
        lstDirlist = new javax.swing.JList();
        buttonPanel = new javax.swing.JPanel();
        btAdd = new javax.swing.JButton();
        btAdd.addActionListener(this);
        btRemove = new javax.swing.JButton();
        btRemove.addActionListener(this);
        btUp = new javax.swing.JButton();
        btUp.addActionListener(this);
        btDown = new javax.swing.JButton();
        btDown.addActionListener(this);
        cbFamily = new javax.swing.JComboBox();

        setMinimumSize(new java.awt.Dimension(600, 400));
        setLayout(new java.awt.GridBagLayout());

        lbDirlist.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DirlistLabel").charAt(0));
        lbDirlist.setLabelFor(spDirlist);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle"); // NOI18N
        lbDirlist.setText(bundle.getString("LBL_DirlistLabel")); // NOI18N
        lbDirlist.setToolTipText(bundle.getString("HINT_DirListLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 4);
        add(lbDirlist, gridBagConstraints);
        lbDirlist.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_DirlistLabel")); // NOI18N
        lbDirlist.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DirlistLabel")); // NOI18N

        lbMakeCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_MakeCommand").charAt(0));
        lbMakeCommand.setLabelFor(tfMakePath);
        lbMakeCommand.setText(bundle.getString("LBL_MakeCommand")); // NOI18N
        lbMakeCommand.setToolTipText(bundle.getString("HINT_MakeCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 0);
        add(lbMakeCommand, gridBagConstraints);
        lbMakeCommand.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_MakeCommand")); // NOI18N
        lbMakeCommand.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MakeCommand")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 2, 0, 0);
        add(tfMakePath, gridBagConstraints);
        tfMakePath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.tfMakePath.AccessibleContext.accessibleDescription")); // NOI18N

        btMakeVersion.setText(bundle.getString("LBL_MakeVersion")); // NOI18N
        btMakeVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btMakeVersionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 6);
        add(btMakeVersion, gridBagConstraints);
        btMakeVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btMakeVersion.AccessibleContext.accessibleDescription")); // NOI18N

        lbGdbCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_GdbCommand").charAt(0));
        lbGdbCommand.setLabelFor(tfGdbPath);
        lbGdbCommand.setText(bundle.getString("LBL_GdbCommand")); // NOI18N
        lbGdbCommand.setToolTipText(bundle.getString("HINT_GdbCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        add(lbGdbCommand, gridBagConstraints);
        lbGdbCommand.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_GdbCommand")); // NOI18N
        lbGdbCommand.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_GdbCommand")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfGdbPath, gridBagConstraints);
        tfGdbPath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.tfGdbPath.AccessibleContext.accessibleDescription")); // NOI18N

        btGdbVersion.setText(bundle.getString("LBL_GdbVersion")); // NOI18N
        btGdbVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btGdbVersionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btGdbVersion, gridBagConstraints);
        btGdbVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btGdbVersion.AccessibleContext.accessibleDescription")); // NOI18N

        lbCCommand.setLabelFor(tfCPath);
        lbCCommand.setText(bundle.getString("LBL_CCommand")); // NOI18N
        lbCCommand.setToolTipText(bundle.getString("HINT_CCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 22, 0, 0);
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

        btCVersion.setText(bundle.getString("LBL_CVersion")); // NOI18N
        btCVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCVersionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btCVersion, gridBagConstraints);
        btCVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btCVersion.AccessibleContext.accessibleDescription")); // NOI18N

        lbCppCommand.setLabelFor(tfCppPath);
        lbCppCommand.setText(bundle.getString("LBL_CppCommand")); // NOI18N
        lbCppCommand.setToolTipText(bundle.getString("HINT_CppCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 22, 0, 0);
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

        btCppVersion.setText(bundle.getString("LBL_CppVersion")); // NOI18N
        btCppVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCppVersionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btCppVersion, gridBagConstraints);
        btCppVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btCppVersion.AccessibleContext.accessibleDescription")); // NOI18N

        lbFortranCommand.setLabelFor(tfFortranPath);
        lbFortranCommand.setText(bundle.getString("LBL_FortranCommand")); // NOI18N
        lbFortranCommand.setToolTipText(bundle.getString("HINT_FortranCommand")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 22, 0, 0);
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

        btFortranVersion.setText(bundle.getString("LBL_FortranVersion")); // NOI18N
        btFortranVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFortranVersionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btFortranVersion, gridBagConstraints);
        btFortranVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btFortranVersion.AccessibleContext.accessibleDescription")); // NOI18N

        lbCompilerCollection.setLabelFor(cbFamily);
        lbCompilerCollection.setText(bundle.getString("LBL_CompilerCollection")); // NOI18N
        lbCompilerCollection.setToolTipText(bundle.getString("HINT_CompilerCollection")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(lbCompilerCollection, gridBagConstraints);
        lbCompilerCollection.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CompilerCollection")); // NOI18N
        lbCompilerCollection.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CompilerCollection")); // NOI18N

        jLabel1.setLabelFor(cbMakeRequired);
        jLabel1.setText(bundle.getString("LBL_RequiredTools")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 0);
        add(jLabel1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        cbMakeRequired.setSelected(true);
        cbMakeRequired.setText(bundle.getString("LBL_RequiredMake")); // NOI18N
        cbMakeRequired.setEnabled(false);
        cbMakeRequired.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(cbMakeRequired, gridBagConstraints);

        cbGdbRequired.setText(bundle.getString("LBL_RequiredGdb")); // NOI18N
        cbGdbRequired.setEnabled(false);
        cbGdbRequired.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(cbGdbRequired, gridBagConstraints);

        cbCRequired.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CCompiler_CB").charAt(0));
        cbCRequired.setText(bundle.getString("LBL_RequiredCompiler_C")); // NOI18N
        cbCRequired.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(cbCRequired, gridBagConstraints);

        cbCppRequired.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CppCompiler_CB").charAt(0));
        cbCppRequired.setText(bundle.getString("LBL_RequiredCompiler_Cpp")); // NOI18N
        cbCppRequired.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(cbCppRequired, gridBagConstraints);

        cbFortranRequired.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_FortranCompiler_CB").charAt(0));
        cbFortranRequired.setText(bundle.getString("LBL_RequiredCompiler_Fortran")); // NOI18N
        cbFortranRequired.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        jPanel1.add(cbFortranRequired, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 0, 6);
        add(jPanel1, gridBagConstraints);

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
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btBaseDirectory, gridBagConstraints);
        btBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N

        btVersions.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_Versions").charAt(0));
        btVersions.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btVersions.text")); // NOI18N
        btVersions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btVersionsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(12, 6, 0, 6);
        add(btVersions, gridBagConstraints);
        btVersions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btVersions.AccessibleContext.accessibleDescription")); // NOI18N

        buttomPanel.setLayout(new java.awt.GridBagLayout());

        errorScrollPane.setBorder(null);

        errorTextArea.setEditable(false);
        errorTextArea.setForeground(new java.awt.Color(255, 0, 0));
        errorTextArea.setLineWrap(true);
        errorTextArea.setRows(3);
        errorTextArea.setBorder(null);
        errorScrollPane.setViewportView(errorTextArea);
        errorTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.errorTextArea.AccessibleContext.accessibleName")); // NOI18N
        errorTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.errorTextArea.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 0, 6);
        buttomPanel.add(errorScrollPane, gridBagConstraints);

        btRestore.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_RestoreDefault_BT").charAt(0));
        btRestore.setText(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btRestore.text")); // NOI18N
        btRestore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRestoreActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 7);
        buttomPanel.add(btRestore, gridBagConstraints);
        btRestore.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ToolsPanel.class, "ToolsPanel.btRestore.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(buttomPanel, gridBagConstraints);

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

        btUp.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_UpButton").charAt(0));
        btUp.setText(bundle.getString("LBL_UpButton")); // NOI18N
        btUp.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        buttonPanel.add(btUp, gridBagConstraints);
        btUp.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_UpButton")); // NOI18N
        btUp.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_UpButton")); // NOI18N

        btDown.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DownButton").charAt(0));
        btDown.setText(bundle.getString("LBL_DownButton")); // NOI18N
        btDown.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        buttonPanel.add(btDown, gridBagConstraints);
        btDown.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_DownButton")); // NOI18N
        btDown.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DownButton")); // NOI18N

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

        cbFamily.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        add(cbFamily, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void btVersionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btVersionsActionPerformed
    if (isPathFieldValid(tfMakePath))
        postVersionInfo(tfMakePath.getText(), tfMakePath.getText());
    if (isPathFieldValid(tfGdbPath))
        postVersionInfo(tfGdbPath.getText(), tfGdbPath.getText());
    if (isPathFieldValid(tfCppPath))
        postVersionInfo(tfCppPath.getText(), tfCppPath.getText());
    if (isPathFieldValid(tfCPath))
        postVersionInfo(tfCPath.getText(), tfCPath.getText());
    if (isPathFieldValid(tfFortranPath))
        postVersionInfo(tfFortranPath.getText(), tfFortranPath.getText());
    
                    
}//GEN-LAST:event_btVersionsActionPerformed

private void btBaseDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBaseDirectoryActionPerformed
    String seed = null;
    if (tfBaseDirectory.getText().length() > 0) {
        seed = tfBaseDirectory.getText();
    }
    else if (FileChooser.getCurrectChooserFile() != null) {
        seed = FileChooser.getCurrectChooserFile().getPath();
    }
    else {
        seed = System.getProperty("user.home"); // NOI18N
    }
    FileChooser fileChooser = new FileChooser(getString("SELECT_BASE_DIRECTORY_TITLE"), null, JFileChooser.DIRECTORIES_ONLY, null, seed, true);
    int ret = fileChooser.showOpenDialog(this);
    if (ret == JFileChooser.CANCEL_OPTION) {
        return;
    }
    String dirPath = fileChooser.getSelectedFile().getPath();
    tfBaseDirectory.setText(dirPath);
    
    CompilerSet cs = (CompilerSet)lstDirlist.getSelectedValue();
    CompilerFlavor cf = (CompilerFlavor)cbFamily.getSelectedItem();
    csm.reInitCompilerSet(cs, dirPath);
    update(false);
    
}//GEN-LAST:event_btBaseDirectoryActionPerformed

private void selectCompiler(JTextField tf) {
    String seed = tfBaseDirectory.getText();
    FileChooser fileChooser = new FileChooser(getString("SELECT_TOOL_TITLE"), null, JFileChooser.FILES_ONLY, null, seed, false);
    int ret = fileChooser.showOpenDialog(this);
    if (ret == JFileChooser.CANCEL_OPTION) {
        return;
    }
    if (!new File(new File(tfBaseDirectory.getText()), fileChooser.getSelectedFile().getName()).exists()) {
        NotifyDescriptor nb = new NotifyDescriptor.Message("Only compilers within base directory are allowed", NotifyDescriptor.ERROR_MESSAGE); // NOI18N
        DialogDisplayer.getDefault().notify(nb);
        return;
    }
    tf.setText(fileChooser.getSelectedFile().getPath());
}

private void selectTool(JTextField tf) {
    String seed = tfBaseDirectory.getText();
    FileChooser fileChooser = new FileChooser(getString("SELECT_TOOL_TITLE"), null, JFileChooser.FILES_ONLY, null, seed, false);
    int ret = fileChooser.showOpenDialog(this);
    if (ret == JFileChooser.CANCEL_OPTION) {
        return;
    }
    tf.setText(fileChooser.getSelectedFile().getPath());
}

private void btCVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCVersionActionPerformed
    selectCompiler(tfCPath);
}//GEN-LAST:event_btCVersionActionPerformed

private void btCppVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCppVersionActionPerformed
    selectCompiler(tfCppPath);
}//GEN-LAST:event_btCppVersionActionPerformed

private void btFortranVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFortranVersionActionPerformed
    selectCompiler(tfFortranPath);
}//GEN-LAST:event_btFortranVersionActionPerformed

private void btMakeVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btMakeVersionActionPerformed
    selectTool(tfMakePath);
}//GEN-LAST:event_btMakeVersionActionPerformed

private void btGdbVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btGdbVersionActionPerformed
    selectTool(tfGdbPath);
}//GEN-LAST:event_btGdbVersionActionPerformed

private void btRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRestoreActionPerformed
    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
            "This will rescan your environment and re-create the default tool sets.\nChanges you may have made to these sets will be lost. Your custom tool sets will not be affected.\n\nDo you want to continue?", // NOI18N
            "Restore Default Tool Sets", // NOI18N
            NotifyDescriptor.OK_CANCEL_OPTION);
    Object ret = DialogDisplayer.getDefault().notify(nd);
    if (ret != NotifyDescriptor.OK_OPTION) {
        return;
    }
    CompilerSetManager newCsm = new CompilerSetManager();
    List<CompilerSet> list = csm.getCompilerSets();
    for (CompilerSet cs : list) {
        if (!cs.isAutoGenerated()) {
            newCsm.add(cs);
        }
    }
    String defaultName = null;
    CompilerSet defaultCS = csm.getDefaultCompilerSet();
    if (defaultCS != null)
        defaultName = defaultCS.getName();
    csm = newCsm;
    CompilerSet defaultCompilerSet = csm.getCompilerSet(defaultName);
    if (defaultCompilerSet != null) {
        csm.setDefault(defaultCompilerSet);
    }
    update(false);
}//GEN-LAST:event_btRestoreActionPerformed
    
    
    private static String getString(String key) {
        return NbBundle.getMessage(ToolsPanel.class, key);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ToolSetPanel;
    private javax.swing.JButton btAdd;
    private javax.swing.JButton btBaseDirectory;
    private javax.swing.JButton btCVersion;
    private javax.swing.JButton btCppVersion;
    private javax.swing.JButton btDown;
    private javax.swing.JButton btFortranVersion;
    private javax.swing.JButton btGdbVersion;
    private javax.swing.JButton btMakeVersion;
    private javax.swing.JButton btRemove;
    private javax.swing.JButton btRestore;
    private javax.swing.JButton btUp;
    private javax.swing.JButton btVersions;
    private javax.swing.JPanel buttomPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JCheckBox cbCRequired;
    private javax.swing.JCheckBox cbCppRequired;
    private javax.swing.JComboBox cbFamily;
    private javax.swing.JCheckBox cbFortranRequired;
    private javax.swing.JCheckBox cbGdbRequired;
    private javax.swing.JCheckBox cbMakeRequired;
    private javax.swing.JScrollPane errorScrollPane;
    private javax.swing.JTextArea errorTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lbBaseDirectory;
    private javax.swing.JLabel lbCCommand;
    private javax.swing.JLabel lbCompilerCollection;
    private javax.swing.JLabel lbCppCommand;
    private javax.swing.JLabel lbDirlist;
    private javax.swing.JLabel lbFortranCommand;
    private javax.swing.JLabel lbGdbCommand;
    private javax.swing.JLabel lbMakeCommand;
    private javax.swing.JList lstDirlist;
    private javax.swing.JScrollPane spDirlist;
    private javax.swing.JTextField tfBaseDirectory;
    private javax.swing.JTextField tfCPath;
    private javax.swing.JTextField tfCppPath;
    private javax.swing.JTextField tfFortranPath;
    private javax.swing.JTextField tfGdbPath;
    private javax.swing.JTextField tfMakePath;
    // End of variables declaration//GEN-END:variables
    
}
