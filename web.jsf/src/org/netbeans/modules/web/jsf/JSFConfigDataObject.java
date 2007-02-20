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

package org.netbeans.modules.web.jsf;

import java.io.IOException;
import java.io.InputStream;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.w3c.dom.Document;
import org.xml.sax.*;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.spi.xml.cookies.*;

/**
 *
 * @author Petr Pisl
 */
public class JSFConfigDataObject extends MultiDataObject
                                    implements org.openide.nodes.CookieSet.Factory  {
    
    private static JSFCatalog jsfCatalog =  new JSFCatalog();
    private boolean documentDirty = true;
    private boolean documentValid=true;
    protected boolean nodeDirty = false;
    private InputStream inputStream;
    /** Editor support for text data object. */
    private transient JSFConfigEditorSupport editorSupport;
    private SAXParseError error;
    private FacesConfig lastGoodFacesConfig = null;
    
    /** Property name for property documentValid */
    public static final String PROP_DOC_VALID = "documentValid"; // NOI18N

    
    /** Creates a new instance of StrutsConfigDataObject */
    public JSFConfigDataObject(FileObject pf, JSFConfigLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        init();
        
    }
 
    private void init() {
        CookieSet cookies = getCookieSet();
        
        getCookieSet().add(JSFConfigEditorSupport.class, this);
        
        // Creates Check XML and Validate XML context actions
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
    }
    
    /**
     * Provides node that should represent this data object. When a node for
     * representation in a parent is requested by a call to getNode (parent)
     * it is the exact copy of this node
     * with only parent changed. This implementation creates instance
     * <CODE>DataNode</CODE>.
     * <P>
     * This method is called only once.
     *
     * @return the node representation for this data object
     * @see DataNode
     */
    protected synchronized Node createNodeDelegate () {
	return new JSFConfigNode(this);
    }
    
    /** Implements <code>CookieSet.Factory</code> interface. */
    public Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(JSFConfigEditorSupport.class))
            return getEditorSupport();
        else
            return null;
    }
    
    /** Gets editor support for this data object. */
    public JSFConfigEditorSupport getEditorSupport() {
        if(editorSupport == null) {
            synchronized(this) {
                if(editorSupport == null)
                    editorSupport = new JSFConfigEditorSupport(this);
            }
        }
        
        return editorSupport;
    }
    
    public FacesConfig getFacesConfig() throws java.io.IOException {
        if (lastGoodFacesConfig == null)
            parsingDocument();
        return lastGoodFacesConfig;
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
        }
        else {
            return getPrimaryFile().getInputStream();
        }
    }
    
    /** This method has to be called everytime after prepareInputSource calling.
     * It is used for closing the stream, because it is not possible to access the
     * underlayed stream hidden in InputSource.
     * It is save to call this method without opening.
     */
    protected void closeInputSource() {
        InputStream is = inputStream;
        if (is != null) {
            try {
                is.close();
            }
            catch (IOException e) {
                // nothing to do, if exception occurs during saving.
            }
            if (is == inputStream) {
                inputStream = null;
            }
        }
    }
    
    /** This method parses XML document and calls updateNode method which
    * updates corresponding Node.
    */
    public void parsingDocument(){
        error = null;
        try {
            error = updateNode(prepareInputSource());
        }
        catch (Exception e) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
            setDocumentValid(false);
            return;
        }
        finally {
            closeInputSource();
            documentDirty=false;
        }
        if (error == null){
            setDocumentValid(true);
        }else {
            setDocumentValid(false);
        }
        setNodeDirty(false);
    }
    
    public void setDocumentValid (boolean valid){
        if (documentValid!=valid) {
            if (valid)
                repairNode();
            documentValid=valid;
            firePropertyChange (PROP_DOC_VALID, !documentValid ? Boolean.TRUE : Boolean.FALSE, documentValid ? Boolean.TRUE : Boolean.FALSE);
        }
    }
    
    /** This method repairs Node Delegate (usually after changing document by property editor)
    */
    protected void repairNode(){
        // PENDING: set the icon in Node
        // ((DataNode)getNodeDelegate()).setIconBase (getIconBaseForValidDocument());
        org.openide.awt.StatusDisplayer.getDefault().setStatusText("");  // NOI18N
    /*    if (inOut!=null) {
            inOut.closeInputOutput();
            errorAnnotation.detach();
        }*/
    }
    
    private org.w3c.dom.Document getDomDocument(InputStream inputSource) throws SAXParseException {
        try {
            // creating w3c document
            org.w3c.dom.Document doc = org.netbeans.modules.schema2beans.GraphManager.
                createXmlDocument(new org.xml.sax.InputSource(inputSource), false, jsfCatalog,
                new J2eeErrorHandler(this));
            return doc;
        } catch(Exception e) {
            //    XXX Change that
            throw new SAXParseException(e.getMessage(), new org.xml.sax.helpers.LocatorImpl());
        }
    }
    

    /** Update the node from document. This method is called after document is changed.
    * @param is Input source for the document
    * @return number of the line with error (document is invalid), 0 (xml document is valid)
    */
    // TODO is prepared for handling arrors, but not time to finish it.
    protected SAXParseError updateNode(InputStream is) throws java.io.IOException{
        try {
            Document doc = getDomDocument(is);
            
            //TODO new api
            //JSF version = JSFCatalog.extractVersion(doc);
            //check version, use impl class to create graph
            //TODO new API
//            if (FacesConfig.VERSION_1_1.equals(version)) {
//                lastGoodFacesConfig = org.netbeans.modules.web.jsf.config.model_1_1.FacesConfig.createGraph(doc);
//            }
//            if (FacesConfig.VERSION_1_0.equals(version)) {
//                lastGoodFacesConfig = org.netbeans.modules.web.jsf.config.model_1_1.FacesConfig.createGraph(doc);
//            }
//            if (FacesConfig.VERSION_1_2.equals(version)) {
//                lastGoodFacesConfig = org.netbeans.modules.web.jsf.config.model_1_2.FacesConfig.createGraph(doc);
//            }
        }
        catch(SAXParseException ex) {
            return new SAXParseError(ex);
        } catch(SAXException ex) {
            throw new IOException();
        }
        return null;
    }
   
    public boolean isDocumentValid(){
        return documentValid;
    }
    /** setter for property documentDirty. Method updateDocument() usually setsDocumentDirty to false
    */
    public void setDocumentDirty(boolean dirty){
        documentDirty=dirty;
    }

    /** Getter for property documentDirty.
    * @return Value of property documentDirty.
    */
    public boolean isDocumentDirty(){
        return documentDirty;
    }
    
    /** Getter for property nodeDirty.
    * @return Value of property nodeDirty.
    */
    public boolean isNodeDirty(){
        return nodeDirty;
    }

    /** Setter for property nodeDirty.
     * @param dirty New value of property nodeDirty.
     */
    public void setNodeDirty(boolean dirty){
        nodeDirty=dirty;
    }
    org.openide.nodes.CookieSet getCookieSet0() {
        return getCookieSet();
    }
    
    public static class J2eeErrorHandler implements ErrorHandler {

        private JSFConfigDataObject dataObject;

        public J2eeErrorHandler(JSFConfigDataObject obj) {
             dataObject=obj;
        }

        public void error(SAXParseException exception) throws SAXException {
            dataObject.createSAXParseError(exception);
            throw exception;
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            dataObject.createSAXParseError(exception);
            throw exception;
        }

        public void warning(SAXParseException exception) throws SAXException {
            dataObject.createSAXParseError(exception);
            throw exception;
        }
    }
    
    private void createSAXParseError(SAXParseException error){
        this.error = new SAXParseError(error);
    }
    
    
}
