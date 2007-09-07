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
package org.netbeans.modules.bpel.navigator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.util.Date;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.KeyStroke;

import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.nodes.BpelProcessNode;
import org.netbeans.modules.bpel.nodes.actions.FindUsagesAction;
import org.netbeans.modules.bpel.nodes.actions.GoToDiagrammAction;
import org.netbeans.modules.bpel.nodes.actions.GoToSourceAction;
import org.netbeans.modules.bpel.nodes.validation.ValidationProxyListener;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.openide.util.HelpCtx;

import org.openide.util.NbBundle;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class BpelNavigatorVisualPanel extends JPanel
        implements ExplorerManager.Provider, Lookup.Provider, HelpCtx.Provider 
{
    
    private static final long serialVersionUID = 1L;
    private static final String DELETE = "delete"; // NOI18N
    private static final KeyStroke DELETE_KEYSTROKE =
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0); // NOI18N
    private static final String GOTOSOURCE = "gotosource"; // NOI18N
    private static final String GOTODIAGRAMM = "gotodiagramm"; // NOI18N
////    private static final String FINDUSAGES = "findusages"; // NOI18N
    private static final KeyStroke GOTOSOURCE_KEYSTROKE =
            KeyStroke.getKeyStroke(GoToSourceAction.GOTOSOURCE_KEYSTROKE);
//    private static final KeyStroke GOTODIAGRAMM_KEYSTROKE =
//            KeyStroke.getKeyStroke(GoToDiagrammAction.GOTODIAGRAMM_KEYSTROKE);
    
    private JLabel myMsgLabel;
    //context Lookup - should contains current
    private Lookup myContextLookup;
    private BpelModel myBpelModel;
    private Lookup myLookup;
    private ExplorerManager myExplorerManager;
    private ValidationProxyListener myVpl;
    
    private BpelModelLogicalBeanTree myBpelModelLogicalBeanTree;
    private boolean isRequireRepaint;
    
    
    public BpelNavigatorVisualPanel() {
        initComponent();
    }
    
    private void initComponent() {
        setLayout(new BorderLayout());
        //init empty panel
        myMsgLabel = new JLabel();
        add(myMsgLabel, BorderLayout.CENTER);
    }
    
    private void initActionMap() {
        ActionMap actionMap = getActionMap();
        
        
        // TODO add delete and some else actions support
////        actionMap.put(DefaultEditorKit.copyAction,
////            ExplorerUtils.actionCopy(myExplorerManager));
////        actionMap.put(DefaultEditorKit.cutAction,
////            ExplorerUtils.actionCut(myExplorerManager));
////        actionMap.put(DefaultEditorKit.pasteAction,
////            ExplorerUtils.actionPaste(myExplorerManager));
        actionMap.put(GOTOSOURCE,SystemAction.get(GoToSourceAction.class));
//        actionMap.put(GOTODIAGRAMM,SystemAction.get(GoToDiagrammAction.class));
        actionMap.put(DELETE, // NOI18N
                ExplorerUtils.actionDelete(myExplorerManager, true));
//////        actionMap.put(FINDUSAGES, SystemAction.get(FindUsagesAction.class));
////
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
////        keys.put(KeyStroke.getKeyStroke("control C"), DefaultEditorKit.copyAction);// NOI18N
////        keys.put(KeyStroke.getKeyStroke("control X"), DefaultEditorKit.cutAction);// NOI18N
////        keys.put(KeyStroke.getKeyStroke("control V"), DefaultEditorKit.pasteAction);// NOI18N
        keys.put(DELETE_KEYSTROKE, DELETE); // NOI18N
        keys.put(GOTOSOURCE_KEYSTROKE, GOTOSOURCE); // NOI18N
//        keys.put(GOTODIAGRAMM_KEYSTROKE, GOTODIAGRAMM); // NOI18N
//////        keys.put((KeyStroke) SystemAction.get(FindUsagesAction.class)
//////            .getValue(FindUsagesAction.ACCELERATOR_KEY), FINDUSAGES); // NOI18N
        
        // ...and initialization of lookup variable
        myLookup = ExplorerUtils.createLookup(myExplorerManager, actionMap);
    }
    
    public void emptyPanel() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeAll();
                repaint();
            }
        });
    }
    
    public void navigate(final Lookup context, final BpelModel bpelModel) {
// get the model and create the new bpel logical tree in background
        if(bpelModel == null) {
            return;
        }
        myBpelModel = bpelModel;
        myContextLookup = context;
        initValidation();
        showWaitMsg();
        Thread navThread = new Thread(new Runnable() {
            public void run() {
                showNavTree();
            }
        });
        
        navThread.start();
        // switch navigator to the appropriate view
        BpelNavigatorController.switchNavigatorPanel();
    }
    
    private void initValidation() {
        myVpl = myContextLookup.lookup(ValidationProxyListener.class);
        if (myVpl == null) {
            myVpl = ValidationProxyListener.getInstance(myContextLookup);
            if (myVpl != null) {
                myContextLookup = new ExtendedLookup(myContextLookup,myVpl);
            }
        }
    }
    
    private void showNavTree(){
        myExplorerManager = new ExplorerManager();
        initActionMap();
//////                myExplorerManager.setRootContext(getProcessNode());
        Node rootNode = NavigatorNodeFactory.getInstance()
            .getProcessNode(myBpelModel,getContextLookup());
        if (rootNode instanceof BpelProcessNode) {
            myExplorerManager.setRootContext(rootNode);
        }

        if (myBpelModelLogicalBeanTree != null) {
            myBpelModelLogicalBeanTree.removeListeners();
        }

        myBpelModelLogicalBeanTree = new BpelModelLogicalBeanTree(
                myExplorerManager,
                myBpelModel,
                getContextLookup());

        final BeanTreeView treeView = myBpelModelLogicalBeanTree.getBeanTreeView();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeAll();
                add(treeView);
                revalidate();
            }
        });
    }
    
    public void showWaitMsg() {
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeAll();
                myMsgLabel.setText(
                        NbBundle.getMessage(BpelNavigatorVisualPanel.class,
                        "LBL_Wait")); // NOI18N
                add(myMsgLabel, BorderLayout.CENTER);
                repaint();
            }
        });
        
    }
    
    
    public ExplorerManager getExplorerManager() {
        return myExplorerManager;
    }
    
    public Lookup getLookup() {
        return myLookup;
    }
    
    public void addNotify() {
        super.addNotify();
//        ExplorerUtils.activateActions(myExplorerManager, true);
    }
    
    public void removeNotify() {
//        ExplorerUtils.activateActions(myExplorerManager, false);
        super.removeNotify();
    }
    
    private Lookup getContextLookup() {
        return myContextLookup;
    }
    
    public HelpCtx getHelpCtx() {
        if (myExplorerManager != null ) {
            Node[] selNodes = myExplorerManager.getSelectedNodes();
            if (selNodes != null && selNodes.length > 0) {
                HelpCtx helpCtx = selNodes[0].getHelpCtx();
                if (helpCtx != null) {
                    return helpCtx;
                }
            }
        }
        return new HelpCtx(BpelNavigatorVisualPanel.class);
    }

    private JTree getJTree() {
        if (myBpelModelLogicalBeanTree == null) {
            return null;
        }
        return getJTree(myBpelModelLogicalBeanTree.getBeanTreeView());
    }
    
    private JTree getJTree(java.awt.Component parent) {
        if (parent instanceof JTree ) {
            return (JTree)parent;
        }
        
        if (! (parent instanceof java.awt.Container)) {
            return null;
        }
        
        java.awt.Component[] comps = ((java.awt.Container)parent).getComponents();
        JTree tmpTree = null;
        for (java.awt.Component elem : comps) {
            tmpTree = getJTree(elem);
            if (tmpTree != null) {
                return tmpTree;
            }
        }
        
        return null;
    }
}
