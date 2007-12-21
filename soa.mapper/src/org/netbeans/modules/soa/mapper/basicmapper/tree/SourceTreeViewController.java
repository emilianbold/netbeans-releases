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

package org.netbeans.modules.soa.mapper.basicmapper.tree;

import java.awt.Point;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.util.List;

import javax.swing.tree.TreePath;

import org.netbeans.modules.soa.mapper.basicmapper.MapperLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeView;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;

/**
 * <p>
 *
 * Title: </p>SourceTreeViewController<p>
 *
 * Description: </p> Provides an implemenation mapper source tree behavior.<p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author Un Seng Leong
 * @version 1.0
 */
public class SourceTreeViewController
         extends AbstractTreeViewController {

    /**
     * Creates a new SourceTreeViewController object.
     */
    public SourceTreeViewController() {
    }

    /**
     * Return a mapper link connected from a mapper tree node contains the
     * drag location of the tree path. If the tree path cannot be
     * found (drag outside the tree), this method returns null.
     *
     * @param event  the DragGestureEvent
     * @return       a mapper link connected from a mapper tree node that contains
     *      the drag location of the tree path.
     */
    protected Object getDragObject(DragGestureEvent event) {
        Point p = event.getDragOrigin();
        TreePath path = ((IMapperTreeView) getView()).getTree().getPathForLocation(p.x, p.y);

        if (path == null) {
            return null;
        }
        if (getDnDCustomizer() != null) {
            if (!getDnDCustomizer().isMappable(path)) {
                return null;
            }
        }
        
        IMapperTreeNode treeAddress =
                ((IMapperTreeView) getView()).getMapperTreeNode(path);
        treeAddress.setHighlightLink(true);

        MapperLink link = new MapperLink();
        link.setStartNode(treeAddress);

        return link;
    }


    /**
     * Return true if specified transfer object is an instance of mapper link
     * from dest tree, false otherwise. This method set the mapper link start
     * node to a mapper tree node that contains the drop location of the tree
     * path. If tree path is not found, return false.
     *
     * @param event         DropTargetDropEvent
     * @param transferData  the object from the event transferable.
     * @return              Return true if specified transfer object is an
     *      instanceof mapper link that is from dest tree
     */
    protected boolean handleJLocalObjectDrop(
            DropTargetDropEvent event,
            Object transferData) {
        if (transferData != null) {
            if (transferData instanceof IMapperLink) {
                Point dropPoint = event.getLocation();
                TreePath dropPath =
                        ((IMapperTreeView) getView()).getTree().getPathForLocation(
                        dropPoint.x,
                        dropPoint.y);

                if (dropPath == null) {
                    return false;
                }

                IMapperTreeNode treeNode =
                        ((IMapperTreeView) getView()).getMapperTreeNode(dropPath);

                ((IMapperLink) transferData).setStartNode(treeNode);
                requestNewLink((IMapperLink) transferData);

                return true;
            }
        }

        return false;
    }


    /**
     * Return true if the method handles JGoObject Drop to this tree
     * successfully, false otherwise. This method constructs an mapper link
     * connents to an mapper tree node that contains a tree path is located by
     * the drop location. Then it calls and return from
     * ICanvasView.createLinkFromDnD to delgates the actural link creation to
     * the canvas.
     *
     * @param event  the DropTargetDropEvent event that supports JGo DataFlavor
     *      in its transferable.
     * @return       true if ICanvasView.createLinkFromDnD handles new link to
     *      this tree successfully, false otherwise.
     */
    protected boolean handleJGoObjectDrop(DropTargetDropEvent event) {
        Point dropPoint = event.getLocation();
        TreePath dropPath =
                ((IMapperTreeView) getView()).getTree().getPathForLocation(
                dropPoint.x,
                dropPoint.y);

        if (dropPath == null) {
            return false;
        }

        IMapperTreeNode treeAddress =
                ((IMapperTreeView) getView()).getMapperTreeNode(dropPath);

        MapperLink newLink = new MapperLink(treeAddress, null);
        return ((ICanvasView) ((IMapperCanvasView) this.getMapperController().getViewManager().getCanvasView()).
            getCanvas()).connectLinkByDrag(newLink);
    }
    
    /**
     * Sets the specified node as the end or start node on the given link,
     * according to the direction that points to this tree.
     */
    protected void setLinkOnNode(IMapperLink link, IMapperTreeNode node) {
        link.setStartNode(node);
    }
    
    /**
     * Sets the specified node as the end or start node on the given link,
     * according to the direction that points to the opposite tree.
     */
    protected void setOppositeLinkOnNode(IMapperLink link, IMapperTreeNode node) {
        link.setEndNode(node);
    }
    
    /**
     * Returns the tree node linked to the opposite tree.
     */
    protected IMapperTreeNode getOriginatingTreeNodeFromLink(IMapperLink link) {
        IMapperNode node = link.getEndNode();
        if (node instanceof IMapperTreeNode) {
            IMapperTreeNode treeNode = (IMapperTreeNode) node;
            return treeNode.isDestTreeNode() ? treeNode : null;
        }
        return null;
    }
    
    /**
     * Determines if any of the links on the node link to the node with the
     * direction of the link pointing to this tree.
     */
    protected boolean isNodeAlreadyLinked(IMapperNode node) {
        boolean isFound = false;
        List links = node.getLinks();
        for ( int i = 0; i < links.size(); i++ ) {
            if ( ((IMapperLink) links.get(i)).getStartNode().equals(node)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the x location of the edge of the tree.
     */
    protected int getDragPortOriginX() {
        return 0;
    }
}
