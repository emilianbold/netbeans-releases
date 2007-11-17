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

package org.netbeans.editor.ext;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeyBinding;

/**
* Initializer for the extended editor settings.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtSettingsInitializer extends Settings.AbstractInitializer {

    public static final String NAME = "ext-settings-initializer"; // NOI18N

    public ExtSettingsInitializer() {
        super(NAME);
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {

        // ------------------------ BaseKit Settings --------------------------------------
//        if (kitClass == BaseKit.class) {
//            // Add key-bindings
//            SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST,
//                                           ExtSettingsDefaults.defaultExtKeyBindings);
//        }

        // ------------------------ ExtKit Settings --------------------------------------
        if (kitClass == ExtKit.class) {

            // List of the additional colorings
            SettingsUtil.updateListSetting(settingsMap, SettingsNames.COLORING_NAME_LIST,
                                           new String[] {
                                               ExtSettingsNames.HIGHLIGHT_CARET_ROW_COLORING,
                                               ExtSettingsNames.HIGHLIGHT_MATCH_BRACE_COLORING,
                                           }
                                          );

            // ExtCaret highlighting options
            settingsMap.put(ExtSettingsNames.HIGHLIGHT_CARET_ROW,
                            ExtSettingsDefaults.defaultHighlightCaretRow);
            settingsMap.put(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE,
                            ExtSettingsDefaults.defaultHighlightMatchBrace);

            // ExtCaret highlighting colorings
            SettingsUtil.setColoring(settingsMap, ExtSettingsNames.HIGHLIGHT_CARET_ROW_COLORING,
                                     ExtSettingsDefaults.defaultHighlightCaretRowColoring);
            SettingsUtil.setColoring(settingsMap, ExtSettingsNames.HIGHLIGHT_MATCH_BRACE_COLORING,
                                     ExtSettingsDefaults.defaultHighlightMatchBraceColoring);

            // Popup menu default action names
            String[] popupMenuActionNames
                = new String[] {
                    BaseKit.cutAction,
                    BaseKit.copyAction,
                    BaseKit.pasteAction,
                    null,
                    BaseKit.removeSelectionAction
                };

            
            List pml = (List)settingsMap.get(ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST);
            if (pml == null || pml.indexOf(BaseKit.cutAction) == -1) {
                SettingsUtil.updateListSetting(settingsMap,
                    ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST, popupMenuActionNames);

                SettingsUtil.updateListSetting(settingsMap,
                    ExtSettingsNames.DIALOG_POPUP_MENU_ACTION_NAME_LIST, popupMenuActionNames);
            }
                                          
            settingsMap.put(ExtSettingsNames.POPUP_MENU_ENABLED, Boolean.TRUE);

            settingsMap.put(ExtSettingsNames.FAST_IMPORT_PACKAGE,
                            ExtSettingsDefaults.defaultFastImportPackage);
            
            // Completion settings
            settingsMap.put(ExtSettingsNames.COMPLETION_AUTO_POPUP,
                            ExtSettingsDefaults.defaultCompletionAutoPopup);

            settingsMap.put(ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
                            ExtSettingsDefaults.defaultCompletionCaseSensitive);

            settingsMap.put(ExtSettingsNames.COMPLETION_NATURAL_SORT,
                            ExtSettingsDefaults.defaultCompletionNaturalSort);
            
            settingsMap.put(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION,
                            ExtSettingsDefaults.defaultCompletionInstantSubstitution);

            settingsMap.put(ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY,
                            ExtSettingsDefaults.defaultCompletionAutoPopupDelay);

            settingsMap.put(ExtSettingsNames.COMPLETION_REFRESH_DELAY,
                            ExtSettingsDefaults.defaultCompletionRefreshDelay);

            settingsMap.put(ExtSettingsNames.COMPLETION_PANE_MIN_SIZE,
                            ExtSettingsDefaults.defaultCompletionPaneMinSize);

            settingsMap.put(ExtSettingsNames.COMPLETION_PANE_MAX_SIZE,
                            ExtSettingsDefaults.defaultCompletionPaneMaxSize);

            // re-indentation settings
            settingsMap.put(ExtSettingsNames.REINDENT_WITH_TEXT_BEFORE,
                            Boolean.TRUE);
            
            settingsMap.put(ExtSettingsNames.JAVADOC_BG_COLOR,
                            ExtSettingsDefaults.defaultJavaDocBGColor);
            
            settingsMap.put(ExtSettingsNames.JAVADOC_AUTO_POPUP_DELAY,
                            ExtSettingsDefaults.defaultJavaDocAutoPopupDelay);
            
            settingsMap.put(ExtSettingsNames.JAVADOC_PREFERRED_SIZE,
                            ExtSettingsDefaults.defaultJavaDocPreferredSize);
            
            settingsMap.put(ExtSettingsNames.JAVADOC_AUTO_POPUP,
                            ExtSettingsDefaults.defaultJavaDocAutoPopup);
            
        }

    }

}
