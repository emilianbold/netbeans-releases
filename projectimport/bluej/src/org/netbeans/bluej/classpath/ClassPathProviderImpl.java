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

package org.netbeans.bluej.classpath;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.bluej.BluejProject;
import org.netbeans.bluej.options.BlueJSettings;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
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
    
    private CPImpl cpimpl;
    
    
    /** Creates a new instance of ClassPathProviderImpl */
    public ClassPathProviderImpl(BluejProject prj) {
        project = prj;
    }
    
    public CPImpl getBluejCPImpl() {
        return cpimpl;
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

    private ClassPath getSourcepath(FileObject file) { //NOPMD we don't care about the file passed in.. always the project dir is root
        if (source == null) {
            source = ClassPathSupport.createClassPath(new FileObject[] { project.getProjectDirectory() });
        }
        return source;
    }

    private ClassPath getRunTimeClasspath(FileObject file) { //NOPMD we don't care about the file passed in.. always the project dir is root
        return null;
    }

    private ClassPath getCompileTimeClasspath(FileObject file) { //NOPMD we don't care about the file passed in.. always the project dir is root
        if (compile == null) {
            // do we need ant cp as it is?
                ClassPath antcp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    FileUtil.toFile(project.getProjectDirectory()), project.getAntProjectHelper().getStandardPropertyEvaluator(), 
                new String[] {"javac.classpath"}));  // NOI18N
            cpimpl = new CPImpl(project);
            ClassPath bluejcp = ClassPathFactory.createClassPath(cpimpl);
            compile = ClassPathSupport.createProxyClassPath( new ClassPath[] {antcp, bluejcp} );
        } 
        return compile;
    }
    
    public ClassPath[] getCompileTimeClasspath() {
        if (compiles == null) {
            compiles = new ClassPath[] { getCompileTimeClasspath(project.getProjectDirectory()),
                                         //make source path, becuase it's equal with the built output path..
                                         ClassPathSupport.createClassPath(new FileObject[] { project.getProjectDirectory() })};
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
