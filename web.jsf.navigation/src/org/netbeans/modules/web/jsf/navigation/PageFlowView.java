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
import java.awt.Image;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Joelle Lam
 */
public class PageFlowView  extends TopComponent implements Lookup.Provider {
    private JSFConfigEditorContext context;
    private PageFlowScene scene;
    private JSFConfigModel configModel;
    
    PageFlowView(JSFConfigEditorContext context){ 
        init();
        PageFlowController pfc = new PageFlowController( context,  this );
        layoutGraph();
//        this(context, new InstanceContent());
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
        add(scene.createView());        
        
        try{
            Node node = DataObject.find(context.getFacesConfigFile()).getNodeDelegate();
            setActivatedNodes(new Node[] { node });
        } catch (Exception e){}
        
    }
    
    /**
     * Layout or Relayout the Graph Nodes.
     */
    protected void layoutGraph(){                
        if(  scene instanceof PageFlowScene ) {
            ((PageFlowScene)scene).layoutScene();
        }
    }
    
    //    private static final Image IMAGE_LIST = Utilities.loadImage("org/netbeans/modules/web/jsf/navigation/graph/resources/list_32.png"); // NOI18N
    private static final Image IMAGE_LIST = null; // NOI18N
    
    private void clearGraph() {
        scene.removeChildren();
    }
    
    /**
     * Creates a PageFlowScene node from a pageNode.  The PageNode will generally be some type of DataObject unless
     * there is no true file to represent it.  In that case a abstractNode should be passed
     * @param the node that represents a dataobject or empty object
     * @param type 
     * @param glyphs 
     * @return 
     */
    protected VMDNodeWidget createNode( AbstractNode pageNode, String type, List<Image> glyphs) {
        VMDNodeWidget widget = (VMDNodeWidget) scene.addNode(pageNode);
        String pageName = pageNode.getName();
        if( pageNode instanceof DataNode ){            
            pageName = ((DataNode)pageNode).getDataObject().getPrimaryFile().getNameExt();
        }
        
        widget.setNodeProperties(IMAGE_LIST, pageName, type, glyphs);
        System.out.println("Page Node: " + pageName);

        scene.addPin(pageNode, pageName +"pin");
        
        return widget;
    }
    
    /**
     * Creates a PageFlowScene pin from a pageNode and pin name String.  
     * In general a pin represents a NavigasbleComponent orginally designed for VWP.
     * @param pageNode 
     * @param name of the navigable component.
     * @return 
     */
    protected VMDPinWidget createPin( AbstractNode pageNode, String navComp) {
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
     * @param rule 
     * @param navCase 
     */
    protected void createEdge( NavigationRule rule, NavigationCase navCase) {
        
        String toPage = navCase.getToViewId();
        String caseName = navCase.getFromOutcome();
        String action = navCase.getFromAction();
        String fromPage = rule.getFromViewId();
        
        ConnectionWidget widget = (ConnectionWidget)scene.addEdge(navCase);
        
        LabelWidget label = new LabelWidget(scene, caseName);
        label.setOpaque(true);
        widget.addChild(label);
        widget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT, 10);
        
        //        graphScene.setEdgeSource(navCase, label);
        
        //I need to remove extension so it matches the DataNode's pins.
        scene.setEdgeSource(navCase, fromPage+"pin");
        scene.setEdgeTarget(navCase, toPage+"pin");
        
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
    
    /**
     * Get's the Palette Controller for the related Palette.
     * @return the Palette Controller.
     */
    public PaletteController getPaletteController() {
        try {
            return PaletteFactory.createPalette( "MyPalette", new PaletteActions() {
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
    
    
    
    
    
}
