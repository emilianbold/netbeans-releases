/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.ruby.railsprojects.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.ruby.RubyLanguage;
import org.netbeans.modules.ruby.railsprojects.SourceRoots;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Defines the various load paths for a Rails project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {

    private static final String JAVAC_CLASSPATH = "javac.classpath";    //NOI18N
    private static final String JAVAC_TEST_CLASSPATH = "javac.test.classpath";  //NOI18N
    private static final String RUN_CLASSPATH = "run.classpath";    //NOI18N
    private static final String RUN_TEST_CLASSPATH = "run.test.classpath";  //NOI18N
    
    
    private final RakeProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;
    private final ClassPath[] cache = new ClassPath[9];

    private final Map<String,FileObject> dirCache = new HashMap<String,FileObject>();

    public ClassPathProviderImpl(RakeProjectHelper helper, PropertyEvaluator evaluator, SourceRoots sourceRoots,
                                 SourceRoots testSourceRoots) {
        this.helper = helper;
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert this.projectDirectory != null;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testSourceRoots = testSourceRoots;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    private synchronized FileObject getDir(String propname) {
        FileObject fo = this.dirCache.get(propname);
        if (fo == null ||  !fo.isValid()) {
            String prop = evaluator.getProperty(propname);
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
 
    private ClassPath getJavascriptsWebClassPath() {
        ClassPath cp = cache[8];
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(new JavascriptsClassPathImplementation(projectDirectory));
            cache[8] = cp;
            
            return cp;
        } else {
            return cp;
        }
    }
    
    private FileObject[] getTestSrcDir() {
        return this.testSourceRoots.getRoots();
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
        // ALL files in the Rails project are considered sources - we don't have
        // separate test roots etc.
        // TODO - check if files are even within the project?
        return 0;
    }
    
    private synchronized ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        return this.getSourcepath(type);
    }
    
    private ClassPath getSourcepath(int type) {
        if (type < 0 || type > 1) {
            return null;
        }
        ClassPath cp = cache[type];
        if (cp == null) {
            switch (type) {
                case 0:
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation (this.sourceRoots, helper, evaluator));
                    break;
                case 1:
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(this.testSourceRoots));
                    break;
            }
        }
        cache[type] = cp;
        return cp;
    }
    
    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache[7];
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(new BootClassPathImplementation(projectDirectory, evaluator));
            cache[7] = cp;
        }
        return cp;
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        /*if (type.equals(RubyLanguage.EXECUTE)) {
            return getRunTimeClasspath(file);
        } else */ if (type.equals(RubyLanguage.SOURCE)) {
            return getSourcepath(file);
        } else if (type.equals(RubyLanguage.BOOT)) {
            return getBootClassPath();
        } else if (type.equals(RubyLanguage.COMPILE)) {
            // Bogus
            return getBootClassPath();
        } else if (type.equals("js/library")) { // NOI18N
            return getJavascriptsWebClassPath();
        } else {
            return null;
        }
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (RubyLanguage.BOOT.equals(type)) {
            return new ClassPath[]{getBootClassPath()};
        }
        if (RubyLanguage.SOURCE.equals(type)) {
            ClassPath[] l = new ClassPath[2];
            l[0] = getSourcepath(0);
            l[1] = getSourcepath(1);
            return l;
        }
        return null;
    }

    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots). Valid types are BOOT, SOURCE and COMPILE.
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        if (RubyLanguage.BOOT.equals(type)) {
             return getBootClassPath();
        }
        if (RubyLanguage.SOURCE.equals(type)) {
            return getSourcepath(0);
        }
        return null;
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }
    
    public String getPropertyName (SourceGroup sg, String type) {
        FileObject root = sg.getRootFolder();
        FileObject[] path = getPrimarySrcPath();
        for (int i=0; i<path.length; i++) {
            if (root.equals(path[i])) {
                if (RubyLanguage.COMPILE.equals(type)) {
                    return JAVAC_CLASSPATH;
                }
                else if (RubyLanguage.EXECUTE.equals(type)) {
                    return RUN_CLASSPATH;
                }
                else {
                    return null;
                }
            }
        }
        path = getTestSrcDir();
        for (int i=0; i<path.length; i++) {
            if (root.equals(path[i])) {
                if (RubyLanguage.COMPILE.equals(type)) {
                    return JAVAC_TEST_CLASSPATH;
                }
                else if (RubyLanguage.EXECUTE.equals(type)) {
                    return RUN_TEST_CLASSPATH;
                }
                else {
                    return null;
                }
            }
        }
        return null;
    }
    
}
