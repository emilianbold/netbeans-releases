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
package org.netbeans.modules.bpel.mapper.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentGroup;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.netbeans.modules.bpel.mapper.palette.Palette;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 * The default implementation of the MapperModel interface for the BPEL Mapper.
 * 
 * @author nk160297
 * @author Vitaly Bychkov
 * @author AlexanderPermyakov
 */
public class BpelMapperModel implements MapperModel, MapperTcContext.Provider {

    public final Graph STUB_GRAPH;
    
    private MapperTcContext mMapperTcContext;
    private GraphChangeProcessor mChangeProcessor;
    private MapperSwingTreeModel mLeftTreeModel;
    private MapperSwingTreeModel mRightTreeModel;
    private ConnectionConstraint[] mConnectionConstraints;
    
    // Maps a TreePath to a Graph
    private Map<TreePath, Graph> mPathGraphMap = new HashMap<TreePath, Graph>();

    public BpelMapperModel(MapperTcContext mapperTcContext, 
            GraphChangeProcessor changeProcessor, 
            MapperTreeModel leftModel, MapperTreeModel rightModel) {
        //
        mMapperTcContext = mapperTcContext;
        mChangeProcessor = changeProcessor;
        //
        mLeftTreeModel = new MapperSwingTreeModel(mMapperTcContext, leftModel);
        //
        mRightTreeModel = new MapperSwingTreeModel(mMapperTcContext, rightModel);
        //
        STUB_GRAPH = new Graph(this);
        //
        mConnectionConstraints = new ConnectionConstraint[] {
                ConnectionConstraint.Access.getGeneralConstraint(this),
                ConnectionConstraint.Access.getPlConstraint(),
                ConnectionConstraint.Access.getMVarConstraint()
        };
    }

    public MapperTcContext getMapperTcContext() {
        return mMapperTcContext;
    }

    public MapperSwingTreeModel getRightTreeModel() {
        return mRightTreeModel;
    }

    public MapperSwingTreeModel getLeftTreeModel() {
        return mLeftTreeModel;
    }

    //==========================================================================
    //  Gentral graph methods
    //==========================================================================
    
    public TreeSourcePin getTreeSourcePin(TreePath treePath) {
        return new TreeSourcePin(treePath);
    }

    public Graph getGraph(TreePath treePath) {
        Graph result = mPathGraphMap.get(treePath);
        return (result == null) ? STUB_GRAPH : result;
    }

    public boolean searchGraphsInside(TreePath path) {
        Object parent = path.getLastPathComponent();

        for (TreePath treePath : mPathGraphMap.keySet()) {
            while (true) {
                // The last path object is skipped here
                treePath = treePath.getParentPath();
                if (treePath == null) {
                    break;
                }
                Object pathItem = treePath.getLastPathComponent();
                if (pathItem == parent) {
                    return true;
                }
            }
        }
        //
        return false;
    }

    public void addGraph(Graph newGraph, TreePath treePath) {
        mPathGraphMap.put(treePath, newGraph);
        // fireGraphChanged(treePath);
        mRightTreeModel.fireTreeChanged(this, treePath);
    }

    /**
     * Delete graph from mapper model. 
     * The method doesn't do any checks and doesn't send any notifications.
     * It simply delete the graph from the cache.
     * 
     * This method is intended to be called as part of update processing 
     * is initiated by a change in mapper. 
     * If you need to remove a graph, you better use emptyGraph method 
     * and it will be deleted automatically. 
     * 
     * @param treePath
     */
    public void deleteGraph(TreePath treePath) {
        Graph graph = mPathGraphMap.get(treePath);
        if (graph != null) {
            mPathGraphMap.remove(treePath);
        }
    }

    /**
     * Remove all graphs are located in a tree branch.
     * @param rootTreePath the root of the tree branch.
     */
    public void removeNestedGraphs(TreePath rootTreePath) {
        Map<TreePath, Graph> nestedGraphs = getGraphsInside(rootTreePath);
        Set<TreePath> graphPathesToDelete = nestedGraphs.keySet();
        //
        boolean modified = false;
        for (TreePath graphTPath : graphPathesToDelete) {
            Graph graph = mPathGraphMap.get(graphTPath);
            if (graph != null) {
                //
                modified = emptyGraph(graph);
                //
                // It's not necessary to remove the graph itself. 
                // It has to be removed by the BPEL updater a bit later.
                // mPathGraphMap.remove(graphTPath);
            }
        }
        //
        if (modified) {
            // Modify BPEL model for all changed graphs in one transaction.
            fireGraphsChanged(graphPathesToDelete);
        }
    }
    
    private boolean emptyGraph(Graph graph) {
        boolean modified = false;
        List<Vertex> vertexList = graph.getVerteces();
        for (Vertex vertex : vertexList) {
            graph.removeVertex(vertex);
            modified = true;
        }
        //
        List<Link> linksList = graph.getLinks();
        for (Link link : linksList) {
            graph.removeLink(link);
            modified = true;
        }
        //
        return modified;
    }
    
    /**
     * Takes an existing graph or creates a new one and registers it in this mapper.
     * @param treePath - the graph's location.
     * @return required graph.
     */
    public Graph graphRequired(TreePath treePath) {
        Graph graph = getGraph(treePath);
        if (graph == null || graph == STUB_GRAPH) {
            graph = new Graph(this);
            mPathGraphMap.put(treePath, graph);
        }
        return graph;
    }

    public Map<TreePath, Graph> getGraphsInside(TreePath root) {
        if (root == null || 
                root.getLastPathComponent() == getRightTreeModel().getRoot()) {
            return mPathGraphMap;
        }
        //
        HashMap<TreePath, Graph> result = new HashMap<TreePath, Graph>();
        for (TreePath tPath : mPathGraphMap.keySet()) {
            if (root.isDescendant(tPath)) {
                Graph graph = mPathGraphMap.get(tPath);
                assert graph != null;
                result.put(tPath, graph);
            }
        }
        //
        Graph rootGraph = mPathGraphMap.get(root);
        if (rootGraph != null) {
            result.put(root, rootGraph);
        }
        //
        return result;
    }
    
    //==========================================================================
    //   Modification methods
    //==========================================================================

    protected ConnectionConstraint[] getConstraints() {
        return mConnectionConstraints;
    }
    
    public boolean canConnect(TreePath treePath, SourcePin source, 
            TargetPin target, TreePath oldTreePath, Link oldLink) 
    {
    
        ConnectionConstraint[] constraints = getConstraints();
        for (ConnectionConstraint constraint : constraints) {
            if (!constraint.canConnect(treePath, source, target, oldTreePath,
                                      oldLink)) {
                return false;
            }
        }
        return true;
            }
       
    public boolean canCopy(TreePath treePath, GraphSubset graphSubset) {
        return true;
    }

    public boolean canMove(TreePath treePath, GraphSubset graphSubset) {
        return true;
    }

    public void connect(TreePath treePath, SourcePin source, TargetPin target,
            TreePath oldTreePath, Link oldLink) 
    {
//        if (oldLink != null) return;
        
        Graph graph = getGraph(treePath);
        //
        Graph resultGraph;
        if (graph == STUB_GRAPH) {
            //
            // Add the new Graph
            resultGraph = new Graph(this);
            mPathGraphMap.put(treePath, resultGraph);
            target = resultGraph;
        } else {
            resultGraph = graph;
        }
        //
        // Process the case when link is drawn to a hairline vertex item
        if (target instanceof VertexItem) {
            VertexItem vItem = (VertexItem) target;
            if (vItem.isHairline()) {
                Vertex vertex = vItem.getVertex();
                Object dataObject = vItem.getDataObject();
                int index = vertex.getItemIndex(vItem);
                if (dataObject instanceof ArgumentDescriptor) {
                    //
                    // A new real vertex item has to be inserted after the hairline item
                    VertexItem newRealVItem = VertexFactory.constructVItem(
                            vertex, (ArgumentDescriptor)dataObject);
                    vertex.addItem(newRealVItem, index + 1);
                    //
                    // A new hairline item has to be inserted after the real vertex item
                    VertexItem newHirelineVItem = 
                            VertexFactory.constructHairline(vertex, dataObject);
                    vertex.addItem(newHirelineVItem, index + 2);
                    //
                    // Eventually 2 new vertex item is added: real and additional hairline
                    target = newRealVItem;
                } else if (dataObject instanceof ArgumentGroup) {
                    List<VertexItem> itemsList = VertexFactory.getInstance().
                            createGroupItems(vertex, (ArgumentGroup) dataObject);
                    //
                    // Insert new vertex items in the back direction
                    //
                    // A new hairline item will appear just after the group's items.
                    VertexItem newHirelineVItem = 
                            VertexFactory.constructHairline(vertex, dataObject);
                    vertex.addItem(newHirelineVItem, index + 1);
                    //
                    // Insert a sequence of vertex items to the position next to 
                    // the initial hairline. The items are inserted in the 
                    // back direction but in the same place, so previous item move 
                    // down when the next is inserted. 
                    ListIterator<VertexItem> backItr =
                            itemsList.listIterator(itemsList.size());
                    while (backItr.hasPrevious()) {
                        VertexItem vertItem = backItr.previous();
                        vertex.addItem(vertItem, index + 1);
                    }
                    //
                    // Looking for the item to which the link has to be connected
                    VertexItem newTargetVItem = null;
                    XPathType sourceType =
                            BpelMapperUtils.calculateXPathSourcePinType(source);
                    if (sourceType != null) {
                        newTargetVItem = BpelMapperUtils.
                                findBestFittedItem(itemsList, sourceType);
                    }
                    //
                    if (newTargetVItem != null) {
                        target = newTargetVItem;
                    }
                }
            }
            vItem.setValue(null);
        }
        //
        if (oldLink == null) {
            Link newLink = new Link(source, target);
            resultGraph.addLink(newLink);
        } else {
            oldLink.setSource(source);
            oldLink.setTarget(target);
            resultGraph.addLink(oldLink);
            if (!Utils.equal(oldTreePath, treePath)) {
                fireGraphChanged(oldTreePath);
                mRightTreeModel.fireTreeChanged(this, oldTreePath);
            }
        }
        //
        fireGraphChanged(treePath);
        mRightTreeModel.fireTreeChanged(this, treePath);
    }

    // vlv
    public GraphSubset getGraphSubset(Transferable transferable) {
//out();
        for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
            try {
//out("see: " + transferable.getTransferData(flavor));
                Object[] objects = (Object[]) transferable.getTransferData(flavor);
                myHandler = (ItemHandler) objects[0];
                GraphSubset graph = myHandler.createGraphSubset();
                Palette palette = (Palette) objects[1];
//out("graph: " + graph);

                if (graph != null) {
                    palette.hideMenu();
                    return graph;
                }
            } catch (IOException e) {
                continue;
            } catch (UnsupportedFlavorException e) {
                continue;
            }
        }
        return null;
    }
    private ItemHandler myHandler;

    private boolean isConnectable(TreePath treePath) {
        if (treePath == null) {
            return false;
        }
        return mRightTreeModel.isConnectable(treePath);
    }

    public GraphSubset add(TreePath treePath, ItemHandler handler, int x, int y) {
        myHandler = handler;
        return doCopy(treePath, null, x, y);
     }

    public GraphSubset copy(TreePath treePath, GraphSubset graphSubset, int x, int y) {
        return doCopy(treePath, graphSubset, x, y);
    }

    private GraphSubset doCopy(TreePath treePath, GraphSubset graphSubset, int x, int y) {
        if ( !isConnectable(treePath)) {
            return null;
        }
        if (myHandler != null) {
            if (myHandler.canAddGraphSubset()) {
                graphSubset = myHandler.createGraphSubset();
                myHandler = null;
            } else {
                graphSubset = null;
            }
        } else {
            graphSubset = new GraphSubset(graphSubset, treePath);
        }
        
        if (graphSubset == null) {
            return null;
        }
        
        Graph graph = graphRequired(treePath);
       // int nextX = x;
        int x0 = 0;
        int y0 = 0;
        if (graphSubset.getVertexCount() > 0) {
            x0 = graphSubset.getMinYVertex().getX();
            y0 = graphSubset.getMinYVertex().getY();
        }
        
        if (y < 0) {y = 0;}
            
        for (int i = 0; i < graphSubset.getVertexCount(); i++) {
            Vertex vertex = graphSubset.getVertex(i);

            int xi = graphSubset.getVertex(i).getX();
            int yi = graphSubset.getVertex(i).getY();
            vertex.setLocation(xi - x0 + x, yi - y0 + y);
       //   vertex.setLocation(nextX, y);
       //   nextX = vertex.getX() + vertex.getWidth() + 3;
        }
        for (int i = 0; i < graphSubset.getVertexCount(); i++) {
            Vertex vertex = graphSubset.getVertex(i);
            graph.addVertex(vertex);

        }
        
        for (int i = 0; i < graphSubset.getLinkCount(); i++) {
            Link link = graphSubset.getLink(i);
            graph.addLink(link);
        }
        fireGraphChanged(treePath);
        mRightTreeModel.fireTreeChanged(this, treePath);
        //
        return graphSubset;
    }

    public void move(TreePath treePath, GraphSubset graphSubset, int newX, int newY) {
        if (!isConnectable(treePath)) {
            return;
        }

        if (graphSubset == null) {
            return;
        }
        Graph graph = graphRequired(treePath);
        Graph oldGraph = null;
        List<Link> links = new ArrayList<Link>();
        int x0 = 0;
        int y0 = 0;
        if (graphSubset.getVertexCount() > 0) {
            x0 = graphSubset.getMinYVertex().getX();
            y0 = graphSubset.getMinYVertex().getY();
        }
        

        if (newY < 0) {newY = 0;}
        
        for (int i = graphSubset.getVertexCount() - 1; i >= 0; i--) {
            Vertex vertex = graphSubset.getVertex(i);
                        
            if (vertex.getGraph() != null) {
                oldGraph = vertex.getGraph();
                if (oldGraph != graph) {
               //     oldGraph.removeVertex(vertex);
                    graph.addVertex(vertex);
                    Link link = vertex.getOutgoingLink();
                    if (link != null && oldGraph == link.getGraph()) {
                        oldGraph.removeLink(link);
                        links.add(link);
                    }
                    for (int j = vertex.getItemCount() - 1; j >= 0; j--) {
                        link = vertex.getItem(j).getIngoingLink();
                        if (link != null && oldGraph == link.getGraph()) {
                            oldGraph.removeLink(link);
                            links.add(link);
                        }
                    }
                }
                
                int xi = graphSubset.getVertex(i).getX();
                int yi = graphSubset.getVertex(i).getY();
                
                vertex.setLocation(xi - x0 + newX, yi - y0 + newY);
            }
        }
                
        if (oldGraph != graph) {
            for (int i = graphSubset.getLinkCount() - 1; i >= 0; i--) {
                Link link = graphSubset.getLink(i);
                TargetPin target = link.getTarget();
                SourcePin source = link.getSource();
                if (!(target instanceof Graph) && 
                        graphSubset.containVertex(((VertexItem) target).getVertex()) &&
                        (source instanceof TreeSourcePin || 
                        graphSubset.containVertex((Vertex) source))) 
                {
                    graph.addLink(link);
                } else {
                    link.setSource(null);
                    link.setTarget(null);
           
                }
                links.remove(link);
            }
            for (Link l : links) {
                l.setSource(null);
                l.setTarget(null);
            }
        }
        //
        TreePath oldTreePath = graphSubset.getTreePath();
        if (!Utils.equal(oldTreePath, treePath)) {
            fireGraphChanged(oldTreePath);
            mRightTreeModel.fireTreeChanged(this, oldTreePath);
        }
        
        fireGraphChanged(treePath);
        mRightTreeModel.fireTreeChanged(this, treePath);
    }
    
    public void delete(TreePath currentTreePath, GraphSubset graphGroup) {
        HashSet<Link> linkSet = new HashSet<Link>();
        //
        // Add to selection all links which connected to the selected verteces
        
        for (int i = graphGroup.getVertexCount() - 1; i >= 0; i--) {
            List<Link> connectedLinkList = 
                    BpelMapperUtils.getConnectedLinkList(graphGroup.getVertex(i));
            linkSet.addAll(connectedLinkList);
        }
        
        for (int i = graphGroup.getLinkCount() - 1; i >=0; i--) {
            linkSet.add(graphGroup.getLink(i));
        }
        // Remove the selected verteces and links
        Graph graph = graphGroup.getGraph();
        for (Link link : linkSet) {
            link.disconnect();
        }
        for (int i = graphGroup.getVertexCount() - 1; i >= 0; i--) {
            graph.removeVertex(graphGroup.getVertex(i));
        }
        
        //
        // Initiate graph repaint
        fireGraphChanged(currentTreePath);
    }
    
    public boolean canEditInplace(VertexItem vItem) {
        //return vItem.getIngoingLink() == null;
        return true;
    }



    public void fireGraphChanged(TreePath treePath) {
        if (mChangeProcessor != null) {
            mChangeProcessor.processChanges(treePath);
        }
    }

    public void fireGraphsChanged(Collection<TreePath> treePathList) {
        if (mChangeProcessor != null) {
            mChangeProcessor.processChanges(treePathList);
        }
    }
    
    //==========================================================================
    //   Right tree methods
    //==========================================================================

    public Object getRoot() {
        return mRightTreeModel.getRoot();
    }

    public Object getChild(Object parent, int index) {
        return mRightTreeModel.getChild(parent, index);
    }

    public int getChildCount(Object parent) {
        return mRightTreeModel.getChildCount(parent);
    }

    public boolean isLeaf(Object node) {
        return mRightTreeModel.isLeaf(node);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        mRightTreeModel.valueForPathChanged(path, newValue);
    }

    public int getIndexOfChild(Object parent, Object child) {
        return mRightTreeModel.getIndexOfChild(parent, child);
    }

    public void addTreeModelListener(TreeModelListener l) {
        mRightTreeModel.addTreeModelListener(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        mRightTreeModel.removeTreeModelListener(l);
    }

    public void valueChanged(TreePath treePath, VertexItem vertexItem, 
            Object newValue) 
    {
        if (newValue != null) {
            vertexItem.setValue(newValue);
            Link link = vertexItem.getIngoingLink(); 
            if (link != null) {
                link.disconnect();
            }
            fireGraphChanged(treePath);
            mRightTreeModel.fireTreeChanged(this, treePath);
        }
    }
    
    //===================================================================
    // Predicates support methods
    //===================================================================


    /**
     * Looks for a list of graphs which depends on the specified data object.
     * The data object relates to the left tree item. 
     * @param leftTreeItemDO
     * @return
     */
    public List<TreePath> getDependentGraphs(Object leftTreeItemDO) {
        ArrayList<TreePath> result = new ArrayList<TreePath>();
        //
        Map<TreePath, Graph> graphs = getGraphsInside(null);
        for (TreePath path : graphs.keySet()) {
            Graph graph = graphs.get(path);
            List<Link> connectedLinksList = 
                    graph.getConnectedIngoingLinks(new ArrayList<Link>());
            for (Link link : connectedLinksList) {
                SourcePin sourcePin = link.getSource();
                assert sourcePin instanceof TreeSourcePin;
                TreePath leftTreePath = ((TreeSourcePin)sourcePin).getTreePath();
                if (MapperSwingTreeModel.containsDataObject(
                        leftTreePath, leftTreeItemDO)) {
                    result.add(path);
                    break;
                }
            }
        }
        //
        return result;
    }
 
    /**
     * Remove links which go from the leftNodePath (left tree), which is 
     * going to be deleted. It is necessary because the links has to be 
     * started somewhere. If a link is connected to a node in the left tree, 
     * then it has to be deleted if the node is deleted. 
     * @param graphPath - specified the tree path (right tree) of the graph, 
     * from which the link is going to be deleted.
     * @param leftNodePath - specified the tree path (left tree) of the node,
     * which is going to be deleted. 
     */
    public void removeIngoingLinks(TreePath graphPath, TreePath leftNodePath) {
        //
        Graph graph = getGraph(graphPath);
        List<Link> ingoingLinks = graph.getIngoingLinks();
        for (Link link : ingoingLinks) {
            SourcePin sourcePin = link.getSource();
            assert sourcePin instanceof TreeSourcePin;
            TreePath tPath = ((TreeSourcePin)sourcePin).getTreePath();
            //
            // The link can be connected not directly to the node, which 
            // is going to be deleted, but to its descendant node. 
            // If a node is deleted, then all branch of its children 
            // is also deleted. 
            if (tPath != null && leftNodePath.isDescendant(tPath)) {
                link.disconnect();
            }
        }
    }
}
