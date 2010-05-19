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
package org.netbeans.modules.sql.framework.ui.model.impl;

import java.awt.Point;
import java.io.StringReader;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeOutput;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLFilter;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.model.VisibleSQLPredicate;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.event.SQLDataEvent;
import org.netbeans.modules.sql.framework.ui.event.SQLLinkEvent;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import net.java.hulp.i18n.Logger;
import com.sun.etl.exception.BaseException;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.event.UndoableEditListener;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;

/**
 * Concrete implementation of SQLBuilderModel for use in representing SQL object models.
 * 
 * @author Ritesh Adval
 */
public class CollabSQLUIModelImpl extends AbstractSQLModel implements CollabSQLUIModel {

    private static final String LOG_CATEGORY = CollabSQLUIModelImpl.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(CollabSQLUIModelImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    protected boolean isReloaded = false;
    protected boolean restoring = false;
    protected SQLDefinition sqlDefinition;

    public CollabSQLUIModelImpl() {
        super();
    }

    // New
    public CollabSQLUIModelImpl(String collaborationName) { // throws BaseException {

        this();
        this.sqlDefinition = SQLModelObjectFactory.getInstance().createSQLDefinition(collaborationName);
        sqlDefinition.addSQLObjectListener(this);
        this.isReloaded = false;

    }

    public void addObject(SQLObject sqlObject) throws BaseException {
        //Have to add it first so that it reflects when you switch from the Source and Design View in the first time 
        //sqlDefinition.addObject(sqlObject);
        super.addObject(sqlObject);
        addObjectIgnoreUndo(sqlObject);
    }

    public void addObjectIgnoreUndo(SQLObject sqlObject) throws BaseException {
        if (sqlObject.getObjectType() == SQLConstants.TARGET_DBMODEL) {
            addTargetTableRuntimeArg((SQLDBModel) sqlObject);
        }

        addSQLObject(sqlObject);
        if (sqlObject.getObjectType() == SQLConstants.VISIBLE_PREDICATE) {
            createVisiblePredicateRefObj((VisibleSQLPredicate) sqlObject);
        }
        // first time when an source table is added we want to handle auto join
        addObjectInGraph(sqlObject, true);
    }

    /**
     * Adds a SourceTableImpl instance using the given DBTable instance as a template, if
     * it does not already exist.
     * 
     * @param srcTable DBTable to serve as template for the new SourceTableImpl instance.
     * @return new SourceTableImpl instance
     * @throws BaseException if error occurs during creation
     */
    public SQLObject addSourceTable(DBTable srcTable, Point loc) throws BaseException {
        SourceTable impl = (SourceTable) sqlDefinition.createObject(SQLConstants.STR_SOURCE_TABLE);
        impl.copyFrom(srcTable);

        if (loc != null) {
            impl.getGUIInfo().setAttribute(GUIInfo.ATTR_X, new Integer(loc.x));
            impl.getGUIInfo().setAttribute(GUIInfo.ATTR_Y, new Integer(loc.y));
        }

        addObject(impl);
        return impl;
    }

    public void addSQLObject(SQLObject sqlObject) throws BaseException {
        sqlDefinition.addObject(sqlObject);
        isDirty = true;
    }

    /**
     * Adds a TargetTableImpl instance using the given DBTable instance as a template, if
     * it does not already exist.
     * 
     * @param targetTable DBTable to serve as template for the new TargetTableImpl
     *        instance.
     * @return SourceTableImpl representing the contents of the given template object; may
     *         be a pre-existing object.
     * @throws BaseException if error occurs during creation
     */
    public SQLObject addTargetTable(DBTable targetTable, Point loc) throws BaseException {
        TargetTable impl = (TargetTable) sqlDefinition.createObject(SQLConstants.STR_TARGET_TABLE);
        impl.copyFrom(targetTable);

        if (loc != null) {
            impl.getGUIInfo().setAttribute(GUIInfo.ATTR_X, new Integer(loc.x));
            impl.getGUIInfo().setAttribute(GUIInfo.ATTR_Y, new Integer(loc.y));
        }

        addObject(impl);

        addTargetTableRuntimeArg(impl);

        return impl;
    }

    public SQLCanvasObject createObject(String className) throws BaseException {
        SQLObject sqlObj = SQLObjectFactory.createSQLObject(className);
        return (SQLCanvasObject) sqlObj;
    }

    /**
     * Indicates whether the table represented by the given DBTable already exists in this
     * model
     * 
     * @param table DBTable whose existence is to be tested
     * @return true if table (source or target) exists in the model, false otherwise
     */
    public boolean exists(DBTable table) {
        List existingTables = Collections.EMPTY_LIST;
        boolean doesExist = false;

        if (table != null) {
            if (table instanceof SourceTable) {
                existingTables = getSQLDefinition().getSourceTables();
            } else if (table instanceof TargetTable) {
                existingTables = getSQLDefinition().getTargetTables();
            }

            Iterator it = existingTables.iterator();
            while (it.hasNext()) {
                DBTable existing = (DBTable) it.next();
                if (existing.toString().equals(table.toString())) {
                    doesExist = true;
                    break;
                }
            }
        }

        return doesExist;
    }

    public SQLJoinView getJoinView(SourceTable sTable) {
        Collection joinViews = this.getSQLDefinition().getObjectsOfType(SQLConstants.JOIN_VIEW);
        Iterator it = joinViews.iterator();

        while (it.hasNext()) {
            SQLJoinView joinView = (SQLJoinView) it.next();
            if (joinView.containsSourceTable(sTable)) {
                return joinView;
            }
        }

        return null;
    }

    public RuntimeDatabaseModel getRuntimeDbModel() {
        return sqlDefinition.getRuntimeDbModel();
    }

    public SQLDefinition getSQLDefinition() {
        return this.sqlDefinition;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isReloaded() {
        return this.isReloaded;
    }

    // reload
    public void reLoad(String sqlDefinitionXml) throws BaseException {
        // clear the listener
        if (this.sqlDefinition != null) {
            this.sqlDefinition.removeSQLObjectListener(this);
        }

        this.sqlDefinition = SQLModelObjectFactory.getInstance().createSQLDefinition();
        // register this as listener after this only parse it
        sqlDefinition.addSQLObjectListener(this);
        // then parse it
        sqlDefinition.parseXML(XmlUtil.loadXMLFile(new StringReader(sqlDefinitionXml)));
        this.isReloaded = true;

        // when reloaded clear the listener list
        listeners.clear();
    }

    public void removeDanglingColumnReference(SourceColumn column) throws BaseException {
        removeDanglingColumnRefFromSourceTables(column);
        removeDanglingColumnRefFromTargetTables(column);
    }

    public void removeObject(SQLObject sqlObject) throws BaseException {
        super.removeObject(sqlObject);
        removeObjectIgnoreUndo(sqlObject);
    }

    public void removeObjectIgnoreUndo(SQLObject sqlObject) throws BaseException {
        if (sqlObject.getObjectType() == SQLConstants.TARGET_TABLE) {
            removeTargetTableRuntimeArg((TargetTable) sqlObject);
        }

        sqlDefinition.removeObject(sqlObject);

        // delete dangling references
        removeDanglingReferences(sqlObject);

        if (sqlObject instanceof SQLCanvasObject) {
            SQLDataEvent evt = new SQLDataEvent(this, (SQLCanvasObject) sqlObject);
            fireSQLDataDeletionEvent(evt);
            isDirty = true;
        }
    }

    public void restoreLinks() {
        // link all tables
        List list = sqlDefinition.getTargetTables();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            SQLConnectableObject sqlExObj = (SQLConnectableObject) it.next();
            restoreLinks(sqlExObj);
        }

        // restore link of runtimeoutput
        RuntimeDatabaseModel runtimeModel = sqlDefinition.getRuntimeDbModel();
        if (runtimeModel != null) {
            RuntimeOutput runtimeOutput = runtimeModel.getRuntimeOutput();
            if (runtimeOutput != null) {
                restoreLinks(runtimeOutput);
            }
        }

        // now link other objects
        Collection objectC = sqlDefinition.getAllObjects();
        it = objectC.iterator();
        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            if (sqlObj instanceof SQLConnectableObject) {
                restoreLinks((SQLConnectableObject) sqlObj);
            }
        }
        isDirty = false;
    }

    public void restoreObjects() throws BaseException {
        Collection col = sqlDefinition.getAllObjects();
        Iterator it = col.iterator();

        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            try {
                // reload time we do not want to handle auto join
                addObjectInGraph(sqlObj, false);
            } catch (BaseException e) {
                mLogger.errorNoloc(mLoc.t("EDIT141: Error caught while restoring object ({0})", sqlObj.getDisplayName()), e);
                throw e;
            }
        }
        isDirty = false;
    }

    /**
     * Rebuilds view model based on object pool and SQLDefinition hierarchy.
     */
    public synchronized void restoreUIState() throws BaseException {
        try {
            restoring = true;
            restoreObjects();
            restoreLinks();
        } finally {
            restoring = false;
        }
    }

    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    public void setReloaded(boolean reloaded) {
        this.isReloaded = reloaded;
    }

    public void setSQLDefinition(SQLDefinition sqlDefinition) {
        this.sqlDefinition = sqlDefinition;
    }

    /**
     * set it to true if a java operator is used in the model
     * 
     * @param javaOp true if there is a java operator
     */
    void setContainsJavaOperators(boolean containsjavaOp) {
        this.sqlDefinition.setContainsJavaOperators(containsjavaOp);
    }

    protected void addObjectInGraph(SQLObject sqlObject, boolean handleAutojoin) throws BaseException {
        if (sqlObject instanceof SQLCanvasObject) {
            SQLDataEvent evt = new SQLDataEvent(this, (SQLCanvasObject) sqlObject);
            fireSQLDataCreationEvent(evt);
            isDirty = true;
        } else if (sqlObject.getObjectType() == SQLConstants.SOURCE_DBMODEL || sqlObject.getObjectType() == SQLConstants.TARGET_DBMODEL || sqlObject.getObjectType() == SQLConstants.RUNTIME_DBMODEL) {
            createTablesInGraph((SQLDBModel) sqlObject);
        }
    }

    protected void addTargetTableRuntimeArg(TargetTable targetTable) {
        RuntimeDatabaseModel runDbModel = this.getRuntimeDbModel();
        RuntimeOutput rOutput = null;
        if (runDbModel != null) {
            rOutput = runDbModel.getRuntimeOutput();
        }

        // If runtime output is null then it is not created in graph (it is added in
        // graph only via TablePanel) so do not create target table runtime args, they
        // will be added from TablePanel.
        if (rOutput == null) {
            return;
        }

        // add Count_<Target_Table_Name> runtime output argument
        String argName = SQLObjectUtil.getTargetTableCountRuntimeOutput(targetTable);
        DBColumn column = rOutput.getColumn(argName);
        if (column == null) {
            TargetColumn tColumn = SQLModelObjectFactory.getInstance().createTargetColumn(argName, Types.NUMERIC, 0, 0, true);

            rOutput.addColumn(tColumn);

            // fire update event
            SQLDataEvent evt = new SQLDataEvent(this, rOutput, tColumn);
            fireChildObjectCreatedEvent(evt);
        }
    }

    protected SQLCanvasObject getTopSQLCanvasObject(SQLObject sqlObj) {
        if (sqlObj instanceof SQLCanvasObject) {
            return (SQLCanvasObject) sqlObj;
        }

        Object parentObj = sqlObj.getParentObject();
        while (parentObj != null && parentObj instanceof SQLObject && !(parentObj instanceof SQLCanvasObject)) {
            parentObj = ((SQLObject) parentObj).getParentObject();
        }

        if (parentObj instanceof SQLCanvasObject) {
            return (SQLCanvasObject) parentObj;
        }

        return null;
    }

    protected void restoreLinks(SQLConnectableObject sqlExObj) {
        if (sqlExObj instanceof SQLFilter) {
            return;
        }

        Map inputMap = sqlExObj.getInputObjectMap();
        Iterator it = inputMap.keySet().iterator();

        while (it.hasNext()) {
            String argName = (String) it.next();
            SQLInputObject inputObj = (SQLInputObject) inputMap.get(argName);
            if (inputObj == null) {
                continue;
            }

            SQLObject srcObj = inputObj.getSQLObject();
            String srcFieldName = null;

            if (srcObj == null) {
                continue;
            }

            // TODO: Temporary check for instance of SQLLiteralImpl(but allow
            // VisibleSQLLiteral)
            // we need to fix this with Source and Target Column as SQLCanvasObject
            // and no need to get top canvas object.
            // if an input object is SQLCanvasObject just create SQLLinkEvent and
            // link it. no need to pass srcFieldName and argName

            if (!(srcObj instanceof VisibleSQLLiteral) && srcObj instanceof SQLLiteral) {
                continue;
            }

            srcFieldName = srcObj.getDisplayName();

            srcObj = getTopSQLCanvasObject(srcObj);
            if (srcObj instanceof SQLCanvasObject) {

                SQLLinkEvent evt = new SQLLinkEvent(this, (SQLCanvasObject) srcObj, sqlExObj, srcFieldName, argName);
                fireSQLLinkCreationEvent(evt);

                if (srcObj instanceof SQLConnectableObject) {
                    restoreLinks((SQLConnectableObject) srcObj);
                }
            }
        }

        // now restore child sql object links
        List children = sqlExObj.getChildSQLObjects();
        it = children.iterator();
        while (it.hasNext()) {
            SQLObject childObj = (SQLObject) it.next();
            if (childObj instanceof SQLConnectableObject) {
                restoreLinks((SQLConnectableObject) childObj);
            }
        }
    }

    private void addTargetTableRuntimeArg(SQLDBModel targetDbModel) {
        Iterator it = targetDbModel.getTables().iterator();
        while (it.hasNext()) {
            TargetTable tt = (TargetTable) it.next();
            addTargetTableRuntimeArg(tt);
        }
    }

    private void createTablesInGraph(SQLDBModel dbModel) throws BaseException {
        Iterator it = dbModel.getTables().iterator();

        while (it.hasNext()) {
            try {
                addObjectInGraph((SQLDBTable) it.next(), false);
            } catch (BaseException e) {
                mLogger.errorNoloc(mLoc.t("EDIT143: Error while adding table to canvas. ({0})", LOG_CATEGORY), e);
                throw e;
            }
        }
    }

    private void removeDanglingColumnReference(SourceTable table) throws BaseException {
        List colList = table.getColumnList();
        Iterator it = colList.iterator();

        while (it.hasNext()) {
            SourceColumn column = (SourceColumn) it.next();
            removeDanglingColumnReference(column);
        }
    }

    private void removeDanglingColumnRefFromSourceTables(SourceColumn column) throws BaseException {
        List sTables = sqlDefinition.getSourceTables();
        Iterator it = sTables.iterator();

        while (it.hasNext()) {
            SourceTable sTable = (SourceTable) it.next();
            // remove from extraction condition
            SQLCondition cond = sTable.getExtractionCondition();
            if (cond != null) {
                cond.removeDanglingColumnRef(column);
            }
        }
    }

    private void removeDanglingColumnRefFromTargetTables(SourceColumn column) throws BaseException {
        List sTables = sqlDefinition.getTargetTables();
        Iterator it = sTables.iterator();

        while (it.hasNext()) {
            TargetTable sTable = (TargetTable) it.next();
            SQLCondition cond = sTable.getJoinCondition();
            if (cond != null) {
                cond.removeDanglingColumnRef(column);
            }

            SQLCondition fcond = sTable.getFilterCondition();
            if (fcond != null) {
                fcond.removeDanglingColumnRef(column);
            }
        }
    }

    private void removeDanglingReferences(SQLObject sqlObject) throws BaseException {
        int objType = sqlObject.getObjectType();
        switch (objType) {
            case SQLConstants.VISIBLE_PREDICATE:
                removeVisiblePredicateRefObj((VisibleSQLPredicate) sqlObject);
                break;
            case SQLConstants.SOURCE_TABLE:
            case SQLConstants.RUNTIME_INPUT:
                removeDanglingColumnReference((SourceTable) sqlObject);
                break;

            case SQLConstants.JOIN_VIEW:
                SQLJoinView joinView = (SQLJoinView) sqlObject;
                Iterator it = joinView.getSourceTables().iterator();
                while (it.hasNext()) {
                    SQLObject sTable = (SQLObject) it.next();
                    removeDanglingReferences(sTable);
                }
                break;
        }
    }

    private void removeTargetTableRuntimeArg(TargetTable targetTable) {
        // remove Count_<Target_Table_Name> runtime output argument
        String argName = SQLObjectUtil.getTargetTableCountRuntimeOutput(targetTable);

        RuntimeDatabaseModel runDbModel = this.getRuntimeDbModel();
        RuntimeOutput rOutput = null;
        if (runDbModel != null) {
            rOutput = runDbModel.getRuntimeOutput();
        }
        if (rOutput == null) {
            return;
        }

        DBColumn column = rOutput.getColumn(argName);
        if (column != null) {
            ((SQLDBTable) rOutput).deleteColumn(argName);

            // fire update event
            SQLDataEvent evt = new SQLDataEvent(this, rOutput, (SQLObject) column);
            fireChildObjectDeletedEvent(evt);
        }
    }

    private void removeVisiblePredicateRefObj(VisibleSQLPredicate predicate) throws BaseException {
        String newOperator = predicate.getOperatorType();
        // FIXME: this check is very weak and may fail if string changes
        // migrate this change
        if (newOperator.equalsIgnoreCase("IS") || newOperator.equalsIgnoreCase("IS NOT")) {
            SQLLiteral nullLiteral = (SQLLiteral) predicate.getSQLObject(SQLPredicate.RIGHT);
            if (nullLiteral != null) {
                this.removeObject(nullLiteral);
            }
        }
    }

    public void removeComponentListener(ComponentListener cl) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addComponentListener(ComponentListener cl) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeUndoableEditListener(UndoableEditListener uel) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addUndoableEditListener(UndoableEditListener uel) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeUndoableRefactorListener(UndoableEditListener uel) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addUndoableRefactorListener(UndoableEditListener uel) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sync() throws IOException {
        //  throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean inSync() {
        return true;
    //  throw new UnsupportedOperationException("Not supported yet.");
    }

    public State getState() {
        return null;
    //  throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isIntransaction() {
        return true;
    // throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean startTransaction() {
        return true;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void endTransaction() {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addChildComponent(Component target, Component child, int index) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeChildComponent(Component child) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public ModelSource getModelSource() {
        return null;
    // throw new UnsupportedOperationException("Not supported yet.");
    }
}
