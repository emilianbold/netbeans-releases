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
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class SunCCCompiler extends SunCCCCompiler {
    private static final String compilerStderrCommand = " -xdryrun -E"; // NOI18N
    private static final String compilerStderrCommand2 = " -xdumpmacros=defs,sys -E"; // NOI18N
    
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
    public SunCCCompiler(CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(flavor, kind, name, displayName, path);
    }
    
    @Override
    public SunCCCompiler createCopy() {
        SunCCCompiler copy = new SunCCCompiler(getFlavor(), getKind(), "", getDisplayName(), getPath());
        copy.setName(getName());
        return copy;
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
    public String getLibraryLevelOptions(int value) {
        return LIBRARY_LEVEL_OPTIONS[value];
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
                    } else {
                        token = line.substring(includeIndex+2);
                    }
                    if ( ! token.equals("-xbuiltin")) { //NOI18N
                        systemIncludeDirectoriesList.addUnique(normalizePath(token));
                    }
                    if (token.endsWith("Cstd")) { // NOI18N
                        // See 89872 "Parser Settings" for Sun Compilers Collection are incorrect
                        systemIncludeDirectoriesList.addUnique(normalizePath(token.substring(0, token.length()-4) + "std")); // NOI18N
                    }
                    // Hack to handle -compat flag. If this flag is added,
                    // the compiler looks in in CC4 and not in CC. Just adding CC4 doesn't
                    // fix this problem but it may work for some include files
//                  if (token.endsWith("include/CC")) // NOI18N
//                      systemIncludeDirectoriesList.addUnique(normalizePath(token + "4")); // NOI18N
                    if (spaceIndex > 0) {
                        includeIndex = line.indexOf("-I", spaceIndex); // NOI18N
                    } else {
                        break;
                    }
                }
                parseUserMacros(line, systemPreprocessorSymbolsList);
                if (line.startsWith("#define ")) { // NOI18N
                    int i = line.indexOf(' ', 8);
                    if (i > 0) {
                        String token = line.substring(8, i) + "=" + line.substring(i+1); // NOI18N
                        systemPreprocessorSymbolsList.add(token);
                    }
                }
            }
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
        return "CC"; // NOI18N
    }
    
    @Override
    protected String getCompilerStderrCommand() {
        return compilerStderrCommand;
    }

    @Override
    protected String getCompilerStderrCommand2() {
        return compilerStderrCommand2;
    }
}
