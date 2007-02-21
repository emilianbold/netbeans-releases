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
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasNodeToTreeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;

/**
 * <p>
 *
 * Title: </p> BasicCanvasNodeToTreeLink <p>
 *
 * Description: </p> BasicCanvasNodeToTreeLink provides basic implementation of
 * ICanvasNodeToTreeLink <p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */
public class BasicCanvasNodeToTreeLink
     extends AbstractCanvasLink
     implements ICanvasNodeToTreeLink {

    /**
     * the start canvas node.
     */
    private ICanvasFieldNode mCanvasNode;

    
    /**
     * Creates a new BasicCanvasNodeToTreeLink object.
     *
     * @param link  the mapper link
     */
    public BasicCanvasNodeToTreeLink(IMapperLink link) {
        super(link);
        this.setArrowHeads(false, false);
        initializeArrowHeads(this);
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
        return (IMapperTreeNode) this.getMapperLink()
            .getEndNode();
    }

    /**
     * Return the canvas field node repersetns the start point of this link.
     *
     * @return   the canvas field node repersetns the start point of this link.
     */
    public ICanvasFieldNode getSourceFieldNode() {
        return mCanvasNode;
    }

    /**
     * Set the canvas contains this canvas node.
     *
     * @param canvas  the canvas contains this canvas node.
     */
    public void setMapperCanvas(ICanvasView canvas) {
        super.setMapperCanvas(canvas);
        mCanvasNode =
            canvas.findCanvasFieldNode(
            (IFieldNode) getMapperLink().getStartNode());

        if (mCanvasNode != null) {
            startPort = (JGoPort) mCanvasNode.getConnectPointObject();
            this.setFromPort(startPort);
        }

        if (canvas instanceof BasicCanvasView) {
            endPort.setLeft(((BasicCanvasView) canvas).getTreeLinkToPortX());

            IMapperLink link = getMapperLink();
            endPort.setTop(
                link.getEndNode().getY() + NodeYChangeListener.Y_AXIS_DIFF
                + ((JGoView) canvas).getViewPosition().y);
            link.getEndNode()
                .addPropertyChangeListener(
                new NodeYChangeListener(
                (JGoView) canvas,
                endPort));
        }
        
        if (getMapperCanvas().isPathHighlightingEnabled()) {
            boolean isHighlighting = new NetworkHighlightDetector(this).isHighlightDetected();
            new NetworkHighlightTraverser(isHighlighting).visit(this);
        }
    }
}
