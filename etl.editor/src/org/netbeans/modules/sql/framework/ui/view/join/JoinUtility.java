/*
 * JoinUtility.java
 *
 * Created on February 2, 2004, 11:27 PM
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
import com.sun.sql.framework.utils.Logger;

/**
 * @author radval
 */
public class JoinUtility {

    private static final String LOG_CATEGORY = JoinUtility.class.getName();

    /** Creates a new instance of JoinUtility */
    private JoinUtility() {
    }

    public static void editJoinView(SQLJoinView oldJView, SQLJoinView modifiedJoinView, List joinSources, List tableNodes, IGraphView gView)
            throws BaseException {
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
        if(!oldJoinView.equals(modifiedJoinView)) {
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
                ArrayList invisibleColumns = new ArrayList();

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
                            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "adjustColumnVisiblity", "can not remove column " + column.getName()
                                + " from joinview table.", ex);

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
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "adjustColumnVisiblity", "can not adjust column visibility for joinview. ", ex);
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
        ArrayList allOldLinks = new ArrayList();

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
            List linkInfos = new ArrayList();
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

    private static List transferTableLinksToJoinView(List links) {
        ArrayList linkInfos = new ArrayList();

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
        ArrayList allObjectList = new ArrayList();
        if (addTable) {
            allObjectList.add(jTable);
        }

        ArrayList tablesSoFar = new ArrayList();

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
            ArrayList newJoin = new ArrayList();
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
        ArrayList allObjectList = new ArrayList();

        ArrayList tablesSoFar = new ArrayList();

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
            ArrayList newJoin = new ArrayList();
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

    public static List getJoinSourceTables(SQLJoinOperator op) {
        ArrayList tables = new ArrayList();

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

    public static List getJoinTables(SQLJoinOperator op) {
        ArrayList tables = new ArrayList();

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

