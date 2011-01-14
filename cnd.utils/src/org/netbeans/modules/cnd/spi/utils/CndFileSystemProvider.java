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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.spi.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.support.InvalidFileObjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class CndFileSystemProvider {

    private static final CndFileSystemProvider DEFAULT = new DefaultProvider();

    public static class FileInfo {
        public final String absolutePath;
        public final boolean directory;
        public FileInfo(String absolutePath, boolean directory) {
            this.absolutePath = absolutePath;
            this.directory = directory;
        }
    }

    private static CndFileSystemProvider getDefault() {
        return DEFAULT;
    }

    public static String getCaseInsensitivePath(CharSequence path) {
        return getDefault().getCaseInsensitivePathImpl(path);
    }

    public static File toFile(FileObject fileObject) {
        // TODO: do we still need this?
        File file = FileUtil.toFile(fileObject);
        if (file == null && fileObject != null && !fileObject.isValid()) {
            file = new File(fileObject.getPath());
        }
        return file;
    }

    public static Boolean exists(CharSequence path) {
        return getDefault().existsImpl(path);
    }

    public static Boolean canRead(CharSequence path) {
        return getDefault().canReadImpl(path);
    }

    public static FileInfo[] getChildInfo(CharSequence path) {
        return getDefault().getChildInfoImpl(path);
    }

    public static FileObject toFileObject(CharSequence absPath) {
        FileObject result = getDefault().toFileObjectImpl(absPath);
        CndUtils.assertNotNull(result, "Null file object"); //NOI18N
        return result;
    }

    public static FileObject urlToFileObject(CharSequence url) {
        return getDefault().urlToFileObjectImpl(url);
    }

    public static CharSequence fileObjectToUrl(FileObject fileObject) {
        CharSequence result = getDefault().fileObjectToUrlImpl(fileObject);
        CndUtils.assertNotNull(result, "Null file object unique string"); //NOI18N
        return result;
    }
    
    public static CharSequence getCanonicalPath(FileSystem fileSystem, CharSequence absPath) throws IOException {
        CndUtils.assertAbsolutePathInConsole(absPath.toString());
        return getDefault().getCanonicalPathImpl(fileSystem, absPath);
    }

    /**
     * Checks whether the file specified by path exists or not
     * @param path
     * @return Boolean.TRUE if the file belongs to this provider file system and exists,
     * Boolean.FALSE if the file belongs to this provider file system and does NOT exist,
     * or NULL if the file does not belong to this provider file system
     */
    protected abstract Boolean existsImpl(CharSequence path);
    protected abstract Boolean canReadImpl(CharSequence path);
    protected abstract FileInfo[] getChildInfoImpl(CharSequence path);

    /** a bridge from cnd.utils to dlight.remote */
    protected abstract FileObject toFileObjectImpl(CharSequence absPath);

    protected abstract CharSequence fileObjectToUrlImpl(FileObject fileObject);
    protected abstract FileObject urlToFileObjectImpl(CharSequence url);

    protected abstract String getCaseInsensitivePathImpl(CharSequence path);
    
    protected abstract CharSequence getCanonicalPathImpl(FileSystem fileSystem, CharSequence absPath) throws IOException;

    private static class DefaultProvider extends CndFileSystemProvider {

        private CndFileSystemProvider[] cache;
        private FileSystem fileFileSystem;

        DefaultProvider() {
            Collection<? extends CndFileSystemProvider> instances =
                    Lookup.getDefault().lookupAll(CndFileSystemProvider.class);
            cache = instances.toArray(new CndFileSystemProvider[instances.size()]);
        }

        private synchronized FileSystem getFileFileSystem() {
            if (fileFileSystem == null) {
                File tmpDirFile = new File(System.getProperty("java.io.tmpdir"));
                tmpDirFile = CndFileUtils.normalizeFile(tmpDirFile);
                FileObject tmpDirFo = FileUtil.toFileObject(tmpDirFile); // File SIC!  //NOI18N
                if (tmpDirFo != null) {
                    try {
                        fileFileSystem = tmpDirFo.getFileSystem();
                    } catch (FileStateInvalidException ex) {
                        // it's no use to log it here
                    }
                }
                if (fileFileSystem == null) {
                    fileFileSystem = InvalidFileObjectSupport.getDummyFileSystem();
                }
            }
            return fileFileSystem;
        }

        @Override
        public FileObject toFileObjectImpl(CharSequence absPath) {
            FileObject  fo;
            for (CndFileSystemProvider provider : cache) {
                fo = provider.toFileObjectImpl(absPath);
                if (fo != null) {
                    return fo;
                }
            }
            // not cnd specific file => use default file system conversion
            File file = new File(FileUtil.normalizePath(absPath.toString()));
            fo = FileUtil.toFileObject(file);
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(getFileFileSystem(), file.getAbsolutePath());
            }
            return fo;
        }

        @Override
        protected Boolean canReadImpl(CharSequence path) {
            for (CndFileSystemProvider provider : cache) {
                Boolean result = provider.canReadImpl(path);
                if (result != null) {
                    return result;
                }
            }
            return new File(path.toString()).canRead();
        }

        @Override
        protected Boolean existsImpl(CharSequence path) {
            for (CndFileSystemProvider provider : cache) {
                Boolean result = provider.existsImpl(path);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        @Override
        protected FileInfo[] getChildInfoImpl(CharSequence path) {
            for (CndFileSystemProvider provider : cache) {
                FileInfo[] result = provider.getChildInfoImpl(path);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        @Override
        protected FileObject urlToFileObjectImpl(CharSequence url) {
            for (CndFileSystemProvider provider : cache) {
                FileObject fo = provider.urlToFileObjectImpl(url);
                if (fo != null) {
                    return fo;
                }
            }
            File file = new File(FileUtil.normalizePath(url.toString()));
            return FileUtil.toFileObject(file);
        }

        @Override
        protected CharSequence fileObjectToUrlImpl(FileObject fileObject) {
            for (CndFileSystemProvider provider : cache) {
                CharSequence path = provider.fileObjectToUrlImpl(fileObject);
                if (path != null) {
                    return path;
                }
            }
            return fileObject.getPath();
        }

        @Override
        public String getCaseInsensitivePathImpl(CharSequence path) {
            for (CndFileSystemProvider provider : cache) {
                String data = provider.getCaseInsensitivePathImpl(path);
                if (data != null) {
                    return data;
                }
            }
            return path.toString();
        }

        @Override
        protected CharSequence getCanonicalPathImpl(FileSystem fileSystem, CharSequence absPath) throws IOException {
            for (CndFileSystemProvider provider : cache) {
                CharSequence canonical = provider.getCanonicalPathImpl(fileSystem, absPath);
                if (canonical != null) {
                    return canonical;
                }
            }
            return absPath;
        }        
    }
}
