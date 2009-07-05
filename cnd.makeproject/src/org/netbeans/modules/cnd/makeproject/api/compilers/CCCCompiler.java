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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.execution.LinkSupport;
import org.netbeans.modules.cnd.api.remote.CommandProvider;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

public abstract class CCCCompiler extends BasicCompiler {
    private Pair compilerDefinitions;
    private static File tmpFile = null;
    
    protected CCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }

    @Override
    public boolean setSystemIncludeDirectories(List<String> values) {
        assert values != null;
        if (compilerDefinitions == null) {
            compilerDefinitions = new Pair();
        }
        if (values.equals(compilerDefinitions.systemIncludeDirectoriesList)) {
            return false;
        }
        PersistentList<String> systemIncludeDirectoriesList = new PersistentList<String>(values);
        normalizePaths(systemIncludeDirectoriesList);
        compilerDefinitions.systemIncludeDirectoriesList = systemIncludeDirectoriesList;
        saveSystemIncludesAndDefines();
        return true;
    }

    @Override
    public boolean setSystemPreprocessorSymbols(List<String> values) {
        assert values != null;
        if (compilerDefinitions == null) {
            compilerDefinitions = new Pair();
        }
        if (values.equals(compilerDefinitions.systemPreprocessorSymbolsList)) {
            return false;
        }
        compilerDefinitions.systemPreprocessorSymbolsList = new PersistentList<String>(values);
        saveSystemIncludesAndDefines();
        return true;
    }

    @Override
    public List<String> getSystemPreprocessorSymbols() {
        if (compilerDefinitions != null){
            return compilerDefinitions.systemPreprocessorSymbolsList;
        }
        getSystemIncludesAndDefines();
        return compilerDefinitions.systemPreprocessorSymbolsList;
    }

    @Override
    public List<String> getSystemIncludeDirectories() {
        if (compilerDefinitions != null){
            return compilerDefinitions.systemIncludeDirectoriesList;
        }
        getSystemIncludesAndDefines();
        return compilerDefinitions.systemIncludeDirectoriesList;
    }

    public void saveSystemIncludesAndDefines() {
        if (compilerDefinitions != null){
            compilerDefinitions.systemIncludeDirectoriesList.saveList(getUniqueID() + "systemIncludeDirectoriesList"); // NOI18N
            compilerDefinitions.systemPreprocessorSymbolsList.saveList(getUniqueID() + "systemPreprocessorSymbolsList"); // NOI18N
        }
    }

    private void restoreSystemIncludesAndDefines() {
        PersistentList<String> systemIncludeDirectoriesList = PersistentList.restoreList(getUniqueID() + "systemIncludeDirectoriesList"); // NOI18N
        PersistentList<String>systemPreprocessorSymbolsList = PersistentList.restoreList(getUniqueID() + "systemPreprocessorSymbolsList"); // NOI18N
        if (systemIncludeDirectoriesList != null && systemPreprocessorSymbolsList != null) {
            compilerDefinitions = new Pair(systemIncludeDirectoriesList, systemPreprocessorSymbolsList);
        }
    }

    private synchronized void getSystemIncludesAndDefines() {
        if (compilerDefinitions == null) {
            restoreSystemIncludesAndDefines();
            if (compilerDefinitions == null) {
                compilerDefinitions = getFreshSystemIncludesAndDefines();
                saveSystemIncludesAndDefines();
            }
        }
    }

    public void resetSystemIncludesAndDefines() {
        compilerDefinitions = getFreshSystemIncludesAndDefines();
        saveSystemIncludesAndDefines();
    }

    public String getMTLevelOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getMultithreadingFlags() != null && compiler.getMultithreadingFlags().length > value){
            return compiler.getMultithreadingFlags()[value];
        }
        return ""; // NOI18N
    }
    
    public String getLibraryLevelOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getLibraryFlags() != null && compiler.getLibraryFlags().length > value){
            return compiler.getLibraryFlags()[value];
        }
        return ""; // NOI18N
    }
    
    public String getStandardsEvolutionOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getStandardFlags() != null && compiler.getStandardFlags().length > value){
            return compiler.getStandardFlags()[value];
        }
        return ""; // NOI18N
    }
    
    public String getLanguageExtOptions(int value) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getLanguageExtensionFlags() != null && compiler.getLanguageExtensionFlags().length > value){
            return compiler.getLanguageExtensionFlags()[value];
        }
        return ""; // NOI18N
    }
    
    protected void getSystemIncludesAndDefines(String arguments, boolean stdout, Pair pair) throws IOException {
        String path = getPath();
        if (path != null && path.length() == 0) {
            return;
        }
        if (path == null || !PlatformInfo.getDefault(getExecutionEnvironment()).fileExists(path)) {
            path = getDefaultPath();
        }
        String command = path;
        path = IpeUtils.getDirName(path);
        if (getExecutionEnvironment().isLocal() && Utilities.isWindows()) {
            command = LinkSupport.resolveWindowsLink(command);
        }

        Process process;
        InputStream is = null;
        BufferedReader reader;

        PlatformInfo pi = PlatformInfo.getDefault(getExecutionEnvironment());
        Map<String, String> env = pi.getEnv();
        if (getExecutionEnvironment().isRemote()) {
            CommandProvider provider = Lookup.getDefault().lookup(CommandProvider.class);
            if (provider != null) {
                String newPath = env.get(pi.getPathName());
                newPath = newPath == null ? "" : newPath + pi.pathSeparator();
                newPath += path;
                env.put(pi.getPathName(), newPath);

                provider.run(getExecutionEnvironment(), remote_command(command + arguments, stdout), env);
                reader = new BufferedReader(new StringReader(provider.getOutput()));
            } else {
                Logger.getLogger("cnd.remote.logger").warning("CommandProvider for remote run is not found"); //NOI18N
                return;
            }
        } else {
            List<String> newEnv = new ArrayList<String>();
            for (Map.Entry<String, String> entry : env.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.equals(pi.getPathName())) {
                    newEnv.add(pi.getPathName() + "=" + path + pi.pathSeparator() + value); // NOI18N
                } else {
                    newEnv.add(key + "=" + (value != null ? value : "")); // NOI18N
                }
            }
            process = Runtime.getRuntime().exec(command + arguments + " " + tmpFile(), newEnv.toArray(new String[newEnv.size()])); // NOI18N
            if (stdout) {
                is = process.getInputStream();
            } else {
                is = process.getErrorStream();
            }
            reader = new BufferedReader(new InputStreamReader(is));
        }
        parseCompilerOutput(reader, pair);
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ex) {
        }
    }
    
    //TODO: move to a more convenient place and remove fixed tempfile name
    private String remote_command(String command, boolean use_stdout) {
        String diversion = use_stdout ? "" : "2>&1 "; // NOI18N
        return "sh -c \"touch /tmp/xyz.c; " + command + " /tmp/xyz.c " + diversion + "; rm -f /tmp/xyz.c\""; // NOI18N
    }
    
    // To be overridden
    protected abstract void parseCompilerOutput(BufferedReader reader, Pair pair);

    protected abstract Pair getFreshSystemIncludesAndDefines();

    protected String getDefaultPath() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getNames().length > 0){
            return compiler.getNames()[0];
        }
        return ""; // NOI18N
    }
    
    /**
     * Determines whether the given macro presents in the list
     * @param macrosList list of macros strings (in the form "macro=value" or just "macro")
     * @param macroToFind the name of the macro to search for
     * @return true if macro with the given name is found, otherwise false
     */
    protected boolean containsMacro(List macrosList, String macroToFind) {
	int len = macroToFind.length();
	for (Iterator it = macrosList.iterator(); it.hasNext();) {
	    String macro = (String) it.next();
	    if (macro.startsWith(macroToFind) ) {
		if( macro.length() == len ) {
		    return true; // they are just equal
		}
		if( macro.charAt(len) == '=' ) {
		    return true; // it presents in the form macro=value
		}
	    }
	}
	return false;
    }

    protected void parseUserMacros(final String line, final PersistentList<String> preprocessorList) {
        int defineIndex = line.indexOf("-D"); // NOI18N
        while (defineIndex >= 0) {
            String token;
            int spaceIndex = line.indexOf(" ", defineIndex + 1); // NOI18N
            if (spaceIndex > 0) {
                token = line.substring(defineIndex+2, spaceIndex);
                if (defineIndex > 0 && line.charAt(defineIndex-1)=='"') {
                    if (token.length() > 0 && token.charAt(token.length()-1)=='"') {
                        token = token.substring(0,token.length()-1);
                    }
                }
                preprocessorList.add(token);
                defineIndex = line.indexOf("-D", spaceIndex); // NOI18N
            } else {
                token = line.substring(defineIndex+2);
                if (defineIndex > 0 && line.charAt(defineIndex-1)=='"') {
                    if (token.length() > 0 && token.charAt(token.length()-1)=='"') {
                        token = token.substring(0,token.length()-1);
                    }
                }
                preprocessorList.add(token);
                break;
            }
        }
    }
    
    private String tmpFile() {
        if (tmpFile == null) {
            try {
                tmpFile = File.createTempFile("xyz", ".c"); // NOI18N
                tmpFile.deleteOnExit();
            } catch (IOException ioe) {
            }
        }
        if (tmpFile != null) {
            return tmpFile.getAbsolutePath();
        } else {
            return "/dev/null"; // NOI18N
        }
    }
    
    protected String getUniqueID() {
        if (getCompilerSet() == null || getCompilerSet().isAutoGenerated()) {
            return getClass().getName() +
                    ExecutionEnvironmentFactory.toUniqueID(getExecutionEnvironment()).hashCode() + getPath().hashCode() + "."; // NOI18N
        } else {
            return getClass().getName() + getCompilerSet().getName() +
                    ExecutionEnvironmentFactory.toUniqueID(getExecutionEnvironment()).hashCode() + getPath().hashCode() + "."; // NOI18N
        }
    }

    private void dumpLists() {
        System.out.println("==================================" + getDisplayName()); // NOI18N
        for (int i = 0; i < compilerDefinitions.systemIncludeDirectoriesList.size(); i++) {
            System.out.println("-I" + compilerDefinitions.systemIncludeDirectoriesList.get(i)); // NOI18N
        }
        for (int i = 0; i < compilerDefinitions.systemPreprocessorSymbolsList.size(); i++) {
            System.out.println("-D" + compilerDefinitions.systemPreprocessorSymbolsList.get(i)); // NOI18N
        }
    }

    protected final class Pair {
        public PersistentList<String> systemIncludeDirectoriesList;
        public PersistentList<String> systemPreprocessorSymbolsList;
        public Pair(){
            systemIncludeDirectoriesList = new PersistentList<String>();
            systemPreprocessorSymbolsList = new PersistentList<String>();
        }
        public Pair(PersistentList<String> systemIncludeDirectoriesList,
                    PersistentList<String> systemPreprocessorSymbolsList){
            this.systemIncludeDirectoriesList = systemIncludeDirectoriesList;
            this.systemPreprocessorSymbolsList = systemPreprocessorSymbolsList;
        }
    }
}
