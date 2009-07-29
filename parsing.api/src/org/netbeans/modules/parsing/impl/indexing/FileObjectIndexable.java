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

package org.netbeans.modules.parsing.impl.indexing;

import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class FileObjectIndexable implements IndexableImpl {

    private final FileObject root;
    private final String relativePath;

    private Object url;
    private String mimeType;
    private FileObject file;

    public FileObjectIndexable (FileObject root, FileObject file) {
        this(root, FileUtil.getRelativePath(root, file));
        this.file = file;
    }

    public FileObjectIndexable (FileObject root, String relativePath) {
        Parameters.notNull("root", root); //NOI18N
        Parameters.notNull("relativePath", relativePath); //NOI18N
        this.root = root;
        this.relativePath = relativePath;
    }

//    public long getLastModified() {
//        return this.getFile().lastModified().getTime();
//    }
//
//    public String getName() {
//        if (name == null) {
//            int idx = relativePath.lastIndexOf('/'); //NOI18N
//            if (idx != -1) {
//                name = relativePath.substring(idx + 1);
//            } else {
//                name = relativePath;
//            }
//        }
//        return name;
//    }

    public String getRelativePath() {
        return relativePath;
    }

    public URL getURL() {
        if (url == null) {
            try {
                FileObject f = getFile();
                if (f != null) {
                    url = f.getURL();
                }
            } catch (FileStateInvalidException ex) {
                url = ex;
            }
        }

        return url instanceof URL ? (URL) url : null;
    }

    public String getMimeType() {
        return mimeType == null ? "content/unknown" : mimeType;
    }

    public boolean isTypeOf(String mimeType) {
        Parameters.notNull("mimeType", mimeType); //NOI18N
        if (this.mimeType == null) {
            FileObject f = getFile();
            if (f != null) {
                String mt = FileUtil.getMIMEType(f, mimeType);
                if (mt != null && !mt.equals("content/unknown")) {
                    this.mimeType = mt;
                }
            }
        }
        return this.mimeType == null ? false : this.mimeType.equals(mimeType);
    }

//    public InputStream openInputStream() throws IOException {
//        return this.getFile().getInputStream();
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileObjectIndexable other = (FileObjectIndexable) obj;
        if (this.root != other.root && (this.root == null || !this.root.equals(other.root))) {
            return false;
        }
        if (this.relativePath != other.relativePath && (this.relativePath == null || !this.relativePath.equals(other.relativePath))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.root != null ? this.root.hashCode() : 0);
        hash = 83 * hash + (this.relativePath != null ? this.relativePath.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "FileObjectIndexable@" + Integer.toHexString(System.identityHashCode(this)) + " [" + toURL(root) + "/" + getRelativePath() + "]"; //NOI18N
    }

    private FileObject getFile() {
        if (file == null) {
            file = root.getFileObject(relativePath);
        }
        return file == null ? null : file.isValid() ? file : null;
    }

    private static String toURL(FileObject f) {
        try {
            return f.getURL().toString();
        } catch (FileStateInvalidException ex) {
            return f.getPath();
        }
    }
}
