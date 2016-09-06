/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.classpath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.ModuleElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestJavaPlatform;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class ModuleClassPathsTest extends NbTestCase {
    
    private static final Comparator<Object> LEX_COMPARATOR =
            (a,b) -> a.toString().compareTo(b.toString());
    
    private SourceRoots src;
    private ClassPath systemModules;
    
    public ModuleClassPathsTest(@NonNull final String name) {
        super(name);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject workDir = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        MockLookup.setInstances(TestProject.createProjectType());
        final FileObject prjDir = FileUtil.createFolder(workDir, "TestProject");    //NOI18N
        assertNotNull(prjDir);
        final FileObject srcDir = FileUtil.createFolder(prjDir, "src");    //NOI18N
        assertNotNull(srcDir);
        final Project prj = TestProject.createProject(prjDir, srcDir, null);
        assertNotNull(prj);
        setSourceLevel(prj, "9");   //NOI18N
        src = Optional.ofNullable(prj.getLookup().lookup(TestProject.class))
                .map((p) -> p.getSourceRoots())
                .orElse(null);
        systemModules = Optional.ofNullable(TestUtilities.getJava9Home())
                .map((jh) -> TestJavaPlatform.createModularPlatform(jh))
                .map((jp) -> jp.getBootstrapLibraries())
                .orElse(null);
    }
    
   
    public void testModuleInfoBasedCp_SystemModules_in_UnnamedModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(systemModules, "java.se");  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }
    
    
    public void testModuleInfoBasedCp_SystemModules_in_NamedEmptyModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        createModuleInfo(src, "Modle"); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(systemModules, "java.base");  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_SystemModules_in_NamedModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        createModuleInfo(src, "Modle", "java.compact1"); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(systemModules, "java.compact1");  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }
    
    private static void setSourceLevel(
            @NonNull final Project prj,
            @NonNull final String sourceLevel) throws IOException {
        try {
            final TestProject tprj = prj.getLookup().lookup(TestProject.class);
            if (tprj == null) {
                throw new IllegalStateException("No TestProject instance: " + prj); //NOI18N
            }
            ProjectManager.mutex().writeAccess((Mutex.ExceptionAction<Void>)()->{
                final EditableProperties ep = tprj.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(ProjectProperties.JAVAC_SOURCE, sourceLevel);
                ep.setProperty(ProjectProperties.JAVAC_TARGET, sourceLevel);
                tprj.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
                return null;
            });
        } catch (MutexException e) {
            throw e.getCause() instanceof IOException ?
                    (IOException) e.getCause() :
                    new IOException(e.getCause());
        }
    }
    
    @NonNull
    private static FileObject createModuleInfo(
            @NonNull final SourceRoots src,
            @NonNull final String moduleName,
            @NonNull final String... requiredModules) throws IOException {
        final FileObject[] roots = src.getRoots();
        if (roots.length == 0) {
            throw new IOException("No source roots");   //NOI18N
        }
        final FileObject[] res = new FileObject[1];
        FileUtil.runAtomicAction((FileSystem.AtomicAction)() -> {
                res[0] = FileUtil.createData(roots[0], "module-info.java");    //NOI18N
                final StringBuilder module = new StringBuilder("module ").append(moduleName).append(" {");    //NOI18N
                for (String mod : requiredModules) {
                    module.append("requires ").append(mod).append(";");
                }
                module.append("}"); //NOI18N
                final FileLock lck = res[0].lock();
                try (OutputStream out = res[0].getOutputStream(lck);
                        InputStream in = new ByteArrayInputStream(module.toString().getBytes(FileEncodingQuery.getEncoding(res[0])))) {
                    FileUtil.copy(in, out);
                } finally {
                    lck.releaseLock();
                }
        });
        return res[0];
    }
    
    private Collection<URL> collectEntries(@NonNull final ClassPath cp) {
        final List<URL> res = new ArrayList<>();
        for (ClassPath.Entry e : cp.entries()) {
            res.add(e.getURL());
        }
        Collections.sort(res, LEX_COMPARATOR);
        return res;
    }
    
    private Collection<URL> reads(
            @NonNull final ClassPath base,
            @NonNull final String moduleName) throws IOException {
        final ClasspathInfo info = new ClasspathInfo.Builder(base)
                .setModuleBootPath(base)
                .build();
        final Set<String> moduleNames = new HashSet<>();
        JavaSource.create(info).runUserActionTask((cc)-> {
                final ModuleElement root = cc.getElements().getModuleElement(moduleName);
                collectDeps(root, moduleNames);
            },
            true);
        return base.entries().stream()
                .map((e) -> e.getURL())
                .filter((url) -> moduleNames.contains(SourceUtils.getModuleName(url)))
                .sorted(LEX_COMPARATOR)
                .collect(Collectors.toList());
    }
    
    private static void collectDeps(
        @NullAllowed final ModuleElement me,
        @NonNull final Collection<? super String> modNames) {
        if (me != null) {
            final String mn = me.getQualifiedName().toString();
            if (!modNames.contains(mn)) {
                modNames.add(mn);
                for (ModuleElement.Directive d : me.getDirectives()) {
                    if (d.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                        final ModuleElement.RequiresDirective rd = (ModuleElement.RequiresDirective)d;
                        if (rd.isPublic() || isMandated(rd)) {
                            collectDeps(rd.getDependency(), modNames);
                        }
                    }
                }
            }
        }
    }
    
    private static boolean isMandated(ModuleElement.RequiresDirective rd) {
        return Optional.ofNullable(rd.getDependency())
                .map((me) -> me.getQualifiedName().toString())
                .map((mn) -> "java.base".equals(mn))
                .orElse(Boolean.FALSE);
    }
}
