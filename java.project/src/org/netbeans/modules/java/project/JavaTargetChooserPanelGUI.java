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

package org.netbeans.modules.java.project;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author  phrebejk
 */
public class JavaTargetChooserPanelGUI extends javax.swing.JPanel implements ActionListener, DocumentListener {
  
    private static final String DEFAULT_NEW_PACKAGE_NAME = 
        NbBundle.getMessage( JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_DefaultNewPackageName" ); // NOI18N
    
    /** prefered dimmension of the panel */
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension (560, 350);
    
    private static final ListCellRenderer CELL_RENDERER = new NodeCellRenderer();
    
    private Project project;
    private ModelItem[] groupItems;
    private String expectedExtension;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private boolean isPackage;
    private SourceGroup folders[];
    
    /** Creates new form SimpleTargetChooserGUI */
    public JavaTargetChooserPanelGUI( Project p, SourceGroup[] groups, Component bottomPanel, boolean isPackage ) {
        this.isPackage = isPackage;
        this.project = p;
        this.folders = groups;
        
        initComponents();        
        if ( isPackage ) {
            packageComboBox.setVisible( false );
            packageLabel.setVisible( false );
            documentNameLabel.setText( NbBundle.getMessage( JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_PackageName_Label" ) ); // NOI18N
            fileLabel.setText( NbBundle.getMessage( JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_CreatedFolder_Label" ) ); // NOI18N
        }        
        else {
            packageComboBox.getEditor().addActionListener( this );
        }
        
                
        if ( bottomPanel != null ) {
            bottomPanelContainer.add( bottomPanel, java.awt.BorderLayout.CENTER );
        }
                
        //initValues( project, null, null );
        documentNameTextField.getDocument().addDocumentListener( this );

        // Not very nice
        Component packageEditor = packageComboBox.getEditor().getEditorComponent();
        if ( packageEditor instanceof javax.swing.JTextField ) {
            ((javax.swing.JTextField)packageEditor).getDocument().addDocumentListener( this );
        }
        else {
            packageComboBox.addActionListener( this );
        }
        
        rootComboBox.setRenderer( CELL_RENDERER );
        packageComboBox.setRenderer( CELL_RENDERER );                
        rootComboBox.addActionListener( this );
        
        setName( NbBundle.getBundle (JavaTargetChooserPanelGUI.class).getString ("LBL_JavaTargetChooserPanelGUI_Name") ); // NOI18N
    }
    
    public void initValues( FileObject template, FileObject preselectedFolder ) {
        assert project != null : "Project must be specified."; // NOI18N
        
        // Create list of groups
        this.groupItems = new ModelItem[ folders.length ]; 
        for( int i = 0; i < folders.length; i++ ) {
            this.groupItems[i] = new ModelItem( folders[i] ); 
        }
                
        // Show name of the project
        projectTextField.setText( ProjectUtils.getInformation(project).getDisplayName() );
        assert template != null;
        
        String displayName = null;
        try {
            DataObject templateDo = DataObject.find (template);
            displayName = templateDo.getNodeDelegate ().getDisplayName ();
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName ();
        }
        
        putClientProperty ("NewFileWizard_Title", displayName);// NOI18N        
        // Setup comboboxes 
        rootComboBox.setModel( new DefaultComboBoxModel( this.groupItems ) );
        ModelItem preselectedGroup = getPreselectedGroup( preselectedFolder );
        rootComboBox.setSelectedItem( preselectedGroup );
        updatePackages();                
        ModelItem preselectedPackage = getPreselectedPackage( preselectedGroup, preselectedFolder );
        if ( preselectedPackage != null ) {            
            if ( isPackage ) {
                
                String docName = preselectedPackage.toString().length() == 0 ? 
                    DEFAULT_NEW_PACKAGE_NAME : 
                    preselectedPackage.toString() + "." + DEFAULT_NEW_PACKAGE_NAME;
                
                documentNameTextField.setText( docName );                    
                int docNameLen = docName.length();
                int defPackageNameLen = DEFAULT_NEW_PACKAGE_NAME.length();
                
                documentNameTextField.setSelectionEnd( docNameLen - 1 );
                documentNameTextField.setSelectionStart( docNameLen - defPackageNameLen );                
            }
            else {
                packageComboBox.setSelectedItem( preselectedPackage );
            }
        }
        updateText();
        
        // Determine the extension
        String ext = template == null ? "" : template.getExt(); // NOI18N
        expectedExtension = ext.length() == 0 ? "" : "." + ext; // NOI18N
    }
        
    public FileObject getRootFolder() {
        return ((ModelItem)rootComboBox.getSelectedItem()).group.getRootFolder();        
    }
    
    public String getPackageFileName() {
        
        if ( isPackage ) {
            return ""; // NOI18N
        }
        
        String packageName = packageComboBox.getEditor().getItem().toString();        
        return  packageName.replace( '.', '/' ); // NOI18N        
    }
    
    public String getPackageName() {
        
        if ( isPackage ) {
            return ""; // NOI18N
        }
        
        return packageComboBox.getEditor().getItem().toString();
    }
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    
    /*
    public String getTargetFolder() {
        File fo = getFolder();
        
        if ( fo == null ) {
            return null;
        }
        else {
            return fo.getPath();
        }
    }
    */
    
    public String getTargetName() {
        String text = documentNameTextField.getText().trim();
        
        if ( text.length() == 0 ) {
            return null;
        }
        else {
            return text;
        }

    }
    
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        documentNameLabel = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        rootComboBox = new javax.swing.JComboBox();
        packageLabel = new javax.swing.JLabel();
        packageComboBox = new javax.swing.JComboBox();
        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        documentNameLabel.setText(org.openide.util.NbBundle.getMessage(JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(documentNameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(documentNameTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        add(jPanel1, gridBagConstraints);

        jLabel5.setText(org.openide.util.NbBundle.getMessage(JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_jLabel5"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel5, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectTextField, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_jLabel1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(rootComboBox, gridBagConstraints);

        packageLabel.setText(org.openide.util.NbBundle.getMessage(JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_jLabel2"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(packageLabel, gridBagConstraints);

        packageComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(packageComboBox, gridBagConstraints);

        fileLabel.setText(org.openide.util.NbBundle.getMessage(JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_CreatedFile_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 12, 0);
        add(fileLabel, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 0);
        add(fileTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(targetSeparator, gridBagConstraints);

        bottomPanelContainer.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(bottomPanelContainer, gridBagConstraints);

    }//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanelContainer;
    private javax.swing.JLabel documentNameLabel;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JComboBox rootComboBox;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
        
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( rootComboBox == e.getSource() ) {            
            updatePackages();
            updateText();
        }
        else if ( packageComboBox == e.getSource() ) {
            updateText();
            fireChange();
        }
        else if ( packageComboBox.getEditor()  == e.getSource() ) {
            updateText();
            fireChange();
        }
    }    
    
    // DocumentListener implementation -----------------------------------------
    
    public void changedUpdate(javax.swing.event.DocumentEvent e) {                
        updateText();
        fireChange();        
    }    
    
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        changedUpdate( e );
    }
    
    // Private methods ---------------------------------------------------------
        
    private void updatePackages() {
        packageComboBox.setModel( new DefaultComboBoxModel( ((ModelItem)rootComboBox.getSelectedItem()).getChildren() ) );        
    }
    
    private File getFolder() {
        FileObject rootFo = getRootFolder();
        File rootFile = FileUtil.toFile( rootFo );
        if ( rootFile == null ) {
            return null;
        }        
        String packageFileName = getPackageFileName();        
        File folder = new File( rootFile, packageFileName );
        return folder;
    }
    
    private void updateText() {
        
        ModelItem modelItem = (ModelItem)rootComboBox.getSelectedItem();
        FileObject rootFolder = modelItem.group.getRootFolder();
        String packageName = getPackageFileName();
        String documentName = documentNameTextField.getText().trim();
        if ( isPackage ) {
            documentName = documentName.replace( '.', '/' ); // NOI18N
        }
        else if ( documentName.length() > 0 ) {
            documentName = documentName + expectedExtension;
        }
        String createdFileName = rootFolder.getPath() + 
            ( packageName.startsWith("/") || packageName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
            packageName + 
            ( packageName.endsWith("/") || packageName.endsWith( File.separator ) || packageName.length() == 0 ? "" : "/" ) + // NOI18N
            documentName;
        
        fileTextField.setText( createdFileName.replace( '/', File.separatorChar ) ); // NOI18N        
    }
    
    private ModelItem getPreselectedGroup( FileObject folder ) {        
        for( int i = 0; folder != null && i < groupItems.length; i++ ) {
            if ( groupItems[i].group.getRootFolder().equals( folder ) || 
                FileUtil.isParentOf( groupItems[i].group.getRootFolder(), folder ) ) {
                return groupItems[i];
            }
        }
        return groupItems[0];
    }
    
    private ModelItem getPreselectedPackage( ModelItem groupItem, FileObject folder ) {
        if ( folder == null ) {
            return null;
        }
        ModelItem ch[] = groupItem.getChildren();
        FileObject root = groupItem.group.getRootFolder();
        
        String relPath = FileUtil.getRelativePath( root, folder );
        
        if ( relPath == null ) {
            // Group Root folder is no a parent of the preselected folder
            // No package should be selected
            return null; 
        }        
        else {
            relPath = relPath.replace( '/', '.' ); //NOI18N
        }        
            
        for( int i = 0; i < ch.length; i++ ) {
            if ( ch[i].toString().equals( relPath ) ) {
                return ch[i];
            }
        }
        
        return null;
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private static class ModelItem {
        
        private static final String DEFAULT_PACKAGE_DISPLAY_NAME =
            NbBundle.getMessage( JavaTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_DefaultPackage" ); // NOI18N
        
        private Node node;        
        private SourceGroup group;
        private Icon icon;
        private ModelItem[] children;
	
        // For source groups
        public ModelItem( SourceGroup group ) {            
            this.group = group;
            this.icon = group.getIcon( false );
        }
        
        // For packages
        public ModelItem( Node node ) {
            this.node = node;
            this.icon = new ImageIcon( node.getIcon( java.beans.BeanInfo.ICON_COLOR_16x16 ) );
        }
        
	public String getDisplayName() {
            if ( group != null ) {
                return group.getDisplayName();
            }
            else {
                String nodeName = node.getName();
                return nodeName.length() == 0 ? DEFAULT_PACKAGE_DISPLAY_NAME : nodeName;
            }
        }
	
        public Icon getIcon() {
            return icon;
        }
                
        public String toString() {
            if ( group != null ) {
                return getDisplayName();
            }
            else {
                return node.getName();
            }
        }        
        
        public ModelItem[] getChildren() {
            if ( group == null ) {
                return null;
            }
            else {
                if ( children == null ) {
                    Children ch = PackageView.createPackageView( group.getRootFolder() );
                    Node nodes[] = ch.getNodes( true );
                    children = new ModelItem[ nodes.length ];
                    for( int i = 0; i < nodes.length; i++ ) {
                        children[i] = new ModelItem( nodes[i] );
                    }
                }
                return children;
            }
        }
        
    }
    
    private static class NodeCellRenderer extends JLabel implements ListCellRenderer {
    
        public NodeCellRenderer() {
            setOpaque( true );
        }
        
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
            if (value instanceof ModelItem) {
                ModelItem item = (ModelItem)value;
                setText( item.getDisplayName() );
                setIcon( item.getIcon() );
            } 
            else {
                setText( value.toString () );
                setIcon( null );
            }
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
             
            }
            return this;        
        }
                
    }
    
}
