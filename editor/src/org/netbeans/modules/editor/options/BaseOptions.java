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
import java.awt.Font;
import java.awt.Insets;
import java.beans.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.List;
import java.util.ResourceBundle;
  
import com.netbeans.editor.Settings;
import com.netbeans.editor.ColoringManager;
import com.netbeans.editor.BaseKit;

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
  
  public static final String SYSTEM_COLORING_ARRAY_PROP = "systemColoringArray";
  
  public static final String TAB_SIZE_PROP = "tabSize";
  
  public static final String TOKEN_COLORING_ARRAY_PROP = "tokenColoringArray";
  
  static final String[] BASE_PROP_NAMES = {
    ABBREV_MAP_PROP,
    CARET_BLINK_RATE_PROP,
    CARET_ITALIC_INSERT_MODE_PROP,
    CARET_ITALIC_OVERWRITE_MODE_PROP,
    CARET_TYPE_INSERT_MODE_PROP,
    CARET_TYPE_OVERWRITE_MODE_PROP,
    CARET_COLOR_INSERT_MODE_PROP,
    CARET_COLOR_OVERWRITE_MODE_PROP,
    EXPAND_TABS_PROP,
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
    SYSTEM_COLORING_ARRAY_PROP,
    TAB_SIZE_PROP,
    TOKEN_COLORING_ARRAY_PROP
  };

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
    changeProperty(Settings.TAB_SIZE, new Integer(tabSize));
  }

  public boolean getExpandTabs() {
    return ((Boolean)getSettingValue(Settings.EXPAND_TABS)).booleanValue();
  }
  public void setExpandTabs(boolean expandTabs) {
    changeProperty(Settings.EXPAND_TABS, new Boolean(expandTabs));
  }
  
  public int getSpacesPerTab() {
    return ((Integer) getSettingValue(Settings.SPACES_PER_TAB)).intValue();
  }
  public void setSpacesPerTab(int i){
    changeProperty(Settings.SPACES_PER_TAB, new Integer(i));
  }
  
  public Map getAbbrevMap() {
    Map m = (Map) getSettingValue(Settings.ABBREV_MAP);
    if (m == null) {
      m = new java.util.HashMap(3);
    }
    return m;
  }
  public void setAbbrevMap(Map map) {
    changeProperty(Settings.ABBREV_MAP, map);
  }
  
  public String getCaretTypeInsertMode() {
    return (String) getSettingValue(Settings.CARET_TYPE_INSERT_MODE);
  }
  public void setCaretTypeInsertMode(String type) {
    changeProperty(Settings.CARET_TYPE_INSERT_MODE, type);
  }
  
  public String getCaretTypeOverwriteMode() {
    return (String) getSettingValue(Settings.CARET_TYPE_OVERWRITE_MODE);
  }
  public void setCaretTypeOverwriteMode(String type) {
    changeProperty(Settings.CARET_TYPE_OVERWRITE_MODE, type);
  }
  
  public boolean getCaretItalicInsertMode() {
    return ((Boolean) getSettingValue(Settings.CARET_ITALIC_INSERT_MODE)).booleanValue();
  }
  public void setCaretItalicInsertMode(boolean b) {
    changeProperty(Settings.CARET_ITALIC_INSERT_MODE, (b ? Boolean.TRUE : Boolean.FALSE));
  }
  
  public boolean getCaretItalicOverwriteMode() {
    return ((Boolean) getSettingValue(Settings.CARET_ITALIC_OVERWRITE_MODE)).booleanValue();
  }
  public void setCaretItalicOverwriteMode(boolean b) {
    changeProperty(Settings.CARET_ITALIC_OVERWRITE_MODE, (b ? Boolean.TRUE : Boolean.FALSE));
  }
  
  public Color getCaretColorInsertMode() {
    return (Color) getSettingValue(Settings.CARET_COLOR_INSERT_MODE);
  }
  public void setCaretColorInsertMode(Color color) {
     changeProperty(Settings.CARET_COLOR_INSERT_MODE, color);
  }
  
  public Color getCaretColorOverwriteMode() {
    return (Color) getSettingValue(Settings.CARET_COLOR_OVERWRITE_MODE);
  }
  public void setCaretColorOverwriteMode(Color color) {
    changeProperty(Settings.CARET_COLOR_OVERWRITE_MODE, color);
  }
  
  public int getCaretBlinkRate() {
    return ((Integer)getSettingValue(Settings.CARET_BLINK_RATE)).intValue();
  }
  public void setCaretBlinkRate(int rate) {
    changeProperty(Settings.CARET_BLINK_RATE, new Integer(rate));
  }

  public boolean getLineNumberVisible() {
    return ((Boolean) getSettingValue(Settings.LINE_NUMBER_VISIBLE)).booleanValue();
  }
  public void setLineNumberVisible(boolean b) {
    changeProperty(Settings.LINE_NUMBER_VISIBLE, (b ? Boolean.TRUE : Boolean.FALSE));
  }
  
  public Insets getScrollJumpInsets() {
    return (Insets)getSettingValue(Settings.SCROLL_JUMP_INSETS);
  }
  public void setScrollJumpInsets(Insets i) {
    changeProperty(Settings.SCROLL_JUMP_INSETS, i);
  }
  
  public Insets getScrollFindInsets() {
    return (Insets)getSettingValue(Settings.SCROLL_FIND_INSETS);
  }
  public void setScrollFindInsets(Insets i) {
    changeProperty(Settings.SCROLL_FIND_INSETS, i);
  }
  
  public List getKeyBindingList() {
    return (List) getSettingValue(Settings.KEY_BINDING_LIST);
  }
  public void setKeyBindingList(List list) {
    changeProperty(Settings.KEY_BINDING_LIST, list);
  }

  public Object[] getSystemColoringArray() {
    ColoringManager cm = (ColoringManager) getSettingValue(Settings.COLORING_MANAGER);
    return new Object[] {cm.getSystemColorings(getKitClass()),
        cm.getSystemColorings(getKitClass())[0], getTypeName()};
  }
  public void setSystemColoringArray(Object[] notUsed) {
    Settings.touchValue(Settings.COLORING_MANAGER);
  }
  
  public Object[] getTokenColoringArray() {
    ColoringManager cm = (ColoringManager) getSettingValue(Settings.COLORING_MANAGER);
    return new Object[] {cm.getTokenColorings(getKitClass()),
        cm.getSystemColorings(getKitClass())[0], getTypeName()};
  }
  public void setTokenColoringArray(Object[] notUsed) {
    Settings.touchValue(Settings.COLORING_MANAGER);
  }
  
  public float getLineHeightCorrection() {
    return ((Float) getSettingValue(Settings.LINE_HEIGHT_CORRECTION)).floatValue();
  }
  public void setLineHeightCorrection(float f) {
    changeProperty(Settings.LINE_HEIGHT_CORRECTION, new Float(f));
  }
  
  public Insets getMargin() {
    return (Insets)getSettingValue(Settings.MARGIN);
  }
  public void setMargin(Insets i) {
    changeProperty(Settings.MARGIN, i);
  }
  
  public Insets getLineNumberMargin() {
    return (Insets)getSettingValue(Settings.LINE_NUMBER_MARGIN);
  }
  public void setLineNumberMargin(Insets i) {
    changeProperty(Settings.LINE_NUMBER_MARGIN, i);
  }
  
  public boolean getStatusBarVisible() {
    return ((Boolean)getSettingValue(Settings.STATUS_BAR_VISIBLE)).booleanValue();
  }
  public void setStatusBarVisible(boolean v) {
    changeProperty(Settings.STATUS_BAR_VISIBLE, v ? Boolean.TRUE : Boolean.FALSE);
  }
  
  public int getStatusBarCaretDelay() {
    return ((Integer)getSettingValue(Settings.STATUS_BAR_CARET_DELAY)).intValue();
  }
  public void setStatusBarCaretDelay(int delay) {
    changeProperty(Settings.STATUS_BAR_CARET_DELAY, new Integer(delay));
  }
  

}

/*
 * Log
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
