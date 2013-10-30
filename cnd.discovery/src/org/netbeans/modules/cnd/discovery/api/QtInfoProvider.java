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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils.Artifacts;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 * Utility to find Qt include directories for project configuration.
 *
 * @author Alexey Vladykin
 */
public abstract class QtInfoProvider {

    private static final QtInfoProvider DEFAULT = new Default();
    private static final Logger LOGGER = Logger.getLogger(QtInfoProvider.class.getName());

    private QtInfoProvider() {
    }

    public static QtInfoProvider getDefault() {
        return DEFAULT;
    }

    public abstract List<String> getQtAdditionalMacros(MakeConfiguration conf);

    public abstract List<String> getQtIncludeDirectories(MakeConfiguration conf);

    private static class Default extends QtInfoProvider {

        private final Map<String, Pair<String, String>> cache;

        private Default() {
            cache = new HashMap<String, Pair<String, String>>();
        }

        @Override
        public List<String> getQtAdditionalMacros(MakeConfiguration conf) {
            final String CXXFLAGS = "CXXFLAGS"; //NOI18N
            Map<String, String> vars = new TreeMap<>();
            FileObject projectDir = conf.getBaseFSPath().getFileObject();
            if (projectDir != null && projectDir.isValid()) {
                try {
                    FileObject qtMakeFile = RemoteFileUtil.getFileObject(projectDir, MakeConfiguration.NBPROJECT_FOLDER + "/qt-" + conf.getName() + ".mk"); //NOI18N
                    Project project = ProjectManager.getDefault().findProject(projectDir);
                    if (project != null && qtMakeFile != null && qtMakeFile.isValid()) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(qtMakeFile.getInputStream()))) {
                            String str;
                            while ((str = reader.readLine()) != null) {
                                String[] lines = str.split("="); //NOI18N
                                if (lines.length == 2) {
                                    String key = lines[0].trim();
                                    vars.put(lines[0].trim(), lines[1].trim());
                                    if (key.equals(CXXFLAGS)) {
                                        Artifacts artifacts = new Artifacts();
                                        DiscoveryUtils.gatherCompilerLine(getActualVarValue(vars, CXXFLAGS), DiscoveryUtils.LogOrigin.BuildLog, artifacts, new ProjectBridge(project), true);
                                        List<String> result = new ArrayList<>(artifacts.userMacros.size());
                                        for (Map.Entry<String, String> pair : artifacts.userMacros.entrySet()) {
                                            if (pair.getValue() == null) {
                                                result.add(pair.getKey());
                                            } else {
                                                result.add(pair.getKey() + "=" + pair.getValue()); //NOI18N
                                            }
                                        }
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException | IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return java.util.Collections.EMPTY_LIST;
        }

        private String getActualVarValue(Map<String, String> vars, String var) {
            String result = vars.get(var);
            for (String v : vars.keySet()) {
                result = result.replace("$(" + v + ")", vars.get(v)); //NOI18N
            }
            return result;
        }
        
        /**
         * Finds Qt include directories for given project configuration.
         *
         * @param conf Qt project configuration
         * @return list of include directories, may be empty if qmake is not
         * found
         */
        @Override
        public List<String> getQtIncludeDirectories(MakeConfiguration conf) {
            Pair<String, String> baseDir = getBaseQtIncludeDir(conf);
            List<String> result;
            if (baseDir != null && (baseDir.first() != null || baseDir.second() != null)) {
                result = new ArrayList<String>();
                if (baseDir.first() != null) {
                    result.add(baseDir.first());
                }
                QmakeConfiguration qmakeConfiguration = conf.getQmakeConfiguration();
                if (qmakeConfiguration.isCoreEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "QtCore.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "QtCore"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isWidgetsEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "QtWidgets.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "QtWidgets"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isGuiEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "QtGui.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "QtGui"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isNetworkEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "QtNetwork.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "QtNetwork"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isOpenglEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "QtOpenGL.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "QtOpenGL"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isPhononEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "phonon.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "phonon"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isQt3SupportEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "Qt3Support.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "Qt3Support"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isPrintSupportEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "QtPrintSupport.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "QtPrintSupport"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isSqlEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "QtSql.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "QtSql"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isSvgEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "QtSvg.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "QtSvg"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isXmlEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "QtXml.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "QtXml"); // NOI18N
                    }
                }
                if (qmakeConfiguration.isWebkitEnabled().getValue()) {
                    if (baseDir.second() != null) {
                        result.add(baseDir.second() + File.separator + "QtWebKit.framework/Headers"); // NOI18N
                    }
                    if (baseDir.first() != null) {
                        result.add(baseDir.first() + File.separator + "QtWebKit"); // NOI18N
                    }
                }
                String uiDir = qmakeConfiguration.getUiDir().getValue();
                if (CndPathUtilities.isPathAbsolute(uiDir)) {
                    result.add(uiDir);
                } else {
                    result.add(conf.getBaseDir() + File.separator + uiDir);
                }
            } else {
                result = Collections.emptyList();
            }
            return result;
        }

        private static String getQmakePath(MakeConfiguration conf) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            if (compilerSet != null) {
                Tool qmakeTool = compilerSet.getTool(PredefinedToolKind.QMakeTool);
                if (qmakeTool != null && 0 < qmakeTool.getPath().length()) {
                    return qmakeTool.getPath();
                }
            }
            return "qmake"; // NOI18N
        }

        private static String getCacheKey(MakeConfiguration conf) {
            return conf.getDevelopmentHost().getHostKey() + '/' + getQmakePath(conf); // NOI18N
        }

        private Pair<String, String> getBaseQtIncludeDir(MakeConfiguration conf) {
            String cacheKey = getCacheKey(conf);
            Pair<String, String> baseDir;
            synchronized (cache) {
                if (cache.containsKey(cacheKey)) {
                    baseDir = cache.get(cacheKey);
                } else {
                    ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
                    String qmakePath = getQmakePath(conf);
                    if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                        boolean isMac = false;
                        try {
                            isMac = HostInfoUtils.getHostInfo(execEnv).getOSFamily() == HostInfo.OSFamily.MACOSX;
                        } catch (IOException | ConnectionManager.CancellationException ex) {
                            ex.printStackTrace(System.err);
                        }
                        String baseInc = queryBaseQtIncludeDir(execEnv, qmakePath);
                        String baseLib = null;
                        if (isMac) {
                            baseLib = queryBaseQtLibsDir(execEnv, qmakePath);
                        }
                        baseDir = Pair.of(baseInc, baseLib);
                        cache.put(cacheKey, baseDir);
                    } else {
                        String baseInc = guessBaseQtIncludeDir(qmakePath);
                        String baseLib = null;
                        baseDir = Pair.of(baseInc, baseLib);
                        // do not cache this result, so that we can
                        // really query qmake once connection is up
                    }
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Qt include dir for {0} = {1}", new Object[]{cacheKey, baseDir});
            }
            return baseDir;
        }

        private static String queryBaseQtIncludeDir(ExecutionEnvironment execEnv, String qmakePath) {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(qmakePath);
            npb.setArguments("-query", "QT_INSTALL_HEADERS"); // NOI18N
            try {
                NativeProcess process = npb.call();
                String output = ProcessUtils.readProcessOutputLine(process).trim();
                if (process.waitFor() == 0 && 0 < output.length()) {
                    return output;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            return null;
        }

        private static String queryBaseQtLibsDir(ExecutionEnvironment execEnv, String qmakePath) {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(qmakePath);
            npb.setArguments("-query", "QT_INSTALL_LIBS"); // NOI18N
            try {
                NativeProcess process = npb.call();
                String output = ProcessUtils.readProcessOutputLine(process).trim();
                if (process.waitFor() == 0 && 0 < output.length()) {
                    return output;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            return null;
        }

        private static String guessBaseQtIncludeDir(String qmakePath) {
            // .../bin/qmake -> .../include/qt4
            String binDir = CndPathUtilities.getDirName(qmakePath);
            if (binDir != null) {
                String baseDir = CndPathUtilities.getDirName(binDir);
                if (baseDir != null) {
                    return baseDir + "/include/qt4"; // NOI18N
                }
            }
            return "/usr/include/qt4"; // NOI18N
        }
    }
}
