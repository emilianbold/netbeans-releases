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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.ui.FoldersListSettings;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.java.j2seproject.SourceRoots;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

/** Handles adding, removing, reordering of source roots.
 *
 * @author Tomas Zezula
 */
final class J2SESourceRootsUi {
  
    public static DefaultTableModel createModel( SourceRoots roots ) {
        
        String[] rootLabels = roots.getRootNames();
        String[] rootProps = roots.getRootProperties();
        URL[] rootURLs = roots.getRootURLs();
        Object[][] data = new Object[rootURLs.length] [2];
        for (int i=0; i< rootURLs.length; i++) {
            data[i][0] = new File (URI.create (rootURLs[i].toExternalForm()));
            if (rootLabels[i].length()>0) {
                data[i][1] = rootLabels[i];
            }
            else if ( "src.dir".equals(rootProps[i])) {   //NOI18N
                data[i][1] = SourceRoots.DEFAULT_SOURCE_LABEL;
            }
            else if ( "test.src.dir".equals(rootProps[i])) { //NOI18N
                data[i][1] = SourceRoots.DEFAULT_TEST_LABEL;
            }
            else {
                data[i][1] = ""; //NOI18N
            }
        }
        return new SourceRootsModel(data);
                
    }
    
    public static EditMediator registerEditMediator( J2SEProject master,
                                             JTable rootsList,
                                             JButton addFolderButton,
                                             JButton removeButton,
                                             JButton upButton,
                                             JButton downButton,
                                             String errorMessagePOR,
                                             String errorMessageRR ) {
        
        EditMediator em = new EditMediator( master,
                                            rootsList,
                                            addFolderButton,
                                            removeButton,
                                            upButton,
                                            downButton,
                                            errorMessagePOR,
                                            errorMessageRR);
        
        // Register the listeners        
        // On all buttons
        addFolderButton.addActionListener( em ); 
        removeButton.addActionListener( em );
        upButton.addActionListener( em );
        downButton.addActionListener( em );
        // On list selection
        rootsList.getSelectionModel().addListSelectionListener( em );
        DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
        editor.addCellEditorListener (em);
        rootsList.setDefaultRenderer( File.class, new FileRenderer (FileUtil.toFile(master.getProjectDirectory())));
        rootsList.setDefaultEditor(String.class, editor);
        // Set the initial state of the buttons
        em.valueChanged( null );
        
        DefaultTableModel model = (DefaultTableModel)rootsList.getModel();
        String[] columnNames = new String[2];
        columnNames[0]  = NbBundle.getMessage( J2SESourceRootsUi.class,"CTL_PackageFolders");
        columnNames[1]  = NbBundle.getMessage( J2SESourceRootsUi.class,"CTL_PackageLabels");
        model.setColumnIdentifiers(columnNames);
        rootsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        return em;
    }
        
    // Private innerclasses ----------------------------------------------------

    public static class EditMediator implements ActionListener, ListSelectionListener, CellEditorListener {

        
        final JTable rootsList;
        final JButton addFolderButton;
        final JButton removeButton;
        final JButton upButton;
        final JButton downButton;
        private final Project project;
        private final Set ownedFolders;
        private DefaultTableModel rootsModel;
        private EditMediator relatedEditMediator;
        private final String errorMessageRR;
        private final String errorMessagePOR;

        
        public EditMediator( J2SEProject master,
                             JTable rootsList,
                             JButton addFolderButton,
                             JButton removeButton,
                             JButton upButton,
                             JButton downButton,
                             String errorMessagePOR,
                             String errorMessageRR ) {

            if ( !( rootsList.getModel() instanceof DefaultTableModel ) ) {
                throw new IllegalArgumentException( "Jtable's model has to be of class DefaultTableModel" ); // NOI18N
            }
                    
            this.rootsList = rootsList;
            this.addFolderButton = addFolderButton;
            this.removeButton = removeButton;
            this.upButton = upButton;
            this.downButton = downButton;
            this.errorMessagePOR = errorMessagePOR;
            this.errorMessageRR = errorMessageRR;
            this.ownedFolders = new HashSet();

            this.project = master;

            this.ownedFolders.clear();
            this.rootsModel = (DefaultTableModel)rootsList.getModel();
            Vector data = rootsModel.getDataVector();
            for (Iterator it = data.iterator(); it.hasNext();) {
                Vector row = (Vector) it.next ();
                File f = (File) row.elementAt(0);
                this.ownedFolders.add (f);
            }
        }
        
        public void setRelatedEditMediator(EditMediator rem) {
            this.relatedEditMediator = rem;
        }
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */        
        public void actionPerformed( ActionEvent e ) {
            
            Object source = e.getSource();
            
            if ( source == addFolderButton ) { 
                
                // Let user search for the Jar file
                JFileChooser chooser = new JFileChooser();
                FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
                chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
                chooser.setMultiSelectionEnabled( true );
                chooser.setDialogTitle( NbBundle.getMessage( J2SESourceRootsUi.class, "LBL_SourceFolder_DialogTitle" )); // NOI18N
                File curDir = FoldersListSettings.getDefault().getLastUsedSourceRootFolder();
                chooser.setCurrentDirectory (curDir);
                int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( addFolderButton ) ); // Sow the chooser
                
                if ( option == JFileChooser.APPROVE_OPTION ) {
                    
                    File files[] = chooser.getSelectedFiles();
                    addFolders( files );
                    curDir = FileUtil.normalizeFile(chooser.getCurrentDirectory());
                    FoldersListSettings.getDefault().setLastUsedSourceRootFolder(curDir);
                }
                
            }
            else if ( source == removeButton ) { 
                removeElements();
            }
            else if ( source == upButton ) {
                moveUp();
            }
            else if ( source == downButton ) {
                moveDown();
            }
        }
        
        // Selection listener implementation  ----------------------------------
        
        /** Handles changes in the selection
         */        
        public void valueChanged( ListSelectionEvent e ) {
            
            int[] si = rootsList.getSelectedRows();
            
            // addJar allways enabled
            
            // addLibrary allways enabled
            
            // addArtifact allways enabled
            
            // edit enabled only if selection is not empty
            boolean edit = si != null && si.length > 0;            

            // remove enabled only if selection is not empty
            boolean remove = si != null && si.length > 0;
            // and when the selection does not contain unremovable item

            // up button enabled if selection is not empty
            // and the first selected index is not the first row
            boolean up = si != null && si.length > 0 && si[0] != 0;
            
            // up button enabled if selection is not empty
            // and the laset selected index is not the last row
            boolean down = si != null && si.length > 0 && si[si.length-1] !=rootsList.getRowCount() - 1;

            removeButton.setEnabled( remove );
            upButton.setEnabled( up );
            downButton.setEnabled( down );       
                        
            //System.out.println("Selection changed " + edit + ", " + remove + ", " +  + ", " + + ", ");
            
        }

        public void editingCanceled(ChangeEvent e) {

        }

        public void editingStopped(ChangeEvent e) {
            // fireActionPerformed(); 
        }
        
        private void addFolders( File files[] ) {
            int[] si = rootsList.getSelectedRows();
            int lastIndex = si == null || si.length == 0 ? -1 : si[si.length - 1];
            ListSelectionModel selectionModel = this.rootsList.getSelectionModel();
            selectionModel.clearSelection();
            Set rootsFromOtherProjects = new HashSet ();
            Set rootsFromRelatedSourceRoots = new HashSet();
            for( int i = 0; i < files.length; i++ ) {
                File normalizedFile = FileUtil.normalizeFile(files[i]);
                Project p;
                if (ownedFolders.contains(normalizedFile)) {
                    Vector dataVector = rootsModel.getDataVector();
                    for (int j=0; j<dataVector.size();j++) {
                        //Sequential search in this minor case is faster than update of positions during each modification
                        File f = (File )((Vector)dataVector.elementAt(j)).elementAt(0);
                        if (f.equals(normalizedFile)) {
                            selectionModel.addSelectionInterval(j,j);
                        }
                    }
                }
                else if (this.relatedEditMediator != null && this.relatedEditMediator.ownedFolders.contains(normalizedFile)) {
                    rootsFromRelatedSourceRoots.add (normalizedFile);
                }
                else if ((p=FileOwnerQuery.getOwner(normalizedFile.toURI()))!=null && !p.equals(project)) {
                    rootsFromOtherProjects.add (normalizedFile);
                }
                else {
                    int current = lastIndex + 1 + i;
                    rootsModel.insertRow( current, new Object[] {normalizedFile,""}); //NOI18N
                    selectionModel.addSelectionInterval(current,current);
                }
            }
            if (rootsFromOtherProjects.size() > 0 || rootsFromRelatedSourceRoots.size() > 0) {
                JPanel warning = new WarningDlg (rootsFromOtherProjects, rootsFromRelatedSourceRoots,
                    this.errorMessagePOR, this.errorMessageRR);
                DialogDescriptor dd = new DialogDescriptor(warning,NbBundle.getMessage(J2SESourceRootsUi.class,"TITLE_InvalidRoot"),
                    true, new Object[] {DialogDescriptor.OK_OPTION},DialogDescriptor.OK_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN, new HelpCtx (J2SESourceRootsUi.class),null);
                DialogDisplayer.getDefault().notify(dd);
            }
            // fireActionPerformed();
        }    

        private void removeElements() {

            int[] si = rootsList.getSelectedRows();

            if(  si == null || si.length == 0 ) {
                assert false : "Remove button should be disabled"; // NOI18N
            }

            // Remove the items
            for( int i = si.length - 1 ; i >= 0 ; i-- ) {
                this.ownedFolders.remove(((Vector)rootsModel.getDataVector().elementAt(0)).elementAt(0));
                rootsModel.removeRow( si[i] );
            }


            if ( rootsModel.getRowCount() != 0) {
                // Select reasonable item
                int selectedIndex = si[si.length - 1] - si.length  + 1; 
                if ( selectedIndex > rootsModel.getRowCount() - 1) {
                    selectedIndex = rootsModel.getRowCount() - 1;
                }
                rootsList.setRowSelectionInterval( selectedIndex, selectedIndex );
            }

            // fireActionPerformed();

        }

        private void moveUp() {

            int[] si = rootsList.getSelectedRows();

            if(  si == null || si.length == 0 ) {
                assert false : "MoveUp button should be disabled"; // NOI18N
            }

            // Move the items up
            ListSelectionModel selectionModel = this.rootsList.getSelectionModel();
            selectionModel.clearSelection();
            for( int i = 0; i < si.length; i++ ) {
                Vector item = (Vector) rootsModel.getDataVector().elementAt(si[i]);
                int newIndex = si[i]-1;
                rootsModel.removeRow( si[i] );
                rootsModel.insertRow( newIndex, item );
                selectionModel.addSelectionInterval(newIndex,newIndex);
            }
            // fireActionPerformed();
        } 

        private void moveDown() {

            int[] si = rootsList.getSelectedRows();

            if(  si == null || si.length == 0 ) {
                assert false : "MoveDown button should be disabled"; // NOI18N
            }

            // Move the items up
            ListSelectionModel selectionModel = this.rootsList.getSelectionModel();
            selectionModel.clearSelection();
            for( int i = si.length -1 ; i >= 0 ; i-- ) {
                Vector item = (Vector) rootsModel.getDataVector().elementAt(si[i]);
                int newIndex = si[i] + 1;
                rootsModel.removeRow( si[i] );
                rootsModel.insertRow( newIndex, item );
                selectionModel.addSelectionInterval(newIndex,newIndex);
            }
            // fireActionPerformed();
        }    
        

    }

    private static class SourceRootsModel extends DefaultTableModel {

        public SourceRootsModel (Object[][] data) {
            super (data,new Object[]{"location","label"});//NOI18N
        }

        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        public Class getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return File.class;
                case 1:
                    return String.class;
                default:
                    return super.getColumnClass (columnIndex);
            }
        }
    }
    
    private static class FileRenderer extends DefaultTableCellRenderer {
        
        private File projectFolder;
        
        public FileRenderer (File projectFolder) {
            this.projectFolder = projectFolder;
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column) {
            File root = (File) value;
            String pfPath = projectFolder.getAbsolutePath() + File.separatorChar;
            String srPath = root.getAbsolutePath();
            String displayName;
            if (srPath.startsWith(pfPath)) {
                displayName = srPath.substring(pfPath.length());
            }
            else {
                displayName = srPath;
            }
            return super.getTableCellRendererComponent(table, displayName, isSelected, hasFocus, row, column);
        }                        
        
    }

    private static class WarningDlg extends JPanel {

        public WarningDlg (Set projectOwned, Set related, String messagePOR, String messageRR) {
            this.initGui (projectOwned, related, messagePOR, messageRR);
        }

        private void initGui (Set projectOwned, Set related, String messagePOR, String messageRR) {
            setLayout( new GridBagLayout ());
            if (projectOwned.size()>0) {
                JLabel tf = new JLabel(messagePOR);
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = GridBagConstraints.RELATIVE;
                c.gridy = GridBagConstraints.RELATIVE;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1.0;
                c.insets = new Insets (12,12,12,12);
                ((GridBagLayout)this.getLayout()).setConstraints(tf,c);
                this.add (tf);
                JList proots = new JList (projectOwned.toArray());
                proots.setCellRenderer (new InvalidRootRenderer(true));
                JScrollPane p = new JScrollPane (proots);
                c = new GridBagConstraints();
                c.gridx = GridBagConstraints.RELATIVE;
                c.gridy = GridBagConstraints.RELATIVE;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.BOTH;
                c.weightx = c.weighty = 1.0;
                c.insets = new Insets (0,12,12,12);
                ((GridBagLayout)this.getLayout()).setConstraints(p,c);
                this.add (p);
            }
            if (related.size()>0) {
                JLabel tf = new JLabel(messageRR);
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = GridBagConstraints.RELATIVE;
                c.gridy = GridBagConstraints.RELATIVE;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1.0;
                c.insets = new Insets (projectOwned.size()>0?0:12,12,12,12);
                ((GridBagLayout)this.getLayout()).setConstraints(tf,c);
                this.add (tf);
                JList rroots = new JList (related.toArray());
                JScrollPane p = new JScrollPane (rroots);
                rroots.setCellRenderer (new InvalidRootRenderer(false));
                c = new GridBagConstraints();
                c.gridx = GridBagConstraints.RELATIVE;
                c.gridy = GridBagConstraints.RELATIVE;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.fill = GridBagConstraints.BOTH;
                c.weightx = c.weighty = 1.0;
                c.insets = new Insets (0,12,12,12);
                ((GridBagLayout)this.getLayout()).setConstraints(p,c);
                this.add (p);
            }
        }

        private static class InvalidRootRenderer extends DefaultListCellRenderer {

            private boolean projectConflict;

            public InvalidRootRenderer (boolean projectConflict) {
                this.projectConflict = projectConflict;
            }

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                File f = (File) value;
                String message = f.getAbsolutePath();
                if (projectConflict) {
                    Project p = FileOwnerQuery.getOwner(f.toURI());
                    if (p!=null) {
                        ProjectInformation pi = (ProjectInformation) p.getLookup().lookup(ProjectInformation.class);
                        if (pi != null) {
                            String projectName = pi.getDisplayName();
                            if (projectName != null) {
                                message = MessageFormat.format (NbBundle.getMessage(J2SESourceRootsUi.class,"TXT_RootOwnedByProject"), new Object[] {
                                    message,
                                    projectName});
                            }
                        }
                    }
                }
                Component c =  super.getListCellRendererComponent(list, message, index, isSelected, cellHasFocus);
                c.setForeground (new Color (164,0,0));
                return c;
            }
        }
    }

}
