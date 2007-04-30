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

package org.netbeans.modules.db.sql.editor;

import java.util.Map;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenContext;

/**
 * Initializes the SQL Settings
 *
 * @author Jesse Beaumont, Andrei Badea
 */
public class SQLSettingsInitializer extends Settings.AbstractInitializer {

    public static final String NAME = "sql-settings-initializer"; // NOI18N
    
    /**
     * Constructor
     */
    public SQLSettingsInitializer() {
        super(NAME);
    }

   /** 
    * Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        if (kitClass == SQLEditorKit.class) {
            SettingsUtil.updateListSetting(
                    settingsMap, 
                    SettingsNames.TOKEN_CONTEXT_LIST,
                    new TokenContext[] { SQLTokenContext.context }
            );
        }
    }
}
