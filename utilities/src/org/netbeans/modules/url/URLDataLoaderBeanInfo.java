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

import org.openide.loaders.UniFileLoader;

/** URL data loader bean info.
*
* @author Ian Formanek
*/
public class URLDataLoaderBeanInfo extends SimpleBeanInfo {

  public BeanInfo[] getAdditionalBeanInfo () {
    try {
      return new BeanInfo[] { Introspector.getBeanInfo (UniFileLoader.class) };
    } catch (IntrospectionException ie) {
      if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
        ie.printStackTrace ();
      return null;
    }
  }

  /** Icons for url data loader. */
  private static Image icon;
  private static Image icon32;

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

}

/*
* Log
*  9    Gandalf   1.8         1/16/00  Jesse Glick     
*  8    Gandalf   1.7         1/12/00  Ian Formanek    NOI18N
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
