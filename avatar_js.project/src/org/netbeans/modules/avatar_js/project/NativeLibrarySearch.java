/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project;

import java.io.File;
import java.io.IOException;

/**
 * Search for the native libraries.
 * 
 * @author Martin
 */
public final class NativeLibrarySearch {
    
    public static final String[] AVATAR_LIB_NAMES = new String[] {
        "avatar-js", "http-parser-java", "uv-java"                     // NOI18N
    };
    
    // The paths searched for libraries
    private static String usr_paths[];
    private static String sys_paths[];

    private NativeLibrarySearch() {}
    
    public static File findLibrary(String name) {
        if (sys_paths == null) {
            sys_paths = getPath("sun.boot.library.path");   // NOI18N
            usr_paths = getPath("java.library.path");       // NOI18N
        }

        String libName = System.mapLibraryName(name);
        for (String sys_path : sys_paths) {
            File libFile = new File(sys_path, libName);
            if (canLoadLibrary(libFile)) {
                return libFile;
            }
        }
        for (String usr_path : usr_paths) {
            File libFile = new File(usr_path, libName);
            if (canLoadLibrary(libFile)) {
                return libFile;
            }
        }
        return null;
    }
    
    public static File findAvatarLibrariesFolder() {
        File dir = null;
        for (String name : AVATAR_LIB_NAMES) {
            File lib = findLibrary(name);
            if (lib == null) {
                return null;
            }
            File parent = lib.getParentFile();
            if (dir == null) {
                dir = parent;
            } else {
                if (!dir.equals(parent)) {
                    return null;
                }
            }
        }
        return dir;
    }
    
    /**
     * 
     * @param folder
     * @return <code>null</code> when nothing is missing.
     */
    public static String[] getMissingAvatarLibrariesIn(File folder) {
        int n = 0;
        for (String name : AVATAR_LIB_NAMES) {
            String libName = System.mapLibraryName(name);
            File lib = new File(folder, libName);
            if (!canLoadLibrary(lib)) {
                n++;
            }
        }
        if (n == 0) {
            return null;
        }
        String[] missing = new String[n];
        n = 0;
        for (String name : AVATAR_LIB_NAMES) {
            String libName = System.mapLibraryName(name);
            File lib = new File(folder, libName);
            if (!canLoadLibrary(lib)) {
                missing[n++] = name;
            }
        }
        return missing;
    }

    private static String[] getPath(String propname) {
        String ldpath = System.getProperty(propname, "");
        String ps = File.pathSeparator;
        // Count the separators in the path
        int n = 0;
        for (int i = ldpath.indexOf(ps); i >= 0; n++) {
            i = ldpath.indexOf(ps, i + 1);
        }

        // The array of paths:
        String[] paths = new String[n + 1];

        // Fill the array with paths from the ldpath
        n = 0;
        int i1 = 0;
        int i2 = ldpath.indexOf(ps);
        while (i2 >= 0) {
            if (i2 - i1 > 0) {
                paths[n++] = ldpath.substring(i1, i2);
            } else if (i1 == i2) {
                paths[n++] = ".";
            }
            i1 = i2 + 1;
            i2 = ldpath.indexOf(ps, i1);
        }
        paths[n] = ldpath.substring(i1, ldpath.length());
        return paths;
    }

    private static boolean canLoadLibrary(File f) {
        if (!f.exists() || !f.canRead()) {
            return false;
        }
        try {
            f.getCanonicalPath();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
}
