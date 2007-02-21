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
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasNodeToNodeLink;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;

/**
 * <p>
 *
 * Title: </p> BasicCanvasNodeToNodeLink<p>
 *
 * Description: </p> BasicCanvasNodeToNodeLink provides basic implemanation of
 * ICanvasNodeToNodeLink. <p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */
public class BasicCanvasNodeToNodeLink
     extends AbstractCanvasLink
     implements ICanvasNodeToNodeLink {

    /**
     * the end canvas node
     */
    private ICanvasFieldNode mDestCanvasNode;

    /**
     * the start canvas node
     */
    private ICanvasFieldNode mSourceCanvasNode;

    
    /**
     * Creates a new BasicCanvasNodeToNodeLink object.
     *
     * @param link  the mapper link
     */
    public BasicCanvasNodeToNodeLink(IMapperLink link) {
        super(link);
        this.setArrowHeads(false, true);
        initializeArrowHeads(this);
    }

    /**
     * Return the canvas field node repersetns the start point of this link.
     *
     * @return   the canvas field node repersetns the start point of this link.
     */
    public ICanvasFieldNode getDestFieldNode() {
        return mDestCanvasNode;
    }

    /**
     * Return the canvas field node repersetns the end point of this link.
     *
     * @return   the canvas node repersetns the end point of this link.
     */
    public ICanvasFieldNode getSourceFieldNode() {
        return mSourceCanvasNode;
    }

    /**
     * Set the canvas contains this canvas link.
     *
     * @param canvas  the canvas contains this canvas link.
     */
    public void setMapperCanvas(ICanvasView canvas) {
        super.setMapperCanvas(canvas);

        mSourceCanvasNode =
            canvas.findCanvasFieldNode(
            (IFieldNode) getMapperLink().getStartNode());

        mDestCanvasNode =
            canvas.findCanvasFieldNode(
            (IFieldNode) getMapperLink().getEndNode());

        if ((mSourceCanvasNode != null) && (mDestCanvasNode != null)) {
            startPort = (JGoPort) mSourceCanvasNode.getConnectPointObject();
            endPort = (JGoPort) mDestCanvasNode.getConnectPointObject();
            this.setFromPort(startPort);
            this.setToPort(endPort);
        }
        
        if (getMapperCanvas().isPathHighlightingEnabled()) {
            boolean isHighlighting = new NetworkHighlightDetector(this).isHighlightDetected();
            new NetworkHighlightTraverser(isHighlighting).visit(this);
        }
    }
}
