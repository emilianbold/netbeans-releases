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
import com.netbeans.editor.SettingsUtil;
import com.netbeans.editor.BaseKit;

import org.openide.util.HelpCtx;

/**
* Options for the plain editor kit
*
* @author Miloslav Metelka
*/
public class BasePrintOptions extends OptionSupport {

  public static final String BASE = "base";
  
  public static final String PRINT_PREFIX = "print_";
  
  public static final String PRINT_LINE_NUMBER_VISIBLE_PROP = "printLineNumberVisible";
  
  public static final String PRINT_COLORING_MAP_PROP = "printColoringMap";
  
  static final String[] BASE_PROP_NAMES = {
    PRINT_LINE_NUMBER_VISIBLE_PROP,
    PRINT_COLORING_MAP_PROP,
  };

    static final long serialVersionUID =7740651671176408299L;
  public BasePrintOptions() {
    this(BaseKit.class, BASE);
  }

  public BasePrintOptions(Class kitClass, String typeName) {
    super(kitClass, typeName);
  }

  public String displayName() {
    return getString(OPTIONS_PREFIX + PRINT_PREFIX + getTypeName());
  }

  public HelpCtx getHelpCtx () {
    return new HelpCtx (BasePrintOptions.class);
  }
  
  public boolean getPrintLineNumberVisible() {
    return ((Boolean)getSettingValue(Settings.PRINT_LINE_NUMBER_VISIBLE)).booleanValue();
  }
  public void setPrintLineNumberVisible(boolean b) {
    setSettingValue(Settings.PRINT_LINE_NUMBER_VISIBLE, (b ? Boolean.TRUE : Boolean.FALSE));
  }

  public Map getPrintColoringMap() {
    Map cm = SettingsUtil.getColoringMap(getKitClass(), false);
    cm.put(null, getKitClass()); // add kit class
    return cm;
  }
  public void setPrintColoringMap(Map coloringMap) {
    coloringMap.remove(null); // remove kit class
    SettingsUtil.updateColoringSettings(getKitClass(), coloringMap, true);
  }
  
}

/*
 * Log
 *  10   Gandalf   1.9         1/11/00  Petr Nejedly    Fix for missing print 
 *       options
 *  9    Gandalf   1.8         12/28/99 Miloslav Metelka 
 *  8    Gandalf   1.7         11/27/99 Patrik Knakal   
 *  7    Gandalf   1.6         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/27/99  Miloslav Metelka 
 *  4    Gandalf   1.3         8/17/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
