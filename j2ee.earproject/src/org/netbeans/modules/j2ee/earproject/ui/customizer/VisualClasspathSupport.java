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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
//import org.netbeans.modules.web.project.WebProjectGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;

import org.netbeans.modules.j2ee.common.J2eeProjectConstants;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;

/** Handles adding, removing, editing and reordering of classpath.
 */
public final class VisualClasspathSupport {
    
    final Project master;
    String j2eePlatform;
    final JTable classpathTable;
    final JButton addJarButton;
    final JButton addLibraryButton;
    final JButton addArtifactButton;
    final JButton editButton;
    final JButton removeButton;
    final JButton upButton;
    final JButton downButton;
    
    private final ClasspathTableModel classpathModel;

    private final ArrayList actionListeners = new ArrayList();
    private static Collection baseLibrarySet = java.util.Collections.EMPTY_LIST; // getLibrarySet(WebProjectGenerator.getBaseLibraries());

        public VisualClasspathSupport(Project master,
                                  String j2eePlatform,
                                  JTable classpathTable,
                                  JButton addJarButton,
                                  JButton addLibraryButton,
                                  JButton addArtifactButton,
                                  JButton editButton,
                                  JButton removeButton,
                                  JButton upButton,
                                  JButton downButton) {
            this(master,j2eePlatform,classpathTable,addJarButton,addLibraryButton, 
                addArtifactButton, editButton,removeButton,upButton,downButton, 
                false);
        }

    public VisualClasspathSupport(Project master,
                                  String j2eePlatform,
                                  JTable classpathTable,
                                  JButton addJarButton,
                                  JButton addLibraryButton,
                                  JButton addArtifactButton,
                                  JButton editButton,
                                  JButton removeButton,
                                  JButton upButton,
                                  JButton downButton,
                                  boolean singleColumn) {
        // Remember all buttons                               
        this.classpathTable = classpathTable;
        this.classpathTable.setGridColor(this.classpathTable.getBackground());
        this.classpathTable.setRowHeight(this.classpathTable.getRowHeight() + 4);
        this.classpathModel = new ClasspathTableModel(singleColumn);
        this.classpathTable.setModel(classpathModel);
        this.classpathTable.setTableHeader(null);
        this.classpathTable.getColumnModel().getColumn(0).setCellRenderer(new LibraryCellRenderer());
        if (!singleColumn) {
        this.classpathTable.getColumnModel().getColumn(1).setCellRenderer(new BooleanCellRenderer(classpathTable));
        this.classpathTable.getColumnModel().getColumn(1).setMaxWidth(25);
        }
        this.classpathTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        this.addJarButton = addJarButton;
        this.addLibraryButton = addLibraryButton;
        this.addArtifactButton = addArtifactButton;
        this.editButton = editButton;
        this.removeButton = removeButton;
        this.upButton = upButton;
        this.downButton = downButton;
                                     
        this.master = master;
        this.j2eePlatform = j2eePlatform;
        
        // Register the listeners
        ClasspathSupportListener csl = new ClasspathSupportListener();
        
        // On all buttons
        addJarButton.addActionListener(csl); 
        addLibraryButton.addActionListener(csl);
        addArtifactButton.addActionListener(csl);
        editButton.addActionListener(csl);
        removeButton.addActionListener(csl);
        upButton.addActionListener(csl);
        downButton.addActionListener(csl);
        // On list selection
        classpathTable.getSelectionModel().addListSelectionListener(csl);

        classpathModel.addTableModelListener(csl);
            
        // Set the initial state of the buttons
        csl.valueChanged(null);
    } 
    
    public void setVisualClassPathItems(List items) {
        classpathModel.setItems(items);
    }
    
    public List getVisualClassPathItems() {
        return classpathModel.getItems();
    }
    
    /** Action listeners will be informed when the value of the
     * list changes.
     */
    public void addActionListener( ActionListener listener ) {
        actionListeners.add( listener );
    }
    
    public void removeActionListener( ActionListener listener ) {
        actionListeners.remove( listener );
    }
    
    private void fireActionPerformed() {
        ArrayList listeners;
        
        synchronized ( this ) {
             listeners = new ArrayList( actionListeners );
        }
        
        ActionEvent ae = new ActionEvent( this, 0, null );
        
        for( Iterator it = listeners.iterator(); it.hasNext(); ) {
            ActionListener al = (ActionListener)it.next();
            al.actionPerformed( ae );
        }
    }
        
    // Private methods ---------------------------------------------------------

    private Collection getLibraries () {
        Collection libs = new HashSet ();
        for (Iterator iter = getVisualClassPathItems().iterator(); iter.hasNext();) {
            VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
            if (vcpi.getType() == VisualClassPathItem.TYPE_LIBRARY) {
                libs.add (vcpi.getObject());
            }
        }
        return libs;
    }

    private void addLibraries(Library[] libraries) {
        if (libraries.length > 0) {   
            List newLibList = new ArrayList(Arrays.asList(libraries));
            classpathTable.clearSelection();
            int n0 = classpathModel.size();
            for (int i = 0; i < n0; i++) {
                VisualClassPathItem item = classpathModel.get(i);
                if(item.getType() == VisualClassPathItem.TYPE_LIBRARY) {
                    if(newLibList.remove(item.getObject())) {
                        classpathTable.addRowSelectionInterval(i, i);
                    }
                }
            }
            int n = newLibList.size();
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    Library library = (Library) newLibList.get(i);
                    VisualClassPathItem item = VisualClassPathItem.create (library, VisualClassPathItem.PATH_IN_WAR_LIB);
                    classpathModel.add(item);
                }
                rowsAdded(n0, n);
            }
        }
    }

    private void addJarFiles(File files[]) {
        final int n = files.length;
        if (n > 0) {
            classpathTable.clearSelection();
            final int n0 = classpathModel.size();
            for (int i = 0; i < n; i++) {
                String pathInWar;
                if (files[i].isDirectory()) {
                    pathInWar = VisualClassPathItem.PATH_IN_WAR_NONE;
                } else {
                    pathInWar = VisualClassPathItem.PATH_IN_WAR_LIB;
                }
                classpathModel.add(VisualClassPathItem.create (files[i], pathInWar));
            }
            rowsAdded(n0, n);
        }
    }
    
    private void addArtifacts(AntArtifact artifacts[]) {
        final int n = artifacts.length;
        if (n > 0) {
            classpathTable.clearSelection();
            final int n0 = classpathModel.size();
            for (int i = 0; i < n; i++) {
                classpathModel.add(VisualClassPathItem.create (artifacts[i], VisualClassPathItem.PATH_IN_WAR_LIB));
            }
            rowsAdded(n0, n);
        }
    }

    private void rowsAdded(int n0, int n) {
        classpathModel.fireTableRowsInserted(n0, n0 + n - 1);
        classpathTable.addRowSelectionInterval(n0, n0 + n - 1);
        fireActionPerformed();
    }

    private void removeSelectedItems() {
        ListSelectionModel sm = classpathTable.getSelectionModel();
        if (sm.isSelectionEmpty()) {
            assert false : "Remove button should be disabled"; // NOI18N
        }
        int index = sm.getMinSelectionIndex();
        Collection elements = new ArrayList();
        final int n0 = classpathModel.size();
        for (int i = n0 - 1; i >=0; i--) {
            if (sm.isSelectedIndex(i) && !isBaseLibraryItem(classpathModel.get(i))) {
                classpathModel.remove(i);
            }
        }
        final int n = classpathModel.size();
        classpathModel.fireTableRowsDeleted(elements.size(), n0 - 1);
        if (index >= n) {
            index = n - 1;
        }
        sm.setSelectionInterval(index, index);

        fireActionPerformed();
    }

    private void moveUp() {
        int[] si = classpathTable.getSelectedRows();

        if (si == null || si.length == 0 || si[0] == 0) {
            assert false : "MoveUp button should be disabled"; // NOI18N
        }

        // Move the items up
        classpathTable.clearSelection();
        for (int i = 0; i < si.length; i++) {
            final int index = si[i] - 1;
            classpathModel.add(index, classpathModel.remove(index + 1));
            classpathTable.addRowSelectionInterval(index, index);
        }

        fireActionPerformed();
    }

    private void moveDown() {
        int[] si = classpathTable.getSelectedRows();

        if (si == null || si.length == 0 || si[si.length - 1] >= (classpathModel.size() - 1)) {
            assert false : "MoveUp button should be disabled"; // NOI18N
        }

        // Move the items up
        classpathTable.clearSelection();
        for( int i = si.length - 1; i >= 0; i-- ) {
            final int index = si[i] + 1;
            classpathModel.add( index, classpathModel.remove( index - 1 ) );
            classpathTable.addRowSelectionInterval(index, index);
        }

        fireActionPerformed();
    }

    private void editLibrary() {
        DefaultListSelectionModel sm = (DefaultListSelectionModel) classpathTable.getSelectionModel();
        int index = sm.getMinSelectionIndex();
        if (sm.isSelectionEmpty())
            assert false : "EditLibrary button should be disabled"; // NOI18N

        VisualClassPathItem item = classpathModel.get(index);
        if (item.getType() == VisualClassPathItem.TYPE_LIBRARY)
            LibrariesCustomizer.showCustomizer((Library) item.getObject());

        fireActionPerformed();
    }

    static Collection getLibrarySet(final Collection libraryNames) {
        final Collection librarySet = new HashSet();
        for (Iterator it = libraryNames.iterator(); it.hasNext();) {
            librarySet.add(LibraryManager.getDefault().getLibrary((String) it.next()));
        }
        return librarySet;
    }

    private static boolean isBaseLibraryItem(final VisualClassPathItem item) {
        return baseLibrarySet.contains(item.getObject());
    }

    public static String getLibraryString(Library lib) {
        final List content = lib.getContent("classpath");
        StringBuffer sb = new StringBuffer();
        for (Iterator it = content.iterator(); it.hasNext();) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(it.next().toString());
        }
        String s = sb.toString();
        return s;
    }

    private static String getBundleResource(final String resourceName) {
        return NbBundle.getMessage(VisualClasspathSupport.class, resourceName);
    }

    // Private innerclasses ----------------------------------------------------

    private class ClasspathSupportListener implements ActionListener, ListSelectionListener, TableModelListener {

        // Implementation of ActionListener ------------------------------------
        /** Handles button events
         */
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();

            if (source == addJarButton) {
                // Let user search for the Jar file
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.setMultiSelectionEnabled( true );
                chooser.setDialogTitle(getBundleResource("LBL_AddJar_DialogTitle")); //NOI18N
                chooser.setFileFilter(new SimpleFileFilter(getBundleResource("LBL_ZipJarFolderFilter"), // NOI18N
                        new String[] {"ZIP","JAR"}));                                                // NOI18N
                chooser.setAcceptAllFileFilterUsed(false);
                
                int option = chooser.showOpenDialog( null ); // Sow the chooser
                if (option == JFileChooser.APPROVE_OPTION) {
                    File files[] = chooser.getSelectedFiles();
                    addJarFiles(files);
                }
            } else if ( source == addLibraryButton ) {
                final LibrariesChooser panel = new LibrariesChooser(getLibraries(), j2eePlatform);
                final JButton btnAddLibrary = new JButton(getBundleResource("LBL_AddLibrary"));
                Object[] options = new Object[]{btnAddLibrary, DialogDescriptor.CANCEL_OPTION};
                final DialogDescriptor desc = new DialogDescriptor(panel,
                        getBundleResource("LBL_CustomizeCompile_Classpath_AddLibrary"), //NOI18N
                        true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
                final Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
                panel.addListSelectionListener(new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        btnAddLibrary.setEnabled(panel.isValidSelection());
                    }
                });
                btnAddLibrary.setEnabled(panel.isValidSelection());
                dlg.setVisible(true);
                if (desc.getValue() == options[0]) {
                    addLibraries(panel.getSelectedLibraries());
                }
                dlg.dispose();
            } else if ( source == addArtifactButton ) { 
         //       AntArtifact artifacts[] = AntArtifactChooser.showDialog(JavaProjectConstants.ARTIFACT_TYPE_JAR, master);
                // XXX this is hardcoded
                AntArtifact artifacts[] = AntArtifactChooser.showDialog(master,
                    new String[] { 
                        J2eeProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE,
                        WebProjectConstants.ARTIFACT_TYPE_WAR_EAR_ARCHIVE,
                        JavaProjectConstants.ARTIFACT_TYPE_JAR });
                if ( artifacts != null ) {
                    addArtifacts( artifacts );
                }
            } else if ( source == removeButton ) { 
                removeSelectedItems();
            } else if ( source == upButton ) {
                moveUp();
            } else if ( source == downButton ) {
                moveDown();
            } else if ( source == editButton ) {
                editLibrary();
            }
        }

        // ListSelectionModel --------------------------------------------------
        /** Handles changes in the selection
         */        
        public void valueChanged( ListSelectionEvent e ) {

            int[] si = classpathTable.getSelectedRows();

            // addJar allways enabled

            // addLibrary allways enabled

            // addArtifact allways enabled

            // edit enabled only if selection is not empty
            boolean edit = false;
            if (si != null && si.length > 0) {
                for (int i = 0; i < si.length; i++) {
                    int index = si[i];
                    final VisualClassPathItem item = classpathModel.get(index);
                    if(item.getType() != VisualClassPathItem.TYPE_LIBRARY && !isBaseLibraryItem(item)) {
                        edit = true;
                        break;
                    }
                }
            }

            // remove enabled only if selection is not empty
            boolean remove = si != null && si.length > 0;
            // and when the selection does not contain unremovable item
            if ( remove ) {
                for ( int i = 0; i < si.length; i++ ) {
                    final int index = si[i];
                    assert index < classpathModel.size()  :
                            "The selected indices " + Arrays.asList (Utilities.toObjectArray (si)) + // NOI18N
                            " at " + i +  // NOI18N
                            " must fit into size of classpathModel" + classpathModel.size() ; // NOI18N
                    VisualClassPathItem item = classpathModel.get( index );
                    if ( !item.canDelete() || isBaseLibraryItem(item)) {
                        remove = false;
                        break;
                    }
                }
            }

            // up button enabled if selection is not empty
            // and the first selected index is not the first row
            boolean up = si != null && si.length > 0 && si[0] != 0;

            // up button enabled if selection is not empty
            // and the laset selected index is not the last row
            boolean down = si != null && si.length > 0 && si[si.length-1] != classpathModel.size() - 1;

            editButton.setEnabled( edit );
            removeButton.setEnabled( remove );
            upButton.setEnabled( up );
            downButton.setEnabled( down );
        }

        // TableModelListener --------------------------------------
        public void tableChanged(TableModelEvent e) {
            if (e.getColumn() == 1) {
                VisualClassPathItem item = classpathModel.get(e.getFirstRow());
                if (classpathModel.getValueAt(e.getFirstRow(), 1) == Boolean.TRUE) {
                    item.setPathInWAR(VisualClassPathItem.PATH_IN_WAR_LIB);
                } else {
                    item.setPathInWAR(VisualClassPathItem.PATH_IN_WAR_NONE);
                }

                fireActionPerformed();
            }
        }
    }

    private static class LibraryCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            final Object o;
            if (value instanceof VisualClassPathItem) {
                final VisualClassPathItem item = (VisualClassPathItem) value;
                setIcon(item.getIcon());
                setEnabled(!isBaseLibraryItem(item));
                final String toolTipText = item.getToolTipText();
                setToolTipText(toolTipText);
                o = item.toString();
            } else {
                o = value;
            }
            return super.getTableCellRendererComponent(table, o, isSelected, false, row, column);
        }
    }

    private static class BooleanCellRenderer implements TableCellRenderer {
        private TableCellRenderer booleanRenderer;
        private TableCellRenderer defaultRenderer;

        public BooleanCellRenderer(JTable table) {
            booleanRenderer = table.getDefaultRenderer(Boolean.class);
            defaultRenderer = new DefaultTableCellRenderer();
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            TableCellRenderer renderer = value instanceof Boolean ? booleanRenderer : defaultRenderer;
            return renderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        }
    }

    class ClasspathTableModel extends AbstractTableModel {
        int columnCount = 2;
        
        ClasspathTableModel(boolean singleColumn) {
            if (singleColumn)
                columnCount = 1;
        }

        private List cpItems;

        public int getColumnCount() {
            return columnCount; //classpath item name, WAR checkbox
        }

        public int getRowCount() {
            if (cpItems == null)
                return 0;
            return cpItems.size();
        }

        public Object getValueAt(int row, int col) {
            final VisualClassPathItem item = (VisualClassPathItem) cpItems.get(row);
            if(col == 0) {
                return item;
            } else {
                return isInWar(item);
            }
        }

        public VisualClassPathItem get(int row) {
            return (VisualClassPathItem) getValueAt(row, 0);
        }

        public void setItem (int row, VisualClassPathItem item) {
            cpItems.set(row, item);
            fireTableCellUpdated(row, 0);
            fireTableCellUpdated(row, 1);
        }

        public void add (VisualClassPathItem item) {
            cpItems.add(item);
        }

        public void add (int index, VisualClassPathItem item) {
            cpItems.add(index, item);
        }

        public VisualClassPathItem remove(int index) {
            return (VisualClassPathItem) cpItems.remove(index);
        }

        private Boolean isInWar(VisualClassPathItem item) {
            Boolean isInWar;
            final String pathInWAR = item.getPathInWAR();
            if (pathInWAR == null || pathInWAR.equals(VisualClassPathItem.PATH_IN_WAR_NONE)) {
                if(isBaseLibraryItem(item) || (item.getType() == VisualClassPathItem.TYPE_JAR && 
                    ((File) item.getObject()).isDirectory())) {
                    isInWar = null;
                } else {
                    isInWar = Boolean.FALSE;
                }
            } else {
                isInWar = Boolean.TRUE;
            }
            return isInWar;
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            if(c == 1) {
                return Boolean.class;
            } else {
                return VisualClassPathItem.class;
            }
        }

        public boolean isCellEditable(int row, int col) {
            if (col == 1) {
                return isInWar(classpathModel.get(row)) instanceof Boolean;
            } else {
                return false;
            }
        }

        public void setValueAt(Object value, int row, int col) {
            if(col == 0) {
                setItem(row, (VisualClassPathItem) value);
            } else {
                if (value instanceof Boolean) {
                    ((VisualClassPathItem) cpItems.get(row)).setPathInWAR(value == Boolean.TRUE ?
                            VisualClassPathItem.PATH_IN_WAR_LIB : VisualClassPathItem.PATH_IN_WAR_NONE);
                    fireTableCellUpdated(row, col);
                }
            }
        }

        public void setItems(List items) {
            cpItems = new ArrayList(items);
            fireTableDataChanged();
        }

        public List getItems() {
            return new ArrayList(cpItems);

        }

        public int size() {
            return getRowCount();
        }
    }
    
    private static class SimpleFileFilter extends FileFilter {
        private String description;
        private Collection extensions;

        public SimpleFileFilter(String description, String[] extensions) {
            this.description = description;
            this.extensions = Arrays.asList(extensions);
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;
            String name = f.getName();
            int index = name.lastIndexOf('.'); //NOI18N
            if (index <= 0 || index==name.length()-1)
                return false;
            String extension = name.substring(index+1).toUpperCase();
            return this.extensions.contains(extension);
        }

        public String getDescription() {
            return this.description;
        }
    }

}
