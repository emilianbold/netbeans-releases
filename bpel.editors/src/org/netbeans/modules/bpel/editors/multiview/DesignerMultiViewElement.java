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
import org.netbeans.modules.bpel.design.DiagramImpl;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataNode;
import org.openide.windows.TopComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.text.JTextComponent;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.bpel.core.validation.BPELValidationController;
import org.netbeans.modules.bpel.core.validation.SelectBpelElement;
import org.netbeans.modules.bpel.design.ZoomManager;
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
import org.netbeans.modules.xml.validation.ValidateAction;
import org.netbeans.modules.xml.validation.ValidateAction.RunAction;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.netbeans.modules.print.api.PrintManagerAccess;
import org.netbeans.modules.xml.search.api.SearchManager;
import org.netbeans.modules.xml.search.api.SearchManagerAccess;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.windows.Mode;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 * @author ads
 */
public class DesignerMultiViewElement extends TopComponent
        implements MultiViewElement, Serializable, SelectBpelElement
{
    private static final long serialVersionUID = 1L;
    private PropertyChangeListener myActiveNodeChangeListener;
    
    
    // for deexternalization
    private DesignerMultiViewElement() {
        super();
    }
    
    /** Creates a new instance of DesignerMultiViewElement. This is the visual
     *  canvas 'Design' view in the multiview
     */
    public DesignerMultiViewElement(BPELDataObject dataObject) {
        myDataObject = dataObject;
        initializeLookup();
        //
        // FIX ME
        //
        initializeUI();
    }
    
    private void removeActiveNodeChangeListener() {
        if (myActiveNodeChangeListener != null) {
            removePropertyChangeListener(myActiveNodeChangeListener);
        }
        myActiveNodeChangeListener = null;
    }
    
    private void initActiveNodeChangeListener() {
        if (myActiveNodeChangeListener == null) {
            myActiveNodeChangeListener = new PropertyChangeListener() {
                /**
                 * TODO: may not be needed at some point when parenting
                 * MultiViewTopComponent delegates properly to its peer's
                 * activatedNodes. see
                 * http://www.netbeans.org/issues/show_bug.cgi?id=67257 note:
                 * TopComponent.setActivatedNodes is final
                 */
                public void propertyChange(PropertyChangeEvent event) {
                    // no constant in TopComponent...lame
                    if(event.getPropertyName().equals("activatedNodes")) { // NOI18N
                        
            TopComponent tc = TopComponent.getRegistry().getActivated();
            /* Ignore event coming from my TC */
                        // if(DEBUG)
                        // Debug.verboseWithin(this,"propertyChange",getDataObject());
                        nodesHack.set(Arrays.asList(getActivatedNodes()),null);
                    }
                };
            };
        } else {
            removePropertyChangeListener(myActiveNodeChangeListener);
        }

        addPropertyChangeListener(myActiveNodeChangeListener);
        setActivatedNodes(new Node[] {getDataObject().getNodeDelegate()});
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(myDataObject);
    }
    
    /**
     * we are using Externalization semantics so that we can get a hook to call
     * initialize() upon deserialization
     */
    public void readExternal( ObjectInput in ) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        
        myDataObject = (BPELDataObject) in.readObject();
        
        initializeLookup();
        //
        // FIX ME
        //
        initializeUI();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    //                                  UI
    ////////////////////////////////////////////////////////////////////////////
    /**
     * This method is added on QA engeneer request
     * to simplify automated test creation.
     */
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
    
    ////////////////////////////////////////////////////////////////////////////
    //                         MultiViewElement
    ////////////////////////////////////////////////////////////////////////////
    
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
        // not sure that we need to add undo manager each time when 
        // component is activated, but calling method addUndoManager() more
        // than once is not a problem.
        addUndoManager();
        myDesignView.getView().requestFocusInWindow();
        myDesignView.getModel().setActivated();
        getValidationController().triggerValidation( true );
    }
    
    public void componentClosed() {
        super.componentClosed();
        
        //required to release all references to OM
        myDesignView.closeView();
        myDesignView = null;
        // todo r | m 
        DataObject dObj = getDataObject();
        if (dObj.isValid()) {
            setActivatedNodes(new Node[] {dObj.getNodeDelegate()});
        }
    }
    
    public void componentDeactivated() {
        super.componentDeactivated();
    }
    
    public void componentHidden() {
        super.componentHidden();
        
        //
        // memory conservation?
        //
        
        if (myDesignView != null) {
            myDesignView.setVisible(false);
        }
        //
        updateBpelTcGroupVisibility(false);
        removeActiveNodeChangeListener();
    }
    
    public void componentOpened() {
        super.componentOpened();
    }
    
    public void componentShowing() {
        Node[] curNodes = getActivatedNodes();
        super.componentShowing();
        
        //
        // memory conservation?
        //
        if (myDesignView != null) {
            myDesignView.setVisible(true);
        }
        addUndoManager();
        //
        updateBpelTcGroupVisibility(true);

        initActiveNodeChangeListener();

        // activate cur node
        if (myMultiViewObserver != null) {
            TopComponent thisTc = myMultiViewObserver.getTopComponent();
            if ( thisTc != null ) {

                // data node is the node associated with dataobject(BPELDataObject)
                if (curNodes == null || curNodes.length == 0 || curNodes[0] instanceof DataNode) {
                    Node node = myDesignView.getNodeForPattern(myDesignView.getRootPattern());
                    
                    if (node != null) {
                        curNodes = new Node[] { node };
                    }
                }
                
                if (curNodes != null && curNodes.length > 0) {
                    thisTc.setActivatedNodes(curNodes);
                    setActivatedNodes(curNodes);
                }
            }
        }
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
            toolbar.add(myDesignView.createExpandAllPatternsToolBarButton());
            toolbar.addSeparator();

            NavigationTools navigationTools = myDesignView.getNavigationTools();
            for (int i = 0; i < navigationTools.getControllersCount(); i++) {
                toolbar.add(navigationTools.getController(i));
                if (i + 1 < navigationTools.getControllersCount()) {
                    toolbar.add(Box.createHorizontalStrut(1));
                }
            }
            
            toolbar.addSeparator();
            
            ZoomManager zoomManager = myDesignView.getZoomManager();
            
            for (int i = 0; i < zoomManager.getComponentCount(); i++) {
                toolbar.add(zoomManager.getComponent(i));
                
                if (i + 1 < zoomManager.getComponentCount()) {
                    toolbar.add(Box.createHorizontalStrut(1));
                }
            }
            // vlv: print
            toolbar.addSeparator();
            toolbar.add(PrintManagerAccess.getManager().getPreviewAction());

            // vlv: search
            SearchManager manager = SearchManagerAccess.getManager();

            if (manager != null) {
              toolbar.add(manager.getSearchAction());
            }
            // valdiation
            toolbar.addSeparator();
            toolbar.add(new BPELValidateAction(myDesignView.getBPELModel()));
            
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
    
    private DesignView createDesignView() {
        DesignView view = new DesignView(getLookup()); // got TC's lookup or no Palette
        return view;
    }
    
    private BPELValidationController getValidationController() {
        return (BPELValidationController) getDataObject().
            getLookup().lookup( BPELValidationController.class );
    }
    
    private void initializeUI() {
        // TODO : add listener for replacing view in the case broken OM.
        //getBpelModel().addEntityChangeListener( new ProxyListener() );
        
        setLayout(new BorderLayout());
        
        myDesignView = createDesignView();
        ThumbScrollPane scroll = new ThumbScrollPane(myDesignView.getView());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        add(myDesignView.getRightStripe(), BorderLayout.EAST);

        // vlv
        SearchManager manager = SearchManagerAccess.getManager();

        if (manager != null) {
          Component search =
            manager.getUI(new DiagramImpl(getDesignView()), null, getDesignView(), false);
        
          if (search != null) {
            search.setVisible(false);
            add(search, BorderLayout.SOUTH);
          }
        }
        setVisible(true);
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
    
    private void initializeLookup() {
        associateLookup(createAssociateLookup());
        initActiveNodeChangeListener();
//        addPropertyChangeListener( new PropertyChangeListener() {        
//                /**
//                 * TODO: may not be needed at some point when parenting
//                 * MultiViewTopComponent delegates properly to its peer's
//                 * activatedNodes. see
//                 * http://www.netbeans.org/issues/show_bug.cgi?id=67257 note:
//                 * TopComponent.setActivatedNodes is final
//                 */
//                public void propertyChange(PropertyChangeEvent event) {
//                    // no constant in TopComponent...lame
//                    if(event.getPropertyName().equals("activatedNodes")) {
//                        // if(DEBUG)
//                        // Debug.verboseWithin(this,"propertyChange",getDataObject());
//                        nodesHack.set(Arrays.asList(getActivatedNodes()),null);
//                    }
//                };
//         });
//        
//        setActivatedNodes(new Node[] {getDataObject().getNodeDelegate()});
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
    
    /*
        1) Get a element from bpel.api package
        2) If it does not implement BpelEntity ignore.
        3) Check if there is matching NodeType.   [Otherwise the bubbling up will eventually lead to a BpelEntity]
        4) If yes then make an entry into the map.
     
        No entry in map as no matching NodeType:
        [Activity, ActivityHolder, AssignChild, BaseCorrelation, BaseFaultHandlers,
         BaseScope, BooleanExpr, BpelContainer, Branches,
         CompensateScope, CompensationHandlerHolder, CompletionCondition, CompositeActivity,
         Condition, ConditionHolder, CorrelationsHolder, DeadlineExpression, Documentation,
         DurationExpression, Expression, ExtendableActivity, ExtensibleAssign, ExtensibleElements,
         Extension, ExtensionActivity, ExtensionContainer, ExtensionEntity, FinalCounterValue,
         For, From, FromPartContainer, Link*, LinkContainer,  Literal, MessageExchangeContainer,
         NamedElement, OnAlarmEvent, OnAlarmPick, OnMessage, OnMessageCommon, PartnerLinkContainer,
         PatternedCorrelationContainer, RepeatEvery, ReThrow, ServiceRef, Source,
         SourceContainer, StartCounterValue, Target, TargetContainer, TimeEvent, TimeEventHolder,
         To, Validate, VariableDeclaration,
     
     
     
        These have an entry in the map:
        [Assign, Catch, CatchAll, Compensate, CompensatableActivityHolder, CompensationHandler, Copy,
         Correlation, CorrelationContainer,
         CorrelationSet, CorrelationSet, Else, ElseIf, Empty, EventHandlers, Exit, FaultHandlers, Flow,
         ForEach, FromPart, If, Import, Invoke, MessageExchange, OnEvent, PartnerLink, PatternedCorrelation,
         Pick, Process,
         Receive, RepeatUntil, Reply, Scope, Sequence, TerminationHandler, Throw, ToPart, Variable,
         VariableContainer, VariableDeclarationScope, Wait, While]
     
     */
    protected static Map<Class<? extends BpelEntity>, NodeType> 
        BPELENTITY_NODETYPE_MAP;
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
    
    /*
     * This class could be used for changing diagram with error message 
     * in the case when model is broken.
     */
    private class ProxyListener extends ChangeEventListenerAdapter {

        /* (non-Javadoc)
         * @see org.netbeans.modules.bpel.model.api.events.ChangeEventListenerAdapter#notifyPropertyUpdated(org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent)
         */
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
    
    /**
     *  Override the ValidateAction so that complete validation results
     *  can be sent to the BPELValidationController.
     */
    private class BPELValidateAction extends ValidateAction {
        
        public BPELValidateAction(BpelModel model) {
            super(model);
        }
        
        public void actionPerformed(ActionEvent event) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    RunAction runAction = new RunAction();
                    runAction.run();
                    
                    List<ResultItem> validationResults = 
                        runAction.getValidationResults();
                    BPELValidationController controller = 
                        (BPELValidationController)((BPELDataObject)
                    getDataObject()).getLookup().lookup(
                            BPELValidationController.class);
                    
                    // Send the complete validation results to the validation controller
                    // so that clients can be notified.
                    if(controller != null) {
                        controller.
                            notifyCompleteValidationResults(validationResults);
                    }
                }
            });
        }
    }
    
    
    private transient MultiViewElementCallback myMultiViewObserver;
    private transient DesignView myDesignView;
    private transient UndoRedo.Manager myUndoManager;
    private transient InstanceContent nodesHack;
    private BPELDataObject myDataObject;
    private transient JComponent myToolBarPanel;
    private static Boolean groupVisible = null;
    
    
//    private class ProxyActionMap extends ActionMap {
//        private ActionMap originalActionMap;
//        
//        public ProxyActionMap(ActionMap originalActionMap) {
//            this.originalActionMap = originalActionMap;
//        }
//        
//        
//        public void remove(Object key) {
//            originalActionMap.remove(key);
//        }
//
//        public Action get(Object key) {
//            return originalActionMap.get();
//            Action retValue;
//            
//            retValue = super.get(key);
//            return retValue;
//        }
//
//        public void put(Object key, Action action) {
//            super.put(key, action);
//        }
//
//        public void setParent(ActionMap map) {
//            super.setParent(map);
//        }
//
//        public int size() {
//            int retValue;
//            
//            retValue = super.size();
//            return retValue;
//        }
//
//        public Object[] keys() {
//            Object[] retValue;
//            
//            retValue = super.keys();
//            return retValue;
//        }
//
//        public ActionMap getParent() {
//            ActionMap retValue;
//            
//            retValue = super.getParent();
//            return retValue;
//        }
//
//        public void clear() {
//            super.clear();
//        }
//
//        public Object[] allKeys() {
//            Object[] retValue;
//            
//            retValue = super.allKeys();
//            return retValue;
//        }
//        
//    }
    
    
//    private class ProxyInputMap extends InputMap {
//        private InputMap originalInputMap;
//            
//        public ProxyInputMap(InputMap originalInputMap) {
//            this.originalInputMap = originalInputMap;
//        }
//                
//        
//        public void setParent(InputMap map) {
//            
//            originalInputMap.setParent(map);
//        }
//        
//
//        public void put(KeyStroke keyStroke, Object actionMapKey) {
//            originalInputMap.put(keyStroke, actionMapKey);
//        }
//        
//
//        public void remove(KeyStroke key) {
//            originalInputMap.remove(key);
//        }
//
//        
//        public Object get(KeyStroke keyStroke) {
//            return originalInputMap.get(keyStroke);
//        }
//
//        
//        public int size() {
//            return originalInputMap.size();
//        }
//        
//
//        public void clear() {
//            super.clear();
//        }
//        
//
//        public KeyStroke[] keys() {
//            return originalInputMap.keys();
//        }
//
//        
//        public InputMap getParent() {
//            InputMap retValue;
//            
//            retValue = super.getParent();
//            return retValue;
//        }
//        
//
//        public KeyStroke[] allKeys() {
//            KeyStroke[] retValue;
//            
//            retValue = super.allKeys();
//            return retValue;
//        }
//        
//    }
}
