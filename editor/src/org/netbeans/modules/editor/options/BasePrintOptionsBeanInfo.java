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

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for plain options
*
* @author Miloslav Metelka, Ales Novak
*/
public class BasePrintOptionsBeanInfo extends SimpleBeanInfo {

  private ResourceBundle bundle;

  /** Prefix of the icon location. */
  private String iconPrefix;

  /** Icons for compiler settings objects. */
  private Image icon;
  private Image icon32;

  /** Propertydescriptors */
  private static PropertyDescriptor[] descriptors;

  public BasePrintOptionsBeanInfo() {
    this("/com/netbeans/developer/modules/text/resources/baseOptions");
  }

  public BasePrintOptionsBeanInfo(String iconPrefix) {
    this.iconPrefix = iconPrefix;
  }

  /*
  * @return Returns an array of PropertyDescriptors
  * describing the editable properties supported by this bean.
  */
  public PropertyDescriptor[] getPropertyDescriptors () {
    if (descriptors == null) {
      String[] propNames = getPropNames();
      try {
        descriptors = new PropertyDescriptor[propNames.length];
        
        for (int i = 0; i < propNames.length; i++) {
          descriptors[i] = new PropertyDescriptor(propNames[i], getBeanClass());
          descriptors[i].setDisplayName(getString("PROP_" + propNames[i]));
          descriptors[i].setShortDescription(getString("HINT_" + propNames[i]));
        }

        getPD(BasePrintOptions.PRINT_COLORING_ARRAY_PROP).setPropertyEditorClass(ColoringArrayEditor.class);

      } catch (IntrospectionException e) {
        descriptors = new PropertyDescriptor[0];
      }
    }
    return descriptors;
  }

  protected String getString(String s) {
    if (bundle == null) {
      bundle = NbBundle.getBundle(BasePrintOptionsBeanInfo.class);
    }
    return bundle.getString(s);
  }

  protected Class getBeanClass() {
    return BasePrintOptions.class;
  }
  
  protected String[] getPropNames() {
    return BasePrintOptions.BASE_PROP_NAMES;
  }
  
  protected PropertyDescriptor getPD(String prop) {
    String[] propNames = getPropNames();
    for (int i = 0; i < descriptors.length; i++) {
      if (prop.equals(propNames[i])) {
        return descriptors[i];
      }
    }
    return null;
  }

  /* @param type Desired type of the icon
  * @return returns the Java loader's icon
  */
  public Image getIcon(final int type) {
    if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
      if (icon == null)
        icon = loadImage(iconPrefix + ".gif");
      return icon;
    }
    else {
      if (icon32 == null)
        icon32 = loadImage(iconPrefix + "32.gif");
      return icon32;
    }
  }
}

/*
* Log
*  8    Gandalf   1.7         11/14/99 Miloslav Metelka 
*  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         9/15/99  Miloslav Metelka 
*  5    Gandalf   1.4         8/27/99  Miloslav Metelka 
*  4    Gandalf   1.3         8/17/99  Miloslav Metelka 
*  3    Gandalf   1.2         7/29/99  Miloslav Metelka 
*  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
*  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
* $
*/
