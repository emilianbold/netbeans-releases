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

import org.openide.loaders.DataLoader;
import org.openide.loaders.UniFileLoader;

public class DataLoaderPool {
  
  private static Image folderIcon;
  private static Image folderIcon32;
  private static Image shadowIcon;
  private static Image shadowIcon32;
  private static Image instanceIcon;
  private static Image instanceIcon32;
  private static Image defaultIcon;
  private static Image defaultIcon32;
  
  public static class FolderLoaderBeanInfo extends SimpleBeanInfo {
    
    public BeanInfo[] getAdditionalBeanInfo () {
      try {
        return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
      } catch (IntrospectionException ie) {
        if (Boolean.getBoolean ("netbeans.debug.exceptions"))
          ie.printStackTrace ();
        return null;
      }
    }
    
    public Image getIcon (int type) {
      if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
        if (folderIcon == null) folderIcon = loadImage ("/org/openide/resources/defaultFolder.gif");
        return folderIcon;
      } else {
        if (folderIcon32 == null) folderIcon32 = loadImage ("/org/openide/resources/defaultFolder32.gif");
        return folderIcon32;
      }
    }
    
  }
  
  public static class InstanceLoaderBeanInfo extends SimpleBeanInfo {
    
    public BeanInfo[] getAdditionalBeanInfo () {
      try {
        return new BeanInfo[] { Introspector.getBeanInfo (UniFileLoader.class) };
      } catch (IntrospectionException ie) {
        if (Boolean.getBoolean ("netbeans.debug.exceptions"))
          ie.printStackTrace ();
        return null;
      }
    }
    
    public Image getIcon (int type) {
      if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
        if (instanceIcon == null) instanceIcon = loadImage ("/com/netbeans/developer/impl/resources/action.gif");
        return instanceIcon;
      } else {
        if (instanceIcon32 == null) instanceIcon32 = loadImage ("/com/netbeans/developer/impl/resources/action32.gif");
        return instanceIcon32;
      }
    }
    
  }
  
  public static class DefaultLoaderBeanInfo extends SimpleBeanInfo {
    
    public BeanInfo[] getAdditionalBeanInfo () {
      try {
        return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
      } catch (IntrospectionException ie) {
        if (Boolean.getBoolean ("netbeans.debug.exceptions"))
          ie.printStackTrace ();
        return null;
      }
    }
    
    public Image getIcon (int type) {
      if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
        if (defaultIcon == null) defaultIcon = loadImage ("/org/openide/resources/pending.gif");
        return defaultIcon;
      } else {
        if (defaultIcon32 == null) defaultIcon32 = loadImage ("/org/openide/resources/pending32.gif");
        return defaultIcon32;
      }
    }
    
  }
  
  public static class ShadowLoaderBeanInfo extends SimpleBeanInfo {
    
    public BeanInfo[] getAdditionalBeanInfo () {
      try {
        return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
      } catch (IntrospectionException ie) {
        if (Boolean.getBoolean ("netbeans.debug.exceptions"))
          ie.printStackTrace ();
        return null;
      }
    }
    
    public PropertyDescriptor[] getPropertyDescriptors () {
      try {
        // Hide the actions property from users, since shadows inherit actions anyway:
        PropertyDescriptor actions = new PropertyDescriptor ("actions", org.openide.loaders.DataLoaderPool.ShadowLoader.class);
        actions.setHidden (true);
        return new PropertyDescriptor[] { actions };
      } catch (IntrospectionException ie) {
        if (Boolean.getBoolean ("netbeans.debug.exceptions"))
          ie.printStackTrace ();
        return null;
      }
    }
    
    public Image getIcon (int type) {
      if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
        if (shadowIcon == null) shadowIcon = loadImage ("/org/openide/resources/actions/copy.gif");
        return shadowIcon;
      } else {
        // [PENDING]
        //if (shadowIcon32 == null) shadowIcon32 = loadImage ("/org/openide/resources/actions/copy32.gif");
        return shadowIcon32;
      }
    }
    
  }
  
}

/*
 * Log
 *  1    Gandalf   1.0         1/13/00  Jesse Glick     
 * $
 */
