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
package org.netbeans.modules.db.sql.execute;


import org.netbeans.modules.db.sql.loader.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
import org.openide.util.Exceptions;

/**
 *
 * @author John Baker
 */
public class SQLHistoryManager  {
    private static SQLHistoryManager _instance = null;    
    private static final Logger LOGGER = Logger.getLogger(SQLHistory.class.getName());
    
    private List<SQLHistory> sqlList = new ArrayList<SQLHistory>();
    
    private SQLHistoryManager() {
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
        new File(System.getProperty("netbeans.user") + File.separator + "config" + File.separator + "Databases" + File.separator);
        sqlList.add(sqlStored);

    }

    private String generateSerializedFilename() {
        return System.getProperty("netbeans.user") + File.separator + "config" + File.separator + "Databases" + File.separator + "sql_history.ser";
    }

    public void save() {
        // XXX Need to check if file exists and rewrite if it does
        if (sqlList != null) {
            ObjectOutputStream os = null;
            try {
                os = new ObjectOutputStream(new FileOutputStream(generateSerializedFilename()));
                os.writeObject(sqlList);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private List<SQLHistory> deserialize() {
//        List<SQLHistory> history = new ArrayList<SQLHistory>();
//        FileInputStream fileIn = null;
//        try {
//            fileIn = new FileInputStream(new File(generateSerFilename()));
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        ObjectInputStream is = null;
//        try {
//            is = new ObjectInputStream(fileIn);
//            history = (List<SQLHistory>)is.readObject();
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (ClassNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        } finally {
//            try {
//                if (is != null) {
//                    is.close();
//                }
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//
//        return history;
        // XXX temporary, for testing functionality
        return new ArrayList<SQLHistory>();

    }

    public List<SQLHistory> getSQLHistory() {
        return sqlList;
    }
    
    public List<SQLHistory> retrieve() {
        return deserialize();
        
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

        private Document currentDocument;

        public SQLEditorRegistryListener() {
        }

        public synchronized void initialize() {
            LOGGER.log(Level.INFO, "SQL Editor = " + currentDocument.TitleProperty);
            EditorRegistry.addPropertyChangeListener(this);
            JTextComponent newComponent = EditorRegistry.lastFocusedComponent();
            currentDocument = newComponent != null ? newComponent.getDocument() : null;
            if (currentDocument != null) {
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
            
            // Serialize SQL when an SQL Editor closes
            if (evt.getPropertyName().equals(EditorRegistry.LAST_FOCUSED_REMOVED_PROPERTY)) {
                save();
            } 
            
//            if (evt.getPropertyName().equals(EditorRegistry.FOCUS_GAINED_PROPERTY)) {
//                List<SQLHistory> sqls = deserialize();
//
//                // XXX temporary, for testing functionality
//                for (SQLHistory history : sqls) {
//                    LOGGER.log(Level.INFO, "History = " + history.getSql());
//                }
            }

        
        public void insertUpdate(DocumentEvent arg0) {
            // Unsupported
        }

        public void removeUpdate(DocumentEvent arg0) {
            // Unsupported
        }

        public void changedUpdate(DocumentEvent arg0) {
            // Unsupported
        }
    }  
}
