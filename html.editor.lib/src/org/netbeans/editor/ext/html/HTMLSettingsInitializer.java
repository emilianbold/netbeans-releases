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

package org.netbeans.editor.ext.html;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenCategory;

/**
* Extended settings provide the settings for the extended editor features
* supported by the various classes of this package.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class HTMLSettingsInitializer extends Settings.AbstractInitializer {

    private final Class htmlKitClass;

    /** Name assigned to initializer */
    public static final String NAME = "html-settings-initializer"; // NOI18N
    
    public static final Acceptor HTML_IDENTIFIER_ACCEPTOR = 
        new Acceptor() {
            public final boolean accept(char ch) {
                return (ch == ':') || AcceptorFactory.JAVA_IDENTIFIER.accept(ch);
            }
        };


    /** Construct HTML Settings initializer
    * @param htmlKitClass the real kit class for which the settings are created.
    *   It's unknown here so it must be passed to this constructor.
    */ 
    public HTMLSettingsInitializer(Class htmlKitClass) {
        super(NAME);
        this.htmlKitClass = htmlKitClass;
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {

        if (kitClass == BaseKit.class)  {

            new HTMLTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);

        }

        if (kitClass == htmlKitClass) {

            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                new TokenContext[] {
                    HTMLTokenContext.context
                }
            );
            
            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR,
                            HTML_IDENTIFIER_ACCEPTOR);

            settingsMap.put(HTMLSettingsNames.COMPLETION_LOWER_CASE,
                            HTMLSettingsDefaults.defaultCompletionLowerCase);
            
        }

    }

    static class HTMLTokenColoringInitializer
    extends SettingsUtil.TokenColoringInitializer {

        Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
        Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
        Settings.Evaluator boldSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.BOLD);
        Settings.Evaluator italicSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);
        Settings.Evaluator lightGraySubst = new SettingsUtil.ForeColorPrintColoringEvaluator(Color.lightGray);

        public HTMLTokenColoringInitializer() {
            super(HTMLTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath,
        TokenCategory tokenIDOrCategory, boolean printingSet) {
            if (!printingSet) {
                switch (tokenIDOrCategory.getNumericID()) {
                    case HTMLTokenContext.TEXT_ID:
                    case HTMLTokenContext.WS_ID:
                        return SettingsDefaults.emptyColoring;

                    case HTMLTokenContext.ERROR_ID:
                        return new Coloring(null, Color.white, Color.red);

                    case HTMLTokenContext.TAG_CATEGORY_ID:
                        return new Coloring(null, Color.blue, null);

                    case HTMLTokenContext.ARGUMENT_ID:
                        return new Coloring(null, Color.green.darker().darker(), null);

                    case HTMLTokenContext.OPERATOR_ID:
                        return new Coloring(null, Color.green.darker().darker(), null);

                    case HTMLTokenContext.VALUE_ID:
                        return new Coloring(null, new Color(153, 0, 107), null);

                    case HTMLTokenContext.BLOCK_COMMENT_ID:
                        // #48502 - comment changed to non-italic font style
                        return new Coloring(null, Color.gray, null);

                    case HTMLTokenContext.SGML_COMMENT_ID:
                        return new Coloring( null, Color.gray, null );

                    case HTMLTokenContext.DECLARATION_ID:
                        return new Coloring(null, new Color(191, 146, 33), null);

                    case HTMLTokenContext.CHARACTER_ID:
                        return new Coloring(null, Color.red.darker(), null);
                        
                }

            } else { // printing set
                switch (tokenIDOrCategory.getNumericID()) {
                    case HTMLTokenContext.BLOCK_COMMENT_ID:
                    case HTMLTokenContext.SGML_COMMENT_ID:
                        return lightGraySubst;

                    default:
                         return SettingsUtil.defaultPrintColoringEvaluator;
                }

            }

            return null;

        }

    }
     
}
