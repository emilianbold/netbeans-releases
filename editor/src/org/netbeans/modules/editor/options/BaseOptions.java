/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSettingsNames;

import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.netbeans.modules.editor.IndentEngineFormatter;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.text.IndentEngine;

/**
* Options for the base editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class BaseOptions extends OptionSupport {

    public static final String ABBREV_MAP_PROP = "abbrevMap"; // NOI18N
    public static final String BASE = "base"; // NOI18N
    public static final String CARET_BLINK_RATE_PROP = "caretBlinkRate"; // NOI18N
    public static final String CARET_COLOR_INSERT_MODE_PROP = "caretColorInsertMode"; // NOI18N
    public static final String CARET_COLOR_OVERWRITE_MODE_PROP = "caretColorOverwriteMode"; // NOI18N
    public static final String CARET_ITALIC_INSERT_MODE_PROP = "caretItalicInsertMode"; // NOI18N
    public static final String CARET_ITALIC_OVERWRITE_MODE_PROP = "caretItalicOverwriteMode"; // NOI18N
    public static final String CARET_TYPE_INSERT_MODE_PROP = "caretTypeInsertMode"; // NOI18N
    public static final String CARET_TYPE_OVERWRITE_MODE_PROP = "caretTypeOverwriteMode"; // NOI18N
    public static final String COLORING_MAP_PROP = "coloringMap"; // NOI18N
    public static final String EXPAND_TABS_PROP = "expandTabs"; // NOI18N
    public static final String FIND_HIGHLIGHT_SEARCH = "findHighlightSearch"; // NOI18N
    public static final String FIND_HISTORY_PROP = "findHistory"; // NOI18N
    public static final String FIND_HISTORY_SIZE_PROP = "findHistorySize"; // NOI18N
    public static final String FIND_INC_SEARCH_DELAY_PROP = "findIncSearchDelay"; // NOI18N
    public static final String FIND_INC_SEARCH_PROP = "findIncSearch"; // NOI18N
    public static final String FIND_MATCH_CASE_PROP = "findMatchCase"; // NOI18N
    public static final String FIND_REG_EXP_PROP = "findRegExp"; // NOI18N
    public static final String FIND_SMART_CASE_PROP = "findSmartCase"; // NOI18N
    public static final String FIND_WHOLE_WORDS_PROP = "findWholeWords"; // NOI18N
    public static final String FIND_WRAP_SEARCH_PROP = "findWrapSearch"; // NOI18N
    public static final String FONT_SIZE_PROP = "fontSize"; // NOI18N
    public static final String HIGHLIGHT_CARET_ROW_PROP = "highlightCaretRow"; // NOI18N
    public static final String HIGHLIGHT_MATCHING_BRACKET_PROP = "highlightMatchingBracket"; // NOI18N
    public static final String INDENT_ENGINE_PROP = "indentEngine"; // NOI18N
    public static final String KEY_BINDING_LIST_PROP = "keyBindingList"; // NOI18N
    public static final String LINE_HEIGHT_CORRECTION_PROP = "lineHeightCorrection"; // NOI18N
    public static final String LINE_NUMBER_MARGIN_PROP = "lineNumberMargin"; // NOI18N
    public static final String LINE_NUMBER_VISIBLE_PROP = "lineNumberVisible"; // NOI18N
    public static final String MARGIN_PROP = "margin"; // NOI18N
    public static final String SCROLL_FIND_INSETS_PROP = "scrollFindInsets"; // NOI18N
    public static final String SCROLL_JUMP_INSETS_PROP = "scrollJumpInsets"; // NOI18N
    public static final String SPACES_PER_TAB_PROP = "spacesPerTab"; // NOI18N
    public static final String STATUS_BAR_CARET_DELAY_PROP = "statusBarCaretDelay"; // NOI18N
    public static final String STATUS_BAR_VISIBLE_PROP = "statusBarVisible"; // NOI18N
    public static final String TAB_SIZE_PROP = "tabSize"; // NOI18N
    public static final String TEXT_LIMIT_LINE_COLOR_PROP = "textLimitLineColor"; // NOI18N
    public static final String TEXT_LIMIT_LINE_VISIBLE_PROP = "textLimitLineVisible"; // NOI18N
    public static final String TEXT_LIMIT_WIDTH_PROP = "textLimitWidth"; // NOI18N

    static final String[] BASE_PROP_NAMES = {
        ABBREV_MAP_PROP,
        CARET_BLINK_RATE_PROP,
        CARET_COLOR_INSERT_MODE_PROP,
        CARET_COLOR_OVERWRITE_MODE_PROP,
        CARET_ITALIC_INSERT_MODE_PROP,
        CARET_ITALIC_OVERWRITE_MODE_PROP,
        CARET_TYPE_INSERT_MODE_PROP,
        CARET_TYPE_OVERWRITE_MODE_PROP,
        COLORING_MAP_PROP,
        EXPAND_TABS_PROP,
        FONT_SIZE_PROP,
        HIGHLIGHT_CARET_ROW_PROP,
        HIGHLIGHT_MATCHING_BRACKET_PROP,
        INDENT_ENGINE_PROP,
        KEY_BINDING_LIST_PROP,
        LINE_HEIGHT_CORRECTION_PROP,
        LINE_NUMBER_MARGIN_PROP,
        LINE_NUMBER_VISIBLE_PROP,
        MARGIN_PROP,
        SCROLL_FIND_INSETS_PROP,
        SCROLL_JUMP_INSETS_PROP,
        SPACES_PER_TAB_PROP,
        STATUS_BAR_CARET_DELAY_PROP,
        STATUS_BAR_VISIBLE_PROP,
        TAB_SIZE_PROP,
        TEXT_LIMIT_LINE_COLOR_PROP,
        TEXT_LIMIT_LINE_VISIBLE_PROP,
        TEXT_LIMIT_WIDTH_PROP
    };

    static final long serialVersionUID =-5469192431366914841L;

    public BaseOptions() {
        this(BaseKit.class, BASE);
    }

    public BaseOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (BaseOptions.class);
    }

    public int getTabSize() {
        return getSettingInteger(SettingsNames.TAB_SIZE);
    }
    public void setTabSize(int tabSize) {
        setSettingInteger(SettingsNames.TAB_SIZE, tabSize);
    }

/*    public boolean getExpandTabs() {
        return getSettingBoolean(SettingsNames.EXPAND_TABS);
    }
    [Mila] Moved to IndentEngine
*/

    public void setExpandTabs(boolean expandTabs) {
        setSettingBoolean(SettingsNames.EXPAND_TABS, expandTabs);
    }

/*    public int getSpacesPerTab() {
        return getSettingInteger(SettingsNames.SPACES_PER_TAB);
    }
    [Mila] Moved to IndentEngine
*/
    public void setSpacesPerTab(int i){
        setSettingInteger(SettingsNames.SPACES_PER_TAB, i);
    }

    public Map getAbbrevMap() {
        return new HashMap( (Map)getSettingValue(SettingsNames.ABBREV_MAP) );
    }

    public void setAbbrevMap(Map map) {
        setSettingValue(SettingsNames.ABBREV_MAP, map);
    }

    public String getCaretTypeInsertMode() {
        return (String) getSettingValue(SettingsNames.CARET_TYPE_INSERT_MODE);
    }
    public void setCaretTypeInsertMode(String type) {
        setSettingValue(SettingsNames.CARET_TYPE_INSERT_MODE, type);
    }

    public String getCaretTypeOverwriteMode() {
        return (String) getSettingValue(SettingsNames.CARET_TYPE_OVERWRITE_MODE);
    }
    public void setCaretTypeOverwriteMode(String type) {
        setSettingValue(SettingsNames.CARET_TYPE_OVERWRITE_MODE, type);
    }

    public boolean getCaretItalicInsertMode() {
        return getSettingBoolean(SettingsNames.CARET_ITALIC_INSERT_MODE);
    }
    public void setCaretItalicInsertMode(boolean b) {
        setSettingBoolean(SettingsNames.CARET_ITALIC_INSERT_MODE, b);
    }

    public boolean getCaretItalicOverwriteMode() {
        return getSettingBoolean(SettingsNames.CARET_ITALIC_OVERWRITE_MODE);
    }
    public void setCaretItalicOverwriteMode(boolean b) {
        setSettingBoolean(SettingsNames.CARET_ITALIC_OVERWRITE_MODE, b);
    }

    public Color getCaretColorInsertMode() {
        return (Color) getSettingValue(SettingsNames.CARET_COLOR_INSERT_MODE);
    }
    public void setCaretColorInsertMode(Color color) {
        setSettingValue(SettingsNames.CARET_COLOR_INSERT_MODE, color);
    }

    public Color getCaretColorOverwriteMode() {
        return (Color) getSettingValue(SettingsNames.CARET_COLOR_OVERWRITE_MODE);
    }
    public void setCaretColorOverwriteMode(Color color) {
        setSettingValue(SettingsNames.CARET_COLOR_OVERWRITE_MODE, color);
    }

    public int getCaretBlinkRate() {
        return getSettingInteger(SettingsNames.CARET_BLINK_RATE);
    }
    public void setCaretBlinkRate(int rate) {
        setSettingInteger(SettingsNames.CARET_BLINK_RATE, rate);
    }

    public boolean getLineNumberVisible() {
        return getSettingBoolean(SettingsNames.LINE_NUMBER_VISIBLE);
    }
    public void setLineNumberVisible(boolean b) {
        setSettingBoolean(SettingsNames.LINE_NUMBER_VISIBLE, b);
    }

    public Insets getScrollJumpInsets() {
        return (Insets)getSettingValue(SettingsNames.SCROLL_JUMP_INSETS);
    }
    public void setScrollJumpInsets(Insets i) {
        setSettingValue(SettingsNames.SCROLL_JUMP_INSETS, i);
    }

    public Insets getScrollFindInsets() {
        return (Insets)getSettingValue(SettingsNames.SCROLL_FIND_INSETS);
    }
    public void setScrollFindInsets(Insets i) {
        setSettingValue(SettingsNames.SCROLL_FIND_INSETS, i);
    }

    public List getKeyBindingList() {
        Class kitClass = getKitClass();
        Settings.KitAndValue[] kav = getSettingValueHierarchy(SettingsNames.KEY_BINDING_LIST);
        List kbList = null;
        for (int i = 0; i < kav.length; i++) {
            if (kav[i].kitClass == kitClass) {
                kbList = (List)kav[i].value;
            }
        }
        if (kbList == null) {
            kbList = new ArrayList();
        }

        // must convert all members to serializable MultiKeyBinding
        int cnt = kbList.size();
        for (int i = 0; i < cnt; i++) {
            Object o = kbList.get(i);
            if (!(o instanceof MultiKeyBinding) && o != null) {
                JTextComponent.KeyBinding b = (JTextComponent.KeyBinding)o;
                kbList.set(i, new MultiKeyBinding(b.key, b.actionName));
            }
        }

        List kb2 = new ArrayList( kbList );
        kb2.add( 0, kitClass.getName() ); //insert kit class name
        return kb2;
    }

    public void setKeyBindingList(List list) {
        if( list.get( 0 ) instanceof Class || list.get( 0 ) instanceof String ) {
            list.remove( 0 ); //remove kit class name
        }
        setSettingValue(SettingsNames.KEY_BINDING_LIST, list);
    }

    public Map getColoringMap() {
        Map cm = new HashMap( SettingsUtil.getColoringMap(getKitClass(), false, true) ); // !!! !evaluateEvaluators
        cm.put(null, getKitClass().getName() ); // add kit class
        return cm;
    }

    public void setColoringMap(Map coloringMap) {
        if (coloringMap != null) {
            coloringMap.remove(null); // remove kit class
            SettingsUtil.setColoringMap( getKitClass(), coloringMap, false );
// !!!           SettingsUtil.updateColoringSettings(getKitClass(), coloringMap, false);
        }
    }

    public int getFontSize() {
        Coloring dc = SettingsUtil.getColoring(getKitClass(), SettingsNames.DEFAULT_COLORING, false);
        return (dc != null) ? dc.getFont().getSize() : SettingsDefaults.defaultFont.getSize();
    }

    public void setFontSize(final int size) {
        final int oldSize = getFontSize();
        Map cm = SettingsUtil.getColoringMap(getKitClass(), false, true); // !!! !evaluateEvaluators
        if (cm != null) {
            Iterator it = cm.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                Object value = entry.getValue();
                if (value instanceof Coloring) {
                    Coloring c = (Coloring)value;
                    Font font = c.getFont();
                    if (font != null && font.getSize() == oldSize) {
                        font = font.deriveFont((float)size);
                        entry.setValue(Coloring.changeFont(c, font));
                    }
                }
            }
            SettingsUtil.setColoringMap( getKitClass(), cm, false );        
// !!!            SettingsUtil.updateColoringSettings(getKitClass(), cm, false);
        }
    }

    public float getLineHeightCorrection() {
        return ((Float) getSettingValue(SettingsNames.LINE_HEIGHT_CORRECTION)).floatValue();
    }
    public void setLineHeightCorrection(float f) {
        setSettingValue(SettingsNames.LINE_HEIGHT_CORRECTION, new Float(f));
    }

    public Insets getMargin() {
        return (Insets)getSettingValue(SettingsNames.MARGIN);
    }
    public void setMargin(Insets i) {
        setSettingValue(SettingsNames.MARGIN, i);
    }

    public Insets getLineNumberMargin() {
        return (Insets)getSettingValue(SettingsNames.LINE_NUMBER_MARGIN);
    }
    public void setLineNumberMargin(Insets i) {
        setSettingValue(SettingsNames.LINE_NUMBER_MARGIN, i);
    }

    public boolean getStatusBarVisible() {
        return getSettingBoolean(SettingsNames.STATUS_BAR_VISIBLE);
    }
    public void setStatusBarVisible(boolean v) {
        setSettingBoolean(SettingsNames.STATUS_BAR_VISIBLE, v);
    }

    public int getStatusBarCaretDelay() {
        return getSettingInteger(SettingsNames.STATUS_BAR_CARET_DELAY);
    }
    public void setStatusBarCaretDelay(int delay) {
        setSettingInteger(SettingsNames.STATUS_BAR_CARET_DELAY, delay);
    }

    public boolean getFindHighlightSearch() {
        return getSettingBoolean(SettingsNames.FIND_HIGHLIGHT_SEARCH);
    }

    public void setFindHighlightSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_HIGHLIGHT_SEARCH, b);
    }

    public boolean getFindIncSearch() {
        return getSettingBoolean(SettingsNames.FIND_INC_SEARCH);
    }

    public void setFindIncSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_INC_SEARCH, b);
    }

    public int getFindIncSearchDelay() {
        return getSettingInteger(SettingsNames.FIND_INC_SEARCH_DELAY);
    }

    public void setFindIncSearchDelay(int delay) {
        setSettingInteger(SettingsNames.FIND_INC_SEARCH_DELAY, delay);
    }

    public boolean getFindWrapSearch() {
        return getSettingBoolean(SettingsNames.FIND_WRAP_SEARCH);
    }

    public void setFindWrapSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_WRAP_SEARCH, b);
    }

    public boolean getFindSmartCase() {
        return getSettingBoolean(SettingsNames.FIND_SMART_CASE);
    }

    public void setFindSmartCase(boolean b) {
        setSettingBoolean(SettingsNames.FIND_SMART_CASE, b);
    }

    public Map getFindHistory() {
        return new HashMap( (Map)getSettingValue(SettingsNames.FIND_HISTORY) );
    }

    public void setFindHistory(Map m) {
        setSettingValue(SettingsNames.FIND_HISTORY, m);
    }

    public int getFindHistorySize() {
        return getSettingInteger(SettingsNames.FIND_HISTORY_SIZE);
    }

    public void setFindHistorySize(int size) {
        setSettingInteger(SettingsNames.FIND_HISTORY_SIZE, size);
    }

    public Color getTextLimitLineColor() {
        return (Color)getSettingValue(SettingsNames.TEXT_LIMIT_LINE_COLOR);
    }

    public void setTextLimitLineColor(Color color) {
        setSettingValue(SettingsNames.TEXT_LIMIT_LINE_COLOR, color);
    }

    public int getTextLimitWidth() {
        return getSettingInteger(SettingsNames.TEXT_LIMIT_WIDTH);
    }

    public void setTextLimitWidth(int width) {
        setSettingInteger(SettingsNames.TEXT_LIMIT_WIDTH, width);
    }

    public boolean getTextLimitLineVisible() {
        return getSettingBoolean(SettingsNames.TEXT_LIMIT_LINE_VISIBLE);
    }

    public void setTextLimitLineVisible(boolean visible) {
        setSettingBoolean(SettingsNames.TEXT_LIMIT_LINE_VISIBLE, visible);
    }

    public boolean getHighlightMatchingBracket() {
        return getSettingBoolean(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE);
    }

    public void setHighlightMatchingBracket(boolean highlight) {
        setSettingBoolean(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE, highlight);
    }

    public boolean getHighlightCaretRow() {
        return getSettingBoolean(ExtSettingsNames.HIGHLIGHT_CARET_ROW);
    }

    public void setHighlightCaretRow(boolean highlight) {
        setSettingBoolean(ExtSettingsNames.HIGHLIGHT_CARET_ROW, highlight);
    }

/*    public IndentEngine getIndentEngine() {
        IndentEngine.Handle handle = (IndentEngine.Handle)getProperty(INDENT_ENGINE_PROP);
        if (handle == null) {
            return null;
        } else {
            return (IndentEngine)handle.getServiceType();
        }
    }

    public void setIndentEngine(IndentEngine eng) {
        putProperty(INDENT_ENGINE_PROP,
            (eng != null) ? new IndentEngine.Handle(eng) : null,
            true
        );

        // Assign a formatter to the Settings
        Formatter f = null;
        if (eng != null) {
            if (eng instanceof FormatterIndentEngine) {
                f = ((FormatterIndentEngine)eng).getFormatter();

            } else { // generic indent engine
                f = new IndentEngineFormatter(getKitClass(), eng);
            }

        }
        Settings.setValue(getKitClass(), NbEditorDocument.FORMATTER, f);
    }
*/

    public IndentEngine getIndentEngine() {
        return (IndentEngine)getProperty(INDENT_ENGINE_PROP);
    }

    public void setIndentEngine(IndentEngine eng) {
        putProperty(INDENT_ENGINE_PROP, eng, true);

        // Assign a formatter to the Settings
        Formatter f = null;
        if (eng != null) {
            if (eng instanceof FormatterIndentEngine) {
                f = ((FormatterIndentEngine)eng).getFormatter();

            } else { // generic indent engine
                f = new IndentEngineFormatter(getKitClass(), eng);
            }

        }
        Settings.setValue(getKitClass(), NbEditorDocument.INDENT_ENGINE, eng);
        Settings.setValue(getKitClass(), NbEditorDocument.FORMATTER, f);
    }

}

/*
 * Log
 *  22   Jaga      1.14.1.1.1.44/13/00  Miloslav Metelka 
 *  21   Jaga      1.14.1.1.1.33/24/00  Miloslav Metelka 
 *  20   Jaga      1.14.1.1.1.23/21/00  Miloslav Metelka 
 *  19   Jaga      1.14.1.1.1.13/20/00  Miloslav Metelka 
 *  18   Jaga      1.14.1.1.1.03/15/00  Miloslav Metelka Structural change
 *  17   Gandalf-post-FCS1.14.1.1    2/28/00  Petr Nejedly    
 *  16   Gandalf-post-FCS1.14.1.0    2/28/00  Petr Nejedly    Redesign of 
 *       ColoringEditor
 *  15   Gandalf   1.14        1/13/00  Miloslav Metelka Localization
 *  14   Gandalf   1.13        1/10/00  Miloslav Metelka 
 *  13   Gandalf   1.12        12/28/99 Miloslav Metelka 
 *  12   Gandalf   1.11        11/11/99 Miloslav Metelka 
 *  11   Gandalf   1.10        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         9/15/99  Miloslav Metelka 
 *  8    Gandalf   1.7         8/27/99  Miloslav Metelka 
 *  7    Gandalf   1.6         8/17/99  Miloslav Metelka 
 *  6    Gandalf   1.5         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  5    Gandalf   1.4         7/29/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/26/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
