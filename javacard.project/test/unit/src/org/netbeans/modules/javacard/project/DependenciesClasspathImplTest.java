/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.Dependencies;
import org.netbeans.modules.javacard.project.deps.DependenciesResolver;
import org.netbeans.modules.javacard.project.deps.Dependency;
import org.netbeans.modules.javacard.project.deps.DependencyKind;
import org.netbeans.modules.javacard.project.deps.DeploymentStrategy;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.modules.javacard.project.deps.ResolvedDependency;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tim Boudreau
 */
public class DependenciesClasspathImplTest extends AbstractJCProjectTest {
    private JCProject project;
    private ClassPath cp;
    private DependenciesClasspathImpl dci;
    PCL cpListener;
    PCL dcListener;
    private Dependencies deps;
    File jar1;
    File jar2;
    File jar3;
    File jar4;
    File jar1src;

    private static final class PCL implements PropertyChangeListener {
        private final String prop;
        boolean fired;
        public PCL (String prop) {
            this.prop = prop;
            assertNotNull(prop);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
//            System.err.println("property change " + evt.getPropertyName() + " from " + evt.getSource() + " new value " + evt.getNewValue());
            if (prop.equals(evt.getPropertyName())) {
                fired = true;
            }
        }

        public void assertFired() {
            boolean old = fired;
            fired = false;
            assertTrue (old);
        }

        public void assertNotFired() {
            assertFalse (fired);
        }

    }
    public DependenciesClasspathImplTest(String name) {
        super (name);
    }

    @Override
    public void setUp() throws Exception {
        JCProject.LOGGER.setLevel(Level.ALL);
        DependenciesResolver.LOGGER.setLevel(Level.ALL);
        //Defeat pointless JUnit logger cleverness
        super.setUp();
        FileObject fo = FileUtil.getConfigFile("Templates/Project/javacard/capproject.properties");
        project = createProject(fo, "CapProject", ProjectKind.CLASSIC_APPLET, "com.foo.bar.baz", "Cap Project", "Bob");
        cp = project.getLibClassPath();
        cp.addPropertyChangeListener(cpListener = new PCL(ClassPath.PROP_ROOTS));
        dci = new DependenciesClasspathImpl(project);
        dci.addPropertyChangeListener(dcListener = new PCL(ClassPathImplementation.PROP_RESOURCES));
        deps = dci.getDeps();
        //JAR which jar1 depends on via its Class-Path: manifest entry, but which is not directly referenced by any project
        jar2 = createJar("org/netbeans/modules/javacard/project/Something.class", "jar2.jar", "Something.class", null);
        //JAR which the CAP project depends on
        jar1 = createJar("org/netbeans/modules/javacard/project/Nothing.class", "jar1.jar", "Nothing.class", jar2.getName());
        //JAR which the CAP project depends on directly, which does not pull in additional dependencies
        jar3 = createJar("org/netbeans/modules/javacard/project/SomethingElse.class", "jar3.jar", "SomethingElse.class", null);
        //JAR which the lib project depends on, which should appear in the closure of the CAP project's classpath
        jar4 = createJar("org/netbeans/modules/javacard/project/StillSomethingElse.class", "jar4.jar", "StillSomethingElse.class", null);
        jar1src = new File(getWorkDir(), "jar1-sources");
        if (!jar1src.exists()) {
            assertTrue (jar1src.mkdirs());
        }
    }

    @Test
    public void testGetDeps() throws Exception {
        assertEquals (0, cp.getRoots().length);
        assertNotNull (deps);
        Dependency jarDep = new Dependency("jar1", DependencyKind.RAW_JAR, DeploymentStrategy.INCLUDE_IN_PROJECT_CLASSES);
        ResolvedDependencies rd = dci.rd();
        HashMap<ArtifactKind, String> m = new HashMap<ArtifactKind, String>();
        m.put(ArtifactKind.ORIGIN, jar1.getAbsolutePath());
        m.put(ArtifactKind.SOURCES_PATH, jar1src.getAbsolutePath());
        ResolvedDependency add = rd.add(jarDep, m);
        assertNotNull (add);
        final CountDownLatch l = new CountDownLatch(1);
        final AtomicBoolean b = new AtomicBoolean(false);
        ChangeListener cl = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                l.countDown();
                b.set(true);
            }

        };
        project.addDependencyChangeListener(cl);
        rd.save();
        l.await(100000, TimeUnit.MILLISECONDS);
        assertTrue (b.get());
        assertEquals (1, rd.all().size());
        assertEquals (1, project.syncGetDependencies().all().size());
        assertSame (project.getLibClassPath(), cp);
        dcListener.assertFired();
        cpListener.assertFired();
        assertEquals (1, cp.getRoots().length);
        assertEquals (1, dci.getDeps().all().size());
        assertEquals (1, dci.getResources().size());
        DependenciesClasspathImpl nue = new DependenciesClasspathImpl(project);
        List <? extends PathResourceImplementation> ress = nue.getResources();
        assertEquals (1, ress.size());

        ClassPathProvider prov = project.getLookup().lookup(ClassPathProvider.class);
        assertNotNull (prov);
        ClassPath path = prov.findClassPath(project.getProjectDirectory().getFileObject("src"), ClassPath.COMPILE);
        assertEquals (1, path.getRoots().length);
    }


}