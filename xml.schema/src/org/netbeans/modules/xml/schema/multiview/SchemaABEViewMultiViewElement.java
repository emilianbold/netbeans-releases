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

package org.netbeans.modules.xml.schema.multiview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.schema.abe.InstanceDesignerPanel;
import org.netbeans.modules.xml.schema.abe.UIUtilities;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.schema.SchemaEditorSupport;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.multiview.ActivatedNodesMediator;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jeri Lockhart
 */
public class SchemaABEViewMultiViewElement extends TopComponent
        implements  MultiViewElement, PropertyChangeListener,
        ExplorerManager.Provider  {
    
    private static final long serialVersionUID = -483941387931729295L;
    private AXIModel axiModel;
    private String errorMessage;
    private SchemaDataObject schemaDataObject;
    private InstanceDesignerPanel abeDesigner;
    private transient JPanel toolBarPanel;
    private javax.swing.JLabel errorLabel = new javax.swing.JLabel();
    private transient MultiViewElementCallback multiViewCallback;
    private ExplorerManager manager;
    private PropertyChangeListener awtPCL = new XAMUtils.AwtPropertyChangeListener(this);
    
    public SchemaABEViewMultiViewElement() {
        super();
        // For deserialization only
    }
    
    public SchemaABEViewMultiViewElement(SchemaDataObject schemaDataObject) {
        super();
        this.schemaDataObject = schemaDataObject;
        initialize();
    }
    
    
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();        
        if(!Model.STATE_PROPERTY.equals(property)) {
            return;
        }
        //
        assert SwingUtilities.isEventDispatchThread();
        //
        Model.State newState = (Model.State)evt.getNewValue();
        if(newState == AXIModel.State.VALID) {
                errorMessage = null;
                    recreateUI();
                return;
            }

        if(errorMessage == null)
            errorMessage = NbBundle.getMessage(
                    SchemaColumnViewMultiViewElement.class,
                    "MSG_InvalidSchema");

        setActivatedNodes(new Node[] {schemaDataObject.getNodeDelegate()});
        emptyUI(errorMessage);
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private void initialize() {
        // Place the palette controller and some other things into the lookup
        Node delegate = schemaDataObject.getNodeDelegate();
        
        //show cookie
        ShowCookie showCookie = new ShowCookie() {
            
            public void show(ResultItem resultItem) {
                Component component = resultItem.getComponents();
                if(component!=null) {
                    abeDesigner.selectUIComponent(component);
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
                    if (abeDesigner != null) {
                        lookup =
                                Lookups.singleton(abeDesigner.getPaletteController());
                    }
                    return lookup;
                }
            }),
            Lookups.fixed(new Object[] {
                // Need the action map in our custom lookup so actions work.
                getActionMap(),
                // Need the data object registered in the lookup so that the
                // projectui code will close our open editor windows when the
                // project is closed.
                schemaDataObject,
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
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SchemaColumnViewMultiViewElement.class,"LBL_ABE_View_name"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SchemaColumnViewMultiViewElement.class,"LBL_ABE_View_name"));
    }
    
    
    /**
     * Initializes the UI. Here it checks for the state of the underlying
     * schema model. If valid, draws the UI, else empties the UI with proper
     * error message.
     */
    boolean firsTime = true;
    private void initUI() {
        if(firsTime){
            setLayout(new BorderLayout());
            //initialize the error label one time.
            errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            errorLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            Color usualWindowBkg = UIManager.getColor("window"); //NOI18N
            errorLabel.setBackground(usualWindowBkg != null ? usualWindowBkg :
                Color.white);
            errorLabel.setOpaque(true);
            firsTime = false;
        }
        
        AXIModel model = getAXIModel();
        if( (model != null) && model.getRoot() != null && 
                (model.getState() == AXIModel.State.VALID) ) {
            recreateUI();
            return;
        }
        
        //if it comes here, either the schema is not well-formed or invalid
        if(errorMessage == null)
            errorMessage = NbBundle.getMessage(
                    SchemaColumnViewMultiViewElement.class,
                    "MSG_InvalidSchema");
        emptyUI(errorMessage);
    }
    
    /**
     * Creates the UI. Creates the abeDesigner only if it was null
     * or the underlying documnet was found modifed externally.
     */
    boolean propChangeListenerAdded = false;
    private void recreateUI() {
        if(abeDesigner == null){
            abeDesigner = new InstanceDesignerPanel(getAXIModel(), schemaDataObject, this);
        }
        // Add the ABE designer panel
        if(!isChild(abeDesigner)){
            add(abeDesigner,BorderLayout.CENTER);
        }
        if(!propChangeListenerAdded){
            abeDesigner.addPropertyChangeListener(this);
            propChangeListenerAdded = true;
        }
        if(errorLabel != null)
            errorLabel.setVisible(false);
        abeDesigner.setVisible(true);
        revalidate();
        repaint();
    }
    
    /**
     * Empties the UI, with proper error message, when the underlying
     * schema model is in INVALID/NOT-WELLFORMED state.
     */
    
    private void emptyUI(String errorMessage) {
        if(abeDesigner != null) {
            abeDesigner.setVisible(false);
        }
        errorLabel.setText("<" + errorMessage + ">");
        if(!isChild(errorLabel))
            add(errorLabel, BorderLayout.NORTH);
        if(propChangeListenerAdded && abeDesigner != null){
            abeDesigner.removePropertyChangeListener(this);
            propChangeListenerAdded = false;
        }
        errorLabel.setVisible(true);
        revalidate();
        repaint();
    }
    
    private boolean isChild(java.awt.Component comp){
        java.awt.Component compArry[] = getComponents();
        if( (compArry == null) || (compArry.length <= 0) ){
            return false;
        }
        return Arrays.asList(compArry).contains(comp);
    }
    
    /////////////////////////////////////////////////////////////////////////////
    // MultiViewElement implementation
    /////////////////////////////////////////////////////////////////////////////
    
    /**
     *
     *
     */
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewCallback = callback;
    }
    
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SchemaABEViewMultiViewDesc.class);
    }
    
    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!SchemaMultiViewSupport.isLastView(multiViewCallback.getTopComponent())) {
            return CloseOperationState.STATE_OK;
        }
        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                "ID_SCHEMA_ABEVIEW_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }
    
    /**
     *
     *
     */
    public void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(manager, true);
        addUndoManager();
        UIUtilities.hideGlassMessage(true);
    }
    
    
    /**
     *
     *
     */
    public void componentClosed() {
        super.componentClosed();
        UIUtilities.hideGlassMessage(true);
        if(abeDesigner != null) {
            abeDesigner.shutdown();
            abeDesigner = null;
        }
        removeUndoManager();
        this.removeAll();
        this.setLayout(null);
        //associateLookup(Lookup.EMPTY);
    }
    
    
    /**
     *
     *
     */
    public void componentDeactivated() {
        super.componentDeactivated();
        ExplorerUtils.activateActions(manager, false);
        UIUtilities.hideGlassMessage(true);
    }
    
    
    /**
     *
     *
     */
    public void componentHidden() {
        super.componentHidden();
        UIUtilities.hideGlassMessage(true);
    }
    
    
    /**
     *
     *
     */
    public void componentOpened() {
        super.componentOpened();
        UIUtilities.hideGlassMessage(true);
    }
    
    
    /**
     *
     *
     */
    public void componentShowing() {
        super.componentShowing();
        initUI();
        addUndoManager();
        UIUtilities.hideGlassMessage(true);
    }

    @SuppressWarnings("deprecation")
    public void requestFocus() {
        super.requestFocus();
        // For Help to work properly, need to take focus.
        if (abeDesigner != null) {
            abeDesigner.requestFocus();
        }
    }

    @SuppressWarnings("deprecation")
    public boolean requestFocusInWindow() {
        boolean retVal = super.requestFocusInWindow();
        // For Help to work properly, need to take focus.
        if (abeDesigner != null) {
            return abeDesigner.requestFocusInWindow();
        }
        return retVal;
    }

    /**
     *
     *
     */
    public javax.swing.JComponent getToolbarRepresentation() {
        if (toolBarPanel == null) {
            toolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            
            //dummy panel for spacing
            //toolBarPanel.add(new JPanel(), BorderLayout.);
            
            JSeparator jsep = new JSeparator(SwingConstants.VERTICAL);
            toolBarPanel.add(jsep);
            
        }
        return toolBarPanel;
    }
    
    
    /**
     * Adds the undo/redo manager to the schema model as an undoable
     * edit listener, so it receives the edits onto the queue.
     */
    private void addUndoManager() {
        SchemaModel model = getAXIModel().getSchemaModel();
        if (model != null) {
            SchemaEditorSupport editor = schemaDataObject.getSchemaEditorSupport();
            QuietUndoManager undo = editor.getUndoManager();
            // Ensure the listener is not added twice.
            model.removeUndoableEditListener(undo);
            model.addUndoableEditListener(undo);
            // Ensure the model is sync'd when undo/redo is invoked,
            // otherwise the edits are added to the queue and eventually
            // cause exceptions.
            undo.setModel(model);
            undo.addWrapperModel(getAXIModel());
        }
    }
    
    private void removeUndoManager() {
        SchemaModel model = getAXIModel().getSchemaModel();
        if (model != null) {
            SchemaEditorSupport editor = schemaDataObject.getSchemaEditorSupport();
            QuietUndoManager undo = editor.getUndoManager();
            model.removeUndoableEditListener(undo);
            undo.removeWrapperModel(model);
        }
    }
    
    /**
     *
     *
     */
    public UndoRedo getUndoRedo() {
        return schemaDataObject.getSchemaEditorSupport().getUndoManager();
    }
    
    private AXIModel getAXIModel() {
        if (axiModel != null) {
            return axiModel;
        }
        try {
            SchemaModel sModel = schemaDataObject.
                    getSchemaEditorSupport().getModel();
            axiModel = AXIModelFactory.getDefault().getModel(sModel);
            if (axiModel != null) {
                // start listening axi model
                PropertyChangeListener pcl = WeakListeners.
                        create(PropertyChangeListener.class, awtPCL, axiModel);
                axiModel.addPropertyChangeListener(pcl);
            }
        } catch (IOException e) {
            errorMessage = e.getMessage();
        }
        return axiModel;
    }
    
    /**
     *
     *
     */
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }
    
}
