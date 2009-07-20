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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.execution.LinkSupport;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.BaseDwarfProvider.CompilerSettings;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.BaseDwarfProvider.GrepEntry;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class DwarfSource implements SourceFileProperties{
    private static final boolean FULL_TRACE = Boolean.getBoolean("cnd.dwarfdiscovery.trace.read.source"); // NOI18N
    private static final boolean CUT_LOCALHOST_NET_ADRESS = Boolean.getBoolean("cnd.dwarfdiscovery.cut.localhost.net.adress"); // NOI18N
    private static boolean ourGatherMacros = true;
    private static boolean ourGatherIncludes = true;
    private static final String CYG_DRIVE_UNIX = "/cygdrive/"; // NOI18N
    private static final String CYG_DRIVE_WIN = "\\cygdrive\\"; // NOI18N
    private static final String CYGWIN_PATH = ":/cygwin"; // NOI18N
    private String cygwinPath;
    
    private String compilePath;
    private String sourceName;
    private String fullName;
    private ItemProperties.LanguageKind language;
    private List<String> userIncludes;
    private List<String> systemIncludes;
    private boolean haveSystemIncludes;
    private Map<String, String> userMacros;
    private Map<String, String> systemMacros;
    private boolean haveSystemMacros;
    private Set<String> includedFiles;
    private CompilerSettings normilizeProvider;
    private Map<String,GrepEntry> grepBase;
    
    DwarfSource(CompilationUnit cu, boolean isCPP, CompilerSettings compilerSettings, Map<String,GrepEntry> grepBase) throws IOException{
        initCompilerSettings(compilerSettings, isCPP);
        this.grepBase = grepBase;
        initSourceSettings(cu, isCPP);
    }

    private void countFileName(CompilationUnit cu) throws IOException {
        fullName = cu.getSourceFileAbsolutePath();
        fullName = fixFileName(fullName);
        File file = new File(fullName);
        fullName = CndFileUtils.normalizeAbsolutePath(file.getAbsolutePath());
        fullName = linkSupport(fullName);
        if (fullName != null && Utilities.isWindows()) {
            fullName = fullName.replace('/', '\\');
        }
        fullName = PathCache.getString(fullName);
        if (FULL_TRACE) {System.out.println("Compilation unit full name:"+fullName);} // NOI18N
    }

    private void initCompilerSettings(CompilerSettings compilerSettings, boolean isCPP){
        List<String> list = compilerSettings.getSystemIncludePaths(isCPP);
       if (list != null){
           systemIncludes = new ArrayList<String>(list);
           //if (FULL_TRACE) {
           //    System.out.println("System Include Paths:"); // NOI18N
           //    for (String s : list) {
           //        System.out.println("\t"+s); // NOI18N
           //    }
           //}
           if (Utilities.isWindows()) {
               if (FULL_TRACE) {System.out.println("CompileFlavor:"+compilerSettings.getCompileFlavor());} // NOI18N
               if ("Cygwin".equals(compilerSettings.getCompileFlavor())) { // NOI18N
                   cygwinPath = compilerSettings.getCygwinDrive();
                   if (cygwinPath == null) {
                       for(String path:list){
                           int i = path.toLowerCase().indexOf(CYGWIN_PATH);
                           if (i > 0) {
                               if (cygwinPath == null) {
                                   cygwinPath = "" + Character.toUpperCase(path.charAt(0)) + CYGWIN_PATH; // NOI18N
                                   for(i = i + CYGWIN_PATH.length();i < path.length();i++){
                                       char c = path.charAt(i);
                                       if (c == '\\'){
                                           break;
                                       }
                                       cygwinPath+=""+c;
                                   }
                                   if (FULL_TRACE) {System.out.println("Detect cygwinPath:"+cygwinPath);} // NOI18N
                                   break;
                               }
                           }
                       }
                   }
               }
            }
        } else {
            systemIncludes = new ArrayList<String>();
        }
        haveSystemIncludes = systemIncludes.size() > 0;
        Map<String, String> map = compilerSettings.getSystemMacroDefinitions(isCPP);
        if (map != null){
            systemMacros = new HashMap<String,String>(map);
        } else {
            systemMacros = new HashMap<String,String>();
        }
        haveSystemMacros = systemMacros.size() > 0;
        normilizeProvider = compilerSettings;
    }
    
    public String getCompilePath() {
        return compilePath;
    }
    
    public String getItemPath() {
        return fullName;
    }
    
    public String getItemName() {
        return sourceName;
    }
    
    public List<String> getUserInludePaths() {
        return userIncludes;
    }
    
    public List<String> getSystemInludePaths() {
        return systemIncludes;
    }
    
    public Set<String> getIncludedFiles() {
        return includedFiles;
    }
    
    public Map<String, String> getUserMacros() {
        return userMacros;
    }
    
    public Map<String, String> getSystemMacros() {
        return systemMacros;
    }
    
    public ItemProperties.LanguageKind getLanguageKind() {
        return language;
    }
    
    private String fixFileName(String fileName) {
        if (fileName == null){
            return fileName;
        }
        if (Utilities.isWindows()) {
            //replace /cygdrive/<something> prefix with <something>:/ prefix:
            if (FULL_TRACE) {System.out.println("Try to fix win name:"+fileName);} // NOI18N
            if (fileName.startsWith(CYG_DRIVE_UNIX)) {
                fileName = fileName.substring(CYG_DRIVE_UNIX.length()); // NOI18N
                fileName = "" + Character.toUpperCase(fileName.charAt(0)) + ':' + fileName.substring(1); // NOI18N
                fileName = fileName.replace('\\', '/');
                if (cygwinPath == null) {
                    cygwinPath = "" + Character.toUpperCase(fileName.charAt(0)) + CYGWIN_PATH;
                    if (FULL_TRACE) {System.out.println("Set cygwinPath:"+cygwinPath);} // NOI18N
                }
            } else {
                int i = fileName.indexOf(CYG_DRIVE_WIN);
                if (i > 0) {
                    //replace D:\cygdrive\c\<something> prefix with <something>:\ prefix:
                    if (cygwinPath == null) {
                        cygwinPath = "" + Character.toUpperCase(fileName.charAt(0)) + CYGWIN_PATH; // NOI18N
                        if (FULL_TRACE) {System.out.println("Set cygwinPath:"+cygwinPath);} // NOI18N
                    }
                    fileName = fileName.substring(i+CYG_DRIVE_UNIX.length());
                    fileName = "" + Character.toUpperCase(fileName.charAt(0)) + ':' + fileName.substring(1); // NOI18N
                    fileName = fileName.replace('\\', '/');
                }
            }
            if (FULL_TRACE) {System.out.println("\t"+fileName);} // NOI18N
        } else if (CUT_LOCALHOST_NET_ADRESS && Utilities.isUnix()) {
            if (fileName.startsWith("/net/")){ // NOI18N
                try {
                    InetAddress addr = InetAddress.getLocalHost();
                    String host = addr.getHostName();
                    if (host != null && host.length()>0) {
                        String u = "/net/"+host+"/"; // NOI18N
                        if (fileName.startsWith(u)){
                            fileName = fileName.substring(u.length()-1);
                        }
                    }
                } catch (UnknownHostException ex) {
                }
            }
        }
        return fileName;
    }
    
    private String linkSupport(String name){
        if (Utilities.isWindows()) {
            if (!new File(name).exists()){
                String link = name+".lnk"; // NOI18N
                if (new File(link).exists()){
                    String resolve = LinkSupport.getOriginalFile(link);
                    if (resolve != null){
                        name = resolve;
                    }
                } else {
                    StringTokenizer st = new StringTokenizer(name,"\\/"); // NOI18N
                    StringBuilder buf = new StringBuilder();
                    while(st.hasMoreTokens()){
                        String token = st.nextToken();
                        if (buf.length()>0){
                            buf.append('\\');
                        }
                        buf.append(token);
                        if (token.length()>0 && token.charAt(token.length()-1) != ':'){
                            String path = buf.toString();
                            if (!new File(path).exists()){
                                link = path+".lnk"; // NOI18N
                                if (new File(link).exists()){
                                    String resolve = LinkSupport.getOriginalFile(link);
                                    if (resolve != null){
                                        buf = new StringBuilder(resolve);
                                    } else {
                                        return name;
                                    }
                                } else {
                                    return name;
                                }
                            }
                        }
                    }
                    name = buf.toString();
                }
            }
        }
        return name;
    }
    
    
    private void initSourceSettings(CompilationUnit cu, boolean isCPP) throws IOException{
        userIncludes = new ArrayList<String>();
        userMacros = new HashMap<String,String>();
        includedFiles = new HashSet<String>();
        countFileName(cu);
        compilePath = PathCache.getString(fixFileName(cu.getCompilationDir()));
        sourceName = PathCache.getString(cu.getSourceFileName());
        
        if (compilePath == null && sourceName.lastIndexOf('/')>0) {
            int i = sourceName.lastIndexOf('/');
            compilePath = sourceName.substring(0,i);
            sourceName = sourceName.substring(i+1);
        } else {
            if (sourceName.startsWith("/")) { // NOI18N
                sourceName = DiscoveryUtils.getRelativePath(compilePath, sourceName);
            }
            if (compilePath == null) {
                if (fullName != null && fullName.lastIndexOf('/')>0) {
                    int i = fullName.lastIndexOf('/');
                    compilePath = fullName.substring(0,i);
                } else {
                    compilePath = ""; // NOI18N
                }
            }
        }
        if (isCPP) {
            language = ItemProperties.LanguageKind.CPP;
        } else {
            language = ItemProperties.LanguageKind.C;
        }
    }
    
    public void process(CompilationUnit cu) throws IOException{
        String line = cu.getCommandLine();
        if (line != null && line.length()>0){
            gatherLine(line);
            gatherIncludedFiles(cu);
        } else {
            gatherMacros(cu);
            gatherIncludes(cu);
        }
    }
    
    private void addUserIncludePath(String path){
        if (!userIncludes.contains(path)) {
            userIncludes.add(path);
        }
    }

    private void gatherLine(String line) {
        // /set/c++/bin/5.9/intel-S2/prod/bin/CC -c -g -DHELLO=75 -Idist  main.cc -Qoption ccfe -prefix -Qoption ccfe .XAKABILBpivFlIc.
        if (FULL_TRACE) {System.out.println("Process command line "+line);} // NOI18N
        Iterator<String> st = DiscoveryUtils.scanCommandLine(line).iterator();
        while(st.hasNext()){
            String option = st.next();
            if (option.startsWith("-D")){ // NOI18N
                String macro = option.substring(2);
                int i = macro.indexOf('=');
                if (i>0){
                    String value = macro.substring(i+1).trim();
                    if (value.length() >= 2 &&
                       (value.charAt(0) == '\'' && value.charAt(value.length()-1) == '\'' || // NOI18N
                        value.charAt(0) == '"' && value.charAt(value.length()-1) == '"' )) { // NOI18N
                        value = value.substring(1,value.length()-1);
                    }
                    userMacros.put(PathCache.getString(macro.substring(0,i)), PathCache.getString(value));
                } else {
                    userMacros.put(PathCache.getString(macro), null);
                }
            } else if (option.startsWith("-I")){ // NOI18N
                String path = option.substring(2);
                if (path.length()==0 && st.hasNext()){
                    path = st.next();
                }
                String include = PathCache.getString(path);
                addUserIncludePath(include);
            } else if (option.startsWith("-Y")){ // NOI18N
                String defaultSearchPath = option.substring(2);
                if (defaultSearchPath.length()==0 && st.hasNext()){
                    defaultSearchPath = st.next();
                }
                if (defaultSearchPath.startsWith("I,")){ // NOI18N
                    defaultSearchPath = defaultSearchPath.substring(2);
                    String include = PathCache.getString(defaultSearchPath);
                    addUserIncludePath(include);
                }
            } else if (option.equals("-fopenmp")){ // NOI18N
                userMacros.put("_OPENMP", "200505"); // NOI18N
            } else if (option.equals("-xopenmp") || option.equals("-xopenmp=parallel") || option.equals("-xopenmp=noopt")){ // NOI18N
                userMacros.put("_OPENMP", null); // NOI18N
            }
        }
    }
    
    private String fixCygwinPath(String path){
        if (cygwinPath != null) {
            if (path.startsWith("/usr/lib/")){// NOI18N
                path = cygwinPath+path.substring(4);
            } else if (path.startsWith("/usr")) { // NOI18N
                path = cygwinPath+path;
            }
        }
        if (path.startsWith(CYG_DRIVE_UNIX)){
            path = fixFileName(path);
        }
        if (Utilities.isWindows()) {
            path = path.replace('\\', '/');
        }
        return path;
    }
    
    private boolean isSystemPath(String path){
        path = fixCygwinPath(path);
        path = normalizePath(path);
        if (path.startsWith("/") || // NOI18N
                path.length()>2 && path.charAt(1)==':'){
            HashSet<String> bits = new HashSet<String>();
            for (String cp : systemIncludes){
                if (path.equals(cp)) {
                    return true;
                }
                for(String sub : grepSystemFolder(cp).includes){
                    bits.add(sub);
                }
            }
            for (String cp : systemIncludes){
                for(String sub : bits) {
                    if (path.startsWith(cp)) {
                        if (path.startsWith(cp+sub)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private void addpath(String path){
        if (haveSystemIncludes) {
            if (!isSystemPath(path)){
                path = fixCygwinPath(path);
                path = normalizePath(path);
                addUserIncludePath(PathCache.getString(path));
                if (FULL_TRACE) {System.out.println("\tuser:"+path);} // NOI18N
            }
        } else {
            if (path.startsWith("/usr")) { // NOI18N
                path = fixCygwinPath(path);
                path = normalizePath(path);
                path = PathCache.getString(path);
                if (!systemIncludes.contains(path)) {
                    systemIncludes.add(path);
                }
                if (FULL_TRACE) {System.out.println("\tsystem:"+path);} // NOI18N
            } else {
                path = fixCygwinPath(path);
                path = normalizePath(path);
                addUserIncludePath(PathCache.getString(path));
                if (FULL_TRACE) {System.out.println("\tuser:"+path);} // NOI18N
            }
        }
    }
    
    private String normalizePath(String path){
        if (path.startsWith("/") || // NOI18N
                path.length()>2 && path.charAt(1)==':') {
            return normilizeProvider.getNormalizedPath(path);
        }
        return path;
    }
    
    
    private void gatherIncludes(final CompilationUnit cu) throws IOException {
        if (!ourGatherIncludes) {
            return;
        }
        DwarfStatementList dwarfTable = cu.getStatementList();
        if (dwarfTable == null) {
            if (FULL_TRACE) {System.out.println("Include paths not found");} // NOI18N
            return;
        }
        for (Iterator<String> it = dwarfTable.getIncludeDirectories().iterator(); it.hasNext();) {
            addpath(it.next());
        }
        List<String> list = grepSourceFile(fullName).includes;
        for(String path : list){
            cutFolderPrefix(path, dwarfTable);
        }
        ArrayList<String> dwarfIncludedFiles = dwarfTable.getFilePaths();
        for(String path : dwarfIncludedFiles){
            String includeFullName = path;
            if (FULL_TRACE) {System.out.println("Included file original:"+path);} // NOI18N
            if (path.startsWith("./")) { // NOI18N
                includeFullName = compilePath+path.substring(1);
            } else if (path.startsWith("../")) { // NOI18N
                includeFullName = compilePath+File.separator+path;
            } else if (!path.startsWith("/")){ // NOI18N
                includeFullName = compilePath+File.separator+path;
            } else {
                includeFullName = fixCygwinPath(path);
                includeFullName = normalizePath(includeFullName);
            }
            if (Utilities.isWindows()) {
                includeFullName = includeFullName.replace('\\', '/');
            }
            if (!isSystemPath(includeFullName)) {
                list = grepSourceFile(includeFullName).includes;
                for(String included : list){
                    cutFolderPrefix(included, dwarfTable);
                }
                int i = includeFullName.lastIndexOf('/');
                if (i > 0) {
                    String userPath = includeFullName.substring(0,i);
                    addpath(userPath);
                }
            }
            includedFiles.add(PathCache.getString(includeFullName));
            if (FULL_TRACE) {System.out.println("Included file:"+includeFullName);} // NOI18N
        }
        if (FULL_TRACE) {System.out.println("Include paths:"+userIncludes);} // NOI18N
    }

    private void cutFolderPrefix(String path, final DwarfStatementList dwarfTable) {
        if (Utilities.isWindows()) {
            path = path.replace('\\', '/'); // NOI18N
        }
        if (path.indexOf('/')>0){ // NOI18N
            int n = path.lastIndexOf('/'); // NOI18N
            String name = path.substring(n+1);
            String relativeDir = path.substring(0,n);
            String dir = "/"+relativeDir; // NOI18N
            ArrayList<String> paths = dwarfTable.getPathsForFile(name);
            if (FULL_TRACE) {System.out.println("Try to find new include paths for:"+name+" in folder "+dir);} // NOI18N
            for(String dwarfPath : paths){
                if (FULL_TRACE) {System.out.println("    candidate:"+dwarfPath);} // NOI18N
                if (dwarfPath.endsWith(dir)){
                    String found = dwarfPath.substring(0,dwarfPath.length()-dir.length());
                    found = fixCygwinPath(found);
                    found = normalizePath(found);
                    if (!userIncludes.contains(found)) {
                        if (haveSystemIncludes) {
                            boolean system = false;
                            if (found.startsWith("/") || // NOI18N
                                    found.length()>2 && found.charAt(1)==':'){
                                system = systemIncludes.contains(found);
                            }
                            if (!system){
                               if (FULL_TRACE) {System.out.println("    Find new include path:"+found);} // NOI18N
                                addUserIncludePath(PathCache.getString(found));
                            }
                        } else {
                            if (!dwarfPath.startsWith("/usr")){ // NOI18N
                                if (FULL_TRACE) {System.out.println("    Find new include path:"+found);} // NOI18N
                                addUserIncludePath(PathCache.getString(found));
                            }
                        }
                    }
                    break;
                } else if (dwarfPath.equals(relativeDir)){
                    String found = "."; // NOI18N
                    if (!userIncludes.contains(found)) {
                        if (FULL_TRACE) {System.out.println("    Find new include path:"+found);} // NOI18N
                        addUserIncludePath(PathCache.getString(found));
                    }
                    break;
                }
            }
        }
    }
    
    private void gatherIncludedFiles(final CompilationUnit cu) throws IOException {
        if (!ourGatherIncludes) {
            return;
        }
        DwarfStatementList dwarfTable = cu.getStatementList();
        if (dwarfTable == null) {
            return;
        }
        for(String path :dwarfTable.getFilePaths()){
            String includeFullName = path;
            if (path.startsWith("./")) { // NOI18N
                includeFullName = compilePath+path.substring(1);
            } else if (path.startsWith("../")) { // NOI18N
                includeFullName = compilePath+File.separator+path;
            }
            includeFullName = normalizePath(includeFullName);
            includedFiles.add(PathCache.getString(includeFullName));
        }
    }
    
    private void gatherMacros(final CompilationUnit cu) throws IOException {
        if (!ourGatherMacros){
            return;
        }
        DwarfMacinfoTable dwarfTable = cu.getMacrosTable();
        if (dwarfTable == null) {
            if (FULL_TRACE) {System.out.println("Macros not found");} // NOI18N
            return;
        }
        int firstMacroLine = grepSourceFile(fullName).firstMacroLine;
        ArrayList<DwarfMacinfoEntry> table = dwarfTable.getCommandLineMarcos();
        for (Iterator<DwarfMacinfoEntry> it = table.iterator(); it.hasNext();) {
            DwarfMacinfoEntry entry = it.next();
            String def = entry.definition;
            int i = def.indexOf(' ');
            String macro;
            String value = null;
            if (i>0){
                macro = PathCache.getString(def.substring(0,i));
                value = PathCache.getString(def.substring(i+1).trim());
            } else {
                macro = PathCache.getString(def);
            }
            if (firstMacroLine == entry.lineNum) {
                if (macro.equals(grepSourceFile(fullName).firstMacro)){
                    break;
                }
            }
            if (haveSystemMacros && systemMacros.containsKey(macro)){
                String sysValue = systemMacros.get(macro);
                if (equalValues(sysValue, value)) {
                    continue;
                }
            }
            userMacros.put(macro,value);
        }
        if (FULL_TRACE) {System.out.println("Macros:"+userMacros);} // NOI18N
    }

    private boolean equalValues(String sysValue, String value) {
        // filter out system macros
        // For example gcc windows dwarf contains following system macros as user:
        // unix=1 __unix=1 __unix__=1 __CYGWIN__=1 __CYGWIN32__=1
        if (value == null || "1".equals(value)) { // NOI18N
            return sysValue == null || "1".equals(sysValue); // NOI18N
        }
        return value.equals(sysValue); // NOI18N
    }
  
    private GrepEntry grepSystemFolder(String path) {
        GrepEntry res = grepBase.get(path);
        if (res != null) {
            return res;
        }
        res = new GrepEntry();
        File folder = new File(path);
        if (folder.exists() && folder.canRead() && folder.isDirectory()) {
            for(File f: folder.listFiles()){
                if (f.exists() && f.canRead() && !f.isDirectory()){
                    List<String> l = grepSourceFile(f.getAbsolutePath()).includes;
                    for (String i : l){
                        if (i.indexOf("..")>0 || i.startsWith("/") || i.indexOf(":")>0) { // NOI18N
                            continue;
                        }
                        if (i.indexOf('/')>0){ // NOI18N
                            int n = i.lastIndexOf('/'); // NOI18N
                            String relativeDir = i.substring(0,n);
                            String dir = "/"+relativeDir; // NOI18N
                            if (!res.includes.contains(dir)){
                                res.includes.add(dir);
                            }
                        }
                    }
                }
            }
        }
        List<String> secondLevel = new ArrayList<String>();
        for(String sub : res.includes) {
            File subFolder = new File(path+sub);
            try {
                if (subFolder.getCanonicalFile().getAbsolutePath().startsWith(path + sub)) {
                    for (String s : grepSystemFolder(path + sub).includes) {
                        secondLevel.add(s);
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        for(String s: secondLevel){
            if (!res.includes.contains(s)){
                res.includes.add(s);
            }
        }
        grepBase.put(path, res);
        return res;
    }
    
    private GrepEntry grepSourceFile(String fileName){
        GrepEntry res = grepBase.get(fileName);
        if (res != null) {
            return res;
        }
        res = new GrepEntry();
        File file = new File(fileName);
        if (file.exists() && file.canRead() && !file.isDirectory()){
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));
                int lineNo = 0;
                while(true){
                    String line = in.readLine();
                    lineNo++;
                    if (line == null){
                        break;
                    }
                    line = line.trim();
                    if (!line.startsWith("#")){ // NOI18N
                        continue;
                    }
                    line = line.substring(1).trim();
                    if (line.startsWith("include")){ // NOI18N
                        line = line.substring(7).trim();
                        if (line.length()>2) {
                            if (line.startsWith("/*")) { // NOI18N
                                int i = line.indexOf("*/"); // NOI18N
                                if (i > 0) {
                                    line = line.substring(i+2).trim();
                                }
                            }
                            char c = line.charAt(0);
                            if (c == '"') {
                                if (line.indexOf('"',1)>0){
                                    res.includes.add(line.substring(1,line.indexOf('"',1)));
                                    if (FULL_TRACE) {System.out.println("find in source:"+line.substring(1,line.indexOf('"',1)));} // NOI18N
                                }
                            } else if (c == '<'){
                                if (line.indexOf('>')>0){
                                    res.includes.add(line.substring(1,line.indexOf('>')));
                                    if (FULL_TRACE) {System.out.println("find in source:"+line.substring(1,line.indexOf('>')));} // NOI18N
                                }
                            }
                        }
                    } else if (line.startsWith("define")){ // NOI18N
                        if (res.firstMacroLine == -1) {
                            line = line.substring(6).trim();
                            if (line.length()>0) {
                                if (line.startsWith("/*")) { // NOI18N
                                    int i = line.indexOf("*/"); // NOI18N
                                    if (i > 0) {
                                        line = line.substring(i+2).trim();
                                    }
                                }
                                StringTokenizer st = new StringTokenizer(line,"\t ("); // NOI18N
                                while(st.hasMoreTokens()) {
                                    res.firstMacroLine = lineNo;
                                    res.firstMacro = st.nextToken();
                                    break;
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            if (FULL_TRACE) {System.out.println("Cannot grep file:"+fileName);} // NOI18N
        }
        grepBase.put(fileName,res);
        return res;
    }
}
