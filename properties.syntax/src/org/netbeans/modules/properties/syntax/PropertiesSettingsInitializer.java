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


package org.netbeans.modules.properties.syntax;


import java.awt.Font;
import java.awt.Color;
import java.awt.SystemColor;
import java.util.*;

import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenCategory;


/** 
 * Initializes properties editor kit settings. 
 * 
 * @author  Mila Metelka
 */
public class PropertiesSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "properties-settings-initializer";


    /** Construct <code>PropertiesSettingsInitializer</code>. */
    public PropertiesSettingsInitializer() {
        super(NAME);
    }


    /** Updates settings map for editor kit class. */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        
        // Properties colorings
        if(kitClass == BaseKit.class) {
            new PropertiesTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);
        } else if(kitClass == PropertiesKit.class) {
            // initialize color for shadowing cells in table view
            settingsMap.put(PropertiesOptions.SHADOW_TABLE_CELL_PROP, new Color(SystemColor.controlHighlight.getRGB()));

            settingsMap.put(SettingsNames.ABBREV_MAP, new TreeMap());
            
            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR, AcceptorFactory.JAVA_IDENTIFIER);

            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                new TokenContext[] {
                    PropertiesTokenContext.context
                }
            );
        }
    }


    /** Properties token coloring initializer. */
    private static class PropertiesTokenColoringInitializer extends SettingsUtil.TokenColoringInitializer {

        /** Bold font. */
        private static final Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
        /** Italic font. */
        private static final Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);

        /** Key coloring. */
        private static final Coloring keyColoring = new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE, Color.blue, null);
        /** Value coloring. */
        private static final Coloring valueColoring = new Coloring(null, Color.magenta, null);
        /** Comment coloring. */
        // #48502 - changed comment coloring to use non-italic font style
        private static final Coloring commentColoring = new Coloring(null, Color.gray, null);
        /** Empty coloring. */
        private static final Coloring emptyColoring = new Coloring(null, null, null);

        
        /** Constructs <code>PropertiesTokenColoringInitializer</code>. */
        public PropertiesTokenColoringInitializer() {
            super(PropertiesTokenContext.context);
        }


        /** Gets token coloring. */
        public Object getTokenColoring(TokenContextPath tokenContextPath,
            TokenCategory tokenIDOrCategory, boolean printingSet) {

            if(!printingSet) {
                int tokenID = tokenIDOrCategory.getNumericID();
                
                if(tokenID == PropertiesTokenContext.KEY_ID) {
                    return keyColoring;
                } else if(tokenID == PropertiesTokenContext.VALUE_ID) {
                    return valueColoring;
                } else if(tokenID == PropertiesTokenContext.LINE_COMMENT_ID) {
                    return commentColoring;
                } else if(tokenID == PropertiesTokenContext.EQ_ID
                            || tokenID == PropertiesTokenContext.TEXT_ID) {
                    return emptyColoring;
                }
            } else { // printing set
                 return SettingsUtil.defaultPrintColoringEvaluator;
            }

            return null;
        }
        
    } // End of class PropertiesTokenColoringInitializer.

}
