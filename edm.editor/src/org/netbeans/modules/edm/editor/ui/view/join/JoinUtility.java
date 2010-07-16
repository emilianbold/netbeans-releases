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
package org.netbeans.modules.edm.editor.ui.view.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDBColumn;
import org.netbeans.modules.edm.model.SQLInputObject;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.model.SQLJoinTable;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SQLModelObjectFactory;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLPredicate;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.impl.VisibleSQLPredicateImpl;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphView;
import org.netbeans.modules.edm.editor.ui.model.CollabSQLUIModel;
import org.netbeans.modules.edm.editor.ui.model.JoinBuilderSQLUIModel;
import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.netbeans.modules.edm.editor.ui.view.TableColumnNode;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.model.DBTable;

/**
 * @author radval
 */
public class JoinUtility {

    /** Creates a new instance of JoinUtility */
    private JoinUtility() {
    }

    public static void editJoinView(SQLJoinView oldJView, SQLJoinView modifiedJoinView, List joinSources, List tableNodes, IGraphView gView) throws EDMException {
        if (modifiedJoinView != null) {
            oldJView.removeAllObjects();
            oldJView.getAllObjects().addAll(modifiedJoinView.getAllObjects());
        }
    }

    private static boolean isColumnVisible(SQLDBColumn column, List tableNodes) {
        Iterator it = tableNodes.iterator();
        while (it.hasNext()) {
            TableColumnNode tNode = (TableColumnNode) it.next();
            Enumeration enu = tNode.children();
            while (enu.hasMoreElements()) {
                TableColumnNode cNode = (TableColumnNode) enu.nextElement();
                SQLDBColumn tColumn = (SQLDBColumn) cNode.getUserObject();
                if (column.equals(tColumn)) {
                    return cNode.isSelected();
                }
            }
        }

        return true;
    }

    public static void handleNewJoinCreation(SQLJoinView newJoin, List tableNodes, CollabSQLUIModel sqlModel) throws EDMException {
        Iterator tIt = newJoin.getSourceTables().iterator();
        while (tIt.hasNext()) {
            SourceTable sTable = (SourceTable) tIt.next();
            sTable.setUsedInJoin(true);
            //if its a new table selected using more table dialog then
            //we need to add this to sql model
            if (!sqlModel.exists(sTable)) {
                sqlModel.addObject(sTable);
            }

            Iterator it = sTable.getColumnList().iterator();
            while (it.hasNext()) {
                SQLDBColumn column = (SQLDBColumn) it.next();
                //if coolumn was visible earlier and now became invisible then we need
                // to remove that
                //column
                if (column.isVisible() && !isColumnVisible(column, tableNodes)) {
                    //make sure first set visible property to false
                    column.setVisible(false);
                }
            }
        }
        //create join view in the canvas
        sqlModel.addObject(newJoin);
        sqlModel.setDirty(true);
    }

    public static void handleAutoJoins(SQLJoinTable jTable, boolean addTable, JoinBuilderSQLUIModel model) throws EDMException {
        //this list keep track of all joins created so far.
        ArrayList<SQLObject> allObjectList = new ArrayList<SQLObject>();
        if (addTable) {
            allObjectList.add(jTable);
        }

        ArrayList<SQLObject> tablesSoFar = new ArrayList<SQLObject>();

        SQLJoinView joinView = model.getSQLJoinView();
        tablesSoFar.addAll(joinView.getSQLJoinTables());

        //find root join from join view this will be previous join
        SQLJoinOperator previousJoin = null;
        Collection joins = joinView.getObjectsOfType(SQLConstants.JOIN);
        Iterator jIt = joins.iterator();
        while (jIt.hasNext()) {
            SQLJoinOperator join = (SQLJoinOperator) jIt.next();
            if (join.isRoot()) {
                previousJoin = join;
                break;
            }
        }

        if (tablesSoFar.size() >= 1) {

            //now find all the auto joins between obj and rest of the joinTables tables
            //and add them as well
            List joinList = SQLObjectUtil.getAutoJoins(jTable, tablesSoFar);
            //create a SQLJoinOperator
            SQLJoinOperator join = SQLModelObjectFactory.getInstance().createSQLJoinOperator();
            //add join to model before calling addInput on it since add Input
            //keep tracks of storing root join information, so join needs to have
            //ad id which is set if we add the join first
            ArrayList<SQLJoinOperator> newJoin = new ArrayList<SQLJoinOperator>();
            newJoin.add(join);
            addObjects(newJoin, model);

            SQLCondition joinCondition = join.getJoinCondition();

            if (previousJoin == null) {
                join.addInput(SQLJoinOperator.LEFT, (SQLJoinTable) tablesSoFar.get(0));
            } else {
                join.addInput(SQLJoinOperator.LEFT, previousJoin);
            }
            join.addInput(SQLJoinOperator.RIGHT, jTable);

            previousJoin = join;

            //two or more table join so need to build composite condition
            if (joinList.size() > 0) {
                Iterator it1 = joinList.iterator();
                SQLPredicate previousPredicate = null;

                if (it1.hasNext()) {
                    SQLJoinOperator joinNew = (SQLJoinOperator) it1.next();
                    SQLCondition joinNewCondition = joinNew.getJoinCondition();
                    previousPredicate = joinNewCondition.getRootPredicate();
                }

                while (it1.hasNext()) {
                    SQLJoinOperator joinNew = (SQLJoinOperator) it1.next();
                    SQLCondition joinNewCondition = joinNew.getJoinCondition();
                    SQLPredicate predicate = joinNewCondition.getRootPredicate();

                    VisibleSQLPredicateImpl newPredicate = new VisibleSQLPredicateImpl();
                    newPredicate.setOperatorType("and");
                    newPredicate.addInput(SQLPredicate.LEFT, previousPredicate);
                    newPredicate.addInput(SQLPredicate.RIGHT, predicate);
                    previousPredicate = newPredicate;
                }
                SQLObjectUtil.migrateJoinCondition(previousPredicate, joinCondition);
                //  Set condition state from unknown to Graphical.
                joinCondition.setGuiMode(SQLCondition.GUIMODE_GRAPHICAL);
                joinCondition.getRootPredicate();

                join.setJoinConditionType(SQLJoinOperator.SYSTEM_DEFINED_CONDITION);
            }
        }

        addObjects(allObjectList, model);
    }

    public static void handleAutoJoins(List joinTables, SQLUIModel model) throws EDMException {
        //this list keep track of all joins created so far.
        ArrayList<SQLObject> allObjectList = new ArrayList<SQLObject>();

        ArrayList<SQLObject> tablesSoFar = new ArrayList<SQLObject>();

        //then add all the joinTables
        Iterator it = joinTables.iterator();
        if (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            tablesSoFar.add(obj);
            allObjectList.add(obj);
        }

        SQLJoinOperator previousJoin = null;

        while (it.hasNext()) {
            SQLObject obj = (SQLObject) it.next();
            allObjectList.add(obj);

            //now find all the auto joins between obj and rest of the joinTables tables
            //and add them as well
            List joinList = SQLObjectUtil.getAutoJoins((SQLJoinTable) obj, tablesSoFar);
            //create a SQLJoinOperator
            SQLJoinOperator join = SQLModelObjectFactory.getInstance().createSQLJoinOperator();
            //add join to model before calling addInput on it since add Input
            //keep tracks of storing root join information, so join needs to have
            //ad id which is set if we add the join first
            ArrayList<SQLJoinOperator> newJoin = new ArrayList<SQLJoinOperator>();
            newJoin.add(join);
            addObjects(newJoin, model);

            SQLCondition joinCondition = join.getJoinCondition();

            if (previousJoin == null) {
                join.addInput(SQLJoinOperator.LEFT, (SQLJoinTable) tablesSoFar.get(0));
            } else {
                join.addInput(SQLJoinOperator.LEFT, previousJoin);
            }
            join.addInput(SQLJoinOperator.RIGHT, obj);

            previousJoin = join;

            //two or more table join so need to build composite condition
            if (joinList.size() > 0) {
                Iterator it1 = joinList.iterator();
                SQLPredicate previousPredicate = null;

                if (it1.hasNext()) {
                    SQLJoinOperator joinNew = (SQLJoinOperator) it1.next();
                    SQLCondition joinNewCondition = joinNew.getJoinCondition();
                    previousPredicate = joinNewCondition.getRootPredicate();
                }

                while (it1.hasNext()) {
                    SQLJoinOperator joinNew = (SQLJoinOperator) it1.next();
                    SQLCondition joinNewCondition = joinNew.getJoinCondition();
                    SQLPredicate predicate = joinNewCondition.getRootPredicate();
                    VisibleSQLPredicateImpl newPredicate = new VisibleSQLPredicateImpl();
                    newPredicate.setOperatorType("and");
                    newPredicate.addInput(SQLPredicate.LEFT, previousPredicate);
                    newPredicate.addInput(SQLPredicate.RIGHT, predicate);
                    previousPredicate = newPredicate;
                }
                SQLObjectUtil.migrateJoinCondition(previousPredicate, joinCondition);
                join.setJoinConditionType(SQLJoinOperator.SYSTEM_DEFINED_CONDITION);
            }

            tablesSoFar.add(obj);
        }

        addObjects(allObjectList, model);
    }

    private static void addObjects(List joinList, SQLUIModel model) throws EDMException {
        Iterator it = joinList.iterator();
        while (it.hasNext()) {
            SQLObject join = (SQLObject) it.next();
            model.addSQLObject(join);
        }
    }

    public static List<DBTable> getJoinSourceTables(SQLJoinOperator op) {
        ArrayList<DBTable> tables = new ArrayList<DBTable>();

        SQLInputObject leftInObj = op.getInput(SQLJoinOperator.LEFT);
        SQLObject leftObj = leftInObj.getSQLObject();

        if (leftObj != null) {
            if (leftObj.getObjectType() == SQLConstants.JOIN) {
                tables.addAll(getJoinSourceTables((SQLJoinOperator) leftObj));
            } else {
                SQLJoinTable jTable = (SQLJoinTable) leftObj;
                SourceTable sTable = jTable.getSourceTable();
                tables.add(sTable);
            }
        }

        SQLInputObject rightInObj = op.getInput(SQLJoinOperator.RIGHT);
        SQLObject rightObj = rightInObj.getSQLObject();

        if (rightObj != null) {
            if (rightObj.getObjectType() == SQLConstants.JOIN) {
                tables.addAll(getJoinSourceTables((SQLJoinOperator) rightObj));
            } else {
                SQLJoinTable jTable = (SQLJoinTable) rightObj;
                tables.add(jTable.getSourceTable());
            }
        }

        return tables;
    }

    public static List<SQLJoinTable> getJoinTables(SQLJoinOperator op) {
        ArrayList<SQLJoinTable> tables = new ArrayList<SQLJoinTable>();

        SQLInputObject leftInObj = op.getInput(SQLJoinOperator.LEFT);
        SQLObject leftObj = leftInObj.getSQLObject();

        if (leftObj != null) {
            if (leftObj.getObjectType() == SQLConstants.JOIN) {
                tables.addAll(getJoinTables((SQLJoinOperator) leftObj));
            } else {
                SQLJoinTable jTable = (SQLJoinTable) leftObj;
                tables.add(jTable);
            }
        }

        SQLInputObject rightInObj = op.getInput(SQLJoinOperator.RIGHT);
        SQLObject rightObj = rightInObj.getSQLObject();

        if (rightObj != null) {
            if (rightObj.getObjectType() == SQLConstants.JOIN) {
                tables.addAll(getJoinTables((SQLJoinOperator) rightObj));
            } else {
                SQLJoinTable jTable = (SQLJoinTable) rightObj;
                tables.add(jTable);
            }
        }

        return tables;
    }
}
