/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
 * @author AlexanderPermyacov
 */
public class DefaultCanvasRendererPrintContext implements CanvasRendererContext {
private Mapper mapper;
    
    private Canvas canvas;
    private LeftTree leftTree;
    private RightTree rightTree;
    
    private int step;
    
    private int canvasVisibleMinX;
    private int canvasVisibleMaxX;
    private int graphX;
    
    public DefaultCanvasRendererPrintContext(Mapper mapper) {
        this.mapper = mapper;
        this.canvas = mapper.getCanvas();
        this.leftTree = mapper.getLeftTree();
        this.rightTree = mapper.getRightTree();
    
        this.step = canvas.getStep();
        this.graphX = canvas.toCanvas(0);
        
               
        canvasVisibleMinX = 0;
        canvasVisibleMaxX = canvas.getWidth();
    }
    
    
    public int getStep() {
        return step;
    }
    

    public JLabel getTextRenderer() {
        return canvas.getTextRenderer();
    }


    public boolean isSelected(TreePath treePath) {
        return false;
    }
    

    public boolean isSelected(TreePath treePath, GraphItem graphItem) {
        return false;
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