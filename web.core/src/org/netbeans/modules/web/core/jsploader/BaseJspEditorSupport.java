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

package org.netbeans.modules.web.core.jsploader;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

import javax.swing.Timer;
import javax.swing.event.CaretListener;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;

import org.openide.text.DataEditorSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.MultiDataObject;
import org.openide.cookies.*;
import org.openide.windows.CloneableTopComponent;
import org.openide.text.CloneableEditor;
import org.openide.util.actions.SystemAction;
import org.openide.actions.CompileAction;
import org.openide.actions.ExecuteAction;
import org.openide.util.TaskListener;
import org.openide.util.Task;

import org.netbeans.modules.web.core.jsploader.TagLibParseSupport;
import org.openide.windows.CloneableOpenSupport;
import org.openide.nodes.Node;

public class BaseJspEditorSupport extends DataEditorSupport implements EditCookie, EditorCookie.Observable, OpenCookie, LineCookie, CloseCookie, PrintCookie {
    
    private static final int AUTO_PARSING_DELAY = 2000;//ms
    
    /** Timer which countdowns the auto-reparsing time. */
    private Timer timer;
    
    /** Cash of encoding of the file */
    private String encoding;
    
    public BaseJspEditorSupport(JspDataObject obj) {
        super(obj, new BaseJspEnv(obj));
        
        String ext = getDataObject().getPrimaryFile().getExt();
        
        if (ext.equals(JspLoader.TAG_FILE_EXTENSION)
        || ext.equals(JspLoader.TAGF_FILE_EXTENSION)
        || ext.equals(JspLoader.TAGX_FILE_EXTENSION))
            setMIMEType(JspLoader.TAG_MIME_TYPE);
        else
            setMIMEType(JspLoader.JSP_MIME_TYPE);
        
        initialize();
    }
    
    private void initialize() {
        // initialize timer
        timer = new Timer(0, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                final TagLibParseSupport sup = (TagLibParseSupport)getDataObject().getCookie(TagLibParseSupport.class);
                if (sup != null) {
                    sup.autoParse().addTaskListener(new TaskListener() {
                        public void taskFinished(Task t) {
                            notifyParsingDone(sup);
                        }
                    });
                }
            }
        });
        timer.setInitialDelay(AUTO_PARSING_DELAY);
        timer.setRepeats(false);
        
        // create document listener
        final DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { change(e); }
            public void changedUpdate(DocumentEvent e) { }
            public void removeUpdate(DocumentEvent e) { change(e); }
            
            private void change(DocumentEvent e) {
                restartTimer(false);
                TagLibParseSupport sup = (TagLibParseSupport)getDataObject().getCookie(TagLibParseSupport.class);
                if (sup != null) {
                    sup.setDocumentDirty(true);
                }
            }
        };
        
        // add change listener
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                if (isDocumentLoaded()) {
                    getDocument().addDocumentListener(docListener);
                }
            }
        });
        
        encoding = null;
    }
    
    /** Restart the timer which starts the parser after the specified delay.
     * @param onlyIfRunning Restarts the timer only if it is already running
     */
    private void restartTimer(boolean onlyIfRunning) {
        if (onlyIfRunning && !timer.isRunning())
            return;
        
        int delay = AUTO_PARSING_DELAY;
        if (delay > 0) {
            timer.setInitialDelay(delay);
            timer.restart();
        }
    }
    
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(stream, getObjectEncoding(false));
            kit.read(reader, doc, 0);
        }
        finally {
            if (reader != null)
                reader.close();
        }
    }
    
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        Writer wr = null;
        try {
            wr = new OutputStreamWriter(stream, encoding);
            kit.write(wr, doc, 0, doc.getLength());
        }
        finally {
            if (wr != null)
                wr.close();
        }
    }
    
    /** Notify about the editor closing.
     */
    protected void notifyClose() {
        TagLibParseSupport sup = (TagLibParseSupport)getDataObject().getCookie(TagLibParseSupport.class);
        if (sup != null) {
            sup.prepare();
        }
        //parsedHook = null;
    }
    
    /** Notify that parsing task has been finished; some dependent data may now
     * be refreshed from up-to-date parsing info */
    protected void notifyParsingDone(TagLibParseSupport sup) {
        if (sup.isDocumentDirty()) {
            restartTimer(false);
        }
    }
    
    protected boolean notifyModified() {
        boolean notify = super.notifyModified();
        if (!notify) {
            return false;
        }
        JspDataObject obj = (JspDataObject)getDataObject();
        if (obj.getCookie(SaveCookie.class) == null) {
            obj.addSaveCookie(new SaveCookie() {
                public void save() throws java.io.IOException {
                    saveDocument();
                }
            });
        }
        return true;
    }
    
    /** Called when the document becomes unmodified.
     * Here, removing the save cookie from the object and marking it unmodified.
     */
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        JspDataObject obj = (JspDataObject)getDataObject();
        obj.removeSaveCookie();
    }
    
    protected String getObjectEncoding(boolean useEditor) {
        encoding =  ((JspDataObject)getDataObject()).getFileEncoding( useEditor);
        return encoding;
    }
    
    /** Save the document in this thread and start reparsing it.
     * @exception IOException on I/O error
     */
    public void saveDocument() throws IOException {
        saveDocument(true, true);
    }
    
    /** Save the document in this thread.
     * @param parse true if the parser should be started, otherwise false
     * @exception IOException on I/O error
     */
    protected void saveDocumentIfNecessary(boolean parse) throws IOException {
        saveDocument(parse, false);
    }
    
    /** Save the document in this thread.
     * @param parse true if the parser should be started, otherwise false
     * @param forceSave if true save always, otherwise only when is modified
     * @exception IOException on I/O error
     */
    private void saveDocument(boolean parse, boolean forceSave) throws IOException {
        if (forceSave || isModified()) {
            getObjectEncoding(true);
            super.saveDocument();
            if (parse) {
                TagLibParseSupport sup = (TagLibParseSupport)getDataObject().getCookie(TagLibParseSupport.class);
                if (sup != null) {
                    sup.prepare();
                }
            }
        }
    }
    
  /* A method to create a new component. Overridden in subclasses.
   * @return the {@link BaseJspEditor} for this support
   */
    protected CloneableEditor createCloneableEditor() {
        return new BaseJspEditor(this);
    }
    
    public static class BaseJspEnv extends DataEditorSupport.Env {
        
        private static final long serialVersionUID = -800036748848958489L;
        
        public BaseJspEnv(JspDataObject obj) {
            super(obj);
        }
        
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        
        protected FileLock takeLock() throws IOException {
            return ((MultiDataObject)getDataObject()).getPrimaryEntry().takeLock();
        }
        
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (BaseJspEditorSupport)getDataObject().getCookie(BaseJspEditorSupport.class);
        }
    }
    
    public static class BaseJspEditor extends CloneableEditor {
        
        /** Listener on caret movements */
        CaretListener caretListener;
        
        public BaseJspEditor() {
            super();
        }
        
        /** Creates new editor */
        public BaseJspEditor(BaseJspEditorSupport s) {
            super(s);
            initialize();
        }
        
        public SystemAction[] getSystemActions() {
            SystemAction[] sa = super.getSystemActions();
            SystemAction[] jspServletActions = new SystemAction[] {
                null,
                SystemAction.get(CompileAction.class),
                null,
                SystemAction.get(ExecuteAction.class),
            };
            return SystemAction.linkActions(sa, jspServletActions);
        }
        
        protected void notifyParsingDone() {
        }
        
        private void initialize() {
            Node nodes[] = {((DataEditorSupport)cloneableEditorSupport()).getDataObject().getNodeDelegate()};
            setActivatedNodes(nodes);
            caretListener = new CaretListener() {
                public void caretUpdate(CaretEvent e) {
                    ((BaseJspEditorSupport)cloneableEditorSupport()).restartTimer(true);
                }
            };
        }
        
        /** Returns Editor pane for private use.
         * @return Editor pane for private use.
         */
        protected JEditorPane getEditorPane() {
            return pane;
        }
        
        /* This method is called when parent window of this component has focus,
         * and this component is preferred one in it.
         */
        protected void componentActivated() {
            // Workaround for bug #37188. If the pane is null, don't activate the component.
            if (getEditorPane() != null){
                getEditorPane().addCaretListener(caretListener);
                super.componentActivated();
            }
        }
        
        /*
         * This method is called when parent window of this component losts focus,
         * or when this component losts preferrence in the parent window.
         */
        protected void componentDeactivated() {
            getEditorPane().removeCaretListener(caretListener);
            super.componentDeactivated();
        }
        
        /* When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
         */
        protected boolean closeLast() {
            if (!super.closeLast()) return false;
            ((BaseJspEditorSupport)cloneableEditorSupport()).notifyClose();
            return true;
        }
        
        /** Deserialize this top component.
         * @param in the stream to deserialize from
         */
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialize();
        }
        
    } // end of JavaEditorComponent inner class
}
