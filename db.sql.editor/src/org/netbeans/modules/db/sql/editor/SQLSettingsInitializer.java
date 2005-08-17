/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.editor;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.openide.util.NbBundle;

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
        if (kitClass == BaseKit.class) {
            new SQLTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);
        }

        if (kitClass == SQLEditorKit.class) {
            SettingsUtil.updateListSetting(
                    settingsMap, 
                    SettingsNames.TOKEN_CONTEXT_LIST,
                    new TokenContext[] { SQLTokenContext.context }
            );
        }
    }
    
    /**
     * Class for adding syntax coloring to the editor
     */
    static class SQLTokenColoringInitializer extends SettingsUtil.TokenColoringInitializer {

        Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
        Settings.Evaluator lightGraySubst = 
                new SettingsUtil.ForeColorPrintColoringEvaluator(Color.lightGray);

        /**
         * Constructor
         */
        public SQLTokenColoringInitializer() {
            super(SQLTokenContext.context);
        }

        /**
         * Get colors for the SQL tokens
         */
        public Object getTokenColoring(
                TokenContextPath tokenContextPath,
                TokenCategory tokenIDOrCategory, 
                boolean printingSet) {
            // get the ones for non printing selecting some sensible defaults
            if (!printingSet) {
                int id = tokenIDOrCategory.getNumericID();
                switch (id) {
                    case SQLTokenContext.WHITESPACE_ID:
                        return SettingsDefaults.emptyColoring;
                    case SQLTokenContext.LINE_COMMENT_ID:
                        return new Coloring(
                                null,
                                Color.gray, 
                                null);
                    case SQLTokenContext.BLOCK_COMMENT_ID:
                        return new Coloring(
                                null,
                                Color.gray, 
                                null);
                    case SQLTokenContext.STRING_ID:
                        return new Coloring(
                                null, 
                                Color.red.darker(), 
                                null);
                    case SQLTokenContext.INCOMPLETE_STRING_ID:
                        return new Coloring(
                                null, 
                                Color.white, 
                                Color.red);
                    case SQLTokenContext.IDENTIFIER_ID:
                        return new Coloring(
                                null, 
                                Color.blue, 
                                null);
                    case SQLTokenContext.INVALID_CHARACTER_ID:
                        return new Coloring(
                                null, 
                                Color.white, 
                                Color.red);
                    case SQLTokenContext.OPERATOR_ID:
                        return new Coloring(
                                null, 
                                Color.black, 
                                null);
                    case SQLTokenContext.INVALID_COMMENT_END_ID:
                        return new Coloring(
                                null, 
                                Color.white, 
                                Color.red);
                    case SQLTokenContext.INT_LITERAL_ID:
                    case SQLTokenContext.DOUBLE_LITERAL_ID:
                        return new Coloring(
                                null, 
                                Color.red.darker(), 
                                null);
                    case SQLTokenContext.KEYWORD_ID:
                        return new Coloring(
                                boldFont, 
                                Coloring.FONT_MODE_APPLY_STYLE, 
                                Color.blue.darker().darker(), 
                                null);
                }

            } else { 
                // get the set for printing (no color)
                switch (tokenIDOrCategory.getNumericID()) {
                    case SQLTokenContext.BLOCK_COMMENT_ID:
                    case SQLTokenContext.LINE_COMMENT_ID:
                        return lightGraySubst;

                    default:
                         return SettingsUtil.defaultPrintColoringEvaluator;
                }
            }
            
            return null;
        }
    }
}
