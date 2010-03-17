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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.db.dataview.output;

import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * DataView to show data of a given sql query string, provides static method to create 
 * the DataView Pannel from a given sql query string and a connection. 
 *
 * TODO: Show execution plan for executed query
 * TODO: Navigate foreign key relationships in results
 * TODO: Save results in various formats (CSV, spreadsheet, html etc)
 * 
 * @author Ahimanikya Satapathy
 */
public class DataView {

    private DatabaseConnection dbConn;
    private List<Throwable> errMessages = new ArrayList<Throwable>();
    private String sqlString; // Once Set, Data View assumes it will never change
    private DataViewDBTable tblMeta;
    private SQLStatementGenerator stmtGenerator;
    private SQLExecutionHelper execHelper;
    private DataViewPageContext dataPage;
    private DataViewUI dataViewUI;
    private boolean nbOutputComponent = false;
    private boolean hasResultSet = false;
    private int updateCount;
    private long executionTime;
    private boolean supportsLimit = false;

    /**
     * Create and populate a DataView Object. Populates 1st data page of default size.
     * The caller can run this in background thread and then create the GUI components 
     * to render to render the DataView by calling DataView.createComponent()
     * 
     * @param dbConn instance of DBExplorer DatabaseConnection 
     * @param queryString SQL query string
     * @param pageSize default page size for this data view
     * @return a new DataView instance
     */
    public static DataView create(DatabaseConnection dbConn, String sqlString, int pageSize) {
        assert dbConn != null;

        DataView dv = new DataView();
        dv.dbConn = dbConn;
        dv.sqlString = sqlString.trim();
        dv.nbOutputComponent = false;
        try {
            dv.dataPage = new DataViewPageContext(pageSize);
            dv.execHelper = new SQLExecutionHelper(dv);
            SQLExecutionHelper.initialDataLoad(dv, dbConn, dv.execHelper);
            dv.stmtGenerator = new SQLStatementGenerator(dv);
        } catch (Exception ex) {
            dv.setErrorStatusText(ex);
        }
        return dv;
    }

    public static DataView create(DatabaseConnection dbConn, String sqlString, int pageSize, boolean nbOutputComponent) {
        DataView dataView = create(dbConn, sqlString, pageSize);
        dataView.nbOutputComponent = nbOutputComponent;
        return dataView;
    }

    /**
     * Create the UI component and renders the data fetched from database on create()
     * 
     * @param dataView DataView Object created using create()
     * @return a JComponent that after rending the given dataview
     */
    public List<Component> createComponents() {
        List<Component> results;
        if (!hasResultSet) {
            return Collections.emptyList();
        }

        synchronized (this) {
            this.dataViewUI = new DataViewUI(this, nbOutputComponent);
            setRowsInTableModel();
            dataViewUI.setEditable(tblMeta == null ? false : tblMeta.hasOneTable());
            resetToolbar(hasExceptions());
        }
        results = new ArrayList<Component>();
        results.add(dataViewUI);
        return results;
    }

    /**
     * Returns true if there were any expection in the last database call.
     * 
     * @return true if error occurred in last database call, false otherwise.
     */
    public boolean hasExceptions() {
        return !errMessages.isEmpty();
    }

    /**
     * Returns true if the statement executed has ResultSet.
     * 
     * @return true if the statement executed has ResultSet, false otherwise.
     */
    public boolean hasResultSet() {
        return hasResultSet;
    }

    /**
     * Returns Collection of a error messages of Throwable type, if there were any 
     * expection in the last database call, empty otherwise
     * 
     * @return Collection<Throwable>
     */
    public Collection<Throwable> getExceptions() {
        return Collections.unmodifiableCollection(errMessages);
    }

    /**
     * Get updated row count for the last executed sql statement.
     * 
     * @return number of rows updated in last execution, -1 if no rows updated
     */
    public int getUpdateCount() {
        return updateCount;
    }

    /**
     * Get execution time for the last executed sql statement
     * 
     * @return execution time for last executed sql statement in milliseconds
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Returns editing tool bar items.
     * 
     * @return an array of JButton
     */
    public JButton[] getEditButtons() {
        assert nbOutputComponent != false;
        return dataViewUI.getEditButtons();
    }

    public synchronized void setEditable(boolean editable) {
        dataViewUI.setEditable(editable);
    }

    public DataViewDBTable getDataViewDBTable() {
        return tblMeta;
    }

    DataViewPageContext getDataViewPageContext() {
        return dataPage;
    }

    DatabaseConnection getDatabaseConnection() {
        return dbConn;
    }

    String getSQLString() {
        return sqlString;
    }

    UpdatedRowContext getUpdatedRowContext() {
        return dataViewUI.getUpdatedRowContext();
    }

    SQLExecutionHelper getSQLExecutionHelper() {
        if (execHelper == null) {
            execHelper = new SQLExecutionHelper(this);
        }
        return execHelper;
    }

    SQLStatementGenerator getSQLStatementGenerator() {
        if (stmtGenerator == null) {
            stmtGenerator = new SQLStatementGenerator(this);
        }
        return stmtGenerator;
    }

    public boolean isEditable() {
        return dataViewUI.isEditable();
    }

    boolean isLimitSupported() {
        return supportsLimit;
    }

    synchronized void disableButtons() {
        assert dataViewUI != null;
        Mutex.EVENT.readAccess(new Runnable() {

            public void run() {
                dataViewUI.disableButtons();
            }
        });
        errMessages.clear();
    }

    synchronized void removeComponents() {
        Mutex.EVENT.readAccess(new Runnable() {

            public void run() {
                dataViewUI.getParent().setVisible(false);
                dataViewUI.removeAll();
                dataViewUI.repaint();
                dataViewUI.revalidate();
            }
        });
    }

    void setInfoStatusText(String statusText) {
        if (statusText != null) {
            StatusDisplayer.getDefault().setStatusText(statusText);
        }
    }

    synchronized void setErrorStatusText(Throwable ex) {
        if (ex != null) {
            if (ex instanceof DBException) {
                if (ex.getCause() instanceof SQLException) {
                    errMessages.add(ex.getCause());
                }
            }
            errMessages.add(ex);

            String title = NbBundle.getMessage(DataView.class, "MSG_error");
            StatusDisplayer.getDefault().setStatusText(title + ": " + ex.getMessage());
        }
    }

    synchronized void setErrorStatusText(String message, Throwable ex) {
        if (ex != null) {
            errMessages.add(ex);
        }

        String title = NbBundle.getMessage(DataView.class, "MSG_error");
        StatusDisplayer.getDefault().setStatusText(title + ": " + message);
    }

    void resetToolbar(final boolean wasError) {
        assert dataViewUI != null;
        Mutex.EVENT.readAccess(new Runnable() {

            public void run() {
                dataViewUI.resetToolbar(wasError);
            }
        });
    }

    void setLimitSupported(boolean supportsLimit) {
        this.supportsLimit = supportsLimit;
    }

    void setRowsInTableModel() {
        assert dataViewUI != null;
        assert dataPage != null;

        if (dataPage.getCurrentRows() != null) {
            Mutex.EVENT.readAccess(new Runnable() {

                public void run() {
                    dataViewUI.setDataRows(dataPage.getCurrentRows());
                    dataViewUI.setTotalCount(dataPage.getTotalRows());
                }
            });
        }
    }

    synchronized void incrementRowSize(int count) {
        assert dataViewUI != null;
        dataPage.setTotalRows(dataPage.getTotalRows() + count);
        Mutex.EVENT.readAccess(new Runnable() {

            public void run() {
                dataViewUI.setTotalCount(dataPage.getTotalRows());
            }
        });
    }

    synchronized void decrementRowSize(int count) {
        assert dataViewUI != null;
        dataPage.decrementRowSize(count);
        Mutex.EVENT.readAccess(new Runnable() {

            public void run() {
                dataViewUI.setTotalCount(dataPage.getTotalRows());
            }
        });
    }

    synchronized void syncPageWithTableModel() {
        dataViewUI.syncPageWithTableModel();
    }

    void setHasResultSet(boolean hasResultSet) {
        this.hasResultSet = hasResultSet;
    }

    void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    void setDataViewDBTable(DataViewDBTable tblMeta) {
        this.tblMeta = tblMeta;
    }

    private DataView() {
    }

    public int getPageSize() {
        if (dataViewUI == null) {
            return -1;
        }
        return dataViewUI.getPageSize();
    }
}
