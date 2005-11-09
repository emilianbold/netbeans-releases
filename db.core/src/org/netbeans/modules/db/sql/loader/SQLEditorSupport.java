/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.loader;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node.Cookie;
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.windows.CloneableOpenSupport;
import org.netbeans.modules.db.api.sql.SQLExecuteCookie;
import org.netbeans.modules.db.sql.execute.ui.SQLResultPanel;
import org.netbeans.modules.db.sql.execute.SQLExecuteHelper;
import org.netbeans.modules.db.sql.execute.SQLExecutionResult;
import org.openide.text.CloneableEditor;
import org.openide.util.Mutex;

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
    
    private static final ErrorManager LOGGER = ErrorManager.getDefault().getInstance(SQLEditorSupport.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(ErrorManager.INFORMATIONAL);
    
    private static final String MIME_TYPE = "text/x-sql"; // NOI18N
    
    private SQLResultPanel resultComponent;
    private SQLExecutionResult executionResult;
    
    /**
     * The RequestProcessor used for executing statements.
     */
    private RequestProcessor rp = new RequestProcessor("SQLExecution"); // NOI18N
    
    /**
     * The task representing the execution of statements.
     */
    private Task task;
    
    private JPanel container = null;
    
    private JSplitPane splitter = null;
    
    private boolean executed = false;
    
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
                obj.addSaveCookie(saveCookie);
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
            obj.removeSaveCookie(saveCookie);
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
    
    protected Component wrapEditorComponent(Component editor) {
        container = new JPanel(new BorderLayout());
        container.add(editor, BorderLayout.CENTER);
        return container;
    }
    
    protected void notifyClosed() {
        super.notifyClosed();
        rp.post(new Runnable() {
            public void run() {
                closeExecutionResult();
            }
        });
        if (isConsole() && getDataObject().isValid()) {
            try {
                getDataObject().delete();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
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
    
    private boolean isConsole() {
        try {
            // the "console" files are stored in the SFS
            return "nbfs".equals(getDataObject().getPrimaryFile().getURL().getProtocol()); // NOI18N
        } catch (FileStateInvalidException e) {
            return false;
        }
    }
    
    /**
     * Executes the statements in this SQLDataObject against the specified
     * database connection.
     * 
     * @param dbconn the database connection; must not be null.
     * @throws NullPointerException if the specified database connection is null.
     */
    public void executeSQL(final DatabaseConnection dbconn) {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        
        synchronized (rp) {
            if (task != null && !task.isFinished()) {
                throw new IllegalStateException("Statements are already being executed."); // NOI18N
            }

            if (LOG) {
                LOGGER.log(ErrorManager.INFORMATIONAL, "Executing against " + dbconn); // NOI18N
            }

            if (!SwingUtilities.isEventDispatchThread()) {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            ConnectionManager.getDefault().showConnectionDialog(dbconn);
                        }
                    });
                } catch (InterruptedException e) {
                    ErrorManager.getDefault().notify(e);
                    return;
                } catch (InvocationTargetException e) {
                    ErrorManager.getDefault().notify(e);
                    return;
                }
            } else {
                ConnectionManager.getDefault().showConnectionDialog(dbconn);
            }

            final Connection conn = dbconn.getJDBCConnection();
            if (LOG) {
                LOGGER.log(ErrorManager.INFORMATIONAL, "SQL connection: " + conn); // NOI18N
            }
            if (conn == null) {
                return;
            }

            JEditorPane[] panes = getOpenedPanes();
            if (panes.length <= 0) {
                throw new IllegalStateException("Cannot execute if the DataObject is not open in an editor."); // NOI18N
            }
            final String sql = panes[0].getText();        

            task = rp.post(new Runnable() {
                public void run() {
                    if (LOG) {
                        LOGGER.log("Started the SQL execution task"); // NOI18N
                    }
                    
                    ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutingStatements"));
                    handle.start();
                    handle.switchToIndeterminate();
                    
                    // TODO: is it OK to remove the text from the status bar?
                    StatusDisplayer.getDefault().setStatusText(""); // NOI18N

                    if (LOG) {
                        LOGGER.log(ErrorManager.INFORMATIONAL, "Closing the old execution result" ); // NOI18N
                    }
                    closeExecutionResult();
                    
                    executionResult = null;

                    try {
                        executionResult = SQLExecuteHelper.execute(new String[] { sql }, conn);
                        // TODO: use the status bar or not?
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutedSuccessfully"));
                        getResultComponent().setExecutionResult(executionResult);
                    } catch (SQLException e) {
                        showExecuteError(e.getMessage());
                    } catch (IOException e) {
                        showExecuteError(e.getMessage());
                    } finally {
                        handle.finish();
                    }
                }
            });
        }
    }
    
    private SQLResultPanel getResultComponent() {
        synchronized (rp) {
            if (resultComponent == null) {
                createResultComponent();
            }
        }
        return resultComponent;
    }
    
    private void createResultComponent() {
        Mutex.EVENT.writeAccess(new Mutex.Action() {
            public Object run() {
                Component editor = container.getComponent(0);
                resultComponent = new SQLResultPanel();
                splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editor, resultComponent);
                splitter.setBorder(null);
                container.removeAll();
                container.add(splitter);
                splitter.setDividerLocation(250);
                splitter.setDividerSize(7);
                return null;
            }
        });
    }
    
    private void showExecuteError(String error) {
        String message = MessageFormat.format(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionErrorMessage"), new Object[] { error });
        NotifyDescriptor desc = new NotifyDescriptor(message, NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionErrorTitle"), 
                NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, 
                new Object[] { NotifyDescriptor.OK_OPTION }, NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(desc);
    }
    
    private void closeExecutionResult() {
        assert rp.isRequestProcessorThread();
        
        if (executionResult != null) {
            try {
                executionResult.close();
            } catch (SQLException e) {
                // probably broken connection
                ErrorManager.getDefault().notify(e);
            }
            executionResult = null;
            
            try {
                getResultComponent().setExecutionResult(null);
            } catch (Exception e) {
                // ignore it, should never occur
            }
        }
    }

    /**
     * Clonable editor which saves its document when its is deactivated
     * or serialized if it was opened as a console.
     */
    protected CloneableEditor createCloneableEditor() {
        return new SQLCloneableEditor(this);
    }
    
    static class SQLCloneableEditor extends CloneableEditor {
        
        public SQLCloneableEditor() {
            super(null);
        }
        
        public SQLCloneableEditor(SQLEditorSupport support) {
            super(support);
        }

        protected void componentDeactivated() {
            if (((SQLEditorSupport)cloneableEditorSupport()).isConsole()) {
                try {
                    cloneableEditorSupport().saveDocument();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            super.componentDeactivated();
        }

        public void writeExternal(java.io.ObjectOutput out) throws IOException {
            if (((SQLEditorSupport)cloneableEditorSupport()).isConsole()) {
                try {
                    cloneableEditorSupport().saveDocument();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            super.writeExternal(out);
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
