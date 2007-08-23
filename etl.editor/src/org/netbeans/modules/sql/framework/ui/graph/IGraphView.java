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

package org.netbeans.modules.sql.framework.ui.graph;

import java.util.Collection;
import java.util.List;

import javax.swing.Action;

/**
 * @author radval
 */
public interface IGraphView extends ICommand {

    /**
     * add a IGraphNode
     * 
     * @param node IGraphNode
     */
    public void addNode(IGraphNode node);

    /**
     * Remove an IGraphNode
     * 
     * @param node IGraphNode
     */
    public void removeNode(IGraphNode node);

    /**
     * add a link
     * 
     * @param from IGraphPort
     * @param to IGraphPort
     */
    public void addLink(IGraphPort from, IGraphPort to);

    /**
     * add a graph link
     * 
     * @param link graph link
     */
    public void addLink(IGraphLink link);

    /**
     * Remove a link
     * 
     * @param from IGraphPort
     * @param to IGraphPort
     */
    public void removeLink(IGraphPort from, IGraphPort to);

    /**
     * set the graph controller on this view
     * 
     * @param controller graph controller
     */
    public void setGraphController(IGraphController controller);

    /**
     * get the graph controller of this view
     * 
     * @return graph controller
     */
    public IGraphController getGraphController();

    /**
     * Expand all the graph objects
     */
    public void expandAll();

    /**
     * collapse all the graph objects
     */
    public void collapseAll();

    /**
     * autolayout all the graph objects
     */
    public void autoLayout();

    /**
     * delete a graph node
     * 
     * @param node graph node
     */
    public void deleteNode(IGraphNode node);

    /**
     * delete a collection of links
     */
    public void deleteLinks(Collection links);

    /**
     * get graph view manager which can manage this view
     * 
     * @return graph view manager
     */
    public Object getGraphViewContainer();

    /**
     * set the graph view manager which this view can refer to
     * 
     * @param mgr graph view manager
     */
    public void setGraphViewContainer(Object mgr);

    /**
     * Print the view
     */
    public void printView();

    /**
     * set the graph model
     * 
     * @param model graph model
     */
    public void setGraphModel(Object model);

    /**
     * get graph model
     * 
     * @return graph model
     */
    public Object getGraphModel();

    /**
     * get the graph actions that need to be shown in popup menu
     * 
     * @return a list of GraphAction, null in list represents a seperator
     */
    public List getGraphActions();

    /**
     * set graph actions on this view
     * 
     * @param actions list of GraphAction
     */
    public void setGraphActions(List actions);

    /**
     * can this graph be edited
     * 
     * @return true if graph is edited
     */
    public boolean canEdit();

    /**
     * set the graph factory which is used for creating nodes in this graph
     * 
     * @param gFactory graph node factory
     */
    public void setGraphFactory(Object gFactory);

    /**
     * get the graph factory
     * 
     * @return graph factory
     */
    public Object getGraphFactory();

    /**
     * get a action based on class name
     * 
     * @param actionClass
     * @return action
     */
    public Action getAction(Class actionClass);

    /**
     * set the toolbar
     * 
     * @param toolBar
     */
    public void setToolBar(IToolBar toolBar);

    /**
     * get the toolbar
     * 
     * @return toolbar
     */
    public IToolBar getToolBar();

    /**
     * remove all the view and document objects
     */
    public void clearAll();

    /**
     * set this graph view modifiable
     * 
     * @param b modifiable
     */
    public void setModifiable(boolean b);

    /**
     * check if this graph view is modifiable
     * 
     * @return modifiable
     */
    public boolean isModifiable();

    /**
     * Execute a command
     * 
     * @param command - command
     * @param args - arguments
     */
    public void execute(String command, Object[] args);

    public IGraphNode findGraphNode(Object obj);

    /**
     * Reset selection colors
     */
    public void resetSelectionColors();

    /**
     * Highlight invalid graph node
     * 
     * @param dataObj data object whose graph node needs to be highlighted
     * @param createSel if true will create a new selection otherwise add the graph node
     *        to existing selection
     */
    public void highlightInvalidNode(Object dataObj, boolean createSel);

    public void clearSelection();
    
    public void setXMLInfo(IOperatorXmlInfo xmlInfo);
}
