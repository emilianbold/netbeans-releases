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

import java.util.Iterator;

import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectFactory;
import org.netbeans.modules.sql.framework.ui.event.SQLDataEvent;
import org.netbeans.modules.sql.framework.ui.event.SQLDataListener;
import org.netbeans.modules.sql.framework.ui.model.JoinBuilderSQLUIModel;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 */
public class JoinBuilderSQLUIModelImpl extends AbstractSQLModel implements JoinBuilderSQLUIModel {

    private SQLJoinView joinView;

    /** Creates a new instance of JoinBuilderSQLUIModelImpl */
    public JoinBuilderSQLUIModelImpl(SQLDefinition def) {
        joinView = SQLModelObjectFactory.getInstance().createSQLJoinView();
        if (def != null) {
            joinView.setParent(def);
        }

        joinView.setDisplayName("JoinView");
    }

    public JoinBuilderSQLUIModelImpl(SQLJoinView jView) {
        this.joinView = jView;
    }

    public void addObject(SQLObject sqlObject) throws BaseException {
        super.addObject(sqlObject);
        addObjectIgnoreUndo(sqlObject);
    }

    public void addObjectIgnoreUndo(SQLObject sqlObject) throws BaseException {
        addSQLObject(sqlObject);

        // first time when an source table is added we want to handle auto join
        addObjectInGraph(sqlObject, true);
    }

    public void addSQLObject(SQLObject sqlObject) throws BaseException {
        joinView.addObject(sqlObject);
        this.setDirty(true);
    }

    public SQLCanvasObject createObject(String className) throws BaseException {
        SQLObject sqlObj = SQLObjectFactory.createSQLObject(className);
        return (SQLCanvasObject) sqlObj;
    }

    // hack need to override this and remove the runnable part
    // otherwise a duplicate join was always created for 3 table join
    public synchronized void fireSQLDataCreationEvent(final SQLDataEvent evt) throws BaseException {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            final SQLDataListener l = (SQLDataListener) it.next();
            l.objectCreated(evt);
        }
    }

    public SQLJoinView getSQLJoinView() {
        return this.joinView;
    }

    public void removeAll() {
        joinView.removeAllObjects();
    }

    public void removeObject(SQLObject sqlObject) throws BaseException {
        super.removeObject(sqlObject);
        removeObjectIgnoreUndo(sqlObject);
    }

    public void removeObjectIgnoreUndo(SQLObject sqlObject) throws BaseException {
        joinView.removeObject(sqlObject);
    }

    public void restoreLinks() {
        // now link other objects
        super.restoreLinks(joinView.getAllObjects());
    }

    /**
     * Rebuilds view model based on object pool and SQLDefinition hierarchy.
     */
    public synchronized void restoreUIState() throws BaseException {
        restoreObjects();
        restoreLinks();
    }

    public void setSQLJoinView(SQLJoinView jView) {
        this.joinView = jView;
    }

    /**
     * set it to true if a java operator is used in the model
     * 
     * @param javaOp true if there is a java operator
     */
    void setContainsJavaOperators(boolean javaOp) {
        // Does not implement this, since no operators are used in join preview
    }

    protected void addObjectInGraph(SQLObject sqlObject, boolean handleAutojoin) throws BaseException {
        if (sqlObject instanceof SQLCanvasObject) {
            SQLDataEvent evt = new SQLDataEvent(this, (SQLCanvasObject) sqlObject);
            fireSQLDataCreationEvent(evt);
            isDirty = true;
        }
    }

    private void restoreObjects() throws BaseException {
        super.restoreObjects(joinView.getAllObjects());
    }

}

