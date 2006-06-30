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
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.sql.execute.ui.SQLResultPanelModel;
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
import org.netbeans.modules.db.sql.execute.SQLExecutionResults;
import org.openide.text.CloneableEditor;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;

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
    
    private static final String EDITOR_CONTAINER = "sqlEditorContainer"; // NOI18N
    
    private static final String MIME_TYPE = "text/x-sql"; // NOI18N
    
    private String encoding;
    
    private SQLExecutionResults executionResults;
    
    /**
     * The RequestProcessor used for executing statements.
     */
    private RequestProcessor rp = new RequestProcessor("SQLExecution"); // NOI18N
    
    /**
     * The task representing the execution of statements.
     */
    private Task task;
    
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
    
    protected void notifyClosed() {
        super.notifyClosed();
        //rp.post(new Runnable() {
            //public void run() {
                closeExecutionResult();
            //}
        //});
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
    
    protected Component wrapEditorComponent(Component editor) {
        JPanel container = new JPanel(new BorderLayout());
        container.setName(EDITOR_CONTAINER); // NOI18N
        container.add(editor, BorderLayout.CENTER);
        return container;
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

            // this causes the CloneableEditors to be initialized after deserialization, 
            // avoiding the NPE in issue 70695
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
                    
                    StatusDisplayer.getDefault().setStatusText(""); // NOI18N

                    if (LOG) {
                        LOGGER.log(ErrorManager.INFORMATIONAL, "Closing the old execution result" ); // NOI18N
                    }
                    closeExecutionResult();
                    
                    executionResults = null;

                    String error = null;
                    SQLResultPanelModel model = null;
                    try {
                        executionResults = SQLExecuteHelper.execute(new String[] { sql }, conn);
                        model = new SQLResultPanelModel(executionResults);
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutedSuccessfully"));
                    } catch (SQLException e) {
                        error = e.getMessage();
                    } catch (IOException e) {
                        error = e.getMessage();
                    } finally {
                        handle.finish();
                    }
                    
                    if (error != null) {
                        showExecuteError(error);
                        return;
                    }
                    
                    if (model != null) {
                        setResultModelToEditors(model);
                    }
                }
            });
        }
    }
    
    private void setResultModelToEditors(final SQLResultPanelModel model) {
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                Enumeration editors = allEditors.getComponents();
                while (editors.hasMoreElements()) {
                    SQLCloneableEditor editor = (SQLCloneableEditor)editors.nextElement();
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
    
    private void showExecuteError(String error) {
        String message = MessageFormat.format(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionErrorMessage"), new Object[] { error });
        NotifyDescriptor desc = new NotifyDescriptor(message, NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionErrorTitle"), 
                NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, 
                new Object[] { NotifyDescriptor.OK_OPTION }, NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(desc);
    }
    
    private void closeExecutionResult() {
        setResultModelToEditors(null);
        
        Runnable run = new Runnable() {
            public void run() {
                if (executionResults != null) {
                    try {
                        executionResults.close();
                    } catch (SQLException e) {
                        // probably broken connection
                        ErrorManager.getDefault().notify(e);
                    }
                    executionResults = null;
                }
            }
        };
        
        if (rp.isRequestProcessorThread()) {
            run.run();
        } else {
            rp.post(run);
        }
    }

    /**
     * Clonable editor which saves its document when its is deactivated
     * or serialized if it was opened as a console.
     */
    protected CloneableEditor createCloneableEditor() {
        return new SQLCloneableEditor(this);
    }

    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, javax.swing.text.BadLocationException {
        encoding = getEncoding(stream);
        if (LOG) {
            LOGGER.log(ErrorManager.INFORMATIONAL, "Encoding: " + encoding); // NOI18N
        }
        if (encoding != null) {
            InputStreamReader reader = new InputStreamReader(stream, encoding);
            try {
                kit.read(reader, doc, 0);
            } finally {
                stream.close();
            }
        } else {
            super.loadFromStreamToKit(doc, stream, kit);
        }
    }

    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, java.io.OutputStream stream) throws IOException, javax.swing.text.BadLocationException {
        if (encoding != null) {
            if ("utf-8".equals(encoding)) { // NOI18N
                // write an utf-8 byte order mark
                stream.write(0xef);
                stream.write(0xbb);
                stream.write(0xbf);
            }
            OutputStreamWriter writer = new OutputStreamWriter(stream, encoding);
            try {
                kit.write(writer, doc, 0, doc.getLength());
            } finally {
                writer.close();
            }
        } else {
            super.saveFromKitToStream(doc, kit, stream);
        }
    }
    
    private static String getEncoding(InputStream stream) throws IOException {
        if (!stream.markSupported()) {
            return null;
        }
        stream.mark(3);
        // test a utf-8 byte order mark
        boolean isUTF8 = (stream.read() == 0xef && stream.read() == 0xbb && stream.read() == 0xbf);
        if (isUTF8) {
            return "utf-8"; // NOI18N
        } else {
            stream.reset();
            return null;
        }
    }

    static class SQLCloneableEditor extends CloneableEditor {
        
        private JPanel container;
        private JSplitPane splitter;
        private SQLResultPanel resultComponent;
        
        public SQLCloneableEditor() {
            super(null);
        }
        
        public SQLCloneableEditor(SQLEditorSupport support) {
            super(support);
        }
        
        public SQLResultPanel getResultComponent() {
            assert SwingUtilities.isEventDispatchThread();
            if (resultComponent == null) {
                createResultComponent();
            }
            return resultComponent;
        }

        private void createResultComponent() {
            JPanel container = findContainer(this);
            if (container == null) {
                // the editor has just been deserialized and has not been initialized yet
                // thus CES.wrapEditorComponent() has not been called yet
                return;
            }
            
            Component editor = container.getComponent(0);
            resultComponent = new SQLResultPanel();
            splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editor, resultComponent);
            splitter.setBorder(null);
            container.removeAll();
            container.add(splitter);
            splitter.setDividerLocation(250);
            splitter.setDividerSize(7);
            
            // #69642: the parent of the CloneableEditor's ActionMap is
            // the editor pane's ActionMap, therefore the delete action is always returned by the
            // CloneableEditor's ActionMap.get(). This workaround delegates to the editor pane 
            // only when the editor pane has the focus.
            getActionMap().setParent(new DelegateActionMap(getActionMap().getParent(), getEditorPane()));
            
            if (equals(TopComponent.getRegistry().getActivated())) {
                // setting back the focus lost when removing the editor from the CloneableEditor
                requestFocusInWindow();
            }
        }
        
        /**
         * Finds the container component added by SQLEditorSupport.wrapEditorComponent.
         * Not very nice, but avoids the API change in #69466.
         */
        private JPanel findContainer(Component parent) {
            if (!(parent instanceof JComponent)) {
                return null;
            }
            Component[] components = ((JComponent)parent).getComponents();
            for (int i = 0; i < components.length; i++) {
                Component component = components[i];
                if (component instanceof JPanel && EDITOR_CONTAINER.equals(component.getName())) {
                    return (JPanel)component;
                }
                JPanel container = findContainer(component);
                if (container != null) {
                    return container;
                }
            }
            return null;
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
    
    private static final class DelegateActionMap extends ActionMap {
        
        private ActionMap delegate;
        private JEditorPane editorPane;
        
        public DelegateActionMap(ActionMap delegate, JEditorPane editorPane) {
            this.delegate = delegate;
            this.editorPane = editorPane;
        }

        public void remove(Object key) {

            super.remove(key);
        }

        public javax.swing.Action get(Object key) {
            boolean isEditorPaneFocused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == editorPane;
            if (isEditorPaneFocused) {
                return delegate.get(key);
            } else {
                return null;
            }
        }

        public void put(Object key, Action action) {
            delegate.put(key, action);
        }

        public void setParent(ActionMap map) {
            delegate.setParent(map);
        }

        public int size() {
            return delegate.size();
        }

        public Object[] keys() {
            return delegate.keys();
        }

        public ActionMap getParent() {
            return delegate.getParent();
        }

        public void clear() {
            delegate.clear();
        }

        public Object[] allKeys() {
            return delegate.allKeys();
        }
    }
}
