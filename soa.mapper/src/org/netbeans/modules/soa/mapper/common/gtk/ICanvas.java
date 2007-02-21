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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;

/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */
public interface ICanvas
     extends ICanvasModelUpdateListener {
    /**
     * Addes a node to the canvas
     *
     * @param node - the canvas node
     */
    void addNode(ICanvasNode node);

    /**
     * Adds a link to the canvas
     *
     * @param link - the canvas link
     */
    void addLink(ICanvasLink link);

    /**
     * Sets the controller for the canvas
     *
     * @param controller - the canvas controller
     */
    void setCanvasController(ICanvasController controller);

    /**
     * Retrieves the UI component
     *
     * @return  Component - the UI component
     */
    Component getUIComponent();

    /**
     * Sets the model
     *
     * @param model - the canvas model
     */
    void setModel(ICanvasModel model);

    /**
     * Sets the canvas palette
     *
     * @param palette - the canvas palette
     */
    void setCanvasPalette(DefaultCanvasPalette palette);

    /**
     * Returns the canvas palette
     *
     * @return DefaultCanvasPalette
     */
    DefaultCanvasPalette getCanvasPalette();

    /**
     * sets the zoom factor, minimum is 0.0, 1.0 represenst 100%
     *
     * @param factor - the zoom factor
     */
    void setZoomFactor(double factor);

    /**
     * Gets the zoomFactor attribute of the ICanvas object
     *
     * @return double -  The zoomFactor value
     */
    double getZoomFactor();

    /**
     * Toggles the link mode
     *
     * @param val - the toggle link mode value
     */
    void toggleLinkMode(boolean val);

    /**
     * Retrieves a list of selected nodes
     *
     * @return   List
     */
    Collection getSelectedNodes();

    /**
     * Retrieves a collection of selected links
     *
     * @return Collection
     */
    Collection getSelectedLinks();

    /**
     * Selectes the data object
     *
     * @param dataObject - the data object
     */
    void selectNode(Object dataObject);

    /**
     * Creates a canvas node
     *
     * @param location  - location
     * @param size      - size
     * @param label     - name of the node
     * @return          - the created node
     */
    ICanvasNode createCanvasNode(
        Point location, Dimension size, String label);

    /**
     * Creates a canvas node with image
     *
     * @param location  - location of the node
     * @param size      - size of the node
     * @param label     - name of the node
     * @param icon      Description of the Parameter
     * @return          - the created node
     */
    ICanvasNode createCanvasNode(
        Point location, Dimension size, Icon icon, String label);

    /**
     * Creates a canvas node with image
     *
     * @param location   - location of the node
     * @param size       - size of the node
     * @param label      - name of the node
     * @param labelIcon - the label Icon
     * @param icon       Description of the Parameter
     * @return   ICanvasGroupNode       - the created node
     */
    ICanvasGroupNode createCanvasGroupNode(Point location, Dimension size,
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
     * Description of the Method
     *
     * @param src              Description of the Parameter
     * @param dest             Description of the Parameter
     * @param isComponentNode  Description of the Parameter
     * @param isWithBinding    Description of the Parameter
     * @return                 Description of the Return Value
     */
    ICanvasLink createCanvasLink(ICanvasNode src,
        ICanvasNode dest, boolean isComponentNode, boolean isWithBinding);

    /**
     * Removes the nodes from the canvas
     *
     * @param nodes - the Nodes to remove
     */
    void removeNodes(Collection nodes);

    /**
     * Retrieves the canvas controller
     *
     * @return ICanvasController
     */
    ICanvasController getCanvasController();

    /**
     * retrieves the canvas node defined by the given data object
     *
     * @param dataObject - the data object
     * @return ICanvasNode
     */
    ICanvasNode getCanvasNodeByDataObject(Object dataObject);

    /**
     * Retrieves all the first level canvas nodes
     *
     * @return List
     */
    List getNodes();

    /**
     * Retreives the mdoel
     *
     * @return ICanvasModel
     */
    ICanvasModel getModel();

    /**
     * checks if the given node exisits
     *
     * @param node  Description of the Parameter
     * @return boolean
     */
    boolean isNodeExisit(ICanvasNode node);

    /**
     * Sets the link's label visiblity to the given value
     *
     * @param val - the link label visibel value
     */
    void setLinkLabelVisible(boolean val);

    /**
     * Is link visible
     *
     * @return boolean
     */
    boolean isLinkLabelVisible();


    /**
     * Retrieves model object at the given location
     *
     * @param modelCor the model cor point
     * @param flag - the flage
     * @return Object
     */
    Object getObjectInModel(Point modelCor, boolean flag);

    /**
     * Description of the Method
     */
    void clearSelection();

    /**
     * Handles default mouse down
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean doDefaultMouseDown(ICanvasMouseData data);

    /**
     * Handles default mouse up
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean doDefaultMouseUp(ICanvasMouseData data);

    /**
     * Handls default mouse move
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean doDefaultMouseMove(ICanvasMouseData data);

    /**
     * Handls default mouse click
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean doDefaultMouseClick(ICanvasMouseData data);

    /**
     * Handles default mouse double click
     *
     * @param data - the canvas mouse data
     * @return boolean
     */
    boolean doDefaultMouseDblClick(ICanvasMouseData data);

    /**
     * Handles default delete
     *
     * @return boolean
     */
    boolean doDefaultDeleteSelection();


    /**
     * Finds and updates parent node
     *
     * @param node   - the node whose parent node will be updated
     * @param nodes  -- the list of potentional parents node to be set
     */
    void updatesParentNode(Object node, Object[] nodes);

    /**
     * Sets whether to pass mouse event to controller
     *
     * @param val  The new passMouseEventToController value
     */
    void setPassMouseEventToController(boolean val);

    /**
     * Retrieves whether we pass mouse event to the controller
     *
     * @return boolean
     */
    boolean isPassMouseEventToController();

    public void addCanvasMouseListener(ICanvasMouseListener listener);

    public void removeCanvasMouseListener(ICanvasMouseListener listener);
}
