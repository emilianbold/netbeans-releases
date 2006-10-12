package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation;
/*
 * TestCatalogModel.java
 *
 * Created on April 2, 2006, 10:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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

