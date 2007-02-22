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

/*
 * TestCatalogModel.java
 *
 * Created on April 2, 2006, 10:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.ModelAccessProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author girix
 */

public class TestUtil{
    
    public static ModelSource createModelSource(final FileObject thisFileObj, boolean editable) throws CatalogModelException{
        assert thisFileObj != null : "Null file object.";
        final DataObject dobj;
        try {
            dobj = DataObject.find(thisFileObj);
        } catch (DataObjectNotFoundException ex) {
            throw new CatalogModelException(ex);
        }
        final Document document = getDocument(thisFileObj);
        Lookup proxyLookup = Lookups.proxy(
                new Lookup.Provider() {
            public Lookup getLookup() {
                return Lookups.fixed(new Object[] {
                    thisFileObj,
                    document,
                    dobj,
                    new ModelAccessProviderImpl(thisFileObj)
                });
            }
        }
        );
        return new ModelSource(proxyLookup, editable);
    }
    
    private static  Document getDocument(FileObject fo){
        Document result = null;
        if (result != null) return result;
        try {
            
            InputStream fis =  fo.getInputStream();
            byte buffer[] = new byte[fis.available()];
//            result = new org.netbeans.editor.BaseDocument(
//                    org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
            result = new javax.swing.text.PlainDocument();
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
    
    static class ModelAccessProviderImpl implements ModelAccessProvider {
        private FileObject mFile;
        
        public ModelAccessProviderImpl(FileObject file) {
            this.mFile = file;
        }
        
        public Object getModelSourceKey(ModelSource source) {
            return mFile;
        }

    }
}

