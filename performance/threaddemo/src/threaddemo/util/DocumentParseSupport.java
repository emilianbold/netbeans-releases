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

package threaddemo.util;

import java.beans.*;
import java.io.IOException;
import java.lang.ref.*;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.event.*;
import javax.swing.text.StyledDocument;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.text.NbDocument;
import threaddemo.locking.Lock;
import threaddemo.locking.LockAction;

// XXX helper methods to parse/rewrite an entire document atomically using Reader/Writer

/**
 * Supports two-way parsing of an arbitrary model from a text document and
 * writing to the text document from the model.
 * <p>The underlying model is a text document. The deltas to the underlying model
 * in this implementation are document text changes or reload events, though that
 * fact is not visible to subclasses. The derived model must be defined by the
 * subclass.
 * @author Jesse Glick
 */
public abstract class DocumentParseSupport extends TwoWaySupport {
    
    private static final Logger logger = Logger.getLogger(DocumentParseSupport.class.getName());
    
    private final EditorCookie.Observable edit;

    /** document loaded; may be null initially */
    private StyledDocument document = null;
    private int listenerCount = 0; // for assertions only
    private int cookieListenerCount = 0; // for assertions only
    private final Listener listener;

    /**
     * Create a support based on an editor cookie and lock.
     * @param edit the container for the document containing some parsable data
     * @param lock a lock
     */
    protected DocumentParseSupport(EditorCookie.Observable edit, Lock lock) {
        super(lock);
        this.edit = edit;
        listener = new Listener();
        edit.addPropertyChangeListener(listener);
        cookieListenerCount++;
    }
    
    /**
     * In this implementation, deltas are either {@link PropertyChangeEvent}s
     * of {@link org.openide.cookies.EditorCookie.Observable#PROP_DOCUMENT} indicating that the whole
     * document changed (was reloaded, for example), or lists of {@link DocumentEvent}s.
     */
    protected final Object composeUnderlyingDeltas(Object underlyingDelta1, Object underlyingDelta2) {
        assert underlyingDelta1 instanceof PropertyChangeEvent || underlyingDelta1 instanceof List;
        assert underlyingDelta2 instanceof PropertyChangeEvent || underlyingDelta2 instanceof List;
        if (underlyingDelta1 instanceof PropertyChangeEvent) {
            // PROP_DOCUMENT that is. Need to recreate the whole thing generally.
            return underlyingDelta1;
        } else if (underlyingDelta2 instanceof PropertyChangeEvent) {
            // Ditto.
            return underlyingDelta2;
        } else {
            // Append changes.
            ((List)underlyingDelta1).addAll((List)underlyingDelta2);
            return underlyingDelta1;
        }
    }
    
    /**
     * In this implementation, prepares the document so that it will soon be loaded,
     * if it is not already.
     */
    protected final void initiating() {
        if (requiresUnmodifiedDocument()) {
            edit.prepareDocument();
            logger.finer("initiating...");
        }
    }

    /**
     * Make sure the correct document is open, and that the correct listeners
     * are attached to it and not its predecessor.
     * @param requireDocument if true, force a document to be loaded; if false,
     *                        permit {@link #document} to remain null, but refresh
     *                        it with a newer document if it has in fact changed
     */
    private void refreshDocument(boolean requireDocument) throws IOException {
        logger.finer("rD begin");
        StyledDocument oldDocument = document;
        edit.removePropertyChangeListener(listener);
        assert --cookieListenerCount == 0;
        try {
            //new Exception("will call " + (requireDocument ? "openDocument" : "getDocument") + " on " + this).printStackTrace();
            document = requireDocument ? edit.openDocument() : (document != null ? edit.getDocument() : null);
            //new Exception("called " + (requireDocument ? "openDocument" : "getDocument") + " on " + this).printStackTrace();
        } finally {
            edit.addPropertyChangeListener(listener);
            assert ++cookieListenerCount == 1;
        }
        assert !requireDocument || document != null;
        if (document != oldDocument) {
            if (oldDocument != null) {
                oldDocument.removeDocumentListener(listener);
                assert --listenerCount == 0;
            }
            if (document != null) {
                document.addDocumentListener(listener);
                assert ++listenerCount == 1 : listenerCount;
            }
        }
        logger.finer("rD end");
    }
    
    /**
     * Parse the document.
     * Calls {@link #doDerive(StyledDocument, List, Object)}.
     */
    protected final DerivationResult doDerive(final Object oldValue, Object underlyingDelta) throws Exception {
        if (document == null) {
            refreshDocument(requiresUnmodifiedDocument());
        }
        final List documentEvents; // List<DocumentEvent>
        if (underlyingDelta instanceof List) {
            documentEvents = (List)underlyingDelta;
        } else {
            documentEvents = null;
        }
        final DerivationResult[] val = new DerivationResult[1];
        final Exception[] exc = new Exception[1];
        Runnable r = new Runnable() {
            public void run() {
                try {
                    val[0] = doDerive(document, documentEvents, oldValue);
                } catch (Exception e) {
                    exc[0] = e;
                }
            }
        };
        if (document != null) {
            document.render(r);
        } else {
            r.run();
        }
        if (exc[0] != null) {
            throw exc[0];
        }
        return val[0];
    }
    
    /**
     * Declare whether the support always requires a document object, even to
     * parse an unmodified file.
     * <p>If true, {@link #doDerive(StyledDocument,List,Object)} is always given a
     * document; if the editor support had never been opened at all, it is
     * nonetheless opened (invisibly) just to provide this document to parse.
     * <p>If false, {@link #doDerive(StyledDocument,List,Object)} may be passed null for
     * its <code>document</code> parameter, meaning that the editor support has
     * not yet loaded a document. In this case the support is expected to run the
     * parse from the editor cookie's underlying storage, e.g. a file. This style
     * is potentially much more efficient when performing model reads from a large
     * number of (unmodified) files.
     * <p>Recreation always uses an open document regardless of this choice.
     * <p>The default value is true, i.e. always open the document for parsing.
     * @return true to always parse from a real document, or false to permit faster
     *         parses from underlying storage
     * @see EditorCookie#getDocument
     * @see EditorCookie#openDocument
     */
    protected boolean requiresUnmodifiedDocument() {
        return true;
    }
    
    /**
     * Create the derived model from a text document.
     * Called with the read lock and with read access to the document.
     * @param document the text document to parse, or may be null if
     *                 {@link #requiresUnmodifiedDocument} if false
     * @param documentEvents a list of {@link DocumentEvent} that happened since
     *                       the last parse, or null if unknown (do a full reparse)
     * @param oldValue the last derived model value, or null
     * @return the new derived model value plus the change made to it
     * @throws Exception (checked) in case of parsing problems
     */
    protected abstract DerivationResult doDerive(StyledDocument document, List documentEvents, Object oldValue) throws Exception;
    
    /**
     * Regenerates the document.
     * Calls {@link #doRecreate(StyledDocument, Object, Object)}.
     */
    protected final Object doRecreate(final Object oldValue, final Object derivedDelta) throws Exception {
        if (document == null) {
            refreshDocument(true);
        }
        final Object[] val = new Object[1];
        final Exception[] exc = new Exception[1];
        Runnable r = new Runnable() {
            public void run() {
                document.removeDocumentListener(listener);
                assert --listenerCount == 0;
                try {
                    val[0] = doRecreate(document, oldValue, derivedDelta);
                } catch (Exception e) {
                    exc[0] = e;
                } finally {
                    document.addDocumentListener(listener);
                    assert ++listenerCount == 1;
                }
            }
        };
        if (runAsUser(derivedDelta)) {
            NbDocument.runAtomicAsUser(document, r);
        } else {
            NbDocument.runAtomic(document, r);
        }
        if (exc[0] != null) {
            throw exc[0];
        }
        return val[0];
    }
    
    /**
     * Decide whether the given change to the derived model must occur in "user"
     * mode, that is, be prevented from modifying guard blocks.
     * The default implementation always returns false.
     * @return true to run using {@link NbDocument#runAtomicAsUser}, false for
     *              {@link NbDocument#runAtomic}
     */
    protected boolean runAsUser(Object derivedDelta) {
        return false;
    }
    
    /**
     * Update the text document to reflect changes in the derived model.
     * Called with the write lock and holding a document lock if possible.
     * @param document the document to modify
     * @param oldValue the old derived model, if any
     * @param derivedDelta the change to the derived model
     * @return the new derived model
     * @see org.openide.text.NbDocument.WriteLockable
     */
    protected abstract Object doRecreate(StyledDocument document, Object oldValue, Object derivedDelta) throws Exception;
    
    /**
     * Listens to changes in identity or content of the text document.
     */
    private final class Listener implements DocumentListener, PropertyChangeListener {
        
        // XXX getting >1 i/rU for one change?
        
        public void insertUpdate(DocumentEvent e) {
            logger.finer("DPS.iU");
            documentUpdate(e);
        }
        
        public void removeUpdate(DocumentEvent e) {
            logger.finer("DPS.rU");
            documentUpdate(e);
        }
        
        private void documentUpdate(DocumentEvent e) {
            final List l = new ArrayList(1); // List<DocumentEvent>
            l.add(e);
            getLock().read(new LockAction() {
                public Object run() {
                    invalidate(l);
                    return null;
                }
            });
        }
        
        public void changedUpdate(DocumentEvent e) {
            // attr change - ignore
        }
        
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(EditorCookie.Observable.PROP_DOCUMENT)) {
                logger.finer("DPS.pC<PROP_DOCUMENT>");
                //new Exception("got PROP_DOCUMENT on " + DocumentParseSupport.this).printStackTrace();
                try {
                    refreshDocument(true);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                // Avoid blocking: because CES fires
                // PROP_DOCUMENT from within a RP task, and we may already be locking
                // the EQ with CES.open or .openDocument.
                getLock().readLater(new Runnable() {
                    public void run() {
                        invalidate(evt);
                    }
                });
            }
        }
        
    }
    
}
