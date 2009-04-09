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

package org.netbeans.modules.websvc.rest.wadl.design.multiview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import org.openide.windows.TopComponent;
import org.openide.awt.UndoRedo;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.websvc.rest.wadl.design.loader.ShowCookie;
import org.netbeans.modules.websvc.rest.wadl.design.loader.WadlDataObject;
import org.netbeans.modules.websvc.rest.wadl.design.loader.WadlEditorSupport;
import org.netbeans.modules.websvc.rest.wadl.design.undo.QuietUndoManager;
import org.netbeans.modules.websvc.rest.wadl.design.view.DesignView;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Ayub Khan
 */
public class DesignMultiViewElement extends TopComponent
        implements MultiViewElement, ExplorerManager.Provider, PropertyChangeListener {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    private transient MultiViewElementCallback multiViewCallback;
    private transient JToolBar toolbar;
    private transient DesignView designView;
    private transient WadlDataObject dataObject;
    private transient Lookup myLookup;
    private WadlModel model;
    private ExplorerManager manager;
    private String errorMessage;
    private transient javax.swing.JLabel errorLabel = new javax.swing.JLabel();

    /**
     * 
     * @param mvSupport 
     */
    public DesignMultiViewElement(WadlDataObject dataObject) {
        this.dataObject = dataObject;
        initialize();
    }
    
    private void initialize() {
        // Place the palette controller and some other things into the lookup
        Node delegate = dataObject.getNodeDelegate();
        
        //show cookie
        ShowCookie showCookie = new ShowCookie() {
            
            public void show(ResultItem resultItem) {
                final Component component = resultItem.getComponents();
                if (component instanceof WadlComponent) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
//                            categoryPane.getCategory().showComponent(
//                                    (WadlComponent) component);
                        }
                    });
                }
            } 
        };
        
        ActivatedNodesMediator nodesMediator =
                new ActivatedNodesMediator(delegate);
        manager = new ExplorerManager();
        nodesMediator.setExplorerManager(this);
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
        
        CookieProxyLookup cpl = new CookieProxyLookup(new Lookup[] {
            nodesMediator.getLookup(),
            // use a proxy lookup here as the abeDesigner is only
            // active
            Lookups.proxy(new Lookup.Provider() {
                public Lookup getLookup() {
                    Lookup lookup = Lookup.EMPTY;
//                    if (abeDesigner != null) {
//                        lookup =
//                                Lookups.singleton(abeDesigner.getPaletteController());
//                    }
                    return lookup;
                }
            }),
            Lookups.fixed(new Object[] {
                // Need the action map in our custom lookup so actions work.
                getActionMap(),
                // Need the data object registered in the lookup so that the
                // projectui code will close our open editor windows when the
                // project is closed.
                dataObject,
                // The Show Cookie in lookup to show schema component
                showCookie,
            }),
            // The Node delegate Lookup must be the last one in the list
            // for the CookieProxyLookup to work properly.
            delegate.getLookup(),
        }, delegate);
        associateLookup(cpl);
        addPropertyChangeListener("activatedNodes", nodesMediator);
        addPropertyChangeListener("activatedNodes", cpl);
        //initUI();
        
        //accessbility
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DesignMultiViewElement.class,"LBL_designView_name"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DesignMultiViewElement.class,"LBL_designView_name"));
    }
    
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewCallback = callback;
    }
    
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
    
    
    /**
     * Initializes the UI. Here it checks for the state of the underlying
     * schema m. If valid, draws the UI, else empties the UI with proper
     * error message.
     */
    private void initUI() {
        removeAll();
        setLayout(new BorderLayout());
        MultiViewSupport mvSupport = dataObject.getCookie(MultiViewSupport.class);
        WadlModel m = null;
        try {
            m = getWadlModel();
        } catch (IOException ex) {
        }
        if (mvSupport!=null && m != null) {
            designView = new DesignView(m, mvSupport.getImplementationBean());
            add(designView);
        } else {
            JLabel emptyLabel = new JLabel("The design view can not be rendered. Please switch to source view.");
            add(emptyLabel,BorderLayout.CENTER);
        }
    }
    
    /**
     * Empties the UI, with proper error message, when the underlying
     * schema m is in INVALID/NOT-WELLFORMED state.
     */
    private void emptyUI(String errorMessage) {
        removeAll();
        errorLabel.setText("<" + errorMessage + ">");
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
        errorLabel.setBackground(usualWindowBkg != null ? usualWindowBkg :
            Color.white);
        errorLabel.setOpaque(true);
        add(errorLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    /**
     * Adds the undo/redo manager to the schema m as an undoable
     * edit listener, so it receives the edits onto the queue.
     */
    private void addUndoManager() {
        try {
            WadlModel m = getWadlModel();
            if (m != null) {
                WadlEditorSupport editor = dataObject.getWadlEditorSupport();
                QuietUndoManager undo = editor.getUndoManager();
                // Ensure the listener is not added twice.
                m.removeUndoableEditListener(undo);
                m.addUndoableEditListener(undo);
                // Ensure the m is sync'd when undo/redo is invoked,
                // otherwise the edits are added to the queue and eventually
                // cause exceptions.
                undo.setModel(m);
            }
        } catch (IOException ex) {
        }
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
//        syncModel();
        addUndoManager();
        ExplorerUtils.activateActions(manager, true);
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
        if(manager != null) {
            ExplorerUtils.activateActions(manager, false);
        }
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
        if(toolbar!= null) toolbar.removeAll();
        if(manager != null) {
            ExplorerUtils.activateActions(manager, false);
            manager = null;
        }
        toolbar = null;
        model = null;
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
//        syncModel();
        initUI();
        addUndoManager();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public UndoRedo getUndoRedo() {
        return dataObject.getWadlEditorSupport().getUndoManager();
    }

    public JComponent getVisualRepresentation() {
        return this;
    }
    
    public JComponent getToolbarRepresentation() {
        // This is called every time user switches between elements.
        if (toolbar == null) {
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            if(designView!=null) designView.addToolbarActions(toolbar);
        }
        return toolbar;
    }
    
    @Override
    public Lookup getLookup() {
        return new ProxyLookup(super.getLookup()/*, myLookup*/);
     }

    private WadlModel getWadlModel() throws IOException {
        if (model != null) {
            return model;
        }
        model = this.dataObject.getWadlEditorSupport().getModel();
        model.addPropertyChangeListener(WeakListeners.propertyChange(this, model));
        return model;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (!WadlModel.STATE_PROPERTY.equals(property)) {
            return;
        }
        State newState = (State)evt.getNewValue();
        if (newState == WadlModel.State.VALID) {
            errorMessage = null;
            initUI();
            return;
        }

        //m is broken
        if (errorMessage == null) {
            errorMessage = NbBundle.getMessage(
                    DesignMultiViewElement.class,
                    "MSG_NotWellformedWadl");
        }
        //fix for IZ:116057
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setActivatedNodes(new Node[] {dataObject.getNodeDelegate()});
            }
        });
        emptyUI(errorMessage);
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private void syncModel() {
        WadlEditorSupport editor = dataObject.getWadlEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
    }
}
