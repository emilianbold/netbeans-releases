/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ejbcore.util;

import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.openide.filesystems.FileObject;

/**
 * Contains utilities methods for work with project classpath.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ClasspathUtils {

    private ClasspathUtils() {
    }

    public static ClassPath getCompileClassPath(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups.length > 0) {
            FileObject sourceGroupRoot = sourceGroups[0].getRootFolder();
            ClassPath classPath = ClassPath.getClassPath(sourceGroupRoot, ClassPath.COMPILE);
            return classPath;
        }
        return null;
    }

    public static boolean removeLibraryFromClasspath(Project project, Library... libraries) throws IOException {
        boolean result = false;
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sourceGroup : sourceGroups) {
            boolean cpChange = ProjectClassPathModifier.removeLibraries(
                    libraries,
                    sourceGroup.getRootFolder(),
                    JavaClassPathConstants.COMPILE_ONLY);
            if (cpChange) {
                result = true;
            }
        }
        return result;
    }

    public static void addLibraryToClasspath(Project project, Library... libraries) throws IOException {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sourceGroup : sourceGroups) {
            ProjectClassPathModifier.addLibraries(
                    libraries,
                    sourceGroup.getRootFolder(),
                    JavaClassPathConstants.COMPILE_ONLY);
        }
    }
}
