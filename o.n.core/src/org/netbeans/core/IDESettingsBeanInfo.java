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

package com.netbeans.developer.impl;

import java.awt.Image;
import java.beans.*;
import java.util.Hashtable;
import java.util.Vector;

/** A BeanInfo for global IDE settings.
*
* @author Ian Formanek
*/
public class IDESettingsBeanInfo extends SimpleBeanInfo {
  /** Icons for compiler settings objects. */
  private static Image icon;
  private static Image icon32;

  /** Array of property descriptors. */
  private static PropertyDescriptor[] desc;

   // initialization of the array of descriptors
  static {
    try {
      desc = new PropertyDescriptor[] {
        new PropertyDescriptor (IDESettings.PROP_SHOW_TIPS_ON_STARTUP, IDESettings.class,
                                "getShowTipsOnStartup", "setShowTipsOnStartup"), // NOI18N
        new PropertyDescriptor (IDESettings.PROP_LAST_TIP, IDESettings.class,
                                "getLastTip", "setLastTip"), // NOI18N
        new PropertyDescriptor (IDESettings.PROP_CONFIRM_DELETE, IDESettings.class,
                                "getConfirmDelete", "setConfirmDelete"), // NOI18N
        new PropertyDescriptor ("loadedBeans", IDESettings.class, // NOI18N
                                "getLoadedBeans", "setLoadedBeans"), // NOI18N
        new PropertyDescriptor (IDESettings.PROP_HOME_PAGE, IDESettings.class,
                                "getHomePage", "setHomePage"), // NOI18N
        new PropertyDescriptor (IDESettings.PROP_USE_PROXY, IDESettings.class,
                                "getUseProxy", "setUseProxy"), // NOI18N
        new PropertyDescriptor (IDESettings.PROP_PROXY_HOST, IDESettings.class,
                                "getProxyHost", "setProxyHost"), // NOI18N
        new PropertyDescriptor (IDESettings.PROP_PROXY_PORT, IDESettings.class,
                                "getProxyPort", "setProxyPort"), // NOI18N
      };

      desc[0].setDisplayName (Main.getString ("PROP_SHOW_TIPS_ON_STARTUP"));
      desc[0].setShortDescription (Main.getString ("HINT_SHOW_TIPS_ON_STARTUP"));

      desc[1].setHidden (true);

      desc[2].setDisplayName (Main.getString ("PROP_CONFIRM_DELETE"));
      desc[2].setShortDescription (Main.getString ("HINT_CONFIRM_DELETE"));

      desc[3].setHidden(true);

      desc[4].setDisplayName (Main.getString ("PROP_HOME_PAGE"));
      desc[4].setShortDescription (Main.getString ("HINT_HOME_PAGE"));
      
      desc[5].setDisplayName (Main.getString ("PROP_USE_PROXY"));
      desc[5].setShortDescription (Main.getString ("HINT_USE_PROXY"));

      desc[6].setDisplayName (Main.getString ("PROP_PROXY_HOST"));
      desc[6].setShortDescription (Main.getString ("HINT_PROXY_HOST"));

      desc[7].setDisplayName (Main.getString ("PROP_PROXY_PORT"));
      desc[7].setShortDescription (Main.getString ("HINT_PROXY_PORT"));

    } catch (IntrospectionException ex) {
      if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
    }
  }

  /** Provides an explicit property info. */
  public PropertyDescriptor[] getPropertyDescriptors() {
    return desc;
  }

  /** Returns the IDESettings' icon */
  public Image getIcon(int type) {
    if (icon == null) {
      icon = loadImage("/com/netbeans/developer/impl/resources/ideSettings.gif"); // NOI18N
      icon32 = loadImage ("/com/netbeans/developer/impl/resources/ideSettings32.gif"); // NOI18N
    }
    if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
      return icon;
    else
      return icon32;
  }

}


/*
 * Log
 *  14   Gandalf   1.13        1/13/00  Jaroslav Tulach I18N
 *  13   Gandalf   1.12        1/10/00  Ian Formanek    Removed Look&Feel 
 *       property
 *  12   Gandalf   1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        8/7/99   Ian Formanek    Cleaned loading of icons
 *  10   Gandalf   1.9         8/1/99   Ian Formanek    Got rid of Output 
 *       Details property
 *  9    Gandalf   1.8         7/24/99  Ian Formanek    Printing stack trace on 
 *       netbeans.debug.exceptions property only
 *  8    Gandalf   1.7         7/21/99  Ian Formanek    Fixed last change
 *  7    Gandalf   1.6         7/21/99  Ian Formanek    settings for proxy, 
 *       property output detail level hidden
 *  6    Gandalf   1.5         7/20/99  Ian Formanek    Removed 
 *       PropertyEditorSearchPath and BeanInfoSearchPath properties
 *  5    Gandalf   1.4         7/19/99  Jan Jancura     
 *  4    Gandalf   1.3         4/8/99   Ian Formanek    Undone last change
 *  3    Gandalf   1.2         4/8/99   Ian Formanek    Removed SearchPath 
 *       properties
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
