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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.bluej.BluejProject;
import org.netbeans.bluej.options.BlueJSettings;
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
            Collection paths = new ArrayList();
            paths.add(ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    FileUtil.toFile(project.getProjectDirectory()), project.getAntProjectHelper().getStandardPropertyEvaluator(), 
                new String[] {"javac.classpath"}))); // NOI18N
            FileObject libs = project.getProjectDirectory().getFileObject("+libs");
            if (libs != null) {
                Collection libJars = new ArrayList();
                FileObject[] fos = libs.getChildren();
                for (int i = 0; i < fos.length; i++) {
                    if (FileUtil.isArchiveFile(fos[i])) {
                        libJars.add(FileUtil.getArchiveRoot(fos[i]));
                    }
                }
                if (libJars.size() > 0) {
                    paths.add(ClassPathSupport.createClassPath((FileObject[])libJars.toArray(new FileObject[libJars.size()])));
                }
            }
            File home = BlueJSettings.getDefault().getHome();
            if (home != null) {
                File userLibs = new File(new File(home, "lib"), "userlib");
                FileObject fo = FileUtil.toFileObject(userLibs);
                if (fo != null) {
                    Collection libJars = new ArrayList();
                    FileObject[] fos = fo.getChildren();
                    for (int i = 0; i < fos.length; i++) {
                        if (FileUtil.isArchiveFile(fos[i])) {
                            libJars.add(FileUtil.getArchiveRoot(fos[i]));
                        }
                    }
                    if (libJars.size() > 0) {
                        paths.add(ClassPathSupport.createClassPath((FileObject[])libJars.toArray(new FileObject[libJars.size()])));
                    }
                }
            }
            String userPath = BlueJSettings.getDefault().getUserLibrariesAsClassPath();
            if (userPath.length() > 0) {
                StringTokenizer tokens = new StringTokenizer(userPath, ":", false);
                Collection userLibJars = new ArrayList();
                while (tokens.hasMoreTokens()) {
                    File fil = new File(tokens.nextToken());
                    FileObject fo = FileUtil.toFileObject(fil);
                    if (fo != null && FileUtil.isArchiveFile(fo)) {
                        userLibJars.add(FileUtil.getArchiveRoot(fo));
                    }
                }
                if (userLibJars.size() > 0) {
                    paths.add(ClassPathSupport.createClassPath((FileObject[])userLibJars.toArray(new FileObject[userLibJars.size()])));
                }
            }
            compile = ClassPathSupport.createProxyClassPath((ClassPath[])paths.toArray(new ClassPath[paths.size()]));
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
