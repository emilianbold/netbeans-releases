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

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 *
 * @author anjeleevich
 */
public class Utils {
    public static Color gray(Color color, int grayPercents) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        
        int y = Math.round(r * 0.299f + g * 0.587f + b * 0.114f);
        
        float k = (float) grayPercents / 100;
        
        r = Math.round(r + k * (y - r));
        g = Math.round(g + k * (y - g));
        b = Math.round(b + k * (y - b));
        
        if (r > 255) r = 255; else if (r < 0) r = 0;
        if (g > 255) g = 255; else if (g < 0) g = 0;
        if (b > 255) b = 255; else if (b < 0) b = 0;
        
        return new Color(r, g, b, color.getAlpha());
    }
    
    
    public static Set<Graph> findGraphs(MapperModel model, TreePath treePath, 
            Set<Graph> result) 
    {
        if (result == null) {
            result = new HashSet<Graph>();
        }
        
        Object parent = treePath.getLastPathComponent();
        
        if (model.searchGraphsInside(treePath) && !model.isLeaf(parent)) {
            int childCount = model.getChildCount(parent);

            for (int i = 0; i < childCount; i++) {
                Object child = model.getChild(parent, i);
                MapperTreePath childPath = new MapperTreePath(treePath, child);
                Graph childGraph = model.getGraph(childPath);
                if (childGraph != null) result.add(childGraph);
                result = findGraphs(model, childPath, result);
            }
        }
        
        return result;
    }
    
    
    
    public static boolean equal(Object o1, Object o2) {
        if (o1 == o2) return true;
        return (o1 == null || o2 == null) ? false : o1.equals(o2);
    }
    
    
    public static Point toScrollPane(Component component, 
            Point componentPoint, 
            Point resultPoint)
    {
        int px = componentPoint.x;
        int py = componentPoint.y;
        
        while (!(component instanceof JScrollPane)) {
            px += component.getX();
            py += component.getY();
            component = component.getParent();
        }
        
        if (resultPoint == null) {
            resultPoint = new Point(px, py);
        } else {
            resultPoint.setLocation(px, py);
        }
        
        return resultPoint;
    }
    
    
    public static Point fromScrollPane(Component component,
            Point scrollPanePoint,
            Point resultPoint)
    {
        int px = scrollPanePoint.x;
        int py = scrollPanePoint.y;
        
        while (!(component instanceof JScrollPane)) {
            px -= component.getX();
            py -= component.getY();
            component = component.getParent();
        }
        
        if (resultPoint == null) {
            resultPoint = new Point(px, py);
        } else {
            resultPoint.setLocation(px, py);
        }
        
        return resultPoint;
    }
    
    
    
    public static List<TreePath> getNonEmptyGraphs(
            MapperModel model)
    {
        List<TreePath> result = new ArrayList<TreePath>();
        if (model != null) {
            Object root = model.getRoot();
            int childCount = (root != null && !model.isLeaf(root)) 
                    ? model.getChildCount(root)
                    : 0;
            if (childCount > 0) {
                TreePath rootPath = new TreePath(root);
                for (int i = 0; i < childCount; i++) {
                    Object child = model.getChild(root, i);
                    collectNonEmptyGraphs(model, rootPath
                            .pathByAddingChild(child), result);
                }
            }
        }
        return result;
    }
    
    
    private static void collectNonEmptyGraphs(MapperModel model, 
            TreePath currentTreePath, List<TreePath> result) 
    {
        Object parent = currentTreePath.getLastPathComponent();
        
        Graph graph = model.getGraph(currentTreePath);
        if (graph != null && !graph.isEmpty()) {
            result.add(currentTreePath);
        }
        
        int childCount = (!model.isLeaf(parent) && model
                .searchGraphsInside(currentTreePath)) 
                ? model.getChildCount(parent) : 0;
        
        for (int i = 0; i < childCount; i++) {
            Object child = model.getChild(parent, i);
            collectNonEmptyGraphs(model, currentTreePath
                    .pathByAddingChild(child), result);
        }
    }
    
    
    public static double distance(Shape shape, int px, int py) {
        double[] coords = new double[6];
        
        double moveX = 0.0;
        double moveY = 0.0;
        
        double x1 = 0.0;
        double y1 = 0.0;
        
        double distance = Double.POSITIVE_INFINITY;
        
        for (PathIterator i = shape.getPathIterator(null, 1.0); !i.isDone(); 
                i.next()) 
        {
            int type = i.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO) {
                moveX = x1 = coords[0];
                moveY = y1 = coords[1];
            } else if (type == PathIterator.SEG_LINETO) {
                double x2 = coords[0];
                double y2 = coords[1];
                
                distance = Math.min(distance, Line2D.ptSegDist(x1, y1, x2, y2, 
                        px, py));
                
                x1 = x2;
                y1 = y2;
            } else if (type == PathIterator.SEG_CLOSE) {
                double x2 = moveX;
                double y2 = moveY;
                
                distance = Math.min(distance, Line2D.ptSegDist(x1, y1, x2, y2, 
                        px, py));
                
                x1 = x2;
                y1 = y2;
            }
        }
        
        return distance;
    }
    
    public static boolean isTreePathExpandable(TreeModel treeModel, 
            TreePath treePath) 
    {
        if (treeModel == null) return false;
        if (treePath == null) return false;

        return !treeModel.isLeaf(treePath.getLastPathComponent())
                && isTreePathInModel(treeModel, treePath);
    }
    
    public static boolean isTreePathInModel(TreeModel treeModel, 
            TreePath treePath) 
    {
        if (treeModel == null) return false;
        if (treePath == null) return false;
        
        TreePath parentPath = treePath.getParentPath();
        
        if (parentPath == null) {
            return treeModel.getRoot() == treePath.getLastPathComponent();
        }
        
        
        if (!isTreePathInModel(treeModel, parentPath)) return false;

        Object node = treePath.getLastPathComponent();
        Object parent = parentPath.getLastPathComponent();
        
        return treeModel.getIndexOfChild(parent, node) >= 0;
    }
}



