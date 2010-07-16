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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import static org.junit.Assert.*;


/**
 *
 * @author anjeleevich
 * @author Nikita Krjukov
 */
public final class TestUtils {

    private TestUtils() {
        
    }

    static {
        registerXMLKit();
    }

    /**
     * Method should be invoked only from test
     *
     * Usage example: BpelModelImpl modelImpl = TestUtils.loadXAMModel(
     *      BpelModelImpl.class, getClass(), "data/empty.bpel");
     *
     * @param <T>
     * @param modelImplClass
     * @param clazz
     * @param relativePath
     * @return
     * @throws java.lang.Exception
     */
    public static <T extends AbstractDocumentModel> T loadXAMModel(
            Class<T> modelImplClass, Class clazz, String relativePath)
                    throws Exception
    {
        Document doc = loadDocument(clazz, relativePath);

        Constructor<T> modelConstructor = modelImplClass
                .getConstructor(Document.class, Lookup.class);
        T model = modelConstructor.newInstance(doc, null);

        model.sync();
        
        return model;
    }

    /**
     * Method should be invoked only from test
     *
     * Usage example: BpelModelImpl modelImpl = TestUtils.loadXAMModel(
     *      BpelModelImpl.class, getClass(), "data/empty.bpel", null, true);
     *
     * @param <T>
     * @param modelImplClass
     * @param classLoaderOwner
     * @param relativePath
     * @param catalogModel
     * @param editable
     * @return
     * @throws java.lang.Exception
     *
     * @author Nikita Krjukov
     */
    public static <T extends AbstractDocumentModel> T loadXAMModel(
            Class<T> modelImplClass, Class classLoaderOwner,
            String relativePath, CatalogModel catalogModel, boolean editable)
                    throws Exception
    {
        URL url = classLoaderOwner.getResource(relativePath);
        URI uri = url.toURI();
        File file = new File(uri);
        FileObject fo = FileUtil.toFileObject(file);
        assert fo != null : "Unknown file: " + relativePath;
        //
        return loadXamModel(modelImplClass, fo, catalogModel, editable);
    }

    /**
     * Loads an XAM model from a file
     * @param <T>
     * @param baseClass the path to the file has to be relative to this class
     * @param resourcePath the path to the file
     * @param modelImplClass the implementation class of the XAM model
     * @return the loaded model
     * @throws java.lang.Exception
     */
    public static <T extends AbstractDocumentModel> T loadXamModel(
            Class baseClass, String resourcePath, Class<T> modelImplClass)
            throws Exception {
        //
        URL url = baseClass.getResource(resourcePath);
        FileObject fo = URLMapper.findFileObject(url);
        T model = loadXamModel(modelImplClass, fo, null, true);
        return model;
    }

    /**
     * Loads an XAM model from an archive file
     * @param <T>
     * @param baseClass the path to archive has to be relative to this class
     * @param archivePath the path to archive file (*.zip)
     * @param resourcePath the path inside of archive file
     * @param modelImplClass the implementation class of the XAM model
     * @return the loaded model
     * @throws java.lang.Exception
     */
    public static <T extends AbstractDocumentModel> T loadXamModel(
            Class baseClass, String archivePath, String resourcePath,
            Class<T> modelImplClass) throws Exception {
        //
        URL url = baseClass.getResource(archivePath);
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

    public static void registerXMLKit() {
        String[] path = new String[] { "Editors", "text", "xml" };
        FileObject target = Repository.getDefault().getDefaultFileSystem().getRoot();
        try {
            for (int i=0; i<path.length; i++) {
                FileObject f = target.getFileObject(path[i]);
                if (f == null) {
                    f = target.createFolder(path[i]);
                }
                target = f;
            }
            String name = "EditorKit.instance";
            if (target.getFileObject(name) == null) {
                FileObject f = target.createData(name);
                f.setAttribute("instanceClass", "org.netbeans.modules.xml.text.syntax.XMLKit");
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
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

    public static Document setDocumentContentTo(Document doc, Class baseClass,
            String resourcePath) throws Exception {
        //
        InputStream is = baseClass.getResourceAsStream(resourcePath);
        assertNotNull("Resource \"" + resourcePath + "\" not found!", is); // NOI18N
        return setDocumentContentTo(doc, is);
    }

    public static Document setDocumentContentTo(Document doc, Class baseClass,
            String archivePath, String resourcePath) throws Exception {
        //
        URL url = baseClass.getResource(archivePath);
        url = new URL("jar:" + url.toString() + "!/" + resourcePath); // NOI18N
        FileObject fo = URLMapper.findFileObject(url);
        //
        assertNotNull("File not found: " + url, fo);
        //
        return setDocumentContentTo(doc, fo.getInputStream());
    }

    public static void setDocumentContentTo(DocumentModel model, Class baseClass,
            String resourcePath) throws Exception {
        //
        setDocumentContentTo(((AbstractDocumentModel)model).getBaseDocument(),
                baseClass, resourcePath);
    }

    public static void setDocumentContentTo(DocumentModel model, Class baseClass,
            String archivePath, String resourcePath) throws Exception {
        //
        setDocumentContentTo(((AbstractDocumentModel)model).getBaseDocument(),
                baseClass, archivePath, resourcePath);
    }


}
