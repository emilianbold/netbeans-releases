/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.sql.history;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.netbeans.modules.db.sql.execute.ui.SQLHistoryPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

/**
 *
 * @author John Baker
 */
public class SQLHistoryManager  {

    JAXBContext context;
    private static final String SQL_HISTORY_DIRECTORY = "Databases/SQLHISTORY"; // NOI18N
    private static final String SQL_HISTORY_FILE = "sql_history.xml"; // NOI18N
    public static final String OPT_SQL_STATEMENTS_SAVED_FOR_HISTORY = "SQL_STATEMENTS_SAVED_FOR_HISTORY"; // NOI18N
    public static final int DEFAULT_SQL_STATEMENTS_SAVED_FOR_HISTORY = 100;
    public static final int MAX_SQL_STATEMENTS_SAVED_FOR_HISTORY = 10000;
    private static SQLHistoryManager _instance = null;    
    private static final Logger LOGGER = Logger.getLogger(SQLHistoryEntry.class.getName());
    private SQLHistory sqlHistory;

    protected SQLHistoryManager() {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(SQLHistoryManager.class.getClassLoader());
        try {
            context = JAXBContext.newInstance("org.netbeans.modules.db.sql.history", SQLHistoryManager.class.getClassLoader());
            loadHistory();
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }
    
    public static SQLHistoryManager getInstance() {
        if (_instance == null) {
            _instance = new SQLHistoryManager();                    
        } 
        return _instance;
    }

    public int getListSize() {
        return NbPreferences.forModule(SQLHistoryPanel.class).getInt("OPT_SQL_STATEMENTS_SAVED_FOR_HISTORY", DEFAULT_SQL_STATEMENTS_SAVED_FOR_HISTORY);
    }

    protected FileObject getHistoryRoot(boolean create) throws IOException {
        FileObject result = null;
        FileObject historyRootDir = getConfigRoot().getFileObject(getRelativeHistoryPath());
        if (historyRootDir != null || create) {
            if (historyRootDir == null) {
                historyRootDir = FileUtil.createFolder(getConfigRoot(), getRelativeHistoryPath());
    }
            FileObject historyRoot = historyRootDir.getFileObject(getHistoryFilename());

            if (historyRoot != null || create) {
                if(historyRoot == null) {
                    historyRoot = historyRootDir.createData(getHistoryFilename());
    }
                result = historyRoot;
    }
        }
        return result;
    }
    
    protected FileObject getConfigRoot() {
        return FileUtil.getConfigRoot();
    }
    
    protected String getRelativeHistoryPath() {
        return SQL_HISTORY_DIRECTORY;
    }

    protected String getHistoryFilename() {
        return SQL_HISTORY_FILE;
                }

    public void setListSize(int listSize) {
        NbPreferences.forModule(SQLHistoryPanel.class).putInt("OPT_SQL_STATEMENTS_SAVED_FOR_HISTORY", listSize);
        sqlHistory.setHistoryLimit(listSize);
    }

    public void saveSQL(SQLHistoryEntry sqlStored) {
        sqlHistory.add(sqlStored);
            }

    private void loadHistory() {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = getHistoryRoot(false).getInputStream();
            sqlHistory = (SQLHistory) unmarshaller.unmarshal(is);
            sqlHistory.setHistoryLimit(getListSize());
            is.close();
        } catch (Exception ex) {
            sqlHistory = new SQLHistory();
            sqlHistory.setHistoryLimit(getListSize());
            LOGGER.log(Level.INFO, ex.getMessage());
        }
    }
    
    public void save() {
        try {
            Marshaller marshaller = context.createMarshaller();
            OutputStream os = getHistoryRoot(true).getOutputStream();
            marshaller.marshal(sqlHistory, os);
            os.close();
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, ex.getMessage());
            }
        }    

    public SQLHistory getSQLHistory() {
        return sqlHistory;
    }
}
