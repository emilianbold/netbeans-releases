/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.List;
import org.openide.util.Utilities;

/**
 * Helper class for getting all the possible document roots, OS dependent.
 * It is e.g. "/var/www" for Linux, "C:\Program Files\Apache\htdocs" for Windows etc.
 * @author Tomas Mysik
 */
public final class DocumentRoots {

    static final FilenameFilter APACHE_FILENAME_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.toLowerCase().startsWith("apache"); // NOI18N
        }
    };

    private DocumentRoots() {
    }

    /**
     * Get the OS dependent {@link Root roots} of document roots and its URLs for the given project name.
     * @param projectName project name for which the directory in the document root is searched.
     *                    Can be <code>null</code> if it is not needed.
     * @return list of {@link Root roots} (can be empty).
     */
    public static List<Root> getRoots(String projectName) {
        if (isSolaris()) {
            return DocumentRootsSolaris.getDocumentRoots(projectName);
        } else if (Utilities.isWindows()) {
            return DocumentRootsWindows.getDocumentRoots(projectName);
        } else if (Utilities.isMac()) {
            return DocumentRootsMac.getDocumentRoots(projectName);
        } else if (Utilities.isUnix()) {
            return DocumentRootsUnix.getDocumentRoots(projectName);
        }
        return Collections.<Root>emptyList();
    }

    private static boolean isSolaris() {
        return (Utilities.getOperatingSystem() & Utilities.OS_SOLARIS) != 0
                || (Utilities.getOperatingSystem() & Utilities.OS_SUNOS) != 0;
    }

    /**
     * Holder for pair: document root - its URL. It also contains flag whether this pair is preferred or not
     * (e.g. "~/public_html" is preferred to "/var/www" on Linux). Only writable directories can be preferred.
     */
    public static final class Root {
        private final String documentRoot;
        private final String url;
        private final boolean preferred;

        public Root(String documentRoot, String url, boolean preferred) {
            this.documentRoot = documentRoot;
            this.url = url;
            this.preferred = preferred;
        }

        public String getDocumentRoot() {
            return documentRoot;
        }

        public String getUrl() {
            return url;
        }

        public boolean isPreferred() {
            return preferred;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append(getClass().getName());
            sb.append(" { documentRoot : ");
            sb.append(documentRoot);
            sb.append(" , url : ");
            sb.append(url);
            sb.append(" , preferred : ");
            sb.append(preferred);
            sb.append(" }");
            return sb.toString();
        }
    }

    static String getFolderName(File location, String name) {
        if (name == null) {
            return location.getAbsolutePath();
        }
        return new File(location, name).getAbsolutePath();
    }

    static String getDefaultUrl(String urlPart) {
        return getDefaultUrl(urlPart, null);
    }

    static String getDefaultUrl(String urlPart, Integer port) {
        StringBuilder url = new StringBuilder(100);
        url.append("http://localhost"); // NOI18N
        if (port != null) {
            url.append(":"); // NOI18N
            url.append(port);
        }
        url.append("/"); // NOI18N
        if (urlPart != null) {
            url.append(urlPart);
            url.append("/"); // NOI18N
        }
        return url.toString();
    }

    /**
     * Return "htdocs" directory or null.
     */
    static File findHtDocsDirectory(File startDir, FilenameFilter filenameFilter) {
        String[] subDirNames = startDir.list(filenameFilter);
        if (subDirNames == null || subDirNames.length == 0) {
            return null;
        }
        for (String subDirName : subDirNames) {
            File subDir = new File(startDir, subDirName);
            File htDocs = new File(subDir, "htdocs"); // NOI18N
            if (htDocs.isDirectory()) {
                return htDocs;
            }
            return findHtDocsDirectory(subDir, filenameFilter);
        }
        return null;
    }
}
