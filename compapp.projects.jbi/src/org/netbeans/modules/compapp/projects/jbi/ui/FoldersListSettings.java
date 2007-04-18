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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.jbi.ui;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.NbBundle;


/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class FoldersListSettings {
    /**
     * DOCUMENT ME!
     */
    static final long serialVersionUID = -4905094097265543014L;
    
    private static final FoldersListSettings INSTANCE = new FoldersListSettings();
    private static final String STRING_DEFAULT_VALUE = "";      // NOI18N
    
    private static final String LAST_EXTERNAL_SOURCE_ROOT = "srcRoot"; // NOI18N
    private static final String NEW_PROJECT_COUNT = "newProjectCount"; // NOI18N
    private static final String SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; // NOI18N

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String displayName() {
        return NbBundle.getMessage(FoldersListSettings.class, "TXT_WebProjectFolderList"); // NOI18N
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(FoldersListSettings.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getLastExternalSourceRoot() {
        return getPreferences().get(LAST_EXTERNAL_SOURCE_ROOT, STRING_DEFAULT_VALUE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param path DOCUMENT ME!
     */
    public void setLastExternalSourceRoot(String path) {
        getPreferences().put(LAST_EXTERNAL_SOURCE_ROOT, path);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getNewProjectCount() {
        return getPreferences().getInt(NEW_PROJECT_COUNT, 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param count DOCUMENT ME!
     */
    public void setNewProjectCount(int count) {
        getPreferences().putInt(NEW_PROJECT_COUNT, count);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isShowAgainBrokenRefAlert() {
        return getPreferences().getBoolean(SHOW_AGAIN_BROKEN_REF_ALERT, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param again DOCUMENT ME!
     */
    public void setShowAgainBrokenRefAlert(boolean again) {
        getPreferences().putBoolean(SHOW_AGAIN_BROKEN_REF_ALERT, again);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static FoldersListSettings getDefault() {
        //return (FoldersListSettings) SystemOption.findObject(FoldersListSettings.class, true);
        return INSTANCE;
    }
}
