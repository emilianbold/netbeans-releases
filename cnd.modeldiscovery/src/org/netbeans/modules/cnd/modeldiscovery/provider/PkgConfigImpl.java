/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modeldiscovery.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.remote.RemoteFile;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.discovery.api.PkgConfigManager.ResolvedPath;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;

/**
 *
 * @author Alexander Simon
 */
public class PkgConfigImpl implements PkgConfig {

    private HashMap<String, PackageConfigurationImpl> configurations = new HashMap<String, PackageConfigurationImpl>();
    private Map<String, List<Pair>> seachBase;
    private String drivePrefix;
    private PlatformInfo pi;

    public PkgConfigImpl(MakeConfiguration mc) {
        // TODO: need to support mc
        if (false && mc != null) {
            pi = mc.getPlatformInfo();
            CompilerSet set = mc.getCompilerSet().getCompilerSet();
            initPackagesFromSet(set);
        } else {
            // otherwise
            pi = PlatformInfo.localhost();
            initPackagesFromSet(null);
        }
    }

    // for test
    PkgConfigImpl(PlatformInfo pi, CompilerSet set) {
        this.pi = pi;
        initPackagesFromSet(set);
    }

    private List<String> envPaths(String folder){
        // TODO: get from remote
        String additionalPaths = System.getenv("PKG_CONFIG_PATH"); // NOI18N
        List<String> res = new ArrayList<String>();
        String prefix = "";
//        ExecutionEnvironment execEnv = pi.getExecutionEnvironment();
//        if (execEnv.isRemote()) {
//            // system files are in local cache
//            prefix = BasicCompiler.getIncludeFilePrefix(execEnv);
//        }
        res.add(prefix + folder);
        if (additionalPaths != null && additionalPaths.length() > 0) {
            StringTokenizer st;
            if (pi.isWindows()){
                st = new StringTokenizer(additionalPaths, ";"); // NOI18N
            } else {
                st = new StringTokenizer(additionalPaths, ":"); // NOI18N
            }
            while(st.hasMoreTokens()) {
                res.add(prefix + st.nextToken());
            }
        }
        return res;
    }

    private void initPackagesFromSet(CompilerSet set) {
        if (pi.isWindows()){
            // at first find pkg-config.exe in paths
            String baseDirectory = getPkgConfihPath();
            if (baseDirectory == null) {
                if (set == null) {
                    for(CompilerSet cs : CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getCompilerSets()) {
                        if (cs.getCompilerFlavor().isCygwinCompiler()) {
                            set = cs;
                            break;
                        }
                    }
                }
                if (set != null){
                    baseDirectory = set.getDirectory();
                    //"C:\cygwin\bin"
                    if (baseDirectory != null && baseDirectory.endsWith("bin")){ // NOI18N
                        drivePrefix = baseDirectory.substring(0, baseDirectory.length()-4);
                        baseDirectory = baseDirectory.substring(0, baseDirectory.length()-3)+"lib/pkgconfig/"; // NOI18N
                    }
                }
                if (baseDirectory == null) {
                    drivePrefix = "c:/cygwin"; // NOI18N
                    baseDirectory = "c:/cygwin/lib/pkgconfig/"; // NOI18N
                }
            } else {
                String suffix = "/lib/pkgconfig/"; // NOI18N
                if (baseDirectory.endsWith(suffix)){
                    drivePrefix = baseDirectory.substring(0, baseDirectory.length()-suffix.length());
                }
            }
            initPackages(envPaths(baseDirectory), true); // NOI18N
        } else {
            //initPackages("/net/elif/export1/sside/as204739/pkgconfig/"); // NOI18N
            initPackages(envPaths("/usr/lib/pkgconfig/"), false); // NOI18N
        }
    }

    private String getPkgConfihPath(){
        for(String path : Path.getPath()){
            File file = new File(path+File.separator+"pkg-config.exe"); // NOI18N
            if (file.exists()) {
                path = path.replace('\\', '/'); // NOI18N
                if (path.endsWith("/")){ // NOI18N
                    path = path.substring(0, path.length()-1);
                }
                int i = path.lastIndexOf('/'); // NOI18N
                if (i > 0){
                    path = path.substring(0, i + 1 )+"lib/pkgconfig/"; // NOI18N
                    return path;
                }
                return null;
            }
        }
        return null;
    }

    private void initPackages(List<String> folders, boolean isWindows) {
        Set<File> done = new HashSet<File>();
        for(String folder:folders) {
            File file = RemoteFile.create(pi.getExecutionEnvironment(), folder);
            if (done.contains(file)) {
                continue;
            }
            done.add(file);
            if (file.exists() && file.isDirectory() && file.canRead()) {
                for (File fpc : file.listFiles()) {
                    String name = fpc.getName();
                    if (name.endsWith(".pc") && fpc.canRead() && fpc.isFile()) { // NOI18N
                        String pkgName = name.substring(0, name.length()-3);
                        PackageConfigurationImpl pc = new PackageConfigurationImpl(pkgName);
                        readConfig(fpc, pc,  isWindows);
                        configurations.put(pkgName, pc);
                    }
                }
            }
        }
    }

    @Override
    public PackageConfiguration getPkgConfig(String pkg) {
        return getConfig(pkg);
    }

    @Override
    public Collection<ResolvedPath> getResolvedPath(String include) {
        Map<String, List<Pair>> map = getLibraryItems();
        List<Pair> pairs = map.get(include);
        if (pairs != null && pairs.size() > 0){
            ArrayList<ResolvedPath> res = new ArrayList<ResolvedPath>(pairs.size());
            for(Pair p : pairs){
                res.add(new ResolvedPathImpl(p.path, p.configurations));
            }
            return res;
        }
        return null;
    }

    /*package-local*/ void trace(){
        List<String> sort = new ArrayList<String>(configurations.keySet());
        Collections.sort(sort);
        for(String pkg: sort){
            traceConfig(pkg, false);
        }
        Map<String, List<Pair>> res = getLibraryItems();
        System.out.println("Known includes size: "+res.size()); // NOI18N
        sort = new ArrayList<String>(res.keySet());
        Collections.sort(sort);
        for(String key: sort){
            List<Pair> pairs = res.get(key);
            if (pairs != null) {
                for(Pair value : pairs) {
                    StringBuilder buf = new StringBuilder();
                    for(PackageConfiguration pc : value.configurations){
                        if (buf.length()>0){
                            buf.append(", "); // NOI18N
                        }
                        buf.append(pc.getName());
                    }
                    System.out.println(key+"\t"+value.path+"\t["+buf.toString()+"]"); // NOI18N
                }
            }
        }

    }

    /*package-local*/ void traceConfig(String pkg, boolean recursive){
        traceConfig(pkg, recursive, new HashSet<String>(), "");

    }
    private void traceConfig(String pkg, boolean recursive, Set<String> visited, String tab){
        if (visited.contains(pkg)) {
            return;
        }
        visited.add(pkg);
        PackageConfigurationImpl pc = configurations.get(pkg);
        if (pc != null){
            System.out.println(tab+"Package definition"); // NOI18N
            System.out.println(tab+"Name:     "+pkg); // NOI18N
            System.out.println(tab+"Requires: "+pc.requires); // NOI18N
            System.out.println(tab+"Macros:   "+pc.macros); // NOI18N
            System.out.println(tab+"Paths:    "+pc.paths); // NOI18N
            if (recursive) {
                for(String p : pc.requires){
                    traceConfig(p, recursive, visited, tab+"    "); // NOI18N
                }
            }
        } else {
            System.out.println("Not found package definition "+pkg); // NOI18N
        }
    }

    /*package-local*/ void traceRecursiveConfig(String pkg){
        PackageConfiguration pc = getConfig(pkg);
        if (pc != null){
            System.out.println("Recursive package definition"); // NOI18N
            System.out.println("Name:    "+pkg); // NOI18N
            System.out.println("Package: "+pkg); // NOI18N
            System.out.println("Macros:  "+pc.getMacros()); // NOI18N
            System.out.println("Paths:   "+pc.getIncludePaths()); // NOI18N
        }
    }

    private PackageConfiguration getConfig(String pkg){
        PackageConfigurationImpl master = new PackageConfigurationImpl(pkg);
        getConfig(master, configurations.get(pkg));
        return master;
    }

    private void getConfig(PackageConfigurationImpl master, PackageConfigurationImpl pc){
        if (pc != null) {
            for(String m : pc.macros){
                if (!master.macros.contains(m)){
                    master.macros.add(m);
                }
            }
            for(String p : pc.paths){
                if (!master.paths.contains(p)){
                    master.paths.add(p);
                }
            }
            for(String require : pc.requires){
                getConfig(master, configurations.get(require));
            }
        }
    }

    private Map<String, List<Pair>> getLibraryItems(){
        Map<String, List<Pair>> res = null;
        if (seachBase != null) {
            res = seachBase;
        }
        if (res == null) {
            res = _getLibraryItems();
            seachBase = res;
        }
        return res;
    }
    private Map<String, List<Pair>> _getLibraryItems(){
        Map<String, Set<PackageConfiguration>> map = new HashMap<String, Set<PackageConfiguration>>();
        for(String pkg : configurations.keySet()){
            PackageConfigurationImpl pc = configurations.get(pkg);
            if (pc != null){
                for (String p : pc.paths){
                    if (drivePrefix != null) {
                        if (p.substring(drivePrefix.length()).equals("/usr/include") || p.substring(drivePrefix.length()).equals("/usr/sfw/include")){ // NOI18N
                            continue;
                        }
                    } else {
                        if (p.equals("/usr/include") || p.equals("/usr/sfw/include")){ // NOI18N
                            continue;
                        }
                    }
                    Set<PackageConfiguration> set = map.get(p);
                    if (set == null){
                        set = new HashSet<PackageConfiguration>();
                        map.put(p, set);
                    }
                    set.add(pc);
                }
            }
        }
        Map<String, List<Pair>> res = new HashMap<String, List<Pair>>();
        for (Map.Entry<String, Set<PackageConfiguration>> entry : map.entrySet()) {
            Pair pair = new Pair(entry.getKey(), entry.getValue());
            File dir = RemoteFile.create(pi.getExecutionEnvironment(), entry.getKey());
            addLibraryItem(res, pair, "", dir, 0); // NOI18N
        }
        return res;
    }

    private void addLibraryItem(Map<String, List<Pair>> res, Pair pkg, String prefix, File dir, int loop){
        if (loop>2) {
            return;
        }
        if (dir.isDirectory() && dir.canRead()){
            for(File f : dir.listFiles()){
                if (f.canRead()) {
                    if (f.isDirectory()) {
                        if (loop == 0) {
                            addLibraryItem(res, pkg, f.getName(), f, loop+1);// NOI18N
                        } else {
                            addLibraryItem(res, pkg, prefix+"/"+f.getName(), f, loop+1); // NOI18N
                        }
                    } else if (f.isFile()) {
                        String key;
                        if (prefix.length()==0) {
                            key = f.getName();
                        } else {
                            key = prefix+"/"+f.getName(); // NOI18N
                        }
                        List<Pair> list = res.get(key);
                        if (list == null){
                            list = new ArrayList<Pair>(1);
                            res.put(key, list);
                        }
                        if (!list.contains(pkg)){
                            list.add(pkg);
                            //if (list.size() > 1) {
                            //    System.out.println("Name conflict '"+key+"' in packages '"+pkg+"' and '"+list.get(0)+"'"); // NOI18N
                            //}
                        }
                    }
                }
            }
        }
    }

//prefix=/usr
//prefix=${pcfiledir}/../..
//exec_prefix=${prefix}
//libdir=${exec_prefix}/lib
//includedir=${prefix}/include
//target=x11
//
//gtk_binary_version=2.4.0
//gtk_host=i386-pc-solaris2.10
//
//Name: GTK+
//Description: GIMP Tool Kit (${target} target)
//Version: 2.4.9
//Requires: gdk-${target}-2.0 atk
//0123456789
//Libs: -L${libdir} -lgtk-${target}-2.0
//Cflags: -I${includedir}/gtk-2.0

    private void readConfig(File file, PackageConfigurationImpl pc, boolean isWindows) {
        try {
            String rootName = null;
            String rootValue = null;
            Map<String, String> vars = new HashMap<String, String>();
            vars.put("pcfiledir", file.getParent()); // NOI18N
            BufferedReader in = new BufferedReader(RemoteFile.createReader(file));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.startsWith("#")) { // NOI18N
                    continue;
                }
                if (line.startsWith("Requires:")){ // NOI18N
                    String value = line.substring(9).trim();
                    value = expandMacros(value,vars);
                    StringTokenizer st = new StringTokenizer(value, " ,"); // NOI18N
                    while(st.hasMoreTokens()) {
                        String s = st.nextToken();
                        if (s.startsWith("<") || s.startsWith(">") || s.startsWith("=")|| Character.isDigit(s.charAt(0))){ // NOI18N
                            continue;
                        }
                        pc.requires.add(s);
                    }
                } else if (line.startsWith("Requires.private:")){ // NOI18N
                    if (false){
                        // It seems the pkg-config has a bug. It shouln't take into account "Requires.private" for --cflags option.
                        // See bug: https://bugs.freedesktop.org/show_bug.cgi?id=3097#c6
                        String value = line.substring(17).trim();
                        value = expandMacros(value,vars);
                        StringTokenizer st = new StringTokenizer(value, " ,"); // NOI18N
                        while(st.hasMoreTokens()) {
                            String s = st.nextToken();
                            if (s.startsWith("<") || s.startsWith(">") || s.startsWith("=")|| Character.isDigit(s.charAt(0))){ // NOI18N
                                continue;
                            }
                            pc.requires.add(s);
                        }
                    }
                } else if (line.startsWith("Version:")){ // NOI18N
                    pc.version = line.substring(8).trim();
                } else if (line.startsWith("Cflags:")){ // NOI18N
                    String value = line.substring(7).trim();
                    value = expandMacros(value,vars);
                    StringTokenizer st = new StringTokenizer(value, " "); // NOI18N
                    while(st.hasMoreTokens()) {
                        String v = st.nextToken();
                        if (v.startsWith("-I")){ // NOI18N
                            v = v.substring(2);
                            if (isWindows) {
                                if (v.length()>2 && v.charAt(1) == ':') {
                                    if (rootName != null && v.startsWith(rootName)) {
                                        if (rootValue != null) {
                                            v = rootValue+v.substring(rootName.length());
                                        } else if (drivePrefix != null) {
                                            v = drivePrefix+v.substring(rootName.length());
                                        }
                                    }
                                } else {
                                    if (v.startsWith("/usr/lib/")) { // NOI18N
                                        v = v.substring(4);
                                    }
                                    if (rootValue != null) {
                                        v = rootValue+v;
                                    } else if (drivePrefix != null) {
                                        v = drivePrefix+v;
                                    }
                                }
                            }
                            pc.paths.add(v);
                        } else if (v.startsWith("-D")){ // NOI18N
                            pc.macros.add(v.substring(2));
                        }
                    }
                } else if (line.indexOf('=')>0){ // NOI18N
                    int i = line.indexOf('='); // NOI18N
                    String name = line.substring(0, i).trim();
                    String value = line.substring(i+1).trim();
                    if (isWindows && name.equals("prefix")) { // NOI18N
                        rootName = value;
                        rootValue = fixPrefixPath(value, file);
                    }
                    vars.put(name, expandMacros(value, vars));
                }
            }
            in.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String fixPrefixPath(String value, File file){
        //prefix=c:/devel/target/e1cabcfbab6c7ee30ed3ffc781169bba
        StringTokenizer st = new StringTokenizer(value, "\\/"); // NOI18N
        while(st.hasMoreTokens()){
            String s = st.nextToken();
            if (s.length() == 32) {
                boolean isHashCode = true;
                for(int i = 0; i < 32; i++){
                    char c = s.charAt(i);
                    switch(c){
                        case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': // NOI18N
                        case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': // NOI18N
                        case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': // NOI18N
                            continue;
                        default:
                            isHashCode = false;
                            break;
                    }
                }
                if (isHashCode) {
                    file = file.getParentFile();
                    if (file != null) {
                        file = file.getParentFile();
                    }
                    if (file != null) {
                        file = file.getParentFile();
                    }
                    if (file != null) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }
        if (value.startsWith("/")) { // NOI18N
            int i = value.indexOf('/', 1); // NOI18N
            file = file.getParentFile();
            if (file != null) {
                file = file.getParentFile();
            }
            if (file != null) {
                file = file.getParentFile();
            }
            if (file != null) {
                if (i > 0) {
                    return file.getAbsolutePath()+value.substring(i);
                } else {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private String expandMacros(String value, Map<String, String> vars){
        if (value.indexOf("${")>=0) { // NOI18N
            while(value.indexOf("${")>=0) { // NOI18N
                int i = value.indexOf("${"); // NOI18N
                int j = value.indexOf('}'); // NOI18N
                if (j < i) {
                    break;
                }
                String macro = value.substring(i+2, j);
                String v = vars.get(macro);
                if (v == null || v.indexOf("${")>=0) { // NOI18N
                    break;
                }
                value = value.substring(0,i)+v+value.substring(j+1);
            }
        }
        return value;
    }

    /*package-local*/ static class PackageConfigurationImpl implements PackageConfiguration {
        List<String> requires = new ArrayList<String>();
        List<String> macros = new ArrayList<String>();
        List<String> paths = new ArrayList<String>();
        private String name;
        private String version;
        private PackageConfigurationImpl(String name){
            this.name = name;
        }

        @Override
        public Collection<String> getIncludePaths() {
            return new ArrayList<String>(paths);
        }

        @Override
        public Collection<String> getMacros() {
            return new ArrayList<String>(macros);
        }

        @Override
        public String getName() {
            return name;
        }
    }

    /*package-local*/ class ResolvedPathImpl implements ResolvedPath {
        private String path;
        private Set<PackageConfiguration> packages;
        private ResolvedPathImpl(String path, Set<PackageConfiguration> packages){
            this.path = path;
            this.packages = packages;
        }

        @Override
        public String getIncludePath() {
            return path;
        }

        @Override
        public Collection<PackageConfiguration> getPackages() {
            List<PackageConfiguration> res = new ArrayList<PackageConfiguration>(packages.size());
            for(PackageConfiguration pc : packages){
                res.add(getPkgConfig(pc.getName()));
            }
            return res;
        }
    }

    private static class Pair {
        private String path;
        private Set<PackageConfiguration> configurations;
        private Pair(String path, Set<PackageConfiguration> configurations){
            this.path = path;
            this.configurations = configurations;
        }
    }
}
