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

package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.util.List;
import java.util.Vector;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;

public class GNUCCompiler extends CCCCompiler {
    private static final String[] DEVELOPMENT_MODE_OPTIONS = {
	"",  // Fast Build // NOI18N
	"-g", // Debug" // NOI18N
	"-g -O", // Performance Debug" // NOI18N
	"-g", // Test Coverage // NOI18N
	"-g -O2", // Dianosable Release // NOI18N
	"-O2", // Release // NOI18N
	"-O3", // Performance Release // NOI18N
    };
    
    private static final String[] WARNING_LEVEL_OPTIONS = {
	"-w", // No Warnings // NOI18N
	"", // Default // NOI18N
	"-Wall", // More Warnings // NOI18N
	"-Werror", // Convert Warnings to Errors // NOI18N
    }; // FIXUP: from Bundle
    
    public String getDevelopmentModeOptions(int value) {
        return DEVELOPMENT_MODE_OPTIONS[value];
    }
    
    /** Creates a new instance of GNUCCompiler */
    public GNUCCompiler() {
        super(CCompiler, "gcc", "GNU C Compiler"); // NOI18N
    }
    
    public String getWarningLevelOptions(int value) {
        if (value < WARNING_LEVEL_OPTIONS.length)
            return WARNING_LEVEL_OPTIONS[value];
        else
            return ""; // NOI18N
    }
    
    public String getSixtyfourBitsOption(boolean value) {
        return value ? "-m64" : ""; // NOI18N
    }
    
    public String getStripOption(boolean value) {
        return value ? "-s" : ""; // NOI18N
    }
    
    public List getSystemPreprocessorSymbols(Platform platform) {
        // FIXUP: add actual preprocessor symbols
        Vector list = new Vector();
	return list;
    }
    
    public List getSystemIncludeDirectories(Platform platform) {
        // FIXUP: add actual include directories
        // FIXUP: use 'platform' to return different list on different platforms.
        Vector list = new Vector();
        if (platform.getId() == Platform.PLATFORM_SOLARIS_INTEL || platform.getId() == Platform.PLATFORM_SOLARIS_SPARC) {
	    list.add("/usr/local/include");
	    list.add("/usr/sfw/include");
	    list.add("/usr/sfw/lib/gcc/i386-pc-solaris2.10/3.4.3/include");
	    list.add("/usr/include");
        }
        else if (platform.getId() == Platform.PLATFORM_LINUX) {
	    list.add("/usr/local/include");
	    list.add("/usr/include");
        }
        else if (platform.getId() == Platform.PLATFORM_WINDOWS) {
            list.add("???"); // NOI18N
        }
	return list;
    }
}
