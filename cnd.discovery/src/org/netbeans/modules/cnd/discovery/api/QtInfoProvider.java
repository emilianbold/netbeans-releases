/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.EnvUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

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

    public abstract List<String> getQtIncludeDirectories(MakeConfiguration conf);

    private static class Default extends QtInfoProvider {

        private final Map<String, String> cache;

        private Default() {
            cache = new HashMap<String, String>();
        }

        /**
         * Finds Qt include directories for given project configuration.
         *
         * @param conf  Qt project configuration
         * @return list of include directories, may be empty if qmake is not found
         */
        @Override
        public List<String> getQtIncludeDirectories(MakeConfiguration conf) {
            String baseDir = getBaseQtIncludeDir(conf);
            List<String> result;
            if (baseDir != null) {
                result = new ArrayList<String>();
                result.add(baseDir);
                QmakeConfiguration qmakeConfiguration = conf.getQmakeConfiguration();
                if (qmakeConfiguration.isCoreEnabled().getValue()) {
                    result.add(baseDir + File.separator + "QtCore"); // NOI18N
                }
                if (qmakeConfiguration.isGuiEnabled().getValue()) {
                    result.add(baseDir + File.separator + "QtGui"); // NOI18N
                }
                if (qmakeConfiguration.isNetworkEnabled().getValue()) {
                    result.add(baseDir + File.separator + "QtNetwork"); // NOI18N
                }
                if (qmakeConfiguration.isOpenglEnabled().getValue()) {
                    result.add(baseDir + File.separator + "QtOpenGL"); // NOI18N
                }
                if (qmakeConfiguration.isPhononEnabled().getValue()) {
                    result.add(baseDir + File.separator + "phonon"); // NOI18N
                }
                if (qmakeConfiguration.isQt3SupportEnabled().getValue()) {
                    result.add(baseDir + File.separator + "Qt3Support"); // NOI18N
                }
                if (qmakeConfiguration.isSqlEnabled().getValue()) {
                    result.add(baseDir + File.separator + "QtSql"); // NOI18N
                }
                if (qmakeConfiguration.isSvgEnabled().getValue()) {
                    result.add(baseDir + File.separator + "QtSvg"); // NOI18N
                }
                if (qmakeConfiguration.isXmlEnabled().getValue()) {
                    result.add(baseDir + File.separator + "QtXml"); // NOI18N
                }
                if (qmakeConfiguration.isWebkitEnabled().getValue()) {
                    result.add(baseDir + File.separator + "QtWebKit"); // NOI18N
                }
                String uiDir = qmakeConfiguration.getUiDir().getValue();
                if (CndPathUtilitities.isPathAbsolute(uiDir)) {
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

        private String getBaseQtIncludeDir(MakeConfiguration conf) {
            String cacheKey = getCacheKey(conf);
            String baseDir;
            synchronized (cache) {
                if (cache.containsKey(cacheKey)) {
                    baseDir = cache.get(cacheKey);
                } else {
                    ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
                    String qmakePath = getQmakePath(conf);
                    if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                        baseDir = queryBaseQtIncludeDir(execEnv, qmakePath);
                        if (baseDir != null && execEnv.isRemote()) {
                            baseDir = CndUtils.getIncludeFilePrefix(EnvUtils.toHostID(execEnv)) + baseDir;
                        }
                        cache.put(cacheKey, baseDir);
                    } else {
                        baseDir = CndUtils.getIncludeFilePrefix(EnvUtils.toHostID(execEnv))
                                + guessBaseQtIncludeDir(qmakePath);
                        // do not cache this result, so that we can
                        // really query qmake once connection is up
                    }
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Qt include dir for {0} = {1}", new Object[] {cacheKey, baseDir});
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

        private static String guessBaseQtIncludeDir(String qmakePath) {
            // .../bin/qmake -> .../include/qt4
            String binDir = CndPathUtilitities.getDirName(qmakePath);
            if (binDir != null) {
                String baseDir = CndPathUtilitities.getDirName(binDir);
                if (baseDir != null) {
                    return baseDir + "/include/qt4"; // NOI18N
                }
            }
            return "/usr/include/qt4"; // NOI18N
        }
    }
}
