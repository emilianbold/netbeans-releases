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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
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

        @Override
        public String toString() {
            return "FileInfo{" + "absolutePath=" + absolutePath + "directory=" + directory + '}';//NOI18N
        }
    }

    public interface CndFileSystemProblemListener {
        void problemOccurred(FSPath fsPath);
        void recovered(FileSystem fileSystem);
    }

    private static CndFileSystemProvider getDefault() {
        return DEFAULT;
    }

    public static void addFileSystemProblemListener(CndFileSystemProblemListener listener, FileSystem fileSystem) {
        getDefault().addFileSystemProblemListenerImpl(listener, fileSystem);
    }
    
    public static void removeFileSystemProblemListener(CndFileSystemProblemListener listener, FileSystem fileSystem) {
        getDefault().removeFileSystemProblemListenerImpl(listener, fileSystem);
    }

    public static File toFile(FileObject fileObject) {
        // TODO: do we still need this?
        File file = FileUtil.toFile(fileObject);
        if (file == null && fileObject != null && !fileObject.isValid()) {
            file = new File(fileObject.getPath());
        }
        return file;
    }
    
    /**
     * JFileChooser works in the term of files.
     * For such "perverted" files FileUtil.toFileObject won't work.
     * @param file
     * @return 
     */
    public static FileObject toFileObject(File file) {
        return getDefault().toFileObjectImpl(file);
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
        CndUtils.assertNotNull(result, "Null file object for ", absPath); //NOI18N
        return result;
    }

    public static FileObject urlToFileObject(CharSequence url) {
        return getDefault().urlToFileObjectImpl(url);
    }

    public static CharSequence toUrl(FSPath fsPath) {
        return getDefault().toUrlImpl(fsPath);
    }
    
    public static CharSequence toUrl(FileSystem fileSystem, CharSequence absPath) {
        return getDefault().toUrlImpl(fileSystem, absPath);
    }

    public static CharSequence fileObjectToUrl(FileObject fileObject) {
        CharSequence result = getDefault().fileObjectToUrlImpl(fileObject);
        CndUtils.assertNotNull(result, "Null URL for file object ", fileObject); //NOI18N
        return result;
    }
    
    public static CharSequence getCanonicalPath(FileSystem fileSystem, CharSequence absPath) throws IOException {
        CndUtils.assertAbsolutePathInConsole(absPath.toString());
        return getDefault().getCanonicalPathImpl(fileSystem, absPath);
    }
    
    public static FileObject getCanonicalFileObject(FileObject fo) throws IOException {
        return getDefault().getCanonicalFileObjectImpl(fo);
    }
    
    public static String getCanonicalPath(FileObject fo) throws IOException {
        return getDefault().getCanonicalPathImpl(fo);        
    }
    
    public static String normalizeAbsolutePath(FileSystem fs, String absPath) {
        return getDefault().normalizeAbsolutePathImpl(fs, absPath);
    }
    
    public static void addFileChangeListener(FileChangeListener listener) {
        getDefault().addFileChangeListenerImpl(listener);
    }

    public static void addFileChangeListener(FileChangeListener listener, FileSystem fileSystem, String path) {
        getDefault().addFileChangeListenerImpl(listener, fileSystem, path);
    }

    public static void removeFileChangeListener(FileChangeListener listener) {
        getDefault().removeFileChangeListenerImpl(listener);
    }

    public static void removeFileChangeListener(FileChangeListener listener, FileSystem fileSystem, String path) {
        getDefault().removeFileChangeListenerImpl(listener, fileSystem, path);
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
    protected abstract CharSequence toUrlImpl(FSPath fSPath);
    protected abstract CharSequence toUrlImpl(FileSystem fileSystem, CharSequence absPath);
    protected abstract FileObject urlToFileObjectImpl(CharSequence url);
    protected abstract FileObject toFileObjectImpl(File file);

    protected abstract CharSequence getCanonicalPathImpl(FileSystem fileSystem, CharSequence absPath) throws IOException;
    protected abstract FileObject getCanonicalFileObjectImpl(FileObject fo) throws IOException;
    protected abstract String getCanonicalPathImpl(FileObject fo) throws IOException;
    
    protected abstract String normalizeAbsolutePathImpl(FileSystem fs, String absPath);
    
    protected abstract boolean addFileChangeListenerImpl(FileChangeListener listener);
    protected abstract boolean removeFileChangeListenerImpl(FileChangeListener listener);
    
    protected abstract boolean addFileChangeListenerImpl(FileChangeListener listener, FileSystem fileSystem, String path);    
    protected abstract boolean removeFileChangeListenerImpl(FileChangeListener listener, FileSystem fileSystem, String path);    

    protected abstract void removeFileSystemProblemListenerImpl(CndFileSystemProblemListener listener, FileSystem fileSystem);
    protected abstract void addFileSystemProblemListenerImpl(CndFileSystemProblemListener listener, FileSystem fileSystem);
    
    private static class DefaultProvider extends CndFileSystemProvider {
        private static final String FILE_PROTOCOL_PREFIX = "file:"; // NOI18N

        private CndFileSystemProvider[] cache;

        DefaultProvider() {
            Collection<? extends CndFileSystemProvider> instances =
                    Lookup.getDefault().lookupAll(CndFileSystemProvider.class);
            cache = instances.toArray(new CndFileSystemProvider[instances.size()]);
            CndUtils.assertTrueInConsole(cache.length > 0, "CndFileSystemProvider NOT FOUND"); // NOI18N
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
                fo = InvalidFileObjectSupport.getInvalidFileObject(file);
            }
            return fo;
        }

        @Override
        protected FileObject toFileObjectImpl(File file) {
            FileObject fo;
            for (CndFileSystemProvider provider : cache) {
                fo = provider.toFileObjectImpl(file);
                if (fo != null) {
                    return fo;
                }
            }
            fo = FileUtil.toFileObject(file);
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(file);
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
            String path = url.toString();
            File file;
            if (path.startsWith(FILE_PROTOCOL_PREFIX)) {
                try {
                    URL u = new URL(path);
                    file = FileUtil.normalizeFile(new File(u.toURI()));
                } catch (IllegalArgumentException ex) {
                    CndUtils.getLogger().log(Level.WARNING, "CndFileSystemProvider.urlToFileObjectImpl can not convert {0}:\n{1}", new Object[]{path, ex.getLocalizedMessage()});
                    return null;
                } catch (URISyntaxException ex) {
                    CndUtils.getLogger().log(Level.WARNING, "CndFileSystemProvider.urlToFileObjectImpl can not convert {0}:\n{1}", new Object[]{path, ex.getLocalizedMessage()});
                    return null;
                } catch (MalformedURLException ex) {
                    CndUtils.getLogger().log(Level.WARNING, "CndFileSystemProvider.urlToFileObjectImpl can not convert {0}:\n{1}", new Object[]{path, ex.getLocalizedMessage()});
                    return null;
                }       
            } else {
                file = new File(FileUtil.normalizePath(path));
            }
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
        protected CharSequence toUrlImpl(FSPath fSPath) {
            for (CndFileSystemProvider provider : cache) {
                CharSequence url = provider.toUrlImpl(fSPath);
                if (url != null) {
                    return url;
                }
            }
            return fSPath.getPath();
        }

        @Override
        protected CharSequence toUrlImpl(FileSystem fileSystem, CharSequence absPath) {
            for (CndFileSystemProvider provider : cache) {
                CharSequence url = provider.toUrlImpl(fileSystem, absPath);
                if (url != null) {
                    return url;
                }
            }
            return absPath;
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

        @Override
        protected FileObject getCanonicalFileObjectImpl(FileObject fo) throws IOException {
            for (CndFileSystemProvider provider : cache) {
                FileObject canonical = provider.getCanonicalFileObjectImpl(fo);
                if (canonical != null) {
                    return canonical;
                }
            }
            return fo;
        }

        @Override
        protected String getCanonicalPathImpl(FileObject fo) throws IOException {
            for (CndFileSystemProvider provider : cache) {
                String canonical = provider.getCanonicalPathImpl(fo);
                if (canonical != null) {
                    return canonical;
                }
            }
            return fo.getPath();
        }

        @Override
        protected String normalizeAbsolutePathImpl(FileSystem fs, String absPath) {
            for (CndFileSystemProvider provider : cache) {
                String normalized = provider.normalizeAbsolutePathImpl(fs, absPath);
                if (normalized != null) {
                    return normalized;
                }
            }
            return absPath;
        }

        @Override
        protected boolean addFileChangeListenerImpl(FileChangeListener listener, FileSystem fileSystem, String path) {
            for (CndFileSystemProvider provider : cache) {
                if (provider.addFileChangeListenerImpl(listener, fileSystem, path)) {
                    return true;
                }
            }
            if (CndFileUtils.isLocalFileSystem(fileSystem)) {
                FileUtil.addFileChangeListener(listener, FileUtil.normalizeFile(new File(path)));
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected boolean removeFileChangeListenerImpl(FileChangeListener listener, FileSystem fileSystem, String path) {
            for (CndFileSystemProvider provider : cache) {
                if (provider.removeFileChangeListenerImpl(listener, fileSystem, path)) {
                    return true;
                }
            }
            if (CndFileUtils.isLocalFileSystem(fileSystem)) {
                FileUtil.removeFileChangeListener(listener, FileUtil.normalizeFile(new File(path)));
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected boolean addFileChangeListenerImpl(FileChangeListener listener) {
            for (CndFileSystemProvider provider : cache) {
                provider.addFileChangeListenerImpl(listener);
            }
            return true;
        }

        @Override
        protected boolean removeFileChangeListenerImpl(FileChangeListener listener) {
            for (CndFileSystemProvider provider : cache) {
                provider.removeFileChangeListenerImpl(listener);
            }
            return true;
        }

        @Override
        protected void addFileSystemProblemListenerImpl(CndFileSystemProblemListener listener, FileSystem fileSystem) {
            for (CndFileSystemProvider provider : cache) {
                provider.addFileSystemProblemListenerImpl(listener, fileSystem);
            }
        }

        @Override
        protected void removeFileSystemProblemListenerImpl(CndFileSystemProblemListener listener, FileSystem fileSystem) {
            for (CndFileSystemProvider provider : cache) {
                provider.removeFileSystemProblemListenerImpl(listener, fileSystem);
            }
        }        
    }
}
