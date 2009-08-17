/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.references.RefCacheSupport;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import static org.junit.Assert.*;

/**
 * Utility methods
 * Most content is taken from SOA.UI module
 *
 * @author anjeleevich
 * @author Nikita Krjukov
 */
public final class TestUtils {

    private TestUtils() {
        
    }

    public static <T extends AbstractDocumentModel> T loadXamModel(
            String resourcePath, Class<T> modelImplClass) throws Exception {
        URL url = TestUtils.class.getResource(resourcePath);
        FileObject fo = URLMapper.findFileObject(url);
        T model = loadXamModel(modelImplClass, fo, null, true);
        return model;
    }

    public static <T extends AbstractDocumentModel> T loadXamModel(
            String archivePath, String resourcePath,
            Class<T> modelImplClass) throws Exception {
        URL url = TestUtils.class.getResource(archivePath);
        url = new URL("jar:" + url.toString() + "!/" + resourcePath); // NOI18N
        FileObject fo = URLMapper.findFileObject(url);
        T model = loadXamModel(modelImplClass, fo, null, true);
        return model;
    }

    /**
     * Method should be invoked only from test
     *
     * Usage example: BpelModelImpl modelImpl = TestUtils.loadXAMModel(
     *      BpelModelImpl.class, fileObj, null, true);
     *
     * @param <T>
     * @param modelImplClass
     * @param xmlFo
     * @param catalogModel
     * @param editable
     * @return
     * @throws java.lang.Exception
     *
     * @author Nikita Krjukov
     */
    public static <T extends AbstractDocumentModel> T loadXamModel(
            Class<T> modelImplClass, FileObject xmlFo,
            CatalogModel catalogModel, boolean editable) throws Exception
    {
        //
        Document doc = loadDocument(xmlFo.getInputStream());
        assert doc != null : "Can't load the document: " + xmlFo.toString();
        //
        if (catalogModel == null) {
            catalogModel = new TestCatalogModel(true);
        }
        //
        Lookup lookup = Lookups.fixed(xmlFo, doc, catalogModel);
        ModelSource ms = new ModelSource(lookup, editable);
        //
        Constructor<T> modelConstructor = modelImplClass
                .getConstructor(ModelSource.class);
        T model = modelConstructor.newInstance(ms);
        //
        model.sync();
        return model;
    }

    /**
     * Method should be invoked only from unit tests
     *
     * @param clazz
     * @param relativePath
     * @return
     * @throws java.lang.Exception
     */
    public static Document loadDocument(Class clazz, String relativePath)
            throws Exception
    {
        return loadDocument(clazz.getResourceAsStream(relativePath));
    }

    /**
     * Method should be invoked only from unit tests
     *
     * @param in
     * @return
     * @throws java.lang.Exception
     */
    public static Document loadDocument(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        StringBuffer sbuf = new StringBuffer();

        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            reader.close();
        }

        return loadDocument(sbuf.toString());
    }

    /**
     * Method should be invoked only from unit tests
     *
     * @param documentContent
     * @return
     * @throws java.lang.Exception
     */
    public static Document loadDocument(String documentContent)
            throws Exception
    {
        Class<?> documentClass = Class.forName(
                "org.netbeans.editor.BaseDocument");

//        Class xmlKitClass = Class.forName(
//                "org.netbeans.modules.xml.text.syntax.XMLKit");
//
//        Constructor documentConstructor = documentClass
//                .getConstructor(Class.class, boolean.class);
//
//        Document document = (Document) documentConstructor
//                .newInstance(xmlKitClass, false);

        Constructor<?> documentConstructor = documentClass
                .getConstructor(boolean.class, String.class);

        Document document = (Document) documentConstructor
                .newInstance(false, "text/xml");

        document.insertString(0, documentContent, null);

        return document;
    }

    /**
     * Recursively checks imports.
     * The method is taken from BPEL Mapper tests
     *
     * @param model
     * @throws java.lang.Exception
     */
    public static void checkImports(Model model) {
        if (model instanceof BpelModelImpl) {
            BpelModelImpl bpelModel = BpelModelImpl.class.cast(model);
            Import[] importArr = bpelModel.getProcess().getImports();
            RefCacheSupport refCache = bpelModel.getRefCacheSupport();
            //
            for (int index = 0; index < importArr.length; index++) {
                Import imp = importArr[index];
                if (Import.SCHEMA_IMPORT_TYPE.equals(imp.getImportType())) {
                    SchemaModel schemaModel = refCache.optimizedSchemaResolve(imp);
                    // SchemaModel schemaModel = ImportHelper.getSchemaModel(imp);
                    assertNotNull(schemaModel);
                    checkImports(schemaModel);
                } else if (Import.WSDL_IMPORT_TYPE.equals(imp.getImportType())) {
                    WSDLModel wsdlModel = refCache.optimizedWsdlResolve(imp);
                    // WSDLModel wsdlModel = ImportHelper.getWsdlModel(imp);
                    assertNotNull(wsdlModel);
                    checkImports(wsdlModel);
                }
            }
        } else if (model instanceof SchemaModelImpl) {
            SchemaModelImpl sModel = SchemaModelImpl.class.cast(model);
            Schema schema = sModel.getSchema();
            //
            Collection<org.netbeans.modules.xml.schema.model.Import> imports =
                    schema.getImports();
            for (org.netbeans.modules.xml.schema.model.Import imp : imports) {
                SchemaModel resolved = sModel.resolve(imp);
                // SchemaModel resolved = imp.resolveReferencedModel();
                assertNotNull(resolved);
                checkImports(resolved);
            }
            //
            Collection<Include> includes = schema.getIncludes();
            for (Include incl : includes) {
                SchemaModel resolved = sModel.resolve(incl);
                // SchemaModel resolved = incl.resolveReferencedModel();
                assertNotNull(resolved);
                checkImports(resolved);
            }
        } else if (model instanceof WSDLModel) {
            Definitions def = WSDLModel.class.cast(model).getDefinitions();
            //
            Collection<org.netbeans.modules.xml.wsdl.model.Import> imports =
                    def.getImports();
            for (org.netbeans.modules.xml.wsdl.model.Import imp : imports) {
                try {
                    WSDLModel resolved = imp.getImportedWSDLModel();
                    assertNotNull(resolved);
                    checkImports(resolved);
                } catch (CatalogModelException ex) {
                    Exceptions.printStackTrace(ex);
                    assertTrue("Exception while resolving a WSDL import", false);
                }
            }
            //
            Types types = def.getTypes();
            if (types != null) {
                Collection<Schema> schemas = types.getSchemas();
                for (Schema schema : schemas) {
                    SchemaModel sModel = schema.getModel();
                    assertNotNull(sModel);
                    checkImports(sModel);
                }
            }
        } else {
            throw new RuntimeException(model.getClass().getSimpleName() +
                    " is unsupported model's class!"); // NOI18N
        }
    }

    public static Document setDocumentContentTo(Document doc, 
            InputStream in) throws Exception {
        //
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        doc.remove(0, doc.getLength());
        doc.insertString(0,sbuf.toString(),null);
        return doc;
    }

    public static Document setDocumentContentTo(Document doc, 
            String resourcePath) throws Exception {
        //
        InputStream is = TestUtils.class.getResourceAsStream(resourcePath);
        assertNotNull("Resource \"" + resourcePath + "\" not found!", is); // NOI18N
        return setDocumentContentTo(doc, is);
    }

    public static Document setDocumentContentTo(Document doc,
            String archivePath, String resourcePath) throws Exception {
        //
        URL url = TestUtils.class.getResource(archivePath);
        url = new URL("jar:" + url.toString() + "!/" + resourcePath); // NOI18N
        FileObject fo = URLMapper.findFileObject(url);
        //
        assertNotNull("File not found: " + url, fo);
        //
        return setDocumentContentTo(doc, fo.getInputStream());
    }

    public static void setDocumentContentTo(DocumentModel model,
            String resourcePath) throws Exception {
        //
        setDocumentContentTo(((AbstractDocumentModel)model).getBaseDocument(),
                resourcePath);
    }

    public static void setDocumentContentTo(DocumentModel model,
            String archivePath, String resourcePath) throws Exception {
        //
        setDocumentContentTo(((AbstractDocumentModel)model).getBaseDocument(),
                archivePath, resourcePath);
    }



}
