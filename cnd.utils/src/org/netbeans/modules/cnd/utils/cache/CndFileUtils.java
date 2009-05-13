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
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * some file utilities used by CND modules due to performance reasons or other
 * @author Vladimir Voskresensky
 */
public final class CndFileUtils {
    private static final boolean TRUE_CASE_SENSITIVE_SYSTEM;

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
        mapFoldersRef.clear();
    }

    public static String normalizePath(String path) {
        //calls++;
        Map<String, String> normalizedPaths = getNormalizedFilesMap();
        String normalized = normalizedPaths.get(path);
        if (normalized == null) {
            // small optimization for true case sensitive OSs
            boolean caseSensitive = isSystemCaseSensitive();
            if (!caseSensitive || (path.contains("..") || path.contains("./") || path.contains(".\\"))) { // NOI18N
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
        //calls++;
        String path = file.getAbsolutePath();
        Boolean exists;
        Map<String, Boolean> files = getFilesMap();
        exists = files.get(path);
        if (exists == null) {
            exists = Boolean.valueOf(file.exists());
            files.put(path, exists);
        } else {
            //hits ++;
        }
        return exists.booleanValue();
    }

    public static boolean isDirectory(File file) {
        //calls++;
        String path = file.getAbsolutePath();
        Boolean exists;
        Map<String, Boolean> dirs = getFoldersMap();
        exists = dirs.get(path);
        if (exists == null) {
            exists = Boolean.valueOf(file.isDirectory());
            dirs.put(path, exists);
        } else {
            //hits ++;
        }
        return exists.booleanValue();
    }

//    public static String getHitRate() {
//	return "" + hits + "/" + calls; // NOI18N
//    }
//    private static int calls = 0;
//    private static int hits = 0;
    
    private static Map<String, Boolean> getFilesMap() {
        Map<String, Boolean> map = mapRef.get();
        if (map == null) {
            try {
                maRefLock.lock();
                map = mapRef.get();
                if (map == null) {
                    map = new ConcurrentHashMap<String, Boolean>();
                    mapRef = new SoftReference<Map<String, Boolean>>(map);
                }
            } finally {
                maRefLock.unlock();
            }
        }
        return map;
    }

    private static Map<String, Boolean> getFoldersMap() {
        Map<String, Boolean> map = mapFoldersRef.get();
        if (map == null) {
            try {
                mapFoldersRefLock.lock();
                map = mapFoldersRef.get();
                if (map == null) {
                    map = new ConcurrentHashMap<String, Boolean>();
                    mapFoldersRef = new SoftReference<Map<String, Boolean>>(map);
                }
            } finally {
                mapFoldersRefLock.unlock();
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
    
    private static final Lock maRefLock = new ReentrantLock();
    private static final Lock mapFoldersRefLock = new ReentrantLock();
    private static final Lock mapNormalizedRefLock = new ReentrantLock();
    
    private static Reference<Map<String, Boolean>> mapRef = new SoftReference<Map<String, Boolean>>(new ConcurrentHashMap<String, Boolean>());
    private static Reference<Map<String, Boolean>> mapFoldersRef = new SoftReference<Map<String, Boolean>>(new ConcurrentHashMap<String, Boolean>());
    private static Reference<Map<String, String>> normalizedRef = new SoftReference<Map<String, String>>(new ConcurrentHashMap<String, String>());
}
