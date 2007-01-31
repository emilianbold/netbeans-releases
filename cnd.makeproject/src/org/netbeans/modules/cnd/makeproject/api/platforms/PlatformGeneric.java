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

import org.netbeans.modules.cnd.makeproject.api.actions.BatchBuildAction;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.openide.util.NbBundle;

public class PlatformGeneric extends Platform {
    public static final String NAME = "Generic"; // NOI18N

    public static final LibraryItem.StdLibItem[] standardLibrariesLinux = {
        // empty
    };

    public PlatformGeneric() {
        super(NAME, NbBundle.getBundle(PlatformGeneric.class).getString("GenericName"), Platform.PLATFORM_GENERIC);
    }

    public LibraryItem.StdLibItem[] getStandardLibraries() {
        return standardLibrariesLinux;
    }
    
    public String getLibraryName(String baseName) {
        // Use Linux style
        return "lib" + baseName + ".so"; // NOI18N
    }
    
    public String getLibraryLinkOption(String libName, String libDir, String libPath, CompilerSet compilerSet) {
        if (libName.endsWith(".so")) { // NOI18N
            int i = libName.indexOf(".so"); // NOI18N
            if (i > 0)
                libName = libName.substring(0, i);
            if (libName.startsWith("lib")) // NOI18N
                libName = libName.substring(3);
            return compilerSet.getLibrarySearchOption() + libDir + " " + compilerSet.getLibraryOption() + libName; // NOI18N
        } else {
            return libPath;
        }
    }
}
