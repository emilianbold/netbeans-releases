/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.source.tasklist;

import java.util.prefs.Preferences;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Lahoda
 */
public class TasklistSettings {

    private static final String KEY_ENABLED = "enabled"; //NOI18N
    private static final String KEY_ERROR_BADGES = "error-badges"; //NOI18N
    private static final String KEY_DEPENDENCY_TRACKING = "dependency-tracking"; //NOI18N
    
    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_ERROR_BADGES = true;
    private static final String DEFAULT_DEPENDENCY_TRACKING = DependencyTracking.ENABLED_WITHIN_ROOT.name();
    
    private TasklistSettings() {
    }
    
    public static boolean isTasklistEnabled() {
        return getPreferencesNode().getBoolean(KEY_ENABLED, DEFAULT_ENABLED);
    }
    
    public static void setTasklistsEnabled(boolean enabled) {
        if (isTasklistEnabled() != enabled) {
            getPreferencesNode().putBoolean(KEY_ENABLED, enabled);
            if (enabled) {
// XXX:                RepositoryUpdater.getDefault().rebuildAll(true);
                IndexingManager.getDefault().refreshAllIndices(JavaIndex.NAME);
            }
            
            ErrorAnnotator an = ErrorAnnotator.getAnnotator();
            
            if (an != null) {
                an.updateAllInError();
            }
            
            JavaTaskProvider.refreshAll();
        }
    }
    
    public static boolean isBadgesEnabled() {
        return getPreferencesNode().getBoolean(KEY_ERROR_BADGES, DEFAULT_ERROR_BADGES);
    }

    public static void setBadgesEnabled(boolean enabled) {
        if (isBadgesEnabled() != enabled) {
            getPreferencesNode().putBoolean(KEY_ERROR_BADGES, enabled);
            
            ErrorAnnotator an = ErrorAnnotator.getAnnotator();
            
            if (an != null) {
                an.updateAllInError();
            }
        }
    }
    
    public static DependencyTracking getDependencyTracking() {
        String s = getPreferencesNode().get(KEY_DEPENDENCY_TRACKING, DEFAULT_DEPENDENCY_TRACKING);
        try {
            return DependencyTracking.valueOf(s);
        } catch (IllegalArgumentException e) {
            return DependencyTracking.valueOf(DEFAULT_DEPENDENCY_TRACKING);
        }
    }
    
    public static void setDependencyTracking(DependencyTracking dt) {
        final DependencyTracking curr = getDependencyTracking();
        if (curr != dt) {
            getPreferencesNode().put(KEY_DEPENDENCY_TRACKING, dt.name());
            if (dt.ordinal() > curr.ordinal()) {
                IndexingManager.getDefault().refreshAllIndices(JavaIndex.NAME);
            }
            
            ErrorAnnotator an = ErrorAnnotator.getAnnotator();
            
            if (an != null) {
                an.updateAllInError();
            }
        }
    }
    
    private static Preferences getPreferencesNode() {
        return NbPreferences.forModule(TasklistSettings.class).node("tasklist");
    }

    public static enum DependencyTracking {
        DISABLED,
        ENABLED_WITHIN_ROOT,
        ENABLED_WITHIN_PROJECT,
        ENABLED
    }
}
