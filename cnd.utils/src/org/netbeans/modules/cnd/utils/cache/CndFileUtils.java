/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.utils.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.modules.cnd.spi.utils.FileSystemsProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * some file utilities used by CND modules due to performance reasons or other
 * @author Vladimir Voskresensky
 */
public final class CndFileUtils {
    private static final boolean TRUE_CASE_SENSITIVE_SYSTEM;

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
        } catch (IOException ex) {
            caseSenstive = Utilities.isUnix() && !Utilities.isMac();
        }
        TRUE_CASE_SENSITIVE_SYSTEM = caseSenstive;
    }

    public static boolean isSystemCaseSensitive() {
        return TRUE_CASE_SENSITIVE_SYSTEM;
    }

    public static void clearFileExistenceCache() {
        mapRef.clear();
    }

    /**
     * normalize file
     * @param file
     * @return
     */
    public static File normalizeFile(File file) {
        if (CndUtils.isDebugMode()) {
            if (!file.isAbsolute()) {
                CndUtils.assertTrueInConsole(false, "Is it OK to normalize not absolute file? [" + file + "] during this session it is [" + file.getAbsolutePath() + "] but will be different if start IDE from another folder");
            }
        }
        String path = file.getPath();
        String normPath = normalizeAbsolutePath(file.getAbsolutePath());
        return path.equals(normPath) ? file : new File(normPath);
    }

    /**
     * normalize absolute paths
     * @param path
     * @return
     */
    public static String normalizeAbsolutePath(String path) {
        if (CndUtils.isDebugMode()) {
            if (!new File(path).isAbsolute()) {
                CndUtils.assertTrueInConsole(false, "path for normalization must be absolute " + path);
            }
        }
        //calls++;
        Map<String, String> normalizedPaths = getNormalizedFilesMap();
        String normalized = normalizedPaths.get(path);
        if (normalized == null) {
            // small optimization for true case sensitive OSs
            boolean caseSensitive = isSystemCaseSensitive();
            if (!caseSensitive || (path.endsWith("/.") || path.endsWith("\\.") || path.contains("..") || path.contains("./") || path.contains(".\\"))) { // NOI18N
                normalized = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
            } else {
                normalized = path;
            }
            normalizedPaths.put(path, normalized);
        } else {
            //hits ++;
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
        return getFlags(file, filePath, false).directory;
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
       FileSystemsProvider.Data data = FileSystemsProvider.get(filePath);
       if (data == null) {
           File file = new File(filePath.toString());
           FileObject fo = FileUtil.toFileObject(file);
           InputStream is;
           if (fo != null) {
               is = fo.getInputStream();
           } else {
               is = new FileInputStream(file);
           }
           return is;
       } else {
           FileObject fo = data.fileSystem.getRoot().getFileObject(data.path);
           if (fo == null) {
               throw new FileNotFoundException(filePath.toString());
           }
           return fo.getInputStream();
       }
   }

    private static Flags getFlags(File file, String absolutePath, boolean indexParentFolder) {
        assert file != null || absolutePath != null;
        absolutePath = (absolutePath == null) ? file.getAbsolutePath() : absolutePath;
        Flags exists;
        ConcurrentMap<String, Flags> files = getFilesMap();
        exists = files.get(absolutePath);
        if (exists == null) {
            file = (file == null) ? new File(absolutePath) : file;
            String parent = file.getParent();
            if (parent != null) {
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
            File[] listFiles = listFilesImpl(file);
            for (int i = 0; i < listFiles.length; i++) {
                File curFile = listFiles[i];
                String absPath = curFile.getAbsolutePath();
                if (curFile.isDirectory()) {
                    files.putIfAbsent(absPath, Flags.DIRECTORY);
                } else {
                    files.put(absPath, Flags.FILE);
                }
            }
        }
        files.put(path, Flags.INDEXED_DIRECTORY);
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

    private static Map<String, String> getNormalizedFilesMap() {
        Map<String, String> map = normalizedRef.get();
        if (map == null) {
            try {
                mapNormalizedRefLock.lock();
                map = normalizedRef.get();
                if (map == null) {
                    map = new ConcurrentHashMap<String, String>();
                    normalizedRef = new SoftReference<Map<String, String>>(map);
                }
            } finally {
                mapNormalizedRefLock.unlock();
            }
        }
        return map;
    }

    private static boolean existsImpl(File file) {
       FileSystemsProvider.Data data = FileSystemsProvider.get(file);
       if (data == null) {
            return file.exists();
       } else {
            FileObject fo = data.fileSystem.getRoot().getFileObject(data.path);
            if (fo == null) {
                return false;
            } else {
                return ! fo.isVirtual();
            }
       }
    }

    private static File[] listFilesImpl(File file) {
       FileSystemsProvider.Data data = FileSystemsProvider.get(file);
       if (data == null) {
            return file.listFiles();
       } else {
           FileObject fo = data.fileSystem.getRoot().getFileObject(data.path);
           //FileObject[] children = fo.getChildren();
           // FIXUP: a very very dirty hack, just to make sure it will fly
           fo.getFileObject("dummy");
           return file.listFiles();
       }
    }


    private static final Lock maRefLock = new ReentrantLock();
    private static final Lock mapNormalizedRefLock = new ReentrantLock();
    
    private static Reference<ConcurrentMap<String, Flags>> mapRef = new SoftReference<ConcurrentMap<String, Flags>>(new ConcurrentHashMap<String, Flags>());
    private static Reference<Map<String, String>> normalizedRef = new SoftReference<Map<String, String>>(new ConcurrentHashMap<String, String>());
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
}
