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

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 * @author girix
 */
public class LSResourceResolverImpl implements LSResourceResolver {
    
    /** Creates a new instance of LSResourceResolverImpl */
    public LSResourceResolverImpl() {
    }
    
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURIStr) {
        //check for sanity of the systemID
        if((systemId == null) || (systemId.trim().length() <=0 ))
            return null;
        URI systemIdURI = null;
        try {
            systemIdURI = new URI(systemId);
        } catch (URISyntaxException ex) {
            return null;
        }
        
        FileObject baseFO = null;
        //get the resolver object
        CatalogModel depRez = null;
        try {
            baseFO = getFileObject(baseURIStr);
            depRez = getResolver(baseFO);
        } catch (CatalogModelException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
        if(depRez == null)
            return null;
        ModelSource baseMS = null;
        try {
            baseMS = org.netbeans.modules.xml.retriever.catalog.Utilities.createModelSource(baseFO, false);
        } catch (CatalogModelException ex) {
        }
        //get the model source from it
        ModelSource resultMS = null;
        try {
            resultMS = depRez.getModelSource(systemIdURI, baseMS);
        } catch (CatalogModelException ex) {
            return null;
        }
        if(resultMS == null)
            return null;
        
        //get file object
        FileObject resultFob = (FileObject) resultMS.getLookup().lookup(FileObject.class);
        if(resultFob == null)
            return null;
        
        //get file
        File resultFile = FileUtil.toFile(resultFob);
        if(resultFile == null)
            return null;
        
        //get URI out of file
        URI resultURI = resultFile.toURI();
        
        
        //create LSInput object
        DOMImplementation domImpl = null;
        try {
            
            domImpl =  DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException ex) {
            return null;
        }
        DOMImplementationLS dols = (DOMImplementationLS) domImpl.getFeature("LS","3.0");
        LSInput lsi = dols.createLSInput();
        InputStream is = getFileStreamFromDocument(resultFile);
        if(is != null)
            lsi.setByteStream(is);
        lsi.setSystemId(resultURI.toString());
        return lsi;
    }
    
    
    private FileObject getFileObject(String baseURIStr) throws IOException{
        if(baseURIStr == null)
            return null;
        URI baseURI = null;
        try {
            baseURI = new URI(baseURIStr);
        } catch (URISyntaxException ex) {
            IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
        if(baseURI.isAbsolute()){
            if(baseURI.getScheme().equalsIgnoreCase("file")){ //NOI18N
                File baseFile = null;
                try{
                    baseFile = new File(baseURI);
                }catch(Exception e){
                    IOException ioe = new IOException();
                    ioe.initCause(e);
                    throw ioe;
                }
                baseFile = FileUtil.normalizeFile(baseFile);
                FileObject baseFileObject = null;
                try{
                    baseFileObject = FileUtil.toFileObject(baseFile);
                }catch(Exception e){
                    IOException ioe = new IOException();
                    ioe.initCause(e);
                    throw ioe;
                }
                return baseFileObject;
            }
        }
        return null;
    }
    
    
    private CatalogModel getResolver(FileObject baseFileObject) throws CatalogModelException{
        if(baseFileObject != null)
            return CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(baseFileObject);
        return null;
    }
    
    private InputStream getFileStreamFromDocument(File resultFile) {
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(resultFile));
        if(fo != null){
            DataObject dobj = null;
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                return null;
            }
            if(dobj.isModified()){
                EditorCookie thisDocumentEditorCookie = (EditorCookie)dobj.getCookie(EditorCookie.class);
                if(thisDocumentEditorCookie == null)
                    return null;
                StyledDocument sd = null;
                try {
                    sd = thisDocumentEditorCookie.openDocument();
                } catch (IOException ex) {
                    return null;
                }
                if(sd == null)
                    return null;
                String docContent = null;
                try {
                    docContent = sd.getText(0, sd.getLength());
                } catch (BadLocationException ex) {
                    return null;
                }
                if(docContent == null)
                    return null;
                BufferedInputStream bis = new BufferedInputStream(new
                        ByteArrayInputStream(docContent.getBytes()));
                return bis;
            }else{
                //return null so that the validator will use normal file path to access doc
                return null;
            }
        }
        return null;
    }
    
    
    
    
    
}
