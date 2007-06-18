/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils.system;

import java.io.File;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class LinuxNativeUtils extends UnixNativeUtils {
    public static final String LIBRARY_PREFIX_LINUX = 
            NATIVE_JNILIB_RESOURCE_SUFFIX + 
            "linux/" ; //NOI18N
            
    public static final String LIBRARY_I386 =
            "linux.so"; //NO18N
    public static final String LIBRARY_AMD64 = 
            "linux-amd64.so"; //NO18N
    
    
    public static final String[] FORBIDDEN_DELETING_FILES_LINUX = {};
    
    LinuxNativeUtils() {
        String library = System.getProperty("os.arch").equals("amd64") ? 
                    LIBRARY_AMD64 : LIBRARY_I386;
        
        loadNativeLibrary(LIBRARY_PREFIX_LINUX + library);
        
        initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_LINUX);
    }
    
    public File getDefaultApplicationsLocation() {
        File usrlocal = new File("/usr/local");
        
        if (usrlocal.exists() && 
                usrlocal.isDirectory() && 
                FileUtils.canWrite(usrlocal)) {
            return usrlocal;
        } else {
            return SystemUtils.getUserHomeDirectory();
        }
    }
}
