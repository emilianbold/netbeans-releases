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
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeyBinding;

/**
* Initializer for the extended editor settings.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtSettingsDefaults extends SettingsDefaults {

    // Highlight row with caret coloring
    public static final Color defaultHighlightCaretRowBackColor = new Color(255, 255, 220);
    public static final Coloring defaultHighlightCaretRowColoring = new Coloring(null, null, defaultHighlightCaretRowBackColor);
    // Highlight matching brace coloring
    public static final Color defaultHighlightMatchBraceForeColor = Color.white;
    public static final Color defaultHighlightMatchBraceBackColor = new Color(255, 50, 210);
    public static final Coloring defaultHighlightMatchBraceColoring = new Coloring(null, defaultHighlightMatchBraceForeColor, defaultHighlightMatchBraceBackColor);

    public static final Boolean defaultHighlightCaretRow = Boolean.TRUE;
    public static final Boolean defaultHighlightMatchBrace = Boolean.TRUE;
    public static final Integer defaultHighlightMatchBraceDelay = new Integer(100);
    public static final Boolean defaultCaretSimpleMatchBrace = Boolean.TRUE;

    public static final Boolean defaultCompletionAutoPopup = Boolean.TRUE;
    public static final Boolean defaultCompletionCaseSensitive = Boolean.FALSE;
    public static final Boolean defaultCompletionNaturalSort = Boolean.FALSE;    
    public static final Integer defaultCompletionAutoPopupDelay = new Integer(250);
    public static final Integer defaultCompletionRefreshDelay = new Integer(200);
    public static final Dimension defaultCompletionPaneMaxSize = new Dimension(400, 300);
    public static final Dimension defaultCompletionPaneMinSize = new Dimension(60, 17);
    public static final Boolean defaultFastImportPackage = Boolean.FALSE;    
    public static final Integer defaultFastImportSelection = new Integer(0);
    public static final Boolean defaultShowDeprecatedMembers = Boolean.TRUE;
    public static final Boolean defaultCompletionInstantSubstitution = Boolean.TRUE;    
    
    public static final Color defaultJavaDocBGColor = new Color(247, 247, 255);
    public static final Integer defaultJavaDocAutoPopupDelay = new Integer(200);
    public static final Dimension defaultJavaDocPreferredSize = new Dimension(500, 300);    
    public static final Boolean defaultJavaDocAutoPopup = Boolean.TRUE;
    private static int MENU_MASK = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    
//    public static final MultiKeyBinding[] defaultExtKeyBindings
//    = new MultiKeyBinding[] {
//          /*new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.ALT_MASK),
//              ExtKit.gotoDeclarationAction
//          ),
//           */
//          new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_F, MENU_MASK),
//              ExtKit.findAction
//          ),
//          new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_FIND, 0),
//              ExtKit.findAction
//          ),
//          new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_H, MENU_MASK),
//              ExtKit.replaceAction
//          ),
//          new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK),
//              ExtKit.gotoAction
//          ),
//          new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK),
//              ExtKit.completionShowAction
//          ),
//          new MultiKeyBinding( // Japanese Solaris uses CTRL+SPACE for IM
//              KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, InputEvent.CTRL_MASK),
//              ExtKit.completionShowAction
//          ),
///*          new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
//              ExtKit.escapeAction
//          ),*/
//          new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, MENU_MASK),
//              ExtKit.matchBraceAction
//          ),
//          new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, MENU_MASK | InputEvent.SHIFT_MASK),
//              ExtKit.selectionMatchBraceAction
//          ),
//          new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_MASK),
//              ExtKit.showPopupMenuAction
//          ),
//          /*      new MultiKeyBinding(
//                    KeyStroke.getKeyStroke(KeyEvent.VK_U, MENU_MASK),
//          //          KeyStroke.getKeyStroke(KeyEvent.VK_BRACELEFT, MENU_MASK),
//                    ExtKit.braceCodeSelectAction
//                ),
//          */
//
//      };

}
