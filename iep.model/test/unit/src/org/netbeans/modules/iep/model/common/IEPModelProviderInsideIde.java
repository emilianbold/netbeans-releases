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
package org.netbeans.modules.iep.model.common;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.logging.Logger;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPModelFactory;
import org.netbeans.modules.xml.retriever.catalog.impl.CatalogWriteModelImpl;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author radval
 */
public class IEPModelProviderInsideIde extends CatalogWriteModelImpl implements 
    IEPModelProvider {
    
    /** Creates a new instance of BPELModelProviderInsideIde */
    public IEPModelProviderInsideIde() {
    }

    public IEPModel getWLMModel(URI locationURI) throws Exception {
        System.out.println(toString() +" :" + locationURI);
        ModelSource source = getModelSource(locationURI);
        IEPModel model = IEPModelFactory.getDefault().getModel(source);
//        model.sync();
       return model;
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

    
    protected BaseDocument getDocument(FileObject fo){
        BaseDocument result = null;
        
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
    
    protected CatalogModel createCatalogModel(FileObject fo) throws CatalogModelException{
        return this;
    }
    
    public ModelSource createModelSource(FileObject thisFileObj, boolean editable) throws CatalogModelException{
        assert thisFileObj != null : "Null file object.";
        final DataObject dobj;
        try {
            dobj = DataObject.find(thisFileObj);
        } catch (DataObjectNotFoundException ex) {
            throw new CatalogModelException(ex);
        }
        Lookup proxyLookup = Lookups.proxy(
                new Lookup.Provider() {
            public Lookup getLookup() {
                Logger l = Logger.getLogger(getClass().getName());
                BaseDocument document = getDocument(dobj.getPrimaryFile());
                return Lookups.fixed(new Object[] {
                    
                    dobj.getPrimaryFile(),
                    document,
                    dobj,
                    IEPModelProviderInsideIde.this
                
                });
            }
        }
        
        );
        return new ModelSource(proxyLookup, editable);
    }
    
    
}
