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

package com.netbeans.developer.impl.beaninfo;

import java.awt.Image;
import java.beans.*;

import org.openide.loaders.MultiFileLoader;

public class XMLDataObject {
  
  private static Image icon;
  private static Image icon32;
  
  public static class LoaderBeanInfo extends SimpleBeanInfo {
    
    public BeanInfo[] getAdditionalBeanInfo () {
      try {
        return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
      } catch (IntrospectionException ie) {
        if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
          ie.printStackTrace ();
        return null;
      }
    }
    
    public Image getIcon (int type) {
      if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
        if (icon == null) icon = loadImage ("/org/openide/resources/xmlObject.gif"); // NOI18N
        return icon;
      } else {
        if (icon32 == null) icon32 = loadImage ("/org/openide/resources/xmlObject32.gif"); // NOI18N
        return icon32;
      }
    }
    
  }
  
}

/*
 * Log
 *  3    Jaga      1.1.1.0     3/9/00   Jesse Glick     
 *  2    Gandalf   1.1         1/13/00  Jaroslav Tulach I18N
 *  1    Gandalf   1.0         1/13/00  Jesse Glick     
 * $
 */
