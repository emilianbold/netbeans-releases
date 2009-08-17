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

package org.netbeans.spi.debugger.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 * Dispatcher of context-related events and provider of active elements in the IDE.
 * 
 * <p>This class tracks the changes of the selected file and active editor and re-fires
 * the changes to registered listeners. The listeners can register based on
 * a MIME type of files which they are interested in. This prevents from unnecessary
 * activity of debugging actions when the context is switched among unrelated files.
 * 
 * <p>The EditorContextDispatcher provides convenient access to currently selected
 * elements and recently selected elements in the GUI.
 * 
 * <H2>Typical usage:</H2>
 * Attach a listener based on file MIME type. The usage of WeakListeners is
 * preferred, unless the listener can be removed explicitely.
 * <pre>
 *  EditorContextDispatcher.getDefault().addPropertyChangeListener("&lt;MIME type&gt;",
 *              WeakListeners.propertyChange(dispatchListener, EditorContextDispatcher.getDefault()));
 * </pre>
 * Then use <code>getCurrent*()</code> methods to find the currently selected
 * elements in the IDE.
 * If recently selected elements are desired, use <code>getMostRecent*()</code>
 * methods. They provide current elements if available, or elements that were
 * current the last time.
 * 
 * 
 * @author Martin Entlicher
 * @since 2.13
 */
public final class EditorContextDispatcher {

    private static final Logger logger = Logger.getLogger(EditorContextDispatcher.class.getName());
    
    /**
     * Name of property fired when the current file changes.
     */
    public static final String PROP_FILE = "file";  // NOI18N
    /**
     * Name of property fired when the current editor changes.
     */
    public static final String PROP_EDITOR = "editor";  // NOI18N
    
    private static EditorContextDispatcher context;
    
    /**
     * Get the default instance of EditorContextDispatcher.
     * @return The EditorContextDispatcher
     */
    public static synchronized EditorContextDispatcher getDefault() {
        if (context == null) {
            context = new EditorContextDispatcher();
        }
        return context;
    }

    private final EditorLookupListener editorLookupListener;
    
    private final RequestProcessor refreshProcessor;
    private final Lookup.Result<FileObject> resFileObject;
    private final Lookup.Result<EditorCookie> resEditorCookie;
    private final PropertyChangeListener  tcListener;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Map<String, PropertyChangeSupport> pcsByMIMEType = new HashMap<String, PropertyChangeSupport>();
    
    private String lastFiredMIMEType = null;
    private Map<String, Object> lastMIMETypeEvents = new HashMap<String, Object>();

    private static final Reference<FileObject> NO_FILE = new WeakReference<FileObject>(null);
    private static final Reference<EditorCookie> NO_COOKIE = new WeakReference<EditorCookie>(null);
    private static final Reference<JEditorPane> NO_EDITOR = new WeakReference<JEditorPane>(null);

    private String currentURL;
    private Reference<FileObject> currentFile = NO_FILE;
    private Reference<EditorCookie> currentEditorCookie = NO_COOKIE;
    private Reference<JEditorPane> currentOpenedPane = NO_EDITOR;
    
    // Most recent in editor:
    private Reference<FileObject> mostRecentFileRef = NO_FILE;
    private Reference<EditorCookie> mostRecentEditorCookieRef = NO_COOKIE;
    private Reference<JEditorPane> mostRecentOpenedPaneRef = NO_EDITOR;
    
    private EditorContextDispatcher() {
        refreshProcessor = new RequestProcessor("Refresh Editor Context", 1);   // NOI18N
        
        resFileObject = Utilities.actionsGlobalContext().lookupResult(FileObject.class);
        resFileObject.addLookupListener(new EditorLookupListener(FileObject.class));
        resFileObject.allItems();
        
        resEditorCookie = Utilities.actionsGlobalContext().lookupResult(EditorCookie.class);
        editorLookupListener = new EditorLookupListener(EditorCookie.class);
        resEditorCookie.addLookupListener(editorLookupListener);
        resEditorCookie.allItems();

        tcListener = new EditorLookupListener(TopComponent.class);
        TopComponent.getRegistry ().addPropertyChangeListener (WeakListeners.propertyChange(
                tcListener, TopComponent.getRegistry()));
    }
    
    /**
     * Get the current active file.
     * @return The current file or <code>null</code> when there is no active file.
     */
    public synchronized FileObject getCurrentFile() {
        return currentFile.get();
    }
    
    /**
     * Get the String representation of URL of the current active file.
     * @return The String representation of URL of the current active file or
     *         an empty String when there is no active file.
     */
    public synchronized String getCurrentURLAsString() {
        if (currentURL == null) {
            FileObject fo = getCurrentFile();
            if (fo != null) {
                try {
                    currentURL = fo.getURL().toString ();
                } catch (FileStateInvalidException ex) {}
            }
            if (currentURL == null) {
                currentURL = ""; // NOI18N
            }
        }
        return currentURL;
    }
    
    /**
     * Get the {@link org.openide.cookies.EditorCookie} of currently edited file.
     * @return The current {@link org.openide.cookies.EditorCookie} or
     *         <code>null</code> when there is no currently edited file.
     */
    private synchronized EditorCookie getCurrentEditorCookie() {
        if (getCurrentEditor() != null) {
            return currentEditorCookie.get();
        } else {
            return null;
        }
    }
    
    /**
     * Get the {@link javax.swing.JEditorPane} of currently edited file.
     * @return The current {@link javax.swing.JEditorPane} or
     *         <code>null</code> when there is no currently edited file.
     */
    public synchronized JEditorPane getCurrentEditor() {
        return currentOpenedPane.get();
    }
    
    /**
     * Get the line number of the caret in the current editor.
     * @return the line number or <code>-1</code> when there is no current editor.
     */
    public int getCurrentLineNumber() {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return -1;
        JEditorPane ep = getCurrentEditor ();
        if (ep == null) return -1;
        StyledDocument d = e.getDocument ();
        if (d == null) return -1;
        Caret caret = ep.getCaret ();
        if (caret == null) return -1;
        int ln = NbDocument.findLineNumber (
            d,
            caret.getDot ()
        );
        return ln + 1;
    }
    
    /**
     * Get the line of the caret in the current editor.
     * @return the line or <code>null</code> when there is no current editor.
     */
    public Line getCurrentLine() {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane ep = getCurrentEditor ();
        if (ep == null) return null;
        StyledDocument d = e.getDocument ();
        if (d == null) return null;
        Caret caret = ep.getCaret ();
        if (caret == null) return null;
        int lineNumber = NbDocument.findLineNumber(d, caret.getDot());
        Line.Set lineSet = e.getLineSet();
        try {
            assert lineSet != null : e;
            return lineSet.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
    
    /**
     * Get the most recent active file. This returns the active file if there's
     * one, or a file, that was most recently active.
     * @return The most recent file or <code>null</code> when there was no recent
     * active file.
     */
    public synchronized FileObject getMostRecentFile() {
        return mostRecentFileRef.get();
    }

    /**
     * Get the String representation of URL of the most recent active file.
     * @return The String representation of URL of the most recent file or
     *         an empty String when there was no recent active file.
     */
    public synchronized String getMostRecentURLAsString() {
        FileObject fo = getMostRecentFile();
        if (fo != null) {
            try {
                return fo.getURL().toString ();
            } catch (FileStateInvalidException ex) {}
        }
        return ""; // NOI18N
    }
    
    private synchronized EditorCookie getMostRecentEditorCookie() {
        if (getMostRecentEditor() != null) {
            return mostRecentEditorCookieRef.get();
        } else {
            return null;
        }
    }
    
    public synchronized JEditorPane getMostRecentEditor() {
        return mostRecentOpenedPaneRef.get();
    }
    
    /**
     * Get the line number of the caret in the most recent editor.
     * This returns the current line number in the current editor if there's one,
     * or a line number of the caret in the editor, that was most recently active.
     * @return the line number or <code>-1</code> when there was no recent active editor.
     */
    public int getMostRecentLineNumber() {
        EditorCookie e = getMostRecentEditorCookie ();
        if (e == null) return -1;
        JEditorPane ep = getMostRecentEditor ();
        if (ep == null) return -1;
        StyledDocument d = e.getDocument ();
        if (d == null) return -1;
        Caret caret = ep.getCaret ();
        if (caret == null) return -1;
        int ln = NbDocument.findLineNumber (
            d,
            caret.getDot ()
        );
        return ln + 1;
    }
    
    /**
     * Get the line of the caret in the most recent editor.
     * This returns the current line in the current editor if there's one,
     * or a line of the caret in the editor, that was most recently active.
     * @return the line or <code>null</code> when there was no recent active editor.
     */
    public Line getMostRecentLine() {
        EditorCookie e = getMostRecentEditorCookie ();
        if (e == null) return null;
        JEditorPane ep = getMostRecentEditor ();
        if (ep == null) return null;
        StyledDocument d = e.getDocument ();
        if (d == null) return null;
        Caret caret = ep.getCaret ();
        if (caret == null) return null;
        int lineNumber = NbDocument.findLineNumber(d, caret.getDot());
        Line.Set lineSet = e.getLineSet();
        try {
            return lineSet.getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
    
    /**
     * Add a PropertyChangeListener to this context dispatcher.
     * It's strongly suggested to use {@link #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
     * instead, if possible, for performance reasons.
     * @param l The PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**
     * Remove a PropertyChangeListener from this context dispatcher.
     * @param l The PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
        // Also remove the listener from all MIME types
        synchronized (pcsByMIMEType) {
            Set<String> MIMETypes = new HashSet(pcsByMIMEType.keySet());
            for (String MIMEType : MIMETypes) {
                PropertyChangeSupport _pcs = pcsByMIMEType.get(MIMEType);
                _pcs.removePropertyChangeListener(l);
                if (_pcs.getPropertyChangeListeners().length == 0) {
                    pcsByMIMEType.remove(MIMEType);
                }
            }
        }
    }
    
    /**
     * Add a PropertyChangeListener to this context dispatcher to be notified
     * about changes of files with a specified MIME type.
     * @param MIMEType The MIME type to report changes for
     * @param l The PropertyChangeListener
     */
    public void addPropertyChangeListener(String MIMEType, PropertyChangeListener l) {
        PropertyChangeSupport _pcs;
        synchronized (pcsByMIMEType) {
            _pcs = pcsByMIMEType.get(MIMEType);
            if (_pcs == null) {
                _pcs = new PropertyChangeSupport(this);
                pcsByMIMEType.put(MIMEType, _pcs);
            }
        }
        _pcs.addPropertyChangeListener(l);
    }
    
    /*public void removePropertyChangeListener(String MIMEType, PropertyChangeListener l) {
        PropertyChangeSupport pcs;
        synchronized (pcsByMIMEType) {
            pcs = pcsByMIMEType.get(MIMEType);
            if (pcs == null) {
                return ;
            }
        }
        pcs.removePropertyChangeListener(l);
    }*/
    
    private void firePropertyChange(PropertyChangeEvent evt, String preferredMIMEType) {
        //System.err.println("EditorContextDispatcher.firePropertyChange("+evt.getPropertyName()+", "+evt.getOldValue()+", "+evt.getNewValue()+")");
        pcs.firePropertyChange(evt);
        if (PROP_FILE.equals(evt.getPropertyName())) {
            // Retrieve the files MIME types and fire to appropriate MIME type listeners:
            FileObject oldFile = (FileObject) evt.getOldValue();
            FileObject newFile = (FileObject) evt.getNewValue();
            String oldMIMEType = (oldFile != null) ? oldFile.getMIMEType() : null;
            String newMIMEType = (newFile != null) ? newFile.getMIMEType() : null;
            PropertyChangeSupport pcsMIMEOld = null, pcsMIMENew = null;
            PropertyChangeEvent evtOld = null, evtNew = null;
            synchronized (pcsByMIMEType) {
                if (oldMIMEType != null && oldMIMEType.equals(newMIMEType)) {
                    pcsMIMEOld = pcsByMIMEType.get(oldMIMEType);
                    evtOld = evt;
                    //System.err.println("EditorContextDispatcher.fireMIMETypeChange("+oldMIMEType+", "+evt.getPropertyName()+", "+evt.getOldValue()+", "+evt.getNewValue()+")");
                } else {
                    if (oldMIMEType != null) {
                        pcsMIMEOld = pcsByMIMEType.get(oldMIMEType);
                        if (pcsMIMEOld != null) {
                            evtOld = new PropertyChangeEvent(evt.getSource(),
                                                             evt.getPropertyName(),
                                                             evt.getOldValue(),
                                                             null);
                        }
                        //System.err.println("EditorContextDispatcher.fireMIMETypeChange("+oldMIMEType+", "+evt.getPropertyName()+", "+evt.getOldValue()+", null)");
                    }
                    if (newMIMEType != null) {
                        pcsMIMENew = pcsByMIMEType.get(newMIMEType);
                        if (pcsMIMENew != null) {
                            evtNew = new PropertyChangeEvent(evt.getSource(),
                                                             evt.getPropertyName(),
                                                             null,
                                                             evt.getNewValue());
                        }
                        //System.err.println("EditorContextDispatcher.fireMIMETypeChange("+newMIMEType+", "+evt.getPropertyName()+", null, "+evt.getNewValue()+")");
                    }
                }
            }
            if (pcsMIMEOld != null) {
                pcsMIMEOld.firePropertyChange(evtOld);
            }
            if (pcsMIMENew != null) {
                pcsMIMENew.firePropertyChange(evtNew);
            }
            // Now check, whether the MIME type has changed and whether we should
            // fire non-file change events with 'null' new values to listeners
            // registered for a particular MIME type:
            if (oldMIMEType != null && !oldMIMEType.equals(newMIMEType) && pcsMIMEOld != null) {
                String lastMIMEType;
                Map<String, Object> lastEvents;
                synchronized (this) {
                    lastMIMEType = lastFiredMIMEType;
                    lastEvents = new HashMap(lastMIMETypeEvents);
                    if (lastMIMEType != null && lastMIMEType.equals(oldMIMEType)) {
                        lastFiredMIMEType = null;
                        lastMIMETypeEvents.clear();
                    } else {
                        lastEvents = null;
                    }
                }
                if (lastEvents != null) {
                    for (String property : lastEvents.keySet()) {
                        pcsMIMEOld.firePropertyChange(property, lastEvents.get(property), null);
                    }
                }
            }
        } else {
            PropertyChangeSupport pcsMIME = null;
            if (preferredMIMEType != null) {
                synchronized (pcsByMIMEType) {
                    pcsMIME = pcsByMIMEType.get(preferredMIMEType);
                }
                if (pcsMIME != null) {
                    pcsMIME.firePropertyChange(evt);
                }
            }
            //System.err.println("EditorContextDispatcher.fireMIMETypeChange("+preferredMIMEType+", "+evt.getPropertyName()+", "+evt.getOldValue()+", "+evt.getNewValue()+")");
            synchronized (this) {
                if (pcsMIME != null) {
                    lastFiredMIMEType = preferredMIMEType;
                    lastMIMETypeEvents.put(evt.getPropertyName(), evt.getNewValue());
                } else {
                    lastFiredMIMEType = null;
                    lastMIMETypeEvents.clear();
                }
            }
        }
    }

    private class EditorLookupListener extends Object implements LookupListener, PropertyChangeListener {
        
        private Class type;
        
        public EditorLookupListener(Class type) {
            this.type = type;
        }
        
        public void resultChanged(LookupEvent ev) {
            //System.err.println("EditorContextDispatcher.resultChanged(), type = "+type);
            if (type == FileObject.class) {
                Collection<? extends FileObject> fos = resFileObject.allInstances();
                FileObject oldFile;
                FileObject newFile;
                synchronized (EditorContextDispatcher.this) {
                    oldFile = currentFile.get();
                    if (fos.size() == 0) {
                        newFile = null;
                    } else if (fos.size() == 1) {
                        newFile = fos.iterator().next();
                    } else {
                        newFile = findPrimary(fos);
                    }
                    //System.err.println("\nCURRENT FILES = "+fos+"\n");
                    currentFile = newFile == null ? NO_FILE : new WeakReference<FileObject>(newFile);
                    currentURL = null;
                    /*if (newFile != null) {  - NO, we need the last file in editor.
                        mostRecentFileRef = new WeakReference(newFile);
                    }*/
                }
                if (oldFile != newFile) {
                    refreshProcessor.post(new EventFirer(PROP_FILE, oldFile, newFile));
                }
            } else if (type == EditorCookie.class) {
                Collection<? extends EditorCookie> ecs = resEditorCookie.allInstances();
                final EditorCookie newEditor;
                final EditorCookie oldEditor;
                synchronized (EditorContextDispatcher.this) {
                    oldEditor = currentEditorCookie.get();
                    if (oldEditor instanceof EditorCookie.Observable) {
                        ((EditorCookie.Observable) oldEditor).removePropertyChangeListener(this);
                    }
                    if (ecs.size() == 0) {
                        newEditor = null;
                    } else {
                        newEditor = ecs.iterator().next();
                    }
                    if (newEditor instanceof EditorCookie.Observable) {
                        ((EditorCookie.Observable) newEditor).addPropertyChangeListener(this);
                    }
                    currentEditorCookie = newEditor == null ? NO_COOKIE : new WeakReference<EditorCookie>(newEditor);
                    if (currentFile.get() != null) {
                        if (newEditor != null) {
                            mostRecentEditorCookieRef = new WeakReference<EditorCookie>(newEditor);
                        }
                    }
                }
                if (newEditor != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        // getOpenedPanes() MUST be called on AWT.
                        public void run() {
                            updateCurrentOpenedPane(TopComponent.getRegistry().getActivated(), newEditor);
                        }
                    });
                } else if (oldEditor != newEditor) {
                    //  newEditor == null
                    refreshProcessor.post(new EventFirer(PROP_EDITOR, oldEditor, newEditor, null));
                }
                /* Fire the editor event only when JEditorPane is set/unset
                if (oldEditor != newEditor) {
                //    refreshProcessor.post(new EventFirer(PROP_EDITOR, oldEditor, newEditor, MIMEType));
                }
                 */
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            //System.err.println("EditorContextDispatcher.propertyChanged("+evt.getPropertyName()+": "+evt.getOldValue()+", "+evt.getNewValue()+"), type = "+type);
            if (type == TopComponent.class) {
                if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED)) {
                    TopComponent newComponnet = (TopComponent) evt.getNewValue();
                    updateCurrentOpenedPane(newComponnet, null);
                }
            }
            if (evt.getSource() instanceof EditorCookie.Observable) {
                final Object source = evt.getSource();
                SwingUtilities.invokeLater(new Runnable() {
                    // getOpenedPanes() MUST be called on AWT.
                    public void run() {
                        updateCurrentOpenedPane(TopComponent.getRegistry().getActivated(), source);
                    }
                });
            }
        }

        private void updateCurrentOpenedPane(TopComponent activeComponent, Object source) {
            JEditorPane oldEditor = null;
            JEditorPane newEditor = null;
            String MIMEType = null;
            synchronized (EditorContextDispatcher.this) {
                boolean isSetPane = false;
                EditorCookie ec = currentEditorCookie.get();
                if ((source == null || source == ec)) {
                    oldEditor = currentOpenedPane.get();
                    if (ec != null && activeComponent != null) {
                        if (ec.getDocument() == null &&  // !currentEditorCookie.prepareDocument().isFinished() &&
                            (ec instanceof EditorCookie.Observable)) {
                            // Document is not yet loaded, wait till we're notified that it is.
                            // See issue #147988
                            logger.fine("Document "+ ec +" NOT yet loaded...");  // NOI18N
                            return ;
                        }
                        logger.fine("Document " + ec + " loaded, updating...");  // NOI18N
                        long t1 = System.nanoTime();
                        JEditorPane openedPane = NbDocument.findRecentEditorPane(ec);
                        long t2 = System.nanoTime();
                        logger.fine("Time to find opened panes = "+(t2 - t1)+" ns = "+(t2 - t1)/1000000+" ms.");  // NOI18N
                        if (openedPane != null) {
                            assert activeComponent.isAncestorOf(openedPane) : "Active component must contain opened pane.";
                            newEditor = openedPane;
                            isSetPane = true;
                        }
                    }
                    if (!isSetPane && source == null) {
                        newEditor = null;
                    }
                    currentOpenedPane = newEditor == null ? NO_EDITOR : new WeakReference<JEditorPane>(newEditor);
                    FileObject f = currentFile.get();
                    if (f != null) {
                        MIMEType = f.getMIMEType();
                        if (newEditor != null) {
                            mostRecentOpenedPaneRef = new WeakReference<JEditorPane>(newEditor);
                            mostRecentFileRef = new WeakReference<FileObject>(f);
                            if (ec != null) {
                                mostRecentEditorCookieRef = new WeakReference<EditorCookie>(ec);
                            }
                        }
                    } else {
                        MIMEType = null;
                    }
                    //System.err.println("\nCurrent Opened Pane = "+currentOpenedPane+", currentFile = "+currentFile+"\n");
                }
            }
            if (oldEditor != newEditor) {
                refreshProcessor.post(new EventFirer(PROP_EDITOR, oldEditor, newEditor, MIMEType));
            }
        }
        
        private FileObject findPrimary(Collection<? extends FileObject> fos) {
            for (FileObject fo : fos) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    if (fo.equals(dobj.getPrimaryFile())) {
                        return fo;
                    }
                } catch (DataObjectNotFoundException ex) {}
            }
            // No primary file, return just the first one:
            return fos.iterator().next();
        }
        
    }
    
    private final class EventFirer implements Runnable {
        
        private final PropertyChangeEvent evt;
        private final String MIMEType;
        
        public EventFirer(String propertyName, Object oldValue, Object newValue) {
            this(propertyName, oldValue, newValue, null);
        }
        
        public EventFirer(String propertyName, Object oldValue, Object newValue, String MIMEType) {
            this.evt = new PropertyChangeEvent(EditorContextDispatcher.this, propertyName, oldValue, newValue);
            this.MIMEType = MIMEType;
        }

        public void run() {
            firePropertyChange(evt, MIMEType);
        }
        
    }
    
}
