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

import javax.swing.Action;

/**
 * @author radval
 */
public interface IToolBar {

    /**
     * set tool bar controller
     * 
     * @param controller tool bar controller
     */
    public void setToolBarController(Object controller);

    /**
     * get tool bar controller
     * 
     * @return tool bar controller
     */
    public Object getToolBarController();

    /**
     * get the toolbar actions that need to be shown
     * 
     * @return a list of GraphAction, null in list represents a seperator
     */
    public List getActions();

    /**
     * set toolbar actions on this view
     * 
     * @param actions list of GraphAction
     */
    public void setActions(List actions);

    /**
     * get a action given its class
     * 
     * @param actionClass
     * @return action
     */
    public Action getAction(Class actionClass);

    /**
     * set the graph view for this tool bar
     * 
     * @param gView graph view
     */
    public void setGraphView(IGraphView gView);

    /**
     * get the graph view for this tool bar
     * 
     * @return graph view
     */
    public IGraphView getGraphView();

    public void initializeToolBar();

    public void enableToolBar(boolean enable);
}

