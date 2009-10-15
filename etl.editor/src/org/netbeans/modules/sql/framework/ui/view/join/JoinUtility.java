/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.ui.view.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.impl.VisibleSQLPredicateImpl;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.graph.IGraphLink;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.JoinBuilderSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.TableColumnNode;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLGraphView;
import com.sun.sql.framework.exception.BaseException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * @author radval
 */
public class JoinUtility {

    private static final String LOG_CATEGORY = JoinUtility.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(JoinUtility.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /** Creates a new instance of JoinUtility */
    private JoinUtility() {
    }

    public static void editJoinView(SQLJoinView oldJView, SQLJoinView modifiedJoinView, List joinSources, List tableNodes, IGraphView gView) throws BaseException {
        //first remove tables which are no longer in modified join
        removeOldTables(oldJView, modifiedJoinView, gView);
        //then refresh join graph view in the canvas, remove columns which are unchecked
        //add columns which newly checked
        adjustColumnVisiblity(oldJView, modifiedJoinView, joinSources, tableNodes, gView);
        //then remove all old objects and all all modified objects
        if (modifiedJoinView != null) {
            oldJView.removeAllObjects();
            oldJView.getAllObjects().addAll(modifiedJoinView.getAllObjects());
        }
    }

    private static void removeOldTables(SQLJoinView oldJView, SQLJoinView modifiedJoinView, IGraphView gView) throws BaseException {
        List newTables = modifiedJoinView.getSourceTables();
        JoinViewGraphNode jGraphView = (JoinViewGraphNode) gView.findGraphNode(oldJView);

        Iterator it = oldJView.getSourceTables().iterator();

        while (it.hasNext()) {
            SourceTable sTable = (SourceTable) it.next();
            if (!newTables.contains(sTable)) {
                if (jGraphView != null) {
                    jGraphView.removeJoinTable(sTable);
                }
            }
        }
    }

    private static void adjustColumnVisiblity(SQLJoinView oldJoinView, SQLJoinView modifiedJoinView, List joinSources, List tableNodes, IGraphView gView) throws BaseException {
        JoinViewGraphNode jGraphView = (JoinViewGraphNode) gView.findGraphNode(oldJoinView);
        CollabSQLUIModel sqlModel = (CollabSQLUIModel) gView.getGraphModel();
        if (!oldJoinView.equals(modifiedJoinView)) {
            sqlModel.setDirty(true);
        }

        if (jGraphView == null) {
            return;
        }

        try {
            Iterator it = joinSources.iterator();

            while (it.hasNext()) {
                SourceTable sTable = (SourceTable) it.next();
                sTable.setUsedInJoin(true);
                ArrayList<SQLDBColumn> invisibleColumns = new ArrayList<SQLDBColumn>();

                List columns = sTable.getColumnList();
                Iterator cIt = columns.iterator();

                while (cIt.hasNext()) {
                    SQLDBColumn column = (SQLDBColumn) cIt.next();
                    //if coolumn was visible earlier and now became invisible then we
                    // need to remove that
                    //column
                    if (column.isVisible() && !isColumnVisible(column, tableNodes)) {
                        try {
                            //make sure first set visible property to false
                            column.setVisible(false);
                            if (!jGraphView.containsTable(sTable)) {
                                invisibleColumns.add(column);
                            } else {
                                //then call remove
                                jGraphView.removeColumn(column);
                            }
                        } catch (BaseException ex) {

                            mLogger.errorNoloc(mLoc.t("EDIT186: cannot remove column {0}from joinview table.", column.getName()), ex);
                            throw ex;
                        }
                    //user selected a column to become visible on canvas
                    } else if (!column.isVisible() && isColumnVisible(column, tableNodes)) {
                        jGraphView.addColumn(column);
                        column.setVisible(true);
                    }
                }

                //if its a new table selected using more table dialog then
                //we need to add this to sql model
                if (!sqlModel.exists(sTable)) {
                    sTable.setUsedInJoin(true);
                    sqlModel.addObject(sTable);
                }

                //is table newly added to join view
                if (!jGraphView.containsTable(sTable)) {
                    //first remove table which is now part of join
                    SQLBasicTableArea node = (SQLBasicTableArea) gView.findGraphNode(sTable);
                    List linkInfos = new ArrayList();
                    if (node != null) {
                        //now remove dangling ref to invisible columns
                        it = invisibleColumns.iterator();
                        while (it.hasNext()) {
                            SQLDBColumn column = (SQLDBColumn) it.next();
                            if (node != null) {
                                node.removeColumnReference(column);
                            }
                        }

                        //record old links
                        linkInfos = transferTableLinksToJoinView(node.getAllLinks());
                        gView.removeNode(node);
                    }
                    //then add table to join view
                    jGraphView.addTable(sTable);
                    //create old links again
                    createOldLinksInJoinView(linkInfos, gView);
                }
            }

            jGraphView.setSize(jGraphView.getMaximumWidth(), jGraphView.getMaximumHeight());
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT187: can not adjust column visibility for joinview.{0}", LOG_CATEGORY), ex);

            throw ex;
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

    public static void handleNewJoinCreation(SQLJoinView newJoin, List tableNodes, IGraphView gView) throws BaseException {
        CollabSQLUIModel sqlModel = (CollabSQLUIModel) gView.getGraphModel();
        ArrayList<LinkInfo> allOldLinks = new ArrayList<LinkInfo>();

        Iterator tIt = newJoin.getSourceTables().iterator();
        while (tIt.hasNext()) {
            SourceTable sTable = (SourceTable) tIt.next();
            sTable.setUsedInJoin(true);
            //if its a new table selected using more table dialog then
            //we need to add this to sql model
            if (!sqlModel.exists(sTable)) {
                sqlModel.addObject(sTable);
            }

            SQLBasicTableArea node = (SQLBasicTableArea) gView.findGraphNode(sTable);
            List<LinkInfo> linkInfos = new ArrayList<LinkInfo>();
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

                //now remove dangling ref to invisible columns, if a canvas table is now
                //becoming part of join view
                if (node != null && !column.isVisible()) {
                    node.removeColumnReference(column);
                }
            }

            //record old links
            if (node != null) {
                linkInfos = transferTableLinksToJoinView(node.getAllLinks());
                allOldLinks.addAll(linkInfos);

                //remove table node as it is now part of join view
                gView.removeNode(node);
            }
        }
        //create join view in the canvas
        sqlModel.addObject(newJoin);

        //create old links again
        createOldLinksInJoinView(allOldLinks, gView);
        sqlModel.setDirty(true);
    }

    private static List<LinkInfo> transferTableLinksToJoinView(List links) {
        ArrayList<LinkInfo> linkInfos = new ArrayList<LinkInfo>();

        IGraphNode srcGraphNode = null;
        IGraphNode destGraphNode = null;

        Iterator it = links.iterator();

        while (it.hasNext()) {
            IGraphLink link = (IGraphLink) it.next();

            IGraphPort from = link.getFromGraphPort();
            IGraphPort to = link.getToGraphPort();

            srcGraphNode = from.getDataNode();
            destGraphNode = to.getDataNode();
            String sParam = srcGraphNode.getFieldName(from);
            String dParam = destGraphNode.getFieldName(to);

            SQLCanvasObject srcObj = (SQLCanvasObject) srcGraphNode.getDataObject();
            SQLConnectableObject destObj = (SQLConnectableObject) destGraphNode.getDataObject();
            linkInfos.add(new LinkInfo(srcObj, destObj, sParam, dParam));
        }

        return linkInfos;
    }

    private static void createOldLinksInJoinView(List linkInfos, IGraphView graphView) {
        Iterator it = linkInfos.iterator();
        while (it.hasNext()) {
            LinkInfo lInfo = (LinkInfo) it.next();

            String sParam = lInfo.getSourceParam();
            String dParam = lInfo.getTargetParam();
            SQLCanvasObject srcObj = lInfo.getSource();
            SQLConnectableObject destObj = lInfo.getTarget();

            ((SQLGraphView) graphView).createLink(srcObj, destObj, sParam, dParam);
        }
    }

    public static void handleAutoJoins(SQLJoinTable jTable, boolean addTable, JoinBuilderSQLUIModel model) throws BaseException {
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

    public static void handleAutoJoins(List joinTables, SQLUIModel model) throws BaseException {
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

    private static void addObjects(List joinList, SQLUIModel model) throws BaseException {
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
