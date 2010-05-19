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
package org.netbeans.modules.xslt.tmap.ui.editors;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.Collections;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.form.ChooserLifeCycle;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.soa.ui.tree.DataObjectHolder;
import org.netbeans.modules.soa.ui.tree.ExtTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.soa.ui.tree.impl.SoaTreeCellRenderer;
import org.netbeans.modules.soa.ui.tree.impl.SoaTreeModelImpl;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @author  nk160297
 */
public class ServiceParamChooser extends JPanel
        implements ChooserLifeCycle<WSDLComponent>,
        Validator.Provider, ValidStateManager.Provider {

    private static final long serialVersionUID = 4953224310870135745L;
    private TMapModel myTMapModel;
    private Class<? extends WSDLComponent> myLeafComponent;
    private DefaultValidator myValidator;
    private ValidStateManager myVSM;

//    protected ServiceParamChooser() {
//        super();
//    }
//    
    public ServiceParamChooser(TMapModel TMapModel,
            Class<? extends WSDLComponent> leafComponent, PortType branchTreeFilter) {
        myTMapModel = TMapModel;
        myLeafComponent = leafComponent;
        //
        createContent(branchTreeFilter);
        initControls();
    }

    public ServiceParamChooser(TMapModel TMapModel,
            Class<? extends WSDLComponent> leafComponent) {
        this(TMapModel, leafComponent, null);
    }

    public void createContent() {
        createContent(null);
    }
    
    public void createContent(PortType branchTreeFilter) {
        assert EventQueue.isDispatchThread();
        initComponents();
        //
        SoaTreeModelImpl tModel =
                new SoaTreeModelImpl(new ServiceParamTreeModel(myTMapModel,
                myLeafComponent, branchTreeFilter));
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
        
        if (branchTreeFilter != null) {
            setSelectedValue(branchTreeFilter, true);
        }
    }

    public boolean initControls() {
        getValidator().revalidate(true);
        return true;
    }

    public void setSelectedValue(WSDLComponent newValue) {
        setSelectedValue(newValue, false);
    }
    
    public void setSelectedValue(WSDLComponent newValue, boolean isExpand) {
        if (newValue == null) {
            return;
        }
        
        TreeModel tModel = subtypesTree.getModel();
        assert tModel instanceof ExtTreeModel;
        WSDLComponentFinder finder = new WSDLComponentFinder(newValue);
        TreeFinderProcessor findProc = new TreeFinderProcessor(
                (ExtTreeModel) tModel);
        TreePath gTypePath = findProc.findFirstNode(
                Collections.singletonList((TreeItemFinder) finder));
        if (gTypePath != null) {
            subtypesTree.setSelectionPath(gTypePath);
            subtypesTree.expandPath(gTypePath);
        } else {
            subtypesTree.setSelectionRow(0); // select the root
        }
    }

    public WSDLComponent getSelectedValue() {
        TreePath selection = subtypesTree.getSelectionPath();
        if (selection != null) {
            Object selectedItem = selection.getLastPathComponent();
            if (selectedItem != null && selectedItem instanceof DataObjectHolder) {
                Object dataObj =
                        ((DataObjectHolder) selectedItem).getDataObject();
                if (dataObj instanceof WSDLComponent) {
                    return (WSDLComponent) dataObj;
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
    public static boolean showDlg(ServiceParamChooser editor) {
        String dlgTitle = NbBundle.getMessage(ServiceParamChooser.class,
                "SERVICE_PARAM_CHOOSER_TITLE"); // NOI18N
        DefaultDialogDescriptor descriptor =
                new DefaultDialogDescriptor(editor, dlgTitle);
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        SoaUtil.setInitialFocusComponentFor(editor);
        dialog.setVisible(true);

        return descriptor.isOkHasPressed();
    }

    public Validator getValidator() {
        if (myValidator == null) {
            myValidator = new DefaultValidator(
                    (ValidStateManager.Provider) ServiceParamChooser.this,
                    ServiceParamChooser.class) {

                public void doFastValidation() {
                    WSDLComponent wsdlComponent = getSelectedValue();
                    if (wsdlComponent == null) {
                        addReasonKey(Severity.ERROR, "MSG_EMPTY_SELECTION"); //NOI18N
                    } else if (!myLeafComponent.isAssignableFrom(wsdlComponent.
                            getClass())) {
                        addReasonKey(Severity.ERROR, "MSG_WRONG_SELECTION",
                                myLeafComponent.getName()); //NOI18N
                    }
                }
            };
        }
        return myValidator;
    }

    public ValidStateManager getValidStateManager(boolean isFast) {
        // Use the same for the fast and detailed validation
        if (myVSM == null) {
            myVSM = new DefaultValidStateManager();
        }
        return myVSM;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblParamChooserTree = new javax.swing.JLabel();
        treeScrollPane = new javax.swing.JScrollPane();
        subtypesTree = new javax.swing.JTree();

        lblParamChooserTree.setDisplayedMnemonic('T');
        lblParamChooserTree.setLabelFor(subtypesTree);
        lblParamChooserTree.setText(org.openide.util.NbBundle.getMessage(ServiceParamChooser.class, "SERVICE_PARAM_CHOOSER_LABEL")); // NOI18N

        treeScrollPane.setViewportView(subtypesTree);
        subtypesTree.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServiceParamChooser.class, "ACSN_ServiceParamChooserTree")); // NOI18N
        subtypesTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceParamChooser.class, "ACSD_ServiceParamChooserTree")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(treeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                    .add(lblParamChooserTree))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(lblParamChooserTree)
                .add(18, 18, 18)
                .add(treeScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                .addContainerGap())
        );

        lblParamChooserTree.getAccessibleContext().setAccessibleName("lblParamChooserTree");
        lblParamChooserTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceParamChooser.class, "SERVICE_PARAM_CHOOSER_LABEL")); // NOI18N
        lblParamChooserTree.getAccessibleContext().setAccessibleParent(subtypesTree);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServiceParamChooser.class, "ACSN_ServiceParamChooser")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServiceParamChooser.class, "ACSD_ServiceParamChooser")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblParamChooserTree;
    private javax.swing.JTree subtypesTree;
    private javax.swing.JScrollPane treeScrollPane;
    // End of variables declaration//GEN-END:variables
}
