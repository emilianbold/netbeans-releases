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

public class SunCCCompiler extends CCCCompiler {
    private static final String[] DEVELOPMENT_MODE_OPTIONS = {
	"",  // Fast Build // NOI18N
	"-g", // Debug" // NOI18N
	"-g0 -xO3 -xhwcprof", // Performance Debug" // NOI18N
	"-xprofile=tcov +d -xinline=", // Test Coverage // NOI18N
	"-g0 -xO2", // Dianosable Release // NOI18N
	"-xO3", // Release // NOI18N
	"-xO5 -xipo=1 -xdepend -fsimple=1 -xlibmil -xlibmopt -xvector -xbuiltin -sync_stdio=no -xalias_level=simple -sync_stdio=no", // Performance Release // NOI18N
    };
    
    private static final String[] WARNING_LEVEL_OPTIONS = {
	"-w", // No Warnings // NOI18N
	"", // Default // NOI18N
	"+w", // More Warnings // NOI18N
	"-xwe", // Convert Warnings to Errors // NOI18N
    };
    
    private static final String[] LIBRARY_LEVEL_OPTIONS = {
	"-library=no%Cstd,no%Crun -filt=no%stdlib", // NOI18N
	"-library=no%Cstd -filt=no%stdlib", // NOI18N
	"-library=iostream,no%Cstd -filt=no%stdlib", // NOI18N
	"", // NOI18N
	"-library=stlport4,no%Cstd", // NOI18N
    };

    private static final String[] MT_LEVEL_OPTIONS = {
	"", // None // NOI18N
	"-mt", // Safe // NOI18N
	"-xautopar -xvector -xreduction -xloopinfo", // Automatic // NOI18N
	"-xopenmp", // Open MP // NOI18N
    };

    private static final String[] STANDARD_OPTIONS = {
	"-compat", // Old // NOI18N
	"-features=no%localfor,no%extinl,no%conststrings", // Legacy // NOI18N
	"", // Default // NOI18N
	"-features=no%anachronisms,no%transitions,tmplife", // Modern // NOI18N
    };

    private static final String[] LANGUAGE_EXT_OPTIONS = {
	"-features=no%longlong", // None // NOI18N
	"", // Default // NOI18N
	"-features=extensions,tmplrefstatic,iddollar", // All // NOI18N
    };
    
    /** Creates a new instance of SunCCompiler */
    public SunCCCompiler() {
        super(CCCompiler, "CC", "Sun C++ Compiler"); // NOI18N
    }
    
    public String getDevelopmentModeOptions(int value) {
        return DEVELOPMENT_MODE_OPTIONS[value];
    }
    
    public String getWarningLevelOptions(int value) {
        if (value < WARNING_LEVEL_OPTIONS.length)
            return WARNING_LEVEL_OPTIONS[value];
        else
            return ""; // NOI18N
    }
    
    public String getSixtyfourBitsOption(boolean value) {
        return value ? "-xarch=generic64" : ""; // NOI18N
    }
    
    public String getStripOption(boolean value) {
        return value ? "-s" : ""; // NOI18N
    }
    
    public List getSystemPreprocessorSymbols(Platform platform) {
        // FIXUP: should use 'platform' and not System.getProperty("os.arch")
        Vector list = new Vector();
	String arch = System.getProperty("os.arch", "").toLowerCase(); // NOI18N
	list.add("__SVR4"); // NOI18N
	list.add("__unix"); // NOI18N
	list.add("__sun"); // NOI18N
	if (arch.indexOf("sparc") >= 0) // NOI18N // FIXUP: need to take this from platform
	    list.add("__sparc"); // NOI18N
	else if (arch.indexOf("86") >= 0) // NOI18N // FIXUP: need to take this from platform
	    list.add("__i386"); // NOI18N
	list.add("unix"); // NOI18N
	list.add("sun"); // NOI18N
	if (arch.indexOf("sparc") >= 0)  // NOI18N
	    list.add("sparc"); // NOI18N
	if (arch.indexOf("86") >= 0)  // NOI18N
	    list.add("i386"); // NOI18N
	return list;
    }
    
    public List getSystemIncludeDirectories(Platform platform) {
        Vector list = new Vector();
        list.add("/opt/SUNWspro/prod/include/CC/Cstd"); // NOI18N
        list.add("/opt/SUNWspro/prod/include/CC"); // NOI18N
        list.add("/opt/SUNWspro/prod/include/CC/rw7"); // NOI18N
        list.add("/opt/SUNWspro/prod/include/cc"); // NOI18N
	return list;
    }
    
    // To be overridden
    public String getMTLevelOptions(int value) {
	return MT_LEVEL_OPTIONS[value];
    }
    
    // To be overridden
    public String getLibraryLevelOptions(int value) {
	return LIBRARY_LEVEL_OPTIONS[value];
    }

    // To be overridden
    public String getStandardsEvolutionOptions(int value) {
	return STANDARD_OPTIONS[value];
    }

    // To be overridden
    public String getLanguageExtOptions(int value) {
	return LANGUAGE_EXT_OPTIONS[value];
    }
}
