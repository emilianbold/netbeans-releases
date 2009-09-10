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
import javax.swing.UIManager;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;

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

    public static final Integer defaultCaretBlinkRate = EditorPreferencesDefaults.defaultCaretBlinkRate;
    public static final Integer defaultTabSize = EditorPreferencesDefaults.defaultTabSize;
    public static final Integer defaultSpacesPerTab = EditorPreferencesDefaults.defaultSpacesPerTab;
    public static final Integer defaultShiftWidth = EditorPreferencesDefaults.defaultShiftWidth;
    // not used as there's a Evaluator for shift width

    public static final Integer defaultStatusBarCaretDelay = EditorPreferencesDefaults.defaultStatusBarCaretDelay;

    public static final Color defaultTextLimitLineColor = new Color(255, 235, 235);
    public static final Integer defaultTextLimitWidth = EditorPreferencesDefaults.defaultTextLimitWidth;

//    public static final Acceptor defaultIdentifierAcceptor = EditorPreferencesDefaults.defaultIdentifierAcceptor;
//    public static final Acceptor defaultWhitespaceAcceptor = EditorPreferencesDefaults.defaultWhitespaceAcceptor;

    public static final Float defaultLineHeightCorrection = EditorPreferencesDefaults.defaultLineHeightCorrection;

    public static final Integer defaultTextLeftMarginWidth = EditorPreferencesDefaults.defaultTextLeftMarginWidth;
    public static final Insets defaultMargin = new Insets(0, 0, 0, 0);
    public static final Insets defaultScrollJumpInsets = new Insets(-5, -10, -5, -30);
    public static final Insets defaultScrollFindInsets = new Insets(-10, -10, -10, -10);
    public static final Dimension defaultComponentSizeIncrement = new Dimension(-5, -30);

    public static final Integer defaultReadBufferSize = EditorPreferencesDefaults.defaultReadBufferSize;
    public static final Integer defaultWriteBufferSize = EditorPreferencesDefaults.defaultWriteBufferSize;
    public static final Integer defaultReadMarkDistance = EditorPreferencesDefaults.defaultReadMarkDistance;
    public static final Integer defaultMarkDistance = EditorPreferencesDefaults.defaultMarkDistance;
    public static final Integer defaultMaxMarkDistance = EditorPreferencesDefaults.defaultMaxMarkDistance;
    public static final Integer defaultMinMarkDistance = EditorPreferencesDefaults.defaultMinMarkDistance;
    public static final Integer defaultSyntaxUpdateBatchSize = EditorPreferencesDefaults.defaultSyntaxUpdateBatchSize;
    public static final Integer defaultLineBatchSize = EditorPreferencesDefaults.defaultLineBatchSize;

    public static final Boolean defaultExpandTabs = EditorPreferencesDefaults.defaultExpandTabs;

    public static final String defaultCaretTypeInsertMode = EditorPreferencesDefaults.defaultCaretTypeInsertMode;
    public static final Integer defaultThickCaretWidth = EditorPreferencesDefaults.defaultThickCaretWidth;
    public static final String defaultCaretTypeOverwriteMode = EditorPreferencesDefaults.defaultCaretTypeOverwriteMode;
    public static final Color defaultCaretColorInsertMode = Color.black;
    public static final Color defaultCaretColorOvwerwriteMode = Color.black;
    public static final Boolean defaultCaretItalicInsertMode = EditorPreferencesDefaults.defaultCaretItalicInsertMode;
    public static final Boolean defaultCaretItalicOverwriteMode = EditorPreferencesDefaults.defaultCaretItalicOverwriteMode;
    public static final Acceptor defaultAbbrevExpandAcceptor = AcceptorFactory.WHITESPACE;
    public static final Acceptor defaultAbbrevAddTypedCharAcceptor = AcceptorFactory.NL;
    public static final Acceptor defaultAbbrevResetAcceptor = AcceptorFactory.NON_JAVA_IDENTIFIER;
    
    /** @deprecated Use Editor Settings, Editor Settings Storage and Editor Code Templates API instead. */
    public static final Map defaultAbbrevMap = new HashMap();

    public static final Map defaultMacroMap = new HashMap();
    
    public static final Boolean defaultStatusBarVisible = EditorPreferencesDefaults.defaultStatusBarVisible;

    public static final Boolean defaultLineNumberVisible = EditorPreferencesDefaults.defaultLineNumberVisible;
    public static final Boolean defaultPrintLineNumberVisible = EditorPreferencesDefaults.defaultPrintLineNumberVisible;
    public static final Boolean defaultTextLimitLineVisible = EditorPreferencesDefaults.defaultTextLimitLineVisible;
    public static final Boolean defaultHomeKeyColumnOne = EditorPreferencesDefaults.defaultHomeKeyColumnOne;
    public static final Boolean defaultWordMoveNewlineStop = EditorPreferencesDefaults.defaultWordMoveNewlineStop;
    public static final Boolean defaultInputMethodsEnabled = EditorPreferencesDefaults.defaultInputMethodsEnabled;
    public static final Boolean defaultFindHighlightSearch = EditorPreferencesDefaults.defaultFindHighlightSearch;
    public static final Boolean defaultFindIncSearch = EditorPreferencesDefaults.defaultFindIncSearch;
    public static final Boolean defaultFindBackwardSearch = EditorPreferencesDefaults.defaultFindBackwardSearch;
    public static final Boolean defaultFindWrapSearch = EditorPreferencesDefaults.defaultFindWrapSearch;
    public static final Boolean defaultFindMatchCase = EditorPreferencesDefaults.defaultFindMatchCase;
    public static final Boolean defaultFindWholeWords = EditorPreferencesDefaults.defaultFindWholeWords;
    public static final Boolean defaultFindRegExp = EditorPreferencesDefaults.defaultFindRegExp;
    public static final Integer defaultFindHistorySize = EditorPreferencesDefaults.defaultFindHistorySize;
    public static final Integer defaultWordMatchSearchLen = EditorPreferencesDefaults.defaultWordMatchSearchLen;
    public static final Boolean defaultWordMatchWrapSearch = EditorPreferencesDefaults.defaultWordMatchWrapSearch;
    public static final Boolean defaultWordMatchMatchOneChar = EditorPreferencesDefaults.defaultWordMatchMatchOneChar;
    public static final Boolean defaultWordMatchMatchCase = EditorPreferencesDefaults.defaultWordMatchMatchCase;
    public static final Boolean defaultWordMatchSmartCase = EditorPreferencesDefaults.defaultWordMatchSmartCase;
    public static final Boolean defaultCodeFoldingEnable = EditorPreferencesDefaults.defaultCodeFoldingEnable;
    
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

}
