/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.text.options;

import java.awt.Color;
import java.awt.Insets;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.swing.text.JTextComponent;
  
import com.netbeans.editor.Settings;
import com.netbeans.editor.Coloring;
import com.netbeans.editor.ColoringManager;
import com.netbeans.editor.BaseKit;
import com.netbeans.editor.Syntax;
import com.netbeans.editor.MultiKeyBinding;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

/**
* Options for the base editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class BaseOptions extends OptionSupport {
  
  public static final String BASE = "base";
  
  public static final String ABBREV_MAP_PROP = "abbrevMap";
  
  public static final String CARET_BLINK_RATE_PROP = "caretBlinkRate";
  
  public static final String CARET_ITALIC_INSERT_MODE_PROP = "caretItalicInsertMode";
  
  public static final String CARET_ITALIC_OVERWRITE_MODE_PROP = "caretItalicOverwriteMode";
  
  public static final String CARET_TYPE_INSERT_MODE_PROP = "caretTypeInsertMode";
  
  public static final String CARET_TYPE_OVERWRITE_MODE_PROP = "caretTypeOverwriteMode";
  
  public static final String CARET_COLOR_INSERT_MODE_PROP = "caretColorInsertMode";
  
  public static final String CARET_COLOR_OVERWRITE_MODE_PROP = "caretColorOverwriteMode";
  
  public static final String COLORING_ARRAY_PROP = "coloringArray";
  
  public static final String EXPAND_TABS_PROP = "expandTabs";
  
  public static final String KEY_BINDING_LIST_PROP = "keyBindingList";
  
  public static final String LINE_HEIGHT_CORRECTION_PROP = "lineHeightCorrection";
  
  public static final String LINE_NUMBER_MARGIN_PROP = "lineNumberMargin";
  
  public static final String LINE_NUMBER_VISIBLE_PROP = "lineNumberVisible";

  public static final String MARGIN_PROP = "margin";
  
  public static final String SCROLL_JUMP_INSETS_PROP = "scrollJumpInsets";
  
  public static final String SCROLL_FIND_INSETS_PROP = "scrollFindInsets";
  
  public static final String SPACES_PER_TAB_PROP = "spacesPerTab";
  
  public static final String STATUS_BAR_CARET_DELAY_PROP = "statusBarCaretDelay";

  public static final String STATUS_BAR_VISIBLE_PROP = "statusBarVisible";
  
  public static final String TAB_SIZE_PROP = "tabSize";
  
  public static final String FIND_HIGHLIGHT_SEARCH = "findHighlightSearch";
  
  public static final String FIND_INC_SEARCH_PROP = "findIncSearch";

  public static final String FIND_INC_SEARCH_DELAY_PROP = "findIncSearchDelay";

  public static final String FIND_WRAP_SEARCH_PROP = "findWrapSearch";

  public static final String FIND_MATCH_CASE_PROP = "findMatchCase";

  public static final String FIND_SMART_CASE_PROP = "findSmartCase";

  public static final String FIND_WHOLE_WORDS_PROP = "findWholeWords";

  public static final String FIND_REG_EXP_PROP = "findRegExp";

  public static final String FIND_HISTORY_PROP = "findHistory";

  public static final String FIND_HISTORY_SIZE_PROP = "findHistorySize";
  
  public static final String FONT_SIZE_PROP = "fontSize";
  
  private static final int[] COLORING_SETS = new int[] {
    ColoringManager.DEFAULT_SET,
    ColoringManager.DOCUMENT_SET,
    ColoringManager.COMPONENT_SET,
    ColoringManager.STATUS_BAR_SET,
    ColoringManager.TOKEN_SET
  };

  static final String[] BASE_PROP_NAMES = {
    ABBREV_MAP_PROP,
    CARET_BLINK_RATE_PROP,
    CARET_ITALIC_INSERT_MODE_PROP,
    CARET_ITALIC_OVERWRITE_MODE_PROP,
    CARET_TYPE_INSERT_MODE_PROP,
    CARET_TYPE_OVERWRITE_MODE_PROP,
    CARET_COLOR_INSERT_MODE_PROP,
    CARET_COLOR_OVERWRITE_MODE_PROP,
    COLORING_ARRAY_PROP,
    EXPAND_TABS_PROP,
    FONT_SIZE_PROP,
    KEY_BINDING_LIST_PROP,
    LINE_HEIGHT_CORRECTION_PROP,
    LINE_NUMBER_MARGIN_PROP,
    LINE_NUMBER_VISIBLE_PROP,
    MARGIN_PROP,
    SCROLL_JUMP_INSETS_PROP,
    SCROLL_FIND_INSETS_PROP,
    SPACES_PER_TAB_PROP,
    STATUS_BAR_CARET_DELAY_PROP,
    STATUS_BAR_VISIBLE_PROP,
    TAB_SIZE_PROP,
  };


  static final long serialVersionUID =-5469192431366914841L;
  
  public BaseOptions() {
    this(BaseKit.class, BASE);
  }

  public BaseOptions(Class kitClass, String typeName) {
    super(kitClass, typeName);
  }
  
  public int getTabSize() {
    return ((Integer)getSettingValue(Settings.TAB_SIZE)).intValue();
  }
  public void setTabSize(int tabSize) {
    setSettingValue(Settings.TAB_SIZE, new Integer(tabSize));
  }

  public boolean getExpandTabs() {
    return getSettingBoolean(Settings.EXPAND_TABS);
  }
  public void setExpandTabs(boolean expandTabs) {
    setSettingBoolean(Settings.EXPAND_TABS, expandTabs);
  }
  
  public int getSpacesPerTab() {
    return ((Integer) getSettingValue(Settings.SPACES_PER_TAB)).intValue();
  }
  public void setSpacesPerTab(int i){
    setSettingValue(Settings.SPACES_PER_TAB, new Integer(i));
  }
  
  public Map getAbbrevMap() {
    return (Map)getSettingValue(Settings.ABBREV_MAP);
  }

  public void setAbbrevMap(Map map) {
    setSettingValue(Settings.ABBREV_MAP, map);
  }
  
  public String getCaretTypeInsertMode() {
    return (String) getSettingValue(Settings.CARET_TYPE_INSERT_MODE);
  }
  public void setCaretTypeInsertMode(String type) {
    setSettingValue(Settings.CARET_TYPE_INSERT_MODE, type);
  }
  
  public String getCaretTypeOverwriteMode() {
    return (String) getSettingValue(Settings.CARET_TYPE_OVERWRITE_MODE);
  }
  public void setCaretTypeOverwriteMode(String type) {
    setSettingValue(Settings.CARET_TYPE_OVERWRITE_MODE, type);
  }
  
  public boolean getCaretItalicInsertMode() {
    return getSettingBoolean(Settings.CARET_ITALIC_INSERT_MODE);
  }
  public void setCaretItalicInsertMode(boolean b) {
    setSettingBoolean(Settings.CARET_ITALIC_INSERT_MODE, b);
  }
  
  public boolean getCaretItalicOverwriteMode() {
    return getSettingBoolean(Settings.CARET_ITALIC_OVERWRITE_MODE);
  }
  public void setCaretItalicOverwriteMode(boolean b) {
    setSettingBoolean(Settings.CARET_ITALIC_OVERWRITE_MODE, b);
  }
  
  public Color getCaretColorInsertMode() {
    return (Color) getSettingValue(Settings.CARET_COLOR_INSERT_MODE);
  }
  public void setCaretColorInsertMode(Color color) {
     setSettingValue(Settings.CARET_COLOR_INSERT_MODE, color);
  }
  
  public Color getCaretColorOverwriteMode() {
    return (Color) getSettingValue(Settings.CARET_COLOR_OVERWRITE_MODE);
  }
  public void setCaretColorOverwriteMode(Color color) {
    setSettingValue(Settings.CARET_COLOR_OVERWRITE_MODE, color);
  }
  
  public int getCaretBlinkRate() {
    return ((Integer)getSettingValue(Settings.CARET_BLINK_RATE)).intValue();
  }
  public void setCaretBlinkRate(int rate) {
    setSettingValue(Settings.CARET_BLINK_RATE, new Integer(rate));
  }

  public boolean getLineNumberVisible() {
    return getSettingBoolean(Settings.LINE_NUMBER_VISIBLE);
  }
  public void setLineNumberVisible(boolean b) {
    setSettingBoolean(Settings.LINE_NUMBER_VISIBLE, b);
  }
  
  public Insets getScrollJumpInsets() {
    return (Insets)getSettingValue(Settings.SCROLL_JUMP_INSETS);
  }
  public void setScrollJumpInsets(Insets i) {
    setSettingValue(Settings.SCROLL_JUMP_INSETS, i);
  }
  
  public Insets getScrollFindInsets() {
    return (Insets)getSettingValue(Settings.SCROLL_FIND_INSETS);
  }
  public void setScrollFindInsets(Insets i) {
    setSettingValue(Settings.SCROLL_FIND_INSETS, i);
  }
  
  public List getKeyBindingList() {
    Class kitClass = getKitClass();
    Settings.KitAndValue[] kav = getSettingKitAndValueArray(Settings.KEY_BINDING_LIST);
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

    return kbList;
  }
  
  public void setKeyBindingList(List list) {
    setSettingValue(Settings.KEY_BINDING_LIST, list);
  }

  public Object[] getColoringArray() {
    return getColoringsHelper(COLORING_SETS);
  }

  public void setColoringArray(Object[] value) {
    setColoringsHelper(value, COLORING_SETS);
  }
  
  public int getFontSize() {
    ColoringManager cm = (ColoringManager)getSettingValue(Settings.COLORING_MANAGER);
    return cm.getDefaultColoring(getKitClass()).getFont().getSize();
  }
  
  public void setFontSize(int size) {
    ColoringManager cm = (ColoringManager)getSettingValue(Settings.COLORING_MANAGER);
    for (int i = 0; i < COLORING_SETS.length; i++) {
      ColoringManager.updateFontSize(cm.getColorings(getKitClass(), COLORING_SETS[i]), size);
    }
    Settings.touchValue(getKitClass(), Settings.COLORING_MANAGER);
  }
  
  public float getLineHeightCorrection() {
    return ((Float) getSettingValue(Settings.LINE_HEIGHT_CORRECTION)).floatValue();
  }
  public void setLineHeightCorrection(float f) {
    setSettingValue(Settings.LINE_HEIGHT_CORRECTION, new Float(f));
  }
  
  public Insets getMargin() {
    return (Insets)getSettingValue(Settings.MARGIN);
  }
  public void setMargin(Insets i) {
    setSettingValue(Settings.MARGIN, i);
  }
  
  public Insets getLineNumberMargin() {
    return (Insets)getSettingValue(Settings.LINE_NUMBER_MARGIN);
  }
  public void setLineNumberMargin(Insets i) {
    setSettingValue(Settings.LINE_NUMBER_MARGIN, i);
  }
  
  public boolean getStatusBarVisible() {
    return getSettingBoolean(Settings.STATUS_BAR_VISIBLE);
  }
  public void setStatusBarVisible(boolean v) {
    setSettingBoolean(Settings.STATUS_BAR_VISIBLE, v);
  }
  
  public int getStatusBarCaretDelay() {
    return ((Integer)getSettingValue(Settings.STATUS_BAR_CARET_DELAY)).intValue();
  }
  public void setStatusBarCaretDelay(int delay) {
    setSettingValue(Settings.STATUS_BAR_CARET_DELAY, new Integer(delay));
  }

  public boolean getFindHighlightSearch() {
    return getSettingBoolean(Settings.FIND_HIGHLIGHT_SEARCH);
  }
  
  public void setFindHighlightSearch(boolean b) {
    setSettingBoolean(Settings.FIND_HIGHLIGHT_SEARCH, b);
  }
  
  public boolean getFindIncSearch() {
    return getSettingBoolean(Settings.FIND_INC_SEARCH);
  }
  
  public void setFindIncSearch(boolean b) {
    setSettingBoolean(Settings.FIND_INC_SEARCH, b);
  }
  
  public int getFindIncSearchDelay() {
    Integer i = (Integer)getSettingValue(Settings.FIND_INC_SEARCH_DELAY);
    return (i != null) ? i.intValue() : 0;
  }
  
  public void setFindIncSearchDelay(int delay) {
    setSettingValue(Settings.FIND_INC_SEARCH_DELAY, new Integer(delay));
  }

  public boolean getFindWrapSearch() {
    return getSettingBoolean(Settings.FIND_WRAP_SEARCH);
  }
  
  public void setFindWrapSearch(boolean b) {
    setSettingBoolean(Settings.FIND_WRAP_SEARCH, b);
  }
  
  public boolean getFindSmartCase() {
    return getSettingBoolean(Settings.FIND_SMART_CASE);
  }
  
  public void setFindSmartCase(boolean b) {
    setSettingBoolean(Settings.FIND_SMART_CASE, b);
  }

  public Map getFindHistory() {
    return (Map)getSettingValue(Settings.FIND_HISTORY);
  }
  
  public void setFindHistory(Map m) {
    setSettingValue(Settings.FIND_HISTORY, m);
  }

  public int getFindHistorySize() {
    Integer i = (Integer)getSettingValue(Settings.FIND_HISTORY_SIZE);
    return (i != null) ? i.intValue() : 0;
  }
  
  public void setFindHistorySize(int size) {
    setSettingValue(Settings.FIND_HISTORY_SIZE, new Integer(size));
  }

}

/*
 * Log
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
