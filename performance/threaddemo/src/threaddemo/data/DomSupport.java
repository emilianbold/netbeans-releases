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

package threaddemo.data;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.text.NbDocument;
import org.openide.util.*;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.*;
import threaddemo.model.*;

// XXX does not work, use TwoWaySupport when done

// XXX needs to attach a DOM event listener to the DOM document to handle structure changes

/**
 * Support class for DOM provider interface.
 * Implementation borrowed loosely from Minicomposer's ScoreSupport.
 * @author Jesse Glick
 */
public final class DomSupport implements DomProvider, Runnable, PropertyChangeListener, DocumentListener, ErrorHandler {
    
    static {
        // Need Xerces DOM - Crimson's DOM has no event support.
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
    }
    
    private static final RequestProcessor RP = new RequestProcessor("DomSupport");
    
    private final Phadhail ph;
    private final EditorCookie.Observable edit;
    private final Mutex mutex;
    private final Set listeners = new HashSet();
    private Document dom = null;
    private IOException parseException = null;
    private boolean addedEditorListener = false;
    private Reference lastUsedDocument = null; // Reference<javax.swing.text.Document>
    private Task prepareTask = null;
    
    public DomSupport(Phadhail ph, EditorCookie.Observable edit, Mutex mutex) {
        this.ph = ph;
        this.edit = edit;
        this.mutex = mutex;
    }
    
    public Document getDocument() throws IOException {
        try {
            return (Document)mutex.readAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    prepare().waitFinished();
                    if (dom != null &&
                            (parseException == null ||
                             PhadhailLookups.getLookup(ph).lookup(SaveCookie.class) != null)) {
                        return dom;
                    } else {
                        assert parseException != null : "parse did not finish as expected";
                        throw parseException;
                    }
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public void setDocument(final Document d) throws IOException {
        try {
            mutex.writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    final Document oldDom = dom;
                    if (d.equals(oldDom)) {
                        return null;
                    }
                    System.err.println("setDocument");
                    prepareTask = Task.EMPTY;
                    dom = d;
                    parseException = null;
                    final StyledDocument doc = edit.openDocument();
                    final BadLocationException[] e = new BadLocationException[] {null};
                    try {
                        NbDocument.runAtomic(doc, new Runnable() {
                            public void run() {
                                doc.removeDocumentListener(DomSupport.this);
                                System.err.println("removed doc listener");
                                try {
                                    generate(d, oldDom, doc);
                                } catch (BadLocationException ble) {
                                    e[0] = ble;
                                } finally {
                                    System.err.println("readded doc listener");
                                    doc.addDocumentListener(DomSupport.this);
                                }
                            }
                        });
                        if (e[0] != null) throw e[0];
                    } catch (BadLocationException ble) {
                        throw (IOException)new IOException().initCause(ble);
                    }
                    fireChange();
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    public boolean isValid() {
        return ((Boolean)mutex.readAccess(new Mutex.Action() {
            public Object run() {
                return (dom != null && parseException == null) ? Boolean.TRUE : Boolean.FALSE;
            }
        })).booleanValue();
    }
    
    public Task prepare() {
        // Note that notifyFinished might be called in RP
        return (Task)mutex.readAccess(new Mutex.Action() {
            public Object run() {
                if (prepareTask == null) {
                    System.err.println("preparing to parse");
                    prepareTask = RP.post(DomSupport.this);
                }
                return prepareTask;
            }
        });
    }
    
    public Mutex mutex() {
        return mutex;
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChange() {
        final ChangeListener[] ls;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            ls = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
        }
        final ChangeEvent ev = new ChangeEvent(this);
        mutex.readAccess(new Mutex.Action() {
            public Object run() {
                System.err.println("firing change");
                for (int i = 0; i < ls.length; i++) {
                    ls[i].stateChanged(ev);
                }
                return null;
            }
        });
    }
    
    public void run() {
        System.err.println("run");
        edit.prepareDocument().waitFinished();
        final javax.swing.text.Document doc = edit.getDocument();
        assert doc != null;
        if (!addedEditorListener) {
            System.err.println("adding editor listener");
            addedEditorListener = true;
            edit.addPropertyChangeListener(WeakListener.propertyChange(this, edit));
        }
        doc.render(new Runnable() {
            public void run() {
                try {
                    setResultAndParseException(parse(doc), null);
                } catch (IOException ioe) {
                    setResultAndParseException(dom, ioe);
                } catch (BadLocationException ble) {
                    IOException ioe = new IOException(ble.toString());
                    setResultAndParseException(dom, ioe);
                }
            }
        });
        javax.swing.text.Document lastDoc = null;
        if (lastUsedDocument != null) {
            lastDoc = (javax.swing.text.Document)lastUsedDocument.get();
        }
        if (lastDoc != doc) {
            if (lastDoc != null) {
                System.err.println("removing listener from old document");
                lastDoc.removeDocumentListener(this);
            }
            System.err.println("adding fresh document listener");
            doc.addDocumentListener(this);
            lastUsedDocument = new WeakReference(doc);
        }
    }
    
    private void setResultAndParseException(final Document d, final IOException e) {
        // Note that this will generally deadlock if mutex == EVENT.
        mutex.readAccess(new Mutex.Action() {
            public Object run() {
                System.err.println("parsed; exception=" + e + "; document=" + d);
                dom = d;
                parseException = e;
                fireChange();
                return null;
            }
        });
    }
    
    private synchronized void invalidate() {
        mutex.postWriteRequest(new Runnable() {
            public void run() {
                System.err.println("invalidated");
                if (prepareTask != null) {
                    prepareTask = null;
                    fireChange();
                }
            }
        });
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        String p = ev.getPropertyName();
        if (EditorCookie.Observable.PROP_DOCUMENT.equals(p) ||
                EditorCookie.Observable.PROP_MODIFIED.equals(p)) {
            System.err.println("Editor state changed");
            invalidate();
        }
    }
    
    public void changedUpdate(DocumentEvent ev) {
        // irrelevant - some text attributes
    }
    
    public void insertUpdate(DocumentEvent ev) {
        // Generally called with an AbstractDocument lock held.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                invalidate();
            }
        });
    }
    
    public void removeUpdate(DocumentEvent ev) {
        insertUpdate(ev);
    }
    
    private Document parse(final javax.swing.text.Document doc) throws IOException, BadLocationException {
        // We don't actually need to lock the phadhail for reading in this case.
        // But in general we might. So pretend we do.
        try {
            return (Document)mutex.readAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException, BadLocationException {
                    String text = doc.getText(0, doc.getLength());
                    try {
                        return XMLUtil.parse(new InputSource(new StringReader(text)), false, false, DomSupport.this, null);
                    } catch (SAXException e) {
                        throw (IOException)new IOException().initCause(e);
                    }
                }
            });
        } catch (MutexException e) {
            Exception e2 = e.getException();
            if (e2 instanceof IOException) {
                throw (IOException)e2;
            } else {
                throw (BadLocationException)e2;
            }
        }
    }
    
    private void generate(final Document d, Document oldDom, final javax.swing.text.Document doc) throws BadLocationException {
        // Again, we don't really need to lock things to write text in this example;
        // but it is indicative of more realistic situations where we might.
        try {
            mutex.writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws BadLocationException {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    CharArrayWriter wr = new CharArrayWriter();
                    try {
                        XMLUtil.write(d, baos, "UTF-8");
                    } catch (IOException ioe) {
                        assert false : ioe;
                        return null;
                    }
                    doc.remove(0, doc.getLength());
                    try {
                        doc.insertString(0, baos.toString("UTF-8"), null);
                    } catch (UnsupportedEncodingException e) {
                        assert false : e;
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (BadLocationException)e.getException();
        }
    }
    
    public void error(SAXParseException exception) throws SAXException {
        throw exception;
    }
    
    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }
    
    public void warning(SAXParseException exception) throws SAXException {
        throw exception;
    }
    
}
