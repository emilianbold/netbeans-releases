/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.Icon;
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
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/** UI panel for collecting refactoring parameters.
 *
 * @author Martin Matula, Jan Becicka, Jan Pokorsky
 */
public final class ExtractInterfacePanel extends JPanel implements CustomRefactoringPanel {
    // helper constants describing columns in the table of members
    private static final String[] COLUMN_NAMES = {"LBL_Selected", "LBL_ExtractInterface_Member"}; // NOI18N
    private static final Class[] COLUMN_CLASSES = {Boolean.class, TreePathHandle.class};
    
    // refactoring this panel provides parameters for
    private final ExtractInterfaceRefactoring refactoring;
    // table model for the table of members
    private final TableModel tableModel;
    // data for the members table (first dimension - rows, second dimension - columns)
    // the columns are: 0 = Selected (true/false), 1 = ExtractInterfaceInfo (Java element)
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
        // set renderer for the second column ("Member") to display name of the feature
        membersTable.setDefaultRenderer(COLUMN_CLASSES[1], new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, extractText(value), isSelected, hasFocus, row, column);
                if (value instanceof ExtractInterfaceInfo) {
                    setIcon(((ExtractInterfaceInfo) value).icon);
                }
                return this;
            }
            protected String extractText(Object value) {
                String displayValue;
                if (value instanceof ExtractInterfaceInfo) {
                    displayValue = ((ExtractInterfaceInfo) value).htmlText;
                } else {
                    displayValue = String.valueOf(value);
                }
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
    
    /** stores data collected via the panel.
     */
    public void storeSettings() {
        List<ElementHandle<VariableElement>> fields = new ArrayList<ElementHandle<VariableElement>>();
        List<ElementHandle<ExecutableElement>> methods = new ArrayList<ElementHandle<ExecutableElement>>();
        List<TypeMirrorHandle<TypeMirror>> implementz = new ArrayList<TypeMirrorHandle<TypeMirror>>();
        
        // go through all rows of a table and collect selected members
        for (int i = 0; i < members.length; i++) {
            if (members[i][0].equals(Boolean.TRUE)) {
                ExtractInterfaceInfo info = (ExtractInterfaceInfo) members[i][1];
                switch(info.group) {
                case FIELD: fields.add((ElementHandle<VariableElement>) info.handle); break;
                case METHOD: methods.add((ElementHandle<ExecutableElement>) info.handle); break;
                case IMPLEMENTS: implementz.add((TypeMirrorHandle<TypeMirror>) info.handle); break;
                }
            }
        }
        
        refactoring.setFields(fields);
        refactoring.setImplements(implementz);
        refactoring.setMethods(methods);
        refactoring.setInterfaceName(nameText.getText());
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
        nameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "ACSD_InterfaceName")); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "ACSD_InterfaceNameDescription")); // NOI18N

        chooseLabel.setLabelFor(membersTable);
        org.openide.awt.Mnemonics.setLocalizedText(chooseLabel, org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "LBL_ExtractInterfaceLabel")); // NOI18N
        chooseLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        namePanel.add(chooseLabel, java.awt.BorderLayout.SOUTH);
        chooseLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExtractInterfacePanel.class, "ExtractInterfacePanel.chooseLabel.AccessibleContext.accessibleDescription")); // NOI18N

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
                throw new IllegalStateException(ex);
            }
        }
        
        private void initializeInTransaction(CompilationController javac, TreePathHandle sourceType) {
            TreePath sourceTreePath = sourceType.resolve(javac);
            ClassTree sourceTree = (ClassTree) sourceTreePath.getLeaf();
            List result = new ArrayList();
            
            for (Tree implTree : sourceTree.getImplementsClause()) {
                TreePath implPath = javac.getTrees().getPath(javac.getCompilationUnit(), implTree);
                TypeMirror implMirror = javac.getTrees().getTypeMirror(implPath);
                result.add(new ExtractInterfaceInfo<TypeMirrorHandle>(
                        TypeMirrorHandle.create(implMirror),
                        "implements " + implTree.toString(), // NOI18N
                        ElementIcons.getElementIcon(ElementKind.INTERFACE, null),
                        implTree.toString(),
                        Group.IMPLEMENTS
                        ));
            }
            
            for (Tree member : sourceTree.getMembers()) {
                TreePath memberTreePath = javac.getTrees().getPath(javac.getCompilationUnit(), member);
                if (javac.getTreeUtilities().isSynthetic(memberTreePath))
                    continue;
                
                Element memberElm = javac.getTrees().getElement(memberTreePath);
                Set<Modifier> mods;
                if (memberElm == null || !(mods = memberElm.getModifiers()).contains(Modifier.PUBLIC))
                    continue;
                
                Group group;
                String format = ElementHeaders.NAME;
                if (memberElm.getKind() == ElementKind.FIELD) {
                    if (!mods.contains(Modifier.STATIC) || !mods.contains(Modifier.FINAL)
                            || ((VariableTree) member).getInitializer() == null)
                        continue;
                    group = Group.FIELD;
                    format += " : " + ElementHeaders.TYPE; // NOI18N
// XXX see ExtractInterfaceRefactoringPlugin class description
//                } else if (member.getKind() == Tree.Kind.CLASS) {
//                    if (!mods.contains(Modifier.STATIC))
//                        continue;
//                    group = 3;
                } else if (memberElm.getKind() == ElementKind.METHOD) {
                    if (mods.contains(Modifier.STATIC))
                        continue;
                    group = Group.METHOD;
                    format += ElementHeaders.PARAMETERS + " : " + ElementHeaders.TYPE; // NOI18N
                } else {
                    continue;
                }
                result.add(new ExtractInterfaceInfo<ElementHandle>(
                        ElementHandle.create(memberElm),
                        ElementHeaders.getHeader(memberElm, javac, format),
                        ElementIcons.getElementIcon(memberElm.getKind(), mods),
                        memberElm.getSimpleName().toString(),
                        group
                        ));
            }

            // the members are collected
            // now, create a tree map (to sort them) and create the table data
            Collections.sort(result, new Comparator() {
                public int compare(Object o1, Object o2) {
                    ExtractInterfaceInfo i1 = (ExtractInterfaceInfo) o1;
                    ExtractInterfaceInfo i2 = (ExtractInterfaceInfo) o2;
                    int result = i1.group.compareTo(i2.group);
                    
                    if (result == 0) {
                        result = i1.name.compareTo(i2.name);
                    }
                    
                    return result;
                }
            });
            members = new Object[result.size()][2];
            for (int i = 0; i < members.length; i++) {
                members[i][0] = Boolean.FALSE;
                members[i][1] = result.get(i);
            }
            // fire event to repaint the table
            this.fireTableDataChanged();
        }
    }

    public Component getComponent() {
        return this;
    }
    
    private static final class ExtractInterfaceInfo<H> {
        final H handle;
        final String htmlText;
        final Icon icon;
        final String name;
        final Group group;
        
        public ExtractInterfaceInfo(H handle, String htmlText, Icon icon, String name, Group group) {
            this.handle = handle;
            this.htmlText = htmlText;
            this.icon = icon;
            this.name = name;
            this.group = group;
        }
    }
    
    private enum Group {
        IMPLEMENTS, METHOD, FIELD;
    }
}
