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
 *
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.navigation.JSFPageFlowMultiviewDescriptor.PageFlowElement;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Joelle Lam
 */
public class PageFlowView  extends TopComponent implements Lookup.Provider, ExplorerManager.Provider {
    private JSFConfigEditorContext context;
    private PageFlowScene scene;
    private JSFConfigModel configModel;
    private PageFlowController pfc;
    private PageFlowElement multiview;
    
    
    PageFlowView(PageFlowElement multiview, JSFConfigEditorContext context){
        this.multiview = multiview;
        this.context = context;
        init();
        pfc = new PageFlowController( context,  this );
        setFocusable(true);
        
        //        this(context, new InstanceContent());
    }
    
    public void requestMultiViewActive() {
        multiview.getMultiViewCallback().requestActive();
        requestFocus();  //This is a hack because requestActive does not call requestFocus when it is already active (BUT IT SHOULD).
    }
    
    
    
    /**
     *
     * @return PageFlowController
     */
    public PageFlowController getPageFlowController() {
        return pfc;
    }
    
    
    /** Weak reference to the lookup. */
    private WeakReference<Lookup> lookupWRef = new WeakReference<Lookup>(null);
    
    
    public Lookup getLookup() {
        Lookup lookup = lookupWRef.get();
        
        if (lookup == null) {
            Lookup superLookup = super.getLookup();
            
            // XXX Needed in order to close the component automatically by project close.
            /* This is currently done at the MultiViewElement level all though we can easily add it here */
            //            DataObject jspDataObject = webform.getJspDataObject();
            //            DataObject jspDataObject = null;
            //            try {
            //                jspDataObject = DataObject.find(context.getFacesConfigFile());
            //            } catch ( DataObjectNotFoundException donfe) {
            //                donfe.printStackTrace();
            //            }
            
            /* Temporarily Removing Palette */
            //            PaletteController paletteController = getPaletteController();
            //            if (paletteController == null) {
            lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.fixed(new Object[]{scene})});
            //            } else {
            //                lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.fixed(new Object[] { paletteController})});
            //            }
            
            lookupWRef = new WeakReference<Lookup>(lookup);
        }
        
        return lookup;
        
    }
    
    
    public void unregstierListeners() {
        if ( pfc != null ) {
            pfc.unregisterListeners();
        }
    }
    
    public void registerListeners() {
        if( pfc != null ) {
            pfc.registerListeners();
        }
    }
    
    
    private JComponent view;
    /*
     * Initializes the Panel and the graph
     **/
    private void init(){
        setLayout(new BorderLayout());
        
        scene = new PageFlowScene(this);
        
        view = scene.createView();
        
        JScrollPane pane = new JScrollPane(view);
        pane.setVisible(true);
        
        add(pane, BorderLayout.CENTER);
        
        setDefaultActivatedNode();
        
    }
    
    /**
     * Set the default actived node to faces config node.
     */
    public void setDefaultActivatedNode() {
        try{
            Node node = org.openide.loaders.DataObject.find(context.getFacesConfigFile()).getNodeDelegate();
            setActivatedNodes(new Node[] { node });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
//   public DataNode getFacesCongFile() {
//       DataNode node = null;
//       try     {
//             node = org.openide.loaders.DataObject.find(context.getFacesConfigFile()).getNodeDelegate();
//        }
//        catch (DataObjectNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        }        
//        if( node == null ){            
//            node = new AbstractNode(Children.LEAF);
//        }
//        return node;
//    }
    
    
    /**
     * 
     */
    public void warnUserMalFormedFacesConfig() {
        //        clearGraph();
        scene.createMalFormedWidget();
    }
    
    public void removeUserMalFormedFacesConfig() {
        scene.removeMalFormedWidget();
    }
    
    //    private static final Image IMAGE_LIST = Utilities.loadImage("org/netbeans/modules/web/jsf/navigation/graph/resources/list_32.png"); // NOI18N
    
    /**
     *
     */
    public void clearGraph() {
        //        scene.removeChildren();
        
        //Workaround: Temporarily Wrapping Collection because of  http://www.netbeans.org/issues/show_bug.cgi?id=97496
        Collection<PageFlowNode> nodes = new HashSet<PageFlowNode>(scene.getNodes());
        for( PageFlowNode node : nodes ){
            scene.removeNodeWithEdges(node);
        }
        scene.validate();
    }
    
    /**
     *
     */
    public void validateGraph() {
        //        scene.layoutScene();
        scene.validate();
    }
    
    public void layoutSceneImmediately() {
        //        scene.layoutSceneImmediately();
    }
    
    
    
    /**
     * Creates a PageFlowScene node from a pageNode.  The PageNode will generally be some type of DataObject unless
     * there is no true file to represent it.  In that case a abstractNode should be passed
     * @param pageNode the node that represents a dataobject or empty object
     * @param type
     * @param glyphs
     * @return
     */
    protected VMDNodeWidget createNode( PageFlowNode pageNode, String type, List<Image> glyphs) {
        VMDNodeWidget widget = (VMDNodeWidget) scene.addNode(pageNode);
        //        String pageName = pageNode.getName();
        //        if( pageNode instanceof DataNode ){
        //            pageName = ((DataNode)pageNode).getDataObject().getPrimaryFile().getNameExt();
        //            System.out.println("PageName : " + pageName);
        //        }
        String pageName = pageNode.getDisplayName();
        //        widget.setNodeProperties(null /*IMAGE_LIST*/, pageName, type, glyphs);
        widget.setNodeProperties(pageNode.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16), pageName, type, glyphs);
        scene.addPin(pageNode, new PinNode(pageNode));
        
        return widget;
    }
    
    //    protected boolean resetNode( PageFlowNode oldPageNode, PageFlowNode newPageNode ){
    //
    //    }
    
    //    /**
    //     * Creates a PageFlowScene pin from a pageNode and pin name String.
    //     * In general a pin represents a NavigasbleComponent orginally designed for VWP.
    //     * @param pageNode
    //     * @param navComp
    //     * @return
    //     */
    //    protected VMDPinWidget createPin( Node pageNode, NavigationCaseNode navComp) {
    //        //        Pin pin = new Pin(page, navComp);
    //        VMDPinWidget widget = (VMDPinWidget) scene.addPin(pageNode, navComp);
    //        //        VMDPinWidget widget = (VMDPinWidget) graphScene.addPin(page, pin);
    //        //        if( navComp != null ){
    //        //            widget.setProperties(navComp, Arrays.asList(navComp.getBufferedIcon()));
    //        //        }
    //        return widget;
    //    }
    
    /**
     * Creates an Edge or Connection in the Graph Scene
     * @param navCaseNode
     * @param fromPageNode
     * @param toPageNode
     */
    protected void createEdge( NavigationCaseNode navCaseNode, PageFlowNode fromPageNode, PageFlowNode toPageNode  ) {
        
        
        //
        //        PageFlowNode fromPageNode = pfc.page2Node.get(fromPage);
        //        PageFlowNode toPageNode = pfc.page2Node.get(toPage);
        
        ConnectionWidget widget = (ConnectionWidget)scene.addEdge(navCaseNode);
        
        
        //I need to remove extension so it matches the DataNode's pins.
        scene.setEdgeSource(navCaseNode, scene.getDefaultPin( fromPageNode) );
        scene.setEdgeTarget(navCaseNode, scene.getDefaultPin( toPageNode) );
        
        //        Collection<String> pins = graphScene.getPins();
        //        String targetPin = null;
        //        String sourcePin = null;
        //        for (String pin : pins ){
        //            if (pin.equals(toPage)) {
        //                sourcePin = pin;
        //                if( targetPin != null ) {
        //                    break;
        //                } else {
        //                    continue;
        //                }
        //            } else if (pin.equals(fromPage)) {
        //                targetPin = fromPage;
        //                if( sourcePin != null ) {
        //                    break;
        //                } else {
        //                    continue;
        //                }
        //            }
        //        }
        //
        //        graphScene.setEdgeTarget(navCase, targetPin);
        //        graphScene.setEdgeSource(navCase, sourcePin);
        
        
    }
    
    
    private static final String PATH_TOOLBAR_FOLDER = "PageFlowEditor/Toolbars"; // NOI18N
    
    
    /**
     *
     * @return
     */
    public JComponent getToolbarRepresentation() {
        
        PageFlowUtilities pfu = PageFlowUtilities.getInstance();
        // TODO -- Look at NbEditorToolBar in the editor - it does stuff
        // with the UI to get better Aqua and Linux toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        //            ToolbarListener listener = new ToolbarListener();
        
        toolbar.addSeparator();
        
        JComboBox comboBox = new JComboBox();
        comboBox.addItem(PageFlowUtilities.LBL_SCOPE_FACESCONFIG);
        comboBox.addItem(PageFlowUtilities.LBL_SCOPE_PROJECT);
        
        //Set the appropriate size of the combo box so it doesn't take up the whole page.
        Dimension prefSize = comboBox.getPreferredSize();
        comboBox.setMinimumSize(prefSize);
        comboBox.setMaximumSize(prefSize);
        
        comboBox.setSelectedItem(pfu.getCurrentScope());
        
        comboBox.addItemListener( new ItemListener() {
            public void itemStateChanged(ItemEvent event)  {
                PageFlowUtilities pfu = PageFlowUtilities.getInstance();
                if ( event.getStateChange() == ItemEvent.SELECTED ) {
                    pfu.setCurrentScope((String)event.getItem());
                    pfc.setupGraph();
                }
                requestMultiViewActive();
            }
        });
        
        toolbar.add(comboBox);
        
        return toolbar;
        
    }
    
    
    
    private static final String PATH_PALETTE_FOLDER = "PageFlowEditor/Palette"; // NOI18N
    
    /**
     * Get's the Palette Controller for the related Palette.
     * @return the Palette Controller.
     */
    public PaletteController getPaletteController() {
        try {
            return PaletteFactory.createPalette( PATH_PALETTE_FOLDER, new PaletteActions() {
                public Action[] getCustomCategoryActions(Lookup lookup) {
                    return new Action[0];
                }
                public Action[] getCustomItemActions(Lookup lookup) {
                    return new Action[0];
                }
                public Action[] getCustomPaletteActions() {
                    return new Action[0];
                }
                public Action[] getImportActions() {
                    return new Action[0];
                }
                public Action getPreferredAction(Lookup lookup) {
                    return null; //TODO
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
            
        }
        return null;
    }
    
    public ExplorerManager getExplorerManager() {
        return explorer;
    }
    
    private ExplorerManager explorer;
    
    public void addNotify() {
        super.addNotify();
        explorer = ExplorerManager.find(this);
    }
    
    public void requestFocus() {
        super.requestFocus();
        view.requestFocus();
    }
    
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return view.requestFocusInWindow();
    }
    
    
    
    
    
    /**
     * Remove the Edge from the scene.
     * @param node
     */
    public void removeEdge( NavigationCaseNode node ){
        
        scene.removeEdge(node);
        //            Node actNode = DataObject.find(context.getFacesConfigFile()).getNodeDelegate();
        //            setActivatedNodes(new org.openide.nodes.Node[]{actNode});
        
        
    }
    
    public void removeNodeWithEdges( PageFlowNode node ){
        //        scene.removeNode(node);
        scene.removeNodeWithEdges(node);
    }
    
    public void resetNodeWidget( PageFlowNode pageNode ){
        //Reset the Node Name
        VMDNodeWidget nodeWidget = (VMDNodeWidget)scene.findWidget(pageNode);
        //        nodeWidget.setNodeName(node.getDisplayName());
        nodeWidget.setNodeProperties(pageNode.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16), pageNode.getDisplayName(), null, null );
    }
}
