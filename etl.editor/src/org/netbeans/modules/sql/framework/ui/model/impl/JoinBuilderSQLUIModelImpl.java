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
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.event.UndoableEditListener;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;

import com.sun.etl.exception.BaseException;

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

