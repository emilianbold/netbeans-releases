/*
 * IGraphController.java
 *
 * Created on April 17, 2003, 4:25 PM
 */

package org.netbeans.modules.sql.framework.ui.graph;

import java.awt.Point;

/**
 * @author radval
 */
public interface IGraphController {

    /**
     * handle node add
     * 
     * @param xmlInfo IOperatorXmlInfo
     * @param dropLocation dropLocation
     */
    public void handleNodeAdded(IOperatorXmlInfo xmlInfo, Point dropLocation);

    /**
     * handle node deletion
     * 
     * @param node IGraphNode
     */
    public void handleNodeRemoved(IGraphNode node);

    /**
     * handle new link
     * 
     * @param from IGraphPort
     * @param to IGraphPort
     */
    public void handleLinkAdded(IGraphPort from, IGraphPort to);

    /**
     * handle link deletion
     * 
     * @param link IGraphLink
     */
    public void handleLinkDeleted(IGraphLink link);

    /**
     * handle drop
     * 
     * @param e DropTargetDropEvent
     */
    public void handleDrop(java.awt.dnd.DropTargetDropEvent e);

    /**
     * handle drop
     * 
     * @param obj object dropped
     */
    public void handleObjectDrop(Object obj);

    /**
     * set the data model which this controller modifies
     * 
     * @param model data model
     */
    public void setDataModel(Object model);

    /**
     * get the data model which this controller modifies
     * 
     * @return data model
     */
    public Object getDataModel();

    /**
     * set the view from which this controller interacts
     * 
     * @param view view
     */
    public void setView(Object view);
}

