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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Set;
import java.util.Set;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.plugins.LocalVarScanner;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Panel contains components for signature change. There is table with
 * parameters, you can add parameters, reorder parameteres or remove not
 * used paramaters (not available yet). You can also change access modifier.
 *
 * @author  Pavel Flaska, Jan Becicka
 */
public class ChangeParametersPanel extends JPanel implements CustomRefactoringPanel {

    TreePathHandle refactoredObj;
    ParamTableModel model;
    private ChangeListener parent;
    
    private static Action editAction = null;
    private String returnType;
    
    private static final String[] modifierNames = {
        "public", // NOI18N
        "protected", // NOI18N
        "<default>", // NOI18N
        "private" // NOI18N
    };
    
    public Component getComponent() {
        return this;
    }
    
    private static final String[] columnNames = {
        getString("LBL_ChangeParsColName"), // NOI18N
        getString("LBL_ChangeParsColType"), // NOI18N
        getString("LBL_ChangeParsColDefVal"), // NOI18N
        getString("LBL_ChangeParsColOrigIdx"), // NOI18N
        getString("LBL_ChangeParsParUsed") // NOI18N
    };

    // modifier items in combo - indexes

    // modifier items in combo - indexes

    // modifier items in combo - indexes
    private static final int MOD_PUBLIC_INDEX = 0;
    private static final int MOD_PROTECTED_INDEX = 1;
    private static final int MOD_DEFAULT_INDEX = 2;
    private static final int MOD_PRIVATE_INDEX = 3;

    private static final String ACTION_INLINE_EDITOR = "invokeInlineEditor";  //NOI18N

    /** Creates new form ChangeMethodSignature */
    public ChangeParametersPanel(TreePathHandle refactoredObj, ChangeListener parent) {
        this.refactoredObj = refactoredObj;
        this.parent = parent;
        model = new ParamTableModel(columnNames, 0);
        initComponents();
    }
    
    private boolean initialized = false;
    public void initialize() {
        try {
            if (initialized) {
                return;
            }
            org.netbeans.api.java.source.JavaSource source = org.netbeans.api.java.source.JavaSource.forFileObject(org.netbeans.modules.refactoring.java.RetoucheUtils.getFileObject(refactoredObj));
            source.runUserActionTask(new org.netbeans.api.java.source.CancellableTask<org.netbeans.api.java.source.CompilationController>() {
                public void run(org.netbeans.api.java.source.CompilationController info) {
                    try {
                        info.toPhase(org.netbeans.api.java.source.JavaSource.Phase.RESOLVED);
                        ExecutableElement e = (ExecutableElement) refactoredObj.resolveElement(info);
                        returnType = e.getReturnType().toString();
                        Element def = SourceUtils.getEnclosingTypeElement(e);
                        if (def.getKind().isInterface()) {
                            modifiersCombo.setEnabled(false);
                        }
                        initTableData(info);
                        setModifier(e.getModifiers());
                        previewChange.setText(genDeclarationString());
                    }
                    catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                public void cancel() {
                }
            }, true);
            initialized = true;
        }
        catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    protected DefaultTableModel getTableModel() {
        return model;
    }
    
    protected Set<Modifier> getModifier() {
        modifiers.remove(Modifier.PRIVATE);
        modifiers.remove(Modifier.PUBLIC);
        modifiers.remove(Modifier.PROTECTED);
        
        switch (modifiersCombo.getSelectedIndex()) {
        case MOD_PRIVATE_INDEX: modifiers.add(Modifier.PRIVATE);break;
        case MOD_DEFAULT_INDEX: break; /* no modifier */
        case MOD_PROTECTED_INDEX: modifiers.add(Modifier.PROTECTED); break;
        case MOD_PUBLIC_INDEX: modifiers.add(Modifier.PUBLIC); break;
        }
        return modifiers;
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        modifiersPanel = new javax.swing.JPanel();
        modifiersLabel = new javax.swing.JLabel();
        modifiersCombo = new javax.swing.JComboBox();
        eastPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();
        fillPanel = new javax.swing.JPanel();
        westPanel = new javax.swing.JScrollPane();
        paramTable = new javax.swing.JTable();
        paramTitle = new javax.swing.JLabel();
        previewChange = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setAutoscrolls(true);
        setName(getString("LBL_TitleChangeParameters"));
        setLayout(new java.awt.GridBagLayout());

        modifiersPanel.setLayout(new java.awt.GridBagLayout());

        modifiersLabel.setLabelFor(modifiersCombo);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/java/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(modifiersLabel, bundle.getString("LBL_ChangeParsMods")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        modifiersPanel.add(modifiersLabel, gridBagConstraints);

        modifiersCombo.setModel(new DefaultComboBoxModel(modifierNames));
        modifiersCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifiersComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        modifiersPanel.add(modifiersCombo, gridBagConstraints);
        modifiersCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_modifiersCombo")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(modifiersPanel, gridBagConstraints);

        eastPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 11, 1, 1));
        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, bundle.getString("LBL_ChangeParsAdd")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        buttonsPanel.add(addButton, gridBagConstraints);
        addButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ChangeParsAdd")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, bundle.getString("LBL_ChangeParsRemove")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        buttonsPanel.add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ChangeParsRemove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, bundle.getString("LBL_ChangeParsMoveUp")); // NOI18N
        moveUpButton.setEnabled(false);
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        buttonsPanel.add(moveUpButton, gridBagConstraints);
        moveUpButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ChangeParsMoveUp")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, bundle.getString("LBL_ChangeParsMoveDown")); // NOI18N
        moveDownButton.setEnabled(false);
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        buttonsPanel.add(moveDownButton, gridBagConstraints);
        moveDownButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ChangeParsMoveDown")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        eastPanel.add(buttonsPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        eastPanel.add(fillPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(eastPanel, gridBagConstraints);

        westPanel.setPreferredSize(new java.awt.Dimension(453, 100));

        paramTable.setModel(model);
        initRenderer();
        paramTable.getSelectionModel().addListSelectionListener(getListener1());
        paramTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        model.addTableModelListener(getListener2());
        paramTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), ACTION_INLINE_EDITOR); //NOI18N
        paramTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), ACTION_INLINE_EDITOR); //NOI18N
        paramTable.getActionMap().put(ACTION_INLINE_EDITOR, getEditAction()); //NOI18N
        paramTable.setSurrendersFocusOnKeystroke(true);
        paramTable.setCellSelectionEnabled(false);
        paramTable.setRowSelectionAllowed(true);
        paramTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE); //NOI18N
        paramTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); //NOI18N
        westPanel.setViewportView(paramTable);
        paramTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_paramTable")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(westPanel, gridBagConstraints);

        paramTitle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        paramTitle.setLabelFor(paramTable);
        org.openide.awt.Mnemonics.setLocalizedText(paramTitle, bundle.getString("LBL_ChangeParsParameters")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        add(paramTitle, gridBagConstraints);

        previewChange.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getBundle(ChangeParametersPanel.class).getString("LBL_ChangeParsPreview"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(previewChange, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // </editor-fold>                        
    // </editor-fold>                        

    private void modifiersComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifiersComboActionPerformed
        previewChange.setText(genDeclarationString());
    }//GEN-LAST:event_modifiersComboActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        acceptEditedValue(); 
        int[] selectedRows = paramTable.getSelectedRows();
        ListSelectionModel selectionModel = paramTable.getSelectionModel();
        for (int i = 0; i < selectedRows.length; ++i) {
            boolean b = ((Boolean) ((Vector) model.getDataVector().get(selectedRows[i] - i)).get(4)).booleanValue();
            if (!b) {
                String title = getString("LBL_ChangeParsCannotDeleteTitle");
                String mes = MessageFormat.format(getString("LBL_ChangeParsCannotDelete"),((Vector) model.getDataVector().get(selectedRows[i] - i)).get(0));
                int a = new JOptionPane().showConfirmDialog(this, mes, title, JOptionPane.YES_NO_OPTION);
                if (a==JOptionPane.YES_OPTION) {
                    model.removeRow(selectedRows[i] - i);
                    selectionModel.clearSelection();
                }
            } else {
                model.removeRow(selectedRows[i] - i);
                selectionModel.clearSelection();
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void moveDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownButtonActionPerformed
        doMove(1);
    }//GEN-LAST:event_moveDownButtonActionPerformed

    private void moveUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpButtonActionPerformed
        doMove(-1);
    }//GEN-LAST:event_moveUpButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        acceptEditedValue(); 
        int rowCount = model.getRowCount();
        model.addRow(new Object[] { "par" + rowCount, "Object", "null", new Integer(-1), Boolean.TRUE }); // NOI18N
    }//GEN-LAST:event_addButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel eastPanel;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JComboBox modifiersCombo;
    private javax.swing.JLabel modifiersLabel;
    private javax.swing.JPanel modifiersPanel;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JTable paramTable;
    private javax.swing.JLabel paramTitle;
    private javax.swing.JLabel previewChange;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane westPanel;
    // End of variables declaration//GEN-END:variables

    private ListSelectionListener getListener1() {
        return new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                if (!lsm.isSelectionEmpty()) {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    setButtons(minIndex, maxIndex);
                    
                    boolean enableRemoveBtn = true;
                    for (int i = minIndex; i <= maxIndex; i++) {
                        enableRemoveBtn = model.isRemovable(i);
                        if (!enableRemoveBtn)
                            break;
                    }
                    removeButton.setEnabled(enableRemoveBtn);
                }
                else {
                    moveDownButton.setEnabled(false);
                    moveUpButton.setEnabled(false);
                    removeButton.setEnabled(false);
                }
            }
        };
    }
    
    private TableModelListener getListener2() {
        return new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                // update buttons availability
                int[] selectedRows = paramTable.getSelectedRows();
                if (selectedRows.length == 0) {
                    removeButton.setEnabled(false);
                }
                else {
                    boolean enableRemoveBtn = true;
                    for (int i = 0; i < selectedRows.length; i++) {
                        if (selectedRows[i] < model.getRowCount()) {
                            enableRemoveBtn = model.isCellEditable(selectedRows[i], 0);
                            if (!enableRemoveBtn)
                                break;
                        }
                    }
                    removeButton.setEnabled(enableRemoveBtn);
                    int min = selectedRows[0];
                    int max = selectedRows[selectedRows.length - 1];
                    setButtons(min, max);
                }
                
                // update preview
                previewChange.setText(genDeclarationString());
                
                parent.stateChanged(null);
            }
        };
    }

    private void initTableData(CompilationController info) {
        ExecutableElement method = (ExecutableElement) refactoredObj.resolveElement(info);
        
        List<? extends VariableElement> pars = method.getParameters();

//        List typeList = new ArrayList();
//        for (Iterator parIt = pars.iterator(); parIt.hasNext(); ) {
//            Parameter par = (Parameter) parIt.next();
//            typeList.add(par.getType());
//        }

        Collection<ExecutableElement> allMethods = new ArrayList();
        allMethods.addAll(RetoucheUtils.getOverridenMethods(method, info));
        allMethods.addAll(RetoucheUtils.getOverridingMethods(method, info));
        allMethods.add(method);
        
        for (ExecutableElement currentMethod: allMethods) {
            int originalIndex = 0;
            for (VariableElement par:currentMethod.getParameters()) {
                TypeMirror desc = par.asType();
                String typeRepresentation = getTypeStringRepresentation(desc);
                LocalVarScanner scan = new LocalVarScanner(info, null);
                scan.scan(info.getTrees().getPath(method), par);
                Boolean removable = !scan.hasRefernces();
                if (model.getRowCount()<=originalIndex) {
                    Object[] parRep = new Object[] { par.toString(), typeRepresentation, "", new Integer(originalIndex), removable };
                    model.addRow(parRep);
                } else {
                    removable = Boolean.valueOf(model.isRemovable(originalIndex) && removable.booleanValue());
                    ((Vector) model.getDataVector().get(originalIndex)).set(4, removable);
                }
                originalIndex++;
            }
        }
    }
    
    private static String getTypeStringRepresentation(TypeMirror desc) {
        return desc.toString();
    }

    private boolean acceptEditedValue() {
        TableCellEditor tce = paramTable.getCellEditor();
        if (tce != null)
            return paramTable.getCellEditor().stopCellEditing();
        return false;
    }
    
    private void doMove(int step) {
        acceptEditedValue(); 
        
        ListSelectionModel selectionModel = paramTable.getSelectionModel();
        int min = selectionModel.getMinSelectionIndex();
        int max = selectionModel.getMaxSelectionIndex();
        
        selectionModel.clearSelection();
        model.moveRow(min, max, min + step);
        selectionModel.addSelectionInterval(min + step, max + step);
    }
    
    private void setButtons(int min, int max) {
        int r = model.getRowCount() - 1;
        moveUpButton.setEnabled(min > 0 ? true : false);
        moveDownButton.setEnabled(max < r ? true : false);
    }
    
    private void initRenderer() {
        TableColumnModel tcm = paramTable.getColumnModel();
        paramTable.removeColumn(tcm.getColumn(3));
        paramTable.removeColumn(tcm.getColumn(3));
        Enumeration columns = paramTable.getColumnModel().getColumns();
        TableColumn tc = null;
        while (columns.hasMoreElements()) {
            tc = (TableColumn) columns.nextElement();
            tc.setCellRenderer(new ParamRenderer());
        }
    }

    private Set<Modifier> modifiers = new HashSet();
    private void setModifier(Set<Modifier> mods) {
        this.modifiers.clear();
        this.modifiers.addAll(mods);
        if (mods.contains(Modifier.PRIVATE))
            modifiersCombo.setSelectedIndex(MOD_PRIVATE_INDEX);
        else if (mods.contains(Modifier.PROTECTED))
            modifiersCombo.setSelectedIndex(MOD_PROTECTED_INDEX);
        else if (mods.contains(Modifier.PUBLIC))
            modifiersCombo.setSelectedIndex(MOD_PUBLIC_INDEX);
        else
            modifiersCombo.setSelectedIndex(MOD_DEFAULT_INDEX);
    }

    public String genDeclarationString() {
        // generate preview for modifiers
        // access modifiers
        String mod = modifiersCombo.getSelectedIndex() != MOD_DEFAULT_INDEX /*default modifier?*/ ?
            (String) modifiersCombo.getSelectedItem() + ' ' : ""; // NOI18N
        
        StringBuffer buf = new StringBuffer(mod);
        // other than access modifiers - using data provided by the element
        // first of all, reset access modifier, because it is generated from combo value
//        String otherMod = Modifier.toString(((CallableFeature) refactoredObj).getModifiers() & 0xFFFFFFF8);
//        if (otherMod.length() != 0) {
//            buf.append(otherMod);
//            buf.append(' ');
//        }
        // generate the return type for the method and name
        // for the both - method and constructor
        String name;
        if (RetoucheUtils.getElementKind(refactoredObj) == ElementKind.METHOD) {
            buf.append(returnType);
            buf.append(' ');
            name = RetoucheUtils.getSimpleName(refactoredObj);
        } else {
            // for constructor, get name from the declaring class
            name = RetoucheUtils.getSimpleName(refactoredObj);
        }
        buf.append(name);
        buf.append('(');
        // generate parameters to the preview string
        List[] parameters = (List[]) model.getDataVector().toArray(new List[0]);
        if (parameters.length > 0) {
            int i;
            for (i = 0; i < parameters.length - 1; i++) {
                buf.append((String) parameters[i].get(1));
                buf.append(' ');
                buf.append((String) parameters[i].get(0));
                buf.append(',').append(' ');
            }
            buf.append((String) parameters[i].get(1));
            buf.append(' ');
            buf.append((String) parameters[i].get(0));
        }
        buf.append(')'); //NOI18N
        
        return buf.toString();
    }

    private static String getString(String key) {
        return NbBundle.getMessage(ChangeParametersPanel.class, key);
    }

    private static Action getEditAction() {
        if (editAction == null) {
            editAction = new EditAction();
        }
        return editAction;
    }

    private static void autoEdit(JTable tab) {
        if (tab.editCellAt(tab.getSelectedRow(), tab.getSelectedColumn(), null) &&
            tab.getEditorComponent() != null)
        {
            JTextField field = (JTextField) tab.getEditorComponent();
            int len = field.getText().length();
            field.setCaretPosition(field.getText().length());
            field.requestFocusInWindow();
            field.selectAll();
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////
    // this class is used for marking rows as read-only. If the user uses
    // standard DefaultTableModel, rows added through its methods is added
    // as a read-write. -- Use methods with Boolean paramater to add
    // rows marked as read-only.
    static class ParamTableModel extends DefaultTableModel {
        
        public ParamTableModel(Object[] data, int rowCount) {
            super(data, rowCount);
        }

        public boolean isCellEditable(int row, int column) {
            if (column > 2) {
                // check box indicating usage of parameter is not editable
                return false;
            }
            // otherwise, check that user can change only the values provided
            // for the new parameter. (name change of old parameters aren't
            // allowed.
            Integer origIdx = (Integer) ((Vector) getDataVector().get(row)).get(3);
            return origIdx.intValue() == -1 ? true : false;
        }
        
        public boolean isRemovable(int row) {
            return true;//((Boolean) ((Vector) getDataVector().get(row)).get(4)).booleanValue();
        }
        
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
    } // end ParamTableModel

    private static class EditAction extends AbstractAction {
        public void actionPerformed(ActionEvent ae) {
            autoEdit((JTable) ae.getSource());
        }
    }
    
    class ParamRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        Color origBackground;
        
        public ParamRenderer() {
            setOpaque(true);
            origBackground = getBackground();
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column)
        {
            super.getTableCellRendererComponent(table,  value, isSelected, hasFocus, row, column);
            boolean isRemovable = model.isRemovable(row);
            if (!isSelected) {
                if (!isRemovable) {
                    setBackground(UIManager.getColor("Panel.background")); // NOI18N
                } else {
                    setBackground(origBackground);
                }
            }
            return this;
        }
        
    }
    
    // end INNERCLASSES
}
