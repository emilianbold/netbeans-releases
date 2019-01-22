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

package org.netbeans.modules.odcs.tasks;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * 
 */
public class ODCSConfig {

    private static ODCSConfig instance = null;
    private static final String QUERY_REFRESH_INT   = "odcs.query_refresh";         // NOI18N
    private static final String QUERY_AUTO_REFRESH  = "odcs.query_auto_refresh_";   // NOI18N
    private static final String ISSUE_REFRESH_INT   = "odcs.issue_refresh";         // NOI18N
    private static final String PREF_SECTION_COLLAPSED = "collapsedSection"; //NOI18N
    private static final String PREF_TASK = "task."; //NOI18N
    
    public static final int DEFAULT_QUERY_REFRESH = 30;
    public static final int DEFAULT_ISSUE_REFRESH = 15;

    private ODCSConfig() { }

    public static ODCSConfig getInstance() {
        if(instance == null) {
            instance = new ODCSConfig();
        }
        return instance;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(ODCSConfig.class);
    }

    public void setQueryRefreshInterval(int i) {
        getPreferences().putInt(QUERY_REFRESH_INT, i);
    }

    public void setIssueRefreshInterval(int i) {
        getPreferences().putInt(ISSUE_REFRESH_INT, i);
    }

    public void setQueryAutoRefresh(String queryName, boolean refresh) {
        getPreferences().putBoolean(QUERY_AUTO_REFRESH + queryName, refresh);
    }

    public int getIssueRefreshInterval() {
        return getPreferences().getInt(ISSUE_REFRESH_INT, DEFAULT_ISSUE_REFRESH);
    }

    public boolean getQueryAutoRefresh(String queryName) {
        return getPreferences().getBoolean(QUERY_AUTO_REFRESH + queryName, false);
    }

    public void setEditorSectionCollapsed (String repositoryId, String taskId, String sectionName, boolean collapsed) {
        String key = getTaskKey(repositoryId, taskId) + PREF_SECTION_COLLAPSED + sectionName;
        getPreferences().putBoolean(key, collapsed);
    }

    public boolean isEditorSectionCollapsed (String repositoryId, String taskId, String sectionName, boolean defaultValue) {
        String key = getTaskKey(repositoryId, taskId) + PREF_SECTION_COLLAPSED + sectionName;
        return getPreferences().getBoolean(key, defaultValue);
    }

    private String getTaskKey (String repositoryId, String taskId) {
        return PREF_TASK + repositoryId + "." + taskId + ".";
    }
}
