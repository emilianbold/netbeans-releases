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

package org.netbeans.modules.editor.html;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.Map;
import java.util.HashMap;
import javax.swing.KeyStroke;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.html.HTMLSettingsDefaults;
import org.netbeans.editor.ext.html.HTMLSettingsNames;

/**
* Nb settings for HTML.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbHTMLSettingsInitializer extends Settings.AbstractInitializer {

    public static final String NAME = "nb-html-settings-initializer"; // NOI18N

    public NbHTMLSettingsInitializer() {
        super(NAME);
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {

        if (kitClass == HTMLKit.class) {

            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, Boolean.TRUE);
            
            settingsMap.put(HTMLSettingsNames.CODE_FOLDING_UPDATE_TIMEOUT,
                            HTMLSettingsDefaults.defaultCodeFoldingUpdateInterval);
            
            SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST, getHTMLKeyBindings());
            
            
        }

    }

    public MultiKeyBinding[] getHTMLKeyBindings() {
        return new MultiKeyBinding[] {
            new MultiKeyBinding(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK),
                HTMLKit.shiftInsertBreakAction
            )
        };
    }

}
