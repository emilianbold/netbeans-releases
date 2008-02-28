/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.earproject.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Defines the various class paths for an Enterprise Application project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {
    
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_BUILT_UNPACKED = 2;
    private static final int TYPE_BUILT_JAR = 3;
    private static final int TYPE_OTHER = -1;
    
    private static final String BUILD_CLASSES_DIR = "build.classes.dir"; // NOI18N
    private static final String DIST_JAR = "dist.jar"; // NOI18N
    private static final String DOC_BASE_DIR = "web.docbase.dir"; // NOI18N
    
    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    @SuppressWarnings("unchecked")
    private final Reference<ClassPath>[] cache = new SoftReference[8];
    
    private final Map<String,FileObject> dirCache = new HashMap<String,FileObject>();
    
    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert this.projectDirectory != null;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }
    
    private synchronized FileObject getDir(String propname) {
        FileObject fo = this.dirCache.get(propname);
        if (fo == null ||  !fo.isValid()) {
            String prop = evaluator.getProperty(propname);
            if (prop != null) {
                fo = helper.resolveFileObject(prop);
                this.dirCache.put(propname, fo);
            }
        }
        return fo;
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
     *         <dt>2</dt> <dd>built class (unpacked)</dd>
     *         <dt>3</dt> <dd>built class (in dist JAR)</dd>
     *         <dt>-1</dt> <dd>something else</dd>
     *         </dl>
     */
    private int getType(FileObject file) {
        FileObject dir = getDocumentBaseDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
            return TYPE_BUILT_UNPACKED;
        }
        dir = getBuildClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return TYPE_BUILT_JAR;
        }
        dir = getDistJar(); // not really a dir at all, of course
        if (dir != null && dir.equals(FileUtil.getArchiveFile(file))) {
            // XXX check whether this is really the root
            return TYPE_BUILT_JAR;
        }
        return TYPE_OTHER;
    }
    
    private ClassPath getCompileTimeClasspath(FileObject file) {
        int type = getType(file);
        return this.getCompileTimeClasspath(type);
    }
    
    private synchronized ClassPath getCompileTimeClasspath(int type) {
        if (type < TYPE_NORMAL || type > TYPE_BUILT_UNPACKED) {
            // Not a source file.
            return null;
        }
        if (type == TYPE_BUILT_UNPACKED) {
            type = TYPE_NORMAL;
        }
        ClassPath cp = null;
        if (cache[TYPE_BUILT_JAR + type] == null || (cp = cache[TYPE_BUILT_JAR + type].get()) == null) {
            if (type == TYPE_NORMAL) {
                cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {"javac.classpath", "debug.classpath"})); // NOI18N
            }
            cache[TYPE_BUILT_JAR + type] = new SoftReference<ClassPath>(cp);
        }
        return cp;
    }
    
    private synchronized ClassPath getRunTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < TYPE_NORMAL || type > 4) {
            // Unregistered file, or in a JAR.
            // For jar:file:$projdir/dist/*.jar!/**/*.class, it is misleading to use
            // run.classpath since that does not actually contain the file!
            // (It contains file:$projdir/build/classes/ instead.)
            return null;
        }
        switch (type){
            case TYPE_BUILT_UNPACKED: type = TYPE_NORMAL; break;
            case TYPE_BUILT_JAR:
            case 4: type -=3; break;
        }
        
        ClassPath cp = null;
        if (cache[6+type] == null || (cp = cache[6+type].get())== null) {
            if (type == TYPE_NORMAL) {
                //XXX : It should return a classpath for run.classpath property, but
                // the run.classpath property was removed from the webproject in the past
                // and I'm a little lazy to return it back in the code:)). In this moment
                // the run classpath equals to the debug classpath. If the debug classpath
                // will be different from the run classpath, then the run classpath should
                // be returned back.
                cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    projectDirectory, evaluator, new String[] {"debug.classpath"})); // NOI18N
            }
            cache[6+type] = new SoftReference<ClassPath>(cp);
        }
        return cp;
    }
    
    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = null;
        if (cache[7] == null || (cp = cache[7].get()) == null) {
            cp = ClassPathFactory.createClassPath(new BootClassPathImplementation(evaluator));
            cache[7] = new SoftReference<ClassPath>(cp);
        }
        return cp;
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(file);
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(file);
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
            l[0] = getCompileTimeClasspath(TYPE_NORMAL);
            return l;
        }
        assert false;
        return null;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }
    
}

