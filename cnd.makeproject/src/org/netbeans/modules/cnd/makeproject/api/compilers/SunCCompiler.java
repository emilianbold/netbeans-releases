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

package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class SunCCompiler extends SunCCCCompiler {
    private static final String compilerStderrCommand = " -xdryrun -E"; // NOI18N
    
    private static final String[] DEVELOPMENT_MODE_OPTIONS = {
        "",  // Fast Build // NOI18N
        "-g", // Debug" // NOI18N
        "-g -xO3 -xhwcprof", // Performance Debug" // NOI18N
        "-xprofile=tcov -xinline=", // Test Coverage // NOI18N
        "-g -xO2", // Dianosable Release // NOI18N
        "-xO3 -xstrconst", // Release // NOI18N
        "-xO5 -xipo=1 -xdepend -fsimple=1 -xlibmil -xlibmopt -xvector -xbuiltin -xalias_level=basic", // Performance Release // NOI18N
    };
    
    private static final String[] WARNING_LEVEL_OPTIONS = {
        "-w", // No Warnings // NOI18N
        "", // Default // NOI18N
        "+w", // More Warnings // NOI18N
        "-errwarn=%all", // Convert Warnings to Errors // NOI18N
    };
    
    private static final String[] MT_LEVEL_OPTIONS = {
        "", // None // NOI18N
        "-mt", // Safe // NOI18N
        "-xautopar -xvector -xreduction -xloopinfo", // Automatic // NOI18N
        "-xopenmp", // Open MP // NOI18N
    };
    
    private static final String[] STANDARD_OPTIONS = {
        "-xc99=none", // Old // NOI18N
        "-xc99=none", // Legacy // NOI18N
        "", // Default // NOI18N
        "-xstrconst -xc99", // Modern // NOI18N
    };
    
    private static final String[] LANGUAGE_EXT_OPTIONS = {
        "-Xc", // None // NOI18N
        "", // Default // NOI18N
        "", // All // NOI18N
    };
    
    /** Creates a new instance of SunCCompiler */
    public SunCCompiler(CompilerFlavor flavor, int kind, String name, String displayName, String path) {
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
        } else {
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
    
    // To be overridden
    @Override
    public String getMTLevelOptions(int value) {
        return MT_LEVEL_OPTIONS[value];
    }
    
    // To be overridden
    @Override
    public String getStandardsEvolutionOptions(int value) {
        return STANDARD_OPTIONS[value];
    }
    
    // To be overridden
    @Override
    public String getLanguageExtOptions(int value) {
        return LANGUAGE_EXT_OPTIONS[value];
    }
    
    @Override
    protected void parseCompilerOutput(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                int includeIndex = line.indexOf("-I"); // NOI18N
                while (includeIndex > 0) {
                    String token;
                    int spaceIndex = line.indexOf(" ", includeIndex + 1); // NOI18N
                    if (spaceIndex > 0) {
                        token = line.substring(includeIndex+2, spaceIndex);
                        systemIncludeDirectoriesList.addUnique(normalizePath(token));
                        includeIndex = line.indexOf("-I", spaceIndex); // NOI18N
                    } else {
                        token = line.substring(includeIndex+2);
                        systemIncludeDirectoriesList.addUnique(normalizePath(token));
                        break;
                    }
                }
                parseUserMacros(line, systemPreprocessorSymbolsList);
            }
            // Adding "__STDC__=0". It's missing from dryrun output
            systemPreprocessorSymbolsList.add("__STDC__=0"); // NOI18N
            
            is.close();
            reader.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
        }
    }
    
    private void dumpLists() {
        System.out.println("==================================" + getDisplayName()); // NOI18N
        for (int i = 0; i < systemIncludeDirectoriesList.size(); i++) {
            System.out.println("-I" + systemIncludeDirectoriesList.get(i)); // NOI18N
        }
        for (int i = 0; i < systemPreprocessorSymbolsList.size(); i++) {
            System.out.println("-D" + systemPreprocessorSymbolsList.get(i)); // NOI18N
        }
    }
    
    
    @Override
    protected String getDefaultPath() {
        return "cc"; // NOI18N
    }
    
    @Override
    protected String getCompilerStderrCommand() {
        return compilerStderrCommand;
    }

    @Override
    protected String getCompilerStderrCommand2() {
        return null;
    }
}
