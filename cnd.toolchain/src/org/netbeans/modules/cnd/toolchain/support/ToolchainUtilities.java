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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.toolchain.support;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetImpl;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.toolchain.compilerset.APIAccessor;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerAccessorImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetPreferences;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolchainValidator;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.WeakSet;

/**
 *
 */
public final class ToolchainUtilities {


    private ToolchainUtilities() {
    }
   
    private static final Set<ChangeListener> codeAssistanceChanged = new WeakSet<ChangeListener>();

    public static void addCodeAssistanceChangeListener(ChangeListener l) {
        synchronized (codeAssistanceChanged) {
            codeAssistanceChanged.add(l);
        }
    }

    public static void removeCodeAssistanceChangeListener(ChangeListener l) {
        synchronized (codeAssistanceChanged) {
            codeAssistanceChanged.remove(l);
        }
    }

    public static void fireCodeAssistanceChange(CompilerSetManager csm) {
        ChangeEvent ev = new ChangeEvent(csm);
        synchronized (codeAssistanceChanged) {
            for (ChangeListener l : codeAssistanceChanged) {
                l.stateChanged(ev);
            }
        }
    }
        
    public static List<CompilerSet> findRemoteCompilerSets(CompilerSetManager csm, String path) {
        if (!csm.getClass().equals(CompilerSetManagerImpl.class)) {
            throw new UnsupportedOperationException("CompilerSetManager class can not be overriden by clients"); // NOI18N
        }
        return ((CompilerSetManagerImpl) csm).findRemoteCompilerSets(path);
    }

    public static CompilerSet createCopy(CompilerSet cs, ExecutionEnvironment env, CompilerFlavor flavor, String directory, String name, String displayName,
            boolean autoGenerated, boolean keepToolFlavor, String setBuildPath, String setRunPath) {
        if (!cs.getClass().equals(CompilerSetImpl.class)) {
            throw new UnsupportedOperationException("CompilerSet class can not be overriden by clients"); // NOI18N
        }
        return ((CompilerSetImpl) cs).createCopy(env, flavor, directory, name, displayName, autoGenerated, keepToolFlavor, setBuildPath, setRunPath);
    }

    public static CompilerSet initCompilerSet(CompilerSetManager csm, ExecutionEnvironment env, CompilerFlavor flavor, String directory) {
        if (!csm.getClass().equals(CompilerSetManagerImpl.class)) {
            throw new UnsupportedOperationException("CompilerSetManager class can not be overriden by clients"); // NOI18N
        }
        CompilerSetImpl cs = CompilerSetImpl.create(flavor, env, directory);
        cs.setAutoGenerated(false);
        ((CompilerSetManagerImpl) csm).initCompilerSet(cs);
        return cs;
    }

    public static void setCSName(CompilerSet cs, String compilerSetName) {
        if (!cs.getClass().equals(CompilerSetImpl.class)) {
            throw new UnsupportedOperationException("CompilerSet class can not be overriden by clients"); // NOI18N
        }
        ((CompilerSetImpl) cs).setName(compilerSetName);
    }

    public static CompilerSetManager create(ExecutionEnvironment execEnv) {
        return CompilerSetManagerAccessorImpl.create(execEnv);
    }

    public static CompilerSetManager getDeepCopy(ExecutionEnvironment execEnv, boolean initialize) {
        return CompilerSetManagerAccessorImpl.getDeepCopy(execEnv, initialize);
    }
    
    public static CompilerSetManager deepCopy(CompilerSetManager csm) {
        if (!csm.getClass().equals(CompilerSetManagerImpl.class)) {
            throw new UnsupportedOperationException("CompilerSetManager class can not be overriden by clients"); // NOI18N
        }
        return ((CompilerSetManagerImpl)csm).deepCopy();
    }    

    public static void saveCompileSetManagers(Collection<CompilerSetManager> allCSMs, List<ExecutionEnvironment> liveServers) {
        CompilerSetManagerAccessorImpl.setManagers(allCSMs, liveServers);
    }

    public static String getUniqueCompilerSetName(CompilerSetManager csm, String baseName) {
        if (!csm.getClass().equals(CompilerSetManagerImpl.class)) {
            throw new UnsupportedOperationException("CompilerSetManager class can not be overriden by clients"); // NOI18N
        }
        return ((CompilerSetManagerImpl)csm).getUniqueCompilerSetName(baseName);
    }
    
    public static void setModifyBuildPath(CompilerSet cs, String modifyBuildPath) {
        if (!cs.getClass().equals(CompilerSetImpl.class)) {
            throw new UnsupportedOperationException("CompilerSet class can not be overriden by clients"); // NOI18N
        }
        ((CompilerSetImpl) cs).setModifyBuildPath(modifyBuildPath);        
    }
    
    public static void setModifyRunPath(CompilerSet cs, String modifyRunPath) {
        if (!cs.getClass().equals(CompilerSetImpl.class)) {
            throw new UnsupportedOperationException("CompilerSet class can not be overriden by clients"); // NOI18N
        }
        ((CompilerSetImpl) cs).setModifyRunPath(modifyRunPath);        
    }    
    
    public static void setToolPath(Tool tool, String p) {
        APIAccessor.get().setToolPath(tool, p);
    }
    
    public static void setCharset(Charset charset, CompilerSet cs) {
        APIAccessor.get().setCharset(charset, cs);
    }    
    
    public static void fixCSM(final Map<Tool, List<List<String>>> needReset, CompilerSetManager csm) {
        ToolchainValidator.INSTANCE.applyChanges(needReset, csm);
    }
    
    public static boolean isUnsupportedMake(String name) {
        name = CndPathUtilities.getBaseName(name);
        return name.toLowerCase().equals("mingw32-make.exe"); // NOI18N
    }
    
    /**
     * Modify path env variable according tool collection rules.
     * 
     * @param execEnv
     * @param env
     * @param cs tool collection
     * @param type of action. Supported "run" or "build" action.
     * @return 
     */
    public static Pair<String,String> modifyPathEnvVariable(final ExecutionEnvironment execEnv, final Map<String, String> env, final CompilerSet cs, final String type) {
        String macro;
        if ("run".equals(type)) { //NOI18N
            macro = cs.getModifyRunPath();
        } else {
            macro = cs.getModifyBuildPath();
        }
        MacroConverter converter = new MacroConverter(execEnv, env);
        if (converter.isWindows) {
            String commands = CompilerSetUtils.getCommandFolder(cs);
            String baseMinGW = CompilerSetUtils.getMinGWBaseFolder(cs);
            String path = "";
            if (commands != null && !commands.isEmpty()) {
                path = commands;
            }
            if (baseMinGW != null && !baseMinGW.isEmpty()) {
                if (path.isEmpty()) {
                    path = baseMinGW;
                } else {
                    path = path+";"+baseMinGW; //NOI18N
                }
            }
            converter.updateUtilitiesPath(path);
        }
        converter.updateToolPath(cs.getDirectory());
        String expandedPath = converter.expand(macro);
        env.put(converter.pathName, expandedPath);
        return Pair.of(converter.pathName, expandedPath);
    }

    /**
     * Returns original path env variable.
     * 
     * @param execEnv
     * @param env
     * @return 
     */
    public static Pair<String,String> defaultPathEnvVariable(final ExecutionEnvironment execEnv, final Map<String, String> env) {
        String macro = CompilerSetPreferences.DEFAULT_TRIVIAL_PATH;
        MacroConverter converter = new MacroConverter(execEnv, env);
        String expandedPath = converter.expand(macro);
        env.put(converter.pathName, expandedPath);
        return Pair.of(converter.pathName, expandedPath);
    }
    
    private static final class MacroConverter {

        private final MacroExpanderFactory.MacroExpander expander;
        private final Map<String, String> envVariables;
        private String homeDir;
        private String pathName = "PATH"; // NOI18N
        private String pathSeparator = ";"; // NOI18N
        private boolean isWindows = false;

        public MacroConverter(ExecutionEnvironment env, Map<String, String> envVariables) {
            this.envVariables = new HashMap<>(envVariables);
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    this.envVariables.putAll(hostInfo.getEnvironment());
                    homeDir = hostInfo.getUserDir();
                    pathName = getPathName(env, hostInfo);
                    pathSeparator = getPathSeparator(hostInfo);
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    // should never == null occur if isHostInfoAvailable(env) => report
                    Exceptions.printStackTrace(ex);
                }
            }
            this.expander = MacroExpanderFactory.getExpander(env, false);
        }
        
        private String getPathName(ExecutionEnvironment env, HostInfo hostInfo) {
            if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                isWindows = true;
                for (String key : HostInfoProvider.getEnv(env).keySet()) {
                    if (key.toLowerCase(Locale.getDefault()).equals("path")) { // NOI18N
                        return key.substring(0, 4);
                    }
                }
            }
            return "PATH"; // NOI18N
        }

        private String getPathSeparator(HostInfo hostInfo) {
            if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                return ";"; // NOI18N
            }
            return ":"; // NOI18N
        }

        private void updateUtilitiesPath(String utilitiesPath) {
            envVariables.put(CompilerSet.UTILITIES_PATH, utilitiesPath);
        }

        private void updateToolPath(String toolPath) {
            envVariables.put(CompilerSet.TOOLS_PATH, toolPath);
        }

        public String expand(String in) {
            try {
                if (homeDir != null) {
                    if (in.startsWith("~")) { //NOI18N
                        in = homeDir+in.substring(1);
                    }
                    in = in.replace(":~", ":"+homeDir); //NOI18N
                    in = in.replace(";~", ";"+homeDir); //NOI18N
                }
                if (pathName != null) {
                    if (!"PATH".equals(pathName)) { //NOI18N
                        in = in.replace(CompilerSetPreferences.DEFAULT_TRIVIAL_PATH, "${"+pathName+"}"); //NOI18N
                    }
                }
                if (pathSeparator != null) {
                    if (!";".equals(pathSeparator)) { //NOI18N
                        in = in.replace(";", pathSeparator); //NOI18N
                    }
                }
                return expander != null ? expander.expandMacros(in, envVariables) : in;
            } catch (ParseException ex) {
                //nothing to do
            }
            return in;
        }
    }

}
