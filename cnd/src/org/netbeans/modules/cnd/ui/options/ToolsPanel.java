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
import java.awt.Dialog;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.modules.cnd.MIMENames;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/** Display the "Tools Default" panel */
public class ToolsPanel extends JPanel implements ActionListener, DocumentListener,
        ListSelectionListener, ItemListener {
    
    // The following are constants so I can do == rather than "equals"
    private final String MAKE_NAME = "make"; // NOI18N
    private final String GDB_NAME = "gdb"; // NOI18N
//    private final String C_NAME = "C"; // NOI18N
//    private final String Cpp_NAME = "C++"; // NOI18N
//    private final String FORTRAN_NAME = "Fortran"; // NOI18N
    
    private static final String DIRECTORY_MOVE_UP = "Up"; // NOI18N
    private static final String DIRECTORY_MOVE_DOWN = "Down"; // NOI18N
    
    public static final String PROP_VALID = "valid"; // NOI18N
    
    private boolean initialized = false;
    private boolean changed;
    private boolean changingCompilerSet;
    private boolean updating;
    private boolean valid;
    private static ArrayList<String> dirlist = null;
    private ToolsPanelModel model = null;
    private Color tfColor = null;
    private boolean gdbEnabled;
    
    private Tool cCommandSelection = null;
    private Tool cppCommandSelection = null;
    private Tool fortranCommandSelection = null;
    
    private static ToolsPanel instance = null;
    
    /** The default (or previously selected) C compiler for each CompilerSet */
    private HashMap<String, String> cSelections;
    
    /** The default (or previously selected) C++ compiler for each CompilerSet */
    private HashMap<String, String> cppSelections;
    
    /** The default (or previously selected) Fortran compiler for each CompilerSet */
    private HashMap<String, String> fortranSelections;
    
    private JFileChooser addDirectoryChooser;
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
        if (dirlist == null) {
            dirlist = Path.getPath();
        }
//        dirlist = model.getPath();
        if (csm == null) {
            csm = CompilerSetManager.getDefault();
        }
        gdbEnabled = model.isGdbEnabled();
        
        cSelections = new HashMap();
        cppSelections = new HashMap();
        fortranSelections = new HashMap();
        addDirectoryChooser = null;
        
        tfMakeCommand.setText(model.getMakeName());
        tfGdbCommand.setText(model.getGdbName());
        
        // Assume fortran is enabled externally before the initial display of this dialog
        boolean fortran = CppSettings.getDefault().isFortranEnabled();
        lbFortranCommand.setVisible(fortran);
        cbFortranCommand.setVisible(fortran);
        tfFortranPath.setVisible(fortran);
        btFortranVersion.setVisible(fortran);
        cbFortranRequired.setVisible(fortran);
        
        // Initialize Required tools. Can't do it in constructor because there is no model then.
        cbGdbRequired.setSelected(model.isGdbRequired());
        cbCRequired.setSelected(model.isCRequired());
        cbCppRequired.setSelected(model.isCppRequired());
        cbFortranRequired.setSelected(model.isFortranRequired());
        
        cbCompilerSet.removeAllItems();
    } 
    
    private void addDirectory() {
        File file;
        int rc;
        
        if (addDirectoryChooser == null) {
            ResourceBundle bundle = NbBundle.getBundle(ToolsPanel.class);
            addDirectoryChooser = new JFileChooser();
            addDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            addDirectoryChooser.setDialogTitle(bundle.getString("LBL_AddDirectoryTitle")); // NOI18N
            addDirectoryChooser.setApproveButtonText(bundle.getString("LBL_AddDirectoryAcceptButton")); // NOI18N
            addDirectoryChooser.setApproveButtonMnemonic(bundle.getString("MNEM_AddDirectoryAcceptButton").charAt(0)); // NOI18N
        }
        try {
            rc = addDirectoryChooser.showDialog(this, null);
            if (rc == JFileChooser.APPROVE_OPTION) {
                changed = true;
                file = addDirectoryChooser.getSelectedFile();
                dirlist.add(0, file.getAbsolutePath());
                csm = new CompilerSetManager(dirlist);
                update(false);
            }
        } catch (HeadlessException ex) {
        }
    }
    
    private void removeDirectory() {
        int idx = lstDirlist.getSelectedIndex();
        assert idx != -1; // the button shouldn't be enabled
        if (checkForCSRemoval(idx)) {
            String dir = dirlist.remove(idx);
            changed = true;
            if (dirlist.size() > 0) {
                if (idx == dirlist.size()) {
                    idx--;
                }
            }

            try {
                for (CompilerSet cs : csm.getCompilerSets()) {
                    StringTokenizer tok = new StringTokenizer(cs.getDirectory(), File.pathSeparator);
                    while (tok.hasMoreTokens()) {
                        String d = tok.nextToken();
                        if (d.equals(dir)) {
                            csm = new CompilerSetManager(dirlist);
                            return;
                        }
                    }
                }
            } finally {
                if (changed) {
                    update(false);
                }
            }
        }
    }
    
    private void moveDirectory(String direction) {
        assert direction == DIRECTORY_MOVE_UP || direction == DIRECTORY_MOVE_DOWN;
        int idx = lstDirlist.getSelectedIndex();
        assert idx != -1; // the button shouldn't be enabled
        String dir = dirlist.get(idx);
        dirlist.remove(idx);
        changed = true;
        if (direction == DIRECTORY_MOVE_UP) {
            idx--;
        } else {
            idx++; 
        }
        dirlist.add(idx, dir);
        lstDirlist.setSelectedIndex(idx);
        update(false);
    }
    
    /**
     * The user has pressed the Remove button id the directories list and the removal
     * of the selected directory will result in the elimination of a compiler set. Ask
     * the user if thats what they want and warn them some projects may be converted to
     * another compiler collection. If the compiler set is the only one, tell the user
     * its not allowed and stop the action.
     *
     * @param idx The index of the selected directory
     * @returns True if the user OK's removal or removal doesn't cause elimination of a compiler collection
     */
    private boolean checkForCSRemoval(int idx) {
        String dir = dirlist.get(idx);
        int count = 0;
        
        for (String d : dirlist) {
            if (d.equals(dir)) {
                count++;
            }
        }
        if (count > 1) {
            return true; // its OK to remove a duplicate directory...
        }
        
        for (CompilerSet cs : csm.getCompilerSets()) {
            String csdirs = cs.getDirectory();
            StringTokenizer tok = new StringTokenizer(csdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                String d = tok.nextToken();
                if (d.equals(dir)) {
                    if (csm.getCompilerSets().size() == 1) {
                        return suppressCSRemoval(dir, cs.getDisplayName());
                    } else {
                        return warnAboutCSRemoval(dir, cs.getDisplayName());
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Post a confirmation dialog about removing a compiler collection.
     *
     * @param dir The directory about to be removed
     * @param csname The Compiler Collection about to be eliminated
     * @returns True if the user pressed OK, false otherwise
     */
    private boolean warnAboutCSRemoval(String dir, String csname) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(ToolsPanel.class, "WARN_CSRemovalPending", dir, csname), // NOI18N
                NbBundle.getMessage(ToolsPanel.class, "LBL_PendingCSRemoval_Title"), // NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION);
        Object ret = DialogDisplayer.getDefault().notify(nd);
        return ret == NotifyDescriptor.OK_OPTION;
    }
    
    /**
     * Post a warning dialog telling the user they can't remove the last compiler collection.
     *
     * @param dir The directory about to be removed
     * @param csname The Compiler Collection about to be eliminated
     * @returns false
     */
    private boolean suppressCSRemoval(String dir, String csname) {
        NotifyDescriptor nb = new NotifyDescriptor.Message(
                NbBundle.getMessage(ToolsPanel.class, "WARN_CSRemovalDisallowed", dir, csname),
                NotifyDescriptor.ERROR_MESSAGE); // NOI18N
        nb.setTitle(NbBundle.getMessage(ToolsPanel.class, "LBL_CSRemovalDisallowed_Title")); // NOI18N
        DialogDisplayer.getDefault().notify(nb);
        return false;
    }
    
    private void setMakePathField(String cmd) {
        String path = findCommandFromPath(cmd);
        
        if (path != null) {
            tfMakePath.setForeground(tfColor);
            tfMakePath.setText(path);
        } else {
            tfMakePath.setForeground(Color.RED);
            try {  // Want to output Red text and make data_valid return false
                Document doc = tfMakePath.getDocument();
                doc.remove(0, doc.getLength());
                doc.insertString(0, NbBundle.getMessage(ToolsPanel.class, "ERR_NotFound"),  // NOI18N
                        SimpleAttributeSet.EMPTY);
            } catch (BadLocationException ex) {
            }
        }
        dataValid();
    }
    
    private void setGdbPathField(String cmd) {
        String path = findCommandFromPath(cmd);
        if (path != null) {
            tfGdbPath.setForeground(tfColor);
            tfGdbPath.setText(path);
        } else {
            tfGdbPath.setForeground(Color.RED);
            try {  // Want to output Red text and make data_valid return false
                Document doc = tfGdbPath.getDocument();
                doc.remove(0, doc.getLength());
                doc.insertString(0, NbBundle.getMessage(ToolsPanel.class, "ERR_NotFound"),  // NOI18N
                        SimpleAttributeSet.EMPTY);
            } catch (BadLocationException ex) {
            }
        }
        dataValid();
    }
    
    private String findCommandFromPath(String cmd) {
        File file;
        String cmd2 = null;
        
        if (cmd.length() > 0) {
            if (Utilities.isWindows() && !cmd.endsWith(".exe")) { // NOI18N
                cmd2 = cmd + ".exe"; // NOI18N
            }

            for (String dir : dirlist) {
                file = new File(dir, cmd);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
                if (cmd2 != null) {
                    file = new File(dir, cmd2);
                    if (file.exists()) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }
    
    /** Update the display */
    public void update() {
        update(true);
    }
    
    /** Update the display */
    public void update(boolean doInitialize) {
        
        updating = true;
        if (!initialized || doInitialize) {
            initialize();
        }
        
        lbGdbCommand.setVisible(gdbEnabled);
        tfGdbCommand.setVisible(gdbEnabled);
        tfGdbPath.setVisible(gdbEnabled);
        btGdbVersion.setVisible(gdbEnabled);
        cbGdbRequired.setVisible(gdbEnabled);
        
        int idx = lstDirlist.getSelectedIndex();
        lstDirlist.setListData(dirlist.toArray());
        if (idx >= 0) {
            lstDirlist.setSelectedIndex(idx);
            lstDirlist.ensureIndexIsVisible(idx);
        }
        setMakePathField(tfMakeCommand.getText());
        setGdbPathField(tfGdbCommand.getText());
        updateCompilers();
        updating = false;
        dataValid();
        initialized = true;
    }
    
    private void updateCompilers() {
        
        if (!csm.getCompilerSets().isEmpty()) {
            String name, dname;
            
            if (cbCompilerSet.getItemCount() > 0) {
                name = ((CompilerSet) cbCompilerSet.getSelectedItem()).getName();
                dname = ((CompilerSet) cbCompilerSet.getSelectedItem()).getDisplayName();
            } else {
                name = model.getCompilerSetName();
                if (name.length() == 0 || csm.getCompilerSet(name) == null) {
                    name = CompilerSetManager.getDefault().getCompilerSet(0).getName();
                    dname = CompilerSetManager.getDefault().getCompilerSet(0).getDisplayName();
                } else {
                    name = csm.getCompilerSet(name).getName(); // Sun12 will fallback match any Sun release
                    dname = csm.getCompilerSet(name).getDisplayName();
                }
            }
            cbCompilerSet.removeAllItems();
            for (CompilerSet cs : csm.getCompilerSets()) {
                cbCompilerSet.addItem(cs);
            }
            
            CompilerSet cs = csm.getCompilerSet(name, dname);
            if (cs == null) {
                cs = csm.getCompilerSet(0);
            }
            cbCompilerSet.setSelectedItem(cs);
            changeCompilerSet(cs);
        } else {
            cbCompilerSet.removeAllItems();
        }
    }
    
    private void changeCompilerSet(CompilerSet cs) {
        boolean fortran = CppSettings.getDefault().isFortranEnabled();
        Tool fortranSelection = null;
        
        changingCompilerSet = true;
        cbCCommand.removeAllItems();
        cbCppCommand.removeAllItems();
        if (fortran) {
            cbFortranCommand.removeAllItems();
            fortranSelection = getDefaultCompiler(cs, Tool.FortranCompiler);
        }
        
        Tool cSelection = getDefaultCompiler(cs, Tool.CCompiler);
        Tool cppSelection = getDefaultCompiler(cs, Tool.CCCompiler);
     
        for (Tool tool : cs.getTools()) {
            if (tool.getName().length() > 0) {
                if (tool.getKind() == Tool.CCompiler) {
                    cbCCommand.addItem(tool);
                }
                if (tool.getKind() == Tool.CCCompiler) {
                    cbCppCommand.addItem(tool);
                }
                if (fortran && tool.getKind() == Tool.FortranCompiler) {
                    cbFortranCommand.addItem(tool);
                }
            }
        }
        if (cSelection != null) {
            cbCCommand.setSelectedItem(cSelection);
            cbCCommand.setToolTipText(cSelection.toString());
            updateCPath(cSelection);
            cCommandSelection = cSelection;
        } else {
            tfCPath.setText("");
            cbCCommand.setSelectedIndex(0);
        }
        if (cppSelection != null) {
            cbCppCommand.setSelectedItem(cppSelection);
            cbCppCommand.setToolTipText(cppSelection.toString());
            updateCppPath(cppSelection);
            cppCommandSelection = cppSelection;
        } else {
            tfCppPath.setText("");
            cbCppCommand.setSelectedIndex(0);
        }
        if (fortran) {
            if (fortranSelection != null) {
                cbFortranCommand.setSelectedItem(fortranSelection);
                cbFortranCommand.setToolTipText(fortranSelection.toString());
                updateFortranPath(fortranSelection);
                fortranCommandSelection = fortranSelection;
            } else {
                tfFortranPath.setText("");
                cbFortranCommand.setSelectedIndex(0);
            }
        }
        changingCompilerSet = false;
        currentCompilerSet = cs;
        fireCompilerSetChange();
        dataValid();
    }
    
    private void updatePath(Tool tool, int kind) {
        if (kind == Tool.CCompiler) {
            updateCPath(tool);
        } else if (kind == Tool.CCCompiler) {
            updateCppPath(tool);
        } else if (kind == Tool.FortranCompiler) {
            updateFortranPath(tool);
        }
    }
    
    private void updateCPath(Tool tool) {
        if (tool != null) {
            int toollen = tool.getPath().length();
            int pathlen = tfCPath.getText().length();

            if ((toollen > 0 && pathlen == 0) || (toollen == 0 && pathlen > 0)) {
                dataValid(); // force fire prop changed on PROP_VALID
            }
            tfCPath.setText(tool.getPath());
            tfCPath.setToolTipText(tool.getPath());
        } else {
            tfCPath.setText("");
            tfCPath.setToolTipText("");
        }
    }
    
    private void updateCppPath(Tool tool) {
        if (tool != null) {
            int toollen = tool.getPath().length();
            int pathlen = tfCppPath.getText().length();

            if ((toollen > 0 && pathlen == 0) || (toollen == 0 && pathlen > 0)) {
                dataValid(); // force fire prop changed on PROP_VALID
            }
            tfCppPath.setText(tool.getPath());
            tfCppPath.setToolTipText(tool.getPath());
        } else {
            tfCppPath.setText("");
            tfCppPath.setToolTipText("");
        }
    }
    
    private void updateFortranPath(Tool tool) {
        if (tool != null) {
            int toollen = tool.getPath().length();
            int pathlen = tfFortranPath.getText().length();

            if ((toollen > 0 && pathlen == 0) || (toollen == 0 && pathlen > 0)) {
                dataValid(); // force fire prop changed on PROP_VALID
            }
            tfFortranPath.setText(tool.getPath());
            tfFortranPath.setToolTipText(tool.getPath());
        } else {
            tfFortranPath.setText("");
            tfFortranPath.setToolTipText("");
        }
    }
    
    /**
     * Set the default compiler for each compiler type. This is useful when the user changes
     * CompilerSets to one whose default compiler had been changed earlier. This lets it get
     * restored to the earlier value.
     *
     * @param type The compiler type
     * @param name The display name of the selected compiler
     */
    protected void setDefaultCompiler(int type, String name) {
        CompilerSet cs = (CompilerSet) cbCompilerSet.getSelectedItem();
        changed = true;
        
        if (type == Tool.CCompiler) {
            cSelections.put(cs.getName(), name);
        } else if (type == Tool.CCCompiler) {
            cppSelections.put(cs.getName(), name);
        } else if (type == Tool.FortranCompiler) {
            fortranSelections.put(cs.getName(), name);
        }
    }
    
    /**
     * Get the default compiler for each compiler type. This is useful when the user changes
     * CompilerSets to one whose default compiler had been changed earlier. This lets it get
     * restored to the earlier value.
     *
     * @param cs The CompilerSet for whom we want a tool
     * @param kind The compiler type we want
     * @returns The Tool for the requested compiler (or null)
     */
    protected Tool getDefaultCompiler(CompilerSet cs, int kind) {
        String name = cs.getName();
        
        if (!name.equals(CompilerSet.None)) {
            if (kind == Tool.CCompiler) {
                if (cSelections.get(name) == null) {
                    return cs.getTool(cs.isSunCompiler() ? "cc" : "gcc"); // NOI18N
                } else {
                    return cs.getTool(cSelections.get(name));
                }
            } else if (kind == Tool.CCCompiler) {
                if (cppSelections.get(name) == null) {
                    return cs.getTool(cs.isSunCompiler() ? "CC" : "g++"); // NOI18N
                } else {
                    Tool tool = cs.getTool(cppSelections.get(name), kind);
                    if (tool == null) {
                        File file = new File(cs.getDirectory(), name);
                        if (file.exists()) {
                            tool = cs.addTool(name, cs.getDirectory(), kind);
                        }
                    }
                    return tool;
                }
            } else if (kind == Tool.FortranCompiler) {
                if (fortranSelections.get(name) == null) {
                    return cs.getTool(cs.isSunCompiler() ? "f90" : "g77"); // NOI18N
                } else {
                    return cs.getTool(fortranSelections.get(name));
                }
            }
        }
        return null;
    }
    
    public void applyChanges(boolean force) {
        changed = force;
        applyChanges();
    }
    
    /** Apply changes */
    public void applyChanges() {
        if (changed) {
            CompilerSet cs = (CompilerSet) cbCompilerSet.getSelectedItem();
            changed = false;
            
            CompilerSetManager.setDefault(csm);
//            model.setPath(dirlist);
            model.setMakeName(tfMakeCommand.getText());
            model.setMakePath(tfMakePath.getText());
            model.setGdbName(tfGdbCommand.getText());
            model.setGdbPath(tfGdbPath.getText());
            model.setCompilerSetName(cs.getName());
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
            boolean makeValid = !tfMakePath.getText().equals(NbBundle.getMessage(
                    ToolsPanel.class, "ERR_NotFound")); // NOI18N
            boolean gdbValid = (gdbEnabled && cbGdbRequired.isSelected()) ? !tfGdbPath.getText().equals(
                    NbBundle.getMessage(ToolsPanel.class, "ERR_NotFound")) : true; // NOI18N
            boolean cValid = cbCRequired.isSelected() ? tfCPath.getText().length() > 0 : true;
            boolean cppValid = cbCppRequired.isSelected() ? tfCppPath.getText().length() > 0 : true;
            boolean fortranValid = cbFortranRequired.isSelected() ? tfFortranPath.getText().length() > 0 : true;
            
            if (!initialized) {
                valid = !(csmValid && makeValid && gdbValid && cValid && cppValid && fortranValid);
            }

            if (valid != (csmValid && makeValid && gdbValid && cValid && cppValid && fortranValid)) {
                valid = !valid;
                firePropertyChange(PROP_VALID, !valid, valid);
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
    
    private void addRemoveUserTool(JComboBox jc, Tool selected, int kind) {
        CompilerSet cs = (CompilerSet) cbCompilerSet.getSelectedItem();
        AddRemoveToolPanel panel = new AddRemoveToolPanel(jc, cs);
        DialogDescriptor dd = new DialogDescriptor(panel, 
                NbBundle.getMessage(ToolsPanel.class, "TITLE_AddRemoveCompiler" + kind));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        jc.removeItemListener(this); // Remove or else we'll recurse setting the selection
        if (dd.getValue() == NotifyDescriptor.OK_OPTION) {
            List<String> addList = panel.getModel().getAddList();
            List removeList = panel.getModel().getRemoveList();
            ArrayList<Tool> rmlist = new ArrayList();
            for (Object o : removeList) {
                for (Tool tool : cs.getTools()) {
                    if (o == tool) {
                        if (tool == selected) {
                            selected = null;
                        }
                        rmlist.add(tool);
                        jc.removeItem(tool);
                    }
                }
            }
            for (Tool tool : rmlist) {
                cs.getTools().remove(tool);
            }
            for (String name : addList) {
                Tool tool = cs.addTool(name, cs.getDirectory(), kind);
                jc.addItem(tool);
                jc.setSelectedItem(tool);
                selected = tool;
            }
        }
        if (selected != null && cs.getTools().contains(selected)) {
            jc.setSelectedItem(selected);
            setDefaultCompiler(kind, selected.getName());
            updatePath(selected, kind);
        } else {
            jc.setSelectedIndex(0);
            if (jc.getItemAt(0) instanceof String) {
                updatePath(null, kind);
            } else {
                updatePath((Tool) jc.getItemAt(0), kind);
            }
        }
        jc.addItemListener(this);
    }
    
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
                moveDirectory(DIRECTORY_MOVE_UP);
            } else if (o == btDown) {
                moveDirectory(DIRECTORY_MOVE_DOWN);
            } else {
                CompilerFlavor flavor = ((CompilerSet) cbCompilerSet.getSelectedItem()).getCompilerFlavor();
                
                if (o == btMakeVersion) {
                    postVersionInfo(tfMakeCommand.getText(), tfMakePath.getText());
                } else if (o == btGdbVersion) {
                    postVersionInfo(tfGdbCommand.getText(), tfGdbPath.getText());
                } else if (o == btCVersion && cbCCommand.getSelectedItem() != null) {
                    postVersionInfo(flavor, cbCCommand.getSelectedItem().toString(),
                            tfCPath.getText());
                } else if (o == btCppVersion && cbCppCommand.getSelectedItem() != null) {
                    postVersionInfo(flavor, cbCppCommand.getSelectedItem().toString(),
                            tfCppPath.getText());
                } else if (o == btFortranVersion && cbFortranCommand.getSelectedItem() != null) {
                    postVersionInfo(flavor, cbFortranCommand.getSelectedItem().toString(),
                            tfFortranPath.getText());
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

                if (o == cbCompilerSet) {
                    changeCompilerSet((CompilerSet) item);
                } else if (o == cbCCommand) {
                    if (item instanceof Tool) {
                        cbCCommand.setToolTipText(((Tool) item).toString());
                        setDefaultCompiler(Tool.CCompiler, ((Tool) item).getName());
                        updateCPath((Tool) item);
                        cCommandSelection = (Tool) item;
                    } else if (!changingCompilerSet) {
                        addRemoveUserTool(cbCCommand, cCommandSelection, Tool.CCompiler);
                    }
                } else if (o == cbCppCommand) {
                    if (item instanceof Tool) {
                        cbCppCommand.setToolTipText(((Tool) item).toString());
                        setDefaultCompiler(Tool.CCCompiler, ((Tool) item).getName());
                        updateCppPath((Tool) item);
                        cppCommandSelection = (Tool) item;
                    } else if (!changingCompilerSet) {
                        addRemoveUserTool(cbCppCommand, cppCommandSelection, Tool.CCCompiler);
                    }
                } else if (o == cbFortranCommand) {
                    if (item instanceof Tool) {
                        cbFortranCommand.setToolTipText(((Tool) item).toString());
                        setDefaultCompiler(Tool.FortranCompiler, ((Tool) item).getName());
                        updateFortranPath((Tool) item);
                        fortranCommandSelection = (Tool) item;
                    } else if (!changingCompilerSet) {
                        addRemoveUserTool(cbFortranCommand, fortranCommandSelection, Tool.FortranCompiler);
                    }
                }
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
                setMakePathField(text);
            } else if (title == GDB_NAME) {
                setGdbPathField(text);
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
                btRemove.setEnabled(lstDirlist.getSelectedIndex() >= 0);
                btUp.setEnabled(lstDirlist.getSelectedIndex() > 0);
                btDown.setEnabled(lstDirlist.getSelectedIndex() < (dirlist.size() - 1) && lstDirlist.getSelectedIndex() >= 0);
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
    
    private class ToolsComboBox extends JComboBox {
        
        ToolsComboBox() {
            super(new ToolsComboBoxModel(NbBundle.getMessage(ToolsPanel.class, "LBL_AddRemove")));
        }
        
        public void removeAllItems() {
            super.removeAllItems();
            setSelectedIndex(0);
        }
        
        public void removeItem(Object o) {
            super.removeItem(o);
            if (getItemAt(0) instanceof String) {
                setSelectedIndex(0);
            }
        }
    }
    
    private class ToolsComboBoxModel extends DefaultComboBoxModel {
        
        public ToolsComboBoxModel(String addRemove) {
            super();
            super.addElement("");
            super.addElement(addRemove);
        }
        
        public void addElement(Object o) {
            if (getSize() == 2 && super.getElementAt(0) instanceof String && 
                    super.getElementAt(0).toString().length() == 0) {
                super.removeElementAt(0);
            }
            int size = getSize();
            super.insertElementAt(o, getSize() - 1);
        }
        
        public void removeElement(Object o) {
            if (o instanceof Tool) {
                super.removeElement(o);
            }
            if (getSize() == 1) {
                super.insertElementAt("", 0);
            }
        }
        
        public void removeAllElements() {
            int size = getSize();
            for (int i = 1; i < size; i++) {
                super.removeElementAt(0);
            }
            super.insertElementAt("", 0);
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

        lbDirlist = new javax.swing.JLabel();
        spDirlist = new JScrollPane(lstDirlist);
        lstDirlist = new javax.swing.JList();
        lbMakeCommand = new javax.swing.JLabel();
        tfMakeCommand = new JTextField(new NameOnlyDocument(), null, 0);
        tfMakeCommand.getDocument().putProperty(Document.TitleProperty, MAKE_NAME);
        tfMakeCommand.getDocument().addDocumentListener(this);
        tfMakePath = new javax.swing.JTextField();
        btMakeVersion = new javax.swing.JButton();
        btMakeVersion.addActionListener(this);
        lbGdbCommand = new javax.swing.JLabel();
        tfGdbCommand = new JTextField(new NameOnlyDocument(), null, 0);
        tfGdbCommand.getDocument().putProperty(Document.TitleProperty, GDB_NAME);
        tfGdbCommand.getDocument().addDocumentListener(this);
        tfGdbPath = new javax.swing.JTextField();
        btGdbVersion = new javax.swing.JButton();
        btGdbVersion.addActionListener(this);
        lbCCommand = new javax.swing.JLabel();
        cbCCommand = new ToolsComboBox();
        cbCCommand.addItemListener(this);
        tfCPath = new javax.swing.JTextField();
        tfColor = tfCPath.getForeground();
        btCVersion = new javax.swing.JButton();
        btCVersion.addActionListener(this);
        lbCppCommand = new javax.swing.JLabel();
        cbCppCommand = new ToolsComboBox();
        cbCppCommand.addItemListener(this);
        tfCppPath = new javax.swing.JTextField();
        btCppVersion = new javax.swing.JButton();
        btCppVersion.addActionListener(this);
        lbFortranCommand = new javax.swing.JLabel();
        cbFortranCommand = new ToolsComboBox();
        cbFortranCommand.addItemListener(this);
        tfFortranPath = new javax.swing.JTextField();
        btFortranVersion = new javax.swing.JButton();
        btFortranVersion.addActionListener(this);
        lbCompilerCollection = new javax.swing.JLabel();
        cbCompilerSet = new javax.swing.JComboBox();
        cbCompilerSet.addItemListener(this);
        btAdd = new javax.swing.JButton();
        btAdd.addActionListener(this);
        btRemove = new javax.swing.JButton();
        btRemove.addActionListener(this);
        btUp = new javax.swing.JButton();
        btUp.addActionListener(this);
        btDown = new javax.swing.JButton();
        btDown.addActionListener(this);
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
        jSeparator1 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(600, 650));
        lbDirlist.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DirlistLabel").charAt(0));
        lbDirlist.setLabelFor(spDirlist);
        lbDirlist.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_DirlistLabel"));
        lbDirlist.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_DirListLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 4);
        add(lbDirlist, gridBagConstraints);
        lbDirlist.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_DirlistLabel"));
        lbDirlist.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_DirlistLabel"));

        spDirlist.setMaximumSize(new java.awt.Dimension(200, 70));
        spDirlist.setMinimumSize(new java.awt.Dimension(100, 70));
        spDirlist.setPreferredSize(new java.awt.Dimension(150, 70));
        lstDirlist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstDirlist.setAutoscrolls(false);
        lstDirlist.addListSelectionListener(this);
        spDirlist.setViewportView(lstDirlist);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 0, 6);
        add(spDirlist, gridBagConstraints);

        lbMakeCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_MakeCommand").charAt(0));
        lbMakeCommand.setLabelFor(tfMakeCommand);
        lbMakeCommand.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_MakeCommand"));
        lbMakeCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_MakeCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(lbMakeCommand, gridBagConstraints);
        lbMakeCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_MakeCommand"));
        lbMakeCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_MakeCommand"));

        tfMakeCommand.setMinimumSize(new java.awt.Dimension(100, 20));
        tfMakeCommand.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfMakeCommand, gridBagConstraints);

        tfMakePath.setEditable(false);
        tfMakePath.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        tfMakePath.setMinimumSize(new java.awt.Dimension(50, 20));
        tfMakePath.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(tfMakePath, gridBagConstraints);

        btMakeVersion.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_MakeVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btMakeVersion, gridBagConstraints);
        btMakeVersion.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_MakeVersion"));
        btMakeVersion.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_MakeVersion"));

        lbGdbCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_GdbCommand").charAt(0));
        lbGdbCommand.setLabelFor(tfGdbCommand);
        lbGdbCommand.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_GdbCommand"));
        lbGdbCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_GdbCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(lbGdbCommand, gridBagConstraints);
        lbGdbCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_GdbCommand"));
        lbGdbCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_GdbCommand"));

        tfGdbCommand.setMinimumSize(new java.awt.Dimension(100, 20));
        tfGdbCommand.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfGdbCommand, gridBagConstraints);

        tfGdbPath.setEditable(false);
        tfGdbPath.setMinimumSize(new java.awt.Dimension(50, 20));
        tfGdbPath.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(tfGdbPath, gridBagConstraints);

        btGdbVersion.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_GdbVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btGdbVersion, gridBagConstraints);
        btGdbVersion.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_GdbVersion"));
        btGdbVersion.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_GdbVersion"));

        lbCCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CCommand").charAt(0));
        lbCCommand.setLabelFor(cbCCommand);
        lbCCommand.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CCommand"));
        lbCCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_CCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(lbCCommand, gridBagConstraints);
        lbCCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CCommand"));
        lbCCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CCommand"));

        cbCCommand.setMinimumSize(new java.awt.Dimension(100, 18));
        cbCCommand.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(cbCCommand, gridBagConstraints);

        tfCPath.setEditable(false);
        tfCPath.setMinimumSize(new java.awt.Dimension(50, 20));
        tfCPath.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(tfCPath, gridBagConstraints);

        btCVersion.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btCVersion, gridBagConstraints);
        btCVersion.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CVersion"));
        btCVersion.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CVersion"));

        lbCppCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CppCommand").charAt(0));
        lbCppCommand.setLabelFor(cbCppCommand);
        lbCppCommand.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CppCommand"));
        lbCppCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_CppCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(lbCppCommand, gridBagConstraints);
        lbCppCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CppCommand"));
        lbCppCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CppCommand"));

        cbCppCommand.setMinimumSize(new java.awt.Dimension(100, 18));
        cbCppCommand.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(cbCppCommand, gridBagConstraints);

        tfCppPath.setEditable(false);
        tfCppPath.setMinimumSize(new java.awt.Dimension(50, 20));
        tfCppPath.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(tfCppPath, gridBagConstraints);

        btCppVersion.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CppVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btCppVersion, gridBagConstraints);
        btCppVersion.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CppVersion"));
        btCppVersion.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CppVersion"));

        lbFortranCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_FortranCommand").charAt(0));
        lbFortranCommand.setLabelFor(cbFortranCommand);
        lbFortranCommand.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_FortranCommand"));
        lbFortranCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_FortranCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(lbFortranCommand, gridBagConstraints);
        lbFortranCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_FortranCommand"));
        lbFortranCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_FortranCommand"));

        cbFortranCommand.setMinimumSize(new java.awt.Dimension(100, 18));
        cbFortranCommand.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(cbFortranCommand, gridBagConstraints);

        tfFortranPath.setEditable(false);
        tfFortranPath.setMinimumSize(new java.awt.Dimension(50, 20));
        tfFortranPath.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(tfFortranPath, gridBagConstraints);

        btFortranVersion.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_FortranVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(btFortranVersion, gridBagConstraints);
        btFortranVersion.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_FortranVersion"));
        btFortranVersion.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_FortranVersion"));

        lbCompilerCollection.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CompilerCollection").charAt(0));
        lbCompilerCollection.setLabelFor(cbCompilerSet);
        lbCompilerCollection.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CompilerCollection"));
        lbCompilerCollection.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_CompilerCollection"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 4, 0, 0);
        add(lbCompilerCollection, gridBagConstraints);
        lbCompilerCollection.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CompilerCollection"));
        lbCompilerCollection.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CompilerCollection"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 2, 0, 0);
        add(cbCompilerSet, gridBagConstraints);

        btAdd.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_AddButton").charAt(0));
        btAdd.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_AddButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 6);
        add(btAdd, gridBagConstraints);
        btAdd.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_AddButton"));
        btAdd.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_AddButton"));

        btRemove.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_RemoveButton").charAt(0));
        btRemove.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_RemoveButton"));
        btRemove.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 6);
        add(btRemove, gridBagConstraints);
        btRemove.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_RemoveButton"));
        btRemove.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_RemoveButton"));

        btUp.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_UpButton").charAt(0));
        btUp.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_UpButton"));
        btUp.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 6);
        add(btUp, gridBagConstraints);
        btUp.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_UpButton"));
        btUp.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_UpButton"));

        btDown.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DownButton").charAt(0));
        btDown.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_DownButton"));
        btDown.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 6);
        add(btDown, gridBagConstraints);
        btDown.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_DownButton"));
        btDown.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_DownButton"));

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_RequiredTools").charAt(0));
        jLabel1.setLabelFor(cbMakeRequired);
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_RequiredTools"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 0);
        add(jLabel1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        cbMakeRequired.setSelected(true);
        cbMakeRequired.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_RequiredMake"));
        cbMakeRequired.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbMakeRequired.setEnabled(false);
        cbMakeRequired.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(cbMakeRequired, gridBagConstraints);

        cbGdbRequired.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_RequiredGdb"));
        cbGdbRequired.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbGdbRequired.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(cbGdbRequired, gridBagConstraints);

        cbCRequired.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_RequiredCompiler_C"));
        cbCRequired.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbCRequired.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(cbCRequired, gridBagConstraints);

        cbCppRequired.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_RequiredCompiler_Cpp"));
        cbCppRequired.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbCppRequired.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(cbCppRequired, gridBagConstraints);

        cbFortranRequired.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_RequiredCompiler_Fortran"));
        cbFortranRequired.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbFortranRequired.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        jPanel1.add(cbFortranRequired, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 2, 0, 0);
        add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSeparator1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAdd;
    private javax.swing.JButton btCVersion;
    private javax.swing.JButton btCppVersion;
    private javax.swing.JButton btDown;
    private javax.swing.JButton btFortranVersion;
    private javax.swing.JButton btGdbVersion;
    private javax.swing.JButton btMakeVersion;
    private javax.swing.JButton btRemove;
    private javax.swing.JButton btUp;
    private javax.swing.JComboBox cbCCommand;
    private javax.swing.JCheckBox cbCRequired;
    private javax.swing.JComboBox cbCompilerSet;
    private javax.swing.JComboBox cbCppCommand;
    private javax.swing.JCheckBox cbCppRequired;
    private javax.swing.JComboBox cbFortranCommand;
    private javax.swing.JCheckBox cbFortranRequired;
    private javax.swing.JCheckBox cbGdbRequired;
    private javax.swing.JCheckBox cbMakeRequired;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lbCCommand;
    private javax.swing.JLabel lbCompilerCollection;
    private javax.swing.JLabel lbCppCommand;
    private javax.swing.JLabel lbDirlist;
    private javax.swing.JLabel lbFortranCommand;
    private javax.swing.JLabel lbGdbCommand;
    private javax.swing.JLabel lbMakeCommand;
    private javax.swing.JList lstDirlist;
    private javax.swing.JScrollPane spDirlist;
    private javax.swing.JTextField tfCPath;
    private javax.swing.JTextField tfCppPath;
    private javax.swing.JTextField tfFortranPath;
    private javax.swing.JTextField tfGdbCommand;
    private javax.swing.JTextField tfGdbPath;
    private javax.swing.JTextField tfMakeCommand;
    private javax.swing.JTextField tfMakePath;
    // End of variables declaration//GEN-END:variables
    
}
