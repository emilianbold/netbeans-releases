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

import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasLink;

/**
 * <p>
 *
 * Title: </p> ICanvasMapperLink<p>
 *
 * Description: </p> ICanvasMapperLink describes the visual repersentation of a
 * mapper link on the canvas. <p>
 *
 * @author    Un Seng Leong
 * @created   January 2, 2003
 */
public interface ICanvasMapperLink extends ICanvasLink {

    public static final Color DEFAULT_LINK_COLOR          = new Color(49, 106, 197);
    public static final Color DEFAULT_LINK_SELECTED_COLOR = new Color(230, 139, 44);
    
    
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
     * Return the mapper link that this canvas link repersents.
     *
     * @return   the mapper link that this canvas link repersents.
     */
    public IMapperLink getMapperLink();
}
