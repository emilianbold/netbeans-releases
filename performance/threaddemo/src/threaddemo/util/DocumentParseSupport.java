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
import javax.swing.event.*;
import javax.swing.text.StyledDocument;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.text.NbDocument;
import org.openide.util.Mutex;

// XXX also support direct parse from FileObject?
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
    
    private final EditorCookie.Observable edit;
    
    private StyledDocument document = null;
    private int listenerCount = 0; // for assertions only
    private final Listener listener;

    /**
     * Create a support based on an editor cookie and mutex.
     * @param edit the container for the document containing some parsable data
     * @param mutex a lock
     */
    protected DocumentParseSupport(EditorCookie.Observable edit, Mutex mutex) {
        super(mutex);
        this.edit = edit;
        listener = new Listener();
        edit.addPropertyChangeListener(listener);
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
        edit.prepareDocument();
        System.err.println("initiating...");//XXX
    }

    /**
     * Make sure the correct document is open, and that the correct listeners
     * are attached to it and not its predecessor.
     */
    private void refreshDocument() throws IOException {
        System.err.println("rD begin");//XXX
        StyledDocument oldDocument = document;
        // XXX is openDocument safe to call from any thread? probably yes, for now...
        edit.removePropertyChangeListener(listener);
        try {
            document = edit.openDocument();
        } finally {
            edit.addPropertyChangeListener(listener);
        }
        assert document != null;
        if (document != oldDocument) {
            if (oldDocument != null) {
                oldDocument.removeDocumentListener(listener);
                assert --listenerCount == 0;
            }
            document.addDocumentListener(listener);
            assert ++listenerCount == 1 : listenerCount;
        }
        System.err.println("rD end");//XXX
    }
    
    /**
     * Parse the document.
     * Calls {@link #doDerive(StyledDocument, List, Object)}.
     */
    protected final DerivationResult doDerive(final Object oldValue, Object underlyingDelta) throws Exception {
        if (document == null) {
            refreshDocument();
        }
        final List documentEvents; // List<DocumentEvent>
        if (underlyingDelta instanceof List) {
            documentEvents = (List)underlyingDelta;
        } else {
            documentEvents = null;
        }
        final DerivationResult[] val = new DerivationResult[1];
        final Exception[] exc = new Exception[1];
        document.render(new Runnable() {
            public void run() {
                try {
                    val[0] = doDerive(document, documentEvents, oldValue);
                } catch (Exception e) {
                    exc[0] = e;
                }
            }
        });
        if (exc[0] != null) {
            throw exc[0];
        }
        return val[0];
    }
    
    /**
     * Create the derived model from a text document.
     * Called with the read mutex and with read access to the document.
     * @param document the text document to parse
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
        assert document != null || permitsClobbering();
        if (document == null) {
            refreshDocument();
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
     * Called with the write mutex and holding a document lock if possible.
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
            System.err.println("DPS.iU");//XXX
            List l = new ArrayList(1); // List<DocumentEvent>
            l.add(e);
            invalidate(l);
        }
        
        public void removeUpdate(DocumentEvent e) {
            System.err.println("DPS.rU");//XXX
            List l = new ArrayList(1); // List<DocumentEvent>
            l.add(e);
            invalidate(l);
        }
        
        public void changedUpdate(DocumentEvent e) {
            // attr change - ignore
        }
        
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(EditorCookie.Observable.PROP_DOCUMENT)) {
                System.err.println("DPS.pC<PROP_DOCUMENT>");//XXX
                try {
                    refreshDocument();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                getMutex().readAccess(new Runnable() {
                    public void run() {
                        invalidate(evt);
                    }
                });
            }
        }
        
    }
    
}
