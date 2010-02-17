/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
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
 * @author Tim Boudreau
 */
public class JCProjectTest extends AbstractJCProjectTest {
    public JCProjectTest() {
        super ("JCProjectTest");
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
        assertEquals (1, prov.findClassPath(srcDir, ClassPath.COMPILE).getRoots().length);

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

        DependenciesProvider.Receiver r = new DependenciesProvider.Receiver() {

            public void receive(ResolvedDependencies deps) {
                try {
                    assertNotNull(deps);
                    assertEquals(1, deps.all().size());
                    assertEquals("dep", deps.all().get(0).getDependency().getID());
                    assertEquals(new File(fakeLib.getAbsolutePath()).getCanonicalFile(), new File(deps.all().get(0).getPath(ArtifactKind.ORIGIN)).getCanonicalFile());
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
        synchronized (c) {
            c.wait(10000);
        }

        assertNotNull (prov);

        ClassPath path = prov.findClassPath(srcDir, ClassPath.COMPILE);

        FileObject[] roots = path.getRoots();
        assertEquals (2, roots.length);

        FileObject clazz = path.findResource(act);
        assertNotNull (clazz);

        String cp = project.getClasspathClosureAsString();
        assertNotNull (cp);
        assertTrue (cp.length() > 0);
        assertEquals (fakeLib, new File(cp));
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
}