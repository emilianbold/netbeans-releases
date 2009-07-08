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

package org.netbeans.modules.maven;

import java.io.File;
import java.io.IOException;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * implementation of CacheDirectoryProvider that places the cache directory in the user
 * directory space of the currently running IDE.
 * @author mkleint
 */
public class CacheDirProvider implements CacheDirectoryProvider {
    private final NbMavenProjectImpl project;

    CacheDirProvider(NbMavenProjectImpl prj) {
        project = prj;
    }

    public FileObject getCacheDirectory() throws IOException {
        int code = project.getProjectDirectory().getPath().hashCode();
        File cacheDir = new File(getCacheRoot(), "" + code);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(cacheDir));
        if (fo != null) {
            return fo;
        }
        throw new IOException("Cannot create a cache directory for project at " + cacheDir); //NOI18N
    }

    private File getCacheRoot() {
        String userdir = System.getProperty("netbeans.user"); //NOI18N
        File file = new File(userdir);
        File root = new File(file, "var" + File.separator + "cache" + File.separator + "mavencachedirs"); //NOI18N
        if (!root.exists()) {
            root.mkdirs();
        }
        return root;
    }

}
