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

package org.netbeans.modules.db.sql.loader;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecuteCookie;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.netbeans.modules.db.sql.execute.ui.SQLResultPanelModel;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node.Cookie;
import org.openide.text.DataEditorSupport;
import org.openide.util.Cancellable;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.db.sql.execute.ui.SQLResultPanel;
import org.netbeans.modules.db.sql.execute.SQLExecuteHelper;
import org.netbeans.modules.db.sql.execute.SQLExecutionResults;
import org.openide.filesystems.FileLock;
import org.openide.loaders.MultiDataObject;
import org.openide.text.CloneableEditor;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.windows.CloneableOpenSupport;

/** 
 * Editor support for SQL data objects. There can be two "kinds" of SQL editors: one for normal
 * DataObjects and one for "console" DataObjects. In the latter case the editor doesn't allow its 
 * contents to be saved explicitly, its name doesn't contain a "*" when it is modified, the respective
 * DataObject is deleted when the editor is closed, and the contents is saved when the editor is 
 * deactivated or upon exiting NetBeans.
 *
 * @author Jesse Beaumont, Andrei Badea
 */
public class SQLEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, SQLExecuteCookie {
    
    private static final Logger LOGGER = Logger.getLogger(SQLEditorSupport.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    static final String EDITOR_CONTAINER = "sqlEditorContainer"; // NOI18N
    
    private static final String MIME_TYPE = "text/x-sql"; // NOI18N
    
    private final PropertyChangeSupport sqlPropChangeSupport = new PropertyChangeSupport(this);
    
    // the RequestProcessor used for executing statements.
    private final RequestProcessor rp = new RequestProcessor("SQLExecution", 1, true); // NOI18N
    
    // the encoding of the current document
    private String encoding;
    
    // the database connection to execute against
    private DatabaseConnection dbconn;
    
    // whether we are executing statements
    private boolean executing;
    
    // execution results. Not synchronized since accessed only from rp of throughput 1.
    private SQLExecutionResults executionResults;
    
    // execution logger
    private SQLExecutionLoggerImpl logger;
    
    /** 
     * SaveCookie for this support instance. The cookie is adding/removing 
     * data object's cookie set depending on if modification flag was set/unset. 
     */
    private final SaveCookie saveCookie = new SaveCookie() {
        public void save() throws IOException {
            saveDocument();
        }
    };
    
    public SQLEditorSupport(SQLDataObject obj) {
        super(obj, new Environment(obj));
        setMIMEType(MIME_TYPE);
    }
    
    protected boolean notifyModified () {
        if (!super.notifyModified()) 
            return false;
        
        if (!isConsole()) {
            FileObject fo = getDataObject().getPrimaryFile();
            // Add the save cookie to the data object
            SQLDataObject obj = (SQLDataObject)getDataObject();
            if (obj.getCookie(SaveCookie.class) == null) {
                obj.addCookie(saveCookie);
                obj.setModified(true);
            }
        }

        return true;
    }

    protected void notifyUnmodified () {
        super.notifyUnmodified();

        // Remove the save cookie from the data object
        SQLDataObject obj = (SQLDataObject)getDataObject();
        Cookie cookie = obj.getCookie(SaveCookie.class);
        if (cookie != null && cookie.equals(saveCookie)) {
            obj.removeCookie(saveCookie);
            obj.setModified(false);
        }
    }
    
    protected String messageToolTip() {
        if (isConsole()) {
            return getDataObject().getPrimaryFile().getName();
        } else {
            return super.messageToolTip();
        }
    }
    
    protected String messageName() {
        if (!getDataObject().isValid()) return ""; // NOI18N
        
        if (isConsole()) {
            // just the name, no modified or r/o flags
            return getDataObject().getName();
        } else {
            return super.messageName();
        }
    }
    
    protected String messageHtmlName() {
        if (!getDataObject().isValid()) return ""; // NOI18N
        
        if (isConsole()) {
            // just the name, no modified or r/o flags
            String name = getDataObject().getName();
            if (name != null) {
                if (!name.startsWith("<html>")) { // NOI18N
                    name = "<html>" + name; // NOI18N
                }
            }
            return name;
        } else {
            return super.messageHtmlName();
        }
    }
    
    protected void notifyClosed() {
        super.notifyClosed();
        
        closeExecutionResult();
        closeLogger();
        
        if (isConsole() && getDataObject().isValid()) {
            try {
                getDataObject().delete();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
    
    protected boolean canClose() {
        if (isConsole()) {
            return true;
        } else {
            return super.canClose();
        }
    }
    
    boolean isConsole() {
        return ((SQLDataObject)getDataObject()).isConsole();
    }
    
    protected CloneableEditor createCloneableEditor() {
        return new SQLCloneableEditor(this);
    }
    
    protected Component wrapEditorComponent(Component editor) {
        JPanel container = new JPanel(new BorderLayout());
        container.setName(EDITOR_CONTAINER); // NOI18N
        container.add(editor, BorderLayout.CENTER);
        return container;
    }
    
    void addSQLPropertyChangeListener(PropertyChangeListener listener) {
        sqlPropChangeSupport.addPropertyChangeListener(listener);
    }
    
    void removeSQLPropertyChangeListener(PropertyChangeListener listener) {
        sqlPropChangeSupport.removePropertyChangeListener(listener);
    }
    
    synchronized DatabaseConnection getDatabaseConnection() {
        return dbconn;
    }
    
    public synchronized void setDatabaseConnection(DatabaseConnection dbconn) {
        this.dbconn = dbconn;
        sqlPropChangeSupport.firePropertyChange(SQLExecution.PROP_DATABASE_CONNECTION, null, null);
    }
    
    public void execute() {
        Document doc = getDocument();
        if (doc == null) {
            return;
        }
        String sql = null;
        try {
            sql = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            // should not happen
            Logger.getLogger("global").log(Level.INFO, null, e);
            sql = ""; // NOI18N
        }
        execute(sql, 0, sql.length());
    }
    
    /**
     * Executes either all or a part of the given sql string (which can contain
     * zero or more SQL statements). If startOffset < endOffset, the part of
     * sql specified is executed. If startOffset == endOffset, the statement
     * containing the character at startOffset, if any, is executed.
     *
     * @param sql the SQL string to execute. If it contains multiple lines they 
     * have to be delimited by \n.
     */
    void execute(String sql, int startOffset, int endOffset) {
        DatabaseConnection dbconn;
        synchronized (this) {
            dbconn = this.dbconn;
        }
        if (dbconn == null) {
            return;
        }
        SQLExecutor executor = new SQLExecutor(this, dbconn, sql, startOffset, endOffset);
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }
    
    synchronized boolean isExecuting() {
        return executing;
    }
    
    private synchronized void setExecuting(boolean executing) {
        this.executing = executing;
        sqlPropChangeSupport.firePropertyChange(SQLExecution.PROP_EXECUTING, null, null);
    }
    
    private void setResultModelToEditors(final SQLResultPanelModel model) {
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                Enumeration editors = allEditors.getComponents();
                while (editors.hasMoreElements()) {
                    SQLCloneableEditor editor = (SQLCloneableEditor)editors.nextElement();
                    if (model == null && !editor.hasResultComponent()) {
                        // if hasResultComponent() is false, setting a null model
                        // would unnecessarily create the result component, so...
                        continue;
                    }
                    
                    SQLResultPanel resultComponent = editor.getResultComponent();
                    
                    // resultComponent will be null for a deserialized 
                    // and not initialized CloneableEditor
                    if (resultComponent != null) {
                        resultComponent.setModel(model);
                    }
                }
            }
        });
    }
    
    private void setExecutionResults(SQLExecutionResults executionResults) {
        this.executionResults = executionResults;
    }
    
    private void closeExecutionResult() {
        setResultModelToEditors(null);
        
        Runnable run = new Runnable() {
            public void run() {
                if (executionResults != null) {
                    executionResults.close();
                    executionResults = null;
                }
            }
        };
        
        // need to run the Runnable in the request processor
        // since it makes JDBC calls, possibly blocking
        // the calling thread
        
        // closeExceptionResult is sometimes called in the RP,
        // e.g. while executing statements
        if (rp.isRequestProcessorThread()) {
            run.run();
        } else {
            rp.post(run);
        }
    }
    
    private SQLExecutionLoggerImpl createLogger() {
        closeLogger();
        
        String loggerDisplayName = null;
        if (isConsole()) {
            loggerDisplayName = getDataObject().getName();
        } else {
            loggerDisplayName = getDataObject().getNodeDelegate().getDisplayName();
        }
        
        synchronized (this) {
            logger = new SQLExecutionLoggerImpl(loggerDisplayName, this);
        }
        return logger;
    }
    
    private synchronized void closeLogger() {
        if (logger != null) {
            logger.close();
        }
    }
    
    private final static class SQLExecutor implements Runnable, Cancellable {
        
        private final SQLEditorSupport parent;

        // the connections which the statements are executed against
        private final DatabaseConnection dbconn;
        
        // the currently executed statement(s)
        private final String sql;
        
        private final int startOffset, endOffset;
        
        // the task representing the execution of statements
        private RequestProcessor.Task task;
        
        public SQLExecutor(SQLEditorSupport parent, DatabaseConnection dbconn, String sql, int startOffset, int endOffset) {
            assert parent != null;
            assert dbconn != null;
            assert sql != null;
            
            this.parent = parent;
            this.dbconn = dbconn;
            this.sql = sql;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public void setTask(RequestProcessor.Task task) {
            this.task = task;
        }
        
        public void run() {
            assert task != null : "Should have called setTask()"; // NOI18N
            
            parent.setExecuting(true);
            try {
                if (LOG) {
                    LOGGER.log(Level.FINE, "Started the SQL execution task"); // NOI18N
                    LOGGER.log(Level.FINE, "Executing against " + dbconn); // NOI18N
                }

                Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
                    public Void run() {
                        ConnectionManager.getDefault().showConnectionDialog(dbconn);
                        return null;
                    }
                });

                Connection conn = dbconn.getJDBCConnection();
                if (LOG) {
                    LOGGER.log(Level.FINE, "SQL connection: " + conn); // NOI18N
                }
                if (conn == null) {
                    return;
                }

                // need to save the document, otherwise the Line.Set.getOriginal mechanism does not work
                try {
                    Mutex.EVENT.readAccess(new Mutex.ExceptionAction<Void>() {
                        public Void run() throws Exception {
                            parent.saveDocument();
                            return null;
                        }
                    });
                } catch (MutexException e) {
                    Exceptions.printStackTrace(e.getException());
                    return;
                }

                ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutingStatements"), this);
                handle.start();
                try {
                    handle.switchToIndeterminate();

                    setStatusText(""); // NOI18N

                    if (LOG) {
                        LOGGER.log(Level.FINE, "Closing the old execution result"); // NOI18N
                    }
                    parent.closeExecutionResult();

                    SQLExecutionLoggerImpl logger = parent.createLogger();
                    SQLExecutionResults executionResults = SQLExecuteHelper.execute(sql, startOffset, endOffset, conn, handle, logger);
                    handleExecutionResults(executionResults, logger);
                } finally {
                    handle.finish();
                }
            } finally {
                parent.setExecuting(false);
            }
        }
        
        private void handleExecutionResults(SQLExecutionResults executionResults, SQLExecutionLoggerImpl logger) {
            if (executionResults == null) {
                // execution cancelled
                setStatusText(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionCancelled"));
                return;
            }
            
            parent.setExecutionResults(executionResults);
            
            if (executionResults.hasExceptions()) {
                // there was at least one exception
                setStatusText(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionFinishedWithErrors"));
                // just that, the exceptions are in the Output window
                return;
            }
            
            if (executionResults.size() <= 0) {
                // no results, but successfull
                setStatusText(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutedSuccessfully"));
                return;
            }
            
            SQLResultPanelModel model;
            try {
                model = SQLResultPanelModel.create(executionResults);
            } catch (SQLException e) {
                logger.logResultSetException(e);
                setStatusText(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ResultSetError"));
                return;
            } catch (IOException e) {
                logger.logResultSetException(e);
                setStatusText(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ResultSetError"));
                return;
            }
            
            if (model == null) {
                // execution cancelled
                setStatusText(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionCancelled"));
                return;
            } 
                
            if (!model.isEmpty()) {
                parent.setResultModelToEditors(model);
            }
            
            setStatusText(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutedSuccessfully"));
        }
        
        private void setStatusText(String statusText) {
            StatusDisplayer.getDefault().setStatusText(statusText);
        }
        
        public boolean cancel() {
            return task.cancel();
        }
    }

    /** 
     * Environment for this support. Ensures that getDataObject().setModified(true)
     * is not called if this support's editor was opened as a console.
     */
    static final class Environment extends DataEditorSupport.Env {

        public static final long serialVersionUID = 7968926994844480435L;

        private transient boolean modified = false;

        private transient FileLock fileLock;

        public Environment(SQLDataObject obj) {
            super(obj);
        }

        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected FileLock takeLock() throws IOException {
            MultiDataObject obj = (MultiDataObject)getDataObject();
            fileLock = obj.getPrimaryEntry().takeLock();
            return fileLock;
        }

        public void markModified() throws IOException {
            if (findSQLEditorSupport().isConsole()) {
                modified = true;
            } else {
                super.markModified();
            }
        }

        public void unmarkModified() {
            if (findSQLEditorSupport().isConsole()) {
                modified = false;
                if (fileLock != null && fileLock.isValid()) {
                    fileLock.releaseLock();
                }
            } else {
                super.unmarkModified();
            }
        }

        public boolean isModified() {
            if (findSQLEditorSupport().isConsole()) {
                return modified;
            } else {
                return super.isModified();
            }
        }

        public CloneableOpenSupport findCloneableOpenSupport() {
            return findSQLEditorSupport();
        }

        private SQLEditorSupport findSQLEditorSupport() {
            return (SQLEditorSupport)getDataObject().getCookie(SQLEditorSupport.class);
        }
    }
}
