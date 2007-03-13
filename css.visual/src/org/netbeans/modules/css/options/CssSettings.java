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
/*
 * CssSettings.java
 *
 * Created on December 10, 2004, 10:47 AM
 */

package org.netbeans.modules.css.options;

import java.util.Map;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenContext;

/**
 * CSS editor setting initializer
 * @author Winston Prakash
 * @version 1.0
 */
public class CssSettings extends Settings.AbstractInitializer {

    /** Name of the editor setting initializer */
    private static final String SETTINGS_NAME = "css-editor-settings-initializer"; // NOI18N

    /** Creates a new instance of CssSettings */
    public CssSettings() {
        super(SETTINGS_NAME);
    }

    /** Update the settings map */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        if (kitClass == null) return;

//        // Add the coloring information to the base kit
//        if (kitClass == BaseKit.class) {
//            new CssTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);
//        }
//        if ("org.netbeans.modules.css.editor.CssEditorKit".equals(kitClass.getName())) { // NOI18N
//            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
//                    new TokenContext[] { CssTokenContext.context });
//        }
//
//        settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, CssSettingsDefaults.defaultCodeFoldingEnable);
    }

}
