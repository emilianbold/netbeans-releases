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

import java.util.List;

/**
 * @author radval
 */
public interface IGraphNode extends IDataNode {
    /**
     * get field name given a port
     * 
     * @param graphPort graph port
     * @return field name
     */
    String getFieldName(IGraphPort graphPort);

    /**
     * get output graph port , given a field name
     * 
     * @param fieldName field name
     * @return graph port
     */
    IGraphPort getOutputGraphPort(String fieldName);

    /**
     * get input graph port , given a field name
     * 
     * @param fieldName field name
     * @return graph port
     */
    IGraphPort getInputGraphPort(String fieldName);

    /**
     * get a list of all input and output links
     * 
     * @return list of input links
     */
    List getAllLinks();

    /**
     * Expand this graph node
     * 
     * @param expand whether to expand or collapse this node
     */
    public void expand(boolean expand);

    /**
     * get the child graphNode
     * 
     * @param obj child data object
     * @return graph node
     */
    public IGraphNode getChildNode(Object obj);

    /**
     * Remove a child object
     * 
     * @param child child object
     */
    public void removeChildNode(IGraphNode childNode) throws Exception;

    /**
     * Get the parent node
     * 
     * @return parent
     */
    public IGraphNode getParentGraphNode();

    /**
     * set the expansion state of the graph node
     * 
     * @param expand expansion state
     */
    public void setExpandedState(boolean expand);

    /**
     * get the expanded state
     * 
     * @return expanded state
     */
    public boolean isExpandedState();

    /**
     * set the graph view whixh hold this node
     * 
     * @param view graph view
     */
    public void setGraphView(IGraphView view);

    /**
     * get the graphview which holds this view
     * 
     * @return graph view
     */
    public IGraphView getGraphView();

    /**
     * update this node with changes in data object
     */
    public void updateUI();

    /**
     * remove the child data object
     * 
     * @param obj child data object
     */
    public void removeChildObject(Object obj);

    /**
     * add child data object
     * 
     * @param obj child data object
     */
    public void addChildObject(Object obj);

    /**
     * set the actions on a node
     * 
     * @param actions
     */
    public void initializeActions(List actions);

    /**
     * is this node can be deleted
     * 
     * @return true if node can be deleted
     */
    public boolean isDeleteAllowed();

}

