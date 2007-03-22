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

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
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
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Display the "Tools Default" panel
 */
public class ToolsPanel extends JPanel implements ActionListener, DocumentListener, ListSelectionListener {
    
    private boolean changed = false;
    private static ToolsPanel instance;
    private ArrayList dirlist;
    
    // The following are constannts so I can do == rather than "equals"
    private final String MAKE_NAME = "make"; // NOI18N
    private final String GDB_NAME = "gdb"; // NOI18N
    private final String C_NAME = "C"; // NOI18N
    private final String Cpp_NAME = "C++"; // NOI18N
    private final String FORTRAN_NAME = "Fortran"; // NOI18N
    
    private static final String DIRECTORY_MOVE_UP = "Up"; // NOI18N
    private static final String DIRECTORY_MOVE_DOWN = "Down"; // NOI18N
    
    private JFileChooser addDirectory;
    
    /** Creates new form ToolsPanel */
    public ToolsPanel() {
        initComponents();
        
        tfMakeCommand.getDocument().putProperty(Document.TitleProperty, MAKE_NAME);
        tfGdbCommand.getDocument().putProperty(Document.TitleProperty, GDB_NAME);
        tfCCommand.getDocument().putProperty(Document.TitleProperty, C_NAME);
        tfCppCommand.getDocument().putProperty(Document.TitleProperty, Cpp_NAME);
        tfFortranCommand.getDocument().putProperty(Document.TitleProperty, FORTRAN_NAME);
        
        cbCompilerCollection.addItem("GNU Compiler Collection"); // TEMPORARY
        cbCompilerCollection.addItem("Sun Studio Compiler Collection"); // TEMPORARY
        lstDirlist.addListSelectionListener(this);
        
        tfMakeCommand.getDocument().addDocumentListener(this);
        tfGdbCommand.getDocument().addDocumentListener(this);
        tfCCommand.getDocument().addDocumentListener(this);
        tfCppCommand.getDocument().addDocumentListener(this);
        tfFortranCommand.getDocument().addDocumentListener(this);
        addDirectory = null;
        setName("TAB_ToolsTab"); // NOI18N (used as a pattern...)
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        changed = true;
        if (o instanceof JButton) {
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
            } else if (title == C_NAME) {
                setCPathField(text);
            } else if (title == Cpp_NAME) {
                setCppPathField(text);
            } else if (title == FORTRAN_NAME) {
                setFortranPathField(text);
            }
        } catch (BadLocationException ex) {
        };
    }
    
    public void removeUpdate(DocumentEvent ev) {
        insertUpdate(ev);
    }
    
    private void addDirectory() {
        File file;
        int rc;
        
        if (addDirectory == null) {
            addDirectory = new JFileChooser();
            addDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            addDirectory.setDialogTitle(getString("LBL_AddDirectoryTitle")); // NOI18N
            addDirectory.setApproveButtonText(getString("LBL_AddDirectoryAcceptButton")); // NOI18N
            addDirectory.setApproveButtonMnemonic(getString("MNEM_AddDirectoryAcceptButton").charAt(0)); // NOI18N
        }
        try {
            rc = addDirectory.showDialog(this, null);
            if (rc == JFileChooser.APPROVE_OPTION) {
                file = addDirectory.getSelectedFile();
                dirlist.add(0, file.getAbsolutePath());
                lstDirlist.setListData(dirlist.toArray());
                lstDirlist.setSelectedIndex(0);
                lstDirlist.ensureIndexIsVisible(0);
            }
        } catch (HeadlessException ex) {
        }
    }
    
    private void removeDirectory() {
        int idx = lstDirlist.getSelectedIndex();
        assert idx != -1; // the button shouldn't be enabled
        dirlist.remove(idx);
        lstDirlist.setListData(dirlist.toArray());
        if (dirlist.size() > 0) {
            if (idx == dirlist.size()) {
                idx--;
            }
            lstDirlist.setSelectedIndex(idx);
            lstDirlist.ensureIndexIsVisible(idx);
        }
    }
    
    private void moveDirectory(String direction) {
        assert direction == DIRECTORY_MOVE_UP || direction == DIRECTORY_MOVE_DOWN;
        int idx = lstDirlist.getSelectedIndex();
        assert idx != -1; // the button shouldn't be enabled
        String dir = (String) dirlist.get(idx);
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
                doc.insertString(0, "<Not Found>", SimpleAttributeSet.EMPTY);  // FIXME
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
                doc.insertString(0, "<Not Found>", SimpleAttributeSet.EMPTY);  // FIXME
            } catch (BadLocationException ex) {
            }
        }
    }
    
    private void setCPathField(String cmd) {
        if (cmd.length() == 0) {
            cmd = getDefaultCCommand();
            tfCCommand.setText(cmd);
        }
        String path = findCommandFromPath(cmd);
        if (path != null) {
            tfCPath.setText(path);
        } else {
            try {  // Want to output Red text and make data_valid return false
                Document doc = tfCPath.getDocument();
                doc.remove(0, doc.getLength());
                doc.insertString(0, "<Not Found>", SimpleAttributeSet.EMPTY);  // FIXME
            } catch (BadLocationException ex) {
            }
        }
    }
    
    private void setCppPathField(String cmd) {
        if (cmd.length() == 0) {
            cmd = getDefaultCppCommand();
            tfCppCommand.setText(cmd);
        }
        String path = findCommandFromPath(cmd);
        if (path != null) {
            tfCppPath.setText(path);
        } else {
            try {  // Want to output Red text and make data_valid return false
                Document doc = tfCppPath.getDocument();
                doc.remove(0, doc.getLength());
                doc.insertString(0, "<Not Found>", SimpleAttributeSet.EMPTY);  // FIXME
            } catch (BadLocationException ex) {
            }
        }
    }
    
    private void setFortranPathField(String cmd) {
        if (cmd.length() == 0) {
            cmd = getDefaultFortranCommand();
            tfFortranCommand.setText(cmd);
        }
        String path = findCommandFromPath(cmd);
        if (path != null) {
            tfFortranPath.setText(path);
        } else {
            try {  // Want to output Red text and make data_valid return false
                Document doc = tfFortranPath.getDocument();
                doc.remove(0, doc.getLength());
                doc.insertString(0, "<Not Found>", SimpleAttributeSet.EMPTY);  // FIXME
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
            Iterator<String> iter = dirlist.iterator();
            while (iter.hasNext()) {
                String dir = iter.next();
                file = new File(dir, cmd);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }
    
    private String getDefaultMakeCommand() {
        String def = System.getProperty("netbeans.cnd.default_make_command"); // NOI18N
        if (def == null) {
            def = "make"; // NOI18N
        }
        return def;
    }
    
    private String getDefaultGdbCommand() {
        String def = System.getProperty("netbeans.cnd.default_gdb_command"); // NOI18N
        if (def == null) {
            def = "gdb"; // NOI18N
        }
        return def;
    }
    
    private String getDefaultCCommand() {
        String def = System.getProperty("netbeans.cnd.default_C_command"); // NOI18N
        if (def == null) {
            def = "gcc"; // NOI18N
        }
        return def;
    }
    
    private String getDefaultCppCommand() {
        String def = System.getProperty("netbeans.cnd.default_C++_command"); // NOI18N
        if (def == null) {
            def = "g++"; // NOI18N
        }
        return def;
    }
    
    private String getDefaultFortranCommand() {
        String def = System.getProperty("netbeans.cnd.default_Fortran_command"); // NOI18N
        if (def == null) {
            def = "g77"; // NOI18N
        }
        return def;
    }
    
    /** Do various localization and accessibility stuff */
    private void fixup(JComponent comp, String key) {
        fixup(comp, key, null);
    }
    
    /**
     * Do various localization and accessibility stuff. All bundle entries for the
     * components created in this panel must have the same base name with the prefixes
     * referenced below.
     *
     * @param comp The component being localized and accessiblized
     * @param key The base key for the component
     * @labelFor An optional component labels reference (can be null)
     */
    private void fixup(JComponent comp, String key, Component labelFor) {
        if (comp instanceof JLabel) {
            JLabel label = (JLabel) comp;
            label.setText(getString("LBL_" + key)); // NOI18N
            label.setDisplayedMnemonic(getString("MNEM_" + key).charAt(0)); // NOI18N
            label.getAccessibleContext().setAccessibleName(getString("ACSN_" + key)); // NOI18N
            label.getAccessibleContext().setAccessibleDescription(getString("ACSD_" + key)); // NOI18N
            if (labelFor != null) {
                label.setLabelFor(labelFor);
                if (labelFor instanceof JTextField) {
                    ((JTextField) labelFor).addActionListener(this);
                }
            }
        } else if (comp instanceof JButton) {
            JButton button = (JButton) comp;
            button.setText(getString("LBL_" + key)); // NOI18N
            button.setMnemonic(getString("MNEM_" + key).charAt(0)); // NOI18N
            button.getAccessibleContext().setAccessibleName(getString("ACSN_" + key)); // NOI18N
            button.getAccessibleContext().setAccessibleDescription(getString("ACSD_" + key)); // NOI18N
            button.addActionListener(this);
        } else if (comp instanceof JList) {
            JList list = (JList) comp;
            list.getAccessibleContext().setAccessibleName(getString("ACSN_" + key)); // NOI18N
            list.getAccessibleContext().setAccessibleDescription(getString("ACSD_" + key)); // NOI18N
        }
    }
    
    private String getString(String key) {
        return NbBundle.getMessage(ToolsPanel.class, key);
    }
    
    private static class NameOnlyDocument extends PlainDocument {
        
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (str.indexOf("/") == -1) {
                super.insertString(offs, str, a);
            }
        }
    }
    
    /** Update the display */
    public void update() {
//        System.err.println("ToolsPanel.update:");
        boolean gdb = isGdbEnabled();
        boolean fortran = CppSettings.getDefault().isFortranEnabled();
        
        lbGdbCommand.setVisible(gdb);
        tfGdbCommand.setVisible(gdb);
        tfGdbPath.setVisible(gdb);
        btGdbVersion.setVisible(gdb);
        lbFortranCommand.setVisible(fortran);
        tfFortranCommand.setVisible(fortran);
        tfFortranPath.setVisible(fortran);
        btFortranVersion.setVisible(fortran);
        
        changed = false;
        dirlist = Path.getPathAsList();
        lstDirlist.setListData(dirlist.toArray());
        setMakePathField(tfMakeCommand.getText());
        setGdbPathField(tfGdbCommand.getText());
        setCPathField(tfCCommand.getText());
        setCppPathField(tfCppCommand.getText());
        if (CppSettings.getDefault().isFortranEnabled()) {
            setFortranPathField(tfFortranCommand.getText());
        }
    }

    /**
     * Apply changes
     */
    public void applyChanges() {
//        System.err.println("ToolsPanel.applyChanges:");
    }
    
    /** What to do if user cancels the dialog (nothing) */
    public void cancel() {
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
        return false;
    }
    
    /**
     * Check if the gdb module is enabled. Don't show the gdb line if it isn't.
     *
     * @return true if the gdb module is enabled, false if missing or disabled
     */
    private boolean isGdbEnabled() {
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
        lbCompilerSets = new javax.swing.JLabel();
        spCompilerSets = new javax.swing.JScrollPane();
        jpCompilerSets = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        lbMakeCommand = new javax.swing.JLabel();
        tfMakeCommand = new JTextField(new NameOnlyDocument(), null, 0);
        tfMakePath = new javax.swing.JTextField();
        btMakeVersion = new javax.swing.JButton();
        lbGdbCommand = new javax.swing.JLabel();
        tfGdbCommand = new JTextField(new NameOnlyDocument(), null, 0);
        tfGdbPath = new javax.swing.JTextField();
        btGdbVersion = new javax.swing.JButton();
        lbCCommand = new javax.swing.JLabel();
        tfCCommand = new JTextField(new NameOnlyDocument(), null, 0);
        tfCPath = new javax.swing.JTextField();
        btCVersion = new javax.swing.JButton();
        lbCppCommand = new javax.swing.JLabel();
        tfCppCommand = new JTextField(new NameOnlyDocument(), null, 0);
        tfCppPath = new javax.swing.JTextField();
        btCppVersion = new javax.swing.JButton();
        lbFortranCommand = new javax.swing.JLabel();
        tfFortranCommand = new JTextField(new NameOnlyDocument(), null, 0);
        tfFortranPath = new javax.swing.JTextField();
        btFortranVersion = new javax.swing.JButton();
        lbCompilerCollection = new javax.swing.JLabel();
        cbCompilerCollection = new javax.swing.JComboBox();
        btAdd = new javax.swing.JButton();
        btRemove = new javax.swing.JButton();
        btUp = new javax.swing.JButton();
        btDown = new javax.swing.JButton();
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

        lbCompilerSets.setLabelFor(spCompilerSets);
        lbCompilerSets.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CompilerSets"));
        lbCompilerSets.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_CompilerSets"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 0);
        add(lbCompilerSets, gridBagConstraints);
        lbCompilerSets.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CompilerSets"));
        lbCompilerSets.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CompilerSets"));

        spCompilerSets.setMaximumSize(new java.awt.Dimension(32767, 50));
        spCompilerSets.setMinimumSize(new java.awt.Dimension(100, 50));
        spCompilerSets.setPreferredSize(new java.awt.Dimension(150, 50));
        jpCompilerSets.setLayout(new java.awt.GridBagLayout());

        jCheckBox1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CS_GNU_Compilsers"));
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 4, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jpCompilerSets.add(jCheckBox1, gridBagConstraints);

        jCheckBox2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CS_SunCompilers"));
        jCheckBox2.setActionCommand("sun compilers");
        jCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox2.setMargin(new java.awt.Insets(0, 4, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jpCompilerSets.add(jCheckBox2, gridBagConstraints);

        spCompilerSets.setViewportView(jpCompilerSets);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 0, 6);
        add(spCompilerSets, gridBagConstraints);

        lbMakeCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_MakeCommand").charAt(0));
        lbMakeCommand.setLabelFor(tfMakeCommand);
        lbMakeCommand.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_MakeCommand"));
        lbMakeCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_MakeCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(lbMakeCommand, gridBagConstraints);
        lbMakeCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_MakeCommand"));
        lbMakeCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_MakeCommand"));

        tfMakeCommand.setMinimumSize(new java.awt.Dimension(80, 20));
        tfMakeCommand.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfMakeCommand, gridBagConstraints);

        tfMakePath.setEditable(false);
        tfMakePath.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        tfMakePath.setMinimumSize(new java.awt.Dimension(50, 20));
        tfMakePath.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
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
        gridBagConstraints.gridy = 7;
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
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(lbGdbCommand, gridBagConstraints);
        lbGdbCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_GdbCommand"));
        lbGdbCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_GdbCommand"));

        tfGdbCommand.setMinimumSize(new java.awt.Dimension(80, 20));
        tfGdbCommand.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfGdbCommand, gridBagConstraints);

        tfGdbPath.setEditable(false);
        tfGdbPath.setMinimumSize(new java.awt.Dimension(50, 20));
        tfGdbPath.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
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
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btGdbVersion, gridBagConstraints);
        btGdbVersion.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_GdbVersion"));
        btGdbVersion.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_GdbVersion"));

        lbCCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CCommand").charAt(0));
        lbCCommand.setLabelFor(tfCCommand);
        lbCCommand.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CCommand"));
        lbCCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_CCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(lbCCommand, gridBagConstraints);
        lbCCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CCommand"));
        lbCCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CCommand"));

        tfCCommand.setMinimumSize(new java.awt.Dimension(80, 20));
        tfCCommand.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCCommand, gridBagConstraints);

        tfCPath.setEditable(false);
        tfCPath.setMinimumSize(new java.awt.Dimension(50, 20));
        tfCPath.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
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
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btCVersion, gridBagConstraints);
        btCVersion.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CVersion"));
        btCVersion.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CVersion"));

        lbCppCommand.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CppCommand").charAt(0));
        lbCppCommand.setLabelFor(tfCppCommand);
        lbCppCommand.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CppCommand"));
        lbCppCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_CppCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(lbCppCommand, gridBagConstraints);
        lbCppCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CppCommand"));
        lbCppCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CppCommand"));

        tfCppCommand.setMinimumSize(new java.awt.Dimension(80, 20));
        tfCppCommand.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(tfCppCommand, gridBagConstraints);

        tfCppPath.setEditable(false);
        tfCppPath.setMinimumSize(new java.awt.Dimension(50, 20));
        tfCppPath.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(tfCppPath, gridBagConstraints);

        btCppVersion.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CppVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btCppVersion, gridBagConstraints);
        btCppVersion.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CppVersion"));
        btCppVersion.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CppVersion"));

        lbFortranCommand.setLabelFor(tfFortranCommand);
        lbFortranCommand.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_FortranCommand"));
        lbFortranCommand.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_FortranCommand"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 6, 0);
        add(lbFortranCommand, gridBagConstraints);
        lbFortranCommand.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_FortranCommand"));
        lbFortranCommand.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_FortranCommand"));

        tfFortranCommand.setMinimumSize(new java.awt.Dimension(80, 20));
        tfFortranCommand.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 5, 0);
        add(tfFortranCommand, gridBagConstraints);

        tfFortranPath.setEditable(false);
        tfFortranPath.setMinimumSize(new java.awt.Dimension(50, 20));
        tfFortranPath.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 100.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
        add(tfFortranPath, gridBagConstraints);

        btFortranVersion.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_FortranVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(btFortranVersion, gridBagConstraints);
        btFortranVersion.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_FortranVersion"));
        btFortranVersion.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_FortranVersion"));

        lbCompilerCollection.setLabelFor(cbCompilerCollection);
        lbCompilerCollection.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CompilerCollection"));
        lbCompilerCollection.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("HINT_CompilerCollection"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(lbCompilerCollection, gridBagConstraints);
        lbCompilerCollection.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_CompilerCollection"));
        lbCompilerCollection.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_CompilerCollection"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 0);
        add(cbCompilerCollection, gridBagConstraints);

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
        btRemove.setActionCommand("Remove");
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
        btUp.setActionCommand("Up");
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
        btDown.setActionCommand("Down");
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
        gridBagConstraints.gridy = 13;
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
    private javax.swing.JComboBox cbCompilerCollection;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel jpCompilerSets;
    private javax.swing.JLabel lbCCommand;
    private javax.swing.JLabel lbCompilerCollection;
    private javax.swing.JLabel lbCompilerSets;
    private javax.swing.JLabel lbCppCommand;
    private javax.swing.JLabel lbDirlist;
    private javax.swing.JLabel lbFortranCommand;
    private javax.swing.JLabel lbGdbCommand;
    private javax.swing.JLabel lbMakeCommand;
    private javax.swing.JList lstDirlist;
    private javax.swing.JScrollPane spCompilerSets;
    private javax.swing.JScrollPane spDirlist;
    private javax.swing.JTextField tfCCommand;
    private javax.swing.JTextField tfCPath;
    private javax.swing.JTextField tfCppCommand;
    private javax.swing.JTextField tfCppPath;
    private javax.swing.JTextField tfFortranCommand;
    private javax.swing.JTextField tfFortranPath;
    private javax.swing.JTextField tfGdbCommand;
    private javax.swing.JTextField tfGdbPath;
    private javax.swing.JTextField tfMakeCommand;
    private javax.swing.JTextField tfMakePath;
    // End of variables declaration//GEN-END:variables
    
}
