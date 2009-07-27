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

package org.netbeans.modules.spring.api;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public final class SpringUtilities {

    private static final String SPRING_CLASS_NAME = "org.springframework.core.SpringVersion"; // NOI18N
    private static final String JSTL_CLASS_NAME = "javax.servlet.jsp.jstl.core.Config"; // NOI18N
    private static final String SPRING_WEBMVC_CLASS_NAME = "org.springframework.web.servlet.DispatcherServlet"; // NOI18N

    private SpringUtilities() {}

    public static Library findSpringLibrary() {
        return getLibrary(SPRING_CLASS_NAME);
    }

    public static Library findJSTLibrary() {
        // see Issue #169105 - first try "pure" JSTL, if there isn't any then bundled JSTL will suffice
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (library.getName().startsWith("jstl")) {
                return library;
            }
        }
        return getLibrary(JSTL_CLASS_NAME);
    }

    public static Library findSpringWebMVCLibrary() {
        return getLibrary(SPRING_WEBMVC_CLASS_NAME);
    }

    public static boolean isSpringLibrary(Library library) {
        return containsClass(library, SPRING_CLASS_NAME);
    }

    public static boolean containsSpring(ClassPath cp) {
        return containsClass(cp, SPRING_CLASS_NAME);
    }

    private static Library getLibrary(String className) {
        for (Library library : LibraryManager.getDefault().getLibraries()) {
            if (containsClass(library, className)) {
                return library;
            }
        }
        return null;
    }

    private static boolean containsClass(Library library, String className) {
        List<URL> urls = library.getContent("classpath"); // NOI18N
        return containsClass(createClassPath(urls), className);
    }

    private static boolean containsClass(ClassPath classPath, String className) {
        String classRelativePath = className.replace('.', '/') + ".class"; //NOI18N
        return classPath.findResource(classRelativePath) != null;
    }

    private static ClassPath createClassPath(List<URL> roots) {
        List<URL> jarRootURLs = new ArrayList<URL>();
        for (URL url : roots) {
            // Workaround for #126307: ClassPath roots should be JAR root URL, not file URLs.
            if (FileUtil.getArchiveFile(url) == null) {
                // Not an archive root URL.
                url = FileUtil.getArchiveRoot(url);
            }
            jarRootURLs.add(url);
        }
        return ClassPathSupport.createClassPath((jarRootURLs.toArray(new URL[jarRootURLs.size()])));
    }
}
