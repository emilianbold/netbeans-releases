/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.openfile;

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
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.openfile.cli.Callback;
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


/**
 * Opens files when requested. Main functionality.
 *
 * @author Jaroslav Tulach, Jesse Glick, Marian Petras, David Konecny
 */
public class DefaultOpenFileImpl implements OpenFileImpl {
    
    /** extenstion for .java files (including the dot) */
    static final String JAVA_EXT = ".JAVA";                             //NOI18N
    /** extension for .txt files (including the dot) */
    static final String TXT_EXT = ".TXT";                               //NOI18N

    private static final String ZIP_EXT = "zip"; //NOI18N
    private static final String JAR_EXT = "jar"; //NOI18N

    /** Creates a new instance of OpenFileImpl */
    public DefaultOpenFileImpl() {
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
    protected void setStatusLineOpening(String fileName, boolean waiting) {
        setStatusLine(NbBundle.getMessage(
                OpenFileImpl.class,
                waiting ? "MSG_openingAndWaiting"                       //NOI18N
                        : "MSG_opening",                                //NOI18N
                fileName));
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
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                NbBundle.getMessage(OpenFileImpl.class,
                                    "MSG_cannotOpenWillClose",          //NOI18N
                                    fileName)));
    }
    
    /**
     * Returns an explorer node for the specified <code>FileObject</code>.
     *
     * @param  fileObject  <code>FileObject</code> to return a node for
     * @param  dataObject  <code>DataObject</code> representing
     *                     the <code>FileObject</code>
     * @return  node representing the specified <code>FileObject</code>
     */
    private final Node getNodeFor(FileObject fileObject,
                                         DataObject dataObject) {
        return dataObject.getNodeDelegate();
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
        if ((cookieClass == EditorCookie.Observable.class)
                || (cookieClass == EditorCookie.class)) {
            final EditorCookie editorCookie = (EditorCookie) cookie;
            editorCookie.open();
            final StyledDocument doc;
            
            /* get the document: */
            try {
                doc = editorCookie.openDocument();
            } catch (IOException ex) {
                String msg = NbBundle.getMessage(
                        OpenFileImpl.class,
                        "MSG_cannotOpenWillClose");                     //NOI18N
                ErrorManager.getDefault().notify(
                        ErrorManager.EXCEPTION,
                        ErrorManager.getDefault().annotate(ex, msg));
                clearStatusLine();
                return false;
            }
            
            /* get the target cursor offset: */
            int lineOffset;
            try {
                lineOffset = (line >= 0) ? NbDocument.findLineOffset(doc, line)
                                         : 0;
            } catch (IndexOutOfBoundsException ex) {
                /* probably line number out of bounds */
                
                Element lineRootElement
                        = NbDocument.findLineRootElement(doc);
                int lineCount = lineRootElement.getElementCount();
                if (line >= lineCount) {
                    lineOffset = NbDocument.findLineOffset(doc, lineCount - 1);
                } else {
                    throw ex;
                }
            }
            final int offset = lineOffset;
            
            class OpenDocAtLineTask implements Runnable {
                private boolean completed = false;
                private PropertyChangeListener listenerToUnregister;
                private boolean perform() {
                    if (EventQueue.isDispatchThread()) {
                        run();
                    } else {
                        try {
                            EventQueue.invokeAndWait(this);
                        } catch (InterruptedException ex) {
                            ErrorManager.getDefault().notify(ex);
                        } catch (InvocationTargetException ex) {
                            ErrorManager.getDefault().notify(
                                    ex.getTargetException());
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
            
            final OpenDocAtLineTask openTask = new OpenDocAtLineTask();
            if (openTask.perform()) {
                return true;
            }
            
            if (cookieClass != EditorCookie.Observable.class) {
                setStatusLine(NbBundle.getMessage(
                        OpenFileImpl.class,
                        "MSG_couldNotOpenAt"));                         //NOI18N
                return true;
            }
            
            final EditorCookie.Observable obEdCookie
                                          = (EditorCookie.Observable) cookie;
            PropertyChangeListener openPanesListener
                    = new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent e) {
                            if (EditorCookie.Observable.PROP_OPENED_PANES
                                    .equals(e.getPropertyName())) {
                                openTask.perform();
                            }
                        }
                    };
            openTask.setListenerToUnregister(openPanesListener);

            obEdCookie.addPropertyChangeListener(openPanesListener);
            openTask.perform();
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
        Node.Cookie cookie;
        Class cookieClass;
        if ((line != -1 && ((cookie = dataObject.getCookie(cookieClass = EditorCookie.Observable.class)) != null
                            || (cookie = dataObject.getCookie(cookieClass = EditorCookie.class)) != null))
                || (cookie = dataObject.getCookie(cookieClass = OpenCookie.class)) != null
                || (cookie = dataObject.getCookie(cookieClass = EditCookie.class)) != null
                || (cookie = dataObject.getCookie(cookieClass = ViewCookie.class)) != null) {
            return openByCookie(cookie, cookieClass, line);
        }
        return false;
    }
    
    /**
     * Tries to open the specified file using the default action
     * of a node representing the file.
     *
     * @param  fileObject  <code>FileObject</code> representing the file to open
     * @param  dataObject  <code>DataObject</code> representing the file
     */
    private final void openByNode(FileObject fileObject,
                                  DataObject dataObject) {
        Node node = getNodeFor(fileObject, dataObject);

        // PENDING Opening in new explorer window was submitted as bug (#8809).
        // Here we check if the data object is default data one, 
        // and try to change it to text one. 
        // 1) We get default data loader,
        // 2) Compare if oyr data object is of deafult data object type,
        // 3) Get its default action
        // 4) If the default action is not FileSystemAction we assume text module
        // is avilable and the default action is Convert to text.
        // 5) Perform the action, find changed data object and open it.
        boolean opened = false;
        DataLoader defaultLoader;
        if ((defaultLoader = getDefaultLoader()) != null
                && dataObject.getClass().getName().equals(
                        defaultLoader.getRepresentationClassName())) {
            // Is default data object.
            Action defaultAction = node.getPreferredAction();
            if (defaultAction != null
                    && !(defaultAction instanceof FileSystemAction)) {
                // Now we suppose Convert To Text Action is available.
                defaultAction.actionPerformed(new ActionEvent(node, 0, null)); 
                fileObject.refresh();
                try {
                    DataObject newDataObject = DataObject.find(fileObject);
                    opened = openDataObjectByCookie(newDataObject, 0);
                } catch (DataObjectNotFoundException dnfe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                                     dnfe);
                }
            }
        }
    }
    
    /**
     * Gets the default system <code>DataLoader</code>.
     *
     * @return  default <code>DataLoader</code>
     */
    private final DataLoader getDefaultLoader() {
        DataLoader defaultLoader = null;
        DataLoaderPool loaderPool
            = (DataLoaderPool) Lookup.getDefault().lookup(DataLoaderPool.class);
        
        /* default loader is the last loader in the enumeration of loaders: */
        for (Enumeration loaders = loaderPool.allLoaders();
             loaders.hasMoreElements();
             defaultLoader = (DataLoader) loaders.nextElement());
        return defaultLoader;
    }
    
    /**
     * Opens the <code>FileObject</code> either by calling {@link EditorCookie}
     * (or {@link OpenCookie} or {@link ViewCookie}),
     * or by showing it in the Explorer.
     */
    public boolean open(final FileObject fileObject, int line, Callback.Waiter waiter) {

        String fileName = fileObject.getNameExt();
                  
        /* Find a DataObject for the FileObject: */
        final DataObject dataObject;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
        
        /* Try to grab an editor/open/edit/view cookie and open the object: */
        setStatusLineOpening(fileName, waiter != null);
        boolean success = openDataObjectByCookie(dataObject, line);
        clearStatusLine();
        if (success) {
            return true;
        }
        
        if (ZIP_EXT.equalsIgnoreCase(fileObject.getExt()) || JAR_EXT.equalsIgnoreCase(fileObject.getExt())) {
            // select it in explorer:
            Node node = getNodeFor(fileObject, dataObject);
            if (node != null) {
                NodeOperation.getDefault().explore(node);
                return true;
            } else {
                return false;
            }
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // This needs to run in EQ generally.
                openByNode(fileObject, dataObject);
            }
        });
        // XXX if waiter != null, call waiter.done() when the document is closed
        return true;
    }
    
    public synchronized FileObject findFileObject(File f) {
        return FileUtil.toFileObject(FileUtil.normalizeFile(f));
    }
   
}
