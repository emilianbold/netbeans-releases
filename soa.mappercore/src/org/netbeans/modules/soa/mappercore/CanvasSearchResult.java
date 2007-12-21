package org.netbeans.modules.soa.mappercore;

import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphItem;

/**
 *
 * @author anjeleevich
 */
public class CanvasSearchResult {
    
    private TreePath treePath;
    private Graph graph;
    
    private GraphItem graphItem;
    private GraphItem pinItem;
    

    public CanvasSearchResult(TreePath treePath, Graph graph, 
            GraphItem graphItem, GraphItem pinItem)
    {
        this.treePath = treePath;
        this.graph = graph;
        this.graphItem = graphItem;
        this.pinItem = pinItem;
    }
    
    
    public TreePath getTreePath() {
        return treePath;
    }
    
    
    public Graph getGraph() {
        return graph;
    }
    
    
    public GraphItem getGraphItem() {
        return graphItem;
    }


    public GraphItem getPinItem() {
        return pinItem;
    }
}
