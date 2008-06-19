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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.editors.multiview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.bpel.core.BPELDataEditorSupport;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.NavigationTools;
import org.netbeans.modules.bpel.design.PartnerLinkFilterButton;
import org.netbeans.modules.bpel.design.SequenceFilterButton;
import org.netbeans.modules.bpel.search.Diagram;
import org.openide.awt.UndoRedo;
import org.openide.windows.TopComponent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.soa.validation.action.ValidationAction;
import org.netbeans.modules.soa.validation.core.Controller;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.bpel.core.SelectBpelElement;
import org.netbeans.modules.bpel.design.ZoomManager;
import org.netbeans.modules.bpel.design.actions.BreakpointsDeleteAction;
import org.netbeans.modules.bpel.design.actions.BreakpointsDisableAction;
import org.netbeans.modules.bpel.design.actions.BreakpointsEnableAction;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.nodes.Node;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableTopComponent;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.CompensatableActivityHolder;
import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.Empty;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.Exit;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListenerAdapter;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.palette.SoaPaletteFactory;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.netbeans.modules.print.api.PrintManager;
import org.netbeans.modules.xml.xam.ui.multiview.ActivatedNodesMediator;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.netbeans.modules.reportgenerator.api.CustomizeReportAction;
import org.netbeans.modules.reportgenerator.api.GenerateReportAction;
import org.netbeans.modules.xml.search.api.SearchManager;
import org.netbeans.modules.bpel.documentation.DocumentationGenerator;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.loaders.DataNode;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 * @author ads
 */
public class DesignerMultiViewElement extends TopComponent
        implements MultiViewElement, ExplorerManager.Provider, Serializable, SelectBpelElement
{
    private static final long serialVersionUID = 1L;   
    
    private DesignerMultiViewElement() {
        super();
    }
    
    /** Creates a new instance of DesignerMultiViewElement. This is the visual
     *  canvas 'Design' view in the multiview
     */
    public DesignerMultiViewElement(BPELDataObject dataObject) {
        myDataObject = dataObject;
        initialize();
        initializeUI();
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(myDataObject);
    }
    
    public void readExternal( ObjectInput in ) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        myDataObject = (BPELDataObject) in.readObject();
        
        initialize();
        initializeUI();
    }
    
    public DesignView getDesignView() {
        return myDesignView;
    }
    
    private GridBagConstraints createGBConstraints() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = java.awt.GridBagConstraints.BOTH;
        gc.insets = new java.awt.Insets(0, 0, 0, 0);
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        return gc;
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    public CloseOperationState canCloseElement() {
        //
        // actually if there are any visual changed NOT committed to the model
        // then we may need to flush something here or something
        //
        boolean lastView = isLastView();
        
        if(!lastView) {
            return CloseOperationState.STATE_OK;
        }
        
        BPELDataEditorSupport editorSupport = myDataObject.getEditorSupport();
        boolean modified = editorSupport.isModified();
        
        if(!modified) {
            return CloseOperationState.STATE_OK;
        } else {
            return MultiViewFactory.createUnsafeCloseState(
                    "Data Object Modified", null, null);    // NOI18N
        }
    }
    
    public void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(myExplorerManager, true);

        addUndoManager();
        myDesignView.requestFocusInWindow();
        myDesignView.getModel().setActivated();
        
        Node[] aNodes = getActivatedNodes();
        setActivatedNodes(new Node[0]);
        setActivatedNodes(aNodes);
//      getValidationController().triggerValidation();
    }
    
    private Controller getValidationController() {
        return (Controller) getDataObject().getLookup().lookup(Controller.class);
    }

    public void componentClosed() {
        super.componentClosed();
        cleanup();
    }
    
    public void componentDeactivated() {
        super.componentDeactivated();
        ExplorerUtils.activateActions(myExplorerManager, false);
    }
    
    public void componentHidden() {
        super.componentHidden();
        
        if (myDesignView != null) {
            myDesignView.setVisible(false);
        }
        updateBpelTcGroupVisibility(false);
    }
    
    public void componentOpened() {
        super.componentOpened();
    }
    
    public void componentShowing() {
        super.componentShowing();
        Node[] curNodes = getActivatedNodes();
        if (curNodes != null) {
            curNodes = curNodes.clone();
        }
        
        //
        // memory conservation?
        //
        if (myDesignView != null) {
            myDesignView.setVisible(true);
        }
        addUndoManager();
        //
        updateBpelTcGroupVisibility(true);
    }

    public JComponent getToolbarRepresentation() {
        if ( myToolBarPanel == null ) {
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.setFocusable(false);
            toolbar.addSeparator();
            
            toolbar.add(new PartnerLinkFilterButton(myDesignView));
            toolbar.add(Box.createHorizontalStrut(1));
            toolbar.add(new SequenceFilterButton(myDesignView));
            toolbar.addSeparator();
            //FIXME toolbar.add(myDesignView.createExpandAllPatternsToolBarButton());
            //toolbar.addSeparator();

            NavigationTools navigationTools = myDesignView.getNavigationTools();
            for (int i = 0; i < navigationTools.getControllersCount(); i++) {
                toolbar.add(navigationTools.getController(i));
                if (i + 1 < navigationTools.getControllersCount()) {
                    toolbar.add(Box.createHorizontalStrut(1));
                }
            }
            toolbar.addSeparator();
            
            // vlv: report
            toolbar.add(new GenerateReportAction(myDataObject,
              new DocumentationGenerator(myDataObject, getDesignView().getProcessView())));
            toolbar.add(new CustomizeReportAction(myDataObject));
            toolbar.addSeparator();

            // zoom
            ZoomManager zoomManager = myDesignView.getZoomManager();

            for (int i = 0; i < zoomManager.getComponentCount(); i++) {
                toolbar.add(zoomManager.getComponent(i));
                
                if (i + 1 < zoomManager.getComponentCount()) {
                    toolbar.add(Box.createHorizontalStrut(1));
                }
            }
            // vlv: print
            toolbar.addSeparator();
            toolbar.add(PrintManager.printPreviewAction());

            // vlv: search
            toolbar.addSeparator();
            toolbar.add(SearchManager.getDefault().getSearchAction());

            // vlv: valdiation
            toolbar.addSeparator();
            toolbar.add(new ValidationAction(getValidationController()));
            
            // ksorokin: breakpoints
            toolbar.addSeparator();
            toolbar.add(new BreakpointsEnableAction(myDesignView));
            toolbar.add(new BreakpointsDisableAction(myDesignView));
            toolbar.add(new BreakpointsDeleteAction(myDesignView));
            
            int maxButtonHeight = 0;
            
            for (Component c : toolbar.getComponents()) {
                if (c instanceof JButton || c instanceof JToggleButton) {
                    maxButtonHeight = Math.max(c.getPreferredSize().height,
                            maxButtonHeight);
                }
            }
            for (Component c : toolbar.getComponents()) {
                if (c instanceof JButton || c instanceof JToggleButton) {
                    Dimension size = c.getMaximumSize();
                    size.height = maxButtonHeight;
                    c.setMaximumSize(size);
                    c.setMinimumSize(c.getPreferredSize());
                    c.setFocusable(false);
                } else if ((c instanceof JTextComponent) 
                        || (c instanceof JComboBox)) 
                {
                    c.setMaximumSize(c.getPreferredSize());
                    c.setMinimumSize(c.getPreferredSize());
                } else {
                    c.setMinimumSize(c.getPreferredSize());
                    c.setFocusable(false);
                }
            }
            myToolBarPanel = toolbar;
        }
        return myToolBarPanel;
    }

    public UndoRedo getUndoRedo() {
        return getDataObject().getEditorSupport().getUndoManager();
    }
    
    public JComponent getVisualRepresentation() {
        return this;
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        myMultiViewObserver = callback;
    }
    
    public void requestVisible() {
        if (myMultiViewObserver != null) {
            myMultiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }
    
    public void requestActive() {
        if (myMultiViewObserver != null) {
            myMultiViewObserver.requestActive();
        } else {
            super.requestActive();
        }
    }
    
    protected boolean closeLast() {
        return true;
    }
    private JButton createExpandAllPatternsToolBarButton() {
        //FIXME
        return null;
//        JButton button = new JButton(new ExpandAllPatternsAction(myDesignView));
//        button.setText(null);
//        button.setFocusable(false);
//        return button;
    }
    private DesignView createDesignView() {
        return new DesignView(getLookup()); // got TC's lookup or no Palette
    }
    
    private void initializeUI() {
        // TODO : add listener for replacing view in the case broken OM.
        //getBpelModel().addEntityChangeListener( new ProxyListener() );
        
        setLayout(new BorderLayout());
        
        myDesignView = createDesignView();

//FIXME        ThumbScrollPane scroll = new ThumbScrollPane(myDesignView.getView());
//        scroll.setBorder(null);
//        scroll.getVerticalScrollBar().setUnitIncrement(16);
//        scroll.getHorizontalScrollBar().setUnitIncrement(16);
//        add(scroll, BorderLayout.CENTER);
        
        add(myDesignView, BorderLayout.CENTER);
        // add copy, cut, paste actions into actionMap to be visible in external menus
        ActionMap map = getActionMap();
        ActionMap designViewMap = myDesignView.getActionMap();
        map.setParent(designViewMap);
        
        designViewMap.put(DefaultEditorKit.copyAction, designViewMap.get("copy-pattern"));
        designViewMap.put(DefaultEditorKit.cutAction, designViewMap.get("cut-pattern"));
        designViewMap.put(DefaultEditorKit.pasteAction, designViewMap.get("paste-pattern"));
        map.put("delete", designViewMap.get("delete-something"));
        add(myDesignView.getRightStripe(), BorderLayout.EAST);

        // vlv: find
        myFind = SearchManager.getDefault().createFind(new Diagram(getDesignView()), getDesignView());
        myFind.setVisible(false);
        add(myFind, BorderLayout.SOUTH);

        initActiveNodeContext();
        setVisible(true);
        
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(DesignerMultiViewElement.class, "ACSN_DesignerMultiviewElement", getName())); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DesignerMultiViewElement.class, "ACSD_DesignerMultiviewElement", getName())); // NOI18N
    }

    private Component myFind;

    private void initActiveNodeContext() {

        Node[] aNodes = getActivatedNodes();

        // activate cur node
        // data node is the node associated with dataobject(BPELDataObject)
        if (aNodes == null || aNodes.length == 0 || aNodes[0] instanceof DataNode) {
            Node node = null;
            
            node = node != null ? node : myDesignView != null 
                    ? myDesignView.getNodeForPattern(myDesignView.getRootPattern()) : null;

            if (node != null) {
                aNodes = new Node[] { node };
            }
        }

        if (aNodes != null && aNodes.length > 0) {
            setActivatedNodes(aNodes);
        }
    }
    
    /**
     *  Open or close the bpel_designer TopComponentGroup.
     */
    private static void updateBpelTcGroupVisibility(final boolean show) {
        // when active TopComponent changes, check if we should open or close
        // the BPEL  editor group of windows
        WindowManager wm = WindowManager.getDefault();
        final TopComponentGroup group = wm.findTopComponentGroup("bpel_designer"); // NOI18N
        if (group == null) {
            return; // group not found (should not happen)
        }
        //
        boolean designerSelected = false;
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
                        if (DesignerMultiViewElementDesc.PREFERRED_ID.equals(id)) {
                            designerSelected = true;
                            break;
                        }
                    }
                }
            }
        }
        //
        if (designerSelected && !Boolean.TRUE.equals(groupVisible)) {
            group.open();
        } else if (!designerSelected && !Boolean.FALSE.equals(groupVisible)) {
            group.close();
        }
        //
        groupVisible = designerSelected ? Boolean.TRUE : Boolean.FALSE;
    }
    
    public static String getMVEditorActivePanelPrefferedId() {
        TopComponent activeTC = WindowManager.getDefault().getRegistry()
        .getActivated();
        MultiViewHandler mvh = MultiViews.findMultiViewHandler(activeTC);
        if (mvh == null) {
            return null;
        }
        
        MultiViewPerspective mvp = mvh.getSelectedPerspective();
        if (mvp != null) {
            return mvp.preferredID();
        }
        
        return null;
    }
    
    private boolean isLastView() {
        boolean oneOrLess = true;
        Enumeration en =
                ((CloneableTopComponent)myMultiViewObserver.getTopComponent()
                ).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                oneOrLess = false;
            }
        }
        
        return oneOrLess;
    }
    
    private BPELDataObject getDataObject() {
        return myDataObject;
    }
    
    private boolean isModelValid(){
        if (getDataObject().getEditorSupport().getBpelModel() != null) {
            State state = getDataObject().getEditorSupport().getBpelModel()
            .getState();
            return State.VALID.equals(state);
        } else {
            return false;
        }
    }

    private void initialize() {
        myExplorerManager = new ExplorerManager();
        // Install our own actions.
        ActionMap actionMap = getActionMap();
//        actionMap.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(myExplorerManager));
//        actionMap.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(myExplorerManager));
//        actionMap.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(myExplorerManager));
//        actionMap.put("delete", ExplorerUtils.actionDelete(manager, false));

        Node delegate = myDataObject.getNodeDelegate();
        myNodesMediator = new ActivatedNodesMediator(delegate);
        myNodesMediator.setExplorerManager(this);
        
/**
new ProxyLookup(new Lookup[] {
            //
            // other than nodesHack what else do we need in the associated
            // lookup?  I think that XmlNavigator needs DataObject
            //
            myDataObject.getLookup(), // this lookup contain objects that are used in OM clients
            
            // This lookup is used by BPELDataEditorSupport 
            // to obtain SelectBpelElement interface implementation
            Lookups.singleton(this),
            
            new AbstractLookup(nodesHack),
            Lookups.singleton(SoaPaletteFactory.getPalette())
        })
 */        
        
        myCookieProxyLookup = new CookieProxyLookup(new Lookup[] {
                Lookups.fixed(new Object[] {
                        // Need ActionMap in lookup so our actions are used.
                        actionMap,
                        // Need the data object registered in the lookup so that the
                        // projectui code will close our open editor windows when the
                        // project is closed.
                        myDataObject,
                }),
                myDataObject.getLookup(),// this lookup contain objects that are used in OM clients
                Lookups.singleton(this),

                Lookups.singleton(SoaPaletteFactory.getPalette()),
                myNodesMediator.getLookup(),
                // The Node delegate Lookup must be the last one in the list
                // for the CookieProxyLookup to work properly.
                delegate.getLookup(),
        }, delegate);

        associateLookup(myCookieProxyLookup);
        addPropertyChangeListener(ACTIVATED_NODES, myNodesMediator);
        addPropertyChangeListener(ACTIVATED_NODES, myCookieProxyLookup);

        setLayout(new BorderLayout());
    }
    
    private void cleanup() {
        try {
            myExplorerManager.setSelectedNodes(new Node[0]);
        } catch (PropertyVetoException e) {
        }
        removePropertyChangeListener(ACTIVATED_NODES, myNodesMediator);
        removePropertyChangeListener(ACTIVATED_NODES, myCookieProxyLookup);
        myNodesMediator = null;
        myCookieProxyLookup = null;

        //required to release all references to OM
        myDesignView.closeView();
        myDesignView = null;

        // # 127503
        if (myFind != null) {
          myFind.setEnabled(false);
          myFind = null;
        }
        removeAll();
    }
    
    private Lookup createAssociateLookup() {
        
        //
        // see http://www.netbeans.org/issues/show_bug.cgi?id=67257
        //
        nodesHack = new InstanceContent();
        return new ProxyLookup(new Lookup[] {
            //
            // other than nodesHack what else do we need in the associated
            // lookup?  I think that XmlNavigator needs DataObject
            //
            myDataObject.getLookup(), // this lookup contain objects that are used in OM clients
            
            // This lookup is used by BPELDataEditorSupport 
            // to obtain SelectBpelElement interface implementation
            Lookups.singleton(this),
            
            new AbstractLookup(nodesHack),
            Lookups.singleton(SoaPaletteFactory.getPalette())
        });
    }
    
    public void select(BpelEntity bpelEntity) {
        
        // Bubble up because some elements may not have
        // a valid NodeType.
        while(BPELENTITY_NODETYPE_MAP.get(bpelEntity.getElementType()) == null) {
            bpelEntity = bpelEntity.getParent();
        }
        
        
        PropertyNodeFactory factory = PropertyNodeFactory.getInstance();
        Node node = factory.createNode(BPELENTITY_NODETYPE_MAP.get(
                bpelEntity.getElementType()),bpelEntity, getLookup());
        
        if(node != null) // double check.
            this.setActivatedNodes(new Node[]{node});
        
        requestVisible();
    }
    
    /**
     * Adds the undo/redo manager to the bpel model as an undoable
     * edit listener, so it receives the edits onto the queue.
     */
    private void addUndoManager() {
        BPELDataEditorSupport support = myDataObject.getEditorSupport();
        if ( support!= null ){
            QuietUndoManager undo = support.getUndoManager();
            support.addUndoManagerToModel( undo );
        }
    }
    
    private BpelModel getBpelModel() {
        return getDataObject().getEditorSupport().getBpelModel();
    }
    
    protected static Map<Class<? extends BpelEntity>, NodeType> BPELENTITY_NODETYPE_MAP;
    static {
        BPELENTITY_NODETYPE_MAP = new HashMap<Class<? extends BpelEntity>,NodeType>();
        BPELENTITY_NODETYPE_MAP.put(Assign.class, NodeType.ASSIGN);
        BPELENTITY_NODETYPE_MAP.put(Catch.class, NodeType.CATCH);
        BPELENTITY_NODETYPE_MAP.put(CatchAll.class, NodeType.CATCH_ALL);
        BPELENTITY_NODETYPE_MAP.put(CompensatableActivityHolder.class, NodeType.CATCH_ALL);
        BPELENTITY_NODETYPE_MAP.put(Compensate.class, NodeType.COMPENSATE);
        BPELENTITY_NODETYPE_MAP.put(CompensateScope.class, NodeType.COMPENSATE_SCOPE);
        BPELENTITY_NODETYPE_MAP.put(CompensationHandler.class, NodeType.COMPENSATION_HANDLER);
        BPELENTITY_NODETYPE_MAP.put(Copy.class, NodeType.COPY);
        BPELENTITY_NODETYPE_MAP.put(Correlation.class, NodeType.CORRELATION);
        BPELENTITY_NODETYPE_MAP.put(CorrelationSet.class, NodeType.CORRELATION_SET);
        BPELENTITY_NODETYPE_MAP.put(Else.class, NodeType.ELSE);
        BPELENTITY_NODETYPE_MAP.put(ElseIf.class, NodeType.ELSE_IF);
        BPELENTITY_NODETYPE_MAP.put(Empty.class, NodeType.EMPTY);
        BPELENTITY_NODETYPE_MAP.put(EventHandlers.class, NodeType.EVENT_HANDLERS);
        BPELENTITY_NODETYPE_MAP.put(Exit.class, NodeType.EXIT);
        BPELENTITY_NODETYPE_MAP.put(FaultHandlers.class, NodeType.FAULT_HANDLERS);
        BPELENTITY_NODETYPE_MAP.put(Flow.class, NodeType.FLOW);
        BPELENTITY_NODETYPE_MAP.put(ForEach.class, NodeType.FOR_EACH);
        BPELENTITY_NODETYPE_MAP.put(FromPart.class, NodeType.FROM_PART);
        BPELENTITY_NODETYPE_MAP.put(If.class, NodeType.IF);
        BPELENTITY_NODETYPE_MAP.put(Import.class, NodeType.IMPORT);
        BPELENTITY_NODETYPE_MAP.put(Invoke.class, NodeType.INVOKE);
        BPELENTITY_NODETYPE_MAP.put(MessageExchange.class, NodeType.MESSAGE_EXCHANGE);
        BPELENTITY_NODETYPE_MAP.put(OnAlarmEvent.class, NodeType.ALARM_HANDLER);
        BPELENTITY_NODETYPE_MAP.put(OnAlarmPick.class, NodeType.ALARM_HANDLER);
        BPELENTITY_NODETYPE_MAP.put(OnEvent.class, NodeType.ON_EVENT);
        BPELENTITY_NODETYPE_MAP.put(OnMessage.class, NodeType.MESSAGE_HANDLER);
        BPELENTITY_NODETYPE_MAP.put(PartnerLink.class, NodeType.PARTNER_LINK);
        BPELENTITY_NODETYPE_MAP.put(PatternedCorrelation.class, NodeType.CORRELATION_P);
        BPELENTITY_NODETYPE_MAP.put(Pick.class, NodeType.PICK);
        BPELENTITY_NODETYPE_MAP.put(Process.class, NodeType.PROCESS);
        BPELENTITY_NODETYPE_MAP.put(Receive.class, NodeType.RECEIVE);
        BPELENTITY_NODETYPE_MAP.put(RepeatUntil.class, NodeType.REPEAT_UNTIL);
        BPELENTITY_NODETYPE_MAP.put(Reply.class, NodeType.REPLY);
        BPELENTITY_NODETYPE_MAP.put(Scope.class, NodeType.SCOPE);
        BPELENTITY_NODETYPE_MAP.put(Sequence.class, NodeType.SEQUENCE);
        BPELENTITY_NODETYPE_MAP.put(TerminationHandler.class, NodeType.TERMINATION_HANDLER);
        BPELENTITY_NODETYPE_MAP.put(Throw.class, NodeType.THROW);
        BPELENTITY_NODETYPE_MAP.put(ToPart.class, NodeType.TO_PART);
        BPELENTITY_NODETYPE_MAP.put(Variable.class, NodeType.VARIABLE);
        BPELENTITY_NODETYPE_MAP.put(VariableContainer.class, NodeType.VARIABLE_CONTAINER);
        BPELENTITY_NODETYPE_MAP.put(VariableDeclarationScope.class, NodeType.VARIABLE_SCOPE);
        BPELENTITY_NODETYPE_MAP.put(Wait.class, NodeType.WAIT);
        BPELENTITY_NODETYPE_MAP.put(While.class, NodeType.WHILE);
    }
    
    public ExplorerManager getExplorerManager() {
        return myExplorerManager;
    }

    private class ProxyListener extends ChangeEventListenerAdapter {

        @Override
        public void notifyPropertyUpdated( PropertyUpdateEvent event )
        {
            if ( BpelModel.STATE.equals( event.getName()) &&
                    Model.State.NOT_WELL_FORMED.equals(event.getNewValue()) ) 
            {
                setActivatedNodes(new Node[] {getDataObject().getNodeDelegate()});
            }
        }

    }

    private static final String ACTIVATED_NODES = "activatedNodes";
    private transient MultiViewElementCallback myMultiViewObserver;
    private transient DesignView myDesignView;
    private transient UndoRedo.Manager myUndoManager;
    private transient InstanceContent nodesHack;
    private BPELDataObject myDataObject;
    private transient JComponent myToolBarPanel;
    private static Boolean groupVisible = null;
    private PropertyChangeListener myActiveNodeChangeListener;
    private ExplorerManager myExplorerManager;
    private ActivatedNodesMediator myNodesMediator;
    private CookieProxyLookup myCookieProxyLookup;
}
