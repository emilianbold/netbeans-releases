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

import java.awt.Component;
import java.awt.Dimension;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.NbBundle;


/** UI panel for collecting refactoring parameters.
 *
 * @author Martin Matula
 */
public class PullUpPanel extends JPanel implements CustomRefactoringPanel {
    // helper constants describing columns in the table of members
    private static final String[] COLUMN_NAMES = {"LBL_PullUp_Selected", "LBL_PullUp_Member", "LBL_PullUp_MakeAbstract"}; // NOI18N
    private static final Class[] COLUMN_CLASSES = {Boolean.class, TreePathHandle.class, Boolean.class};
    
    // refactoring this panel provides parameters for
    private final PullUpRefactoring refactoring;
    // table model for the table of members
    private final TableModel tableModel;
    // pre-selected members (comes from the refactoring action - the elements
    // that should be pre-selected in the table of members)
    private Set selectedMembers;
    // target type to move the members to
    private TreePathHandle targetType;
    // data for the members table (first dimension - rows, second dimension - columns)
    // the columns are: 0 = Selected (true/false), 1 = Member (Java element), 2 = Make Abstract (true/false)
    private Object[][] members = new Object[0][0];
    
    /** Creates new form PullUpPanel
     * @param refactoring The refactoring this panel provides parameters for.
     * @param selectedMembers Members that should be pre-selected in the panel
     *      (determined by which nodes the action was invoked on - e.g. if it was
     *      invoked on a method, the method will be pre-selected to be pulled up)
     */
    public PullUpPanel(PullUpRefactoring refactoring, Set selectedMembers, final ChangeListener parent) {
        this.refactoring = refactoring;
        this.tableModel = new TableModel();
        this.selectedMembers = selectedMembers;
        initComponents();
        setPreferredSize(new Dimension(420, 380));
        membersTable.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                parent.stateChanged(null);
            }
        });
    }

    /** Initialization of the panel (called by the parent window).
     */
    public void initialize() {
//        // retrieve supertypes (will be used in the combo)
//        JavaClass[] supertypes = refactoring.collectSupertypes();
//        
//        // *** initialize combo
//        // set renderer for the combo (to display name of the class)
//        supertypeCombo.setRenderer(new UIUtilities.JavaElementListCellRenderer() {
//            /** Returns display text of the class. The text is returned in the
//             * following format: SimpleName (package.name). If the class is an inner
//             * class the text is: Outer.SimpleName (package.name).
//             */
//            protected String extractText(Object value) {
//                // the value is always an instance of JavaClass
//                JavaClass topLevel = (JavaClass) value;
//                Object current;
//                // iterate up through the parents to find the top-level class
//                while ((current = topLevel.refImmediateComposite()) instanceof JavaClass) {
//                    topLevel = (JavaClass) current;
//                }
//                // derive the package name by subtracting the simple name of top-level class
//                // from the fully qualified name of the top-level class
//                String packageName = topLevel.getName();
//                packageName = packageName.substring(0, packageName.length() - topLevel.getSimpleName().length());
//                // now, get the class name by subtracting the package name from the class FQN
//                String className = ((JavaClass) value).getName().substring(packageName.length());
//                // remove the ending dot from the package name and surrond it by parentheses
//                if (packageName.length() > 0) {
//                    packageName = " (" + packageName.substring(0, packageName.length() - 1) + ")"; // NOI18N
//                }
//                // create the displayText (concatenate the class name and package name)
//                return className.concat(packageName);
//            }
//        });
//        // set combo model
//        supertypeCombo.setModel(new ComboModel(supertypes));
//        
//        // *** initialize table
//        // set renderer for the second column ("Member") do display name of the feature
//        membersTable.setDefaultRenderer(COLUMN_CLASSES[1], new UIUtilities.JavaElementTableCellRenderer() {
//            // override the extractText method to add "implements " prefix to the text
//            // in case the value is instance of MultipartId (i.e. it represents an interface
//            // name from implements clause)
//            protected String extractText(Object value) {
//                String displayValue = super.extractText(value);
//                if (value instanceof MultipartId) {
//                    displayValue = "implements " + displayValue; // NOI18N
//                }
//                return displayValue;
//            }
//        });
//        // send renderer for the third column ("Make Abstract") to make the checkbox:
//        // 1. hidden for elements that are not methods
//        // 2. be disabled for static methods
//        // 3. be disabled and checked for methods if the target type is an interface
//        // 4. be disabled and check for abstract methods
//        membersTable.getColumnModel().getColumn(2).setCellRenderer(new UIUtilities.BooleanTableCellRenderer() {
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//                // make the checkbox checked (even if "Make Abstract" is not set)
//                // for non-static methods if the target type is an interface
//                Object object = table.getModel().getValueAt(row, 1);
//                if (object instanceof Method) {
//                    if ((targetType.isInterface() && !Modifier.isStatic(((Method) object).getModifiers())) || Modifier.isAbstract(((Method) object).getModifiers())) {
//                        value = Boolean.TRUE;
//                    }
//                }
//                // the super method automatically makes sure the checkbox is not visible if the
//                // "Make Abstract" value is null (which holds for non-methods)
//                // and that the checkbox is disabled if the cell is not editable (which holds for
//                // static methods all the time and for all methods in case the target type is an interface
//                // - see the table model)
//                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//            }
//        });
//        // set background color of the scroll pane to be the same as the background
//        // of the table
//        scrollPane.setBackground(membersTable.getBackground());
//        scrollPane.getViewport().setBackground(membersTable.getBackground());
//        // set default row height
//        membersTable.setRowHeight(18);
//        // set grid color to be consistent with other netbeans tables
//        if (UIManager.getColor("control") != null) { // NOI18N
//            membersTable.setGridColor(UIManager.getColor("control")); // NOI18N
//        }
//        // compute and set the preferred width for the first and the third column
//        UIUtilities.initColumnWidth(membersTable, 0, Boolean.TRUE, 4);
//        UIUtilities.initColumnWidth(membersTable, 2, Boolean.TRUE, 4);
    }
    
    // --- GETTERS FOR REFACTORING PARAMETERS ----------------------------------
    
    /** Getter used by the refactoring UI to get value
     * of target type.
     * @return Target type.
     */
    public TreePathHandle getTargetType() {
        return targetType;
    }
    
    /** Getter used by the refactoring UI to get members to be pulled up.
     * @return Descriptors of members to be pulled up.
     */
    public PullUpRefactoring.MemberInfo[] getMembers() {
//        List list = new ArrayList();
//        // remeber if the target type is an interface (will be used in the loop)
//        boolean targetIsInterface = targetType.isInterface();
//        // go through all rows of a table and collect selected members
//        for (int i = 0; i < members.length; i++) {
//            // if the current row is selected, create MemberInfo for it and
//            // add it to the list of selected members
//            if (members[i][0].equals(Boolean.TRUE)) {
//                Object element = members[i][1];
//                Object member;
//                if (element instanceof Field) {
//                    member = new PullUpRefactoring.MemberInfo((Field) element);
//                } else if (element instanceof JavaClass) {
//                    member = new PullUpRefactoring.MemberInfo((JavaClass) element);
//                } else if (element instanceof MultipartId) {
//                    member = new PullUpRefactoring.MemberInfo((MultipartId) element);
//                } else {
//                    // for methods the makeAbstract is always set to true if the
//                    // target type is an interface
//                    member = new PullUpRefactoring.MemberInfo((Method) element, targetIsInterface || ((Boolean) members[i][2]).booleanValue());
//                }
//                list.add(member);
//            }
//        }
//        // return the array of selected members
//        return (PullUpRefactoring.MemberInfo[]) list.toArray(new PullUpRefactoring.MemberInfo[list.size()]);
        return new PullUpRefactoring.MemberInfo[0];
    }
    
    // --- GENERATED CODE ------------------------------------------------------
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        supertypePanel = new javax.swing.JPanel();
        supertypeCombo = new javax.swing.JComboBox();
        supertypeLabel = new javax.swing.JLabel();
        chooseLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        membersTable = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setName(org.openide.util.NbBundle.getMessage(PullUpPanel.class, "LBL_PullUpHeader", new Object[] {UIUtilities.getDisplayText(refactoring.getSourceType())}) /* NOI18N */);
        supertypePanel.setLayout(new java.awt.BorderLayout(12, 0));

        supertypePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        supertypePanel.add(supertypeCombo, java.awt.BorderLayout.CENTER);

        supertypeLabel.setLabelFor(supertypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(supertypeLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/ui/Bundle").getString("LBL_PullUp_Supertype"));
        supertypePanel.add(supertypeLabel, java.awt.BorderLayout.WEST);
        supertypeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PullUpPanel.class, "ACSD_DestinationSupertypeName"));
        supertypeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PullUpPanel.class, "ACSD_DestinationSupertypeDescription"));

        chooseLabel.setLabelFor(membersTable);
        org.openide.awt.Mnemonics.setLocalizedText(chooseLabel, org.openide.util.NbBundle.getMessage(PullUpPanel.class, "LBL_PullUpLabel"));
        chooseLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        supertypePanel.add(chooseLabel, java.awt.BorderLayout.SOUTH);

        add(supertypePanel, java.awt.BorderLayout.NORTH);

        membersTable.setModel(tableModel);
        membersTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        scrollPane.setViewportView(membersTable);
        membersTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PullUpPanel.class, "ACSD_MembersToPullUp"));
        membersTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PullUpPanel.class, "ACSD_MembersToPullUpDescription"));

        add(scrollPane, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel chooseLabel;
    private javax.swing.JTable membersTable;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JComboBox supertypeCombo;
    private javax.swing.JLabel supertypeLabel;
    private javax.swing.JPanel supertypePanel;
    // End of variables declaration//GEN-END:variables
    
    public Component getComponent() {
        return this;
    }

    // --- MODELS --------------------------------------------------------------
    
    /** Model for the members table.
     */
    private class TableModel extends AbstractTableModel {
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        public String getColumnName(int column) {
            return UIUtilities.getColumnName(NbBundle.getMessage(PullUpPanel.class, COLUMN_NAMES[column]));
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
            fireTableDataChanged();
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 2) {
                // column 2 is editable only in case of non-static methods
                // if the target type is not an interface
                // if the method is abstract
                if (members[rowIndex][2] == null) {
                    return false;
                }
                Object element = members[rowIndex][1];
                //TODO:
                //return !targetType.isInterface() && !Modifier.isStatic(((Method) element).getModifiers()) && !Modifier.isAbstract(((Method) element).getModifiers());
                return false;
            } else {
                // column 0 is always editable, column 1 is never editable
                return columnIndex == 0;
            }
        }
        

//        /** Method called by target type combo box model when the selection changes
//         * (i.e. when the selected target type changes).
//         * Updates table rows based on the change (all members from the source type
//         * up to the direct subtypes of the target type need to be displayed).
//         * @param classes Classes the members of which should be displayed (these are all classes
//         *      that are supertypes of source type (including the source type) and at the same time subtypes
//         *      of the target type (excluding the target type).
//         */
//        void update(JavaClass[] classes) {
//            Map map = new HashMap();
//            // go through the passed classes, collect all members from them and
//            // create a map mapping a member to an array of java.lang.Object representing
//            // a future table row corresponding to that member
//            for (int i = 0; i < classes.length; i++) {
//                // collect interface names
//                for (Iterator it = classes[i].getInterfaceNames().iterator(); it.hasNext();) {
//                    Object ifcName = it.next();
//                    map.put(ifcName, new Object[] {Boolean.FALSE, ifcName, null});
//                }
//                // collect fields, methods and inner classes
//                Object[] features = classes[i].getFeatures().toArray();
//                for (int j = 0; j < features.length; j++) {
//                    if (features[j] instanceof JavaClass || features[j] instanceof Field || features[j] instanceof Method) {
//                        map.put(features[j], new Object[] {Boolean.FALSE, features[j], (features[j] instanceof Method) ? Boolean.FALSE : null});
//                    }
//                }
//            }
//            // select some members if applicable
//            if (selectedMembers != null) {
//                // if the collection of pre-selected members is not null
//                // this is the first creation of the table data ->
//                // -> select the members from the selectedMembers collection
//                for (Iterator it = selectedMembers.iterator(); it.hasNext();) {
//                    Object[] value = (Object[]) map.get(it.next());
//                    if (value != null) {
//                        value[0] = Boolean.TRUE;
//                    }
//                }
//                selectedMembers = null;
//            } else {
//                // this is not the first update of the table content ->
//                // -> select elements that were selected before the update
//                // (if they will still be present in the table)
//                for (int i = 0; i < members.length; i++) {
//                    Object[] value = (Object[]) map.get(members[i][1]);
//                    if (value != null) {
//                        map.put(value[1], members[i]);
//                    }
//                }
//            }
//            
//            // TODO: remove overrides, since they cannot be pulled up
//            
//            // the members are collected
//            // now, create a tree map (to sort them) and create the table data
//            TreeMap treeMap = new TreeMap(new Comparator() {
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
//            treeMap.putAll(map);
//            members = new Object[treeMap.size()][];
//            int i = 0;
//            for (Iterator it = treeMap.values().iterator(); it.hasNext(); i++) {
//                members[i] = (Object[]) it.next();
//            }
//            // fire event to repaint the table
//            this.fireTableDataChanged();
//        }
//    }

    /** Model for combo box for choosing target type.
     */
    private class ComboModel extends AbstractListModel implements ComboBoxModel {
        private final TreePathHandle[] supertypes;
       
        /** Creates the combo model.
         * @param supertypes List of applicable supertypes that may be chosen to be
         *      target types.
         */
        ComboModel(TreePathHandle[] supertypes) {
            this.supertypes = supertypes;
            if (supertypes.length > 0) {
                setSelectedItem(supertypes[0]);
            }
        }
        
        /** Gets invoked when the selection changes. Computes the classes the members
         * of which can be pulled up and calls table model's update() method to
         * update the table content with changed set of members.
         * @param anItem Class selected to be the target.
         */
        public void setSelectedItem(Object anItem) {
//            if (targetType != anItem) {
//                targetType = (TreePathHandle) anItem;
//                // must fire this (according to the ComboBoxModel interface contract)
//                fireContentsChanged(this, -1, -1);
//                // compute the classes (they must be superclasses of source type - including it -
//                // and subtypes of target type)
//                List classes = new ArrayList();
//                // add source type (it is always included)
//                classes.add(refactoring.getSourceType());
//                for (int i = 0; i < supertypes.length; i++) {
//                    // add the other subtypes of the target type
//                    if (!supertypes[i].equals(targetType) && supertypes[i].isSubTypeOf(targetType)) {
//                        classes.add(supertypes[i]);
//                    }
//                }
//                // update the table
//                tableModel.update((JavaClass[]) classes.toArray(new JavaClass[classes.size()]));
//            }
        }

        public Object getSelectedItem() {
            return targetType;
        }

        public Object getElementAt(int index) {
            return supertypes[index];
        }

        public int getSize() {
            return supertypes.length;
        }
    }

    }
}
