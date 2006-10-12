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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/** Handles adding and removing of additional war content.
 */
final class VisualArchiveIncludesSupport {
    
    final Project master;
    final String j2eePlatform;
    final JTable classpathTable;
    final JButton addJarButton;
    final JButton addLibraryButton;
    final JButton addArtifactButton;
    final JButton removeButton;
    
    private final ClasspathTableModel classpathModel;
    private Object[][] data; 

    private final List<ActionListener> actionListeners = new ArrayList<ActionListener>();
    
    public VisualArchiveIncludesSupport(Project master,
                                    String j2eePlatform,
                                    JTable classpathTable,
                                    JButton addJarButton,
                                    JButton addLibraryButton,
                                    JButton addArtifactButton,
                                    JButton removeButton) {

        // Remember all buttons                               
        this.classpathTable = classpathTable;
        this.classpathModel = new ClasspathTableModel();
        this.classpathTable.setModel(classpathModel);
        this.classpathTable.getColumnModel().getColumn(0).setHeaderValue(NbBundle.getMessage(VisualArchiveIncludesSupport.class, "TXT_Archive_Item"));
        this.classpathTable.getColumnModel().getColumn(1).setHeaderValue(NbBundle.getMessage(VisualArchiveIncludesSupport.class, "TXT_Archive_PathInArchive"));
        this.classpathTable.getColumnModel().getColumn(0).setCellRenderer(new ClassPathCellRenderer());
        this.classpathTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                if (value != null) {
                    setToolTipText(value.toString());
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });

        this.classpathTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        this.addJarButton = addJarButton;
        this.addLibraryButton = addLibraryButton;
        this.addArtifactButton = addArtifactButton;
        this.removeButton = removeButton;
                       
        this.master = master;
        this.j2eePlatform = j2eePlatform;

        // Register the listeners
        ClasspathSupportListener csl = new ClasspathSupportListener();
        
        // On all buttons
        addJarButton.addActionListener(csl); 
        addLibraryButton.addActionListener(csl);
        addArtifactButton.addActionListener(csl);
        removeButton.addActionListener(csl);
        // On list selection
        classpathTable.getSelectionModel().addListSelectionListener(csl);

        classpathModel.addTableModelListener(csl);

        // Set the initial state of the buttons
        csl.valueChanged(null);
    } 
    
    public void setVisualWarItems(List<VisualClassPathItem> items) {
        Object data[][] = new Object[items.size()][2];
        this.data = data;
        for (int i = 0; i < items.size(); i++) {
            classpathModel.setValueAt(items.get(i), i, 0);
            String pathInWAR = items.get(i).getPathInEAR();
            classpathModel.setValueAt(pathInWAR, i, 1);
        }
        
        classpathModel.fireTableDataChanged();        
    }
    
    public List<VisualClassPathItem> getVisualWarItems() {
        List<VisualClassPathItem> items = new ArrayList<VisualClassPathItem>();
        for (int i = 0; i < data.length; i++) {
            items.add((VisualClassPathItem) classpathModel.getValueAt(i, 0));
        }
        return items;
    } 
    
    public void addTableModelListener(TableModelListener tml) {
        classpathModel.addTableModelListener(tml);
    }
    
    public void removeTableModelListener(TableModelListener tml) {
        classpathModel.removeTableModelListener(tml);
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
        List<ActionListener> listeners;
        
        synchronized (this) {
             listeners = new ArrayList<ActionListener>( actionListeners );
        }
        
        ActionEvent ae = new ActionEvent( this, 0, null );
        
        for (ActionListener al : listeners) {
            al.actionPerformed( ae );
        }
    }
        
    // Private methods ---------------------------------------------------------

    private Collection<Object> getLibraries () {
        List<Object> list = new ArrayList<Object>();
        for (VisualClassPathItem vcpi : getVisualWarItems()) {
            if (vcpi.getType() == VisualClassPathItem.Type.LIBRARY) {
                list.add(vcpi.getObject());
            }
        }
        return list;
    }
    
    private void addLibraries (Library[] libraries) {
        if (libraries.length > 0) {   
            List<Library> newLibList = new ArrayList<Library>(Arrays.asList(libraries));
            classpathTable.clearSelection();
            int n0 = data.length;
            for (int i = 0; i < n0; i++) {
                VisualClassPathItem item = (VisualClassPathItem) data[i][0];
                if(item.getType() == VisualClassPathItem.Type.LIBRARY &&
                        newLibList.remove(item.getObject())) {
                    classpathTable.addRowSelectionInterval(i, i);
                }
            }
            int n = newLibList.size();
            if (n > 0) {
                Object[][] newData = new Object[n0 + n][2];
                for (int i = 0; i < n0; i++) {
                    newData[i] = data[i];
                }
                for (int i = 0; i < n; i++) {
                    Library library = newLibList.get(i);
                    VisualClassPathItem item = VisualClassPathItem.createLibrary(library);
                    newData[n0 + i][0] = item; 
                    newData[n0 + i][1] = VisualClassPathItem.PATH_IN_EAR;
                }

                data = newData;
                classpathModel.fireTableRowsInserted(n0, n0 + n - 1);
                classpathTable.addRowSelectionInterval(n0, n0 + n - 1);
            }

            fireActionPerformed();
        }

    }

    private void addJarFiles( File files[] ) {
        Object[][] newData = new Object[data.length + files.length][2];
        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }
        for (int i = 0; i < files.length; i++) {
            VisualClassPathItem jarFile = VisualClassPathItem.createJAR(files[i]);
            newData[data.length + i][0] = jarFile;
            newData[data.length + i][1] = jarFile.getPathInEAR();
        }
        
        data = newData;
        classpathModel.fireTableRowsInserted(data.length, data.length + files.length - 1);
        
        fireActionPerformed();
    }
    
    private void addArtifacts( AntArtifact artifacts[] ) {
        Object[][] newData = new Object[data.length + artifacts.length][2];
        for (int i = 0; i < data.length; i++) {
            newData[i] = data[i];
        }
        for (int i = 0; i < artifacts.length; i++) {
            VisualClassPathItem vcpi = VisualClassPathItem.createArtifact(artifacts[i]);
            newData[data.length + i][0] = vcpi;
            newData[data.length + i][1] = vcpi.getPathInEAR();
        }
        
        data = newData;
        classpathModel.fireTableRowsInserted(data.length, data.length + artifacts.length - 1);
        
        fireActionPerformed();
    }
    
    private void removeElements() {
        ListSelectionModel sm = classpathTable.getSelectionModel();
        int index = sm.getMinSelectionIndex();
        if (sm.isSelectionEmpty()) {
            assert false : "Remove button should be disabled"; // NOI18N
        }
        Collection<Object> elements = new ArrayList<Object>();
        final int n0 = data.length;
        for (int i = 0; i < n0; i++) {
            if (!sm.isSelectedIndex(i)) {
                elements.add(data[i]);
            }
        }
        final int n = elements.size();
        data = elements.toArray(new Object[n][2]);
        classpathModel.fireTableRowsDeleted(elements.size(), n0 - 1);

        if (index >= n) {
            index = n - 1;
        }
        sm.setSelectionInterval(index, index);

        fireActionPerformed();
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private class ClasspathSupportListener implements ActionListener, ListSelectionListener, TableModelListener {     
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */        
        public void actionPerformed( ActionEvent e ) {
            Object source = e.getSource();
            if ( source == addJarButton ) { 
                // Let user search for the Jar file
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
                chooser.setMultiSelectionEnabled( true );
                chooser.setDialogTitle( NbBundle.getMessage( VisualArchiveIncludesSupport.class, "LBL_CustomizeCompile_Classpath_AddJar_JButton" ) ); // NOI18N
                //chooser.setFileFilter( ProjectDirFilter.INSTANCE );
                chooser.setAcceptAllFileFilterUsed( false );
                
                int option = chooser.showOpenDialog( null ); // Show the chooser
                
                if ( option == JFileChooser.APPROVE_OPTION ) {
                    File files[] = chooser.getSelectedFiles();
                    addJarFiles( files );
                }
            } else if ( source == addLibraryButton ) {
                LibrariesChooser panel = new LibrariesChooser(getLibraries(), j2eePlatform);
                Object[] options = new Object[] {
                    NbBundle.getMessage (VisualArchiveIncludesSupport.class,"LBL_AddLibrary"),
                    DialogDescriptor.CANCEL_OPTION
                };
                DialogDescriptor desc = new DialogDescriptor(panel,NbBundle.getMessage( VisualArchiveIncludesSupport.class, "LBL_CustomizeCompile_Classpath_AddLibrary" ),
                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN,null,null);
                Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
                dlg.setVisible(true);
                if (desc.getValue() == options[0]) {
                   addLibraries (panel.getSelectedLibraries());
                }
                dlg.dispose();
            } else if ( source == addArtifactButton ) { 
//       AntArtifact artifacts[] = AntArtifactChooser.showDialog(JavaProjectConstants.ARTIFACT_TYPE_JAR, master);
                // XXX this is hardcoded
                AntArtifact artifacts[] = AntArtifactChooser.showDialog(master,
                    new String[] { 
                        EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE,
                        WebProjectConstants.ARTIFACT_TYPE_WAR_EAR_ARCHIVE,
                        JavaProjectConstants.ARTIFACT_TYPE_JAR });
         //                AntArtifact artifacts[] = AntArtifactChooser.showDialog(JavaProjectConstants.ARTIFACT_TYPE_JAR, master);
                if ( artifacts != null ) {
                    addArtifacts( artifacts );
                }
            } else if ( source == removeButton ) { 
                removeElements();
            }
        }
        
        // ListSelectionModel --------------------------------------------------
        
        /** Handles changes in the selection
         */        
        public void valueChanged( ListSelectionEvent e ) {
            DefaultListSelectionModel sm = (DefaultListSelectionModel) classpathTable.getSelectionModel();
            int index = sm.getMinSelectionIndex();
            
            // remove enabled only if selection is not empty
            boolean remove = index != -1;
            // and when the selection does not contain unremovable item
            if (remove) {
                VisualClassPathItem vcpi = (VisualClassPathItem) classpathModel.getValueAt(index, 0);
                if (!vcpi.canDelete()) {
                    remove = false;
                }
            }
                        
            removeButton.setEnabled(remove);
        }
        
        // TableModelListener --------------------------------------
        public void tableChanged(TableModelEvent e) {
            if (e.getColumn() == 1) {
                VisualClassPathItem cpItem = (VisualClassPathItem) classpathModel.getValueAt(e.getFirstRow(), 0);
                cpItem.setPathInEAR((String) classpathModel.getValueAt(e.getFirstRow(), 1));
                
                fireActionPerformed();
            }
        }

    }
    
    private static class ClassPathCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof VisualClassPathItem) {
                final VisualClassPathItem item = (VisualClassPathItem) value;
                setIcon(item.getIcon());
                // XXX integrate this into the generic VCPI...
//                setToolTipText(item.getToolTipText());
            }
            final String s = value == null ? null : value.toString();
            return super.getTableCellRendererComponent(table, s, isSelected, false, row, column);
        }
    }

    class ClasspathTableModel extends AbstractTableModel {
        public int getColumnCount() {
            return 2; //classpath item name, item location within WAR
        }

        public int getRowCount() {
            if (data == null) {
                return 0;
            }
            return data.length;
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public boolean isCellEditable(int row, int col) {
            return col == 1;
        }

        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }

}
