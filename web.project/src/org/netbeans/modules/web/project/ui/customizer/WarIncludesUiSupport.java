/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.customizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

import org.openide.util.NbBundle;

import org.netbeans.api.project.libraries.Library;

import org.netbeans.modules.web.project.classpath.ClassPathSupport;

/**
 *
 * @author Petr Hrebejk, Radko Najman
 */
public class WarIncludesUiSupport {
    
    static private Object[][] data; 

    // Methods for working with list models ------------------------------------
    
    public static ClasspathTableModel createTableModel( List items ) {
        
        ClasspathTableModel model = new ClasspathTableModel();
        
        data = new Object[items.size()][2];
        for (int i = 0; i < items.size(); i++) {
            model.setValueAt((ClassPathSupport.Item) items.get(i), i, 0);
            String pathInWAR = ((ClassPathSupport.Item) items.get(i)).getPathInWAR();
            model.setValueAt(pathInWAR, i, 1);
        }
        
        model.fireTableDataChanged();        
        
        return model;
    }
    
    public static Iterator getIterator( ClasspathTableModel model ) {
        List items= new LinkedList();
        for (int i = 0; i < data.length; i++)
            items.add(data[i][0]);
        
        return items.iterator();        
    }
    
    /** Removes selected indices from the model. Returns the index to be selected 
     */
    public static void remove( JTable list ) {
        ListSelectionModel sm = list.getSelectionModel();
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
        ((ClasspathTableModel) list.getModel()).fireTableRowsDeleted(elements.size(), n0 - 1);

        if (index >= n) {
            index = n - 1;
        }
        sm.setSelectionInterval(index, index);
    }
    
    public static void addLibraries(Library[] libraries, Set/*<Library>*/ alreadyIncludedLibs, JTable table) {
        if (libraries.length > 0) {   
            List newLibList = new ArrayList(Arrays.asList(libraries));
            table.clearSelection();
            int n0 = data.length;
            for (int i = 0; i < n0; i++) {
                ClassPathSupport.Item item = (ClassPathSupport.Item) data[i][0];
                if(item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                    if(newLibList.remove(item.getObject()))
                        table.addRowSelectionInterval(i, i);
                }
            }
            int n = newLibList.size();
            if (n > 0) {
                Object[][] newData = new Object[n0 + n][2];
                for (int i = 0; i < n0; i++)
                    newData[i] = data[i];
                for (int i = 0; i < n; i++) {
                    newData[n0 + i][0] = ClassPathSupport.Item.create((Library) newLibList.get(i), null, ClassPathSupport.Item.PATH_IN_WAR_APPLET);
                    newData[n0 + i][1] = ClassPathSupport.Item.PATH_IN_WAR_APPLET;
                }

                data = newData;
                ((ClasspathTableModel) table.getModel()).fireTableRowsInserted(n0, n0 + n - 1);
                table.addRowSelectionInterval(n0, n0 + n - 1);
            }
        }
    }

    public static void addJarFiles(File files[], ClasspathTableModel tableModel) {
        Object[][] newData = new Object[data.length + files.length][2];
        for (int i = 0; i < data.length; i++)
            newData[i] = data[i];
        for (int i = 0; i < files.length; i++) {
            newData[data.length + i][0] = ClassPathSupport.Item.create (files[i], null, ClassPathSupport.Item.PATH_IN_WAR_APPLET);
            newData[data.length + i][1] = ClassPathSupport.Item.PATH_IN_WAR_APPLET;
        }
        
        data = newData;
        tableModel.fireTableRowsInserted(data.length, data.length + files.length - 1);
    }
    
    public static void addArtifacts(AntArtifactChooser.ArtifactItem artifactItems[], ClasspathTableModel tableModel) {
        Object[][] newData = new Object[data.length + artifactItems.length][2];
        for (int i = 0; i < data.length; i++)
            newData[i] = data[i];
        for (int i = 0; i < artifactItems.length; i++) {
            newData[data.length + i][0] = ClassPathSupport.Item.create (artifactItems[i].getArtifact(), artifactItems[i].getArtifactURI(), null, ClassPathSupport.Item.PATH_IN_WAR_APPLET);
            newData[data.length + i][1] = ClassPathSupport.Item.PATH_IN_WAR_APPLET;
        }
        
        data = newData;
        tableModel.fireTableRowsInserted(data.length, data.length + artifactItems.length - 1);
    }
    
    static class ClasspathTableModel extends AbstractTableModel {
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
