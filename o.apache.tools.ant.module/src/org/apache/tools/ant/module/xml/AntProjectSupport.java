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

package org.apache.tools.ant.module.xml;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class AntProjectSupport implements AntProjectCookie.ParseStatus, javax.swing.event.DocumentListener,
    /*FileChangeListener,*/ PropertyChangeListener {
    
    private FileObject fo;

    private Document projDoc = null; // [PENDING] SoftReference
    private Throwable exception = null;
    private boolean parsed = false;
    private java.lang.ref.WeakReference styledDocRef = null;
    private Object parseLock; // see init()

    private Set listeners; // see init(); Set<ChangeListener>
    private EditorCookie.Observable editor = null;
    
    private DocumentBuilder documentBuilder;
    
    // milliseconds of quiet time after a textual document change after which
    // changes will be fired and the XML may be reparsed
    private static final int REPARSE_DELAY = 3000;

    public AntProjectSupport (FileObject fo) {
        this.fo = fo;
        parseLock = new Object ();
        listeners = new HashSet ();
        rp = new RequestProcessor("AntProjectSupport[" + fo + "]"); // NOI18N
    }
  
    private synchronized EditorCookie.Observable getEditor() {
        FileObject fo = getFileObject();
        if (fo == null) return null;
        if (editor == null) {
            try {
                editor = (EditorCookie.Observable)DataObject.find(fo).getCookie(EditorCookie.Observable.class);
                if (editor != null) {
                    editor.addPropertyChangeListener(WeakListeners.propertyChange(this, editor));
                }
            } catch (DataObjectNotFoundException donfe) {
                AntModule.err.notify(ErrorManager.INFORMATIONAL, donfe);
            }
        }
        return editor;
    }
    
    public File getFile () {
        FileObject fo = getFileObject();
        if (fo != null) {
            return FileUtil.toFile(fo);
        } else {
            return null;
        }
    }
    
    public FileObject getFileObject () {
        if (fo != null && !fo.isValid()) { // #11065
            return null;
        }
        return fo;
    }
    
    public void setFile (File f) { // #11979
        fo = FileUtil.toFileObject(f);
        invalidate ();
    }
    
    public void setFileObject (FileObject fo) { // #11979
        this.fo = fo;
        invalidate ();
    }
    
    public boolean isParsed() {
        return parsed;
    }
    
    public Document getDocument () {
        if (parsed) {
            return projDoc;
        }
        synchronized (parseLock) {
            if (parsed) {
                return projDoc;
            }
            parseDocument ();
            return projDoc;
        }
    }
    
    public Throwable getParseException () {
        if (parsed) {
            return exception;
        }
        synchronized (parseLock) {
            if (parsed) {
                return exception;
            }
            parseDocument ();
            return exception;
        }
    }
    
    /**
     * Make a DocumentBuilder object for use in this support.
     * Thread-safe, but of course the result is not.
     * @throws Exception for various reasons of configuration
     */
    private static synchronized DocumentBuilder createDocumentBuilder() throws Exception {
        //DocumentBuilderFactory factory = (DocumentBuilderFactory)Class.forName(XERCES_DOCUMENT_BUILDER_FACTORY).newInstance();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setErrorHandler(ErrHandler.DEFAULT);
        return documentBuilder;
    }
    
    /**
     * XML parser error handler; passes on all errors.
     */
    private static final class ErrHandler implements ErrorHandler {
        static final ErrorHandler DEFAULT = new ErrHandler();
        private ErrHandler() {}
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
    
    private static EditorKit findKit(JEditorPane[] panes) {
        EditorKit kit;
        if (panes != null) {
            kit = panes[0].getEditorKit();
        } else {
            kit = JEditorPane.createEditorKitForContentType("text/xml"); // NOI18N
            if (kit == null) {
                // #39301: fallback; can happen if xml/text-edit is disabled
                kit = new DefaultEditorKit();
            }
        }
        assert kit != null;
        return kit;
    }
    
    /**
     * Utility method to get a properly configured XML input source for a script.
     */
    public static InputSource createInputSource(FileObject fo, EditorCookie editor, final StyledDocument document) throws IOException, BadLocationException {
        final StringWriter w = new StringWriter(document.getLength());
        final EditorKit kit = findKit(editor.getOpenedPanes());
        final IOException[] ioe = new IOException[1];
        final BadLocationException[] ble = new BadLocationException[1];
        document.render(new Runnable() {
            public void run() {
                try {
                    kit.write(w, document, 0, document.getLength());
                } catch (IOException e) {
                    ioe[0] = e;
                } catch (BadLocationException e) {
                    ble[0] = e;
                }
            }
        });
        if (ioe[0] != null) {
            throw ioe[0];
        } else if (ble[0] != null) {
            throw ble[0];
        }
        InputSource in = new InputSource(new StringReader(w.toString()));
        if (fo != null) { // #10348
            try {
                in.setSystemId(fo.getURL().toExternalForm());
            } catch (FileStateInvalidException e) {
                assert false : e;
            }
            // [PENDING] Ant's ProjectHelper has an elaborate set of work-
            // arounds for inconsistent parser behavior, e.g. file:foo.xml
            // works in Ant but not with Xerces parser. You must use just foo.xml
            // as the system ID. If necessary, Ant's algorithm could be copied
            // here to make the behavior match perfectly, but it ought not be necessary.
        }
        return in;
    }
    
    private void parseDocument () {
        assert Thread.holdsLock(parseLock); // so it is OK to use documentBuilder
        FileObject fo = getFileObject ();
        AntModule.err.log ("AntProjectSupport.parseDocument: fo=" + fo);
        try {
            if (documentBuilder == null) {
                documentBuilder = createDocumentBuilder();
            }
            EditorCookie editor = getEditor ();
            Document doc;
            if (editor != null) {
                final StyledDocument document = editor.openDocument();
                // add only one Listener (listeners for doc are hold in a List!)
                if ((styledDocRef != null && styledDocRef.get () != document) || styledDocRef == null) {
                    document.addDocumentListener(this);
                    styledDocRef = new WeakReference(document);
                }
                InputSource in = createInputSource(fo, editor, document);
                doc = documentBuilder.parse(in);
            } else if (fo != null) {
                InputStream is = fo.getInputStream();
                try {
                    InputSource in = new InputSource(is);
                    try {
                        in.setSystemId(fo.getURL().toExternalForm());
                    } catch (FileStateInvalidException e) {
                        assert false : e;
                    }
                    doc = documentBuilder.parse(is);
                } finally {
                    is.close();
                }
            } else {
                exception = new FileNotFoundException("Ant script probably deleted"); // NOI18N
                return;
            }
            projDoc = doc;
            exception = null;
        } catch (Exception e) {
            // leave projDoc the way it is...
            exception = e;
            if (!(exception instanceof SAXParseException)) {
                AntModule.err.annotate(exception, ErrorManager.UNKNOWN, "Strange parse error in " + this, null, null, null); // NOI18N
                AntModule.err.notify(ErrorManager.INFORMATIONAL, exception);
            }
        }
        fireChangeEvent(false);
        parsed = true;
    }
    
    public Element getProjectElement () {
        Document doc = getDocument ();
        if (doc != null) {
            return doc.getDocumentElement ();
        } else {
            return null;
        }
    }
    
    public boolean equals (Object o) {
        if (! (o instanceof AntProjectSupport)) return false;
        AntProjectSupport other = (AntProjectSupport) o;
        if (fo != null) {
            return fo.equals (other.fo);
        } else {
            return false;
        }
    }
    
    public int hashCode () {
        return 27825 ^ (fo != null ? fo.hashCode() : 0);
    }
    
    public String toString () {
        FileObject fo = getFileObject ();
        if (fo != null) {
            return fo.toString();
        } else {
            return "<missing Ant script>"; // NOI18N
        }
    }
    
    public void addChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }
    
    public void removeChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }
    
    private final RequestProcessor rp;
    private RequestProcessor.Task task = null;
    
    protected void fireChangeEvent(boolean delay) {
        AntModule.err.log ("AntProjectSupport.fireChangeEvent: fo=" + fo);
        Iterator it;
        synchronized (listeners) {
            it = new HashSet (listeners).iterator ();
        }
        ChangeEvent ev = new ChangeEvent (this);
        ChangeFirer f = new ChangeFirer(it, ev);
        synchronized (this) {
            if (task == null) {
                task = rp.post(f, delay ? REPARSE_DELAY : 0);
            } else if (!delay) {
                task.schedule(0);
            }
        }
    }
    private final class ChangeFirer implements Runnable {
        private final Iterator it; // Iterator<ChangeListener>
        private final ChangeEvent ev;
        public ChangeFirer (Iterator it, ChangeEvent ev) {
            this.it = it;
            this.ev = ev;
        }
        public void run () {
            AntModule.err.log ("AntProjectSupport.ChangeFirer.run");
            synchronized (AntProjectSupport.this) {
                if (task == null) {
                    return;
                }
                task = null;
            }
            while (it.hasNext ()) {
                ChangeListener l = (ChangeListener) it.next ();
                try {
                    l.stateChanged (ev);
                } catch (RuntimeException re) {
                    AntModule.err.notify (re);
                }
            }
        }
    }
    
    public void removeUpdate (javax.swing.event.DocumentEvent ev) {
        invalidate();
    }
    
    public void changedUpdate (javax.swing.event.DocumentEvent ev) {
        // Not to worry, just text attributes or something...
    }
    
    public void insertUpdate (javax.swing.event.DocumentEvent ev) {
        invalidate();
    }
    
    // Called when editor support changes state: #11616
    public void propertyChange(PropertyChangeEvent e) {
        if (EditorCookie.Observable.PROP_DOCUMENT.equals(e.getPropertyName())) {
            invalidate();
        }
    }
    
    public void fileDeleted(FileEvent p1) {
        // Hmm, not our problem.
    }
    
    public void fileDataCreated(FileEvent p1) {
        // ignore
    }
    
    public void fileFolderCreated(FileEvent p1) {
        // ignore
    }
    
    public void fileRenamed(FileRenameEvent p1) {
        // ignore
    }
    
    public void fileAttributeChanged(FileAttributeEvent p1) {
        // ignore
    }
    
    public void fileChanged(FileEvent p1) {
        invalidate ();
    }
    
    protected final void invalidate () {
        AntModule.err.log ("AntProjectSupport.invalidate: fo=" + fo);
        parsed = false;
        fireChangeEvent(true);
    }

}
