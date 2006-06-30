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

package org.netbeans.editor.example;

import java.awt.Font;
import java.awt.Color;
import java.awt.SystemColor;
import java.util.*;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenCategory;
import org.netbeans.modules.properties.syntax.*;


public class PropertiesSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "properties-settings-initializer"; // NOI18N
    private Class propertiesClass;

    public PropertiesSettingsInitializer( Class propertiesClass ) {
        super(NAME);
        this.propertiesClass = propertiesClass;
    }

    public void updateSettingsMap (Class kitClass, Map settingsMap) {
        
        // Properties colorings
        if (kitClass == BaseKit.class) {
            new PropertiesTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);

        }

        if (kitClass == propertiesClass) {
            settingsMap.put (org.netbeans.editor.SettingsNames.ABBREV_MAP, getPropertiesAbbrevMap());

            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                new TokenContext[] {
                    PropertiesTokenContext.context
                }
            );

        }

    }


    Map getPropertiesAbbrevMap() {
        Map propertiesAbbrevMap = new TreeMap ();
        return propertiesAbbrevMap;
    }

    static class PropertiesTokenColoringInitializer
    extends SettingsUtil.TokenColoringInitializer {

        Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
        Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);

        Coloring emptyColoring = new Coloring(null, null, null);
        Coloring commentColoring = new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE,
                            Color.gray, null);

        Coloring numbersColoring = new Coloring(null, Color.red, null);

        public PropertiesTokenColoringInitializer() {
            super(PropertiesTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath,
        TokenCategory tokenIDOrCategory, boolean printingSet) {

            if (!printingSet) {
                switch (tokenIDOrCategory.getNumericID()) {
                    case PropertiesTokenContext.KEY_ID:
                        return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                                Color.blue, null);

                    case PropertiesTokenContext.EQ_ID:
                    case PropertiesTokenContext.TEXT_ID:
                        return emptyColoring;

                    case PropertiesTokenContext.LINE_COMMENT_ID:
                        return new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE,
                                Color.gray, null);

                    case PropertiesTokenContext.VALUE_ID:
                        return new Coloring(null, Color.magenta, null);
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
