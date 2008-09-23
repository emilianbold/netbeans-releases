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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.classpath;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.groovy.grailsproject.GrailsSources;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public class SourceRoots {

    private final FileObject projectRoot;

    public SourceRoots(FileObject projectRoot) {
        this.projectRoot = projectRoot;
    }

    public FileObject[] getRoots() {
        List<FileObject> result = new ArrayList<FileObject>();
        
        addRoot(SourceCategory.GRAILSAPP_CONF, result);
        addRoot(SourceCategory.GRAILSAPP_CONTROLLERS, result);
        addRoot(SourceCategory.GRAILSAPP_DOMAIN, result);
        addRoot(SourceCategory.GRAILSAPP_SERVICES, result);
        addRoot(SourceCategory.GRAILSAPP_TAGLIB, result);
        addRoot(SourceCategory.GRAILSAPP_UTILS, result);
        addRoot(SourceCategory.SCRIPTS, result);
        addRoot(SourceCategory.SRC_GROOVY, result);
        addRoot(SourceCategory.SRC_JAVA, result);
        addRoot(SourceCategory.TEST_INTEGRATION, result);
        addRoot(SourceCategory.TEST_UNIT, result);
        for (FileObject child : projectRoot.getChildren()) {
            if (child.isFolder() && VisibilityQuery.getDefault().isVisible(child) &&
                    !GrailsSources.KNOWN_FOLDERS.contains(child.getName())) {
                result.add(child);
            }
        }
        FileObject grailsAppFo = projectRoot.getFileObject("grails-app"); // NOI18N
        if (grailsAppFo != null) {
            for (FileObject child : grailsAppFo.getChildren()) {
                if (child.isFolder() && VisibilityQuery.getDefault().isVisible(child) &&
                        !GrailsSources.KNOWN_FOLDERS_IN_GRAILS_APP.contains(child.getName())) {
                    result.add(child);
                }
            }
        }

        return result.toArray(new FileObject[result.size()]);
    }

    public List<URL> getRootURLs() {
        List<URL> urls = new ArrayList<URL>();
        try {
            for (FileObject fileObject : getRoots()) {
                urls.add(FileUtil.toFile(fileObject).toURI().toURL());
            }
        } catch (MalformedURLException murle) {
            Exceptions.printStackTrace(murle);
        }
        return urls;
    }

    private void addRoot(SourceCategory category, List<FileObject> roots) {
        FileObject root = projectRoot.getFileObject(category.getRelativePath());
        if (root != null) {
            roots.add(root);
        }
    }

}
