/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.apt.utils;

import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;

/**
 *
 * @author Vladimir Voskresensky
 */
public class APTIncludeUtils {
    
    private APTIncludeUtils() {
    }
    
    /** 
     * finds file relatively to the baseFile 
     * caller must check that resolved path is not the same as base file
     * to prevent recursive inclusions 
     */
    public static ResolvedPath resolveFilePath(String inclString, CharSequence baseFile) {
        if (baseFile != null) {
            String folder = new File(baseFile.toString()).getParent();
            File fileFromBasePath = new File(folder, inclString);
            if (!isDirectory(fileFromBasePath) && exists(fileFromBasePath)) {
                //return fileFromBasePath.getAbsolutePath();
                return new ResolvedPath(folder, fileFromBasePath.getAbsolutePath(), true, 0);
            }
        }
        return null;
    }
    
    public static ResolvedPath resolveAbsFilePath(String file) {
        if (APTTraceFlags.APT_ABSOLUTE_INCLUDES) {
            File absFile = new File(file);
            if (absFile.isAbsolute() && !isDirectory(absFile) && exists(absFile)) {
                return new ResolvedPath(absFile.getParent(), file, false, 0);
            }
        }   
        return null;
    }
    
    public static void clearFileExistenceCache() {
        mapRef.clear();
        mapFoldersRef.clear();
    }
    
    public static ResolvedPath resolveFilePath(Iterator<CharSequence> searchPaths, String includedFile, int dirOffset) {
        while( searchPaths.hasNext() ) {
            CharSequence sysPrefix = searchPaths.next();
            String sysPrefixString = sysPrefix.toString();
            File fileFromPath = new File(new File(sysPrefixString), includedFile);
            if (!isDirectory(fileFromPath) && exists(fileFromPath)) {
                return new ResolvedPath(sysPrefix, fileFromPath.getAbsolutePath(), false, dirOffset);
            } else {
                if (sysPrefixString.endsWith("/Frameworks")){ // NOI18N
                    int i = includedFile.indexOf('/'); // NOI18N
                    if (i > 0) {
                        // possible it is framework include (see IZ#160043)
                        // #include <GLUT/glut.h>
                        // header is located in the /System/Library/Frameworks/GLUT.framework/Headers
                        // system path is /System/Library/Frameworks
                        // So convert framework path
                        String fileName = sysPrefixString+"/"+includedFile.substring(0,i)+".framework/Headers"+includedFile.substring(i); // NOI18N
                        fileFromPath = new File(fileName);
                        if (!isDirectory(fileFromPath) && exists(fileFromPath)) {
                            return new ResolvedPath(sysPrefix, fileFromPath.getAbsolutePath(), false, dirOffset);
                        }
                    }
                }
            }
            dirOffset++;
        }
        return null;
    }
    
    private static boolean exists(File file) {
        if( APTTraceFlags.OPTIMIZE_INCLUDE_SEARCH ) {
            //calls++;
            String path = file.getAbsolutePath();
            Boolean exists;
//            synchronized (mapRef) {
                Map<String, Boolean> files = getFilesMap();
                exists = files.get(path);
                if( exists == null ) {
                    exists = Boolean.valueOf(file.exists());
                    files.put(FilePathCache.getManager().getString(path).toString(), exists);
                } else {
                    //hits ++;
                }
//            }
            return exists.booleanValue();
        } else {
            return file.exists();
        }
    }
    
    private static boolean isDirectory(File file) {
        if( APTTraceFlags.OPTIMIZE_INCLUDE_SEARCH ) {
            //calls++;
            String path = file.getAbsolutePath();
            Boolean exists;
//            synchronized (mapFoldersRef) {
                Map<String, Boolean> dirs = getFoldersMap();                
                exists = dirs.get(path);
                if( exists == null ) {
                    exists = Boolean.valueOf(file.isDirectory());
                    dirs.put(FilePathCache.getManager().getString(path).toString(), exists);
                } else {
                    //hits ++;
                }
//            }
            return exists.booleanValue();
        } else {
            return file.isDirectory();
        }
    }
    
//    public static String getHitRate() {
//	return "" + hits + "/" + calls; // NOI18N
//    }   
//    private static int calls = 0;
//    private static int hits = 0;

    private static Map<String, Boolean> getFilesMap() {
        Map<String, Boolean> map = mapRef.get();
        if( map == null ) {
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
        if( map == null ) {
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
    private static Lock maRefLock = new ReentrantLock();
    private static Lock mapFoldersRefLock = new ReentrantLock();
    
    private static Reference<Map<String,Boolean>> mapRef = new SoftReference<Map<String,Boolean>>(new ConcurrentHashMap<String, Boolean>());
    private static Reference<Map<String,Boolean>> mapFoldersRef = new SoftReference<Map<String,Boolean>>(new ConcurrentHashMap<String, Boolean>());
    
}
