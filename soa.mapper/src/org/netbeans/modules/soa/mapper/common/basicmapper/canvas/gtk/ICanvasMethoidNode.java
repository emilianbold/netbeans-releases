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

import java.awt.Point;
import java.awt.Rectangle;

import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasGroupNode;

/**
 * <p>
 *
 * Title: </p> ICanvasMethoidNode<p>
 *
 * Description: </p> ICanvasMethoidNode describes the visual repersentation of a
 * methoid mapper node on the canvas. <p>
 *
 * @author    Un Seng Leong
 * @created   December 26, 2002
 */
public interface ICanvasMethoidNode
     extends ICanvasGroupNode {

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
    public IMethoidNode getMethoidNode();

    /**
     * Return a canvas field node that contains the specified connection object.
     *
     * @param pointObj  the connection object to be matched.
     * @return          a canvas field node that contains the specified
     *      connection object.
     */
    public ICanvasFieldNode getFieldNodeByConnectPointObject(Object pointObj);

    /**
     * Return a canvas field node that contains the specified point in its
     * bounding.
     *
     * @param point  the point to match
     * @return       a canvas field node that contains the specified point in
     *      its bounding.
     */
    public ICanvasFieldNode getFieldNodeByPoint(Point point);

    /**
     * Return the bounding rectangle of this node.
     *
     * @return the bounding rectangle of this node.
     */
    public Rectangle getBounding();

    /**
     * Sets the title string for this methoid.
     */
    public void setTitle(String title);
    
    /**
     * Returns the title of this methoid.
     */
    public String getTitle();

}
