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

package org.netbeans.modules.web.core.syntax.settings;

import org.netbeans.modules.web.core.syntax.settings.JspSettings;
import org.netbeans.modules.web.core.syntax.*;
import java.util.*;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.html.HTMLSettingsInitializer;
import org.netbeans.editor.ext.java.JavaSettingsDefaults;
import org.netbeans.editor.ext.java.JavaSettingsNames;
import org.netbeans.modules.editor.java.JavaKit;

public class JspMultiSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "jsp-multi-settings-initializer"; // NOI18N

    public JspMultiSettingsInitializer() {
        super(NAME);
    }

    public void updateSettingsMap (Class kitClass, Map settingsMap) {
	if (kitClass == JavaKit.class) {
            settingsMap.put(JavaSettingsNames.INDENT_HOT_CHARS_ACCEPTOR,
                    JavaSettingsDefaults.defaultIndentHotCharsAcceptor);
        }
        
        // Jsp Settings
        if (kitClass == JSPKit.class) {
            settingsMap.put(JavaSettingsNames.PAIR_CHARACTERS_COMPLETION,
                        JavaSettingsDefaults.defaultPairCharactersCompletion);

            //enable code folding
            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, JavaSettingsDefaults.defaultCodeFoldingEnable);
            settingsMap.put(JspSettings.CODE_FOLDING_UPDATE_TIMEOUT, JspSettings.defaultCodeFoldingUpdateInterval);
            settingsMap.put(JspSettings.CARET_SIMPLE_MATCH_BRACE, JspSettings.defaultCaretSimpleMatchBrace);
            
            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR,
                            HTMLSettingsInitializer.HTML_IDENTIFIER_ACCEPTOR);
        }
    }
}

