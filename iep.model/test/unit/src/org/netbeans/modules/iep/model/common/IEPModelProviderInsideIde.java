/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
