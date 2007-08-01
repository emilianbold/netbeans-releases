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
 * ClientOptionsPanelGUI.java
 *
 * Created on August 8, 2005, 4:55 PM
 */
package org.netbeans.modules.mobility.end2end.ui.wizard;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Skvor
 */
public class ClientOptionsPanelGUI extends javax.swing.JPanel implements DocumentListener, ActionListener {
    
    private static Project clientProject;
    private String expectedExtension;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    private static final ListCellRenderer CELL_RENDERER = new NodeCellRenderer();
    
    private SourceGroup preselectedGroup;
    
    /** Creates new form ClientOptionsPanelGUI */
    public ClientOptionsPanelGUI() {
        initComponents();
        
        packageComboBox.setRenderer( CELL_RENDERER );
        
        initAccessibility();
    }
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName( NbBundle.getMessage( ClientOptionsPanelGUI.class, "ACSN_Client_Options_Panel" ));
        getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientOptionsPanelGUI.class, "ACSD_Client_Options_Panel" ));
        
        clientNameTextField.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientOptionsPanelGUI.class, "ACSD_Client_Name" ));
        projectTextField.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientOptionsPanelGUI.class, "ACSD_Client_Project" ));
        packageComboBox.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientOptionsPanelGUI.class, "ACSD_Client_Package" ));
        createdFileTextField.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientOptionsPanelGUI.class, "ACSD_Client_Created_File" ));
        
        generateStubsCheckBox.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientOptionsPanelGUI.class, "ACSD_Generate_Stubs" ));
        floatingPointCheckBox.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientOptionsPanelGUI.class, "ACSD_Floating_point" ));
        
        createDataBindingCheckBox.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( ClientOptionsPanelGUI.class, "ACSD_Sample_MIDlet" ));
    }
    
    public void setValues( final Project project, final String clientName, final FileObject preselectedFolder ) {
        
        clientProject = project;
        
        clientNameTextField.setText( clientName );
        
        // Determine the extension
        final String ext = "java"; // NOI18N
        expectedExtension = ext.length() == 0 ? "" : "." + ext; // NOI18N
        
        final Sources sources = ProjectUtils.getSources( project );
        final SourceGroup[] groups = sources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );
        if (preselectedGroup == null){
            preselectedGroup = getPreselectedGroup( groups, preselectedFolder );
            if( preselectedGroup != null ) {
                final ModelItem groupItem = new ModelItem( preselectedGroup );
                final ModelItem[] nodes = groupItem.getChildren();
                packageComboBox.setModel( new DefaultComboBoxModel( nodes ));
                final ModelItem folderItem = getPreselectedPackage( groupItem, preselectedFolder );
                if( folderItem != null )
                    packageComboBox.setSelectedItem( folderItem );
            } else {
                packageComboBox.setModel( new DefaultComboBoxModel());
            }
        }
        projectTextField.setText( ProjectUtils.getInformation( project ).getDisplayName());
        
        final Component packageEditor = packageComboBox.getEditor().getEditorComponent();
        ((javax.swing.JTextField)packageEditor).getDocument().addDocumentListener( this );
        
        clientNameTextField.getDocument().addDocumentListener( this );
                
        updateText();
    }
    
    public void insertUpdate( final DocumentEvent e ) {
        changedUpdate( e );
    }
    
    public void removeUpdate( final DocumentEvent e ) {
        changedUpdate( e );
    }
    
    public void changedUpdate( @SuppressWarnings("unused")
	final DocumentEvent e ) {
        //System.err.println("- changed");
        updateText();
        fireChange();
    }
    
    public void actionPerformed(final java.awt.event.ActionEvent e) {
        if( packageComboBox == e.getSource()) {
            updateText();
        }
    }
    
    public String getProjectName() {
        return projectTextField.getText();
    }
    
    public String getTargetName() {
        return normalizedString( clientNameTextField.getText());
    }
    
    public boolean isGenerateStubs() {
        return generateStubsCheckBox.isSelected();
    }
    
    public boolean isFloatingPointUsed() {
        return floatingPointCheckBox.isSelected();
    }
    
    public boolean isDataBinded() {
        return createDataBindingCheckBox.isSelected();
    }
        
    public String getCreatedFile() {
        return createdFileTextField.getText();
    }
    
    final FileObject getRootFolder() {
        return preselectedGroup.getRootFolder();
    }
    
    final SourceGroup getSourceGroup(){
        return preselectedGroup;
    }
    
    private void updateText() {
        final File projdirFile = FileUtil.toFile( clientProject.getProjectDirectory());
        if( projdirFile != null ) {
            final String documentName = clientNameTextField.getText().trim();
            if( documentName.length() == 0 ) {
                createdFileTextField.setText( "" ); // NOI18N
            } else {
                final File folder = getFolder();
                if( folder != null ) {
                    final File newFile = new File( folder, documentName + expectedExtension );
                    createdFileTextField.setText( newFile.getAbsolutePath());
                } else {
                    createdFileTextField.setText( "" ); // NOI18N
                }
            }
        } else {
            // Not on disk.
            createdFileTextField.setText( "" ); // NOI18N
        }
    }
    
    public void addChangeListener( final ChangeListener l ) {
        listeners.add( l );
    }
    
    public void removeChangeListener( final ChangeListener l ) {
        listeners.remove( l );
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent( this );
        for ( ChangeListener cl : listeners ) {
            cl.stateChanged( e );
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

        clientNameLabel = new javax.swing.JLabel();
        clientNameTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        packageLabel = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        createdFileLabel = new javax.swing.JLabel();
        createdFileTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        generateStubsCheckBox = new javax.swing.JCheckBox();
        floatingPointCheckBox = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        createDataBindingCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(560, 350));
        setLayout(new java.awt.GridBagLayout());

        clientNameLabel.setLabelFor(clientNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(clientNameLabel, org.openide.util.NbBundle.getMessage(ClientOptionsPanelGUI.class, "LBL_ClientName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(clientNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 65;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(clientNameTextField, gridBagConstraints);

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(ClientOptionsPanelGUI.class, "LBL_Client_Project")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(projectLabel, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 65;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(projectTextField, gridBagConstraints);

        packageLabel.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(ClientOptionsPanelGUI.class, "LBL_Client_Package")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(packageLabel, gridBagConstraints);

        packageComboBox.setEditable(true);
        packageComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(packageComboBox, gridBagConstraints);

        createdFileLabel.setLabelFor(createdFileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFileLabel, org.openide.util.NbBundle.getMessage(ClientOptionsPanelGUI.class, "LBL_Client_Created_File")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(createdFileLabel, gridBagConstraints);

        createdFileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 65;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(createdFileTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jSeparator1, gridBagConstraints);

        generateStubsCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(generateStubsCheckBox, org.openide.util.NbBundle.getBundle(ClientOptionsPanelGUI.class).getString("LBL_Generate_Stubs")); // NOI18N
        generateStubsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        generateStubsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        generateStubsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateStubsCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(generateStubsCheckBox, gridBagConstraints);

        floatingPointCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(floatingPointCheckBox, org.openide.util.NbBundle.getMessage(ClientOptionsPanelGUI.class, "LBL_Floating_Point")); // NOI18N
        floatingPointCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        floatingPointCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(floatingPointCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jSeparator2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(createDataBindingCheckBox, org.openide.util.NbBundle.getMessage(ClientOptionsPanelGUI.class, "LBL_Create_Sample_MIDlet")); // NOI18N
        createDataBindingCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        createDataBindingCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(createDataBindingCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void generateStubsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateStubsCheckBoxActionPerformed
        fireChange();
    }//GEN-LAST:event_generateStubsCheckBoxActionPerformed
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel clientNameLabel;
    private javax.swing.JTextField clientNameTextField;
    private javax.swing.JCheckBox createDataBindingCheckBox;
    private javax.swing.JLabel createdFileLabel;
    private javax.swing.JTextField createdFileTextField;
    private javax.swing.JCheckBox floatingPointCheckBox;
    private javax.swing.JCheckBox generateStubsCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    // End of variables declaration//GEN-END:variables
    
    private SourceGroup getPreselectedGroup( final SourceGroup[] groups, final FileObject preselectedFolder ) {
        if( preselectedFolder != null ) for( int i = 0; i < groups.length; i++ ) {
            if( groups[i].getRootFolder().equals( preselectedFolder ) ||
                    FileUtil.isParentOf( groups[i].getRootFolder(), preselectedFolder )) {
                return groups[i];
            }
        }
        return groups.length >= 0 ? groups[0] : null;
    }
    
    private ModelItem getPreselectedPackage( final ModelItem groupItem, final FileObject preselectedFolder ) {
        
        if( preselectedFolder == null )
            return null;
        
        final ModelItem ch[] = groupItem.getChildren();
        final FileObject root = groupItem.group.getRootFolder();
        String relPath = FileUtil.getRelativePath( root, preselectedFolder );
        relPath = relPath == null ? "" : relPath.replace( '/', '.' ); //NOI18N
        
        for( int i = 0; i < ch.length; i++ )
            if( ch[i].toString().equals( relPath ))
                return ch[i];
        return null;
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
        return packageName.replace( '.', '/' ); // NOI18N;
    }
    
    private static String normalizedString( String text ) {
        if (text == null)
            return null;
        text = text.trim();
        if (text.length() <= 0)
            return null;
        return text;
    }
    
    /**
     *
     */
    private static class ModelItem {
        
        public static final String DEFAULT_PACKAGE_DISPLAY_NAME =
                NbBundle.getMessage( ClientOptionsPanelGUI.class, "LBL_MIDPTargetChooserPanelGUI_DefaultPackage"); // NOI18N
        
        private Node node;
        protected SourceGroup group;
        final private Icon icon;
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
    
    /**
     *
     */
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
