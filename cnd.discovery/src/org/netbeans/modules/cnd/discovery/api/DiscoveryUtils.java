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

package org.netbeans.modules.cnd.discovery.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class DiscoveryUtils {

    public static final List<String> C89 = Collections.unmodifiableList(Arrays.asList("-std=c89","-std=iso9899:1990","-std=iso9899:1990","-std=c90")); // NOI18N
    public static final List<String> C99 = Collections.unmodifiableList(Arrays.asList("-xc99","-std=c9x","-std=iso9899:199409","-std=iso9899:199x","-std=iso9899:1999","-std=gnu99","-std=gnu9x","-std=c99")); // NOI18N
    public static final List<String> C11 = Collections.unmodifiableList(Arrays.asList("-std=c11","-std=gnu1x","-std=gnu11","-std=iso9899:2011","-std=c1x","-std=c11")); // NOI18N
    public static final List<String> CPP98 = Collections.unmodifiableList(Arrays.asList("-std=c++98","-std=c++03")); // NOI18N
    public static final List<String> CPP11 = Collections.unmodifiableList(Arrays.asList("-std=c++0x","-std=c++11","-std=gnu++0x","-std=gnu++11")); // NOI18N
    public static final List<String> CPP14 = Collections.unmodifiableList(Arrays.asList("-std=c++14","-std=gnu++1y","-std=c++1y","-std=gnu++1z","-std=c++1z")); // NOI18N
    
    private DiscoveryUtils() {
    }
    
    public static ProjectBridge getProjectBridge(ProjectProxy project) {
        if (project != null) {
            Project p = project.getProject();
            if (p != null){
                ProjectBridge bridge = new ProjectBridge(p);
                if (bridge.isValid()) {
                    return bridge;
                }
            }
        }
        return null;
    }
    
    public static String resolveSymbolicLink(FileSystem fileSystem, final String aPath) {
        if (fileSystem == null || FileSystemProvider.getExecutionEnvironment(fileSystem).isLocal()) {
            try {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                    @Override
                    public String run() throws IOException {
                        String path = aPath;
                        for (int i = 0; i < 5; i++) {
                            final Path file = Paths.get(Utilities.toURI(new File(path)));
                            if (Files.isSymbolicLink(file)) {
                                Path to = Files.readSymbolicLink(file);
                                if (!to.isAbsolute()) {
                                    to = file.getParent().resolve(Files.readSymbolicLink(file)).normalize();
                                }
                                if (Files.isRegularFile(to)) {
                                    return to.toString();
                                }
                                path = to.toString();
                            } else {
                                return null;
                            }
                        }
                        return null;
                    }
                });
            } catch (Exception ex) {
                CndUtils.printStackTraceOnce(ex);
                return null;
            }
        } else {
            try {
                FileObject fo = fileSystem.findResource(aPath);
                if (fo == null) {
                    return null;
                }
                if (FileSystemProvider.isLink(fo)) {
                    return FileSystemProvider.resolveLink(fo);
                }
                return null;
            } catch (Exception ex) {
                CndUtils.printStackTraceOnce(ex);
                return null;
            }
        }
    }
    
    public static Set<String> getCompilerNames(ProjectProxy project, PredefinedToolKind kind) {
        Project p = null;
        if (project != null) {
            p = project.getProject();
        }
        return BuildTraceSupport.getCompilerNames(p, kind);
    }

    public static List<String> getSystemIncludePaths(ProjectBridge bridge, boolean isCPP) {
        if (bridge != null) {
            return bridge.getSystemIncludePaths(isCPP);
        }
        return new ArrayList<>();
    }
    
    public static CompilerFlavor getCompilerFlavor(ProjectBridge bridge){
        if (bridge != null) {
            return bridge.getCompilerFlavor();
        }
        return null;
    }

    public static String getCygwinDrive(ProjectBridge bridge){
        if (bridge != null) {
            return bridge.getCygwinDrive();
        }
        return null;
    }

    public static Map<String,String> getSystemMacroDefinitions(ProjectBridge bridge, boolean isCPP) {
        if (bridge != null) {
            return bridge.getSystemMacroDefinitions(isCPP);
        }
        return new HashMap<>();
    }

    public static List<String> scanCommandLine(String line, LogOrigin isScriptOutput){
        List<String> res = new ArrayList<>();
        int i = 0;
        StringBuilder current = new StringBuilder();
        boolean isSingleQuoteMode = false;
        boolean isDoubleQuoteMode = false;
        boolean isParen = false;
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
                    isParen = false;
                    current.append(c);
                    break;
                case '\"': // NOI18N
                    if (isDoubleQuoteMode) {
                        isDoubleQuoteMode = false;
                    } else if (!isSingleQuoteMode) {
                        isDoubleQuoteMode = true;
                    }
                    isParen = false;
                    current.append(c);
                    break;
                case ' ': // NOI18N
                case '\t': // NOI18N
                case '\n': // NOI18N
                case '\r': // NOI18N
                    if (isSingleQuoteMode || isDoubleQuoteMode) {
                        current.append(c);
                        break;
                    } else if (isParen && isScriptOutput == LogOrigin.DwarfCompileLine) {
                        current.append(c);
                    } else {
                        if (current.length()>0) {
                            res.add(current.toString());
                            current.setLength(0);
                        }
                    }
                    break;
                case '(': // NOI18N
                    if (!(isSingleQuoteMode || isDoubleQuoteMode)) {
                        isParen = true;
                    }
                    current.append(c);
                    break;
                case ')': // NOI18N
                    if (!(isSingleQuoteMode || isDoubleQuoteMode)) {
                        isParen = false;
                    }
                    current.append(c);
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

    public static String getRelativePath(String base, String path) {
        if (path.equals(base)) {
            return path;
        } else if (path.startsWith(base + '/')) { // NOI18N
            return path.substring(base.length()+1);
        } else if (path.startsWith(base + '\\')) { // NOI18N
            return path.substring(base.length() + 1);
        } else if (!(path.startsWith("/") || path.startsWith("\\") || // NOI18N
                     path.length() > 2 && path.charAt(2)==':')) { // NOI18N
            return path;
        } else {
            StringTokenizer stb = new StringTokenizer(base, "\\/"); // NOI18N
            StringTokenizer stp = new StringTokenizer(path, "\\/"); // NOI18N
            int match = 0;
            String pstring = null;
            while(stb.hasMoreTokens() && stp.hasMoreTokens()) {
                String bstring = stb.nextToken();
                pstring = stp.nextToken();
                if (bstring.equals(pstring)) {
                    match++;
                } else {
                    break;
                }
            }
            if (match <= 1){
                return path;
            }
            StringBuilder s = new StringBuilder();
            while(stb.hasMoreTokens()) {
                String bstring = stb.nextToken();
                s.append("..").append(File.separator); // NOI18N
            }
            s.append("..").append(File.separator).append(pstring); // NOI18N
            while(stp.hasMoreTokens()) {
                s.append(File.separator).append(stp.nextToken()); // NOI18N
            }
            return s.toString();
        }
    }

    public static String normalizeAbsolutePath(String path) {
        boolean caseSensitive = CndFileUtils.isSystemCaseSensitive();
        if (!caseSensitive) {
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
        }
        String normalized;
        // small optimization for true case sensitive OSs
        if (!caseSensitive || (path.endsWith("/.") || path.endsWith("\\.") || path.contains("..") || path.contains("./") || path.contains(".\\"))) { // NOI18N
            normalized = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
        } else {
            normalized = path;
        }
        return normalized;
    }

    /**
     * Path is include path like:
     * .
     * ../
     * include
     * Returns path in Unix style
     */
    public static String convertRelativePathToAbsolute(SourceFileProperties source, String path){
        if ( !( path.startsWith("/") || (path.length()>1 && path.charAt(1)==':') ) ) { // NOI18N
            if (path.equals(".")) { // NOI18N
                path = source.getCompilePath();
            } else {
                path = source.getCompilePath()+File.separator+path;
            }
            File file = new File(path);
            path = CndFileUtils.normalizeFile(file).getAbsolutePath();
        }
        if (Utilities.isWindows()) {
            path = path.replace('\\', '/'); // NOI18N
        }
        return path;
    }
    
    /**
     * parse compile line
     */
    public static List<String> gatherCompilerLine(String line, LogOrigin isScriptOutput, Artifacts artifacts, ProjectBridge bridge, boolean isCpp){
        List<String> list = DiscoveryUtils.scanCommandLine(line, isScriptOutput);
        boolean hasQuotes = false;
        for(String s : list){
            if (s.startsWith("\"")){  //NOI18N
                hasQuotes = true;
                break;
            }
        }
        if (hasQuotes) {
            List<String> newList = new ArrayList<>();
            for(int i = 0; i < list.size();) {
                String s = list.get(i); 
                if (s.startsWith("-D") && s.endsWith("=") && i+1 < list.size() && list.get(i+1).startsWith("\"")){ // NOI18N
                    String longString = null;
                    for(int j = i+1; j < list.size() && list.get(j).startsWith("\""); j++){  //NOI18N
                        if (longString != null) {
                            longString += " " + list.get(j);  //NOI18N
                        } else {
                            longString = list.get(j);
                        }
                        i = j;
                    }
                    newList.add(s+"`"+longString+"`");  //NOI18N
                } else {
                    newList.add(s);
                }
                i++;
            }
            list = newList;
        }
        ListIterator<String> st = list.listIterator();
        if (st.hasNext()) {
            String option = st.next();
            if (option.equals("+") && st.hasNext()) { // NOI18N
                st.next();
            }
        }
        return gatherCompilerLine(st, isScriptOutput, artifacts, bridge, isCpp);
    }
    /**
     * parse compile line
     */
    public static List<String> gatherCompilerLine(ListIterator<String> st, LogOrigin isScriptOutput, Artifacts artifacts, ProjectBridge bridge, boolean isCpp){
        boolean TRACE = false;
        String option; 
        List<String> what = new ArrayList<>(1);
        List<String> importantCandidates = new ArrayList<>();
        while(st.hasNext()){
            option = st.next();
            boolean isQuote = false;
            if (isScriptOutput == LogOrigin.BuildLog) {
                if (option.startsWith("'") && option.endsWith("'") || // NOI18N
                    option.startsWith("\"") && option.endsWith("\"")){ // NOI18N
                    if (option.length() >= 2) {
                        option = option.substring(1,option.length()-1);
                        isQuote = true;
                    }
                }
            }
            if (option.startsWith("--")) { // NOI18N
                option = option.substring(1);
            }
            if (option.startsWith("-D")){ // NOI18N
                String macro;
                if (option.equals("-D") && st.hasNext()){  //NOI18N
                    macro = st.next();
                } else {
                    macro = option.substring(2);
                }
                macro = removeQuotes(macro);
                int i = macro.indexOf('=');
                if (i>0){
                    String value = macro.substring(i+1).trim();
                    switch (isScriptOutput) {
                        case BuildLog:
                            if (value.length() >= 2 && value.charAt(0) == '`' && value.charAt(value.length()-1) == '`'){ // NOI18N
                                value = value.substring(1,value.length()-1);  // NOI18N
                            }
                            if (value.length() >= 6 &&
                                (value.charAt(0) == '"' && value.charAt(1) == '\\' && value.charAt(2) == '"' &&  // NOI18N
                                value.charAt(value.length()-3) == '\\' && value.charAt(value.length()-2) == '"' && value.charAt(value.length()-1) == '"')) { // NOI18N
                                // What is it?
                                value = value.substring(2,value.length()-3)+"\"";  // NOI18N
                            } else if (value.length() >= 4 &&
                                (value.charAt(0) == '\\' && value.charAt(1) == '"' &&  // NOI18N
                                value.charAt(value.length()-2) == '\\' && value.charAt(value.length()-1) == '"' )) { // NOI18N
                                value = value.substring(1,value.length()-2)+"\"";  // NOI18N
                            } else if (value.length() >= 4 &&
                                (value.charAt(0) == '\\' && value.charAt(1) == '\'' &&  // NOI18N
                                value.charAt(value.length()-2) == '\\' && value.charAt(value.length()-1) == '\'' )) { // NOI18N
                                value = value.substring(1,value.length()-2)+"'";  // NOI18N
                            } else if (!isQuote && value.length() >= 2 &&
                               (value.charAt(0) == '\'' && value.charAt(value.length()-1) == '\'' || // NOI18N
                                value.charAt(0) == '"' && value.charAt(value.length()-1) == '"' )) { // NOI18N
                                value = value.substring(1,value.length()-1);
                            }
                            break;
                        case DwarfCompileLine:
                            if (value.length() >= 2 &&
                               (value.charAt(0) == '\'' && value.charAt(value.length()-1) == '\'' || // NOI18N
                                value.charAt(0) == '"' && value.charAt(value.length()-1) == '"' )) { // NOI18N
                                value = DiscoveryUtils.removeEscape(value.substring(1,value.length()-1));
                            }
                            break;
                        case ExecLog:
                            // do nothing
                            break;
                    }
                    String key = removeEscape(macro.substring(0,i));
                    addDef(key, value, artifacts.userMacros, artifacts.undefinedMacros);
                } else {
                    String key = removeEscape(macro);
                    addDef(key, null, artifacts.userMacros, artifacts.undefinedMacros);
                }
            } else if (option.startsWith("-U")){ // NOI18N
                String macro = option.substring(2);
                if (macro.length()==0 && st.hasNext()){
                    macro = st.next();
                }
                macro = removeQuotes(macro);
                addUndef(macro, artifacts.userMacros, artifacts.undefinedMacros);
            } else if (option.startsWith("-I")){ // NOI18N
                String path = option.substring(2);
                if (path.length()==0 && st.hasNext()){
                    path = st.next();
                }
                path = removeQuotes(path);
                artifacts.userIncludes.add(path);
            } else if (option.startsWith("-isystem")){ // NOI18N
                String path = option.substring(8);
                if (path.length()==0 && st.hasNext()){
                    path = st.next();
                }
                path = removeQuotes(path);
                artifacts.userIncludes.add(path);
            } else if (option.startsWith("-include")){ // NOI18N
                String path = option.substring(8);
                if (path.length()==0 && st.hasNext()){
                    path = st.next();
                }
                path = removeQuotes(path);
                artifacts.userFiles.add(path);
            } else if (option.startsWith("-imacros")){ // NOI18N
                String path = option.substring(8);
                if (path.length()==0 && st.hasNext()){
                    path = st.next();
                }
                path = removeQuotes(path);
                artifacts.userFiles.add(path);
            } else if (option.startsWith("-Y")){ // NOI18N
                String defaultSearchPath = option.substring(2);
                if (defaultSearchPath.length()==0 && st.hasNext()){
                    defaultSearchPath = st.next();
                }
                if (defaultSearchPath.startsWith("I,")){ // NOI18N
                    defaultSearchPath = defaultSearchPath.substring(2);
                    defaultSearchPath = removeQuotes(defaultSearchPath);
                    artifacts.userIncludes.add(defaultSearchPath);
                }
            } else if (option.startsWith("-idirafter")){ // NOI18N
                //Search dir for header files, but do it after all directories specified with -I and the standard system directories have been exhausted.
                if (option.equals("-idirafter") && st.hasNext()) { // NOI18N
                    st.next();
                }
            } else if (option.startsWith("-iprefix")){ // NOI18N
                //Specify prefix as the prefix for subsequent -iwithprefix options.
                if (option.equals("-iprefix") && st.hasNext()) { // NOI18N
                    st.next();
                }
            } else if (option.startsWith("-iwithprefix")){ // NOI18N
                //Append dir to the prefix specified previously with -iprefix, and add the resulting directory to the include search path.
                if (option.equals("-iwithprefix") && st.hasNext()) { // NOI18N
                    st.next();
                }
            } else if (option.startsWith("-iwithprefixbefore")){ // NOI18N
                //Append dir to the prefix specified previously with -iprefix, and add the resulting directory to the include search path.
                if (option.equals("-iwithprefixbefore") && st.hasNext()) { // NOI18N
                    st.next();
                }
            } else if (option.startsWith("-isysroot")){ // NOI18N
                //This option is like the --sysroot option, but applies only to header files.
                if (option.equals("-isysroot") && st.hasNext()) { // NOI18N
                    st.next();
                }
            } else if (option.startsWith("-iquote")){ // NOI18N
                //Search dir only for header files requested with "#include " file ""
                if (option.equals("-iquote") && st.hasNext()) { // NOI18N
                    st.next();
                }
            } else if (option.equals("-K")){ // NOI18N
                // Skip pic
                if (st.hasNext()){
                    String next = st.next();
                    if (next.equals("PIC") || next.equals("pic")) { // NOI18N
                        // options = "-K"+next;
                        importantCandidates.add(option+next);
                    } else {
                        st.previous();
                    }
                }
            } else if (option.equals("-R")){ // NOI18N
                // Skip runtime search path 
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.startsWith("-l")){ // NOI18N
                String lib = option.substring(2);
                if (lib.length() == 0 && st.hasNext()){
                    lib = st.next();
                }
                // library
                if (lib.length()>0){
                    artifacts.libraries.add(lib);
                }
            } else if (option.equals("-L")){ // NOI18N
                // Skip library search path
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-M")){ // NOI18N
                // Skip library search path
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-h")){ // NOI18N
                // Skip generated dynamic shared library
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-o")){ // NOI18N
                // Skip result
                if (st.hasNext()){
                    artifacts.output = st.next();
                }
            // generation 2 of params
            } else if (option.equals("-z")){ // NOI18N
                // ld params of gcc
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-x")){ // NOI18N
                // Specify explicitly the language for the following input files (rather than letting the compiler choose a default based on the file name suffix).
                if (st.hasNext()){
                    String lang = st.next();
                    artifacts.languageArtifacts.add(lang);
                    if (lang.equals("c")) {// NOI18N
                        isCpp = false;
                    } else if (lang.equals("c++")) {// NOI18N
                        isCpp = true;
                    } 
                    importantCandidates.add(option+lang);
                }
            } else if (option.equals("-xc")){ // NOI18N
                artifacts.languageArtifacts.add("c"); // NOI18N	
                isCpp = false;
                importantCandidates.add(option);
            } else if (option.equals("-xc++")){ // NOI18N
                artifacts.languageArtifacts.add("c++"); // NOI18N
                isCpp = true;
                importantCandidates.add(option);
            } else if (C89.contains(option)){
                artifacts.languageArtifacts.add("c89"); // NOI18N
                isCpp = false;
                importantCandidates.add(option);
            } else if (C99.contains(option)){
                artifacts.languageArtifacts.add("c99"); // NOI18N
                isCpp = false;
                importantCandidates.add(option);
            } else if (C11.contains(option)){
                artifacts.languageArtifacts.add("c11"); // NOI18N
                isCpp = false;
                importantCandidates.add(option);
            } else if (CPP11.contains(option)){
                artifacts.languageArtifacts.add("c++11"); // NOI18N
                isCpp = true;
                importantCandidates.add(option);
            } else if (CPP14.contains(option)){
                artifacts.languageArtifacts.add("c++14"); // NOI18N
                isCpp = true;
                importantCandidates.add(option);
            } else if (CPP98.contains(option)){
                artifacts.languageArtifacts.add("c++98"); // NOI18N
                isCpp = true;
                importantCandidates.add(option);
            } else if (option.equals("-xMF")){ // NOI18N
                // ignore dependency output file
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-MF")){ // NOI18N
                // ignore dependency output file
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-MT")){ // NOI18N
                // once more fancy preprocessor option with parameter. Ignore.
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-MQ")){ // NOI18N
                // once more fancy preprocessor option with parameter. Ignore.
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-aux-info")){ // NOI18N
                // Output to the given filename prototyped declarations for all functions declared and/or defined in a translation unit, including those in header files. Ignore.
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.startsWith("-")){ // NOI18N
                importantCandidates.add(option);
            } else if (option.startsWith("ccfe")){ // NOI18N
                // Skip option
            } else if (option.startsWith(">")){ // NOI18N
                // Skip redurect
                break;
            } else {
                if (SourcesVisibilityQuery.getDefault().isVisible(option)) {
                    what.add(option);
                }
            }
        }
        if (bridge != null) {
            for(String candidate : importantCandidates) {
                if (bridge.isImportantFlag(candidate, isCpp)) {
                    artifacts.importantFlags.add(candidate);
                }
            }
        }
        return what;
    }

    private static void addDef(String macro, String value, Map<String, String> userMacros, List<String> undefinedMacros) {
        undefinedMacros.remove(macro);
        userMacros.put(macro, value);
    }

    private static void addUndef(String macro, Map<String, String> userMacros, List<String> undefinedMacros) {
        if (userMacros.containsKey(macro)) {
            userMacros.remove(macro);
        } else {
            if (!undefinedMacros.contains(macro)) {
                undefinedMacros.add(macro);
            }
        }
    }
    
    public static String removeQuotes(String path) {
        if (path.length() >= 2 && (path.charAt(0) == '\'' && path.charAt(path.length() - 1) == '\'' || // NOI18N
            path.charAt(0) == '"' && path.charAt(path.length() - 1) == '"')) {// NOI18N

            path = path.substring(1, path.length() - 1); // NOI18N
        }
        return path;
    }

    // reverse of the CndPathUtilities.escapeOddCharacters(String s)
    public static String removeEscape(String s) {
        int n = s.length();
        StringBuilder ret = new StringBuilder(n);
        char prev = 0;
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if ((c == ' ') || (c == '\t') || // NOI18N
                    (c == ':') || (c == '\'') || // NOI18N
                    (c == '*') || (c == '\"') || // NOI18N
                    (c == '[') || (c == ']') || // NOI18N
                    (c == '(') || (c == ')') || // NOI18N
                    (c == ';')) { // NOI18N
                if (prev == '\\') { // NOI18N
                    ret.setLength(ret.length()-1);
                }
            }
            ret.append(c);
            prev = c;
        }
        return ret.toString();
    }
                    
                    
    public enum LogOrigin {
        BuildLog,
        DwarfCompileLine,
        ExecLog
    }
    
    public static final class Artifacts {
        public final List<String> userIncludes = new ArrayList<>();
        public final List<String> userFiles = new ArrayList<>();
        public final Map<String, String> userMacros = new HashMap<>();
        public final List<String> undefinedMacros = new ArrayList<>();
        public final Set<String> libraries = new HashSet<>();
        public final List<String> languageArtifacts = new ArrayList<>();
        public final List<String> importantFlags = new ArrayList<>();
        public String output;
        public Artifacts() {
        }
        public String getImportantFlags() {
            StringBuilder buf = new StringBuilder();
            for(String flag : importantFlags) {
                if (buf.length() > 0) {
                    buf.append(' ');
                }
                buf.append(flag);
            }
            return buf.toString();
        }
        public ItemProperties.LanguageStandard getLanguageStandard(ItemProperties.LanguageStandard standard) {
            for(String lang : languageArtifacts) {
                if ("c89".equals(lang)) { //NOI18N
                    standard = ItemProperties.LanguageStandard.C89;
                } else if ("c99".equals(lang)) { //NOI18N
                    standard = ItemProperties.LanguageStandard.C99;
                } else if ("c11".equals(lang)) { //NOI18N
                    standard = ItemProperties.LanguageStandard.C11;
                } else if ("c++98".equals(lang)) { //NOI18N
                    standard = ItemProperties.LanguageStandard.CPP;
                } else if ("c++11".equals(lang)) { //NOI18N
                    standard = ItemProperties.LanguageStandard.CPP11;
                } else if ("c++14".equals(lang)) { //NOI18N
                    standard = ItemProperties.LanguageStandard.CPP14;
                } 
            }
            return standard;
        }
    }
}
