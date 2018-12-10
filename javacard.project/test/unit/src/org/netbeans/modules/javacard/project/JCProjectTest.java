/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.swing.table.DefaultTableModel;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.project.customizer.ClassicAppletProjectProperties;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider;
import org.netbeans.modules.javacard.project.deps.Dependency;
import org.netbeans.modules.javacard.project.deps.DependencyKind;
import org.netbeans.modules.javacard.project.deps.DeploymentStrategy;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import static org.junit.Assert.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;

/**
 *
 */
public class JCProjectTest extends AbstractJCProjectTest {
    public static final String SYSTEM_PROP_UNIT_TEST = "JCProject.test";
    public JCProjectTest() {
        super ("JCProjectTest");
        System.setProperty (SYSTEM_PROP_UNIT_TEST, "true");
    }

    JCProject project;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        FileObject fo = FileUtil.getConfigFile("Templates/Project/javacard/capproject.properties");
        project = createProject(fo, "CapProject", ProjectKind.CLASSIC_APPLET, "com.foo.bar.baz", "Cap Project", "Bob");
    }

    @Test
    public void testPropertyEvaluator() {
        assertSame (ProjectKind.CLASSIC_APPLET, project.getLookup().lookup(ProjectKind.class));
        assertTrue (project.isBadPlatformOrCard());

        LogicalViewProvider prov = project.getLookup().lookup (LogicalViewProvider.class);
        assertNotNull (prov);
        assertTrue (prov.createLogicalView().getHtmlDisplayName() != null);

        EditableProperties props =
                project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        PropertyEvaluator eval = project.evaluator();

        String defaultName = JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME;

        assertEquals (defaultName,
                props.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM));

        String activePlatformFromProps = props.getProperty (ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        String fromEvaluator = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        assertEquals (activePlatformFromProps, fromEvaluator);

        assertEquals (props.getProperty (ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE),
                eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE));
    }

    public void testClasspathIdentityDoesNotChange() throws Exception {
        ClassPathProvider prov = project.getLookup().lookup(ClassPathProvider.class);
        ClassPath pth = prov.findClassPath(null, ClassPath.SOURCE);
        assertNotNull (pth);
        assertEquals(pth, prov.findClassPath(null, ClassPath.SOURCE));
        assertSame ("Classpath identity changed w/o reason",pth, prov.findClassPath(null, ClassPath.SOURCE));

        pth = prov.findClassPath(null, ClassPath.BOOT);
        assertNotNull (pth);
        assertEquals(pth, prov.findClassPath(null, ClassPath.BOOT));
        assertSame ("Classpath identity changed w/o reason",pth, prov.findClassPath(null, ClassPath.BOOT));

        pth = prov.findClassPath(null, ClassPath.COMPILE);
        assertNotNull (pth);
        assertEquals(pth, prov.findClassPath(null, ClassPath.COMPILE));
        assertSame ("Classpath identity changed w/o reason",pth, prov.findClassPath(null, ClassPath.COMPILE));

        pth = prov.findClassPath(null, ClassPath.EXECUTE);
        assertNotNull (pth);
        assertEquals(pth, prov.findClassPath(null, ClassPath.EXECUTE));
        assertSame ("Classpath identity changed w/o reason",pth, prov.findClassPath(null, ClassPath.EXECUTE));
    }

    public void testPropertyEvaluatorSanity() {
        FileObject fo = project.getProjectDirectory().getFileObject("nbproject/project.properties");
        File f = FileUtil.toFile (fo);
        PropertyProvider prov = PropertyUtils.propertiesFilePropertyProvider(f);
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(
                project.getAntProjectHelper().getStockPropertyPreprovider(),
                PropertyUtils.globalPropertyProvider(), prov);
        String ap = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        assertEquals ("javacard_default", ap);
    }

    public void testDependencies() throws Exception {
        FileObject srcDir = project.getProjectDirectory().getFileObject("src");
        assertNotNull(srcDir);
        ClassPathProvider prov = project.getLookup().lookup(ClassPathProvider.class);

        assertEquals (1, prov.findClassPath(srcDir, ClassPath.SOURCE).getRoots().length);

        assertEquals (0, prov.findClassPath(srcDir, ClassPath.COMPILE).getRoots().length);

        ResolvedDependencies deps = project.syncGetResolvedDependencies();
        assertTrue (deps.all().isEmpty());
        String act = "org/netbeans/modules/javacard/project/Nothing.class";
        final File fakeLib = createJar (act, "fakeLib.jar", "Nothing.class", null);
        Map<ArtifactKind,String> m = new HashMap <ArtifactKind, String>();
        Dependency dep = new Dependency ("dep", DependencyKind.RAW_JAR, DeploymentStrategy.ALREADY_ON_CARD);
        m.put (ArtifactKind.ORIGIN, fakeLib.getAbsolutePath());
        deps.add(dep, m);
        assertEquals (1, deps.all().size());
        deps.save();

        //Now force a re-read of the data
        final CountDownLatch latch = new CountDownLatch(1);

        DependenciesProvider.Receiver r = new DependenciesProvider.Receiver() {

            public void receive(ResolvedDependencies deps) {
                try {
                    assertNotNull(deps);
                    assertEquals(1, deps.all().size());
                    assertEquals("dep", deps.all().get(0).getDependency().getID());
                    assertEquals(new File(fakeLib.getAbsolutePath()).getCanonicalFile(), new File(deps.all().get(0).getPath(ArtifactKind.ORIGIN)).getCanonicalFile());
                    latch.countDown();
                } catch (IOException ex) {
                    throw new IllegalStateException (ex);
                }
            }

            public boolean failed(Throwable failure) {
                throw new IllegalStateException(failure);
            }
        };
        DependenciesProvider p = project.getLookup().lookup(DependenciesProvider.class);
        assertNotNull (p);
        Cancellable c = p.requestDependencies(r);
        assertNotNull(c);
        latch.await();

        assertNotNull (prov);

        ClassPath path = prov.findClassPath(srcDir, ClassPath.COMPILE);

        FileObject[] roots = path.getRoots();
        assertEquals (1, roots.length);

        FileObject clazz = path.findResource(act);
        assertNotNull (clazz);

        String cp = project.getClasspathClosureAsString();
        assertNotNull (cp);
        assertTrue (cp.length() > 0);
        assertEquals (fakeLib, new File(cp));
        assertEquals (1, prov.findClassPath(srcDir, ClassPath.COMPILE).getRoots().length);
    }

    public void testAntArtifactProvider() throws Exception {
        AntArtifactProvider prov = project.getLookup().lookup(AntArtifactProvider.class);
        assertNotNull (prov);
        boolean jarFound = false;
        for (AntArtifact a : prov.getBuildArtifacts()) {
            if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(a.getType())) {
                jarFound = true;
                URI[] uris = a.getArtifactLocations();
                assertNotNull (uris);
                assertEquals (1, uris.length);
                File f = new File (uris[0]);
                File f2 = new File (FileUtil.toFile (project.getProjectDirectory()), "dist" + File.separatorChar + "CapProject.cap");
                assertEquals (f, f2);
            }
        }
        assertTrue (jarFound);
    }

    public void testUseProxiesCreatesProxiesFolderAndSetsUpProperties() {
        JCCustomizerProvider prov = project.getLookup().lookup(JCCustomizerProvider.class);
        assertNotNull (prov);
        JCProjectProperties props = JCCustomizerProvider.createProjectProperties(project.kind(), project, project.evaluator(), project.getAntProjectHelper());
        assertNotNull (props);
        assertTrue (props instanceof ClassicAppletProjectProperties);
        ClassicAppletProjectProperties p = (ClassicAppletProjectProperties) props;
        assertNull ("Newly create classic project should not have a proxy source folder",
                project.getProjectDirectory().getFileObject(ClassicAppletProjectProperties.PROXY_SOURCE_DIR));
        EditableProperties pr = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull (pr);
        String s = pr.getProperty(ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES);
        assertTrue ("Newly created classic project's " + 
                ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES + 
                " property should be false or null",  s == null || Boolean.valueOf(s) == false);
        p.setUseMyProxies(true);
        assertTrue (p.isUseMyProxies());
        p.storeProperties();
        pr = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull (pr);
        s = pr.getProperty(ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES);
        assertFalse ("After setting useMyProxies, " +
                ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES +
                " should be true", s == null || Boolean.valueOf(s) == false);
        assertNotNull ("Proxy source dir not created", project.getProjectDirectory().getFileObject (ClassicAppletProjectProperties.PROXY_SOURCE_DIR));
        p = (ClassicAppletProjectProperties) JCCustomizerProvider.createProjectProperties(project.kind(), project, project.evaluator(), project.getAntProjectHelper());
        p.setUseMyProxies(false);
        assertFalse (p.isUseMyProxies());
        p.storeProperties();
        p = (ClassicAppletProjectProperties) JCCustomizerProvider.createProjectProperties(project.kind(), project, project.evaluator(), project.getAntProjectHelper());
        pr = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertNotNull (pr);
        s = pr.getProperty(ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES);
        assertTrue ("After save, " +ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES+ " should be unset: " + s, s == null || Boolean.valueOf(s) == false);
        assertNotNull ("Setting use my proxies to false should not cause proxy source dir to be deleted",
                project.getProjectDirectory().getFileObject(ClassicAppletProjectProperties.PROXY_SOURCE_DIR));

    }

    public void testStorePropertiesDoesNotRewriteProxiesIfNoChangeToUseMyProperties() throws Exception {
        JCCustomizerProvider prov = project.getLookup().lookup(JCCustomizerProvider.class);
        assertNotNull (prov);
        JCProjectProperties props = JCCustomizerProvider.createProjectProperties(project.kind(), project, project.evaluator(), project.getAntProjectHelper());
        assertNotNull (props);
        assertTrue (props instanceof ClassicAppletProjectProperties);
        ClassicAppletProjectProperties p = (ClassicAppletProjectProperties) props;
        p.setUseMyProxies(true);
        assertTrue (p.isUseMyProxies());
        p.setUseMyProxies(false);
        p.storeProperties();
        EditableProperties pr = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        FileObject fo = project.getProjectDirectory().getFileObject("proxies");
        assertNull (fo);
        assertEquals ("${use.my.proxies}", project.evaluator().evaluate("${use.my.proxies}"));
        assertFalse(pr.containsKey("use.my.proxies"));

        props = JCCustomizerProvider.createProjectProperties(project.kind(), project, project.evaluator(), project.getAntProjectHelper());
        p.setPlatformName("junk");
        p.storeProperties();
        pr = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        fo = project.getProjectDirectory().getFileObject("proxies");
        assertNull (fo);
        assertEquals ("${use.my.proxies}", project.evaluator().evaluate("${use.my.proxies}"));
        assertFalse(pr.containsKey("use.my.proxies"));
    }

    public void testSourceClasspathFiresChanges() throws Exception {
        FileObject newRoot = project.getProjectDirectory().createFolder ("stuff");
        File newRootFile = FileUtil.toFile(newRoot);
        assertNotNull (newRootFile);
        JCProjectProperties props = JCCustomizerProvider.createProjectProperties(project.kind(), project, project.evaluator(), project.getAntProjectHelper());
        ClassPath path = project.getSourceClassPath();
        assertNotNull (path);
        class PCL implements PropertyChangeListener {
            boolean changed;
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (ClassPath.PROP_ROOTS.equals (evt.getPropertyName())) {
                    changed = true;
                }
            }

            void assertChanged () {
                boolean old = changed;
                changed = false;
                assertTrue ("Source classpath should have fired a change", old);
            }

            void assertNotChanged() {
                assertFalse ("Unexpected change fired", changed);
            }
        }
        PCL pcl = new PCL();
        path.addPropertyChangeListener(pcl);
        pcl.assertNotChanged();

        DefaultTableModel srcTable = props.SOURCE_ROOTS_MODEL;
        srcTable.addRow(new Object[] { newRootFile, "Stuff" });
        props.storeProperties();
        assertSame ("Classpath identity should not change", path,
                project.getSourceClassPath());

        pcl.assertChanged();

        props = JCCustomizerProvider.createProjectProperties(project.kind(), project, project.evaluator(), project.getAntProjectHelper());
        srcTable = props.SOURCE_ROOTS_MODEL;
        assertEquals (2, srcTable.getRowCount());

    }
}