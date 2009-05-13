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
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;

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
                String absolutePath = fileFromBasePath.getAbsolutePath();
                return new ResolvedPath(folder, normalize(absolutePath), absolutePath, true, 0);
            }
        }
        return null;
    }
    
    public static ResolvedPath resolveAbsFilePath(String file) {
        if (APTTraceFlags.APT_ABSOLUTE_INCLUDES) {
            File absFile = new File(file);
            if (absFile.isAbsolute() && !isDirectory(absFile) && exists(absFile)) {
                return new ResolvedPath(absFile.getParent(), normalize(file), file, false, 0);
            }
        }   
        return null;
    }    
    
    public static ResolvedPath resolveFilePath(Iterator<CharSequence> searchPaths, String includedFile, int dirOffset) {
        while( searchPaths.hasNext() ) {
            CharSequence sysPrefix = searchPaths.next();
            String sysPrefixString = sysPrefix.toString();
            File fileFromPath = new File(new File(sysPrefixString), includedFile);
            if (!isDirectory(fileFromPath) && exists(fileFromPath)) {
                String absolutePath = fileFromPath.getAbsolutePath();
                return new ResolvedPath(sysPrefix, normalize(absolutePath), absolutePath, false, dirOffset);
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
                            String absolutePath = fileFromPath.getAbsolutePath();
                            return new ResolvedPath(sysPrefix, normalize(absolutePath), absolutePath, false, dirOffset);
                        }
                    }
                }
            }
            dirOffset++;
        }
        return null;
    }

    public static String normalize(String path) {
        return CndFileUtils.normalizePath(path);
    }

    private static boolean exists(File file) {
        return CndFileUtils.exists(file);
    }
    
    private static boolean isDirectory(File file) {
        return CndFileUtils.isDirectory(file);
    }    
}
