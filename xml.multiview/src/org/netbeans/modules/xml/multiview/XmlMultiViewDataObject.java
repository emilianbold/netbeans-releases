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

    private XmlMultiViewEditorSupport editor;
    private boolean documentValid;
    boolean changedFromUI;
    private Reader reader;
    
    /** Creates a new instance of XmlMultiViewDataObject */
    public XmlMultiViewDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().add(XmlMultiViewEditorSupport.class, this);
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
    
    protected abstract org.xml.sax.SAXException updateModelFromDocument() throws java.io.IOException ;
    
    protected boolean isChangedFromUI() {
        return changedFromUI;
    }
     /** This method parses XML document and calls abstract updateModelFromInputSource method which
    * updates corresponding DataModel.
    */    
    protected void updateModelFromSource() {
        org.xml.sax.SAXException err=null;
        try {
            //inputReader = createInputReader();
            //err=updateModelFromDocument(inputReader);
            err=updateModelFromDocument();
            System.out.println("err="+err);
        }
        catch (java.io.IOException e) {
            org.openide.ErrorManager.getDefault ().notify (org.openide.ErrorManager.INFORMATIONAL, e);
        }
        if (err==null){
            setDocumentValid(true);
        }else {
            setDocumentValid(false);
        }
    }
    
    protected void setDocumentValid(boolean valid) {
        documentValid=valid;
    }
    
    public boolean isDocumentValid() {
        return documentValid;
    }

    /** This method is used for obtaining the current source of xml document.
    * First try if document is in the memory. If not, provide the input from
    * underlayed file object.
    * @return The input source from memory or from file
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
    * First try if document is in the memory. If not, provide the input from
    * underlayed file object.
    * @return The input source from memory or from file
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
            return null;
            // loading from the file
            //return new InputSource(new FileReader(org.openide.filesystems.FileUtil.toFile(getPrimaryFile())));
        }
    }
    
    /** Display Name for MultiView editor
     */
    protected abstract String getDisplayName();
    
    /** Icon Base for MultiView editor
     */
    protected abstract String getIconBase();
    
    public void UIUpdated() {
        changedFromUI=true;
        if(getCookie(SaveCookie.class) == null) {
            System.out.println("1");
            getCookieSet0().add(getEditorSupport().saveCookie);
            setModified(true);
        }
    }
    
    /** Icon Base for MultiView editor
     */
    protected abstract DesignMultiViewDesc[] getMultiViewDesc();
    
}
