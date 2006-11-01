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

package org.netbeans.modules.db.core;

import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Andrei Badea
 */
public class SQLOptions  {
    private static SQLOptions INSTANCE = new SQLOptions();
    private static final String PROP_FETCH_STEP = "fetchStep"; // NOI18N
    private static final int DEFAULT_FETCH_STEP = 200;

    public static SQLOptions getDefault() {
        return INSTANCE;
    }
    
    public String displayName() {
        return NbBundle.getMessage(SQLOptions.class, "LBL_SQLOptions");
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(SQLOptions.class);
    }
        
    public int getFetchStep() {
        return getPreferences().getInt(PROP_FETCH_STEP, DEFAULT_FETCH_STEP);
    }
    
    public void setFetchStep(int value) {
        getPreferences().putInt(PROP_FETCH_STEP, value);
    }    
}
