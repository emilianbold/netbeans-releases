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

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

/**
* Options for the base editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class OptionSupport extends SystemOption {
  
  static final String OPTIONS_PREFIX = "OPTIONS_";

  private static ResourceBundle bundle;

  
  private Class kitClass;
  
  private String typeName;
  
  private PropertyChangeListener settingsListener;
  
  
  public OptionSupport(Class kitClass, String typeName) {
    this.kitClass = kitClass;
    this.typeName = typeName;
    
    settingsListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          refresh(evt);
        }
    };
    Settings.addPropertyChangeListener(settingsListener);
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

  Object getSettingValue(String name) {
//    Object val = getProperty(name);
//    if (val == null) {
      Object val = Settings.getValue(kitClass, name);
//      putProperty(name, val);
//    }
    return val;
  }

  void refresh(PropertyChangeEvent evt) {
  }

  /** @return localized string */
  static String getString(String s) {
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
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
