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
import java.io.IOException;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.ErrorManager;

public class SunCCompiler extends SunCCCCompiler {
    private static final String compilerStderrCommand = " -xdryrun -E"; // NOI18N
    
    /**
     * Creates a new instance of SunCCompiler
     * private: use factory methods instead
     */
    private SunCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }
    
    @Override
    public SunCCompiler createCopy() {
        SunCCompiler copy = new SunCCompiler(getExecutionEnvironment(), getFlavor(), getKind(), "", getDisplayName(), getPath());
        copy.setName(getName());
        copy.setSystemIncludeDirectories(getSystemIncludeDirectories());
        copy.setSystemPreprocessorSymbols(getSystemPreprocessorSymbols());
        return copy;
    }

    public static SunCCompiler create(ExecutionEnvironment env, CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        return new SunCCompiler(env, flavor, kind, name, displayName, path);
    }

    @Override
    public CompilerDescriptor getDescriptor() {
        return getFlavor().getToolchainDescriptor().getC();
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
                    int spaceIndex = line.indexOf(" ", includeIndex + 1); // NOI18N
                    if (spaceIndex > 0) {
                        token = line.substring(includeIndex+2, spaceIndex);
                        pair.systemIncludeDirectoriesList.addUnique(applyPathPrefix(token));
                        includeIndex = line.indexOf("-I", spaceIndex); // NOI18N
                    } else {
                        token = line.substring(includeIndex+2);
                        pair.systemIncludeDirectoriesList.addUnique(applyPathPrefix(token));
                        break;
                    }
                }
                parseUserMacros(line, pair.systemPreprocessorSymbolsList);
            }
            // Adding "__STDC__=0". It's missing from dryrun output
            pair.systemPreprocessorSymbolsList.add("__STDC__=0"); // NOI18N
            
            reader.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
        }
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
