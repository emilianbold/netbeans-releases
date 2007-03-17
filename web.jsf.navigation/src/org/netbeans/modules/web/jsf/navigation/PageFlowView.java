/*
 * JSFTopComponent.java
 *
 * Created on February 8, 2007, 12:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Node;
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
    
    PageFlowView(JSFConfigEditorContext context){
        init();
        pfc = new PageFlowController( context,  this );
        
        //        this(context, new InstanceContent());
    }
    
    
    /**
     *
     * @return PageFlowController
     */
    public PageFlowController getPageFlowController() {
        return pfc;
    }
    
    //    PageFlowView(JSFConfigEditorContext context, InstanceContent ic ){
    //        super( new AbstractLookup( ic ) );
    //        ic.add( initializePalette() );
    //        this.context = context;
    //
    //
    //    }
    /** Weak reference to the lookup. */
    private WeakReference lookupWRef = new WeakReference(null);
    
    
    public Lookup getLookup() {
        Lookup lookup = (Lookup)lookupWRef.get();
        
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
            PaletteController paletteController = getPaletteController();
            if (paletteController == null) {
                lookup = new ProxyLookup(new Lookup[] {superLookup});
            } else {
                lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.fixed(new Object[] { paletteController})});
            }
            
            lookupWRef = new WeakReference(lookup);
        }
        
        return lookup;
        
    }
    
    
    /*
     * Initializes the Panel and the graph
     **/
    private void init(){
        setLayout(new BorderLayout());
        
        scene = new PageFlowScene(this);
        
        JScrollPane pane = new JScrollPane(scene.createView());
        pane.setVisible(true);
        
        //        Dimension viewDim = pane.getViewportBorderBounds().getSize();
        //        scene.setPreferredBounds(pane.getViewportBorderBounds());
        //        scene.setPreferredSize(pane.getViewportBorderBounds().getSize());
        add(pane, BorderLayout.CENTER);
        
        try{
            Node node = DataObject.find(context.getFacesConfigFile()).getNodeDelegate();
            setActivatedNodes(new Node[] { node });
        } catch (Exception e){}
        
    }
    
    
    public void warnUserMalFormedFacesConfig() {
        clearGraph();
        scene.createMalFormedWidget();
    }
    
    //    private static final Image IMAGE_LIST = Utilities.loadImage("org/netbeans/modules/web/jsf/navigation/graph/resources/list_32.png"); // NOI18N
    private static final Image IMAGE_LIST = null; // NOI18N
    
    /**
     *
     */
    public void clearGraph() {
        //        scene.removeChildren();
        
        //Temporarily Wrapping Collection because of  http://www.netbeans.org/issues/show_bug.cgi?id=97496
        Collection<Node> nodes = new HashSet<Node>(scene.getNodes());
        for( Node node : nodes ){
            scene.removeNodeWithEdges(node);
        }
        scene.validate();
    }
    
    /**
     *
     */
    public void validateGraph() {
        scene.layoutScene();
        scene.validate();
    }
    
    
    
    /**
     * Creates a PageFlowScene node from a pageNode.  The PageNode will generally be some type of DataObject unless
     * there is no true file to represent it.  In that case a abstractNode should be passed
     * @param pageNode the node that represents a dataobject or empty object
     * @param type
     * @param glyphs
     * @return
     */
    protected VMDNodeWidget createNode( Node pageNode, String type, List<Image> glyphs) {
        VMDNodeWidget widget = (VMDNodeWidget) scene.addNode(pageNode);
        //        String pageName = pageNode.getName();
        //        if( pageNode instanceof DataNode ){
        //            pageName = ((DataNode)pageNode).getDataObject().getPrimaryFile().getNameExt();
        //            System.out.println("PageName : " + pageName);
        //        }
        String pageName = pageNode.getDisplayName();
        
        widget.setNodeProperties(IMAGE_LIST, pageName, type, glyphs);
        
        scene.addPin(pageNode, pageName +"pin");
        
        return widget;
    }
    
    /**
     * Creates a PageFlowScene pin from a pageNode and pin name String.
     * In general a pin represents a NavigasbleComponent orginally designed for VWP.
     * @param pageNode
     * @param navComp
     * @return
     */
    protected VMDPinWidget createPin( Node pageNode, String navComp) {
        //        Pin pin = new Pin(page, navComp);
        VMDPinWidget widget = (VMDPinWidget) scene.addPin(pageNode, navComp);
        //        VMDPinWidget widget = (VMDPinWidget) graphScene.addPin(page, pin);
        //        if( navComp != null ){
        //            widget.setProperties(navComp, Arrays.asList(navComp.getBufferedIcon()));
        //        }
        return widget;
    }
    
    /**
     * Creates an Edge or Connection in the Graph Scene
     * @param navCaseNode
     */
    protected void createEdge( NavigationCaseNode navCaseNode) {
        

        String toPage = navCaseNode.getToViewId();
//        String caseName = navCaseNode.getFromOuctome();
        String action = navCaseNode.getFromAction();        
        String fromPage = navCaseNode.getFromViewId();
        
        ConnectionWidget widget = (ConnectionWidget)scene.addEdge(navCaseNode);
        

        
        //        graphScene.setEdgeSource(navCase, label);
        
        //I need to remove extension so it matches the DataNode's pins.
        scene.setEdgeSource(navCaseNode, fromPage+"pin");
        scene.setEdgeTarget(navCaseNode, toPage+"pin");
        
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
    
    
   
}
