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

package com.netbeans.developer.modules.loaders.image;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** Image data loader bean info.
*
* @author Dafe Simonek
*/
public class ImageDataLoaderBeanInfo extends SimpleBeanInfo {

  /** Icons for image data loader. */
  private static Image icon;
  private static Image icon32;

  /** Property descriptors. */
  private static PropertyDescriptor[] descriptors;


  /** Default constructor.
  */
  public ImageDataLoaderBeanInfo() {
  }

  public PropertyDescriptor[] getPropertyDescriptors () {
    if (descriptors == null) initializeDescriptors();
    return descriptors;
  }

  public Image getIcon(final int type) {
    if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
        (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
      if (icon == null)
        icon = loadImage("/com/netbeans/developer/modules/loaders/image/imageObject.gif"); // NOI18N
      return icon;
    } else {
      if (icon32 == null)
        icon32 = loadImage ("/com/netbeans/developer/modules/loaders/image/imageObject32.gif"); // NOI18N
      return icon32;
    }
  }

  private static void initializeDescriptors () {
    final ResourceBundle bundle =
      NbBundle.getBundle(ImageDataLoaderBeanInfo.class);
    try {
      descriptors =  new PropertyDescriptor[] {
        new PropertyDescriptor ("displayName", ImageDataLoader.class, // NOI18N
                                "getDisplayName", null), // NOI18N
        new PropertyDescriptor ("extensions", ImageDataLoader.class, // NOI18N
                                "getExtensions", "setExtensions") // NOI18N
      };
      descriptors[0].setDisplayName(bundle.getString("PROP_Name"));
      descriptors[0].setShortDescription(bundle.getString("HINT_Name"));
      descriptors[1].setDisplayName(bundle.getString("PROP_Extensions"));
      descriptors[1].setShortDescription(bundle.getString("HINT_Extensions"));
    } catch (IntrospectionException e) {
      e.printStackTrace ();
    }
  }

}

/*
 * Log
 *  8    Gandalf   1.7         1/5/00   Ian Formanek    NOI18N
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         4/13/99  Jesse Glick     Clean-ups of comments 
 *       and such for public perusal.
 *  4    Gandalf   1.3         3/22/99  Ian Formanek    Icons moved from 
 *       modules/resources to this package
 *  3    Gandalf   1.2         2/16/99  David Simonek   
 *  2    Gandalf   1.1         1/22/99  Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
