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
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/** A BeanInfo for global IDE settings.
*
* @author Ian Formanek
* @version 0.14, May 20, 1998
*/
public class IDESettingsBeanInfo extends SimpleBeanInfo {
  /** Icons for compiler settings objects. */
  Image icon;
  Image icon32;

  /** Array of property descriptors. */
  private static PropertyDescriptor[] desc;

   // initialization of the array of descriptors
  static {
    try {
      desc = new PropertyDescriptor[] {
        new PropertyDescriptor (IDESettings.PROP_LOOK_AND_FEEL, IDESettings.class,
                                "getLookAndFeel", "setLookAndFeel"),
        new PropertyDescriptor (IDESettings.PROP_SHOW_TIPS_ON_STARTUP, IDESettings.class,
                                "getShowTipsOnStartup", "setShowTipsOnStartup"),
        new PropertyDescriptor (IDESettings.PROP_LAST_TIP, IDESettings.class,
                                "getLastTip", "setLastTip"),
        new PropertyDescriptor (IDESettings.PROP_CONFIRM_DELETE, IDESettings.class,
                                "getConfirmDelete", "setConfirmDelete"),
        new PropertyDescriptor ("loadedBeans", IDESettings.class,
                                "getLoadedBeans", "setLoadedBeans"),
        new PropertyDescriptor (IDESettings.PROP_HOME_PAGE, IDESettings.class,
                                "getHomePage", "setHomePage"),
        new PropertyDescriptor (IDESettings.PROP_USE_PROXY, IDESettings.class,
                                "getUseProxy", "setUseProxy"),
        new PropertyDescriptor (IDESettings.PROP_PROXY_HOST, IDESettings.class,
                                "getProxyHost", "setProxyHost"),
        new PropertyDescriptor (IDESettings.PROP_PROXY_PORT, IDESettings.class,
                                "getProxyPort", "setProxyPort"),
      };

      desc[0].setDisplayName (Main.getString ("PROP_LOOK_AND_FEEL"));
      desc[0].setShortDescription (Main.getString ("HINT_LOOK_AND_FEEL"));
      desc[0].setPropertyEditorClass (IDESettingsBeanInfo.LookAndFeelPropertyEditor.class);

      desc[1].setDisplayName (Main.getString ("PROP_SHOW_TIPS_ON_STARTUP"));
      desc[1].setShortDescription (Main.getString ("HINT_SHOW_TIPS_ON_STARTUP"));

      desc[2].setHidden (true);

      desc[3].setDisplayName (Main.getString ("PROP_CONFIRM_DELETE"));
      desc[3].setShortDescription (Main.getString ("HINT_CONFIRM_DELETE"));

      desc[4].setHidden(true);

      desc[5].setDisplayName (Main.getString ("PROP_HOME_PAGE"));
      desc[5].setShortDescription (Main.getString ("HINT_HOME_PAGE"));
      
      desc[6].setDisplayName (Main.getString ("PROP_USE_PROXY"));
      desc[6].setShortDescription (Main.getString ("HINT_USE_PROXY"));

      desc[7].setDisplayName (Main.getString ("PROP_PROXY_HOST"));
      desc[7].setShortDescription (Main.getString ("HINT_PROXY_HOST"));

      desc[8].setDisplayName (Main.getString ("PROP_PROXY_PORT"));
      desc[8].setShortDescription (Main.getString ("HINT_PROXY_PORT"));

    } catch (IntrospectionException ex) {
      if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
    }
  }

  /** Creates a new IDESettingsBeanInfo */
  public IDESettingsBeanInfo () {
    icon = loadImage("/com/netbeans/developer/impl/resources/ideSettings.gif");
    icon32 = loadImage ("/com/netbeans/developer/impl/resources/ideSettings32.gif");
  }

  /** Provides an explicit property info. */
  public PropertyDescriptor[] getPropertyDescriptors() {
    return desc;
  }

  /** Returns the IDESettings' icon */
  public Image getIcon(int type) {
    if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
      return icon;
    else
      return icon32;
  }

  public static class LookAndFeelPropertyEditor extends PropertyEditorSupport {
    /** The LookAndFeels. */
    private static java.util.Vector lfs;
    /** Names for LookAndFeels. */
    private static String[] plafNames;
    /** mapping LookAndFeel -> index in the table */
    private static Hashtable lafToIndex;

    static {
      // populate the list of available and supported Look&Feels
      UIManager.LookAndFeelInfo[] plafInfos = UIManager.getInstalledLookAndFeels();

      lfs = new java.util.Vector();
      lafToIndex = new Hashtable ();

      for (int i=0; i<plafInfos.length; i++) {
        try {
          Class lfClass = Class.forName(plafInfos[i].getClassName());
          LookAndFeel laf = (LookAndFeel)lfClass.newInstance();
          if (laf.isSupportedLookAndFeel ()) {
            lfs.addElement (laf);
          }
        } catch (Exception e) {
          // problem with L&F - ignore that L&F
        }
      }
      plafNames = new String[lfs.size()];
      for (int i=0; i < lfs.size (); i++) {
        LookAndFeel laf = (LookAndFeel)lfs.elementAt (i);
        plafNames [i] = laf.getName ();
        lafToIndex.put (laf.getClass (), new Integer (i));
      }
    }

    /** @return names of the supported LookAndFeels */
    public String[] getTags () {
      return plafNames;
    }

    /** @return text for the current value */
    public String getAsText () {
      Object obj = getValue ();
      if (obj != null) {
        Integer index = (Integer) lafToIndex.get (obj.getClass ());
        if (index != null)
          return plafNames [index.intValue ()];
      }
      return "";
    }

    /** @param text A text for the current value. */
    public void setAsText (String text) {
      for (int i=0; i < plafNames.length; i++)
        if (plafNames[i].equals (text)) {
          setValue (lfs.elementAt (i));
          return;
        }
      throw new IllegalArgumentException ();
    }

  }
}


/*
 * Log
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
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Palka       add property ShowTipsOnStartup nad lastTip
 */
