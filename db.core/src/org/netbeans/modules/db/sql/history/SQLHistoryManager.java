/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.sql.history;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.sql.execute.ui.SQLHistoryPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author John Baker
 */
public class SQLHistoryManager  {
    public static final String SQL_HISTORY_FOLDER = "Databases/SQLHISTORY"; // NOI18N
    public static final String SQL_HISTORY_FILE_NAME = "sql_history.xml";  // NOI18N
    private static SQLHistoryManager _instance = null;    
    private static final Logger LOGGER = Logger.getLogger(SQLHistory.class.getName());
    private List<SQLHistory> sqlList = new ArrayList<SQLHistory>();
    private int listSize;
    private FileObject historyRoot;

    private SQLHistoryManager() {
    }
    
    public static SQLHistoryManager getInstance() {
        if (_instance == null) {
            _instance = new SQLHistoryManager();                    
        } 
        return _instance;
    }

    /**
     * Get the value of listSize
     *
     * @return the value of listSize
     */
    public int getListSize() {
        return listSize;
    }
    
    public FileObject getHistoryRoot() {
        return historyRoot;
    }

    public void setHistoryRoot(FileObject root) {
        historyRoot = root;
    }

    /**
     * Set the value of listSize
     *
     * @param listSize new value of listSize
     */
    public void setListSize(int listSize) {
        this.listSize = listSize;
    }

    
    public void saveSQL(SQLHistory sqlStored) {
        sqlList.add(sqlStored);
    }
    
    public void save(FileObject userdirRoot) {
        try {
            // create a folder in the userdir for sql_history.xml file that maintains a list of executed SQL
            setHistoryRoot(userdirRoot.getFileObject(SQL_HISTORY_FOLDER));
            if (null == historyRoot || !historyRoot.isValid()) {
                historyRoot = FileUtil.createFolder(userdirRoot, SQL_HISTORY_FOLDER);
            }
            // Start managing the persistence of SQL statements that have been executed
            SQLHistoryPersistenceManager.getInstance().create(historyRoot, sqlList);
            sqlList.clear();
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, ex.getMessage());
            LOGGER.log(Level.INFO, NbBundle.getMessage(SQLHistoryManager.class, "MSG_ErrorParsingHistoryFile"));
        }
    }
    
    public List<SQLHistory> getSQLHistory() {
        return sqlList;
    }
    
    public List<SQLHistory> retrieve() {
        return new ArrayList<SQLHistory>();
        
    }

    public List<String> retrieve(String url) {
        List<String> sqlAsString = new ArrayList<String>();
        String sql;
        if (!getUrlsUsed().isEmpty()) {
            for (SQLHistory historyItem : sqlList) {
                sql = historyItem.getSql();
                if (url.equals(historyItem.getUrl())) {
                    sqlAsString.add(sql);
                }
            }
        }
        return sqlAsString;

    }

    private List<String> getUrlsUsed() {
        List<String> urls = new ArrayList<String>();
        String url;
        for (SQLHistory historyItem : sqlList) {
            url = historyItem.getUrl();
            if (!urls.contains(url)) {
                urls.add(url);
            }
        }
        return urls;
    }
    
    public int updateList(int limit, String historyFilePath, FileObject root) throws SQLHistoryException {
        List<SQLHistory> updatedSQLHistoryList = new ArrayList<SQLHistory>();
        int numItemsToRemove = 0;
        try {
            updatedSQLHistoryList = SQLHistoryPersistenceManager.getInstance().retrieve(historyFilePath, root);
            if (limit >= updatedSQLHistoryList.size()) {
                // no changes needed to the current list
                return -1;
            }
            // Remove elements from list based on the number of statements to save that is set in the SQL History dialog
            numItemsToRemove = updatedSQLHistoryList.size() - limit;
            for (int i = 0; i < numItemsToRemove; i++) {
                updatedSQLHistoryList.remove(0);
            }
         } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(SQLHistoryPanel.class, "MSG_RuntimeErrorRetrievingHistory") + ex);
        }    
        sqlList = updatedSQLHistoryList;
        return numItemsToRemove;
    }
}
