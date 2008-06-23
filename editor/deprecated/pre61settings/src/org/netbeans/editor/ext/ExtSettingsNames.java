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
    public static final String POPUP_MENU_ACTION_NAME_LIST = "popup-menu-action-name-list"; // NOI18N

    /** List of the action names that should be shown in the popup menu
     * when JEditorPane is shown in the dialogs. It corresponds
     * Null name means separator.
     * Values: java.util.List containing java.lang.String instances
     */
    public static final String DIALOG_POPUP_MENU_ACTION_NAME_LIST = "dialog-popup-menu-action-name-list"; // NOI18N

    /** Whether popup menu will be displayed on mouse right-click or not.
     * It's set to true by default.
     * Values: java.lang.Boolean
     */
    public static final String POPUP_MENU_ENABLED = "popup-menu-enabled"; // NOI18N

    /** Highlight the row where the caret currently is. The ExtCaret must be used.
     * Values: java.lang.Boolean 
     * @deprecated Without any replacement.
     */
    public static final String HIGHLIGHT_CARET_ROW = "highlight-caret-row"; // NOI18N

    /** Highlight the matching brace (if the caret currently stands after the brace).
     * The ExtCaret must be used.
     * Values: java.lang.Boolean 
     * @deprecated Without any replacement.
     */
    public static final String HIGHLIGHT_MATCH_BRACE = "highlight-match-brace"; // NOI18N

    /** 
     * Coloring used to highlight the row where the caret resides
     * @deprecated Without any replacement.
     */
    public static final String HIGHLIGHT_CARET_ROW_COLORING = "highlight-caret-row"; // NOI18N

    /** 
     * Coloring used to highlight the matching brace
     * @deprecated Without any replacement.
     */
    public static final String HIGHLIGHT_MATCH_BRACE_COLORING = "highlight-match-brace"; // NOI18N

    /** Delay (milliseconds) after which the matching brace
     * will be updated. This is intended to eliminate flicker
     * if the user holds the arrow key pressed.
     * @deprecated Without any replacement.
     */
    public static final String HIGHLIGHT_MATCH_BRACE_DELAY = "highlight-match-brace-delay"; // NOI18N

    /** Whether the fast and simple matching should be used for higlighting
     * the matching brace. Its disadvantage is that it doesn't ignore the comments
     * and string and character constants in the search.
     * @deprecated Without any replacement.
     */
    public static final String CARET_SIMPLE_MATCH_BRACE = "caret-simple-match-brace"; // NOI18N

    /** Whether the code completion window should popup automatically.
    * Values: java.lang.Boolean
    */
    public static final String COMPLETION_AUTO_POPUP = "completion-auto-popup"; // NOI18N

    /** Whether the code completion query search will be case  sensitive
    * Values: java.lang.Boolean
    */
    public static final String COMPLETION_CASE_SENSITIVE = "completion-case-sensitive"; // NOI18N

    /** Whether the code completion sorting will be natural
    * Values: java.lang.Boolean
    */
    public static final String COMPLETION_NATURAL_SORT = "completion-natural-sort"; // NOI18N
    
    /** Whether perform instant substitution, if the search result contains only one item
    * Values: java.lang.Boolean
    */
    public static final String COMPLETION_INSTANT_SUBSTITUTION = "completion-instant-substitution"; // NOI18N

    /** The delay after which the completion window is shown automatically.
    * Values: java.lang.Integer
    */
    public static final String COMPLETION_AUTO_POPUP_DELAY = "completion-auto-popup-delay"; // NOI18N

    /** The delay after which the completion window is refreshed.
    * Values: java.lang.Integer
    */
    public static final String COMPLETION_REFRESH_DELAY = "completion-refresh-delay"; // NOI18N

    /** The minimum size of the completion pane component.
    * Values: java.awt.Dimension
    */
    public static final String COMPLETION_PANE_MIN_SIZE = "completion-pane-min-size"; // NOI18N

    /** The maximum size of the completion pane component.
    * Values: java.awt.Dimension
    */
    public static final String COMPLETION_PANE_MAX_SIZE = "completion-pane-max-size"; // NOI18N

    /** Acceptor sensitive to characters that cause that
     * that the current line will be reformatted immediately.
     */
    public static final String INDENT_HOT_CHARS_ACCEPTOR = "indent-hot-chars-acceptor"; // NOI18N

    /** Whether lines should be indented on an indent hot key if there is non whitespace before
     * the typed hot key. See editor issue #10771.
     * Values: java.lang.Boolean
     */
    public static final String REINDENT_WITH_TEXT_BEFORE = "reindent-with-text-before"; // NOI18N

    /** Whether the fast import should offer packages instead of classes
     * Values: java.lang.Integer
     */
    public static final String FAST_IMPORT_SELECTION = "fast-import-selection"; // NOI18N
    
    /** Whether the fast import should offer packages instead of classes
     * Values: java.lang.Boolean
     * @deprecated replaced by FAST_IMPORT_SELECTION
     */
    public static final String FAST_IMPORT_PACKAGE = "fast-import-package"; // NOI18N
    
    /** Background color of javaDoc popup window 
     *  Values: java.awt.Color
     * @deprecated Use Editor Settings and Settings Storage APIs.
     */
    public static final String JAVADOC_BG_COLOR = "javadoc-bg-color"; //NOI18N
    
    /** The delay after which the javaDoc window is shown automatically.
    *   Values: java.lang.Integer
    */
    public static final String JAVADOC_AUTO_POPUP_DELAY = "javadoc-auto-popup-delay"; //NOI18N
    
    /** The preferred size of javaDoc popup window
    *   Values: java.awt.Dimension
    */ 
    public static final String JAVADOC_PREFERRED_SIZE = "javadoc-preferred-size"; //NOI18N

    /** Whether the javaDoc window should popup automatically.
    * Values: java.lang.Boolean
    */
    public static final String JAVADOC_AUTO_POPUP = "javadoc-auto-popup"; // NOI18N
    
    /** Whether show deprecated members in code completion popup window
    * Values: java.lang.Boolean
    */
    public static final String SHOW_DEPRECATED_MEMBERS = "show-deprecated-members"; // NOI18N
    
    /**
     * The Code Completion DB is always updated after mounting a new filesystem.
     * @deprecated Without any replacement.
     */
    public static final String ALWAYS = "pd_always";//NOI18N
    /** 
     * The Code Completion DB is never updated after mounting a new filesystem.
     * @deprecated Without any replacement.
     */
    public static final String NEVER = "pd_never";//NOI18N
    
}
