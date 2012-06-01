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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.modules.apisupport.project.Evaluator;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

public final class ClassPathProviderImpl implements ClassPathProvider {

    public static final String BOOTCLASSPATH_PREPEND = "bootclasspath.prepend";
    
    private final NbModuleProject project;
    
    public ClassPathProviderImpl(NbModuleProject project) {
        this.project = project;
    }
    
    private ClassPath boot;
    private ClassPath source;
    private ClassPath compile;
    private ClassPath execute;
    private ClassPath testSource;
    private ClassPath testCompile;
    private ClassPath testExecute;
    private ClassPath funcTestSource;
    private ClassPath funcTestCompile;
    private ClassPath funcTestExecute;
    private Map<FileObject,ClassPath> extraCompilationUnitsCompile = null;
    private Map<FileObject,ClassPath> extraCompilationUnitsExecute = null;

    private static final Logger LOG = Logger.getLogger(ClassPathProviderImpl.class.getName());

    public @Override ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.BOOT)) {
            if (boot == null) {
                boot = ClassPathFactory.createClassPath(ClassPathSupport.createProxyClassPathImplementation(
                        createPathFromProperty(BOOTCLASSPATH_PREPEND),
                        createPathFromProperty(Evaluator.NBJDK_BOOTCLASSPATH)));
            }
            return boot;
        }
        FileObject srcDir = project.getSourceDirectory();
        FileObject testSrcDir = project.getTestSourceDirectory("unit");
        FileObject funcTestSrcDir = project.getTestSourceDirectory("qa-functional");
        @NonNull File dir = project.getClassesDirectory();
        // #164282: workaround for not refreshed FS cache
        dir = FileUtil.normalizeFile(dir);
        FileObject classesDir = dir.exists() ? FileUtil.toFileObject(dir) : null;
        dir = project.getTestClassesDirectory("unit");
        dir = FileUtil.normalizeFile(dir);
        FileObject testClassesDir = dir.exists() ? FileUtil.toFileObject(dir) : null;
        File moduleJar;
        URL generatedClasses = FileUtil.urlForArchiveOrDir(project.getGeneratedClassesDirectory());
        URL generatedUnitTestClasses = FileUtil.urlForArchiveOrDir(project.getTestGeneratedClassesDirectory("unit"));
        URL generatedFunctionalTestClasses = FileUtil.urlForArchiveOrDir(project.getTestGeneratedClassesDirectory("qa-functional"));
        String fileU = file.toURL().toString();
        if (srcDir != null && generatedClasses != null &&
                (FileUtil.isParentOf(srcDir, file) || file == srcDir || fileU.startsWith(generatedClasses.toString()))) {
            // Regular sources.
            if (type.equals(ClassPath.COMPILE)) {
                if (compile == null) {
                    compile = ClassPathFactory.createClassPath(createCompileClasspath());
                    LOG.log(Level.FINE, "compile/execute-time classpath for file ''{0}'' (prj: {1}): {2}", new Object[] {file.getPath(), project, compile});
                }
                return compile;
            } else if (type.equals(ClassPath.EXECUTE) || type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
                if (execute == null) {
                    execute = ClassPathFactory.createClassPath(createExecuteClasspath());
                }
                return execute;
            } else if (type.equals(ClassPath.SOURCE)) {
                if (source == null) {
                    source = ClassPathSupport.createClassPath(srcDir.toURL(), generatedClasses);
                }
                return source;
            }
        } else if (testSrcDir != null && generatedUnitTestClasses != null &&
                (FileUtil.isParentOf(testSrcDir, file) || file == testSrcDir || fileU.startsWith(generatedUnitTestClasses.toString()))) {
            // Unit tests.
            // XXX refactor to use project.supportedTestTypes
            if (type.equals(ClassPath.COMPILE)) {
                if (testCompile == null) {
                    testCompile = ClassPathFactory.createClassPath(createTestCompileClasspath("unit"));
                    LOG.log(Level.FINE, "compile-time classpath for tests for file ''{0}'' (prj: {1}): {2}", new Object[] {file.getPath(), project, testCompile});
                }
                return testCompile;
            } else if (type.equals(ClassPath.EXECUTE) || type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
                if (testExecute == null) {
                    testExecute = ClassPathFactory.createClassPath(createTestExecuteClasspath("unit"));
                    LOG.log(Level.FINE, "runtime classpath for tests for file ''{0}'' (prj: {1}): {2}", new Object[] {file.getPath(), project, testExecute});
                }
                return testExecute;
            } else if (type.equals(ClassPath.SOURCE)) {
                if (testSource == null) {
                    testSource = ClassPathSupport.createClassPath(testSrcDir.toURL(), generatedUnitTestClasses);
                }
                return testSource;
            }
        } else if (funcTestSrcDir != null && generatedFunctionalTestClasses != null &&
                (FileUtil.isParentOf(funcTestSrcDir, file) || file == funcTestSrcDir || fileU.startsWith(generatedFunctionalTestClasses.toString()))) {
            // Functional tests.
            if (type.equals(ClassPath.SOURCE)) {
                if (funcTestSource == null) {
                    funcTestSource = ClassPathSupport.createClassPath(funcTestSrcDir.toURL(), generatedFunctionalTestClasses);
                }
                return funcTestSource;
            } else if (type.equals(ClassPath.COMPILE)) {
                // See #42331.
                if (funcTestCompile == null) {
                    funcTestCompile = ClassPathFactory.createClassPath(createTestCompileClasspath("qa-functional"));
                    LOG.log(Level.FINE, "compile-time classpath for func tests for file ''{0}'' (prj: {1}): {2}", new Object[] {file.getPath(), project, funcTestCompile});
                }
                return funcTestCompile;
            } else if (type.equals(ClassPath.EXECUTE) || type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
                if (funcTestExecute == null) {
                    funcTestExecute = ClassPathFactory.createClassPath(createTestExecuteClasspath("qa-functional"));
                }
                return funcTestExecute;
            }
        } else if (classesDir != null && (classesDir.equals(file) || FileUtil.isParentOf(classesDir,file))) {
            if (ClassPath.EXECUTE.equals(type)) {
                List<PathResourceImplementation> roots = new ArrayList<PathResourceImplementation>();
                roots.add ( ClassPathSupport.createResource(classesDir.toURL()));
                roots.addAll(createCompileClasspath().getResources());
                return ClassPathSupport.createClassPath (roots);
            }
        } else if (testClassesDir != null && (testClassesDir.equals(file) || FileUtil.isParentOf(testClassesDir,file))) {
            if (ClassPath.EXECUTE.equals(type)) {
                if (testExecute == null) {
                    testExecute = ClassPathFactory.createClassPath(createTestExecuteClasspath("unit"));
                    LOG.log(Level.FINE, "runtime classpath for tests for file ''{0}'' (prj: {1}): {2}", new Object[] {file.getPath(), project, testExecute});
                }
                return testExecute;
            }
        } else if (FileUtil.getArchiveFile(file) != null &&
                file.equals(FileUtil.getArchiveRoot(FileUtil.getArchiveFile(file))) &&
                FileUtil.toFile(FileUtil.getArchiveFile(file)).equals(moduleJar = project.getModuleJarLocation())) {
            if (ClassPath.EXECUTE.equals(type)) {
                List<PathResourceImplementation> roots = new ArrayList<PathResourceImplementation>();
                roots.add(ClassPathSupport.createResource(FileUtil.urlForArchiveOrDir(moduleJar)));
                roots.addAll(createCompileClasspath().getResources());
                return ClassPathSupport.createClassPath (roots);
            }
        }
        else {
            calculateExtraCompilationUnits();
            for (Map.Entry<FileObject,ClassPath> entry : extraCompilationUnitsCompile.entrySet()) {
                FileObject pkgroot = entry.getKey();
                if (FileUtil.isParentOf(pkgroot, file) || file == pkgroot) {
                    if (type.equals(ClassPath.COMPILE)) {
                        return entry.getValue();
                    } else if (type.equals(ClassPath.EXECUTE)) {
                        return extraCompilationUnitsExecute.get(pkgroot);
                    } else if (type.equals(ClassPath.SOURCE)) {
                        // XXX should these be cached?
                        return ClassPathSupport.createClassPath(new FileObject[] {pkgroot});
                    } else {
                        break;
                    }
                }
            }
        }
        if (type.equals(ClassPath.SOURCE)) {
            Map<String,String> properties = project.evaluator().getProperties();
            if (properties != null) {
            for (Map.Entry<String,String> entry : properties.entrySet()) {
                if (entry.getKey().startsWith(NbModuleProject.SOURCE_START)) {
                    FileObject jar = project.getHelper().resolveFileObject(entry.getValue());
                    if (jar != null) {
                        FileObject root = FileUtil.getArchiveRoot(jar);
                        if (root != null && (root == file || FileUtil.isParentOf(root, file))) {
                            return ClassPathSupport.createClassPath(new FileObject[] {root});
                        }
                    }
                }
            }
            }
        }
        // Something not supported.
        return null;
    }
    
    private ClassPathImplementation createPathFromProperty(String prop) {
        return ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
            project.getProjectDirectoryFile(), project.evaluator(), new String[] {prop});
    }
    
    /** &lt;compile-dependency&gt; is what we care about. */
    private ClassPathImplementation createCompileClasspath() {
        return createPathFromProperty(Evaluator.CP);
    }
    
    private void addPathFromProjectEvaluated(List<PathResourceImplementation> entries, String path) {
        if (path != null) {
            for (String piece : PropertyUtils.tokenizePath(path)) {
                URL url = FileUtil.urlForArchiveOrDir(project.getHelper().resolveFile(piece));
                if (url != null) { // #135292
                    entries.add(ClassPathSupport.createResource(url));
                }
            }
        }
    }
    
    private ClassPathImplementation createTestCompileClasspath(String testType) {
        return createPathFromProperty("test." + testType + ".cp"); // NOI18N
    }
    
    private ClassPathImplementation createTestExecuteClasspath(String testType) {
        return createPathFromProperty("test." + testType + ".run.cp"); // NOI18N
    }
    
    private ClassPathImplementation createExecuteClasspath() {
        return createPathFromProperty(Evaluator.RUN_CP);
    }
    
    private void calculateExtraCompilationUnits() {
        if (extraCompilationUnitsCompile != null) {
            return;
        }
        extraCompilationUnitsCompile = new HashMap<FileObject,ClassPath>();
        extraCompilationUnitsExecute = new HashMap<FileObject,ClassPath>();
        for (Map.Entry<FileObject,Element> entry : project.getExtraCompilationUnits().entrySet()) {
            final FileObject pkgroot = entry.getKey();
            Element pkgrootEl = entry.getValue();
            Element classpathEl = XMLUtil.findElement(pkgrootEl, "classpath", NbModuleProject.NAMESPACE_SHARED); // NOI18N
            assert classpathEl != null : "no <classpath> in " + pkgrootEl;
            final String classpathS = XMLUtil.findText(classpathEl);
            if (classpathS == null) {
                extraCompilationUnitsCompile.put(pkgroot, ClassPathSupport.createClassPath(new URL[0]));
                extraCompilationUnitsExecute.put(pkgroot, ClassPathSupport.createClassPath(new URL[0]));
            } else {
                class CPI implements ClassPathImplementation, PropertyChangeListener, AntProjectListener {
                    final Set<String> relevantProperties = new HashSet<String>();
                    final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
                    String cpS = classpathS;
                    @SuppressWarnings("LeakingThisInConstructor")
                    CPI() {
                        project.evaluator().addPropertyChangeListener(WeakListeners.propertyChange(this, project.evaluator()));
                        project.getHelper().addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, project.getHelper()));
                        Matcher m = Pattern.compile("\\$\\{([^{}]+)\\}").matcher(cpS);
                        while (m.find()) {
                            relevantProperties.add(m.group(1));
                        }
                    }
                    public @Override List<? extends PathResourceImplementation> getResources() {
                        List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation>();
                        addPathFromProjectEvaluated(resources, project.evaluator().evaluate(cpS));
                        return resources;
                    }
                    public @Override void addPropertyChangeListener(PropertyChangeListener listener) {
                        pcs.addPropertyChangeListener(listener);
                    }
                    public @Override void removePropertyChangeListener(PropertyChangeListener listener) {
                        pcs.removePropertyChangeListener(listener);
                    }
                    public @Override void propertyChange(PropertyChangeEvent evt) {
                        if (relevantProperties.contains(evt.getPropertyName())) {
                            pcs.firePropertyChange(PROP_RESOURCES, null, null);
                        }
                    }
                    public @Override void configurationXmlChanged(AntProjectEvent ev) {
                        Element pkgrootEl = project.getExtraCompilationUnits().get(pkgroot);
                        Element classpathEl = XMLUtil.findElement(pkgrootEl, "classpath", NbModuleProject.NAMESPACE_SHARED); // NOI18N
                        assert classpathEl != null : "no <classpath> in " + pkgrootEl;
                        cpS = XMLUtil.findText(classpathEl);
                        pcs.firePropertyChange(PROP_RESOURCES, null, null);
                    }
                    public @Override void propertiesChanged(AntProjectEvent ev) {}
                }
                ClassPathImplementation ecuCompile = new CPI();
                extraCompilationUnitsCompile.put(pkgroot, ClassPathFactory.createClassPath(ecuCompile));
                // Add <built-to> dirs and JARs for ClassPath.EXECUTE.
                List<PathResourceImplementation> extraEntries = new ArrayList<PathResourceImplementation>();
                for (Element kid : XMLUtil.findSubElements(pkgrootEl)) {
                    if (!kid.getLocalName().equals("built-to")) { // NOI18N
                        continue;
                    }
                    String rawtext = XMLUtil.findText(kid);
                    assert rawtext != null : "Null content for <built-to> in " + project;
                    String text = project.evaluator().evaluate(rawtext);
                    if (text == null) {
                        continue;
                    }
                    addPathFromProjectEvaluated(extraEntries, text);
                }
                extraCompilationUnitsExecute.put(pkgroot, ClassPathFactory.createClassPath(
                        ClassPathSupport.createProxyClassPathImplementation(ecuCompile, ClassPathSupport.createClassPathImplementation(extraEntries))));
            }
        }
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                return new ClassPath[] {findClassPath(srcDir, ClassPath.BOOT)};
            }
        }
        List<ClassPath> paths = new ArrayList<ClassPath>(3);
        if (ClassPath.COMPILE.equals(type)) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                paths.add(findClassPath(srcDir, ClassPath.COMPILE));
            }
            for (String testType : project.supportedTestTypes()) {
                FileObject testSrcDir = project.getTestSourceDirectory(testType);
                if (testSrcDir != null) {
                    paths.add(findClassPath(testSrcDir, ClassPath.COMPILE));
                }
            }
            calculateExtraCompilationUnits();
            paths.addAll(extraCompilationUnitsCompile.values());
        } else if (ClassPath.EXECUTE.equals(type)) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                paths.add(findClassPath(srcDir, ClassPath.EXECUTE));
            }
            for (String testType : project.supportedTestTypes()) {
                FileObject testSrcDir = project.getTestSourceDirectory(testType);
                if (testSrcDir != null) {
                    paths.add(findClassPath(testSrcDir, ClassPath.EXECUTE));
                }
            }
            calculateExtraCompilationUnits();
            paths.addAll(extraCompilationUnitsExecute.values());
        } else if (ClassPath.SOURCE.equals(type)) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                paths.add(findClassPath(srcDir, ClassPath.SOURCE));
            }
            for (String testType : project.supportedTestTypes()) {
                FileObject testSrcDir = project.getTestSourceDirectory(testType);
                if (testSrcDir != null) {
                    paths.add(findClassPath(testSrcDir, ClassPath.SOURCE));
                }
            }
            calculateExtraCompilationUnits();
            for (FileObject root : extraCompilationUnitsCompile.keySet()) {
                paths.add(ClassPathSupport.createClassPath(new FileObject[] {root}));
            }
        }
        return paths.toArray(new ClassPath[paths.size()]);
    }
    
}
