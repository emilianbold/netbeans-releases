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

import org.netbeans.installer.utils.SystemUtils;
import static org.netbeans.installer.utils.helper.Platform.*;
/**
 *
 * @author Kirill Sorokin
 */
public class SolarisNativeUtils extends UnixNativeUtils {
    
    public static final String LIBRARY_PATH_SOLARIS_SPARC =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-sparc/" + //NOI18N
            "solaris-sparc.so"; //NOI18N
    
    public static final String LIBRARY_PATH_SOLARIS_SPARCV9 =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-sparc/" + //NOI18N
            "solaris-sparcv9.so"; //NOI18N
    
    public static final String LIBRARY_PATH_SOLARIS_X86 =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-x86/" + //NOI18N
            "solaris-x86.so"; // NOI18N
    
    public static final String LIBRARY_PATH_SOLARIS_X64 =
            NATIVE_JNILIB_RESOURCE_SUFFIX +
            "solaris-x86/" + //NOI18N
            "solaris-amd64.so"; // NOI18N
    
    private static final String[] FORBIDDEN_DELETING_FILES_SOLARIS = {};
    
    SolarisNativeUtils() {
        String library = null;
        
        if(System.getProperty("os.arch").contains("sparc")) {
            library = SystemUtils.isCurrentJava64Bit() ? 
                LIBRARY_PATH_SOLARIS_SPARCV9 : 
                LIBRARY_PATH_SOLARIS_SPARC;
        } else {
            library = SystemUtils.isCurrentJava64Bit() ? 
                LIBRARY_PATH_SOLARIS_X64 : 
                LIBRARY_PATH_SOLARIS_X86;
        }
        
        loadNativeLibrary(library);
        initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_SOLARIS);
    }
}
