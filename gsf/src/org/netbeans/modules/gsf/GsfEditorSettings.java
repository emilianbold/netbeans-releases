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
package org.netbeans.modules.gsf;

import java.util.Map;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.editor.NbEditorDocument;


/**
 * The classes in here no one should ever implement, as I would think all this
 * information could be defined in a more declarative way: either via a simple interface
 * implementation where returning specific flags enables a set of settings
 * or a table or some external xml-like file.
 * Maybe that is all there already in NetBeans but I could not find it.
 * This is called from the ModuleInstall class and it's key for the editor to work.
 */
public class GsfEditorSettings extends Settings.AbstractInitializer {
    private static final String SETTINGS_NAME = "gls-editor-settings-initializer"; // NOI18N

    public GsfEditorSettings() {
        super(SETTINGS_NAME);
    }
    
    public static final Acceptor defaultAbbrevResetAcceptor = new Acceptor() {
          public final boolean accept(char ch) {
              return !Character.isJavaIdentifierPart(ch) && ch != ':';
          }
      };

    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        if (kitClass == null) {
            return;
        }

        if (kitClass == GsfEditorKitFactory.GsfEditorKit.class) {
            // This is wrong; I should be calling Formatter.indentSize() to get the default,
            // but I can't get to the mime type from here. In 6.0 the editor settings are
            // being redone so I can hopefully fix this soon.
            settingsMap.put(SettingsNames.SPACES_PER_TAB, Integer.valueOf(2));
            //settingsMap.put(SettingsNames.INDENT_SHIFT_WIDTH, Integer.valueOf(2));
            settingsMap.put(ExtSettingsNames.CARET_SIMPLE_MATCH_BRACE, Boolean.FALSE);
            settingsMap.put(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE, Boolean.TRUE);
            settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE, Boolean.TRUE);
            settingsMap.put(ExtSettingsNames.REINDENT_WITH_TEXT_BEFORE, Boolean.FALSE);
            settingsMap.put(ExtSettingsNames.COMPLETION_AUTO_POPUP, Boolean.TRUE);
            settingsMap.put(SettingsNames.PAIR_CHARACTERS_COMPLETION, Boolean.TRUE);
                    
            settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR, defaultAbbrevResetAcceptor);

            //ExtSettingsNames.SHOW_DEPRECATED_MEMBERS
            //ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION
            //ExtSettingsNames.COMPLETION_CASE_SENSITIVE
            //settingsMap.put(SettingsNames.WORD_MATCH_STATIC_WORDS,
            //                defaultWordMatchStaticWords);
        }
    }

    //    public static final String defaultWordMatchStaticWords
    //    = "Exception IntrospectionException FileNotFoundException IOException" // NOI18N
    //      + " ArrayIndexOutOfBoundsException ClassCastException ClassNotFoundException" // NOI18N
    //      + " CloneNotSupportedException NullPointerException NumberFormatException" // NOI18N
    //      + " SQLException IllegalAccessException IllegalArgumentException"; // NOI18N
}
