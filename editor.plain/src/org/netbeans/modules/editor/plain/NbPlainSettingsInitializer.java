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

package org.netbeans.modules.editor.plain;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.plain.PlainSyntax;
import org.netbeans.editor.ext.plain.PlainTokenContext;

/**
* Settings for plain kit
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbPlainSettingsInitializer extends Settings.AbstractInitializer {

    public static final String NAME = "nb-plain-settings-initializer"; // NOI18N

    public NbPlainSettingsInitializer() {
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

        if (kitClass == BaseKit.class) {

            new PlainTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);

        }


        if (kitClass == PlainKit.class) {


        }

    }

    static class PlainTokenColoringInitializer
    extends SettingsUtil.TokenColoringInitializer {

        Coloring emptyColoring = new Coloring(null, null, null);

        public PlainTokenColoringInitializer() {
            super(PlainTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath,
        TokenCategory tokenIDOrCategory, boolean printingSet) {
            if (!printingSet) {
                switch (tokenIDOrCategory.getNumericID()) {
                    case PlainTokenContext.TEXT_ID:
                        return emptyColoring;
                }

            } else { // printing set
                switch (tokenIDOrCategory.getNumericID()) {
                    default:
                         return SettingsUtil.defaultPrintColoringEvaluator;
                }

            }

            return null;

        }

    }

}
