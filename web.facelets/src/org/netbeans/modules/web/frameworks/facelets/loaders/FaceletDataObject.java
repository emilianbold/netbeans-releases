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

package org.netbeans.modules.web.frameworks.facelets.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.modules.web.frameworks.facelets.editor.FaceletsAnnotationManager;
import org.netbeans.modules.web.frameworks.facelets.editor.FaceletsEditorErrors;
import org.netbeans.modules.web.frameworks.facelets.editor.FaceletsEditorSupport;
import org.netbeans.modules.web.frameworks.facelets.taglib.FaceletsCatalog;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Petr Pisl
 */

public class FaceletDataObject extends MultiDataObject {
    
    transient private FaceletsEditorSupport editorSupport = null;
    transient protected FaceletsEditorErrors.Error error = null;
    transient private FaceletsAnnotationManager annotationManager = null;
    
    /** Property name for property documentValid */
    public static final String PROP_DOC_VALID = "documentValid"; // NOI18N
    
    public FaceletDataObject(FileObject pf, FaceletDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        // System.out.println("Creating FaceletsDataObject");
        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        cookies.add(checkCookie);
        annotationManager = new FaceletsAnnotationManager(this);
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new FaceletDataNode(this);
    }
    
    @Override
    public Node.Cookie getCookie(Class type) {
        Node.Cookie value = null;
        if (type.isAssignableFrom(FaceletsEditorSupport.class))
            value =  getEditorSupport();
        else
            value = super.getCookie(type);
        return value;
    }
   
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    protected synchronized FaceletsEditorSupport getEditorSupport() {
        if (editorSupport == null) {
            editorSupport = new FaceletsEditorSupport(this, getCookieSet());
        }
        return editorSupport;
    }
    
    // ------------- things connected with the parsing ----------------
    private boolean documentDirty = true;
    /** setter for property documentDirty. Method updateDocument() usually setsDocumentDirty to false
     */
    public void setDocumentDirty(boolean dirty){
        documentDirty=dirty;
    }
    
    /** This method is used for obtaining the current source of xml document.
     * First try if document is in the memory. If not, provide the input from
     * underlayed file object.
     * @return The input source from memory or from file
     * @exception IOException if some problem occurs
     */
    protected InputStream prepareInputSource() throws java.io.IOException {
        if ((getEditorSupport() != null) && (getEditorSupport().isDocumentLoaded())) {
            // loading from the memory (Document)
            return getEditorSupport().getInputStream();
        } else {
            return getPrimaryFile().getInputStream();
        }
    }
    
    private org.w3c.dom.Document parseDocument(InputStream inputSource) throws SAXParseException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document doc =  null;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(new FaceletsCatalog());
            doc = docBuilder.parse(inputSource);
        } catch (ParserConfigurationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (SAXParseException ex) {
            error = new FaceletsEditorErrors.ParseError(ex.getLineNumber(), ex.getColumnNumber(), ex.getLocalizedMessage());
        } catch (SAXException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (UnsupportedEncodingException ex) {
            error = new FaceletsEditorErrors.EncodingError(ex.getMessage());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        try {
            //Annotation has to be invoke in awt thread
            SwingUtilities.invokeAndWait(new AnnotationThread(doc));
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
        } catch (InvocationTargetException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
        }
        return doc;
    }
    
    /** This method parses XML document and calls updateNode method which
     * updates corresponding Node.
     */
    public void parsingDocument(){
        error = null;
        InputStream is =  null;
        try {
            is = prepareInputSource();
            updateNode(prepareInputSource());
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            setDocumentValid(false);
            return;
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    // nothing to do, if exception occurs during saving.
                }
            documentDirty=false;
        }
        if (error == null){
            setDocumentValid(true);
        }else {
            setDocumentValid(false);
        }
        
    }
    
    protected SAXException updateNode(InputStream is) throws java.io.IOException{
        try {
            Document doc = parseDocument(is);
        } catch(SAXException ex) {
            return ex;
        }
        return null;
    }
    
    private boolean documentValid=true;
    
    public void setDocumentValid(boolean valid){
        if (documentValid!=valid) {
            if (valid)
                repairNode();
            documentValid=valid;
            firePropertyChange(PROP_DOC_VALID, !documentValid ? Boolean.TRUE : Boolean.FALSE, documentValid ? Boolean.TRUE : Boolean.FALSE);
        }
    }
    
    /** This method repairs Node Delegate (usually after changing document by property editor)
     */
    protected void repairNode(){
        org.openide.awt.StatusDisplayer.getDefault().setStatusText("");  // NOI18N
    }
    
    protected FaceletsAnnotationManager getAnnotationManager(){
        return annotationManager;
    }
    
    private class AnnotationThread implements Runnable {
        private Document doc;
        public AnnotationThread(Document doc) {
            this.doc = doc;
            
        }

        
        public void run() {
            if (doc != null) {
                annotationManager.annotate(new FaceletsEditorErrors.Error[0]);
                error = null; 
                //Parser2.analyseDoc(doc);
            }
            else  {
                annotationManager.annotate(new FaceletsEditorErrors.Error[]{error});
            }
        }
        
    }
}
