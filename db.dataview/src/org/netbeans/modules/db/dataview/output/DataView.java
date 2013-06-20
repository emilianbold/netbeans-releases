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

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.meta.DBColumn;
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
    private static final int MAX_TAB_LENGTH = 25;
    private DatabaseConnection dbConn;
    private List<Throwable> errMessages = new ArrayList<Throwable>();
    private String sqlString; // Once Set, Data View assumes it will never change
    private SQLStatementGenerator stmtGenerator;
    private SQLExecutionHelper execHelper;
    private final List<DataViewPageContext> dataPage = new ArrayList<DataViewPageContext>();
    private final List<DataViewUI> dataViewUI = new ArrayList<DataViewUI>();
    private JComponent container;
    private int initialPageSize = org.netbeans.modules.db.dataview.api.DataViewPageContext.getStoredPageSize();
    private boolean nbOutputComponent = false;
    private int updateCount;
    private long executionTime;

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
        if(pageSize > 0) {
            dv.initialPageSize = pageSize;
        }
        try {
            dv.execHelper = new SQLExecutionHelper(dv);
            dv.execHelper.initialDataLoad();
            dv.stmtGenerator = new SQLStatementGenerator();
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
    public synchronized List<Component> createComponents() {
        List<Component> results;
        if (! hasResultSet()) {
            return Collections.emptyList();
        }

        if(dataPage.size() > 1) {
            container = new JTabbedPane();
        } else {
            container = new JPanel(new BorderLayout());
        }

        for (int i = 0; i < dataPage.size(); i++) {
            DataViewUI ui = new DataViewUI(this, dataPage.get(i), nbOutputComponent);
            ui.setName("Result Set " + i);
            dataViewUI.add(ui);
            container.add(ui);
            resetToolbar(hasExceptions());
        }

        String sql = getSQLString();
        if (sql.length() > MAX_TAB_LENGTH) {
            String trimmed = NbBundle.getMessage(DataViewUI.class, "DataViewUI_TrimmedTabName", sql.substring(0, Math.min(sql.length(), MAX_TAB_LENGTH)));
            container.setName(trimmed);
        } else {
            container.setName(sql);
        }
        container.setToolTipText(sql);

        results = new ArrayList<Component>();
        results.add(container);
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
        return dataPage.size() > 0;
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
        return dataViewUI.get(0).getEditButtons();
    }

    public synchronized void setEditable(final boolean editable) {
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                for (DataViewPageContext pageContext : dataPage) {
                    pageContext.getModel().setEditable(editable);
                }
            }
        });
    }

    // Used by org.netbeans.modules.db.dataview.api.DataViewPageContext#getPageSize
    public int getPageSize() {
        if (dataViewUI.isEmpty()) {
            return initialPageSize;
        }
        return dataViewUI.get(0).getPageSize();
    }

    // Non API modules follow

    List<DataViewPageContext> getPageContexts() {
        return this.dataPage;
    }

    DataViewPageContext getPageContext(int i) {
        return this.dataPage.get(i);
    }

    DataViewPageContext addPageContext(final DataViewDBTable table) {
        final DataViewPageContext pageContext = new DataViewPageContext(initialPageSize);
        this.dataPage.add(pageContext);
        Mutex.EVENT.writeAccess(new Mutex.Action<Object>() {
            @Override
            public Void run() {
                pageContext.setTableMetaData(table);
                pageContext.getModel().setColumns(table.getColumns().toArray(new DBColumn[0]));
                return null;
            }
        });
        return pageContext;
    }

    DatabaseConnection getDatabaseConnection() {
        return dbConn;
    }

    String getSQLString() {
        return sqlString;
    }

    SQLExecutionHelper getSQLExecutionHelper() {
        if (execHelper == null) {
            execHelper = new SQLExecutionHelper(this);
        }
        return execHelper;
    }

    SQLStatementGenerator getSQLStatementGenerator() {
        if (stmtGenerator == null) {
            stmtGenerator = new SQLStatementGenerator();
        }
        return stmtGenerator;
    }

    public void resetEditable() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                for (DataViewPageContext pageContext : dataPage) {
                    pageContext.resetEditableState();
                }
            }
        });
    }

    public boolean isEditable() {
        if(dataPage.isEmpty()) {
            return false;
        } else {
            return Mutex.EVENT.readAccess(new Mutex.Action<Boolean>() {
                @Override
                public Boolean run() {
                    boolean editable = true;
                    for (DataViewPageContext pageContext : dataPage) {
                        editable &= pageContext.getModel().isEditable();
                    }
                    return editable;
                }
            });
        }
    }

    synchronized void disableButtons() {
        assert dataViewUI != null;
        Mutex.EVENT.readAccess(new Runnable() {

            @Override
            public void run() {
                for(DataViewUI ui: dataViewUI) {
                    ui.disableButtons();
                }
            }
        });
        errMessages.clear();
    }

    synchronized void removeComponents() {
        Mutex.EVENT.readAccess(new Runnable() {

            @Override
            public void run() {
                if (container != null) {
                    if (container != null) {
                        container.getParent().remove(container);
                    }
                    container.removeAll();
                    container.repaint();
                    container.revalidate();
                }
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

            @Override
            public void run() {
                for(DataViewUI ui: dataViewUI) {
                    ui.resetToolbar(wasError);
                }
            }
        });
    }

    void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    private DataView() {
    }
}
