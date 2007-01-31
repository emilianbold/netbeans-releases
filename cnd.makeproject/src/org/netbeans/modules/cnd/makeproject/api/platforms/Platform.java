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
import org.openide.util.Utilities;

public abstract class Platform {
    // Platform id's
    public static final int PLATFORM_SOLARIS_SPARC = 0;
    public static final int PLATFORM_SOLARIS_INTEL = 1;
    public static final int PLATFORM_LINUX = 2;
    public static final int PLATFORM_WINDOWS = 3;
    public static final int PLATFORM_GENERIC = 4;
    
    private static int defaultPlatform = -1;
    
    private String name;
    private String displayName;
    private int id;
    
    public Platform(String name, String displayName, int id) {
        this.name = name;
        this.displayName = displayName;
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getId() {
        return id;
    }
    
    public abstract LibraryItem.StdLibItem[] getStandardLibraries();
    
    public abstract String getLibraryName(String baseName);
    
    public abstract String getLibraryLinkOption(String libName, String libDir, String libPath, CompilerSet compilerSet);
    
    public LibraryItem.StdLibItem getStandardLibrarie(String name) {
	for (int i = 0; i < getStandardLibraries().length; i++) {
	    if (getStandardLibraries()[i].getName().equals(name))
		return getStandardLibraries()[i];
	}
	return null;
    }
    
    public static int getDefaultPlatform() {
        if (defaultPlatform <= 0) {
            String arch = System.getProperty("os.arch"); // NOI18N
            if (Utilities.isWindows())
                defaultPlatform = Platform.PLATFORM_WINDOWS;
            else if (Utilities.getOperatingSystem() == Utilities.OS_LINUX)
                defaultPlatform = Platform.PLATFORM_LINUX;
            else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS && arch.indexOf("86") >= 0) // NOI18N
                defaultPlatform = Platform.PLATFORM_SOLARIS_INTEL;
            else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS)
                defaultPlatform = Platform.PLATFORM_SOLARIS_SPARC;
            else 
                defaultPlatform = Platform.PLATFORM_GENERIC;
        }
        return defaultPlatform;
    }
}
