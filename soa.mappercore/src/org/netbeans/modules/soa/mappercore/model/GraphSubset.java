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

package org.netbeans.modules.soa.mappercore.model;

import java.util.List;
import javax.swing.tree.TreePath;

/**
 *
 * @author anjeleevich
 */
public class GraphSubset {

    private TreePath treePath;
    private Graph graph;
    
    private Vertex[] verteces;
    private Link[] links;
    
    
    public GraphSubset(Vertex vertex) {
        this.treePath = null;
        this.graph = null;
        
        if (vertex != null) {
            this.verteces = new Vertex[] { vertex };
        } else {
            this.verteces = EMPTY_VERTECES;
        }
        
        this.links = EMPTY_LINKS;
    }
    
    
    public GraphSubset(TreePath treePath, Graph graph, List<Vertex> verteces) {
        this(treePath, graph, verteces, null);
    }
    
    
    public GraphSubset(TreePath treePath, Graph graph, 
            List<Vertex> verteces, 
            List<Link> links) 
    {
        this.treePath = treePath;
        this.graph = graph;
        this.verteces = (verteces != null && !verteces.isEmpty()) 
                ? verteces.toArray(new Vertex[verteces.size()])
                : EMPTY_VERTECES;
        this.links = (links != null && !links.isEmpty()) 
                ? links.toArray(new Link[links.size()])
                : EMPTY_LINKS;
    }
    
    
    public TreePath getTreePath() { return treePath; }
    public Graph getGraph() { return graph; }

    public int getVertexCount() { return verteces.length; }
    public Vertex getVertex(int i) { return verteces[i]; }
    
    public int getLinkCount() { return links.length; }
    public Link getLink(int i) { return links[i]; }
    
    public boolean isEmpty() { return links.length == 0 && verteces.length == 0; }
    
    
    private static final Vertex[] EMPTY_VERTECES = new Vertex[0];
    private static final Link[] EMPTY_LINKS = new Link[0];
}
