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
package org.netbeans.modules.j2ee.earproject.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Defines the various class paths for a J2SE project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {
    
    private static final String SRC_DIR = "src.dir"; // NOI18N
    private static final String BUILD_CLASSES_DIR = "build.classes.dir"; // NOI18N
    private static final String DIST_JAR = "dist.jar"; // NOI18N
    private static final String DOC_BASE_DIR = "web.docbase.dir"; // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final Reference[] cache = new SoftReference[8];

    private final Map/*<String,FileObject>*/ dirCache = new HashMap();

    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    private synchronized FileObject getDir(String propname) {
        FileObject fo = (FileObject) this.dirCache.get (propname);
        if (fo == null ||  !fo.isValid()) {
            String prop = evaluator.getProperty(propname);
            if (prop != null) {
                fo = helper.resolveFileObject(prop);
                this.dirCache.put (propname, fo);
            }
        }
        return fo;
    }
    
    private FileObject getPrimarySrcDir() {
        return getDir(SRC_DIR);
    }
       
    private FileObject getBuildClassesDir() {
        return getDir(BUILD_CLASSES_DIR);
    }
    
    private FileObject getDistJar() {
        return getDir(DIST_JAR);
    }
    
    private FileObject getDocumentBaseDir() {
        return getDir(DOC_BASE_DIR);
    }
    
     /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of: <dl>
     *         <dt>0</dt> <dd>normal source</dd>
     *         <dt>1</dt> <dd>web pages</dd>
     *         <dt>2</dt> <dd>built class (unpacked)</dd>
     *         <dt>3</dt> <dd>built class (in dist JAR)</dd>
     *         <dt>-1</dt> <dd>something else</dd>
     *         </dl>
     */
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
        dir = getDistJar(); // not really a dir at all, of course
        if (dir != null && dir.equals(FileUtil.getArchiveFile(file))) {
            // XXX check whether this is really the root
            return 3;
        }
        return -1;
    }
    
    private ClassPath getCompileTimeClasspath(FileObject file) {
        int type = getType(file);
        return this.getCompileTimeClasspath(type);
    }
    
    private ClassPath getCompileTimeClasspath(int type) {        
        if (type < 0 || type > 2) {
            // Not a source file.
            return null;
        }
        if (type == 2) type = 0;
        ClassPath cp = null;
        if (cache[3+type] == null || (cp = (ClassPath)cache[3+type].get()) == null) {
            if (type == 0) {    
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper, "${javac.classpath}:${build.classes.dir}", evaluator, false));      //NOI18N
            }
            cache[3+type] = new SoftReference(cp);
        }
        return cp;
        
    }
    
    private ClassPath getRunTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 4) {
            // Unregistered file, or in a JAR.
            // For jar:file:$projdir/dist/*.jar!/**/*.class, it is misleading to use
            // run.classpath since that does not actually contain the file!
            // (It contains file:$projdir/build/classes/ instead.)
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
                new ProjectClassPathImplementation(helper, "debug.classpath", evaluator)); // NOI18N
            }
            cache[6+type] = new SoftReference(cp);
        }
        return cp;
    }
    
    private ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        return this.getSourcepath(type);
    }
    
    private ClassPath getSourcepath(int type) {
        if (type < 0 || type > 2) {
            // Unknown.
            return null;
        }
        ClassPath cp = null;
        if (cache[type] == null || (cp = (ClassPath)cache[type].get()) == null) {
            if (type == 0) {
                cp = ClassPathFactory.createClassPath(
                new ProjectClassPathImplementation(helper, SRC_DIR, evaluator)); // NOI18N
            }
            else {
                if (type == 2){
                    // TODO We need in the classpath the src.dir as well. 
                    cp = ClassPathFactory.createClassPath(
                    new ProjectClassPathImplementation(helper, DOC_BASE_DIR, evaluator)); // NOI18N
                }
            }
            cache[type] = new SoftReference(cp);
        }
        return cp;
    }
    
    private ClassPath getBootClassPath() {
        ClassPath cp = null;
        if (cache[7] == null || (cp = (ClassPath)cache[7].get()) == null) {
            cp = ClassPathFactory.createClassPath(new BootClassPathImplementation(helper, evaluator));
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
            ClassPath[] l = new ClassPath[1];
            l[0] = getCompileTimeClasspath(0);
            return l;
        }
        if (ClassPath.SOURCE.equals(type)) {
            ClassPath[] l = new ClassPath[2];
            l[0] = getSourcepath(0);
            l[1] = getSourcepath(2);
            return l;
        }
        assert false;
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }

}

