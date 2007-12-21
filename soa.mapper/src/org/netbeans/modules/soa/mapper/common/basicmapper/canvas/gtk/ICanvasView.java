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

import java.awt.Color;
import java.awt.Point;

import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvas;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasLink;

/**
 * <p>
 *
 * Title: </p> ICanvasView <p>
 *
 * Description: </p> ICanvasView extends ICanvas to provide more interfaces
 * required by the mapper canvas<p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 26, 2002
 * @version   1.0
 */
public interface ICanvasView
     extends ICanvas {

    /**
     * Set the canvas node border color.
     *
     * @param color  the canvas node border color.
     */
    public void setNodeBorderColor(Color color);

    /**
     * Return the canvas node border color.
     *
     * @return   the canvas node border color.
     */
    public Color getNodeBorderColor();

    /**
     * Set factory that creates the mapper canvas objects.
     *
     * @param factory  factory that creates the canvas objects.
     */
    public void setCanvasObjectFactory(ICanvasObjectFactory factory);

    /**
     * Return the factory that creates mapper canvas object objects.
     *
     * @return   the factory that creates mapper canvas object objects.
     */
    public ICanvasObjectFactory getCanvasObjectFactory();

    /**
     * Find and return a canvas field node the containing the specified mapper
     * field node.
     *
     * @param fieldNode  the mapper field node to match
     * @return           a canvas field node the containing the specified mapper
     *      field node.
     */
    public ICanvasFieldNode findCanvasFieldNode(IFieldNode fieldNode);

    /**
     * Find and return a canvas methoid node the containing the specified mapper
     * methoid node.
     *
     * @param methoidNode  the mapper methoid node to match
     * @return             a canvas methoid node the containing the specified
     *      mapper methoid node.
     */
    public ICanvasMethoidNode findCanvasMethoidNode(
        IMethoidNode methoidNode);

    /**
     * Return a canvas methoid node that contains the specified point in its
     * bounding.
     *
     * @param point  the point to be matched
     * @return       a canvas methoid node that contains the specified point in
     *      its bounding
     */
    public ICanvasMethoidNode getCanvasMethoidNodeByPoint(Point point);

    /**
     * Return a canvas field node that contains the specified point in its
     * bounding.
     *
     * @param point  the point to be matched
     * @return       a canvas field node that contains the specified point in
     *      its bounding
     */
    public ICanvasFieldNode getCanvasFieldNodeByPoint(Point point);

    /**
     * Return true if the specified mapper link can be connected by a mapper
     * node that is the closest drag operation from the canvas, false otherwise.
     *
     * @param link  the mapper link that should has 1 end does not connected to
     *      any node.
     * @return      true if the specified mapper link can be connected by a
     *      mapper node that is the closest drag operation from the canvas,
     *      false otherwise.
     */
    public boolean connectLinkByDrag(IMapperLink link);

    /**
     * Return true if the specified mapper link can be connected by a mapper
     * node that can be found by the specified point, false otherwise.
     *
     * @param point  the point where the mapper node to be found
     * @param link   the link that should has 1 end does not connected to any
     *      nodes.
     * @return       true if a canvas link contains the specified mapper link
     *      can be created, false otherwise.
     */
    public boolean connectLinkByPoint(Point point, IMapperLink link);
    
    /**
     * Returns true if a JGoObject is found in the selected layer at the point.
     */
    public Object getObjectInModel(Point modelCor, boolean flag);

    /**
     * Return the canvas mapper view that contains this canvas.
     *
     * @return   the canvas mapper view that contains this canvas.
     */
    public IMapperCanvasView getParentView();

    /**
     * Collapse all nodes in the canvas.
     */
    public void collapseAllNode();

    /**
     * Expend all nodes in this canvas.
     */
    public void expendAllNode();
    
    /**
     * Return a default location for the specified new node that will possible 
     * add to this canvas view. 
     * 
     * @param newNode  the node to be use to calculate the new location
     * @return a point where the defualt location of a new node.
     */
    public Point getDefaultLocationForNewNode (IMethoidNode newNode);
    
    /**
     * Convert a view location into a model location.
     */
    public void convertViewToDoc(Point p);
    
    /**
     * Convert a model location into a doc location.
     */
    public void convertDocToView(Point p);

    /**
     * Simply adds a link to the canvas, and does nothing more.
     */
    public void addRawLink(ICanvasLink link);
    
    /**
     * Simply removes the link from the canvas, doing nothing more.
     */
    public void removeRawLink(ICanvasLink link);
    
    /**
     * Whether an entire chain of connected links/node elements will
     * appear highlighted upon selection of any element in the chain.
     */
    public boolean isPathHighlightingEnabled();
    
    /**
     * Sets whether chains of linked nodes are highlighted.
     * Enable this if multiple mapping paths are shown in the mapper
     * at the same time... thereby allowing the user to see which
     * path is currently connected to the selection.
     */
    public void setPathHighlightingEnabled(boolean isEnabled);
    
    /**
     * Return a canvas link that contains the mapper link, or null if no such
     * mapper link.
     *
     * @param dataObject  the mapper link
     * @return            a canvas link that contains the mapper link, or null
     *      if no such mapper link.
     */
    public ICanvasLink getCanvasLinkByDataObject(Object dataObject);
}
