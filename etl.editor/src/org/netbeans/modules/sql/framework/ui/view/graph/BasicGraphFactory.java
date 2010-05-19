/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.Dimension;
import java.awt.Point;

import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLCaseOperator;
import org.netbeans.modules.sql.framework.model.SQLCastOperator;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLOperator;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.OperatorXmlInfoModel;
import org.netbeans.modules.sql.framework.ui.view.join.JoinViewGraphNode;

import com.nwoods.jgo.JGoObject;
import com.sun.etl.exception.BaseException;

/**
 * @author Ritesh Adval
 */
public class BasicGraphFactory extends AbstractGraphFactory {

    private Point sourceTableLoc = new Point(80, 50);
    private Point targetTableLoc = new Point(400, 50);
    private String operatorFolder;

    public BasicGraphFactory(String operatorFolder) {
        this.operatorFolder = operatorFolder;
    }

    /**
     * Factory method for creating instance of IGraphNode given an SQLObject
     * 
     * @param canvasObj sql object to be represented in the graph
     * @return an instance of IGraphNode
     */
    public IGraphNode createGraphNode(SQLCanvasObject canvasObj) throws BaseException {
        int objectType = canvasObj.getObjectType();
        GUIInfo gInfo = canvasObj.getGUIInfo();

        Point location = new Point(gInfo.getX(), gInfo.getY());
        Dimension size = new Dimension(gInfo.getWidth(), gInfo.getHeight());

        IGraphNode graphNode = null;
        boolean setSavedSize = true;
        SQLOperator operator = null;
        String operatorType = null;
        IOperatorXmlInfo opXmlInfo = null;

        // TODO Refactor! Break it down into protected methods that accept concrete
        // instances of SQLCanvasObject.
        if (canvasObj instanceof SQLOperator) {
            operator = (SQLOperator) canvasObj;
            operatorType = operator.getOperatorType();
            opXmlInfo = OperatorXmlInfoModel.getInstance(operatorFolder).findOperatorXmlInfo(operatorType);
            
            if (canvasObj instanceof SQLCastOperator) {
                graphNode = new SQLCastAsGraphNode(opXmlInfo);
            } else {
            	if (operator.isCustomOperator()){
                    graphNode = new CustomSQLOperatorGraphNode(operator.getOperatorXmlInfo(),
                                                         true,
                                                         operator.getCustomOperatorName());
                } else {
                    graphNode = new SQLOperatorGraphNode(opXmlInfo,
                                                         opXmlInfo.isShowParenthesis());
            	}
            }
            setSavedSize = false;
        } else if (canvasObj instanceof VisibleSQLLiteral) {
            IOperatorXmlInfo literal = OperatorXmlInfoModel.getInstance(operatorFolder).findOperatorXmlInfo("literal");
            if (literal != null) {
                SQLLiteralGraphNode literalObj = new SQLLiteralGraphNode(literal);

                literalObj.setDataObject(canvasObj);
                literalObj.setLocation(location);

                if (size.width == -1 && size.height == -1) {
                    size = new Dimension(literalObj.getMaximumWidth(), literalObj.getMaximumHeight());
                }
                literalObj.setSize(size);
                literalObj.setExpandedState(gInfo.isExpanded());

                return literalObj;
            }
        } else if (canvasObj instanceof SQLDBTable) {
            TableGraphNodeFactory fac = new TableGraphNodeFactory();
            graphNode = fac.createGraphNode(canvasObj);
            if (size.width == -1 && size.height == -1) {
                if (graphNode instanceof BasicCanvasArea) {
                    size = ((BasicCanvasArea) graphNode).getBoundingRect().getSize();
                } else {
                    size = new Dimension(150, 150);
                }
            }

            //if tables are dropped first time using wizard their location will be
            // -1, -1 so we need to set location for them
            if (location.x == -1 && location.y == -1) {
                if (objectType == SQLConstants.SOURCE_TABLE || objectType == SQLConstants.RUNTIME_INPUT) {
                    location = sourceTableLoc;
                } else {
                    location = targetTableLoc;
                }
            }
        } else if (canvasObj instanceof SQLCaseOperator) {
            SQLCaseArea caseArea = new SQLCaseArea();
            graphNode = caseArea;
            graphNode.setDataObject(canvasObj);

            if (size.width == -1 && size.height == -1) {
                size = new Dimension(caseArea.getMaximumWidth() + 10, caseArea.getMaximumHeight());
            }

            caseArea.setLocation(location);
            caseArea.setSize(size);

            graphNode.setExpandedState(gInfo.isExpanded());
            return graphNode;
        } else if (canvasObj instanceof SQLJoinView) {
            SQLJoinView joinView = (SQLJoinView) canvasObj;
            JoinViewGraphNode joinNode = new JoinViewGraphNode(joinView);
            graphNode = joinNode;
        }

        // If graph node is not null set canvas object as data object
        // also set location and add this graph node
        if (graphNode != null) {
            graphNode.setDataObject(canvasObj);
            JGoObject gNode = (JGoObject) graphNode;
            gNode.setLocation(location);
            if (setSavedSize && size.width != -1 && size.height != -1) {
                gNode.setSize(size);
            }
            graphNode.setExpandedState(gInfo.isExpanded());
        } else {
            throw new BaseException("Failed to create GUI representation of " + canvasObj.getDisplayName());
        }

        return graphNode;
    }
}

