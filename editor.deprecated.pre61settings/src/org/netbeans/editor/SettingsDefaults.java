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

package org.netbeans.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.UIManager;
import java.util.Map;
import java.util.HashMap;

/**
* Default values for the settings. They are used
* by <tt>BaseSettingsInitializer</tt> to initialize
* the settings with the default values. They can be also used
* for substitution if the value of the particular setting
* is unacceptable.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class SettingsDefaults {

    private static final Integer INTEGER_MAX_VALUE = new Integer(Integer.MAX_VALUE);

    // Caret color
    public static final Color defaultCaretColor = Color.black;

    // Empty coloring - it doesn't change font or colors
    public static final Coloring emptyColoring = new Coloring(null, null, null);

    // Default coloring
    private static int defaultFontSize; // Fix of #33249
    static {
        Integer customFontSize = (Integer)UIManager.get("customFontSize"); // NOI18N
        if (customFontSize != null) {
            defaultFontSize = customFontSize.intValue();
        } else {
            Font systemDefaultFont = UIManager.getFont("TextField.font"); // NOI18N
            defaultFontSize = (systemDefaultFont != null)
                ? systemDefaultFont.getSize()
                : 12;
        }
    }

    public static final Font defaultFont = new Font("Monospaced", Font.PLAIN, defaultFontSize); // NOI18N
    public static final Color defaultForeColor = Color.black;
    public static final Color defaultBackColor = Color.white;
    public static final Coloring defaultColoring
    = new Coloring(defaultFont, defaultForeColor, defaultBackColor);
    // line number coloring
    public static final Color defaultLineNumberForeColor = Color.BLACK;
    public static final Color defaultLineNumberBackColor = new Color(233, 232, 226);
    public static final Coloring defaultLineNumberColoring
    = new Coloring(null, defaultLineNumberForeColor, defaultLineNumberBackColor);
    // gutter line
    public static final Color defaultGutterLine = new Color(184, 184, 184);
    // caret selection coloring
    public static final Color defaultSelectionForeColor = Color.white;
    public static final Color defaultSelectionBackColor = new Color(180, 180, 180);
    public static final Coloring defaultSelectionColoring
    = new Coloring(null, defaultSelectionForeColor, defaultSelectionBackColor);
    // Highlight search coloring
    public static final Color defaultHighlightSearchForeColor = Color.black;
    public static final Color defaultHighlightSearchBackColor = new Color(246, 248, 139);
    public static final Coloring defaultHighlightSearchColoring
    = new Coloring(null, defaultHighlightSearchForeColor, defaultHighlightSearchBackColor);
    // Incremental search coloring
    public static final Color defaultIncSearchForeColor = Color.black;
    public static final Color defaultIncSearchBackColor = new Color(255, 153, 0);
    public static final Coloring defaultIncSearchColoring
    = new Coloring(null, defaultIncSearchForeColor, defaultIncSearchBackColor);
    // Block search coloring
    public static final Color defaultBlockSearchForeColor = Color.black;
    public static final Color defaultBlockSearchBackColor = new Color(224,232,241);
    public static final Coloring defaultblockSearchColoring
    = new Coloring(null, defaultBlockSearchForeColor, defaultBlockSearchBackColor);

    // Guarded blocks coloring
    public static final Color defaultGuardedForeColor = null;
    public static final Color defaultGuardedBackColor = new Color(225, 236, 247);
    public static final Coloring defaultGuardedColoring
    = new Coloring(null, defaultGuardedForeColor, defaultGuardedBackColor);
    
    // code folding coloring
    public static final Color defaultCodeFoldingForeColor = new Color(102, 102, 102);
    public static final Coloring defaultCodeFoldingColoring
    = new Coloring(null, defaultCodeFoldingForeColor, null);
    
    // code folding bar coloring
    public static final Color defaultCodeFoldingBarForeColor = new Color(102, 102, 102);
    public static final Color defaultCodeFoldingBarBackColor = Color.WHITE;
    public static final Coloring defaultCodeFoldingBarColoring
    = new Coloring(null, defaultCodeFoldingBarForeColor, null);

    public static final Color defaultStatusBarForeColor = 
            UIManager.getColor("ScrollPane.foreground"); //NOI18N
    public static final Color defaultStatusBarBackColor =
            UIManager.getColor("ScrollPane.background"); // NOI18N
    public static final Coloring defaultStatusBarColoring
    = new Coloring(defaultFont, defaultStatusBarForeColor, defaultStatusBarBackColor);

    public static final Color defaultStatusBarBoldForeColor = Color.white;
    public static final Color defaultStatusBarBoldBackColor = Color.red;
    public static final Coloring defaultStatusBarBoldColoring
    = new Coloring(defaultFont, defaultStatusBarBoldForeColor, defaultStatusBarBoldBackColor);

    public static final Integer defaultCaretBlinkRate = new Integer(300);
    public static final Integer defaultTabSize = new Integer(8);
    public static final Integer defaultSpacesPerTab = new Integer(4);
    public static final Integer defaultShiftWidth = new Integer(4); // usually
    // not used as there's a Evaluator for shift width

    public static final Integer defaultStatusBarCaretDelay = new Integer(200);

    public static final Color defaultTextLimitLineColor = new Color(255, 235, 235);
    public static final Integer defaultTextLimitWidth = new Integer(80);

    public static final Acceptor defaultIdentifierAcceptor = AcceptorFactory.LETTER_DIGIT;
    public static final Acceptor defaultWhitespaceAcceptor = AcceptorFactory.WHITESPACE;

    public static final Float defaultLineHeightCorrection = new Float(1.0f);

    public static final Integer defaultTextLeftMarginWidth = new Integer(2);
    public static final Insets defaultMargin = new Insets(0, 0, 0, 0);
    public static final Insets defaultScrollJumpInsets = new Insets(-5, -10, -5, -30);
    public static final Insets defaultScrollFindInsets = new Insets(-10, -10, -10, -10);
    public static final Dimension defaultComponentSizeIncrement = new Dimension(-5, -30);

    public static final Integer defaultReadBufferSize = new Integer(16384);
    public static final Integer defaultWriteBufferSize = new Integer(16384);
    public static final Integer defaultReadMarkDistance = new Integer(180);
    public static final Integer defaultMarkDistance = new Integer(100);
    public static final Integer defaultMaxMarkDistance = new Integer(150);
    public static final Integer defaultMinMarkDistance = new Integer(50);
    public static final Integer defaultSyntaxUpdateBatchSize
    = new Integer(defaultMarkDistance.intValue() * 7);
    public static final Integer defaultLineBatchSize = new Integer(2);

    public static final Boolean defaultExpandTabs = Boolean.TRUE;

    public static final String defaultCaretTypeInsertMode = BaseCaret.LINE_CARET;
    public static final String defaultCaretTypeOverwriteMode = BaseCaret.BLOCK_CARET;
    public static final Color defaultCaretColorInsertMode = Color.black;
    public static final Color defaultCaretColorOvwerwriteMode = Color.black;
    public static final Boolean defaultCaretItalicInsertMode = Boolean.FALSE;
    public static final Boolean defaultCaretItalicOverwriteMode = Boolean.FALSE;
    public static final Acceptor defaultAbbrevExpandAcceptor = AcceptorFactory.WHITESPACE;
    public static final Acceptor defaultAbbrevAddTypedCharAcceptor = AcceptorFactory.NL;
    public static final Acceptor defaultAbbrevResetAcceptor = AcceptorFactory.NON_JAVA_IDENTIFIER;
    
    /** @deprecated Use Editor Settings, Editor Settings Storage and Editor Code Templates API instead. */
    public static final Map defaultAbbrevMap = new HashMap();

    public static final Map defaultMacroMap = new HashMap();
    
    public static final Boolean defaultStatusBarVisible = Boolean.TRUE;

    public static final Boolean defaultLineNumberVisible = Boolean.FALSE;
    public static final Boolean defaultPrintLineNumberVisible = Boolean.TRUE;
    public static final Boolean defaultTextLimitLineVisible = Boolean.TRUE;
    public static final Boolean defaultHomeKeyColumnOne = Boolean.FALSE;
    public static final Boolean defaultWordMoveNewlineStop = Boolean.TRUE;
    public static final Boolean defaultInputMethodsEnabled = Boolean.TRUE;
    public static final Boolean defaultFindHighlightSearch = Boolean.TRUE;
    public static final Boolean defaultFindIncSearch = Boolean.TRUE;
    public static final Boolean defaultFindBackwardSearch = Boolean.FALSE;
    public static final Boolean defaultFindWrapSearch = Boolean.TRUE;
    public static final Boolean defaultFindMatchCase = Boolean.FALSE;
    public static final Boolean defaultFindWholeWords = Boolean.FALSE;
    public static final Boolean defaultFindRegExp = Boolean.FALSE;
    public static final Integer defaultFindHistorySize = new Integer(30);
    public static final Integer defaultWordMatchSearchLen = INTEGER_MAX_VALUE;
    public static final Boolean defaultWordMatchWrapSearch = Boolean.TRUE;
    public static final Boolean defaultWordMatchMatchOneChar = Boolean.TRUE;
    public static final Boolean defaultWordMatchMatchCase = Boolean.FALSE;
    public static final Boolean defaultWordMatchSmartCase = Boolean.FALSE;
    public static final Boolean defaultCodeFoldingEnable = Boolean.FALSE;
    
    public static final String[] defaultColoringNames
    = new String[] {
          SettingsNames.DEFAULT_COLORING,
          SettingsNames.LINE_NUMBER_COLORING,
          SettingsNames.GUARDED_COLORING,
          SettingsNames.CODE_FOLDING_COLORING,
          SettingsNames.CODE_FOLDING_BAR_COLORING,
          SettingsNames.SELECTION_COLORING,
          SettingsNames.HIGHLIGHT_SEARCH_COLORING,
          SettingsNames.INC_SEARCH_COLORING,
          SettingsNames.BLOCK_SEARCH_COLORING,
          SettingsNames.STATUS_BAR_COLORING,
          SettingsNames.STATUS_BAR_BOLD_COLORING
      };

    
    private static final int FIND_NEXT_KEY = 
        System.getProperty("mrj.version") == null ?
        KeyEvent.VK_F3 : KeyEvent.VK_G;
        
    private static final int FIND_NEXT_MASK = 
        System.getProperty("mrj.version") == null ?
        0 : KeyEvent.META_DOWN_MASK;

    //#26854 - use Command, not Ctrl, on mac    
    private static final int MENU_MASK = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    //Default behavior on mac is that alt+arrows is word jumps
    private static final int WORD_SELECT_MASK = System.getProperty("mrj.version") == null ?
        InputEvent.CTRL_DOWN_MASK : InputEvent.ALT_DOWN_MASK;
    
    private static final int ALT_MASK = System.getProperty("mrj.version") == null ?
        InputEvent.ALT_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
        
//    public static MultiKeyBinding[] defaultKeyBindings
//    = new MultiKeyBinding[] {
//          new MultiKeyBinding( //1
//              KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
//              BaseKit.insertBreakAction
//          ),
//          new MultiKeyBinding( //2
//              KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, MENU_MASK),
//              BaseKit.splitLineAction
//          ),
//          new MultiKeyBinding( //3
//              KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK),
//              BaseKit.startNewLineAction
//          ),
//          
//	  // start-new-line-action cannot be registered here as there
//	  // is already another action registered for Shift+enter in
//	  // the ext-kit. The code is added directly there
//	  //           new MultiKeyBinding(
//	  //               KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK),
//	  //               BaseKit.startNewLineAction
//	  //           ),
//          new MultiKeyBinding( //4
//              KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
//              BaseKit.insertTabAction
//          ),
//          new MultiKeyBinding( //5
//              KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK),
//              BaseKit.removeTabAction
//          ),
//          new MultiKeyBinding( //6
//              KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
//              BaseKit.deletePrevCharAction
//          ),
//          new MultiKeyBinding( //7
//              KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.SHIFT_MASK),
//              BaseKit.deletePrevCharAction
//          ),
///*          new MultiKeyBinding(
//              KeyStroke.getKeyStroke(KeyEvent.VK_H, MENU_MASK | InputEvent.SHIFT_MASK),
//              BaseKit.deletePrevCharAction
//          ),
//*/          new MultiKeyBinding( //8
//              KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
//              BaseKit.deleteNextCharAction
//          ),
//          new MultiKeyBinding( //9
//              KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
//              BaseKit.forwardAction
//          ),
//          new MultiKeyBinding( //10
//              KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), // keypad right
//              BaseKit.forwardAction
//          ),
//          new MultiKeyBinding( //11
//              KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK),
//              BaseKit.selectionForwardAction
//          ),
//          new MultiKeyBinding( //12
//              KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, WORD_SELECT_MASK),
//              BaseKit.nextWordAction
//          ),
//          new MultiKeyBinding( //13
//              KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | WORD_SELECT_MASK),
//              BaseKit.selectionNextWordAction
//          ),
//          new MultiKeyBinding( //14
//              KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
//              BaseKit.backwardAction
//          ),
//          new MultiKeyBinding( //15
//              KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), // keypad left
//              BaseKit.backwardAction
//          ),
//          new MultiKeyBinding( //16
//              KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,  InputEvent.SHIFT_MASK),
//              BaseKit.selectionBackwardAction
//          ),
//          new MultiKeyBinding( //17
//              KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, WORD_SELECT_MASK),
//              BaseKit.previousWordAction
//          ),
//          new MultiKeyBinding( //18
//              KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | WORD_SELECT_MASK),
//              BaseKit.selectionPreviousWordAction
//          ),
//          new MultiKeyBinding( //19
//              KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
//              BaseKit.downAction
//          ),
//          new MultiKeyBinding( //20
//              KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), // keypad down
//              BaseKit.downAction
//          ),
//          new MultiKeyBinding( //21
//              KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_MASK),
//              BaseKit.selectionDownAction
//          ),
//          new MultiKeyBinding( //22
//              KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK),
//              BaseKit.scrollUpAction
//          ),
//          new MultiKeyBinding( //23
//              KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
//              BaseKit.upAction
//          ),
//          new MultiKeyBinding( //24
//              KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), // keypad up
//              BaseKit.upAction
//          ),
//          new MultiKeyBinding( //25
//              KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_MASK),
//              BaseKit.selectionUpAction
//          ),
//          new MultiKeyBinding( //26
//              KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK),
//              BaseKit.scrollDownAction
//          ),
//          new MultiKeyBinding( //27
//              KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
//              BaseKit.pageDownAction
//          ),
//          new MultiKeyBinding( //28
//              KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.SHIFT_MASK),
//              BaseKit.selectionPageDownAction
//          ),
//          new MultiKeyBinding( //29
//              KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
//              BaseKit.pageUpAction
//          ),
//          new MultiKeyBinding( //30
//              KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.SHIFT_MASK),
//              BaseKit.selectionPageUpAction
//          ),
//          new MultiKeyBinding( //31
//              KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
//              BaseKit.beginLineAction
//          ),
//          new MultiKeyBinding( //32
//              KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.SHIFT_MASK),
//              BaseKit.selectionBeginLineAction
//          ),
//          new MultiKeyBinding( //33
//              KeyStroke.getKeyStroke(KeyEvent.VK_HOME, MENU_MASK),
//              BaseKit.beginAction
//          ),
//          new MultiKeyBinding( //34
//              KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.SHIFT_MASK | MENU_MASK),
//              BaseKit.selectionBeginAction
//          ),
//          new MultiKeyBinding( //35
//              KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
//              BaseKit.endLineAction
//          ),
//          new MultiKeyBinding( //36
//              KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.SHIFT_MASK),
//              BaseKit.selectionEndLineAction
//          ),
//          new MultiKeyBinding( //37
//              KeyStroke.getKeyStroke(KeyEvent.VK_END, MENU_MASK),
//              BaseKit.endAction
//          ),
//          new MultiKeyBinding( //38
//              KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.SHIFT_MASK | MENU_MASK),
//              BaseKit.selectionEndAction
//          ),
//
//          // clipboard bindings
//          new MultiKeyBinding( //39
//              KeyStroke.getKeyStroke(KeyEvent.VK_C, MENU_MASK),
//              BaseKit.copyAction
//          ),
//          new MultiKeyBinding( //40
//              KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, MENU_MASK),
//              BaseKit.copyAction
//          ),
//          new MultiKeyBinding( //41
//              KeyStroke.getKeyStroke(KeyEvent.VK_COPY, 0),
//              BaseKit.copyAction
//          ),
//          new MultiKeyBinding( //42
//              KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK),
//              BaseKit.cutAction
//          ),
//          new MultiKeyBinding( //43
//              KeyStroke.getKeyStroke(KeyEvent.VK_X, MENU_MASK),
//              BaseKit.cutAction
//          ),
//          new MultiKeyBinding( //44
//              KeyStroke.getKeyStroke(KeyEvent.VK_CUT, 0),
//              BaseKit.cutAction
//          ),
//          new MultiKeyBinding( //45
//              KeyStroke.getKeyStroke(KeyEvent.VK_V, MENU_MASK),
//              BaseKit.pasteAction
//          ),
//          new MultiKeyBinding( //46
//              KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK),
//              BaseKit.pasteAction
//          ),
//          new MultiKeyBinding( //47
//              KeyStroke.getKeyStroke(KeyEvent.VK_PASTE, 0),
//              BaseKit.pasteAction
//          ),
//          new MultiKeyBinding( //48
//              KeyStroke.getKeyStroke(KeyEvent.VK_V, MENU_MASK | InputEvent.SHIFT_MASK),
//              BaseKit.pasteFormatedAction
//          ),
//
//          // undo and redo bindings - handled at system level
//          new MultiKeyBinding( //49
//              KeyStroke.getKeyStroke(KeyEvent.VK_Z, MENU_MASK),
//              BaseKit.undoAction
//          ),
//          new MultiKeyBinding( //50
//              KeyStroke.getKeyStroke(KeyEvent.VK_UNDO, 0),
//              BaseKit.undoAction
//          ),
//          new MultiKeyBinding( //51
//              KeyStroke.getKeyStroke(KeyEvent.VK_Y, MENU_MASK),
//              BaseKit.redoAction
//          ),
//
//          // other bindings
//          new MultiKeyBinding( //52
//              KeyStroke.getKeyStroke(KeyEvent.VK_A, MENU_MASK),
//              BaseKit.selectAllAction
//          ),
//          new MultiKeyBinding( //53
//              new KeyStroke[] {
//                  KeyStroke.getKeyStroke(KeyEvent.VK_U, ALT_MASK),
//                  KeyStroke.getKeyStroke(KeyEvent.VK_E, 0),
//              },
//              BaseKit.endWordAction
//          ),
//          /* #47709
//          new MultiKeyBinding( //54
//              KeyStroke.getKeyStroke(KeyEvent.VK_W, MENU_MASK),
//              BaseKit.removeWordAction
//          ),
//           */
//          new MultiKeyBinding( //55
//              KeyStroke.getKeyStroke(KeyEvent.VK_U, MENU_MASK),
//              BaseKit.removeLineBeginAction
//          ),
//          new MultiKeyBinding( //56
//              KeyStroke.getKeyStroke(KeyEvent.VK_E, MENU_MASK),
//              BaseKit.removeLineAction
//          ),
//          new MultiKeyBinding( //57
//              KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),
//              BaseKit.toggleTypingModeAction
//          ),
///*          new MultiKeyBinding( //58
//              KeyStroke.getKeyStroke(KeyEvent.VK_F2, MENU_MASK),
//              BaseKit.toggleBookmarkAction
//          ),
//          new MultiKeyBinding( //59
//              KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
//              BaseKit.gotoNextBookmarkAction
//          ),
//          new MultiKeyBinding( //59.5
//              KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_DOWN_MASK),
//              BaseKit.gotoPreviousBookmarkAction
//          ),
// */
//          new MultiKeyBinding( //60
//              KeyStroke.getKeyStroke(FIND_NEXT_KEY, FIND_NEXT_MASK),
//              BaseKit.findNextAction
//          ),
//          new MultiKeyBinding( //61
//              KeyStroke.getKeyStroke(FIND_NEXT_KEY, FIND_NEXT_MASK | InputEvent.SHIFT_MASK),
//              BaseKit.findPreviousAction
//          ),
//          new MultiKeyBinding( //62
//              KeyStroke.getKeyStroke(FIND_NEXT_KEY, FIND_NEXT_MASK | InputEvent.CTRL_MASK),
//              BaseKit.findSelectionAction
//          ),
//          new MultiKeyBinding( //63
//              KeyStroke.getKeyStroke(KeyEvent.VK_H, ALT_MASK | InputEvent.SHIFT_MASK),
//              BaseKit.toggleHighlightSearchAction
//          ),
//          new MultiKeyBinding( //64
//              KeyStroke.getKeyStroke(KeyEvent.VK_L, MENU_MASK),
//              BaseKit.wordMatchNextAction
//          ),
//          new MultiKeyBinding( //65
//              KeyStroke.getKeyStroke(KeyEvent.VK_K, MENU_MASK),
//              BaseKit.wordMatchPrevAction
//          ),
//          new MultiKeyBinding( //66
//              KeyStroke.getKeyStroke(KeyEvent.VK_T, MENU_MASK),
//              BaseKit.shiftLineRightAction
//          ),
//          new MultiKeyBinding( //67
//              KeyStroke.getKeyStroke(KeyEvent.VK_D, MENU_MASK),
//              BaseKit.shiftLineLeftAction
//          ),
//          new MultiKeyBinding( //68
//              KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.SHIFT_MASK),
//              BaseKit.abbrevResetAction
//          ),
//          new MultiKeyBinding( //69
//              KeyStroke.getKeyStroke(KeyEvent.VK_C, MENU_MASK | InputEvent.SHIFT_MASK),
//              BaseKit.annotationsCyclingAction
//          ),
//
//          new MultiKeyBinding( //70
//              new KeyStroke[] {
//                  KeyStroke.getKeyStroke(KeyEvent.VK_U, ALT_MASK),
//                  KeyStroke.getKeyStroke(KeyEvent.VK_T, 0),
//              },
//              BaseKit.adjustWindowTopAction
//          ),
//          new MultiKeyBinding( //71
//              new KeyStroke[] {
//                  KeyStroke.getKeyStroke(KeyEvent.VK_U, ALT_MASK),
//                  KeyStroke.getKeyStroke(KeyEvent.VK_M, 0),
//              },
//              BaseKit.adjustWindowCenterAction
//          ),
//          new MultiKeyBinding( //72
//              new KeyStroke[] {
//                  KeyStroke.getKeyStroke(KeyEvent.VK_U, ALT_MASK),
//                  KeyStroke.getKeyStroke(KeyEvent.VK_B, 0),
//              },
//              BaseKit.adjustWindowBottomAction
//          ),
//
//          new MultiKeyBinding( //73
//              KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.SHIFT_MASK | ALT_MASK),
//              BaseKit.adjustCaretTopAction
//          ),
//          new MultiKeyBinding( //74
//              KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.SHIFT_MASK | ALT_MASK),
//              BaseKit.adjustCaretCenterAction
//          ),
//          new MultiKeyBinding( //75
//              KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.SHIFT_MASK | ALT_MASK),
//              BaseKit.adjustCaretBottomAction
//          ),
//
//          new MultiKeyBinding( //76
//              KeyStroke.getKeyStroke(KeyEvent.VK_F, MENU_MASK | InputEvent.SHIFT_MASK ),
//              BaseKit.formatAction
//          ),
//          new MultiKeyBinding( //77
//              KeyStroke.getKeyStroke(KeyEvent.VK_J, ALT_MASK),
//              BaseKit.selectIdentifierAction
//          ),
//          new MultiKeyBinding( //78
//              KeyStroke.getKeyStroke(KeyEvent.VK_K, ALT_MASK),
//              BaseKit.jumpListPrevAction
//          ),
//          new MultiKeyBinding( //79
//              KeyStroke.getKeyStroke(KeyEvent.VK_L, ALT_MASK),
//              BaseKit.jumpListNextAction
//          ),
//          new MultiKeyBinding( //80
//              KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.SHIFT_MASK | ALT_MASK),
//              BaseKit.jumpListPrevComponentAction
//          ),
//          new MultiKeyBinding( //81
//              KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.SHIFT_MASK | ALT_MASK),
//              BaseKit.jumpListNextComponentAction
//          ),
//          new MultiKeyBinding( //82
//              new KeyStroke[] {
//                  KeyStroke.getKeyStroke(KeyEvent.VK_U, ALT_MASK),
//                  KeyStroke.getKeyStroke(KeyEvent.VK_U, 0),
//              },
//              BaseKit.toUpperCaseAction
//          ),
//          new MultiKeyBinding( //83
//              new KeyStroke[] {
//                  KeyStroke.getKeyStroke(KeyEvent.VK_U, ALT_MASK),
//                  KeyStroke.getKeyStroke(KeyEvent.VK_L, 0),
//              },
//              BaseKit.toLowerCaseAction
//          ),
//          new MultiKeyBinding( //84
//              new KeyStroke[] {
//                  KeyStroke.getKeyStroke(KeyEvent.VK_U, ALT_MASK),
//                  KeyStroke.getKeyStroke(KeyEvent.VK_R, 0),
//              },
//              BaseKit.switchCaseAction
//          ),
//
//          new MultiKeyBinding( //85
//              KeyStroke.getKeyStroke(KeyEvent.VK_M, MENU_MASK),
//              BaseKit.selectNextParameterAction
//          ),
//
//          new MultiKeyBinding( //86
//              new KeyStroke[] {
//                  KeyStroke.getKeyStroke(KeyEvent.VK_J, MENU_MASK),
//                  KeyStroke.getKeyStroke(KeyEvent.VK_S, 0),
//              },
//              BaseKit.startMacroRecordingAction
//          ),
//
//          new MultiKeyBinding( //87
//              new KeyStroke[] {
//                  KeyStroke.getKeyStroke(KeyEvent.VK_J, MENU_MASK),
//                  KeyStroke.getKeyStroke(KeyEvent.VK_E, 0),
//              },
//              BaseKit.stopMacroRecordingAction
//          ),
//          
//          new MultiKeyBinding( //88
//              KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, MENU_MASK),
//              BaseKit.collapseFoldAction
//          ),
//
//          new MultiKeyBinding( //89
//              KeyStroke.getKeyStroke(KeyEvent.VK_ADD, MENU_MASK),
//              BaseKit.expandFoldAction
//          ),
//          
//          new MultiKeyBinding( //90
//              KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, MENU_MASK | InputEvent.SHIFT_MASK),
//              BaseKit.collapseAllFoldsAction
//          ),
//          
//          new MultiKeyBinding( //91
//              KeyStroke.getKeyStroke(KeyEvent.VK_ADD, MENU_MASK | InputEvent.SHIFT_MASK),
//              BaseKit.expandAllFoldsAction
//          ),
//
//          new MultiKeyBinding( //92
//              KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, MENU_MASK),
//              BaseKit.collapseFoldAction
//          ),
//
//          new MultiKeyBinding( //93
//              KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, MENU_MASK),
//              BaseKit.expandFoldAction
//          ),
//
//          new MultiKeyBinding( //94
//              KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, MENU_MASK),
//              BaseKit.expandFoldAction
//          ),
//
//          new MultiKeyBinding( //95
//              KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, MENU_MASK | InputEvent.SHIFT_MASK),
//              BaseKit.collapseAllFoldsAction
//          ),
//          
//          new MultiKeyBinding( //96
//              KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, MENU_MASK | InputEvent.SHIFT_MASK),
//              BaseKit.expandAllFoldsAction
//          ),
//          
//          new MultiKeyBinding( //97
//              KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, MENU_MASK | InputEvent.SHIFT_MASK),
//              BaseKit.expandAllFoldsAction
//          ),
//          
//          new MultiKeyBinding( //98
//              KeyStroke.getKeyStroke(KeyEvent.VK_Q, ALT_MASK | InputEvent.SHIFT_MASK),
//              "dump-view-hierarchy" // NOI18N
//          ),
//          new MultiKeyBinding( //99
//              KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, MENU_MASK),
//              BaseKit.removePreviousWordAction
//          ),
//          new MultiKeyBinding( //100
//              KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, MENU_MASK),
//              BaseKit.removeNextWordAction
//          )
//      };
      
      //#46811 - Install mac specific key handling
      static {
          int end = 8;
          if (System.getProperty("mrj.version") != null) { //NOI18N
//              MultiKeyBinding[] nue = new MultiKeyBinding[defaultKeyBindings.length + end];
//              
//              nue[0] = new MultiKeyBinding(
//                 KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.META_MASK),
//                 BaseKit.endLineAction
//              );
//              
//              nue[1] = new MultiKeyBinding(
//                 KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.META_MASK),
//                 BaseKit.beginLineAction
//              );
//              
//              nue[2] = new MultiKeyBinding(
//                KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.META_MASK),
//                BaseKit.beginAction
//              );
//              
//              nue[3] = new MultiKeyBinding(
//                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.META_MASK),
//                BaseKit.endAction
//              );
//              
//              nue[4] = new MultiKeyBinding(
//                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.META_MASK | InputEvent.SHIFT_MASK),
//                BaseKit.selectionEndAction
//              );
//              
//              nue[5] = new MultiKeyBinding(
//                KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.META_MASK | InputEvent.SHIFT_MASK),
//                BaseKit.selectionBeginAction
//              );
//              
//              nue[6] = new MultiKeyBinding( //36
//                  KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.META_MASK),
//                  BaseKit.selectionEndLineAction
//              );
//              
//              nue[7] = new MultiKeyBinding( //32
//                  KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.META_MASK),
//                  BaseKit.selectionBeginLineAction
//              );
//              
//              
//              System.arraycopy(defaultKeyBindings, 0, nue, end, defaultKeyBindings.length);
//              defaultKeyBindings = nue;
          }
      }
}
