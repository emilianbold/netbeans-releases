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

package org.netbeans.modules.compapp.test.ui.wizards;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import org.openide.util.NbBundle;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.compapp.test.wsdl.Util;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

public class NewTestcaseOperationVisualPanel extends javax.swing.JPanel  {
    
    private javax.swing.JScrollPane mScrollPanel;
    private javax.swing.JTree mTree;
    private NewTestcaseOperationWizardPanel mPanel;    
    private WSDLModel wsdlModel;
    
    /** Creates new form NewTestcaseOperationVisualPanel */
    public NewTestcaseOperationVisualPanel(NewTestcaseOperationWizardPanel panel) {
        mPanel = panel;
        initComponents();
        mScrollPanel = new javax.swing.JScrollPane();
        mTree = new javax.swing.JTree();
        mTree.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(NewTestcaseWsdlVisualPanel.class, "ACS_OperationTree_A11YName"));  // NOI18N
        mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        mTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                BindingOperation bindingOp = getSelectedBindingOperation();
                jTextFieldSelectedOperation.setText(bindingOp == null ? 
                    "" : getOperationSignature(bindingOp)); // NOI18N
                mPanel.fireChangeEvent(); // Notify that the panel changed
            }
        });
        mTree.setRootVisible(false);
        mTree.setEditable(false);
        mTree.setCellRenderer(new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree,
                    Object value,
                    boolean sel,
                    boolean expanded,
                    boolean leaf,
                    int row,
                    boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(
                        tree, value, sel, expanded, leaf, row, hasFocus);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object usrObj = node.getUserObject();
                if (usrObj instanceof Binding) {
                    label.setText(((Binding)usrObj).getName());
                } else if (usrObj instanceof BindingOperation) {
                    label.setText(((BindingOperation)usrObj).getName());
                }
                return label;
            }
        });
        
        mScrollPanel.setViewportView(mTree);
        jLabelOperations.setLabelFor(mTree);
        
        org.jdesktop.layout.GroupLayout jPanel1Layout = (org.jdesktop.layout.GroupLayout) jPanel1.getLayout();
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(mScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(mScrollPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                );
    }
    
    public String getName() {
        return NbBundle.getMessage(NewTestcaseOperationVisualPanel.class,
                "LBL_Select_the_operation_to_test");  // NOI18N
    }
    
    public void setWsdlModel(WSDLModel wsdlModel) {
        if (this.wsdlModel == wsdlModel) {
            return;
        }
        
        this.wsdlModel = wsdlModel;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (Binding binding : Util.getSortedBindings(wsdlModel)) {
            DefaultMutableTreeNode bindingNode = new DefaultMutableTreeNode(binding);
            root.add(bindingNode);
            for (BindingOperation bindingOp : Util.getSortedBindingOperations(binding)) {
                DefaultMutableTreeNode bindingOpNode = new DefaultMutableTreeNode(bindingOp);
                bindingNode.add(bindingOpNode);
                bindingOpNode.setAllowsChildren(false);
            }
        }
        DefaultTreeModel dtm = new DefaultTreeModel(root);
        mTree.setModel(dtm);
        int cnt = root.getChildCount();
        for (int i = 0; i < cnt; i++) {
            mTree.expandPath(new TreePath(((DefaultMutableTreeNode)root.getChildAt(i)).getPath()));
        }
    }
    
    public Binding getSelectedBinding() {
        Object value = mTree.getLastSelectedPathComponent();
        if (value == null) {
            return null;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        if (node.getUserObject() instanceof BindingOperation) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            return (Binding)parent.getUserObject();
        }
        return null;
    }
    
    public BindingOperation getSelectedBindingOperation() {
        Object value = mTree.getLastSelectedPathComponent();
        if (value != null) {
            Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObj instanceof BindingOperation) {
                return (BindingOperation) userObj;
            }
        }
        return null;
    }
    
    public JTree getBindingTree() {
        return mTree;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabelSelectedOperation = new javax.swing.JLabel();
        jTextFieldSelectedOperation = new javax.swing.JTextField();
        jLabelOperations = new javax.swing.JLabel();

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 255, Short.MAX_VALUE)
        );

        jLabelSelectedOperation.setLabelFor(jTextFieldSelectedOperation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelSelectedOperation, org.openide.util.NbBundle.getMessage(NewTestcaseOperationVisualPanel.class, "LBL_The_operation_selected")); // NOI18N

        jTextFieldSelectedOperation.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelOperations, "Binding &Operations:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabelSelectedOperation)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldSelectedOperation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(jLabelOperations)
                .addContainerGap())
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jLabelOperations)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelSelectedOperation)
                    .add(jTextFieldSelectedOperation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jLabelSelectedOperation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewTestcaseOperationVisualPanel.class, "ACS_SELECTED_OPERATION_LABEL")); // NOI18N
        jLabelOperations.getAccessibleContext().setAccessibleName("Binding Operations:");
        jLabelOperations.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewTestcaseOperationVisualPanel.class, "ACS_BINDING_OPERATIONS")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewTestcaseOperationVisualPanel.class, "ACS_NewTestcaseOperationVisualPanel_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelOperations;
    private javax.swing.JLabel jLabelSelectedOperation;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextFieldSelectedOperation;
    // End of variables declaration//GEN-END:variables
    
    private static String getOperationSignature(BindingOperation bindingOp) {
        Operation op = bindingOp.getOperation().get();
        
        Input input = op.getInput();
        Output output = op.getOutput();
        StringBuffer sb = new StringBuffer();
        sb.append(op.getName() + "(");  // NOI18N
        if (input != null) {
            sb.append(input.getMessage().getQName().getLocalPart());
        }
        sb.append("): ");  // NOI18N
        if (output != null) {
            sb.append(output.getMessage().getQName().getLocalPart());
        } else {
            sb.append("void");  // NOI18N
        }
        return sb.toString();
    }
}
