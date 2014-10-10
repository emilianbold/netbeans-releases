/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class NodeJsUtils {

    private NodeJsUtils() {
    }

    public static boolean isJsLibrary(Project project) {
        return getSiteRoots(project).isEmpty();
    }

    @CheckForNull
    public static File getSourceRoot(Project project) {
        for (File root : getSourceRoots(project)) {
            return root;
        }
        return null;
    }

    @CheckForNull
    public static File getSiteRoot(Project project) {
        for (File root : getSiteRoots(project)) {
            return root;
        }
        return null;
    }

    @CheckForNull
    public static File getTestRoot(Project project) {
        for (File root : getTestRoots(project)) {
            return root;
        }
        return null;
    }

    public static List<File> getSourceRoots(Project project) {
        return getRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5);
    }

    public static List<File> getSiteRoots(Project project) {
        return getRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT);
    }

    public static List<File> getTestRoots(Project project) {
        return getRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST);
    }

    public static List<File> getRoots(Project project, String type) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(type);
        List<File> roots = new ArrayList<>(sourceGroups.length);
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            File root = FileUtil.toFile(rootFolder);
            assert root != null : rootFolder;
            roots.add(root);
        }
        return roots;
    }

}
