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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
import org.netbeans.modules.xml.validation.action.ValidateAction;
import org.netbeans.modules.xml.validation.ui.ShowCookie;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.GraphView;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.palette.WSDLPaletteFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.multiview.ActivatedNodesMediator;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.openide.actions.SaveAction;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * TopComponent MultiViewElement for the WSDL graphical editor.
 *
 * @author radval
 * @author Nathan Fiedler
 */
public class WSDLDesignMultiViewElement extends TopComponent
        implements MultiViewElement, ExplorerManager.Provider, PropertyChangeListener {
    private static final long serialVersionUID = -655912409997381426L;
    private static final String ACTIVATED_NODES = "activatedNodes";//NOI18N
    /** The WSDL DataObject this view represents. */
    private WSDLDataObject wsdlDataObject;
    /** Used to enable various node actions, used in the graphical view. */
    private ExplorerManager explorerManager;
    /** The component which displays the graphical editor. */
    private GraphView graphComponent;
    private transient MultiViewElementCallback multiViewObserver;
    private transient javax.swing.JLabel errorLabel = new javax.swing.JLabel();
    private transient JToolBar mToolbar;
    private ActivatedNodesMediator nodesMediator;
    private CookieProxyLookup cpl;
    private WSDLModel wsdlModel;
    
    public WSDLDesignMultiViewElement() {
        super();
    }
    
    public WSDLDesignMultiViewElement(WSDLDataObject dObj) {
        wsdlDataObject = dObj;
        initialize();
    }
    
    private void initialize() {
        // Enable widgets to get the keyboard focus.
        setFocusable(true);
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

        // For some reason, the Ctrl-s is not in our action map (issue 94698).
        SaveAction saveAction = SystemAction.get(SaveAction.class);
        map.put("save", saveAction);
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = (KeyStroke) saveAction.getValue(Action.ACCELERATOR_KEY);
        if (key == null) {
            key = KeyStroke.getKeyStroke("control S");
        }
        keys.put(key, "save");

        //show cookie
        ShowCookie showCookie = new ShowCookie() {
            public void show(ResultItem resultItem) {
                Component component = resultItem.getComponents();
                graphComponent.showComponent(component);
            }
        };

        Node delegate = wsdlDataObject.getNodeDelegate();
        nodesMediator =
                new ActivatedNodesMediator(delegate);
        nodesMediator.setExplorerManager(this);
        cpl = new CookieProxyLookup(new Lookup[] {
            Lookups.fixed(new Object[] {
                    //For the explorer actions to be enabled.
                    map,
                    // Need the data object registered in the lookup so that the
                    // projectui code will close our open editor windows when the
                    // project is closed.
                    wsdlDataObject,
                    // The Show Cookie in lookup to show the component
                    showCookie,
                    // Component palette for the partner view.
                    WSDLPaletteFactory.getPalette(),
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
        wsdlModel = wsdlDataObject.getWSDLEditorSupport().getModel();
        wsdlModel.addPropertyChangeListener(WeakListeners.propertyChange(this, wsdlModel));
        return wsdlModel;
    }
    
    private void cleanup() {
        try {
            explorerManager.setSelectedNodes(new Node[0]);
        } catch (PropertyVetoException e) {
        }
        removePropertyChangeListener(ACTIVATED_NODES, nodesMediator);
        removePropertyChangeListener(ACTIVATED_NODES, cpl);
        nodesMediator.setExplorerManager(null);
        nodesMediator = null;
        cpl = null;
        
        
        if (mToolbar != null) mToolbar.removeAll();
        mToolbar = null;
        removeAll();
        graphComponent = null;
        wsdlModel = null;
        explorerManager = null;
        multiViewObserver = null;
        setActivatedNodes(new Node[0]);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (WSDLModel.STATE_PROPERTY.equals(evt.getPropertyName())) {
            WSDLModel.State state = (WSDLModel.State) evt.getNewValue();
            if (state != null) {
                initUI();
            }
        }
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public int getPersistenceType() {
        // This is likely ignored, the one in MultiViewDesc matters.
        return PERSISTENCE_ALWAYS;
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
        return wsdlDataObject.getWSDLEditorSupport().getUndoManager();
    }


    @Override
    public void componentHidden() {
        super.componentHidden();
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
        ExplorerUtils.activateActions(explorerManager, true);
        WSDLMultiViewFactory.updateGroupVisibility(WSDLDesignMultiViewDesc.PREFERRED_ID);
        // Ensure the graph widgets have the focus.
        // Also helps to make F1 open the correct help topic.
        if (graphComponent != null) {
            graphComponent.requestFocusInWindow();
        }
    }
    
    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
        //Strangely, gets called after componentClosed is called
        if (explorerManager != null) {
            ExplorerUtils.activateActions(explorerManager, false);
        }
        WSDLMultiViewFactory.updateGroupVisibility(WSDLDesignMultiViewDesc.PREFERRED_ID);
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
    }
    
    @Override
    protected String preferredID() {
        return "WSDLDesignMultiViewElementTC";  //  NOI18N
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
            if (graphComponent == null) {
                graphComponent = new GraphView(wsdlModel);
            }
            removeAll();
            add(graphComponent, BorderLayout.CENTER);
            return;
        }

        String errorMessage = null;
        // If it comes here, either the model is not well-formed or invalid.
        if (wsdlModel == null ||
                wsdlModel.getState() == WSDLModel.State.NOT_WELL_FORMED) {
            errorMessage = NbBundle.getMessage(
               WSDLTreeViewMultiViewElement.class, "MSG_NotWellformedWsdl");
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
                mToolbar.addSeparator();
                if (graphComponent != null) graphComponent.addToolbarActions(mToolbar);

                mToolbar.addSeparator();
                mToolbar.add(new ValidateAction(model));
            }
        }
        return mToolbar;
    }

    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public void requestActive() {
        super.requestActive();
        // Ensure the graph widgets have the focus.
        // Also helps to make F1 open the correct help topic.
        if (graphComponent != null) {
            graphComponent.requestFocus();
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
      return new HelpCtx(WSDLDesignMultiViewDesc.class);
    }

    @Override
    public void readExternal(ObjectInput in) throws
            IOException, ClassNotFoundException {
        super.readExternal(in);
        Object obj = in.readObject();
        if (obj instanceof WSDLDataObject) {
            wsdlDataObject = (WSDLDataObject) obj;
            initialize();
            if (graphComponent == null) initUI();
        }
        if (graphComponent != null) {
            try {
                // By default the widgets are visible, so only need to make
                // them not visible, if the settings dictate.
                if (!in.readBoolean()) {
                    graphComponent.setCollaborationsVisible(false);
                }
                if (!in.readBoolean()) {
                    graphComponent.setMessagesVisible(false);
                }
            } catch (IOException ioe) {
                // Visibility settings must not have been saved.
            }
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(wsdlDataObject);
        // Persist the visibility of the widgets.
        boolean collaborationsShowing = false;
        boolean messagesShowing = true;
        
        if (graphComponent != null) {
            collaborationsShowing = graphComponent.isCollaborationsShowing();
            messagesShowing = graphComponent.isMessagesShowing();
        }
        out.writeBoolean(collaborationsShowing);
        out.writeBoolean(messagesShowing);
    }

}
