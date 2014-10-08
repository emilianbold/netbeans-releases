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

package org.netbeans.modules.javascript.nodejs.preferences;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;

/**
 * Project specific Node.js preferences.
 */
public final class NodeJsPreferences {

    public static final String ENABLED = "enabled"; // NOI18N
    public static final String NODE_PATH = "node.path"; // NOI18N
    public static final String NODE_DEFAULT = "node.default"; // NOI18N
    public static final String START_FILE = "start.file"; // NOI18N
    public static final String START_ARGS = "start.args"; // NOI18N
    public static final String DEBUG_PORT = "debug.port"; // NOI18N

    private final Project project;

    // @GuardedBy("this")
    private Preferences preferences;


    public NodeJsPreferences(Project project) {
        assert project != null;
        this.project = project;
    }

    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        getPreferences().addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        getPreferences().removePreferenceChangeListener(listener);
    }

    public boolean isEnabled() {
        return getPreferences().getBoolean(ENABLED, false);
    }

    public void setEnabled(boolean enabled) {
        getPreferences().putBoolean(ENABLED, enabled);
    }

    @CheckForNull
    public String getNode() {
        return FileUtils.resolvePath(project, getPreferences().get(NODE_PATH, null));
    }

    public void setNode(String node) {
        getPreferences().put(NODE_PATH, FileUtils.relativizePath(project, node));
    }

    public boolean isDefaultNode() {
        return getPreferences().getBoolean(NODE_DEFAULT, true);
    }

    public void setDefaultNode(boolean defaultNode) {
        getPreferences().putBoolean(NODE_DEFAULT, defaultNode);
    }

    @CheckForNull
    public String getStartFile() {
        return FileUtils.resolvePath(project, getPreferences().get(START_FILE, null));
    }

    public void setStartFile(String startFile) {
        getPreferences().put(START_FILE, FileUtils.relativizePath(project, startFile));
    }

    @CheckForNull
    public String getStartArgs() {
        return getPreferences().get(START_ARGS, null);
    }

    public void setStartArgs(String startArgs) {
        getPreferences().put(START_ARGS, startArgs);
    }

    public int getDebugPort() {
        return getPreferences().getInt(DEBUG_PORT, NodeExecutable.DEFAULT_DEBUG_PORT);
    }

    public void setDebugPort(int debugPort) {
        getPreferences().putInt(DEBUG_PORT, debugPort);
    }

    private synchronized Preferences getPreferences() {
        if (preferences == null) {
            preferences = ProjectUtils.getPreferences(project, NodeJsPreferences.class, false);
        }
        return preferences;
    }

}
