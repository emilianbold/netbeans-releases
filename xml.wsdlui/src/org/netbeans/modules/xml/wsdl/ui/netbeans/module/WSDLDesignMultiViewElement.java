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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * WSDLDesignMultiViewElement.java
 *
 * Created on 2006/08/15, 20:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.Date;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.print.spi.PrintProvider;
import org.netbeans.modules.print.spi.PrintProviderCookie;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.validation.ValidateAction;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.GraphView;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.palette.WSDLPaletteFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.multiview.ActivatedNodesMediator;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author radval
 * @author Nathan Fiedler
 */
public class WSDLDesignMultiViewElement extends TopComponent
        implements MultiViewElement, ExplorerManager.Provider {
    private static final long serialVersionUID = -655912409997381426L;
    private static final String ACTIVATED_NODES = "activatedNodes";//NOI18N
    private WSDLDataObject mObj;
    /** Used to enable various node actions, used in the graphical view. */
    private ExplorerManager explorerManager;
    /** The component which displays the graphical editor. */
    private GraphView graphComponent;
    private WSDLEditorSupport mEditorSupport;
    private transient MultiViewElementCallback multiViewObserver;
    private transient javax.swing.JLabel errorLabel = new javax.swing.JLabel();
    private transient JToolBar mToolbar;
    
    public WSDLDesignMultiViewElement() {
        super();
    }
    
    public WSDLDesignMultiViewElement(WSDLDataObject dObj) {
        this.mObj = dObj;
        
        initialize();
        // Enable widgets to get the keyboard focus.
        setFocusable(true);
    }
    
    private void initialize() {
        // This is needed so that copy/paste and delete node actions are
        // enabled in the context menu of the widgets, which are based
        // on the underlying nodes. In addition, the ExplorerUtils.
        // createLookup() call below is required.
	explorerManager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(explorerManager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(explorerManager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(explorerManager));
        map.put("delete", ExplorerUtils.actionDelete(explorerManager, false));

        //show cookie
        ShowCookie showCookie = new ShowCookie() {
            public void showComponent(Component component,
                    String errorMessage, String recommmendedCorrection) {
                assert false:"ShowCookie.showComponent method is deprecated";
            }

            public void show(ResultItem resultItem) {
//                Component component = resultItem.getComponents();
                // TODO: show the corresponding widget in the scene
            }
        };

        Node delegate = mObj.getNodeDelegate();
        ActivatedNodesMediator nodesMediator =
                new ActivatedNodesMediator(delegate);
        nodesMediator.setExplorerManager(this);
        CookieProxyLookup cpl = new CookieProxyLookup(new Lookup[] {
            Lookups.fixed(new Object[] {
                // Need the data object registered in the lookup so that the
                // projectui code will close our open editor windows when the
                // project is closed.
                mObj,
                // The Show Cookie in lookup to show the component
                showCookie,
                // Provides the PrintProvider for printing
                new DesignViewPrintProvider(),
                // Component palette for the partner view.
                WSDLPaletteFactory.getPalette(),
                // This is unusal, not sure why this is here.
                this,
            }),
            // For the explorer actions to be enabled.
            ExplorerUtils.createLookup(explorerManager, map),
            nodesMediator.getLookup(),
            // The Node delegate Lookup must be the last one in the list
            // for the CookieProxyLookup to work properly.
            delegate.getLookup(),
        }, delegate);
        associateLookup(cpl);
        addPropertyChangeListener(ACTIVATED_NODES, nodesMediator);
        addPropertyChangeListener(ACTIVATED_NODES, cpl);
        mEditorSupport = mObj.getWSDLEditorSupport();
        setLayout(new BorderLayout());
        initUI();
    }

    public ExplorerManager getExplorerManager() {
	return explorerManager;
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
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
	ExplorerUtils.activateActions(explorerManager, true);
        mObj.getWSDLEditorSupport().syncModel();
    }
    
    @Override
    public void componentDeactivated() {
	ExplorerUtils.activateActions(explorerManager, false);
        super.componentDeactivated();
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        initUI();
    }
    
    @Override
    protected String preferredID() {
        return "WSDLDesignMultiViewElementTC";  //  NOI18N
    }

    /**
     * Construct the user interface.
     */
    private void initUI() {
        WSDLEditorSupport editor = mObj.getWSDLEditorSupport();
        WSDLModel wsdlModel = null;
        String errorMessage = null;
        try {
            wsdlModel = editor.getModel();
            if (wsdlModel != null &&
                    wsdlModel.getState() == WSDLModel.State.VALID) {
                // Construct the standard editor interface.
                if (graphComponent == null) {
                    graphComponent = new GraphView(wsdlModel);
                }
                removeAll();
                add(graphComponent, BorderLayout.CENTER);
                return;
            }
        } catch (IOException ex) {
            errorMessage = ex.getMessage();
        }

        // If it comes here, either the model is not well-formed or invalid.
        if (wsdlModel == null ||
                wsdlModel.getState() == WSDLModel.State.NOT_WELL_FORMED) {
            if (errorMessage == null) {
                errorMessage = NbBundle.getMessage(
                        WSDLTreeViewMultiViewElement.class,
                        "MSG_NotWellformedWsdl");
            }
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
            try {
                WSDLModel model = mObj.getWSDLEditorSupport().getModel();
                if (model != null && model.getState() == WSDLModel.State.VALID) {
                    mToolbar = new JToolBar();
                    mToolbar.setFloatable(false);
                    mToolbar.addSeparator();
                    graphComponent.addToolbarActions(mToolbar);
                    mToolbar.addSeparator();
                    mToolbar.add(new ValidateAction(model));
                }
            } catch (IOException e) {
                //wait until the model is loaded
            }
        }
        return mToolbar;
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }   

    @SuppressWarnings("deprecation")
    public void requestFocus() {
        super.requestFocus();
        // Ensure the graph widgets have the focus.
        if (graphComponent != null) {
            graphComponent.requestFocus();
        }
    }

    @SuppressWarnings("deprecation")
    public boolean requestFocusInWindow() {
        boolean retVal = super.requestFocusInWindow();
        // Ensure the graph widgets have the focus.
        if (graphComponent != null) {
            return graphComponent.requestFocusInWindow();
        }
        return retVal;
    }

    /**
     * Provides the PrintProvider which allows us to print the design view
     * to a printer using the Print API.
     */
    private class DesignViewPrintProvider implements PrintProviderCookie {

        public PrintProvider getPrintProvider() {
            return new PrintProvider.Component() {
                public String getName() {
                    return mObj.getName();
                }

                public Date getLastModifiedDate() {
                    return mObj.getPrimaryFile().lastModified();
                }

                public JComponent getComponent() {
                    return graphComponent.getContent();
                }
            };
        }
    }
}
