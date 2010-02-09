/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.toolchain.compilers;

import java.io.BufferedReader;
import java.io.IOException;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.ErrorManager;

/*package*/ class SunCCCompiler extends SunCCCCompiler {
    /** 
     * Creates a new instance of SunCCCompiler
     */
    protected SunCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }
    
    @Override
    public SunCCCompiler createCopy() {
        SunCCCompiler copy = new SunCCCompiler(getExecutionEnvironment(), getFlavor(), getKind(), getName(), getDisplayName(), getPath());
        if (isReady()) {
            copy.setSystemIncludeDirectories(getSystemIncludeDirectories());
            copy.setSystemPreprocessorSymbols(getSystemPreprocessorSymbols());
        }
        return copy;
    }

    public static SunCCCompiler create(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        return new SunCCCompiler(env, flavor, kind, name, displayName, path);
    }

    @Override
    public CompilerDescriptor getDescriptor() {
        return getFlavor().getToolchainDescriptor().getCpp();
    }
    
    @Override
    protected void parseCompilerOutput(BufferedReader reader, Pair pair) {
        
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                int includeIndex = line.indexOf("-I"); // NOI18N
                while (includeIndex > 0) {
                    String token;
                    int spaceIndex = line.indexOf(' ', includeIndex + 1); // NOI18N
                    if (spaceIndex > 0) {
                        token = line.substring(includeIndex+2, spaceIndex);
                    } else {
                        token = line.substring(includeIndex+2);
                    }
                    if ( ! token.equals("-xbuiltin")) { //NOI18N
                        pair.systemIncludeDirectoriesList.addUnique(applyPathPrefix(token));
                    }
                    if (token.endsWith("Cstd")) { // NOI18N
                        // See 89872 "Parser Settings" for Sun Compilers Collection are incorrect
                        pair.systemIncludeDirectoriesList.addUnique(applyPathPrefix(token.substring(0, token.length()-4) + "std")); // NOI18N
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
                parseUserMacros(line, pair.systemPreprocessorSymbolsList);
                if (line.startsWith("#define ")) { // NOI18N
                    int sepIdx = -1; // index of space separating macro name and body
                    int parCount = 0; // parenthesis counter
                    loop: for (int i = 8; i < line.length(); ++i) {
                        switch (line.charAt(i)) {
                            case '(':
                                ++parCount;
                                break;
                            case ')':
                                --parCount;
                                break;
                            case ' ':
                                if (parCount == 0) {
                                    sepIdx = i;
                                    break loop;
                                }
                        }
                    }
                    if (sepIdx > 0) {
                        String token = line.substring(8, sepIdx) + "=" + line.substring(sepIdx + 1); // NOI18N
                        pair.systemPreprocessorSymbolsList.addUnique(token);
                    }
                }
            }
            reader.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
        }
    }   
}
