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

package org.netbeans.modules.editor.options;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.beans.SimpleBeanInfo;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

/**
* BeanInfo for Coloring class.
*
* @author Ales Novak
*/

public class ColoringBeanBeanInfo extends SimpleBeanInfo {
  /** Prefix of the icon location. */
  private String iconPrefix = "/org/netbeans/editor/resources/coloring"; // NOI18N

  /** Icons for compiler settings objects. */
  private Image icon;
  private Image icon32;

  /** Propertydescriptors */
  private static PropertyDescriptor[] descriptors;

  /*
  * @return Returns an array of PropertyDescriptors
  * describing the editable properties supported by this bean.
  */
  public PropertyDescriptor[] getPropertyDescriptors () {
    if (descriptors == null) {
      try {
        descriptors = new PropertyDescriptor[] {
          new PropertyDescriptor("coloring", ColoringBean.class) // NOI18N
        };
        descriptors[0].setPropertyEditorClass(ColoringEditor.class);
      } catch (Exception e) {
        descriptors = new PropertyDescriptor[0];
      }
    }
    return descriptors;
  }

  /* @param type Desired type of the icon
  * @return returns the Java loader's icon
  */
  public Image getIcon(final int type) {
    if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
      if (icon == null)
        icon = loadImage(iconPrefix + ".gif"); // NOI18N
      return icon;
    }
    else {
      if (icon32 == null)
        icon32 = loadImage(iconPrefix + "32.gif"); // NOI18N
      return icon32;
    }
  }

}

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Miloslav Metelka Localization
 *  5    Gandalf   1.4         12/28/99 Miloslav Metelka 
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/20/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/3/99   Ian Formanek    Changed package 
 *       statement to make it compilable
 *  1    Gandalf   1.0         6/30/99  Ales Novak      
 * $
 */

