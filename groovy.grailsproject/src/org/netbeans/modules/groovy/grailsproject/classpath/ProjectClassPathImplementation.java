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

package org.netbeans.modules.groovy.grailsproject.classpath;

import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileObject;

final class ProjectClassPathImplementation implements ClassPathImplementation {

    private List<PathResourceImplementation> resources;
    private final File projectRoot;

    public ProjectClassPathImplementation(FileObject projectRoot) {
        this.projectRoot = FileUtil.toFile(projectRoot);
    }
    
    public synchronized List<PathResourceImplementation> getResources() {
        if (this.resources == null) {
            this.resources = this.getPath();
        }
        return this.resources;
    }

    private List<PathResourceImplementation> getPath() {
        List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
        // lib directory from project root
        addLibs(projectRoot, result);
        File pluginsDir = new File(projectRoot, "plugins"); // NOI18N
        if (pluginsDir.isDirectory()) {
            for (String name : pluginsDir.list()) {
                File file = new File(pluginsDir, name);
                if (file.isDirectory()) {
                    // lib directories of installed plugins
                    addLibs(file, result);
                    // sources of installed plugins
                    addSources(file, result);
                }
            }
        }
        
        return Collections.unmodifiableList(result);
    }

    private static void addLibs(File root, List<PathResourceImplementation> result) {
        File[] jars = new File(root, "lib").listFiles(); // NOI18N
        if (jars != null) {
            for (File f : jars) {
                try {
                    if (f.isFile()) {
                        URL entry = f.toURI().toURL();
                        if (FileUtil.isArchiveFile(entry)) {
                            entry = FileUtil.getArchiveRoot(entry);
                            result.add(ClassPathSupport.createResource(entry));
                        }
                    }
                } catch (MalformedURLException mue) {
                    assert false : mue;
                }
            }
        }
    }

    // XXX I am handling plugin sources as 'library' for owning project, is that correct?
    private static void addSources(File root, List<PathResourceImplementation> result) {
        SourceRoots sourceRoots = new SourceRoots(FileUtil.toFileObject(root));
        for (URL url : sourceRoots.getRootURLs()) {
            result.add(ClassPathSupport.createResource(url));
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

}
