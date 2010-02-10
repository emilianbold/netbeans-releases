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

package org.netbeans.modules.bugtracking;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class BugtrackingConfig {

    private static BugtrackingConfig instance = null;
    private static final String ARCHIVED_TTL_KEY  = "bugtracking.archived_time_to_live";      // NOI18N
    private static final String COLUMN_WIDTH_PREFIX  = "bugtracking.issuetable.columnwidth";  // NOI18N
    private static long DEFAULT_ARCHIVED_TTL  = 7; // days

    private BugtrackingConfig() { }

    public static BugtrackingConfig getInstance() {
        if(instance == null) {
            instance = new BugtrackingConfig();
        }
        return instance;
    }

    public Preferences getPreferences() {
        return NbPreferences.forModule(BugtrackingConfig.class);
    }

    public void setArchivedIssuesTTL(int l) {
        getPreferences().putLong(ARCHIVED_TTL_KEY, l);
    }

    public long getArchivedIssuesTTL() {
        return getPreferences().getLong(ARCHIVED_TTL_KEY, DEFAULT_ARCHIVED_TTL);
    }

    public void storeColumns(String key, String columns) {
        getPreferences().put(COLUMN_WIDTH_PREFIX + "." + key, columns); // NOI18N
    }

    public String getColumns(String key) {
        return getPreferences().get(COLUMN_WIDTH_PREFIX + "." + key, ""); // NOI18N
    }

    @Deprecated
    public int[] getColumnWidths(String key) {
        List<Integer> retval = new ArrayList<Integer>();
        try {
            String[] keys = getPreferences().keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(COLUMN_WIDTH_PREFIX + "." + key + ".")) { // NOI18N
                    int idx = Integer.parseInt(k.substring(k.lastIndexOf('.') + 1));    // NOI18N
                    int value = getPreferences().getInt(k, -1);
                    retval.add(idx, value);
                    getPreferences().remove(k);
                }
            }
            int[] ret = new int[retval.size()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = retval.get(i);
            }
            return ret;
        } catch (Exception ex) {
            BugtrackingManager.LOG.log(Level.INFO, null, ex);
            return new int[0];
        }
    }

}
