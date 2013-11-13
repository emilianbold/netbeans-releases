/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.karma.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.exec.KarmaExecutable;
import org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class KarmaUtils {

    private static final FilenameFilter KARMA_CONFIG_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith("karma") // NOI18N
                    && name.endsWith(".conf.js"); // NOI18N
        }
    };


    private KarmaUtils() {
    }

    public static File getConfigDir(Project project) {
        ProjectDirectoriesProvider directoriesProvider = project.getLookup().lookup(ProjectDirectoriesProvider.class);
        if (directoriesProvider != null) {
            FileObject configDirectory = directoriesProvider.getConfigDirectory();
            if (configDirectory != null
                    && configDirectory.isValid()) {
                return FileUtil.toFile(configDirectory);
            }
        }
        return FileUtil.toFile(project.getProjectDirectory());
    }

    public static List<File> findKarmaConfigs(File configDir) {
        assert configDir != null;
        assert configDir.isDirectory() : configDir;
        File[] configs = configDir.listFiles(KARMA_CONFIG_FILTER);
        if (configs == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(configs);
    }

    /**
     * Tries to find the "best" Karma config file.
     */
    @CheckForNull
    public static File findKarmaConfig(File configDir) {
        List<File> karmaConfigs = findKarmaConfigs(configDir);
        int indexOf = karmaConfigs.indexOf(new File(configDir, "karma.conf.js")); // NOI18N
        if (indexOf != -1) {
            return karmaConfigs.get(indexOf);
        }
        File firstConfig = null;
        for (File config : karmaConfigs) {
            if (firstConfig == null) {
                firstConfig = config;
            }
            String configName = config.getName().toLowerCase();
            if (configName.contains("share") // NOI18N
                    || configName.contains("common")) { // NOI18N
                continue;
            }
            return config;
        }
        return firstConfig;
    }

    /**
     * Tries to find the "best" Karma file (first project one, then a system one).
     */
    @CheckForNull
    public static File findKarma(Project project) {
        // first, project karma
        FileObject projectKarma = project.getProjectDirectory().getFileObject(KarmaExecutable.PROJECT_KARMA_PATH);
        if (projectKarma != null
                && projectKarma.isValid()
                && projectKarma.isData()) {
            return FileUtil.toFile(projectKarma);
        }
        // search on user's PATH
        List<String> karmas = FileUtils.findFileOnUsersPath(KarmaExecutable.KARMA_NAME, KarmaExecutable.KARMA_LONG_NAME);
        if (!karmas.isEmpty()) {
            return new File(karmas.get(0));
        }
        return null;
    }

}
