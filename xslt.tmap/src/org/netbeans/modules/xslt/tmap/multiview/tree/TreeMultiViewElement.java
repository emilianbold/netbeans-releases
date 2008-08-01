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

package org.netbeans.modules.xslt.tmap.multiview.tree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.windows.TopComponent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.ActionMap;
import javax.swing.JButton;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;

import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.xam.ui.multiview.ActivatedNodesMediator;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.netbeans.modules.xslt.tmap.TMapDataEditorSupport;
import org.netbeans.modules.xslt.tmap.TMapDataObject;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.navigator.TMapLogicalPanel;
import org.netbeans.modules.xslt.tmap.navigator.TMapNavigatorLookupHint;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.loaders.DataNode;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.openide.nodes.Node;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TreeMultiViewElement extends TopComponent
        implements MultiViewElement, ExplorerManager.Provider, Serializable {

    private static final long serialVersionUID = 1L;
    private static final String ACTIVATED_NODES = "activatedNodes"; // NOI18N
    private transient MultiViewElementCallback myMultiViewObserver;
    private transient TMapLogicalPanel myTreeView;

    private TMapDataObject myDataObject;
    private transient JComponent myToolBarPanel;
    private static Boolean groupVisible = null;
    private transient InstanceContent nodesHack;
    private ExplorerManager myExplorerManager;
    private ActivatedNodesMediator myNodesMediator;
    private CookieProxyLookup myCookieProxyLookup;
    
    // for deserialization
    private TreeMultiViewElement() {
        super();
    }
    
    /** Creates a new instance of DesignerMultiViewElement. This is the visual
     *  canvas 'Design' view in the multiview
     */
    public TreeMultiViewElement(TMapDataObject dataObject) {
        myDataObject = dataObject;
        initialize();
//        initializeLookup();
        initializeUI();
    }
    
//    private void removeActiveNodeChangeListener() {
//        if (myActiveNodeChangeListener != null) {
//            removePropertyChangeListener(myActiveNodeChangeListener);
//        }
//        myActiveNodeChangeListener = null;
//    }
//    
//    private void initActiveNodeChangeListener() {
//        if (myActiveNodeChangeListener == null) {
//            myActiveNodeChangeListener = new PropertyChangeListener() {
//                /**
//                 * TODO: may not be needed at some point when parenting
//                 * MultiViewTopComponent delegates properly to its peer's
//                 * activatedNodes. see
//                 * http://www.netbeans.org/issues/show_bug.cgi?id=67257 note:
//                 * TopComponent.setActivatedNodes is final
//                 */
//
//                public void propertyChange(PropertyChangeEvent event) {
//                    // no constant in TopComponent...lame
//                    if (event.getPropertyName().equals("activatedNodes")) { // NOI18N
//
//                        TopComponent tc = TopComponent.getRegistry().getActivated();
//                        /* Ignore event coming from my TC */
//                        // if(DEBUG)
//                        // Debug.verboseWithin(this,"propertyChange",getDataObject());
//                        nodesHack.set(Collections.EMPTY_LIST, null);
//                        nodesHack.set(Arrays.asList(getActivatedNodes()), null);
//                    }
//                };
//            };
//        } else {
//            removePropertyChangeListener(myActiveNodeChangeListener);
//        }
//
//        addPropertyChangeListener(myActiveNodeChangeListener);
//        setActivatedNodes(new Node[0]);
//        setActivatedNodes(new Node[] {getDataObject().getNodeDelegate()});
//    }    
//    

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(myDataObject);
    }
    
    /**
     * we are using Externalization semantics so that we can get a hook to call
     * initialize() upon deserialization
     */
    @Override
    public void readExternal( ObjectInput in ) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        myDataObject = (TMapDataObject) in.readObject();
        initialize();
        initializeUI();
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
    @Override
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
        
        TMapDataEditorSupport editorSupport = myDataObject.getEditorSupport();
        boolean modified = editorSupport.isModified();
        
        if(!modified) {
            return CloseOperationState.STATE_OK;
        } else {
            return MultiViewFactory.createUnsafeCloseState(
                    "Data Object Modified", null, null);    // NOI18N
        }
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
        // not sure that we need to add undo manager each time when 
        // component is activated, but calling method addUndoManager() more
        // than once is not a problem.
////        addUndoManager();
        ExplorerUtils.activateActions(myExplorerManager, true);
        // not sure that we need to add undo manager each time when 
        // component is activated, but calling method addUndoManager() more
        // than once is not a problem.
//        addUndoManager();
        myTreeView.requestFocusInWindow();
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        cleanup();
    }
    
    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
        ExplorerUtils.activateActions(myExplorerManager, false);
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
        if (myTreeView != null) {
            myTreeView.setVisible(false);
        }
        updateTMapTcGroupVisibility(false);
//        removeActiveNodeChangeListener();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
//        Node[] curNodes = getActivatedNodes();
//        if (curNodes != null) {
//            curNodes = curNodes.clone();
//        }

        if (myTreeView != null) {
            myTreeView.setVisible(true);
        }
    
////        addUndoManager();
        //
        updateTMapTcGroupVisibility(true);
//        initActiveNodeChangeListener();
        
//        showActivatedNodeStatus();
        
        // activate cur node
//        if (myMultiViewObserver != null) {
//            TopComponent thisTc = myMultiViewObserver.getTopComponent();
//            if ( thisTc != null ) {
//
//                // data node is the node associated with dataobject(BPELDataObject)
//                if (curNodes == null || curNodes.length == 0 || curNodes[0] instanceof DataNode) {
//                    curNodes= myTreeView.getExplorerManager().getSelectedNodes();
//                    curNodes = curNodes != null 
//                            ? curNodes.clone() : new Node[] {myDataObject.getNodeDelegate()};
//                }
//                
//                if (curNodes != null && curNodes.length > 0) {
//                    thisTc.setActivatedNodes(new Node[0]);
//                    setActivatedNodes(new Node[0]);
//
//                    
//                    thisTc.setActivatedNodes(curNodes);
//                    setActivatedNodes(curNodes);
//                }
//            }
//        }        
        
    }

//    private void showActivatedNodeStatus() {
//        if (myMultiViewObserver != null) {
//            TopComponent thisTc = myMultiViewObserver.getTopComponent();
//            if ( thisTc != null ) {
//                Node[] tcActivatedNodes = thisTc.getActivatedNodes();
//                System.out.println("design MVTC activated nodes: "+tcActivatedNodes);
//                if (tcActivatedNodes != null) {
//                    for (int i = 0; i < tcActivatedNodes.length; i++) {
//                        Node node = tcActivatedNodes[i];
//                        System.out.println(i+") design tc activated node: "+node+"; displayName: "+node.getDisplayName());
//                    }
//                } else {
//                    System.out.println("tcActivatedNodes is null");
//                }
//                
//                Node[] designMvActivatedNodes = getActivatedNodes();
//                if (designMvActivatedNodes != null) {
//                    for (int i = 0; i < designMvActivatedNodes.length; i++) {
//                        Node node = designMvActivatedNodes[i];
//                        System.out.println(i+") design mv activated node: "+node+"; displayName: "+node.getDisplayName());
//                    }
//                } else {
//                    System.out.println("designMvActivatedNodes is null");
//                }
//                
//            } else {
//                System.out.println("this TC is null");
//            }
//        } else {
//            System.out.println("myMultiViewObserver is null");
//        }
//        
//////        TopComponent regTcActive = TopComponent.getRegistry().getActivated();
//////        System.out.println("design tc : regTcActive: "+regTcActive);
////        Node[] regNodes = TopComponent.getRegistry().getActivatedNodes();
////        if (regNodes != null) {
////            for (int i = 0; i < regNodes.length; i++) {
////                Node node = regNodes[i];
////                System.out.println(i+") design tc registry activated node: "+node+"; displayName: "+node.getDisplayName());
////            }
////        } else {
////            System.out.println("regNodes is null");
////        }
//        
//    }    
    
    public JComponent getToolbarRepresentation() {
        if ( myToolBarPanel == null ) {
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
//TODO a            toolbar.addSeparator();
            
//            toolbar.add(Box.createHorizontalStrut(1));
// TODO r            
//            toolbar.add(new JButton("testButton"));
// TODO a            toolbar.addSeparator();
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
                } else if (c instanceof JTextComponent) {
                    c.setMaximumSize(c.getPreferredSize());
                    c.setMinimumSize(c.getPreferredSize());
                } else if (c instanceof JSlider) {
                    Dimension size;
                    size = c.getMaximumSize();
                    size.width = 160;
                    c.setMaximumSize(size);
                    
                    size = c.getPreferredSize();
                    size.width = 160;
                    c.setPreferredSize(size);
                } else {
                    c.setMinimumSize(c.getPreferredSize());
                }
            }
            myToolBarPanel = toolbar;
        }
        return myToolBarPanel;
    }

    @Override
    public UndoRedo getUndoRedo() {
        return myDataObject.getEditorSupport().getUndoManager();
    }

    public JComponent getVisualRepresentation() {
        return this;
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        myMultiViewObserver = callback;
    }
    
    @Override
    public void requestVisible() {
        if (myMultiViewObserver != null) {
            myMultiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }
    
    @Override
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
    
    private TMapLogicalPanel createTreeView() {
        TMapLogicalPanel view = new TreeMultiViewVisualPanel();//TMapLogicalPanel(); 
        
        Lookup lookup = getLookup();
        TMapModel model = lookup.lookup(TMapModel.class);
        view.navigate(lookup, model);
        
        return view;
    }
    
    private void initializeUI() {
        setLayout(new GridBagLayout());
        
        myTreeView = createTreeView();
        JScrollPane scroll = new JScrollPane(/*new JPanel()*/myTreeView);
        scroll.setBorder(null);
        GridBagConstraints gc = createGBConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        add(scroll, gc);

        setVisible(true);
    }

    /**
     *  Open or close the tmap_designer TopComponentGroup.
     */
    private static void updateTMapTcGroupVisibility(final boolean show) {
        // when active TopComponent changes, check if we should open or close
        // the TMap editor group of windows
        WindowManager wm = WindowManager.getDefault();
        final TopComponentGroup group = wm.findTopComponentGroup("tmap_designer"); // NOI18N
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
                        if (TreeMultiViewElementDesc.PREFERRED_ID.equals(id)) {
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
    
    private TMapDataObject getDataObject() {
        return myDataObject;
    }

    private void initialize() {
        myExplorerManager = new ExplorerManager();

        ActionMap actionMap = getActionMap();
        actionMap.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(myExplorerManager));
        actionMap.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(myExplorerManager));
        actionMap.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(myExplorerManager));
        actionMap.put("delete", ExplorerUtils.actionDelete(myExplorerManager, false));

        Node delegate = myDataObject.getNodeDelegate();
        myNodesMediator = new ActivatedNodesMediator(delegate);
        myNodesMediator.setExplorerManager(this);
        
        myCookieProxyLookup = new CookieProxyLookup(new Lookup[] {
                Lookups.fixed(new Object[] {
                        TMapNavigatorLookupHint.getInstance(),
                        // Need ActionMap in lookup so our actions are used.
                        actionMap,
                        // Need the data object registered in the lookup so that the
                        // projectui code will close our open editor windows when the
                        // project is closed.
//                        myDataObject,
                }),
                Lookups.singleton(myDataObject),
                myDataObject.getLookup(),// this lookup contain objects that are used in OM clients
                Lookups.singleton(this),
                myNodesMediator.getLookup(),
                // The Node delegate Lookup must be the last one in the list
                // for the CookieProxyLookup to work properly.
                delegate.getLookup(),
        }, delegate);
        
//        proxyLookup = Lookups.exclude(proxyLookup, ActionMap.class);
        
//        myCookieProxyLookup = new CookieProxyLookup(new Lookup[] {proxyLookup}, delegate);
        
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
        myTreeView = null;
        removeAll();
    }
    
//    private void initializeLookup() {
//        associateLookup(createAssociateLookup());
//        initActiveNodeChangeListener();
//    }
    
    private Lookup createAssociateLookup() {
        ActionMap actionMap = getActionMap();
        actionMap.put(DefaultEditorKit.copyAction, null);
        actionMap.put(DefaultEditorKit.cutAction, null);
        actionMap.put(DefaultEditorKit.pasteAction, null);
        
        nodesHack = new InstanceContent();
        //
        // see http://www.netbeans.org/issues/show_bug.cgi?id=67257
        //
        return new ProxyLookup(new Lookup[] {
            myDataObject.getLookup(), // this lookup contain objects that are used in OM clients
            Lookups.fixed(actionMap),
            new AbstractLookup(nodesHack),
            
        });
    }

    public ExplorerManager getExplorerManager() {
        return myExplorerManager;
    }
    

////    /**
////     * Adds the undo/redo manager to the bpel model as an undoable
////     * edit listener, so it receives the edits onto the queue.
////     */
////    private void addUndoManager() {
////        TMapDataEditorSupport support = myDataObject.getEditorSupport();
////        if ( support!= null ){
////            QuietUndoManager undo = support.getUndoManager();
////            support.addUndoManagerToModel( undo );
////        }
////    }
}
