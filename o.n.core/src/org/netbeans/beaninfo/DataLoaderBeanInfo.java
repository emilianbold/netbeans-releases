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
import org.openide.util.NbBundle;

/** BeanInfo for {@link DataLoader}. */
public class DataLoaderBeanInfo extends SimpleBeanInfo {
  
  public PropertyDescriptor[] getPropertyDescriptors () {
    try {
      PropertyDescriptor representationClass = new PropertyDescriptor ("representationClass", DataLoader.class, "getRepresentationClass", null);
      representationClass.setDisplayName (NbBundle.getBundle (DataLoaderBeanInfo.class).getString ("PROP_representationClass"));
      representationClass.setShortDescription (NbBundle.getBundle (DataLoaderBeanInfo.class).getString ("HINT_representationClass"));
      representationClass.setExpert (true);
      // [PENDING] maybe later "actions" should be included--once
      // it has a decent property editor, that is
      return new PropertyDescriptor[] { representationClass };
    } catch (IntrospectionException ie) {
      if (Boolean.getBoolean ("netbeans.debug.exceptions"))
        ie.printStackTrace ();
      return null;
    }
  }
  
}

/*
 * Log
 *  2    Gandalf   1.1         11/25/99 Jesse Glick     representationClass 
 *       expert loader property.
 *  1    Gandalf   1.0         11/3/99  Jesse Glick     
 * $
 */
