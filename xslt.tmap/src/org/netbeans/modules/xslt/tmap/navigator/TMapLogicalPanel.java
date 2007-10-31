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
        Thread treeThread = new Thread(new Runnable() {
            public void run() {
                showNavTree();
            }
        });
        treeThread.start();
        
// switch navigator to the appropriate view
//        BpelNavigatorController.switchNavigatorPanel();
    }
    
    protected TMapModel getModel() {
        return myModel;
    }

    public ExplorerManager getExplorerManager() {
        return myExplorerManager;
    }

    protected Lookup getContextLookup() {
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

    protected boolean initNavTree() {
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
    
    protected BeanTreeView getBeanTreeView() {
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
    
    protected void initActionMap() {
        ActionMap actionMap = getActionMap();
        
        
        // TODO add delete and some else actions support
//        actionMap.put(DefaultEditorKit.copyAction,
//            ExplorerUtils.actionCopy(myExplorerManager));
//        actionMap.put(DefaultEditorKit.cutAction,
//            ExplorerUtils.actionCut(myExplorerManager));
//        actionMap.put(DefaultEditorKit.pasteAction,
//            ExplorerUtils.actionPaste(myExplorerManager));
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

