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
package org.netbeans.modules.sql.framework.ui.model.impl;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.event.UndoableEditListener;
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
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;


/**
 * @author radval
 */
public class ConditionBuilderSQLUIModelImpl extends AbstractSQLModel implements ConditionBuilderSQLUiModel {

    private static final String LOG_CATEGORY = ConditionBuilderSQLUIModelImpl.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(ConditionBuilderSQLUIModelImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
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
                mLogger.errorNoloc(mLoc.t("EDIT141: Error caught while restoring object ({0})", sqlObj.getDisplayName()), e);
            }
        }

        isDirty = false;
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
        throw new UnsupportedOperationException("Not supported yet.");
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

