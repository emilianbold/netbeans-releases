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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class LogReader {
    private static boolean TRACE = Boolean.getBoolean("cnd.dwarfdiscovery.trace.read.log"); // NOI18N

    private String workingDir;
    private String guessWorkingDir;
    private String baseWorkingDir;
    private final String root;
    private final String fileName;
    private List<SourceFileProperties> result;
    private final PathMap pathMapper;
    private final ProjectProxy project;

    public LogReader(String fileName, String root, ProjectProxy project) {
        if (root.length()>0) {
            this.root = CndFileUtils.normalizeFile(new File(root)).getAbsolutePath();
        } else {
            this.root = root;
        }
        this.fileName = fileName;
        this.project = project;
        this.pathMapper = getPathMapper(project);

        // XXX
        setWorkingDir(root);
    }

    private String convertPath(String path){
        if(pathMapper != null) {
            if (CndPathUtilitities.isPathAbsolute(path)) {
                String local = pathMapper.getLocalPath(path);
                if (local != null) {
                    return local;
                }
            }
        }
        return path;
    }

    private PathMap getPathMapper(ProjectProxy project) {
        Project p = project.getProject();
        if (p != null) {
            // it won't now return null for local environment
            return RemoteSyncSupport.getPathMap(p);
        }
        return null;
    }

    private MakeConfiguration getConfiguration(ProjectProxy project) {
        if (project != null && project.getProject() != null) {
            ConfigurationDescriptorProvider pdp = project.getProject().getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp != null && pdp.gotDescriptor()) {
                MakeConfigurationDescriptor confDescr = pdp.getConfigurationDescriptor();
                if (confDescr != null) {
                    return confDescr.getActiveConfiguration();
                }
            }
        }
        return null;
    }

    private void run(Progress progress, AtomicBoolean isStoped, CompileLineStorage storage) {
        if (TRACE) {System.out.println("LogReader is run for " + fileName);} //NOI18N
        Pattern pattern = Pattern.compile(";|\\|\\||&&"); // ;, ||, && //NOI18N
        result = new ArrayList<SourceFileProperties>();
        File file = new File(fileName);
        if (file.exists() && file.canRead()){
            try {
                MakeConfiguration conf = getConfiguration(this.project);
                PkgConfig pkgConfig = PkgConfigManager.getDefault().getPkgConfig(conf);
                BufferedReader in = new BufferedReader(new FileReader(file));
                long length = file.length();
                long read = 0;
                int done = 0;
                if (length <= 0){
                    progress = null;
                }
                if (progress != null) {
                    progress.start(100);
                }
                int nFoundFiles = 0;
                try {
                    while(true){
                        if (isStoped.get()) {
                            break;
                        }
                        String line = in.readLine();
                        if (line == null){
                            break;
                        }
                        read += line.length()+1;
                        line = line.trim();
                        while (line.endsWith("\\")) { // NOI18N
                            String oneMoreLine = in.readLine();
                            if (oneMoreLine == null) {
                                break;
                            }
                            line = line.substring(0, line.length() - 1) + " " + oneMoreLine.trim(); //NOI18N
                        }
                        line = trimBackApostropheCalls(line, pkgConfig);

                        String[] cmds = pattern.split(line);
                        for (int i = 0; i < cmds.length; i++) {
                            if (parseLine(cmds[i], storage)){
                                nFoundFiles++;
                            }
                        }
                        if (read*100/length > done && done < 100){
                            done++;
                            if (progress != null) {
                                progress.increment(null);
                            }
                        }
                    }
                } finally {
                    if (progress != null) {
                        progress.done();
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

    public List<SourceFileProperties> getResults(Progress progress, AtomicBoolean isStoped, CompileLineStorage storage) {
        if (result == null) {
            run(progress, isStoped, storage);
            if (subFolders != null) {
                subFolders.clear();
                subFolders = null;
                findBase.clear();
                findBase = null;
            }
        }
        return result;
    }

    private final ArrayList<List<String>> makeStack = new ArrayList<List<String>>();

    private int getMakeLevel(String line){
        int i1 = line.indexOf('[');
        if (i1 > 0){
            int i2 = line.indexOf(']');
            if (i2 > i1) {
                String s = line.substring(i1+1, i2);
                try {
                    int res = Integer.parseInt(s);
                    return res;
                } catch (NumberFormatException ex) {

                }
            }
        }
        return -1;
    }

    private void enterMakeStack(String dir, int level){
        if (level < 0) {
            return;
        }
        for(int i = makeStack.size(); i <= level; i++) {
            makeStack.add(new ArrayList<String>());
        }
        List<String> list = makeStack.get(level);
        list.add(dir);
    }

    private boolean leaveMakeStack(String dir, int level) {
        if (level < 0) {
            return false;
        }
        if (makeStack.size() <= level) {
            return false;
        }
        List<String> list = makeStack.get(level);
        for(String s : list) {
            if (s.equals(dir)) {
                list.remove(s);
                return true;
            }
        }
        return false;
    }

    private List<String> getMakeTop(int level){
        ArrayList<String> res = new ArrayList<String>();
        for(int i = Math.min(makeStack.size(), level-1); i >=0; i--){
            List<String> list = makeStack.get(i);
            if (list.size() > 0) {
                if (res.isEmpty()) {
                    res.addAll(list);
                } else {
                    if (list.size() > 1) {
                        res.addAll(list);
                    }
                }
            }
        }
        return res;
    }

    private static final String CURRENT_DIRECTORY = "Current working directory"; //NOI18N
    private static final String ENTERING_DIRECTORY = "Entering directory"; //NOI18N
    private static final String LEAVING_DIRECTORY = "Leaving directory"; //NOI18N

    private boolean checkDirectoryChange(String line) {
        String workDir = null, message = null;

        if (line.startsWith(CURRENT_DIRECTORY)) {
            workDir = convertPath(line.substring(CURRENT_DIRECTORY.length() + 1).trim());
            if (TRACE) {message = "**>> by [" + CURRENT_DIRECTORY + "] ";} //NOI18N
        } else if (line.indexOf(ENTERING_DIRECTORY) >= 0) {
            String dirMessage = line.substring(line.indexOf(ENTERING_DIRECTORY) + ENTERING_DIRECTORY.length() + 1).trim();
            workDir = convertPath(dirMessage.replaceAll("`|'|\"", "")); //NOI18N
            if (TRACE) {message = "**>> by [" + ENTERING_DIRECTORY + "] ";} //NOI18N
            baseWorkingDir = workDir;
            enterMakeStack(workDir, getMakeLevel(line));
        } else if (line.indexOf(LEAVING_DIRECTORY) >= 0) {
            String dirMessage = line.substring(line.indexOf(LEAVING_DIRECTORY) + LEAVING_DIRECTORY.length() + 1).trim();
            workDir = convertPath(dirMessage.replaceAll("`|'|\"", "")); //NOI18N
            if (TRACE) {message = "**>> by [" + LEAVING_DIRECTORY + "] ";} //NOI18N
            int level = getMakeLevel(line);
            if (leaveMakeStack(workDir, level)){
                List<String> paths = getMakeTop(level);
                if (paths.size()== 1) {
                    baseWorkingDir = paths.get(0);
                } else {
                    // TODO: make is performed in several threads
                    // algorithm should have guessing to select needed top of stack
                    //System.err.println("");
                }
            } else {
                // This is root or error
                //System.err.println("");
            }
        } else if (line.startsWith(LABEL_CD)) {
            int end = line.indexOf(MAKE_DELIMITER);
            workDir = convertPath((end == -1 ? line : line.substring(0, end)).substring(LABEL_CD.length()).trim());
            if (TRACE) {message = "**>> by [ " + LABEL_CD + "] ";} //NOI18N
            if (workDir.startsWith("/")){ // NOI18N
                baseWorkingDir = workDir;
            }
        } else if (line.startsWith("/") && line.indexOf(" ") < 0) {  //NOI18N
            workDir = convertPath(line.trim());
            if (TRACE) {message = "**>> by [just path string] ";} //NOI18N
        }

        if (workDir == null || workDir.length() == 0) {
            return false;
        }

        if (Utilities.isWindows() && workDir.startsWith("/cygdrive/") && workDir.length()>11){ // NOI18N
            workDir = ""+workDir.charAt(10)+":"+workDir.substring(11); // NOI18N
        }

        if (workDir.charAt(0) == '/' || workDir.charAt(0) == '\\' || (workDir.length() > 1 && workDir.charAt(1) == ':')) {
            if ((new File(workDir).exists())) {
                if (TRACE) {System.err.print(message);}
                setWorkingDir(workDir);
                return true;
            } else {
                String netFile = fixNetHost(workDir);
                if (netFile != null) {
                    setWorkingDir(netFile);
                }
            }
        }
        String dir = workingDir + File.separator + workDir;
        if (new File(dir).exists()) {
            if (TRACE) {System.err.print(message);}
            setWorkingDir(dir);
            return true;
        }
        if (Utilities.isWindows() && workDir.length()>3 &&
            workDir.charAt(0)=='/' &&
            workDir.charAt(2)=='/'){
            String d = ""+workDir.charAt(1)+":"+workDir.substring(2); // NOI18N
            if (new File(d).exists()) {
                if (TRACE) {System.err.print(message);}
                setWorkingDir(d);
                return true;
            }
        }
        if (baseWorkingDir != null) {
            dir = baseWorkingDir + File.separator + workDir;
            if (new File(dir).exists()) {
                if (TRACE) {System.err.print(message);}
                setWorkingDir(dir);
                return true;
            }
        }
        return false;
    }

    private String fixNetHost(String dir) {
        if (root.startsWith("/net/")) { // NOI18N
            int i = root.indexOf('/', 5);
            if (i > 0) {
                String localPath = root.substring(i);
                String prefix = root.substring(0,i);
                if (dir.startsWith(localPath)) {
                    String netFile = prefix + dir;
                    if (new File(netFile).exists()) {
                        return netFile;
                    }
                }
            }
        }
        return null;
    }

    /*package-local*/ enum CompilerType {
        CPP, C, FORTRAN, UNKNOWN;
    };

    /*package-local*/ static class LineInfo {
        public String compileLine;
        public String compiler;
        public CompilerType compilerType = CompilerType.UNKNOWN;

        LineInfo(String line) {
            compileLine = line;
        }

        ItemProperties.LanguageKind getLanguage() {
            switch (compilerType) {
                case C:
                    return ItemProperties.LanguageKind.C;
                case CPP:
                    return ItemProperties.LanguageKind.CPP;
                case FORTRAN:
                    return ItemProperties.LanguageKind.Fortran;
                case UNKNOWN:
                default:
                    return ItemProperties.LanguageKind.Unknown;
            }
        }
    }

    private static final String LABEL_CD        = "cd "; //NOI18N
    private static final String INVOKE_GNU_C    = "gcc "; //NOI18N
    private static final String INVOKE_GNU_C2   = "gcc.exe "; //NOI18N
    private static final String INVOKE_SUN_C    = "cc "; //NOI18N
    //private static final String INVOKE_GNU_XC = "xgcc "; //NOI18N
    private static final String INVOKE_GNU_Cpp  = "g++ "; //NOI18N
    private static final String INVOKE_GNU_Cpp2 = "g++.exe "; //NOI18N
    private static final String INVOKE_GNU_Cpp3 = "c++ "; //NOI18N
    private static final String INVOKE_GNU_Cpp4 = "c++.exe "; //NOI18N
    private static final String INVOKE_SUN_Cpp  = "CC "; //NOI18N
    private static final String INVOKE_MSVC_Cpp = "cl "; //NOI18N

// Gnu: gfortran,g95,g90,g77
    private static final String INVOKE_GNU_Fortran1 = "gfortran "; //NOI18N
    private static final String INVOKE_GNU_Fortran2 = "gfortran.exe "; //NOI18N
    private static final String INVOKE_GNU_Fortran3 = "g95.exe "; //NOI18N
    private static final String INVOKE_GNU_Fortran4 = "g90.exe "; //NOI18N
    private static final String INVOKE_GNU_Fortran5 = "g77.exe "; //NOI18N
// common for gnu and sun ? prefer gnu family 
    private static final String INVOKE_GNU_Fortran6 = "g95 "; //NOI18N
    private static final String INVOKE_GNU_Fortran7 = "g90 "; //NOI18N
    private static final String INVOKE_GNU_Fortran8 = "g77 "; //NOI18N
// Sun: ffortran,f95,f90,f77
    private static final String INVOKE_SUN_Fortran  = "ffortran "; //NOI18N
    private static final String INVOKE_SUN_Fortran1 = "f95 "; //NOI18N
    private static final String INVOKE_SUN_Fortran2 = "f90 "; //NOI18N
    private static final String INVOKE_SUN_Fortran3 = "f77 "; //NOI18N
    private static final String MAKE_DELIMITER  = ";"; //NOI18N

    private static int[] foundCompiler(String line, String ... patterns){
        for(String pattern : patterns)    {
            int start = line.indexOf(pattern);
            if (start >=0) {
                char prev = ' ';
                if (start > 0) {
                    prev = line.charAt(start-1);
                }
                if (prev == ' ' || prev == '\t' || prev == '/' || prev == '\\' ) {
                    int end = start + pattern.length();
                    return new int[]{start,end};
                }
            }
        }
        return null;
    }

    /*package-local*/ static LineInfo testCompilerInvocation(String line) {
        LineInfo li = new LineInfo(line);
        int start = 0, end = -1;
        if (li.compilerType == CompilerType.UNKNOWN) {
            //TODO: can fail on gcc calls with -shared-libgcc
            int[] res = foundCompiler(line, INVOKE_GNU_C, INVOKE_GNU_C2 /*, INVOKE_GNU_XC*/);
            if (res != null) {
                start = res[0];
                end = res[1];
                li.compilerType = CompilerType.C;
                li.compiler = "gcc"; // NOI18N
            }
        }
        if (li.compilerType == CompilerType.UNKNOWN) {
            int[] res = foundCompiler(line, INVOKE_GNU_Cpp,INVOKE_GNU_Cpp2,INVOKE_GNU_Cpp3,INVOKE_GNU_Cpp4);
            if (res != null) {
                start = res[0];
                end = res[1];
                li.compilerType = CompilerType.CPP;
                li.compiler = "g++"; // NOI18N
            }
        }
        if (li.compilerType == CompilerType.UNKNOWN) {
            int[] res = foundCompiler(line, INVOKE_SUN_C);
            if (res != null) {
                start = res[0];
                end = res[1];
                li.compilerType = CompilerType.C;
                li.compiler = "cc"; // NOI18N
            }
        }
        if (li.compilerType == CompilerType.UNKNOWN) {
            int[] res = foundCompiler(line, INVOKE_SUN_Cpp);
            if (res != null) {
                start = res[0];
                end = res[1];
                li.compilerType = CompilerType.CPP;
                li.compiler = "CC"; // NOI18N
            }
        }
        if (li.compilerType == CompilerType.UNKNOWN) {
            int[] res = foundCompiler(line, INVOKE_GNU_Fortran1,INVOKE_GNU_Fortran2,INVOKE_GNU_Fortran3,INVOKE_GNU_Fortran4,INVOKE_GNU_Fortran5,INVOKE_GNU_Fortran6,INVOKE_GNU_Fortran7,INVOKE_GNU_Fortran8);
            if (res != null) {
                start = res[0];
                end = res[1];
                li.compilerType = CompilerType.FORTRAN;
                li.compiler = "gfortran"; // NOI18N
            }
        }
        if (li.compilerType == CompilerType.UNKNOWN) {
            int[] res = foundCompiler(line, INVOKE_SUN_Fortran,INVOKE_SUN_Fortran1,INVOKE_SUN_Fortran2,INVOKE_SUN_Fortran3);
            if (res != null) {
                start = res[0];
                end = res[1];
                li.compilerType = CompilerType.FORTRAN;
                li.compiler = "ffortran"; // NOI18N
            }
        }
        if (li.compilerType == CompilerType.UNKNOWN) {
            int[] res = foundCompiler(line, INVOKE_MSVC_Cpp);
            if (res != null) {
                start = res[0];
                end = res[1];
                li.compilerType = CompilerType.CPP;
                li.compiler = "cl"; // NOI18N
            }
        }

        if (li.compilerType != CompilerType.UNKNOWN) {
            li.compileLine = line.substring(start);
            while(end < line.length() && (line.charAt(end) == ' ' || line.charAt(end) == '\t')){
                end++;
            }
            if (end >= line.length() || line.charAt(end)!='-') {
                // suspected compiler invocation has no options or a part of a path?? -- noway
                li.compilerType =  CompilerType.UNKNOWN;
//            } else if (start > 0 && line.charAt(start-1)!='/') {
//                // suspected compiler invocation is not first command in line?? -- noway
//                String prefix = line.substring(0, start - 1).trim();
//                // wait! maybe it's called in condition?
//                if (!(line.charAt(start - 1) == ' ' &&
//                        ( prefix.equals("if") || prefix.equals("then") || prefix.equals("else") ))) { //NOI18N
//                    // or it's a lib compiled by libtool?
//                    int ltStart = line.substring(0, start).indexOf("libtool"); //NOI18N
//                        if (!(ltStart >= 0 && line.substring(ltStart, start).indexOf("compile") >= 0)) { //NOI18N
//                            // no, it's not a compile line
//                            li.compilerType = CompilerType.UNKNOWN;
//                            // I hope
//                            if (TRACE) {System.err.println("Suspicious line: " + line);}
//                        }
//                    }
            }
        }
        return li;
    }

    private void setWorkingDir(String workingDir) {
        if (TRACE) {System.err.println("**>> new working dir: " + workingDir);}
        this.workingDir = CndFileUtils.normalizeFile(new File(workingDir)).getAbsolutePath();
    }

    private void setGuessWorkingDir(String workingDir) {
        if (TRACE) {System.err.println("**>> alternative guess working dir: " + workingDir);}
        this.guessWorkingDir = CndFileUtils.normalizeFile(new File(workingDir)).getAbsolutePath();
    }

    private boolean parseLine(String line, CompileLineStorage storage){
       if (checkDirectoryChange(line)) {
           return false;
       }
       if (workingDir == null) {
           return false;
       }
       if (!workingDir.startsWith(root)){
           return false;
       }

       LineInfo li = testCompilerInvocation(line);
       if (li.compilerType != CompilerType.UNKNOWN) {
           gatherLine(li, storage);
           return true;
       }
       return false;
    }

    private static final String PKG_CONFIG_PATTERN = "pkg-config "; //NOI18N
    private static final String ECHO_PATTERN = "echo "; //NOI18N
    /*package-local*/ static String trimBackApostropheCalls(String line, PkgConfig pkgConfig) {
        int i = line.indexOf('`'); //NOI18N
        if (line.lastIndexOf('`') == i) {  //NOI18N // do not trim unclosed `quotes`
            return line;
        }
        if (i < 0 || i == line.length() - 1) {
            return line;
        } else {
            StringBuilder out = new StringBuilder();
            if (i > 0) {
                out.append(line.substring(0, i));
            }
            line = line.substring(i+1);
            int j = line.indexOf('`'); //NOI18N
            if (j < 0) {
                return line;
            }
            String pkg = line.substring(0,j);
            if (pkg.startsWith(PKG_CONFIG_PATTERN)) { //NOI18N
                pkg = pkg.substring(PKG_CONFIG_PATTERN.length());
                StringTokenizer st = new StringTokenizer(pkg);
                boolean readFlags = false;
                String findPkg = null;
                while(st.hasMoreTokens()) {
                    String aPkg = st.nextToken();
                    if (aPkg.equals("--cflags")) { //NOI18N
                        readFlags = true;
                        continue;
                    }
                    if (aPkg.startsWith("-")) { //NOI18N
                        readFlags = false;
                        continue;
                    }
                    findPkg = aPkg;
                }
                if (readFlags && pkgConfig != null && findPkg != null) {
                    PackageConfiguration pc = pkgConfig.getPkgConfig(findPkg);
                    if (pc != null) {
                        for(String p : pc.getIncludePaths()){
                            out.append(" -I").append(p); //NOI18N
                        }
                        for(String p : pc.getMacros()){
                            out.append(" -D").append(p); //NOI18N
                        }
                        out.append(" "); //NOI18N
                    }
                }
            } else if (pkg.startsWith(ECHO_PATTERN)) {
                pkg = pkg.substring(ECHO_PATTERN.length());
                if (pkg.startsWith("'") && pkg.endsWith("'")) { //NOI18N
                    out.append(pkg.substring(1, pkg.length()-1));
                } else {
                    StringTokenizer st = new StringTokenizer(pkg);
                    boolean first = true;
                    if (st.hasMoreTokens()) {
                        if (!first) {
                            out.append(" "); //NOI18N
                        }
                        first = false;
                        out.append(st.nextToken());
                    }
                }
            } else if (pkg.contains(ECHO_PATTERN)) {
                pkg = pkg.substring(pkg.indexOf(ECHO_PATTERN)+ECHO_PATTERN.length());
                if (pkg.startsWith("'") && pkg.endsWith("'")) { //NOI18N
                    out.append(pkg.substring(1, pkg.length()-1)); //NOI18N
                } else {
                    StringTokenizer st = new StringTokenizer(pkg);
                    boolean first = true;
                    if (st.hasMoreTokens()) {
                        if (!first) {
                            out.append(" "); //NOI18N
                        }
                        first = false;
                        out.append(st.nextToken());
                    }
                }
            }
            out.append(line.substring(j+1));
            return trimBackApostropheCalls(out.toString(), pkgConfig);
        }
    }

    private void gatherLine(LineInfo li, CompileLineStorage storage) {
        String line = li.compileLine;
        List<String> userIncludes = new ArrayList<String>();
        Map<String, String> userMacros = new HashMap<String, String>();
        List<String> languageArtifacts = new ArrayList<String>();
        List<String> sourcesList = DiscoveryUtils.gatherCompilerLine(line, true, userIncludes, userMacros, null, languageArtifacts);
        for(String what : sourcesList) {
            if (what == null){
                continue;
            }
            if (what.endsWith(".s") || what.endsWith(".S")) {  //NOI18N
                // It seems assembler file was compiled by C compiler.
                // Exclude assembler files from C/C++ code model.
                continue;
            }
            String file = null;
            if (what.startsWith("/")){  //NOI18N
                file = what;
            } else {
                file = workingDir+"/"+what;  //NOI18N
            }
            List<String> userIncludesCached = new ArrayList<String>(userIncludes.size());
            for(String s : userIncludes){
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
            if (f.exists() && f.isFile()) {
                if (TRACE) {System.err.println("**** Gotcha: " + file);}
                result.add(new CommandLineSource(li, languageArtifacts, workingDir, what, userIncludesCached, userMacrosCached, storage));
                continue;
            }
            if (guessWorkingDir != null && !what.startsWith("/")) { //NOI18N
                f = new File(guessWorkingDir+"/"+what);  //NOI18N
                if (f.exists() && f.isFile()) {
                    if (TRACE) {System.err.println("**** Gotcha guess: " + file);}
                    result.add(new CommandLineSource(li, languageArtifacts, guessWorkingDir, what, userIncludesCached, userMacrosCached, storage));
                    continue;
                }
            }
            if (TRACE)  {System.err.println("**** Not found "+file);} //NOI18N
            if (!what.startsWith("/") && userIncludes.size()+userMacros.size() > 0){  //NOI18N
                List<String> res = findFiles(what);
                if (res == null || res.isEmpty()) {
                    if (TRACE) {System.err.println("** And there is no such file under root");}
                } else {
                    if (res.size() == 1) {
                        result.add(new CommandLineSource(li, languageArtifacts, res.get(0), what, userIncludes, userMacros, storage));
                        if (TRACE) {System.err.println("** Gotcha: " + res.get(0) + File.separator + what);}
                        // kinda adventure but it works
                        setGuessWorkingDir(res.get(0));
                        continue;
                    } else {
                        if (TRACE) {System.err.println("**There are several candidates and I'm not clever enough yet to find correct one.");}
                    }
                }
                if (TRACE) {System.err.println(""+ (line.length() > 120 ? line.substring(0,117) + ">>>" : line) + " [" + what + "]");} //NOI18N
            }
        }
    }

    static class CommandLineSource implements SourceFileProperties {

        private String compilePath;
        private String sourceName;
        private String fullName;
        private String compiler;
        private ItemProperties.LanguageKind language;
        private List<String> userIncludes;
        private List<String> systemIncludes = Collections.<String>emptyList();
        private Map<String, String> userMacros;
        private Map<String, String> systemMacros = Collections.<String, String>emptyMap();
        private Set<String> includedFiles = Collections.<String>emptySet();
        private CompileLineStorage storage;
        private int handler = -1;

        CommandLineSource(LineInfo li, List<String> languageArtifacts, String compilePath, String sourcePath,
                List<String> userIncludes, Map<String, String> userMacros, CompileLineStorage storage) {
            language = li.getLanguage();
            if (languageArtifacts.contains("c")) { // NOI18N
                language = ItemProperties.LanguageKind.C;
            } else if (languageArtifacts.contains("c++")) { // NOI18N
                language = ItemProperties.LanguageKind.CPP;
            } else {
                String mime =MIMESupport.getKnownSourceFileMIMETypeByExtension(sourcePath);
                if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
                    if (li.getLanguage() != ItemProperties.LanguageKind.CPP) {
                        language = ItemProperties.LanguageKind.CPP;
                    }
                } else if (MIMENames.C_MIME_TYPE.equals(mime)) {
                    if (li.getLanguage() != ItemProperties.LanguageKind.C) {
                        language = ItemProperties.LanguageKind.C;
                    }
                }
            }
            this.compiler = li.compiler;
            this.compilePath =compilePath;
            sourceName = sourcePath;
            if (sourceName.startsWith("/")) { // NOI18N
                fullName = sourceName;
                sourceName = DiscoveryUtils.getRelativePath(compilePath, sourceName);
            } else {
                fullName = compilePath+"/"+sourceName; //NOI18N
            }
            File file = new File(fullName);
            fullName = CndFileUtils.normalizeFile(file).getAbsolutePath();
            fullName = PathCache.getString(fullName);
            this.userIncludes = userIncludes;
            this.userMacros = userMacros;
            this.storage = storage;
            if (storage != null) {
                handler = storage.putCompileLine(li.compileLine);
            }
        }

        @Override
        public String getCompilePath() {
            return compilePath;
        }

        @Override
        public String getItemPath() {
            return fullName;
        }


        @Override
        public String getCompileLine() {
            if (storage != null && handler != -1) {
                return storage.getCompileLine(handler);
            }
            return null;
        }

        @Override
        public String getItemName() {
            return sourceName;
        }

        @Override
        public List<String> getUserInludePaths() {
            return userIncludes;
        }

        @Override
        public List<String> getSystemInludePaths() {
            return systemIncludes;
        }

        public Set<String> getIncludedFiles() {
            return includedFiles;
        }

        @Override
        public Map<String, String> getUserMacros() {
            return userMacros;
        }

        @Override
        public Map<String, String> getSystemMacros() {
            return systemMacros;
        }

        @Override
        public ItemProperties.LanguageKind getLanguageKind() {
            return language;
        }

        @Override
        public String getCompilerName() {
            return compiler;
        }

        @Override
        public LanguageStandard getLanguageStandard() {
            return LanguageStandard.Unknown;
        }
    }

    // java -cp main/nbbuild/netbeans/cnd/modules/org-netbeans-modules-cnd-dwarfdiscovery.jar:main/nbbuild/netbeans/cnd/modules/org-netbeans-modules-cnd-discovery.jar:main/nbbuild/netbeans/cnd/modules/org-netbeans-modules-cnd-apt.jar:main/nbbuild/netbeans/cnd/modules/org-netbeans-modules-cnd-utils.jar:main/nbbuild/netbeans/platform/core/org-openide-filesystems.jar:main/nbbuild/netbeans/platform/lib/org-openide-util.jar org.netbeans.modules.cnd.dwarfdiscovery.provider.LogReader filename root
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Not enough parameters. Format: bla-bla-bla filename root");
            return;
        }
        String objFileName = args[0];
        String root = args[1];
        LogReader.TRACE = true;
        LogReader clrf = new LogReader(objFileName, root, null);
        List<SourceFileProperties> list = clrf.getResults(null, new AtomicBoolean(false), null);
        System.err.print("\n*** Results: ");
        for (SourceFileProperties sourceFileProperties : list) {
            String fileName = sourceFileProperties.getItemName();
            while (fileName.indexOf("../") == 0) { //NOI18N
                fileName = fileName.substring(3);
            }
            System.err.print(fileName + " ");
        }
        System.err.println();
    }

    private List<String> getFiles(String name){
        getSubfolders();
        return findBase.get(name);
    }

    private List<String> findFiles(String relativePath) {
        relativePath = relativePath.replace('\\', '/');
        int i = relativePath.lastIndexOf('/');
        String name;
        String relativeFolder = null;
        if (i > 0) {
            name = relativePath.substring(i+1);
            relativeFolder = relativePath.substring(0,i);
        } else {
            name = relativePath;
        }
        String subFolder = null;
        if (relativeFolder != null) {
            int j = relativeFolder.lastIndexOf("../"); // NOI18N
            if (j >= 0) {
                subFolder = relativePath.substring(j+2);
            }
        }
        List<String> files = getFiles(name);
        if (files != null) {
            List<String> res = new ArrayList<String>(files.size());
            for(String s : files) {
                if (relativeFolder == null) {
                    res.add(s);
                    if (res.size() > 1) {
                        return res;
                    }
                } else {
                    if (subFolder == null) {
                        String path = s;
                        if (path.endsWith(relativeFolder)) {
                            path = path.substring(0,path.length()-relativeFolder.length()-1);
                            res.add(path);
                            if (res.size() > 1) {
                                return res;
                            }
                        }
                    } else {
                        for(String sub : getSubfolders()) {
                            String pathCandidate = normalizeFile(sub + "/" + relativePath); // NOI18N
                            int j = pathCandidate.lastIndexOf('/');
                            if (j > 0) {
                                 pathCandidate = pathCandidate.substring(0,j);
                                if (subFolders.contains(pathCandidate)){
                                    res.add(sub);
                                    if (res.size() > 1) {
                                        return res;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return res;
        }
        return null;
    }

    private String normalizeFile(String path) {
        path = path.replace("/./", "/"); // NOI18N
        while (true) {
            int i = path.indexOf("/../"); // NOI18N
            if (i < 0) {
                break;
            }
            int prev = -1;
            for (int j = i - 1; j >= 0; j--) {
                if (path.charAt(j) == '/') {
                    prev = j;
                    break;
                }
            }
            if (prev == -1) {
                break;
            }
            path = path.substring(0, prev)+path.substring(i+3);
        }
        return path;
    }

    private Set<String> getSubfolders(){
        if (subFolders == null){
            subFolders = new HashSet<String>();
            File f = new File(root);
            gatherSubFolders(f, new HashSet<String>());
            findBase = new HashMap<String,List<String>>();
            initSearchMap();
        }
        return subFolders;
    }
    private HashSet<String> subFolders;
    private Map<String,List<String>> findBase;

    private void gatherSubFolders(File d, HashSet<String> antiLoop){
        if (d.exists() && d.isDirectory() && d.canRead()){
            if (CndPathUtilitities.isIgnoredFolder(d)){
                return;
            }
            String canPath;
            try {
                canPath = d.getCanonicalPath();
            } catch (IOException ex) {
                return;
            }
            if (!antiLoop.contains(canPath)){
                antiLoop.add(canPath);
                subFolders.add(d.getAbsolutePath().replace('\\', '/'));
                File[] ff = d.listFiles();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (ff[i].isDirectory()) {
                            gatherSubFolders(ff[i], antiLoop);
                        }
                    }
                }
            }
        }
    }

    private void initSearchMap(){
        for (String it : subFolders){
            File d = new File(it);
            if (d.exists() && d.isDirectory() && d.canRead()){
                File[] ff = d.listFiles();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (ff[i].isFile()) {
                            List<String> l = findBase.get(ff[i].getName());
                            if (l==null){
                                l = new ArrayList<String>();
                                findBase.put(ff[i].getName(),l);
                            }
                            l.add(d.getAbsolutePath().replace('\\', '/'));
                        }
                    }
                }
            }
        }
    }

}
