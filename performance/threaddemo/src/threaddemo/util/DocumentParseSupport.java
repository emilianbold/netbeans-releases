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
 * @author Jesse Glick
 */
public abstract class DocumentParseSupport extends TwoWaySupport {
    
    private final EditorCookie.Observable edit;
    
    private StyledDocument document = null;
    private final Listener listener;
    
    protected DocumentParseSupport(EditorCookie.Observable edit, Mutex mutex) {
        super(mutex);
        this.edit = edit;
        listener = new Listener();
        edit.addPropertyChangeListener(listener);
    }
    
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
    
    protected final void initiating() {
        edit.prepareDocument();
    }
    
    private void refreshDocument() throws IOException {
        StyledDocument oldDocument = document;
        // XXX is openDocument safe to call from any thread? probably yes, for now...
        document = edit.openDocument();
        assert document != null;
        if (document != oldDocument) {
            if (oldDocument != null) {
                oldDocument.removeDocumentListener(listener);
            }
            document.addDocumentListener(listener);
        }
    }
    
    protected final Object doDerive(final Object oldValue, Object underlyingDelta) throws Exception {
        if (document == null) {
            refreshDocument();
        }
        final List documentEvents; // List<DocumentEvent>
        if (underlyingDelta instanceof List) {
            documentEvents = (List)underlyingDelta;
        } else {
            documentEvents = null;
        }
        final Object[] val = new Object[1];
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
     * @return the new derived model value
     * @throws Exception (checked) in case of parsing problems
     */
    protected abstract Object doDerive(StyledDocument document, List documentEvents, Object oldValue) throws Exception;
    
    protected final Object doRecreate(final Object oldValue, final Object derivedDelta) throws Exception {
        assert document != null || permitsClobbering();
        if (document == null) {
            refreshDocument();
        }
        final Object[] val = new Object[1];
        final Exception[] exc = new Exception[1];
        Runnable r = new Runnable() {
            public void run() {
                try {
                    val[0] = doRecreate(document, oldValue, derivedDelta);
                } catch (Exception e) {
                    exc[0] = e;
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
     * mode, i.e. be prevented from modifying guard blocks.
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
     * @see NbDocument#WriteLockable
     */
    protected abstract Object doRecreate(StyledDocument document, Object oldValue, Object derivedDelta) throws Exception;
    
    private final class Listener implements DocumentListener, PropertyChangeListener {
        
        public void insertUpdate(DocumentEvent e) {
            List l = new ArrayList(1); // List<DocumentEvent>
            l.add(e);
            invalidate(l);
        }
        
        public void removeUpdate(DocumentEvent e) {
            List l = new ArrayList(1); // List<DocumentEvent>
            l.add(e);
            invalidate(l);
        }
        
        public void changedUpdate(DocumentEvent e) {
            // attr change - ignore
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(EditorCookie.Observable.PROP_DOCUMENT)) {
                try {
                    refreshDocument();
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                invalidate(evt);
            }
        }
        
    }
    
}
