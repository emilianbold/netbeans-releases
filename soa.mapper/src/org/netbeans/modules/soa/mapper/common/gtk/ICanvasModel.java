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

import java.awt.Rectangle;
import java.io.File;
import java.util.List;

/**
 * ICanvasModel represents a group of ICanvasNode and ICanvasLink that
 * can be viewed inside a ICanvas.  ICanvasModel represents the model
 * in the MVC architecture; ICanvas plays the role of the view and as
 * a default controller.
 * <p>
 * The ICanvasModel should be thought of as an ordered list of objects.
 * The objects are drawn in sequential order, so objects at the beginning
 * of the list appear "behind" objects that are at the end.
 * <p>
 * The ICanvasNode held in the document have a size and position.
 * The coordinate system used by the document just comes from the units
 * used by its objects.
 * ICanvas has a coordinate system that may be translated and scaled
 * from that of the document, so as to support panning and zooming.
 * The document's size is automatically expanded to encompass all of
 * its objects.
 * <p>
 * The document keeps track of all registered ICanvasModelUpdateListener.
 * ICanvas is a predefined implementor of ICanvasModelUpdateListener.
 * It needs to notice when document objects change so that it can update
 * the visible rendering of those objects.
 * You can register your own listeners to notice changes to the document
 * or its objects.  The fireUpdate() method actually does the notification of
 * all document listeners.
 * <p>
 * In addition to all of the objects held by the document, the document
 * has its own notion of the background color, called the paper color.
 * This is independent of the ICanvas Background color, which affects
 * the view's border.  The paper color takes precedence over the view's
 * Background color.
 * The default is null, and ICanvas's default Background color is Color.White.
 * This change permits the document to specify the color, as before, and now
 * also permits the view to specify the color when the PaperColor is null.
 * <p>
 * ICanvasModel implements Transferable and provides a default DataFlavor.
 * <p>
 * ICanvasModel is not thread-safe.
 * <p>
 * ICanvasModel always has at least one layer.
 * If you do use layers, it is as if the list of objects has been segmented
 * into consecutive groups of objects.
 * <p>
 * There are methods to add, re-order, and remove layers from a ICanvasModel,
 * and to get the first, next, previous, and last layers.  Each ICanvasModel
 * also has a defaultLayer property, which is used by methods that need to
 * create objects in a ICanvasModel but do not know which layer to put them in.
 * <p>
 * ICanvasModel has built-in support for Undo and Redo.
 * To turn on undo/redo functionality, create an instance of UndoManager and
 * call the document's setUndoManager method.  The UndoManager is a model
 * listener that records all changes to model properties and the document's
 * objects.
 * Those change records are implemented as ICanvasModelChangedEdits and
 * are accumulated in a CompoundEdit for each user's mouse action, bounded
 * by calls to startTransaction and endTransaction.
 * <p>
 * Your application will want to call the canUndo and canRedo predicates
 * and the undo and redo methods.  You may want to call discardAllEdits
 * when the model is saved, to avoid accumulating too much undo state.
 * <p>
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public interface ICanvasModel {

    /**
     * static key used to represent a process tag
     */
    String PROCESS_TAG = "Process";

    /**
     * static key used to represent a component node
     */
    String NODE_TAG = "Node";

    /**
     * static key used to represent a component group node
     */
    String GROUP_NODE_TAG = "GroupNode";

    /**
     * static key used to represent a flow link
     */
    String FLOW_TAG = "Flow";

    /**
     * static key used to represent the Logical View
     */
    String GUI_LOGICAL = "GUI_LOGICAL";

    /**
     * static key used to represent the Landscape View
     */
    String GUI_LANDSCAPE = "GUI_LANDSCAPE";

    /**
     * static key used to represent the domains used in the deployments
     */
    String GUI_DEPLOYMENT_DOMAINS = "DEPLOYMENT_DOMAINS";

    /**
     * static key used to represent the Domain View
     */
    String GUI_DOMAIN = "GUI_DOMAIN";

    /**
     * folliwing static keys used for attribute names when writing out
     * XML
     */
    String UID = "uid";
    /**
     * Description of the Field
     */
    String TYPE = "type";
    /**
     * Description of the Field
     */
    String X = "x";
    /**
     * Description of the Field
     */
    String Y = "y";
    /**
     * Description of the Field
     */
    String ICON = "icon";
    /**
     * Description of the Field
     */
    String WIDTH = "width";
    /**
     * Description of the Field
     */
    String HEIGHT = "height";
    /**
     * Description of the Field
     */
    String TEXT = "text";
    /**
     * Description of the Field
     */
    String TO = "to";
    /**
     * Description of the Field
     */
    String FROM = "from";
    /**
     * Description of the Field
     */
    String NAME = "name";
    /**
     * Description of the Field
     */
    String DOMAIN = "domain";

    /**
     * Adds a node to the model
     *
     * @param node  - node to be added
     */
    void addCanvasNode(ICanvasNode node);

    /**
     * @param node - canvas node to be added
     */
    void addToNodes(ICanvasNode node);

    /**
     * Removes a node from the moel
     *
     * @param node  - node to be removed
     */
    void removeCanvasNode(ICanvasNode node);

    /**
     * Compute and retrieves the bounds
     *
     * @return   the bounds
     */
    Rectangle getBounds();

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
     * This method finds the link as defined by the given from and to
     * nodes
     *
     * @param from  - the from node of the link
     * @param to    - the to node
     * @return      - the link as defined by the two nodes, null if
     *      failed.
     */
    ICanvasLink findLinkByNodes(ICanvasNode from, ICanvasNode to);

    /**
     * Adds a link
     *
     * @param link  - the canvas link to be added
     */
    void addLink(ICanvasLink link);


    /**
     * Saves the graphical information of this canvas to a DOM object
     *
     * @return   saved object canvas was saved to; otherwise null
     */
    Object save();

    /**
     * Saves the graphical information of this canvas
     *
     * @param filename  filename to write graphical information to
     * @return          true if save was successful; otherwise false
     */
    boolean save(File filename);


    /**
     * Removes the link by specified from and to nodes
     *
     * @param from - the from canvas node (source)
     * @param to - the to canvase node (destination)
     */
    void removeLinkByNodes(ICanvasNode from, ICanvasNode to);

    /**
     * Adds a new canvas update listener
     *
     * @param listener - the canvas model update listener
     */
    void addCanvasModelUpdateListener(ICanvasModelUpdateListener listener);

    /**
     * Removes the canvas update listener
     *
     * @param listener - the canvas model update listener
     */
    void removeCanvasModelUpdateListener(ICanvasModelUpdateListener listener);

    /**
     * loads the source
     *
     * @param dataModel - the data model
     * @param source - the source
     * @return boolean
     */
    boolean load(Object dataModel, Object source);

}
