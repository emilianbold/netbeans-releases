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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbPreferences;

/**
 *
 * @author John Baker
 */
public class SQLHistoryModelImpl implements SQLHistoryModel {    
    private static final String SQL_HISTORY_FOLDER = "Databases/SQLHISTORY"; // NOI18N
    private static final String SQL_HISTORY_FILE_NAME = "sql_history";  // NOI18N
    private static final String SAVE_STATEMENTS_MAX_LIMIT_ENTERED = "10000"; // NOI18N
    private static final int SAVE_STATEMENTS_EMPTY = 0; // NOI18N
    private static final String SAVE_STATEMENTS_CLEARED = ""; // NOI18N  
    private static final Logger LOGGER = Logger.getLogger(SQLHistoryModelImpl.class.getName());
    private static final FileObject USERDIR = Repository.getDefault().getDefaultFileSystem().getRoot();

    List<SQLHistory> _sqlHistoryList = new ArrayList<SQLHistory>();
    
    public void initialize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFilter(String filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getFilter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<SQLHistory> getSQLHistoryList() throws SQLHistoryException {
        List<SQLHistory> retrievedSQL = new ArrayList<SQLHistory>();
        _sqlHistoryList.clear();
        try {
            boolean isRewriteSQLRequired = false;
            FileObject historyRoot = USERDIR.getFileObject(SQL_HISTORY_FOLDER);
            if (historyRoot == null) {
                return new ArrayList<SQLHistory>();
            }
            String historyFilePath = FileUtil.getFileDisplayName(historyRoot) + File.separator + SQL_HISTORY_FILE_NAME + ".xml"; // NOI18N            
            // Read persisted SQL from  file            
            retrievedSQL = SQLHistoryPersistenceManager.getInstance().retrieve(historyFilePath, historyRoot);
            // Remove duplicates
            if (!isSQLUnique(retrievedSQL)) {
                retrievedSQL = removeDuplicates(retrievedSQL);
                isRewriteSQLRequired = true;
            }
            // Get saved limit
            String savedLimit = NbPreferences.forModule(SQLHistoryPersistenceManager.class).get("SQL_STATEMENTS_SAVED_FOR_HISTORY", "");
            if (savedLimit.equals(SAVE_STATEMENTS_CLEARED)) {
                savedLimit = SAVE_STATEMENTS_MAX_LIMIT_ENTERED;
            }
            int limit = Integer.parseInt(savedLimit);
            // Remove any elements if save limit is exceeded
            if (retrievedSQL.size() > limit) {
                retrievedSQL = removeExtraSQL(retrievedSQL, limit);
                isRewriteSQLRequired = true;
            }
            if (isRewriteSQLRequired) {
                // Remove all elements from sql_history.xml
                SQLHistoryPersistenceManager.getInstance().updateSQLSaved(SAVE_STATEMENTS_EMPTY, historyRoot);
                // Write new list;  reversing list is required for persisting the SQL
                Collections.reverse(retrievedSQL);
                SQLHistoryPersistenceManager.getInstance().create(historyRoot, retrievedSQL);
                // return list to the expected order for viewing
                Collections.reverse(retrievedSQL);
            }
        } catch (ClassNotFoundException ex) {
            throw new SQLHistoryException();
        }
        return retrievedSQL;
    }
    
    private boolean isSQLUnique(List<SQLHistory> sqlHistoryList) {
        List<SQLHistory> revSqLHistoryList = new ArrayList<SQLHistory>();
        boolean isUnique = true;

        for (SQLHistory sqlHistory : sqlHistoryList) {
            for (SQLHistory revHistory : revSqLHistoryList) {
                if (revHistory.getSql().trim().equals(sqlHistory.getSql().trim())) {
                    if (revHistory.getUrl().equals(sqlHistory.getUrl())) {
                        isUnique = false;
                    }
                }
            }
            revSqLHistoryList.add(sqlHistory);
        }
        return isUnique;
    }
    
    private List<SQLHistory> removeDuplicates(List<SQLHistory> sqlHistoryList) {
         List<SQLHistory> revSqLHistoryList = new ArrayList<SQLHistory>();
         boolean canAdd = true;
            for (SQLHistory sqlHistory : sqlHistoryList) {
                for (SQLHistory revHistory : revSqLHistoryList) {
                    if (revHistory.getSql().trim().equals(sqlHistory.getSql().trim())) {
                        if (revHistory.getUrl().equals(sqlHistory.getUrl())) {
                            canAdd = false;
                        }
                    }
                }
                if (canAdd) {
                    revSqLHistoryList.add(sqlHistory);
                } else {
                    canAdd = true;
                }
            }
            return revSqLHistoryList;        
    }
    
    private List<SQLHistory> removeExtraSQL(List<SQLHistory> sqlHistoryList, int limit) {
        int i = 0;
        List<SQLHistory> revSqLHistoryList = new ArrayList<SQLHistory>();

        for (SQLHistory sqlHistory : sqlHistoryList) {
            if (i < limit) {
                revSqLHistoryList.add(sqlHistory);
            }
            i++;
        }    
        return revSqLHistoryList;
    }

    public void setSQLHistoryList(List<SQLHistory> sqlHistoryList) {
        _sqlHistoryList = sqlHistoryList;
    }
}
