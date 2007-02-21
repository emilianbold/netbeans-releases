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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo;

import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoView;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.AbstractCanvasLink.NodeYChangeListener;

import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasTreeToTreeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;

/**
 * <p>
 *
 * Title: </p> <p>
 *
 * Description: </p> BasicCanvasTreeToTreeLink provides basic implementation of
 * ICanvasTreeToTreeLink <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    unascribed
 * @created   January 2, 2003
 * @version   1.0
 */
public class BasicCanvasTreeToTreeLink
     extends AbstractCanvasLink
     implements ICanvasTreeToTreeLink {

    /**
     * the start node y change listener
     */
    private NodeYChangeListener endYListener;

    /**
     * the end node y change listener
     */
    private NodeYChangeListener startYListener;

    
    /**
     * Creates a new BasicCanvasTreeToTreeLink object.
     *
     * @param link  the mapper link
     */
    public BasicCanvasTreeToTreeLink(IMapperLink link) {
        super(link);
        this.setRelinkable(false);
        this.setOrthogonal(true);

        startPort = new BasicCanvasPort();
        startPort.setDraggable(false);
        startPort.setSelectable(false);
        startPort.setStyle(JGoPort.StyleHidden);
        this.setFromPort(startPort);

        endPort = new BasicCanvasPort();
        endPort.setDraggable(false);
        endPort.setSelectable(false);
        endPort.setStyle(JGoPort.StyleHidden);
        this.setToPort(endPort);
    }

    /**
     * Return the tree node repersetns the end point of this link.
     *
     * @return   the tree node repersetns the end point of this link.
     */
    public IMapperTreeNode getDestTreeAddress() {
        return (IMapperTreeNode) getMapperLink()
            .getEndNode();
    }

    /**
     * Return the tree node repersetns the start point of this link.
     *
     * @return   the tree node repersetns the start point of this link.
     */
    public IMapperTreeNode getSourceTreeAddress() {
        return (IMapperTreeNode) getMapperLink()
            .getStartNode();
    }

    /**
     * Set the canvas contains this canvas node.
     *
     * @param canvas  the canvas contains this canvas node.
     */
    public void setMapperCanvas(ICanvasView canvas) {
        super.setMapperCanvas(canvas);

        if (canvas instanceof BasicCanvasView) {
            startPort.setLeft(
                ((BasicCanvasView) canvas).getTreeLinkFromPortX());
            endPort.setLeft(((BasicCanvasView) canvas).getTreeLinkToPortX());

            IMapperLink link = getMapperLink();
            startPort.setTop(
                link.getStartNode().getY() + NodeYChangeListener.Y_AXIS_DIFF
                + ((JGoView) canvas).getViewPosition().y);
            link.getStartNode()
                .addPropertyChangeListener(
                new NodeYChangeListener(
                (JGoView) canvas,
                startPort));
            endPort.setTop(
                link.getEndNode().getY() + NodeYChangeListener.Y_AXIS_DIFF
                + ((JGoView) canvas).getViewPosition().y);
            link.getEndNode()
                .addPropertyChangeListener(
                new NodeYChangeListener(
                (JGoView) canvas,
                endPort));
        }
    }

    protected int getMidOrthoPosition(int from, int to, boolean vertical) {
        int result;
        if (mPosition > 0) {
            int newPos = from + (mPosition*6);
            result = newPos;
        } else {
            result = super.getMidOrthoPosition(from, to, vertical);
        }
        return result;
    }
}
