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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import org.netbeans.modules.sql.framework.ui.event.SQLDataEvent;
import org.netbeans.modules.sql.framework.ui.event.SQLDataListener;
import org.netbeans.modules.sql.framework.ui.event.SQLLinkEvent;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorManager;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLCollaborationView extends JPanel implements SQLDataListener {

    private SQLGraphView graphView;
    private IOperatorManager mgr;

    /** Creates a new instance of SQLCollaborationView */
    public SQLCollaborationView(IOperatorManager opManager) {
        super();
        this.mgr = opManager;
        this.setLayout(new BorderLayout());
        graphView = (SQLGraphView) opManager.getGraphView();
        if(opManager.getOperatorView() != null ){
            this.add((Component) opManager.getOperatorView(), BorderLayout.NORTH);
        }
        if(opManager.getGraphView() != null) {
            this.add((Component) opManager.getGraphView(), BorderLayout.CENTER);
        }
    }
    
    public IOperatorManager getIOperatorManager() {
        return this.mgr;
    }

    public IGraphView getGraphView() {
        return graphView;
    }

    public IGraphNode findGraphNode(Object dataObj) {
        return graphView.findGraphNode(dataObj);
    }

    /**
     * delete a graph node
     * 
     * @param node graph node
     */
    public void deleteNode(IGraphNode node) {
        graphView.deleteNode(node);
    }

    public void setZoomFactor(double factor) {
        graphView.setScale(factor);
    }

    public double getZoomFactor() {
        return graphView.getScale();
    }

    public void linkCreated(SQLLinkEvent evt) {
        graphView.linkCreated(evt);
    }

    public void linkDeleted(SQLLinkEvent evt) {
        graphView.linkDeleted(evt);
    }

    public void objectCreated(SQLDataEvent evt) throws BaseException {
        graphView.objectCreated(evt);
    }

    public void objectDeleted(SQLDataEvent evt) {
        graphView.objectDeleted(evt);
    }

    public void requestFocus() {
        super.requestFocus();
        //This should have focus so that user can delete selected objects which
        //we select when reload happens only one object is selected but
        //without focus on graph view key event are not delegated to graph view
        graphView.requestFocus();
    }

    public void reset() {
        graphView.reset();
    }

    public void highlightInvalidNode(Object obj, boolean createSel) {
        graphView.highlightInvalidNode(obj, createSel);
    }

    public void childObjectCreated(SQLDataEvent evt) {
        graphView.childObjectCreated(evt);
    }

    public void childObjectDeleted(SQLDataEvent evt) {
        graphView.childObjectDeleted(evt);
    }

    public void objectUpdated(SQLDataEvent evt) {
        graphView.objectUpdated(evt);
    }

    /**
     * This method is called after each undoable operation in order to refresh the
     * presentation state of the undo/redo GUI
     */

    public void refreshUndoRedo() {
        //toolBar.refreshUndoRedo();
    }
}

