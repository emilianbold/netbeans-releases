/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.earproject.ui;

import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class FoldersListSettings {

    private static final FoldersListSettings INSTANCE = new FoldersListSettings();
    
    static final long serialVersionUID = -4905094097265543015L;

    private static final String LAST_EXTERNAL_SOURCE_ROOT = "srcRoot";  //NOI18N

    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N

    private static final String SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; //NOI18N

    private static final String SHOW_AGAIN_BROKEN_SERVER_ALERT = "showAgainBrokenServerAlert"; //NOI18N
    
    private static final String AGREED_SET_JDK_14 = "agreeSetJdk14"; // NOI18N
    
    private static final String AGREED_SET_SOURCE_LEVEL_14 = "agreeSetSourceLevel14"; // NOI18N

    private static final String AGREED_SET_JDK_15 = "agreeSetJdk15"; // NOI18N
    
    private static final String AGREED_SET_SOURCE_LEVEL_15 = "agreeSetSourceLevel15"; // NOI18N

    
    private static final String LAST_USED_SERVER = "lastUsedServer"; // NOI18N

    public String displayName() {
        return NbBundle.getMessage (FoldersListSettings.class, "TXT_EarProjectFolderList"); //NOI18N
    }

    public String getLastExternalSourceRoot () {
        return getPreferences().get(LAST_EXTERNAL_SOURCE_ROOT, null);
    }

    public void setLastExternalSourceRoot (String path) {
        getPreferences().put(LAST_EXTERNAL_SOURCE_ROOT, path);
    }

    public int getNewProjectCount () {
        return getPreferences().getInt(NEW_PROJECT_COUNT, 0);
    }

    public void setNewProjectCount (int count) {
        getPreferences().putInt(NEW_PROJECT_COUNT, count);
    }
    
    public boolean isShowAgainBrokenRefAlert() {
        return getPreferences().getBoolean(SHOW_AGAIN_BROKEN_REF_ALERT, true);
    }
    
    public void setShowAgainBrokenRefAlert(boolean again) {
        getPreferences().putBoolean(SHOW_AGAIN_BROKEN_REF_ALERT, again);
    }

    public boolean isShowAgainBrokenServerAlert() {
        return getPreferences().getBoolean(SHOW_AGAIN_BROKEN_SERVER_ALERT, true);
    }
    
    public void setShowAgainBrokenServerAlert(boolean again) {
        getPreferences().putBoolean(SHOW_AGAIN_BROKEN_SERVER_ALERT, again);
    }

    public static FoldersListSettings getDefault () {
        return INSTANCE;
    }
    
    public boolean isAgreedSetJdk14() {
        return getPreferences().getBoolean(AGREED_SET_JDK_14, true);
    }
    
    public void setAgreedSetJdk14(boolean agreed) {
        getPreferences().putBoolean(AGREED_SET_JDK_14, agreed);
    }
    
    public boolean isAgreedSetSourceLevel14() {
        return getPreferences().getBoolean(AGREED_SET_SOURCE_LEVEL_14, true);
    }
    
    public void setAgreedSetSourceLevel14(boolean agreed) {
        getPreferences().putBoolean(AGREED_SET_SOURCE_LEVEL_14, agreed);
    }

    public boolean isAgreedSetJdk15() {
        return getPreferences().getBoolean(AGREED_SET_JDK_15, true);
    }
    
    public void setAgreedSetJdk15(boolean agreed) {
        getPreferences().putBoolean(AGREED_SET_JDK_15, agreed);
    }
    
    public boolean isAgreedSetSourceLevel15() {
        return getPreferences().getBoolean(AGREED_SET_SOURCE_LEVEL_15, true);
    }
    
    public void setAgreedSetSourceLevel15(boolean agreed) {
        getPreferences().putBoolean(AGREED_SET_SOURCE_LEVEL_15, agreed);
    }

    public void setLastUsedServer(String serverInstanceID) {
        getPreferences().put(LAST_USED_SERVER, serverInstanceID);
    }

    public String getLastUsedServer() {
        return getPreferences().get(LAST_USED_SERVER, null);
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(FoldersListSettings.class);
    }

}
