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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
  
import com.netbeans.editor.Settings;
import com.netbeans.editor.Coloring;
import com.netbeans.editor.ColoringManager;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

/**
* Options for the base editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class OptionSupport extends SystemOption {
  
  static final long serialVersionUID = 2002899758839584077L;

  static final String OPTIONS_PREFIX = "OPTIONS_";

  private static ResourceBundle bundle;

  
  private Class kitClass;
  
  private String typeName;
  
  private PropertyChangeListener settingsListener;
  
  
  public OptionSupport(Class kitClass, String typeName) {
    this.kitClass = kitClass;
    this.typeName = typeName;
  }

  Class getKitClass() {
    return kitClass;
  }
  
  String getTypeName() {
    return typeName;
  }

  public String displayName() {
    return getString(OPTIONS_PREFIX + typeName);
  }

  void setSettingValue(String name, Object newValue) {
    Object oldValue = getSettingValue(name);
    if ((oldValue == null && newValue == null)
        || (oldValue != null && oldValue.equals(newValue))
    ) {
      return; // no change
    }
    
    Settings.setValue(kitClass, name, newValue);
    firePropertyChange(name, oldValue, newValue);
  }

  Object getSettingValue(String settingName) {
    return Settings.getValue(kitClass, settingName);
  }
  
  Settings.KitAndValue[] getSettingKitAndValueArray(String settingName) {
    return Settings.getKitAndValueArray(kitClass, settingName);
  }

  boolean getSettingBoolean(String settingName) {
    Boolean val = (Boolean)getSettingValue(settingName);
    return (val != null) ? val.booleanValue() : false;
  }

  void setSettingBoolean(String settingName, boolean newValue) {
    setSettingValue(settingName, newValue ? Boolean.TRUE : Boolean.FALSE);
  }

  int getSettingInteger(String settingName) {
    Integer val = (Integer)getSettingValue(settingName);
    return (val != null) ? val.intValue() : 0;
  }

  void setSettingInteger(String settingName, int newValue) {
    setSettingValue(settingName, new Integer(newValue));
  }

  public void setColoringsHelper(Object[] value, int[] sets) {
    ColoringManager cm = (ColoringManager) getSettingValue(Settings.COLORING_MANAGER);
    for (int i = 0; i < sets.length; i++) {
      Coloring[] cols = (Coloring[])value[i + 2];
      System.arraycopy(cols, 0,
          cm.getColorings(getKitClass(), sets[i]), 0, cols.length);
    }
    Settings.touchValue(getKitClass(), Settings.COLORING_MANAGER);
  }
  
  public Object[] getColoringsHelper(int[] sets) {
    ColoringManager cm = (ColoringManager) getSettingValue(Settings.COLORING_MANAGER);
    Object[] ret = new Object[2 + sets.length];
    ret[0] = getTypeName();
    ret[1] = cm.getDefaultColoring(getKitClass());
    for (int i = 0; i < sets.length; i++) {
      ret[i + 2] = cm.getColorings(getKitClass(), sets[i]);
    }
    return ret;
  }

  /** @return localized string */
  protected String getString(String s) {
    if (bundle == null) {
      bundle = NbBundle.getBundle(OptionSupport.class);
    }
    return bundle.getString(s);
  }

  public static String[] mergeStringArrays(String[] a1, String[] a2) {
    String[] ret = new String[a1.length + a2.length];
    for (int i = 0; i < a1.length; i++) {
      ret[i] = a1[i];
    }
    for (int i = 0; i < a2.length; i++) {
      ret[a1.length + i] = a2[i];
    }
    return ret;
  }

}

/*
 * Log
 *  9    Gandalf   1.8         11/11/99 Miloslav Metelka SVUID explicitly 
 *       specified
 *  8    Gandalf   1.7         11/11/99 Miloslav Metelka 
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/15/99  Miloslav Metelka 
 *  5    Gandalf   1.4         8/27/99  Miloslav Metelka 
 *  4    Gandalf   1.3         8/17/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
