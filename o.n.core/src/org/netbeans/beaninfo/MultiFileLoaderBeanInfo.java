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

import java.beans.*;

import org.openide.loaders.DataLoader;

public class MultiFileLoaderBeanInfo extends SimpleBeanInfo {
  
  public BeanInfo[] getAdditionalBeanInfo () {
    try {
      return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
    } catch (IntrospectionException ie) {
      if (Boolean.getBoolean ("netbeans.debug.exceptions"))
        ie.printStackTrace ();
      return null;
    }
  }
  
}

/*
 * Log
 *  1    Gandalf   1.0         1/13/00  Jesse Glick     
 * $
 */
