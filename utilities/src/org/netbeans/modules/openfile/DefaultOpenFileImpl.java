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
package org.netbeans.modules.openfile;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.FileSystemAction;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;


/**
 * Opens files when requested. Main functionality.
 *
 * @author Jaroslav Tulach, Jesse Glick, Marian Petras, David Konecny
 */
public class DefaultOpenFileImpl implements OpenFileImpl, Runnable {
    
    /** extenstion for .java files (including the dot) */
    static final String JAVA_EXT = ".JAVA";                             //NOI18N
    /** extension for .txt files (including the dot) */
    static final String TXT_EXT = ".TXT";                               //NOI18N
    /**
     * if opening file using non-observable <code>EditorCookie</code>,
     * how long should we wait (in milliseconds) between tries?
     *
     * @see  #openDocAtLine
     */
    private static final int OPEN_EDITOR_WAIT_PERIOD_MS = 100;
    /**
     * if opening file using non-observable <code>EditorCookie</code>,
     * how long should we wait (in milliseconds) in total before giving up?
     *
     * @see  #openDocAtLine
     */
    private static final int OPEN_EDITOR_TOTAL_TIMEOUT_MS = 1000;

    private static final String ZIP_EXT = "zip"; //NOI18N
    private static final String JAR_EXT = "jar"; //NOI18N

    /**
     * parameter of this <code>Runnable</code>
     * - file to open
     */
    private final FileObject fileObject;
    /**
     * parameter of this <code>Runnable</code>
     * - line number to open the {@link #fileObject file} at, or <code>-1</code>
     *   to ignore
     */
    private final int line;
    
    /**
     * Creates an instance of this class.
     * It is used only as an instance of <code>Runnable</code>
     * used for rescheduling to the AWT thread.
     * The arguments are stored to local variables and when the
     * <code>run()</code> method gets executed (in the AWT thread),
     * they are passed to the <code>open(...)</code> method.
     *
     * @param  file  file to open (must exist)
     * @param  line  line number to try to open to (starting at zero),
     *               or <code>-1</code> to ignore
     * @param  waiter  double-callback or <code>null</code>
     */
    private DefaultOpenFileImpl(FileObject fileObject,
                                int line) {
        this.fileObject = fileObject;
        this.line = line;
    }

    /** Creates a new instance of OpenFileImpl */
    public DefaultOpenFileImpl() {
        
        /* These fields are not used in the default instance. */
        this.fileObject = null;
        this.line = -1;
    }
    
    /**
     * Sets the specified text into the status line.
     *
     * @param  text  text to be displayed
     */
    protected final void setStatusLine(String text) {
        StatusDisplayer.getDefault().setStatusText(text);
    }
    
    /** Clears the status line. */
    protected final void clearStatusLine() {
        setStatusLine("");                                              //NOI18N
    }
    
    /**
     * Prints a text into the status line that a file is being opened
     * and (optionally) that the Open File Server is waiting
     * for it to be closed.
     *
     * @param  fileName  name of the file
     * @param  waiting  <code>true</code> if the server will wait for the file,
     *                  <code>false</code> if not
     */
    protected void setStatusLineOpening(String fileName) {
        setStatusLine(NbBundle.getMessage(DefaultOpenFileImpl.class, "MSG_opening", fileName));
    }
    
    /**
     * Displays a dialog that the file cannot be open.
     * This method is to be used in cases that the file was open via
     * the Open File Server. The message also informs that
     * the launcher will be notified as if the file
     * was closed immediately.
     *
     * @param  fileName  name of file that could not be opened
     */
    protected void notifyCannotOpen(String fileName) {
        assert EventQueue.isDispatchThread();
        
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                NbBundle.getMessage(DefaultOpenFileImpl.class,
                                    "MSG_cannotOpenWillClose",          //NOI18N
                                    fileName)));
    }
    
    /**
     * Opens an editor using <code>EditorCookie</code>.
     * If non-negative line number is passed, it also places cursor at the given
     * line.
     *
     * @param  cookie  cookie to use for opening an editor
     * @param  observable  whether the cookie is
     *                     <code>EditorCookie.Observable</code>
     * @param  line  line number to place cursor to (starting at <code>0</code>)
     * @return  <code>true</code> if the cookie was successfully activated,
     *          <code>false</code> if some error occured
     */
    private boolean openEditor(final EditorCookie editorCookie,
                               final int line) {
        assert EventQueue.isDispatchThread();
        
        /* if the editor is already open, just set the cursor and activate it */
        JEditorPane[] openPanes = editorCookie.getOpenedPanes();
        if (openPanes != null) {
            if (line >= 0) {
                int cursorOffset = getCursorOffset(editorCookie.getDocument(),
                                                   line);
                openPanes[0].setCaretPosition(cursorOffset);
            }
            Container c = SwingUtilities.getAncestorOfClass(TopComponent.class,
                                                            openPanes[0]);
            assert c != null;
            
            final TopComponent tc = (TopComponent) c;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    tc.requestActive();
                }
            });
            return true;
        }
        
        /* get the document: */
        final StyledDocument doc;
        try {
            doc = editorCookie.openDocument();
        } catch (IOException ex) {
            String msg = NbBundle.getMessage(
                    DefaultOpenFileImpl.class,
                    "MSG_cannotOpenWillClose");                     //NOI18N
            ErrorManager.getDefault().notify(
                    ErrorManager.EXCEPTION,
                    ErrorManager.getDefault().annotate(ex, msg));
            clearStatusLine();
            return false;
        }

        if (line < 0) {
            editorCookie.open();
            
            /*
             * editorCookie.open() may return before the editor is actually
             * open. But since the document was successfully open,
             * the editor should be opened quite quickly and no problem
             * should occur.
             */
        } else {
            openDocAtLine(editorCookie, doc, line);
        }
        return true;
    }
    
    /**
     * Opens a document in the editor at a given line.
     * This method is used in the case that the editor is not opened yet
     * (<code>EditorCookie.getOpenedPanes()</code> returned <code>null</code>)
     * and is to be opened at a specific line.
     *
     * @param  editorCookie  editor cookie to use for opening the document
     * @param  doc  document already loaded using the editor cookie
     * @param  line  line to open the document at (first line = <code>0</code>);
     *               must be non-negative
     */
    private void openDocAtLine(final EditorCookie editorCookie,
                               final StyledDocument doc,
                               final int line) {
        assert EventQueue.isDispatchThread();
        assert line >= 0;
        assert editorCookie.getDocument() == doc;
        
        /* offset must be computed here so that it is available to the task: */
        final int offset = getCursorOffset(doc, line);
        
        class SetCursorTask implements Runnable {
            private boolean completed = false;
            private PropertyChangeListener listenerToUnregister;
            private boolean perform() {
                if (EventQueue.isDispatchThread()) {
                    run();
                } else {
                    try {
                        EventQueue.invokeAndWait(this);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                        
                        completed = true; //so that only one exception is thrown
                    }
                }
                return completed;
            }
            public void run() {
                assert EventQueue.isDispatchThread();

                if (completed) {
                    return;
                }

                JEditorPane[] panes = editorCookie.getOpenedPanes();
                if (panes != null) {
                    panes[0].setCaretPosition(offset);
                    if (listenerToUnregister != null) {
                        ((EditorCookie.Observable) editorCookie)
                        .removePropertyChangeListener(listenerToUnregister);
                    }
                    completed = true;
                }
            }
            private void setListenerToUnregister(PropertyChangeListener l) {
                listenerToUnregister = l;
            }
        }

        final SetCursorTask setCursorTask = new SetCursorTask();
        
        editorCookie.open();
        if (setCursorTask.perform()) {
            return;
        }
        if (editorCookie instanceof EditorCookie.Observable) {
            if (!setCursorTask.perform()) {
                PropertyChangeListener openPanesListener
                        = new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent e) {
                                if (EditorCookie.Observable.PROP_OPENED_PANES
                                        .equals(e.getPropertyName())) {
                                    setCursorTask.perform();
                                }
                            }
                        };
                setCursorTask.setListenerToUnregister(openPanesListener);
                ((EditorCookie.Observable) editorCookie)
                        .addPropertyChangeListener(openPanesListener);
                setCursorTask.perform();
            }
        } else {
            final int numberOfTries = OPEN_EDITOR_TOTAL_TIMEOUT_MS
                                      / OPEN_EDITOR_WAIT_PERIOD_MS;
            for (int i = 0; i < numberOfTries; i++) {
                try {
                    Thread.currentThread().sleep(OPEN_EDITOR_WAIT_PERIOD_MS);
                } catch (InterruptedException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,
                                                     ex);
                }
                if (setCursorTask.perform()) {
                    break;
                }
            }
            if (!setCursorTask.completed) {
                setStatusLine(NbBundle.getMessage(
                        DefaultOpenFileImpl.class,
                        "MSG_couldNotOpenAt"));                         //NOI18N
            }
        }
    }
    
    /**
     * Computes cursor offset of a given line of a document.
     * The line number must be non-negative.
     * If the line number is greater than number of the last line,
     * the returned offset corresponds to the last line of the document.
     *
     * @param  doc  document to computer offset for
     * @param  line  line number (first line = <code>0</code>)
     * @return  cursor offset of the beginning of the given line
     */
    private int getCursorOffset(StyledDocument doc, int line) {
        assert EventQueue.isDispatchThread();
        assert line >= 0;
        
        try {
            return NbDocument.findLineOffset(doc, line);
        } catch (IndexOutOfBoundsException ex) {
            /* probably line number out of bounds */

            Element lineRootElement
                    = NbDocument.findLineRootElement(doc);
            int lineCount = lineRootElement.getElementCount();
            if (line >= lineCount) {
                return NbDocument.findLineOffset(doc, lineCount - 1);
            } else {
                throw ex;
            }
        }
    }
    
    /**
     * Activates the specified cookie, thus opening a file.
     * The file is specified by the cookie, because the cookie was obtained
     * from it. The cookie must be one of <code>EditorCookie</code>
     * <code>OpenCookie</code>, <code>EditCookie</code>,
     * <code>ViewCookie</code>.
     *
     * @param  cookie  cookie to activate
     * @param  cookieClass  type of the cookie - specifies action to activate
     * @param  line  used only by <code>EditorCookie</code>s&nbsp;-
     *               specifies initial line to open the file at
     * @return  <code>true</code> if the cookie was successfully activated,
     *          <code>false</code> if some error occured
     * @exception  java.lang.IllegalArgumentException
     *             if <code>cookieClass</code> is not any of
     *             <code>EditorCookie</code>, <code>OpenCookie</code>,
     *             <code>ViewCookie</code>
     * @exception  java.lang.ClassCastException
     *             if the <code>cookie</code> is not an instance
     *             of the specified cookie class
     */
    protected boolean openByCookie(Node.Cookie cookie,
                                   Class cookieClass,
                                   final int line) {
        assert EventQueue.isDispatchThread();
        
        if ((cookieClass == EditorCookie.Observable.class)
                || (cookieClass == EditorCookie.Observable.class)) {
            return openEditor((EditorCookie) cookie, line);
        } else if (cookieClass == OpenCookie.class) {
            ((OpenCookie) cookie).open();
        } else if (cookieClass == EditCookie.class) {
            ((EditCookie) cookie).edit();
        } else if (cookieClass == ViewCookie.class) {
            ((ViewCookie) cookie).view();
        } else {
            throw new IllegalArgumentException();
        }
        return true;
    }
    
    /**
     * Tries to open the specified file, using one of <code>EditorCookie</code>,
     * <code>OpenCookie</code>, <code>EditCookie</code>, <code>ViewCookie</code>
     * (in the same order).
     * If the client of the open file server wants, waits until the file is
     * closed and notifies the client.
     *
     * @param  dataObject  <code>DataObject</code> representing the file
     * @param  line  if <code>EditorCookie</code> is used,
     *               specifies initial line to open the file at
     * @return  <code>true</code> if the file was successfully open,
     *          <code>false</code> otherwise
     */
    private final boolean openDataObjectByCookie(DataObject dataObject,
                                       int line) {
        
        Class cookieClass;        
        Node.Cookie cookie;
        if( (    cookie = dataObject.getCookie(cookieClass = OpenCookie.class)) != null
             || (cookie = dataObject.getCookie(cookieClass = EditCookie.class)) != null
             || (cookie = dataObject.getCookie(cookieClass = ViewCookie.class)) != null) {
            return openByCookie(cookie, cookieClass, line);
        }
        return false;
    }
    
    /**
     * This method is called when it is rescheduled to the AWT thread.
     * (from a different thread). It is always run in the AWT thread.
     */
    public void run() {
        assert EventQueue.isDispatchThread();
        
        open(fileObject, line);
    }
    
    /**
     * Opens the <code>FileObject</code> either by calling {@link EditorCookie}
     * (or {@link OpenCookie} or {@link ViewCookie}),
     * or by showing it in the Explorer.
     */
    public boolean open(final FileObject fileObject, int line) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(
                    new DefaultOpenFileImpl(fileObject, line));
            return true;
        }
        
        
        assert EventQueue.isDispatchThread();

        String fileName = fileObject.getNameExt();
                  
        /* Find a DataObject for the FileObject: */
        final DataObject dataObject;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }

        Class cookieClass;        
        Node.Cookie cookie;
        
        if ( (line != -1 && ((cookie = dataObject.getCookie(cookieClass = EditorCookie.Observable.class)) != null
             || (cookie = dataObject.getCookie(cookieClass = EditorCookie.class)) != null)) ){
            boolean ret = openByCookie(cookie,cookieClass, line);      
            clearStatusLine();                              
            return ret;
        } 
                            
        /* try to open the object using the default action */
        final Node dataNode = dataObject.getNodeDelegate();        
        final Action action = dataNode.getPreferredAction();
        if (action != null && !(action instanceof FileSystemAction)) {            
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    action.actionPerformed(new ActionEvent(dataNode, 0, null));                                 
                    clearStatusLine();            
                }
            });            
            return true;            
        }             
        
        /* Try to grab an editor/open/edit/view cookie and open the object: */
        setStatusLineOpening(fileName);
        boolean success = openDataObjectByCookie(dataObject, line);
        clearStatusLine();
        if (success) {
            return true;
        }
        
        if (
            ZIP_EXT.equalsIgnoreCase(fileObject.getExt()) || 
            JAR_EXT.equalsIgnoreCase(fileObject.getExt()) ||
            fileObject.isFolder()
        ) {
            // select it in explorer:
            Node node = dataObject.getNodeDelegate();
            if (node != null) {
                NodeOperation.getDefault().explore(node);
                return true;
            } else {
                return false;
            }
        }
        
        return false;
    }
    
}
