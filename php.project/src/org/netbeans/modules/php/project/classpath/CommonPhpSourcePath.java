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

package org.netbeans.modules.php.project.classpath;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;

/**
 * Helper class for sharing the same code between {@link org.netbeans.modules.php.project.api.PhpSourcePath}
 * and {@link ClassPathProviderImpl}.
 * @author Tomas Mysik
 */
public final class CommonPhpSourcePath {

    // GuardedBy(CommonPhpSourcePath.class)
    private static List<FileObject> internalFolders = null;

    private CommonPhpSourcePath() {
    }

    public static synchronized List<FileObject> getInternalPath() {
        if (internalFolders == null) {
            internalFolders = getInternalFolders();
        }
        return internalFolders;
    }

    // workaround because gsf uses toFile() and this causes NPE for SFS
    // see #131401 for more information
    private static List<FileObject> getInternalFolders() {
        assert Thread.holdsLock(CommonPhpSourcePath.class);

        // FS AtomicAction should not be needed (synchronized)
        FileObject sfsFolder = Repository.getDefault().getDefaultFileSystem().findResource("PHP/RuntimeLibraries"); // NOI18N
        for (FileObject fo : sfsFolder.getChildren()) {
            // XXX need to handle file updates as well
            if (FileUtil.toFile(fo) != null) {
                continue;
            }
            InputStream is = null;
            ByteArrayOutputStream bos = null;
            try {
                is = fo.getInputStream();
                bos = new ByteArrayOutputStream();
                FileUtil.copy(is, bos);
            } catch (IOException exc) {
                Exceptions.printStackTrace(exc);
            } finally {
                closeStreams(is);
            }
            OutputStream os = null;
            try {
                os = fo.getOutputStream();
                os.write(bos.toByteArray());
            } catch (IOException exc) {
                Exceptions.printStackTrace(exc);
            } finally {
                closeStreams(os, bos);
            }
        }
        File file = FileUtil.toFile(sfsFolder);
        assert file != null : "Folder PHP/RuntimeLibraries cannot be resolved as a java.io.File";
        List<FileObject> folders = new ArrayList<FileObject>();
        folders.add(sfsFolder);
        folders.addAll(PhpSourcePath.getPreindexedFolders());
        return folders;
    }

    private static void closeStreams(Closeable... streams) {
        for (Closeable stream : streams) {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
