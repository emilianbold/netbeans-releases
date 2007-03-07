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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject.classpath;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.ejbjarproject.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Defines the various class paths for a EJB project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, AntProjectListener {
    
    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;
    
    /**
     * Cache for classpaths:
     * <dl>
     *     <dt>0</dt> <dd>sources classpath</dd>
     *     <dt>1</dt> <dd>test sources classpath</dd>
     *     <dt>2</dt> <dd>sources compile classpath</dd>
     *     <dt>3</dt> <dd>test sources compile classpath</dd>
     *     <dt>4</dt> <dd>sources and built sources run classpath</dd>
     *     <dt>5</dt> <dd>test sources and built test sources run classpath</dd>
     *     <dt>6</dt> <dd>XXX: todo</dd>
     *     <dt>7</dt> <dd>boot classpath</dd>
     *     <dt>8</dt> <dd>J2EE platform classpath</dd>
     * </dl>
     */
    private final ClassPath[] cache = new ClassPath[9];

    private final Map dirCache = new HashMap ();

    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, 
            SourceRoots sourceRoots, SourceRoots testSourceRoots) {
        this.helper = helper;
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert this.projectDirectory != null;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testSourceRoots = testSourceRoots;
        this.helper.addAntProjectListener (this);
    }

    private synchronized FileObject getDir(String propname) {
        FileObject fo = (FileObject) this.dirCache.get (propname);
        if (fo == null ||  !fo.isValid()) {
            String prop = helper.getStandardPropertyEvaluator ().getProperty (propname);
            if (prop != null) {
                fo = helper.resolveFileObject(prop);
                this.dirCache.put (propname, fo);
            }
        }
        return fo;
    }
    
    private FileObject[] getPrimarySrcPath() {
        return this.sourceRoots.getRoots();
    }
    
    private FileObject[] getTestSrcDir() {
         return this.testSourceRoots.getRoots();
    }
    
    private FileObject getBuildClassesDir() {
        return getDir("build.classes.dir");    //NOI18N
    }
    
    private FileObject getBuildJar() {
        return getDir("dist.jar");            //NOI18N
    }
    
    private FileObject getBuildTestClassesDir() {
        return getDir("build.test.classes.dir"); // NOI18N
    }
    
    /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of: <dl>
     *         <dt>0</dt> <dd>normal source</dd>
     *         <dt>1</dt> <dd>test source</dd>
     *         <dt>2</dt> <dd>built class (unpacked)</dd>
     *         <dt>3</dt> <dd>built test class</dd>
     *         <dt>4</dt> <dd>built class (in dist JAR)</dd>
     *         <dt>-1</dt> <dd>something else</dd>
     *         </dl>
     */
    private int getType(FileObject file) {
        FileObject[] srcPath = getPrimarySrcPath();
        for (int i=0; i < srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 0;
            }
        }        
        srcPath = getTestSrcDir();
        for (int i=0; i< srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 1;
            }
        }
        FileObject dir = getBuildClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 2;
        }
        dir = getBuildJar();
        if (dir != null && (dir.equals(file))) {     //TODO: When MasterFs check also isParentOf
            return 4;
        }
        dir = getBuildTestClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
            return 3;
        }
        return -1;
    }
    
    private ClassPath getCompileTimeClasspath(FileObject file) {
        int type = getType(file);
        return this.getCompileTimeClasspath(type);
    }
    
    private synchronized ClassPath getCompileTimeClasspath(int type) {        
        if (type < 0 || type > 1) {
            // Not a source file.
            return null;
        }
        ClassPath cp = cache[2+type];
        if ( cp == null) {
            if (type == 0) {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper, "${javac.classpath}:${" //NOI18N
                        + EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH 
                        + "}", evaluator, false));  //NOI18N
            }
            else {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper, "${javac.test.classpath}:${" // NOI18N
                        + EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH
                        + "}", evaluator, false)); // NOI18N
            }
            cache[2+type] = cp;
        }
        return cp;
    }
    
    private synchronized ClassPath getRunTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 4) {
            return null;
        } else if (type > 1) {
            type -= 2;
        }
        
        ClassPath cp = cache[4+type];
        if (cp == null) {
            if (type == 0) {
                // XXX: should return run.classpath, but since there's no run classpath,
                // in and EJB project, using debug.classpath instead
                cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {"debug.classpath"})); // NOI18N
            }
            else if (type == 1) {
                cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {"run.test.classpath"})); // NOI18N
            }
            else if (type == 2) {
                //Only to make the CompiledDataNode hapy
                //Todo: Strictly it should return ${run.classpath} - ${build.classes.dir} + ${dist.jar}
                cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {"dist.jar"})); // NOI18N
            }
            
            cache[4+type] = cp;
        }
        return cp;
    }
    
    private ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        return this.getSourcepath(type);
    }
    
    private synchronized ClassPath getSourcepath(int type) {
        if (type < 0 || type > 1) {
            return null;
        }
        ClassPath cp = cache[type];
        if (cp == null) {
            switch (type) {
                case 0:
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation (this.sourceRoots,this.helper));
                    break;
                case 1:
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation (this.testSourceRoots,this.helper));
                    break;
            }
        }
        cache[type] = cp;
        return cp;
    }

    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache[7];
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(new BootClassPathImplementation(evaluator));
            cache[7] = cp;
        }
        return cp;
    }
    
    public synchronized ClassPath getJ2eePlatformClassPath() {
        ClassPath cp = cache[8];
        if (cp == null) {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper,  "${" + //NOI18N
                        EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH  +  
                        "}", evaluator, false));  //NOI18N
            cache[8] = cp;
        }
        return cp;
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
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return new ClassPath[]{getBootClassPath()};
        }
        if (ClassPath.COMPILE.equals(type)) {
            ClassPath[] l = new ClassPath[2];
            l[0] = getCompileTimeClasspath(0);
            l[1] = getCompileTimeClasspath(1);
            return l;
        }
        if (ClassPath.SOURCE.equals(type)) {
            ClassPath[] l = new ClassPath[2];
            l[0] = getSourcepath(0);
            l[1] = getSourcepath(1);
            return l;
        }
        assert false;
        return null;
    }

    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots). Valid types are SOURCE and COMPILE.
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(0);
        }
        if (ClassPath.COMPILE.equals(type)) {
            return getCompileTimeClasspath(0);
        }
        assert false;
        return null;
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        this.dirCache.clear();
    }

    public synchronized void propertiesChanged(AntProjectEvent ev) {
        this.dirCache.clear();
    }

}

