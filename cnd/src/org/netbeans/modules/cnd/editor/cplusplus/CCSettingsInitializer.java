/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.cplusplus;

import java.util.Map;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.ext.ExtSettingsNames;

/** Extended settings for CC */ 
public class CCSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "cc-settings-initializer";  //NOI18N

    private Class ccKitClass;

    /**
     * Construct new cc-settings-initializer.
     * @param ccKitClass the real kit class for which the settings are created.
     *   It's unknown here so it must be passed to this constructor.
     */
    public CCSettingsInitializer(Class ccKitClass) {
        super(NAME);
        this.ccKitClass = ccKitClass;
    }

    /**
     *  Update map filled with the settings.
     *
     *  @param kitClass kit class for which the settings are being updated. It is always non-null value.
     *  @param settingsMap map holding [setting-name, setting-value] pairs.
     *   The map can be empty if this is the first initializer
     *   that updates it or if no previous initializers updated it.
     */
    @SuppressWarnings("unchecked")
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        
        if (kitClass == ccKitClass) {
            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                new TokenContext[] { CCTokenContext.context });

            settingsMap.put(ExtSettingsNames.CARET_SIMPLE_MATCH_BRACE,
                            CCSettingsDefaults.defaultCaretSimpleMatchBrace);

            settingsMap.put(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE,
                            CCSettingsDefaults.defaultHighlightMatchBrace);

            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR,
                            CCSettingsDefaults.defaultCCIdentifierAcceptor);

            settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR,
                            CCSettingsDefaults.defaultAbbrevResetAcceptor);

            settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE,
                            CCSettingsDefaults.defaultWordMatchMatchCase);

            settingsMap.put(SettingsNames.WORD_MATCH_STATIC_WORDS,
                            CCSettingsDefaults.defaultWordMatchStaticWords);

            // Formatting settings
            settingsMap.put(CCSettingsNames.CC_FORMAT_SPACE_BEFORE_PARENTHESIS,
                            CCSettingsDefaults.defaultCCFormatSpaceBeforeParenthesis);

            settingsMap.put(CCSettingsNames.CC_FORMAT_SPACE_AFTER_COMMA,
                            CCSettingsDefaults.defaultCCFormatSpaceAfterComma);

            settingsMap.put(CCSettingsNames.CC_FORMAT_NEWLINE_BEFORE_BRACE,
                            CCSettingsDefaults.defaultCCFormatNewlineBeforeBrace);

            settingsMap.put(CCSettingsNames.CC_FORMAT_LEADING_SPACE_IN_COMMENT,
                            CCSettingsDefaults.defaultCCFormatLeadingSpaceInComment);

            settingsMap.put(CCSettingsNames.CC_FORMAT_LEADING_STAR_IN_COMMENT,
                            CCSettingsDefaults.defaultCCFormatLeadingStarInComment);

            settingsMap.put(CCSettingsNames.INDENT_HOT_CHARS_ACCEPTOR,
                            CCSettingsDefaults.defaultIndentHotCharsAcceptor);

	    // Code folding settings
	    settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, CCSettingsDefaults.defaultCCCodeFoldingEnable);
            
	    settingsMap.put(SettingsNames.PAIR_CHARACTERS_COMPLETION,
			    CCSettingsDefaults.defaultPairCharactersCompletion);
            
            settingsMap.put(ExtSettingsNames.JAVADOC_AUTO_POPUP,
                            CCSettingsDefaults.defaultCCDocAutoPopup);
        }
    }
}
 
