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

package org.netbeans.modules.cnd.utils.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.modules.cnd.spi.utils.CndFileExistSensitiveCache;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.support.InvalidFileObjectSupport;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * some file utilities used by CND modules due to performance reasons or other
 * @author Vladimir Voskresensky
 */
public final class CndFileUtils {
    private static final boolean TRUE_CASE_SENSITIVE_SYSTEM;
    private static final FileChangeListener FSL = new FSListener();

    private CndFileUtils() {
    }

    static {
        boolean caseSenstive;
        try {
            File tmpFile = File.createTempFile("CaseSensitiveFile", ".check"); // NOI18N
            String absPath = tmpFile.getAbsolutePath();
            absPath = absPath.toUpperCase();
            caseSenstive = !new File(absPath).exists();
            tmpFile.delete();
            FileUtil.addFileChangeListener(FSL);
        } catch (IOException ex) {
            caseSenstive = Utilities.isUnix() && !Utilities.isMac();
        }
        TRUE_CASE_SENSITIVE_SYSTEM = caseSenstive;
    }

    public static boolean isSystemCaseSensitive() {
        return TRUE_CASE_SENSITIVE_SYSTEM;
    }

    public static boolean areFilenamesEqual(String firstFile, String secondFile) {
        return isSystemCaseSensitive() ? firstFile.equals(secondFile) : firstFile.equalsIgnoreCase(secondFile);
    }

    public static void clearFileExistenceCache() {
        mapRef.clear();
        for (CndFileExistSensitiveCache cache : getCaches()) {
            cache.invalidateAll();
        }
    }

    /**
     * normalize file
     * @param file
     * @return
     */
    public static File normalizeFile(File file) {
        CndUtils.assertAbsoluteFileInConsole(file, "Is it OK to normalize not absolute file? [" + file + "] during this session it is [" + file.getAbsolutePath() + "] but will be different if start IDE from another folder"); //NOI18N
        String path = file.getPath();
        String normPath = normalizeAbsolutePath(file.getAbsolutePath());
        return path.equals(normPath) ? file : new File(normPath);
    }

    public static File toFile(FileObject fileObject) {
        return CndFileSystemProvider.toFile(fileObject);
    }

    public static FileObject toFileObject(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            return InvalidFileObjectSupport.getInvalidFileObject(file);
        }
        return fo;
    }

    public static FileObject toFileObject(CharSequence absoluteLocalPath) {
        return CndFileSystemProvider.toFileObject(absoluteLocalPath);
    }

    public static String getCanonicalPath(CharSequence path) throws IOException {
        return new File(path.toString()).getCanonicalPath(); // XXX:FileObject conversion - delegate to provider!
    }

    public static FileObject getCanonicalFileObject(FileObject fo) throws IOException {
        File file = FileUtil.toFile(fo);
        if (file != null) {
            return FileUtil.toFileObject(file.getCanonicalFile()); // XXX:FileObject conversion - delegate to provider!
        } else {
            return fo;
        }
    }

    public static boolean isValidLocalFile(String absolutePath) {
        if (CndPathUtilitities.isPathAbsolute(absolutePath)) {
            return new File(absolutePath).exists();
        } else {
            return false;
        }
    }

    public static boolean isValidLocalFile(String base, String name) {
        if (CndPathUtilitities.isPathAbsolute(base)) {
            return new File(base, name).exists();
        } else {
            return false;
        }
    }

    public static boolean isValidLocalFile(File base, String name) {
        if (CndPathUtilitities.isPathAbsolute(base.getPath())) {
            return new File(base, name).exists();
        } else {
            return false;
        }
    }

    public static File createLocalFile(String absolutePath) {
        Parameters.notNull("null path", absolutePath); //NOI18N
        CndUtils.assertAbsolutePathInConsole(absolutePath);
        return new File(absolutePath);
    }

    public static File createLocalFile(File base,  String path) {
        Parameters.notNull("null base file", base); //NOI18N
        CndUtils.assertAbsoluteFileInConsole(base); //NOI18N
        Parameters.notNull("null path", path); //NOI18N
        return new File(base, path);
    }

    public static File createLocalFile(String base,  String path) {
        Parameters.notNull("null base file", base); //NOI18N
        CndUtils.assertAbsolutePathInConsole(base);
        Parameters.notNull("null path", path); //NOI18N
        return new File(base, path);
    }

    public static File createLocalFile(URI uri) {
        File file = new File(uri);
        CndUtils.assertAbsoluteFileInConsole(file); //NOI18N
        return file;
    }

    /**
     * normalize absolute paths
     * @param path
     * @return
     */
    public static String normalizeAbsolutePath(String path) {
        CndUtils.assertAbsolutePathInConsole(path, "path for normalization must be absolute"); //NOI18N
        boolean caseSensitive = isSystemCaseSensitive();
        if (!caseSensitive) {
            // with case sensitive "path"s returned by remote compilers
            path = CndFileSystemProvider.getCaseInsensitivePath(path);
        }
        String normalized;
        // small optimization for true case sensitive OSs
        if (!caseSensitive || (path.endsWith("/.") || path.endsWith("\\.") || path.contains("..") || path.contains("./") || path.contains(".\\"))) { // NOI18N
            normalized = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
        } else {
            normalized = path;
        }        
        return normalized;
    }

    public static boolean exists(File file) {
        return getFlags(file, null, true).exist;
    }

    /**
     * Tests whether the file exists and not directory.
     * @param file
     * @return
     */
    public static boolean isExistingFile(File file) {
        return isExistingFile(file, null);
    }

    public static boolean isExistingFile(String filePath) {
        return isExistingFile(null, filePath);
    }

    /**
     * Tests whether the file exists and not directory. One of file or filePath
     * must be not null
     * @param file
     * @param filePath
     * @return
     */
    public static boolean isExistingFile(File file, String filePath) {
        Flags flags = getFlags(file, filePath, true);
        return flags.exist && !flags.directory;
    }
    /**
     * Tests whether the file is an existing directory.
     * @param file
     * @return
     */
    public static boolean isExistingDirectory(File file) {
        return isExistingDirectory(file, null);
    }

    public static boolean isExistingDirectory(String filePath) {
        return isExistingDirectory(null, filePath);
    }

    public static boolean isExistingDirectory(File file, String filePath) {
        Flags flags = getFlags(file, filePath, false);
        return flags.exist && flags.directory;
    }

   /**
    * Gets file input stream. File can be either a "classic" file,
    * or other kind of file, say, remote one.
    *
    * NB: can be ver slow; e.g. can cause connection to the host in the case of the remote file
    *
    * NB: does nothing to buffer the file - it's up to caller
    *
    * @param filePath is either just a path to local file
    * or a URL of remote or other kind of file
    *
    * @return input stream
    *
    * @throws java.io.IOException
    */
   public static InputStream getInputStream(CharSequence filePath) throws IOException {
       FileObject fo = CndFileSystemProvider.toFileObject(filePath);
       if (fo == null) {
           File file = new File(filePath.toString());
           fo = FileUtil.toFileObject(file);
           InputStream is;
           if (fo != null) {
               is = fo.getInputStream();
           } else {
               is = new FileInputStream(file);
           }
           return is;
       } else {
           return fo.getInputStream();
       }
   }

   /** just to speed it up, since Utilities.isWindows will get string property, test equals, etc */
   private static final boolean isWindows = Utilities.isWindows();

    private static Flags getFlags(File file, String absolutePath, boolean indexParentFolder) {
        assert file != null || absolutePath != null;
        absolutePath = (absolutePath == null) ? file.getAbsolutePath() : absolutePath;
        if (isWindows) {
            absolutePath = absolutePath.replace('/', '\\');
        }
        absolutePath = changeStringCaseIfNeeded(absolutePath);
        Flags exists;
        ConcurrentMap<String, Flags> files = getFilesMap();
        exists = files.get(absolutePath);
        if (exists == null) {
            file = (file == null) ? new File(absolutePath) : file;
            String parent = file.getParent();
            if (parent != null) {
                parent = changeStringCaseIfNeeded(parent);
                Flags parentDirFlags = files.get(parent);
                if (parentDirFlags == null || parentDirFlags == Flags.DIRECTORY) {
                    File parentFile = file.getParentFile();
                    if (parentDirFlags == null) {
                        // not yet checked
                        parentDirFlags = Flags.get(parentFile);
                        files.put(parent, parentDirFlags);
                    }
                    if (parentDirFlags == Flags.NOT_FOUND || parentDirFlags == Flags.FILE) {
                        // no need to check non existing file
                        exists = Flags.NOT_FOUND;
//                        files.put(path, exists);
                    } else if (indexParentFolder) {
                        assert (parentDirFlags == Flags.DIRECTORY) : "must be DIRECTORY but was " + parentDirFlags; // NOI18N
                        // let's index not indexed directory
                        index(parentFile, parent, files);
                        exists = files.get(absolutePath);
                    }
                } else {
                    // no need to check non existing file
                    exists = Flags.NOT_FOUND;
//                    files.put(path, exists);
                }
            }
            if (exists == null) {
                exists = Flags.get(file);
                files.put(absolutePath, exists);
            }
            if (exists == Flags.DIRECTORY) {
                // let's index not indexed directory
                index(file, absolutePath, files);
            }
        } else {
            //hits ++;
        }
        return exists;
    }

    private static void index(File file, String path, ConcurrentMap<String, Flags> files) {
        if (file.canRead()) {
            CndFileSystemProvider.FileInfo[] listFiles = listFilesImpl(file);
            for (int i = 0; i < listFiles.length; i++) {
                CndFileSystemProvider.FileInfo curFile = listFiles[i];
                String absPath = changeStringCaseIfNeeded(curFile.absolutePath);
                if (isWindows) {
                    absPath = absPath.replace('/', '\\');
                }
                if (curFile.directory) {
                    files.putIfAbsent(absPath, Flags.DIRECTORY);
                } else {
                    files.put(absPath, Flags.FILE);
                }
            }
        }
        // path is already converted into correct case
        assert changeStringCaseIfNeeded(path).equals(path);
        files.put(path, Flags.INDEXED_DIRECTORY);
    }

    private static String changeStringCaseIfNeeded(String path) {
        return CndFileSystemProvider.lowerPathCaseIfNeeded(path).toString();
    }
    
//    public static String getHitRate() {
//	return "" + hits + "/" + calls; // NOI18N
//    }
//    private static int calls = 0;
//    private static int hits = 0;

    private static ConcurrentMap<String, Flags> getFilesMap() {
        ConcurrentMap<String, Flags> map = mapRef.get();
        if (map == null) {
            try {
                maRefLock.lock();
                map = mapRef.get();
                if (map == null) {
                    map = new ConcurrentHashMap<String, Flags>();
                    mapRef = new SoftReference<ConcurrentMap<String, Flags>>(map);
                }
            } finally {
                maRefLock.unlock();
            }
        }
        return map;
    }

    private static boolean existsImpl(File file) {
       Boolean exists = CndFileSystemProvider.exists(file.getAbsolutePath());
       if (exists == null) {
            return file.exists();
       } else {
            return exists.booleanValue();
       }
    }

    private static CndFileSystemProvider.FileInfo[] listFilesImpl(File file) {
       CndFileSystemProvider.FileInfo[] info = CndFileSystemProvider.getChildInfo(file.getAbsolutePath());
       if (info == null) {
            File[] children = file.listFiles();
            info = new CndFileSystemProvider.FileInfo[(children == null) ? 0 : children.length];
            for (int i = 0; i < children.length; i++) {
                info[i] = new CndFileSystemProvider.FileInfo(children[i].getAbsolutePath(), children[i].isDirectory());
            }
       }
       return info;
    }

    private static final Lock maRefLock = new ReentrantLock();
    
    private static Reference<ConcurrentMap<String, Flags>> mapRef = new SoftReference<ConcurrentMap<String, Flags>>(new ConcurrentHashMap<String, Flags>());
    private final static class Flags {

        private final boolean exist;
        private final boolean directory;
        private Flags(boolean exist, boolean directory){
            this.exist = exist;
            this.directory = directory;
        }
        private static final Flags FILE = new Flags(true,false);
        private static final Flags DIRECTORY = new Flags(true,true);
        private static final Flags INDEXED_DIRECTORY = new Flags(true,true);
        private static final Flags NOT_FOUND = new Flags(false,true);
        private static final Flags NOT_FOUND_INDEXED_DIRECTORY = new Flags(false, true);

        private static Flags get(File file) {
            if (existsImpl(file)) {
                if (file.isDirectory()) {
                    return DIRECTORY;
                } else {
                    return FILE;
                }
            } else {
                return NOT_FOUND;
            }
        }

        @Override
        public String toString() {
            if (this == NOT_FOUND) {
                return "NOT_FOUND"; // NOI18N
            } else if (this == NOT_FOUND_INDEXED_DIRECTORY) {
                return "NOT_FOUND_INDEXED_DIRECTORY"; // NOI18N
            } else if (this == INDEXED_DIRECTORY) {
                return "INDEXED_DIRECTORY"; // NOI18N
            } else if (this == DIRECTORY) {
                return "DIRECTORY"; // NOI18N
            } else if (this == FILE) {
                return "FILE"; // NOI18N
            } else {
                return "UNKNOWN"; // NOI18N
            }
        }

    }

    private static final class FSListener implements FileChangeListener {

        private FSListener() {
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            clearCachesAboutFile(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            clearCachesAboutFile(fe);
        }


        @Override
        public void fileDeleted(FileEvent fe) {
            clearCachesAboutFile(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            final File parent = clearCachesAboutFile(fe);
            // update info about old file as well
            if (parent != null) {
                final String ext = fe.getExt();
                final String oldName = (ext.length() == 0) ? fe.getName() : (fe.getName() + "." + ext); // NOI18N
                clearCachesAboutFile(new File(parent, oldName), false);
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // no update
        }
        
        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // no update
        }

        private File clearCachesAboutFile(FileEvent fe) {
            return clearCachesAboutFile(CndFileUtils.toFile(fe.getFile()), true);
        }
        
        private File clearCachesAboutFile(File f, boolean withParent) {
            cleanCachesImpl(f.getAbsolutePath());
            if (withParent) {
                File parent = f.getParentFile();
                if (parent != null) {
                    cleanCachesImpl(parent.getAbsolutePath());
                }
                return parent;
            }
            return null;
        }

        private void cleanCachesImpl(String file) {
            if (TRACE_EXTERNAL_CHANGES) {
                System.err.println("clean cache for " + file);
            }
            getFilesMap().remove(file);
            for (CndFileExistSensitiveCache cache : getCaches()) {
                cache.invalidateFile(file);
            }
        }
    }
    private static final boolean TRACE_EXTERNAL_CHANGES = Boolean.getBoolean("cnd.modelimpl.trace.external.changes"); // NOI18N
    private static volatile Collection<? extends CndFileExistSensitiveCache> listeners;
    private static Collection<? extends CndFileExistSensitiveCache> getCaches() {
        if (listeners == null) {
             listeners = Lookup.getDefault().lookupAll(CndFileExistSensitiveCache.class);
        }
        return listeners;
    }
}
