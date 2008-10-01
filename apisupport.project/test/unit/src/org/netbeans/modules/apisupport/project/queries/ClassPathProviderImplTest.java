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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

// XXX test GPR usage

/**
 * Test functionality of {@link ClassPathProviderImpl}.
 * @author Jesse Glick
 */
public class ClassPathProviderImplTest extends TestBase {
    
    public ClassPathProviderImplTest(String name) {
        super(name);
    }
    
    private File copyOfSuite2;
    private FileObject copyOfMiscDir;
    private NbModuleProject copyOfMiscProject;
    private ProjectXMLManager copyOfMiscXMLManager;
    
    private static final Logger LOG = Logger.getLogger(ClassPathProviderImplTest.class.getName());
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        copyOfSuite2 = copyFolder(resolveEEPFile("/suite2"));
        File miscF = new File(copyOfSuite2, "misc-project");
        copyOfMiscDir = FileUtil.toFileObject(miscF);
        copyOfMiscProject = (NbModuleProject) ProjectManager.getDefault().findProject(copyOfMiscDir);
        assertNotNull(copyOfMiscProject);
        copyOfMiscXMLManager = new ProjectXMLManager(copyOfMiscProject);
        // make sure its platform-private.properties is correct:
        Project copyOfSuite2P = ProjectManager.getDefault().findProject(FileUtil.toFileObject(copyOfSuite2));
        ((SuiteProject) copyOfSuite2P).open();
    }
    
    private String urlForJar(String path) {
        return FileUtil.urlForArchiveOrDir(PropertyUtils.resolveFile(nbRootFile(), path)).toExternalForm();
    }
    
    private String urlForDir(String path) {
        return FileUtil.urlForArchiveOrDir(file(path)).toExternalForm();
    }
    
    private Set<String> urlsOfCp(ClassPath cp) {
        Set<String> s = new TreeSet<String>();
        for (ClassPath.Entry entry : cp.entries()) {
            s.add(entry.getURL().toExternalForm());
        }
        return s;
    }
    
    /**
     * Asserts when (sorted) sequences are not equal
     * and prints CP entries.
     */
    private <T> void assertEquals(String msg, Iterable<T> expSeq, Iterable<T> actSeq) {
        int i = 0, cnt = 0;
        boolean dif = false;
        Iterator<T> it1 = expSeq.iterator(), it2 = actSeq.iterator();
        StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder();
        
        while (it1.hasNext() || it2.hasNext()) {
            T t1 = null, t2 = null;
            if (it1.hasNext()) {
                t1 = it1.next();
                sb1.append("\t" + t1.toString() + "\n");
            } else {
                dif = true;
            }
            if (it2.hasNext()) {
                t2 = it2.next();
                sb2.append("\t" + t2.toString() + "\n");
            } else {
                dif = true;
            }
            if (!dif && !t1.equals(t2)) {
                dif = true;
            }
            if (! dif) i++;
            cnt++;
        }
        if (!dif) {
            return;
        }
        String failMsg = String.format("Comparison failed: %1$s\nClasspaths differ at entry #%4$d.\nExpected:\n%2$sActual:\n%3$s",
                msg, sb1, sb2, i);
        fail(failMsg);
    }
    
    private static final Set<String> TESTLIBS = new HashSet<String>(Arrays.asList(
        "junit.jar", "org-netbeans-modules-nbjunit.jar", "org-netbeans-insane.jar"));
    
    private Set<String> urlsOfCp4Tests(ClassPath cp) {
        Set<String> s = new TreeSet<String>();
        for (ClassPath.Entry entry : cp.entries()) {
            String url = entry.getURL().toExternalForm();
            if (url.indexOf("$%7B") != -1) {
                // Unevaluated Ant reference (after octet escaping), so skip.
                continue;
            }
            String simplifiedJarName = url.replaceFirst("^.+/([^/]+?)[0-9_.-]*\\.jar!/$", "$1.jar");
            if (TESTLIBS.contains(simplifiedJarName)) {
                s.add(simplifiedJarName);
            } else {
                s.add(url);
            }
        }
        return s;
    }

//    XXX: failing test, fix or delete
//    public void testMainClasspath() throws Exception {
//        FileObject src = nbRoot().getFileObject("o.apache.tools.ant.module/src");
//        assertNotNull("have o.apache.tools.ant.module/src", src);
//        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
//        assertNotNull("have a COMPILE classpath", cp);
//        Set<String> expectedRoots = new TreeSet<String>();
//        // Keep up to date w/ changes in o.apache.tools.ant.module/nbproject/project.{xml,properties}:
//        // ${module.classpath}:
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-xml.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-execution.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-explorer.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-spi-navigator.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-jdesktop-layout.jar"));
//        expectedRoots.add(urlForJar("o.jdesktop.layout/external/swing-layout-1.0.3.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-options-api.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectapi.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectuiapi.jar"));
//        assertEquals("right COMPILE classpath for o.apache.tools.ant.module/src", expectedRoots, urlsOfCp(cp));
//        cp = ClassPath.getClassPath(src, ClassPath.EXECUTE);
//        assertNotNull("have an EXECUTE classpath", cp);
//        // #48099: need to include build/classes here too
//        expectedRoots.add(urlForDir("o.apache.tools.ant.module/build/classes"));
//        // And #70206: transitive runtime deps too.
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-queries.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-editor-mimelookup.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-swing-outline.jar"));
//        assertEquals("right EXECUTE classpath (COMPILE plus classes)", expectedRoots, urlsOfCp(cp));
//        cp = ClassPath.getClassPath(src, ClassPath.SOURCE);
//        assertNotNull("have a SOURCE classpath", cp);
//        assertEquals("right SOURCE classpath", Collections.singleton(src), new HashSet<FileObject>(Arrays.asList(cp.getRoots())));
//        // XXX test BOOT
//    }

    public void testMainClasspathExternalModules() throws Exception {
        FileObject src = resolveEEP("suite3/dummy-project/src");
        assertNotNull("have .../dummy-project/src", src);
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.add(urlForJar(resolveEEPPath("/suite3/nbplatform/random/modules/random.jar")));
        expectedRoots.add(urlForJar(resolveEEPPath("/suite3/nbplatform/random/modules/ext/stuff.jar")));
        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp(cp));
    }
    
    /**
     * #52354: interpret <class-path-extension>s both in myself and in dependent modules.
     */
    /* XXX uses obsolete module:
    public void testClasspathExtensions() throws Exception {
        // java/javacore has its own <class-path-extension> and uses others from dependents.
        FileObject src = nbCVSRoot().getFileObject("java/javacore/src");
        assertNotNull("have java/javacore/src", src);
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set<String> expectedRoots = new TreeSet<String>();
        // Keep up to date w/ changes in java/javacore/nbproject/project.xml:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-java.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-classfile.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/extra/modules/org-netbeans-jmi-javamodel.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/javax-jmi-reflect.jar"));
        expectedRoots.add(urlForJar("mdr/external/jmi.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/javax-jmi-model.jar"));
        expectedRoots.add(urlForJar("mdr/external/mof.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-mdr.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectapi.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-mdr.jar"));
        expectedRoots.add(urlForJar("mdr/dist/mdr.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-jmiutils.jar"));
        expectedRoots.add(urlForJar("mdr/jmiutils/dist/jmiutils.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        expectedRoots.add(urlForJar("java/parser/dist/java-parser.jar"));
        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp(cp));
        assertTrue("#76341: EXECUTE classpath also has extension",
                urlsOfCp(ClassPath.getClassPath(src, ClassPath.EXECUTE)).contains(urlForJar("java/parser/dist/java-parser.jar")));
    }
     */

    /* XXX would need to include ${ant.core.lib}, never mind:
    public void testExtraCompilationUnits() throws Exception {
        FileObject srcbridge = nbRoot().getFileObject("o.apache.tools.ant.module/src-bridge");
        assertNotNull("have o.apache.tools.ant.module/src-bridge", srcbridge);
        ClassPath cp = ClassPath.getClassPath(srcbridge, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set<String> expectedRoots = new TreeSet<String>();
        // Keep up to date w/ changes in o.apache.tools.ant.module/nbproject/project.{xml,properties}:
        expectedRoots.add(urlForDir("o.apache.tools.ant.module/build/classes"));
        expectedRoots.add(urlForJar("o.apache.tools.ant.module/external/lib/ant.jar"));
        // ${module.classpath}:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-xml.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-execution.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-explorer.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-spi-navigator.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-jdesktop-layout.jar"));
        expectedRoots.add(urlForJar("o.jdesktop.layout/external/swing-layout-1.0.3.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-options-api.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectapi.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectuiapi.jar"));
        assertEquals("right COMPILE classpath for o.apache.tools.ant.module/src-bridge", expectedRoots, urlsOfCp(cp));
        cp = ClassPath.getClassPath(srcbridge, ClassPath.EXECUTE);
        assertNotNull("have an EXECUTE classpath", cp);
        expectedRoots.add(urlForDir("o.apache.tools.ant.module/build/bridge-classes"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/ant/nblib/bridge.jar"));
        assertEquals("right EXECUTE classpath (COMPILE plus classes plus JAR)", expectedRoots, urlsOfCp(cp));
        cp = ClassPath.getClassPath(srcbridge, ClassPath.SOURCE);
        assertNotNull("have a SOURCE classpath", cp);
        assertEquals("right SOURCE classpath", Collections.singleton(srcbridge), new HashSet<FileObject>(Arrays.asList(cp.getRoots())));
        // XXX test BOOT
    }
     */

    /* XXX failing, should be rewritten to use generated module:
    public void testUnitTestClasspaths() throws Exception {
        FileObject src = nbCVSRoot().getFileObject("autoupdate/test/unit/src");
        assertNotNull("have autoupdate/test/unit/src", src);
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set<String> expectedRoots = new TreeSet<String>();
        // Keep up to date w/ changes in autoupdate/nbproject/project.{xml,properties}:
        // module.classpath:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/boot.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-explorer.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-options.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-core.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/core.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-editor-mimelookup.jar"));
        // cp.extra:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/ext/updater.jar"));
        // module JAR:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-autoupdate.jar"));
        expectedRoots.add("junit.jar");
        expectedRoots.add("nbjunit.jar");
        expectedRoots.add("nbjunit-ide.jar");
        expectedRoots.add("insanelib.jar");
        // recursive dependencies
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-swing-plaf.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        
        assertSets("right COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
        
        cp = ClassPath.getClassPath(src, ClassPath.EXECUTE);
        assertNotNull("have an EXECUTE classpath", cp);
        expectedRoots.add(urlForDir("autoupdate/build/test/unit/classes"));
        // test.unit.run.cp.extra:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/boot.jar"));
        assertSets("right EXECUTE classpath (COMPILE plus classes)", expectedRoots, urlsOfCp4Tests(cp));
        cp = ClassPath.getClassPath(src, ClassPath.SOURCE);
        assertNotNull("have a SOURCE classpath", cp);
        assertEquals("right SOURCE classpath", Collections.singleton(src), new HashSet(Arrays.asList(cp.getRoots())));
        // XXX test BOOT
    }
     */

//    XXX: failing test, fix or delete
//    public void testUnitTestClasspathsExternalModules() throws Exception {
//        FileObject src = resolveEEP("suite1/support/lib-project/test/unit/src");
//        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
//        assertNotNull("have a COMPILE classpath", cp);
//        Set<String> expectedRoots = new TreeSet<String>();
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite1/build/cluster/modules/org-netbeans-examples-modules-lib.jar")));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-libs-junit4.jar"));
//        expectedRoots.add("junit.jar");
//        expectedRoots.add("org-netbeans-insane.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
//        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
//        // Now test in suite3, where there is no source...
//        src = resolveEEP("suite3/dummy-project/test/unit/src");
//        cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
//        assertNotNull("have a COMPILE classpath", cp);
//        expectedRoots = new TreeSet<String>();
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite3/dummy-project/build/cluster/modules/org-netbeans-examples-modules-dummy.jar")));
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite3/nbplatform/random/modules/random.jar")));
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite3/nbplatform/random/modules/ext/stuff.jar")));
//        expectedRoots.add("junit.jar");
//        expectedRoots.add("nbjunit.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
//        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
//
//        // test dependencies
//        expectedRoots.clear();
//        src  = resolveEEP("/suite4/module2/test/unit/src");
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite4/build/testdist/unit/cluster/module1/tests.jar")));
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite4/build/cluster/modules/module1.jar")));
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite4/build/cluster/modules/module2.jar")));
//        expectedRoots.add("junit.jar");
//        expectedRoots.add("nbjunit.jar");
//        expectedRoots.add("insanelib.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
//        cp = ClassPath.getClassPath(src,ClassPath.COMPILE);
//        FileObject roots[] = cp.getRoots();
//        assertEquals("right compileclasspath", expectedRoots, urlsOfCp4Tests(cp));
//    }
    
    /* XXX failing, should be rewritten to use generated module:
    public void testQaFunctionalTestClasspath() throws Exception {
        FileObject qaftsrc = nbCVSRoot().getFileObject("performance/test/qa-functional/src");
        if (qaftsrc == null) {
            System.err.println("Skipping testQaFunctionalTestClasspath since performance not checked out");
            return;
        }
        ClassPath cp = ClassPath.getClassPath(qaftsrc, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set<String> expectedRoots = new TreeSet<String>();
        // Keep up to date w/ changes in /space/src/nb_all/performance/nbproject/project.properties
        // & nbbuild/templates/xtest-qa-functional.xml:
        expectedRoots.add("junit.jar");
        expectedRoots.add("nbjunit.jar");
        expectedRoots.add("nbjunit-ide.jar");
        expectedRoots.add("insanelib.jar");
        // jemmy.and.jelly.path:
        expectedRoots.add(urlForJar("jemmy/builds/jemmy.jar"));
        expectedRoots.add(urlForJar("jellytools/builds/jelly2-nb.jar"));
        // test.qa-functional.cp.extra currently empty
        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
        cp = ClassPath.getClassPath(qaftsrc, ClassPath.EXECUTE);
        assertNotNull("have an EXECUTE classpath", cp);
        expectedRoots.add(urlForDir("performance/build/test/qa-functional/classes"));
        assertEquals("right EXECUTE classpath (COMPILE plus classes)", expectedRoots, urlsOfCp4Tests(cp));
        cp = ClassPath.getClassPath(qaftsrc, ClassPath.SOURCE);
        assertNotNull("have a SOURCE classpath", cp);
        assertEquals("right SOURCE classpath", Collections.singleton(qaftsrc), new HashSet<FileObject>(Arrays.asList(cp.getRoots())));
        // XXX test BOOT
    }
     */

//    XXX: failing test, fix or delete
//    public void testQaFunctionalTestClasspathExternalModules() throws Exception {
//        FileObject qaftsrc = resolveEEP("suite1/action-project/test/qa-functional/src");
//        assertNotNull("have action-project/test/qa-functional/src", qaftsrc);
//        ClassPath cp = ClassPath.getClassPath(qaftsrc, ClassPath.COMPILE);
//        assertNotNull("have a COMPILE classpath", cp);
//        Set<String> expectedRoots = new TreeSet<String>();
//        expectedRoots.add("junit.jar");
//        expectedRoots.add("nbjunit.jar");
//        expectedRoots.add("nbjunit-ide.jar");
//        expectedRoots.add("insanelib.jar");
//        expectedRoots.add(urlForJar("jemmy/builds/jemmy.jar"));
//        expectedRoots.add(urlForJar("jellytools/builds/jelly2-nb.jar"));
//        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
//        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
//        TestBase.TestPCL l = new TestBase.TestPCL();
//        cp.addPropertyChangeListener(l);
//        NbModuleProject p = (NbModuleProject) FileOwnerQuery.getOwner(qaftsrc);
//        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
//        File added = file("xtest/lib/xtest.jar");
//        ep.setProperty("test.qa-functional.cp.extra", added.getAbsolutePath());
//        p.getHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
//        // do not save! just want to test change, not actually make it...
//        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
//        expectedRoots.add(urlForJar("xtest/lib/xtest.jar"));
//        assertEquals("right COMPILE classpath after added to .extra", expectedRoots, urlsOfCp4Tests(cp));
//    }

    /* XXX failing, but what was it supposed to be testing? I cannot decipher this. -jglick
    public void testBuildClassPath () throws Exception {
        FileObject srcRoot = nbCVSRoot().getFileObject("ant/project/src/");
        assertNotNull("have ant/project/src",srcRoot);
        ClassPath ccp = ClassPath.getClassPath(srcRoot, ClassPath.COMPILE);
        assertNotNull("No compile ClassPath for sources",ccp);
        FileObject  buildClasses = nbCVSRoot().getFileObject("ant/project/build/classes/");
        assertNotNull("have ant/project/build/classes",buildClasses);
                
        assertNull ("ClassPath.SOURCE for build must be null",ClassPath.getClassPath(buildClasses, ClassPath.SOURCE));
        assertNull ("ClassPath.COMPILE for build must be null",ClassPath.getClassPath(buildClasses, ClassPath.COMPILE));
        ClassPath cp = ClassPath.getClassPath(buildClasses, ClassPath.EXECUTE);
        assertNotNull("ClassPath.EXECUTE for build must NOT be null",cp);
        ClassPath expectedCp = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                ClassPathSupport.createClassPath(new FileObject[] {buildClasses}),
                ccp
        });
        assertClassPathsHaveTheSameResources(cp, expectedCp);
        
        FileObject testSrcRoot = nbCVSRoot().getFileObject("ant/project/test/unit/src/");
        assertNotNull("have ant/project/test/unit/src/",testSrcRoot);
        ClassPath tccp = ClassPath.getClassPath(testSrcRoot, ClassPath.COMPILE);
        assertNotNull("No compile ClassPath for tests",tccp);
        Project prj = FileOwnerQuery.getOwner(testSrcRoot);
        assertNotNull("No project found",prj);
        assertTrue("Invalid project type", prj instanceof NbModuleProject);
        FileObject testBuildClasses = nbCVSRoot().getFileObject ("ant/project/build/test/unit/classes/");
        if (testBuildClasses == null) {
            // Have to have it, so we can call CP.gCP on it:
            testBuildClasses = FileUtil.createFolder(nbCVSRoot(), "ant/project/build/test/unit/classes");
        }
        assertNull ("ClassPath.SOURCE for build/test must be null",ClassPath.getClassPath(testBuildClasses, ClassPath.SOURCE));
        assertNull ("ClassPath.COMPILE for build/test must be null",ClassPath.getClassPath(testBuildClasses, ClassPath.COMPILE));
        cp = ClassPath.getClassPath(testBuildClasses, ClassPath.EXECUTE);
        
        String path = ((NbModuleProject)prj).evaluator().getProperty("test.unit.run.cp.extra");     //NOI18N
        List<PathResourceImplementation> trExtra = new ArrayList<PathResourceImplementation>();
        if (path != null) {
            String[] pieces = PropertyUtils.tokenizePath(path);
            for (int i = 0; i < pieces.length; i++) {
                File f = ((NbModuleProject)prj).getHelper().resolveFile(pieces[i]);
                URL url = f.toURI().toURL();
                if (FileUtil.isArchiveFile(url)) {
                    url = FileUtil.getArchiveRoot (url);
                }
                else {
                    String stringifiedURL = url.toString ();
                    if (!stringifiedURL.endsWith("/")) {        //NOI18N
                        url = new URL (stringifiedURL+"/");     //NOI18N
                    }
                }
                trExtra.add(ClassPathSupport.createResource(url));
            }
        }        
        assertNotNull("ClassPath.EXECUTE for build/test must NOT be null", cp);
        expectedCp = ClassPathSupport.createProxyClassPath(
                ClassPathSupport.createClassPath(testBuildClasses),
                tccp,
                ClassPathSupport.createClassPath(trExtra),
                ClassPathSupport.createClassPath(new URL(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-masterfs.jar"))));
        assertClassPathsHaveTheSameResources(expectedCp, cp);

        File jarFile = ((NbModuleProject) prj).getModuleJarLocation();
        FileObject jarFO = FileUtil.toFileObject(jarFile);
        assertNotNull("No module jar", jarFO);
        FileObject jarRoot = FileUtil.getArchiveRoot(jarFO);
//        assertEquals(prj, FileOwnerQuery.getOwner(jarRoot));
        assertNull("ClassPath.SOURCE for module jar must be null", ClassPath.getClassPath(jarRoot, ClassPath.SOURCE));
        assertNull("ClassPath.COMPILE for module jar must be null", ClassPath.getClassPath(jarRoot, ClassPath.COMPILE));
        cp = ClassPath.getClassPath(jarRoot, ClassPath.EXECUTE);
        assertNotNull("ClassPath.EXECUTE for module jar must NOT be null", cp);
        expectedCp = ClassPathSupport.createProxyClassPath(ClassPathSupport.createClassPath(jarRoot), ccp);
        assertClassPathsHaveTheSameResources(expectedCp, cp);
    }
     */
    
    public void testCompileClasspathChanges() throws Exception {
        ClassPath cp = ClassPath.getClassPath(copyOfMiscDir.getFileObject("src"), ClassPath.COMPILE);
        Set<String> expectedRoots = new TreeSet<String>();
        assertEquals("right initial COMPILE classpath", expectedRoots, urlsOfCp(cp));
        TestBase.TestPCL l = new TestBase.TestPCL();
        cp.addPropertyChangeListener(l);
        ModuleEntry ioEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.io");
        assertNotNull(ioEntry);
        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(ioEntry)));
        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
        l.changed.clear();
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        assertEquals("right COMPILE classpath after changing project.xml", expectedRoots, urlsOfCp(cp));
        ModuleEntry utilEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.util");
        assertNotNull(utilEntry);
        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(utilEntry)));
        assertTrue("got changes again", l.changed.contains(ClassPath.PROP_ROOTS));
        l.changed.clear();
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        assertEquals("right COMPILE classpath after changing project.xml again", expectedRoots, urlsOfCp(cp));
    }
    
    public void testExecuteClasspathChanges() throws Exception {
        ClassPath cp = ClassPath.getClassPath(copyOfMiscDir.getFileObject("src"), ClassPath.EXECUTE);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.add(FileUtil.urlForArchiveOrDir(file(FileUtil.toFile(copyOfMiscDir), "build/classes")).toExternalForm());
        assertEquals("right initial EXECUTE classpath", expectedRoots, urlsOfCp(cp));
        TestBase.TestPCL l = new TestBase.TestPCL();
        cp.addPropertyChangeListener(l);
        ModuleEntry ioEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.io");
        assertNotNull(ioEntry);
        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(ioEntry)));
        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        // #70206: transitive deps added too:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        assertEquals("right EXECUTE classpath after changing project.xml", expectedRoots, urlsOfCp(cp));
    }

//    XXX: failing test, fix or delete
//    public void testUnitTestCompileClasspathChanges() throws Exception {
//        ClassPath cp = ClassPath.getClassPath(copyOfMiscDir.getFileObject("test/unit/src"), ClassPath.COMPILE);
//        Set<String> expectedRoots = new TreeSet<String>();
//        expectedRoots.add(FileUtil.urlForArchiveOrDir(file(copyOfSuite2, "build/cluster/modules/org-netbeans-examples-modules-misc.jar")).toExternalForm());
//        expectedRoots.add("junit.jar");
//        expectedRoots.add("nbjunit.jar");
//        expectedRoots.add("insanelib.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
//        assertEquals("right initial COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
//        TestBase.TestPCL l = new TestBase.TestPCL();
//        cp.addPropertyChangeListener(l);
//        ModuleEntry ioEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.io");
//        assertNotNull(ioEntry);
//        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(ioEntry)));
//        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
//        l.changed.clear();
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
//        assertEquals("right COMPILE classpath after changing project.xml", expectedRoots, urlsOfCp4Tests(cp));
//        EditableProperties props = copyOfMiscProject.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        props.setProperty("test.unit.cp.extra", "${netbeans.dest.dir}/lib/fnord.jar");
//        copyOfMiscProject.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
//        assertTrue("got changes again", l.changed.contains(ClassPath.PROP_ROOTS));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/lib/fnord.jar"));
//        assertEquals("right COMPILE classpath after changing project.properties", expectedRoots, urlsOfCp4Tests(cp));
//    }

    /* XXX no longer available here
    public void testBinaryOriginAbsolutePath() throws Exception {
        File jmfhome = new File(getWorkDir(), "jmfhome");
        File audioFiles = file("platform/samples/audio-files");
        if (!audioFiles.isDirectory()) {
            System.err.println("Skipping testBinaryOriginAbsolutePath since platform not checked out");
            return;
        }
        File audioviewer = copyFolder(audioFiles);
        // Make it a standalone module so we can copy it:
        File pp = new File(audioviewer, "nbproject/private/private.properties".replace('/', File.separatorChar));
        pp.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(pp);
        try {
            Properties p = new Properties();
            p.setProperty("jmf.home", jmfhome.getAbsolutePath());
            p.store(os, null);
        } finally {
            os.close();
        }
        pp = new File(audioviewer, "nbproject/private/platform-private.properties".replace('/', File.separatorChar));
        pp.getParentFile().mkdirs();
        os = new FileOutputStream(pp);
        try {
            Properties p = new Properties();
            p.setProperty("netbeans.dest.dir", file("nbbuild/netbeans").getAbsolutePath());
            p.store(os, null);
        } finally {
            os.close();
        }
        File px = new File(audioviewer, "nbproject/project.xml".replace('/', File.separatorChar));
        Document doc = XMLUtil.parse(new InputSource(px.toURI().toString()), false, true, null, null);
        NodeList nl = doc.getDocumentElement().getElementsByTagNameNS(NbModuleProjectType.NAMESPACE_SHARED_2, "data");
        assertEquals(1, nl.getLength());
        Element data = (Element) nl.item(0);
        // XXX insert at position 1, between <c-n-b> and <m-d>:
        data.appendChild(doc.createElementNS(NbModuleProjectType.NAMESPACE_SHARED_2, "standalone"));
        os = new FileOutputStream(px);
        try {
            XMLUtil.write(doc, os, "UTF-8");
        } finally {
            os.close();
        }
        FileObject audioviewerFO = FileUtil.toFileObject(audioviewer);
        Project p = ProjectManager.getDefault().findProject(audioviewerFO);
        assertNotNull(p);
        FileObject src = audioviewerFO.getFileObject("src");
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        File lib = new File(jmfhome, "lib");
        expectedRoots.add(FileUtil.urlForArchiveOrDir(new File(lib, "jmf.jar")).toExternalForm());
        expectedRoots.add(FileUtil.urlForArchiveOrDir(new File(lib, "mediaplayer.jar")).toExternalForm());
        assertEquals("right COMPILE classpath incl. absolute locations of JARs",
            expectedRoots, urlsOfCp(cp));
    }
    
    private void assertClassPathsHaveTheSameResources(ClassPath actual, ClassPath expected) {
        assertEquals(urlsOfCp(expected).toString(), urlsOfCp(actual).toString());
    }
     */

//    XXX: failing test, fix or delete
//    public void testTransitiveExecuteClasspath() throws Exception { // #70206
//        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "prj");
//        Util.addDependency(p, "org.openide.windows");
//        ProjectManager.getDefault().saveProject(p);
//        ClassPath cp = ClassPath.getClassPath(p.getSourceDirectory(), ClassPath.EXECUTE);
//        Set<String> expectedRoots = new TreeSet<String>();
//        // What we just added:
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
//        // And its transitive deps:
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
//        // Dialogs API depends on Progress API
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
//        // And the usual:
//        expectedRoots.add(FileUtil.urlForArchiveOrDir(new File(p.getProjectDirectoryFile(), "build/classes")).toExternalForm());
//        assertEquals("right EXECUTE classpath incl. transitive deps",
//            expectedRoots, urlsOfCp(cp));
//    }
    
}
