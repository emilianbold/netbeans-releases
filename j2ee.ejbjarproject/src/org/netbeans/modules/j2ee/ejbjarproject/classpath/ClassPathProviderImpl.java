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
package org.netbeans.modules.j2ee.ejbjarproject.classpath;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Defines the various class paths for a J2SE project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, AntProjectListener {
    
    private final AntProjectHelper helper;
    private final Reference[] cache = new SoftReference[8];

    private final Map dirCache = new HashMap ();

    public ClassPathProviderImpl(AntProjectHelper helper) {
        this.helper = helper;
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
    
    private FileObject getPrimarySrcDir() {
        return getDir("src.dir"); // NOI18N
    }
    
    private FileObject getBuildClassesDir() {
        return getDir("build.classes.dir");    //NOI18N
    }
    
    private FileObject getBuildJar() {
        return getDir("dist.jar");            //NOI18N
    }
    
    private FileObject getDocumentBaseDir() {
        return getDir("web.docbase.dir");
    }
    
    private int getType(FileObject file) {
        FileObject dir = getPrimarySrcDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 0;
        }
        dir = getDocumentBaseDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
            return 2;
        }
        dir = getBuildClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 3;
        }
        dir = getBuildJar();
        if (dir != null && (dir.equals(file))) {     //TODO: When MasterFs check also isParentOf
            return 3;
        }
        return -1;
    }
    
    private ClassPath getCompileTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 2) {
            return null;
        }
        if (type == 2) type = 0;
        ClassPath cp = null;
        if (cache[3+type] == null || (cp = (ClassPath)cache[3+type].get()) == null) {
            if (type == 0) {    
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper, "javac.classpath"));      //NOI18N
            }
            cache[3+type] = new SoftReference(cp);
        }
        return cp;
        
    }
    
    private ClassPath getRunTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 4) {
            return null;
        } 
        switch (type){
            case 2: type = 0; break;
            case 3:
            case 4: type -=3; break;
        }
        
        ClassPath cp = null;
        if (cache[6+type] == null || (cp = (ClassPath)cache[6+type].get())== null) {
            if (type == 0) {
                //XXX : It should return a classpath for run.classpath property, but
                // the run.classpath property was removed from the webproject in the past
                // and I'm a little lazy to return it back in the code:)). In this moment
                // the run classpath equals to the debug classpath. If the debug classpath
                // will be different from the run classpath, then the run classpath should
                // be returned back. 
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper, "debug.classpath")); // NOI18N
            }
            cache[6+type] = new SoftReference(cp);
        }
        return cp;
    }
    
    private ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 2) {
            return null;
        }
        ClassPath cp = null;
        if (cache[type] == null || (cp = (ClassPath)cache[type].get()) == null) {
            if (type == 0) {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper, "src.dir")); // NOI18N
            }
            else {
                if (type == 2){
                    // TODO We need in the classpath the src.dir as well. 
                    cp = ClassPathFactory.createClassPath(
                    new ProjectClassPathImplementation(helper, "web.docbase.dir")); // NOI18N
                }
            }
            cache[type] = new SoftReference(cp);
        }
        return cp;
    }
    
    private ClassPath getBootClassPath() {
        ClassPath cp = null;
        if (cache[7] == null || (cp = (ClassPath)cache[7].get()) == null) {
            cp = ClassPathFactory.createClassPath(new BootClassPathImplementation(helper));
            cache[7] = new SoftReference(cp);
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
            return (ClassPath[])l.toArray(new ClassPath[l.size()]);
        }
        if (ClassPath.SOURCE.equals(type)) {
            List/*<ClassPath>*/ l = new ArrayList(2);
            FileObject d = getPrimarySrcDir();
            if (d != null) {
                l.add(getSourcepath(d));
            }
            return (ClassPath[])l.toArray(new ClassPath[l.size()]);
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

