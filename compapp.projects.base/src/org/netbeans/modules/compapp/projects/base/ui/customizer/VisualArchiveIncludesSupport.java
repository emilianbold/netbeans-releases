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

package org.netbeans.modules.compapp.projects.base.ui.customizer;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.projects.base.IcanproConstants;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Handles adding and removing of additional war content.
 *
 * @author 
 * @version 
 */

public final class VisualArchiveIncludesSupport {
    /**
     * DOCUMENT ME!
     */
   // static final String JBIPROJ_JAR_LOC = "/org-netbeans-modules-compapp-projects-jbi.jar";
    private IcanproProjectProperties webProperties;
    
    /**
     * DOCUMENT ME!
     */
    final Project master;
    
    /**
     * DOCUMENT ME!
     */
    final JTable classpathTable;
    
    /**
     * DOCUMENT ME!
     */
    final JButton addArtifactButton;
    
    /**
     * DOCUMENT ME!
     */
    final JButton removeButton;
    
    /**
     * DOCUMENT ME!
     */
    //final JTable jTableComp;
    
    /**
     * DOCUMENT ME!
     */
    //final JButton jButtonUpdate;
    
    /**
     * DOCUMENT ME!
     */
    final JButton jButtonConfig;
    private final VisualArchiveIncludesSupport.ClasspathTableModel classpathModel;
    private Object[][] data;
    private final List<ActionListener> actionListeners = new ArrayList<ActionListener>();
//    private ComponentTableModel mTableModel;
//    private ComponentTableRenderer mTableRenderer;
    private Vector mColumnNames;
    private String nbuser;
    private String compFilename;
    private String compFileSrc;
    private String compFileDst;
    private String jbiFilename;
    private String jbiFileLoc;
    private JComboBox comboTarget = null;
    private DefaultComboBoxModel comboModel = null;
    private Vector comboValues = new Vector();
    private Vector bindingList = null; // new Vector();
    private AntArtifact bcjar = null;
    private String mModuleDir = null;
    
    
    
    /**
     * Creates a new VisualArchiveIncludesSupport object.
     *
     * @param webProperties DOCUMENT ME!
     * @param jTableComp DOCUMENT ME!
     * @param classpathTable DOCUMENT ME!
     * @param jButtonUpdate DOCUMENT ME!
     * @param jButtonConfig DOCUMENT ME!
     * @param addArtifactButton DOCUMENT ME!
     * @param removeButton DOCUMENT ME!
     */
    public VisualArchiveIncludesSupport(
            IcanproProjectProperties webProperties, /*JTable jTableComp, */JTable classpathTable,
            /*JButton jButtonUpdate,*/ JButton jButtonConfig, JButton addArtifactButton,
            JButton removeButton
            ) {
        // Remember all buttons
        this.webProperties = webProperties;
        this.jButtonConfig = jButtonConfig;
        this.jButtonConfig.setEnabled(false);
             
        this.classpathTable = classpathTable;
        this.classpathModel = new VisualArchiveIncludesSupport.ClasspathTableModel();
        this.classpathTable.setModel(classpathModel);
        this.classpathTable.getColumnModel().getColumn(0).setHeaderValue(
                NbBundle.getMessage(VisualArchiveIncludesSupport.class, "TXT_Archive_Item")
                );
        this.classpathTable.getColumnModel().getColumn(1).setHeaderValue(
                NbBundle.getMessage(VisualArchiveIncludesSupport.class, "TXT_Archive_PathInArchive")
                );
        this.classpathTable.getColumnModel().getColumn(0).setCellRenderer(
                new VisualArchiveIncludesSupport.ClassPathCellRenderer()
                );      
        this.classpathTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        this.addArtifactButton = addArtifactButton;
        this.removeButton = removeButton;
        this.master = webProperties.getProject();
        
        // Register the listeners
        VisualArchiveIncludesSupport.ClasspathSupportListener csl = new VisualArchiveIncludesSupport.ClasspathSupportListener();
        
        // On all buttons
        this.jButtonConfig.addActionListener(csl);
        this.addArtifactButton.addActionListener(csl);
        this.removeButton.addActionListener(csl);
        
        // On list selection
        classpathTable.getSelectionModel().addListSelectionListener(csl);
        classpathModel.addTableModelListener(csl);
        
        // Set the initial state of the buttons
        csl.valueChanged(null);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param items DOCUMENT ME!
     */
    public void setVisualWarItems(List items) {
        Object[][] data = new Object[items.size()][2];
        this.data = data;
        
        for (int i = 0; i < items.size(); i++) {
            VisualClassPathItem vi = (VisualClassPathItem) items.get(i);
            classpathModel.setValueAt(vi, i, 0);
            classpathModel.setValueAt("", i, 1);
        }
        
        classpathModel.fireTableDataChanged();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public List getVisualWarItems() {
        ArrayList items = new ArrayList();
        
        for (int i = 0; i < data.length; i++)
            items.add((VisualClassPathItem) classpathModel.getValueAt(i, 0));
        
        return items;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param tml DOCUMENT ME!
     */
    public void addTableModelListener(TableModelListener tml) {
        classpathModel.addTableModelListener(tml);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param tml DOCUMENT ME!
     */
    public void removeTableModelListener(TableModelListener tml) {
        classpathModel.removeTableModelListener(tml);
    }
    
    /**
     * Action listeners will be informed when the value of the list changes.
     *
     * @param listener DOCUMENT ME!
     */
    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param listener DOCUMENT ME!
     */
    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }
    
    private void fireActionPerformed() {
        List<ActionListener> listeners;
        
        synchronized (this) {
            listeners = new ArrayList<ActionListener>(actionListeners);
        }
        
        ActionEvent ae = new ActionEvent(this, 0, null);
        
        for (Iterator<ActionListener> it = listeners.iterator(); it.hasNext();) {
            ActionListener al = it.next();
            al.actionPerformed(ae);
        }
    }
    
    // Private methods ---------------------------------------------------------
    private void addArtifacts(AntArtifact[] artifacts) {
        Object[][] newData = new Object[data.length + artifacts.length][2];
        
        for (int i = 0; i < data.length; i++)
            newData[i] = data[i];
        
        for (int i = 0; i < artifacts.length; i++) {
            VisualClassPathItem vi = new VisualClassPathItem(
                    artifacts[i], VisualClassPathItem.TYPE_ARTIFACT, null,
                    artifacts[i].getArtifactLocations()[0].toString(), true
                    );
            
            newData[data.length + i][0] = vi;
            newData[data.length + i][1]=artifacts[i].getType(); //tbd my test
     //       newData[data.length + i][1] = getDefaultTarget(vi.getAsaType());
        }
        
        data = newData;
        classpathModel.fireTableRowsInserted(data.length, (data.length + artifacts.length) - 1);
        
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
    
    private String parseTargetID(String str) {
        if (str != null) {
            int i = str.indexOf(" [");
            int j = str.lastIndexOf(']');
            
            if ((i > 0) && (j > 0)) {
                return str.substring(i + 2, j);
            }
        }
        
        return null;
    }
    
    private String getDefaultTarget(String type) {
        int tsize = comboValues.size();
        
        for (int i = 0; i < tsize; i++) {
            String val = (String) comboValues.get(i);
            
            if (val.startsWith(type)) {
                return val;
            }
        }
        
        return "";
    }
    private void updateComboTarget() {
        comboModel.removeAllElements();
        comboModel.addElement(" ");
        
        for (int i = 0; i < comboValues.size(); i++) {
            comboModel.addElement(comboValues.get(i));
        }
    }
    
    private void updateComboTargetWithType(String type) {
        comboModel.removeAllElements();
        comboModel.addElement(" ");
        
        if ((type == null) || (type.length() < 1)) {
            return;
        }
        
        for (int i = 0; i < comboValues.size(); i++) {
            String val = (String) comboValues.get(i);
            
            if (val.startsWith(type)) {
                comboModel.addElement(val);
            }
        }
    }

    private void updateModels(String jar, String uuid, String desc, String cid) {
        for (int i = 0, size = classpathModel.getRowCount(); i < size; i++) {
            VisualClassPathItem vi = (VisualClassPathItem) classpathModel.getValueAt(i, 0);
//            System.out.println(vi);
        } //tbd temp add
    }
    
  
    /**
     * DOCUMENT ME!
     */
    public void initTableValues() {
// tbd
    }
       
    private void updateAsaTarget() {
        for (int i = 0, size = classpathModel.getRowCount(); i < size; i++) {
            VisualClassPathItem vi = (VisualClassPathItem) classpathModel.getValueAt(i, 0);
            String tid = (String) classpathModel.getValueAt(i, 1);
            
            if ((tid == null) || (tid.trim().length() < 1)) {
                // not set yet.. default to the first non-blank traget on the list
                //tbd temp add classpathModel.setValueAt(getDefaultTarget(vi.getAsaType()), i, 1);
                classpathModel.setValueAt("test", i, 1);
            }
        }
    }
    
    private String getCompList(VisualArchiveIncludesSupport.ClasspathTableModel classpathModel) {
        String val = "";
        
        for (int i = 0; i < classpathModel.getRowCount(); i++) {
            if (i > 0) {
                val += ";";
            }
            
            //String targetID =  parseTargetID((String) classpathModel.getValueAt(i, 1));
            String targetID = (String) classpathModel.getValueAt(i, 1);
            val += (((targetID == null) || (targetID.length() < 1)) ? "null" : targetID);
        }
        
        return val;
    }
        
    private class ClasspathSupportListener implements ActionListener, ListSelectionListener,
            TableModelListener {
        // Implementation of ActionListener ------------------------------------
        
        /**
         * Handles button events
         *
         * @param e DOCUMENT ME!
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            
            if (source == addArtifactButton) {
                AntArtifact[] artifacts = AntArtifactChooser.showDialog(
                        IcanproConstants.ARTIFACT_TYPE_EJB_WS, master
                        );
                
                if (artifacts != null) {
                    addArtifacts(artifacts);
                }
            } else if (source == removeButton) {
                removeElements();
            } else if (source == jButtonConfig) {
                // removeElements();
            }
        }
        
        // ListSelectionModel --------------------------------------------------
        
        /**
         * Handles changes in the selection
         *
         * @param e DOCUMENT ME!
         */
        public void valueChanged(ListSelectionEvent e) {
            DefaultListSelectionModel sm = (DefaultListSelectionModel) classpathTable.getSelectionModel();
            int index = sm.getMinSelectionIndex();
            
            // remove enabled only if selection is not empty
            boolean remove = index != -1;
            
            // and when the selection does not contain unremovable item
            if (remove) {
                VisualClassPathItem vcpi = (VisualClassPathItem) classpathModel.getValueAt(
                        index, 0
                        );
                
                if (!vcpi.canDelete()) {
                    remove = false;
                }
            }
            
            removeButton.setEnabled(remove);
        }
        
        // TableModelListener --------------------------------------
        public void tableChanged(TableModelEvent e) {
//            webProperties.put(
//                    IcanproProjectProperties.JBI_CONTENT_COMPONENT, getCompList(classpathModel)
//                    );
            
            if (e.getColumn() == 1) {
                //VisualClassPathItem cpItem = (VisualClassPathItem) classpathModel.getValueAt(e.getFirstRow(), 0);
                // cpItem.setPathInWAR((String) classpathModel.getValueAt(e.getFirstRow(), 1));
                fireActionPerformed();
            }
        }
    }
    
    private static class ClassPathCellRenderer extends DefaultTableCellRenderer {
        /**
         * DOCUMENT ME!
         *
         * @param table DOCUMENT ME!
         * @param value DOCUMENT ME!
         * @param isSelected DOCUMENT ME!
         * @param hasFocus DOCUMENT ME!
         * @param row DOCUMENT ME!
         * @param column DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
                ) {
            if (value instanceof VisualClassPathItem) {
                final VisualClassPathItem item = (VisualClassPathItem) value;
                setIcon(item.getIcon());
            }
            
            final String s = (value == null) ? null : value.toString();
            
            return super.getTableCellRendererComponent(table, s, isSelected, false, row, column);
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @author 
     * @version 
     */
    class ClasspathTableModel extends AbstractTableModel {
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int getColumnCount() {
            return 2; //classpath item name, item location within WAR
        }
        
        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int getRowCount() {
            if (data == null) {
                return 0;
            }
            
            return data.length;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param row DOCUMENT ME!
         * @param col DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param row DOCUMENT ME!
         * @param col DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean isCellEditable(int row, int col) {
            if (col == 1) {
                return true;
            } else {
                return false;
            }
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param value DOCUMENT ME!
         * @param row DOCUMENT ME!
         * @param col DOCUMENT ME!
         */
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }
    
    private class TargetComboBoxEditor extends DefaultCellEditor {
        /**
         * DOCUMENT ME!
         */
        JComboBox combo = null;
        
        /**
         * Creates a new TargetComboBoxEditor object.
         *
         * @param combo DOCUMENT ME!
         */
        public TargetComboBoxEditor(JComboBox combo) {
            super(combo);
            this.combo = combo;
        }
        
        /**
         * DOCUMENT ME!
         *
         * @param table DOCUMENT ME!
         * @param value DOCUMENT ME!
         * @param isSelected DOCUMENT ME!
         * @param row DOCUMENT ME!
         * @param column DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column
                ) {
            String type = null;
            VisualClassPathItem vi = (VisualClassPathItem) classpathModel.getValueAt(row, 0);
            
            if (vi != null) {
                //type = vi.getAsaType();
                type = "test";
            }
            
            updateComboTargetWithType(type);
            
            return combo;
        }
    }
}
