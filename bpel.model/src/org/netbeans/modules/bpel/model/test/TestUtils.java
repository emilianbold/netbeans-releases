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

package org.netbeans.modules.bpel.model.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import javax.swing.text.Document;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 *
 * @author anjeleevich
 */
public final class TestUtils {

    private TestUtils() {
        
    }

    static {
        registerXMLKit();
    }

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
     * Method should be invoked only from unit tests
     *
     * @param clazz
     * @param relativePath
     * @return
     * @throws java.lang.Exception
     */
    public static BpelModel loadBPELModel(Class clazz, String relativePath) 
            throws Exception
    {
        Document doc = loadDocument(clazz, relativePath);
        BpelModelImpl model = new BpelModelImpl(doc, null);
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
}
