/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;

public final class ClassPathProviderImpl implements ClassPathProvider {
    
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
    
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.BOOT)) {
            if (boot == null) {
                boot = ClassPathFactory.createClassPath(createPathFromProperty("nbjdk.bootclasspath")); // NOI18N
            }
            return boot;
        }
        FileObject srcDir = project.getSourceDirectory();
        FileObject testSrcDir = project.getTestSourceDirectory();
        FileObject funcTestSrcDir = project.getFunctionalTestSourceDirectory();        
        File dir = project.getClassesDirectory();
        FileObject classesDir = dir == null ? null : FileUtil.toFileObject(FileUtil.normalizeFile(dir));
        dir = project.getTestClassesDirectory();
        FileObject testClassesDir = dir == null ? null : FileUtil.toFileObject(FileUtil.normalizeFile(dir));
        File moduleJar = project.getModuleJarLocation();
        if (srcDir != null && (FileUtil.isParentOf(srcDir, file) || file == srcDir)) {
            // Regular sources.
            if (type.equals(ClassPath.COMPILE)) {
                if (compile == null) {
                    compile = ClassPathFactory.createClassPath(createCompileClasspath());
                    Util.err.log("compile/execute-time classpath for " + project + ": " + compile);
                }
                return compile;
            } else if (type.equals(ClassPath.EXECUTE)) {
                if (execute == null) {
                    execute = ClassPathFactory.createClassPath(createExecuteClasspath());
                }
                return execute;
            } else if (type.equals(ClassPath.SOURCE)) {
                if (source == null) {
                    source = ClassPathSupport.createClassPath(new FileObject[] {srcDir});
                }
                return source;
            }
        } else if (testSrcDir != null && (FileUtil.isParentOf(testSrcDir, file) || file == testSrcDir)) {
            // Unit tests.
            if (type.equals(ClassPath.COMPILE)) {
                if (testCompile == null) {
                    testCompile = ClassPathFactory.createClassPath(createTestCompileClasspath());
                    Util.err.log("compile-time classpath for tests in " + project + ": " + testCompile);
                }
                return testCompile;
            } else if (type.equals(ClassPath.EXECUTE)) {
                if (testExecute == null) {
                    testExecute = ClassPathFactory.createClassPath(createTestExecuteClasspath());
                    Util.err.log("runtime classpath for tests in " + project + ": " + testExecute);
                }
                return testExecute;
            } else if (type.equals(ClassPath.SOURCE)) {
                if (testSource == null) {
                    testSource = ClassPathSupport.createClassPath(new FileObject[] {testSrcDir});
                }
                return testSource;
            }
        } else if (funcTestSrcDir != null && (FileUtil.isParentOf(funcTestSrcDir, file) || file == funcTestSrcDir)) {
            // Functional tests.
            if (type.equals(ClassPath.SOURCE)) {
                if (funcTestSource == null) {
                    funcTestSource = ClassPathSupport.createClassPath(new FileObject[] {funcTestSrcDir});
                }
                return funcTestSource;
            } else if (type.equals(ClassPath.COMPILE)) {
                // See #42331.
                if (funcTestCompile == null) {
                    funcTestCompile = ClassPathFactory.createClassPath(createFuncTestCompileClasspath());
                    Util.err.log("compile-time classpath for func tests in " + project + ": " + funcTestCompile);
                }
                return funcTestCompile;
            } else if (type.equals(ClassPath.EXECUTE)) {
                if (funcTestExecute == null) {
                    funcTestExecute = ClassPathFactory.createClassPath(createFuncTestExecuteClasspath());
                }
                return funcTestExecute;
            }
        } else if (classesDir != null && (classesDir.equals(file) || FileUtil.isParentOf(classesDir,file))) {
            if (ClassPath.EXECUTE.equals(type)) {
                try {
                    List<PathResourceImplementation> roots = new ArrayList<PathResourceImplementation>();
                    roots.add ( ClassPathSupport.createResource(classesDir.getURL()));
                    roots.addAll(createCompileClasspath().getResources());
                    return ClassPathSupport.createClassPath (roots);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify (e);
                    return null;
                }
            }
        } else if (testClassesDir != null && (testClassesDir.equals(file) || FileUtil.isParentOf(testClassesDir,file))) {
            if (ClassPath.EXECUTE.equals(type)) {
                if (testExecute == null) {
                    testExecute = ClassPathFactory.createClassPath(createTestExecuteClasspath());
                    Util.err.log("runtime classpath for tests in " + project + ": " + testExecute);
                }
                return testExecute;
            }
        } else if (FileUtil.getArchiveFile(file) != null &&
                FileUtil.toFile(FileUtil.getArchiveFile(file)).equals(moduleJar) &&
                file.equals(FileUtil.getArchiveRoot(FileUtil.getArchiveFile(file)))) {
            if (ClassPath.EXECUTE.equals(type)) {
                List<PathResourceImplementation> roots = new ArrayList<PathResourceImplementation>();
                roots.add(ClassPathSupport.createResource(Util.urlForJar(moduleJar)));
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
        // Something not supported.
        return null;
    }
    
    private ClassPathImplementation createPathFromProperty(String prop) {
        return ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
            project.getProjectDirectoryFile(), project.evaluator(), new String[] {prop});
    }
    
    /** &lt;compile-dependency&gt; is what we care about. */
    private ClassPathImplementation createCompileClasspath() {
        return createPathFromProperty("cp"); // NOI18N
    }
    
    private void addPathFromProjectEvaluated(List<PathResourceImplementation> entries, String path) {
        if (path != null) {
            String[] pieces = PropertyUtils.tokenizePath(path);
            for (int i = 0; i < pieces.length; i++) {
                entries.add(ClassPathSupport.createResource(Util.urlForDirOrJar(project.getHelper().resolveFile(pieces[i]))));
            }
        }
    }
    
    private ClassPathImplementation createTestCompileClasspath() {
        return createPathFromProperty("test.unit.cp"); // NOI18N
    }
    
    private ClassPathImplementation createTestExecuteClasspath() {
        return createPathFromProperty("test.unit.run.cp"); // NOI18N
    }
    
    private ClassPathImplementation createFuncTestCompileClasspath() {
        return createPathFromProperty("test.qa-functional.cp"); // NOI18N
    }
    
    private ClassPathImplementation createFuncTestExecuteClasspath() {
        return createPathFromProperty("test.qa-functional.run.cp"); // NOI18N
    }
    
    private ClassPathImplementation createExecuteClasspath() {
        return createPathFromProperty("run.cp"); // NOI18N
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
            Element classpathEl = Util.findElement(pkgrootEl, "classpath", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
            assert classpathEl != null : "no <classpath> in " + pkgrootEl;
            final String classpathS = Util.findText(classpathEl);
            if (classpathS == null) {
                extraCompilationUnitsCompile.put(pkgroot, ClassPathSupport.createClassPath(new URL[0]));
                extraCompilationUnitsExecute.put(pkgroot, ClassPathSupport.createClassPath(new URL[0]));
            } else {
                class CPI implements ClassPathImplementation, PropertyChangeListener, AntProjectListener {
                    final Set<String> relevantProperties = new HashSet<String>();
                    final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
                    String cpS = classpathS;
                    CPI() {
                        project.evaluator().addPropertyChangeListener(WeakListeners.propertyChange(this, project.evaluator()));
                        project.getHelper().addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, project.getHelper()));
                        Matcher m = Pattern.compile("\\$\\{([^{}]+)\\}").matcher(cpS);
                        while (m.find()) {
                            relevantProperties.add(m.group(1));
                        }
                    }
                    public List<? extends PathResourceImplementation> getResources() {
                        List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation>();
                        addPathFromProjectEvaluated(resources, project.evaluator().evaluate(cpS));
                        return resources;
                    }
                    public void addPropertyChangeListener(PropertyChangeListener listener) {
                        pcs.addPropertyChangeListener(listener);
                    }
                    public void removePropertyChangeListener(PropertyChangeListener listener) {
                        pcs.removePropertyChangeListener(listener);
                    }
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (relevantProperties.contains(evt.getPropertyName())) {
                            pcs.firePropertyChange(PROP_RESOURCES, null, null);
                        }
                    }
                    public void configurationXmlChanged(AntProjectEvent ev) {
                        Element pkgrootEl = project.getExtraCompilationUnits().get(pkgroot);
                        Element classpathEl = Util.findElement(pkgrootEl, "classpath", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                        assert classpathEl != null : "no <classpath> in " + pkgrootEl;
                        cpS = Util.findText(classpathEl);
                        pcs.firePropertyChange(PROP_RESOURCES, null, null);
                    }
                    public void propertiesChanged(AntProjectEvent ev) {}
                }
                ClassPathImplementation ecuCompile = new CPI();
                extraCompilationUnitsCompile.put(pkgroot, ClassPathFactory.createClassPath(ecuCompile));
                // Add <built-to> dirs and JARs for ClassPath.EXECUTE.
                List<PathResourceImplementation> extraEntries = new ArrayList<PathResourceImplementation>();
                for (Element kid : Util.findSubElements(pkgrootEl)) {
                    if (!kid.getLocalName().equals("built-to")) { // NOI18N
                        continue;
                    }
                    String rawtext = Util.findText(kid);
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
            FileObject testSrcDir = project.getTestSourceDirectory();
            if (testSrcDir != null) {
                paths.add(findClassPath(testSrcDir, ClassPath.COMPILE));
            }
            FileObject funcTestSrcDir = project.getFunctionalTestSourceDirectory();
            if (funcTestSrcDir != null) {
                paths.add(findClassPath(funcTestSrcDir, ClassPath.COMPILE));
            }
            calculateExtraCompilationUnits();
            paths.addAll(extraCompilationUnitsCompile.values());
        } else if (ClassPath.EXECUTE.equals(type)) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                paths.add(findClassPath(srcDir, ClassPath.EXECUTE));
            }
            FileObject testSrcDir = project.getTestSourceDirectory();
            if (testSrcDir != null) {
                paths.add(findClassPath(testSrcDir, ClassPath.EXECUTE));
            }
            FileObject funcTestSrcDir = project.getFunctionalTestSourceDirectory();
            if (funcTestSrcDir != null) {
                paths.add(findClassPath(funcTestSrcDir, ClassPath.EXECUTE));
            }
            calculateExtraCompilationUnits();
            paths.addAll(extraCompilationUnitsExecute.values());
        } else if (ClassPath.SOURCE.equals(type)) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                paths.add(findClassPath(srcDir, ClassPath.SOURCE));
            }
            FileObject testSrcDir = project.getTestSourceDirectory();
            if (testSrcDir != null) {
                paths.add(findClassPath(testSrcDir, ClassPath.SOURCE));
            }
            FileObject funcTestSrcDir = project.getFunctionalTestSourceDirectory();
            if (funcTestSrcDir != null) {
                paths.add(findClassPath(funcTestSrcDir, ClassPath.SOURCE));
            }
            calculateExtraCompilationUnits();
            for (FileObject root : extraCompilationUnitsCompile.keySet()) {
                paths.add(ClassPathSupport.createClassPath(new FileObject[] {root}));
            }
        }
        return paths.toArray(new ClassPath[paths.size()]);
    }
    
}
