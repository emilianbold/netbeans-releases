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
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.remote.CommandProvider;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.openide.util.Lookup;

public abstract class CCCCompiler extends BasicCompiler {
    private static File tmpFile = null;
    
    public CCCCompiler(String hkey, CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(hkey, flavor, kind, name, displayName, path);
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
    
    protected void getSystemIncludesAndDefines(String path, String command, boolean stdout) throws IOException {
            Process process;
            InputStream is = null;
            BufferedReader reader;
            
            if (path == null) {
                path = ""; // NOI18N
            }
            PlatformInfo pi = PlatformInfo.getDefault(getHostKey());
            Map<String, String> env = pi.getEnv();
            if (!getHostKey().equals(CompilerSetManager.LOCALHOST)) {
                CommandProvider provider = (CommandProvider) Lookup.getDefault().lookup(CommandProvider.class);
                if (provider != null) {
                    String newPath = env.get(pi.getPathName());
                    newPath = newPath == null? "" : newPath + pi.pathSeparator();
                    newPath += path;
                    env.put(pi.getPathName(), newPath);

                    provider.run(getHostKey(), remote_command(command, stdout), env);
                    reader = new BufferedReader(new StringReader(provider.getOutput()));
                } else {
                    Logger.getLogger("cnd.remote.logger").warning("CommandProvider for remote run is not found"); //NOI18N
                    return;
                }
            } else {
                List<String> newEnv = new ArrayList<String>();
                for (String key : env.keySet()) {
                    String value = env.get(key);
                    if (key.equals(pi.getPathName())) {
                        newEnv.add(pi.getPathName() + "=" + path + pi.pathSeparator() + value); // NOI18N
                    }
                    else {
                        String entry = key + "=" + (value != null ? value : ""); // NOI18N
                        newEnv.add(entry);
                    }
                }
                process = Runtime.getRuntime().exec(command + " " + tmpFile(), (String[])newEnv.toArray(new String[newEnv.size()])); // NOI18N
                if (stdout) {
                    is = process.getInputStream();
                } else {
                    is = process.getErrorStream();
                }
                reader = new BufferedReader(new InputStreamReader(is));
            }
            parseCompilerOutput(reader);
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
            }
        }
    
    //TODO: move to a more convenient place and remove fixed tempfile name
    private String remote_command(String command, boolean use_stdout) {
        String diversion = use_stdout ? "" : "2>&1 > /dev/null"; // NOI18N
        return "touch /tmp/xyz.c; " + command + " /tmp/xyz.c " + diversion + "; rm -f /tmp/xyz.c"; // NOI18N
    }
    
    // To be overridden
    public abstract void saveSystemIncludesAndDefines();
    
    // To be overridden
    public abstract void resetSystemIncludesAndDefines();
    
    // To be overridden
    protected abstract void parseCompilerOutput(BufferedReader reader);
    
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

    protected void parseUserMacros(final String line, final PersistentList preprocessorList) {
        int defineIndex = line.indexOf("-D"); // NOI18N
        while (defineIndex > 0) {
            String token;
            int spaceIndex = line.indexOf(" ", defineIndex + 1); // NOI18N
            if (spaceIndex > 0) {
                token = line.substring(defineIndex+2, spaceIndex);
                preprocessorList.add(token);
                defineIndex = line.indexOf("-D", spaceIndex); // NOI18N
            } else {
                token = line.substring(defineIndex+2);
                preprocessorList.add(token);
                break;
            }
        }
    }
    
    private String tmpFile() {
        if (tmpFile == null) {
            try {
                tmpFile = File.createTempFile("xyz", ".c"); // NOI18N
}
            catch (IOException ioe) {
            }
            tmpFile.deleteOnExit();
        }
        if (tmpFile != null)
            return tmpFile.getAbsolutePath();
        else
            return "/dev/null"; // NOI18N
    }
    
    protected String getUniqueID() {
        if (getCompilerSet() == null || getCompilerSet().isAutoGenerated())
            return getClass().getName() + getHostKey().hashCode() + getPath().hashCode() + "."; // NOI18N
        else
            return getClass().getName() + getCompilerSet().getName() + getHostKey().hashCode() + getPath().hashCode() + "."; // NOI18N
    }
}
