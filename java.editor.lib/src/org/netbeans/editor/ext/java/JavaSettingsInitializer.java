/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.editor.ext.java;

import java.util.Map;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.ext.ExtSettingsNames;

/**
* Extended settings for Java.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "java-settings-initializer"; // NOI18N

    private Class javaKitClass;

    /** Construct new java-settings-initializer.
    * @param javaKitClass the real kit class for which the settings are created.
    *   It's unknown here so it must be passed to this constructor.
    */
    public JavaSettingsInitializer(Class javaKitClass) {
        super(NAME);
        this.javaKitClass = javaKitClass;
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        if (kitClass == javaKitClass) {

            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                new TokenContext[] {
                    JavaTokenContext.context,
                    JavaLayerTokenContext.context
                }
            );

            settingsMap.put(ExtSettingsNames.CARET_SIMPLE_MATCH_BRACE,
                            JavaSettingsDefaults.defaultCaretSimpleMatchBrace);

            settingsMap.put(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE,
                            JavaSettingsDefaults.defaultHighlightMatchBrace);

            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR,
                            JavaSettingsDefaults.defaultIdentifierAcceptor);

            settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR,
                            JavaSettingsDefaults.defaultAbbrevResetAcceptor);

            settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE,
                            JavaSettingsDefaults.defaultWordMatchMatchCase);

            settingsMap.put(SettingsNames.WORD_MATCH_STATIC_WORDS,
                            JavaSettingsDefaults.defaultWordMatchStaticWords);

            // Formatting settings
            settingsMap.put(JavaSettingsNames.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS,
                            JavaSettingsDefaults.defaultJavaFormatSpaceBeforeParenthesis);

            settingsMap.put(JavaSettingsNames.JAVA_FORMAT_SPACE_AFTER_COMMA,
                            JavaSettingsDefaults.defaultJavaFormatSpaceAfterComma);

            settingsMap.put(JavaSettingsNames.JAVA_FORMAT_NEWLINE_BEFORE_BRACE,
                            JavaSettingsDefaults.defaultJavaFormatNewlineBeforeBrace);

            settingsMap.put(JavaSettingsNames.JAVA_FORMAT_LEADING_SPACE_IN_COMMENT,
                            JavaSettingsDefaults.defaultJavaFormatLeadingSpaceInComment);

            settingsMap.put(JavaSettingsNames.JAVA_FORMAT_LEADING_STAR_IN_COMMENT,
                            JavaSettingsDefaults.defaultJavaFormatLeadingStarInComment);

            settingsMap.put(JavaSettingsNames.INDENT_HOT_CHARS_ACCEPTOR,
                            JavaSettingsDefaults.defaultIndentHotCharsAcceptor);

            settingsMap.put(ExtSettingsNames.REINDENT_WITH_TEXT_BEFORE,
                            Boolean.FALSE);

	    settingsMap.put(JavaSettingsNames.PAIR_CHARACTERS_COMPLETION,
			    JavaSettingsDefaults.defaultPairCharactersCompletion);

            settingsMap.put(JavaSettingsNames.GOTO_CLASS_CASE_SENSITIVE,
                            JavaSettingsDefaults.defaultGotoClassCaseSensitive);

            settingsMap.put(JavaSettingsNames.GOTO_CLASS_SHOW_INNER_CLASSES,
                            JavaSettingsDefaults.defaultGotoClassShowInnerClasses);

            settingsMap.put(JavaSettingsNames.GOTO_CLASS_SHOW_LIBRARY_CLASSES,
                            JavaSettingsDefaults.defaultGotoClassShowLibraryClasses);
        }

    }

}
