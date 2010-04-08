/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.search.api.SearchManager;
import org.netbeans.modules.xml.validation.ui.ShowCookie;
import org.netbeans.modules.xml.validation.action.ValidateAction;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.cookies.RefreshExtensibilityElementNodeCookie;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLSettings.ViewMode;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.category.Category;
import org.netbeans.modules.xml.xam.ui.category.CategoryPane;
import org.netbeans.modules.xml.xam.ui.category.DefaultCategoryPane;
import org.netbeans.modules.xml.xam.ui.multiview.ActivatedNodesMediator;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.openide.actions.FindAction;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WSDLTreeViewMultiViewElement extends TopComponent
        implements MultiViewElement, ExplorerManager.Provider, PropertyChangeListener {
    private static final long serialVersionUID = -655912409997381426L;
    private static final String ACTIVATED_NODES = "activatedNodes";//NOI18N
    private ExplorerManager manager;
    private WSDLDataObject mObj;
    private CategoryPane categoryPane;
    private transient MultiViewElementCallback multiViewObserver;
    private transient javax.swing.JLabel errorLabel = new javax.swing.JLabel();
    private transient JToolBar mToolbar;
    private ActivatedNodesMediator nodesMediator;
    private CookieProxyLookup cpl;
    private WSDLModel wsdlModel;

    public WSDLTreeViewMultiViewElement() {
        super();
    }

    public WSDLTreeViewMultiViewElement(WSDLDataObject dObj) {
        super();
        this.mObj = dObj;
        initialize();
    }

    private void initialize() {
        manager = new ExplorerManager();
        // Install our own actions.
        CallbackSystemAction globalFindAction = SystemAction.get(FindAction.class);
        Object mapKey = globalFindAction.getActionMapKey();
        Action action = new WSDLFindAction();
        ActionMap map = getActionMap();
        map.put(mapKey, action);
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, false));

        // Define the keyboard shortcuts for the actions.
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = (KeyStroke) globalFindAction.getValue(Action.ACCELERATOR_KEY);
        if (key == null) {
            key = KeyStroke.getKeyStroke("control F");
        }
        keys.put(key, mapKey);

        //show cookie
        ShowCookie showCookie = new ShowCookie() {

            public void show(ResultItem resultItem) {
                Component component = resultItem.getComponents();
                if (categoryPane != null && component instanceof DocumentComponent) {
                    categoryPane.getCategory().showComponent(component);
                }
            }             
        };
        
        //Refresh Nodes cookie
        RefreshExtensibilityElementNodeCookie reenc = new RefreshExtensibilityElementNodeCookie() {

            public void refresh() {
                if (categoryPane != null) {
                    Collection<? extends RefreshExtensibilityElementNodeCookie> lookupAll = categoryPane.getCategory().getLookup().lookupAll(RefreshExtensibilityElementNodeCookie.class);
                    for (RefreshExtensibilityElementNodeCookie cookie : lookupAll) {
                        if (cookie != null && cookie != this) {
                            cookie.refresh();
                        }
                    }
                }
            }
        };

        Node delegate = mObj.getNodeDelegate();
        nodesMediator = new ActivatedNodesMediator(delegate);
        nodesMediator.setExplorerManager(this);
        cpl = new CookieProxyLookup(new Lookup[] {
                Lookups.fixed(new Object[] {
                        // Need ActionMap in lookup so our actions are used.
                        map,
                        // Need the data object registered in the lookup so that the
                        // projectui code will close our open editor windows when the
                        // project is closed.
                        mObj,
                        // The Show Cookie in lookup to show the component
                        showCookie,
                        reenc
                }),
                nodesMediator.getLookup(),
                // The Node delegate Lookup must be the last one in the list
                // for the CookieProxyLookup to work properly.
                delegate.getLookup(),
        }, delegate);
        associateLookup(cpl);
        addPropertyChangeListener(ACTIVATED_NODES, nodesMediator);
        addPropertyChangeListener(ACTIVATED_NODES, cpl);

        setLayout(new BorderLayout());
    }

    
    private WSDLModel getWSDLModel() {
        if (wsdlModel != null) {
            return wsdlModel;
        }
        wsdlModel = mObj.getWSDLEditorSupport().getModel();
        wsdlModel.addPropertyChangeListener(WeakListeners.propertyChange(this, wsdlModel));
        return wsdlModel;
    }
    
    private void cleanup() {
        try {
            manager.setSelectedNodes(new Node[0]);
        } catch (PropertyVetoException e) {
        }
        removePropertyChangeListener(ACTIVATED_NODES, nodesMediator);
        removePropertyChangeListener(ACTIVATED_NODES, cpl);
        nodesMediator.setExplorerManager(null);
        nodesMediator = null;
        cpl = null;
        
        //TODO: try to clean the category.
        categoryPane = null;
        if (mToolbar != null) mToolbar.removeAll();
        mToolbar = null;
        removeAll();
        ExplorerUtils.activateActions(manager, false);
        manager = null;
        multiViewObserver = null;
        setActivatedNodes(new Node[0]);
        
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (WSDLModel.STATE_PROPERTY.equals(evt.getPropertyName())) {
            WSDLModel.State state = (WSDLModel.State) evt.getNewValue();
            if (state != null) {
                //IZ 148214 Call initui in event queue
                initUIInAWTThread();
            }
        }
    }
    
    private void initUIInAWTThread() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    initUI();
                }
            });
        } else {
            initUI();
        }
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    public void setMultiViewCallback(final MultiViewElementCallback callback) {
        multiViewObserver = callback;
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!WSDLEditorSupport.isLastView(multiViewObserver.getTopComponent())) {
            return CloseOperationState.STATE_OK;
        }
        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                "ID_TEXT_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }

    @Override
    public UndoRedo getUndoRedo() {
    return mObj.getWSDLEditorSupport().getUndoManager();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        if (categoryPane != null) {
            Category cat = categoryPane.getCategory();
            if (cat != null) {
                cat.componentHidden();
            }
        }
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        cleanup();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        initUI();
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(manager, true);
        WSDLMultiViewFactory.updateGroupVisibility(WSDLTreeViewMultiViewDesc.PREFERRED_ID);
        // Ensure the graph widgets have the focus.
        // Also helps to make F1 open the correct help topic.
        if (categoryPane != null) {
            categoryPane.getComponent().requestFocusInWindow();
        }
    }
    
    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
        if (manager != null) {
            ExplorerUtils.activateActions(manager, false);
        }
        WSDLMultiViewFactory.updateGroupVisibility(WSDLTreeViewMultiViewDesc.PREFERRED_ID);
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();

        if (categoryPane != null) {
            Category cat = categoryPane.getCategory();
            if (cat != null) {
                cat.componentShown();
            }
        }
    }

    @Override
    public void requestActive() {
        super.requestActive();
        // For Help to work properly, need to take focus.
        if (categoryPane != null) {
            categoryPane.getComponent().requestFocus();
        }
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(WSDLTreeViewMultiViewDesc.class);
    }

    @Override
    protected String preferredID() {
        return "WSDLTreeViewMultiViewElementTC";  //  NOI18N
    }

    /**
     * Construct the user interface.
     */
    private void initUI() {
        WSDLModel wsdlModel = getWSDLModel();
        if (mToolbar != null) {
            mToolbar.removeAll();
            mToolbar = null;
        }
        if (wsdlModel != null &&
                wsdlModel.getState() == WSDLModel.State.VALID) {
            // Construct the standard editor interface.
            if (categoryPane == null) {
                Lookup lookup = getLookup();
                categoryPane = new DefaultCategoryPane();
                Category tree = new WSDLTreeCategory(wsdlModel, lookup);
                categoryPane.addCategory(tree);
                Category columns = new WSDLColumnsCategory(wsdlModel, lookup);
                categoryPane.addCategory(columns);
                // Set the default view according to the persisted setting.
                ViewMode mode = WSDLSettings.getDefault().getViewMode();
                switch (mode) {
                case COLUMN:
                    categoryPane.setCategory(columns);
                    break;
                case TREE:
                    categoryPane.setCategory(tree);
                    break;
                }
            }
            removeAll();
            add(categoryPane.getComponent(), BorderLayout.CENTER);
            return;
        }

        String errorMessage = null;
        // If it comes here, either the model is not well-formed or invalid.
        if (wsdlModel == null ||
                wsdlModel.getState() == WSDLModel.State.NOT_WELL_FORMED) {
            errorMessage = NbBundle.getMessage(
                    WSDLTreeViewMultiViewElement.class,
                    "MSG_NotWellformedWsdl");
        }

        // Clear the interface and show the error message.
        removeAll();
        errorLabel.setText("<" + errorMessage + ">");
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        errorLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        errorLabel.setBackground(usualWindowBkg != null ? usualWindowBkg :
            Color.white);
        errorLabel.setOpaque(true);
        add(errorLabel, BorderLayout.CENTER);
    }

    public javax.swing.JComponent getToolbarRepresentation() {
        //When IDE loads existing files, the returning null toolbar throws AssertionError.
        //Create a toolbar regardless of if model is valid or not
        if (mToolbar == null) {
            mToolbar = new JToolBar();
            mToolbar.setFloatable(false);
            WSDLModel model = getWSDLModel();
            if (model != null && model.getState() == WSDLModel.State.VALID) {
                if (categoryPane != null) {
                    mToolbar.addSeparator();
                    categoryPane.populateToolbar(mToolbar);
                }
                mToolbar.addSeparator();
                // vlv: search
                //Fix 166881 NPE
                SearchManager searchManager = SearchManager.getDefault();
                if (searchManager != null) {
                    Action searchAction = searchManager.getSearchAction();
                    if (searchAction != null) {
                        mToolbar.addSeparator();
                        mToolbar.add(searchAction);
                    }
                }

                mToolbar.addSeparator();
                mToolbar.add(new ValidateAction(model));
            }
        }
        return mToolbar;
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }   

    /**
     * Find action for WSDL editor.
     *
     * @author  Nathan Fiedler
     */
    private class WSDLFindAction extends AbstractAction {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance of WSDLFindAction.
         */
        public WSDLFindAction() {
        }

        public void actionPerformed(ActionEvent event) {
            WSDLTreeViewMultiViewElement parent =
                    WSDLTreeViewMultiViewElement.this;
            if (parent.categoryPane != null) {
                CategoryPane pane = parent.categoryPane;
                pane.getSearchComponent().showComponent();
            }
        }
    }
}
