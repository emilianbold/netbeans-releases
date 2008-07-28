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

package org.netbeans.modules.php.project.ui;

import java.io.File;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

/**
 * Helper class to remember the last selected folders for every file chooser in PHP project.
 * @author Tomas Mysik
 */
public final class LastUsedFolders {
    private static final String LAST_USED_FOLDERS_NODE = "lastUsedFoldersNode";

    private static final String OPTIONS_INTERPRETER = "optionsInterpreter"; // NOI18N
    private static final String COPY_FILES = "copyFiles"; // NOI18N
    private static final String INCLUDE_PAH = "includePath"; // NOI18N
    private static final String SOURCES = "sources"; // NOI18N
    private static final String PROJECT = "project"; // NOI18N

    private LastUsedFolders() {
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(LastUsedFolders.class).node(LAST_USED_FOLDERS_NODE);
    }

    private static File getFile(String option) {
        String path = getPreferences().get(option, null);
        if (path == null) {
            return null;
        }
        return new File(path);
    }

    private static void setFile(String option, File file) {
        if (file == null) {
            return;
        }
        file = FileUtil.normalizeFile(file);
        String path = null;
        if (file.isDirectory()) {
            path = file.getAbsolutePath();
        } else {
            path = file.getParentFile().getAbsolutePath();
        }
        getPreferences().put(option, path);
    }

    public static File getOptionsInterpreter() {
        return getFile(OPTIONS_INTERPRETER);
    }

    public static void setOptionsInterpreter(File optionsInterpreter) {
        setFile(OPTIONS_INTERPRETER, optionsInterpreter);
    }

    public static File getCopyFiles() {
        return getFile(COPY_FILES);
    }

    public static void setCopyFiles(File copyFiles) {
        setFile(COPY_FILES, copyFiles);
    }

    public static File getIncludePath() {
        return getFile(INCLUDE_PAH);
    }

    public static void setIncludePath(File includePath) {
        setFile(INCLUDE_PAH, includePath);
    }

    public static File getSources() {
        return getFile(SOURCES);
    }

    public static void setSources(File sources) {
        setFile(SOURCES, sources);
    }

    public static File getProject() {
        return getFile(PROJECT);
    }

    public static void setProject(File project) {
        setFile(PROJECT, project);
    }
}
