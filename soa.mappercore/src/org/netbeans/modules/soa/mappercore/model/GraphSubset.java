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

import java.util.ArrayList;
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
    
    private Vertex minXVertex;
    private Vertex minYVertex;
    
    
    public GraphSubset(Vertex vertex) {
        this.treePath = null;
        this.graph = null;
        
        if (vertex != null) {
            this.verteces = new Vertex[] { vertex };
            minXVertex = vertex;
            minYVertex = vertex;
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
        
        if (verteces != null && !verteces.isEmpty()) {
            minXVertex = verteces.get(0);
            for (int i = verteces.size() - 1; i > 0; i--) {
                Vertex vertex = verteces.get(i);
                if (minXVertex.getX() > vertex.getX()) {
                    minXVertex = vertex;
                }
            }

            minYVertex = verteces.get(0);
            for (int i = verteces.size() - 1; i > 0; i--) {
                Vertex vertex = verteces.get(i);
                if (minYVertex.getY() > vertex.getY()) {
                    minYVertex = vertex;
                }
            }
        }
    }
    
    public GraphSubset(GraphSubset graphSubset) {
        this(graphSubset, graphSubset.getTreePath());
    }
    
    public GraphSubset(GraphSubset graphSubset, TreePath treePath) {
        List<Vertex> verteces = new ArrayList<Vertex>();
        List<Link> links = new ArrayList<Link>();
        
        for (int i = 0; i < graphSubset.getVertexCount(); i++) {
            verteces.add(createVertex(graphSubset.getVertex(i)));
        }
        
        for (int i = 0; i < graphSubset.getLinkCount(); i++) {
            Link link = createLink(graphSubset.getLink(i), verteces, graphSubset);
            if (link != null) {
                links.add(link);
            }
        }
        
        this.treePath = treePath;
        this.graph = graphSubset.getGraph();
        this.verteces = (verteces != null && !verteces.isEmpty()) 
                ? verteces.toArray(new Vertex[verteces.size()])
                : EMPTY_VERTECES;
        this.links = (links != null && !links.isEmpty()) 
                ? links.toArray(new Link[links.size()])
                : EMPTY_LINKS;
        
        minXVertex = graphSubset.getMinXVertex();
        minYVertex = graphSubset.getMinYVertex();
    }
           
    
    public TreePath getTreePath() { return treePath; }
    public Graph getGraph() { return graph; }

    public int getVertexCount() { return verteces.length; }
    public Vertex getVertex(int i) { return verteces[i]; }
    
    public int getLinkCount() { return links.length; }
    public Link getLink(int i) { return links[i]; }
    
    public boolean isEmpty() { return links.length == 0 && verteces.length == 0; }
    
    public boolean containVertex(Vertex vertex) {
        int length = verteces.length - 1;
        for (int i = length; i >= 0; i--) {
            if (verteces[i] == vertex) { return true; }
        }
        return false;
    }
    
    public Vertex getMinXVertex() {
        return minXVertex;
    }
    
    public Vertex getMinYVertex() {
        return minYVertex;
    }
    
    private int ingexOf(Vertex vertex) {
        int length = verteces.length;
        for (int i = 0; i < length; i++) {
            if (verteces[i] == vertex) { return i; }
        }
        return -1;
    }
    private Vertex createVertex(Vertex vertex) {
        Vertex newVertex = null;
        if (vertex instanceof Constant) {
            newVertex = new Constant(vertex.getDataObject(), vertex.getIcon());
        } 
        if (vertex instanceof Operation) {
            newVertex = new Operation(vertex.getDataObject(), vertex.getIcon());
        }
        if (vertex instanceof Function) {
            String name = ((Function)vertex).getName();
            String text = ((Function)vertex).getResultText();
            newVertex = new Function(vertex.getDataObject(), vertex.getIcon(), name, text);
        }
//        if (vertex.getClass() == Vertex.class ) {
//            newVertex = new Vertex(vertex.getDataObject(), vertex.getIcon()) {};
//        }
        for (int i = 0; i < vertex.getItemCount(); i++) {
            VertexItem item= vertex.getItem(i);
            new VertexItem(newVertex, item.getDataObject(), item.getValue(),
                    item.getValueType(), item.getShortDescription(), item.isHairline());
            newVertex.addItem(new VertexItem(newVertex, item.getDataObject(), item.getValue(),
                    item.getValueType(), item.getShortDescription(), item.isHairline()));
        }
        
        newVertex.setLocation(vertex.getX(), vertex.getY());
        newVertex.setWidth(vertex.getWidth());
        newVertex.setHeight(vertex.getHeight());
        newVertex.setResultText(vertex.getResultText());
       // newVertex.setGraph(vertex.getGraph());
        return newVertex;
    }
    
    private Link createLink(Link link, List<Vertex> verteces, GraphSubset graphSubset) {
        TargetPin target = link.getTarget();
        SourcePin source = link.getSource();
        TargetPin newTarget = null;
        SourcePin newSource = null;
        if (source instanceof TreeSourcePin) {
            newSource = source;
        }
        if (source instanceof Vertex) {
            int index = graphSubset.ingexOf((Vertex) source);
            if (index < 0) {
                newSource = null;
            } else {
                newSource = verteces.get(index);
            }
        }
        
        if (target instanceof Graph) {
            newTarget = null;
        }
        
        if (target instanceof VertexItem) {
            VertexItem item = (VertexItem) target;
            int indexV = graphSubset.ingexOf(item.getVertex());
            int indexI = item.getVertex().getItemIndex(item);
            if (indexV < 0) {
                newTarget = null;
            } else {
                newTarget = verteces.get(indexV).getItem(indexI);
            }
        }
        
        if (newSource != null && newTarget != null) {
            return new Link(newSource, newTarget);
        } 
    
        return null;
    }
    
    private static final Vertex[] EMPTY_VERTECES = new Vertex[0];
    private static final Link[] EMPTY_LINKS = new Link[0];
}
