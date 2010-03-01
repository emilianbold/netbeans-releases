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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.jsf.editor;

import java.net.URL;
import java.util.Collections;
import java.util.StringTokenizer;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.gsf.HtmlLanguage;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * @author Marek Fukala
 */
public class TestBase extends CslTestBase {

    private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N

    public TestBase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(MockMimeLookup.class);
        super.setUp();
    }

    protected BaseDocument createDocument() {
        NbEditorDocument doc = new NbEditorDocument(HtmlKit.HTML_MIME_TYPE);
        doc.putProperty(PROP_MIME_TYPE, HtmlKit.HTML_MIME_TYPE);
        doc.putProperty(Language.class, HTMLTokenId.language());
        return doc;
    }

    protected Document[] createDocuments(String... fileName) {
        try {
            List<Document> docs = new ArrayList<Document>();
            FileSystem memFS = FileUtil.createMemoryFileSystem();
            for (String fName : fileName) {

                //we may also create folders
                StringTokenizer items = new StringTokenizer(fName, "/");
                FileObject fo = memFS.getRoot();
                while (items.hasMoreTokens()) {
                    String item = items.nextToken();
                    if (items.hasMoreTokens()) {
                        //folder
                        fo = fo.createFolder(item);
                    } else {
                        //last, create file
                        fo = fo.createData(item);
                    }
                    assertNotNull(fo);
                }

                DataObject dobj = DataObject.find(fo);
                assertNotNull(dobj);

                EditorCookie cookie = dobj.getCookie(EditorCookie.class);
                assertNotNull(cookie);

                Document document = (Document) cookie.openDocument();
                assertEquals(0, document.getLength());

                docs.add(document);

            }
            return docs.toArray(new Document[]{});
        } catch (Exception ex) {
            throw new IllegalStateException("Error setting up tests", ex);
        }
    }

    public Document getDefaultDocument(FileObject fo) throws DataObjectNotFoundException, IOException {
        DataObject dobj = DataObject.find(fo);
        assertNotNull(dobj);

        EditorCookie cookie = dobj.getCookie(EditorCookie.class);
        assertNotNull(cookie);

        Document document = (Document) cookie.openDocument();
        return document;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new HtmlLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return HtmlKit.HTML_MIME_TYPE;
    }

    public HtmlParserResult getHtmlParserResult(String fileName) throws ParseException {
        FileObject file = getTestFile(fileName);

        assertNotNull(file);

        Source source = getTestSource(file);
        assertNotNull(source);

        final HtmlParserResult[] _result = new HtmlParserResult[1];
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                _result[0] = (HtmlParserResult) WebUtils.getResultIterator(resultIterator, "text/html").getParserResult();
            }
        });

        HtmlParserResult result = _result[0];
        assertNotNull(result);

        return result;
    }

    protected class TestClassPathProvider implements ClassPathProvider {

        private Map<String, ClassPath> map;

        public TestClassPathProvider(Map<String, ClassPath> map) {
            this.map = map;
        }

        public ClassPath findClassPath(FileObject file, String type) {
            if (map != null) {
                return map.get(type);
            } else {
                return null;
            }
        }
    }

    protected Map<String, ClassPath> createClassPaths() throws Exception {
        Map<String, ClassPath> cps = new HashMap<String, ClassPath>();
        ClassPath cp = createServletAPIClassPath();
        cps.put(ClassPath.COMPILE, cp);
        cps.put(ClassPath.SOURCE, cp);
        cps.put(ClassPath.BOOT, createBootClassPath());
        return cps;
    }

     /**
     * Creates boot {@link ClassPath} for platform the test is running on,
     * it uses the sun.boot.class.path property to find out the boot path roots.
     * @return ClassPath
     * @throws java.io.IOException when boot path property contains non valid path
     */
    public static ClassPath createBootClassPath () throws IOException {
        String bootPath = System.getProperty ("sun.boot.class.path");
        String[] paths = bootPath.split(File.pathSeparator);
        List<URL>roots = new ArrayList<URL> (paths.length);
        for (String path : paths) {
            File f = new File (path);
            if (!f.exists()) {
                continue;
            }
            URL url = f.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            roots.add (url);
//            System.out.println(url);
        }
//        System.out.println("-----------");
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }

    public final ClassPath createServletAPIClassPath() throws MalformedURLException, IOException {
        String path = System.getProperty("web.project.jars");
        String[] st = PropertyUtils.tokenizePath(path);
        List<FileObject> fos = new ArrayList<FileObject>();
        for (int i = 0; i < st.length; i++) {
            String token = st[i];
            File f = new File(token);
            if (!f.exists()) {
                fail("cannot find file " + token);
            }
            FileObject fo = FileUtil.toFileObject(f);
            fos.add(FileUtil.getArchiveRoot(fo));
        }
        return ClassPathSupport.createClassPath(fos.toArray(new FileObject[fos.size()]));
    }

    protected static class FakeWebModuleProvider implements WebModuleProvider {

        private FileObject webRoot;

        public FakeWebModuleProvider(FileObject webRoot) {
            this.webRoot = webRoot;
        }

        public WebModule findWebModule(FileObject file) {
            return WebModuleFactory.createWebModule(new FakeWebModuleImplementation2(webRoot));
        }
    }

    private static class FakeWebModuleImplementation2 implements WebModuleImplementation2 {

        private FileObject webRoot;

        public FakeWebModuleImplementation2(FileObject webRoot) {
            this.webRoot = webRoot;
        }

        public FileObject getDocumentBase() {
            return webRoot;
        }

        public String getContextPath() {
            return "/";
        }

        public Profile getJ2eeProfile() {
            return Profile.JAVA_EE_6_FULL;
        }

        public FileObject getWebInf() {
            return null;
        }

        public FileObject getDeploymentDescriptor() {
            return null;
        }

        public FileObject[] getJavaSources() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MetadataModel<WebAppMetadata> getMetadataModel() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
