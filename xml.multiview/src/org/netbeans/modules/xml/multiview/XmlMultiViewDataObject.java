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

package org.netbeans.modules.xml.multiview;

import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.nodes.CookieSet;
import org.openide.filesystems.FileObject;
import org.netbeans.core.spi.multiview.*;
import java.io.*;
import org.xml.sax.InputSource;
/**
 * XmlMultiviewDataObject.java
 *
 * Created on October 5, 2004, 10:49 AM
 * @author  mkuchtiak
 */
public abstract class XmlMultiViewDataObject extends MultiDataObject implements CookieSet.Factory {

    public static final String PROP_DOCUMENT_VALID = "document_valid"; //NOI18N
    private XmlMultiViewEditorSupport editor;
    private org.xml.sax.SAXException saxError;
    boolean changedFromUI;
    private boolean modelUpdated;
    private Reader reader;
    
    /** Creates a new instance of XmlMultiViewDataObject */
    public XmlMultiViewDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().add(XmlMultiViewEditorSupport.class, this);
        try {
            modelUpdated = createModelFromFileObject(pf);
        } catch (IOException ex) {
            modelUpdated = false;
        }
        //getCookieSet().add(EditCookie.class, this);
        //getCookieSet().add(EditorCookie.class, this);
    }
    
    public org.openide.nodes.Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(XmlMultiViewEditorSupport.class))
            return getEditorSupport();
        else
            return null;
    }
    
    // Package accessibility for XmlMultiViewEditorSupport:
    CookieSet getCookieSet0() {
        return getCookieSet();
    }
    
    /** Gets editor support for this data object. */
    protected synchronized XmlMultiViewEditorSupport getEditorSupport() {
        if(editor == null) {
            editor = new XmlMultiViewEditorSupport(this);
        }
        return editor;
    }
    /** Create the data model from file object. Called from constructor.
    * @return true if model was succesfully created, false otherwise
    */
    protected abstract boolean createModelFromFileObject(FileObject fo) throws java.io.IOException;
    
    /** Update data model from document text . Called when something is changed in xml editor. 
    * @return true if model was succesfully created, false otherwise
    */
    protected abstract boolean updateModelFromDocument() throws java.io.IOException ;
    
    protected void validateSource(){
    }
    /** Update text document from data model. Called when something is changed in visual editor.
    */
    protected abstract String generateDocumentFromModel();
    
    protected boolean isModelUpdated() {
        return modelUpdated;
    }
    /** enables to switch quickly to XML perspective in multi view editor
     */
    public void goToXmlView() {
        getEditorSupport().goToXmlPerspective();
    }
    
    protected boolean isChangedFromUI() {
        return changedFromUI;
    }
     /** This method parses XML document and calls abstract updateModelFromDocument() method which
    * is trying to update corresponding data model.
    */    
    protected void updateModelFromSource() {
        boolean modelUpd=false;
        try {
            modelUpd=updateModelFromDocument();
        }
        catch (java.io.IOException e) {
            modelUpd=false;
            org.openide.ErrorManager.getDefault ().notify (org.openide.ErrorManager.INFORMATIONAL, e);
        }
        modelUpdated=modelUpd;
    }
    
    protected void setSaxError(org.xml.sax.SAXException saxError) {
        if (this.saxError==null) {
            if (saxError!=null) firePropertyChange(PROP_DOCUMENT_VALID, Boolean.TRUE, Boolean.FALSE);
        } else {
            if (saxError==null) firePropertyChange(PROP_DOCUMENT_VALID, Boolean.FALSE, Boolean.TRUE);
        }
        this.saxError=saxError;
    }
    
    public org.xml.sax.SAXException getSaxError() {
        return saxError;
    }

    /** This method is used for obtaining the current source of xml document.
    * First try if document is open in editor. If not, provide the input from
    * underlayed file object.
    * @return The InputStream from swing document or from file
    * @exception IOException if some problem occurs
    */
    protected InputStream createInputStream() throws java.io.IOException {
        if ((editor != null) && (editor.isDocumentLoaded())) {
            // loading from the memory (Document)
            final javax.swing.text.Document doc = editor.getDocument();
            final String[] str = new String[1];
            // safely take the text from the document
            Runnable run = new Runnable() {
                public void run() {
                    try {
                        str[0] = doc.getText(0, doc.getLength());
                    }
                    catch (javax.swing.text.BadLocationException e) {
                        // impossible
                    }
                }
            };
            
            doc.render(run);
            return new StringBufferInputStream(str[0]);
        } 
        else {
            // loading from the file
            return getPrimaryFile().getInputStream();
        }
    }
    
    /** This method is used for obtaining the current source of xml document.
    * First try if document is open in editor. If not, provide the input from
    * underlayed file object.
    * @return The InputSource from swing document or null
    * @exception IOException if some problem occurs
    */
    protected InputSource createInputSource() throws java.io.IOException {
        if ((editor != null) && (editor.isDocumentLoaded())) {
            // loading from the memory (Document)
            final javax.swing.text.Document doc = editor.getDocument();
            final String[] str = new String[1];
            // safely take the text from the document
            Runnable run = new Runnable() {
                public void run() {
                    try {
                        str[0] = doc.getText(0, doc.getLength());
                    }
                    catch (javax.swing.text.BadLocationException e) {
                        // impossible
                    }
                }
            };
            
            doc.render(run);
            return new InputSource(new StringReader(str[0]));
        } 
        else {
            //return null;
            // loading from the file
            return new InputSource(new FileReader(org.openide.filesystems.FileUtil.toFile(getPrimaryFile())));
        }
    }
    
    protected void updateDocument() {
        //System.out.println("restart Gen");           
        final String newDoc = generateDocumentFromModel();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    javax.swing.text.Document doc = getEditorSupport().openDocument();
                    Utils.replaceDocument(doc,newDoc);
                    //setDocumentValid(true);
                    //if (saveAfterNodeChanges){
                    //    SaveCookie savec = (SaveCookie) getCookie(SaveCookie.class);
                    //    if (savec!=null) savec.save();
                    //}
                    // this is necessary for correct undo behaviour
                    //getEditorSupport().getUndo().discardAllEdits();
                }
                catch (javax.swing.text.BadLocationException e) {
                    org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
                }

                catch (IOException e) {
                    org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
                }
                finally {
                    changedFromUI=false;                             
                }
            }
        });
    }
    
    /** Display Name for MultiView editor
     */
    protected abstract String getDisplayName();
    
    /** Icon Base for MultiView editor
     */
    protected abstract String getIconBase();
    
    public void modelUpdatedFromUI() {
        changedFromUI=true;
        if(getCookie(SaveCookie.class) == null) {
            getCookieSet0().add(getEditorSupport().saveCookie);
            getEditorSupport().getMVTC().setDisplayName(getEditorSupport().getMVTC().getDisplayName()+" *"); // NOI18N
        }
        updateDocument();
        //setModified(true);
    }
    
    /** MultiViewDesc for MultiView editor
     */
    protected abstract DesignMultiViewDesc[] getMultiViewDesc();
    
}
