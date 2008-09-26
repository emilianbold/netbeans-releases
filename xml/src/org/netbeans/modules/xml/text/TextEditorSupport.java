/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.text;

import org.netbeans.modules.xml.util.Util;
import java.io.*;
import java.text.*;
import java.util.Enumeration;
import java.lang.ref.WeakReference;
import java.beans.PropertyChangeEvent;

import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.text.*;

import org.netbeans.modules.xml.api.EncodingUtil;
import org.openide.*;
import org.openide.awt.StatusDisplayer;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.CloneableOpenSupport;
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;

import org.netbeans.modules.xml.*;
import org.netbeans.modules.xml.lib.*;
import org.netbeans.modules.xml.sync.*;
import org.netbeans.modules.xml.cookies.*;

/**
 * Text editor support that handles I/O encoding and sync with tree.
 * There are two timers a long time and a short time. The long time
 * updates tree even in middle of writing text. The short time is restarted
 * at every text change..
 * <p>
 * Listens for: text document change (edit), timers and document status change (loading).
 */
public class TextEditorSupport extends DataEditorSupport implements EditorCookie.Observable,
        OpenCookie, EditCookie, CloseCookie, PrintCookie {
// ToDo:
// + extend CloneableEditorSupport instead of DataEditorSupport which is associated with DataObject
    
    /**
     * Swings document property added by this support.
     */
    public static final String PROP_DOCUMENT_URL = "doc-url";
    
    /**
     * Timer which countdowns the auto-reparsing time.
     */
    private Timer timer;
    
    /**
     * Used as lock object in close and openCloneableTopComponent.
     */
    private static java.awt.Container awtLock;
        
    private Representation rep;  //it is my representation
    
    /**
     * public jsu for backward compatibility purposes.
     */
    protected TextEditorSupport(XMLDataObjectLook xmlDO, Env env, String mime_type) {
        super((DataObject)xmlDO, env);        
        setMIMEType(mime_type);        
        initTimer();        
        initListeners();        
    }
    
    /**
     * public jsu for backward compatibility purposes.
     */
    public TextEditorSupport(XMLDataObjectLook xmlDO, String mime_type) {
        this(xmlDO, new Env(xmlDO), mime_type);
    }
    
    /**
     * Initialize timers and handle their ticks.
     */
    private void initTimer() {
        timer = new Timer(0, new java.awt.event.ActionListener() {
            // we are called from the AWT thread so put itno other one
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("$$ TextEditorSupport::initTimer::actionPerformed: event = " + e);
                
                RequestProcessor.postRequest( new Runnable() {
                    public void run() {
                        syncDocument(false);
                    }
                });
            }
        });
        
        timer.setInitialDelay(getAutoParsingDelay());
        timer.setRepeats(false);
    }
    
    
    /*
     * Add listeners at Document and document memory status (loading).
     */
    private void initListeners() {        
        // create document listener        
        final DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("** TextEditorSupport::DocumentListener::insertUpdate: event = " + e);                
                restartTime();
            }
            
            public void changedUpdate(DocumentEvent e) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("** TextEditorSupport::DocumentListener::changedUpdate: event = " + e);                
                // not interested in attribute changes
            }
            
            public void removeUpdate(DocumentEvent e) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("** TextEditorSupport::DocumentListener::removeUpdate: event = " + e);                
                restartTime();
            }
            
            private void restartTime() {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("** TextEditorSupport::DocumentListener::restartTime: isInSync = " +
                getXMLDataObjectLook().getSyncInterface().isInSync());
                
                if (getXMLDataObjectLook().getSyncInterface().isInSync()) {
                    return;
                }
                restartTimer(false);
            }
        };
        
        // listen for document loading then register to it the docListener as weak
        
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                
                if (isDocumentLoaded()) {
                    
                    Document doc = getDocument();
                    // when the document is not yet loaded, do nothing
                    if (doc == null)
                        return;
                    doc.addDocumentListener(WeakListeners.document(docListener, doc));
                    
                    if (rep == null) {
                        XMLDataObjectLook dobj = (XMLDataObjectLook) getDataObject();
                        Synchronizator sync = dobj.getSyncInterface();
                        
                        //!!! What does this hardcoding mean???
                        //[DEPENDENCY] it introduces really ugly core to it's client dependencies!!!
                        if (dobj instanceof org.netbeans.modules.xml.XMLDataObject) {
                            rep = new XMLTextRepresentation(TextEditorSupport.this, sync);
                        } else if (dobj instanceof DTDDataObject) {
                            rep = new DTDTextRepresentation(TextEditorSupport.this, sync);
                        } else if (dobj instanceof EntityDataObject) {
                            rep = new EntityTextRepresentation(TextEditorSupport.this, sync);
                        }
                        
                        if (rep != null) {
                            sync.addRepresentation(rep);
                        }
                    }
                }
            }
        });
        
    }
    
    
    /**
     * It simply calls super.notifyClosed() for all instances except
     * TextEditorSupport.class == this.getClass().
     */
    protected void notifyClosed() {
        super.notifyClosed();
        
        // #15756 following code handles synchronization on text editor closing only!
        if (this.getClass() != TextEditorSupport.class) return;
        
        XMLDataObjectLook dobj = (XMLDataObjectLook) getDataObject();
        Synchronizator sync = dobj.getSyncInterface();
        Representation oldRep = rep;
        rep = null;
        if ( oldRep != null ) { // because of remove modified document
            sync.removeRepresentation(oldRep);
        }
        
//          if ( isModified() ) { // possible way to remove needless closeDocument followed by open
//              Task reload = reloadDocument();
//              reload.waitFinished();
//          }
    }
    
    /**
     */
    Env getEnv() {
        return (Env) env;
    }
    
    
    /**
     */
    protected XMLDataObjectLook getXMLDataObjectLook() {
        return getEnv().getXMLDataObjectLook();
    }
    
    /*
     * Update presence of SaveCookie on first keystroke.
     */
    protected boolean notifyModified() {
        if (getEnv().isModified()) {
            return true;
        }
        if (!super.notifyModified()) {
            return false;
        }
        
        CookieManagerCookie manager = getEnv().getXMLDataObjectLook().getCookieManager();
        manager.addCookie(getEnv());
        XMLDataObjectLook obj = (XMLDataObjectLook) getDataObject();        
        if (obj.getCookie(SaveCookie.class) == null) {
            obj.getCookieManager().addCookie(new SaveCookie() {
                public void save() throws java.io.IOException {
                    try {
                        saveDocument();
                    } catch(UserCancelException e) {
                        //just ignore
                    }
                }
            });
        }
        
        return true;
    }
    
    /*
     * Update presence of SaveCookie after save.
     */
    protected void notifyUnmodified() {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Notifing unmodified"); // NOI18N
        
        super.notifyUnmodified();
        CookieManagerCookie manager = getEnv().getXMLDataObjectLook().getCookieManager();
        manager.removeCookie(getEnv());
    }
    
//~~~~~~~~~~~~~~~~~~~~~~~~~ I/O ENCODING HANDLING ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    //indicates than document has wrong encoding @see #edit
    private volatile boolean encodingErr = false;
    
    /**
     * Read the file from the stream, detect right encoding.
     */
    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream in, EditorKit kit) throws IOException, BadLocationException {
        // predetect it to get optimalized XmlReader if utf-8
        String enc = EncodingUtil.detectEncoding(in);
        if (enc == null) {
            enc = "UTF8";  //!!! // NOI18N
        }
        Reader reader = null;
        try {
            reader = EncodingUtil.getUnicodeReader(in, enc);
            kit.read(reader, doc, 0);
        } catch (CharConversionException ex) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("\n!!! TextEditorSupport.loadFromStreamToKit: enc = '" + enc + "'", ex);            
            encodingErr = true;
        } catch (UnsupportedEncodingException ex) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("\n!!! TextEditorSupport.loadFromStreamToKit: enc = '" + enc + "'", ex);            
            encodingErr = true;
        } finally {
            if(reader != null)
                reader.close();
        }
        
    }
    
    /** Store the document in proper encoding.
     */
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream out) throws IOException, BadLocationException {
        String enc = EncodingUtil.detectEncoding(doc);
        if (enc == null) {
            enc = "UTF8"; //!!! // NOI18N
        }
        try {            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Saving using encoding");//, new RuntimeException (enc)); // NOI18N
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("!!! TextEditorSupport::saveFromKitToStream: enc = " + enc);            
            //test encoding on dummy stream
            new OutputStreamWriter(new ByteArrayOutputStream(1), enc);            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("!!!                  ::saveFromKitToStream: after first test -> OK");            
            Writer writer = new OutputStreamWriter(out, enc);            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("!!!                  ::saveFromKitToStream: writer = " + writer);            
            kit.write(writer, doc, 0, doc.getLength());
        } catch (UnsupportedEncodingException ex) {
            //!!! just write nothing //?? save say as UTF-8            
            ErrorManager emgr = ErrorManager.getDefault();
            IOException ioex = new IOException("Unsupported encoding " + enc); // NOI18N
            emgr.annotate(ioex, Util.THIS.getString(
                    TextEditorSupport.class, "MSG_unsupported_encoding", enc));
            throw ioex;
        }
    }
    
    /*
     * Save document using encoding declared in XML prolog if possible otherwise
     * at UTF-8 (in such case it updates the prolog).
     */
    public void saveDocument() throws IOException {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("saveDocument()..."); // NOI18N        
        final StyledDocument doc = getDocument();        
        String enc = EncodingUtil.detectEncoding(doc);        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("!!! TextEditorSupport::saveDocument: enc = " + enc);        
        if (enc == null) {
            enc = "UTF8"; //!!! // NOI18N
        }        
        try {
            //test encoding on dummy stream
            new OutputStreamWriter(new ByteArrayOutputStream(1), enc);
            if (!checkCharsetConversion(EncodingUtil.getJava2IANAMapping(enc))){
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Let unsaved."); // NOI18N
                return;
            }
            super.saveDocument();
            //moved from Env.save()
            getDataObject().setModified(false);
            getXMLDataObjectLook().getSyncInterface().representationChanged(Document.class);            
        } catch (UnsupportedEncodingException ex) {
            //ask user what next?
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                    java.text.MessageFormat.format(Util.THIS.getString(
                    TextEditorSupport.class, "TEXT_SAVE_AS_UTF"), new Object[] {enc}));
            Object res = DialogDisplayer.getDefault().notify(descriptor);
            if (res.equals(NotifyDescriptor.YES_OPTION)) {
                updateDocumentWithNewEncoding(doc);
            } else { // NotifyDescriptor != YES_OPTION
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Let unsaved."); // NOI18N
                throw new UserCancelException();
            }
        } // of catch UnsupportedEncodingException
    }
    
    /**
     * update prolog to new valid encoding
     */
    private void updateDocumentWithNewEncoding(final StyledDocument doc) throws IOException {
        try {
            final int MAX_PROLOG = 1000;
            int maxPrologLen = Math.min(MAX_PROLOG, doc.getLength());
            final char prolog[] = doc.getText(0, maxPrologLen).toCharArray();
            int prologLen = 0;  // actual prolog length
            //parse prolog and get prolog end
            if (prolog[0] == '<' && prolog[1] == '?' && prolog[2] == 'x') {
                // look for delimitting ?>
                for (int i = 3; i<maxPrologLen; i++) {
                    if (prolog[i] == '?' && prolog[i+1] == '>') {
                        prologLen = i + 1;
                        break;
                    }
                }
            }
            final int passPrologLen = prologLen;
            Runnable edit = new Runnable() {
                public void run() {
                    try {
                        doc.remove(0, passPrologLen + 1); // +1 it removes exclusive
                        doc.insertString(0, "<?xml version='1.0' encoding='UTF-8' ?> \n<!-- was: " + new String(prolog, 0, passPrologLen + 1) + " -->", null); // NOI18N
                    } catch (BadLocationException e) {
                        if (System.getProperty("netbeans.debug.exceptions") != null) // NOI18N
                            e.printStackTrace();
                    }
                }
            };
            NbDocument.runAtomic(doc, edit);
            super.saveDocument();
            //moved from Env.save()
            getDataObject().setModified(false);
            getXMLDataObjectLook().getSyncInterface().representationChanged(Document.class);
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Saved."); // NOI18N
        } catch (BadLocationException lex) {                    
            ErrorManager.getDefault().notify(lex);
        }        
    }    
    
    private boolean checkCharsetConversion(String encoding) /*throws UnsupportedEncodingException*/{
        boolean value = true;
        try {
            java.nio.charset.CharsetEncoder coder = java.nio.charset.Charset.forName(encoding).newEncoder();
            if (!coder.canEncode(getDocument().getText(0, getDocument().getLength()))){
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(TextEditorSupport.class, "MSG_BadCharConversion", //NOI18N
                        new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                                encoding}),
                                        NotifyDescriptor.YES_NO_OPTION,
                                        NotifyDescriptor.WARNING_MESSAGE);
                                nd.setValue(NotifyDescriptor.NO_OPTION);
                                DialogDisplayer.getDefault().notify(nd);
                                if(nd.getValue() != NotifyDescriptor.YES_OPTION)
                                    value = false;
            }
        } catch (javax.swing.text.BadLocationException e){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        /*catch (java.nio.charset.UnsupportedCharsetException e){
            throw new UnsupportedEncodingException();
        }*/
        return value;
    }
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~ SYNC ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
     * TEXT changed -> update TREE.
     */
    protected void syncDocument(boolean fromFocus) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("@@ TextEditorSupport::syncDocument: fromFocus = " + fromFocus);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("@@                  ::syncDocument: timer.isRunning = " + timer.isRunning());
        
        if (fromFocus && !timer.isRunning())
            return;
        if (timer.isRunning())
            timer.stop();
        
        XMLDataObjectLook sync = getXMLDataObjectLook();
        if (sync != null) { // && isModified()) {
            sync.getSyncInterface().representationChanged(Document.class);
        }
        
    }
    
    int getAutoParsingDelay () {
        return 3000;
    }
    
    /** Restart the timer which starts the parser after the specified delay.
     * @param onlyIfRunning Restarts the timer only if it is already running
     */
    void restartTimer(boolean onlyIfRunning) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("## TextEditorSupport::restartTimer: onlyIfRunning = " + onlyIfRunning);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("##                  ::restartTimer: timer.isRunning = " + timer.isRunning());
        
        if (onlyIfRunning && !timer.isRunning())
            return;
        
        int delay = getAutoParsingDelay();
        if (delay > 0) {
            timer.setInitialDelay(delay);
            timer.restart();
        }
    }
    
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /*
     * An entry point via EditCookie.
     * Delegate to <code>openDocument()</code>.
     */
    public final void edit() {
        
        try {
            openDocument(); //use sync version of call  - prepare encodingErr
            if (encodingErr) {
                String pattern = Util.THIS.getString(
                        TextEditorSupport.class, "TEXT_WRONG_ENCODING");
                String msg = MessageFormat.format(pattern, new Object[] { getDataObject().getPrimaryFile().toString() /*compatibleEntry.getFile().toString()*/});
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                
            } else {
                Mutex.EVENT.writeAccess(new Runnable() {
                    public void run() {
                        CloneableTopComponent editor = openCloneableEditor();
                        editor.requestActive();
                    }
                });
            }
        } catch (UserQuestionException e){  //this is a hack due to the issue #50701
            open();
            if(isDocumentLoaded()) {
                if (encodingErr) {
                    String pattern = Util.THIS.getString(
                            TextEditorSupport.class, "TEXT_WRONG_ENCODING");
                    String msg = MessageFormat.format(pattern, new Object[] { getDataObject().getPrimaryFile().toString() /*compatibleEntry.getFile().toString()*/});
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                    
                } else {
                    Mutex.EVENT.writeAccess(new Runnable() {
                        public void run() {
                            CloneableTopComponent editor = openCloneableEditor();
                            editor.requestActive();
                        }
                    });
                }
            }
        } catch (IOException ex) {
            String pattern = Util.THIS.getString(
                    TextEditorSupport.class, "TEXT_LOADING_ERROR");
            String msg = MessageFormat.format(pattern, new Object[] { getDataObject().getPrimaryFile().toString() /*compatibleEntry.getFile().toString()*/});
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
        }
        
    }
    
    
    /*
     * Simply open for an cloneable editor. It at first tries to locate
     * existing component in <code>allEditors</code> then if it fails create new one
     * and registers it with <code>allEditors>/code>.
     */
    protected final CloneableEditor openCloneableEditor() {
        
        CloneableEditor ret = null;
        
        synchronized (getLock()) {
            
            String msg = messageOpening();
            if (msg != null) {
                StatusDisplayer.getDefault().setStatusText(msg);
            }
            
            Enumeration en = allEditors.getComponents();
            while ( en.hasMoreElements() ) {
                CloneableTopComponent editor = (CloneableTopComponent)en.nextElement();
                if ( editor instanceof CloneableEditor ) {
                    editor.open();
                    ret = (CloneableEditor) editor;
                }
            }
            
            // no opened editor, create a new one
            
            if (ret == null) {
                CloneableEditor editor = (CloneableEditor)createCloneableTopComponent(); // this is important -- see final createCloneableTopComponent
                editor.setReference(allEditors);
                editor.open();
                ret = editor;
            }
            
            msg = messageOpened();
            if (msg == null) {
                msg = ""; // NOI18N
            }
            StatusDisplayer.getDefault().setStatusText(msg);
            
            return ret;
        }
    }
    
    /**
     * Creates lock object used in close and openCloneableTopComponent.
     * @return never null
     */
    protected Object getLock() {
        if (awtLock == null) {
            awtLock = new java.awt.Container();
        }
        return awtLock.getTreeLock();
    }
    
    /*
     * @return component visualizing this support.
     */
    protected CloneableEditor createCloneableEditor() {
        return new TextEditorComponent(this);
    }
    
    // This must call super createCloneableTopComponent because it prepare document, create cloneable editor and initialize it. See super.
    protected final CloneableTopComponent createCloneableTopComponent() {
        return super.createCloneableTopComponent(); // creates CloneableEditor (calling createCloneableEditor)
    }
    
    /**
     */
    public static final TextEditorSupportFactory findEditorSupportFactory(XMLDataObjectLook xmlDO, String mime) {
        return new TextEditorSupportFactory(xmlDO, mime);
    }
    
    //
    // class Env
    //
    
    /**
     *
     */
    protected static class Env extends DataEditorSupport.Env implements SaveCookie {
        
        /** Serial Version UID */
        private static final long serialVersionUID=-5285524519399090028L;
        
        /** */
        public Env(XMLDataObjectLook obj) {
            super((DataObject)obj);
        }
        
        /**
         */
        protected XMLDataObjectLook getXMLDataObjectLook() {
            return (XMLDataObjectLook) getDataObject();
        }
        
        /**
         */
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        
        /**
         */
        protected FileLock takeLock() throws IOException {
            return ((MultiDataObject)getDataObject()).getPrimaryEntry().takeLock();
        }
        
        
        /**
         */
        public synchronized void save() throws IOException {
            findTextEditorSupport().saveDocument();
        }
        
        /**
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return findTextEditorSupport();
        }
        
        /**
         */
        public TextEditorSupport findTextEditorSupport() {
            EditCookie cookie = getDataObject().getCookie(EditCookie.class);
            if(cookie instanceof TextEditorSupport)
                return (TextEditorSupport)cookie;
            
            return null;
        }
        
        // copy pasted, do not get it
        public void propertyChange(PropertyChangeEvent ev) {
            if (DataObject.PROP_PRIMARY_FILE.equals(ev.getPropertyName())) {
                changeFile();
            }
            super.propertyChange(ev);
        }
        
        
    } // end: class Env
    
    
    //
    // class TextEditorSupportFactory
    //
    
    /**
     *
     */
    public static class TextEditorSupportFactory implements CookieSet.Factory {
        /** */
        private WeakReference editorRef;
        /** */
        private final XMLDataObjectLook dataObject; // used while creating the editor
        /** */
        private final String mime;                  // used while creating the editor
        
        //
        // init
        //
        
        /** Create new TextEditorSupportFactory. */
        public TextEditorSupportFactory(XMLDataObjectLook dobj, String mime) {
            this.dataObject = dobj;
            this.mime       = mime;
        }
        
        
        /**
         */
        protected Class[] supportedCookies() {
            return new Class[] { EditorCookie.class,
                    EditorCookie.Observable.class,
                    OpenCookie.class,
                    EditCookie.class,
                    CloseCookie.class,
                    PrintCookie.class,
            };
        }
        
        /**
         */
        public final void registerCookies(CookieSet cookieSet) {
            Class[] supportedCookies = supportedCookies();
            for (int i = 0; i < supportedCookies.length; i++) {
                cookieSet.add(supportedCookies[i], this);
            }
        }
        
        /** Creates a Node.Cookie of given class. The method
         * may be called more than once.
         */
        public final Node.Cookie createCookie(Class klass) {
            Class[] supportedCookies = supportedCookies();
            for (int i = 0; i < supportedCookies.length; i++) {
                if ( supportedCookies[i].isAssignableFrom(klass) ) {
                    return createEditor();
                }
            }
            return null;
        }
        
        /**
         */
        public final synchronized TextEditorSupport createEditor() { // atomic test and set
            TextEditorSupport editorSupport = null;
            
            if ( editorRef != null ) {
                editorSupport = (TextEditorSupport) editorRef.get();
            }
            if ( editorSupport == null ) {
                editorSupport = prepareEditor();
                editorRef = new WeakReference(editorSupport);
            }
            
            return editorSupport;
        }
        
        /**
         */
        protected TextEditorSupport prepareEditor() {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Initializing TextEditorSupport ..."); // NOI18N
            
            return new TextEditorSupport(getDataObject(), getMIMEType());
        }
        
        /**
         */
        protected final XMLDataObjectLook getDataObject() {
            return dataObject;
        }
        
        /**
         */
        protected final String getMIMEType() {
            return mime;
        }
        
    } // end of class TextEditorSupportFactory
    
}
