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

package org.netbeans.modules.sql.framework.ui.model.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectFactory;
import org.netbeans.modules.sql.framework.model.VisibleSQLPredicate;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.event.SQLDataEvent;
import org.netbeans.modules.sql.framework.ui.event.SQLLinkEvent;
import org.netbeans.modules.sql.framework.ui.model.ConditionBuilderSQLUiModel;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;

/**
 * @author radval
 */
public class ConditionBuilderSQLUIModelImpl extends AbstractSQLModel implements ConditionBuilderSQLUiModel  {
    private static final String LOG_CATEGORY = ConditionBuilderSQLUIModelImpl.class.getName();

    private SQLCondition sqlCondition;

    /** Creates a new instance of ConditionBuilderSQLUIModelImpl */
    public ConditionBuilderSQLUIModelImpl() {
        super();
    }

    public ConditionBuilderSQLUIModelImpl(SQLCondition sqlCondition) {
        this();
        this.sqlCondition = sqlCondition;
    }

    public void addObject(SQLObject sqlObject) throws BaseException {
        super.addObject(sqlObject);
        addObjectIgnoreUndo(sqlObject);
    }

    public void addObjectIgnoreUndo(SQLObject sqlObject) throws BaseException {
        addSQLObject(sqlObject);
        if (sqlObject.getObjectType() == SQLConstants.VISIBLE_PREDICATE) {
            createVisiblePredicateRefObj((VisibleSQLPredicate) sqlObject);
        }

        // first time when an source table is added we want to handle auto join
        addObjectInGraph(sqlObject, true);
    }

    public void addSQLObject(SQLObject sqlObject) throws BaseException {
        sqlCondition.addObject(sqlObject);
        isDirty = true;
    }

    public SQLCanvasObject createObject(String className) throws BaseException {
        SQLObject sqlObj = SQLObjectFactory.createSQLObject(className);
        return (SQLCanvasObject) sqlObj;
    }

    public SQLCondition getSQLCondition() {
        return this.sqlCondition;
    }

    public void removeObject(SQLObject sqlObject) throws BaseException {
        super.removeObject(sqlObject);
        removeObjectIgnoreUndo(sqlObject);
    }

    public void removeObjectIgnoreUndo(SQLObject sqlObject) throws BaseException {
        sqlCondition.removeObject(sqlObject);

        if (sqlObject instanceof SQLCanvasObject) {
            SQLDataEvent evt = new SQLDataEvent(this, (SQLCanvasObject) sqlObject);
            fireSQLDataDeletionEvent(evt);
            isDirty = true;
        }
    }

    public void restoreLinks() {
        // Now link other objects
        Collection objectC = sqlCondition.getAllObjects();
        Iterator it = objectC.iterator();
        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            if (sqlObj instanceof SQLConnectableObject) {
                restoreLinks((SQLConnectableObject) sqlObj);
            }
        }

        isDirty = false;
    }

    /**
     * Rebuilds view model based on object pool and SQLDefinition hierarchy.
     */
    public synchronized void restoreUIState() {
        restoreObjects();
        restoreLinks();
    }

    public void setSQLCondition(SQLCondition cond) {
        this.sqlCondition = cond;
    }

    /**
     * set it to true if a java operator is used in the model
     * 
     * @param javaOp true if there is a java operator
     */
    void setContainsJavaOperators(boolean containsjavaOp) {
        this.sqlCondition.setContainsJavaOperators(containsjavaOp);
    }

    protected void addObjectInGraph(SQLObject sqlObject, boolean handleAutojoin) throws BaseException {
        if (sqlObject instanceof SQLCanvasObject) {
            SQLDataEvent evt = new SQLDataEvent(this, (SQLCanvasObject) sqlObject);
            fireSQLDataCreationEvent(evt);
            isDirty = true;
        }
    }

    protected void restoreLinks(SQLConnectableObject sqlExObj) {
        Map inputMap = sqlExObj.getInputObjectMap();
        Iterator it = inputMap.keySet().iterator();

        while (it.hasNext()) {
            String argName = (String) it.next();
            SQLInputObject inputObj = (SQLInputObject) inputMap.get(argName);
            SQLObject srcObj = inputObj.getSQLObject();
            String srcFieldName = null;

            // Do not link internal literals.
            if (sqlExObj.isInputStatic(argName) || srcObj == null) {
                continue;
            }

            srcFieldName = srcObj.getDisplayName();
            srcObj = SQLObjectUtil.getTopSQLCanvasObject(srcObj);
            // Prevent infinite recursion if srcObj returned by SQLObjectUtil == sqlExObj.
            if (srcObj instanceof SQLCanvasObject && srcObj != sqlExObj) {
                SQLLinkEvent evt = new SQLLinkEvent(this, (SQLCanvasObject) srcObj, sqlExObj, srcFieldName, argName);
                fireSQLLinkCreationEvent(evt);

                if (srcObj instanceof SQLConnectableObject) {
                    restoreLinks((SQLConnectableObject) srcObj);
                }
            }
        }

        // Now restore child SQL object links
        List children = sqlExObj.getChildSQLObjects();
        it = children.iterator();
        while (it.hasNext()) {
            SQLObject childObj = (SQLObject) it.next();
            if (childObj instanceof SQLConnectableObject) {
                restoreLinks((SQLConnectableObject) childObj);
            }
        }
    }

    private void restoreObjects() {
        Collection col = sqlCondition.getAllObjects();
        Iterator it = col.iterator();

        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            try {
                // reload time we do not want to handle auto join
                addObjectInGraph(sqlObj, false);
            } catch (BaseException e) {
                Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "restoreObjects", "Error caught while restoring object (" + sqlObj.getDisplayName()
                    + ")", e);
            }
        }

        isDirty = false;
    }

}

