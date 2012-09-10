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

package org.netbeans.modules.ods.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.netbeans.modules.ods.tasks.query.C2CQuery;
import org.netbeans.modules.ods.tasks.repository.C2CRepository;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class C2CConfig {

    private static C2CConfig instance = null;
    private static final String LAST_CHANGE_FROM    = "c2c.last_change_from";      // NOI18N // XXX
    private static final String QUERY_NAME          = "c2c.query_";                // NOI18N
    private static final String QUERY_REFRESH_INT   = "c2c.query_refresh";         // NOI18N
    private static final String QUERY_AUTO_REFRESH  = "c2c.query_auto_refresh_";   // NOI18N
    private static final String ISSUE_REFRESH_INT   = "c2c.issue_refresh";         // NOI18N
    private static final String DELIMITER           = "<=>";                            // NOI18N
//    private static final String CHECK_UPDATES       = "c2c.check_updates";         // NOI18N

    public static final int DEFAULT_QUERY_REFRESH = 30;
    public static final int DEFAULT_ISSUE_REFRESH = 15;
    private Map<String, Icon> priorityIcons;

    private C2CConfig() { }

    public static C2CConfig getInstance() {
        if(instance == null) {
            instance = new C2CConfig();
        }
        return instance;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(C2CConfig.class);
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

//    public void setCheckUpdates(boolean bl) {
//        getPreferences().putBoolean(CHECK_UPDATES, bl);
//    }

    public int getQueryRefreshInterval() {
        return getPreferences().getInt(QUERY_REFRESH_INT, DEFAULT_QUERY_REFRESH);
    }

    public int getIssueRefreshInterval() {
        return getPreferences().getInt(ISSUE_REFRESH_INT, DEFAULT_ISSUE_REFRESH);
    }

    public boolean getQueryAutoRefresh(String queryName) {
        return getPreferences().getBoolean(QUERY_AUTO_REFRESH + queryName, false);
    }

//    public boolean getCheckUpdates() {
//        return getPreferences().getBoolean(CHECK_UPDATES, true);
//    }

    public void putQuery(C2CRepository repository, C2CQuery query) {
        getPreferences().put(
                getQueryKey(repository.getID(), query.getDisplayName()),
                query.getParametersString());
    }

    public void removeQuery(C2CRepository repository, C2CQuery query) {
        getPreferences().remove(getQueryKey(repository.getID(), query.getDisplayName()));
    }

    public C2CQuery getQuery(C2CRepository repository, String queryName) {
        String value = getStoredQuery(repository, queryName);
        if(value == null) {
            return null;
        }
        String[] values = value.split(DELIMITER);
        assert values.length >= 2;
        String parametersString = values[0];
        return null; // XXX new C2CQuery(queryName, repository, parametersString, true, true);
    }

    public String getUrlParams(C2CRepository repository, String queryName) {
        String value = getStoredQuery(repository, queryName);
        if(value == null) {
            return null;
        }
        String[] values = value.split(DELIMITER);
        assert values.length >= 2;
        return values[0];
    }

    public String[] getQueries(String repoID) {
        return getKeysWithPrefix(QUERY_NAME + repoID + DELIMITER);
    }

    private String[] getKeysWithPrefix(String prefix) {
        String[] keys = null;
        try {
            keys = getPreferences().keys();
        } catch (BackingStoreException ex) {
            C2C.LOG.log(Level.SEVERE, null, ex); // XXX
        }
        if (keys == null || keys.length == 0) {
            return new String[0];
        }
        List<String> ret = new ArrayList<String>();
        for (String key : keys) {
            if (key.startsWith(prefix)) {
                ret.add(key.substring(prefix.length()));
            }
        }
        return ret.toArray(new String[ret.size()]);
    }

    private String getQueryKey(String repositoryID, String queryName) {
        return QUERY_NAME + repositoryID + DELIMITER + queryName;
    }

    private String getStoredQuery(C2CRepository repository, String queryName) {
        String value = getPreferences().get(getQueryKey(repository.getID(), queryName), null);
        return value;
    }

    public void setLastChangeFrom(String value) {
        getPreferences().put(LAST_CHANGE_FROM, value);
    }

    public String getLastChangeFrom() {
        return getPreferences().get(LAST_CHANGE_FROM, "");                      // NOI18N
    }
    
    public String getDefaultParameters() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

//    public Icon getPriorityIcon(String priority) {
//        if(priorityIcons == null) {
//            priorityIcons = new HashMap<String, Icon>();
//            priorityIcons.put("P1", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p1.png", true)); // NOI18N
//            priorityIcons.put("P2", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p2.png", true)); // NOI18N
//            priorityIcons.put("P3", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p3.png", true)); // NOI18N
//            priorityIcons.put("P4", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p4.png", true)); // NOI18N
//            priorityIcons.put("P5", ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/p5.png", true)); // NOI18N
//        }
//        return priorityIcons.get(priority);
//    }

}
