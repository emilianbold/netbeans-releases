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
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;

/**
 * <p>
 *
 * Title: </p> ICanvasObjectFactory <p>
 *
 * Description: </p> ICanvasObjectFactory describes the interfaces that a canvas
 * view would use to create the canvas object repersenting the mapper objects.
 * <p>
 *
 * @author    Un Seng Leong
 * @created   December 26, 2002
 */
public interface ICanvasObjectFactory {

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
     * Return a new canvas link repersenting the specified mapper link.
     *
     * @param link  the mapper link
     * @return      a new canvas link repersenting the specified mapper link.
     */
    public ICanvasMapperLink createLink(IMapperLink link);

    /**
     * Return a new canvas field node repersenting the specified mapper field
     * node.
     *
     * @param node  the mapper field node
     * @return      a new canvas field node repersenting the specified mapper
     *      field node.
     */
    public ICanvasFieldNode createFieldNode(IFieldNode node);

    /**
     * Return a new canvas methoid node repersenting the specified mapper
     * methoid node.
     *
     * @param methoidNode  the mapper methoid node
     * @return             a new canvas methoid node repersenting the specified
     *      mapper methoid node.
     */
    public ICanvasMethoidNode createMethoidNode(IMethoidNode methoidNode);
}
