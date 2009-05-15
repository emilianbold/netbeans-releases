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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class SolarisLogReader {
    private static boolean TRACE = false;
    // environment variable  path to prototype
    // ROOT=/export/opensolaris/testws80/proto/root_i386
    private static final String ENV_ROOT = "ROOT="; // NOI18N
    // environment variable path to sources
    // SRC=/export/opensolaris/testws80/usr/src
    private static final String ENV_SRC = "SRC="; // NOI18N
    
    private String workingDir;
    private String prevWorkingDir;
    private final String root;
    private final String fileName;
    private List<SourceFileProperties> result;
    private List<InstallLine> copyHeader;
    private Map<String,List<String>> findBase;
    private TreeMap<String,Set<String>> libraries;
    private String buidMashinePrototype;
    private String buidMashineSources;
    
    public SolarisLogReader(String fileName, String root){
        this.root = root;
        this.fileName = fileName;
       
        // XXX
        setWorkingDir(root);
    }
    
    private void run() {
        if (TRACE) {System.out.println("LogReader is run for " + fileName);} //NOI18N
        Pattern pattern = Pattern.compile(";|\\|\\||&&"); // NOI18N
        result = new ArrayList<SourceFileProperties>();
        copyHeader = new ArrayList<InstallLine>();
        libraries = new TreeMap<String,Set<String>>();
        File file = new File(fileName);
        if (file.exists() && file.canRead()){
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));
                int nFoundFiles = 0;
                while(true){
                    String line = in.readLine();
                    if (line == null){
                        break;
                    }
                    line = line.trim();
                    if (buidMashinePrototype == null || buidMashineSources == null){
                        if (line.startsWith(ENV_ROOT)){
                            buidMashinePrototype = line.substring(ENV_ROOT.length());
                            if (TRACE) {System.out.println("Environment variable path to prototype: " + buidMashinePrototype);} //NOI18N
                            continue;
                        }
                        if (line.startsWith(ENV_SRC)){
                            buidMashineSources = line.substring(ENV_SRC.length());
                            if (TRACE) {System.out.println("Environment variable path to sources: " + buidMashineSources);} //NOI18N
                            continue;
                        }
                    }
                    while (line.endsWith("\\")) { // NOI18N
                        String oneMoreLine = in.readLine();
                        if (oneMoreLine == null) {
                            break;
                        }
                        line = line.substring(0, line.length() - 1) + " " + oneMoreLine.trim(); //NOI18N
                    }
                    line = trimBackApostropheCalls(line);

                    String[] cmds = pattern.split(line);
                    for (int i = 0; i < cmds.length; i++) {
                        if (parseLine(cmds[i])){
                            nFoundFiles++;
                        } else {
                            InstallLine copy = testInstall(cmds[i]);
                            if (copy != null) {
                                copy.destination = relocate(copy.destination);
                                copy.source = relocate(copy.source);
                                File source = new File(copy.source);
                                if (source.exists() && !source.isDirectory()){
                                    copyHeader.add(copy);
                                }
                            }
                        }
                    }

                }
                if (TRACE) {
                    System.out.println("Files found: " + nFoundFiles); //NOI18N
                    System.out.println("Files included in result: "+ result.size()); //NOI18N
                }
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public List<SourceFileProperties> getResults() {
        if (result == null) {
            run();
            findBase.clear();
            findBase = null;
        }
        return result;
    }

    /*package-local*/ List<InstallLine> getInstalls() {
        return copyHeader;
    }
    
    private static final String CURRENT_DIRECTORY = "Current working directory"; //NOI18N
    private static final String ENTERING_DIRECTORY = "Entering directory"; //NOI18N

    private boolean checkDirectoryChange(String line) {
        String workDir = null, message = null;

        if (line.startsWith(CURRENT_DIRECTORY)) {
            workDir = line.substring(CURRENT_DIRECTORY.length() + 1).trim();
            if (TRACE) {message = "**>> by [" + CURRENT_DIRECTORY + "] ";} //NOI18N
        } else if (line.indexOf(ENTERING_DIRECTORY) >= 0) {
            String dirMessage = line.substring(line.indexOf(ENTERING_DIRECTORY) + ENTERING_DIRECTORY.length() + 1).trim();
            workDir = dirMessage.replaceAll("`|'|\"", ""); //NOI18N
            if (TRACE) {message = "**>> by [" + ENTERING_DIRECTORY + "] ";} //NOI18N
        } else if (line.startsWith(LABEL_CD)) {
            int end = line.indexOf(MAKE_DELIMITER);
            workDir = (end == -1 ? line : line.substring(0, end)).substring(LABEL_CD.length()).trim();
            if (TRACE) {message = "**>> by [ " + LABEL_CD + "] ";} //NOI18N
        } else if (line.startsWith("/") && line.indexOf(" ") < 0) {  //NOI18N
            workDir = line.trim();
            if (TRACE) {message = "**>> by [just path string] ";} //NOI18N
        }

        if (workDir == null) {
            return false;
        }
        workDir = relocate(workDir);
        
        if (new File(workDir).exists()) {
            if (TRACE) {System.err.print(message);}
            setWorkingDir(workDir);
            return true;
        } else {
            workDir = getWorkingDir() + File.separator + workDir;
            workDir = cutLocalRelative(workDir);
            if (new File(workDir).exists()) {
                if (TRACE) {System.err.print(message);}
                setWorkingDir(workDir);
                return true;
            }
        }
        return false;
    }

    private String relocate(String path){
        String res = path;
        if (buidMashineSources != null && root.length() > 0 && res.startsWith(buidMashineSources)){
            // buidMashineSources=/export/opensolaris/testws80/usr/src
            // root=/export/opensolaris/testws80
            int i = buidMashineSources.lastIndexOf("/usr/src"); //NOI18N
            if (i > 0) {
                res = root+path.substring(i); //NOI18N
            }
        } else if (buidMashinePrototype != null && root.length() > 0 && res.startsWith(buidMashinePrototype)){
            // buidMashinePrototype=/export/opensolaris/testws80/proto/root_i386
            // root=/export/opensolaris/testws80
            int i = buidMashinePrototype.lastIndexOf("/proto/"); //NOI18N
            if (i > 0) {
                res = root+path.substring(i); //NOI18N
            }
        }
        if (TRACE && !path.equals(res)) {
            System.out.println("Relocate path from: "+path+" to "+res); //NOI18N
        }
        return res;
    }
    
    public String getWorkingDir() {
        return workingDir;
    }

    //  /usr/bin/rm -f /export/opensolaris/testws80/proto/root_i386/usr/include/stdio.h; 
    //  install -s -m 644 -f /export/opensolaris/testws80/proto/root_i386/usr/include stdio.h
    private InstallLine testInstall(String line) {
        line = line.trim();
        if (line.startsWith("install ") && line.indexOf("/proto/") > 0 && // NOI18N
            (line.indexOf("/usr/include") > 0 || // NOI18N
             line.indexOf("/usr/ucbinclude") > 0 || // NOI18N
             line.indexOf("/usr/sfw/include") > 0)) { // NOI18N
            return parseInstall(line.substring(line.indexOf(' ')+1)); // NOI18N
        }
        return null;
    }
    private InstallLine parseInstall(String line){
        Iterator<String> st = DiscoveryUtils.scanCommandLine(line).iterator();
        String path = "";
        String name = null;
        while(st.hasNext()){
            String option = st.next();
            if (option.equals("-c")){ // NOI18N
                if (path.length()==0 && st.hasNext()){
                    path = st.next();
                }
            } else if (option.equals("-f")){ // NOI18N
                if (path.length()==0 && st.hasNext()){
                    path = st.next();
                }
            } else if (option.equals("-n")){ // NOI18N
                if (path.length()==0 && st.hasNext()){
                    path = st.next();
                }
            } else if (option.equals("-m")){ // NOI18N
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-u")){ // NOI18N
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.equals("-g")){ // NOI18N
                if (st.hasNext()){
                    st.next();
                }
            } else if (option.startsWith("-d")){ // NOI18N
            } else if (option.startsWith("-i")){ // NOI18N
            } else if (option.startsWith("-d")){ // NOI18N
            } else if (option.startsWith("-o")){ // NOI18N
            } else if (option.startsWith("-s")){ // NOI18N
            } else {
                if (name == null) {
                    name = option;
                    if (name.startsWith("\"") && name.endsWith("\"")){ // NOI18N
                        name = name.substring(1,name.length()-1);
                    }
                } else {
                    if (TRACE) {System.err.println("What is "+option+" in line "+line);} // NOI18N
                }
            }
        }
        if (path.length()>0 && name != null && getWorkingDir() != null){
            String source = getWorkingDir();
            if (getWorkingDir().endsWith("/usr/src/uts/common/sys/lvm")) { // NOI18N
                // It is an opensolaris bug?
                source = source.substring(0, source.lastIndexOf("/lvm")); // NOI18N
            }
            if (name.startsWith("/")){ // NOI18N
                source = name;
            } else {
                source = source+"/"+name; // NOI18N
            }
            int j = name.lastIndexOf('/'); // NOI18N
            if (j >0){
                name = name.substring(j+1);
            }
            String destination = path+"/"+name; // NOI18N
            if (TRACE) {
                File fileTo = new File(destination);
                if (fileTo.exists() && !fileTo.isDirectory()){
                    File fileFrom = new File(source);
                    if (fileFrom.exists() && !fileFrom.isDirectory()){
                        System.err.println("Ok "+source+"->"+destination); // NOI18N
                    } else {
                        System.err.println("No source "+source+"->"+destination); // NOI18N
                    }
                } else {
                    System.err.println("No destination "+source+"->"+destination); // NOI18N
                }
            }
            return new InstallLine(source, destination);
        } else {
            // It is an install of a folder
        }
        return null;
    }
    
    private enum CompilerType {
        CPP, C, UNKNOWN;
    };
    private enum CompilerFamily {
        SUN, GNU, UNKNOWN;
    };
    
    private static class LineInfo {
        public String compileLine;
        public CompilerType compilerType = CompilerType.UNKNOWN;
        public CompilerFamily compilerFamily = CompilerFamily.UNKNOWN;
        
        LineInfo(String line) {
            compileLine = line;
        }
    }
    
    private static final String LABEL_CD = "cd "; //NOI18N
    private static final String INVOKE_SUN_C = "cc "; //NOI18N
    private static final String INVOKE_SUN_CC = "CC "; //NOI18N
    private static final String INVOKE_GNU_C = "gcc "; //NOI18N
    //private static final String INVOKE_GNU_XC = "xgcc "; //NOI18N
    private static final String INVOKE_GNU_CC = "g++ "; //NOI18N
    private static final String MAKE_DELIMITER = ";"; //NOI18N
    
    private LineInfo testCompilerInvocation(String line) {
        LineInfo li = new LineInfo(line);
        int start = 0, end = -1;
//        if (li.compilerType == CompilerType.UNKNOWN) {
//            start = line.indexOf(INVOKE_GNU_XC);
//            if (start>=0) {
//                li.compilerType = CompilerType.C;
//                end = start + INVOKE_GNU_XC.length();
//            }
//        } 
        if (li.compilerType == CompilerType.UNKNOWN) {
            start = line.indexOf(INVOKE_GNU_C);
            if (start>=0) {
                li.compilerType = CompilerType.C;
                li.compilerFamily = CompilerFamily.GNU;
                end = start + INVOKE_GNU_C.length();
            }
        } 
        if (li.compilerType == CompilerType.UNKNOWN) {
            start = line.indexOf(INVOKE_GNU_CC);
            if (start>=0) {
                li.compilerType = CompilerType.CPP;
                li.compilerFamily = CompilerFamily.GNU;
                end = start + INVOKE_GNU_CC.length();
            }
        } 
        if (li.compilerType == CompilerType.UNKNOWN) {
            start = line.indexOf(INVOKE_SUN_C);
            if (start>=0) {
                li.compilerType = CompilerType.C;
                li.compilerFamily = CompilerFamily.SUN;
                end = start + INVOKE_SUN_C.length();
            }
        } 
        if (li.compilerType == CompilerType.UNKNOWN) {
            start = line.indexOf(INVOKE_SUN_CC);
            if (start>=0) {
                li.compilerType = CompilerType.CPP;
                li.compilerFamily = CompilerFamily.SUN;
                end = start + INVOKE_SUN_CC.length();
            }
        }
       
        if (li.compilerType != CompilerType.UNKNOWN) {
            li.compileLine = line.substring(start);
            while(end < line.length() && (line.charAt(end) == ' ' || line.charAt(end) == '\t')) {
                end++;
            }
            if (end >= line.length() || line.charAt(end)!='-') {
                // suspected compiler invocation has no options or a part of a path?? -- noway
                li.compilerType =  CompilerType.UNKNOWN;
            } 
            
            else if (start > 0 && line.charAt(start-1)!='/') {
                // suspected compiler invocation is not first command in line?? -- noway
                String prefix = line.substring(0, start - 1).trim();
                // wait! maybe it's called in condition?
                if (!(line.charAt(start - 1) == ' ' && 
                        ( prefix.equals("if") || prefix.equals("then") || prefix.equals("else") ))) { //NOI18N
                    // or it's a lib compiled by libtool? 
                    int ltStart = line.substring(0, start).indexOf("libtool"); //NOI18N
                    if (!(ltStart >= 0 && line.substring(ltStart, start).indexOf("compile") >= 0)) { //NOI18N
                        // no, it's not a compile line
                        li.compilerType = CompilerType.UNKNOWN;
                        // I hope
                        if (TRACE) {System.err.println("Suspicious line: " + line);} //NOI18N
                    }
                }
            }
        }
        return li;
    }
    
    private void setWorkingDir(String workingDir) {
        if (TRACE) {System.err.println("**>> new working dir: " + workingDir);} //NOI18N
        if (!workingDir.equals(this.workingDir)) {
            prevWorkingDir = this.workingDir;
            this.workingDir = workingDir;
        }
    }
    
    private boolean parseLine(String line){
       if (checkDirectoryChange(line)) {
           return false;
       }
//       if (line.startsWith(CURRENT_DIRECTORY)) {
//           workingDir= line.substring(CURRENT_DIRECTORY.length()+1).trim();
//           return false;
//       }
       if (getWorkingDir() == null) {
           return false;
       }
       if (!getWorkingDir().startsWith(root)){
           return false;
       }
       
       LineInfo li = testCompilerInvocation(line);
       if (li.compilerType != CompilerType.UNKNOWN) {
           return gatherLine(li.compileLine, line.startsWith("+"), li.compilerType == CompilerType.CPP, li.compilerFamily); // NOI18N
       }
       return false;
    }

    private static String trimBackApostropheCalls(String line) {
        int i = line.indexOf('`');
        if (line.lastIndexOf('`') == i) { // do not trim unclosed `quotes`
            return line;
        }
        if (i < 0 || i == line.length() - 1) {
            return line;
        } else {
            String out = line.substring(0, i);
            line = line.substring(i+1);
            int j = line.indexOf('`');
            if (j < 0) {
                return line;
            }
            out += line.substring(j+1);
            return trimBackApostropheCalls(out);
        }
    }
    
    private boolean gatherLine(String line, boolean isScriptOutput, boolean isCPP, CompilerFamily compiler) {
        // /set/c++/bin/5.9/intel-S2/prod/bin/CC -c -g -DHELLO=75 -Idist  main.cc -Qoption ccfe -prefix -Qoption ccfe .XAKABILBpivFlIc.
        // /opt/SUNWspro/bin/cc -xO3 -xarch=amd64 -Ui386 -U__i386 -Xa -xildoff -errtags=yes -errwarn=%all
        // -erroff=E_EMPTY_TRANSLATION_UNIT -erroff=E_STATEMENT_NOT_REACHED -xc99=%none -W0,-xglobalstatic
        // -D_ELF64 -DTEXT_DOMAIN="SUNW_OST_OSCMD" -D_TS_ERRNO -I/export/opensolaris/testws77/proto/root_i386/usr/include
        // -I/export/opensolaris/testws77/usr/src/uts/common/inet/ipf -I/export/opensolaris/testws77/usr/src/uts/common/inet/pfil
        // -DSUNDDI -DUSE_INET6 -DSOLARIS2=11 -I. -DIPFILTER_LOOKUP -DIPFILTER_LOG -c ../ipmon_l.c -o ipmon_l.o
        //if (line.indexOf("../port/gen/errlst.c") > 0) {
        //    System.out.println(line);
        //}
        List<String> userIncludes = new ArrayList<String>();
        Map<String, String> userMacros = new HashMap<String, String>();
        Set<String> libs = new HashSet<String>();
        String what = DiscoveryUtils.gatherCompilerLine(line, isScriptOutput, userIncludes, userMacros, libs);
        if (libs.size()>0){
            Set<String> l = libraries.get(getWorkingDir());
            if (l == null){
                libraries.put(getWorkingDir(), libs);
            } else {
                l.addAll(libs);
            }
        }
        if (what == null){
            return false;
        }
        String file = null;
        if (what.startsWith("/")){  //NOI18N
            file = relocate(what);
            what = file;
        } else {
            file = getWorkingDir()+"/"+what;  //NOI18N
            File f = new File(file);
            if (!f.exists() || !f.isFile()) {
                if (this.prevWorkingDir != null) {
                    String file2 = null;
                    file2 = this.prevWorkingDir+"/"+what;  //NOI18N
                    f = new File(file2);
                    if (f.exists() && f.isFile()) {
                        if (TRACE) {System.out.println("restore path "+getWorkingDir()+"->"+this.prevWorkingDir);}  //NOI18N
                        this.setWorkingDir(this.prevWorkingDir);
                        file = file2;
                    }
                }
            }
        }
        List<String> userIncludesCached = new ArrayList<String>(userIncludes.size());
        for(String s : userIncludes){
            s = relocate(s);
            userIncludesCached.add(PathCache.getString(s));
        }
        Map<String, String> userMacrosCached = new HashMap<String, String>(userMacros.size());
        for(Map.Entry<String,String> e : userMacros.entrySet()){
            if (e.getValue() == null) {
                userMacrosCached.put(PathCache.getString(e.getKey()), null);
            } else {
                userMacrosCached.put(PathCache.getString(e.getKey()), PathCache.getString(e.getValue()));
            }
        }
        File f = new File(file);
        if (!f.exists() || !f.isFile()) {
            if (TRACE)  {System.err.println("**** Not found "+file);}  //NOI18N
            String relative = "";
            if (!what.startsWith("/")){  //NOI18N
                int i = what.lastIndexOf('/'); //NOI18N
                if (i > 0) {
                    relative = what.substring(0,i);
                    what = what.substring(i+1);
                    file = getWorkingDir()+"/"+what;  //NOI18N
                    f = new File(file);
                    if (f.exists() && f.isFile()) {
                        addToResult(new CommandLineSource(isCPP, compiler==CompilerFamily.SUN, getWorkingDir(), what, userIncludesCached, userMacrosCached));
                        return true;
                    }
                }
                String search = findFiles(what, getWorkingDir(), relative);
                if (search != null) {
                    setWorkingDir(search);
                    addToResult(new CommandLineSource(isCPP, compiler==CompilerFamily.SUN, getWorkingDir(), what, userIncludesCached, userMacrosCached));
                    if (TRACE) {System.err.println("** Gotcha: " + search + File.separator + what);} //NOI18N
                    // kinda adventure but it works
                    return true;
                }
            } 
            if (TRACE) {System.err.println(""+ (line.length() > 120 ? line.substring(0,117) + ">>>" : line) + " [" + what + "]");} //NOI18N
            return false;
        } else if (TRACE) {System.err.println("**** Gotcha: " + file);} //NOI18N
        addToResult(new CommandLineSource(isCPP, compiler==CompilerFamily.SUN, getWorkingDir(), what, userIncludesCached, userMacrosCached));
        return true;
    }
    
    private void addToResult(CommandLineSource source){
        if (result.size()>0) {
            CommandLineSource prev = (CommandLineSource) result.get(result.size()-1);
            if (prev.getItemPath().equals(source.getItemPath())) {
                // first compilation is GNU, second is SUN
                if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                    // Rplace GNU compilation on SUN
                    if (!prev.isSunCompiler && source.isSunCompiler) {
                        result.set(result.size()-1, source);
                        return;
                    }
                } else {
                    // Skip SUN compilation
                    if (!prev.isSunCompiler && source.isSunCompiler) {
                        return;
                    }
                }
            }
        }
        result.add(source);
    }
    
    private String findFiles(String name, String wd, String relative) {
        List<String> res = getFiles(name);
        if (res != null && res.size()==1) {
            return res.get(0);
        } else if (res != null) {
            if (TRACE) {System.out.println("More then one "+name);} //NOI18N
            for (String r: res){
                if (r.startsWith(wd)) {
                    return r;
                }
                return null;
            }
        } else {
            if (TRACE) {System.out.println("Not found "+name);} //NOI18N
        }
        return null;
    }
        
    private List<String> getFiles(String name){
        if (findBase == null) {
            findBase = initSearchMap();
        }
        return findBase.get(name);
    }
    
    private Map<String,List<String>> initSearchMap(){
        HashSet<String> set = new HashSet<String>();
        File f = new File(root);
        gatherSubFolders(f, set);
        HashMap<String,List<String>> map = new HashMap<String,List<String>>();
        for (Iterator it = set.iterator(); it.hasNext();){
            File d = new File((String)it.next());
            if (d.exists() && d.isDirectory() && d.canRead()){
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    if (ff[i].isFile()) {
                        List<String> l = map.get(ff[i].getName());
                        if (l==null){
                            l = new ArrayList<String>();
                            map.put(ff[i].getName(),l);
                        }
                        l.add(d.getAbsolutePath());
                    }
                }
            }
        }
        return map;
    }
    
    private void gatherSubFolders(File d, HashSet<String> set){
        if (d.exists() && d.isDirectory() && d.canRead()){
            if (DiscoveryUtils.ignoreFolder(d)){
                return;
            }
            String path = d.getAbsolutePath();
            if (!set.contains(path)){
                set.add(d.getAbsolutePath());
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    gatherSubFolders(ff[i], set);
                }
            }
        }
    }
    
    /*package-local*/ TreeMap<String,Set<String>> getLibraries(){
        return libraries;
    }
    
    /*package-local*/ TreeMap<String,Set<String>> readMapFile(){
        List<String> mapfile = getFiles("mapfile-vers"); //NOI18N
        if (mapfile != null) {
            TreeMap<String, Set<String>> res = new TreeMap<String, Set<String>>();
            Collections.sort(mapfile);
            for(String name:mapfile){
                Set<String> set = readMapFile(name+"/mapfile-vers"); //NOI18N
                if (set != null) {
                    res.put(name, set);
                }
            }
            return res;
        }
        return null;
    }
    
    private Set<String> readMapFile(String name){
        File file = new File(name);
        if (file.exists() && file.canRead()){
            try {
                Set<String> set = new HashSet<String>();
                BufferedReader in = new BufferedReader(new FileReader(file));
                //System.out.println(name);
                boolean inBlock = false;
                boolean inGlobal = false;
                while(true){
                    String line = in.readLine();
                    if (line == null){
                        break;
                    }
                    line = line.trim();
                    if (line.startsWith("#")){ //NOI18N
                        continue;
                    }
                    if (line.endsWith("{")){ //NOI18N
                        inGlobal = false;
                        inBlock = true;
                        continue;
                    }
                    if (line.startsWith("}")){ //NOI18N
                        inGlobal = false;
                        inBlock = false;
                        continue;
                    }
                    if (inBlock && line.endsWith("global:")){ //NOI18N
                        inGlobal = true;
                        continue;
                    }
                    if (inBlock && line.endsWith("local:")){ //NOI18N
                        inGlobal = false;
                        continue;
                    }
                    if (inBlock && inGlobal &&
                        line.indexOf(";") > 0 && // NOI18N
                        line.indexOf("FILTER") < 0){ // NOI18N
                        String res = line.substring(0,line.indexOf(";")); // NOI18N
                        if (res.indexOf("=")>0) { // NOI18N
                            res = res.substring(0, res.indexOf("=")); // NOI18N
                        }
                        if (res.indexOf(".")<0) { // NOI18N
                            res = res.trim();
                            set.add(res);
                            //System.out.println("\t"+res);
                        }
                    }
                }
                in.close();
                if (set.size()>0) {
                    return set;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    private static final String PATTERN_1 = File.separator+"."+File.separator; // NOI18N
    private static final String PATTERN_2 = File.separator+"."; // NOI18N
    private static final String PATTERN_3 = File.separator+".."+File.separator; // NOI18N
    private static final String PATTERN_4 = File.separator+".."; // NOI18N
    private static String cutLocalRelative(String path){
        String pattern = PATTERN_1;
        while(true) {
            int i = path.indexOf(pattern);
            if (i < 0){
                break;
            }
            path = path.substring(0,i+1)+path.substring(i+pattern.length());
        }
        pattern = PATTERN_2;
        if (path.endsWith(pattern)){
            path = path.substring(0,path.length()-pattern.length());
        }
        pattern = PATTERN_3;
        while(true) {
            int i = path.indexOf(pattern);
            if (i < 0){
                break;
            }
            int k = -1;
            for (int j = i-1; j >= 0; j-- ){
                if ( path.charAt(j)==File.separatorChar){
                    k = j;
                    break;
                }
            }
            if (k<0) {
                break;
            }
            path = path.substring(0,k+1)+path.substring(i+pattern.length());
        }
        pattern = PATTERN_4;
        if (path.endsWith(pattern)){
            int k = -1;
            for (int j = path.length()-pattern.length()-1; j >= 0; j-- ){
                if ( path.charAt(j)==File.separatorChar){
                    k = j;
                    break;
                }
            }
            if (k>0) {
                path = path.substring(0,k);
            }
        }
        return path;
    }

    private static class CommandLineSource implements SourceFileProperties {

        private String compilePath;
        private String sourceName;
        private String fullName;
        private ItemProperties.LanguageKind language;
        private List<String> userIncludes;
        private List<String> systemIncludes = Collections.<String>emptyList();
        private Map<String, String> userMacros;
        private Map<String, String> systemMacros = Collections.<String, String>emptyMap();
        private Set<String> includedFiles = Collections.<String>emptySet();
        private boolean isSunCompiler;

        private CommandLineSource(boolean isCPP, boolean isSunCompiler, String compilePath, String sourcePath, 
                List<String> userIncludes, Map<String, String> userMacros) {
            if (isCPP) {
                language = ItemProperties.LanguageKind.CPP;
            } else {
                language = ItemProperties.LanguageKind.C;
            }
            this.isSunCompiler = isSunCompiler;
            this.compilePath =compilePath;
            sourceName = sourcePath;
            if (sourceName.startsWith("/")) { // NOI18N
                fullName = sourceName;
                sourceName = DiscoveryUtils.getRelativePath(compilePath, sourceName);
            } else {
                fullName = compilePath+"/"+sourceName; //NOI18N
            }
            fullName = CndFileUtils.normalizeAbsolutePath(fullName);
            fullName = PathCache.getString(fullName);
            this.userIncludes = userIncludes;
            this.userMacros = userMacros;
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
    }

    /*package-local*/ static class InstallLine {
        private String source;
        private String destination;
        private InstallLine(String source, String destination){
            this.source = source;
            this.destination = destination;
        }
        /*package-local*/ void install(){
            // copy resource
            File from = new File(source);
            if (from.exists() && from.canRead() && !from.isDirectory()) {
                File to = new File(destination);
                if (to.exists()) {
                    return;
                }
                FileOutputStream out = null;
                FileInputStream in = null;
                try {
                    FileUtil.createFolder(to.getParentFile());
                    out = new FileOutputStream(to);
                    in = new FileInputStream(from);
                    FileUtil.copy(in, out);
                    if (TRACE) {System.err.println("Installed "+source+"->"+destination);} // NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
    }
}
