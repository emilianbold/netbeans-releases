/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.platforms;

import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;

public class PlatformWindows extends Platform {
    public static final String NAME = "Windows";

    public static final LibraryItem.StdLibItem[] standardLibrariesLinux = {
        new LibraryItem.StdLibItem("Mathematics", "Mathematics", new String[] {"m"}),
        new LibraryItem.StdLibItem("DataCompression", "Data Compression", new String[] {"z"}),
        new LibraryItem.StdLibItem("PosixThreads", "Posix Threads", new String[] {"pthread"}),
    };
    
    public PlatformWindows() {
        super(NAME, "Windows", Platform.PLATFORM_WINDOWS);
    }
    
    public LibraryItem.StdLibItem[] getStandardLibraries() {
        return standardLibrariesLinux;
    }
    
    public String getLibraryName(String baseName) {
        return "cyg" + baseName + ".dll"; // NOI18N // FIXUP: cyg hardcoded...
    }
    
    public String getLibraryLinkOption(String libName, String libDir, String libPath, CompilerSet compilerSet) {
        if (libName.endsWith(".dll")) {
            int i = libName.indexOf(".dll");
            if (i > 0)
                libName = libName.substring(0, i);
            if (libName.startsWith("cyg"))// NOI18N // FIXUP: cyg hardcoded...
                libName = libName.substring(3);
            return compilerSet.getLibrarySearchOption() + libDir + " " + "-l" + libName;
        } else {
            return libPath;
        }
    }
}
