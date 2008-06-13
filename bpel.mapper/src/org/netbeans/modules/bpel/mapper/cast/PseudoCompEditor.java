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

package org.netbeans.modules.bpel.mapper.cast;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.soa.ui.tree.DataObjectHolder;
import org.netbeans.modules.soa.ui.tree.impl.SoaTreeCellRenderer;
import org.netbeans.modules.soa.ui.tree.impl.SoaTreeModelImpl;
import org.netbeans.modules.soa.ui.tree.impl.SoaTreeNodeImpl;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.openide.util.NbBundle;

/**
 * Represents a dialog for creating a new pseudo schema component. 
 * 
 * TODO: Convert from chooser to editor.
 * 
 * @author  nk160297
 */
public class PseudoCompEditor extends EditorLifeCycleAdapter 
        implements Validator.Provider, ValidStateManager.Provider  {

    private BpelMapperModel mMapperModel;
    private boolean mInLeftTree;
    
    private AnyElement mAnyElement;
    private AnyAttribute mAnyAttr;
    private boolean mIsAttribute;
    
    private Iterable<Object> mAnyLocationPath; // represents a TreePath to the any
    private BpelModel mBpelModel;
    
    private SoaTreeModelImpl mSoaTreeModel;
    private DefaultValidator mValidator;
    private ValidStateManager mFastVSM;
    private ValidStateManager mFullVSM;

    public PseudoCompEditor(Iterable<Object> path, AnyAttribute anyAttr, 
            BpelModel bpelModel, BpelMapperModel mapperModel, boolean inLeftTree) {
        //
        mAnyLocationPath = path;
        mAnyAttr = anyAttr;
        mBpelModel = bpelModel;
        mIsAttribute = true;
        //
        mMapperModel = mapperModel;
        mInLeftTree = inLeftTree;
        //
        createContent();
        initControls();
    }
    
    public PseudoCompEditor(Iterable<Object> path, AnyElement anyElement, 
            BpelModel bpelModel, BpelMapperModel mapperModel, boolean inLeftTree) {
        //
        mAnyLocationPath = path;
        mAnyElement = anyElement;
        mBpelModel = bpelModel;
        mIsAttribute = false;
        //
        mMapperModel = mapperModel;
        mInLeftTree = inLeftTree;
        //
        createContent();
        initControls();
    }

    @Override
    public void createContent() {
        assert EventQueue.isDispatchThread();
        initComponents();
        //
        mSoaTreeModel = new SoaTreeModelImpl(
                new TypeChooserTreeModel(mBpelModel, mIsAttribute));
        gElementsTree.setModel(mSoaTreeModel);
        gElementsTree.setRootVisible(false);
        gElementsTree.setShowsRootHandles(true);
        ToolTipManager.sharedInstance().registerComponent(gElementsTree);
        //
        gElementsTree.setCellRenderer(new SoaTreeCellRenderer(mSoaTreeModel) {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree, Object value, boolean selected, 
                    boolean expanded, boolean leaf, int row, boolean hasFocus) {
                //
                super.getTreeCellRendererComponent(
                        tree, value, selected, expanded, leaf, row, hasFocus);
                //
                assert value instanceof SoaTreeNodeImpl;
                SoaTreeNodeImpl node = (SoaTreeNodeImpl)value;
                Object dataObj = node.getDataObject();
                if (dataObj instanceof GlobalType) {
                    String text = mModelImpl.getDisplayName(node);
                    text = EditorUtil.getAccentedString(text);
                    this.setText(text);
                }
                //
                return this;
            }
        });
        //
        gElementsTree.getSelectionModel().addTreeSelectionListener(
                new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                //
                // Synchronize name and namespace
                GlobalType gType = getSelectedType();
                if (gType != null) {
                    fldName.setText(gType.getName());
                    String namespace = gType.getModel().getEffectiveNamespace(gType);
                    fldNamespace.setText(namespace);
                }
                //
                getValidStateManager(true).clearReasons();
                getValidator().revalidate(true);
            }
        });
        //
//        subtypesTree.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent ev) {
//                if (ev.getClickCount() == 2) {
//                    
//                }
//            }
//        });
        //
        SoaUtil.activateInlineMnemonics(this);
    }

    @Override
    public boolean initControls() {
        getValidator().revalidate(true);
        return true;
    }

    public void setSelectedValue(DetachedPseudoComp newValue) {
        // It is not used and not implemented yet. 
        //
//        TreeModel tModel = gElementsTree.getModel();
//        assert tModel instanceof ExtTreeModel;
//        GlobalType type = newValue.getType();
//        GlobalSchemaComponentFinder finder = 
//                new GlobalSchemaComponentFinder(type);
//        TreeFinderProcessor findProc = new TreeFinderProcessor((ExtTreeModel)tModel);
//        TreePath gTypePath = findProc.findFirstNode(
//                Collections.singletonList((TreeItemFinder)finder));
//        if (gTypePath != null) {
//            gElementsTree.setSelectionPath(gTypePath);
//        } else {
//            gElementsTree.setSelectionRow(0); // select the root
//        }
//        //
//        // TODO: set name and namespace
    }

    public DetachedPseudoComp getSelectedValue() {
        GlobalType type = getSelectedType();
        String name = fldName.getText();
        String namespace = fldNamespace.getText();
        //
        DetachedPseudoComp newPseudoComp = new DetachedPseudoComp(
                type, name, namespace, mIsAttribute);
        return newPseudoComp;
    }

    private GlobalType getSelectedType() {
        TreePath selection = gElementsTree.getSelectionPath();
        if (selection != null) {
            Object selectedItem = selection.getLastPathComponent();
            if (selectedItem != null && selectedItem instanceof DataObjectHolder) {
                Object dataObj = ((DataObjectHolder)selectedItem).getDataObject();
                if (dataObj != null && dataObj instanceof GlobalType) {
                    GlobalType type = (GlobalType)dataObj;
                    return type;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns true if the user press Ok
     * @param editor
     * @return
     */
    public static boolean showDlg(PseudoCompEditor editor, 
            Callable<Boolean> okProcessor) {
        //
        String dlgTitle = null;
        if (editor.mIsAttribute) {
            dlgTitle = NbBundle.getMessage(PseudoCompEditor.class,
                "PSEUDO_ATTRIBUTE_DLG_TITLE"); // NOI18N
        } else {
            dlgTitle = NbBundle.getMessage(PseudoCompEditor.class,
                "PSEUDO_ELEMENT_DLG_TITLE"); // NOI18N
        }
        //
        DefaultDialogDescriptor descriptor = 
                new DefaultDialogDescriptor(editor, dlgTitle);
        descriptor.setOkButtonProcessor(okProcessor);
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        SoaUtil.setInitialFocusComponentFor(editor);
        dialog.setVisible(true);

        return descriptor.isOkHasPressed();
    }

    public Validator getValidator() {
        if (mValidator == null) {
            mValidator = new DefaultValidator(
                    (ValidStateManager.Provider)PseudoCompEditor.this, 
                    PseudoCompEditor.class) {
                
                public void doFastValidation() {
                    GlobalType type = getSelectedType();
                    if (type == null) {
                        addReasonKey(Severity.ERROR, "EMPTY_SELECTION"); //NOI18N
                    }
                    //
                    String name = fldName.getText();
                    if (name == null || name.length() == 0) {
                        // Check that a name is specified
                        if (mIsAttribute) {
                            addReasonKey(Severity.ERROR, "EMPTY_ATTRIBUTE_NAME"); //NOI18N
                        } else {
                            addReasonKey(Severity.ERROR, "EMPTY_ELEMENT_NAME"); //NOI18N
                        }
                    }
                    //
                    String namespace = fldNamespace.getText();
                    if (namespace == null || namespace.length() == 0) {
                        // Check that a namespace is specified
                        if (mIsAttribute) {
                            addReasonKey(Severity.WARNING, "EMPTY_ATTRIBUTE_NAMESPACE"); //NOI18N
                        } else {
                            addReasonKey(Severity.WARNING, "EMPTY_ELEMENT_NAMESPACE"); //NOI18N
                        }
                    }
                }
                
                @Override
                public void doDetailedValidation() {
                    doFastValidation();
                    //
                    String namespace = fldNamespace.getText();
                    String name = fldName.getText();
                    //
                    if (name != null || name.length() != 0) {
                        boolean hasSibling = BpelMapperUtils.hasSibling(
                                mMapperModel, mInLeftTree, mAnyLocationPath, 
                                name, namespace, mIsAttribute);
                        //
                        if (hasSibling) {
                            if (mIsAttribute) {
                                addReasonKey(Severity.ERROR, "NOT_UNIQUE_ATTRIBUTE"); //NOI18N
                            } else {
                                addReasonKey(Severity.ERROR, "NOT_UNIQUE_ELEMENT"); //NOI18N
                            }
                        }
                    }
                }
                
            };
        }
        return mValidator;
    }

    public ValidStateManager getValidStateManager(boolean isFast) {
        // Different VSM are used for fast and detailed validation because 
        // only the fast VSM should be used for enabling/disabling the Ok button.
        if (isFast) {
            if (mFastVSM == null) {
                mFastVSM = new DefaultValidStateManager();
            }
            return mFastVSM;
        } else {
            if (mFullVSM == null) {
                mFullVSM = new DefaultValidStateManager();
            }
            return mFullVSM;
        }
    }

    private class MyTree extends JTree {
        
        @Override
        public String getToolTipText(MouseEvent event) {
            //
            if (mSoaTreeModel == null) {
                return null;
            }
            //
            TreePath treePath = getPathForLocation(event.getX(), event.getY());
            if (treePath == null) {
                return null;
            }
            //
            Object value = treePath.getLastPathComponent();
            if (value == null) {
                return null;
            }
            //
            String text = mSoaTreeModel.getToolTipText(value);
            return text;
        }
    
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treeScrollPane = new javax.swing.JScrollPane();
        gElementsTree = new MyTree();
        lblName = new javax.swing.JLabel();
        fldName = new javax.swing.JTextField();
        lblNamespace = new javax.swing.JLabel();
        fldNamespace = new javax.swing.JTextField();

        treeScrollPane.setViewportView(gElementsTree);

        lblName.setText(org.openide.util.NbBundle.getMessage(PseudoCompEditor.class, "LBL_Name")); // NOI18N

        lblNamespace.setText(org.openide.util.NbBundle.getMessage(PseudoCompEditor.class, "LBL_Namespace")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, treeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, fldNamespace, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(lblName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblNamespace))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblName)
                    .add(fldName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblNamespace)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fldNamespace, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(treeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE))
        );

        lblName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PseudoCompEditor.class, "ACSN_Name")); // NOI18N
        lblName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PseudoCompEditor.class, "ACSD_Name")); // NOI18N
        fldName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PseudoCompEditor.class, "ACSN_Name")); // NOI18N
        fldName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PseudoCompEditor.class, "ACSD_Name")); // NOI18N
        fldNamespace.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PseudoCompEditor.class, "ACSN_Namespace")); // NOI18N
        fldNamespace.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PseudoCompEditor.class, "ACSD_Namespace")); // NOI18N

        getAccessibleContext().setAccessibleName("null");
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PseudoCompEditor.class).getString("ACSD_DLG_TypeCastChooser")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField fldName;
    private javax.swing.JTextField fldNamespace;
    private javax.swing.JTree gElementsTree;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNamespace;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables

}
