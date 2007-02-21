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

import java.awt.Point;
import javax.swing.Icon;

/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public interface ICanvasNode {

    /**
     * Retrieves the name of the node
     *
     * @return   - name
     */
    String getName();

    /**
     * Sets the name of the name
     *
     * @param name  - new name
     */
    void setName(String name);

    /**
     * Description of the Method
     *
     * @param text  Description of the Parameter
     */
    void updateLabel(String text);


    /**
     * Retrieves the data object
     *
     * @return   - underlying data object represented by this node
     */
    Object getDataObject();

    /**
     * sets the data object
     *
     * @param obj  - then object to be represented by this node
     */
    void setDataObject(Object obj);

    /**
     * Gets the Icon which representing this node
     *
     * @return   the icon, null if there is no icon associated
     */
    Icon getIcon();

    /**
     * Sets the icon associated with this node
     *
     * @param icon - the icon
     */
    void setIcon(Icon icon);

    /**
     * Retrieves the underlying ui component
     *
     * @return Object
     */
    Object getUIComponent();

    /**
     * Sets the action
     *
     * @param action - the canvas action
     */
    void setAction(ICanvasAction action);

    /**
     * Retrieves the action
     *
     * @return   The action value
     */
    ICanvasAction getAction();

    /**
     * Sets the abstract canvas node popup
     *
     * @param popup - the canvas node popup box
     */
    void setPopup(ICanvasNodePopupBox popup);

    /**
     * Retrieves the popup
     *
     * @return ICanvasNodePopupBox
     */
    ICanvasNodePopupBox getPopup();

    /**
     * Sets the aux popup this node is associated with
     *
     * @param popup - the canvas node popup box
     */
    void setAuxPopup(ICanvasNodePopupBox popup);

    /**
     * Retrieves the aux popup
     *
     * @return ICanvasNodePopupBox
     */
    ICanvasNodePopupBox getAuxPopup();

    /**
     * Sets the node visible on the canvas
     *
     * @param val - value
     */
    void setVisible(boolean val);

    /**
     * Retrieves the location
     *
     * @return Point
     */
    Point getNodeLocation();

    /**
     * Sets the location
     *
     * @param p  - location
     */
    void setNodeLocation(Point p);

    /**
     * Sets the canvas this node belongs to
     *
     * @param canvas - the canvas
     */
    void setCanvas(ICanvas canvas);

    /**
     * Retrieves the cavnas
     *
     * @return ICanvas
     */
    ICanvas getCanvas();

    /**
     * Sets the container this node belongs to
     *
     * @param group - the canvas group node
     */
    void setContainer(ICanvasGroupNode group);

    /**
     * Retrieves the container
     *
     * @return ICanvasGroupNode
     */
    ICanvasGroupNode getContainer();

    /**
     * Retrieves the control node associated with the canvas node.
     * Return null is nothing associated.
     *
     * @return ICanvasControlNode
     */
    ICanvasControlNode getControlNode();

    /**
     * Sets the control node associated with this canvas node
     *
     * @param node - the canvas control node
     */
    void setControlNode(ICanvasControlNode node);

    /**
     * Retrieves the component node associated with the canvas node.
     * Return null is nothing associated.
     *
     * @return ICanvasComponentNode
     */
    ICanvasComponentNode getComponentNode();

    /**
     * Sets the component node associated with this canvas node
     *
     * @param node - the canvas component node
     */
    void setComponentNode(ICanvasComponentNode node);

    /**
     * Returns if node visible
     *
     * @return boolean
     */
    boolean isVisible();

    /**
     * Refreshs the node image with the given icon
     *
     * @param icon - the icon
     */
    void refreshNodeImage(Icon icon);

}
