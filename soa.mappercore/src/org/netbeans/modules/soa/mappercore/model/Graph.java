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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.CanvasRendererContext;

/**
 *
 * @author anjeleevich
 */
public class Graph implements TargetPin {
    
    private MapperModel mapperModel;

    private List<Link> links;
    private List<Vertex> verteces;
    
    private int preferredHeight = 2;

    private List<Link> ingoingLinks = null;
    private boolean hasOutgoingLinks = false;
    private Link outgoingLink = null;
    
    private Set<Link> connectedIngoingLinks = null;
    private Set<Link> connectedOutgoingLinks = null;
    
    private boolean validLinks = false;
    private boolean validBounds = false;
    private boolean validOutgoingLinks = false;
    
    private Rectangle bounds = null;
    
    private List<GraphListener> graphListeners = null;

    private Object dataObject;

    
    public Graph(MapperModel mapperModel) {
        this(mapperModel, null);
    }
    
    
    public Graph(MapperModel mapperModel, Object dataObject) {
        this.mapperModel = mapperModel;
        this.dataObject = dataObject;
    }
    
    
    public Object getDataObject() {
        return dataObject;
    }

    public Link getPrevLink(Link link, List<Link> links) {
        if (links == null || links.size() == 0) return null;
        
        Link maxLink = null;
        if (link == null) {
            maxLink = links.get(0);
            for (Link l : links) {
                if (LINK_COMPARATOR.compare(maxLink, l) < 0) {
                    maxLink = l;
                }
            }
            return maxLink;
        } else {
            if (!links.contains(link)) {
                return null;
            }
            for (Link l : links) {
                if (LINK_COMPARATOR.compare(link, l) > 0) {
                    if (maxLink == null) {
                        maxLink = l;
                    } else {
                        if (LINK_COMPARATOR.compare(maxLink, l) < 0) {
                            maxLink = l;
                        }
                    }
                }
            }
            return maxLink;
        }
    }
    
    public Link getPrevLink(Link link) {
        return getPrevLink(link, ingoingLinks);
    }
    
    public void setDataObject(Object dataObject) {
        this.dataObject = dataObject;
    }
    
    
    public boolean hasVerteces() {
        return verteces != null && !verteces.isEmpty();
    }
    
    
    public boolean hasLinks() {
        return links != null && !links.isEmpty();
    }
    
    
    public boolean isEmpty() {
        return !hasLinks() && !hasVerteces();
    }
    
    public boolean isEmptyOrOneLink() {
        return !hasVerteces() && (links == null || links.size() <= 1);
    }
    
    public void addGraphListener(GraphListener graphListener) {
        if (graphListener == null) throw new IllegalArgumentException();
        
        if (graphListeners == null) {
            graphListeners = new ArrayList<GraphListener>();
        }
        
        graphListeners.add(graphListener);
    }
    
    
    public void removeGraphListener(GraphListener graphListener) {
        if (graphListener == null) throw new IllegalArgumentException();
        
        if (graphListeners != null) {
            int index = graphListeners.lastIndexOf(graphListener);
            if (index >= 0) {
                graphListeners.remove(index);
                if (graphListeners.isEmpty()) {
                    graphListeners = null;
                }
            }
        }
    }
    

    void fireGraphBoundsChanged() {
        if (graphListeners != null) {
            for (int i = graphListeners.size() - 1; i >= 0; i--) {
                graphListeners.get(i).graphBoundsChanged(this);
            }
        }
    }
    
    
    void fireGraphLinksChanges() {
        if (graphListeners != null) {
            for (int i = graphListeners.size() - 1; i >= 0; i--) {
                graphListeners.get(i).graphLinksChanged(this);
            }
        }
    }
    
    
    void fireGraphContentChanged() {
        if (graphListeners != null) {
            for (int i = graphListeners.size() - 1; i >= 0; i--) {
                graphListeners.get(i).graphContentChanged(this);
            }
        }
    }
    
    
    public boolean hasIngoingLinks() {
        validateLinks();
        return ingoingLinks != null && !ingoingLinks.isEmpty();
    }
    
    
    public boolean hasOutgoingLinks() {
        validateOutgoingLinks();
        return hasOutgoingLinks;
    }
    
    public List<Link> getOutgoingLinks() {
        return null;
    }
    
    public Link getOutgoingLink() {
        validateOutgoingLinks();
        return outgoingLink;
    }
    
    public boolean hasConnectedOutgoingLinks() {
        validateLinks();
        return connectedOutgoingLinks != null 
                && !connectedOutgoingLinks.isEmpty();
    }
    
    
    
    public List<Link> getIngoingLinks() {
        return getIngoingLinks(null);
    }
    
    
    public List<Link> getIngoingLinks(List<Link> result) {
        validateLinks();
        
        if (result == null) {
            result = (ingoingLinks != null) 
                    ? new ArrayList<Link>(ingoingLinks.size())
                    : new ArrayList<Link>();
        }
        
        if (ingoingLinks != null) {
            result.addAll(ingoingLinks);
        }
        
        return result;
    }
    
    
    public List<Link> getConnectedIngoingLinks(List<Link> result) {
        validateLinks();
        
        if (result == null) {
            result = (connectedIngoingLinks != null) 
                    ? new ArrayList<Link>(connectedIngoingLinks.size())
                    : new ArrayList<Link>();
        }
        
        if (connectedIngoingLinks != null) {
            result.addAll(connectedIngoingLinks);
        }
        
        return result;
    }
    
    
    public boolean isConnectedIngoingLink(Link link) {
        validateLinks();
        return (connectedIngoingLinks != null) 
                && connectedIngoingLinks.contains(link);
    }
    
    
    public boolean isConnectedOutgoingLink(Link link) {
        validateLinks();
        return (connectedOutgoingLinks != null) 
                && connectedOutgoingLinks.contains(link);
    }
    
    
    public Rectangle getBounds() {
        validateBounds();
        return new Rectangle(bounds);
    };
    

    public int getHeight() {
        validateBounds();
        return bounds.height;
    }
    
    
    public int getX() {
        validateBounds();
        return bounds.x;
    }
    
    
    public int getWidth() {
        validateBounds();
        return bounds.width;
    };
    
    
    public void addVertex(Vertex vertex) { 
        if (vertex == null) throw new IllegalArgumentException();
        
        Graph oldGraph = vertex.getGraph();
        if (oldGraph != null && oldGraph != this) {
            oldGraph.removeVertex(vertex);
        }
        
        if (verteces == null) {
            verteces = new ArrayList<Vertex>();
            verteces.add(vertex);
        } else {
            verteces.remove(vertex);
            verteces.add(vertex);
        }
        
        vertex.setGraph(this); 
        invalidateBounds();
        fireGraphContentChanged();
    }
    
    
    public void moveOnTop(Vertex vertex) {
        if (vertex == null) return;
        if (verteces == null) return;

        int index = verteces.indexOf(vertex);
        
        if (index < 0 || index + 1 == verteces.size()) return;
        
        verteces.add(verteces.remove(index));
        
        fireGraphContentChanged();
    }
    

    public void moveOnTop(Link link) {
        if (link == null) return;
        if (links == null) return;

        int index = links.indexOf(link);
        
        if (index < 0 || index + 1 == links.size()) return;
        
        links.add(links.remove(index));
        
        fireGraphContentChanged();
    }
    
    
    public void removeVertex(Vertex vertex) { 
        if (vertex == null) throw new IllegalArgumentException();
        if (vertex.getGraph() != this) throw new IllegalArgumentException();

        verteces.remove(vertex);
        
        if (verteces.isEmpty()) {
            verteces = null;
        }
        
        vertex.setGraph(null);
        invalidateBounds();
        fireGraphContentChanged();
    }
    
    
    public int getVertexCount() {
        return (verteces == null) ? 0 : verteces.size();
    }
    
    
    public Vertex getVertex(int i) {
        return verteces.get(i);
    }
    
    
    public List<Vertex> getVerteces() { 
        return getVerteces(null); 
    }
    
    
    public List<Vertex> getVerteces(List<Vertex> result) { 
        if (result == null) {
            result = new ArrayList<Vertex>(getVertexCount());
        }
        
        if (verteces != null) {
            result.addAll(verteces);
        }
        
        return result;    
    }
    

    public void addLink(Link link) {
        if (link == null) throw new IllegalArgumentException();
        
        Graph oldGraph = link.getGraph();
        if (oldGraph != null && oldGraph != this) {
            oldGraph.removeLink(link);
        }
        
        if (links == null) {
            links = new ArrayList<Link>();
            links.add(link);
        } else {
            links.remove(link);
            links.add(link);
        }
        
        link.setGraph(this);
        invalidateLinks();
        fireGraphContentChanged();
    }
    
    
    public void removeLink(Link link) { 
        if (link == null) throw new IllegalArgumentException();
        if (link.getGraph() != this) throw new IllegalArgumentException();

        links.remove(link);
        
        if (links.isEmpty()) {
            links = null;
        }
        
        link.setGraph(null);
        invalidateLinks();
        fireGraphContentChanged();
    }
    
    
    public int getLinkCount() {
        return (links == null) ? 0 : links.size();
    }
    
    
    public Link getLink(int i) {
        return links.get(i);
    }
    
    
    public List<Link> getLinks() { 
        return getLinks(null); 
    }
    
    
    public List<Link> getLinks(List<Link> result) { 
        if (result == null) {
            result = new ArrayList<Link>(getLinkCount());
        }
        
        if (links != null) {
            result.addAll(links);
        }
        
        return result;
    }
    
    
    public void paintLinks(Graphics2D g2, TreePath treePath, 
            CanvasRendererContext rendererContext, int graphY) 
    {
        int count = getLinkCount();
        for (int i = 0; i < count; i++) {
            getLink(i).paint(g2, treePath, rendererContext, graphY);
        }
    }
   
    
    
    public void paintVerteces(Graphics2D g2, TreePath treePath,
            CanvasRendererContext rendererContext, int graphY) 
    {
        int count = getVertexCount();
        for (int i = 0; i < count; i++) {
            getVertex(i).paint(g2, treePath, rendererContext, graphY);
        }
    }

    
    public Point getTargetPinPoint(int graphX, int graphY, int step) {
        return new Point(Integer.MAX_VALUE, graphY + getHeight() * step / 2);
    }
    
    
    private void validateBounds() {
        if (validBounds) return;
        
        int x1 = Integer.MAX_VALUE;
        int x2 = Integer.MIN_VALUE;

        int y1 = Integer.MAX_VALUE;
        int y2 = Integer.MIN_VALUE;

        for (Vertex v : getVerteces()) {
            int x = v.getX();
            int y = v.getY();
            int w = v.getWidth();
            int h = v.getHeight();

            x1 = Math.min(x1, x);
            y1 = Math.min(y1, y);
            x2 = Math.max(x2, x + w);
            y2 = Math.max(y2, y + h);
        }

        bounds = (x1 <= x2 && y1 <= y2) 
                ? new Rectangle(x1, 0, x2 - x1, Math.max(y2, preferredHeight))
                : new Rectangle(2, Math.max(2, preferredHeight));
        
        validBounds = true;
    }
    
    
    void invalidateBounds() {
        if (validBounds) {
            validBounds = false;
            bounds = null;
            fireGraphBoundsChanged();
        }
    }
    
    
    public Vertex getNextVertex(Vertex currentVertex) {
        if (verteces == null || verteces.isEmpty()) return null;
        int count = verteces.size();
        
        Vertex minVertex = null;
        
        if (currentVertex == null) {
            minVertex = verteces.get(0);
            for (Vertex v : verteces) {
                if (VERTEX_COMPARATOR.compare(v, minVertex) < 0 ) {
                    minVertex = v;
                }
            }
            return minVertex;
        }
        
        for (int i = 0; i < count; i++) {
            Vertex vertex = verteces.get(i);
            if (vertex == currentVertex) continue;
            
            int cmp = VERTEX_COMPARATOR.compare(currentVertex, vertex);
            if (cmp < 0) {
                if (minVertex == null) {
                    minVertex = vertex;
                } else {
                    cmp = VERTEX_COMPARATOR.compare(vertex, minVertex);
                    if (cmp < 0) minVertex = vertex;
                }
            }
        }
        
        return (minVertex != null) ? minVertex : currentVertex;
    }
    
    
    public Vertex getPrevVertex(Vertex currentVertex) {
        if (verteces == null || verteces.isEmpty()) return null;
        int count = verteces.size();
        
        Vertex maxVertex = null;
        
        if (currentVertex == null) {
            maxVertex = verteces.get(0);
            for (Vertex v : verteces) {
                if (VERTEX_COMPARATOR.compare(v, maxVertex) > 0 ) {
                    maxVertex = v;
                }
            }
            return maxVertex;
        }
        for (int i = 0; i < count; i++) {
            Vertex vertex = verteces.get(i);
            if (vertex == currentVertex) continue;
            
            int cmp = VERTEX_COMPARATOR.compare(vertex, currentVertex);
            if (cmp < 0) {
                if (maxVertex == null) {
                    maxVertex = vertex;
                } else {
                    cmp = VERTEX_COMPARATOR.compare(maxVertex, vertex);
                    if (cmp < 0) maxVertex = vertex;
                }
            }
        }
        
        return (maxVertex != null) ? maxVertex : currentVertex;
    }
    
    private void validateOutgoingLinks() {
        if (validOutgoingLinks) {return; }
        
        for (int i = getLinkCount() - 1; i >= 0; i--) {
            Link link = getLink(i);
            if (link.getTarget() == this) {
                outgoingLink = link;
                validOutgoingLinks = true;
                hasOutgoingLinks = true;
                return;
            }
        }
        
        hasOutgoingLinks = false;
        validOutgoingLinks = true;
        outgoingLink = null;
    }
    
    private void validateLinks() {
        if (validLinks) return;
        
        List<Link> ingoingLinks = new ArrayList<Link>();
        Set<Link> outgoingLinks = new HashSet<Link>();

        Set<Link> connectedIngoingLinks = new HashSet<Link>();
        Set<Link> connectedOutgoingLinks = new HashSet<Link>();
        
        for (int i = getLinkCount() - 1; i >= 0; i--) {
            Link link = getLink(i);

            SourcePin source = link.getSource();
            TargetPin target = link.getTarget();

            if (source instanceof TreeSourcePin) {
                ingoingLinks.add(link);
            }

            if (target instanceof Graph) {
                hasOutgoingLinks = true;
                outgoingLinks.add(link);
            }
        }
        
        
        if (hasOutgoingLinks) {
            Set<Link> result = new HashSet<Link>();
            Set<Vertex> visitedVerteces = new HashSet<Vertex>();
        
            for (Link link : outgoingLinks) {
                findConnectedIngoingLinks(link, result, visitedVerteces);
                
                if (!result.isEmpty()) {
                    connectedOutgoingLinks.add(link);
                    connectedIngoingLinks.addAll(result);
                    result.clear();
                }
                
                visitedVerteces.clear();
            }
        }
        
        this.ingoingLinks = (ingoingLinks.isEmpty()) ? null : ingoingLinks;
        
        this.connectedIngoingLinks = (connectedIngoingLinks.isEmpty()) ? null 
                : connectedIngoingLinks;
        
        this.connectedOutgoingLinks = (connectedOutgoingLinks.isEmpty()) ? null 
                : connectedOutgoingLinks;
        
        this.validLinks = true;
        validOutgoingLinks = true;
        if (!outgoingLinks.isEmpty()) {
            outgoingLink = outgoingLinks.iterator().next();
        }
    }
    
    
    void invalidateLinks() {
        if (validLinks) {
            hasOutgoingLinks = false;
            ingoingLinks = null;
            connectedIngoingLinks = null;
            connectedOutgoingLinks = null;
            outgoingLink = null;
            validLinks = false;
            validOutgoingLinks = false;
            fireGraphLinksChanges();
        }
    }
    
    
    private void findConnectedIngoingLinks(Link link, Set<Link> result,
            Set<Vertex> visitedVerteces) 
    {
        SourcePin source = link.getSource();
        
        if (source instanceof TreeSourcePin) {
            result.add(link);
        } else if (source instanceof Vertex) {
            Vertex vertex = ((Vertex) source);
            
            if (!visitedVerteces.contains(vertex)) {
                visitedVerteces.add(vertex);
                for (int i = vertex.getItemCount() - 1; i >= 0; i--) {
                    Link e = vertex.getItem(i).getIngoingLink();
                    if (e != null) {
                        findConnectedIngoingLinks(e, result, visitedVerteces);
                    }
                }
            }
        }
    }
    
    
    private static final Comparator<Vertex> VERTEX_COMPARATOR 
            = new Comparator<Vertex>()
    {
        public int compare(Vertex vertex1, Vertex vertex2) {
            int cmp = vertex1.getX() - vertex2.getX();
            if (cmp != 0) return cmp;
            
            cmp = vertex1.getY() - vertex2.getY();
            if (cmp != 0) return cmp;
            
            long cmp2 = vertex1.uid - vertex2.uid;
            if (cmp2 > 0) return 1;
            if (cmp2 < 0) return -1;
            return 0;
        }
    };
    
    private static final Comparator<Link> LINK_COMPARATOR 
            = new Comparator<Link>()
    {
        public int compare(Link link1, Link link2) {
            if (link1 == link2) return 0;
            
            if (link1.getTarget() instanceof Graph) return -1;
            if (link2.getTarget() instanceof Graph) return 1;
            
            Vertex vertex1 = ((VertexItem) link1.getTarget()).getVertex();
            Vertex vertex2 = ((VertexItem) link2.getTarget()).getVertex();
            
            if (vertex1 == vertex2) {
                int index1 = vertex1.getItemIndex((VertexItem)link1.getTarget());
                int index2 = vertex2.getItemIndex((VertexItem)link2.getTarget());
                return index1 - index2;
            }
            int cmp = vertex1.getY() - vertex2.getY();
            if (cmp != 0) return cmp;
                        
            cmp = vertex1.getX() - vertex2.getX();
            if (cmp != 0) return cmp;
  
            long cmp2 = vertex1.uid - vertex2.uid;
            if (cmp2 > 0) return 1;
            if (cmp2 < 0) return -1;
            return 0;
        }
    };
}
