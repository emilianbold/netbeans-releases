/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.PredefinedMacro;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetPreferences;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

public abstract class CCCCompiler extends AbstractCompiler {

    private static final Logger LOG = Logger.getLogger(CCCCompiler.class.getName());
    private static final String DEV_NULL = "/dev/null"; // NOI18N
    private static final String NB69_VERSION_PATTERN = "/var/cache/cnd/remote-includes/"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("ReadErrorStream", 2); // NOI18N

    private volatile Pair compilerDefinitions;
    private static File emptyFile = null;

    protected CCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }

    @Override
    public boolean setSystemIncludeDirectories(List<String> values) {
        return copySystemIncludeDirectoriesImpl(values, true);
    }
    
    protected final boolean copySystemIncludeDirectories(List<String> values) {
        boolean res = copySystemIncludeDirectoriesImpl(values, false);
        if (res) {
            if (values instanceof CompilerDefinition) {
                compilerDefinitions.systemIncludeDirectoriesList.userAddedDefinitions.clear();
                compilerDefinitions.systemIncludeDirectoriesList.userAddedDefinitions.addAll(((CompilerDefinition)values).userAddedDefinitions);
            }
        }
        return res;
    }
    
    private boolean copySystemIncludeDirectoriesImpl(List<String> values, boolean normalize) {
        assert values != null;
        if (compilerDefinitions == null) {
            compilerDefinitions = new Pair();
        }
        if (values.equals(compilerDefinitions.systemIncludeDirectoriesList)) {
            return false;
        }
        CompilerDefinition systemIncludeDirectoriesList = new CompilerDefinition(values);
        if (normalize) {
            normalizePaths(systemIncludeDirectoriesList);
        }
        systemIncludeDirectoriesList.userAddedDefinitions.addAll(compilerDefinitions.systemIncludeDirectoriesList.userAddedDefinitions);
        compilerDefinitions.systemIncludeDirectoriesList = systemIncludeDirectoriesList;
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
        CompilerDefinition systemPreprocessorSymbolsList = new CompilerDefinition(values);
        systemPreprocessorSymbolsList.userAddedDefinitions.addAll(compilerDefinitions.systemPreprocessorSymbolsList.userAddedDefinitions);
        compilerDefinitions.systemPreprocessorSymbolsList = systemPreprocessorSymbolsList;
        return true;
    }
    
    protected final boolean copySystemPreprocessorSymbols(List<String> values) {
        boolean res = setSystemPreprocessorSymbols(values);
        if (res) {
            if (values instanceof CompilerDefinition) {
                compilerDefinitions.systemPreprocessorSymbolsList.userAddedDefinitions.clear();
                compilerDefinitions.systemPreprocessorSymbolsList.userAddedDefinitions.addAll(((CompilerDefinition)values).userAddedDefinitions);
            }
        }
        return res;
    }

    @Override
    public List<String> getSystemPreprocessorSymbols() {
        if (compilerDefinitions == null) {
            resetSystemProperties();
        }
        return compilerDefinitions.systemPreprocessorSymbolsList;
    }

    @Override
    public List<String> getSystemIncludeDirectories() {
        if (compilerDefinitions == null) {
            resetSystemProperties();
        }
        return compilerDefinitions.systemIncludeDirectoriesList;
    }

    @Override
    public boolean isReady() {
        return compilerDefinitions != null;
    }

    @Override
    public void waitReady(boolean reset) {
        if (reset || !isReady()) {
            resetSystemProperties();
        }
    }

    @Override
    public void resetSystemProperties(boolean lazy) {
        if (lazy) {
            compilerDefinitions = null;
        } else {
            CndUtils.assertNonUiThread();
            compilerDefinitions = getFreshSystemIncludesAndDefines();
        }
    }

    
    @Override
    public void loadSettings(Preferences prefs, String prefix) {
        String version = prefs.get(CompilerSetPreferences.VERSION_KEY, "1.0"); // NOI18N
        List<String> includeDirList = new ArrayList<String>();
        List<Integer> userAddedInclude = new ArrayList<Integer>();
        String includeDirPrefix = prefix + ".systemIncludes"; // NOI18N
        int includeDirCount = prefs.getInt(includeDirPrefix + ".count", 0); // NOI18N
        for (int i = 0; i < includeDirCount; ++i) {
            String includeDir = prefs.get(includeDirPrefix + '.' + i, null); // NOI18N
            if (includeDir != null) {
                if ("1.1".equals(version)) { // NOI18N
                    if (Utilities.isWindows()) {
                        includeDir = includeDir.replace('\\', '/'); // NOI18N
                    }
                    int start = includeDir.indexOf(NB69_VERSION_PATTERN);
                    if (start > 0) {
                        includeDir = includeDir.substring(start+NB69_VERSION_PATTERN.length());
                        int index = includeDir.indexOf('/'); // NOI18N
                        if (index > 0) {
                            includeDir = includeDir.substring(index);
                        }
                    }
                    
                }
                includeDirList.add(includeDir);
                String added = prefs.get(includeDirPrefix + ".useradded." + i, null); // NOI18N
                if ("true".equals(added)) { // NOI18N
                    userAddedInclude.add(includeDirList.size()-1);
                }
            }
        }
        if (includeDirList.isEmpty()) {
            // try to load using the old way;  this might be removed at some moment in future
            List<String> oldIncludeDirList = PersistentList.restoreList(getUniqueID() + "systemIncludeDirectoriesList"); // NOI18N
            if (oldIncludeDirList != null) {
                includeDirList.addAll(oldIncludeDirList);
            }
        }
        copySystemIncludeDirectories(includeDirList);
        if (!userAddedInclude.isEmpty()) {
            for(Integer i : userAddedInclude) {
                compilerDefinitions.systemIncludeDirectoriesList.setUserAdded(true, i);
            }
        }

        List<String> preprocSymbolList = new ArrayList<String>();
        List<Integer> userAddedpreprocSymbol = new ArrayList<Integer>();
        String preprocSymbolPrefix = prefix + ".systemMacros"; // NOI18N
        int preprocSymbolCount = prefs.getInt(preprocSymbolPrefix + ".count", 0); // NOI18N
        for (int i = 0; i < preprocSymbolCount; ++i) {
            String preprocSymbol = prefs.get(preprocSymbolPrefix + '.' + i, null); // NOI18N
            if (preprocSymbol != null) {
                preprocSymbolList.add(preprocSymbol);
                String added = prefs.get(preprocSymbolPrefix + ".useradded." + i, null); // NOI18N
                if ("true".equals(added)) { // NOI18N
                    userAddedpreprocSymbol.add(preprocSymbolList.size()-1);
                }
            }
        }
        if (preprocSymbolList.isEmpty()) {
            // try to load using the old way;  this might be removed at some moment in future
            List<String> oldPreprocSymbolList = PersistentList.restoreList(getUniqueID() + "systemPreprocessorSymbolsList"); // NOI18N
            if (oldPreprocSymbolList != null) {
                preprocSymbolList.addAll(oldPreprocSymbolList);
            }
        }
        copySystemPreprocessorSymbols(preprocSymbolList);
        if (!userAddedpreprocSymbol.isEmpty()) {
            for(Integer i : userAddedpreprocSymbol) {
                compilerDefinitions.systemPreprocessorSymbolsList.setUserAdded(true, i);
            }
        }
    }

    @Override
    public void saveSettings(Preferences prefs, String prefix) {
        List<String> includeDirList = getSystemIncludeDirectories();
        String includeDirPrefix = prefix + ".systemIncludes"; // NOI18N
        prefs.putInt(includeDirPrefix + ".count", includeDirList.size()); // NOI18N
        for (int i = 0; i < includeDirList.size(); ++i) {
            prefs.put(includeDirPrefix + '.' + i, includeDirList.get(i)); // NOI18N
            if (compilerDefinitions.systemIncludeDirectoriesList.isUserAdded(i)) {
                prefs.put(includeDirPrefix + ".useradded." + i, "true"); // NOI18N
            }
        }

        List<String> preprocSymbolList = getSystemPreprocessorSymbols();
        String preprocSymbolPrefix = prefix + ".systemMacros"; // NOI18N
        prefs.putInt(preprocSymbolPrefix + ".count", preprocSymbolList.size()); // NOI18N
        for (int i = 0; i < preprocSymbolList.size(); ++i) {
            prefs.put(preprocSymbolPrefix + '.' + i, preprocSymbolList.get(i)); // NOI18N
            if (compilerDefinitions.systemPreprocessorSymbolsList.isUserAdded(i)) {
                prefs.put(preprocSymbolPrefix + ".useradded." + i, "true"); // NOI18N
            }
        }
    }

    protected final void getSystemIncludesAndDefines(String arguments, final boolean stdout, Pair pair) throws IOException {
        String compilerPath = getPath();
        if (compilerPath == null || compilerPath.length() == 0) {
            return;
        }
        ExecutionEnvironment execEnv = getExecutionEnvironment();
        NativeProcess startedProcess = null;
        Task errorTask = null;
        try {
            if (execEnv.isLocal() && Utilities.isWindows()) {
                compilerPath = LinkSupport.resolveWindowsLink(compilerPath);
            }
            if (!HostInfoUtils.fileExists(execEnv, compilerPath)) {
                compilerPath = getDefaultPath();
                if (!HostInfoUtils.fileExists(execEnv, compilerPath)) {
                    return;
                }
            }

            List<String> argsList = new ArrayList<String>();
            argsList.addAll(Arrays.asList(arguments.trim().split(" +"))); // NOI18N
            argsList.add(getEmptyFile(execEnv));

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(compilerPath);
            npb.setArguments(argsList.toArray(new String[argsList.size()]));
            npb.getEnvironment().prependPathVariable("PATH", ToolUtils.getDirName(compilerPath)); // NOI18N
            
            final NativeProcess process = npb.call();
            startedProcess = process;
            if (process.getState() != State.ERROR) {
                InputStream stream = stdout? process.getInputStream() : process.getErrorStream();
                errorTask = RP.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (stdout) {
                                ProcessUtils.readProcessError(process);
                            } else {
                                ProcessUtils.readProcessOutput(process);
                            }
                        } catch (Throwable ex) {
                        }
                    }
                });
                
                if (stream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    try {
                        parseCompilerOutput(reader, pair);
                    } finally {
                        reader.close();
                    }
                }
                process.waitFor();
                startedProcess = null;
                errorTask = null;
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new IOException(ex);
        } finally {
            if (errorTask != null){
                errorTask.cancel();
            }
            if (startedProcess != null) {
                startedProcess.destroy();
            }
        }
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
    protected boolean containsMacro(List<String> macrosList, String macroToFind) {
	int len = macroToFind.length();
	for (Iterator<String> it = macrosList.iterator(); it.hasNext();) {
	    String macro = it.next();
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

    static void parseUserMacros(final String line, final List<String> preprocessorList) {
        List<String> list = scanCommandLine(line);
        for(String s : list) {
            if (s.startsWith("\"") && s.endsWith("\"") || // NOI18N
                s.startsWith("'") && s.endsWith("'")) { // NOI18N
                if (s.length() > 2) {
                    s = s.substring(1, s.length()-1).trim();
                }
            }
            if (s.startsWith("-D")) { // NOI18N
                String token = s.substring(2);
                if (token.length() > 0) {
                    String name = token;
                    int i = token.indexOf('=');
                    if (i >= 0) {
                        name = token.substring(0,i);
                    }
                    if (isValidMacroName(name)) {
                        addUnique(preprocessorList, token);
                    }
                }
            }
        }
    }

    static boolean isValidMacroName(String macroName) {
        boolean par = false;
        for (int i = 0; i < macroName.length(); i++) {
            char c = macroName.charAt(i);
            if (c == '_') {
                continue;
            } else if (c >= 'A' && c <= 'Z') {
                continue;
            } else if (c >= 'a' && c <= 'z') {
                continue;
            } else if (c >= '0' && c <= '9' && i > 0) {
                continue;
            } else if (c == '(' && i > 0) {
                if (par) {
                    return false;
                }
                par = true;
            } else if (c == ')') {
                if (!par) {
                    return false;
                }
                return i == macroName.length() - 1;
            } else if (c == ' ' || c == ',' || c == '.') {
                if (!par) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
    
    static String[] getMacro(String line) {
        int sepIdx = -1; // index of space separating macro name and body
        int parCount = 0; // parenthesis counter
        loop:
        for (int i = 0; i < line.length(); ++i) {
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
            return new String[] {line.substring(0, sepIdx),line.substring(sepIdx + 1).trim()};
        } else {
            return new String[] {line, null};
        }
    }
    
    private static List<String> scanCommandLine(String line){
        List<String> res = new ArrayList<String>();
        int i = 0;
        StringBuilder current = new StringBuilder();
        boolean isSingleQuoteMode = false;
        boolean isDoubleQuoteMode = false;
        while (i < line.length()) {
            char c = line.charAt(i);
            i++;
            switch (c){
                case '\'': // NOI18N
                    if (isSingleQuoteMode) {
                        isSingleQuoteMode = false;
                    } else if (!isDoubleQuoteMode) {
                        isSingleQuoteMode = true;
                    }
                    current.append(c);
                    break;
                case '\"': // NOI18N
                    if (isDoubleQuoteMode) {
                        isDoubleQuoteMode = false;
                    } else if (!isSingleQuoteMode) {
                        isDoubleQuoteMode = true;
                    }
                    current.append(c);
                    break;
                case ' ': // NOI18N
                case '\t': // NOI18N
                case '\n': // NOI18N
                case '\r': // NOI18N
                    if (isSingleQuoteMode || isDoubleQuoteMode) {
                        current.append(c);
                        break;
                    } else {
                        if (current.length()>0) {
                            res.add(current.toString());
                            current.setLength(0);
                        }
                    }
                    break;
                default:
                    current.append(c);
                    break;
            }
        }
        if (current.length()>0) {
            res.add(current.toString());
        }
        return res;
    }

    private String getEmptyFile(ExecutionEnvironment execEnv) {
        if (execEnv.isLocal() && Utilities.isWindows()) {
            // no /dev/null on Windows, so we need a real file
            if (emptyFile == null) {
                try {
                    File tmpFile = File.createTempFile("xyz", ".c"); // NOI18N
                    tmpFile.deleteOnExit();
                    emptyFile = tmpFile;
                } catch (IOException ioe) {
                }
            }
            return emptyFile == null? DEV_NULL : emptyFile.getAbsolutePath();
        } else {
            return DEV_NULL;
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

    protected static void addUnique(List<String> list, String element) {
        String pattern = element;
        if (element.indexOf('=') > 0) {
            pattern = pattern.substring(0, element.indexOf('='));
        }
        for(String s : list) {
            if (s.indexOf('=') > 0) {
                if (pattern.equals(s.substring(0, s.indexOf('=')))) {
                    return;
                }
            } else {
                if (pattern.equals(s)) {
                    return;
                }
            }
        }
        list.add(element);
    }
    
    protected static void removeUnique(List<String> list, String element) {
        for(int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (s.startsWith(element)) {
                if (s.length() > element.length() && s.charAt(element.length())=='=' ||
                    s.contains(element)) {
                    list.remove(i);
                    break;
                }
            }
        }
    }
    
    protected void checkModel(Pair res, MyCallable<Pair> get) {
        if (!LOG.isLoggable(Level.FINE)) {
            return;
        }
        final CompilerDescriptor descriptor = getDescriptor();
        if (descriptor == null) {
            return;
        }
        final List<ToolchainManager.PredefinedMacro> predefinedMacros = descriptor.getPredefinedMacros();
        if (predefinedMacros == null || predefinedMacros.isEmpty()) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        buf.append("Compiler: ").append(getPath()); // NOI18N
        Set<String> checked = new HashSet<String>();
        for (ToolchainManager.PredefinedMacro macro : predefinedMacros) {
            if (macro.getFlags() != null && !checked.contains(macro.getFlags())) {
                checked.add(macro.getFlags());
                Pair tmp = get.call(macro.getFlags());
                if (tmp.systemPreprocessorSymbolsList.size() == 0) {
                    buf.append("\nThe flag ").append(macro.getFlags()).append(" is not supported"); // NOI18N
                    continue;
                }
                completePredefinedMacros(tmp);
                List<String> acatualDiff = new ArrayList<String>();
                for (String t : tmp.systemPreprocessorSymbolsList) {
                    boolean found = false;
                    for (String s : res.systemPreprocessorSymbolsList) {
                        if (s.equals(t)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        acatualDiff.add(t);
                    }
                }
                List<String> actualRm = new ArrayList<String>();
                for (String t : res.systemPreprocessorSymbolsList) {
                    boolean found = false;
                    for (String s : tmp.systemPreprocessorSymbolsList) {
                        if (s.equals(t)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        actualRm.add(t);
                    }
                }
                List<String> expectedDiff = new ArrayList<String>();
                List<String> expectedRm = new ArrayList<String>();
                for (ToolchainManager.PredefinedMacro m : predefinedMacros) {
                    if (m.getFlags() != null && m.getFlags().equals(macro.getFlags())) {
                        if (m.isHidden()) {
                            expectedRm.add(m.getMacro());
                        } else {
                            expectedDiff.add(m.getMacro());
                        }
                    }
                }
                if (!acatualDiff.isEmpty() || !expectedDiff.isEmpty() || !actualRm.isEmpty() || !expectedRm.isEmpty()) {
                    buf.append("\nThe flag ").append(macro.getFlags()); // NOI18N
                    if (!acatualDiff.isEmpty() || !expectedDiff.isEmpty()) {
                        buf.append("\n\tadds/changes predefined macros:"); // NOI18N
                        for (String t : acatualDiff) {
                            buf.append("\n\t\t").append(t); // NOI18N
                        }
                        buf.append("\n\tby tool collection descriptor:"); // NOI18N
                        for (String t : expectedDiff) {
                            buf.append("\n\t\t").append(t); // NOI18N
                        }
                    }
                    if (!actualRm.isEmpty() || !expectedRm.isEmpty()) {
                        buf.append("\n\tremoves predefined macros:"); // NOI18N
                        for (String t : actualRm) {
                            buf.append("\n\t\t").append(t); // NOI18N
                        }
                        buf.append("\n\tby tool collection descriptor:"); // NOI18N
                        for (String t : expectedRm) {
                            buf.append("\n\t\t").append(t); // NOI18N
                        }
                    }
                }
            }
        }
        LOG.log(Level.FINE, buf.toString());
    }

    protected static final class Pair {
        public CompilerDefinition systemIncludeDirectoriesList;
        public CompilerDefinition systemPreprocessorSymbolsList;
        public Pair(){
            systemIncludeDirectoriesList = new CompilerDefinition(0);
            systemPreprocessorSymbolsList = new CompilerDefinition(0);
        }
    }
    
    protected void completePredefinedMacros(Pair pair) {
        final CompilerDescriptor descriptor = getDescriptor();
        if (descriptor != null) {
            final List<PredefinedMacro> predefinedMacros = descriptor.getPredefinedMacros();
            if (predefinedMacros != null) {
                for(ToolchainManager.PredefinedMacro macro : predefinedMacros) {
                    if (macro.getFlags() == null) {
                        if (macro.isHidden()) {
                            // remove macro
                            removeUnique(pair.systemPreprocessorSymbolsList, macro.getMacro());
                        } else {
                            // add macro
                            addUnique(pair.systemPreprocessorSymbolsList, macro.getMacro());
                        }
                    }
                }
            }
        }
    }
    
    public static final class CompilerDefinition extends ArrayList<String> {
        private List<Integer> userAddedDefinitions = new ArrayList<Integer>(0);
        
        public CompilerDefinition() {
            super();
        }
        
        public CompilerDefinition(int size) {
            super(size);
        }
        
        public CompilerDefinition(Collection<String> c) {
            super(c);
        }
        
        public boolean isUserAdded(int i) {
            return userAddedDefinitions.contains(i);
        }
        
        public void setUserAdded(boolean isUserAddes, int i) {
            if (isUserAddes) {
                if (!userAddedDefinitions.contains(i)) {
                    userAddedDefinitions.add(i);
                }
            } else {
                if (userAddedDefinitions.contains(i)) {
                    userAddedDefinitions.remove(Integer.valueOf(i));
                }
            }
        }

        public void sort() {
            Set<String> set = new HashSet<String>();
            for(Integer i : userAddedDefinitions) {
                if (i < size()) {
                    set.add(get(i));
                }
            }
            Collections.sort(this, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareToIgnoreCase(s2);
                }
            });
            userAddedDefinitions.clear();
            for(String s : set) {
                userAddedDefinitions.add(indexOf(s));
            }
        }
    }
    
    protected interface MyCallable<V>{
        V call(String p);
    }
}
