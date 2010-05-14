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

import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;

/**
 *
 * @author anjeleevich
 */
public class DefaultCanvasRendererContext implements CanvasRendererContext {
    
    private Mapper mapper;
    
    private Canvas canvas;
    private LeftTree leftTree;
    private RightTree rightTree;
    
    private int step;
    
    private int canvasVisibleMinX;
    private int canvasVisibleMaxX;
    private int graphX;
    
    public DefaultCanvasRendererContext(Mapper mapper) {
        this.mapper = mapper;
        this.canvas = mapper.getCanvas();
        this.leftTree = mapper.getLeftTree();
        this.rightTree = mapper.getRightTree();
    
        this.step = canvas.getStep();
        this.graphX = canvas.toCanvas(0);
        
        Rectangle viewRect = canvas.getScrollPane().getViewport().getViewRect();
        
        canvasVisibleMinX = viewRect.x;
        canvasVisibleMaxX = viewRect.x + viewRect.width;
    }
    
    
    public int getStep() {
        return step;
    }
    

    public JLabel getTextRenderer() {
        return canvas.getTextRenderer();
    }


    public boolean isSelected(TreePath treePath) {
        return mapper.getSelectionModel().isSelected(treePath);
    }
    

    public boolean isSelected(TreePath treePath, GraphItem graphItem) {
        return mapper.getSelectionModel().isSelected(treePath, graphItem);
    }
    

    public boolean paintVertex(TreePath treePath, Vertex vertex) {
        return true;
    }
    

    public boolean paintLink(TreePath treePath, Link link) {
        return true;
    }
    

    public boolean paintVertexItemPin(TreePath treePath, VertexItem vertexItem) {
        return vertexItem.getIngoingLink() == null;
    }
    

    public boolean paintVertexPin(TreePath treePath, Vertex vertex) {
        return vertex.getOutgoingLink() == null;
    }

    
    public Mapper getMapper() {
        return mapper;
    }

    
    public Canvas getCanvas() {
        return canvas;
    }

    
    public LeftTree getLeftTree() {
        return leftTree;
    }

    
    public RightTree getRightTree() {
        return rightTree;
    }

    
    public int getCanvasVisibleMinX() {
        return canvasVisibleMinX;
    }

    
    public int getCanvasVisibleMaxX() {
        return canvasVisibleMaxX;
    }

    
    public int getGraphX() {
        return graphX;
    }

    public int getCanvasVisibleCenterX() {
        return (canvasVisibleMinX + canvasVisibleMaxX) / 2;
    }
}
