/*
 * GraphUtilities.java
 *
 * Created on July 11, 2006, 12:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse.util;

import java.util.Iterator;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import prefuse.Visualization;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;

/**
 *
 * @author Jeri Lockhart
 */
public abstract class GraphUtilities {
    
   
    /**
     *  Expand or collapse a file node
     *  depending on whehter it is currently expanded or collapsed
     *
     *
     *
     */
    public static void expandCollapseFileNode(NodeItem fileNode){
        if (fileNode == null){
            return;
        }
        Visualization vis = fileNode.getVisualization();
        boolean isExpanded = fileNode.getBoolean(AnalysisConstants.IS_EXPANDED);
        fileNode.setBoolean(AnalysisConstants.IS_EXPANDED, !isExpanded);
        
        Predicate p = (Predicate)
        ExpressionParser.parse("["+AnalysisConstants.FILE_GROUP+"] = " +      //NOI18N
                fileNode.getInt(AnalysisConstants.FILE_NODE_FILE_GROUP));
        vis.setVisible(AnalysisConstants.GRAPH_GROUP, p, !isExpanded);
        
        
        // hide or show the edge from the file node to the query node
        Iterator outEdges = fileNode.outEdges();
        while(outEdges.hasNext()){
            EdgeItem edge = EdgeItem.class.cast(outEdges.next());
            if (edge.getString(AnalysisConstants.EDGE_TYPE).equals(
                    AnalysisConstants.FILE_EDGE_TYPE)){
                edge.setVisible(isExpanded);
            }
        }
    }
    
}
