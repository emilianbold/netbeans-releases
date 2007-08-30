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

/*
 * WhereUsedView.java
 *
 * Created on October 25, 2005, 2:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.refactoring.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.xml.nbprefuse.View;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.nbprefuse.layout.AggregateLayout;
import org.netbeans.modules.xml.nbprefuse.layout.NbFruchtermanReingoldLayout;
import org.netbeans.modules.xml.nbprefuse.AggregateDragControl;
import org.netbeans.modules.xml.nbprefuse.EdgeFillColorAction;
import org.netbeans.modules.xml.nbprefuse.EdgeStrokeColorAction;
import org.netbeans.modules.xml.nbprefuse.FindUsagesFocusControl;
import org.netbeans.modules.xml.nbprefuse.MouseoverActionControl;
import org.netbeans.modules.xml.nbprefuse.NodeExpansionMouseControl;
import org.netbeans.modules.xml.nbprefuse.NodeFillColorAction;
import org.netbeans.modules.xml.nbprefuse.NodeStrokeColorAction;
import org.netbeans.modules.xml.nbprefuse.NodeTextColorAction;
import org.netbeans.modules.xml.nbprefuse.PopupMouseControl;
import org.netbeans.modules.xml.nbprefuse.render.CompositionEdgeRenderer;
import org.netbeans.modules.xml.nbprefuse.render.FindUsagesRendererFactory;
import org.netbeans.modules.xml.nbprefuse.render.GeneralizationEdgeRenderer;
import org.netbeans.modules.xml.nbprefuse.render.NbLabelRenderer;
import org.netbeans.modules.xml.nbprefuse.render.ReferenceEdgeRenderer;
import org.netbeans.modules.xml.refactoring.spi.AnalysisUtilities;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.ControlAdapter;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.render.EdgeRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;


/**
 *
 * @author Jeri Lockhart
 */
public class WhereUsedView implements View, PropertyChangeListener {
    
    private Model model;
    private Referenceable query;
    private Graph graph;
    private Display display = null;
    private JPanel displayPanel;
    private boolean isPrimitive;
    private boolean usePacer = false;    // slow in slow out pacer for initial
    //              graph animation
    private DefaultTreeModel defaultTreeModel;
//    
    private static int paletteCount = 0;
    public static final String
            NODE_SELECTION_CHANGE = "node-selection-change";
    
    public WhereUsedView(Referenceable ref){
        if (ref != null){
            this.query = ref;
            if (ref instanceof Component) {
                this.model = ((Component)ref).getModel();
            } else if (ref instanceof Model) {
                this.model = (Model) ref;
            } else {
                throw new IllegalArgumentException("Expect Component or Model");
            }
        }
    }
  
    
    
    /**
     *  Implement View
     *
     */
    
//    public JPanel getDisplayPanel() {
//        return displayPanel;
//    }
     public Referenceable getQueryComponent() {
         return query;
     }
     
      
    /**
     * Create the graph and the tree model
     *  
     *
     */
  /*  public Object[] createModels(FindUsageResult fuResult ){
        defaultTreeModel = null;
        Graph graph = this.createGraph(fuResult, true);
        return new Object[] {defaultTreeModel, graph};
    }*/
    
    
    /**
     *
     *
     */
    /*private Graph createGraph(FindUsageResult fuResult, boolean createDefaultTreeModel){
        usePacer = true;
        graph = null;
        Object[] models = new WhereUsedReader().loadGraph(fuResult, query, 
                createDefaultTreeModel, isPrimitive, model);
        if (models != null && models.length >0){
            graph = (Graph)models[0];
            if (createDefaultTreeModel && models.length>1) {
                defaultTreeModel = (DefaultTreeModel)models[1];
            }
        }
        return graph;
        
    }*/
    
    public boolean showView(AnalysisViewer viewer) {
        boolean wasShown = false;
        
        final Visualization viz = new Visualization();
        if (graph == null){
            ErrorManager.getDefault().log(ErrorManager.ERROR,
                    NbBundle.getMessage(WhereUsedView.class,
                    "LBL_Graph_Not_Created_Error"));
            return wasShown;
        }
        
        try {
            
            // initialize display
            this.display = new Display();
            display.setBackground(Color.WHITE);
            viz.addGraph(AnalysisConstants.GRAPH_GROUP, graph);
            display.setVisualization(viz);
            
            int aggrCount = 0;
            if (graph.getNodeCount() > 1){
                AggregateTable at = viz.addAggregates(AnalysisConstants.GRAPH_GROUP_AGGR);
                at.addColumn(VisualItem.POLYGON, float[].class);
                at.addColumn(AnalysisConstants.ID, int.class,-1);   // default value -1
                at.addColumn(AnalysisConstants.IS_FILE_GROUP_AGGREGATE, boolean.class, false);
                
                List<NodeItem> fileNodes = new ArrayList<NodeItem>();
                aggrCount = createAggregates(viz, at, fileNodes);
                
                AnalysisUtilities.expandCollapseFileNodes(fileNodes);
            }
            
            // size the AnalysisViewer to the available space in the main
            //    parent panel
            // if width is < 1, use preferred size
            Dimension dim = viewer.getPanel().getBounds().getSize();
//            System.out.println("AnalysisView dimensions:" + dim.toString());
            Dimension displayDim = display.getBounds().getSize();
            if (dim.width < 1  || dim.height < 1){
                //when the view is first displayed, if we go with preferred width (i.e 300)
                //then the usages graph is very small and unreadable
                //get the width of the netbeans IDE running, subtract 100 to be on the safe side and use that
                int width = WindowManager.getDefault().getMainWindow().getWidth() -100;
                display.setSize(dim.width < 1? width:dim.width,dim.height<1?AnalysisConstants.DISPLAY_PREFERRED_HEIGHT:dim.height);
                viewer.getPanel().setSize(display.getSize());
            } else if (!dim.equals(displayDim)) {
                display.setSize(dim.width, dim.height);
            }
            
          //  display.setSize(1500, 200);
            //viewer.getPanel().setSize(display.getSize());
            this.displayPanel = new JPanel(new BorderLayout());
            displayPanel.add(display, BorderLayout.CENTER);
            viewer.addDisplayPanel(displayPanel);
            
            
            // initialize renderers
            
            // draw aggregates as polygons with curved edges
            Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
            ((PolygonRenderer)polyR).setCurveSlack(0.15f);
            
            viz.setRendererFactory(new FindUsagesRendererFactory(
                    new NbLabelRenderer(),
                    new NbLabelRenderer(),
                    new GeneralizationEdgeRenderer(),
                    new CompositionEdgeRenderer(),
                    new ReferenceEdgeRenderer(),
                    new EdgeRenderer(),
                    polyR
                    ));
            
            
            
            final int[][] palettes = new int[][]{
                AnalysisUtilities.getHSBPalette(
                        aggrCount+1, // palette size
                        0.17f,  // saturation 23%
                        1.0f,    // brightness 100%
                        null
//                        new AnalysisConstants.HSBHues[] {
//                                AnalysisConstants.HSBHues.GREEN,
//                                AnalysisConstants.HSBHues.PINK,
//                                AnalysisConstants.HSBHues.RED}
                        ),
                ColorLib.getGrayscalePalette(
                        aggrCount // palette size
                        ),
                ColorLib.getCoolPalette(
                        aggrCount // palette size
                        ),
                ColorLib.getHotPalette(
                        aggrCount // palette size
                        ),
                ColorLib.getInterpolatedPalette(
                        aggrCount, // palette size
                        new Color(204, 255, 255).getRGB(),
                        new Color(255, 204, 255).getRGB()
                        )
            };
            
            final RepaintAction repaintAction = new RepaintAction();
            viz.putAction(AnalysisConstants.ACTION_REPAINT, repaintAction);
            
            ColorAction aggrFill = new DataColorAction(
                    AnalysisConstants.GRAPH_GROUP_AGGR,
                    AnalysisConstants.ID,
                    Constants.NOMINAL,
                    VisualItem.FILLCOLOR, palettes[0]);
            
            NodeFillColorAction nFill = new NodeFillColorAction();
            NodeTextColorAction nText = new NodeTextColorAction();
            NodeStrokeColorAction nStroke = new NodeStrokeColorAction();
            EdgeStrokeColorAction eStroke = new EdgeStrokeColorAction();
            EdgeFillColorAction eFill = new EdgeFillColorAction();
            
            ActionList draw = new ActionList();
            draw.add(nFill);
            draw.add(nText);
            draw.add(nStroke);
            draw.add(eStroke);
            draw.add(eFill);
            draw.add(aggrFill);
            viz.putAction(AnalysisConstants.ACTION_DRAW, draw);
            
            
//            System.out.println("layout.getLayoutBounds() " + layout.getLayoutBounds());   // null
//            Rectangle2D rect = layout.getLayoutBounds(viz);
//            layout.setLayoutBounds(new Rectangle(rect.getBounds().width-10, rect.getBounds().height-10));
            
//            ActionList update = new ActionList(Activity.INFINITY);
            ActionList update = new ActionList(viz);
            update.add(nFill);
            update.add(nText);
            update.add(nStroke);
            update.add(eStroke);
            update.add(eFill);
            update.add(aggrFill);
            viz.putAction(AnalysisConstants.ACTION_UPDATE, update);
            
            ActionList updateRepaint = new ActionList(viz);
            updateRepaint.add(nFill);
            updateRepaint.add(nText);
            updateRepaint.add(nStroke);
            updateRepaint.add(eStroke);
            updateRepaint.add(eFill);
            updateRepaint.add(aggrFill);
            updateRepaint.add(repaintAction);
            viz.putAction(AnalysisConstants.ACTION_UPDATE_REPAINT, updateRepaint);
            
            AggregateLayout aggregateLayout = new AggregateLayout(viz,
                    AnalysisConstants.GRAPH_GROUP_AGGR);
            viz.putAction(AnalysisConstants.ACTION_AGGREGATE_LAYOUT, aggregateLayout);
            
            
            ActionList updateAggregateLayoutRepaint = new ActionList(viz);
            updateAggregateLayoutRepaint.add(nFill);
            updateAggregateLayoutRepaint.add(nText);
            updateAggregateLayoutRepaint.add(nStroke);
            updateAggregateLayoutRepaint.add(eStroke);
            updateAggregateLayoutRepaint.add(eFill);
            updateAggregateLayoutRepaint.add(aggrFill);
            updateAggregateLayoutRepaint.add(aggregateLayout);
            updateAggregateLayoutRepaint.add(repaintAction);
            viz.putAction(AnalysisConstants.ACTION_UPDATE_AGGREGATE_LAYOUT_REPAINT,
                    updateAggregateLayoutRepaint);
            
            ActionList layout = new ActionList();
            layout.add(new NbFruchtermanReingoldLayout(
                    AnalysisConstants.GRAPH_GROUP));
            layout.add(aggregateLayout);
            layout.add(repaintAction);
            viz.putAction(AnalysisConstants.ACTION_LAYOUT, layout);
            
            ActionList layoutR = new ActionList();
            layoutR.add(new NbFruchtermanReingoldLayout(
                    AnalysisConstants.GRAPH_GROUP));
            layoutR.add(aggregateLayout);
            layoutR.add(repaintAction);
            viz.putAction(AnalysisConstants.ACTION_LAYOUT_REPAINT, layoutR);
            
            
            // remove SubtreeDragControl from Display - it doesn't
            // work well with the AggregateItems
//            display.addControlListener(new SubtreeDragControl());
            display.setHighQuality(true);
            display.addControlListener(new AggregateDragControl());
            display.addControlListener(new PanControl());
            display.addControlListener(new ZoomControl());
            display.addControlListener(new WheelZoomControl());
            display.addControlListener(new ToolTipControl(
                    AnalysisConstants.TOOLTIP)); // "tooltip"
            FindUsagesFocusControl selectionFocusControl =
                    new FindUsagesFocusControl(1,
                    AnalysisConstants.ACTION_UPDATE_REPAINT);
            FindUsagesFocusControl schemaViewSearchFocusControl =
                    new FindUsagesFocusControl(2,
                    AnalysisConstants.ACTION_UPDATE_AGGREGATE_LAYOUT_REPAINT);
            selectionFocusControl.addGraphNodeSelectionChangeListener(
                    viewer);
            // FindUsagesFocusControl default is SINGLE selection
            display.addControlListener(selectionFocusControl);   // one click
            display.addControlListener(schemaViewSearchFocusControl);   // double click
            display.addControlListener(
                    new MouseoverActionControl(AnalysisConstants.ACTION_UPDATE_REPAINT));// mouseover
            display.addControlListener(
                    new NeighborHighlightControl(AnalysisConstants.ACTION_UPDATE_REPAINT)); //NOI18N
            display.addControlListener(new PopupMouseControl());
            display.addControlListener(new NodeExpansionMouseControl(viz,
                    AnalysisConstants.ACTION_UPDATE_AGGREGATE_LAYOUT_REPAINT));
            //////////////////////////////////////////////////////////////////
            display.addControlListener(new ControlAdapter(){
                public void mouseClicked(MouseEvent evt){
                    if ( evt.isAltDown()){
                        paletteCount++;
                        if (paletteCount > palettes.length-1){
                            paletteCount = 0;
                        }
                        ColorAction aFill = new DataColorAction(
                                AnalysisConstants.GRAPH_GROUP_AGGR,
                                AnalysisConstants.ID,
                                Constants.NOMINAL,
                                VisualItem.FILLCOLOR, palettes[1]);
//                                VisualItem.FILLCOLOR, palettes[paletteCount]);
                        ActionList drawAggr = new ActionList();
                        drawAggr.add(aFill);
                        drawAggr.add(repaintAction);
                        viz.putAction("drawAggr", drawAggr);
                        viz.run("drawAggr");
			String paletteName = null;
                        switch (paletteCount){
                            case 0:
                                paletteName = "HSB Palette:  Saturation 10f, Brightness 150f";
                                break;
                            case 1:
                                paletteName = "Grayscale Palette";
                                break;
                            case 2:
                                paletteName = "Cool Palette";
                                break;
                            case 3:
                                paletteName = "Hot Palette";
                                break;
                            case 4:
                                paletteName = "Interpolated (204,255,255) to (255,204,255)";
                                break;
                        }
//                        StatusDisplayer.getDefault().setStatusText(paletteName);
                    }
                }
            });
            
            if (usePacer) {
                // animated transition
                ActionList animate = new ActionList(1500, 40);
                animate.setPacingFunction(new SlowInSlowOutPacer());
                animate.add(new QualityControlAnimator());
                animate.add(new VisibilityAnimator());
//                animate.add(new PolarLocationAnimator(
//                        AnalysisConstants.GRAPH_GROUP));
                animate.add(new ColorAnimator());
                animate.add(repaintAction);
                viz.putAction(AnalysisConstants.ACTION_ANIMATE, animate);
                viz.runAfter(AnalysisConstants.ACTION_LAYOUT_REPAINT, AnalysisConstants.ACTION_ANIMATE);
		usePacer(false);
            }
            viz.runAfter(AnalysisConstants.ACTION_DRAW, AnalysisConstants.ACTION_LAYOUT_REPAINT);
            
            viz.run(AnalysisConstants.ACTION_DRAW);
            wasShown = true;
            
            
        } catch ( Exception e ) {
            ErrorManager.getDefault().notify(e);
        }
        return wasShown;
    }
    
    
    /**
     *  Create visual aggregates for each file node group
     * returns the number of aggregates created
     *
     */
    private int createAggregates(Visualization vis, AggregateTable at, List<NodeItem> fileNodes){
        Map<Integer,AggregateItem> fileMap = new HashMap<Integer,AggregateItem>();
        Iterator nodes = vis.getGroup(AnalysisConstants.GRAPH_GROUP_NODES).tuples();
        while(nodes.hasNext()){
            Integer fileGroup = null;
            NodeItem n = NodeItem.class.cast(nodes.next());
            // skip the query node
            if (n.getBoolean(AnalysisConstants.IS_QUERY_NODE)){
                continue;
            }
            // schema component nodes will have a positive FILE_GROUP number
            if (n.canGetInt(AnalysisConstants.FILE_GROUP)){
                fileGroup = (Integer)n.getInt(AnalysisConstants.FILE_GROUP);
                if (fileGroup == -1){
                    // file nodes will have a positive FILE_NODE_FILE_GROUP
                    if (n.canGetInt(AnalysisConstants.FILE_NODE_FILE_GROUP)){
                        fileNodes.add(n);
                        fileGroup =
                                (Integer)n.getInt(AnalysisConstants.FILE_NODE_FILE_GROUP);
                    }
                }
            }
            if (fileGroup != null){
                AggregateItem ai = null;
                if (fileMap.containsKey(fileGroup)){
                    ai = fileMap.get(fileGroup);
                } else {
                    ai = (AggregateItem)at.addItem();
                    ai.setInt(AnalysisConstants.ID, fileGroup.intValue());
                    ai.setBoolean(AnalysisConstants.IS_FILE_GROUP_AGGREGATE,true);
                    fileMap.put(fileGroup,ai);
                }
                ai.addItem(n);
            }
        }
        return fileMap.size();
    }
    
    public void usePacer(boolean use) {
        this.usePacer = use;
    }
    
    /**
     *  Should the SchemaColumnView make the Column
     *  that the View is shown in as wide as possible?
     *  @return boolean true if View should be shown
     *    in a column as wide as the available horizontal space
     *    in the column view
     */
    public boolean getMaximizeWidth(){
        return true;
    }
    
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(NODE_SELECTION_CHANGE)){
            // In the FindUsages explorer, a new node has been selected
            Object newVal = evt.getNewValue();
           // if (newVal instanceof Usage) {
            if(newVal instanceof RefactoringElement){
                newVal = ((RefactoringElement)newVal).getLookup().lookup(Object.class);
            }
            if (display == null || graph == null){
                return;
            }
            Visualization vis = display.getVisualization();
            // get the current focus set
            TupleSet ts = vis.getFocusGroup(vis.FOCUS_ITEMS);
//            setUnselectNodeStroke(ts);
            ts.clear();
            if (newVal == null){
                vis.run(AnalysisConstants.ACTION_UPDATE_REPAINT);
                return;
            }
            TupleSet allItems = vis.getGroup(AnalysisConstants.GRAPH_GROUP_NODES);
            Iterator it = allItems.tuples();
            while (it.hasNext()){
                Tuple n = (Tuple)it.next();
               // Component sc = (Component)n.get(AnalysisConstants.XAM_COMPONENT);
                Object sc = n.get(AnalysisConstants.USER_OBJECT);
                if(sc == null)
                    if(n.canGetString(AnalysisConstants.XAM_COMPONENT))
                        sc = (Component)n.get(AnalysisConstants.XAM_COMPONENT);
                FileObject fo = (FileObject)n.get(AnalysisConstants.FILE_OBJECT);
                if (sc == newVal || fo == newVal) {
                    ts.setTuple(n);
                    break;
                }
            }
            vis.run(AnalysisConstants.ACTION_UPDATE_REPAINT);
        }
    }
    
    /**
     *  Switch to a different graph
     * @param newGraph the new Graph to use for the Visualization
     */
    public void setGraph(Graph newGraph){
        this.graph = newGraph;
    }

    public Object[] createModels() {
        return null;
    }
    
    
}
