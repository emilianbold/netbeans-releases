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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;

/**
 * Utility to find Qt include directories for project configuration.
 *
 * @author Alexey Vladykin
 */
public abstract class QtInfoProvider {

    private static final QtInfoProvider DEFAULT = new Default();

    private QtInfoProvider() {
    }

    public static QtInfoProvider getDefault() {
        return DEFAULT;
    }

    public abstract List<String> getQtIncludeDirectories(MakeConfiguration conf);

    private static class Default extends QtInfoProvider {

        private static final String FAKE_DIR = "FAKE_DIR"; // NOI18N
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
        public List<String> getQtIncludeDirectories(MakeConfiguration conf) {
            String baseDir;
            synchronized (cache) {
                baseDir = cache.get(conf.getDevelopmentHost().getHostKey());
                if (baseDir == null) {
                    baseDir = queryQtIncludeDir(conf);
                    cache.put(conf.getDevelopmentHost().getHostKey(), baseDir);
                }
            }
            if (baseDir != null && !baseDir.equals(FAKE_DIR)) {
                List<String> list = new ArrayList<String>();
                list.add(baseDir);
                QmakeConfiguration qmakeConfiguration = conf.getQmakeConfiguration();
                if (qmakeConfiguration.isCoreEnabled().getValue()) {
                    list.add(baseDir + File.separator + "QtCore"); // NOI18N
                }
                if (qmakeConfiguration.isGuiEnabled().getValue()) {
                    list.add(baseDir + File.separator + "QtGui"); // NOI18N
                }
                if (qmakeConfiguration.isNetworkEnabled().getValue()) {
                    list.add(baseDir + File.separator + "QtNetwork"); // NOI18N
                }
                if (qmakeConfiguration.isOpenglEnabled().getValue()) {
                    list.add(baseDir + File.separator + "QtOpenGL"); // NOI18N
                }
                if (qmakeConfiguration.isPhononEnabled().getValue()) {
                    list.add(baseDir + File.separator + "phonon"); // NOI18N
                }
                if (qmakeConfiguration.isQt3SupportEnabled().getValue()) {
                    list.add(baseDir + File.separator + "Qt3Support"); // NOI18N
                }
                if (qmakeConfiguration.isSqlEnabled().getValue()) {
                    list.add(baseDir + File.separator + "QtSql"); // NOI18N
                }
                if (qmakeConfiguration.isSvgEnabled().getValue()) {
                    list.add(baseDir + File.separator + "QtSvg"); // NOI18N
                }
                if (qmakeConfiguration.isXmlEnabled().getValue()) {
                    list.add(baseDir + File.separator + "QtXml"); // NOI18N
                }
                if (qmakeConfiguration.isWebkitEnabled().getValue()) {
                    list.add(baseDir + File.separator + "QtWebKit"); // NOI18N
                }
                String uiDir = qmakeConfiguration.getUiDir().getValue();
                if (IpeUtils.isPathAbsolute(uiDir)) {
                    list.add(uiDir);
                } else {
                    list.add(conf.getBaseDir() + File.separator + uiDir);
                }
                return list;
            } else {
                return Collections.emptyList();
            }
        }

        private String queryQtIncludeDir(MakeConfiguration conf) {
            if (conf.getDevelopmentHost().getExecutionEnvironment().isLocal()) {
                try {
                    Process process = Runtime.getRuntime().exec("qmake -query QT_INSTALL_HEADERS"); // NOI18N
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    try {
                        String line = reader.readLine().trim();
                        if (0 < line.length()) {
                            return line;
                        }
                    } finally {
                        reader.close();
                    }
                } catch (IOException ex) {
                    // probably qmake was not found
                    // ignore and return FAKE_DIR
                }
            } else {
                // remote is not supported yet
            }
            return FAKE_DIR;
        }
    }
}
