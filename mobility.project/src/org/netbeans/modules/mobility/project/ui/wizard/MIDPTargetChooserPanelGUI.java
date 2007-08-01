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

package org.netbeans.modules.mobility.project.ui.wizard;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

// XXX I18N

/**
 *
 * @author  phrebejk, David Kaspar
 */
public class MIDPTargetChooserPanelGUI extends javax.swing.JPanel implements ActionListener, DocumentListener {
    
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(560, 350);
    
    private static final ListCellRenderer CELL_RENDERER = new NodeCellRenderer();
    
    public static final String IS_MIDLET_TEMPLATE_ATTRIBUTE = "isMIDletTemplate"; // NOI18N
    
    private Project project;
    protected AntProjectHelper helper;
    private String expectedExtension;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private boolean updateClassName = true;
    private boolean loadIcons = true;
    private Document packageComboBoxDocument = null;
    
    private boolean isMIDlet;
    
    public MIDPTargetChooserPanelGUI() {
        initComponents();
        initAccessibility();
        lNote.setFont(lNote.getFont().deriveFont(Font.ITALIC));
        
        // Not very nice
        Component packageEditor = packageComboBox.getEditor().getEditorComponent();
        if (packageEditor instanceof javax.swing.JTextField) {
            packageComboBoxDocument = ((javax.swing.JTextField) packageEditor).getDocument();
            packageComboBoxDocument.addDocumentListener(this);
        } else
            packageComboBox.addActionListener(this);
        
        packageComboBox.setRenderer( CELL_RENDERER );
        packageComboBox.addActionListener( this );
    }
    
    private void addThisListeners() {
        tClassName.getDocument().addDocumentListener(this);
    }
    
    private void removeThisListeners() {
        tClassName.getDocument().removeDocumentListener(this);
    }
    
    public void initValues(final Project project, final FileObject template, final FileObject preselectedFolder) {
        this.project = project;
        this.helper = project.getLookup().lookup(AntProjectHelper.class);
        final Object obj = template.getAttribute(IS_MIDLET_TEMPLATE_ATTRIBUTE);
        isMIDlet = false;
        if (obj instanceof Boolean)
            isMIDlet = ((Boolean) obj).booleanValue();
        
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        
        final Sources sources = ProjectUtils.getSources(project);
        final SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        final SourceGroup preselectedGroup = getPreselectedGroup(groups, preselectedFolder);
        if (preselectedGroup != null) {
            final ModelItem groupItem = new ModelItem(preselectedGroup);
            final ModelItem[] nodes = groupItem.getChildren();
            packageComboBox.setModel(new DefaultComboBoxModel(nodes));
            final Object folderItem = getPreselectedPackage(groupItem, preselectedFolder);
            if (folderItem != null)
                packageComboBox.setSelectedItem(folderItem);
        } else {
            packageComboBox.setModel(new DefaultComboBoxModel());
        }
        
        // Determine the extension
        final String ext = template == null ? "" : template.getExt(); // NOI18N
        expectedExtension = ext.length() == 0 ? "" : "." + ext; // NOI18N
        
        lName.setVisible(isMIDlet);
        tName.setVisible(isMIDlet);
        lIcon.setVisible(isMIDlet);
        cIcon.setVisible(isMIDlet);
        lNote.setVisible(isMIDlet);
        lClassName.setText(NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, isMIDlet ? "LBL_File_MIDletClassName" : "LBL_File_MIDPClassName")); // NOI18N
        
        // Show name of the project
        if (isMIDlet) {
            tName.getDocument().removeDocumentListener(this);
            tName.setText( template.getName() );
            updateClassNameAndIcon();
            if (testIfFileNameExists(preselectedGroup)  &&  updateClassName) {
                String name = tName.getText();
                int i = 1;
                for (;;) {
                    tName.setText(name + "_" + i); // NOI18N
                    updateClassNameAndIcon();
                    if (! testIfFileNameExists(preselectedGroup)  ||  ! updateClassName)
                        break;
                    i++;
                }
            }
            tName.getDocument().addDocumentListener(this);
        } else {
            tClassName.setText( template.getName() );
            if (testIfFileNameExists(preselectedGroup)) {
                String name = tClassName.getText();
                int i = 1;
                for (;;) {
                    tClassName.setText(name + "_" + i); // NOI18N
                    if (!testIfFileNameExists(preselectedGroup))
                        break;
                    i++;
                }
            }
            tClassName.getDocument().addDocumentListener(this);
        }
        updateText();
        
        // Find all icons
        if (loadIcons) {
            loadIcons = false;
            final DefaultComboBoxModel icons = new DefaultComboBoxModel();
            cIcon.setModel(icons);
            cIcon.setSelectedItem("");//NOI18N
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    final ArrayList<FileObject> roots = new ArrayList<FileObject>();
                    roots.add(helper.resolveFileObject(helper.getStandardPropertyEvaluator().getProperty("src.dir"))); //NOI18N
                    final String libs = J2MEProjectUtils.evaluateProperty(helper, DefaultPropertiesDescriptor.LIBS_CLASSPATH);
                    if (libs != null) {
                        final String elements[] = PropertyUtils.tokenizePath(helper.resolvePath(libs));
                        for (int i=0; i<elements.length; i++) try {
                            final FileObject root = FileUtil.toFileObject(FileUtil.normalizeFile(new File(elements[i])));
                            if (root != null) roots.add(FileUtil.isArchiveFile(root) ? FileUtil.getArchiveRoot(root): root);
                        } catch (Exception e) {}
                    }
                    for (FileObject root : roots) {
                        if (root != null) {
                            final int rootLength = root.getPath().length();
                            final Enumeration en = root.getChildren(true);
                            while (en.hasMoreElements()) {
                                final FileObject fo = (FileObject)en.nextElement();
                                if (fo.isData()) {
                                    final String ext = fo.getExt().toLowerCase();
                                    if ("png".equals(ext)) { // NOI18N
                                        String name = fo.getPath().substring(rootLength);
                                        if (!name.startsWith("/")) name = "/" + name; //NOI18N
                                        if (icons.getIndexOf(name) < 0) icons.addElement(name);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }
    
    private boolean testIfFileNameExists(final SourceGroup preselectedGroup) {
        FileObject folder = null;
        if (preselectedGroup != null)
            folder = preselectedGroup.getRootFolder();
        if (folder == null)
            return false;
        final String pkg = getPackageFileName();
        if (pkg != null  &&  ! "".equals(pkg)) { // NOI18N
            folder = folder.getFileObject(pkg);
            if (folder == null)
                return false;
        }
        return folder.getFileObject(tClassName.getText() + expectedExtension) != null;
    }
    
    public void updateClassNameAndIcon() {
        if (! isMIDlet)
            return;
        removeThisListeners();
        
        String name = tName.getText();
        final StringBuffer sb = new StringBuffer();
        for (int a = 0; a < name.length(); a ++) {
            final char c = name.charAt(a);
            if (! Character.isJavaIdentifierPart(c))
                continue;
            if (sb.length() <= 0  &&  ! Character.isJavaIdentifierStart(c))
                continue;
            sb.append(c);
        }
        name = sb.toString();
        
        if (updateClassName)
            tClassName.setText(name);
        
        addThisListeners();
    }
    
    public boolean isMIDletTemplate() {
        return isMIDlet;
    }
    
    public String getMIDletName() {
        return normalizedString(tName.getText());
    }
    
    public String getClassName() {
        return normalizedString(tClassName.getText());
    }
    
    public String getMIDletIcon() {
        return normalizedString(cIcon.getEditor().getItem().toString());
    }
    
    public String getTargetName() {
        return normalizedString(tClassName.getText());
    }
    
    public String getCreatedFile() {
        return fileTextField.getText();
    }
    
    private static String normalizedString(String text) {
        if (text == null)
            return null;
        text = text.trim();
        if (text.length() <= 0)
            return null;
        return text;
    }
    
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(final ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( ChangeListener lit : listeners ) {
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
        lName = new javax.swing.JLabel();
        tName = new javax.swing.JTextField();
        lClassName = new javax.swing.JLabel();
        tClassName = new javax.swing.JTextField();
        lIcon = new javax.swing.JLabel();
        cIcon = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        lNote = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, "TITLE_File")); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        lName.setLabelFor(tName);
        org.openide.awt.Mnemonics.setLocalizedText(lName, org.openide.util.NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, "LBL_File_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 4);
        jPanel1.add(lName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 3, 4);
        jPanel1.add(tName, gridBagConstraints);

        lClassName.setLabelFor(tClassName);
        org.openide.awt.Mnemonics.setLocalizedText(lClassName, org.openide.util.NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, "LBL_File_MIDletClassName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 4);
        jPanel1.add(lClassName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 3, 4);
        jPanel1.add(tClassName, gridBagConstraints);

        lIcon.setLabelFor(cIcon);
        org.openide.awt.Mnemonics.setLocalizedText(lIcon, org.openide.util.NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, "LBL_File_Icon")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 4);
        jPanel1.add(lIcon, gridBagConstraints);

        cIcon.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 3, 4);
        jPanel1.add(cIcon, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        add(jPanel1, gridBagConstraints);

        jLabel5.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, "LBL_File_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel5, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectTextField, gridBagConstraints);

        jLabel2.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, "LBL_File_Package")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel2, gridBagConstraints);

        packageComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(packageComboBox, gridBagConstraints);

        jLabel4.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, "LBL_File_Created")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel4, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(fileTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(lNote, NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, "LBL_MIDPTarget_Note")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(lNote, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, "ACSN_File"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MIDPTargetChooserPanelGUI.class, "ACSD_File"));
    }
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JComboBox cIcon;
    public javax.swing.JTextField fileTextField;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel4;
    public javax.swing.JLabel jLabel5;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel3;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JLabel lClassName;
    public javax.swing.JLabel lIcon;
    public javax.swing.JLabel lName;
    public javax.swing.JLabel lNote;
    public javax.swing.JComboBox packageComboBox;
    public javax.swing.JTextField projectTextField;
    public javax.swing.JTextField tClassName;
    public javax.swing.JTextField tName;
    // End of variables declaration//GEN-END:variables
    
    // ActionListener implementation -------------------------------------------
    
    public void actionPerformed(final java.awt.event.ActionEvent e) {
        if ( packageComboBox == e.getSource() ) {
            updateText();
        }
    }
    
    // DocumentListener implementation -----------------------------------------
    
    public void changedUpdate(final javax.swing.event.DocumentEvent e) {
        if (e.getDocument() == tName.getDocument()) {
            updateClassNameAndIcon();
            updateText();
            fireChange();
        } else if (e.getDocument() == tClassName.getDocument()) {
            updateText();
            fireChange();
            updateClassName = false;
        } else if (packageComboBoxDocument != null  &&  e.getDocument() == packageComboBoxDocument) {
            updateText();
            fireChange();
        }
    }
    
    public void insertUpdate(final javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    public void removeUpdate(final javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    // Private methods ---------------------------------------------------------
    
    private SourceGroup getPreselectedGroup(final SourceGroup[] groups, final FileObject preselectedFolder) {
        if (preselectedFolder != null) for (int i = 0; i < groups.length; i++) {
            if (groups[i].getRootFolder().equals(preselectedFolder) ||
                    FileUtil.isParentOf(groups[i].getRootFolder(), preselectedFolder)) {
                return groups[i];
            }
        }
        return groups.length >= 0 ? groups[0] : null;
    }
    
    private Object getPreselectedPackage(final ModelItem groupItem, final FileObject preselectedFolder) {
        if (preselectedFolder == null)
            return null;
        final ModelItem ch[] = groupItem.getChildren();
        final FileObject root = groupItem.group.getRootFolder();
        String relPath = FileUtil.getRelativePath(root, preselectedFolder);
        relPath = relPath == null ? "" : relPath.replace('/', '.'); //NOI18N
        
        for (int i = 0; i < ch.length; i++)
            if (ch[i].toString().equals(relPath))
                return ch[i];
        
        return relPath;
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
        String packageName = packageComboBox.getEditor().getItem().toString();
        if (ModelItem.DEFAULT_PACKAGE_DISPLAY_NAME.equals(packageName))
            packageName = ""; // NOI18N
        return packageName.replace( '.', '/' ); // NOI18N
    }
    
    private void updateText() {
        final File projdirFile = FileUtil.toFile(project.getProjectDirectory());
        if (projdirFile != null) {
            final String documentName = tClassName.getText().trim();
            if (documentName.length() == 0) {
                fileTextField.setText(""); // NOI18N
            } else {
                final File folder = getFolder();
                if (folder != null) {
                    final File newFile = new File(folder, documentName + expectedExtension);
                    fileTextField.setText(newFile.getAbsolutePath());
                } else {
                    fileTextField.setText(""); // NOI18N
                }
            }
        } else {
            // Not on disk.
            fileTextField.setText(""); // NOI18N
        }
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static class ModelItem {
        
        public static final String DEFAULT_PACKAGE_DISPLAY_NAME =
                NbBundle.getMessage(MIDPTargetChooserPanel.class, "LBL_MIDPTargetChooserPanelGUI_DefaultPackage"); // NOI18N
        
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
            if (group == null) return null;
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
