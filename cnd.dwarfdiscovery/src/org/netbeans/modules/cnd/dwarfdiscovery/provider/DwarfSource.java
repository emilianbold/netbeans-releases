/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.BaseDwarfProvider.CompilerSettings;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class DwarfSource implements SourceFileProperties{
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
    private Map<String,List<String>> grepBase;
    
    DwarfSource(CompilationUnit cu, boolean isCPP, CompilerSettings compilerSettings, Map<String,List<String>> grepBase){
        initCompilerSettings(compilerSettings, isCPP);
        this.grepBase = grepBase;
        initSourceSettings(cu, isCPP);
    }
    
    private void initCompilerSettings(CompilerSettings compilerSettings, boolean isCPP){
        List<String> list = compilerSettings.getSystemIncludePaths(isCPP);
        if (list != null){
            systemIncludes = new ArrayList<String>(list);
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
    
    /**
     * Path is include path like:
     * .
     * ../
     * include
     * Returns path in unix style
     */
    public static String convertRelativePathToAbsolute(SourceFileProperties source, String path){
        if ( !( path.startsWith("/") || (path.length()>1 && path.charAt(1)==':') ) ) { // NOI18N
            if (path.equals(".")) { // NOI18N
                path = source.getCompilePath();
            } else {
                path = source.getCompilePath()+File.separator+path;
            }
            File file = new File(path);
            path = FileUtil.normalizeFile(file).getAbsolutePath();
        }
        if (Utilities.isWindows()) {
            path = path.replace('\\', '/');
        }
        return path;
    }

    public static final String getRelativePath(String base, String path) {
        if (path.equals(base)) {
            return path;
        } else if (path.startsWith(base + '/')) {
            return path.substring(base.length()+1);
        } else if (path.startsWith(base + '\\')) {
            return path.substring(base.length() + 1);
        } else if (!(path.startsWith("/") || path.startsWith("\\") || // NOI18N
                     path.length() > 2 && path.charAt(2)==':')) {
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
                s.append(".." + File.separator); // NOI18N
            }
            s.append(".." + File.separator + pstring); // NOI18N
            while(stp.hasMoreTokens()) {
                s.append(File.separator + stp.nextToken() ); // NOI18N
            }
            return s.toString();
        }
    }

    private String fixFileName(String fileName) {
        if (fileName == null){
            return fileName;
        }
        if (Utilities.isWindows()) {
            //replace /cygdrive/<something> prefix with <something>:/ prefix:
            if (fileName.startsWith(CYG_DRIVE_UNIX)) {
                fileName = fileName.substring(CYG_DRIVE_UNIX.length()); // NOI18N
                fileName = "" + Character.toUpperCase(fileName.charAt(0)) + ':' + fileName.substring(1); // NOI18N
                fileName = fileName.replace('\\', '/');
                if (cygwinPath == null) {
                    cygwinPath = "" + Character.toUpperCase(fileName.charAt(0)) + CYGWIN_PATH;
                }
            } else {
                int i = fileName.indexOf(CYG_DRIVE_WIN);
                if (i > 0) {
                    //replace D:\cygdrive\c\<something> prefix with <something>:\ prefix:
                    if (cygwinPath == null) {
                        cygwinPath = "" + Character.toUpperCase(fileName.charAt(0)) + CYGWIN_PATH; // NOI18N
                    }
                    fileName = fileName.substring(i+CYG_DRIVE_UNIX.length());
                    fileName = "" + Character.toUpperCase(fileName.charAt(0)) + ':' + fileName.substring(1); // NOI18N
                    fileName = fileName.replace('\\', '/');
                }
            }
        } else if (Utilities.isUnix()) {
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
                    try {
                        LinkReader linkReader = new LinkReader(link);
                        if (linkReader.getSource() != null){
                            name = linkReader.getSource();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
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
                                    try {
                                        LinkReader linkReader = new LinkReader(link);
                                        if (linkReader.getSource() != null){
                                            buf = new StringBuilder(linkReader.getSource());
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
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
    
    
    private void initSourceSettings(CompilationUnit cu, boolean isCPP){
        userIncludes = new ArrayList<String>();
        userMacros = new HashMap<String,String>();
        includedFiles = new HashSet<String>();
        File file = new File(cu.getSourceFileAbsolutePath());
        fullName = FileUtil.normalizeFile(file).getAbsolutePath();
        fullName = PathCache.getString(fixFileName(fullName));
        fullName = linkSupport(fullName);
        if (fullName != null && Utilities.isWindows()) {
            fullName = fullName.replace('/', '\\');
        }
        compilePath = PathCache.getString(fixFileName(cu.getCompilationDir()));
        sourceName = PathCache.getString(cu.getSourceFileName());
        
        if (compilePath == null && sourceName.lastIndexOf('/')>0) {
            int i = sourceName.lastIndexOf('/');
            compilePath = sourceName.substring(0,i);
            sourceName = sourceName.substring(i+1);
        } else {
            if (sourceName.startsWith("/")) { // NOI18N
                sourceName = getRelativePath(compilePath, sourceName);
            }
        }
        if (isCPP) {
            language = ItemProperties.LanguageKind.CPP;
        } else {
            language = ItemProperties.LanguageKind.C;
        }
    }
    
    public void process(CompilationUnit cu){
        String line = cu.getCommandLine();
        if (line != null && line.length()>0){
            gatherLine(line);
            gatherIncludedFiles(cu);
        } else {
            gatherMacros(cu);
            gatherIncludes(cu);
        }
    }
    
    private void gatherLine(String line) {
        // /set/c++/bin/5.9/intel-S2/prod/bin/CC -c -g -DHELLO=75 -Idist  main.cc -Qoption ccfe -prefix -Qoption ccfe .XAKABILBpivFlIc.
        StringTokenizer st = new StringTokenizer(line);
        while(st.hasMoreTokens()){
            String option = st.nextToken();
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
                if (path.length()==0 && st.hasMoreTokens()){
                    path = st.nextToken();
                }
                String include = PathCache.getString(path);
                userIncludes.add(include);
            } else if (option.startsWith("-Y")){ // NOI18N
                String defaultSearchPath = option.substring(2);
                if (defaultSearchPath.length()==0 && st.hasMoreTokens()){
                    defaultSearchPath = st.nextToken();
                }
                if (defaultSearchPath.startsWith("I,")){ // NOI18N
                    defaultSearchPath = defaultSearchPath.substring(2);
                    String include = PathCache.getString(defaultSearchPath);
                    userIncludes.add(include);
                }
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
            for (String cp : systemIncludes){
                if (path.startsWith(cp)){
                    return true;
                }
            }
        }
        return false;
    }
    
    private void addpath(String path){
        if (haveSystemIncludes) {
            if (!isSystemPath(path)){
                userIncludes.add(PathCache.getString(path));
            }
        } else {
            if (path.startsWith("/usr")) { // NOI18N
                path = fixCygwinPath(path);
                path = normalizePath(path);
                systemIncludes.add(PathCache.getString(path));
            } else {
                path = fixCygwinPath(path);
                path = normalizePath(path);
                userIncludes.add(PathCache.getString(path));
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
    
    private void gatherIncludes(final CompilationUnit cu) {
        if (!ourGatherIncludes) {
            return;
        }
        DwarfStatementList dwarfTable = cu.getStatementList();
        if (dwarfTable == null) {
            return;
        }
        for (Iterator<String> it = dwarfTable.getIncludeDirectories().iterator(); it.hasNext();) {
            addpath(it.next());
        }
        List<String> list = grepSourceFile(fullName);
        for(String path : list){
            cutFolderPrefix(path, dwarfTable);
        }
        ArrayList<String> dwarfIncludedFiles = dwarfTable.getFilePaths();
        for(String path : dwarfIncludedFiles){
            String includeFullName = path;
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
            list = grepSourceFile(includeFullName);
            for(String included : list){
                cutFolderPrefix(included, dwarfTable);
            }
            includedFiles.add(PathCache.getString(includeFullName));
        }
    }

    private void cutFolderPrefix(final String path, final DwarfStatementList dwarfTable) {
        if (path.indexOf(File.separatorChar)>0){
            int n = path.lastIndexOf(File.separatorChar);
            String name = path.substring(n+1);
            String relativeDir = path.substring(0,n);
            String dir = File.separator+relativeDir;
            ArrayList<String> paths = dwarfTable.getPathsForFile(name);
            for(String dwarfPath : paths){
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
                                userIncludes.add(PathCache.getString(found));
                            }
                        } else {
                            if (!dwarfPath.startsWith("/usr")){ // NOI18N
                                userIncludes.add(PathCache.getString(found));
                            }
                        }
                    }
                    break;
                } else if (dwarfPath.equals(relativeDir)){
                    String found = "."; // NOI18N
                    if (!userIncludes.contains(found)) {
                        userIncludes.add(PathCache.getString(found));
                    }
                    break;
                }
            }
        }
    }
    
    private void gatherIncludedFiles(final CompilationUnit cu) {
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
    
    private void gatherMacros(final CompilationUnit cu) {
        if (!ourGatherMacros){
            return;
        }
        DwarfMacinfoTable dwarfTable = cu.getMacrosTable();
        if (dwarfTable == null) {
            return;
        }
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
            if (haveSystemMacros && systemMacros.containsKey(macro)){
                // filter out system macros
                // For example gcc windows dwarf contains following system macros as user:
                // unix=1 __unix=1 __unix__=1 __CYGWIN__=1 __CYGWIN32__=1
                if (value == null || "1".equals(value)){ // NOI18N
                    continue;
                }
            }
            userMacros.put(macro,value);
        }
    }
    
    private List<String> grepSourceFile(String fileName){
        List<String> res = grepBase.get(fileName);
        if (res != null) {
            return res;
        }
        res = new ArrayList<String>();
        File file = new File(fileName);
        if (file.exists() && file.canRead()){
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));
                while(true){
                    String line = in.readLine();
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
                            char c = line.charAt(0);
                            if (c == '"') {
                                if (line.indexOf('"',1)>0){
                                    res.add(line.substring(1,line.indexOf('"',1)));
                                }
                            } else if (c == '<'){
                                if (line.indexOf('>')>0){
                                    res.add(line.substring(1,line.indexOf('>')));
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        grepBase.put(fileName,res);
        return res;
    }
}
