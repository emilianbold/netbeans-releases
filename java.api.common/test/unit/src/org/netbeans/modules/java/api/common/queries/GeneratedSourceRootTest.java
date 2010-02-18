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

package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test for #105645 functionality: build/generated-sources/NAME/ roots.
 */
public class GeneratedSourceRootTest extends NbTestCase {

    public GeneratedSourceRootTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws IOException {
        FileObject fo = FileUtil.getConfigFile("Services");
        if (fo != null) {
            fo.delete();
        }
        clearWorkDir();
        MockLookup.setInstances(new TestAntBasedProjectType());
    }

    public void testSourceRoots() throws Exception {
        Project p = createTestProject(true);
        FileObject d = p.getProjectDirectory();
        FileObject src = d.getFileObject("src");
        FileObject stuff = d.getFileObject("build/generated-sources/stuff");
        SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assertEquals(2, groups.length);
        assertEquals(src, groups[0].getRootFolder());
        assertEquals(d.getFileObject("test"), groups[1].getRootFolder());
        ClassPath sourcePath = ClassPath.getClassPath(src, ClassPath.SOURCE);
        assertEquals(Arrays.asList(src, stuff), Arrays.asList(sourcePath.getRoots()));
        FileObject moreStuff = FileUtil.createFolder(d, "build/generated-sources/morestuff");
        final Set<FileObject> expected = new TreeSet<FileObject>(new FOComparator());
        expected.addAll(Arrays.asList(src, stuff, moreStuff));
        final Set<FileObject> result = new TreeSet<FileObject>(new FOComparator());
        result.addAll(Arrays.asList(sourcePath.getRoots()));
        assertEquals(expected, result);
        ClassPath compile = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertEquals(compile, ClassPath.getClassPath(stuff, ClassPath.COMPILE));
        assertEquals(compile, ClassPath.getClassPath(moreStuff, ClassPath.COMPILE));
        assertEquals(ClassPath.getClassPath(src, ClassPath.EXECUTE), ClassPath.getClassPath(stuff, ClassPath.EXECUTE));
        assertEquals(ClassPath.getClassPath(src, ClassPath.BOOT), ClassPath.getClassPath(stuff, ClassPath.BOOT));
        d.getFileObject("build").delete();
        assertEquals(Arrays.asList(src), Arrays.asList(sourcePath.getRoots()));
    }

    public void testMiscellaneousQueries() throws Exception {
        Project p = createTestProject(true);
        FileObject d = p.getProjectDirectory();
        FileObject src = d.getFileObject("src");
        FileObject test = d.getFileObject("test");
        FileObject stuff = d.getFileObject("build/generated-sources/stuff");
        URL classes = new URL(d.getURL(), "build/classes/");
        URL testClasses = new URL(d.getURL(), "build/test/classes/");
        FileObject xgen = stuff.getFileObject("net/nowhere/XGen.java");
        assertEquals(Arrays.asList(src, stuff), Arrays.asList(SourceForBinaryQuery.findSourceRoots(classes).getRoots()));
        assertEquals(Arrays.asList(test), Arrays.asList(SourceForBinaryQuery.findSourceRoots(testClasses).getRoots()));
        assertEquals(Collections.singletonList(classes), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(src.getURL()).getRoots()));
        assertEquals(Collections.singletonList(testClasses), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(test.getURL()).getRoots()));
        assertEquals(Collections.singletonList(classes), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(stuff.getURL()).getRoots()));
        assertEquals(Collections.singletonList(src.getURL()), Arrays.asList(UnitTestForSourceQuery.findSources(test)));
        assertEquals(Collections.singletonList(test.getURL()), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src)));
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(stuff));
        FileBuiltQuery.Status status = FileBuiltQuery.getStatus(xgen);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        FileUtil.createData(d, "build/classes/net/nowhere/XGen.class");
        assertTrue(status.isBuilt());
        assertEquals("ISO-8859-2", FileEncodingQuery.getEncoding(xgen).name());
        // check also dynamic changes in set of gensrc roots:
        FileObject moreStuff = FileUtil.createFolder(d, "build/generated-sources/morestuff");
        FileObject ygen = FileUtil.createData(moreStuff, "net/nowhere/YGen.java");
        assertEquals(new HashSet<FileObject>(Arrays.asList(src, stuff, moreStuff)),
                new HashSet<FileObject>(Arrays.asList(SourceForBinaryQuery.findSourceRoots(classes).getRoots())));
        assertEquals(Collections.singletonList(classes), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(moreStuff.getURL()).getRoots()));
        // XXX should previously created Result objects fire changes? ideally yes, but probably unnecessary
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(moreStuff));
        status = FileBuiltQuery.getStatus(ygen);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        FileUtil.createData(d, "build/classes/net/nowhere/YGen.class");
        assertTrue(status.isBuilt());
        assertEquals("ISO-8859-2", FileEncodingQuery.getEncoding(ygen).name());
        d.getFileObject("build").delete();
        assertEquals(Arrays.asList(src), Arrays.asList(SourceForBinaryQuery.findSourceRoots(classes).getRoots()));
    }

    public void testFirstGenSrcAddedDynamically() throws Exception {
        Project p = createTestProject(false);
        FileObject d = p.getProjectDirectory();
        FileObject src = d.getFileObject("src");
        URL classes = new URL(d.getURL(), "build/classes/");
        ClassPath sourcePath = ClassPath.getClassPath(src, ClassPath.SOURCE);
        assertEquals(Arrays.asList(src), Arrays.asList(sourcePath.getRoots()));
        assertEquals(Arrays.asList(src), Arrays.asList(SourceForBinaryQuery.findSourceRoots(classes).getRoots()));
        // now add the first gensrc root:
        FileObject stuff = FileUtil.createFolder(d, "build/generated-sources/stuff");
        FileObject xgen = FileUtil.createData(stuff, "net/nowhere/XGen.java");
        assertEquals(Arrays.asList(src, stuff), Arrays.asList(sourcePath.getRoots()));
        assertEquals(ClassPath.getClassPath(src, ClassPath.COMPILE), ClassPath.getClassPath(stuff, ClassPath.COMPILE));
        assertEquals(Arrays.asList(src, stuff), Arrays.asList(SourceForBinaryQuery.findSourceRoots(classes).getRoots()));
        assertEquals(Collections.singletonList(classes), Arrays.asList(BinaryForSourceQuery.findBinaryRoots(stuff.getURL()).getRoots()));
        FileBuiltQuery.Status status = FileBuiltQuery.getStatus(xgen);
        assertNotNull(status);
        assertFalse(status.isBuilt());
        FileUtil.createData(d, "build/classes/net/nowhere/XGen.class");
        assertTrue(status.isBuilt());
    }


    private Project createTestProject(boolean initGenRoot) throws Exception {
        final FileObject dir = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(dir, "src/net/nowhere/X.java",
                "package net.nowhere; public class X {}");
        TestFileUtils.writeFile(dir, "test/net/nowhere/XTest.java",
                "package net.nowhere; public class XTest {}");
        if (initGenRoot) {
            TestFileUtils.writeFile(dir, "build/generated-sources/stuff/net/nowhere/XGen.java",
                    "package net.nowhere; public class XGen {}");
        }
        return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Project>() {
            public Project run() throws Exception {
                AntProjectHelper h = ProjectGenerator.createProject(dir, "test");
                EditableProperties pp = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                pp.setProperty("src.dir", "src");
                pp.setProperty("test.src.dir", "test");
                pp.setProperty("build.dir", "build");
                pp.setProperty("build.classes.dir", "${build.dir}/classes");
                pp.setProperty("build.test.classes.dir", "${build.dir}/test/classes");
                pp.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources");
                pp.setProperty("javac.classpath", "lib.jar");
                pp.setProperty("javac.test.classpath", "${javac.classpath}:junit.jar");
                pp.setProperty("run.classpath", "${javac.classpath}:${build.classes.dir}:runlib.jar");
                pp.setProperty("run.test.classpath", "${javac.test.classpath}:${build.test.classes.dir}:runlib.jar");
                pp.setProperty("dist.dir", "dist");
                pp.setProperty("dist.jar", "${dist.dir}/x.jar");
                pp.setProperty("javac.source", "1.5");
                pp.setProperty("encoding", "ISO-8859-2");
                h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pp);
                Element data = h.getPrimaryConfigurationData(true);
                Document doc = data.getOwnerDocument();
                ((Element) data.appendChild(doc.createElementNS(NS, "source-roots")).
                        appendChild(doc.createElementNS(NS, "root"))).
                        setAttribute("id", "src.dir");
                ((Element) data.appendChild(doc.createElementNS(NS, "test-roots")).
                        appendChild(doc.createElementNS(NS, "root"))).
                        setAttribute("id", "test.src.dir");
                h.putPrimaryConfigurationData(data, true);
                Project p = ProjectManager.getDefault().findProject(dir);
                assertEquals(TestProject.class, p.getClass());
                ProjectManager.getDefault().saveProject(p);
                return p;
            }
        });
    }

    private static final String NS = "urn:test";
    private static class TestAntBasedProjectType implements AntBasedProjectType {
        public String getType() {
            return "test";
        }
        public Project createProject(AntProjectHelper helper) throws IOException {
            return new TestProject(helper);
        }
        public String getPrimaryConfigurationDataElementName(boolean shared) {
            return "data";
        }
        public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
            return NS;
        }
    }
    private static class TestProject implements Project {
        private final AntProjectHelper helper;
        private Lookup lookup;
        TestProject(AntProjectHelper helper) {
            this.helper = helper;
        }
        public FileObject getProjectDirectory() {
            return helper.getProjectDirectory();
        }
        public synchronized Lookup getLookup() {
            if (lookup == null) {
                PropertyEvaluator evaluator = helper.getStandardPropertyEvaluator();
                AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
                ReferenceHelper refHelper = new ReferenceHelper(helper, aux, evaluator);
                UpdateHelper upHelper = new UpdateHelper(new UpdateImplementation() {
                    public boolean isCurrent() {return true;}
                    public boolean canUpdate() {throw new AssertionError();}
                    public void saveUpdate(EditableProperties props) throws IOException {throw new AssertionError();}
                    public Element getUpdatedSharedConfigurationData() {throw new AssertionError();}
                    public EditableProperties getUpdatedProjectProperties() {throw new AssertionError();}
                }, helper);
                SourceRoots src = SourceRoots.create(upHelper, evaluator, refHelper, NS,
                        "source-roots", false, "src.{0}{1}.dir");
                SourceRoots test = SourceRoots.create(upHelper, evaluator, refHelper, NS,
                        "test-roots", false, "test.{0}{1}.dir");
                lookup = Lookups.fixed(
                    aux,
                    new TestSources(this, helper, evaluator, src, test),
                    new ClassPathProviderImpl(this.helper, evaluator, src, test),
                    QuerySupport.createCompiledSourceForBinaryQuery(helper, evaluator, src, test),
                    QuerySupport.createBinaryForSourceQueryImplementation(src, test, helper, evaluator),
                    QuerySupport.createUnitTestForSourceQuery(src, test),
                    QuerySupport.createSourceLevelQuery(evaluator),
                    QuerySupport.createFileBuiltQuery(helper, evaluator, src, test),
                    QuerySupport.createFileEncodingQuery(evaluator, "encoding")
                );
            }
            return lookup;
        }
    }
    /** Simplified copy of J2SESources. */
    private static class TestSources implements Sources, PropertyChangeListener, ChangeListener {
        private final Project project;
        private final AntProjectHelper helper;
        private final PropertyEvaluator evaluator;
        private final SourceRoots sourceRoots;
        private final SourceRoots testRoots;
        private SourcesHelper sourcesHelper;
        private Sources delegate;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        TestSources(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots, SourceRoots testRoots) {
            this.project = project;
            this.helper = helper;
            this.evaluator = evaluator;
            this.evaluator.addPropertyChangeListener(this);
            this.sourceRoots = sourceRoots;
            this.sourceRoots.addPropertyChangeListener(this);
            this.testRoots = testRoots;
            this.testRoots.addPropertyChangeListener(this);
            initSources();
        }
        public SourceGroup[] getSourceGroups(final String type) {
            return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
                public SourceGroup[] run() {
                    Sources _delegate;
                    synchronized (TestSources.this) {
                        if (delegate == null) {
                            delegate = initSources();
                            delegate.addChangeListener(TestSources.this);
                        }
                        _delegate = delegate;
                    }
                    return _delegate.getSourceGroups(type);
                }
            });
        }
        private Sources initSources() {
            sourcesHelper = new SourcesHelper(project, helper, evaluator);
            register(sourceRoots);
            register(testRoots);
            sourcesHelper.addNonSourceRoot("${build.dir}");
            return sourcesHelper.createSources();
        }
        private void register(SourceRoots roots) {
            String[] propNames = roots.getRootProperties();
            String[] rootNames = roots.getRootNames();
            for (int i = 0; i < propNames.length; i++) {
                String prop = propNames[i];
                String displayName = roots.getRootDisplayName(rootNames[i], prop);
                String loc = "${" + prop + "}";
                sourcesHelper.sourceRoot(loc).displayName(displayName).add();
                sourcesHelper.sourceRoot(loc).type(JavaProjectConstants.SOURCES_TYPE_JAVA).displayName(displayName).add();
            }
        }
        public void addChangeListener(ChangeListener changeListener) {
            changeSupport.addChangeListener(changeListener);
        }
        public void removeChangeListener(ChangeListener changeListener) {
            changeSupport.removeChangeListener(changeListener);
        }
        private void fireChange() {
            synchronized (this) {
                if (delegate != null) {
                    delegate.removeChangeListener(this);
                    delegate = null;
                }
            }
            changeSupport.fireChange();
        }
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (SourceRoots.PROP_ROOT_PROPERTIES.equals(propName) || "build.dir".equals(propName)) {
                this.fireChange();
            }
        }
        public void stateChanged(ChangeEvent event) {
            this.fireChange();
        }
    }

    private static class FOComparator implements Comparator<FileObject> {
        @Override
        public int compare(FileObject o1, FileObject o2) {
            return o1.getName().compareTo(o2.getName());
        }

    }

}
