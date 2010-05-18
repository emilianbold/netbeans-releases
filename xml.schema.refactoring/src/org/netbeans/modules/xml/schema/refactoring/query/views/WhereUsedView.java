/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * WhereUsedView.java
 *
 * Created on October 25, 2005, 2:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query.views;

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
import org.netbeans.modules.xml.nbprefuse.AggregateDragControl;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.nbprefuse.EdgeFillColorAction;
import org.netbeans.modules.xml.nbprefuse.EdgeStrokeColorAction;
import org.netbeans.modules.xml.nbprefuse.FindUsagesFocusControl;
import org.netbeans.modules.xml.nbprefuse.MouseoverActionControl;
import org.netbeans.modules.xml.nbprefuse.NodeExpansionMouseControl;
import org.netbeans.modules.xml.nbprefuse.NodeFillColorAction;
import org.netbeans.modules.xml.nbprefuse.NodeStrokeColorAction;
import org.netbeans.modules.xml.nbprefuse.NodeTextColorAction;
import org.netbeans.modules.xml.nbprefuse.PopupMouseControl;
import org.netbeans.modules.xml.nbprefuse.View;
import org.netbeans.modules.xml.nbprefuse.layout.AggregateLayout;
import org.netbeans.modules.xml.nbprefuse.layout.NbFruchtermanReingoldLayout;
import org.netbeans.modules.xml.nbprefuse.render.CompositionEdgeRenderer;
import org.netbeans.modules.xml.nbprefuse.render.FindUsagesRendererFactory;
import org.netbeans.modules.xml.nbprefuse.render.GeneralizationEdgeRenderer;
import org.netbeans.modules.xml.nbprefuse.render.NbLabelRenderer;
import org.netbeans.modules.xml.nbprefuse.render.ReferenceEdgeRenderer;
import org.netbeans.modules.xml.refactoring.spi.AnalysisUtilities;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryUtilities;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
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
    
    private SchemaModel model;
    private SchemaComponent query;
    private Display display = null;
    private JPanel displayPanel;
    private boolean isPrimitive;
    private Graph graph;
    private boolean usePacer = false;    // slow in slow out pacer for initial
    //              graph animation
//    private boolean firstShow = false;
    private int resizeCounter = 0;       // counter to prevent reshowing
    //view on first invocation
//    private DefaultTreeModel defaultTreeModel;
    
    private static int paletteCount = 0;
    
    
    
    public WhereUsedView(SchemaComponentReference ref){
        if (ref != null){
            this.query = (SchemaComponent)ref.get();
            this.isPrimitive =  query.getModel() ==
                    SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            this.model =
                    (this.isPrimitive?
                        SchemaModelFactory.getDefault().getPrimitiveTypesModel():
                        query.getModel());
        }
    }
    /**
     * Creates a new instance of WhereUsedView
     *  taking a SchemaComponent argument
     */
    public WhereUsedView(SchemaModel model,
            SchemaComponent query,
            boolean isPrimitive
            ) {
        this.model = model;
        this.query = query;
        this.isPrimitive = isPrimitive;
    }
    
    
    /**
     * Creates a new instance of WhereUsedView
     *  taking a SchemaComponent argument
     */
    public WhereUsedView(SchemaModel model, SchemaComponent query) {
        this(model, query, false);
    }
    
    
    /**
     *  Implement View
     *
     */
    
    public JPanel getDisplayPanel() {
        return displayPanel;
    }
    public Named getQueryComponent() {
        return (Named)query;
    }
    
    /**
     * Create the graph and the tree model
     *  or only the graph
     *
     */
    public Object[] createModels(){
        return null;
    }
    
   
    public boolean showView(AnalysisViewer viewer) {
        boolean wasShown = false;
        
        
        // resizing will call showView() from AnalysisViewer
        // prevent showing the view twice the first time
        if (resizeCounter == 0){
            resizeCounter++;
        } else if (resizeCounter == 1 || resizeCounter == 2){
            resizeCounter++;
            return wasShown;
        }
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
                display.setSize(dim.width < 1?AnalysisConstants.DISPLAY_PREFERRED_WIDTH:
                    dim.width,dim.height<1?AnalysisConstants.DISPLAY_PREFERRED_HEIGHT:dim.height);
                viewer.getPanel().setSize(display.getSize());
            } else if (!dim.equals(displayDim)) {
                display.setSize(dim.width, dim.height);
            }
            
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
                QueryUtilities.getHSBPalette(
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
    
    // TODO was in RefactoringPanel
    public static final String
            NODE_SELECTION_CHANGE = "node-selection-change"; // NOI18N  fire
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(
                NODE_SELECTION_CHANGE)){
            // In the FindUsages explorer, a new node has been selected
            Object newVal = evt.getNewValue();
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
                if (n.canGet(AnalysisConstants.XAM_COMPONENT,
                        Component.class)){
                    Component sc = (Component)n.get(
                            AnalysisConstants.XAM_COMPONENT);
                    if (sc == newVal){
                        ts.setTuple(n);
//                        NodeItem.class.cast(n).setStroke(AnalysisConstants.SELECTED_STROKE);
                        break;
                    }
                } else if (n.canGet(AnalysisConstants.FILE_OBJECT,
                        FileObject.class)){
                    FileObject fo = (FileObject)n.get(
                            AnalysisConstants.FILE_OBJECT);
                    if (fo == newVal){
                        ts.setTuple(n);
//                        NodeItem.class.cast(n).setStroke(AnalysisConstants.SELECTED_STROKE);
                        break;
                    }
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
    
    
    /**
     *  Assumes single selection of graph nodes
     *
     *
     */
//    private void setUnselectNodeStroke(TupleSet currentlySelectedTuples){
//        Tuple currentlySelected = null;
//        Iterator currentlySelectedIt = currentlySelectedTuples.tuples();
//        if (currentlySelectedIt.hasNext()){
//            currentlySelected = (Tuple)currentlySelectedIt.next();
//        }
//        assert (currentlySelectedIt.hasNext()==false):"The Find Usages graph supports only single selection of nodes.";
//        if (currentlySelected instanceof NodeItem){
//            NodeItem.class.cast(currentlySelected).setStroke(AnalysisConstants.UNSELECTED_STROKE);
//        }
//        else if (currentlySelected != null){
//            assert true:"The currently selected item is not a NodeItem: " + currentlySelected;
//        }
//    }
    
    
}
