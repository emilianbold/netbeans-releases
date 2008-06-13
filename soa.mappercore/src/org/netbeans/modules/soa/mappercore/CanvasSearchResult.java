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
