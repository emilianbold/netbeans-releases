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
package org.netbeans.modules.edm.editor.ui.model.impl;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.netbeans.modules.edm.model.SQLCanvasObject;
import org.netbeans.modules.edm.model.SQLConnectableObject;
import org.netbeans.modules.edm.model.SQLInputObject;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLObjectEvent;
import org.netbeans.modules.edm.model.SQLObjectListener;
import org.netbeans.modules.edm.model.SQLOperator;
import org.netbeans.modules.edm.model.VisibleSQLPredicate;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.editor.ui.event.SQLDataEvent;
import org.netbeans.modules.edm.editor.ui.event.SQLDataListener;
import org.netbeans.modules.edm.editor.ui.event.SQLLinkEvent;
import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.model.DBTable;
import org.openide.util.NbBundle;

/**
 * @author Ritesh Adval
 */
public abstract class AbstractSQLModel implements SQLObjectListener, SQLUIModel {

    private static transient final Logger mLogger = Logger.getLogger(AbstractSQLModel.class.getName());
    protected boolean isDirty = false;
    protected List<SQLDataListener> listeners = new ArrayList<SQLDataListener>();
    private List<SQLOperator> javaOperatorList = new ArrayList<SQLOperator>();

    protected AbstractSQLModel() {
    }

    public void addJavaOperator(SQLOperator javaOp) {
        this.javaOperatorList.add(javaOp);
    }

    public void addObject(SQLObject sqlObject) throws EDMException {
    }

    public abstract void addObjectIgnoreUndo(SQLObject sqlObject) throws EDMException;

    /**
     * Adds a SourceTableImpl instance using the given DBTable instance as a template, if
     * it does not already exist.
     * 
     * @param srcTable DBTable to serve as template for the new SourceTableImpl instance.
     * @return new SourceTableImpl instance
     * @throws EDMException if error occurs during creation
     */
    public SQLObject addSourceTable(DBTable srcTable, Point loc) throws EDMException {
        return null;
    }

    public synchronized void addSQLDataListener(SQLDataListener l) {
        listeners.add(l);
    }

    public abstract void addSQLObject(SQLObject sqlObject) throws EDMException;

    /**
     * Adds a TargetTableImpl instance using the given DBTable instance as a template, if
     * it does not already exist.
     * 
     * @param targetTable DBTable to serve as template for the new TargetTableImpl
     *        instance.
     * @return SourceTableImpl representing the contents of the given template object; may
     *         be a pre-existing object.
     * @throws EDMException if error occurs during creation
     */
    public SQLObject addTargetTable(DBTable targetTable, Point loc) throws EDMException {
        return null;
    }

    public void clearJavaOperators() {
        this.javaOperatorList.clear();
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
     * @throws EDMException if error occurs during linking
     */
    public void createLink(SQLCanvasObject srcObject, String srcFieldName, SQLConnectableObject destObject, String destFieldName)
            throws EDMException {

        SQLObject fieldObj = srcObject.getOutput(srcFieldName);
        destObject.addInput(destFieldName, fieldObj);

        SQLLinkEvent evt = new SQLLinkEvent(this, srcObject, destObject, srcFieldName, destFieldName);
        fireSQLLinkCreationEvent(evt);
        isDirty = true;
    }

    /**
     * Called when a link is created in collaboration view
     * 
     * @param srcObject object which is source of new link
     * @param srcFieldName -
     * @param destObject object which is destination of new link
     * @param destFieldName -
     * @throws EDMException if error occurs during linking
     */
    public void createLinkIgnoreUndo(SQLCanvasObject srcObject, String srcFieldName, SQLConnectableObject destObject, String destFieldName)
            throws EDMException {

        SQLObject fieldObj = srcObject.getOutput(srcFieldName);
        destObject.addInput(destFieldName, fieldObj);

        SQLLinkEvent evt = new SQLLinkEvent(this, srcObject, destObject, srcFieldName, destFieldName);
        fireSQLLinkCreationEvent(evt);
    }

    public abstract SQLCanvasObject createObject(String className) throws EDMException;

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

    public synchronized void fireSQLDataCreationEvent(final SQLDataEvent evt) throws EDMException {
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
    }

    /**
     * Removes a link from backend model object.
     * 
     * @param srcObject object which is source of link removed
     * @param srcFieldName -
     * @param destObject object which is destination of link removed
     * @param destFieldName -
     * @throws EDMException if error occurs during unlinking
     */
    public void removeLink(SQLCanvasObject srcObject, String srcFieldName, SQLConnectableObject destObject, String destFieldName)
            throws EDMException {
        SQLObject fieldObj = srcObject.getOutput(srcFieldName);
        destObject.removeInputByArgName(destFieldName, fieldObj);

        SQLLinkEvent evt = new SQLLinkEvent(this, srcObject, destObject, srcFieldName, destFieldName);

        fireSQLLinkDeletionEvent(evt);
        isDirty = true;
    }

    /**
     * Removes a link from backend model object.
     * 
     * @param srcObject object which is source of link removed
     * @param srcFieldName -
     * @param destObject object which is destination of link removed
     * @param destFieldName -
     * @throws EDMException if error occurs during unlinking
     */
    public void removeLinkIgnoreUndo(SQLCanvasObject srcObject, String srcFieldName, SQLConnectableObject destObject, String destFieldName)
            throws EDMException {

        SQLObject fieldObj = srcObject.getOutput(srcFieldName);
        destObject.removeInputByArgName(destFieldName, fieldObj);

        SQLLinkEvent evt = new SQLLinkEvent(this, srcObject, destObject, srcFieldName, destFieldName);

        fireSQLLinkDeletionEvent(evt);
    }

    public void removeObject(SQLObject sqlObject) throws EDMException {
    }

    public abstract void removeObjectIgnoreUndo(SQLObject sqlObject) throws EDMException;

    public synchronized void removeSQLDataListener(SQLDataListener l) {
        listeners.remove(l);
    }

    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    // this is temp till I refactor join
    protected abstract void addObjectInGraph(SQLObject obj, boolean handleAutojoin) throws EDMException;

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

    protected void restoreObjects(Collection col) throws EDMException {
        Iterator it = col.iterator();

        while (it.hasNext()) {
            SQLObject sqlObj = (SQLObject) it.next();
            try {
                // reload time we do not want to handle auto join
                addObjectInGraph(sqlObj, false);
            } catch (EDMException e) {
                mLogger.log(Level.INFO,NbBundle.getMessage(AbstractSQLModel.class, "MSG_Error_caught_while_restoring_object",new Object[] {sqlObj.getDisplayName()}), e);
                throw e;
            }
        }

        isDirty = false;
    }
}

