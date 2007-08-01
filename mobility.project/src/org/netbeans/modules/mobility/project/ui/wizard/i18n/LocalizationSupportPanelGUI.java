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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * I18nSupportAdvancedSettingsPanel.java
 *
 * Created on May 11, 2004, 11:22 AM
 */
package org.netbeans.modules.mobility.project.ui.wizard.i18n;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.java.project.JavaProjectConstants;
import javax.swing.text.Document;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.util.NbBundle;

/**
 *
 * @author  breh
 */
public class LocalizationSupportPanelGUI extends JPanel implements ActionListener, ItemListener, DocumentListener {
    /** Creates new form I18nSupportAdvancedSettingsPanel */
    
    
    private static final Dimension PREF_DIM = new Dimension(560, 350);
    
    private static final ListCellRenderer CELL_RENDERER = new NodeCellRenderer();
    private Project project;
    private AntProjectHelper helper;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    private Document pkgComboBoxDocument = null;
    
    
    
    
    public LocalizationSupportPanelGUI() {
        initComponents();
        initAccessibility();
        //initValues( project, null, null );
        Component packageEditor = pkgComboBox.getEditor().getEditorComponent();
        if (packageEditor instanceof javax.swing.JTextField) {
            pkgComboBoxDocument = ((javax.swing.JTextField) packageEditor).getDocument();
            pkgComboBoxDocument.addDocumentListener(this);
        }
        pkgComboBox.setRenderer( CELL_RENDERER );
        pkgComboBox.addActionListener( this );
    }
    
    
    public String getTargetClassName() {
        return normalizedString(clsTextField.getText());
    }
    
    public String getTargetMessageFileName() {
        return normalizedString(rnTextField.getText());
    }
    
    // returns the message filename with extensions
    public String getTargetMessageFileNameExt() {
        return getTargetMessageFileName()+".properties";    // NOI18N
    }
    
    public String getTargetDefaultStringValue() {
        return normalizedString(dsTextField.getText());
    }
    
    public String getTargetDefaultErrorMessageValue() {
        return normalizedString(emTextField.getText());
    }
    
    public String getCreatedClass() {
        return cClassTextField.getText();
    }
    
    public String getCreatedMessageFile() {
        return cResourceTextField.getText();
    }
    
    
    private static String normalizedString(String text) {
        if (text == null)
            return null;
        text = text.trim();
        if (text.length() <= 0)
            return null;
        return text;
    }
    
    
    void initValues( final Project project, final FileObject template, final FileObject preselectedFolder ) {
        this.project = project;
        this.helper = project.getLookup().lookup(AntProjectHelper.class);
        prjTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        final Sources sources = ProjectUtils.getSources(project);
        final SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        final SourceGroup preselectedGroup = getPreselectedGroup(groups, preselectedFolder);
        if (preselectedGroup != null) {
            final LocalizationSupportPanelGUI.ModelItem groupItem = new LocalizationSupportPanelGUI.ModelItem(preselectedGroup);
            final LocalizationSupportPanelGUI.ModelItem[] nodes = groupItem.getChildren();
            pkgComboBox.setModel(new DefaultComboBoxModel(nodes));
            final ModelItem folderItem = getPreselectedPackage(groupItem, preselectedFolder);
            if (folderItem != null)
                pkgComboBox.setSelectedItem(folderItem);
        } else {
            pkgComboBox.setModel(new DefaultComboBoxModel());
        }
        
        
        // default name for classs
        clsTextField.setText( template.getName() );
        
        // add listeners
        addThisListeners();
        
        
        // clear created files textboxes
        updateCreatedClassName();
        updateCreatedMessageFileName();
        
    }
    
    private void addThisListeners() {
        clsTextField.getDocument().addDocumentListener(this);
        rnTextField.getDocument().addDocumentListener(this);
    }
    
    public void actionPerformed(final ActionEvent e) {
        if ( pkgComboBox == e.getSource() ) {
            updateCreatedClassName();
            updateCreatedMessageFileName();
            fireChange();
        }
    }
    
    public void itemStateChanged(@SuppressWarnings("unused")
	final ItemEvent e) {
    }
    
    public void changedUpdate(final DocumentEvent e) {
        if (e.getDocument() == clsTextField.getDocument()) {
            updateCreatedClassName();
            fireChange();
        } else if (e.getDocument() == rnTextField.getDocument()) {
            updateCreatedMessageFileName();
            fireChange();
        }  else if ((pkgComboBoxDocument != null)  &&  (e.getDocument() == pkgComboBoxDocument)) {
            updateCreatedClassName();
            updateCreatedMessageFileName();
            fireChange();
        }
    }
    
    public void insertUpdate(final DocumentEvent e) {
        changedUpdate( e );
    }
    
    public void removeUpdate(final DocumentEvent e) {
        changedUpdate( e );
    }
    
    
    private SourceGroup getPreselectedGroup(final SourceGroup[] groups, final FileObject preselectedFolder) {
        if (preselectedFolder != null) for (int i = 0; i < groups.length; i++) {
            if (groups[i].getRootFolder().equals(preselectedFolder) ||
                    FileUtil.isParentOf(groups[i].getRootFolder(), preselectedFolder)) {
                return groups[i];
            }
        }
        return groups.length >= 0 ? groups[0] : null;
    }
    
    private ModelItem getPreselectedPackage(final ModelItem groupItem, final FileObject preselectedFolder) {
        if (preselectedFolder == null)
            return null;
        final ModelItem ch[] = groupItem.getChildren();
        final FileObject root = groupItem.group.getRootFolder();
        final String relPath = FileUtil.getRelativePath(root, preselectedFolder).replace('/', '.'); //NOI18N
        
        for (int i = 0; i < ch.length; i++)
            if (ch[i].toString().equals(relPath))
                return ch[i];
        return null;
    }
    
    public FileObject getRootFolder() {
        return helper.resolveFileObject(helper.getStandardPropertyEvaluator().getProperty("src.dir")); // NOI18N
    }
    
    public File getFolder() {
        final FileObject root = getRootFolder();
        final File rootFile = FileUtil.toFile(root);
        if (rootFile == null)
            return null;
        return new File(rootFile, getPackageFileName());
    }
    
    
    public String getPackageFileName() {
        String packageName = pkgComboBox.getEditor().getItem().toString();
        if (ModelItem.DEFAULT_PACKAGE_DISPLAY_NAME.equals(packageName))
            packageName = ""; // NOI18N
        return packageName.replace( '.', '/' ); // NOI18N
    }
    
    public Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    
    private void updateCreatedClassName() {
        updateCreatedFileTextField(getTargetClassName(), ".java", cClassTextField);     //NOI18N
    }
    
    private void updateCreatedMessageFileName() {
        updateCreatedFileTextField(getTargetMessageFileName(), ".properties", cResourceTextField); // NOI18N
    }
    
    private void updateCreatedFileTextField(final String createdResource, final String extension, final JTextField textField) {
        if ((createdResource != null) && (createdResource.length() > 0)) {
            final File projdirFile = FileUtil.toFile(project.getProjectDirectory());
            if (projdirFile != null) {
                
                final File folder = getFolder();
                if (folder != null) {
                    final File newFile = new File(folder, createdResource + extension);
                    textField.setText(newFile.getAbsolutePath());
                    // we're done
                    return;
                }
            }
        }
        textField.setText(""); // NOI18N
    }
    
    
    
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(final ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener lit: listeners) {
            lit.stateChanged(e);
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

        jPanel1 = new javax.swing.JPanel();
        clsLabel = new javax.swing.JLabel();
        clsTextField = new javax.swing.JTextField();
        rnLabel = new javax.swing.JLabel();
        rnTextField = new javax.swing.JTextField();
        prjLabel = new javax.swing.JLabel();
        prjTextField = new javax.swing.JTextField();
        pkgLabel = new javax.swing.JLabel();
        pkgComboBox = new javax.swing.JComboBox();
        cClassLabel = new javax.swing.JLabel();
        cClassTextField = new javax.swing.JTextField();
        cResourceLabel = new javax.swing.JLabel();
        cResourceTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        dsLabel = new javax.swing.JLabel();
        dsTextField = new javax.swing.JTextField();
        emLabel = new javax.swing.JLabel();
        emTextField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(245, 230));
        setName(org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "TITLE_File")); // NOI18N
        setPreferredSize(new java.awt.Dimension(245, 232));
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        clsLabel.setLabelFor(clsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(clsLabel, org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "LBL_File_MIDPClassName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        jPanel1.add(clsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(clsTextField, gridBagConstraints);

        rnLabel.setLabelFor(rnTextField);
        org.openide.awt.Mnemonics.setLocalizedText(rnLabel, org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "LBL_Resource_FileName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel1.add(rnLabel, gridBagConstraints);

        rnTextField.setText(org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "TXT_DefValue_MessagesFile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(rnTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        add(jPanel1, gridBagConstraints);

        prjLabel.setLabelFor(prjTextField);
        org.openide.awt.Mnemonics.setLocalizedText(prjLabel, org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "LBL_File_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(prjLabel, gridBagConstraints);

        prjTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(prjTextField, gridBagConstraints);

        pkgLabel.setLabelFor(pkgComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(pkgLabel, org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "LBL_File_Package")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(pkgLabel, gridBagConstraints);

        pkgComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 11, 0);
        add(pkgComboBox, gridBagConstraints);

        cClassLabel.setLabelFor(cClassTextField);
        org.openide.awt.Mnemonics.setLocalizedText(cClassLabel, org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "LBL_File_Created_Class")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(cClassLabel, gridBagConstraints);

        cClassTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(cClassTextField, gridBagConstraints);

        cResourceLabel.setLabelFor(cResourceTextField);
        org.openide.awt.Mnemonics.setLocalizedText(cResourceLabel, org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "LBL_File_Created_Resource")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(cResourceLabel, gridBagConstraints);

        cResourceTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(cResourceTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(targetSeparator, gridBagConstraints);

        dsLabel.setLabelFor(dsTextField);
        org.openide.awt.Mnemonics.setLocalizedText(dsLabel, org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "LBL_Resource_DefaultString")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(dsLabel, gridBagConstraints);

        dsTextField.setText(org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "TXT_DefValue_DefaultString")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(dsTextField, gridBagConstraints);

        emLabel.setLabelFor(emTextField);
        org.openide.awt.Mnemonics.setLocalizedText(emLabel, org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "LBL_Resource_ErrorMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(emLabel, gridBagConstraints);

        emTextField.setText(org.openide.util.NbBundle.getMessage(LocalizationSupportPanelGUI.class, "TXT_DefValue_ErrorMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(emTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel4, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(LocalizationSupportPanelGUI.class, "ACSN_I18N"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(LocalizationSupportPanelGUI.class, "ACSD_I18N"));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cClassLabel;
    private javax.swing.JTextField cClassTextField;
    private javax.swing.JLabel cResourceLabel;
    private javax.swing.JTextField cResourceTextField;
    private javax.swing.JLabel clsLabel;
    private javax.swing.JTextField clsTextField;
    private javax.swing.JLabel dsLabel;
    private javax.swing.JTextField dsTextField;
    private javax.swing.JLabel emLabel;
    private javax.swing.JTextField emTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JComboBox pkgComboBox;
    private javax.swing.JLabel pkgLabel;
    private javax.swing.JLabel prjLabel;
    private javax.swing.JTextField prjTextField;
    private javax.swing.JLabel rnLabel;
    private javax.swing.JTextField rnTextField;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables
    
    
    
    // Private innerclasses ----------------------------------------------------
    
    private static class ModelItem {
        
        public static final String DEFAULT_PACKAGE_DISPLAY_NAME =
                NbBundle.getMessage(LocalizationSupportPanel.class, "LBL_DefaultPackage"); // NOI18N
        
        final private Icon icon;
        
        private Node node;
        protected SourceGroup group;
        private ModelItem[] children;
        
        // For source groups
        public ModelItem(SourceGroup group) {
            this.group = group;
            this.icon = group.getIcon(false);
        }
        
        // For packages
        public ModelItem(Node node) {
            this.node = node;
            this.icon = new ImageIcon(node.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16));
        }
        
        public String getDisplayName() {
            if (group != null) {
                return group.getDisplayName();
            } 
            final String nodeName = node.getName();
            return nodeName.length() == 0 ? DEFAULT_PACKAGE_DISPLAY_NAME : nodeName;
        }
        
        public Icon getIcon() {
            return icon;
        }
        
        public String toString() {
            if (group != null) {
                return getDisplayName();
            } 
            return node.getName();
        }
        
        public ModelItem[] getChildren() {
            if (group == null) {
                return null;
            } 
           	if (children == null) {
                final Node n = PackageView.createPackageView(group);
                if (n == null)
                    return null;
                final Children ch = n.getChildren();
                if (ch == null)
                    return null;
                final Node nodes[] = ch.getNodes(true);
                children = new ModelItem[nodes.length];
                for (int i = 0; i < nodes.length; i++) {
                    children[i] = new ModelItem(nodes[i]);
                }
            }
            return children;
        }
    }
    
    private static class NodeCellRenderer extends JLabel implements ListCellRenderer {
        
        public NodeCellRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(final JList list, final Object value, @SuppressWarnings("unused")
		final int index, final boolean isSelected, @SuppressWarnings("unused")
		final boolean cellHasFocus) {
            if (value instanceof ModelItem) {
                final ModelItem item = (ModelItem) value;
                setText(item.getDisplayName());
                setIcon(item.getIcon());
            } else {
                setText(value.toString());
                setIcon(null);
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                
            }
            return this;
        }
        
    }
}


