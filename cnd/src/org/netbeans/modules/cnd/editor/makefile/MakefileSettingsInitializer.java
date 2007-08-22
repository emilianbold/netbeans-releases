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

package org.netbeans.modules.cnd.editor.makefile;

import java.util.Map;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtSettingsNames;

/**
* Extended settings for Makefile.
*
*/

public class MakefileSettingsInitializer extends Settings.AbstractInitializer {

  /** Name assigned to initializer */
  public static final String NAME = "makefile-settings-initializer"; //NOI18N

  private Class fKitClass;

  /** Construct new f-settings-initializer.
  * @param fKitClass the real kit class for which the settings are created.
  *   It's unknown here so it must be passed to this constructor.
  */
  public MakefileSettingsInitializer(Class fKitClass) {
    super(NAME);
    this.fKitClass = fKitClass;
  }

  /** Update map filled with the settings.
  * @param kitClass kit class for which the settings are being updated.
  *   It is always non-null value.
  * @param settingsMap map holding [setting-name, setting-value] pairs.
  *   The map can be empty if this is the first initializer
  *   that updates it or if no previous initializers updated it.
  */
    @SuppressWarnings("unchecked")
  public void updateSettingsMap(Class kitClass, Map settingsMap) {


    if (kitClass == fKitClass) {
      SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                   new TokenContext[] {
                                       MakefileTokenContext.context,
                   }
                  );

      settingsMap.put(ExtSettingsNames.CARET_SIMPLE_MATCH_BRACE,
                      MakefileSettingsDefaults.defaultCaretSimpleMatchBrace);

      settingsMap.put(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE,
                      MakefileSettingsDefaults.defaultHighlightMatchBrace);

      settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR,
                      MakefileSettingsDefaults.defaultIdentifierAcceptor);

      settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR,
                      MakefileSettingsDefaults.defaultAbbrevResetAcceptor);

      settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE,
                      MakefileSettingsDefaults.defaultMakeWordMatchMatchCase);

      settingsMap.put(SettingsNames.WORD_MATCH_STATIC_WORDS,
                      MakefileSettingsDefaults.defaultWordMatchStaticWords);

    }//if

  }//updateSettingsMap

}
