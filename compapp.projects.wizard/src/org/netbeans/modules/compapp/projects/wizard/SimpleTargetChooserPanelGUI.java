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

package org.netbeans.modules.compapp.projects.wizard;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
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
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 *
 * @author  Tientien Li
 */
public class SimpleTargetChooserPanelGUI extends javax.swing.JPanel implements ActionListener, DocumentListener {

    /** prefered dimmension of the panels */
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension (500, 340);

    private static final String NEW_FILE_PREFIX =
        NbBundle.getMessage( SimpleTargetChooserPanelGUI.class, "LBL_SimpleTargetChooserPanelGUI_NewFilePrefix" ); // NOI18N

    private final ListCellRenderer CELL_RENDERER = new GroupCellRenderer();

    private Project project;
    private String expectedExtension;
    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private SourceGroup[] folders;
    private boolean isFolder;

    private FileObject mFolderRoot = null;
    private String mFolderText = "";

    /** Creates new form SimpleTargetChooserGUI */
    public SimpleTargetChooserPanelGUI( Project project, SourceGroup[] folders, Component bottomPanel, boolean isFolder ) {
        this.project = project;
        this.folders=folders;
        this.isFolder = isFolder;
        initComponents();

        locationComboBox.setRenderer( CELL_RENDERER );

        if ( bottomPanel != null ) {
            bottomPanelContainer.add( bottomPanel, java.awt.BorderLayout.CENTER );
        }
        initValues( null, null );

        browseButton.addActionListener( this );
        locationComboBox.addActionListener( this );
        documentNameTextField.getDocument().addDocumentListener( this );
        folderTextField.getDocument().addDocumentListener( this );

        setName (NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_SimpleTargetChooserPanel_Name")); // NOI18N
    }

    public void initValues( FileObject template, FileObject preselectedFolder ) {
        assert project != null;

        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

        Sources sources = ProjectUtils.getSources( project );

        folders = sources.getSourceGroups(NewFileIterator.SOURCE_TYPE_BIZPRO /* Sources.TYPE_GENERIC */ );
        if ((folders == null) || (folders.length < 1)) {
            folders = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }

        if ( folders.length < 2 ) {
            // one source group i.e. hide Location
            locationLabel.setVisible( false );
            locationComboBox.setVisible( false );
        }
        else {
            // more source groups user needs to select location
            locationLabel.setVisible( true );
            locationComboBox.setVisible( true );

        }

        locationComboBox.setModel( new DefaultComboBoxModel( folders ) );
        // Guess the group we want to create the file in
        SourceGroup preselectedGroup = getPreselectedGroup( folders, preselectedFolder );
        locationComboBox.setSelectedItem( preselectedGroup );
        // Create OS dependent relative name
        folderTextField.setText( getRelativeNativeName( preselectedGroup.getRootFolder(), preselectedFolder ) );
        /*
        mFolderRoot = preselectedFolder;
        mFolderText = ( getRelativeNativeName( preselectedGroup.getRootFolder(), preselectedFolder ) );
        folderTextField.setText("");
        */
        String ext = template == null ? "" : template.getExt(); // NOI18N
        expectedExtension = ext.length() == 0 ? "" : "." + ext; // NOI18N

        String displayName = null;
        try {
            if (template != null) {
                DataObject templateDo = DataObject.find (template);
                displayName = templateDo.getNodeDelegate ().getDisplayName ();
            }
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName ();
        }
        putClientProperty ("NewFileWizard_Title", displayName);// NOI18N

        if (template != null) {
            documentNameTextField.setText (NEW_FILE_PREFIX + template.getName ());
            documentNameTextField.selectAll ();
        }

        if (isFolder) {
            jLabel3.setText (NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_FolderName_Label")); // NOI18N
            jLabel3.setDisplayedMnemonic (NbBundle.getMessage (SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_FolderName_Label").charAt (0)); // NOI18N
            jLabel2.setText (NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_ParentFolder_Label")); // NOI18N
            jLabel2.setDisplayedMnemonic (NbBundle.getMessage (SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_ParentFolder_Label").charAt (0)); // NOI18N
            jLabel4.setText (NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_CreatedFolder_Label")); // NOI18N
            jLabel4.setDisplayedMnemonic (NbBundle.getMessage (SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_CreatedFolder_Label").charAt (0)); // NOI18N
        } else {
            jLabel3.setText (NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_FileName_Label")); // NOI18N
            jLabel2.setText (NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_Folder_Label")); // NOI18N
            jLabel4.setText (NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_CreatedFile_Label")); // NOI18N
            jLabel3.setDisplayedMnemonic (NbBundle.getMessage (SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_FileName_Label").charAt (0)); // NOI18N
            jLabel2.setDisplayedMnemonic (NbBundle.getMessage (SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_Folder_Label").charAt (0)); // NOI18N
            jLabel4.setDisplayedMnemonic (NbBundle.getMessage (SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_CreatedFile_Label").charAt (0)); // NOI18N
        }
    }

    public SourceGroup getTargetGroup() {
        return (SourceGroup)locationComboBox.getSelectedItem();
    }

    public String getTargetFolder() {
        /*
        String fName = folderTextField.getText().trim();

        String folderName = mFolderText +
                    ( fName.startsWith("/") || fName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
                    fName; // folderTextField.getText().trim();
        */
        String folderName = folderTextField.getText().trim();

        if ( folderName.length() == 0 ) {
            return null;
        }
        else {
            return folderName.replace( File.separatorChar, '/' ); // NOI18N
        }
    }

    public String getTargetName() {

        String text = documentNameTextField.getText().trim();

        if ( text.length() == 0 ) {
            return null;
        }
        else {
            return text;
        }
    }


    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List templist;
        synchronized (this) {
            templist = new ArrayList (listeners);
        }
        Iterator it = templist.iterator();
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
        jLabel3 = new javax.swing.JLabel();
        documentNameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        folderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        targetSeparator = new javax.swing.JSeparator();
        bottomPanelContainer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_SimpleTargetChooserPanelGUI"));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel3.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_FileName_Label").charAt(0));
        jLabel3.setLabelFor(documentNameTextField);
        jLabel3.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_FileName_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(documentNameTextField, gridBagConstraints);
        documentNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_documentNameTextField"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 24, 0);
        add(jPanel1, gridBagConstraints);

        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_Project_Label").charAt(0));
        jLabel1.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_Project_Label"));
        jLabel1.setLabelFor(projectTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(jLabel1, gridBagConstraints);

        projectTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        add(projectTextField, gridBagConstraints);
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_projectTextField"));

        locationLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_Location_Label").charAt(0));
        locationLabel.setLabelFor(locationComboBox);
        locationLabel.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_Location_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(locationLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(locationComboBox, gridBagConstraints);
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_locationComboBox"));

        jLabel2.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_ParentFolder_Label").charAt(0));
        jLabel2.setLabelFor(folderTextField);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_Folder_Label"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(folderTextField, gridBagConstraints);
        folderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_folderTextField"));

        browseButton.setMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_Browse_Button").charAt(0));
        browseButton.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_Browse_Button"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_browseButton"));

        jLabel4.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "MNE_TargetChooser_CreatedFile_Label").charAt(0));
        jLabel4.setText(org.openide.util.NbBundle.getMessage(SimpleTargetChooserPanelGUI.class, "LBL_TargetChooser_CreatedFile_Label"));
        jLabel4.setLabelFor(fileTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabel4, gridBagConstraints);

        fileTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(fileTextField, gridBagConstraints);
        fileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SimpleTargetChooserPanelGUI.class).getString("AD_fileTextField"));

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
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField documentNameTextField;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JTextField folderTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JSeparator targetSeparator;
    // End of variables declaration//GEN-END:variables

    private SourceGroup getPreselectedGroup( SourceGroup[] groups, FileObject folder ) {
        for( int i = 0; folder != null && i < groups.length; i++ ) {
            if( FileUtil.isParentOf( groups[i].getRootFolder(), folder ) ) {
                return groups[i];
            }
        }
        return groups[0];
    }

    private String getRelativeNativeName( FileObject root, FileObject folder ) {
        if (root == null) {
            throw new NullPointerException("null root passed to getRelativeNativeName"); // NOI18N
        }

        String path;

        if (folder == null) {
            path = ""; // NOI18N
        }
        else {
            path = FileUtil.getRelativePath( root, folder );
        }

        return path == null ? "" : path.replace( '/', File.separatorChar ); // NOI18N
    }

    private void updateCreatedFolder() {

        FileObject root = ((SourceGroup)locationComboBox.getSelectedItem()).getRootFolder();

        String folderName = folderTextField.getText().trim();
        String documentName = documentNameTextField.getText().trim();

        String createdFileName = FileUtil.getFileDisplayName( root ) +
            /*
            ( mFolderText.startsWith("/") || mFolderText.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
            mFolderText +
            */
            ( folderName.startsWith("/") || folderName.startsWith( File.separator ) ? "" : "/" ) + // NOI18N
            folderName +
            ( folderName.endsWith("/") || folderName.endsWith( File.separator ) || folderName.length() == 0 ? "" : "/" ) + // NOI18N
            documentName + expectedExtension;

        fileTextField.setText( createdFileName.replace( '/', File.separatorChar ) ); // NOI18N

        fireChange();
    }


    // ActionListener implementation -------------------------------------------

    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ( browseButton == e.getSource() ) {
            FileObject fo=null;
            // Show the browse dialog

            SourceGroup group = (SourceGroup)locationComboBox.getSelectedItem();

            fo = BrowseFolders.showDialog( new SourceGroup[] { group },
                                           project,
                                           folderTextField.getText().replace( File.separatorChar, '/' ) ); // NOI18N

            if ( fo != null && fo.isFolder() ) {
                String relPath = FileUtil.getRelativePath( group.getRootFolder(), fo );
                folderTextField.setText( relPath.replace( '/', File.separatorChar ) ); // NOI18N
            }
        }
        else if ( locationComboBox == e.getSource() )  {
            updateCreatedFolder();
        }
    }

    // DocumentListener implementation -----------------------------------------

    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFolder();
    }

    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFolder();
    }

    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFolder();
    }


    // Rendering of the location combo box -------------------------------------

    private class GroupCellRenderer extends JLabel implements ListCellRenderer {

        public GroupCellRenderer() {
            setOpaque( true );
        }

        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
            if (value instanceof SourceGroup) {
                SourceGroup group = (SourceGroup)value;
                String projectDisplayName = ProjectUtils.getInformation( project ).getDisplayName();
                String groupDisplayName = group.getDisplayName();
                if ( projectDisplayName.equals( groupDisplayName ) ) {
                    setText( groupDisplayName );
                }
                else {
                    /*
                    setText( MessageFormat.format( org.netbeans.modules.project.ui.PhysicalView.GroupNode.GROUP_NAME_PATTERN,
                        new Object[] { groupDisplayName, projectDisplayName, group.getRootFolder().getName() } ) );
                    */
                    setText( MessageFormat.format(
                        NbBundle.getMessage( SimpleTargetChooserPanelGUI.class, "FMT_TargetChooser_GroupProjectNameBadge" ), // NOI18N
                        new Object[] { groupDisplayName, projectDisplayName } ) );
                }

                setIcon( group.getIcon( false ) );
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
