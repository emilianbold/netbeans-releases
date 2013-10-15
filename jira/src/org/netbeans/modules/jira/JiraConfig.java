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

package org.netbeans.modules.jira;

import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class JiraConfig {

    private static JiraConfig instance = null;
    private static final String QUERY_NAME          = "bugzilla.query_";            // NOI18N
    private static final String LAST_CHANGE_FROM    = "jira.last_change_from";      // NOI18N 
    private static final String QUERY_REFRESH_INT   = "jira.query_refresh";         // NOI18N
    private static final String QUERY_LAST_REFRESH  = "jira.query_last_refresh";    // NOI18N
    private static final String QUERY_AUTO_REFRESH  = "jira.query_auto_refresh_";   // NOI18N
    private static final String ISSUE_REFRESH_INT   = "jira.issue_refresh";         // NOI18N

    private static final String DELIMITER           = "<=>";                        // NOI18N
    
    private HashMap<String, Icon> priorityIcons;
    private HashMap<String, String> priorityIconsURL;
    private JiraConfig() { }

    public static JiraConfig getInstance() {
        if(instance == null) {
            instance = new JiraConfig();
        }
        return instance;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(JiraConfig.class);
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

    public int getQueryRefreshInterval() {
        return getPreferences().getInt(QUERY_REFRESH_INT, 30);
    }

    public int getIssueRefreshInterval() {
        return getPreferences().getInt(ISSUE_REFRESH_INT, 15);
    }

    public boolean getQueryAutoRefresh(String queryName) {
        return getPreferences().getBoolean(QUERY_AUTO_REFRESH + queryName, false);
    }

    public void setLastChangeFrom(String value) {
        getPreferences().put(LAST_CHANGE_FROM, value);
    }

    public String getLastChangeFrom() {
        return getPreferences().get(LAST_CHANGE_FROM, "");                      // NOI18N
    }

    public Icon getPriorityIcon(String priorityId) {
        if(priorityIcons == null) {
            priorityIcons = new HashMap<String, Icon>();
            priorityIcons.put(Priority.BLOCKER_ID, ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/blocker.png", true));   // NOI18N
            priorityIcons.put(Priority.CRITICAL_ID, ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/critical.png", true)); // NOI18N
            priorityIcons.put(Priority.MAJOR_ID, ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/major.png", true));       // NOI18N
            priorityIcons.put(Priority.MINOR_ID, ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/minor.png", true));       // NOI18N
            priorityIcons.put(Priority.TRIVIAL_ID, ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/trivial.png", true));   // NOI18N
        }
        return priorityIcons.get(priorityId);
    }

    public String getPriorityIconURL (String priorityId) {
        if(priorityIconsURL == null) {
            priorityIconsURL = new HashMap<>();
            priorityIconsURL.put(Priority.BLOCKER_ID, JiraConfig.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/blocker.png").toString()); //NOI18N
            priorityIconsURL.put(Priority.CRITICAL_ID, JiraConfig.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/critical.png").toString()); //NOI18N
            priorityIconsURL.put(Priority.MAJOR_ID, JiraConfig.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/major.png").toString()); //NOI18N
            priorityIconsURL.put(Priority.MINOR_ID, JiraConfig.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/minor.png").toString()); //NOI18N
            priorityIconsURL.put(Priority.TRIVIAL_ID, JiraConfig.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/trivial.png").toString()); //NOI18N
        }
        return priorityIconsURL.get(priorityId);
    }
    
    public long getLastQueryRefresh(JiraRepository repository, String queryName) {
        return getPreferences().getLong(getQueryKey(repository.getID(), queryName) + QUERY_LAST_REFRESH, -1);
    }
    
    public void putLastQueryRefresh(JiraRepository repository, String queryName, long lastRefresh) {
        getPreferences().putLong(getQueryKey(repository.getID(), queryName) + QUERY_LAST_REFRESH, lastRefresh);
    }
    
    private String getQueryKey(String repositoryID, String queryName) {
        return QUERY_NAME + repositoryID + DELIMITER + queryName;
    }

}
