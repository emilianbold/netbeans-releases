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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.db.sql.loader.SQLDataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;

/**
 *
 * @author John Baker
 */
public class SQLHistoryManager  {
    
    // XXX  move to the history package
    
    private static SQLHistoryManager _instance = null;    
    private static final Logger LOGGER = Logger.getLogger(SQLHistory.class.getName());
    private List<SQLHistory> sqlList = new ArrayList<SQLHistory>();
    
    private SQLHistoryManager() {
        generateSerializedFilename();
        SQLEditorRegistryListener editorRegistry = new SQLEditorRegistryListener();
        editorRegistry.initialize();
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

    private void generateSerializedFilename()  {
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
        LOGGER.log(Level.INFO, "SQL Saved (test message)");

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
    
     /**
     * Editor Registry listener to detect when an SQL editor closes.  SQL History is then serialized
     */
    private final class SQLEditorRegistryListener implements PropertyChangeListener, DocumentListener {
        private static final String SQL_MIME_TYPE = "text/x-sql"; // NOI18N
        private Document currentDocument;
        private String mimeType;
        
        public SQLEditorRegistryListener() {
        }

        public synchronized void initialize() {
            EditorRegistry.addPropertyChangeListener(this);
            JTextComponent newComponent = EditorRegistry.lastFocusedComponent();
            currentDocument = newComponent != null ? newComponent.getDocument() : null;
            if (currentDocument != null) {
                SQLDataObject sqldo = (SQLDataObject)currentDocument.getProperty("stream");
                FileObject fo = sqldo.getLookup().lookup(FileObject.class);
                mimeType = fo.getMIMEType();
                currentDocument.addDocumentListener(this);
            }
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            assert SwingUtilities.isEventDispatchThread(); 
            JTextComponent newComponent = EditorRegistry.lastFocusedComponent();
            Document newDocument = newComponent != null ? newComponent.getDocument() : null;
            if (currentDocument == newDocument) {
                return;
            }
            if (currentDocument != null) {
                currentDocument.removeDocumentListener(this);
            }
            currentDocument = newDocument;
            if (currentDocument != null) {
                currentDocument.addDocumentListener(this);
            }
            
            // XXX create a unit test 
            
            // Serialize SQL when an SQL Editor closes
            if (evt.getPropertyName().equals(EditorRegistry.LAST_FOCUSED_REMOVED_PROPERTY)) {                
                newComponent = EditorRegistry.lastFocusedComponent();
                newDocument = newComponent != null ? newComponent.getDocument() : null;
                
                // Save the MIME type of the new document 
                if (newDocument != null && newDocument.getProperty("stream").getClass().equals(SQLDataObject.class)) {
                    SQLDataObject sqldo = (SQLDataObject) newDocument.getProperty("stream");
                    FileObject fo = sqldo.getLookup().lookup(FileObject.class);
                    mimeType = fo.getMIMEType();
                    LOGGER.log(Level.INFO, "SQL HISTORY: NEW DOCUMENT = " + newDocument + ", MIME TYPE = " + mimeType);
                }
                if (mimeType.equals(SQL_MIME_TYPE)) {
                    LOGGER.log(Level.INFO, "SQL HISTORY: SAVED");
                    save();
                }
            }
        }
        
        public void insertUpdate(DocumentEvent evt) {
            // Unsupported
        }

        public void removeUpdate(DocumentEvent evt) {
            // Unsupported
        }

        public void changedUpdate(DocumentEvent evt) {
            // Unsupported
        }
    }  
}
