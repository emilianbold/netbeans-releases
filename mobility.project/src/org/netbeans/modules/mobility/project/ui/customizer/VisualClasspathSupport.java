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

package org.netbeans.modules.mobility.project.ui.customizer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.project.support.customizer.AntArtifactChooser;
import org.netbeans.modules.project.support.customizer.LibrariesChooser;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

/** Handles adding, removing, editing and reordering of classpath.
 *
 * @author Petr Hrebejk
 */
public final class VisualClasspathSupport {
    
    static File lastFile = null;
    
    final JTable classpathTable;
    final JButton addJarButton;
    final JButton addFolderButton;
    final JButton addLibraryButton;
    final String libraryType;
    final JButton addArtifactButton;
    final JButton removeButton;
    final JButton upButton;
    final JButton downButton;
    private FileObject myRoot;
    
    private final ClasspathTableModel classpathModel;
    private ProjectProperties properties;
    private String libsClasspath, extraClasspath;
    
    
    public VisualClasspathSupport( JTable classpathTable,
            JButton addJarButton,
            JButton addFolderButton,
            JButton addLibraryButton,
            String libraryType,
            JButton addArtifactButton,
            JButton removeButton,
            JButton upButton,
            JButton downButton ) {
        // Remember all buttons
        this.classpathTable = classpathTable;
        this.classpathModel = new ClasspathTableModel();
        this.classpathTable.setModel( classpathModel );
        this.classpathTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.classpathTable.setDefaultRenderer(VisualClassPathItem.class, new ClassPathRenderer());
        TableColumn column = classpathTable.getColumnModel().getColumn(1);
        JTableHeader header = classpathTable.getTableHeader();
        column.setMaxWidth(24 + SwingUtilities.computeStringWidth(header.getFontMetrics(header.getFont()), String.valueOf(column.getHeaderValue())));
        
        this.addJarButton = addJarButton;
        this.addFolderButton = addFolderButton;
        this.addLibraryButton = addLibraryButton;
        this.libraryType = libraryType;
        this.addArtifactButton = addArtifactButton;
        this.removeButton = removeButton;
        this.upButton = upButton;
        this.downButton = downButton;
        
        // Register the listeners
        ClasspathSupportListener csl = new ClasspathSupportListener();
        
        // On all buttons
        addJarButton.addActionListener( csl );
        addFolderButton.addActionListener( csl );
        addLibraryButton.addActionListener( csl );
        addArtifactButton.addActionListener( csl );
        removeButton.addActionListener( csl );
        upButton.addActionListener( csl );
        downButton.addActionListener( csl );
        // On list selection
        classpathTable.getSelectionModel().addListSelectionListener( csl );
        
        // Set the initial state of the buttons
        csl.valueChanged( null );
        
    }
    
    public void setProperties(ProjectProperties props) {
        this.properties = props;
        this.myRoot = props.getProjectDirectory();
    }
    
    public void setEnabled(final boolean enabled) {
        classpathTable.setEnabled(enabled);
        addJarButton.setEnabled(enabled);
        addFolderButton.setEnabled(enabled);
        addLibraryButton.setEnabled(enabled);
        addArtifactButton.setEnabled(enabled);
        if (enabled) {
            final int[] si = classpathTable.getSelectedRows();
            
            // remove enabled only if selection is not empty
            boolean remove = si != null && si.length > 0;
            // and when the selection does not contain unremovable item
            if ( remove ) {
                for ( int i = 0; i < si.length; i++ ) {
                    final VisualClassPathItem vcpi = (VisualClassPathItem)classpathModel.get( si[i] );
                    if ( !vcpi.canDelete() ) {
                        remove = false;
                        break;
                    }
                }
            }
            
            // up button enabled if selection is not empty
            // and the first selected index is not the first row
            final boolean up = si != null && si.length > 0 && si[0] != 0;
            
            // up button enabled if selection is not empty
            // and the laset selected index is not the last row
            final boolean down = si != null && si.length > 0 && si[si.length-1] != classpathModel.getRowCount() - 1;
            
            removeButton.setEnabled( remove );
            upButton.setEnabled( up );
            downButton.setEnabled( down );
        } else {
            removeButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }
    }
    
    public void setVisualClassPathItems( final List<VisualClassPathItem> items ) {
        
        classpathModel.clear();
        for( final VisualClassPathItem cpItem : items ) {
            classpathModel.addElement( cpItem );
        }
    }
    
    public List<VisualClassPathItem> getVisualClassPathItems() {
        
        final ArrayList<VisualClassPathItem> items = new ArrayList<VisualClassPathItem>();
        for( final Enumeration e = classpathModel.elements(); e.hasMoreElements(); ) {
            final VisualClassPathItem cpItem = (VisualClassPathItem)e.nextElement();
            items.add( cpItem );
        }
        
        return items;
    }

    public List<VisualClassPathItem> getExtraClassPathItems() {
        
        final ArrayList<VisualClassPathItem> items = new ArrayList<VisualClassPathItem>();
        for( final Enumeration e = classpathModel.elements(); e.hasMoreElements(); ) {
            final VisualClassPathItem cpItem = (VisualClassPathItem)e.nextElement();
            if (cpItem.isExtra()) items.add( cpItem );
        }
        
        return items;
    }

    public synchronized void setPropertyNames(String libs, String extra) {
        this.libsClasspath = libs;
        this.extraClasspath = extra;
    }
    
    private synchronized void fireActionPerformed() {
        properties.put(libsClasspath, getVisualClassPathItems());
        properties.put(extraClasspath, getExtraClassPathItems());
    }
    
    // Private methods ---------------------------------------------------------
    
    protected void addLibraries(final Library[] libraries) {
        final int[] si = classpathTable.getSelectedRows();
        final int lastIndex = si == null || si.length == 0 ? -1 : si[si.length - 1];
        for (int i = 0; i < libraries.length; i++) {
            final String libraryName = libraries[i].getName();
            classpathModel.add(
                    lastIndex + 1 + i,
                    new VisualClassPathItem( libraries[i],
                    VisualClassPathItem.TYPE_LIBRARY,
                    "${libs."+libraryName+".classpath}", //NOI18N
                    libraries[i].getDisplayName()));
        }
        fireActionPerformed();
    }
    
    protected void addJarFiles( File files[] ) {
        
        final int[] si = classpathTable.getSelectedRows();
        
        final int lastIndex = si == null || si.length == 0 ? -1 : si[si.length - 1];
        
        for( int i = 0; i < files.length; i++ ) {
            files[i] = FileUtil.normalizeFile(files[i]);
            classpathModel.add(
                    lastIndex + 1 + i,
                    new VisualClassPathItem( files[i],
                    VisualClassPathItem.TYPE_JAR,
                    null,
                    files[i].getPath() ) );
        }
        
        fireActionPerformed();
        
    }
    
    protected void addFolders( File files[] ) {
        
        final int[] si = classpathTable.getSelectedRows();
        
        final int lastIndex = si == null || si.length == 0 ? -1 : si[si.length - 1];
        
        for( int i = 0; i < files.length; i++ ) {
            files[i] = FileUtil.normalizeFile(files[i]);
            classpathModel.add(
                    lastIndex + 1 + i,
                    new VisualClassPathItem( files[i],
                    VisualClassPathItem.TYPE_FOLDER,
                    null,
                    files[i].getPath() ) );
        }
        
        fireActionPerformed();
        
    }
    
    protected void addArtifacts( final AntArtifactChooser.ArtifactItem artifacts[] ) {
        
        final int[] si = classpathTable.getSelectedRows();
        
        final int lastIndex = si == null || si.length == 0 ? -1 : si[si.length - 1];
        
        for( int i = 0; i < artifacts.length; i++ ) {
            final AntArtifact artifact = artifacts[i].getArtifact();
            final URI uri =  artifacts[i].getURI();
            String location;
            try {
                location = FileUtil.normalizeFile(new File(artifact.getScriptLocation().getParentFile().toURI().resolve(uri))).getPath();
            } catch (Exception e) {
                location = uri.getPath();
            }
            final Project p = artifact.getProject();
            if (p == null || !p.getProjectDirectory().equals(myRoot)) {
                classpathModel.add(
                        lastIndex + 1 + i,
                        new VisualClassPathItem( artifact, uri,
                        VisualClassPathItem.TYPE_ARTIFACT,
                        null,
                        location ) );
            }
        }
        
        fireActionPerformed();
        
    }
    
    protected void removeElements() {
        
        final int[] si = classpathTable.getSelectedRows();
        
        if(  si == null || si.length == 0 ) {
            assert false : "Remove button should be disabled"; // NOI18N
        }
        
        // Remove the items
        for( int i = si.length - 1 ; i >= 0 ; i-- ) {
            classpathModel.remove( si[i] );
        }
        
        
        if (classpathModel.getRowCount() > 0) {
            // Select reasonable item
            int selectedIndex = si[si.length - 1] - si.length  + 1;
            if ( selectedIndex > classpathModel.getRowCount() - 1) {
                selectedIndex = classpathModel.getRowCount() - 1;
            }
            classpathTable.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
        }
        
        fireActionPerformed();
        
    }
    
    protected void moveUp() {
        
        int[] si = classpathTable.getSelectedRows();
        
        if(  si == null || si.length == 0 ) {
            assert false : "MoveUp button should be disabled"; // NOI18N
        }
        
        // Move the items up
        for( int i = 0; i < si.length; i++ ) {
            final VisualClassPathItem item = classpathModel.get( si[i] );
            classpathModel.remove( si[i] );
            classpathModel.add( si[i] - 1, item );
        }
        
        // Keep the selection a before
        for( int i = 0; i < si.length; i++ ) {
            si[i] -= 1;
            if (i==0) classpathTable.getSelectionModel().setSelectionInterval(si[i], si[i]);
            else classpathTable.getSelectionModel().addSelectionInterval(si[i], si[i]);
        }
        
        fireActionPerformed();
    }
    
    protected void moveDown() {
        
        int[] si = classpathTable.getSelectedRows();
        
        if(  si == null || si.length == 0 ) {
            assert false : "MoveDown button should be disabled"; // NOI18N
        }
        
        // Move the items up
        for( int i = si.length -1 ; i >= 0 ; i-- ) {
            final VisualClassPathItem item = classpathModel.get( si[i] );
            classpathModel.remove( si[i] );
            classpathModel.add( si[i] + 1, item );
        }
        
        // Keep the selection a before
        for( int i = 0; i < si.length; i++ ) {
            si[i] += 1;
            if (i==0) classpathTable.getSelectionModel().setSelectionInterval(si[i], si[i]);
            else classpathTable.getSelectionModel().addSelectionInterval(si[i], si[i]);
        }
        
        fireActionPerformed();
    }
    
    
    // Private innerclasses ----------------------------------------------------
    
    private class ClasspathSupportListener implements ActionListener, ListSelectionListener {
        
        private ClasspathSupportListener()
        {
            //Just to avoid creation of accessor class
        }
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */
        public void actionPerformed( final ActionEvent e ) {
            
            final Object source = e.getSource();
            
            if ( source == addJarButton ) {
                
                // Let user search for the Jar file
                final JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
                chooser.setMultiSelectionEnabled( true );
                chooser.setDialogTitle( NbBundle.getMessage( VisualClasspathSupport.class, "LBL_Classpath_AddJar" ) ); // NOI18N
                chooser.setFileFilter(new JarFileFilter());
                chooser.setAcceptAllFileFilterUsed( false );
                if (lastFile != null) chooser.setSelectedFile(lastFile);
                final int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( addJarButton ) ); // Sow the chooser
                
                if ( option == JFileChooser.APPROVE_OPTION ) {
                    
                    final File files[] = chooser.getSelectedFiles();
                    if (files.length > 0) lastFile = files[0];
                    addJarFiles( files );
                }
                
            } else if ( source == addFolderButton ) {
                
                // Let user search for the Jar file
                final JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
                chooser.setMultiSelectionEnabled( true );
                chooser.setDialogTitle( NbBundle.getMessage( VisualClasspathSupport.class, "LBL_Classpath_AddFolder" ) ); // NOI18N
                if (lastFile != null) chooser.setSelectedFile(lastFile);
                final int option = chooser.showOpenDialog( SwingUtilities.getWindowAncestor( addFolderButton ) ); // Sow the chooser
                
                if ( option == JFileChooser.APPROVE_OPTION ) {
                    
                    final File files[] = chooser.getSelectedFiles();
                    if (files.length > 0) lastFile = files[0];
                    addFolders( files );
                }
                
            } else if ( source == addLibraryButton ) {
                final LibrariesChooser panel = new LibrariesChooser(libraryType);
                final Object[] options = new Object[] {
                    NbBundle.getMessage(VisualClasspathSupport.class,"LBL_AddLibrary"), //NOI18N
                    NotifyDescriptor.CANCEL_OPTION
                };
                final DialogDescriptor desc = new DialogDescriptor(panel,NbBundle.getMessage( VisualClasspathSupport.class, "LBL_Classpath_AddLibrary" ), //NOI18N
                        true, options, options[0], DialogDescriptor.DEFAULT_ALIGN,null,null);
                desc.setHelpCtx(new HelpCtx(LibrariesChooser.class));
                final Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
                dlg.setVisible(true);
                if (desc.getValue() == options[0]) {
                    addLibraries(panel.getSelectedLibraries());
                }
                dlg.dispose();
            } else if ( source == addArtifactButton ) {
                final AntArtifactChooser.ArtifactItem artifacts[] = AntArtifactChooser.showDialog( JavaProjectConstants.ARTIFACT_TYPE_JAR );
                if ( artifacts != null ) {
                    addArtifacts( artifacts );
                }
            } else if ( source == removeButton ) {
                removeElements();
            } else if ( source == upButton ) {
                moveUp();
            } else if ( source == downButton ) {
                moveDown();
            }
        }
        
        // ListSelectionModel --------------------------------------------------
        
        /** Handles changes in the selection
         */
        public void valueChanged( @SuppressWarnings("unused")
		final ListSelectionEvent e ) {
            setEnabled(true);
        }
        
    }
    
    
    private static class ClassPathRenderer extends DefaultTableCellRenderer {
        
        private ClassPathRenderer()
        {
            //Just to avoid creation of accessor class
        }
        
        public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column );
            final VisualClassPathItem visualClassPathItem = (VisualClassPathItem)value;
            setIcon(visualClassPathItem == null ? null : visualClassPathItem.getIcon());
            return this;
        }
        
    }
    
    private static class JarFileFilter extends FileFilter {
        
        private JarFileFilter()
        {
            //Just to avoid creation of accessor class
        }
        
        public boolean accept(final File f) {
            final String s = f.getName().toLowerCase();
            return f.isDirectory() || s.endsWith(".zip") || s.endsWith(".jar"); //NOI18N
        }
        
        public String getDescription() {
            return NbBundle.getMessage( VisualClasspathSupport.class, "LBL_JarFileFilter"); //NOI18N
        }
        
    }
    
    private class ClasspathTableModel extends AbstractTableModel {

        private Vector<VisualClassPathItem> items = new Vector();

        public String getColumnName(int column) {
            return NbBundle.getMessage(VisualClasspathSupport.class, column == 0 ? "CPTable_Column1" : "CPTable_Column2"); //NOI18N
        }

        public Class getColumnClass(int arg0) {
            return arg0 == 0 ? VisualClassPathItem.class : Boolean.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            items.get(rowIndex).setExtra(!((Boolean)aValue).booleanValue());
            VisualClasspathSupport.this.fireActionPerformed();
        }
        
        public int getRowCount() {
            return items.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public VisualClassPathItem get(int row) {
            return items.get(row);
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            return columnIndex == 0 ? get(rowIndex) : Boolean.valueOf(!get(rowIndex).isExtra());
        }
       
        public void clear() {
            items.clear();
            fireTableDataChanged();
        }
        
        public void addElement(VisualClassPathItem elem) {
            int row = items.size();
            items.add(elem);
            fireTableRowsInserted(row, row);
        }

        public void add(int index, VisualClassPathItem elem) {
            items.add(index, elem);
            fireTableRowsInserted(index, index);
        }

        public void remove(int index) {
            items.remove(index);
            fireTableRowsDeleted(index, index);
        }
        
        public Enumeration<VisualClassPathItem> elements() {
            return items.elements();
        }
    }
}
