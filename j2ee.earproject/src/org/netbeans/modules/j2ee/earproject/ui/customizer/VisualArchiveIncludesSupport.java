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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;

import org.netbeans.modules.j2ee.common.J2eeProjectConstants;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;

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

    private final ArrayList actionListeners = new ArrayList();
    
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
    
    public void setVisualWarItems(List items) {
        Object data[][] = new Object[items.size()][2];
        this.data = data;
        for (int i = 0; i < items.size(); i++) {
            classpathModel.setValueAt((VisualClassPathItem) items.get(i), i, 0);
            String pathInWAR = ((VisualClassPathItem) items.get(i)).getPathInWAR();
            classpathModel.setValueAt(pathInWAR, i, 1);
        }
        
        classpathModel.fireTableDataChanged();        
    }
    
    public List getVisualWarItems() {
        ArrayList items = new ArrayList();
        for (int i = 0; i < data.length; i++)
            items.add((VisualClassPathItem) classpathModel.getValueAt(i, 0));
        
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
        ArrayList listeners;
        
        synchronized (this) {
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
        ArrayList list = new ArrayList ();
        for (Iterator iter = getVisualWarItems().iterator(); iter.hasNext();) {
            VisualClassPathItem vcpi = (VisualClassPathItem) iter.next();
            if (vcpi.getType() == VisualClassPathItem.TYPE_LIBRARY) {
                list.add (vcpi.getObject());
            }
        }
        return list;
    }
    
    private void addLibraries (Library[] libraries) {
        if (libraries.length > 0) {   
            List newLibList = new ArrayList(Arrays.asList(libraries));
            classpathTable.clearSelection();
            int n0 = data.length;
            for (int i = 0; i < n0; i++) {
                VisualClassPathItem item = (VisualClassPathItem) data[i][0];
                if(item.getType() == VisualClassPathItem.TYPE_LIBRARY) {
                    if(newLibList.remove(item.getObject()))
                        classpathTable.addRowSelectionInterval(i, i);
                }
            }
            int n = newLibList.size();
            if (n > 0) {
                Object[][] newData = new Object[n0 + n][2];
                for (int i = 0; i < n0; i++)
                    newData[i] = data[i];
                for (int i = 0; i < n; i++) {
                    Library library = (Library) newLibList.get(i);
                    String libraryName = library.getName();
                    VisualClassPathItem item = new VisualClassPathItem(library, VisualClassPathItem.TYPE_LIBRARY, "${libs."+libraryName+".classpath}", libraryName, VisualClassPathItem.PATH_IN_WAR_APPLET);//NOI18N
                    newData[n0 + i][0] = item; 
                    newData[n0 + i][1] = VisualClassPathItem.PATH_IN_WAR_APPLET;
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
        for (int i = 0; i < data.length; i++)
            newData[i] = data[i];
        for (int i = 0; i < files.length; i++) {
            newData[data.length + i][0] = new VisualClassPathItem(files[i], VisualClassPathItem.TYPE_JAR, null, files[i].getPath(), VisualClassPathItem.PATH_IN_WAR_APPLET);
            newData[data.length + i][1] = VisualClassPathItem.PATH_IN_WAR_APPLET;
        }
        
        data = newData;
        classpathModel.fireTableRowsInserted(data.length, data.length + files.length - 1);
        
        fireActionPerformed();
    }
    
    private void addArtifacts( AntArtifact artifacts[] ) {
        Object[][] newData = new Object[data.length + artifacts.length][2];
        for (int i = 0; i < data.length; i++)
            newData[i] = data[i];
        for (int i = 0; i < artifacts.length; i++) {
            newData[data.length + i][0] = new VisualClassPathItem(artifacts[i], VisualClassPathItem.TYPE_ARTIFACT, null, artifacts[i].getArtifactLocations()[0].toString(), VisualClassPathItem.PATH_IN_WAR_APPLET);
            newData[data.length + i][1] = VisualClassPathItem.PATH_IN_WAR_APPLET;
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
        Collection elements = new ArrayList();
        final int n0 = data.length;
        for (int i = 0; i < n0; i++) {
            if (!sm.isSelectedIndex(i)) {
                elements.add(data[i]);
            }
        }
        final int n = elements.size();
        data = (Object[][]) elements.toArray(new Object[n][2]);
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
                if (desc.getValue() == options[0])
                   addLibraries (panel.getSelectedLibraries());
                
                dlg.dispose();
            } else if ( source == addArtifactButton ) { 
//       AntArtifact artifacts[] = AntArtifactChooser.showDialog(JavaProjectConstants.ARTIFACT_TYPE_JAR, master);
                // XXX this is hardcoded
                AntArtifact artifacts[] = AntArtifactChooser.showDialog(master,
                    new String[] { 
                        J2eeProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE,
                        WebProjectConstants.ARTIFACT_TYPE_WAR_EAR_ARCHIVE,
                        JavaProjectConstants.ARTIFACT_TYPE_JAR });
         //                AntArtifact artifacts[] = AntArtifactChooser.showDialog(JavaProjectConstants.ARTIFACT_TYPE_JAR, master);
                if ( artifacts != null )
                    addArtifacts( artifacts );
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
                if (!vcpi.canDelete())
                    remove = false;
            }
                        
            removeButton.setEnabled(remove);
        }
        
        // TableModelListener --------------------------------------
        public void tableChanged(TableModelEvent e) {
            if (e.getType() == javax.swing.event.TableModelEvent.DELETE) {
//                cpItem
            }
            if (e.getColumn() == 1) {
                VisualClassPathItem cpItem = (VisualClassPathItem) classpathModel.getValueAt(e.getFirstRow(), 0);
                cpItem.setPathInWAR((String) classpathModel.getValueAt(e.getFirstRow(), 1));
                
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
            if (data == null)
                return 0;
            return data.length;
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public boolean isCellEditable(int row, int col) {
            if (col == 1)
                return true;
            else
                return false;
        }

        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }

}
