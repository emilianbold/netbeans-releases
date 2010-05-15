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
package org.netbeans.modules.soa.xpath.mapper.lsm.ui.predicate;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPredicate;
import org.netbeans.modules.soa.xpath.mapper.model.AbstractModelUpdater;
import org.netbeans.modules.soa.xpath.mapper.model.MapperFactory;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.model.updater.GraphInfoCollector;
import org.netbeans.modules.soa.xpath.mapper.tree.FinderListBuilder;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.openide.util.NbBundle;

/**
 *
 * @author  nk160297
 */
public abstract class PredicateEditor extends EditorLifeCycleAdapter
        implements Validator.Provider, ValidStateManager.Provider, 
        MapperFactory, AbstractModelUpdater.Constructor,
        MapperPredicate.Calculator {
    
    private Mapper mMapper;
    protected XPathMapperModel mMapperModel;
    protected XPathSchemaContext mSchContext;
    private MapperPredicate mPred;
    private String mDlgTitle;
    
    private DefaultValidator mValidator;
    private ValidStateManager mVSM;
    protected MapperStaticContext mStContext;
    
    public PredicateEditor(XPathSchemaContext schContext, MapperPredicate pred,
             XPathMapperModel mapperModel, MapperStaticContext stContext) {
        //
        mSchContext = schContext;
        mPred = pred;
        mMapperModel = mapperModel;
        mStContext = stContext;
        //
        // Following methods has to be called in constructors of derived classes!
        // createContent();
        // initControls();
    }
    
    public Mapper getMapper() {
        return mMapper;
    }
    
    @Override
    public void createContent() {
        assert EventQueue.isDispatchThread();
        mMapper = createMapper(mMapperModel);
        initComponents();
        //
        mDlgTitle = NbBundle.getMessage(PredicateEditor.class,
            "PREDICATE_DLG_TITLE"); // NOI18N
        //
        btnGoToContext.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
  //              if (mSContext != null) {
                    showSchemaContextInSourceTree();
 //              }
            }
        });
        SoaUtil.activateInlineMnemonics(this);
    }

    @Override
    public boolean initControls() {
        if (mSchContext != null) {
            String predContext = calculatePredContextStr();
            fldContext.setText(predContext);
            //
            showSchemaContextInSourceTree();
            mMapper.expandNonEmptyGraphs();
            mMapper.expandMappedLeftTreeItems();            
        } else {
            fldContext.setText(NbBundle.getMessage(PredicateEditor.class,
            "UNKNOWN_SCHEMA_CONTEXT"));
        }
        return true;
    }
    
    public String getDlgTitle() {
        return mDlgTitle;
    }
    
    /**
     * Returns true if the user press Ok
     * @param editor
     * @return
     */
    public static boolean showDlg(PredicateEditor editor,
            Callable<Boolean> okProcessor) {
        DefaultDialogDescriptor descriptor = 
                new DefaultDialogDescriptor(editor, editor.getDlgTitle());
        descriptor.setOkButtonProcessor(okProcessor);
        //
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        SoaUtil.setInitialFocusComponentFor(editor);
        //
        dialog.setVisible(true);
        return descriptor.isOkHasPressed();
    }

    protected abstract String calculatePredContextStr();
    
    protected abstract JPanel createPalette();

//    public abstract void addPredicateCondition();
//
//    public abstract void deletePredicateCondition();
    
    /**
     * Try open source tree and show the Schema component that the 
     * schema context points to.
     */
    private void showSchemaContextInSourceTree() {
        // TODO: A Variable or a part has to be passed here 
        // TODO: Need constructing finders here
        if (mSchContext == null) {
            return;
        }
        //
        // Prepare finders' list
        FinderListBuilder flb = mMapperModel.getLeftTreeModel().getFinderListBuilder();
        //
        List<TreeItemFinder> finderList =  (mSchContext instanceof PredicatedSchemaContext) ?
                flb.build(((PredicatedSchemaContext)mSchContext).getBaseContext()) :
                flb.build(mSchContext);
        //
        // Look for the tree node
        TreeModel leftTreeModel = mMapperModel.getLeftTreeModel();
        assert leftTreeModel instanceof MapperSwingTreeModel;
        TreeFinderProcessor fProcessor = new TreeFinderProcessor(
                (MapperSwingTreeModel)leftTreeModel);
        TreePath schemaContextPath = fProcessor.findFirstNode(finderList);
//        TreePath schemaContextPath = ((BpelMapperSwingTreeModel)leftTreeModel).
//                findFirstNode(finderList);
        //
        // Show context path
        if (schemaContextPath != null) {
            JTree tree = mMapper.getLeftTree();
            tree.expandPath(schemaContextPath);
            tree.scrollPathToVisible(schemaContextPath);
            tree.setSelectionPath(schemaContextPath);
        }
       
    }
    
    public ValidStateManager getValidStateManager(boolean isFast) {
        if (isFast) {
            // Only detailed validation is supported here
            return null;
        }
        if (mVSM == null) {
            mVSM = new DefaultValidStateManager();
        }
        return mVSM;
    }

    public Validator getValidator() {
        if (mValidator == null) {
            mValidator = new DefaultValidator(
                    (ValidStateManager.Provider)PredicateEditor.this, 
                    PredicateEditor.class) {
                
                public void doFastValidation() {
                }
                
                @Override
                public void doDetailedValidation() {
                    //
                    assert mMapperModel instanceof XPathMapperModel;
                    Map<TreePath, Graph> notEmptyGraphs = 
                            ((XPathMapperModel)mMapperModel).getGraphsInside(null);
                    //
                    int unconnectedGraphs = 0; // Graphs without connection to the right tree
                    int incompleteGraphs = 0; // Graphs with unconnected vertices (group of vertices)
                    //
                    for (Graph graph : notEmptyGraphs.values()) {
                        //
                        // The XPath mapper related GraphInfoCollector is used here
                        // because its functions are enough for the validation.
                        GraphInfoCollector graphInfo = new GraphInfoCollector(graph);
                        if (graphInfo.noLinksAtAll()) {
                            unconnectedGraphs++;
                        }
                        ArrayList<Vertex> sRoots = graphInfo.getSecondryRoots();
                        if (sRoots != null && !sRoots.isEmpty()) {
                            incompleteGraphs++;
                        }
                    }
                    //
                    if (notEmptyGraphs.size() == unconnectedGraphs) {
                        addReasonKey(Severity.ERROR,
                                "WARN_THERE_ARENT_ANY_CONNECTED_GRAPH"); //NOI18N
                    } else if (unconnectedGraphs > 0) {
                        addReasonKey(Severity.WARNING,
                                "WARN_THERE_ARE_UNCONNECTED_GRAPHS"); //NOI18N
                    }
                    if (incompleteGraphs > 0) {
                        addReasonKey(Severity.WARNING, 
                                "WARN_THERE_ARE_INCOMPLETE_GRAPHS"); //NOI18N
                    }
                }
            };
        }
        return mValidator;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMenu = createPalette();
        pnlCenter = new javax.swing.JPanel();
        javax.swing.JPanel pnlMapper = mMapper;
        lblContext = new javax.swing.JLabel();
        fldContext = new javax.swing.JTextField();
        btnGoToContext = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(1000, 600));
        setLayout(new java.awt.BorderLayout());
        add(pnlMenu, java.awt.BorderLayout.NORTH);

        pnlMapper.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Separator.shadow")));

        lblContext.setLabelFor(fldContext);
        lblContext.setText(org.openide.util.NbBundle.getMessage(PredicateEditor.class, "LBL_Context")); // NOI18N

        fldContext.setEditable(false);

        btnGoToContext.setText(org.openide.util.NbBundle.getMessage(PredicateEditor.class, "BTN_Context")); // NOI18N

        org.jdesktop.layout.GroupLayout pnlCenterLayout = new org.jdesktop.layout.GroupLayout(pnlCenter);
        pnlCenter.setLayout(pnlCenterLayout);
        pnlCenterLayout.setHorizontalGroup(
            pnlCenterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1000, Short.MAX_VALUE)
            .add(pnlCenterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pnlCenterLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(pnlCenterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pnlMapper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 976, Short.MAX_VALUE)
                        .add(pnlCenterLayout.createSequentialGroup()
                            .add(lblContext)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(fldContext, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(btnGoToContext)))
                    .addContainerGap()))
        );
        pnlCenterLayout.setVerticalGroup(
            pnlCenterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 630, Short.MAX_VALUE)
            .add(pnlCenterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pnlCenterLayout.createSequentialGroup()
                    .add(2, 2, 2)
                    .add(pnlMapper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(pnlCenterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(lblContext)
                        .add(btnGoToContext)
                        .add(fldContext, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(2, 2, 2)))
        );

        fldContext.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PredicateEditor.class).getString("ACSN_TXTFLD_SchemaContext")); // NOI18N
        fldContext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PredicateEditor.class).getString("ACSD_TXTFLD_SchemaContext")); // NOI18N
        btnGoToContext.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PredicateEditor.class).getString("ACSN_BTN_SetFocus")); // NOI18N
        btnGoToContext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PredicateEditor.class).getString("ACSD_BTN_SetFocus")); // NOI18N

        add(pnlCenter, java.awt.BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PredicateEditor.class).getString("ACSN_DLG_PredicateEditor")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PredicateEditor.class).getString("ACSD_DLG_PredicateEditor")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGoToContext;
    private javax.swing.JTextField fldContext;
    private javax.swing.JLabel lblContext;
    private javax.swing.JPanel pnlCenter;
    private javax.swing.JPanel pnlMenu;
    // End of variables declaration//GEN-END:variables

}
