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

import javax.swing.JLabel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 *
 * @author anjeleevich
 */
public class LinkToolCanvasRendererContext implements CanvasRendererContext {
    
    private LinkTool linkTool;
    private CanvasRendererContext defaultContext;
    
    
    public LinkToolCanvasRendererContext(LinkTool linkTool) {
        this.defaultContext = linkTool.getCanvas().getDefaultRendererContext();
        this.linkTool = linkTool;
    }
    
    
    private CanvasRendererContext getDefaultRendererContext() {
        return defaultContext;
    }
    

    public JLabel getTextRenderer() {
        return defaultContext.getTextRenderer();
    }
    
    
    public boolean isSelected(TreePath treePath) {
        return defaultContext.isSelected(treePath);
    }
    

    public boolean isSelected(TreePath treePath, GraphItem graphItem) {
        return defaultContext.isSelected(treePath, graphItem);
    }

    
    public boolean paintVertex(TreePath treePath, Vertex vertex) {
        return true;
    }

    
    public boolean paintLink(TreePath treePath, Link link) {
        if (link != linkTool.getOldLink()) return true;
        return !Utils.equal(treePath, linkTool.getOldTreePath());
    }

    
    public boolean paintVertexItemPin(TreePath treePath, VertexItem vertexItem) {
        return linkTool.getActivePins().contains(treePath, vertexItem) 
                && linkTool.getTargetPin() != vertexItem;
    }

    
    public boolean paintVertexPin(TreePath treePath, Vertex vertex) {
        return linkTool.getActivePins().contains(treePath, vertex)
                && linkTool.getSourcePin() != vertex;
    }

    
    public int getStep() {
        return defaultContext.getStep();
    }

    public Mapper getMapper() {
        return defaultContext.getMapper();
    }

    public Canvas getCanvas() {
        return defaultContext.getCanvas();
    }

    public LeftTree getLeftTree() {
        return defaultContext.getLeftTree();
    }

    public RightTree getRightTree() {
        return defaultContext.getRightTree();
    }

    public int getCanvasVisibleMinX() {
        return defaultContext.getCanvasVisibleMinX();
    }

    public int getCanvasVisibleMaxX() {
        return defaultContext.getCanvasVisibleMaxX();
    }

    public int getGraphX() {
        return defaultContext.getGraphX();
    }

    
    public int getCanvasVisibleCenterX() {
        return defaultContext.getCanvasVisibleCenterX();
    }
}
