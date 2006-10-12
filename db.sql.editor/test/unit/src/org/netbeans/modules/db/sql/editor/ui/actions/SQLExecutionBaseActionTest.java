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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Andrei Badea
 */
public class SQLExecutionBaseActionTest extends NbTestCase {

    private InstanceContent instanceContent;
    private Lookup context;
    private SQLExecutionBaseActionImpl baseAction;
    private Action action;
    private SQLExecutionImpl sqlExecution;

    public SQLExecutionBaseActionTest(String testName) {
        super(testName);
    }

    public void setUp() {
        instanceContent = new InstanceContent();
        context = new AbstractLookup(instanceContent);
        baseAction = new SQLExecutionBaseActionImpl();
        action = baseAction.createContextAwareInstance(context);
        sqlExecution = new SQLExecutionImpl();
    }

    public void tearDown() {
        sqlExecution = null;
        action = null;
        baseAction = null;
        context = null;
        instanceContent = null;
    }

    protected boolean runInEQ() {
        return true;
    }

    public void testEnabled() {
        assertFalse("Should be disabled when no SQLExecution in the context", action.isEnabled());
        assertEquals("none", action.getValue(Action.NAME));

        instanceContent.add(sqlExecution);
        assertTrue("Should be enabled when SQLExecution in the context", action.isEnabled());
        assertEquals("idle", action.getValue(Action.NAME));

        sqlExecution.setExecuting(true);
        assertFalse("Should be disabled while executing", action.isEnabled());
        assertEquals("executing", action.getValue(Action.NAME));

        sqlExecution.setExecuting(false);
        assertTrue("Should be disabled when execution finished", action.isEnabled());
        assertEquals("idle", action.getValue(Action.NAME));

        instanceContent.remove(sqlExecution);
        assertFalse("Should be disabled when no SQLExecution removed from the context", action.isEnabled());
        assertEquals("none", action.getValue(Action.NAME));
    }

    public void testActionPerformed() {
        instanceContent.add(sqlExecution);
        Component tp = ((Presenter.Toolbar)action).getToolbarPresenter();
        assertTrue("The toolbar presenter should be a JButton", tp instanceof JButton);

        JButton button = (JButton)tp;
        button.doClick();
        assertTrue("Should perform the action when SQLExecution in the context", baseAction.actionPeformedCount == 1);

        instanceContent.remove(sqlExecution);
        button.doClick();
        assertTrue("Should not perform the action when no SQLExecution in the context", baseAction.actionPeformedCount == 1);

        instanceContent.add(sqlExecution);
        button.doClick();
        assertTrue("Should perform the action when SQLExecution in the context", baseAction.actionPeformedCount == 2);
    }

    private static final class SQLExecutionBaseActionImpl extends SQLExecutionBaseAction {

        private int actionPeformedCount;

        public String getDisplayName(SQLExecution sqlExecution) {
            if (sqlExecution == null) {
                return "none";
            } else if (sqlExecution.isExecuting()) {
                return "executing";
            } else {
                return "idle";
            }
        }

        protected void actionPerformed(SQLExecution sqlExecution) {
            assertNotNull(sqlExecution);
            actionPeformedCount++;
        }
    }

    private static final class SQLExecutionImpl implements SQLExecution {

        private PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
        private boolean executing = false;

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propChangeSupport.removePropertyChangeListener(listener);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propChangeSupport.addPropertyChangeListener(listener);
        }

        public boolean isExecuting() {
            return executing;
        }
        
        public void setExecuting(boolean executing) {
            this.executing = executing;
            propChangeSupport.firePropertyChange(SQLExecution.PROP_EXECUTING, null, null);
        }
        
        public boolean isSelection() {
            return false;
        }

        public void execute() {
        }

        public void executeSelection() {
        }

        public void setDatabaseConnection(DatabaseConnection dbconn) {
        }

        public DatabaseConnection getDatabaseConnection() {
            return null;
        }
    }
}
