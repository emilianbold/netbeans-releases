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

import java.util.HashMap;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider;
import org.netbeans.modules.jira.client.spi.Priority;
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
    private static final String QUERY_LAST_REFRESH  = "jira.query_last_refresh";    // NOI18N
    private static final String ACTIVE_CONNECTOR_CNB= "jira.active_connector";      // NOI18N    
    private static final String SHOW_CONNECTOR_WARNING = "jira.show.connector.warning"; // NOI18N    
    private static final String PREF_SECTION_COLLAPSED = "collapsedSection"; //NOI18N
    private static final String PREF_TASK = "task."; //NOI18N
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
    
    private String getQueryKey(String repositoryID, String queryName) {
        return QUERY_NAME + repositoryID + DELIMITER + queryName;
    }

    public JiraConnectorProvider.Type getActiveConnector() {
        String cnb = getPreferences().get(ACTIVE_CONNECTOR_CNB, JiraConnectorProvider.Type.XMLRPC.getCnb());
        if(cnb == null) {
            return JiraConnectorProvider.Type.XMLRPC;
}
        JiraConnectorProvider.Type[] vs = JiraConnectorProvider.Type.values();
        for (JiraConnectorProvider.Type type : vs) {
            if(cnb.equals(type.getCnb())) {
                return type;
            }
        }
        return JiraConnectorProvider.Type.XMLRPC;
    }
    
    public void setActiveConnector(JiraConnectorProvider.Type type) {
        getPreferences().put(ACTIVE_CONNECTOR_CNB, type.getCnb());
    }    

    public void stopShowingChangeConnectorWarning() {
        getPreferences().putBoolean(SHOW_CONNECTOR_WARNING, false);
    }
    
    public boolean showChangeConnectorWarning() {
        return getPreferences().getBoolean(SHOW_CONNECTOR_WARNING, true);
    }
}
