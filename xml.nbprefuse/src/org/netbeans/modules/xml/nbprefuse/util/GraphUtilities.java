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
