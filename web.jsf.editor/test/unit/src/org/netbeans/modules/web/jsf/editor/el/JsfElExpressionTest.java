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
package org.netbeans.modules.web.jsf.editor.el;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache.JsfBeansProvider;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean.Scope;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty;
import org.netbeans.modules.web.jsf.editor.JsfHtmlExtension;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.netbeans.modules.web.jsf.editor.TestBase;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.MockLookup;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class JsfElExpressionTest extends TestBase {

    private ClassPathProvider classpathProvider;
    private Sources sources;

    public JsfElExpressionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        FileObject srcFo = getTestFile("testWebProject/src");

        this.classpathProvider = new TestClassPathProvider(createClassPaths());
        this.sources = new TestSources(srcFo, getTestFile("testWebProject/web"));

        TestJsfBeansProvider jsfbprovider = new TestJsfBeansProvider(
                Collections.singletonList(new FacesManagedBeanImpl("testbean", "java.lang.String")));

        MockLookup.setInstances(
                new TestUserCatalog(),
                jsfbprovider,
                new RepositoryImpl(),
                new TestProjectFactory(),
                new SimpleFileOwnerQueryImplementation(),
                classpathProvider,
                new TestLanguageProvider(),
                new FakeWebModuleProvider(getTestFile("testWebProject")));

//        IndexingManager.getDefault().refreshIndexAndWait(srcFo.getURL(), null);
    }

    public void testFakeJsfBeansCache() {
        List<FacesManagedBean> beans = JSFBeanCache.getBeans(null);
        assertNotNull(beans);
        assertEquals(1, beans.size());
        FacesManagedBean bean = beans.get(0);
        assertNotNull(bean);
        assertEquals("testbean", bean.getManagedBeanName());
        assertEquals("java.lang.String", bean.getManagedBeanClass());
    }

    public void testParseExpression() throws BadLocationException, IOException, ParseException {
        FileObject file = getTestFile("testWebProject/web/index.xhtml");
        assertNotNull(file);
        Document doc = getDefaultDocument(file);

        WebModule wm = WebModule.getWebModule(file);
        assertNotNull(wm);

        JsfElExpression expr = new JsfElExpression(wm, doc);

        //initialize the html extension
        JsfSupport.findFor(file);
        Collection<HtmlExtension> extensions = HtmlExtension.getRegisteredExtensions("text/xhtml");
        assertNotNull(extensions);
        assertEquals(1, extensions.size());
        final HtmlExtension hext = extensions.iterator().next();

        //init the EL embedding
        Source source = Source.create(file);
        ParserManager.parse(Collections.singletonList(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                HtmlParserResult result = (HtmlParserResult)Utils.getResultIterator(resultIterator, "text/html").getParserResult();
                ((JsfHtmlExtension)hext).checkELEnabled(result);
                //block until the recolor AWT task finishes
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        //no-op
                    }
                });
            }
        });

        doc.remove(0, doc.getLength());
        doc.insertString(0, "#{testbean.}", null);
        //                   012345678901

        int offset = 11;
        int code = expr.parse(offset);

        System.out.println("code=" + code);
        System.out.println("clazz=" + expr.getObjectClass());
        List<CompletionItem> items = expr.getMethodCompletionItems(expr.getObjectClass(), offset);
        for(CompletionItem item : items) {
            System.out.println(item);
        }



    }

    private final class TestProjectFactory implements ProjectFactory {

        TestProjectFactory() {
        }

        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            return new TestProject(projectDirectory, state);
        }

        public void saveProject(Project project) throws IOException, ClassCastException {
        }

        public boolean isProject(FileObject dir) {
            FileObject testproject = dir.getFileObject("web");
            return testproject != null && testproject.isFolder();
        }
    }

    private final class TestProject implements Project {

        private final FileObject dir;
        final ProjectState state;
        Throwable error;
        int saveCount = 0;
        private Lookup lookup;

        public TestProject(FileObject dir, ProjectState state) {
            this.dir = dir;
            this.state = state;

            InstanceContent ic = new InstanceContent();
            ic.add(classpathProvider);
            ic.add(sources);

            this.lookup = new AbstractLookup(ic);

        }

        public Lookup getLookup() {
            return lookup;
        }

        public FileObject getProjectDirectory() {
            return dir;
        }

        public String toString() {
            return "testproject:" + getProjectDirectory().getNameExt();
        }
    }

    private final class TestSources implements Sources {

        private FileObject[] roots;

        TestSources(FileObject... roots) {
            this.roots = roots;
        }

        public SourceGroup[] getSourceGroups(String type) {
            SourceGroup[] sg = new SourceGroup[roots.length];
            for (int i = 0; i < roots.length; i++) {
                sg[i] = new TestSourceGroup(roots[i]);
            }
            return sg;
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }
    }

    private final class TestSourceGroup implements SourceGroup {

        private FileObject root;

        public TestSourceGroup(FileObject root) {
            this.root = root;
        }

        public FileObject getRootFolder() {
            return root;
        }

        public String getName() {
            return root.getNameExt();
        }

        public String getDisplayName() {
            return getName();
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            return FileUtil.getRelativePath(root, file) != null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
    }

    public static class RepositoryImpl extends Repository {

        public RepositoryImpl() {
            super(createDefFs());
        }

        private static FileSystem createDefFs() {
//            try {
                FileSystem writeFs = FileUtil.createMemoryFileSystem();
//                FileSystem glassfish = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/glassfish/javaee/layer.xml"));
//                FileSystem glassfish2 = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/j2ee/sun/ide/j2ee/layer.xml"));
                return new MultiFileSystem(new FileSystem[]{writeFs/*, glassfish, glassfish2*/});
//            } catch (SAXException ex) {
//                Exceptions.printStackTrace(ex);
//                return null;

//            }
        }
    }

    public class TestJsfBeansProvider implements JsfBeansProvider {

        private List<? extends FacesManagedBean> beans;

        public TestJsfBeansProvider(List<? extends FacesManagedBean> beans) {
            this.beans = beans;
        }

        public List<FacesManagedBean> getBeans(WebModule webModule) {
            return (List<FacesManagedBean>) beans;
        }

    }


    public static class FacesManagedBeanImpl implements FacesManagedBean {

        private String name, clazz;

        public FacesManagedBeanImpl(String name, String clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public Boolean getEager() {
            return true; //???
        }

        public String getManagedBeanName() {
            return name;
        }

        public String getManagedBeanClass() {
            return clazz;
        }

        public Scope getManagedBeanScope() {
            return Scope.REQUEST;
        }

        public String getManagedBeanScopeString() {
            return getManagedBeanScope().toString(); //???
        }

        public List<ManagedProperty> getManagedProperties() {
            return Collections.emptyList();
        }

    }

    public static class TestUserCatalog extends UserCatalog {

        @Override
        public EntityResolver getEntityResolver() {
            return new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return null;
                }
            };
        }

    }
}
