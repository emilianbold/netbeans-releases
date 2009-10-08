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
package org.netbeans.modules.sql.framework.ui.model.impl;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLObjectEvent;
import org.netbeans.modules.sql.framework.model.SQLObjectListener;
import org.netbeans.modules.sql.framework.model.SQLOperator;
import org.netbeans.modules.sql.framework.model.VisibleSQLPredicate;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.event.SQLDataEvent;
import org.netbeans.modules.sql.framework.ui.event.SQLDataListener;
import org.netbeans.modules.sql.framework.ui.event.SQLLinkEvent;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.undo.AddLink;
import org.netbeans.modules.sql.framework.ui.undo.AddNode;
import org.netbeans.modules.sql.framework.ui.undo.RemoveLink;
import org.netbeans.modules.sql.framework.ui.undo.RemoveNode;
import org.netbeans.modules.sql.framework.ui.undo.SQLUndoManager;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * @author Ritesh Adval
 */
public abstract class AbstractSQLModel implements SQLObjectListener, SQLUIModel {

    private static final String LOG_CATEGORY = AbstractSQLModel.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(AbstractSQLModel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    protected UndoableEditSupport editSupport = new UndoableEditSupport();
    protected boolean isDirty = false;
    protected List<SQLDataListener> listeners = new ArrayList<SQLDataListener>();
    protected SQLUndoManager undoManager = new SQLUndoManager();
    private List<SQLOperator> javaOperatorList = new ArrayList<SQLOperator>();

    protected AbstractSQLModel() {
        UndoableEditSupport editSupt = this.getUndoEditSupport();
        editSupt.addUndoableEditListener(this.getUndoManager());
    }

    public void addJavaOperator(SQLOperator javaOp) {
        this.javaOperatorList.add(javaOp);
        setContainsJavaOperators(true);
    }

    public void addObject(SQLObject sqlObject) throws BaseException {
        editSupport.postEdit(new AddNode(this, sqlObject));
    }

    public abstract void addObjectIgnoreUndo(SQLObject sqlObject) throws BaseException;

    /**
     * Adds a SourceTableImpl instance using the given DBTable instance as a template, if
     * it does not already exist.
     * 
     * @param srcTable DBTable to serve as template for the new SourceTableImpl instance.
     * @return new SourceTableImpl instance
     * @throws BaseException if error occurs during creation
     */
    public SQLObject addSourceTable(DBTable srcTable, Point loc) throws BaseException {
        return null;
    }

    public synchronized void addSQLDataListener(SQLDataListener l) {
        listeners.add(l);
    }

    public abstract void addSQLObject(SQLObject sqlObject) throws BaseException;

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
        return null;
    }

    public void clearJavaOperators() {
        this.javaOperatorList.clear();
        setContainsJavaOperators(false);
    }

    public void clearListener() {
        this.listeners.clear();
    }

    /**
     * Called when a link is created in collaboration view
     * 
     * @param srcObject object which is source of new link
     * @param srcFieldName -
     * @param destObject object which is destination of new link
     * @param destFieldName -
     * @throws BaseException if error occurs during linking
     */
    public void createLink(SQLCanvasObject srcObject, String srcFieldName, SQLConnectableObject destObject, String destFieldName)
            throws BaseException {

        SQLObject fieldObj = srcObject.getOutput(srcFieldName);
        destObject.addInput(destFieldName, fieldObj);

        SQLLinkEvent evt = new SQLLinkEvent(this, srcObject, destObject, srcFieldName, destFieldName);
        fireSQLLinkCreationEvent(evt);
        isDirty = true;
        editSupport.postEdit(new AddLink(this, srcObject, srcFieldName, destObject, destFieldName));
    }

    /**
     * Called when a link is created in collaboration view
     * 
     * @param srcObject object which is source of new link
     * @param srcFieldName -
     * @param destObject object which is destination of new link
     * @param destFieldName -
     * @throws BaseException if error occurs during linking
     */
    public void createLinkIgnoreUndo(SQLCanvasObject srcObject, String srcFieldName, SQLConnectableObject destObject, String destFieldName)
            throws BaseException {

        SQLObject fieldObj = srcObject.getOutput(srcFieldName);
        destObject.addInput(destFieldName, fieldObj);

        SQLLinkEvent evt = new SQLLinkEvent(this, srcObject, destObject, srcFieldName, destFieldName);
        fireSQLLinkCreationEvent(evt);
    }

    public abstract SQLCanvasObject createObject(String className) throws BaseException;

    public void createVisiblePredicateRefObj(VisibleSQLPredicate predicate) {
        String newOperator = predicate.getOperatorType();
        if (newOperator == null) {
            return;
        }
    }

    public synchronized void fireChildObjectCreatedEvent(final SQLDataEvent evt) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            final SQLDataListener l = (SQLDataListener) it.next();
            Runnable run = new Runnable() {

                public void run() {
                    l.childObjectCreated(evt);
                }
            };

            SwingUtilities.invokeLater(run);
        }
    }

    public synchronized void fireChildObjectDeletedEvent(final SQLDataEvent evt) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            final SQLDataListener l = (SQLDataListener) it.next();
            Runnable run = new Runnable() {

                public void run() {
                    l.childObjectDeleted(evt);
                }
            };

            SwingUtilities.invokeLater(run);
        }
    }

    public synchronized void fireSQLDataCreationEvent(final SQLDataEvent evt) throws BaseException {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            final SQLDataListener l = (SQLDataListener) it.next();
            l.objectCreated(evt);
        }

    }

    public synchronized void fireSQLDataDeletionEvent(final SQLDataEvent evt) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            final SQLDataListener l = (SQLDataListener) it.next();
            Runnable run = new Runnable() {

                public void run() {
                    l.objectDeleted(evt);
                }
            };

            SwingUtilities.invokeLater(run);
        }
    }

    public synchronized void fireSQLDataUpdatedEvent(final SQLDataEvent evt) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            final SQLDataListener l = (SQLDataListener) it.next();
            Runnable run = new Runnable() {

                public void run() {
                    l.objectUpdated(evt);
                }
            };

            SwingUtilities.invokeLater(run);
        }
    }

    public synchronized void fireSQLLinkCreationEvent(final SQLLinkEvent evt) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            final SQLDataListener l = (SQLDataListener) it.next();
            Runnable run = new Runnable() {

                public void run() {
                    l.linkCreated(evt);
                }
            };

            SwingUtilities.invokeLater(run);
        }
    }

    public synchronized void fireSQLLinkDeletionEvent(final SQLLinkEvent evt) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            final SQLDataListener l = (SQLDataListener) it.next();
            Runnable run = new Runnable() {

                public void run() {
                    l.linkDeleted(evt);
                }
            };

            SwingUtilities.invokeLater(run);
        }
    }

    public UndoableEditSupport getUndoEditSupport() {
        return editSupport;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * Check if a java operator is used in the model.
     * 
     * @return true if a java operator is used.
     */
    public boolean isContainsJavaOperators() {
        return this.javaOperatorList.size() != 0;
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    /**
     * called when an sql object is added
     * 
     * @param evt event object
     */
    public void objectAdded(SQLObjectEvent evt) {
        this.setDirty(true);
    }

    /**
     * called when an sql object is removed
     * 
     * @param evt event object
     */
    public void objectRemoved(SQLObjectEvent evt) {
        this.setDirty(true);
    }

    public void removeJavaOperator(SQLOperator javaOp) {
        this.javaOperatorList.remove(javaOp);

        // if we removed the last java operator from the
        // graph then reset the flag
        if (!isContainsJavaOperators()) {
            setContainsJavaOperators(false);
        }
    }

    /**
     * Removes a link from backend model object.
     * 
     * @param srcObject object which is source of link removed
     * @param srcFieldName -
     * @param destObject object which is destination of link removed
     * @param destFieldName -
     * @throws BaseException if error occurs during unlinking
     */
    public void removeLink(SQLCanvasObject srcObject, String srcFieldName, SQLConnectableObject destObject, String destFieldName)
            throws BaseException {
        SQLObject fieldObj = srcObject.getOutput(srcFieldName);
        destObject.removeInputByArgName(destFieldName, fieldObj);

        SQLLinkEvent evt = new SQLLinkEvent(this, srcObject, destObject, srcFieldName, destFieldName);

        fireSQLLinkDeletionEvent(evt);
        isDirty = true;
        editSupport.postEdit(new RemoveLink(this, srcObject, srcFieldName, destObject, destFieldName));
    }

    /**
     * Removes a link from backend model object.
     * 
     * @param srcObject object which is source of link removed
     * @param srcFieldName -
     * @param destObject object which is destination of link removed
     * @param destFieldName -
     * @throws BaseException if error occurs during unlinking
     */
    public void removeLinkIgnoreUndo(SQLCanvasObject srcObject, String srcFieldName, SQLConnectableObject destObject, String destFieldName)
            throws BaseException {

        SQLObject fieldObj = srcObject.getOutput(srcFieldName);
        destObject.removeInputByArgName(destFieldName, fieldObj);

        SQLLinkEvent evt = new SQLLinkEvent(this, srcObject, destObject, srcFieldName, destFieldName);

        fireSQLLinkDeletionEvent(evt);
    }

    public void removeObject(SQLObject sqlObject) throws BaseException {
        editSupport.postEdit(new RemoveNode(this, sqlObject));
    }

    public abstract void removeObjectIgnoreUndo(SQLObject sqlObject) throws BaseException;

    public synchronized void removeSQLDataListener(SQLDataListener l) {
        listeners.remove(l);
    }

    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    abstract void setContainsJavaOperators(boolean containsJavaOp);

    // this is temp till I refactor join
    protected abstract void addObjectInGraph(SQLObject obj, boolean handleAutojoin) throws BaseException;

    protected void restoreLinks(Collection objectC) {

        // now link other objects
        Iterator it = objectC.iterator();
        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            if (sqlObj instanceof SQLConnectableObject) {
                restoreLinks((SQLConnectableObject) sqlObj);
            }
        }

        isDirty = false;
    }

    protected void restoreLinks(SQLConnectableObject sqlExObj) {

        Map inputMap = sqlExObj.getInputObjectMap();
        Iterator it = inputMap.keySet().iterator();

        while (it.hasNext()) {
            String argName = (String) it.next();
            SQLInputObject inputObj = (SQLInputObject) inputMap.get(argName);
            SQLObject srcObj = inputObj.getSQLObject();
            String srcFieldName = null;

            if (srcObj == null) {
                continue;
            }

            srcFieldName = srcObj.getDisplayName();

            srcObj = SQLObjectUtil.getTopSQLCanvasObject(srcObj);

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

    protected void restoreObjects(Collection col) throws BaseException {
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
}

