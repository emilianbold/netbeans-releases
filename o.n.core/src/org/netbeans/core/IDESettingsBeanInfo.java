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
        new PropertyDescriptor (IDESettings.PROP_OUTPUT_LEVEL, IDESettings.class,
                                "getOutputLevel", "setOutputLevel"),
        new PropertyDescriptor (IDESettings.PROP_LOOK_AND_FEEL, IDESettings.class,
                                "getLookAndFeel", "setLookAndFeel"),
        new PropertyDescriptor (IDESettings.PROP_SHOW_TIPS_ON_STARTUP, IDESettings.class,
                                "getShowTipsOnStartup", "setShowTipsOnStartup"),
        new PropertyDescriptor (IDESettings.PROP_LAST_TIP, IDESettings.class,
                                "getLastTip", "setLastTip"),
        new PropertyDescriptor (IDESettings.PROP_BEANINFO_SEARCH_PATH, IDESettings.class,
                                "getBeanInfoSearchPath", "setBeanInfoSearchPath"),
        new PropertyDescriptor (IDESettings.PROP_PROPERTYEDITOR_SEARCH_PATH, IDESettings.class,
                                "getPropertyEditorSearchPath", "setPropertyEditorSearchPath"),
        new PropertyDescriptor (IDESettings.PROP_CONFIRM_DELETE, IDESettings.class,
                                "getConfirmDelete", "setConfirmDelete"),
        new PropertyDescriptor ("loadedBeans", IDESettings.class,
                                "getLoadedBeans", "setLoadedBeans"),
        new PropertyDescriptor (IDESettings.PROP_HOME_PAGE, IDESettings.class,
                                "getHomePage", "setHomePage"),
      };

      desc[0].setDisplayName (Main.getString ("PROP_OUTPUT_LEVEL"));
      desc[0].setShortDescription (Main.getString ("HINT_OUTPUT_LEVEL"));
      desc[0].setPropertyEditorClass (IDESettingsBeanInfo.OutputLevelEditor.class);

      desc[1].setDisplayName (Main.getString ("PROP_LOOK_AND_FEEL"));
      desc[1].setShortDescription (Main.getString ("HINT_LOOK_AND_FEEL"));
      desc[1].setPropertyEditorClass (IDESettingsBeanInfo.LookAndFeelPropertyEditor.class);

      desc[2].setDisplayName (Main.getString ("PROP_SHOW_TIPS_ON_STARTUP"));
      desc[2].setShortDescription (Main.getString ("HINT_SHOW_TIPS_ON_STARTUP"));

      desc[3].setHidden (true);

      desc[4].setDisplayName (Main.getString ("PROP_BEANINFO_SEARCH_PATH"));
      desc[4].setShortDescription (Main.getString ("HINT_BEANINFO_SEARCH_PATH"));
      desc[4].setExpert (true);

      desc[5].setDisplayName (Main.getString ("PROP_PROPERTYEDITOR_SEARCH_PATH"));
      desc[5].setShortDescription (Main.getString ("HINT_PROPERTYEDITOR_SEARCH_PATH"));
      desc[5].setExpert (true);

      desc[6].setDisplayName (Main.getString ("PROP_CONFIRM_DELETE"));
      desc[6].setShortDescription (Main.getString ("HINT_CONFIRM_DELETE"));

      desc[7].setHidden(true);

      desc[8].setDisplayName (Main.getString ("PROP_HOME_PAGE"));
      desc[8].setShortDescription (Main.getString ("HINT_HOME_PAGE"));
      
    } catch (IntrospectionException ex) {
      ex.printStackTrace ();
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

  final public static class OutputLevelEditor extends java.beans.PropertyEditorSupport {
    /** Display Names for alignment. */
    private static final String[] names = {
      Main.getString ("VALUE_OutputLevel_Minimum"),
      Main.getString ("VALUE_OutputLevel_Normal"),
      Main.getString ("VALUE_OutputLevel_Maximum"),
    };

    /** @return names of the possible directions */
    public String[] getTags () {
      return names;
    }

    /** @return text for the current value */
    public String getAsText () {
      int value = ((Integer)getValue ()).intValue ();
      if ((value < 0) || (value > 2)) return null;
      return names [value];
    }

    /** Setter.
    * @param str string equal to one value from directions array
    */
    public void setAsText (String str) {
      for (int i = 0; i <= 2; i++) {
        if (names[i].equals (str)) {
          setValue (new Integer (i));
          return;
        }
      }
    }

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
