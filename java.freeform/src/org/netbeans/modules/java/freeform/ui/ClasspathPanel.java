/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.freeform.ui;

import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import org.netbeans.modules.ant.freeform.spi.ProjectPropertiesPanel;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.JavaProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Konecny
 */
public class ClasspathPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private DefaultListModel listModel;
    private File lastChosenFile = null;
    private boolean isSeparateClasspath = true;
    private List/*<ProjectModel.CompilationUnitKey>*/ compUnitsKeys;
    private boolean ignoreEvent;
    private ProjectModel model;
    
    /** Creates new form ClasspathPanel */
    public ClasspathPanel() {
        this(true);
    }
    
    public ClasspathPanel(boolean isWizard) {
        initComponents();
        jTextArea1.setBackground(getBackground());
        listModel = new DefaultListModel();
        classpath.setModel(listModel);
        if (!isWizard) {
            jTextArea1.setText(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_Explanation"));
        }
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( ClasspathPanel.class );
    }
    
    private void updateControls() {
        sourceFolder.removeAllItems();
        compUnitsKeys = model.createCompilationUnitKeys();
        isSeparateClasspath = !ProjectModel.isSingleCompilationUnit(compUnitsKeys);
        List names = createComboContent(compUnitsKeys, model.getEvaluator(), model.getNBProjectFolder());
        Iterator it = names.iterator();
        while (it.hasNext()) {
            String nm = (String)it.next();
            sourceFolder.addItem(nm);
        }
        if (names.size() > 0) {
            ignoreEvent = true;
            sourceFolder.setSelectedIndex(0);
            ignoreEvent = false;
        }
        loadClasspath();        
        
        // enable/disable "Separate Classpath" checkbox
        boolean sepClasspath = model.canHaveSeparateClasspath();
        separateClasspath.setEnabled(sepClasspath);
        jLabel2.setEnabled(sepClasspath && isSeparateClasspath);
        sourceFolder.setEnabled(sepClasspath && isSeparateClasspath);
        // set initial value of the checkbox
        ignoreEvent = true;
        separateClasspath.setSelected(isSeparateClasspath);
        ignoreEvent = false;

        // disable classpath panel and Add Classpath button if there is 
        // no compilation unit ot be configured
        addClasspath.setEnabled(compUnitsKeys.size() > 0);
        classpath.setEnabled(compUnitsKeys.size() > 0);
    }
    
    
    static List/*<String>*/ createComboContent(List/*<ProjectModel.CompilationUnitKey>*/ compilationUnitKeys, PropertyEvaluator evaluator, File nbProjectFolder) {
        List l = new ArrayList();
        if (ProjectModel.isSingleCompilationUnit(compilationUnitKeys)) {
            return l;
        }
        Iterator it = compilationUnitKeys.iterator();
        while (it.hasNext()) {
            ProjectModel.CompilationUnitKey cul = (ProjectModel.CompilationUnitKey)it.next();
            String name;
            if (cul.locations.size() == 1) {
                if (cul.label != null) {
                    name = cul.label + " ["+SourceFoldersPanel.getLocationDisplayName(evaluator, nbProjectFolder, (String)cul.locations.get(0))+"]"; // NOI18N
                } else {
                    name = convertListToString(cul.locations);
                }
            } else {
                    name = convertListToString(cul.locations);
            }
            l.add(name);
        }
        return l;
    }
    
    private static String convertListToString(List/*<String>*/ l) {
        StringBuffer sb = new StringBuffer();
        Iterator it = l.iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            sb.append(s);
            if (it.hasNext()) {
                sb.append(File.pathSeparatorChar+" "); // NOI18N
            }
        }
        return sb.toString();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        addClasspath = new javax.swing.JButton();
        removeClasspath = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        classpath = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        sourceFolder = new javax.swing.JComboBox();
        moveUp = new javax.swing.JButton();
        moveDown = new javax.swing.JButton();
        jTextArea1 = new javax.swing.JTextArea();
        separateClasspath = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(275, 202));
        jLabel2.setLabelFor(sourceFolder);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_jLabel2"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_jLabel2"));

        jLabel3.setLabelFor(classpath);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_jLabel3"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_jLabel3"));

        org.openide.awt.Mnemonics.setLocalizedText(addClasspath, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "BTN_ClasspathPanel_addClasspath"));
        addClasspath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClasspathActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(addClasspath, gridBagConstraints);
        addClasspath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_addClasspath"));

        org.openide.awt.Mnemonics.setLocalizedText(removeClasspath, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "BTN_ClasspathPanel_removeClasspath"));
        removeClasspath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClasspathActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(removeClasspath, gridBagConstraints);
        removeClasspath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_removeClasspath"));

        classpath.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                classpathValueChanged(evt);
            }
        });

        jScrollPane1.setViewportView(classpath);
        classpath.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_classpath"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        sourceFolder.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceFolderItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(sourceFolder, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanel1, gridBagConstraints);

        moveUp.setText(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_Move_Up"));
        moveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(moveUp, gridBagConstraints);

        moveDown.setText(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_Move_Down"));
        moveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(moveDown, gridBagConstraints);

        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "MSG_ClasspathPanel_jTextArea"));
        jTextArea1.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jTextArea1, gridBagConstraints);
        jTextArea1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSN_ClasspathPanel_jTextArea"));
        jTextArea1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "ACSD_ClasspathPanel_jTextArea"));

        separateClasspath.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(separateClasspath, org.openide.util.NbBundle.getMessage(ClasspathPanel.class, "LBL_ClasspathPanel_sepatateClasspath"));
        separateClasspath.setMargin(new java.awt.Insets(0, 0, 0, 0));
        separateClasspath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                separateClasspathActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(separateClasspath, gridBagConstraints);

    }//GEN-END:initComponents

    private void classpathValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_classpathValueChanged
        updateButtons();
    }//GEN-LAST:event_classpathValueChanged

    private void separateClasspathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_separateClasspathActionPerformed
        if (ignoreEvent) {
            return;
        }
        applyChanges();
        isSeparateClasspath = separateClasspath.isSelected();
        model.updateCompilationUnits(isSeparateClasspath);
        updateControls();
    }//GEN-LAST:event_separateClasspathActionPerformed

    private void moveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownActionPerformed
        int indices[] = classpath.getSelectedIndices();
        if (indices.length == 0 ||
                indices[indices.length - 1] == listModel.getSize() - 1) {
            return;
        }
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            Object o = listModel.remove(index);
            index++;
            listModel.add(index, o);
            indices[i] = index;
        }
        classpath.setSelectedIndices(indices);
        applyChanges();
        updateButtons();
    }//GEN-LAST:event_moveDownActionPerformed

    private void moveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpActionPerformed
        int indices[] = classpath.getSelectedIndices();
        if (indices.length == 0 || indices[0] == 0) {
            return;
        }
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            Object o = listModel.remove(index);
            index--;
            listModel.add(index, o);
            indices[i] = index;
        }
        classpath.setSelectedIndices(indices);
        applyChanges();
        updateButtons();
    }//GEN-LAST:event_moveUpActionPerformed

    private void sourceFolderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sourceFolderItemStateChanged
        if (ignoreEvent) {
            return;
        }
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            int index = findIndex(evt.getItem());
            // if index == -1 then item was removed and will not be saved
            if (index != -1) {
                saveClasspath(index);
            }
        } else {
            loadClasspath();
        }
        updateButtons();
    }//GEN-LAST:event_sourceFolderItemStateChanged

    private int findIndex(Object o) {
        for (int i=0; i<sourceFolder.getModel().getSize(); i++) {
            if (sourceFolder.getModel().getElementAt(i).equals(o)) {
                return i;
            }
        }
        return -1;
    }

    /** Source package combo is changing - take classpath from the listbox and
     * store it in compilaiton unit identified by the index.*/
    private void saveClasspath(int index) {
        ProjectModel.CompilationUnitKey key = (ProjectModel.CompilationUnitKey)compUnitsKeys.get(index);
        JavaProjectGenerator.JavaCompilationUnit cu = model.getCompilationUnit(key);
        updateCompilationUnitCompileClasspath(cu);
    }

    /** Source package has changed - find current source package and read its classpath and
     * update classpath listbox with it.*/
    private void loadClasspath() {
        int index;
        if (isSeparateClasspath) {
            index = sourceFolder.getSelectedIndex();
            if (index == -1) {
                return;
            }
        } else {
            index = 0;
        }
        ProjectModel.CompilationUnitKey key = (ProjectModel.CompilationUnitKey)compUnitsKeys.get(index);
        JavaProjectGenerator.JavaCompilationUnit cu = model.getCompilationUnit(key);
        updateJListClassPath(cu.classpath);
    }

    /** Update compilation unit classpath list with the classpath specified
     * in classpath list box. */
    private void updateCompilationUnitCompileClasspath(JavaProjectGenerator.JavaCompilationUnit cu) {
        List cps = cu.classpath;
        if (cps != null) {
            Iterator it = cps.iterator();
            while (it.hasNext()) {
                JavaProjectGenerator.JavaCompilationUnit.CP cp = (JavaProjectGenerator.JavaCompilationUnit.CP)it.next();
                if (cp.mode.equals(ProjectModel.CLASSPATH_MODE_COMPILE)) {
                    it.remove();
                    // there should be only one, but go on
                    // break;
                }
            }
        }
        if (classpath.getModel().getSize() == 0) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<classpath.getModel().getSize(); i++) {
            File f = new File((String)classpath.getModel().getElementAt(i));
            String path = Util.relativizeLocation(model.getBaseFolder(), model.getNBProjectFolder(), f);
            sb.append(path);
            if (i+1<classpath.getModel().getSize()) {
                sb.append(File.pathSeparatorChar);
            }
        }
        if (sb.length() > 0) {
            if (cps == null) {
                cps = new ArrayList();
                cu.classpath = cps;
            }
            JavaProjectGenerator.JavaCompilationUnit.CP cp = new JavaProjectGenerator.JavaCompilationUnit.CP();
            cp.mode = ProjectModel.CLASSPATH_MODE_COMPILE;
            cp.classpath = sb.toString();
            cps.add(cp);
        }
    }

    /** Reads "compile" mode classpath and updates panel's list box.*/
    private void updateJListClassPath(List/*<JavaProjectGenerator.JavaCompilationUnit.CP>*/ cps) {
        listModel.removeAllElements();
        if (cps == null) {
            return;
        }
        Iterator it = cps.iterator();
        while (it.hasNext()) {
            JavaProjectGenerator.JavaCompilationUnit.CP cp = (JavaProjectGenerator.JavaCompilationUnit.CP)it.next();
            if (cp.mode.equals(ProjectModel.CLASSPATH_MODE_COMPILE)) {
                String[] cpa = PropertyUtils.tokenizePath(model.getEvaluator().evaluate(cp.classpath));
                for (int i=0; i<cpa.length; i++) {
                    String path = cpa[i];
                    path = PropertyUtils.resolveFile(model.getNBProjectFolder(), path).getAbsolutePath();
                    if (path != null) {
                        listModel.addElement(path);
                    }
                }
            }
        }
        updateButtons();
    }
    
    private void updateButtons() {
        int indices[] = classpath.getSelectedIndices();
        removeClasspath.setEnabled(listModel.getSize() > 0 && indices.length != 0);
        moveUp.setEnabled(indices.length > 0 && indices[0] != 0);
        moveDown.setEnabled(indices.length > 0 && indices[indices.length - 1] != listModel.getSize() - 1);
    }
    
    private void removeClasspathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClasspathActionPerformed
        int entries[] = classpath.getSelectedIndices();
        for (int i = 0; i < entries.length; i++) {
            listModel.remove(entries[i] - i);
        }
        applyChanges();
        updateButtons();
    }//GEN-LAST:event_removeClasspathActionPerformed

    private void addClasspathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClasspathActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        if (lastChosenFile != null) {
            chooser.setSelectedFile(lastChosenFile);
        } else {
            File files[] = model.getBaseFolder().listFiles();
            if (files != null && files.length > 0) {
                chooser.setSelectedFile(files[0]);
            } else {
                chooser.setSelectedFile(model.getBaseFolder());
            }
        }
        chooser.setDialogTitle(NbBundle.getMessage(ClasspathPanel.class, "LBL_Browse_Classpath"));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File files[] = chooser.getSelectedFiles();
            for (int i=0; i<files.length; i++) {
                File file = FileUtil.normalizeFile(files[i]);
                listModel.addElement(file.getAbsolutePath());
                lastChosenFile = file;
            }
            applyChanges();
            updateButtons();
        }
    }//GEN-LAST:event_addClasspathActionPerformed

    private void applyChanges() {
        if (isSeparateClasspath) {
            if (sourceFolder.getSelectedIndex() != -1) {
                saveClasspath(sourceFolder.getSelectedIndex());
            }
        } else {
            saveClasspath(0);
        }
    }

    public static class Panel implements ProjectPropertiesPanel {
        
        private ClasspathPanel panel;
        private ProjectModel model;
        
        public Panel(ProjectModel model) {
            this.model = model;
        }
    
        public void storeValues() {
        }    

        public String getDisplayName() {
            return NbBundle.getMessage(ClasspathPanel.class, "LBL_ProjectCustomizer_Category_Classpath");
        }

        public JComponent getComponent() {
            if (panel == null) {
                panel = new ClasspathPanel(false);
                panel.setModel(model);
            }
            return panel;
        }

        public int getPreferredPosition() {
            return 400;
        }
        
    }
    
    public void setModel(ProjectModel model) {
        this.model = model;
        updateControls();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addClasspath;
    private javax.swing.JList classpath;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton moveDown;
    private javax.swing.JButton moveUp;
    private javax.swing.JButton removeClasspath;
    private javax.swing.JCheckBox separateClasspath;
    private javax.swing.JComboBox sourceFolder;
    // End of variables declaration//GEN-END:variables
    
}
