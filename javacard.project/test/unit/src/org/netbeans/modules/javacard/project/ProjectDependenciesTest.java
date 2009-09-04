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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider;
import org.netbeans.modules.javacard.project.deps.Dependency;
import org.netbeans.modules.javacard.project.deps.DependencyKind;
import org.netbeans.modules.javacard.project.deps.DeploymentStrategy;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import static org.junit.Assert.*;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Cancellable;

/**
 *
 * @author Tim Boudreau
 */
public class ProjectDependenciesTest extends AbstractJCProjectTest {
    public ProjectDependenciesTest() {
        super ("Test");
    }

    JCProject project;
    JCProject lib;
    File jar1;
    File jar2;
    File jar3;
    File jar4;
    File jar3src;
    FileObject jar1srcRoot;
    private static int ct;

    @Before
    @Override
    public void setUp() throws Exception {
        //Uniquify names to avoid accidentally trying to modify a deleted project
        //instance from the previous run which is still held by the project
        //manager
        ct++;
        System.err.println("run " + ct);
    /*
    Create a project with the following dependency tree:
    - project
       - jar1
         - jar2
       - jar3
       - lib1
         - jar4
     */
        super.setUp();
        for (int i=0; i < 5; i++) {
            //Try to flush ProjectManager and other caches
            System.gc();
            System.runFinalization();
        }
        FileObject fo = FileUtil.getConfigFile("Templates/Project/javacard/capproject.properties");
        project = createProject(fo, "ClassicAppletProject" + ct, ProjectKind.CLASSIC_APPLET, "com.foo.bar.baz", "Classic Applet Project" + ct, "Bob");
        assertNotNull (project);

        fo = FileUtil.getConfigFile("Templates/Project/javacard/clslibproject.properties");
        //Library project used by the CAP project
        lib = createProject(fo, "LibProject" + ct, ProjectKind.CLASSIC_LIBRARY, "com.foo.baa.bing", "Clslib Project" + ct, "Moo");
        assertNotNull (lib);

        //JAR which jar1 depends on via its Class-Path: manifest entry, but which is not directly referenced by any project
        jar2 = createJar("org/netbeans/modules/javacard/project/Something.class", "jar2.jar", "Something.class", null);
        //JAR which the CAP project depends on
        jar1 = createJar("org/netbeans/modules/javacard/project/Nothing.class", "jar1.jar", "Nothing.class", jar2.getName());
        //JAR which the CAP project depends on directly, which does not pull in additional dependencies
        jar3 = createJar("org/netbeans/modules/javacard/project/SomethingElse.class", "jar3.jar", "SomethingElse.class", null);
        //JAR which the lib project depends on, which should appear in the closure of the CAP project's classpath
        jar4 = createJar("org/netbeans/modules/javacard/project/StillSomethingElse.class", "jar4.jar", "StillSomethingElse.class", null);

        jar3src = createZip ("org/netbeans/modules/javacard/project/SomethingElse.java", SOMETHING_ELSE, "jar3.zip", "SomethingElse.java");
        File jar1src = new File(getWorkDir(), "jar1-sources");
        if (!jar1src.exists()) {
            assertTrue (jar1src.mkdirs());
        }
        jar1srcRoot = FileUtil.toFileObject (jar1src);
        assertNotNull (jar1srcRoot);
        FileObject srcFile = jar1srcRoot.getFileObject("org/netbeans/modules/javacard/project/Nothing.java");
        if (srcFile==null) {
            srcFile = FileUtil.createData(jar1srcRoot, "org/netbeans/modules/javacard/project/Nothing.java");
            OutputStream out = srcFile.getOutputStream();
            InputStream in = new ByteArrayInputStream (NOTHING.getBytes());
            try {
                FileUtil.copy (in, out);
            } finally {
                out.close();
            }
        }

        ResolvedDependencies deps = project.syncGetResolvedDependencies();
        Dependency libDep = new Dependency("lib1", DependencyKind.CLASSIC_LIB, DeploymentStrategy.DEPLOY_TO_CARD);
        Map<ArtifactKind, String> m = new HashMap<ArtifactKind, String>();
        m.put(ArtifactKind.ORIGIN, FileUtil.toFile(lib.getProjectDirectory()).getAbsolutePath());
        deps.add(libDep, m);

        m = new HashMap<ArtifactKind, String>();
        m.put(ArtifactKind.ORIGIN, jar1.getAbsolutePath());
        m.put(ArtifactKind.SOURCES_PATH, jar1src.getAbsolutePath());
        
        Dependency jarDep = new Dependency("jar1", DependencyKind.RAW_JAR, DeploymentStrategy.INCLUDE_IN_PROJECT_CLASSES);
        deps.add(jarDep, m);

        m = new HashMap<ArtifactKind, String>();
        m.put(ArtifactKind.ORIGIN, jar3.getAbsolutePath());
        m.put(ArtifactKind.SOURCES_PATH, jar3src.getAbsolutePath());
        jarDep = new Dependency("jar3", DependencyKind.RAW_JAR, DeploymentStrategy.INCLUDE_IN_PROJECT_CLASSES);
        deps.add(jarDep, m);

        //If you get an ISE here, probably a PropertiesBasedDataObject is writing itself to the
        //system FS.  For some reason this clobbers the declarative ant project type registrations,
        //causing the open projects to be destroyed.
        deps.save();

        deps = lib.syncGetResolvedDependencies();
        Dependency subJarDep = new Dependency("subjar1", DependencyKind.RAW_JAR, DeploymentStrategy.ALREADY_ON_CARD);
        m = new HashMap<ArtifactKind, String>();
        m.put(ArtifactKind.ORIGIN, jar4.getAbsolutePath());
        deps.add (subJarDep, m);
        deps.save();
    }

    private static String NOTHING = "package org.netbeans.modules.javacard.project;\npublic class Nothing {\n    public String foo() {\n        return \"foo\";\n    }\n}\n";
    private static String SOMETHING_ELSE = "package org.netbeans.modules.javacard.project;\npublic class SomethingElse {\n    public String foo() {\n        return \"foo\";\n    }\n}\n";
    
    @Test
    public void testDependencies() throws Exception {
        System.out.println("testDependencies");
        FileObject srcDir = project.getProjectDirectory().getFileObject("src");
        assertNotNull(srcDir);

        ClassPathProvider prov = project.getLookup().lookup(ClassPathProvider.class);
        assertNotNull (prov);
        assertEquals (3, prov.findClassPath(srcDir, ClassPath.COMPILE).getRoots().length);

        DependenciesProvider.Receiver r = new DependenciesProvider.Receiver() {

            public void receive(ResolvedDependencies deps) {
                    assertNotNull(deps);
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

        ClassPath path = prov.findClassPath(srcDir, ClassPath.COMPILE);

        FileObject[] roots = path.getRoots();
        System.err.println("Classpath roots: ");
        for (FileObject fo : roots) {
            System.err.println("   " + fo.getPath());
        }

        String classpathClosure = project.getClasspathClosureAsString();
        String[] components = classpathClosure.split(File.pathSeparator);
        assertEquals (5, components.length);
        String[] expected = new String[] {
            new File(FileUtil.toFile(lib.getProjectDirectory()), "dist" + File.separator + lib.getProjectDirectory().getName() + ".cap").getPath(),
            jar4.getAbsolutePath(),
            jar1.getAbsolutePath(),
            jar2.getAbsolutePath(),
            jar3.getAbsolutePath(),
        };
        assertTrue (Arrays.equals(expected, components));

        System.err.println("Classpath closure: " + project.getClasspathClosureAsString());
        
        System.err.println("Lib classpath closure " + lib.getClasspathClosureAsString());
        assertEquals (jar4.getAbsolutePath(), lib.getClasspathClosureAsString());
    }

    public void testClassPathProvider() throws Exception {
        System.out.println("testClassPathProvider");
        ClassPathProvider prov = project.getLookup().lookup(ClassPathProvider.class);
        ClassPath compileCp = prov.findClassPath(project.getProjectDirectory().getFileObject("src"), ClassPath.COMPILE);
        assertNotNull (compileCp);
        FileObject fo = compileCp.findResource("org/netbeans/modules/javacard/project/Nothing.class");
        assertNotNull (fo);
    }

    public void testSourceForBinaryQueryOverDirectory() throws Exception {
        System.out.println("testSourceForBinaryQueryOverDirectory");
        URL u = FileUtil.getArchiveRoot(jar1.toURI().toURL());
        FileObject archive = URLMapper.findFileObject(u);
        assertNotNull (archive);
        FileObject classFile = archive.getFileObject ("org/netbeans/modules/javacard/project/Nothing.class");
        assertNotNull (classFile);
        URL cfUrl = classFile.getURL();
        assertNotNull (cfUrl);

        SourceForBinaryQueryImplementation q = project.getLookup().lookup(SourceForBinaryQueryImplementation.class);
        Result r = q.findSourceRoots(u);
        assertNotNull (r);
        FileObject[] roots = r.getRoots();
        assertEquals (1, roots.length);
        assertEquals (jar1srcRoot, roots[0]);
    }

    public void testSourceForBinaryQueryOverArchive() throws Exception {
        System.out.println("testSourceForBinaryQueryOverArchive");
        URL u = FileUtil.getArchiveRoot(jar3.toURI().toURL());
        FileObject archive = URLMapper.findFileObject(u);
        assertNotNull (archive);
        FileObject classFile = archive.getFileObject ("org/netbeans/modules/javacard/project/SomethingElse.class");
        assertNotNull (classFile);
        URL cfUrl = classFile.getURL();
        assertNotNull (cfUrl);

        SourceForBinaryQueryImplementation q = project.getLookup().lookup(SourceForBinaryQueryImplementation.class);
        Result r = q.findSourceRoots(u);
        assertNotNull (r);
        FileObject[] roots = r.getRoots();
        assertEquals (1, roots.length);
        assertEquals (FileUtil.toFileObject(FileUtil.normalizeFile(jar3src)), roots[0]);
    }
}
