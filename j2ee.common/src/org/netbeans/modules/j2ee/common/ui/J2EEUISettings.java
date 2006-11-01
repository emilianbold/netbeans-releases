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

package org.netbeans.modules.j2ee.common.ui;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public class J2EEUISettings  {
    private static final J2EEUISettings INSTANCE = new J2EEUISettings();
    private static final String SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; // NOI18N
    private static final String SHOW_AGAIN_BROKEN_SERVER_ALERT = "showAgainBrokenServerAlert"; // NOI18N
    
    public String displayName() {
        return "J2EEUISettings"; // NOI18N (not shown in UI)
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(J2EEUISettings.class);
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
    
    public static J2EEUISettings getDefault() {
        return INSTANCE;
    }
    
}
