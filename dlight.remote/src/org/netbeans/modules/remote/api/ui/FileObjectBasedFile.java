/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.api.ui;

import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author ak119685
 */
/*package*/ class FileObjectBasedFile extends File {

    private final FileObject fo;
    private File[] NO_CHILDREN = new File[0];

    public FileObjectBasedFile(String path) {
        super(path);
        this.fo = null;
    }

    public FileObjectBasedFile(FileObject fo) {
        super("".equals(fo.getPath()) ? "/" : fo.getPath()); // NOI18N
        this.fo = fo;
    }

    @Override
    public boolean isDirectory() {
        return fo == null ? false : fo.isFolder();
    }

    @Override
    public boolean exists() {
        return fo == null ? false : fo.isValid();
    }

    @Override
    public String getPath() {
        return fo == null ? super.getPath() : fo.getPath();
    }

    @Override
    public String getAbsolutePath() {
        return fo == null ? super.getAbsolutePath() : fo.getPath();
    }


    @Override
    public File getParentFile() {
        if (fo == null) {
            return null;
        }
        FileObject parent = fo.getParent();
        return parent == null ? null : new FileObjectBasedFile(parent);
    }

    @Override
    public boolean isFile() {
        return !isDirectory();
    }

    @Override
    public String getAbsolutePath() {
        String res = super.getAbsolutePath();
        if (res != null && Utilities.isWindows()) {
            res = res.replace('\\', '/'); // NOI18N
            while (res.startsWith("//")) { // NOI18N
                res = res.substring(1);
            }
        }
        return res;
    }

    @Override
    public File[] listFiles() {
        if (fo == null) {
            return NO_CHILDREN;
        }

        FileObject[] children = fo.getChildren();

        if (children.length == 0) {
            fo.refresh();
            children = fo.getChildren();
        }

        File[] res = new File[children.length];
        int idx = 0;
        for (FileObject child : children) {
            res[idx++] = new FileObjectBasedFile(child);
        }

        return res;
    }

    @Override
    public File getCanonicalFile() throws IOException {
        return this;
    }
}
