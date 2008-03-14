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

package org.netbeans.modules.soa.mappercore.utils;

import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;

/**
 *
 * @author anjeleevich
 */
public class GraphLayout {

    public static int getNextFreeX(Graph graph) {
        if (graph == null) {
            return 0;
        }
        int maxX = 0;
//System.out.println();

        for (int i=0; i < graph.getVertexCount(); i++) {
            Vertex vertex = graph.getVertex(i);
            int x = vertex.getX() + vertex.getWidth();

            if (x > maxX) {
                maxX = x;
            }
//System.out.println("see: " + vertex.getX() + " "  + vertex.getWidth());
        }
        if (maxX == 0) {
            maxX += INDENT;
        }
        else {
            maxX += GAP;
        }
        return maxX;
    }
    
    public static void layout(Graph graph) {
        layout(graph, -8, 8, 1);
    }
    
    public static void layout(Graph graph, int x, int xMargin, int yMargin) {
        Set<Vertex> primaryRoots = new HashSet<Vertex>();
        Set<Vertex> roots = new HashSet<Vertex>();
        
        for (int i = graph.getVertexCount() - 1; i >= 0; i--) {
            Vertex vertex = graph.getVertex(i);
            Link link = vertex.getOutgoingLink();
            
            if (link == null) {
                roots.add(vertex);
            } else if (link.getTarget() == graph) {
                primaryRoots.add(vertex);
            }
        }
        
        Set<Vertex> layouted = new HashSet<Vertex>();
        
        int y = 0;
        
        int width = -1;
        
        for (Vertex root : primaryRoots) {
            Dimension size = layout(root, x, y, layouted, xMargin, yMargin);
            if (size != null) {
                y += size.height + yMargin;
                width = Math.max(width, size.width);
            }
        }
        
        if (width >= 0) {
            x -= width + xMargin;
        }

        y = 0;
        for (Vertex root : roots) {
            Dimension size = layout(root, x, y, layouted, xMargin, yMargin);
            if (size != null) {
                y += size.height + yMargin;
            }
        }
    }
    
    
    public static Dimension layout(Vertex vertex, int topRightX, int topRightY, 
            Set<Vertex> layouted, int xMargin, int yMargin) 
    {
        if (layouted.contains(vertex)) return null;
        
        layouted.add(vertex);
        
        int vertexWidth = vertex.getWidth();
        int vertexHeight = vertex.getHeight();
        
        int x = topRightX - vertexWidth - xMargin;
        int y = topRightY;
        
        int vertexItemCount = vertex.getItemCount();
        
        int width = -1;
        
        for (int i = 0; i < vertexItemCount; i++) {
            VertexItem item = vertex.getItem(i);
            Link link = item.getIngoingLink();
            
            if (link == null) continue;
            
            SourcePin sourcePin = link.getSource();
            
            if (sourcePin instanceof Vertex) {
                Vertex prevVertex = (Vertex) sourcePin;
                
                Dimension prevSize = layout(prevVertex, x, y, layouted, 
                        xMargin, yMargin);
                
                if (prevSize != null) {
                    y += prevSize.height + yMargin;
                    width = Math.max(width, prevSize.width);
                }
            }
        }
        
        if (width < 0) {
            width = vertexWidth;
        } else {
            width += xMargin + vertexWidth;
        }
        
        int height = (y != topRightY) ? y - yMargin - topRightY : 0;
        int vertexX = topRightX - vertexWidth;
        int vertexY = topRightY + Math.max(0, (height - vertexHeight) / 2);
        
        height = Math.max(height, vertexY + vertexHeight - topRightY);
        
        vertex.setLocation(vertexX, vertexY);
        
        return new Dimension(width, height);
    }

    private static final int GAP = 3;
    private static final int INDENT = 2;
}
