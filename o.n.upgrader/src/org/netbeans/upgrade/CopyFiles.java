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
package org.netbeans.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;

/** Does copy of files according to include/exclude patterns.
 *
 * @author Jiri Skrivanek
 */
final class CopyFiles extends Object {

    private File sourceRoot;
    private File targetRoot;
    private IncludeExclude includeExclude;
    private static final Logger LOGGER = Logger.getLogger(CopyFiles.class.getName());

    private CopyFiles(File source, File target, IncludeExclude includeExclude) {
        this.sourceRoot = source;
        this.targetRoot = target;
        this.includeExclude = includeExclude;
    }

    public static void copyDeep(File source, File target, IncludeExclude includeExclude) throws IOException {
        CopyFiles copyFiles = new CopyFiles(source, target, includeExclude);
        LOGGER.fine("Copying from: " + copyFiles.sourceRoot + "\nto: " + copyFiles.targetRoot);  //NOI18N
        copyFiles.copyFolder(copyFiles.sourceRoot);
    }

    private void copyFolder(File sourceFolder) throws IOException {
        File[] srcChildren = sourceFolder.listFiles();
        for (File child : srcChildren) {
            if (child.isDirectory()) {
                copyFolder(child);
            } else {
                String relativePath = getRelativePath(this.sourceRoot, child);
                if (includeExclude.contains(relativePath)) {
                    LOGGER.fine("Path: " + relativePath);
                    copyFile(child, new File(targetRoot, relativePath));
                }
            }
        }
    }

    /** Returns slash separated path relative to given root. */
    private static String getRelativePath(File root, File file) {
        String result = file.getAbsolutePath().substring(root.getAbsolutePath().length());
        result = result.replace('\\', '/');  //NOI18N
        if (result.startsWith("/") && !result.startsWith("//")) {  //NOI18N
            result = result.substring(1);
        }
        return result;
    }

    /** Copy source file to target file. It creates necessary sub folders.
     * @param sourceFile source file
     * @param targetFile target file
     * @throws java.io.IOException if copying fails
     */
    private static void copyFile(File sourceFile, File targetFile) throws IOException {
        ensureParent(targetFile);
        InputStream ins = null;
        OutputStream out = null;
        try {
            ins = new FileInputStream(sourceFile);
            out = new FileOutputStream(targetFile);
            FileUtil.copy(ins, out);
        } finally {
            if (ins != null) {
                ins.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /** Creates parent of given file, if doesn't exist. */
    private static void ensureParent(File file) throws IOException {
        final File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Cannot create folder: " + parent.getAbsolutePath());  //NOI18N
            }
        }
    }
}
