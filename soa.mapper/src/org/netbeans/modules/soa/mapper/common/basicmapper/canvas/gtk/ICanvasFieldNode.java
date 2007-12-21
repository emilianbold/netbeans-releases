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

package org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk;

import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasNode;
import java.awt.Rectangle;

/**
 * <p>
 *
 * Title: </p> ICanvasFieldNode<p>
 *
 * Description: </p> ICanvasFieldNode descibes the visual repersentation of a
 * field mapper node on the canvas. <p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */
public interface ICanvasFieldNode
     extends ICanvasNode {

    /**
     * Set the canvas contains this canvas node.
     *
     * @param canvas  the canvas contains this canvas node.
     */
    public void setMapperCanvas(ICanvasView canvas);

    /**
     * Return the canvas that contains this canvas node.
     *
     * @return   the canvas that contains this canvas node.
     */
    public ICanvasView getMapperCanvas();

    /**
     * Return the mapper node that this canvas node repersents.
     *
     * @return   the mapper node that this canvas node repersents.
     */
    public IFieldNode getFieldNode();

    /**
     * Retrun the link connection point in an Object repersentation
     *
     * @return   the link connection point in an Object repersentation
     */
    public Object getConnectPointObject();

    /**
     * Return the bounding rectangle of this node.
     *
     * @return the bounding rectangle of this node.
     */
    public Rectangle getBounding();
    
    /**
     * Set the highlighted status of a field node.
     * This visually indicates that a field node is 
     * somehow highlighted.
     */
    public void setHighlight(boolean isHighlighted);
}
