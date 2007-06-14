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

package org.netbeans.modules.visualweb.dataconnectivity.ui;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public class DatasourceUISettings  {
    private static final DatasourceUISettings INSTANCE = new DatasourceUISettings();
    private static final String SHOW_AGAIN_BROKEN_DATASOURCE_ALERT = "showAgainBrokenDatasourceAlert"; // NOI18N
    
    public String displayName() {
        return "DatasourceUISettings"; // NOI18N (not shown in UI)
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(DatasourceUISettings.class);
    }       
    
    public boolean isShowAgainBrokenDatasourceAlert() {
        return getPreferences().getBoolean(SHOW_AGAIN_BROKEN_DATASOURCE_ALERT, true);
    }
    
    public void setShowAgainBrokenDatasourceAlert(boolean again) {
        getPreferences().putBoolean(SHOW_AGAIN_BROKEN_DATASOURCE_ALERT, again);
    }
    
    public static DatasourceUISettings getDefault() {
        return INSTANCE;
    }
    
}
