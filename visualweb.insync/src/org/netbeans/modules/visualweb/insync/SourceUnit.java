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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.StyledDocument;

import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.cookies.SaveCookie;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.netbeans.modules.visualweb.insync.Unit.State;
//NB60 import org.netbeans.modules.visualweb.insync.faces.refactoring.MdrInSyncSynchronizer;

import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;

/**
 * A partial Unit implementation that provides common functionality for all source-based Units.
 * @author Carl Quinn
 */
public abstract class SourceUnit implements Unit, DocumentListener, UndoableEditListener, PropertyChangeListener, FileChangeListener {

    // Information about this unit
    protected FileObject fobj;  // may be null, e.g. for testsuite & stand-alone use
    protected StyledDocument styledDocument;
    protected UndoManager undoManager;
    private DataObject dataObject = null;
    private EditorCookie ec = null;
    
    protected Date lastModified;
    private FileChangeListener lastModifiedTracker;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a SourceUnit from an existing source Document
     * @param dobj
     */
    protected SourceUnit(FileObject fobj, UndoManager undoManager) {
        this.fobj = fobj;
        // Cache the data object. If the data object is not cached
        // then diffrent CloneableEditorSupport may be created for
        // the FileObject on request and our added listener may be lost.

        try {
            dataObject = DataObject.find(fobj);
        } catch (Exception exc){
            ErrorManager.getDefault().notify(exc);
        }
        this.undoManager = undoManager;
        //Trace.enableTraceCategory("insync");

        state = State.SOURCEDIRTY;  // need to perform initial read of source into model

        // Listen for the editor closing the document so that we can release it
        ec = (EditorCookie)Util.getCookie(fobj, EditorCookie.class);
        if (ec instanceof CloneableEditorSupport)
            ((CloneableEditorSupport)ec).addPropertyChangeListener(this);

        styledDocument = ec.getDocument();  // get if open. Do not block

        if (styledDocument != null){
            styledDocument.addDocumentListener(this);
            styledDocument.addUndoableEditListener(this);
        }else{

            // Add listener to the File Object so that we can listen to the FileObject modification
            // When the StyledDocument is obtained, the listener will be removed and then on we listen
            // only to the StyledDocument events.
            fobj.addFileChangeListener(this);
        }
        lastModified = fobj.lastModified();
        lastModifiedTracker = new FileChangeListener() {
            public void fileAttributeChanged(FileAttributeEvent fe) {}
            public void fileChanged(FileEvent fe) {
                lastModified = SourceUnit.this.fobj.lastModified();
            }
            public void fileDataCreated(FileEvent fe) {}
            
            public void fileDeleted(FileEvent fe) {}

            public void fileFolderCreated(FileEvent fe) {}

            public void fileRenamed(FileRenameEvent fe) {
                lastModified = SourceUnit.this.fobj.lastModified();
            }
        };
        fobj.addFileChangeListener(lastModifiedTracker);
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#destroy()
     */
    public void destroy() {
        releaseDocument();
        // Make sure FileObject listener is also removed that was added by the
        // above releaseDocument() call
        if (fobj != null) {
            fobj.removeFileChangeListener(this);
            fobj.removeFileChangeListener(lastModifiedTracker);
        }
        listeners = null;

        if (ec instanceof CloneableEditorSupport) {
            ((CloneableEditorSupport)ec).removePropertyChangeListener(this);
            ec = null;
        }
        undoManager = null;
    }
    
    //----------------------------------- Implementation of file change listener ------------------
    
    public void fileFolderCreated (FileEvent fe){}
    public void fileDataCreated (FileEvent fe){}
    public void fileChanged(FileEvent fe){
        // If both the model and the disk copy have been changed,
        // take the model.
        if (state != State.MODELDIRTY) {
            setSourceDirty();
        }
    }
    public void fileDeleted (FileEvent fe){}
    public void fileRenamed (FileRenameEvent fe){}
    public void fileAttributeChanged (FileAttributeEvent fe){}

    
    //---------------------------------------------------------------------------- Document handling
    
   /**
     * Release the current document and all listener hooks. The document will have to be grabbed 
     * again before use.
     */
    protected void releaseDocument() {
        if (styledDocument != null) {
            styledDocument.removeDocumentListener(this);
            styledDocument.removeUndoableEditListener(this);
            styledDocument = null;
            
            //need to tell the undo manager to clear events on the Undo/Redo stack
            if (undoManager != null) { // XXX Prevent NPE from leaked unit?
                undoManager.notifyBufferEdited(this);
            }
            
            // Add back the FileObject change listener, now that we no longer 
            // listen to the StyledDocument changes
            if (fobj != null) { // XXX Prevent NPE from leaked unit?
                fobj.addFileChangeListener(this);
            }
        }
    }
    
    /*
     * Looks like the editor is getting a new document--release our grip on it for now.
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (EditorCookie.Observable.PROP_DOCUMENT.equals(event.getPropertyName())) {
            if (event.getNewValue() == null) {
/*//NB6.0
                // Bug Fix # 6473201 llegalStateException, When renaming a page
                // When a document (e.g. managed-beans.xml) is reloaded during refactoring
                // do not set the source dirty. The changes to the model will be flushed
                // at the end of refactoring.
                if (!MdrInSyncSynchronizer.get().isRefactoringSessionInProgress()) {
*/
                Date newLastModified = fobj.lastModified();
                if (lastModified.equals(newLastModified)) {
                    setSourceDirty();
                } else {
                    lastModified = newLastModified;
                }
/*
                }
//*/
                releaseDocument();
            }
            if ((event.getNewValue() != null) && (event.getOldValue() == null)){
                // Remove the FileObject change listener, now that we start
                // listening to the StyledDocument changes
                fobj.removeFileChangeListener(this);
                styledDocument = (StyledDocument) event.getNewValue();
                styledDocument.addDocumentListener(this);
                styledDocument.addUndoableEditListener(this);
            }
   
        } else if (EditorCookie.Observable.PROP_MODIFIED.equals(event.getPropertyName())) {
            // TODO !EAT: Take a look at cleaning up the need for listeners ?
            Boolean newValue = (Boolean) event.getNewValue();
            if (newValue != null && newValue.equals(Boolean.FALSE)) {
                fireSaved();
            }
        }
    }
    
    //------------------------------------------------------------------------------- State tracking
    
    State state = State.CLEAN;
    
    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#getState()
     */
    public State getState() {
        return state;
    }
    
    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#getErrors()
     */
    public ParserAnnotation[] getErrors() {
        return ParserAnnotation.EMPTY_ARRAY;
    }
    
    /**
     * Called by various subclasses when they actually mutate their model
     */
    public void setModelDirty() {
        //This would be good, but many sync() handlers would need a lock and cant place it with source dirty...
        //if (writerCount == 0)
        //    throw new IllegalStateException("Illegal model modification without a lock " + name);
        if (state == State.SOURCEDIRTY)
            throw new IllegalStateException("Illegal model modification with dirty source " + getName());
        if (state == State.MODELDIRTY)
            return; // Already dirty
        // We know that this is just a temporary state and that a flush will be coming soon anyway
        //markDocumentModified();
        if (state == State.CLEAN) {
            assert Trace.trace("insync", "SU.setModelDirty UpToDate => ModelDirty");
            state = State.MODELDIRTY;
            fireModelDirtied();
        }
    }
    
    /**
     * Called by document listeners to let us know that our buffer is dirty and needs re-syncing
     */
    public void setSourceDirty() {
        if (state == State.MODELDIRTY) {
            // When a file is moved, it is copied to new destination, and old file is deleted.
            // We could be notified of the copy being created prior to the delete, in that case
            // I will still be listening on document events that do not pertain to me anymore, so
            // ignore that case.
            if (fobj != null && !fobj.isValid())
                return;
            throw new IllegalStateException("Illegal source modification with dirty model " + getName());
        }
        if (state != State.SOURCEDIRTY) {
            // See above, breaking it out so test is not always done
            if (fobj != null && !fobj.isValid())
                return;
            if (state == State.CLEAN) {
                assert Trace.trace("insync", "SU.setModelDirty Clean => SourceDirty");
                state = State.SOURCEDIRTY;
            } else if (state == State.BUSTED) {
                assert Trace.trace("insync", "SU.setModelDirty Busted => SourceDirty");
                state = State.SOURCEDIRTY;
            }
            fireSourceDirtied();
        }
    }
    
    /**
     * Return true if my state is busted.
     * @return
     */
    public boolean isBusted() {
        return state.isBusted();
    }
    
    /**
     * Mark the source as being busted - e.g. cannot be parsed.
     * @todo Provide notification of invalid state changes?
     */
    public void setBusted() {
        if (state == State.BUSTED)
            return; // Already busted
        if (state == State.SOURCEDIRTY) {
            assert Trace.trace("insync", "SU.setInvalid SourceDirty => Busted");
            state = State.BUSTED;
        } else { // should only be called during a sync - from source dirty state
            throw new IllegalStateException("Illegal source busting from " + state + " "+ getName());
        }
    }
    
    /**
     *
     */
    public void setClean() {
        //Note: applying a model brings you up to date but not saved - don't
        // call notifyUnmodified
        if (state == State.SOURCEDIRTY)
            assert Trace.trace("insync", "SU.setSourceUpToDate SourceDirty => Clean");
        else if (state == State.BUSTED)
            assert Trace.trace("insync", "SU.setSourceUpToDate Busted => Clean");
        else if (state == State.MODELDIRTY)
            assert Trace.trace("insync", "SU.setSourceUpToDate ModelDirty => Clean");
        state = State.CLEAN;
    }
    
    
    //----------------------------------------------------------------------------- DocumentListener
    
    /*
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {
        assert Trace.trace("insync-listener", "SU.changedUpdate");
        //setSourceDirty();  // these are usually non-substantial changes but things
        // like editor annotations uses attributes so just adding a breakpoint to
        // a line will cause a changedUpdate which we definitely don't want to treat
        // as an insert/delete/source dirty operation
    }
    
    /*
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
        assert Trace.trace("insync-listener", "SU.insertUpdate");
        undoManager.notifyBufferEdited(this);
        setSourceDirty();
    }
    
    /*
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
        assert Trace.trace("insync-listener", "SU.removeUpdate");
        undoManager.notifyBufferEdited(this);
        setSourceDirty();
    }
    
    /*
     * @see javax.swing.event.UndoableEditListener#undoableEditHappened(javax.swing.event.UndoableEditEvent)
     */
    public void undoableEditHappened(UndoableEditEvent e) {
        if (undoManager != null) {
            undoManager.notifyUndoableEditEvent(this);
        }
    }
    
    //-------------------------------------------------------------------------------------- Locking
    
    int readerCount;
    int writerCount;
    Thread writingThread;
    
    /**
     * Fetches the current writing thread if there is one. This can be used to distinguish whether a
     * method is being called as part of an existing modification or if a lock needs to be acquired
     * and a new transaction started.
     *
     * @return the thread actively modifying the document or <code>null</code> if there are no
     *         modifications in progress
     */
    protected synchronized final Thread getWritingThread() {
        return writingThread;
    }
    
    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#writeLock(org.netbeans.modules.visualweb.insync.UndoEvent)
     */
    public synchronized final void writeLock(UndoEvent event) {
        //if (state == State.SOURCEDIRTY)
        //    throw new IllegalStateException("SU.writeLock attempt to writeLock a model with dirty source");
        //if (state == State.BUSTED) {
        //    throw new IllegalStateException("SU.writeLock attempt to writeLock a model with busted source");
        //}
        try {
            while (readerCount > 0 || writingThread != null) {
                if (Thread.currentThread() == writingThread) {
                    /*
                    if (notifyingListeners) {
                        // Assuming one doesn't do something wrong in a subclass this should only
                        // happen if a UnitListener tries to mutate the unit.
                        throw new IllegalStateException("Attempt to mutate in notification");
                    }*/
                    writerCount++;
                    return;
                }
                wait();
            }
            writingThread = Thread.currentThread();
            writerCount = 1;
            firstWriteLock();
            //if (doc instanceof BaseDocument)
            //    ((BaseDocument)doc).atomicLock();
            // or even:
            // doc.writeLock();
        } catch (InterruptedException e) {
            throw new UnsupportedOperationException("Interrupted attempt to aquire write lock");
        }
        return;
    }
    
    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#writeUnlock(org.netbeans.modules.visualweb.insync.UndoEvent)
     */
    public synchronized final boolean writeUnlock(UndoEvent event) {
        if (--writerCount <= 0) {
            //if (doc instanceof BaseDocument)
            //    ((BaseDocument)doc).atomicUnlock();
            // or even:
            // doc.writeUnlock();
            writerCount = 0;
            flush();
            writingThread = null;
            lastWriteUnlock();
            notifyAll();
            return true;
        }
        return false;
    }
    
    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#isWriteLocked()
     */
    public boolean isWriteLocked() {
        return writerCount > 0;
    }
    
    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#readLock()
     */
    public synchronized final void readLock() {
        try {
            while (writingThread != null) {
                if (writingThread == Thread.currentThread()) {
                    // writer has full read access.... may try to acquire
                    // lock in notification
                    return;
                }
                wait();
            }
            readerCount += 1;
        } catch (InterruptedException e) {
            throw new UnsupportedOperationException("Interrupted attempt to aquire read lock");
        }
    }
    
    /*
     * @see org.netbeans.modules.visualweb.insync.Unit#readUnlock()
     */
    public synchronized final void readUnlock() {
        if (writingThread == Thread.currentThread()) {
            // writer has full read access.... may try to acquire
            // lock in notification
            return;
        }
        if (readerCount <= 0)
            throw new IllegalStateException("BAD_LOCK_STATE");
        readerCount -= 1;
        notify();
    }
    
    //---------------------------------------------------------------------------------------- Input
    
    /**
     * Read the actual characters from the source document's content. Concrete subclasses must
     * override this method to process the buffer characters into the model.
     */
    protected abstract void read(char[] cbuf, int len);
    
    /**
     * Load the document into buf
     */
    private final Util.BufferResult loadBuf() {
        Util.BufferResult bufferResult;
        if (styledDocument == null) {
            bufferResult = Util.loadFileObjectBuffer(fobj);
        } else {
            bufferResult = Util.loadDocumentBuffer(styledDocument);
        }
        return bufferResult;
    }
    
    /**
     * Implicit read. Read the document supplied during construction into this model.
     *
     * @return whether or not the read affected the model.
     */
    public boolean sync() {  
        // make sure it is necessary & ok to read.
        //assert Trace.trace("insync", "SU.sync of " + getName() + " state:" + state + " len:" + styledDocument.getLength());
        if (state == State.CLEAN)
            return false;
        if (state == State.MODELDIRTY) {
            assert Trace.trace("insync", "SU.sync attempt to read source into a dirty model");
            //Trace.printStackTrace();
            return false;
        }
        
        Util.BufferResult bufferResult = loadBuf();
        read(bufferResult.getBuffer(), bufferResult.getSize());
                 
        if (state == State.SOURCEDIRTY) // read() may have set an error, if so don't do to Clean
            setClean();  //state = State.CLEAN;
        
        return true;
    }
    
    //--------------------------------------------------------------------------------------- Output
    
    /**
     * @param w
     * @throws java.io.IOException
     */
    public abstract void writeTo(Writer w) throws java.io.IOException;
    
    /**
     * @param out
     * @throws java.io.IOException
     */
    public void writeTo(OutputStream out) throws java.io.IOException {
        Writer w = new BufferedWriter(new OutputStreamWriter(out));
        writeTo(w);
        w.flush();
    }
    
    /**
     * @param out
     */
    public final void dumpTo(OutputStream out) {
        PrintWriter w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
        dumpTo(w);
        w.flush();
    }
    
    /**
     * Internal routine for performing the smallest single update operation to the document given an
     * old document and a new text string.
     *
     * @param newText  The new document text contents.
     */
    protected boolean minimalReplace(String newText) {
        try {
            Util.BufferResult bufferResult = loadBuf();
            char[] buf = bufferResult.getBuffer();
            int end = styledDocument.getLength();
            int newEnd = newText.length();
            int start = 0;
            while (start < end && start < newEnd && buf[start] == newText.charAt(start))
                start++;
            while (end > start && newEnd > start && buf[end-1] == newText.charAt(newEnd-1)) {
                end--;
                newEnd--;
            }
            if (end > start || newEnd > start) {
                String newSeg = (start > 0 || newEnd < newText.length())
                ? newText.substring(start, newEnd)
                : newText;
                styledDocument.remove(start, end-start);
                styledDocument.insertString(start, newSeg, null);
                return true;
            }
        } catch (javax.swing.text.BadLocationException e) {
            // we know the location we passed is good...
            assert Trace.trace("insync", "Unexpected error in SU.flush: " + e);
        }
        return false;
    }
    
    /**
     * Flush this unit to its document, writing changes as needed and updating flags.
     *
     * @see #writeLock
     * @return true iff the document was written to by the operation public abstract boolean
     *         flush();
     */
    public boolean flush() {
        // make sure it is necessary & ok to write.
        Trace.trace("insync", "SU.flush of " + getName() + " state:" + state);
        if (state == State.CLEAN)
            return false;
        
        // these two should not happen since we should have caught this at writeLock time
        if (state == State.SOURCEDIRTY) {
            assert Trace.trace("insync", "SU.flush attempt to flush a model with dirty source");
            assert Trace.printStackTrace();
            return false;
        }
        if (state == State.BUSTED) {
            assert Trace.trace("insync", "SU.flush attempt to flush a model with busted source");
            return false;
        }
        
        //grabDocument(State.MODELDIRTY, true);  // if the disk copy was dirty, too bad: nothing we can do
        if(styledDocument == null){
            styledDocument = Util.retrieveDocument(fobj, true);
        }
        
        final boolean[] didReplace = new boolean[1];
        
        // Flush to the document if the document exists
        if(styledDocument != null){
//            System.out.println("SU.flush of " + getName() + " state:" + state + "Mode: Document");
            // Seems no op
            startFlush();
            try {
                // Stop listening to doc while it is us doing the writing
                styledDocument.removeDocumentListener(this);
                
                // Lock the document atomically
                // !TN TODO: BaseDocument.replace() should do this automatically;
                // investigating that now
                NbDocument.runAtomic(styledDocument, new Runnable() {
                    public void run() {
                        try {
                            Writer w = new StringWriter();
                            writeTo(w);
                            didReplace[0] = minimalReplace(w.toString());
                        } catch (java.io.IOException e) {
                            //!CQ TODO: log this better
                            assert Trace.trace("insync", "Unexpected error in SU.flush: " + e);
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                });
            } finally {
                // Start listening again
                styledDocument.addDocumentListener(this);
                // Seems no op
                endFlush(didReplace[0]);
            }
        }
        
        // Flush file documents -- used for testing
        if (styledDocument instanceof FileDocument) {
            FileDocument fdoc = (FileDocument)styledDocument;
            try {
                fdoc.write();  // flush to file
            } catch (java.io.IOException e) {
                assert Trace.trace("insync", "Can't write file: " + getName());
            }
        }
        
        //state = State.CLEAN;
        setClean();
        return didReplace[0];
    }
    
    /**
     * Saves the owning file using project apis
     */
    public void save(){
        DataObject dataObject = getDataObject();
        if (dataObject != null && state == State.CLEAN) {
            SaveCookie cookie = (SaveCookie) dataObject.getCookie(SaveCookie.class);
            if (cookie != null) {
                try {
                    cookie.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    /**
     * Writer implementation to assist with brute-force output position rtacking.
     * @author cquinn
     */
    public class CountingWriter extends Writer {
        public int pos;
        public void close() {}
        public void flush() {
            //System.err.flush();
        }
        public void write(char[] buf) {
            //System.err.print("("+pos+")");
            //System.err.print(buf);
            pos += buf.length;
        }
        public void write(char[] buf, int off, int len) {
            //System.err.print("("+pos+")");
            //System.err.print(new String(buf).substring(off, len));
            pos += len;
        }
        public void write(int c) {
            //System.err.print("("+pos+")");
            //System.err.print(c);
            pos += 1;
        }
        public void write(String str) {
            //System.err.print("("+pos+")");
            //System.err.print(str);
            pos += str.length();
        }
        public void write(String str, int off, int len) {
            //System.err.print("("+pos+")");
            //System.err.print(str.substring(off, len));
            pos += len;
        }
    }
    
    //------------------------------------------------------------------------------------ Accessors
    
    /**
     * @return
     */
    public String getName() {
        return FileUtil.toFile(fobj).getAbsolutePath();
    }
    
    /**
     * @return
     */
    public StyledDocument getSourceDocument() {
        if(styledDocument == null){
            styledDocument = Util.retrieveDocument(fobj, true);
        }
        if(state != State.MODELDIRTY){
            state = State.CLEAN;
        }
        return styledDocument;
    }
    
    /**
     * @return
     */
    public FileObject getFileObject() {
        return fobj;
    }
    
    /**
     * @return
     */
    public DataObject getDataObject() {
        if (fobj != null && !fobj.isValid())
            return null;
        return Util.findDataObject(fobj);
    }
    
    protected HashSet listeners = null;
    public void addListener(SourceUnitListener listener) {
        if (listeners == null)
            listeners = new HashSet();
        listeners.add(listener);
    }
    public void removeListener(SourceUnitListener listener) {
        if (listeners == null)
            return;
        listeners.remove(listener);
    }
    
    /**
     * Notify my listeners of the fact that my model has been made dirty.
     *
     */
    protected void fireModelDirtied() {
        if (listeners == null)
            return;
        for (Iterator iterator = listeners.iterator(); iterator.hasNext(); ) {
            SourceUnitListener listener = (SourceUnitListener) iterator.next();
            listener.sourceUnitModelDirtied(this);
        }
    }
    
    /**
     * Notify my listeners of the fact that my source has been made dirty.
     *
     */
    protected void fireSourceDirtied() {
        if (listeners == null)
            return;
        for (Iterator iterator = listeners.iterator(); iterator.hasNext(); ) {
            SourceUnitListener listener = (SourceUnitListener) iterator.next();
            listener.sourceUnitSourceDirtied(this);
        }
    }
    
    protected void fireSaved() {
        if (listeners == null)
            return;
        for (Iterator iterator = listeners.iterator(); iterator.hasNext(); ) {
            SourceUnitListener listener = (SourceUnitListener) iterator.next();
            listener.sourceUnitSaved(this);
        }
    }
    
    /**
     * Improve the readability of String display in debugger's variable inspector.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(30);
        sb.append("["); // NOI18N
        toString(sb);
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Subclasses should override to Improve the readability of String display in debugger's variable inspector.
     */
    protected void toString(StringBuffer sb) {
        sb.append(getClass().getName().substring(getClass().getPackage().getName().length() + 1));
        sb.append(" fobj: ");
        if (fobj == null) {
            sb.append("null");
        } else {
            sb.append(fobj.getNameExt());
        }
    }
    
    protected void startFlush() {
    }
    
    protected void endFlush(boolean madeDirty) {
    }

    protected synchronized void firstWriteLock() {
        //nop
    }

    protected synchronized void lastWriteUnlock() {
        //nop
    }
    
}
