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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
  
import com.netbeans.editor.Settings;
import com.netbeans.editor.ColoringManager;
import com.netbeans.editor.BaseKit;

/**
* Options for the plain editor kit
*
* @author Miloslav Metelka
*/
public class BasePrintOptions extends OptionSupport {

  public static final String BASE = "base";
  
  public static final String PRINT_PREFIX = "print_";
  
  public static final String PRINT_LINE_NUMBER_VISIBLE_PROP = "printLineNumberVisible";
  
  public static final String PRINT_SYSTEM_COLORING_ARRAY_PROP = "printSystemColoringArray";
  
  public static final String PRINT_TOKEN_COLORING_ARRAY_PROP = "printTokenColoringArray";
  
  static final String[] BASE_PROP_NAMES = {
    PRINT_LINE_NUMBER_VISIBLE_PROP,
    PRINT_SYSTEM_COLORING_ARRAY_PROP,
    PRINT_TOKEN_COLORING_ARRAY_PROP
  };

  public BasePrintOptions() {
    this(BaseKit.class, BASE);
  }

  public BasePrintOptions(Class kitClass, String typeName) {
    super(kitClass, typeName);
  }

  public String displayName() {
    return getString(OPTIONS_PREFIX + PRINT_PREFIX + getTypeName());
  }

  public boolean getPrintLineNumberVisible() {
    return ((Boolean)getSettingValue(Settings.PRINT_LINE_NUMBER_VISIBLE)).booleanValue();
  }
  public void setPrintLineNumberVisible(boolean b) {
    changeProperty(Settings.PRINT_LINE_NUMBER_VISIBLE, (b ? Boolean.TRUE : Boolean.FALSE));
  }

  public Object[] getPrintSystemColoringArray() {
    ColoringManager cm = (ColoringManager)getSettingValue(Settings.COLORING_MANAGER);
    return new Object[] {
      cm.getPrintSystemColorings(getKitClass()),
      cm.getPrintDefaultColoring(getKitClass()),
      BASE
    };
  }
  public void setPrintSystemColoringArray(Object[] notUsed) {
    Settings.touchValue(Settings.COLORING_MANAGER);
  }
  
  public Object[] getPrintTokenColoringArray() {
    ColoringManager cm = (ColoringManager)getSettingValue(Settings.COLORING_MANAGER);
    return new Object[] {
      cm.getPrintTokenColorings(getKitClass()),
      cm.getPrintDefaultColoring(getKitClass()),
      getTypeName()
    };
  }
  public void setPrintTokenColoringArray(Object[] notUsed) {
    Settings.touchValue(Settings.COLORING_MANAGER);
  }
  
}

/*
 * Log
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
