/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.bluej.classpath;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.bluej.BluejProject;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class ClassPathProviderImpl implements ClassPathProvider {

    private BluejProject project;
    
    private ClassPath boot;
    private ClassPath source;
    private ClassPath compile;
    private ClassPath[] boots;
    private ClassPath[] sources;
    private ClassPath[] compiles;

    
    
    /** Creates a new instance of ClassPathProviderImpl */
    public ClassPathProviderImpl(BluejProject prj) {
        project = prj;
    }

    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(file);
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(file);
        } else if (type.equals(ClassPath.SOURCE)) {
            return getSourcepath(file);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else {
            return null;
        }
    }

    private ClassPath getBootClassPath() {
        if (boot == null) {
            boot = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        return boot;
    }

    private ClassPath getSourcepath(FileObject file) {
        if (source == null) {
            source = ClassPathSupport.createClassPath(new FileObject[] { project.getProjectDirectory() });
        }
        return source;
    }

    private ClassPath getRunTimeClasspath(FileObject file) {
        return null;
    }

    private ClassPath getCompileTimeClasspath(FileObject file) {
        if (compile == null) {
            compile = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    FileUtil.toFile(project.getProjectDirectory()), project.getAntProjectHelper().getStandardPropertyEvaluator(), 
                new String[] {"javac.classpath"})); // NOI18N
        } 
        return compile;
    }
    
    public ClassPath[] getCompileTimeClasspath() {
        if (compiles == null) {
            compiles = new ClassPath[] { getCompileTimeClasspath(project.getProjectDirectory()) };
        }
        return compiles;
    }
    
    public ClassPath[] getSourcePath() {
        if (sources == null) {
            sources = new ClassPath[] { getSourcepath(project.getProjectDirectory()) };
        }
        return sources;
    }
    
    public ClassPath[] getBootPath() {
        if (boots == null) {
            boots = new ClassPath[] { getBootClassPath() };
        }
        return boots;
    }
    
}
