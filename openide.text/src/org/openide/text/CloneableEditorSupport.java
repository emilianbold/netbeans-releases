/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.text;

import java.awt.Component;
import java.awt.Container;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

//import org.openide.util.actions.SystemAction;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.UserQuestionException;
import org.openide.windows.*;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import java.util.*;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.UserCancelException;


/** Support for associating an editor and a Swing {@link Document}.
* Can be assigned as a cookie to any editable data object.
* This class is abstract, so any subclass has to provide implementation
* for abstract method (usually for generating of messages) and also
* provide environment {@link Env} to give this support access to
* input/output streams, mime type and other features of edited object.
*
* <P>
* This class implements methods of the interfaces
* {@link org.openide.cookies.EditorCookie}, {@link org.openide.cookies.OpenCookie},
* {@link org.openide.cookies.EditCookie},
* {@link org.openide.cookies.ViewCookie}, {@link org.openide.cookies.LineCookie},
* {@link org.openide.cookies.CloseCookie}, and {@link org.openide.cookies.PrintCookie}
* but does not implement
* those interfaces. It is up to the subclass to decide which interfaces
* really implement and which not.
*
* @author Jaroslav Tulach
*/
public abstract class CloneableEditorSupport extends CloneableOpenSupport {
    private static final RequestProcessor RP = new RequestProcessor("org.openide.text Document Processing");
    
    /** Common name for editor mode. */
    public static final String EDITOR_MODE = "editor"; // NOI18N
    private static final String PROP_PANE = "CloneableEditorSupport.Pane"; //NOI18N
    private static final int DOCUMENT_NO = 0;
    private static final int DOCUMENT_LOADING = 1;
    private static final int DOCUMENT_READY = 2;
    private static final int DOCUMENT_RELOADING = 3;

    /** Used for allowing to pass getDocument method
     * when called from loadDocument. */
    private static final ThreadLocal<Boolean> LOCAL_LOAD_TASK = new ThreadLocal<Boolean>();

    /** error manager for CloneableEditorSupport logging and error reporting */
    private static final Logger ERR = Logger.getLogger("org.openide.text.CloneableEditorSupport"); // NOI18N

    /** Flag saying if the CloneableEditorSupport handles already the UserQuestionException*/
    private boolean inUserQuestionExceptionHandler;

    /** Task for preparing the document. Consists for loading a document,
    * firing </code>stateChange</code> and
    * initializing it by attaching listeners listening to document changes, such as SavingManager and
    * LineSet.
    */
    private Task prepareTask;
    
    /** editor kit to work with */
    private EditorKit kit;

    /** document we work with */
    private StrongRef doc;

    /** Lock used for access to <code>doc</code> variable. */
    private final Object LOCK_STRONG_REF = new Object();
    
    /** State of doc reference, it is set to true in prepareDocument when StrongRef is created
     * and set to false when document loading is finished. It helps to reset doc reference to weak just once. */
    private boolean isStrongSet = false;

    private int counterGetDocument = 0;
    private int counterOpenDocument = 0;
    private int counterPrepareDocument = 0;
    private int counterOpenAtImpl = 0;

    /** Non default MIME type used to editing */
    private String mimeType;

    /** Actions to show in toolbar */

    //    private SystemAction[] actions;

    /** Listener to the document changes and all other changes */
    private Listener listener;

    /** the undo/redo manager to use for this document */
    private UndoRedo.Manager undoRedo;

    /** lines set for this object */
    private Line.Set lineSet;

    /** Helper variable to prevent multiple cocurrent printing of this
     * instance. */
    private boolean printing;

    /** Lock used for access to <code>printing</code> variable. */
    private final Object LOCK_PRINTING = new Object();

    /** position manager */
    private PositionRef.Manager positionManager;

    /** Listeners for the changing of the state - document in memory X closed. */
    private Set<ChangeListener> listeners;

    /** last selected editor pane. */
    private transient Reference<Pane> lastSelected;

    /** The time of the last save to determine the real external modifications */
    private long lastSaveTime;

    /** Whether the reload dialog is currently opened. Prevents poping of multiple
     * reload dialogs if there is more external saves.
     */
    private boolean reloadDialogOpened;

    /** Support for property change listeners*/
    private PropertyChangeSupport propertyChangeSupport;

    /** context of this editor support */
    private Lookup lookup;

    /** Flag whether the document is already modified or not.*/

    // #34728 performance optimization 
    private boolean alreadyModified;

    /**
     * Whether previous or upcoming undo is being undone
     * once the notifyModified() is prohibited.
     * <br>
     * Also set when document is being reloaded.
     */
    private boolean revertingUndoOrReloading;
    private boolean justRevertedToNotModified;
    private volatile int documentStatus = DOCUMENT_NO;
    private Throwable prepareDocumentRuntimeException;

    /** Reference to WeakHashMap that is used by all Line.Sets created
     * for this CloneableEditorSupport.
     */
    private Map<Line,Reference<Line>> lineSetWHM;
    private boolean annotationsLoaded;

    /** Creates new CloneableEditorSupport attached to given environment.
    *
    * @param env environment that is source of all actions around the
    *    data object
    */
    public CloneableEditorSupport(Env env) {
        this(env, Lookup.EMPTY);
    }

    /** Creates new CloneableEditorSupport attached to given environment.
    *
    * @param env environment that is source of all actions around the
    *    data object
    * @param l the context that will be passed to each Line produced
    *    by this support's Line.Set. The line will return it from Line.getLookup
    *    call
    */
    public CloneableEditorSupport(Env env, Lookup l) {
        super(env);
        this.lookup = l;
    }

    //
    // abstract messages section
    //

    /** Constructs message that should be displayed when the data object
    * is modified and is being closed.
    *
    * @return text to show to the user
    */
    protected abstract String messageSave();

    /** Constructs message that should be used to name the editor component.
    *
    * @return name of the editor
    */
    protected abstract String messageName();
    
    /** Constructs message that should be used to name the editor component
     * in html fashion, with possible coloring, text styles etc.
     *
     * May return null if no html name is needed or available.
     *
     * @return html name of the editor component or null
     * @since 6.8
     */
    protected String messageHtmlName() {
        return null;
    }

    /** Constructs the ID used for persistence of opened editors.
     * Should be overridden to return sane ID of the underlying document,
     * like the name of the disk file.
     *
     * @return ID of the document
     * @since 4.24
     */
    protected String documentID() {
        return messageName();
    }

    /** Text to use as tooltip for component.
     *
     * @return text to show to the user
     */
    protected abstract String messageToolTip();

    /** Computes display name for a line produced
     * by this {@link CloneableEditorSupport#getLineSet }. The default
     * implementation reuses messageName and line number of the line.
     *
     * @param line the line object to compute display name for
     * @return display name for the line like "MyFile.java:243"
     *
     * @since 4.3
     */
    protected String messageLine(Line line) {
        return NbBundle.getMessage(Line.class, "FMT_CESLineDisplayName", messageName(), line.getLineNumber() + 1);
    }

    //
    // Section of getter of default objects
    // 

    /** Getter for the environment that was provided in the constructor.
    * @return the environment
    */
    final Env cesEnv() {
        return (Env) env;
    }

    /** Getter for the kit that loaded the document.
    */
    final EditorKit cesKit() {
        return kit;
    }

    /**
     * Gets the undo redo manager.
     * @return the manager
     */
    protected final synchronized UndoRedo.Manager getUndoRedo() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getUndoRedo();
        }
        
        if (undoRedo == null) {
            undoRedo = createUndoRedoManager();
        }

        return undoRedo;
    }

    /** Provides access to position manager for the document.
    * It maintains a set of positions even the document is in memory
    * or is on the disk.
    *
    * @return position manager
    */
    final synchronized PositionRef.Manager getPositionManager() {
        if (positionManager == null) {
            positionManager = new PositionRef.Manager(this);
        }

        return positionManager;
    }

    void ensureAnnotationsLoaded() {
        if (!annotationsLoaded) {
            /*ERR.log(Level.FINE,"CES.ensureAnnotationsLoaded Enter Asynchronous"
            + " Time:" + System.currentTimeMillis()
            + " Thread:" + Thread.currentThread().getName());*/
            annotationsLoaded = true;

            Line.Set lines = getLineSet();
            for (AnnotationProvider act : Lookup.getDefault().lookupAll(AnnotationProvider.class)) {
                act.annotate(lines, lookup);
            }
        }
    }

    /** When openning of a document fails with an UserQuestionException
     * this is the method that is supposed to handle the communication.
     */
    private void askUserAndDoOpen(UserQuestionException e, Callable<Void> run) {
        while (e != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    e.getLocalizedMessage(), NotifyDescriptor.YES_NO_OPTION
                );
            nd.setOptions(new Object[] { NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION });

            Object res = DialogDisplayer.getDefault().notify(nd);

            if (NotifyDescriptor.OK_OPTION.equals(res)) {
                try {
                    e.confirmed();
                } catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);

                    return;
                }
            } else {
                return;
            }

            e = null;

            try {
                run.call();
            } catch (UserQuestionException ex) {
                e = ex;
            } catch (IOException ex) {
                ERR.log(Level.INFO, null, ex);
            } catch (Exception ex) {
                ERR.log(Level.SEVERE, null, ex);
            }
        }
    }

    /** Overrides superclass method, first processes document preparation.
     * @see #prepareDocument */
    @Override
    public void open() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            redirect.open();
            return;
        }
        
        try {
            if (getListener().loadExc instanceof UserQuestionException) {
                getListener().loadExc = null;
                prepareTask = null;
                documentStatus = DOCUMENT_NO;
            }
            
            //Assign reference to local variable to avoid gc before return
            StyledDocument doc = openDocument();
            super.open();
        } catch (final UserQuestionException e) {
            class Query implements Runnable, Callable<Void> {

                public void run() {
                    askUserAndDoOpen(e, this);
                }

                public Void call() throws IOException {
                    getListener().loadExc = null;
                    prepareTask = null;
                    documentStatus = DOCUMENT_NO;
                    //Assign reference to local variable to avoid gc before return
                    StyledDocument doc = openDocument();

                    CloneableEditorSupport.super.open();
                    return null;
                }

            }

            Query query = new Query();
            Mutex.EVENT.readAccess(query);
        } catch (IOException e) {
            ERR.log(Level.INFO, null, e);
        }
    }

    //
    // EditorCookie.Observable implementation
    // 

    /** Add a PropertyChangeListener to the listener list.
     * See {@link org.openide.cookies.EditorCookie.Observable}.
     * @param l  the PropertyChangeListener to be added
     * @since 3.40
     */
    public final void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        getPropertyChangeSupport().addPropertyChangeListener(l);
    }

    /** Remove a PropertyChangeListener from the listener list.
     * See {@link org.openide.cookies.EditorCookie.Observable}.
     * @param l the PropertyChangeListener to be removed
     * @since 3.40
     */
    public final void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        getPropertyChangeSupport().removePropertyChangeListener(l);
    }

    /** Report a bound property update to any registered listeners.
     * @param propertyName the programmatic name of the property that was changed.
     * @param oldValue rhe old value of the property.
     * @param newValue the new value of the property.
     * @since 3.40
     */
    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        getPropertyChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
    }

    private synchronized PropertyChangeSupport getPropertyChangeSupport() {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }

        return propertyChangeSupport;
    }

    private boolean canReleaseDoc () {
        if ((counterGetDocument == 0) && (counterOpenDocument == 0) &&
            (counterPrepareDocument == 0) && (counterOpenAtImpl == 0)) {
            return true;
        } else {
            return false;
        }
    }

    //
    // EditorCookie implementation
    // 
    // editor cookie .......................................................................

    /** Load the document into memory. This is done
    * in different thread. A task for the thread is returned
    * so anyone may test whether the loading has been finished
    * or is still in process.
    *
    * @return task for control over loading
    */
    public Task prepareDocument() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.prepareDocument();
        }
        synchronized (getLock()) {
            StyledDocument doc = getDoc();
            if ((doc == null) && (documentStatus != DOCUMENT_NO)) {
                //Sync document status
                closeDocument();
            }
            switch (documentStatus) {
            case DOCUMENT_NO:
                documentStatus = DOCUMENT_LOADING;
                counterPrepareDocument++;
                Task t = prepareDocument(false);
                
                t.addTaskListener(new TaskListener() {
                    public void taskFinished(Task task) {
                        counterPrepareDocument--;
                        if (isStrongSet && canReleaseDoc()) {
                            isStrongSet = false;
                            CloneableEditorSupport.this.setStrong(false, true);
                        }
                        task.removeTaskListener(this);
                    }
                });
                
                return t;
                
            default:

                if (prepareTask == null) { // should never happen
                    throw new IllegalStateException();
                }

                return prepareTask;
            }
        }
    }

    /** @param clearDocument indicates whether the document is needed
     *                       to clear before (used for reloading) */
    private Task prepareDocument(final boolean notUsed) {
        assert Thread.holdsLock(getLock());

        if (prepareTask != null) {
            return prepareTask;
        }

        boolean failed = true;
	
        //#144722: Help variable to make sure we always return non null task from prepareDocument
        Task prepareTaskReturn = null;
        try {
            // listen to modifications on env, but remove
            // previous instance first
            env.removePropertyChangeListener(getListener());
            env.addPropertyChangeListener(getListener());

            // after call to this method the originalDoc and kit are initialized
            // in spite of that the document is not yet fully read in
            kit = createEditorKit();

            final StyledDocument[] docToLoad = { getDoc() };
            if (docToLoad[0] == null) {
                docToLoad[0] = createStyledDocument(kit);
                setDoc(docToLoad[0], true);
                isStrongSet = true;

                // here would be the testability hook for issue 56413
                // (Deadlock56413Test), but I use the reflection in the test
                // instead, so the test depends on the above assignment
            } else {
                setDoc(docToLoad[0], true);
                isStrongSet = true;
            }


            // The thread nume should be: "Loading document " + env; // NOI18N
            prepareTask = RP.create(new Runnable() {
                                                   private boolean runningInAtomicLock;
                                                   private boolean fireEvent;

                                                   public void run() {
                                                       doRun();
                                                       docToLoad[0] = null;
                                                   }
                                                   
                                                   private void doRun() {
                                                       // Run the operations under atomic lock primarily due
                                                       // to reload which occurs in a widely published document instance
                                                       // where another threads may operate already
                                                       if (!runningInAtomicLock) {
                                                           runningInAtomicLock = true;
                                                           NbDocument.runAtomic(docToLoad[0], this);
                                                           if (fireEvent) {
                                                               fireDocumentChange(getDoc(), false);
                                                           }
                                                           return;
                                                       }
                                                       // Prevent operating on top of no longer active document
                                                       synchronized (getLock()) {
                                                           if (documentStatus ==
                                                               DOCUMENT_NO) {
                                                               return;
                                                           }
                                                           // Check whether the document to be loaded was not closed
                                                           if (getDoc() != docToLoad[0]) {
                                                               return;
                                                           }
                                                           prepareDocumentRuntimeException = null;
                                                           int targetStatus = DOCUMENT_NO;

                                                           try {
                                                               // uses the listener's run method to initialize whole document
                                                               getListener().run();
                                                               // assign before fireDocumentChange() as listener should be able to access getDocument()
                                                               documentStatus = DOCUMENT_READY;
                                                               fireEvent = true;
                                                               // Confirm that whole loading succeeded
                                                               targetStatus = DOCUMENT_READY;
                                                               // Add undoable listener when all work in
                                                               // atomic action has finished
                                                               // definitively sooner than leaving lock section
                                                               // and notifying al waiters, see #47022
                                                               getDoc().addUndoableEditListener(getUndoRedo());
                                                           } catch (DelegateIOExc t) {
                                                               prepareDocumentRuntimeException = t;
                                                               prepareTask = null;
                                                           } catch (RuntimeException t) {
                                                               prepareDocumentRuntimeException = t;
                                                               prepareTask = null;
                                                               Exceptions.printStackTrace(t);
                                                               throw t;
                                                           } catch (Error t) {
                                                               prepareDocumentRuntimeException = t;
                                                               prepareTask = null;
                                                               Exceptions.printStackTrace(t);
                                                               throw t;
                                                           } finally {
                                                               synchronized (getLock()) {
                                                                   documentStatus = targetStatus;
                                                                   getLock().notifyAll();
                                                               }
                                                           }
                                                       }
                                                   }
                                               });
            prepareTaskReturn = prepareTask;
            ((RequestProcessor.Task)prepareTask).schedule(0);
            if (RP.isRequestProcessorThread()) {
                prepareTask.waitFinished();
            }
	    failed = false;
        } catch (RuntimeException ex) {
            prepareDocumentRuntimeException = ex;
            throw ex;
        } catch (Error err) {
            prepareDocumentRuntimeException = err;
            throw err;
	} finally {
	    if (failed) {
                documentStatus = DOCUMENT_NO;
                getLock().notifyAll();
	    }
        }
        assert prepareTaskReturn != null : "CloneableEditorSupport.prepareDocument must return non null value";
        return prepareTaskReturn;
    }
    
    final void addRemoveDocListener(Document d, boolean add) {
        if (d == null) {
            return;
        }
        if (Boolean.TRUE.equals(d.getProperty("supportsModificationListener"))) { // NOI18N
            if (add) {
                d.putProperty("modificationListener", getListener()); // NOI18N
            } else {
                d.putProperty("modificationListener", null); // NOI18N
            }
        } else {
            if (add) {
                d.addDocumentListener(getListener());
            } else {
                d.removeDocumentListener(getListener());
            }
        }
    }

    /** Clears the <code>doc</code> document. Helper method. */
    private void clearDocument() {
        NbDocument.runAtomic(
            getDoc(),
            new Runnable() {
                public void run() {
                    try {
                        addRemoveDocListener(getDoc(), false);
                        getDoc().remove(0, getDoc().getLength()); 
                        addRemoveDocListener(getDoc(), true);
                    } catch (BadLocationException ble) {
                        ERR.log(Level.INFO, null, ble);
                    }
                }
            }
        
        );
    }

    /** Get the document associated with this cookie.
    * It is an instance of Swing's {@link StyledDocument} but it should
    * also understand the NetBeans {@link NbDocument#GUARDED} to
    * prevent certain lines from being edited by the user.
    * <P>
    * If the document is not loaded the method blocks until
    * it is.
    * 
    * <p>Method will throw {@link org.openide.util.UserQuestionException} exception
    * if file size is too big. This exception could be caught and 
    * its method {@link org.openide.util.UserQuestionException#confirmed} 
    * can be used for confirmation. You need to call {@link #openDocument}}
    * one more time after confirmation.
    *
    * @return the styled document for this cookie that
    *   understands the guarded attribute
    * @exception IOException if the document could not be loaded
    */
    public StyledDocument openDocument() throws IOException {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.openDocument();
        }
        synchronized (getLock()) {
            //It is to avoid gc of loaded document while we work with it
            boolean wasCounterIncremented = false;
            try {
                StyledDocument doc = getDoc();
                if ((doc == null) && (documentStatus != DOCUMENT_NO)) {
                    //Sync document status
                    closeDocument();
                }
                //For DOCUMENT_NO strong reference is set in prepareDocument
                if ((documentStatus == DOCUMENT_READY) || (documentStatus == DOCUMENT_LOADING) || (documentStatus == DOCUMENT_RELOADING)) {
                    counterOpenDocument++;
                    wasCounterIncremented = true;
                    setStrong(true, true);
                }
                try {
                    counterOpenDocument++;
                    doc = openDocumentCheckIOE();
                    return doc;
                } finally {
                    counterOpenDocument--;
                }
            } finally {
                if (wasCounterIncremented) {
                    counterOpenDocument--;
                }
                if (isStrongSet && canReleaseDoc()) {
                    isStrongSet = false;
                    setStrong(false, true);
                }
            }
        }
    }

    private StyledDocument openDocumentCheckIOE() throws IOException {
        StyledDocument locDoc = openDocumentImpl();

        IOException ioe = getListener().checkLoadException();

        if (ioe != null) {
            throw ioe;
        }

        return locDoc;
    }

    /**
     * Must be called under getLock().
     */
    private StyledDocument openDocumentImpl() throws IOException, InterruptedIOException {
        switch (documentStatus) {
        case DOCUMENT_NO:
            documentStatus = DOCUMENT_LOADING;
            prepareDocument(false);

            return openDocumentImpl();

        case DOCUMENT_RELOADING: // proceed to DOCUMENT_READY
        case DOCUMENT_READY:
            StyledDocument document = getDoc();
            assert document != null : "no document although status is " + documentStatus + "; doc=" + doc;
            return document;

        default: // loading

            try {
                getLock().wait();
            } catch (InterruptedException e) {
                throw (InterruptedIOException) new InterruptedIOException().initCause(e);
            }

            if (prepareDocumentRuntimeException != null) {
                if (prepareDocumentRuntimeException instanceof DelegateIOExc) {
                    Exception ex = new Exception();
                    ERR.log(Level.INFO, "Outer callstack", ex);
                    throw (IOException) prepareDocumentRuntimeException.getCause();
                }

                if (prepareDocumentRuntimeException instanceof Error) {
                    throw (Error) prepareDocumentRuntimeException;
                } else {
                    throw (RuntimeException) prepareDocumentRuntimeException;
                }
            }

            return openDocumentImpl();
        }
    }

    Throwable getPrepareDocumentRuntimeException () {
        return prepareDocumentRuntimeException;
    }

    /** Get the document. This method may be called before the document initialization
     * (<code>prepareTask</code>)
     * has been completed, in such a case the document must not be modified.
     * @return document or <code>null</code> if it is not yet loaded
     */
    public StyledDocument getDocument() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getDocument();
        }
        //#149717 Do not block when document is loading
        if (documentStatus != DOCUMENT_READY) {
            return null;
        }
        synchronized (getLock()) {
            StyledDocument doc = getDoc();
            if ((doc == null) && (documentStatus != DOCUMENT_NO)) {
                //Sync document status
                closeDocument();
            }
            while (true) {
                switch (documentStatus) {
                case DOCUMENT_NO:
                    return null;

                default: // ready, loading or reloading

                    if (LOCAL_LOAD_TASK.get() != null) {
                        return getDoc();
                    }

                    try {
                        counterGetDocument++;
                        setStrong(true, true);
                        try {
                            doc = openDocumentCheckIOE();
                            return doc;
                        } catch (IOException e) {
                            return null;
                        }
                    } finally {
                        counterGetDocument--;
                        if (isStrongSet && canReleaseDoc()) {
                            isStrongSet = false;
                            setStrong(false, true);
                        }
                    }
                }
            }
        }
    }

    /** Test whether the document is modified.
    * @return <code>true</code> if the document is in memory and is modified;
    *   otherwise <code>false</code>
    */
    public boolean isModified() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.isModified();
        }
        return cesEnv().isModified();
    }

    /** Save the document in this thread.
    * Create 'orig' document for the case that the save would fail.
    * @exception IOException on I/O error
    */
    public void saveDocument() throws IOException {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            redirect.saveDocument();
            return;
        }
        // #17714: Don't try to save unmodified doc.
        if (!cesEnv().isModified()) {
            return;
        }
        final StyledDocument myDoc = getDocument();
        if (myDoc == null) {
            return;
        }

        long prevLST = lastSaveTime;
        if (prevLST != -1) {
            final long externalMod = cesEnv().getTime().getTime();
            if (externalMod > prevLST) {
                throw new UserQuestionException(mimeType) {
                    @Override
                    public String getLocalizedMessage() {
                        return NbBundle.getMessage(
                            CloneableEditorSupport.class,
                            "FMT_External_change_write",
                            myDoc.getProperty(Document.TitleProperty)
                        );
                    }

                    @Override
                    public void confirmed() throws IOException {
                        setLastSaveTime(externalMod);
                        saveDocument();
                    }
                };
            }
        }

        // save the document as a reader
        class SaveAsReader implements Runnable {
            private boolean doMarkAsUnmodified;
            private IOException ex;

            public void run() {
                try {
                    OutputStream os = null;

                    // write the document
                    long oldSaveTime = lastSaveTime;

                    try {
                        setLastSaveTime(-1);
                        os = new BufferedOutputStream(cesEnv().outputStream());
                        saveFromKitToStream(myDoc, kit, os);

                        os.close(); // performs firing
                        os = null;

                        // remember time of last save
                        ERR.fine("Save ok, assign new time, while old was: " + oldSaveTime); // NOI18N
                        // #149069 - Cannot use System.currentTimeMillis()
                        // because there can be a delay between closing stream
                        // and setting file modification time by OS.
                        setLastSaveTime(cesEnv().getTime().getTime());
                        doMarkAsUnmodified = true;
                        ERR.fine("doMarkAsUnmodified"); // NOI18N
                    } catch (BadLocationException blex) {
                        Exceptions.printStackTrace(blex);
                    } finally {
                        if (lastSaveTime == -1) { // restore for unsuccessful save
                            ERR.fine("restoring old save time"); // NOI18N
                            setLastSaveTime(oldSaveTime);
                        }

                        if (os != null) { // try to close if not yet done
                            os.close();
                        }
                    }

                    // Insert before-save undo event to enable unmodifying undo
                    getUndoRedo().undoableEditHappened(new UndoableEditEvent(this, new BeforeSaveEdit(lastSaveTime)));

                    // update cached info about lines
                    updateLineSet(true);
                    // updateTitles(); radim #58266
                } catch (IOException e) {
                    this.ex = e;
                }
            }

            public void after() throws IOException {
                if (doMarkAsUnmodified) {
                    callNotifyUnmodified();
                }

                if (ex != null) {
                    throw ex;
                }
            }
        }

        // Run before-save actions
        Runnable beforeSaveRunnable = (Runnable) myDoc.getProperty("beforeSaveRunnable");
        if (beforeSaveRunnable != null) {
            beforeSaveRunnable.run();
        }

        SaveAsReader saveAsReader = new SaveAsReader();
        myDoc.render(saveAsReader);
        saveAsReader.after();
    }

    /**
     * Gets editor panes opened by this support.
     * Can be called from AWT event thread only.
     *
     * @return a non-empty array of panes, or null
     * @see EditorCookie#getOpenedPanes
     */
    public JEditorPane[] getOpenedPanes() {
        // expected in AWT only
        assert SwingUtilities.isEventDispatchThread()
                : "CloneableEditorSupport.getOpenedPanes() must be called from AWT thread only"; // NOI18N
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getOpenedPanes();
        }
        
        LinkedList<JEditorPane> ll = new LinkedList<JEditorPane>();
        Enumeration en = allEditors.getComponents();
        
        Pane last = getLastSelected();
        while (en.hasMoreElements()) {
            CloneableTopComponent ctc = (CloneableTopComponent) en.nextElement();
            Pane ed = (Pane) ctc.getClientProperty(PROP_PANE);
            
            if ((ed == null) && ctc instanceof Pane) {
                ed = (Pane) ctc;
            }
            
            if (ed != null) {
                // #23491: pane could be still null, not yet shown component.
                // [PENDING] Right solution? TopComponent opened, but pane not.
                JEditorPane p = ed.getEditorPane();
                
                if (p == null) {
                    continue;
                }
                
                if ((last == ed) ||
                    ((last != null) && (last instanceof Component) && (ed instanceof Container)
                    && ((Container) ed).isAncestorOf((Component) last))) {
                    ll.addFirst(p);
                } else {
                    ll.add(p);
                }
            } else {
                throw new IllegalStateException("No reference to Pane. Please file a bug against openide/text");
            }
        }

        return ll.isEmpty() ? null : ll.toArray(new JEditorPane[ll.size()]);
    }

    /**
     * Gets recently selected editor pane opened by this support
     * Can be called from AWT event thread only. It is nonblocking. It returns either pane
     * if pane intialization is finished or null if initialization is still in progress.
     *
     * @return pane or null
     *
     */
    JEditorPane getRecentPane () {
        // expected in AWT only
        assert SwingUtilities.isEventDispatchThread()
                : "CloneableEditorSupport.getOpenedPaneForTC must be called from AWT thread only"; // NOI18N
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getRecentPane();
        }
        
        Enumeration en = allEditors.getComponents();
        
        Pane last = getLastSelected();
        while (en.hasMoreElements()) {
            CloneableTopComponent ctc = (CloneableTopComponent) en.nextElement();
            Pane ed = (Pane) ctc.getClientProperty(PROP_PANE);
            
            if ((ed == null) && ctc instanceof Pane) {
                ed = (Pane) ctc;
            }
            
            if (ed != null) {
                JEditorPane p = null;
                if ((last == ed) ||
                    ((last != null) && (last instanceof Component) && (ed instanceof Container)
                    && ((Container) ed).isAncestorOf((Component) last))) {
                    if (ed instanceof CloneableEditor) {
                        if (((CloneableEditor) ed).isEditorPaneReady()) {
                            p = ed.getEditorPane();
                        }
                    } else {
                        p = ed.getEditorPane();
                    }
                }
                return p;
            } else {
                throw new IllegalStateException("No reference to Pane. Please file a bug against openide/text");
            }
        }

        return null;
    }
    
    /** Returns the lastly selected Pane or null
     */
    final Pane getLastSelected() {
        Reference<Pane> r = lastSelected;

        return (r == null) ? null : r.get();
    }

    final void setLastSelected(Pane lastSelected) {
        this.lastSelected = new WeakReference<Pane>(lastSelected);
    }

    //
    // LineSet interface impl
    //

    /** Get the line set for all paragraphs in the document.
    * @return positions of all paragraphs on last save
    */
    public Line.Set getLineSet() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getLineSet();
        }
        return updateLineSet(false);
    }

    /** Lazyly creates or finds already created map for internal use.
     */
    final Map<Line,Reference<Line>> findWeakHashMap() {
        // any lock not hold for too much time will do as we do not 
        // call outside in the sync block
        synchronized (LOCK_PRINTING) {
            if (lineSetWHM != null) {
                return lineSetWHM;
            }

            lineSetWHM = new WeakHashMap<Line,Reference<Line>>();

            return lineSetWHM;
        }
    }

    //
    // Print interface
    //

    /** A printing implementation suitable for {@link org.openide.cookies.PrintCookie}. */
    public void print() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            redirect.print();
            return;
        }
        // XXX should this run synch? can be slow for an enormous doc
        synchronized (LOCK_PRINTING) {
            if (printing) {
                return;
            }

            printing = true;
        }

        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            Object o = NbDocument.findPageable(openDocument());

            if (o instanceof Pageable) {
                job.setPageable((Pageable) o);
            } else {
                PageFormat pf = PrintPreferences.getPageFormat(job);
                job.setPrintable((Printable) o, pf);
            }

            if (job.printDialog()) {
                job.print();
            }
        } catch (FileNotFoundException e) {
            notifyProblem(e, "CTL_Bad_File"); // NOI18N
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } catch (PrinterAbortException e) { // user exception
            notifyProblem(e, "CTL_Printer_Abort"); // NOI18N
        }catch (PrinterException e) {
            notifyProblem(e, "EXC_Printer_Problem"); // NOI18N
        } finally {
            synchronized (LOCK_PRINTING) {
                printing = false;
            }
        }
    }

    private static void notifyProblem(Exception e, String key) {
        String msg = NbBundle.getMessage(CloneableEditorSupport.class, key, e.getLocalizedMessage());
        Exceptions.attachLocalizedMessage(e, msg);
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Exception(e));
    }

    //
    // Methods overriden from CloneableOpenSupport
    // 

    /** Prepares document, creates and initializes
     * new <code>CloneableEditor</code> component.
     * Typically do not override this method.
     * For creating your own <code>CloneableEditor</code> type component
     * override {@link #createCloneableEditor} method.
     *
     * @return the {@link CloneableEditor} for this support
     */
    protected CloneableTopComponent createCloneableTopComponent() {
        // initializes the document if not initialized
        prepareDocument();

        Pane pane = createPane();
        pane.getComponent().putClientProperty(PROP_PANE, pane);

        return pane.getComponent();
    }

    /** Creates and initializes
     * new <code>CloneableEditor</code> component.
     * Typically do not override this method.
     * For creating your own <code>CloneableEditor</code> type component
     * override {@link #createCloneableEditor} method.
     *
     * @return the {@link Pane} for this support
     */
    protected Pane createPane() {
        CloneableEditor ed = createCloneableEditor();
        initializeCloneableEditor(ed);

        return ed;
    }
    
    /**
     * Wraps the editor component in a custom component, allowing for creating
     * more complicated user interfaces which contain the editor UI in 
     * an arbitrary place. 
     *
     * <p>The default implementation merely returns the passed 
     * <code>editorComponent</code> parameter.</p> 
     *
     * @param editorComponent the component containing the editor 
     *        (usually not the JEditorPane, but some its ancestor).
     * 
     * @return a component containing <code>editorComponent</code> or
     *         <code>editorComponent</code> itself.
     *
     * @since 6.3
     */
    protected Component wrapEditorComponent(Component editorComponent) {
        return editorComponent;
    }

    /** Should test whether all data is saved, and if not, prompt the user
    * to save.
    *
    * @return <code>true</code> if everything can be closed
    */
    @Override
    protected boolean canClose() {
        if (cesEnv().isModified()) {

			class SafeAWTAccess implements Runnable {
				boolean running;
				boolean finished;
				int ret;
				
				public void run() {
					synchronized (this) {
						running = true;
						notifyAll();
					}
					
					try {
						ret = canCloseImpl();
					} finally {					
						synchronized (this) {
							finished = true;
							notifyAll();
						}
					}
				}
				
				
				
				public synchronized void waitForResult() throws InterruptedException {
					if (!running) {
						wait(10000);
					}
					if (!running) {
						throw new InterruptedException("Waiting 10s for AWT and nothing! Exiting to prevent deadlock"); // NOI18N
					}
					
					while (!finished) {
						wait();
					}
				}
			}
			
			
			SafeAWTAccess safe = new SafeAWTAccess();
            if (SwingUtilities.isEventDispatchThread()) {
                safe.run(); 
            } else {
                SwingUtilities.invokeLater(safe); 
                try {
                    safe.waitForResult();
                } catch (InterruptedException ex) {
                    ERR.log(Level.INFO, null, ex);
                    return false;
                }
            }
			
            if (safe.ret == 0) {
                return false;
            }

            if (safe.ret == 1) {
                try {
                    saveDocument();
                } catch (UserCancelException uce) {
                    return false;
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);

                    return false;
                }
            }
        }

        return true;
    }
	
	/** @return 0 => cannot close, -1 can close and do not save, 1 can close and save */
	private int canCloseImpl() {
		String msg = messageSave();

		ResourceBundle bundle = NbBundle.getBundle(CloneableEditorSupport.class);

		JButton saveOption = new JButton(bundle.getString("CTL_Save")); // NOI18N
		saveOption.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Save")); // NOI18N
		saveOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Save")); // NOI18N

		JButton discardOption = new JButton(bundle.getString("CTL_Discard")); // NOI18N
		discardOption.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Discard")); // NOI18N
		discardOption.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CTL_Discard")); // NOI18N
		discardOption.setMnemonic(bundle.getString("CTL_Discard_Mnemonic").charAt(0)); // NOI18N

		NotifyDescriptor nd = new NotifyDescriptor(
				msg, bundle.getString("LBL_SaveFile_Title"), NotifyDescriptor.YES_NO_CANCEL_OPTION,
				NotifyDescriptor.QUESTION_MESSAGE,
				new Object[] { saveOption, discardOption, NotifyDescriptor.CANCEL_OPTION }, saveOption
			);

		Object ret = DialogDisplayer.getDefault().notify(nd);

		if (NotifyDescriptor.CANCEL_OPTION.equals(ret) || NotifyDescriptor.CLOSED_OPTION.equals(ret)) {
			return 0;
		}

		if (saveOption.equals(ret)) {
			return 1;
		} else {
			return -1;
		}
	}

    //
    // public methods provided by this class
    //

    /** Test whether the document is in memory, or whether loading is still in progress.
    * @return <code>true</code> if document is loaded
    */
    public boolean isDocumentLoaded() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.isDocumentLoaded();
        }
        return documentStatus != DOCUMENT_NO;
    }
    
    /** Test whether the document is ready.
    * @return <code>true</code> if document is ready
    */
    private boolean isDocumentReady() {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.isDocumentReady();
        }
        return documentStatus == DOCUMENT_READY;
    }

    /**
    * Set the MIME type for the document.
    * @param s the new MIME type
    */
    public void setMIMEType(String s) {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            redirect.setMIMEType(s);
            return;
        }
        mimeType = s;
    }

    /** Adds a listener for status changes. An event is fired
    * when the document is moved or removed from memory.
    * @param l new listener
    * @deprecated Deprecated since 3.40. Use {@link #addPropertyChangeListener} instead.
    * See also {@link org.openide.cookies.EditorCookie.Observable}.
    */
    @Deprecated
    public synchronized void addChangeListener(ChangeListener l) {
        if (listeners == null) {
            listeners = new HashSet<ChangeListener>(8);
        }

        listeners.add(l);
    }

    /** Removes a listener for status changes.
     * @param l listener to remove
    * @deprecated Deprecated since 3.40. Use {@link #removePropertyChangeListener} instead.
    * See also {@link org.openide.cookies.EditorCookie.Observable}.
    */
    @Deprecated
    public synchronized void removeChangeListener(ChangeListener l) {
        if (listeners != null) {
            listeners.remove(l);
        }
    }

    // Position management methods

    /** Create a position reference for the given offset.
    * The position moves as the document is modified and
    * reacts to closing and opening of the document.
    *
    * @param offset the offset to create position at
    * @param bias the Position.Bias for new creating position.
    * @return position reference for that offset
    */
    public final PositionRef createPositionRef(int offset, Position.Bias bias) {
        return new PositionRef(getPositionManager(), offset, bias);
    }

    //
    // Methods that can be overriden by subclasses
    //

    /** Allows subclasses to create their own version
     * of <code>CloneableEditor</code> component.
     * @return the {@link CloneableEditor} for this support
     */
    protected CloneableEditor createCloneableEditor() {
        return new CloneableEditor(this);
    }

    /** Initialize the editor. This method is called after the editor component
     * is deserialized and also when the component is created. It allows
     * the subclasses to annotate the component with icon, selected nodes, etc.
     *
     * @param editor the editor that has been created and should be annotated
     */
    protected void initializeCloneableEditor(CloneableEditor editor) {
    }

    /** Create an undo/redo manager.
    * This manager is then attached to the document, and listens to
    * all changes made in it.
    * <P>
    * The default implementation uses improved <code>UndoRedo.Manager</code>.
    *
    * @return the undo/redo manager
    */
    protected UndoRedo.Manager createUndoRedoManager() {
        return new CESUndoRedoManager(this);
    }

    /** Returns an InputStream which reads the current data from this editor, taking into
     * account the encoding of the file. The returned InputStream will be useful for
     * example when passing the file to an external compiler or other tool, which
     * expects an input stream and which deals with encoding internally.<br>
     *
     * See also {@link #saveFromKitToStream}.
     *
     * @return input stream for the file. If the file is open in the editor (and possibly modified),
     * then the returned <code>InputStream</code> will contain the same data as if the file
     * was written out to the {@link CloneableEditorSupport.Env} (usually disk). So it will contain
     * guarded block markers etc. If the document is not loaded,
     * then the <code>InputStream</code> will be taken from the {@link CloneableEditorSupport.Env}.
     *
     * @throws IOException if saving the document to a virtual stream or other IO operation fails
     * @since 4.7
     */
    public InputStream getInputStream() throws IOException {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.getInputStream();
        }
        // Implementation note
        // Piped stream will not work, as we are in the same thread
        // Doing this in a different thread would need to lock the document for
        // reading through doc.render() while this stream is open, which may be unacceptable
        // So we copy the document in memory
        StyledDocument tmpDoc = getDocument();

        if (tmpDoc == null) {
            return cesEnv().inputStream();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            saveFromKitToStream(tmpDoc, kit, baos);
        } catch (BadLocationException e) {
            //assert false : e;
            // should not happen
            ERR.log(Level.INFO, null, e);
            throw (IllegalStateException) new IllegalStateException(e.getMessage()).initCause(e);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Actually write file data to an output stream from an editor kit's document.
     * Called during a file save by {@link #saveDocument}.
     * <p>The default implementation just calls {@link EditorKit#write(OutputStream, Document, int, int) EditorKit.write(...)}.
     * Subclasses could override this to provide support for persistent guard blocks, for example.
     * @param doc the document to write from
     * @param kit the associated editor kit
     * @param stream the open stream to write to
     * @throws IOException if there was a problem writing the file
     * @throws BadLocationException should not normally be thrown
     * @see #loadFromStreamToKit
     */
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream)
    throws IOException, BadLocationException {
        kit.write(stream, doc, 0, doc.getLength());
    }

    /**
     * Actually read file data into an editor kit's document from an input stream.
     * Called during a file load by {@link #prepareDocument}.
     * <p>The default implementation just calls {@link EditorKit#read(InputStream, Document, int) EditorKit.read(...)}.
     * Subclasses could override this to provide support for persistent guard blocks, for example.
     * @param doc the document to read into
     * @param stream the open stream to read from
     * @param kit the associated editor kit
     * @throws IOException if there was a problem reading the file
     * @throws BadLocationException should not normally be thrown
     * @see #saveFromKitToStream
     */
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit)
    throws IOException, BadLocationException {
        kit.read(stream, doc, 0);
    }

    private boolean reloadDocumentFireDocumentChangeClose = false;
    private boolean reloadDocumentFireDocumentChangeOpen = false;
    /** Reload the document in response to external modification.
    * @return task that reloads the document. It can be also obtained
    *  by calling <tt>prepareDocument()</tt>.
    */
    protected Task reloadDocument() {
        ERR.fine("reloadDocument in " + Thread.currentThread()); // NOI18N

        if (getDoc() != null) {
            final JEditorPane[] panes = getOpenedPanes();
            // acquire write access
            NbDocument.runAtomic(getDoc(),
                                 new Runnable() {

                                     public void run() {
                                         // UndoManager must be detached from document here because it will be attached in loadDocument()
                                         getDoc().removeUndoableEditListener(getUndoRedo());
                                         // Remember caret positions in all opened panes
                                         final int[] carets;

                                         if (panes != null) {
                                             carets = new int[panes.length];
                                             for (int i = 0; i < panes.length; i++) {
                                                 carets[i] = panes[i].getCaretPosition();
                                             }
                                         } else {
                                             carets = new int[0];
                                         }
                                         documentStatus = DOCUMENT_RELOADING;
                                         prepareDocumentRuntimeException = null;

                                         class Query implements Runnable, Callable<Void> {
                                             int targetStatus = DOCUMENT_NO;
                                             UserQuestionException e;

                                             public void run() {
                                                askUserAndDoOpen(e, this);
                                             }

                                             public Void call() {
                                                 // #24676. Reloading: Put positions into memory
                                                 // and fire document is closing (little trick
                                                 // to detach annotations).
                                                 getPositionManager().documentClosed();
                                                 updateLineSet(true);
                                                 reloadDocumentFireDocumentChangeClose = true;
                                                 //fireDocumentChange(getDoc(), true);
                                                 ERR.fine("clearDocument");
                                                 clearDocument();
                                                 // uses the listener's run method to initialize whole document
                                                 prepareTask = new Task(getListener());
                                                 ERR.fine("new prepare task: " +
                                                          prepareTask);
                                                 prepareTask.run();
                                                 ERR.fine("prepareTask finished");
                                                 documentStatus = DOCUMENT_READY;
                                                 reloadDocumentFireDocumentChangeOpen = true;
                                                 //fireDocumentChange(getDoc(), false);
                                                 // Confirm that whole loading succeeded
                                                 targetStatus = DOCUMENT_READY;
                                                 return null;
                                             }
                                         }
                                         Query query = new Query();
                                         try {
                                             query.call();
                                         } 
                                         catch (RuntimeException t) {
                                             if (t.getCause() instanceof UserQuestionException) {
                                                 query.e = (UserQuestionException)t.getCause();
                                                 Mutex.EVENT.readAccess(query);
                                                 return;
                                             }
                                             prepareDocumentRuntimeException = t;
                                             prepareTask = null;
                                             throw t;
                                         }
                                         catch (Error t) {
                                             prepareDocumentRuntimeException = t;
                                             prepareTask = null;
                                             throw t;
                                         }
                                         finally {
                                             synchronized (getLock()) {
                                                 if (query.targetStatus == DOCUMENT_NO) setDoc(null, false);
                                                 documentStatus = query.targetStatus;
                                                 getLock().notifyAll();
                                             }
                                         }
                                         ERR.fine("post-reload task posting to AWT");

                                         //#160252: Set caret position synchronously.
                                         Runnable run1 = new Runnable() {
                                             public void run() {
                                                 if (getDoc() == null) {
                                                     return;
                                                 }
                                                 if (panes != null) {
                                                     for (int i = 0; i <
                                                                     panes.length; i++) {
                                                         // #26407 Adjusts caret position,
                                                         // (reloaded doc could be shorter).
                                                         int textLength = panes[i].getDocument().getLength();

                                                         if (carets[i] >
                                                             textLength) {
                                                             carets[i] = textLength;
                                                         }
                                                         panes[i].setCaretPosition(carets[i]);
                                                     }
                                                 }
                                             }
                                         };

                                         Runnable run2 = new Runnable() {
                                             public void run() {
                                                 if (getDoc() == null) {
                                                     return;
                                                 }
                                                 // XXX do this from AWT???
                                                 ERR.fine("task-discardAllEdits");
                                                 getUndoRedo().discardAllEdits();
                                                 ERR.fine("task-check already modified");
                                                 // #57104 - if modified previously now it should become unmodified
                                                 if (isAlreadyModified()) {
                                                     ERR.fine("task-callNotifyUnmodified");
                                                     callNotifyUnmodified();
                                                 }
                                                 updateLineSet(true);
                                                 ERR.fine("task-addUndoableEditListener");
                                                 // Add undoable listener after atomic change has finished
                                                 getDoc().addUndoableEditListener(getUndoRedo());
                                             }
                                         };

                                         if (getDoc() != null) {
                                             ERR.fine("Posting the AWT runnable: " +
                                                      run2);
                                             run1.run();
                                             SwingUtilities.invokeLater(run2);
                                             ERR.fine("Posted in " +
                                                      Thread.currentThread());
                                         }
                                     }
                                 });

            return prepareTask;
        }

        return prepareDocument();
    }

    /**
     * Gets an <code>EditorKit</code> from Netbeans registry. The method looks
     * in the <code>MimeLookup</code> for <code>EditorKit</code>s registered for
     * the mime-path passed in and returns the first one it finds. If there is
     * no <code>EditorKit</code> registered for the mime-path it will fall back
     * to the 'text/plain' <code>EditorKit</code> and eventually to its own
     * default kit.
     * 
     * <div class="nonnormative">
     * <p>A mime-path is a concatenation of one or more mime-types allowing to
     * address fragments of text with a different mime-type than the mime-type
     * of a document that contains those fragments. As an example you can use
     * a JSP page containing a java scriplet. The JSP page is a document of
     * 'text/x-jsp' mime-type, while the mime-type of the java scriplet is 'text/x-java'.
     * When accessing settings or services such as an 'EditorKit' for java scriplets
     * embedded in a JSP page the scriplet's mime-path 'text/x-jsp/text/x-java'
     * should be used.
     * </p>
     * <p>If you are trying to get an 'EditorKit' for the whole document you can
     * simply pass in the document's mime-type (e.g. 'text/x-java'). For the main
     * document its mime-type and mime-path are the same.
     * </div>
     *
     * @param mimePath    The mime-path to find an <code>EditorKit</code> for.
     *
     * @return The <code>EditorKit</code> implementation registered for the given mime-path.
     * @see org.netbeans.api.editor.mimelookup.MimeLookup
     * @since org.openide.text 6.12
     */
    public static EditorKit getEditorKit(String mimePath) {
        Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimePath));
        EditorKit kit = lookup.lookup(EditorKit.class);
        
        if (kit == null) {
            // Try 'text/plain'
            lookup = MimeLookup.getLookup(MimePath.parse("text/plain"));
            kit = lookup.lookup(EditorKit.class);
        }
        
        // Don't use the prototype instance straightaway
        return kit != null ? (EditorKit) kit.clone() : new PlainEditorKit();
    }
    
    /** Creates editor kit for this source.
    * @return editor kit
    */
    protected EditorKit createEditorKit() {
        if (kit != null) {
            return kit;
        }

        if (mimeType != null) {
            kit = getEditorKit(mimeType);
        } else {
            String defaultMIMEType = cesEnv().getMimeType();
            kit = getEditorKit(defaultMIMEType);
        }

        return kit;
    }

    /** Method that can be overriden by children to create empty
    * styled document or attach additional document properties to it.
    *
    * @param kit the kit to use
    * @return styled document to use
    */
    protected StyledDocument createStyledDocument(EditorKit kit) {
        StyledDocument sd = createNetBeansDocument(kit.createDefaultDocument());
        sd.putProperty("mimeType", (mimeType != null) ? mimeType : cesEnv().getMimeType()); // NOI18N

        return sd;
    }

    /** Notification method called when the document become unmodified.
    * Called after save or after reload of document.
    * <P>
    * This implementation simply marks the associated
    * environement unmodified and updates titles of all components.
    */
    protected void notifyUnmodified() {
        env.unmarkModified();
        updateTitles();
    }

    /** Conditionally calls notifyModified
     * @return true if the modification was allowed, false if it should be prohibited
     */
    final boolean callNotifyModified() {
        // #57104 - when reverting undo the revertingUndoOrReloading flag is set
        // to prevent infinite undoing which could happen now due to fix #56963
        // (undoable edit being undone in the document notifies
        // document's modification listener to mark the file as modified).
        // Maybe clearing alreadyModified flag
        // AFTER revertPreviousOrUpcomingUndo() could suffice as well
        // instead of the revertingUndoOrReloading flag.
        // Also notifyModified() is not called during reloadDocument()
        // to prevent situation when output stream is taken from the file
        // (for which CloneableEditorSupport exists) under file's lock
        // and once closed (still under file's lock) the CES is trying to reload
        // the file calling notifyModified() that tries to grab the lock
        // and fails leading to undoing of the file's content to the one
        // before the reload.
        if (!isAlreadyModified() && !revertingUndoOrReloading) {
            setAlreadyModified(true);
            
            if (!notifyModified()) {
                setAlreadyModified(false);
                revertingUndoOrReloading = true;
                revertPreviousOrUpcomingUndo();
                revertingUndoOrReloading = false;

                return false;
            }
        }

        return true;
    }

    final void callNotifyUnmodified() {
        setAlreadyModified(false);
        notifyUnmodified();
    }

    /** Called when the document is being modified.
    * The responsibility of this method is to inform the environment
    * that its document is modified. Current implementation
    * Just calls env.setModified (true) to notify it about
    * modification.
    *
    * @return true if the environment accepted being marked as modified
    *    or false if it refused it and the document should still be unmodified
    */
    protected boolean notifyModified() {
        boolean locked = true;

        try {
            env.markModified();
        } catch (final UserQuestionException ex) {
            synchronized (this) {
                if (!this.inUserQuestionExceptionHandler) {
                    this.inUserQuestionExceptionHandler = true;
                    RP.post(new Runnable() {

                                                           public void run() {
                                                               NotifyDescriptor nd = new NotifyDescriptor.Confirmation(ex.getLocalizedMessage(),
                                                                                                                       NotifyDescriptor.YES_NO_OPTION);
                                                               Object res = DialogDisplayer.getDefault().notify(nd);

                                                               if (NotifyDescriptor.OK_OPTION.equals(res)) {
                                                                   try {
                                                                       ex.confirmed();
                                                                   }
                                                                   catch (IOException ex1) {
                                                                       Exceptions.printStackTrace(ex1);
                                                                   }
                                                               }
                                                               synchronized (CloneableEditorSupport.this) {
                                                                   CloneableEditorSupport.this.inUserQuestionExceptionHandler = false;
                                                               }
                                                           }
                                                       });
                }
            }

            locked = false;
        } catch (IOException e) { // locking failed
            //#169695: Added exception log to investigate
            ERR.log( Level.INFO, "Could not lock document", e);
            //#169695: END
            String message = null;

            if ((Object)e.getMessage() != e.getLocalizedMessage()) {
                message = e.getLocalizedMessage();
            } else {
                message = Exceptions.findLocalizedMessage(e);
            }

            if (message != null) {
                StatusDisplayer.getDefault().setStatusText(message);
            }

            locked = false;
        }

        if (!locked) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }

        // source modified, remove it from tab-reusing slot
        lastReusable.clear();
        updateTitles();

        return true;
    }

    /** Resets listening on <code>UndoRedo</code>,
     * and in case next undo edit comes, schedules processesing of it.
     * Used to revert modification e.g. of document of [read-only] env. */
    private void revertPreviousOrUpcomingUndo() {
        UndoRedo.Manager ur = getUndoRedo();
        Listener l = getListener();

        if (Boolean.TRUE.equals(getDocument().getProperty("supportsModificationListener"))) { // NOI18N

            // revert undos now
            SearchBeforeModificationEdit edit = new SearchBeforeModificationEdit();

            try {
                for (;;) {
                    edit.delegate = null;
                    ur.undoableEditHappened(new UndoableEditEvent(getDocument(), edit));

                    if (edit.delegate == null) break; // no previous edit
                    
                    if (edit.delegate instanceof BeforeModificationEdit) {
                        if (edit.delegate != null) {
                            // undo anyway
                            ur.undo();
                        }

                        // and exit
                        break;
                    }

                    if (edit.delegate instanceof BeforeSaveEdit) {
                        break;
                    }

                    // otherwise remove the edit 
                    ur.undo();
                }
            } catch (CannotUndoException ex) {
                // ok, cannot undo, just ignore this
            }
        } else {
            // revert upcomming undo
            l.setUndoTask(new Runnable() {
                    public void run() {
                        undoAll();
                    }
                }
            );
            ur.addChangeListener(l);
        }
    }

    /** Creates <code>Runnable</code> which tries to make one undo. Helper method.
     * @see #revertUpcomingUndo */
    final void undoAll() {
        StyledDocument sd = getDoc();

        if (sd == null) {
            // #20883, doc can be null(!), doCloseDocument was faster.
            return;
        }

        UndoRedo ur = getUndoRedo();
        addRemoveDocListener(sd, false);

        try {
            if (ur.canUndo()) {
                ur.undo();
            }
        } catch (CannotUndoException cne) {
            ERR.log(Level.INFO, null, cne);
        } finally {
            addRemoveDocListener(sd, true);
        }
    }

    /** Method that is called when all components of the support are
    * closed. The default implementation closes the document.
    *
    */
    protected void notifyClosed() {
        closeDocument();
    }

    // XXX #25762 [PENDING] Needed protected method to allow subclasses to alter it.

    /** Indicates whether the <code>Env</code> is read only. */
    boolean isEnvReadOnly() {
        return false;
    }

    /** Allows access to the document without any checking.
    */
    final StyledDocument getDocumentHack() {
        return getDoc();
    }

    /** Getter for context associated with this
    * data object.
    */
    final org.openide.util.Lookup getLookup() {
        return lookup;
    }

    // LineSet methods .....................................................................

    /** Updates the line set.
    * @param clear clear any cached set?
    * @return the set
    */
    Line.Set updateLineSet(boolean clear) {
        synchronized (getLock()) {
            if ((lineSet != null) && !clear) {
                return lineSet;
            }

            Line.Set oldSet = lineSet;

            if ((getDoc() == null) || (documentStatus == DOCUMENT_RELOADING)) {
                lineSet = new EditorSupportLineSet.Closed(CloneableEditorSupport.this);
            } else {
                lineSet = new EditorSupportLineSet(CloneableEditorSupport.this,getDoc());
            }

            return lineSet;
        }
    }

    /** Loads the document for this object.
    * @param kit kit to use
    * @param d original document to load data into
    */
    private void loadDocument(EditorKit kit, StyledDocument doc)
    throws IOException {
        Throwable aProblem = null;

        try {
            InputStream is = new BufferedInputStream(cesEnv().inputStream());

            try {
                // read the document
                loadFromStreamToKit(doc, is, kit);
            } finally {
                is.close();
            }
        } catch (UserQuestionException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception e) { // incl. BadLocationException
            aProblem = e;
        } finally {
            if (aProblem != null) {
                final Throwable tmp = aProblem;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Exceptions.attachLocalizedMessage(tmp,
                        NbBundle.getMessage(CloneableEditorSupport.class,
                        "EXC_LoadDocument",
                        messageName()));
                        Exceptions.printStackTrace(tmp);
                    }
                });
            }
        }
    }

    /** Closes all opened editors (if the user agrees) and
    * flushes content of the document to the file.
    *
    * @param ask ask whether to save the document or not?
    * @return <code>false</code> if the operation is cancelled
    */
    @Override
    protected boolean close(boolean ask) {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.close(ask);
        }
        
        if (!super.close(ask)) {
            // if not all editors has been closed
            return false;
        }

        notifyClosed();
        
        return true;
    }

    /** Clears all data from memory.
    */
    private void closeDocument() {
        boolean fireEvent = false;
        StyledDocument d = null;
        try {
            synchronized (getLock()) {
                while (true) {
                    switch (documentStatus) {
                    case DOCUMENT_NO:
                        return;
                        
                    case DOCUMENT_LOADING:
                    case DOCUMENT_RELOADING:
                    // let it flow to default:
                    //                        openDocumentImpl();
                    //                        break; // try to close again
                    default:
                        d = getDoc();
                        fireEvent = doCloseDocument();
                        return;
                    }
                }
            }
        } finally {
            if (fireEvent) {
                fireDocumentChange(d, true);
            }
        }
    }
    
    /** Is called under getLock () to close the document.
     */
    private boolean doCloseDocument() {
        boolean fireEvent = false;
        prepareTask = null;

        // notifies the support that 
        cesEnv().removePropertyChangeListener(getListener());
        callNotifyUnmodified();

        StyledDocument d = getDoc();
        if (d != null) {
            d.removeUndoableEditListener(getUndoRedo());
            addRemoveDocListener(d, false);
        }

        if (positionManager != null) {
            positionManager.documentClosed();
        }
        
        documentStatus = DOCUMENT_NO;
        fireEvent = true;
        setDoc(null, false);
        kit = null;
        
        getUndoRedo().discardAllEdits();
        updateLineSet(true);
        return fireEvent;
    }

    /** Handles the actual reload of document.
    * @param doReload false if we should first ask the user
    */
    private void checkReload(boolean doReload) {
        StyledDocument d;

        synchronized (getLock()) {
            // don't try to reload if first load is in progress, cf.56413
            if (documentStatus != DOCUMENT_READY) {
                return;
            }

            d = getDoc(); // used with reload dialog - should not be null
        }

        if (!doReload && !reloadDialogOpened) {
            String msg = NbBundle.getMessage(
                    CloneableEditorSupport.class, "FMT_External_change", // NOI18N
                    d.getProperty(javax.swing.text.Document.TitleProperty)
                );

            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);

            reloadDialogOpened = true;

            try {
                Object ret = DialogDisplayer.getDefault().notify(nd);

                if (NotifyDescriptor.YES_OPTION.equals(ret)) {
                    doReload = true;
                }
            } finally {
                reloadDialogOpened = false;
            }
        }

        synchronized (getLock()) {
            // don't try to reload if first load is in progress, cf.56413
            if (documentStatus != DOCUMENT_READY) {
                return;
            }

            if (doReload) {
                // #33165
                // reloadDocument() itself should be fast and the task
                // that it returns is scheduled to RP automatically
                reloadDocument();

                /* #33165 - not posting to RP, reason is above
                                //Bugfix #9612: Call of reloadDocument() is now posted to
                                //RequestProcessor
                                RequestProcessor.getDefault().post(new Runnable() {
                                    public void run () {
                                        reloadDocument().waitFinished();
                                    }
                                });
                 */
            }
        }
        if (reloadDocumentFireDocumentChangeClose) {
            reloadDocumentFireDocumentChangeClose = false;
            fireDocumentChange(getDoc(), true);
        }
        if (reloadDocumentFireDocumentChangeOpen) {
            reloadDocumentFireDocumentChangeOpen = false;
            fireDocumentChange(getDoc(), false);
        }
    }
    
    /** Creates netbeans document for a given document.
    * @param d document to use as underlaying one
    * @return styled document that could support Guarded.ATTRIBUTE
    */
    private static StyledDocument createNetBeansDocument(Document d) {
        if (d instanceof StyledDocument) {
            return (StyledDocument) d;
        } else {
            // create filter
            return new FilterDocument(d);
        }
    }

    private final void fireDocumentChange(StyledDocument document, boolean closing) {
        fireStateChangeEvent(document, closing);
        firePropertyChange(EditorCookie.Observable.PROP_DOCUMENT,
                closing ? document : null,
                closing ? null : document);
    }

    /** Fires a status change event to all listeners. */
    private final void fireStateChangeEvent(StyledDocument document, boolean closing) {
        if (listeners != null) {
            EnhancedChangeEvent event = new EnhancedChangeEvent(this, document, closing);
            ChangeListener[] ls;

            synchronized (this) {
                ls = listeners.toArray(new ChangeListener[listeners.size()]);
            }

            for (ChangeListener l : ls) {
                l.stateChanged(event);
            }
        }
    }

    /** Updates titles of all editors.
    */
    protected void updateTitles() {
        Enumeration en = allEditors.getComponents();

        while (en.hasMoreElements()) {
            CloneableTopComponent o = (CloneableTopComponent) en.nextElement();
            Pane e = (Pane) o.getClientProperty(PROP_PANE);

            if ((e == null) && o instanceof Pane) {
                e = (Pane) o;
            }

            if (e != null) {
                e.updateName();
            } else {
                throw new IllegalStateException("No reference to Pane. Please file a bug against openide/text");
            }
        }
    }

    private static Reference<CloneableTopComponent> lastReusable = new WeakReference<CloneableTopComponent>(null);

    private static void replaceTc(TopComponent orig, TopComponent open) {
        int pos = orig.getTabPosition ();
        orig.close();
        open.openAtTabPosition (pos);
    }

    // #18981. There could happen a thing also another class type
    // of CloneableTopCoponent then CloneableEditor could be in allEditors.

    /** Opens a <code>CloneableEditor</code> component. */
    private Pane openPane(boolean reuse) {
        Pane ce = null;
        boolean displayMsgOpened = false;

        synchronized (getLock()) {
            ce = getAnyEditor();

            if (ce == null) {
                // no opened editor
                String msg = messageOpening();

                if (msg != null) {
                    StatusDisplayer.getDefault().setStatusText(msg);
                }

                // initializes the document if not initialized
                prepareDocument();
                ce = createPane();
                ce.getComponent().putClientProperty(PROP_PANE, ce);
                ce.getComponent().setReference(allEditors);

                // signal opened msg should be displayed after subsequent open finishes
                displayMsgOpened = true;
            }
        }

        // #36601 - open moved outside getLock() synchronization
        CloneableTopComponent ctc = ce.getComponent();
        if (reuse && displayMsgOpened) {
            CloneableTopComponent last = lastReusable.get();
            if (last != null) {
                replaceTc(last, ctc);
            } else {
                ctc.open();
            }
            lastReusable = new WeakReference<CloneableTopComponent>(ctc);
        } else {
            ctc.open();
        }
        
        if (displayMsgOpened) {
            String msg = messageOpened();

            if (msg == null) {
                msg = ""; // NOI18N
            }

            StatusDisplayer.getDefault().setStatusText(msg);
        }

        return ce;
    }

    /** If one or more editors are opened finds one.
    * @return an editor or null if none is opened
    */
    Pane getAnyEditor() {
        CloneableTopComponent ctc;
        ctc = allEditors.getArbitraryComponent();

        if (ctc == null) {
            return null;
        }

        Pane e = (Pane) ctc.getClientProperty(PROP_PANE);

        if (e != null) {
            return e;
        } else {
            if (ctc instanceof Pane) {
                return (Pane) ctc;
            }

            Enumeration en = allEditors.getComponents();

            while (en.hasMoreElements()) {
                ctc = (CloneableTopComponent) en.nextElement();
                e = (Pane) ctc.getClientProperty(PROP_PANE);

                if (e != null) {
                    return e;
                } else {
                    if (ctc instanceof Pane) {
                        return (Pane) ctc;
                    }

                    throw new IllegalStateException("No reference to Pane. Please file a bug against openide/text");
                }
            }

            return null;
        }
    }

    @Deprecated
    final Pane openReuse(final PositionRef pos, final int column, int mode) {
        if (mode == Line.SHOW_REUSE_NEW) lastReusable.clear();
        return openAtImpl(pos, column, true);
    }

    final Pane openReuse(final PositionRef pos, final int column, Line.ShowOpenType mode) {
        if (mode == Line.ShowOpenType.REUSE_NEW) lastReusable.clear();
        return openAtImpl(pos, column, true);
    }
    
    /** Forcibly create one editor component. Then set the caret
    * to the given position.
    * @param pos where to place the caret
    * @param column where to place the caret
    * @return always non-<code>null</code> editor
    * @since 5.2
    */
    protected final Pane openAt(final PositionRef pos, final int column) {
        return openAtImpl(pos, column, false);
    }
    /** Forcibly create one editor component. Then set the caret
    * to the given position.
    * @param pos where to place the caret
    * @param column where to place the caret
    * @param reuse if true, the infrastructure tries to reuse other, already opened editor
     * for the purpose of opening this file/line. 
    * @return always non-<code>null</code> editor
    */
    private final Pane openAtImpl(final PositionRef pos, final int column, boolean reuse) {
        CloneableEditorSupport redirect = CloneableEditorSupportRedirector.findRedirect(this);
        if (redirect != null) {
            return redirect.openAtImpl(pos, column, reuse);
        }
        counterOpenAtImpl++;
        final Pane e = openPane(reuse);
        final Task t = prepareDocument();
        e.ensureVisible();
        class Selector implements TaskListener, Runnable {
            private boolean documentLocked = false;
            private int counterRun = 0;

            public void taskFinished(org.openide.util.Task t2) {
                javax.swing.SwingUtilities.invokeLater(this);
                t2.removeTaskListener(this);
            }

            public void run() {
                counterRun++;
                try {
                    // #25435. Pane can be null.
                    JEditorPane ePane = e.getEditorPane();

                    if (ePane == null) {
                        return;
                    }

                    StyledDocument doc = getDocument();

                    if (doc == null) {
                        return; // already closed or error loading
                    }

                    if (!documentLocked) {
                        documentLocked = true;
                        doc.render(this);
                    } else {
                        Caret caret = ePane.getCaret();

                        if (caret == null) {
                            return;
                        }

                        int offset;

                        javax.swing.text.Element el = NbDocument.findLineRootElement(doc);
                        el = el.getElement(el.getElementIndex(pos.getOffset()));
                        offset = el.getStartOffset() + Math.max(0, column);

                        if (offset > el.getEndOffset()) {
                            offset = Math.max(el.getStartOffset(), el.getEndOffset() - 1);
                        }

                        caret.setDot(offset);

                        try { // scroll to show reasonable part of the document
                            Rectangle r = ePane.modelToView(offset);
                            if (r != null) {
                                r.height *= 5;
                                ePane.scrollRectToVisible(r);
                            }
                        } catch (BadLocationException ex) {
                            ERR.log(Level.WARNING, "Can't scroll to text: pos.getOffset=" + pos.getOffset() //NOI18N
                                + ", column=" + column + ", offset=" + offset //NOI18N
                                + ", doc.getLength=" + doc.getLength(), ex); //NOI18N
                        }
                    }
                } finally {
                    counterRun--;
                    if (counterRun == 0) {
                        counterOpenAtImpl--;
                        if (isStrongSet && canReleaseDoc()) {
                            isStrongSet = false;
                            CloneableEditorSupport.this.setStrong(false, true);
                        }
                    }
                }
            }
        }
        t.addTaskListener(new Selector());

        return e;
    }

    /** Access to lock on operations on the support
    */
    final Object getLock() {
        return allEditors;
    }

    /** Accessor to the <code>Listener</code> instance, lazy created on demand.
     * The instance serves as a listener on document, environment
     * and also provides document initialization task for this support.
     * @see Listener */
    private Listener getListener() {
        // Should not need to lock; it is always first
        // called within a synchronized(getLock()) block anyway.
        if (listener == null) {
            listener = new Listener();
        }

        return listener;
    }

    // [pnejedly]: helper for 40766 test
    void howToReproduceDeadlock40766(boolean beforeLock) {
    }

    /** Make sure we log every access to last save time.
     * @param lst the time in millis of last save
     */
    final void setLastSaveTime(long lst) {
        ERR.fine("Setting new lastSaveTime to " + lst);
        this.lastSaveTime = lst;
    }

    final boolean isAlreadyModified() {
        return alreadyModified;
    }

    final void setAlreadyModified(boolean alreadyModified) {
        ERR.log(Level.FINE, null, new Exception("Setting to modified: " + alreadyModified));

        this.alreadyModified = alreadyModified;
        setStrong(alreadyModified, false);
    }

    private StyledDocument getDoc() {
        synchronized (LOCK_STRONG_REF) {
            StrongRef _doc = doc;
            return _doc != null ? _doc.get() : null;
        }
    }

    private void setDoc(StyledDocument doc, boolean strong) {
        synchronized (LOCK_STRONG_REF) {
            if (doc == null) {
                this.doc = null;
                return;
            }
            this.doc = new StrongRef(doc, strong);
            Logger.getLogger("TIMER").log(Level.FINE, "TextDocument", doc);
        }
    }

    private void setStrong (boolean strong, boolean setFlag) {
        synchronized (LOCK_STRONG_REF) {
            if (doc != null) {
                if (strong) {
                    doc.setStrong(true);
                    if (setFlag) {
                        isStrongSet = true;
                    }
                } else {
                    if (!isAlreadyModified()) {
                        doc.setStrong(false);
                    }
                }
            }
        }
    }
    
    private final class StrongRef extends WeakReference<StyledDocument> 
    implements Runnable {
        private StyledDocument doc;
        
        public StrongRef(StyledDocument doc, boolean strong) {
            super(doc, org.openide.util.Utilities.activeReferenceQueue());
            if (strong) {
                this.doc = doc;
            }
        }
        
        @Override
        public StyledDocument get() {
            return doc != null ? doc : super.get();
        }

        public void run() {
            if (this != CloneableEditorSupport.this.doc) {
                return;
            }
            closeDocument();
        }

        private void setStrong(boolean alreadyModified) {
            if (alreadyModified) {
                this.doc = super.get();
            } else {
                this.doc = null;
            }
        }

        @Override
        public String toString() {
            return "StrongRef@" + Integer.toHexString(System.identityHashCode(this)) + "[doc=" + doc + ",super.get=" + super.get() + "]";
        }

    } // end of StrongRef

    /** Interface for providing data for the support and also
    * locking the source of data.
    */
    public static interface Env extends CloneableOpenSupport.Env {
        /** property that is fired when time of the data is changed */
        public static final String PROP_TIME = "time"; // NOI18N

        /** Obtains the input stream.
         * @return an input stream permitting the document to be loaded
        * @exception IOException if an I/O error occures
        */
        public InputStream inputStream() throws IOException;

        /** Obtains the output stream.
         * @return an output stream permitting the document to be saved
        * @exception IOException if an I/O error occures
        */
        public OutputStream outputStream() throws IOException;

        /**
         * Gets the last modification time for the document.
         * @return the date and time when the document is considered to have been
         *         last changed
         */
        public Date getTime();

        /** Mime type of the document.
        * @return the mime type to use for the document
        */
        public String getMimeType();
    }

    /** Describes one existing editor.
     */
    public interface Pane {
        /**
         * get the editor pane component represented by this wrapper.
         */
        public JEditorPane getEditorPane();

        /**
         * Get the TopComponent that contains the EditorPane
         */
        public CloneableTopComponent getComponent();

        public void updateName();

        /**
         * callback for the Pane implementation to adjust itself to the openAt() request.
         */
        public void ensureVisible();
    }

    /** Default editor kit.
    */
    private static final class PlainEditorKit extends DefaultEditorKit implements ViewFactory {
        static final long serialVersionUID = -5788777967029507963L;

        PlainEditorKit() {
        }

        /** @return cloned instance
        */
        @Override
        public Object clone() {
            return new PlainEditorKit();
        }

        /** @return this (I am the ViewFactory)
        */
        @Override
        public ViewFactory getViewFactory() {
            return this;
        }

        /** Plain view for the element
        */
        public View create(Element elem) {
            return new WrappedPlainView(elem);
        }

        /** Set to a sane font (not proportional!). */
        @Override
        public void install(JEditorPane pane) {
            super.install(pane);
            pane.setFont(new Font("Monospaced", Font.PLAIN, pane.getFont().getSize() + 1)); //NOI18N
        }
    }

    /** The listener that this support uses to communicate with
     * document, environment and also temporarilly on undoredo.
     */
    private final class Listener extends Object implements ChangeListener, DocumentListener, PropertyChangeListener,
        Runnable, java.beans.VetoableChangeListener {
        /** revert modification if asked */
        private boolean revertModifiedFlag;

        /** Stores exception from loadDocument, can be set in run method */
        private IOException loadExc;

        /** Stores temporarilly undo task for reverting prohibited changes.
         * @see CloneableEditorSupport#createUndoTask */
        private Runnable undoTask;

        Listener() {
        }

        /** Returns exception from loadDocument, caller thread can check
         * it after load task finishes. Returns null if no exception happened.
         * It resets loadExc to null. */
        public IOException checkLoadException() {
            IOException ret = loadExc;

            //            loadExc = null;
            return ret;
        }

        /** Sets undo task used to revert prohibited change. */
        public void setUndoTask(Runnable undoTask) {
            this.undoTask = undoTask;
        }

        /** Schedules reverting(undoing) of prohibited change.
         * Implements <code>ChangeListener</code>.
         * @see #revertUpcomingUndo */
        public void stateChanged(ChangeEvent evt) {
            getUndoRedo().removeChangeListener(this);
            undoTask.run();

            //SwingUtilities.invokeLater(undoTask);
            undoTask = null;
        }

        /** Gives notification that an attribute or set of attributes changed.
        * @param ev event describing the action
        */
        public void changedUpdate(DocumentEvent ev) {
            //modified(); (bugfix #1492)
        }

        public void vetoableChange(PropertyChangeEvent evt)
        throws java.beans.PropertyVetoException {
            if ("modified".equals(evt.getPropertyName())) { // NOI18N

                if (Boolean.TRUE.equals(evt.getNewValue())) {
                    boolean wasModified = isAlreadyModified();

                    if (!callNotifyModified()) {
                        throw new java.beans.PropertyVetoException("Not allowed", evt); // NOI18N
                    }

                    revertModifiedFlag = !wasModified;
                } else {
                    if (revertModifiedFlag) {
                        callNotifyUnmodified();
                    }
                }
            }
        }

        /** Gives notification that there was an insert into the document.
        * @param ev event describing the action
        */
        public void insertUpdate(DocumentEvent ev) {
            callNotifyModified();
            revertModifiedFlag = false;
        }

        /** Gives notification that a portion of the document has been removed.
        * @param ev event describing the action
        */
        public void removeUpdate(DocumentEvent ev) {
            callNotifyModified();
            revertModifiedFlag = false;
        }

        /** Listener to changes in the Env.
        */
        public void propertyChange(PropertyChangeEvent ev) {
            if ("expectedTime".equals(ev.getPropertyName())) { // NOI18N
                lastSaveTime = ((Date)ev.getNewValue()).getTime();
            }
            if (Env.PROP_TIME.equals(ev.getPropertyName())) {
                // empty new value means to force reload all the time
                final Date time = (Date) ev.getNewValue();
                
                ERR.fine("PROP_TIME new value: " + time + ", " + (time != null ? time.getTime() : -1));
                ERR.fine("       lastSaveTime: " + new Date(lastSaveTime) + ", " + lastSaveTime);
                
                boolean reload = (lastSaveTime != -1) && ((time == null) || (time.getTime() > lastSaveTime));
                ERR.fine("             reload: " + reload);

                if (reload) {
                    // - post in AWT event thread because of possible dialog popup
                    // - acquire the write access before checking, so there is no
                    //   clash in-between and we're safe for potential reload.
                    SwingUtilities.invokeLater(
                        new Runnable() {
                            boolean inWriteAccess;

                            public void run() {
                                if (!inWriteAccess) {
                                    inWriteAccess = true;

                                    StyledDocument sd = getDoc();

                                    if (sd == null) {
                                        return;
                                    }

                                    // #57104 - avoid notifyModified() which takes file lock
                                    revertingUndoOrReloading = true;
                                    NbDocument.runAtomic(sd, this);
                                    revertingUndoOrReloading = false; // #57104

                                    return;
                                }
                                ERR.fine("checkReload starting"); // NOI18N
                                boolean noAsk = time == null || !isModified();
                                ERR.fine("checkReload noAsk: " + noAsk);
                                checkReload(noAsk);
                            }
                        }
                    );
                    ERR.fine("reload task posted"); // NOI18N
                }
            }

            if (Env.PROP_MODIFIED.equals(ev.getPropertyName())) {
                CloneableEditorSupport.this.firePropertyChange(
                    EditorCookie.Observable.PROP_MODIFIED, ev.getOldValue(), ev.getNewValue()
                );
            }

            // #129178 - update title if read-only state is externally changed
            if ("DataEditorSupport.read-only.changing".equals(ev.getPropertyName())) {  //NOI18N
                updateTitles();
            }
        }

        /** Initialization of the document.
        */
        public void run() {
            //             synchronized (getLock ()) {

            /* Remove existing listener before running the loading task
            * This should prevent firing of insertUpdate() during load (or reload)
            * which can prevent dedloks that sometimes occured during file reload.
            */
            addRemoveDocListener(getDoc(), false);

            try {
                loadExc = null;
                LOCAL_LOAD_TASK.set(true);
                loadDocument(kit,getDoc());
            } catch (IOException e) {
                loadExc = e;
                throw new DelegateIOExc(e);
            } finally {
                LOCAL_LOAD_TASK.set(null);
            }

            // opening the document, inform position manager
            getPositionManager().documentOpened(doc);

            // create new description of lines
            updateLineSet(true);

            setLastSaveTime(cesEnv().getTime().getTime());

            // Insert before-save undo event to enable unmodifying undo
            getUndoRedo().undoableEditHappened(new UndoableEditEvent(this, new BeforeSaveEdit(lastSaveTime)));

            // Start listening on changes in document
            addRemoveDocListener(getDoc(), true);
        }

        //        }
    }

    /** Generic undoable edit that delegates to the given undoable edit. */
    private class FilterUndoableEdit implements UndoableEdit {
        protected UndoableEdit delegate;

        FilterUndoableEdit() {
        }

        public void undo() throws CannotUndoException {
            if (delegate != null) {
                delegate.undo();
            }
        }

        public boolean canUndo() {
            if (delegate != null) {
                return delegate.canUndo();
            } else {
                return false;
            }
        }

        public void redo() throws CannotRedoException {
            if (delegate != null) {
                delegate.redo();
            }
        }

        public boolean canRedo() {
            if (delegate != null) {
                return delegate.canRedo();
            } else {
                return false;
            }
        }

        public void die() {
            if (delegate != null) {
                delegate.die();
            }
        }

        public boolean addEdit(UndoableEdit anEdit) {
            if (delegate != null) {
                return delegate.addEdit(anEdit);
            } else {
                return false;
            }
        }

        public boolean replaceEdit(UndoableEdit anEdit) {
            if (delegate != null) {
                return delegate.replaceEdit(anEdit);
            } else {
                return false;
            }
        }

        public boolean isSignificant() {
            if (delegate != null) {
                return delegate.isSignificant();
            } else {
                return true;
            }
        }

        public String getPresentationName() {
            if (delegate != null) {
                return delegate.getPresentationName();
            } else {
                return ""; // NOI18N
            }
        }

        public String getUndoPresentationName() {
            if (delegate != null) {
                return delegate.getUndoPresentationName();
            } else {
                return ""; // NOI18N
            }
        }

        public String getRedoPresentationName() {
            if (delegate != null) {
                return delegate.getRedoPresentationName();
            } else {
                return ""; // NOI18N
            }
        }
    }

    /** Undoable edit that is put before the savepoint. Its replaceEdit()
     * method will consume and wrap the edit that precedes the save.
     * If the edit is added to the begining of the queue then
     * the isSignificant() implementation guarantees that the edit
     * will not be removed from the queue.
     * When redone it marks the document as not modified.
     */
    private class BeforeSaveEdit extends FilterUndoableEdit {
        private long saveTime;

        BeforeSaveEdit(long saveTime) {
            this.saveTime = saveTime;
        }

        @Override
        public boolean replaceEdit(UndoableEdit anEdit) {
            if (delegate == null) {
                delegate = anEdit;

                return true; // signal consumed
            }

            return false;
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (!(anEdit instanceof BeforeModificationEdit) && !(anEdit instanceof SearchBeforeModificationEdit)) {
                /* UndoRedo.addEdit() must not be done lazily
                 * because the edit must be "inserted" before the current one.
                 */
                getUndoRedo().addEdit(new BeforeModificationEdit(saveTime, anEdit));

                return true;
            }

            return false;
        }

        @Override
        public void redo() {
            super.redo();

            if (saveTime == lastSaveTime) {
                justRevertedToNotModified = true;
            }
        }

        @Override
        public boolean isSignificant() {
            return (delegate != null);
        }
    }

    /** Edit that is created by wrapping the given edit.
     * When undone it marks the document as not modified.
     */
    private class BeforeModificationEdit extends FilterUndoableEdit {
        private long saveTime;

        BeforeModificationEdit(long saveTime, UndoableEdit delegate) {
            this.saveTime = saveTime;
            this.delegate = delegate;
            ERR.log(Level.FINEST, null, new Exception("new BeforeModificationEdit(" + saveTime +")")); // NOI18N
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if ((delegate == null) && !(anEdit instanceof SearchBeforeModificationEdit)) {
                delegate = anEdit;

                return true;
            }

            return delegate.addEdit(anEdit);
        }

        @Override
        public void undo() {
            super.undo();

            boolean res = saveTime == lastSaveTime;
            ERR.fine("Comparing saveTime and lastSaveTime: " + saveTime + "==" + lastSaveTime + " is " + res); // NOI18N
            if (res) {
                justRevertedToNotModified = true;
            }
        }
    }

    /** This edit is used to search for BeforeModificationEdit in UndoRedo
     * manager. This is not much nice solution, but well, there is not
     * much other chances to get inside UndoRedo.
     */
    private class SearchBeforeModificationEdit extends FilterUndoableEdit {
        SearchBeforeModificationEdit() {
        }

        @Override
        public boolean replaceEdit(UndoableEdit anEdit) {
            if (delegate == null) {
                delegate = anEdit;

                return true; // signal consumed
            }

            return false;
        }
    }

    /** An improved version of UndoRedo manager that locks document before
     * doing any other operations.
     */
    private final static class CESUndoRedoManager extends UndoRedo.Manager {
        private CloneableEditorSupport support;

        public CESUndoRedoManager(CloneableEditorSupport c) {
            this.support = c;
            super.setLimit(1000);
        }

        @Override
        public void redo() throws javax.swing.undo.CannotRedoException {
            final StyledDocument myDoc = support.getDocument();

            if (myDoc == null) {
                throw new javax.swing.undo.CannotRedoException(); // NOI18N
            }
            
            support.justRevertedToNotModified = false;
            new RenderUndo(0, myDoc);

            if (support.justRevertedToNotModified && support.isAlreadyModified()) {
                support.callNotifyUnmodified();
            }
        }

        @Override
        public void undo() throws javax.swing.undo.CannotUndoException {
            final StyledDocument myDoc = support.getDocument();

            if (myDoc == null) {
                throw new javax.swing.undo.CannotUndoException(); // NOI18N
            }

            support.justRevertedToNotModified = false;
            new RenderUndo(1, myDoc);

            if (support.justRevertedToNotModified && support.isAlreadyModified()) {
                support.callNotifyUnmodified();
            }
        }

        @Override
        public boolean canRedo() {
            final StyledDocument myDoc = support.getDocument();

            return new RenderUndo(2, myDoc).booleanResult;
        }

        @Override
        public boolean canUndo() {
            final StyledDocument myDoc = support.getDocument();

            return new RenderUndo(3, myDoc).booleanResult;
        }

        @Override
        public int getLimit() {
            final StyledDocument myDoc = support.getDocument();

            return new RenderUndo(4, myDoc).intResult;
        }

        @Override
        public void discardAllEdits() {
            final StyledDocument myDoc = support.getDocument();
            new RenderUndo(5, myDoc);
            // Insert before-save undo event to enable unmodifying undo
            undoableEditHappened(new UndoableEditEvent(support, support.new BeforeSaveEdit(support.lastSaveTime)));
        }

        @Override
        public void setLimit(int l) {
            final StyledDocument myDoc = support.getDocument();
            new RenderUndo(6, myDoc, l);
        }

        @Override
        public boolean canUndoOrRedo() {
            final StyledDocument myDoc = support.getDocument();

            return new RenderUndo(7, myDoc).booleanResult;
        }

        @Override
        public java.lang.String getUndoOrRedoPresentationName() {
            if (support.isDocumentReady()) {
                final StyledDocument myDoc = support.getDocument();
                return new RenderUndo(8, myDoc).stringResult;
            } else {
                return "";
            }
        }

        @Override
        public java.lang.String getRedoPresentationName() {
            if (support.isDocumentReady()) {
                final StyledDocument myDoc = support.getDocument();
                return new RenderUndo(9, myDoc).stringResult;
            } else {
                return "";
            }
        }

        @Override
        public java.lang.String getUndoPresentationName() {
            if (support.isDocumentReady()) {
                final StyledDocument myDoc = support.getDocument();
                return new RenderUndo(10, myDoc).stringResult;
            } else {
                return "";
            }
        }

        @Override
        public void undoOrRedo() throws javax.swing.undo.CannotUndoException, javax.swing.undo.CannotRedoException {
            final StyledDocument myDoc = support.getDocument();

            if (myDoc == null) {
                throw new javax.swing.undo.CannotUndoException(); // NOI18N
            }

            support.justRevertedToNotModified = false;
            new RenderUndo(11, myDoc);

            if (support.justRevertedToNotModified && support.isAlreadyModified()) {
                support.callNotifyUnmodified();
            }
        }

        private final class RenderUndo implements Runnable {
            private final int type;
            public boolean booleanResult;
            public int intResult;
            public String stringResult;

            public RenderUndo(int type, StyledDocument doc) {
                this(type, doc, 0);
            }

            public RenderUndo(int type, StyledDocument doc, int intValue) {
                this.type = type;
                this.intResult = intValue;

                if (doc instanceof NbDocument.WriteLockable) {
                    ((NbDocument.WriteLockable) doc).runAtomic(this);
                } else {
                    // if the document is not one of "NetBeans ready"
                    // that supports locking we do not have many 
                    // chances to do something. Maybe check for AbstractDocument
                    // and call writeLock using reflection, but better than
                    // that, let's leave this simple for now and wait for
                    // bug reports (if any appear)
                    run();
                }
            }

            public void run() {
                switch (type) {
                case 0:
                    CESUndoRedoManager.super.redo();

                    break;

                case 1:
                    CESUndoRedoManager.super.undo();

                    break;

                case 2:
                    booleanResult = CESUndoRedoManager.super.canRedo();

                    break;

                case 3:
                    booleanResult = CESUndoRedoManager.super.canUndo();

                    break;

                case 4:
                    intResult = CESUndoRedoManager.super.getLimit();

                    break;

                case 5:
                    CESUndoRedoManager.super.discardAllEdits();

                    break;

                case 6:
                    CESUndoRedoManager.super.setLimit(intResult);

                    break;

                case 7:
                    CESUndoRedoManager.super.canUndoOrRedo();

                    break;

                case 8:
                    stringResult = CESUndoRedoManager.super.getUndoOrRedoPresentationName();

                    break;

                case 9:
                    stringResult = CESUndoRedoManager.super.getRedoPresentationName();

                    break;

                case 10:
                    stringResult = CESUndoRedoManager.super.getUndoPresentationName();

                    break;

                case 11:
                    CESUndoRedoManager.super.undoOrRedo();

                    break;

                default:
                    throw new IllegalArgumentException("Unknown type: " + type);
                }
            }
        }
    }

    /** Special runtime exception that holds the original I/O failure.
     */
    static final class DelegateIOExc extends IllegalStateException {
        public DelegateIOExc(IOException ex) {
            super(ex.getMessage());
            initCause(ex);
        }
    }

}
