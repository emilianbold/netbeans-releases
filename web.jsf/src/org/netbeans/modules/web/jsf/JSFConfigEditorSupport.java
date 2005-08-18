/*
 * StrutsConfigEditorSupport.java
 *
 * Created on May 9, 2005, 1:57 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.web.jsf;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.xml.core.lib.EncodingHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node.Cookie;
import org.openide.text.DataEditorSupport;
import org.openide.cookies.*;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class JSFConfigEditorSupport extends DataEditorSupport
        implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie  {
    
    /** SaveCookie for this support instance. The cookie is adding/removing 
     * data object's cookie set depending on if modification flag was set/unset. */
    private final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws java.io.IOException {
            saveDocument();
        }
    };
    
    /*
     * Save document using encoding declared in XML prolog if possible otherwise
     * at UTF-8 (in such case it updates the prolog).
     */
    public void saveDocument () throws java.io.IOException {
        final javax.swing.text.StyledDocument doc = getDocument();
        // dependency on xml/core
        String enc = EncodingHelper.detectEncoding(doc);
        if (enc == null) enc = "UTF8"; //!!! // NOI18N
        
        try {
            //test encoding on dummy stream
            new java.io.OutputStreamWriter(new java.io.ByteArrayOutputStream(1), enc);
            super.saveDocument();
            //moved from Env.save()
            getDataObject().setModified (false);
        } catch (java.io.UnsupportedEncodingException ex) {
            // ask user what next?
            String message = NbBundle.getMessage(JSFConfigEditorSupport.class,"TEXT_SAVE_AS_UTF",enc);
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message);
            Object res = DialogDisplayer.getDefault().notify(descriptor);

            if (res.equals(NotifyDescriptor.YES_OPTION)) {

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
                    getDataObject().setModified (false);

                } catch (BadLocationException lex) {
                    org.openide.ErrorManager.getDefault().notify(lex);
                }

            } else { // NotifyDescriptor != YES_OPTION
                return;
            }
        }
    }
    
    public JSFConfigEditorSupport(JSFConfigDataObject dobj) {
        super(dobj,new XmlEnv(dobj));
        // TODO - there should not be this mimetype, but some differetnt. We need distinguish the completion providers.
        setMIMEType("text/x-jsf+xml");
        
    }
    
    /** 
     * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified
     */
    protected boolean notifyModified () {
        if (!super.notifyModified()) 
            return false;

        addSaveCookie();

        return true;
    }

    /** Overrides superclass method. Adds removing of save cookie. */
    protected void notifyUnmodified () {
        super.notifyUnmodified();

        removeSaveCookie();
    }

    /** Helper method. Adds save cookie to the data object. */
    private void addSaveCookie() {
        JSFConfigDataObject obj = (JSFConfigDataObject)getDataObject();

        // Adds save cookie to the data object.
        if(obj.getCookie(SaveCookie.class) == null) {
            obj.getCookieSet0().add(saveCookie);
            obj.setModified(true);
        }
    }

    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        JSFConfigDataObject obj = (JSFConfigDataObject)getDataObject();
        
        // Remove save cookie from the data object.
        Cookie cookie = obj.getCookie(SaveCookie.class);

        if(cookie != null && cookie.equals(saveCookie)) {
            obj.getCookieSet0().remove(saveCookie);
            obj.setModified(false);
        }
    }
    
    /** A description of the binding between the editor support and the object.
     * Note this may be serialized as part of the window system and so
     * should be static, and use the transient modifier where needed.
     */
    private static class XmlEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = -800036748848958489L;
        
        //private static final long serialVersionUID = ...L;

        /** Create a new environment based on the data object.
         * @param obj the data object to edit
         */
        public XmlEnv (JSFConfigDataObject obj) {
            super (obj);
        }

        /** Get the file to edit.
         * @return the primary file normally
         */
        protected FileObject getFile () {
            return getDataObject ().getPrimaryFile ();
        }

        /** Lock the file to edit.
         * Should be taken from the file entry if possible, helpful during
         * e.g. deletion of the file.
         * @return a lock on the primary file normally
         * @throws IOException if the lock could not be taken
         */
        protected FileLock takeLock () throws java.io.IOException {
            return ((JSFConfigDataObject) getDataObject ()).getPrimaryEntry ().takeLock ();
        }

        /** Find the editor support this environment represents.
         * Note that we have to look it up, as keeping a direct
         * reference would not permit this environment to be serialized.
         * @return the editor support
         */
        public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport () {
            return (JSFConfigEditorSupport) getDataObject ().getCookie (JSFConfigEditorSupport.class);
        }
    }
}
