/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

import com.sun.etl.exception.BaseException;

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

    @Override
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

