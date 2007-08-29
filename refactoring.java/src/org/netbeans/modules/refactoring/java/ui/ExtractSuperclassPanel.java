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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils.PrintPart;
import org.netbeans.modules.refactoring.java.api.ExtractSuperclassRefactoring;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** UI panel for collecting refactoring parameters.
 *
 * @author Martin Matula, Jan Pokorsky
 */
public class ExtractSuperclassPanel extends JPanel implements CustomRefactoringPanel {
    // helper constants describing columns in the table of members
    private static final String[] COLUMN_NAMES = {"LBL_Selected", "LBL_ExtractSC_Member", "LBL_ExtractSC_MakeAbstract"}; // NOI18N
    private static final Class[] COLUMN_CLASSES = {Boolean.class, MemberInfo.class, Boolean.class};
    
    // refactoring this panel provides parameters for
    private final ExtractSuperclassRefactoring refactoring;
    // table model for the table of members
    private final TableModel tableModel;
    // data for the members table (first dimension - rows, second dimension - columns)
    // the columns are: 0 = Selected (true/false), 1 = Member (Java element), 2 = Make Abstract (true/false)
    private Object[][] members = new Object[0][0];
    
    /** Creates new form ExtractSuperclassPanel
     * @param refactoring The refactoring this panel provides parameters for.
     * @param selectedMembers Members that should be pre-selected in the panel
     *      (determined by which nodes the action was invoked on - e.g. if it was
     *      invoked on a method, the method will be pre-selected to be pulled up)
     */
    public ExtractSuperclassPanel(ExtractSuperclassRefactoring refactoring, final ChangeListener parent) {
        this.refactoring = refactoring;
        this.tableModel = new TableModel();
        initComponents();
        setPreferredSize(new Dimension(420, 380));
        String defaultName = "NewClass"; //NOI18N
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
        membersTable.setDefaultRenderer(COLUMN_CLASSES[1], new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, extractText(value), isSelected, hasFocus, row, column);
                if (value instanceof MemberInfo) {
                    setIcon(((MemberInfo) value).getIcon());
                }
                return this;
            }
            protected String extractText(Object value) {
                String displayValue;
                if (value instanceof MemberInfo) {
                    displayValue = ((MemberInfo) value).getHtmlText();
                } else {
                    displayValue = String.valueOf(value);
                }
                return displayValue;
            }
        });
        // send renderer for the third column ("Make Abstract") to make the checkbox:
        // 1. hidden for elements that are not methods
        // 2. be disabled for static methods
        membersTable.getColumnModel().getColumn(2).setCellRenderer(new UIUtilities.BooleanTableCellRenderer());
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
        UIUtilities.initColumnWidth(membersTable, 2, Boolean.TRUE, 4);
    }
    
    // --- GETTERS FOR REFACTORING PARAMETERS ----------------------------------
    
    public String getSuperClassName() {
        return nameText.getText();
    }
    
    /** Getter used by the refactoring UI to get members to be pulled up.
     * @return Descriptors of members to be pulled up.
     */
    public MemberInfo[] getMembers() {
        List<MemberInfo> list = new ArrayList<MemberInfo>();
        // go through all rows of a table and collect selected members
        for (int i = 0; i < members.length; i++) {
            // if the current row is selected, create MemberInfo for it and
            // add it to the list of selected members
            if (members[i][0].equals(Boolean.TRUE)) {
                MemberInfo member = (MemberInfo) members[i][1];
                member.setMakeAbstract(members[i][2] != null && ((Boolean) members[i][2]));
                list.add(member);
            }
        }
        // return the array of selected members
        return (MemberInfo[]) list.toArray(new MemberInfo[list.size()]);
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
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "LBL_ExtractSC_Name")); // NOI18N
        namePanel.add(nameLabel, java.awt.BorderLayout.WEST);
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "ExtractSuperclassPanel.nameLabel.AccessibleContext.accessibleDescription")); // NOI18N

        chooseLabel.setLabelFor(membersTable);
        org.openide.awt.Mnemonics.setLocalizedText(chooseLabel, org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "LBL_ExtractSCLabel")); // NOI18N
        chooseLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        namePanel.add(chooseLabel, java.awt.BorderLayout.SOUTH);
        chooseLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExtractSuperclassPanel.class, "ExtractSuperclassPanel.chooseLabel.AccessibleContext.accessibleDescription")); // NOI18N

        namePanel.add(nameText, java.awt.BorderLayout.CENTER);
        nameText.getAccessibleContext().setAccessibleName(null);
        nameText.getAccessibleContext().setAccessibleDescription(null);

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
            return UIUtilities.getColumnName(NbBundle.getMessage(ExtractSuperclassPanel.class, COLUMN_NAMES[column]));
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
            if (columnIndex == 2) {
                // column 2 is editable only in case of non-static methods
                // if the target type is not an interface
                if (members[rowIndex][2] == null) {
                    return false;
                }
                MemberInfo element = (MemberInfo) members[rowIndex][1];
                return !(element.getModifiers().contains(Modifier.STATIC) || element.getModifiers().contains(Modifier.ABSTRACT));
            } else {
                // column 0 is always editable, column 1 is never editable
                return columnIndex == 0;
            }
        }

        private void initialize() {
            final TreePathHandle sourceType = refactoring.getSourceType();
            if (sourceType == null) return;
            
            FileObject fo = sourceType.getFileObject();
            JavaSource js = JavaSource.forFileObject(fo);
            try {
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                        public void cancel() {
                        }

                        public void run(CompilationController javac) throws Exception {
                            javac.toPhase(JavaSource.Phase.RESOLVED);
                            initializeInTransaction(javac, sourceType);
                        }

                    }, true);
            } catch (IOException ex) {
                new IllegalStateException(ex);
            }
        }
        
        private void initializeInTransaction(CompilationController javac, TreePathHandle sourceType) {
            TreePath sourceTreePath = sourceType.resolve(javac);
            ClassTree sourceTree = (ClassTree) sourceTreePath.getLeaf();
            List<MemberInfo<?>> result = new ArrayList<MemberInfo<?>>();
            
            for (Tree implTree : sourceTree.getImplementsClause()) {
                TreePath implPath = javac.getTrees().getPath(javac.getCompilationUnit(), implTree);
                TypeMirror implMirror = javac.getTrees().getTypeMirror(implPath);
                result.add(MemberInfo.create(implMirror, implTree,javac));
            }
            
            for (Tree member : sourceTree.getMembers()) {
                TreePath memberTreePath = javac.getTrees().getPath(javac.getCompilationUnit(), member);
                if (javac.getTreeUtilities().isSynthetic(memberTreePath))
                    continue;
                
                Element memberElm = javac.getTrees().getElement(memberTreePath);
                if (memberElm == null)
                    continue;
                
                if (memberElm.getModifiers().contains(Modifier.PRIVATE)) {
                    //ignore private members.
                    continue;
                }
                if (memberElm.getKind() == ElementKind.FIELD) {
                    result.add(MemberInfo.create(memberElm, javac));
                } else if (memberElm.getKind() == ElementKind.METHOD) {
                    result.add(MemberInfo.create(memberElm,javac));
                }
            }
            
            // the members are collected
            // now, create a tree map (to sort them) and create the table data
            Collections.sort(result, new Comparator<MemberInfo<?>>() {
                public int compare(MemberInfo<?> mi1, MemberInfo<?> mi2) {
                    int result = mi1.getGroup().compareTo(mi2.getGroup());
                    
                    if (result == 0) {
                        result = mi1.getName().compareTo(mi2.getName());
                    }
                    
                    return result;
                }
            });
            members = new Object[result.size()][3];
            for (int i = 0; i < members.length; i++) {
                members[i][0] = Boolean.FALSE;
                MemberInfo<?> member = result.get(i);
                members[i][1] = member;
                if (member.getGroup() == MemberInfo.Group.METHOD) {
                    members[i][2] = member.isMakeAbstract();
                } else {
                    members[i][2] = null;
                }
            }
            // fire event to repaint the table
            this.fireTableDataChanged();
        }
    }

    public Component getComponent() {
        return this;
    }
}
