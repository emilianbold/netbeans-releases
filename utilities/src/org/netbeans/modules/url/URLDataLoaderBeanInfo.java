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

package com.netbeans.developer.modules.loaders.url;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** URL data loader bean info.
*
* @author Ian Formanek
*/
public class URLDataLoaderBeanInfo extends SimpleBeanInfo {

  /** Icons for url data loader. */
  private static Image icon;
  private static Image icon32;

  /** Propertydescriptors */
  private static PropertyDescriptor[] descriptors;

  /**
  * @return Returns an array of PropertyDescriptors
  * describing the editable properties supported by this bean.
  */
  public PropertyDescriptor[] getPropertyDescriptors () {
    if (descriptors == null) { 
      initializeDescriptors();
    }
    return descriptors;
  }

  /** @param type Desired type of the icon
  * @return returns the URL loader's icon
  */
  public Image getIcon(final int type) {
    if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
        (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
      if (icon == null)
        icon = loadImage("/com/netbeans/developer/modules/loaders/url/urlObject.gif"); // NOI18N
      return icon;
    } else {
      if (icon32 == null)
        icon32 = loadImage ("/com/netbeans/developer/modules/loaders/url/urlObject32.gif"); // NOI18N
      return icon32;
    }
  }

  private static void initializeDescriptors () {
    final ResourceBundle bundle =
      NbBundle.getBundle(URLDataLoaderBeanInfo.class);
    try {
      descriptors =  new PropertyDescriptor[] {
        new PropertyDescriptor ("displayName", URLDataLoader.class, "getDisplayName", null),
      };
      descriptors[0].setDisplayName(bundle.getString("PROP_Name"));
      descriptors[0].setShortDescription(bundle.getString("HINT_Name"));
    } catch (IntrospectionException e) {
      e.printStackTrace ();
    }
  }

}

/*
* Log
*  7    Gandalf   1.6         1/5/00   Ian Formanek    NOI18N
*  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         3/9/99   Ian Formanek    
*  3    Gandalf   1.2         3/9/99   Ian Formanek    
*  2    Gandalf   1.1         2/16/99  David Simonek   
*  1    Gandalf   1.0         1/22/99  Ian Formanek    
* $
*/
