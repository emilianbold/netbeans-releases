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

/**
 *
 * @author anjeleevich
 */
public interface CanvasRendererContext {
    public Mapper getMapper();
    public Canvas getCanvas();
    public LeftTree getLeftTree();
    public RightTree getRightTree();
    
    public int getCanvasVisibleMinX();
    public int getCanvasVisibleMaxX();
    public int getCanvasVisibleCenterX();
    
    public int getGraphX();
    
    public JLabel getTextRenderer();

    public int getStep();
    
    public boolean isSelected(TreePath treePath);
    
    public boolean isSelected(TreePath treePath, GraphItem vertex);
    
    public boolean paintVertex(TreePath treePath, Vertex vertex);
    public boolean paintLink(TreePath treePath, Link link);
    public boolean paintVertexItemPin(TreePath treePath, VertexItem vertexItem);
    public boolean paintVertexPin(TreePath treePath, Vertex vertex);
}
