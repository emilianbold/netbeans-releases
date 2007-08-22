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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.validation.ValidateAction;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLSettings.ViewMode;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.category.Category;
import org.netbeans.modules.xml.xam.ui.category.CategoryPane;
import org.netbeans.modules.xml.xam.ui.category.DefaultCategoryPane;
import org.netbeans.modules.xml.xam.ui.multiview.ActivatedNodesMediator;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.netbeans.modules.xml.search.api.SearchManager;
import org.netbeans.modules.xml.search.api.SearchManagerAccess;
import org.openide.actions.FindAction;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WSDLTreeViewMultiViewElement extends TopComponent
        implements MultiViewElement, ExplorerManager.Provider {
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
                        showCookie
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

    
    private void cleanup() {
        try {
            manager.setSelectedNodes(new Node[0]);
        } catch (PropertyVetoException e) {
        }
        removePropertyChangeListener(ACTIVATED_NODES, nodesMediator);
        removePropertyChangeListener(ACTIVATED_NODES, cpl);
        nodesMediator = null;
        cpl = null;
        
        //TODO: try to clean the category.
        categoryPane = null;
        if (mToolbar != null) mToolbar.removeAll();
        mToolbar = null;
        removeAll();
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
        mObj.getWSDLEditorSupport().syncModel();
        updateGroupVisibility();
        // Ensure the graph widgets have the focus.
        // Also helps to make F1 open the correct help topic.
        if (categoryPane != null) {
            categoryPane.getComponent().requestFocusInWindow();
        }
    }
    
    @Override
    public void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
        super.componentDeactivated();
        updateGroupVisibility();
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        initUI();

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

    private static Boolean groupVisible = null;
    
    private void updateGroupVisibility() {
        WindowManager wm = WindowManager.getDefault();
        final TopComponentGroup group = wm.findTopComponentGroup("wsdl_ui"); // NOI18N
        if (group == null) {
            return; // group not found (should not happen)
        }
        //
        boolean isWSDLViewSelected = false;
        Iterator it = wm.getModes().iterator();
        while (it.hasNext()) {
            Mode mode = (Mode) it.next();
            TopComponent selected = mode.getSelectedTopComponent();
            if (selected != null) {
            MultiViewHandler mvh = MultiViews.findMultiViewHandler(selected);
                if (mvh != null) {
                    MultiViewPerspective mvp = mvh.getSelectedPerspective();
                    if (mvp != null) {
                        String id = mvp.preferredID();
                        if (WSDLTreeViewMultiViewDesc.PREFERRED_ID.equals(id)) {
                            isWSDLViewSelected = true;
                            break;
                        }
                    }
                }
            }
        }
        //
        if (isWSDLViewSelected && !Boolean.TRUE.equals(groupVisible)) {
            group.open();
        } else if (!isWSDLViewSelected && !Boolean.FALSE.equals(groupVisible)) {
            group.close();
        }
        //
        groupVisible = isWSDLViewSelected ? Boolean.TRUE : Boolean.FALSE;
        
    }

    @Override
    protected String preferredID() {
        return "WSDLTreeViewMultiViewElementTC";  //  NOI18N
    }

    /**
     * Construct the user interface.
     */
    private void initUI() {
        WSDLEditorSupport editor = mObj.getWSDLEditorSupport();
        WSDLModel wsdlModel = null;
        String errorMessage = null;
        wsdlModel = editor.getModel();
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
        if (mToolbar == null) {
            WSDLModel model = mObj.getWSDLEditorSupport().getModel();
            if (model != null && model.getState() == WSDLModel.State.VALID) {
                mToolbar = new JToolBar();
                mToolbar.setFloatable(false);
                if (categoryPane != null) {
                    mToolbar.addSeparator();
                    categoryPane.populateToolbar(mToolbar);
                }
                // vlv: search
                mToolbar.addSeparator();
                SearchManager searchManager = SearchManagerAccess.getManager();

                if (searchManager != null) {
                    mToolbar.add(searchManager.getSearchAction());
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
