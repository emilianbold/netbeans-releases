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

package org.netbeans.modules.soa.mapper.common.gtk;

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.Icon;

/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public interface ICanvasObjectFactory {

    /**
     * Creates a canvas node
     *
     * @param location  - location
     * @param size      - size
     * @param label     - name of the node
     * @return          - the created node
     */
    ICanvasNode createCanvasNode(Point location, Dimension size,
        String label);

    /**
     * Creates a canvas node with image
     *
     * @param location  - location of the node
     * @param size      - size of the node
     * @param image     - image
     * @param label     - name of the node
     * @return          - the created node
     */
    ICanvasNode createCanvasNode(Point location, Dimension size,
        Icon image, String label);

    /**
     * Creates a canvas node with image
     *
     * @param location   - location of the node
     * @param size       - size of the node
     * @param label      - name of the node
     * @param labelIcon - the label icon
     * @param icon       Description of the Parameter
     * @return ICanvasNode
     */
    ICanvasNode createCanvasNode(Point location, Dimension size,
        Icon icon, String label, Icon labelIcon);

    /**
     * Creates a link between two nodes
     *
     * @param src   - the source node
     * @param dest  - the destination node
     * @return      - return the node
     */
    ICanvasLink createCanvasLink(ICanvasNode src,
        ICanvasNode dest);

    /**
     * Creates a canvas model with the given name
     *
     * @param name  - the name
     * @return      - a newly created canvas model
     */
    ICanvasModel createCanvasModel(String name);

    /**
     * Description of the Method
     *
     * @param size        Description of the Parameter
     * @param icon        Description of the Parameter
     * @param label       Description of the Parameter
     * @param parentLink  Description of the Parameter
     * @return            Description of the Return Value
     */
    ICanvasLinkLabel createLinkLabel(Dimension size,
        Icon icon, String label, ICanvasLink parentLink);


    /**
     * Creates a canvas
     *
     * @param model - the canvas model
     * @return ICanvas
     */
    ICanvas createCanvas(ICanvasModel model);

    /**
     * creates the controller
     *
     * @param canvas - the canvas
     * @return ICanvasController
     */
    ICanvasController createCanvasController(ICanvas canvas);
}
