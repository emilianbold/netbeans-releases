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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import javax.swing.text.Document;
import org.netbeans.modules.xml.retriever.catalog.impl.CatalogWriteModelImpl;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author girix
 */

public class TestCatalogModel extends CatalogWriteModelImpl {
    private TestCatalogModel(){
    }
    
    static TestCatalogModel singletonCatMod = null;

    public static TestCatalogModel getDefault(){
        if (singletonCatMod == null){
            singletonCatMod = new TestCatalogModel();
        }
        return singletonCatMod;
    }
    
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
        File file = new File(locationURI);
        FileObject thisFileObj = FileUtil.toFileObject(file);
        return createModelSource(thisFileObj, true);
    }

    public ModelSource getModelSource(URI locationURI, ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        if(locationURI == null) {
            return null;
        }
        
        URI resolvedURI = locationURI;
        
        if(modelSourceOfSourceDocument != null) {
            FileObject sFileObj = (FileObject) modelSourceOfSourceDocument.getLookup().lookup(FileObject.class);
            if(sFileObj != null) {
                File sFile = FileUtil.toFile(sFileObj);
                if(sFile != null) {
                    URI sURI = sFile.toURI();
                    resolvedURI = sURI.resolve(locationURI);
                }
            }
        }
        
        if(resolvedURI != null) {
            return getModelSource(resolvedURI);
        }
        
        return null;
    }

    
    protected Document getDocument(FileObject fo){
        Document result = null;
        
        if (result != null) return result;
        try {

            File file = FileUtil.toFile(fo);
            FileInputStream fis = new FileInputStream(file);
            byte buffer[] = new byte[fis.available()];
                result = new org.netbeans.editor.BaseDocument(
                        org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
//            result = new javax.swing.text.PlainDocument();
            result.remove(0, result.getLength());
            fis.read(buffer);
            fis.close();
            String str = new String(buffer);
            result.insertString(0,str,null);
            
        } catch (Exception dObjEx) {
            return null;
        }
        
        return result;
    }
    
    public ModelSource createModelSource(FileObject thisFileObj, boolean readOnly) throws CatalogModelException{
        File file = FileUtil.toFile(thisFileObj);
        Lookup lookup = Lookups.fixed(new Object[]{
            file,
            thisFileObj,
            getDocument(thisFileObj),
            getDefault()
        });
        return new ModelSource(lookup, readOnly);
    }
    
    
    
    public WSDLModel getWSDLModel(URI locationURI) throws Exception {
        ModelSource source = getDefault().getModelSource(locationURI);
        WSDLModel model = WSDLModelFactory.getDefault().getModel(source);
        model.sync();
        return model;
    }

    
    public String toString(){
        return "TestCatalogModel"+super.toString();
    }
}

