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

import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.Collections;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.tree.search.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.tree.spi.ExtTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemFinder;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.form.ChooserLifeCycle;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.soa.ui.tree.DataObjectHolder;
import org.netbeans.modules.soa.ui.tree.impl.SoaTreeCellRenderer;
import org.netbeans.modules.soa.ui.tree.impl.SoaTreeModelImpl;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.openide.util.NbBundle;

/**
 *
 * @author  nk160297
 */
public class SubtypeChooser extends JPanel 
        implements ChooserLifeCycle<GlobalType>, 
        Validator.Provider, ValidStateManager.Provider  {

    private GlobalType mRootGType;
    private BpelModel mBpelModel;
    
    private DefaultValidator mValidator;
    private ValidStateManager mVSM;
    
    public SubtypeChooser(GlobalType gRootType, BpelModel bpelModel) {
        mRootGType = gRootType;
        mBpelModel = bpelModel;
        //
        createContent();
        initControls();
    }
    
    public void createContent() {
        assert EventQueue.isDispatchThread();
        initComponents();
        //
        SoaTreeModelImpl tModel = 
                new SoaTreeModelImpl(new TypeCastTreeModel(mRootGType, mBpelModel));
        subtypesTree.setModel(tModel);
        subtypesTree.setRootVisible(true);
        subtypesTree.setShowsRootHandles(false);
        //
        subtypesTree.setCellRenderer(new SoaTreeCellRenderer(tModel));
        //
        subtypesTree.getSelectionModel().addTreeSelectionListener(
                new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
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

    public boolean initControls() {
        getValidator().revalidate(true);
        return true;
    }
    
    public void setSelectedValue(GlobalType newValue) {
        TreeModel tModel = subtypesTree.getModel();
        assert tModel instanceof ExtTreeModel;
        GlobalSchemaComponentFinder finder = new GlobalSchemaComponentFinder(newValue);
        TreeFinderProcessor findProc = new TreeFinderProcessor((ExtTreeModel)tModel);
        TreePath gTypePath = findProc.findFirstNode(
                Collections.singletonList((TreeItemFinder)finder));
        if (gTypePath != null) {
            subtypesTree.setSelectionPath(gTypePath);
        } else {
            subtypesTree.setSelectionRow(0); // select the root
        }
    }

    public GlobalType getSelectedValue() {
        TreePath selection = subtypesTree.getSelectionPath();
        if (selection != null) {
            Object selectedItem = selection.getLastPathComponent();
            if (selectedItem != null && selectedItem instanceof DataObjectHolder) {
                Object dataObj = ((DataObjectHolder)selectedItem).getDataObject();
                if (dataObj != null && dataObj instanceof GlobalType) {
                    return (GlobalType)dataObj;
                }
            }
        }
        return null;
    }

    public boolean subscribeListeners() {
        return true;
    }

    public boolean unsubscribeListeners() {
        return true;
    }

    public boolean afterClose() {
        return true;
    }

    /**
     * Returns true if the user press Ok
     * @param editor
     * @return
     */
    public static boolean showDlg(SubtypeChooser editor) {
        String dlgTitle = NbBundle.getMessage(SubtypeChooser.class,
            "SUBTYPE_CHOOSER_TITLE"); // NOI18N
        DefaultDialogDescriptor descriptor = 
                new DefaultDialogDescriptor(editor, dlgTitle);
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        SoaUtil.setInitialFocusComponentFor(editor);
        dialog.setVisible(true);

        return descriptor.isOkHasPressed();
    }
    
    public Validator getValidator() {
        if (mValidator == null) {
            mValidator = new DefaultValidator(
                    (ValidStateManager.Provider)SubtypeChooser.this, 
                    SubtypeChooser.class) {
                
                public void doFastValidation() {
                    GlobalType currGType = getSelectedValue();
                    if (currGType == null) {
                        addReasonKey(Severity.ERROR, "EMPTY_SELECTION"); //NOI18N
                    } else if (currGType.equals(mRootGType)) {
                        addReasonKey(Severity.ERROR, "CAST_TO_SELF"); //NOI18N
                    }
                }
                
            };
        }
        return mValidator;
    }

    public ValidStateManager getValidStateManager(boolean isFast) {
        // Use the same for the fast and detailed validation
        if (mVSM == null) {
            mVSM = new DefaultValidStateManager();
        }
        return mVSM;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treeScrollPane = new javax.swing.JScrollPane();
        subtypesTree = new javax.swing.JTree();

        treeScrollPane.setViewportView(subtypesTree);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(treeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(treeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(SubtypeChooser.class).getString("ACSN_DLG_TypeCastChooser")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(SubtypeChooser.class).getString("ACSD_DLG_TypeCastChooser")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree subtypesTree;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables

}
