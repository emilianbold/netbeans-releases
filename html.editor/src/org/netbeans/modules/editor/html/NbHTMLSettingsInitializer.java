/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
