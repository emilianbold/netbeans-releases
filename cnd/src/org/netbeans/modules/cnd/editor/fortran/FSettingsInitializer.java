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

package org.netbeans.modules.cnd.editor.fortran;

import java.util.Map;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.openide.actions.CutAction;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.ToolsAction;
import org.openide.actions.PropertiesAction;
import org.openide.windows.TopComponent;

/**
* Extended settings for Fortran.
*
*/

public class FSettingsInitializer extends Settings.AbstractInitializer {

  /** Name assigned to initializer */
  public static final String NAME = "f-settings-initializer";  //NOI18N

  private Class fKitClass;

  /** Construct new f-settings-initializer.
  * @param fKitClass the real kit class for which the settings are created.
  *   It's unknown here so it must be passed to this constructor.
  */
  public FSettingsInitializer(Class fKitClass) {
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
      SettingsUtil.updateListSetting(settingsMap, 
				     SettingsNames.TOKEN_CONTEXT_LIST,
				     new TokenContext[] {
					 FTokenContext.context,
					 //FLayerTokenContext.context
				     }
				     );

	    SettingsUtil.updateListSetting(settingsMap,
                         ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST,
		         new String [] { BaseKit.formatAction,
					 null,
					 TopComponent.class.getName(),
					 null,
					 //CompileAction.class.getName(),
					 //null,
                                         //org.openide.actions.ToggleBreakpointAction.class.getName(),
                                         //org.openide.actions.AddWatchAction.class.getName(),
					 //null,
					 CutAction.class.getName(),
					 CopyAction.class.getName(),
					 PasteAction.class.getName(),
					 null,
					 DeleteAction.class.getName(),
					 null,
					 NewAction.class.getName(),
					 null,
					 FKit.gotoHelpAction,
					 null,
					 ToolsAction.class.getName(),
					 BaseKit.generateGutterPopupAction,
					 PropertiesAction.class.getName() }
				  );

      settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR,
                      FSettingsDefaults.defaultIdentifierAcceptor);

      settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR,
                      FSettingsDefaults.defaultAbbrevResetAcceptor);

      settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE,
                      FSettingsDefaults.defaultFortranWordMatchMatchCase);

      settingsMap.put(SettingsNames.WORD_MATCH_STATIC_WORDS,
                      FSettingsDefaults.defaultWordMatchStaticWords);

      // Formatting settings
      /* remove settings XXX not needed yet
      settingsMap.put(FSettingsNames.FORMAT_SPACE_AFTER_COMMA,
                      FSettingsDefaults.defaultFormatSpaceAfterComma);

      settingsMap.put(FSettingsNames.FREE_FORMAT,
                      FSettingsDefaults.defaultFreeFormat);
      */

      settingsMap.put(FSettingsNames.INDENT_HOT_CHARS_ACCEPTOR,
                      FSettingsDefaults.defaultIndentHotCharsAcceptor);

      // Separator line at 72: everything else is ignored...
      settingsMap.put(SettingsNames.TEXT_LIMIT_WIDTH,
                      new Integer(FSettingsDefaults.maximumTextWidth));
    }//if

  }//updateSettingsMap

}
 
