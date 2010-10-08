/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.api.remote;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileUtil {

    public static boolean fileExists(String absolutePath, ExecutionEnvironment executionEnvironment) {
        FileObject fo = getFileObject(absolutePath, executionEnvironment);
        return (fo != null && fo.isValid());
    }

    public static boolean isDirectory(String absolutePath, ExecutionEnvironment executionEnvironment) {
        FileObject fo = getFileObject(absolutePath, executionEnvironment);
        return (fo != null && fo.isFolder());
    }

    private RemoteFileUtil() {}
    
    public static FileObject getFileObject(String absolutePath, ExecutionEnvironment execEnv, RemoteProject.Mode remoteMode) {
        switch (remoteMode) {
            case LOCAL_SOURCES:
                return getFileObject(absolutePath, ExecutionEnvironmentFactory.getLocal());
            case REMOTE_SOURCES:
                return getFileObject(absolutePath, execEnv);
            default:
                throw new IllegalArgumentException("Unexpected remote mode: " + remoteMode); //NOI18N
        }
    }

    public static FileObject getFileObject(String absolutePath, ExecutionEnvironment execEnv) {
        String normalizedPath = CndFileUtils.normalizeAbsolutePath(absolutePath);
        if (CndUtils.isDebugMode() && ! normalizedPath.equals(absolutePath)) {
            CndUtils.assertTrueInConsole(false, "Warning: path is not normalized: " + absolutePath);
        }
        if (execEnv.isRemote()) {
            return FileSystemProvider.getFileSystem(execEnv, "/").findResource(normalizedPath); //NOI18N
        } else {
            return CndFileUtils.toFileObject(normalizedPath);
        }
    }

    public static FileObject getFileObject(String absolutePath, Project project) {
        String normalizedPath = CndFileUtils.normalizeAbsolutePath(absolutePath);
        if (CndUtils.isDebugMode() && ! normalizedPath.equals(absolutePath)) {
            CndUtils.assertTrueInConsole(false, "Warning: path is not normalized: " + absolutePath);
        }
        if (project != null) {
            RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
            if (remoteProject != null) {
                if (remoteProject.getRemoteMode() == RemoteProject.Mode.REMOTE_SOURCES) {
                    ExecutionEnvironment execEnv = remoteProject.getSourceFileSystemHost();
                    return FileSystemProvider.getFileSystem(execEnv, "/").findResource(normalizedPath); //NOI18N
                }
            }
        }
        return CndFileUtils.toFileObject(normalizedPath);
    }

    public static String getAbsolutePath(FileObject fileObject) {
        return fileObject.getPath();
    }

    public static String getCanonicalPath(FileObject fo) throws IOException {
        //XXX:fullRemote
        File file = FileUtil.toFile(fo);
        return (file == null) ? fo.getPath() : file.getCanonicalPath();
    }

    public static JFileChooser createFileChooser(RemoteProject.Mode remoteMode, ExecutionEnvironment execEnv,
            String titleText, String buttonText, int mode, FileFilter[] filters,
            String initialPath, boolean useParent) {

        return createFileChooser(
                (remoteMode == RemoteProject.Mode.REMOTE_SOURCES) ? execEnv : ExecutionEnvironmentFactory.getLocal(),
                titleText, buttonText, mode, filters, initialPath, useParent);
    }

    public static JFileChooser createFileChooser(ExecutionEnvironment execEnv,
            String titleText, String buttonText, int mode, FileFilter[] filters,
            String initialPath, boolean useParent) {

        JFileChooser fileChooser;
        if (execEnv.isLocal()) {
            fileChooser = new FileChooser(
                    titleText,
                    buttonText,
                    mode,
                    null,
                    initialPath,
                    false);
        } else {            
            fileChooser = new FileChooserBuilder(execEnv).createFileChooser(initialPath);
            fileChooser.setApproveButtonText(buttonText);
            fileChooser.setDialogTitle(titleText);
            fileChooser.setFileSelectionMode(mode);
        }
        return fileChooser;
    }
}
