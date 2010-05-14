/*
 * JoinGraphController.java
 *
 * Created on February 8, 2004, 11:42 PM
 */

package org.netbeans.modules.edm.editor.ui.view.join;

import java.awt.Point;

import org.netbeans.modules.edm.editor.graph.jgo.IGraphController;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphLink;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphNode;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphPort;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphView;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfo;
import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;


/**
 * @author radval
 */
public class JoinGraphController implements IGraphController {

    protected SQLUIModel collabModel;
    protected IGraphView viewC;

    /** Creates a new instance of JoinGraphController */
    public JoinGraphController() {
    }

    public Object getDataModel() {
        return collabModel;
    }

    /**
     * handle drop
     * 
     * @param e DropTargetDropEvent
     */
    public void handleDrop(java.awt.dnd.DropTargetDropEvent e) {
    }

    /**
     * handle new link
     * 
     * @param from IGraphPort
     * @param to IGraphPort
     */
    public void handleLinkAdded(IGraphPort from, IGraphPort to) {
    }

    /**
     * handle link deletion
     * 
     * @param link IGraphLink
     */
    public void handleLinkDeleted(IGraphLink link) {
    }

    /**
     * handle node add
     * 
     * @param xmlInfo IOperatorXmlInfo
     * @param dropLocation dropLocation
     */
    public void handleNodeAdded(IOperatorXmlInfo xmlInfo, Point dropLocation) {
    }

    /**
     * handle node deletion
     * 
     * @param node IGraphNode
     */
    public void handleNodeRemoved(IGraphNode node) {
    }

    /**
     * handle drop
     * 
     * @param obj object dropped
     */
    public void handleObjectDrop(Object obj) {
    }

    /**
     * set the data model which this controller modifies
     * 
     * @param model data model
     */
    public void setDataModel(Object newModel) {
        collabModel = (SQLUIModel) newModel;
    }

    /**
     * set the view from which this controller interacts
     * 
     * @param view view
     */
    public void setView(Object view) {
        viewC = (IGraphView) view;
    }

}

