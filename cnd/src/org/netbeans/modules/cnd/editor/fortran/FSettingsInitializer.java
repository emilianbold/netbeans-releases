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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 
