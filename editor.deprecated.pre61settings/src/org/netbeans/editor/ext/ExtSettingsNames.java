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

import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;

/**
* Names of the extended editor settings.
*
* @author Miloslav Metelka
* @version 1.00
*/
public class ExtSettingsNames extends SettingsNames {

    /** List of the action names that should be shown in the popup menu.
    * Null name means separator.
    * Values: java.util.List containing java.lang.String instances
    */
    public static final String POPUP_MENU_ACTION_NAME_LIST = EditorPreferencesKeys.POPUP_MENU_ACTION_NAME_LIST; // NOI18N

    /** List of the action names that should be shown in the popup menu
     * when JEditorPane is shown in the dialogs. It corresponds
     * Null name means separator.
     * Values: java.util.List containing java.lang.String instances
     */
    public static final String DIALOG_POPUP_MENU_ACTION_NAME_LIST = EditorPreferencesKeys.DIALOG_POPUP_MENU_ACTION_NAME_LIST; // NOI18N

    /** Whether popup menu will be displayed on mouse right-click or not.
     * It's set to true by default.
     * Values: java.lang.Boolean
     */
    public static final String POPUP_MENU_ENABLED = EditorPreferencesKeys.POPUP_MENU_ENABLED; // NOI18N

    /** Highlight the row where the caret currently is. The ExtCaret must be used.
     * Values: java.lang.Boolean 
     * @deprecated Without any replacement.
     */
    public static final String HIGHLIGHT_CARET_ROW = EditorPreferencesKeys.HIGHLIGHT_CARET_ROW; // NOI18N

    /** Highlight the matching brace (if the caret currently stands after the brace).
     * The ExtCaret must be used.
     * Values: java.lang.Boolean 
     * @deprecated Without any replacement.
     */
    public static final String HIGHLIGHT_MATCH_BRACE = EditorPreferencesKeys.HIGHLIGHT_MATCH_BRACE; // NOI18N

    /** 
     * Coloring used to highlight the row where the caret resides
     * @deprecated Without any replacement.
     */
    public static final String HIGHLIGHT_CARET_ROW_COLORING = EditorPreferencesKeys.HIGHLIGHT_CARET_ROW_COLORING; // NOI18N

    /** 
     * Coloring used to highlight the matching brace
     * @deprecated Without any replacement.
     */
    public static final String HIGHLIGHT_MATCH_BRACE_COLORING = EditorPreferencesKeys.HIGHLIGHT_MATCH_BRACE_COLORING; // NOI18N

    /** Delay (milliseconds) after which the matching brace
     * will be updated. This is intended to eliminate flicker
     * if the user holds the arrow key pressed.
     * @deprecated Without any replacement.
     */
    public static final String HIGHLIGHT_MATCH_BRACE_DELAY = EditorPreferencesKeys.HIGHLIGHT_MATCH_BRACE_DELAY; // NOI18N

    /** Whether the fast and simple matching should be used for higlighting
     * the matching brace. Its disadvantage is that it doesn't ignore the comments
     * and string and character constants in the search.
     * @deprecated Without any replacement.
     */
    public static final String CARET_SIMPLE_MATCH_BRACE = EditorPreferencesKeys.CARET_SIMPLE_MATCH_BRACE; // NOI18N

    /** Whether the code completion window should popup automatically.
    * Values: java.lang.Boolean
    */
    public static final String COMPLETION_AUTO_POPUP = EditorPreferencesKeys.COMPLETION_AUTO_POPUP; // NOI18N

    /** Whether the code completion query search will be case  sensitive
    * Values: java.lang.Boolean
    */
    public static final String COMPLETION_CASE_SENSITIVE = EditorPreferencesKeys.COMPLETION_CASE_SENSITIVE; // NOI18N

    /** Whether the code completion sorting will be natural
    * Values: java.lang.Boolean
    */
    public static final String COMPLETION_NATURAL_SORT = EditorPreferencesKeys.COMPLETION_NATURAL_SORT; // NOI18N
    
    /** Whether perform instant substitution, if the search result contains only one item
    * Values: java.lang.Boolean
    */
    public static final String COMPLETION_INSTANT_SUBSTITUTION = EditorPreferencesKeys.COMPLETION_INSTANT_SUBSTITUTION; // NOI18N

    /** The delay after which the completion window is shown automatically.
    * Values: java.lang.Integer
    */
    public static final String COMPLETION_AUTO_POPUP_DELAY = EditorPreferencesKeys.COMPLETION_AUTO_POPUP_DELAY; // NOI18N

    /** The delay after which the completion window is refreshed.
    * Values: java.lang.Integer
    */
    public static final String COMPLETION_REFRESH_DELAY = EditorPreferencesKeys.COMPLETION_REFRESH_DELAY; // NOI18N

    /** The minimum size of the completion pane component.
    * Values: java.awt.Dimension
    */
    public static final String COMPLETION_PANE_MIN_SIZE = EditorPreferencesKeys.COMPLETION_PANE_MIN_SIZE; // NOI18N

    /** The maximum size of the completion pane component.
    * Values: java.awt.Dimension
    */
    public static final String COMPLETION_PANE_MAX_SIZE = EditorPreferencesKeys.COMPLETION_PANE_MAX_SIZE; // NOI18N

    /** Acceptor sensitive to characters that cause that
     * that the current line will be reformatted immediately.
     */
    public static final String INDENT_HOT_CHARS_ACCEPTOR = EditorPreferencesKeys.INDENT_HOT_CHARS_ACCEPTOR; // NOI18N

    /** Whether lines should be indented on an indent hot key if there is non whitespace before
     * the typed hot key. See editor issue #10771.
     * Values: java.lang.Boolean
     */
    public static final String REINDENT_WITH_TEXT_BEFORE = EditorPreferencesKeys.REINDENT_WITH_TEXT_BEFORE; // NOI18N

    /** Whether the fast import should offer packages instead of classes
     * Values: java.lang.Integer
     */
    public static final String FAST_IMPORT_SELECTION = EditorPreferencesKeys.FAST_IMPORT_SELECTION; // NOI18N
    
    /** Whether the fast import should offer packages instead of classes
     * Values: java.lang.Boolean
     * @deprecated replaced by FAST_IMPORT_SELECTION
     */
    public static final String FAST_IMPORT_PACKAGE = EditorPreferencesKeys.FAST_IMPORT_PACKAGE; // NOI18N
    
    /** Background color of javaDoc popup window 
     *  Values: java.awt.Color
     * @deprecated Use Editor Settings and Settings Storage APIs.
     */
    public static final String JAVADOC_BG_COLOR = EditorPreferencesKeys.JAVADOC_BG_COLOR; //NOI18N
    
    /** The delay after which the javaDoc window is shown automatically.
    *   Values: java.lang.Integer
    */
    public static final String JAVADOC_AUTO_POPUP_DELAY = EditorPreferencesKeys.JAVADOC_AUTO_POPUP_DELAY; //NOI18N
    
    /** The preferred size of javaDoc popup window
    *   Values: java.awt.Dimension
    */ 
    public static final String JAVADOC_PREFERRED_SIZE = EditorPreferencesKeys.JAVADOC_PREFERRED_SIZE; //NOI18N

    /** Whether the javaDoc window should popup automatically.
    * Values: java.lang.Boolean
    */
    public static final String JAVADOC_AUTO_POPUP = EditorPreferencesKeys.JAVADOC_AUTO_POPUP; // NOI18N
    
    /** Whether show deprecated members in code completion popup window
    * Values: java.lang.Boolean
    */
    public static final String SHOW_DEPRECATED_MEMBERS = EditorPreferencesKeys.SHOW_DEPRECATED_MEMBERS; // NOI18N
    
    /**
     * The Code Completion DB is always updated after mounting a new filesystem.
     * @deprecated Without any replacement.
     */
    public static final String ALWAYS = EditorPreferencesKeys.ALWAYS;//NOI18N
    /** 
     * The Code Completion DB is never updated after mounting a new filesystem.
     * @deprecated Without any replacement.
     */
    public static final String NEVER = EditorPreferencesKeys.NEVER;//NOI18N
    
}
