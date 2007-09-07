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
package org.netbeans.modules.xslt.tmap.navigator;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.nodes.LogicalTreeHandler;
import org.netbeans.modules.xslt.tmap.nodes.NavigatorNodeFactory;
import org.netbeans.modules.xslt.tmap.nodes.TransformMapNode;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapLogicalPanel extends JPanel 
     implements ExplorerManager.Provider, Lookup.Provider, HelpCtx.Provider 
{
    private static final long serialVersionUID = 1L;
    private static final String DELETE = "delete"; // NOI18N
    private static final KeyStroke DELETE_KEYSTROKE =
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0); // NOI18N
    private JLabel myMsgLabel;
    private ExplorerManager myExplorerManager;
    private LogicalTreeHandler myTreeHandler;
    
    // this navigator context lookup
    private Lookup myContextLookup;
    
    private TMapModel myModel;
    // this panel lookup
    private Lookup myLookup;

    public TMapLogicalPanel() {
        initComponent();
    }

    public void navigate(Lookup contextLookup, TMapModel model) {
// get the model and create the new logical tree in background
        if(contextLookup == null || model == null) {
            return;
        }
        myModel = model;
        myContextLookup = contextLookup;
        showWaitMsg();
        showNavTree();
        // switch navigator to the appropriate view
//        BpelNavigatorController.switchNavigatorPanel();
    }

    public ExplorerManager getExplorerManager() {
        return myExplorerManager;
    }

    private Lookup getContextLookup() {
        return myContextLookup;
    }

    public Lookup getLookup() {
        return myLookup;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    private void initComponent() {
        setLayout(new BorderLayout());
        //init empty panel
        myMsgLabel = new JLabel();
        add(myMsgLabel, BorderLayout.CENTER);
    }

    public void showWaitMsg() {
        if (SwingUtilities.isEventDispatchThread()) {
            showingWaitMessage();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showingWaitMessage();
                }
            });
        }
    }
    
    private void showingWaitMessage() {
        removeAll();
        myMsgLabel.setText(
                NbBundle.getMessage(TMapLogicalPanel.class,
                "LBL_Wait")); // NOI18N
        add(myMsgLabel, BorderLayout.CENTER);
        repaint();
    }
    
    private void showNavTree(){
        final BeanTreeView beanTree = getBeanTreeView();
        if (beanTree == null) {
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeAll();
                add(beanTree);
                revalidate();
            }
        });
    }

    private boolean initNavTree() {
        myExplorerManager = new ExplorerManager();
        if (!Model.State.VALID.equals(myModel.getState())) {
            return false;
        }
        initActionMap();
        Lookup contextLookup = getContextLookup();
        Node rootNode = NavigatorNodeFactory.getInstance()
                .getTransformMapNode(myModel,contextLookup);
        if (rootNode == null) {
            return false;
        }
        myExplorerManager.setRootContext(rootNode);
        return true;
    }
    
    private BeanTreeView getBeanTreeView() {
        boolean isInited = initNavTree();
        if (!isInited) {
            return null;
        }
        
        BeanTreeView beanTree = null;
        if (myTreeHandler != null) {
            myTreeHandler.removeListeners();
            myTreeHandler = null;
        }
        
        myTreeHandler = new LogicalTreeHandler(
                myExplorerManager,
                myModel,
                getContextLookup());
                
        beanTree = myTreeHandler.getBeanTreeView();
        return beanTree;
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
//        actionMap.put(GOTOSOURCE,SystemAction.get(GoToSourceAction.class));
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
//        keys.put(GOTOSOURCE_KEYSTROKE, GOTOSOURCE); // NOI18N
//        keys.put(GOTODIAGRAMM_KEYSTROKE, GOTODIAGRAMM); // NOI18N
//////        keys.put((KeyStroke) SystemAction.get(FindUsagesAction.class)
//////            .getValue(FindUsagesAction.ACCELERATOR_KEY), FINDUSAGES); // NOI18N
        
        // ...and initialization of lookup variable

        //myLookup = ExplorerUtils.createLookup(myExplorerManager, actionMap);
        myLookup = new ProxyLookup(myContextLookup, ExplorerUtils.createLookup(myExplorerManager, actionMap));
    }

}

