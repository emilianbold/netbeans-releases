/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.j2seproject.classpath;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Defines the various class paths for a J2SE project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider {
    
    private final AntProjectHelper helper;
    private final Reference[] cache = new SoftReference[7];
    
    public ClassPathProviderImpl(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    private FileObject getDir(String propname) {
        String prop = helper.evaluate(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        } else {
            return null;
        }
    }
    
    private FileObject getPrimarySrcDir() {
        return getDir("src.dir"); // NOI18N
    }
    
    private FileObject getTestSrcDir() {
        return getDir("test.src.dir"); // NOI18N
    }
    
    private FileObject getBuildClassesDir() {
        return getDir("build.classes.dir");    //NOI18N
    }
    
    private FileObject getBuildJar() {
        return getDir("dist.jar");            //NOI18N
    }
    
    private FileObject getBuildTestClassesDir() {
        return getDir("build.test.classes.dir");   //NOI18N
    }
    
    private int getType(FileObject file) {
        FileObject dir = getPrimarySrcDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 0;
        }
        dir = getTestSrcDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 1;
        }
        dir = getBuildClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 2;
        }
        dir = getBuildJar();
        if (dir != null && (dir.equals(file))) {     //TODO: When MasterFs check also isParentOf
            return 2;
        }
        dir = getBuildTestClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
            return 3;
        }
        return -1;
    }
    
    private ClassPath getCompileTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 1) {
            return null;
        }
        ClassPath cp = null;
        if (cache[2+type] == null || (cp = (ClassPath)cache[2+type].get()) == null) {
            if (type == 0) {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper,"javac.classpath"));      //NOI18N
            }
            else {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper,"javac.test.classpath")); //NOI18N
            }
            cache[2+type] = new SoftReference(cp);
        }
        return cp;
    }
    
    private ClassPath getRunTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 3) {
            return null;
        } else if (type > 1) {
            type-=2;            //Compiled source transform into source
        }
        ClassPath cp = null;
        if (cache[4+type] == null || (cp = (ClassPath)cache[4+type].get())== null) {
            if (type == 0) {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper,"run.classpath")); // NOI18N
            }
            else if (type == 1) {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper,"run.test.classpath")); // NOI18N
            }
            cache[4+type] = new SoftReference(cp);
        }
        return cp;
    }
    
    private ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 1) {
            return null;
        }
        ClassPath cp = null;
        if (cache[type] == null || (cp = (ClassPath)cache[type].get()) == null) {
            if (type == 0) {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper,"src.dir")); // NOI18N
            }
            else {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper,"test.src.dir")); // NOI18N
            }
            cache[type] = new SoftReference(cp);
        }
        return cp;
    }
    
    private ClassPath getBootClassPath() {
        ClassPath cp = null;
        if (cache[6] == null || (cp = (ClassPath)cache[6].get()) == null) {
            cp = ClassPathFactory.createClassPath(new BootClassPathImplementation(helper));
            cache[6] = new SoftReference(cp);
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
            List/*<ClassPath>*/ l = new ArrayList(2);
            FileObject d = getPrimarySrcDir();
            if (d != null) {
                l.add(getCompileTimeClasspath(d));
            }
            d = getTestSrcDir();
            if (d != null) {
                l.add(getCompileTimeClasspath(d));
            }
            return (ClassPath[])l.toArray(new ClassPath[l.size()]);
        }
        if (ClassPath.SOURCE.equals(type)) {
            List/*<ClassPath>*/ l = new ArrayList(2);
            FileObject d = getPrimarySrcDir();
            if (d != null) {
                l.add(getSourcepath(d));
            }
            d = getTestSrcDir();
            if (d != null) {
                l.add(getSourcepath(d));
            }
            return (ClassPath[])l.toArray(new ClassPath[l.size()]);
        }
        assert false;
        return null;
    }
    
}

