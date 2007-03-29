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

package org.netbeans.modules.cnd.ui.options;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Display the "Tools Default" panel */
public class ToolsPanel extends JPanel implements ActionListener, DocumentListener, ListSelectionListener, ItemListener {
    
    private boolean changed;
    private boolean updating = false;
    private ArrayList<String> dirlist;
    private ToolsPanelModel model;
    
    // The following are constants so I can do == rather than "equals"
    private final String MAKE_NAME = "make"; // NOI18N
    private final String GDB_NAME = "gdb"; // NOI18N
    private final String C_NAME = "C"; // NOI18N
    private final String Cpp_NAME = "C++"; // NOI18N
    private final String FORTRAN_NAME = "Fortran"; // NOI18N
    
    private static final String DIRECTORY_MOVE_UP = "Up"; // NOI18N
    private static final String DIRECTORY_MOVE_DOWN = "Down"; // NOI18N
    
    /** The default (or previously selected) C compiler for each CompilerSet */
    private HashMap<String, String> cSelections = new HashMap();
    
    /** The default (or previously selected) C++ compiler for each CompilerSet */
    private HashMap<String, String> cppSelections = new HashMap();
    
    /** The default (or previously selected) Fortran compiler for each CompilerSet */
    private HashMap<String, String> fortranSelections = new HashMap();
    
    private JFileChooser addDirectoryChooser = null;
    private CompilerSetManager csm;
    private String compilerSetName = null;
    
    /** Creates new form ToolsPanel */
    public ToolsPanel() {
        dirlist = Path.getPath();
        model = new ToolsPanelModel();
        csm = CompilerSetManager.getDefault();
        initComponents();
        setName("TAB_ToolsTab"); // NOI18N (used as a pattern...)
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
                file = addDirectoryChooser.getSelectedFile();
                dirlist.add(0, file.getAbsolutePath());
                lstDirlist.setListData(dirlist.toArray());
                lstDirlist.setSelectedIndex(0);
                lstDirlist.ensureIndexIsVisible(0);
                csm = new CompilerSetManager(dirlist);
                updateCompilers();
            }
        } catch (HeadlessException ex) {
        }
    }
    
    private void removeDirectory() {
        int idx = lstDirlist.getSelectedIndex();
        assert idx != -1; // the button shouldn't be enabled
        String dir = dirlist.remove(idx);
        lstDirlist.setListData(dirlist.toArray());
        if (dirlist.size() > 0) {
            if (idx == dirlist.size()) {
                idx--;
            }
            lstDirlist.setSelectedIndex(idx);
            lstDirlist.ensureIndexIsVisible(idx);
        }
        
        for (CompilerSet cs : csm.getCompilerSets()) {
            StringTokenizer tok = new StringTokenizer(cs.getDirectory(), File.pathSeparator);
            while (tok.hasMoreTokens()) {
                String d = tok.nextToken();
                if (d.equals(dir)) {
                    csm = new CompilerSetManager(dirlist);
                    updateCompilers();
                    return;
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
        if (direction == DIRECTORY_MOVE_UP) {
            idx--;
        } else {
            idx++; 
        }
        dirlist.add(idx, dir);
        lstDirlist.setListData(dirlist.toArray());
        lstDirlist.setSelectedIndex(idx);
        lstDirlist.ensureIndexIsVisible(idx);
    }
    
    private void setMakePathField(String cmd) {
        
        if (cmd.length() == 0) {
            cmd = getDefaultMakeCommand();
            tfMakeCommand.setText(cmd);
        }
        String path = findCommandFromPath(cmd);
        if (path != null) {
            tfMakePath.setText(path);
        } else {
            try {  // Want to output Red text and make data_valid return false
                Document doc = tfMakePath.getDocument();
                doc.remove(0, doc.getLength());
                doc.insertString(0, NbBundle.getMessage(ToolsPanel.class, "ERR_NotFound"),  // NOI18N
                        SimpleAttributeSet.EMPTY);
            } catch (BadLocationException ex) {
            }
        }
    }
    
    private void setGdbPathField(String cmd) {
        if (cmd.length() == 0) {
            cmd = getDefaultGdbCommand();
            tfGdbCommand.setText(cmd);
        }
        String path = findCommandFromPath(cmd);
        if (path != null) {
            tfGdbPath.setText(path);
        } else {
            try {  // Want to output Red text and make data_valid return false
                Document doc = tfGdbPath.getDocument();
                doc.remove(0, doc.getLength());
                doc.insertString(0, NbBundle.getMessage(ToolsPanel.class, "ERR_NotFound"),  // NOI18N
                        SimpleAttributeSet.EMPTY);
            } catch (BadLocationException ex) {
            }
        }
    }
    
    private String findCommandFromPath(String cmd) {
        if (Utilities.isWindows() && !cmd.endsWith(".exe")) {
            cmd = cmd + ".exe"; // NOI18N
        }
        File file = new File(cmd);
        
        if (file.exists() && file.isAbsolute()) {
            return file.getAbsolutePath();
        } else {
            for (String dir : dirlist) {
                file = new File(dir, cmd);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }
    
    private String getDefaultMakeCommand() {
        return model.getMakeName();
    }
    
    private String getDefaultGdbCommand() {
        return model.getGdbName();
    }
    
    /** Update the display */
    public void update() {
        updating = true;
        boolean gdb = isGdbEnabled();
        boolean fortran = CppSettings.getDefault().isFortranEnabled();
        
        lbGdbCommand.setVisible(gdb);
        tfGdbCommand.setVisible(gdb);
        tfGdbPath.setVisible(gdb);
        btGdbVersion.setVisible(gdb);
        lbFortranCommand.setVisible(fortran);
        cbFortranCommand.setVisible(fortran);
        tfFortranPath.setVisible(fortran);
        btFortranVersion.setVisible(fortran);
        
        changed = false;
        lstDirlist.setListData(dirlist.toArray());
        setMakePathField(tfMakeCommand.getText());
        setGdbPathField(tfGdbCommand.getText());
        updateCompilers();
        updating = false;
    }
    
    private void updateCompilers() {
        cbCompilerSet.removeAllItems();
        
        if (!csm.getCompilerSets().isEmpty()) {
            String name = model.getCompilerSetName();
            for (CompilerSet cs : csm.getCompilerSets()) {
                cbCompilerSet.addItem(cs);
            }
            CompilerSet cs = csm.getCompilerSet(model.getCompilerSetName());
            if (name.length() > 0 && !name.equals(cs.getName())) {
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        NbBundle.getMessage(ToolsPanel.class, "MSG_MissingCompilerSet")); // NOI18N
                DialogDisplayer.getDefault().notify(d);
            }
            cbCompilerSet.setSelectedItem(cs);
            changeCompilerSet(cs);
        }
    }
    
    private void changeCompilerSet(CompilerSet cs) {
        setCompilerSet(cs);
        cbCCommand.removeAllItems();
        cbCppCommand.removeAllItems();
        cbFortranCommand.removeAllItems();
        tfCppPath.setText("");
        tfCPath.setText("");
        tfFortranPath.setText("");
        
        Tool cSelection = getDefaultCompiler(cs, Tool.CCompiler);
        Tool cppSelection = getDefaultCompiler(cs, Tool.CCCompiler);
        Tool fortranSelection = getDefaultCompiler(cs, Tool.FortranCompiler);
     
        for (Tool tool : cs.getTools()) {
            if (tool.getKind() == Tool.CCompiler) {
                cbCCommand.addItem(tool);
                if (cSelection == tool || cSelection == null) {
                    cbCCommand.setSelectedItem(tool);
                    cbCCommand.setToolTipText(tool.getDisplayName());
                    updateCPath(cs, tool);
                }
            }
            if (tool.getKind() == Tool.CCCompiler) {
                cbCppCommand.addItem(tool);
                if (cppSelection == tool || cppSelection == null) {
                    cbCppCommand.setSelectedItem(tool);
                    cbCppCommand.setToolTipText(tool.getDisplayName());
                    updateCppPath(cs, tool);
                }
            }
            if (tool.getKind() == Tool.FortranCompiler) {
                cbFortranCommand.addItem(tool);
                if (fortranSelection == tool || fortranSelection == null) {
                    cbFortranCommand.setSelectedItem(tool);
                    cbFortranCommand.setToolTipText(tool.getDisplayName());
                    updateFortranPath(cs, tool);
                }
            }
        }
    }
    
    private void updateCPath(CompilerSet cs, Tool tool) {
        tfCPath.setText(tool.getPath());
        tfCPath.setToolTipText(tool.getPath());
    }
    
    private void updateCppPath(CompilerSet cs, Tool tool) {
        tfCppPath.setText(tool.getPath());
        tfCppPath.setToolTipText(tool.getPath());
    }
    
    private void updateFortranPath(CompilerSet cs, Tool tool) {
        tfFortranPath.setText(tool.getPath());
        tfFortranPath.setToolTipText(tool.getPath());
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
        CompilerSet cs = getCompilerSet();
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
     * @param type The compiler type we want
     * @returns The Tool for the requested compiler (or null)
     */
    protected Tool getDefaultCompiler(CompilerSet cs, int type) {
        String name = cs.getName();
        
        if (type == Tool.CCompiler) {
            if (cSelections.get(name) == null) {
                return cs.getTool(model.getCCompilerName());
            } else {
                return cs.getTool(cSelections.get(cs.getName()));
            }
        } else if (type == Tool.CCCompiler) {
            if (cppSelections.get(name) == null) {
                return cs.getTool(model.getCppCompilerName());
            } else {
                return cs.getTool(cppSelections.get(cs.getName()));
            }
        } else if (type == Tool.FortranCompiler) {
            if (fortranSelections.get(name) == null) {
                return cs.getTool(model.getFortranCompilerName());
            } else {
                return cs.getTool(fortranSelections.get(cs.getName()));
            }
        }
        return null;
    }

    /** @returns the active CompilerSet */
    protected CompilerSet getCompilerSet() {
        return csm.getCompilerSet(compilerSetName != null ? compilerSetName : model.getCompilerSetName());
    }
    
    /** @param cs The newly selected CompilerSet */
    protected void setCompilerSet(CompilerSet cs) {
        compilerSetName = cs.getName();
    }
    
    /** Apply changes */
    public void applyChanges() {
        if (changed) {
            CompilerSet cs = getCompilerSet();
            changed = false;
            
            if (compilerSetName != null) {
                model.setCompilerSetName(compilerSetName);
                model.setCompilerSetDirectories(csm.getCompilerSet(compilerSetName).getDirectory());
            }
            if (cSelections.get(cs.getName()) != null) {
                model.setCCompilerName(cSelections.get(cs.getName()));
            }
            if (cppSelections.get(cs.getName()) != null) {
                model.setCppCompilerName(cppSelections.get(cs.getName()));
            }
            if (fortranSelections.get(cs.getName()) != null) {
                model.setFortranCompilerName(fortranSelections.get(cs.getName()));
            }
            CompilerSetManager.setDefault(csm);
        }
    }
    
    /** What to do if user cancels the dialog (nothing) */
    public void cancel() {
        changed = false;
    }
    
    /**
     * Lets NB know if the data in the panel is valid and OK should be enabled
     * 
     * @return Returns true if all data is valid
     */
    public boolean dataValid() {
        return true;
    }
    
    /**
     * Lets caller know if any data has been changed.
     * 
     * @return True if anything has been changed
     */
    public boolean isChanged() {
        return changed;
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
            }
        }
    }
    
    // implemet ItemListener
    public void itemStateChanged(ItemEvent ev) {
        
        if (!updating && ev.getStateChange() == ItemEvent.SELECTED) {
            Object o = ev.getSource();
            Object item = ev.getItem();
            changed = true;
            
            if (o == cbCompilerSet) {
                changeCompilerSet((CompilerSet) item);
            } else if (o == cbCCommand) {
                cbCCommand.setToolTipText(((Tool) item).getDisplayName());
                setDefaultCompiler(Tool.CCompiler, ((Tool) item).getDisplayName());
                updateCPath((CompilerSet) cbCompilerSet.getSelectedItem(), (Tool) item);
            } else if (o == cbCppCommand) {
                cbCppCommand.setToolTipText(((Tool) item).getDisplayName());
                setDefaultCompiler(Tool.CCCompiler, ((Tool) item).getDisplayName());
                updateCppPath((CompilerSet) cbCompilerSet.getSelectedItem(), (Tool) item);
            } else if (o == cbFortranCommand) {
                cbFortranCommand.setToolTipText(((Tool) item).getDisplayName());
                setDefaultCompiler(Tool.FortranCompiler, ((Tool) item).getDisplayName());
                updateFortranPath((CompilerSet) cbCompilerSet.getSelectedItem(), (Tool) item);
            }
        }
    }
    
    // implement DocumentListener
    public void changedUpdate(DocumentEvent ev) {}
    
    public void insertUpdate(DocumentEvent ev) {
        String text;
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
            if (str.indexOf("/") == -1) {
                super.insertString(offs, str, a);
            }
        }
    }
    
    /**
     * Check if the gdb module is enabled. Don't show the gdb line if it isn't.
     *
     * @return true if the gdb module is enabled, false if missing or disabled
     */
    protected boolean isGdbEnabled() {
        Iterator iter = Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class)).allInstances().iterator();
        while (iter.hasNext()) {
            ModuleInfo info = (ModuleInfo) iter.next();
            if (info.getCodeNameBase().equals("org.netbeans.modules.cnd.debugger.gdb") && info.isEnabled()) {
                return true;
            }
        }
        return false;
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
        cbCCommand = new javax.swing.JComboBox();
        cbCCommand.addItemListener(this);
        tfCPath = new javax.swing.JTextField();
        btCVersion = new javax.swing.JButton();
        btCVersion.addActionListener(this);
        lbCppCommand = new javax.swing.JLabel();
        cbCppCommand = new javax.swing.JComboBox();
        cbCppCommand.addItemListener(this);
        tfCppPath = new javax.swing.JTextField();
        btCppVersion = new javax.swing.JButton();
        btCppVersion.addActionListener(this);
        lbFortranCommand = new javax.swing.JLabel();
        cbFortranCommand = new javax.swing.JComboBox();
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
        jSeparator1 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(600, 600));
        lbDirlist.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_DirlistLabel").charAt(0));
        lbDirlist.setLabelFor(spDirlist);
        lbDirlist.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_DirlistLabel"));
        lbDirlist.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_DirListLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 4);
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

        btMakeVersion.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_MakeVersion").charAt(0));
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

        btGdbVersion.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_GdbVersion").charAt(0));
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

        btCVersion.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CVersion").charAt(0));
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

        lbFortranCommand.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_FortranCommand"));
        lbFortranCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_FortranCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 6, 0);
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
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
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
        btAdd.setActionCommand("Add");
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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
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
    private javax.swing.JComboBox cbCompilerSet;
    private javax.swing.JComboBox cbCppCommand;
    private javax.swing.JComboBox cbFortranCommand;
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
