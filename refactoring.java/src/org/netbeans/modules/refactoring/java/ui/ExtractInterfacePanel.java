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
package org.netbeans.modules.refactoring.java.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;

/** UI panel for collecting refactoring parameters.
 *
 * @author Martin Matula, Jan Becicka
 */
public class ExtractInterfacePanel extends CustomRefactoringPanel {
    // helper constants describing columns in the table of members
    private static final String[] COLUMN_NAMES = {"LBL_Selected", "LBL_ExtractInterface_Member"}; // NOI18N
    private static final Class[] COLUMN_CLASSES = {Boolean.class, TreePathHandle.class};
    
    // refactoring this panel provides parameters for
    private final ExtractInterfaceRefactoring refactoring;
    // table model for the table of members
    private final TableModel tableModel;
    // data for the members table (first dimension - rows, second dimension - columns)
    // the columns are: 0 = Selected (true/false), 1 = Member (Java element)
    private Object[][] members = new Object[0][0];
    
    /** Creates new form ExtractInterfacePanel
     * @param refactoring The refactoring this panel provides parameters for.
     */
    public ExtractInterfacePanel(ExtractInterfaceRefactoring refactoring, final ChangeListener parent) {
        this.refactoring = refactoring;
        this.tableModel = new TableModel();
        initComponents();
        setPreferredSize(new Dimension(420, 380));
        String defaultName = "NewInterface"; //NOI18N
        nameText.setText(defaultName); 
        nameText.setSelectionStart(0);
        nameText.setSelectionEnd(defaultName.length());
        nameText.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent event) {
                parent.stateChanged(null);
            }
            public void insertUpdate(DocumentEvent event) {
                parent.stateChanged(null);
            }
            public void removeUpdate(DocumentEvent event) {
                parent.stateChanged(null);
            }
        });
    }
    
    public void requestFocus() {
        super.requestFocus();
        nameText.requestFocus();
    }
    

    /** Initialization of the panel (called by the parent window).
     */
    public void initialize() {
        // *** initialize table
        // set renderer for the second column ("Member") do display name of the feature
        membersTable.setDefaultRenderer(COLUMN_CLASSES[1], new UIUtilities.JavaElementTableCellRenderer() {
            // override the extractText method to add "implements " prefix to the text
            // in case the value is instance of MultipartId (i.e. it represents an interface
            // name from implements clause)
            protected String extractText(Object value) {
                String displayValue = super.extractText(value);
//                if (value instanceof MultipartId) {
//                    displayValue = "implements " + displayValue; // NOI18N
//                }
                return displayValue;
            }
        });
        // set background color of the scroll pane to be the same as the background
        // of the table
        scrollPane.setBackground(membersTable.getBackground());
        scrollPane.getViewport().setBackground(membersTable.getBackground());
        // set default row height
        membersTable.setRowHeight(18);
        // set grid color to be consistent with other netbeans tables
        if (UIManager.getColor("control") != null) { // NOI18N
            membersTable.setGridColor(UIManager.getColor("control")); // NOI18N
        }
        // compute and set the preferred width for the first and the third column
        UIUtilities.initColumnWidth(membersTable, 0, Boolean.TRUE, 4);
    }
    
    // --- GETTERS FOR REFACTORING PARAMETERS ----------------------------------
    
    /** Getter used by the refactoring UI to get value
     * of interface name.
     * @return Interface name.
     */
    public String getIfcName() {
        return nameText.getText();
    }
    
    /** Getter used by the refactoring UI to get members to be extracted.
     * @return Members to be extracted.
     */
    public TreePathHandle[] getMembers() {
        List list = new ArrayList();
        // go through all rows of a table and collect selected members
        for (int i = 0; i < members.length; i++) {
            if (members[i][0].equals(Boolean.TRUE)) {
                list.add(members[i][1]);
            }
        }
        // return the array of selected members
        return (TreePathHandle[]) list.toArray(new TreePathHandle[list.size()]);
    }
    
    // --- GENERATED CODE ------------------------------------------------------
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        chooseLabel = new javax.swing.JLabel();
        nameText = new javax.swing.JTextField();
        scrollPane = new javax.swing.JScrollPane();
        membersTable = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setLayout(new java.awt.BorderLayout());

        namePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        namePanel.setLayout(new java.awt.BorderLayout(12, 0));

        nameLabel.setLabelFor(nameText);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "LBL_ExtractInterface_Name")); // NOI18N
        namePanel.add(nameLabel, java.awt.BorderLayout.WEST);
        nameLabel.getAccessibleContext().setAccessibleName(null);
        nameLabel.getAccessibleContext().setAccessibleDescription(null);

        chooseLabel.setLabelFor(membersTable);
        org.openide.awt.Mnemonics.setLocalizedText(chooseLabel, org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "LBL_ExtractInterfaceLabel")); // NOI18N
        chooseLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        namePanel.add(chooseLabel, java.awt.BorderLayout.SOUTH);
        namePanel.add(nameText, java.awt.BorderLayout.CENTER);

        add(namePanel, java.awt.BorderLayout.NORTH);

        membersTable.setModel(tableModel);
        membersTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        scrollPane.setViewportView(membersTable);
        membersTable.getAccessibleContext().setAccessibleName(null);
        membersTable.getAccessibleContext().setAccessibleDescription(null);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel chooseLabel;
    private javax.swing.JTable membersTable;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel namePanel;
    private javax.swing.JTextField nameText;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    
    // --- MODELS --------------------------------------------------------------
    
    /** Model for the members table.
     */
    private class TableModel extends AbstractTableModel {
        TableModel() {
            initialize();
        }
        
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        public String getColumnName(int column) {
            return UIUtilities.getColumnName(NbBundle.getMessage(ExtractInterfacePanel.class, COLUMN_NAMES[column]));
        }

        public Class getColumnClass(int columnIndex) {
            return COLUMN_CLASSES[columnIndex];
        }

        public int getRowCount() {
            return members.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return members[rowIndex][columnIndex];
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            members[rowIndex][columnIndex] = value;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // column 0 is always editable, column 1 is never editable
            return columnIndex == 0;
        }
        

        private void initialize() {
//            TreePathHandle sourceType = refactoring.getSourceType();
//            if (sourceType == null) return;
//            
//            ArrayList result = new ArrayList();
//            
//            for (Iterator it = sourceType.getInterfaceNames().iterator(); it.hasNext();) {
//                result.add(it.next());
//            }
//            // collect fields, methods and inner classes
//            Feature[] features = (Feature[]) sourceType.getFeatures().toArray(new Feature[0]);
//            for (int j = 0; j < features.length; j++) {
//                if (refactoring.acceptFeature(features[j])) {
//                    result.add(features[j]);
//                }
//            }
//            // the members are collected
//            // now, create a tree map (to sort them) and create the table data
//            Collections.sort(result, new Comparator() {
//                public int compare(Object o1, Object o2) {
//                    NamedElement ne1 = (NamedElement) o1, ne2 = (NamedElement) o2;
//                    // elements are sorted primarily by their class name
//                    int result = ne1.getClass().getName().compareTo(ne2.getClass().getName());
//                    if (result == 0) {
//                        // then by their display text
//                        result = UIUtilities.getDisplayText(ne1).compareTo(UIUtilities.getDisplayText(ne2));
//                    }
//                    if (result == 0) {
//                        // then the mofid is compared (to not take two non-identical
//                        // elements as equals)
//                        result = ne1.refMofId().compareTo(ne2.refMofId());
//                    }
//                    return result;
//                }
//            });
//            members = new Object[result.size()][2];
//            for (int i = 0; i < members.length; i++) {
//                members[i][0] = Boolean.FALSE;
//                members[i][1] = result.get(i);
//            }
//            // fire event to repaint the table
//            this.fireTableDataChanged();
        }
    }
}
