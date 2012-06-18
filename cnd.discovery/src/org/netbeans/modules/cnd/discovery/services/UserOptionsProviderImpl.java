/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import org.netbeans.modules.cnd.api.project.NativeFileItem.LanguageFlavor;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.PredefinedMacro;
import org.netbeans.modules.cnd.discovery.api.QtInfoProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.ResolvedPath;
import org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.openide.util.CharSequences;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider.class)
public class UserOptionsProviderImpl implements UserOptionsProvider {
    private final Map<String,PkgConfig> pkgConfigs = new HashMap<String,PkgConfig>();
    private final Map<ExecutionEnvironment, Map<String,PackageConfiguration>> commandCache = new WeakHashMap<ExecutionEnvironment, Map<String,PackageConfiguration>>();

    public UserOptionsProviderImpl(){
    }

    @Override
    public List<String> getItemUserIncludePaths(List<String> includes, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
        List<String> res =new ArrayList<String>(includes);
        if (makeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE){
            for(PackageConfiguration pc : getPackages(compilerOptions.getAllOptions(compiler), makeConfiguration)) {
                for (String path : pc.getIncludePaths()) {
                    res.add(path);
                    
                }
            }
        }
        if (makeConfiguration.isQmakeConfiguration()) {
            res.addAll(QtInfoProvider.getDefault().getQtIncludeDirectories(makeConfiguration));
        }
        return res;
    }

    private ExecutionEnvironment getExecutionEnvironment(MakeConfiguration makeConfiguration) {
        return makeConfiguration.getDevelopmentHost().getExecutionEnvironment();
    }
    
    @Override
    public List<String> getItemUserMacros(List<String> macros, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
        List<String> res =new ArrayList<String>(macros);
        if (makeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE){
            String options = compilerOptions.getAllOptions(compiler);
            for(PackageConfiguration pc : getPackages(options, makeConfiguration)) {
                res.addAll(pc.getMacros());
            }
            convertOptionsToMacros(compiler, options, res);
        }
        return res;
    }

    private void convertOptionsToMacros(AbstractCompiler compiler, String options, List<String> res) {
        if (compiler == null || compiler.getDescriptor() == null) {
            return;
        }
        final List<PredefinedMacro> predefinedMacros = compiler.getDescriptor().getPredefinedMacros();
        if (predefinedMacros == null || predefinedMacros.isEmpty()) {
            return;
        }
        String[] split = options.split(" "); //NOI18N
        for(String s : split) {
            if (s.startsWith("-")) { //NOI18N
                for(ToolchainManager.PredefinedMacro macro : predefinedMacros){
                    if (macro.getFlags() != null && macro.getFlags().equals(s)) {
                        if (!macro.isHidden()) {
                            // add macro
                            res.add(macro.getMacro());
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<String> getItemUserUndefinedMacros(List<String> macros, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
        List<String> res =new ArrayList<String>(macros);
        if (makeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE){
            String options = compilerOptions.getAllOptions(compiler);
            convertOptionsToUndefinedMacros(compiler, options, res);
        }
        return res;
    }

    private void convertOptionsToUndefinedMacros(AbstractCompiler compiler, String options, List<String> res) {
        if (compiler == null || compiler.getDescriptor() == null) {
            return;
        }
        final List<PredefinedMacro> predefinedMacros = compiler.getDescriptor().getPredefinedMacros();
        if (predefinedMacros == null || predefinedMacros.isEmpty()) {
            return;
        }
        String[] split = options.split(" "); //NOI18N
        for(String s : split) {
            if (s.startsWith("-")) { //NOI18N
                for(ToolchainManager.PredefinedMacro macro : predefinedMacros){
                    if (macro.getFlags() != null && macro.getFlags().equals(s)) {
                        if (macro.isHidden()) {
                            res.add(macro.getMacro());
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public LanguageFlavor getLanguageFlavor(AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
        if (makeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE){
            String options = compilerOptions.getAllOptions(compiler);
            if (compiler.getKind() == PredefinedToolKind.CCompiler) {
                if (options.indexOf("-xc99") >= 0) { // NOI18N
                    return LanguageFlavor.C99;
                } else if (options.indexOf("-std=c89") >= 0) { // NOI18N
                    return LanguageFlavor.C89;
                } else if (options.indexOf("-std=c99") >= 0) { // NOI18N
                    return LanguageFlavor.C99;
                }
            } else if (compiler.getKind() == PredefinedToolKind.CCCompiler) {
                if (options.indexOf("-std=c++0x") >= 0 || // NOI18N
                        options.indexOf("-std=c++11") >= 0 || // NOI18N
                        options.indexOf("-std=gnu++0x") >= 0 || // NOI18N
                        options.indexOf("-std=gnu++11") >= 0) { // NOI18N
                    return LanguageFlavor.CPP11;
                } else {
                    return LanguageFlavor.CPP;
                }
            } else if (compiler.getKind() == PredefinedToolKind.FortranCompiler) {
                // TODO
            }
        } 
        return LanguageFlavor.UNKNOWN;
    }
    
    private List<PackageConfiguration> getPackages(String s, MakeConfiguration conf){
        List<PackageConfiguration> res = new ArrayList<PackageConfiguration>();
        while(true){
            int i = s.indexOf('`'); // NOI18N
            if (i >= 0) {
                String pkg = s.substring(i+1);
                int j = pkg.indexOf('`'); // NOI18N
                if (j > 0) {
                    final String executable = pkg.substring(0, j);
                    s = s.substring(i+executable.length()+2);
                    if (executable.startsWith("pkg-config ")) { //NOI18N
                        PackageConfiguration config = getPkgConfigOutput(conf, executable);
                        if (config != null){
                            res.add(config);
                        }
                    } else {
                        PackageConfiguration config = getCommandOutput(conf, executable);
                        if (config != null) {
                            res.add(config);
                        }
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return res;
    }

    private PkgConfig getPkgConfig(ExecutionEnvironment env){
        String hostKey = ExecutionEnvironmentFactory.toUniqueID(env);
        PkgConfig pkg;
        synchronized(pkgConfigs){
            pkg = pkgConfigs.get(hostKey);
            if (pkg == null) {
                pkg = PkgConfigManager.getDefault().getPkgConfig(env);
                pkgConfigs.put(hostKey, pkg);
            }
        }
        return pkg;
    }

    @Override
    public NativeFileSearch getPackageFileSearch(ExecutionEnvironment env) {
        final PkgConfig pkg = getPkgConfig(env);
        if (pkg != null) {
            return new NativeFileSearch() {
                @Override
                public Collection<CharSequence> searchFile(NativeProject project, String fileName) {
                    Collection<ResolvedPath> resolvedPath = pkg.getResolvedPath(fileName);
                    ArrayList<CharSequence> res = new ArrayList<CharSequence>(1);
                    if (resolvedPath != null) {
                        for(ResolvedPath path : resolvedPath) {
                            res.add(CharSequences.create(path.getIncludePath()+File.separator+fileName));
                        }
                    }
                    return res;
                }
            };
        }
        return null;
    }

    private PackageConfiguration getPkgConfigOutput(MakeConfiguration conf, String executable){
        String pkg = executable.substring(11).trim();
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
        if (readFlags && findPkg != null) {
            PkgConfig configs = getPkgConfig(getExecutionEnvironment(conf));
            PackageConfiguration config = configs.getPkgConfig(findPkg);
            if (config != null){
                return config;
            }
        }
        return null;
    }

    private synchronized PackageConfiguration getCommandOutput(MakeConfiguration conf, String command) {
        ExecutionEnvironment env = getExecutionEnvironment(conf);
        Map<String, PackageConfiguration> map = commandCache.get(env);
        if (map == null) {
            map = new HashMap<String, PackageConfiguration>();
            commandCache.put(env, map);
        }
        if (map.containsKey(command)) {
            return map.get(command);
        }
        ArrayList<String> args = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(command," "); // NOI18N
        String executable = null;
        while(st.hasMoreTokens()) {
            if (executable == null) {
                executable = st.nextToken();
            } else {
                args.add(st.nextToken());
            }
        }
        ExitStatus status = ProcessUtils.executeInDir(conf.getMakefileConfiguration().getAbsBuildCommandWorkingDir(), env, executable, args.toArray(new String[args.size()]));
        final String flags = status.output;
        PackageConfiguration config = null;
        if (flags != null) {
            config = new MyPackageConfiguration(executable, flags);
        }
        map.put(command, config);
        return config;
    }

    private static final class MyPackageConfiguration implements PackageConfiguration {
        private final String executable;
        private final List<String> macros = new ArrayList<String>();
        private final List<String> paths = new ArrayList<String>();

        private MyPackageConfiguration(String executable, String flags) {
            this.executable = executable;
            StringTokenizer st = new StringTokenizer(flags, " "); //NOI18N
            while(st.hasMoreElements()) {
                String t = st.nextToken();
                if (t.startsWith("-I")) { //NOI18N
                    paths.add(t.substring(2));
                } else if (t.startsWith("-D")) { //NOI18N
                    macros.add(t.substring(2));
                }
            }
        }

        @Override
        public String getName() {
            return executable;
        }

        @Override
        public Collection<String> getIncludePaths() {
            return paths;
        }

        @Override
        public Collection<String> getMacros() {
            return macros;
        }

        @Override
        public String getDisplayName() {
            return executable;
        }

        @Override
        public String getLibs() {
            return ""; //NOI18N
        }

        @Override
        public String getVersion() {
            return ""; //NOI18N
        }
    }
}
