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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.util.TreePath;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.plugins.EncapsulateFieldRefactoringPlugin;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring.EncapsulateFieldInfo;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;

/**
 * Panel used by Encapsulate Field refactoring. Contains components to
 * set parameters for the refactoring.
 *
 * @author  Pavel Flaska
 */
public final class EncapsulateFieldPanel extends JPanel implements CustomRefactoringPanel {
    
    private DefaultTableModel model;
    private TreePathHandle selectedObjects;
    private ChangeListener parent;
    private String classname;
    private String[][] methodNames; // array of String {getterName, setterName}
    
    private static final String modifierNames[] = {
        "public", // NOI18N
        "protected", // NOI18N
        "<default>", // NOI18N
        "private" // NOI18N
    };
    
    private static final String[] columnNames = {
        getString("LBL_ColField"),  // NOI18N
        "    ", // NOI18N 
        getString("LBL_ColGetter"), // NOI18N
        "    ", // NOI18N 
        getString("LBL_ColSetter")  // NOI18N
    };
    
    // modifier items in combo - indexes
    private static final int MOD_PUBLIC_INDEX = 0;
    private static final int MOD_PROTECTED_INDEX = 1;
    private static final int MOD_DEFAULT_INDEX = 2;
    private static final int MOD_PRIVATE_INDEX = 3;

    private static final Class[] columnTypes = new Class[] {
        MemberInfo.class, java.lang.Boolean.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class
    };
    
    /** 
     * Creates new form EncapsulateFieldPanel.
     *
     * @param selectedObjects  array of selected objects
     */
    public EncapsulateFieldPanel(TreePathHandle selectedObject, ChangeListener parent) {
        String title = getString("LBL_TitleEncapsulateFields");
        
        this.selectedObjects = selectedObject;
        this.parent = parent;
        model = new TabM(columnNames, 0);
        initComponents();
        setName(title);
        // *** initialize table
        // set renderer for the column "Field" to display name of the feature (with icon)
        jTableFields.setDefaultRenderer(MemberInfo.class, new UIUtilities.JavaElementTableCellRenderer());
        // set background color of the scroll pane to be the same as the background
        // of the table
        jScrollField.setBackground(jTableFields.getBackground());
        jScrollField.getViewport().setBackground(jTableFields.getBackground());
        // set default row height
        jTableFields.setRowHeight(18);
        // set grid color to be consistent with other netbeans tables
        if (UIManager.getColor("control") != null) { // NOI18N
            jTableFields.setGridColor(UIManager.getColor("control")); // NOI18N
        }
    }

    public Component getComponent() {
        return this;
    }
    
    private boolean initialized = false;
    public void initialize() {
        if (initialized)
            return ;
        
        JavaSource js = JavaSource.forFileObject(selectedObjects.getFileObject());
        try {
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController javac) throws Exception {
                    javac.toPhase(JavaSource.Phase.RESOLVED);
                    initialize(javac);
                }
            } , true);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
        
    public void initialize(CompilationController javac) {
        TreePath selectedPath = selectedObjects.resolve(javac);
        Element selectedElm = javac.getTrees().getElement(selectedPath);
        List<String[]> names = new ArrayList<String[]>();
        
        for (VariableElement field : initFields(selectedPath, javac)) {
            TreePath fieldTPath = javac.getTrees().getPath(field);
            boolean createGetter = selectedElm == field;
            boolean createSetter = createGetter && !field.getModifiers().contains(Modifier.FINAL);
            String[] getset = new String[] {
                EncapsulateFieldRefactoringPlugin.computeGetterName(field),
                EncapsulateFieldRefactoringPlugin.computeSetterName(field)};
            names.add(getset);
            model.addRow(new Object[] { 
                MemberInfo.create(fieldTPath, javac),
                createGetter ? Boolean.TRUE : Boolean.FALSE,                        
                createGetter ? getset[0]:null,
                createSetter ? Boolean.TRUE : Boolean.FALSE,                        
                createSetter ? getset[1]:null
            });
        }
        
        this.methodNames = names.toArray(new String[names.size()][]);

        packRows(jTableFields);
        
        jTableFields.getTableHeader().setReorderingAllowed(false);
        setColumnWidth(1);
        setColumnWidth(3);

        jTableFields.invalidate();
        jTableFields.repaint();
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                int col = e.getColumn();
                int row = e.getFirstRow();
                if (col == 1 || col==3 ) {
                    Boolean value = (Boolean) model.getValueAt(row, col);
                    if (value.booleanValue()) {
                        if (col==1) {
                            model.setValueAt(methodNames[row][0], row, col+1);
                        } else {
                            model.setValueAt(methodNames[row][1], row, col+1);
                        }
                    } else {
                        if (!(model.getValueAt(row, col+1)==null))
                            model.setValueAt(null, row, col+1);
                    }
                } else {
                    String value = (String) model.getValueAt(row, col);
                    if (value == null | "".equals(value)) {
                        model.setValueAt(Boolean.FALSE, row, col-1);
                    }
                }
                parent.stateChanged(null);
            }
        });
        
        initialized = true;
    }
    
    private void setColumnWidth(int a) {
        TableColumn col = jTableFields.getColumnModel().getColumn(a);
        JCheckBox box = new JCheckBox();
        int width = (int) box.getPreferredSize().getWidth();
        col.setPreferredWidth(width);
        col.setMinWidth(width);
        col.setMaxWidth(width);
        col.setResizable(false);        
    }
    
    private int getMinimumRowHeight(JTable table, int rowIndex) {
        int height = table.getRowHeight();
        
        for (int c=0; c<table.getColumnCount(); c++) {
            TableCellRenderer renderer = table.getCellRenderer(rowIndex, c);
            Component comp = table.prepareRenderer(renderer, rowIndex, c);
            int h = comp.getMinimumSize().height;
            height = Math.max(height, h);
        }
        return height;
    }
    
    private void packRows(JTable table) {
        int max = 0;
        int h;
        for (int r=0; r<table.getRowCount(); r++) {
            h = getMinimumRowHeight(table, r);
            if (h>max)
                max=h;
        }
        table.setRowHeight(max);
        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getPreferredSize().width, max));
    }
    
    /**
     * Returns table model with data provided by user.
     *
     * @return  data provided in table by user
     */
    protected DefaultTableModel getTableModel() {
        return model;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLblTitle = new javax.swing.JLabel();
        jScrollField = new javax.swing.JScrollPane();
        jTableFields = new javax.swing.JTable();
        jLblFieldVis = new javax.swing.JLabel();
        jComboField = new javax.swing.JComboBox();
        jLblAccessVis = new javax.swing.JLabel();
        jComboAccess = new javax.swing.JComboBox();
        jCheckAccess = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        jLblTitle.setLabelFor(jTableFields);
        org.openide.awt.Mnemonics.setLocalizedText(jLblTitle, getString("LBL_FieldList"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(jLblTitle, gridBagConstraints);

        jScrollField.setPreferredSize(new java.awt.Dimension(300, 200));
        jTableFields.setModel(model);
        jScrollField.setViewportView(jTableFields);
        jTableFields.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle").getString("ACSD_jTableFields"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 12, 2);
        add(jScrollField, gridBagConstraints);

        jLblFieldVis.setLabelFor(jComboField);
        org.openide.awt.Mnemonics.setLocalizedText(jLblFieldVis, getString("LBL_FieldVis"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 5);
        add(jLblFieldVis, gridBagConstraints);

        jComboField.setModel(new DefaultComboBoxModel(modifierNames));
        jComboField.setSelectedIndex(3);
        jComboField.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jComboField, gridBagConstraints);
        jComboField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle").getString("ACSD_fieldModifiers"));

        jLblAccessVis.setLabelFor(jComboAccess);
        org.openide.awt.Mnemonics.setLocalizedText(jLblAccessVis, getString("LBL_AccessVis"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 5);
        add(jLblAccessVis, gridBagConstraints);

        jComboAccess.setModel(new DefaultComboBoxModel(modifierNames));
        jComboAccess.setSelectedIndex(0);
        jComboAccess.setPreferredSize(new java.awt.Dimension(100, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jComboAccess, gridBagConstraints);
        jComboAccess.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle").getString("ACSD_methodAcc"));

        jCheckAccess.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jCheckAccess, getString("LBL_AccessorsEven"));
        // NOI18N
        jCheckAccess.setMargin(new java.awt.Insets(12, 2, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        add(jCheckAccess, gridBagConstraints);
        jCheckAccess.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle").getString("ACSD_useAccessors"));

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckAccess;
    private javax.swing.JComboBox jComboAccess;
    private javax.swing.JComboBox jComboField;
    private javax.swing.JLabel jLblAccessVis;
    private javax.swing.JLabel jLblFieldVis;
    private javax.swing.JLabel jLblTitle;
    private javax.swing.JScrollPane jScrollField;
    private javax.swing.JTable jTableFields;
    // End of variables declaration//GEN-END:variables

    private static String getString(String key) {
        return NbBundle.getMessage(EncapsulateFieldPanel.class, key);
    }

    /**
     * Returns the array of all fields from class which contains
     * selectedField provided as a parameter.
     *
     * @param   selectedField field, whose class is used for obtaining
     *                        array of fields.
     * @return  array of all fields in a class.
     */
    private List<VariableElement> initFields(TreePath selectedField, CompilationInfo javac) {
        Element elm = javac.getTrees().getElement(selectedField);
        TypeElement encloser = null;
        if (ElementKind.FIELD == elm.getKind()) {
            encloser = (TypeElement) elm.getEnclosingElement();
        } else {
            encloser = (TypeElement) elm;
        }
        
        List<VariableElement> result = new ArrayList<VariableElement>();
        for (Element member : encloser.getEnclosedElements()) {
            if (ElementKind.FIELD == member.getKind()) {
                result.add((VariableElement) member);
            }
        }
        
        this.classname = encloser.getQualifiedName().toString();
        final String title = " - " + classname; // NOI18N
        setName(getName() + title);
        
        return result;
    }
    
    public Collection<EncapsulateFieldInfo> getAllFields() {
        List<EncapsulateFieldInfo> result = new ArrayList<EncapsulateFieldInfo>();
        List rows = model.getDataVector();
        for (Iterator rowIt = rows.iterator(); rowIt.hasNext();) {
            List row = (List) rowIt.next();
            if (row.get(1) == Boolean.TRUE || row.get(3) == Boolean.TRUE) {
                String getterName = (String) row.get(2);
                String setterName = (String) row.get(4);
                MemberInfo mi = (MemberInfo) row.get(0);
                result.add(new EncapsulateFieldInfo(
                        (TreePathHandle) mi.getElementHandle(),
                        "".equals(getterName)?null:getterName, // NOI18N
                        "".equals(setterName)?null:setterName)); // NOI18N
            }
        }

        return result;
    }
    
    public boolean isCheckAccess() {
        return jCheckAccess.isSelected();
    }
    
    public Set<Modifier> getFieldModifiers() {
        return Collections.singleton(getModifier(jComboField.getSelectedIndex()));
    }
    
    public Set<Modifier> getMethodModifiers() {
        return Collections.singleton(getModifier(jComboAccess.getSelectedIndex()));
    }

    private Modifier getModifier(int index) {
        switch (index) {
            case MOD_PRIVATE_INDEX: 
                return Modifier.PRIVATE;
            case MOD_DEFAULT_INDEX: 
                return null; /* no modifier */
            case MOD_PROTECTED_INDEX: 
                return Modifier.PROTECTED;
            case MOD_PUBLIC_INDEX:
                return Modifier.PUBLIC;
        }
        throw new IllegalStateException("unknown index: " + index); // NOI18N
    }

    String getClassname() {
        return classname;
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The class is used by EncapsulateFieldPanel - it represents table model
     * used inside in jTable. It denies to edit first column, returns the
     * column classes (Boolean, String, String, String) etc.
     */
    private static class TabM extends DefaultTableModel {
        
        public TabM(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }
        
        /**
         * Returns the appropriate class for column.
         *
         * @param  columnIndex  index of column for which we are looking for a class
         * @return  class which is used in the column
         */
        @Override
        public Class getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
        }

        /**
         * We deny edit the field column (index 1), because field can't
         * be renamed when we encapsulate it.
         *
         * @param  row  doesn't matter
         * @param  column  for value 1, it returns false, otherwise true
         *
         * @return  true, if the cell is editable
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 0)
                return false;
            if (column == 1 || column == 3)
                return true;
            return ((Boolean) getValueAt(row, column-1)).booleanValue();
        }
    }
    // end INNER CLASSES
}
