/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.rest.wadl.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.xml.retriever.catalog.impl.CatalogFileWrapperDOMImpl;
import org.netbeans.modules.xml.retriever.catalog.impl.CatalogWriteModelImpl;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * 
 */

public class TestCatalogModel extends CatalogWriteModelImpl{
    private TestCatalogModel(File file) throws IOException{
        super(file);
    }
    
    static TestCatalogModel singletonCatMod = null;
    public static TestCatalogModel getDefault(){
        if (singletonCatMod == null){
            CatalogFileWrapperDOMImpl.TEST_ENVIRONMENT = true;
            try {
                singletonCatMod = new TestCatalogModel(new File(System.getProperty("java.io.tmpdir")));
                FileObject catalogFO = singletonCatMod.getCatalogFileObject();
                File catFile = FileUtil.toFile(catalogFO);
                catFile.deleteOnExit();
                initCatalogFile();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return singletonCatMod;
    }
    
    
    
    /**
     * This method could be overridden by the Unit testcase to return a special
     * ModelSource object for a FileObject with custom impl of classes added to the lookup.
     * This is optional if both getDocument(FO) and createCatalogModel(FO) are overridden.
     */
    protected ModelSource createModelSource(final FileObject thisFileObj, boolean editable) throws CatalogModelException{
        assert thisFileObj != null : "Null file object.";
        final CatalogModel catalogModel = createCatalogModel(thisFileObj);
        final DataObject dobj;
        try {
            dobj = DataObject.find(thisFileObj);
        } catch (DataObjectNotFoundException ex) {
            throw new CatalogModelException(ex);
        }
        Lookup proxyLookup = Lookups.proxy(
                new Lookup.Provider() {
            public Lookup getLookup() {
                Document document = null;
                Logger l = Logger.getLogger(getClass().getName());
                document = getDocument(thisFileObj);
                return Lookups.fixed(new Object[] {
                    thisFileObj,
                    document,
                    dobj,
                    catalogModel
                });
            }
        }
        );
        return new ModelSource(proxyLookup, editable);
    }
    
    private Document  getDocument(FileObject fo){
        Document result = null;
        if (documentPooling) {
            result = documentPool().get(fo);
        }
        if (result != null) return result;
        try {
            
            File file = FileUtil.toFile(fo);
            FileInputStream fis = new FileInputStream(file);
            byte buffer[] = new byte[fis.available()];
            result = new org.netbeans.editor.BaseDocument(
                    org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
            result.remove(0, result.getLength());
            fis.read(buffer);
            fis.close();
            String str = new String(buffer);
            result.insertString(0,str,null);
            
        } catch (Exception dObjEx) {
            return null;
        }
        if (documentPooling) {
            documentPool().put(fo, result);
        }
        return result;
    }
    
    protected CatalogModel createCatalogModel(FileObject fo) throws CatalogModelException{
        return getDefault();
    }
    
    public ModelSource createTestModelSource(FileObject fo, boolean editable) throws CatalogModelException{
        final DataObject dobj;
        final CatalogModel catalogModel = createCatalogModel(fo);
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            throw new CatalogModelException(ex);
        }
        Lookup lookup = Lookups.proxy(new Lookup.Provider() {
            public Lookup getLookup() {
                        return Lookups.fixed(new Object[] {
                            dobj.getPrimaryFile(),
                            getDocument(dobj.getPrimaryFile()),
                            dobj,
                            catalogModel
                        });

            }
        } );
        return new ModelSource(lookup, editable);
    }
    
    public void addNamespace(NamespaceLocation nl) throws Exception {
        this.addURI(nl.getLocationURI(), nl.getResourceURI());
    }
    
    public SchemaModel getSchemaModel(NamespaceLocation nl) throws Exception {
        return getSchemaModel(nl.getLocationURI());
    }
    
    public SchemaModel getSchemaModel(URI lcationURI) throws Exception {
        SchemaModel model = SchemaModelFactory.getDefault().getModel(
                singletonCatMod.getModelSource(lcationURI));
        return model;
    }
    
    private Map<FileObject,Document> fileToDocumentMap;
    private Map<FileObject,Document> documentPool() {
        if (fileToDocumentMap == null) {
            fileToDocumentMap = new HashMap<FileObject,Document>();
        }
        return fileToDocumentMap;
    }
    private boolean documentPooling = true;
    
    public void setDocumentPooling(boolean v) {
        documentPooling = v;
        if (! documentPooling) {
            clearDocumentPool();
        }
    }

    public void clearDocumentPool() {
        fileToDocumentMap = null;
    }
    
    private static void initCatalogFile() throws Exception {
        TestCatalogModel instance = singletonCatMod;
        for (NamespaceLocation nl:NamespaceLocation.values()) {
            instance.addNamespace(nl);
        }
    }
    
    public WadlModel getWadlModel(URI locationURI) throws Exception {
        ModelSource source = getDefault().getModelSource(locationURI);
        WadlModel model = WadlModelFactory.getDefault().getModel(source);
        return model;
    }
    
    public WadlModel getWadlModel(NamespaceLocation nl) throws Exception {
        nl.refreshResourceFile();
        return getWadlModel(nl.getLocationURI());
    }
    
    @Override
    public String toString(){
        return "TestCatalogModel"+super.toString();
    }
}

