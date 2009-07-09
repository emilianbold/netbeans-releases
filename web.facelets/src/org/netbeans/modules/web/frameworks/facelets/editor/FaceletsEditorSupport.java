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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.frameworks.facelets.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.web.frameworks.facelets.loaders.FaceletDataObject;
import org.netbeans.modules.web.frameworks.facelets.palette.FaceletsPaletteFactory;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.spi.palette.PaletteController;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableOpenSupport;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsEditorSupport extends DataEditorSupport
        implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie{
    
    //private static final Logger LOGGER = Logger.getLogger(FaceletsEditorSupport.class.getName());
    
    /** Delay for automatic parsing - in milliseconds */
    private static final int AUTO_PARSING_DELAY = 2000;
    private RequestProcessor.Task parsingDocumentTask;
    private FaceletDataObject dataObject;
    
    private CookieSet set;
    
    /** Creates a new instance of FaceletsEditorSupport */
    public FaceletsEditorSupport(FaceletDataObject fdo, CookieSet set) {
        super(fdo, new Environment(fdo));
        this.set = set;
        this.dataObject = fdo;
        initialize();
    }
    
    private void initialize() {
        // Create DocumentListener
        final DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent event) { change(event); }
            public void changedUpdate(DocumentEvent event) { }
            public void removeUpdate(DocumentEvent event) { change(event); }
            
            private void change(DocumentEvent event) {
                restartTimer();
            }
        };
        // the listener add only when the document is move to memory
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())
                && isDocumentLoaded() && getDocument() != null) {
                    getDocument().addDocumentListener(docListener);
                }
            }
        });
        restartTimer();
    }
    
    /** SaveCookie for this support instance. The cookie is adding/removing
     * data object's cookie set depending on if modification flag was set/unset. */
    private final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws IOException {
            FaceletsEditorSupport.this.saveDocument();
        }
    };
    
    /**
     * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified
     */
    protected boolean notifyModified() {
        if (!super.notifyModified())
            return false;
        
        addSaveCookie();
        
        return true;
    }
    
    /** Overrides superclass method. Adds removing of save cookie. */
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        
        removeSaveCookie();
    }
    
    /** Helper method. Adds save cookie to the data object. */
    private void addSaveCookie() {
        // Adds save cookie to the data object.
        if(dataObject.getCookie(SaveCookie.class) == null) {
            set.add(saveCookie);
            dataObject.setModified(true);
        }
    }
    
    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        DataObject obj = getDataObject();
        
        // Remove save cookie from the data object.
        Cookie cookie = obj.getCookie(SaveCookie.class);
        
        if(cookie != null && cookie.equals(saveCookie)) {
            set.remove(saveCookie);
            obj.setModified(false);
        }
    }
    
    // -------------- Method for parsing --------------------
    /** Restart the timer which starts the parser after the specified delay.
     */
    public void restartTimer() {
        if (parsingDocumentTask==null || parsingDocumentTask.isFinished() ||
                parsingDocumentTask.cancel()) {
            dataObject.setDocumentDirty(true);
            Runnable runable = new Runnable() {
                public void run() {
                    synchronized(this) {
                        dataObject.parsingDocument();
                    }
                }
            };
            if (parsingDocumentTask != null)
                parsingDocumentTask = RequestProcessor.getDefault().post(runable, AUTO_PARSING_DELAY);
            else
                parsingDocumentTask = RequestProcessor.getDefault().post(runable, 100);
        }
    }
    
    private boolean isSupportedEncoding(String encoding){
        boolean supported;
        try{
            supported = java.nio.charset.Charset.isSupported(encoding);
        } catch (java.nio.charset.IllegalCharsetNameException e){
            supported = false;
        }
        
        return supported;
    }
    
    
    @Override
    public void open() {
        super.open();
        // parse once after opening the document
        restartTimer();
    }
    
    /*
     * Save document using encoding declared in XML prolog if possible otherwise
     * at UTF-8 (in such case it updates the prolog).
     */
    public void saveDocument() throws java.io.IOException {
        final javax.swing.text.StyledDocument doc = getDocument();
        String defaultEncoding = "UTF-8"; // NOI18N
        // dependency on xml/core
        String enc = EncodingUtil.detectEncoding(doc);
        boolean changeEncodingToDefault = false;
        if (enc == null) enc = defaultEncoding;
        
        //test encoding
        if (!isSupportedEncoding(enc)){
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(FaceletsEditorSupport.class, "MSG_BadEncodingDuringSave", //NOI18N
                    new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                    enc,
                    defaultEncoding} ),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE);
            nd.setValue(NotifyDescriptor.NO_OPTION);
            DialogDisplayer.getDefault().notify(nd);
            if(nd.getValue() != NotifyDescriptor.YES_OPTION) return;
            changeEncodingToDefault = true;
        }
        
        if (!changeEncodingToDefault){
            // is it possible to save the document in the encoding?
            try {
                java.nio.charset.CharsetEncoder coder = java.nio.charset.Charset.forName(enc).newEncoder();
                if (!coder.canEncode(doc.getText(0, doc.getLength()))){
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(FaceletsEditorSupport.class, "MSG_BadCharConversion", //NOI18N
                            new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                            enc}),
                            NotifyDescriptor.YES_NO_OPTION,
                            NotifyDescriptor.WARNING_MESSAGE);
                    nd.setValue(NotifyDescriptor.NO_OPTION);
                    DialogDisplayer.getDefault().notify(nd);
                    if(nd.getValue() != NotifyDescriptor.YES_OPTION) return;
                }
            } catch (javax.swing.text.BadLocationException e){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            super.saveDocument();
            //moved from Env.save()
            getDataObject().setModified(false);
        } else {
            // update prolog to new valid encoding
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
            } catch (javax.swing.text.BadLocationException e){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    protected CloneableEditor createCloneableEditor() {
        return new FaceletsEditor(this);
    }
    
    private static class Environment extends DataEditorSupport.Env{
        
        private static final long serialVersionUID = 1L;

        Environment(FaceletDataObject fdo){
            super(fdo);
        }
        
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        
        protected FileLock takeLock() throws IOException {
            return ((MultiDataObject)getDataObject()).getPrimaryEntry().takeLock();
        }
        
        /**
         * Overrides superclass method.
         * @return text editor support (instance of enclosing class)
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (FaceletsEditorSupport)getDataObject().getCookie(FaceletsEditorSupport.class);
        }
    }
    
    public static class FaceletsEditor extends CloneableEditor {
        
        public FaceletsEditor() {
        }
        
        void associatePalette(FaceletsEditorSupport s) {
            
            DataObject dataObject = s.getDataObject();
            if (dataObject instanceof FaceletDataObject) {
                try {
                    PaletteController pc = FaceletsPaletteFactory.getPalette();
                    Lookup pcl = Lookups.singleton(pc);
                    Lookup anl = getActivatedNodes()[0].getLookup();
                    Lookup actionMap = Lookups.singleton(getActionMap());
                    ProxyLookup l = new ProxyLookup(new Lookup[] { anl, actionMap, pcl });
                    associateLookup(l);
                } catch (IOException ioe) {
                    //TODO exception handling
                    ioe.printStackTrace();
                }
            }
        }
        
        /** Creates new editor */
        public FaceletsEditor(FaceletsEditorSupport s) {
            super(s);
            initialize();
        }
        
        private void initialize() {
            Node nodes[] = {((DataEditorSupport)cloneableEditorSupport()).getDataObject().getNodeDelegate()};
            setActivatedNodes(nodes);
            associatePalette((FaceletsEditorSupport)cloneableEditorSupport());
        }
        
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialize();
        }
        
    }
}
