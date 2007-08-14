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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.compilers;

import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;

public class SunFortranCompiler extends BasicCompiler {
    private static final String[] DEVELOPMENT_MODE_OPTIONS = {
        "",  // Fast Build // NOI18N
        "-g", // Debug" // NOI18N
        "-g -O", // Performance Debug" // NOI18N
        "--g", // Test Coverage // NOI18N
        "-g -O2", // Dianosable Release // NOI18N
        "-O3", // Release // NOI18N
        "-O4", // Performance Release // NOI18N
    };

    private static final String[] WARNING_LEVEL_OPTIONS = {
        "-w", // No Warnings // NOI18N
        "-w1", // Default // NOI18N
        "-w2", // More Warnings // NOI18N
        "-erroff", // Convert Warnings to Errors // NOI18N
    };
    
    /** Creates a new instance of SunCCompiler */
    public SunFortranCompiler(CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(flavor, kind, name, displayName, path);
    }
    
    @Override
    public String getDevelopmentModeOptions(int value) {
        return DEVELOPMENT_MODE_OPTIONS[value];
    }
    
    @Override
    public String getWarningLevelOptions(int value) {
        if (value < WARNING_LEVEL_OPTIONS.length)
            return WARNING_LEVEL_OPTIONS[value];
        else
            return ""; // NOI18N
    }
    
    @Override
    public String getSixtyfourBitsOption(int value) {
        if (getFlavor() == CompilerFlavor.Sun12) {
            if (value == BasicCompilerConfiguration.BITS_DEFAULT)
                return ""; // NOI18N
            else if (value == BasicCompilerConfiguration.BITS_32)
                return "-m32"; // NOI18N
            else if (value == BasicCompilerConfiguration.BITS_64)
                return "-m64"; // NOI18N
        else
                return ""; // NOI18N
    }
        else {
            if (value == BasicCompilerConfiguration.BITS_DEFAULT)
                return ""; // NOI18N
            else if (value == BasicCompilerConfiguration.BITS_32)
                return ""; // NOI18N
            else if (value == BasicCompilerConfiguration.BITS_64)
                return "-xarch=generic64"; // NOI18N
            else
                return ""; // NOI18N
        }
    }
    
    @Override
    public String getStripOption(boolean value) {
        return value ? "-s" : ""; // NOI18N
    }
}
