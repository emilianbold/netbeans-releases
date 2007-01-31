/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;

/**
 *
 * @author Alexander Simon
 */
public class DwarfSource implements SourceFileProperties{
    private static boolean ourGatherMacros = true;
    private static boolean ourGatherIncludes = true;
    
    private String compilePath;
    private String sourceName;
    private String fullName;
    private ItemProperties.LanguageKind language;
    private List<String> userIncludes;
    private List<String> systemIncludes;
    private Map<String, String> userMacros;
    private Set<String> includedFiles;
    
    DwarfSource(CompilationUnit cu){
        init(cu);
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
        return null;
    }
    
    public ItemProperties.LanguageKind getLanguageKind() {
        return language;
    }
    
    private void init(CompilationUnit cu){
        userIncludes = new ArrayList<String>();
        systemIncludes = new ArrayList<String>();
        userMacros = new HashMap<String,String>();
        includedFiles = new HashSet<String>();
        fullName = cu.getSourceFileFullName();
        compilePath = cu.getCompilationDir();
        sourceName = cu.getSourceFileName();
        
        if (compilePath == null && sourceName.lastIndexOf('/')>0) {
            int i = sourceName.lastIndexOf('/');
            compilePath = sourceName.substring(0,i);
            sourceName = sourceName.substring(i+1);
        }
        //if (sourceName.indexOf('/')>=0) {
        //    System.out.println("Source file "+cu.getSourceFileFullName());
        //    System.out.println("    was compiled in folder "+compilePath);
        //    System.out.println("    with name "+sourceName);
        //}
        language = ItemProperties.LanguageKind.C;
        if (LANG.DW_LANG_C_plus_plus.toString().equals(cu.getSourceLanguage())){
            language = ItemProperties.LanguageKind.CPP;
        }
        String line = cu.getCommandLine();
        if (line != null && line.length()>0){
            gatherLine(line);
            gatherIncludedFiles(cu);
        } else {
            gatherMacros(cu);
            // TODO: this thile in MySQL has bad configuration because Backup.hpp has "/" in include
            //if (sourceName.endsWith("Backup.hpp")) {
            //    System.out.println(sourceName);
            //}
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
                int i = macro.indexOf(' ');
                if (i>0){
                    userMacros.put(macro.substring(0,i), macro.substring(i+1).trim());
                } else {
                    userMacros.put(macro, null);
                }
            } else if (option.startsWith("-I")){ // NOI18N
                String include = option.substring(2);
                userIncludes.add(include);
            }
        }
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
            String path = it.next();
            if (path.startsWith("/usr")) { // NOI18N
                systemIncludes.add(path);
            } else {
                userIncludes.add(path);
            }
        }
        List<String> list = grepSourceFile(cu.getSourceFileFullName());
        for(String path : list){
            if (path.indexOf(File.separatorChar)>0){
                int n = path.lastIndexOf(File.separatorChar);
                String name = path.substring(n+1);
                String dir = File.separator+path.substring(0,n);
                ArrayList<String> paths = dwarfTable.getPathsForFile(name);
                for(String dwarfPath : paths){
                    if (dwarfPath.endsWith(dir) && !dwarfPath.startsWith("/usr")){ // NOI18N
                        String found = dwarfPath.substring(0,dwarfPath.length()-dir.length());
                        if (!userIncludes.contains(found)) {
                            userIncludes.add(found);
                        }
                    }
                }
            }
        }
        for(String path :dwarfTable.getFilePaths()){
            String fullName = path;
            if (path.startsWith("./")) { // NOI18N
                fullName = cu.getCompilationDir()+path.substring(1);
            } else if (path.startsWith("../")) { // NOI18N
                fullName = cu.getCompilationDir()+File.separator+path;
            }
            includedFiles.add(fullName);
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
            String fullName = path;
            if (path.startsWith("./")) { // NOI18N
                fullName = cu.getCompilationDir()+path.substring(1);
            } else if (path.startsWith("../")) { // NOI18N
                fullName = cu.getCompilationDir()+File.separator+path;
            }
            includedFiles.add(fullName);
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
            if (i>0){
                userMacros.put(def.substring(0,i), def.substring(i+1).trim());
            } else {
                userMacros.put(def, null);
            }
        }
    }
    
    private List<String> grepSourceFile(String fileName){
        List<String> res = new ArrayList<String>();
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
        return res;
    }
}
