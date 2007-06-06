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


package org.netbeans.modules.html;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.palette.HTMLPaletteFactory;
import org.netbeans.spi.palette.PaletteController;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableOpenSupport;



/**
 * Editor support for HTML data objects.
 *
 * @author Radim Kubacki
 * @see org.openide.text.DataEditorSupportH
 */
public final class HtmlEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie {
    
    //constants used when finding html document content type
    private static final String CHARSET_DECL = "CHARSET="; //NOI18N
    private static final String HEAD_END_TAG_NAME = "</HEAD>"; //NOI18N
    
    
    /** SaveCookie for this support instance. The cookie is adding/removing
     * data object's cookie set depending on if modification flag was set/unset.
     * It also invokes beforeSave() method on the HtmlDataObject to give it
     * a chance to eg. reflect changes in 'charset' attribute
     * */
    
    private final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws IOException {
            String encoding = ((HtmlDataObject)HtmlEditorSupport.this.getDataObject()).getFileEncoding();
            
            if (canUseEncoding(encoding)) {
                
                HtmlEditorSupport.this.saveDocument();
                HtmlEditorSupport.this.getDataObject().setModified(false);
            }
        }
    };
    
    
    /** Constructor. */
    HtmlEditorSupport(HtmlDataObject obj) {
        super(obj, new Environment(obj));
        
        setMIMEType("text/html"); // NOI18N
    }
    
    /**
     * Adds notification to the data object to allow it to retrieve the correct encoding information
     */
    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        HtmlDataObject hdo = (HtmlDataObject)getDataObject();
        hdo.useEncodingFromFile();
        super.loadFromStreamToKit(doc, stream, kit);
        hdo.useEncodingFromEditor();
    }
    
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
        HtmlDataObject obj = (HtmlDataObject)getDataObject();
        
        // Adds save cookie to the data object.
        if(obj.getCookie(SaveCookie.class) == null) {
            obj.getCookieSet0().add(saveCookie);
            obj.setModified(true);
        }
    }
    
    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        HtmlDataObject obj = (HtmlDataObject)getDataObject();
        
        // Remove save cookie from the data object.
        Cookie cookie = obj.getCookie(SaveCookie.class);
        
        if(cookie != null && cookie.equals(saveCookie)) {
            obj.getCookieSet0().remove(saveCookie);
            obj.setModified(false);
        }
    }
    
    private String getDocumentText() {
        String text = "";
        try {
            StyledDocument doc = getDocument();
            if (doc != null) {
                text = doc.getText(doc.getStartPosition().getOffset(), doc.getLength());
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
        return text;
    }
    
    String getHtmlEncoding() {
        String docText = getDocumentText();
        return HtmlEditorSupport.findEncoding(docText);
    }
    
    void setEncodingProperty(String oldEncoding, String encoding) {
        boolean storeEncoding = true;
        // epmty property value means "use the encoding from the owning project"
        if (encoding == null || encoding.length()==0) {
            String defaultEnc = ((HtmlDataObject) getDataObject()).getDefaultFileEncoding();
            storeEncoding = canUseEncoding(defaultEnc);
            encoding = null;
        } else {
            storeEncoding = canUseEncoding(encoding);
        }
        if (storeEncoding) { // encoding conversion is either safe or the user decided to go for unsafe conversion
            // only if the encoding has been actually changed
            if (((encoding == null && oldEncoding != null) ||  
                    (encoding != null && oldEncoding == null)) || 
                    !encoding.equals(oldEncoding)) {
                ((HtmlDataObject) getDataObject()).setFileEncoding(encoding);
                try {
                    // do litle magic here to force the new encoding to take effect
                    notifyModified();
                    saveDocument();
                    reloadDocument();
                } catch (IOException e) {
                    // in case of exception revert the encoding
                    notifyUnmodified();
                    ((HtmlDataObject) getDataObject()).setFileEncoding(oldEncoding);
                }
            }
        }
    }
    
    boolean canUseEncoding(String encoding) {
        return canUseEncoding(getDocumentText(), encoding);
    }
    
    private boolean canUseEncoding(String docText, String encoding) {
        if (!isSupportedEncoding(encoding)) return false;
        java.nio.charset.CharsetEncoder coder = java.nio.charset.Charset.forName(encoding).newEncoder();
        if (!coder.canEncode(docText)) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(HtmlEditorSupport.class, "MSG_BadCharConversion", //NOI18N
                    new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                    encoding}),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE);
            nd.setValue(NotifyDescriptor.NO_OPTION);
            DialogDisplayer.getDefault().notify(nd);
            if(nd.getValue() != NotifyDescriptor.YES_OPTION) return false;
        }
        return true;
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
    
    /** Tries to guess the mime type from given input stream. Tries to find
     *   <em>&lt;meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"&gt;</em>
     * @param txt the string to search in (should be in upper case)
     * @return the encoding or null if no has been found
     */
    static String findEncoding(String txt) {
        int[] offsets = findEncodingOffsets(txt);
        if (offsets.length == 3) {
            String encoding = txt.substring(offsets[0] + offsets[1], offsets[0] + offsets[2]);
            return encoding;
        }
        return null;
    }
    
    private static int[] findEncodingOffsets(String txt) {
        int[] rslt = new int[0];
        int headEndOffset = txt.indexOf(HEAD_END_TAG_NAME); // NOI18N
        headEndOffset = headEndOffset == -1 ? txt.indexOf(HEAD_END_TAG_NAME.toLowerCase()) : headEndOffset;
        
        if (headEndOffset == -1){
            return rslt;
        }
        
        TokenHierarchy hi = TokenHierarchy.create(txt, HTMLTokenId.language());
        TokenSequence ts = hi.tokenSequence();
        ts.moveStart();
        while(ts.moveNext()) {
            Token token = ts.token();
            
            //test we do not overlap </head>
            if(token.offset(hi) >= headEndOffset) {
                break;
            }
            
            if(token.id() == HTMLTokenId.VALUE) {
                String tokenImage = token.text().toString();
                int charsetOffset = tokenImage.indexOf(CHARSET_DECL);
                charsetOffset = charsetOffset == -1 ? tokenImage.indexOf(CHARSET_DECL.toLowerCase()) : charsetOffset;
                
                int charsetEndOffset = charsetOffset + CHARSET_DECL.length();
                if (charsetOffset != -1){
                    int endOffset = tokenImage.indexOf('"', charsetEndOffset);
                    
                    if (endOffset == -1){
                        endOffset = tokenImage.indexOf('\'', charsetEndOffset);
                    }
                    
                    if (endOffset == -1){
                        endOffset = tokenImage.indexOf(';', charsetEndOffset);
                    }
                    
                    if (endOffset == -1){
                        return rslt;
                    }
                    
                    rslt =  new int[]{token.offset(hi), charsetEndOffset, endOffset};
                }
            }
        }
        return rslt;
    }
    
    /** Nested class. Environment for this support. Extends <code>DataEditorSupport.Env</code> abstract class. */
    private static class Environment extends DataEditorSupport.Env {
        
        private static final long serialVersionUID = 3035543168452715818L;
        
        /** Constructor. */
        public Environment(HtmlDataObject obj) {
            super(obj);
        }
        
        
        /** Implements abstract superclass method. */
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        
        /** Implements abstract superclass method.*/
        protected FileLock takeLock() throws IOException {
            return ((HtmlDataObject)getDataObject()).getPrimaryEntry().takeLock();
        }
        
        /**
         * Overrides superclass method.
         * @return text editor support (instance of enclosing class)
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (HtmlEditorSupport)getDataObject().getCookie(HtmlEditorSupport.class);
        }
    } // End of nested Environment class.
    
    
    /** A method to create a new component. Overridden in subclasses.
     * @return the {@link HtmlEditor} for this support
     */
    protected CloneableEditor createCloneableEditor() {
        return new HtmlEditor(this);
    }
    
    public static class HtmlEditor extends CloneableEditor {
        
        public HtmlEditor() {
        }
        
        void associatePalette(HtmlEditorSupport s) {
            
            Node nodes[] = { s.getDataObject().getNodeDelegate() };
            InstanceContent instanceContent = new InstanceContent();
            associateLookup(new ProxyLookup(new Lookup[] { new AbstractLookup(instanceContent), nodes[0].getLookup()}));
            instanceContent.add(getActionMap());
            
            setActivatedNodes(nodes);
            
            DataObject dataObject = s.getDataObject();
            if (dataObject instanceof HtmlDataObject) {
                try {
                    PaletteController pc = HTMLPaletteFactory.getPalette();
                    instanceContent.add(pc);
                } catch (IOException ioe) {
                    //TODO exception handling
                    ioe.printStackTrace();
                }
            }
        }
        
        /** Creates new editor */
        public HtmlEditor(HtmlEditorSupport s) {
            super(s);
            initialize();
        }
        
        private void initialize() {
            associatePalette((HtmlEditorSupport)cloneableEditorSupport());
        }
        
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialize();
        }
        
    }
    
}
