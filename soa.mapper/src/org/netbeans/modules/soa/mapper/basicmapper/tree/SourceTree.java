/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.soa.mapper.basicmapper.tree;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;

/**
 * <p>
 *
 * Title: </p> SourceTree<p>
 *
 * Description: </p> Provide implementation to handle mapper source tree visual
 * functionalilites. <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public class SourceTree
     extends AbstractMapperTree {

    /**
     * Returns a SoureTree with a sample model.
     */
    public SourceTree() {
        this (null);
    }

    /**
     * Returns an instance of SourceTree which displays the root node -- the tree is
     * created using the specified data model.
     *
     * @param newModel  Description of the Parameter
     */
    public SourceTree(TreeModel newModel) {
        super(newModel);
        initialize();
    }

    /**
     * Return a new mapper tree node of this view that contains the specified tree
     * path.
     *
     * @param treePath  the tree path to be store in the mapper tree node.
     * @return          a new mapper tree node of this view that contains the
     *      specified tree path.
     * @deprecated      createMapperTreeNode should no longer be called due the
     *      cache issue of the tree. use getMapperTreeNode(TreePath) to get and
     *      create a IMapperTreeNode. Or use IBasicMapper.createMapperTreeNode.
     */
    public IMapperTreeNode createMapperTreeNode(TreePath treePath) {
        return newMapperTreeNode(treePath);
    }

    /**
     * Return a new mapper tree node of this view that contains the specified tree
     * path.
     *
     * @param treePath  the tree path to be store in the mapper tree node.
     * @return          a new mapper tree node of this view that contains the
     *      specified tree path.
     */
    protected IMapperTreeNode newMapperTreeNode(TreePath treePath) {
        return new BasicMapperTreeNode(this, treePath, true, false);
    }

    /**
     * Return true if this tree view contains the specified tree node, false
     * otherwise.
     *
     * @param treeNode  Description of the Parameter
     * @return          true if this tree view contains the specified tree node,
     *      false otherwise.
     */
    public boolean contains(IMapperTreeNode treeNode) {
        return treeNode.isSourceTreeNode();
    }

    /**
     * Return the mid point of the west border of the specified rectangle to
     * be the start drawing point of a tree node link.
     *
     * @param pathRect  the rectangle bounding of the tree path
     * @return          the point where the link start to be drawn of the
     *      specified rectangle.
     */
    public Point getTreeNodePoint(Rectangle pathRect) {
        // draw the link outside visible treepath bound rectangle
        int y = pathRect.y + (int) (pathRect.height / 2);
        int x = pathRect.x + pathRect.width;

        return new Point(x, y);
    }

    /**
     * This method draw all the links of the specified tree node. Also, it
     * calcuate the tree node x, y as the tree node has moved (due to scrolling, etc.).
     *
     * @param g     the tree graphic where the link will be display on
     * @param treeNode  the specified tree node to be drawn.
     */
    protected void drawNode(Graphics g, IMapperTreeNode treeNode) {
        if (treeNode.getLinkCount() <= 0) {
            return;
        }

        List links = treeNode.getLinks();
        boolean isFound = false;
        for ( int i = 0; i < links.size(); i++ ) {
            if ( ((IMapperLink) links.get(i)).getStartNode().equals(treeNode)) {
                isFound = true;
                break;
            }
        }
        if ( !isFound ) {
            return;
        }

        TreePath path = treeNode.getPath();

        Rectangle pathRect = getShowingPathRectBound(path);

        if (pathRect == null) {
            return;
        }

        Point nodePoint = getTreeNodePoint(pathRect);

        if (nodePoint == null) {
            return;
        }

        // the linked line width depends on its parent size
        int lineWidth = getTree().getSize().width - nodePoint.x;

        boolean isFolded = !pathRect.equals(getTree().getPathBounds(path));
        drawLine(treeNode, isFolded, g, nodePoint.x, nodePoint.y, nodePoint.x + lineWidth, nodePoint.y);

        int yPos = getScrollerPane().getViewport().getViewPosition().y;
        treeNode.setX(nodePoint.x);
        treeNode.setY(nodePoint.y - yPos);
    }

    /**
     * Initialize the tree.
     */
    private void initialize() {
        this.getScrollerPane().setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT);
        this.getScrollerPane().setPreferredSize(new Dimension(250,100));
    }

    protected void highlightSingleLink(IMapperLink link) {
        IMapperNode mapperNode = link.getEndNode();
            JGoObject obj = (JGoObject) getViewManager().getCanvasView().getCanvas().getCanvasNodeByDataObject(mapperNode);
            if(obj != null) {
                ((JGoView) getViewManager().getCanvasView().getCanvas()).scrollRectToVisible(obj.getBoundingRect());
            }else if(mapperNode instanceof IMapperTreeNode) {
                //if it is tree node then scroll in tree
                IMapperTreeNode treeNode = (IMapperTreeNode) mapperNode;
                highlightSingleTreeNode(treeNode);
                scrollToSingleTreeNode(treeNode);
            }
    }
    
    protected void unHighlightSingleLink(IMapperLink link) {
        IMapperNode mapperNode = link.getEndNode();
            if(mapperNode instanceof IMapperTreeNode) {
                //if it is tree node then scroll in tree
                IMapperTreeNode treeNode = (IMapperTreeNode) mapperNode;
                unHighlightSingleTreeNode(treeNode);
            }
    }
    
    private void highlightSingleTreeNode(IMapperTreeNode treeNode) {
        treeNode.setHighlightLink(true);
        getViewManager().getDestView().getTree().repaint();
    }
    
    private void unHighlightSingleTreeNode(IMapperTreeNode treeNode) {
        treeNode.setHighlightLink(false);
        getViewManager().getDestView().getTree().repaint();
    }
    
    private void scrollToSingleTreeNode(IMapperTreeNode treeNode) {
        getViewManager().getDestView().getTree().scrollPathToVisible(treeNode.getPath());
    }
}
