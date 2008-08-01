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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathFactory;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathProvider;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Defines the various class paths for a J2SE project.
 */
public final class GsfClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {
    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final WebSources sourceRoots;
    private final ClassPath[] cache = new ClassPath[8];

    private final Map<String,FileObject> dirCache = new HashMap<String,FileObject>();

    public GsfClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, WebSources sources) {
        this.helper = helper;
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert this.projectDirectory != null;
        this.evaluator = evaluator;
        this.sourceRoots = sources;
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

    
    private FileObject[] getPrimarySrcPath() {
        List<FileObject> roots = new ArrayList<FileObject>();
        SourceGroup[] sourceGroups = sourceRoots.getSourceGroups(Sources.TYPE_GENERIC);
        for (SourceGroup sg : sourceGroups) {
            roots.add(sg.getRootFolder());
        }
        
        return roots.toArray(new FileObject[roots.size()]);
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
                    cp = ClassPathFactory.createClassPath(new GsfSourcePathImplementation (this.sourceRoots, helper, evaluator));
                    break;
                //case 1:
                //    cp = ClassPathFactory.createClassPath(new SourcePathImplementation (this.testSourceRoots));
                //    break;
            }
        }
        cache[type] = cp;
        return cp;
    }
    
    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache[7];
        if ( cp== null ) {
            cp = ClassPathFactory.createClassPath(new GsfBootClassPathImplementation(projectDirectory, evaluator));
            cache[7] = cp;
        }
        return cp;
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        /*if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(file);
        } else */ if (type.equals(ClassPath.SOURCE)) {
            return getSourcepath(file);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else if (type.equals(ClassPath.COMPILE)) {
            // Bogus
            return getBootClassPath();
        } else if (type.equals("js/library")) { // NOI18N
            return getSourcepath(0);
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
        //if (ClassPath.COMPILE.equals(type)) {
        //    ClassPath[] l = new ClassPath[2];
        //    l[0] = getCompileTimeClasspath(0);
        //    l[1] = getCompileTimeClasspath(1);
        //    return l;
        //}
        if (ClassPath.SOURCE.equals(type)) {
            ClassPath[] l = new ClassPath[1];
            l[0] = getSourcepath(0);
            //l[1] = getSourcepath(1);
            return l;
        }
        return null;
    }

    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots). Valid types are BOOT, SOURCE and COMPILE.
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.BOOT.equals(type)) {
             return getBootClassPath();
        }
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(0);
        }
        if (ClassPath.COMPILE.equals(type)) {
             return getBootClassPath();
        }
        return null;
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }
}
