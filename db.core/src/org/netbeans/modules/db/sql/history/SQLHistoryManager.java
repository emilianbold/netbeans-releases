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


import java.io.IOException;
import org.netbeans.modules.db.sql.execute.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;

/**
 *
 * @author John Baker
 */
public class SQLHistoryManager  {
    public static final String SQL_HISTORY_FOLDER = "Databases/SQLHISTORY"; // NOI18N
    private static SQLHistoryManager _instance = null;    
    private static final Logger LOGGER = Logger.getLogger(SQLHistory.class.getName());
    private List<SQLHistory> sqlList = new ArrayList<SQLHistory>();
    
    private SQLHistoryManager() {
        generatePersistedFilename();
    }
    
    public static SQLHistoryManager getInstance() {
        if (_instance == null) {
            _instance = new SQLHistoryManager();                    
        } 
        return _instance;
    }

    public void saveSQL(SQLHistory sqlStored) {
        sqlList.add(sqlStored);
    }

    private void generatePersistedFilename()  {
        FileObject databaseDir = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject("Databases");
        try {
            if (databaseDir.getFileObject("sql_history", "xml") == null) {  // NOI18N
                databaseDir.createData("sql_history", "xml"); // NOI18N
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void save() {
        // create a folder in the userdir for sql_history.xml file that maintains a list of executed SQL
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject tmpFo = root.getFileObject(SQL_HISTORY_FOLDER);

        if (tmpFo == null) {
            try {
                tmpFo = FileUtil.createFolder(root, SQL_HISTORY_FOLDER);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        } 
        // Start managing the persistence of SQL statements that have been executed
        try {
            SQLHistoryPersistenceManager.getInstance().create(tmpFo, sqlList);
            sqlList.clear();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
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
}
