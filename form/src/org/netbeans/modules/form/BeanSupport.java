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

package com.netbeans.developer.modules.loaders.form;

import org.openide.nodes.*;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/** BeanSupport is a utility class with various static methods supporting 
* operations with JavaBeans.
*
* @author Ian Formanek
*/
public class BeanSupport {
// -----------------------------------------------------------------------------
// Private variables

  private static HashMap errorEmptyMap = new HashMap (3);
  private static HashMap valuesCache = new HashMap (30);
  private static HashMap instancesCache = new HashMap (30);

// -----------------------------------------------------------------------------
// Public methods

  /** Utility method to create an instance of given class. Returns null on error.
  * @param beanClass the class to create inctance of
  * @return new instance of specified class or null if an error occured during instantiation
  */
  public static Object createBeanInstance (Class beanClass) {
    try {
      return beanClass.newInstance ();
    } catch (IllegalAccessException e) {
      // problem => return null;
    } catch (InstantiationException e) {
      // problem => return null;
    }
    return null;
  }

  /** Utility method to obtain a BeanInfo of given JavaBean class. Returns null on error.
  * @param beanClass the class to obtain BeanInfo for
  * @return BeanInfo instance or null if an error occured or the BeanInfo cannot be found 
  *                  throughout the BeanInfoSearchPath
  */
  public static BeanInfo createBeanInfo (Class beanClass) {
    try {
      return org.openide.util.Utilities.getBeanInfo (beanClass);
    } catch (IntrospectionException e) {
      return null;
    }
  }
  
  /** Utility method to obtain an instance of specified beanClass. The instance is reused, and 
  * thus should only be used to obtain info about settings of default instances of the specified class.
  * @param beanClass the class to create inctance of
  * @return instance of specified class or null if an error occured during instantiation
  */
  public static Object getDefaultInstance (Class beanClass) {
    Object defInstance = instancesCache.get (beanClass);
    if (defInstance == null) {
      defInstance = createBeanInstance (beanClass);
      instancesCache.put (beanClass, defInstance);
    }
    return defInstance;
  }

  /** Utility method to obtain a default property values of specified JavaBean class.
  * The default values are property values immediately after the instance is created.
  * Because some AWT components initialize their properties only after the peer is
  * created, these are treated specially and default values for those properties
  * are provided explicitely (e.g. though the value of Font property of java.awt.Button
  * is null after an instance of Button is created, this method will return the
  * Font (Dialog, 12, PLAIN) as the default value).
  *
  * @param beanClass The Class of the JavaBean for which the default values are to be obtained
  * @returns Map containing pairs <PropertyName (String), value (Object)>
  * @see #getDefaultPropertyValue
  */
  public static Map getDefaultPropertyValues (Class beanClass) {
    Map defValues = (Map) valuesCache.get (beanClass);
    if (defValues == null) {
      Object beanInstance = getDefaultInstance (beanClass);
      if (beanInstance == null)
        return errorEmptyMap;
      defValues = getPropertyValues (beanInstance);
      valuesCache.put (beanClass, defValues);
    }
    return defValues;
  }
  
  /** Utility method to obtain a default value of specified JavaBean class and property name.
  * The default values are property values immediately after the instance is created.
  * Because some AWT components initialize their properties only after the peer is
  * created, these are treated specially and default values for those properties
  * are provided explicitely (e.g. though the value of Font property of java.awt.Button
  * is null after an instance of Button is created, this method will return the
  * Font (Dialog, 12, PLAIN) as the default value).
  *
  * @param beanClass The Class of the JavaBean for which the default value is to be obtained
  * @param beanClass The name of the propertyn for which the default value is to be obtained
  * @returns The default property value for specified property on specified JavaBean class
  * @see #getDefaultPropertyValues
  */
  public Object getDefaultPropertyValue (Class beanClass, String propertyName) {
    return getDefaultPropertyValues (beanClass).get (propertyName);
  }

  /** Utility method to obtain a current property values of given JavaBean instance.
  * Only the properties specified in bean info (if it exists) are provided.
  *
  * @returns Map containing pairs <PropertyName (String), value (Object)>
  */
  public static Map getPropertyValues (Object beanInstance) {
    if (beanInstance == null) {
      return errorEmptyMap;
    }
    
    BeanInfo info = createBeanInfo (beanInstance.getClass ());
    PropertyDescriptor[] properties = info.getPropertyDescriptors ();
    HashMap defaultValues = new HashMap (properties.length * 2);
    
    for (int i = 0; i < properties.length; i++) {
      Method readMethod = properties[i].getReadMethod ();
      if (readMethod != null) {
        try {
          Object value = readMethod.invoke (beanInstance, new Object [0]);
          if (value == null)
            value = getSpecialDefaultAWTValue (beanInstance, properties[i].getName ());
          defaultValues.put (properties[i].getName (), value);
        } catch (Exception e) {
          // problem with reading property ==>> no default value
          if (FormEditor.getFormSettings ().getOutputLevel () != FormLoaderSettings.OUTPUT_MINIMUM) {
//            notifyPropertyException (beanInstance.getClass (), properties [i].getName (), "component", e, true);
          }  
        } 
      } else { // the property does not have plain read method
        if (properties[i] instanceof IndexedPropertyDescriptor) {
//          [PENDING]
//          Method indexedReadMethod = ((IndexedPropertyDescriptor)properties[i]).getIndexedReadMethod ();
        } 
      }
    } 

    return defaultValues;
  }

  /** Utility method to obtain an icon of specified JavaBean class and property name.
  *
  * @param iconType The icon type as defined in BeanInfo (BeanInfo.ICON_COLOR_16x16, ...)
  * @returns The icon of specified JavaBean or null if not defined
  */
  public static Image getBeanIcon (Class beanClass, int iconType) {
    // [FUTURE: the icon should be obtained from the InstanceCookie somehow, and customizable by the user]
    BeanInfo bi = createBeanInfo (beanClass);
    if (bi != null) {
      return bi.getIcon (iconType);
    }
    return null;
  }
  
  /** A utility method that returns a class of event adapter for
  * specified listener. It works only on known listeners from java.awt.event.
  * Null is returned for unknown listeners.
  * @return class of an adapter for specified listener or null if
  *               unknown/does not exist
  */
  public static Class getAdapterForListener (Class listener) {
    if (java.awt.event.ComponentListener.class.equals (listener))
      return java.awt.event.ComponentAdapter.class;
    else if (java.awt.event.ContainerListener.class.equals (listener))
      return java.awt.event.ContainerAdapter.class;
    else if (java.awt.event.FocusListener.class.equals (listener))
      return java.awt.event.FocusAdapter.class;
    else if (java.awt.event.KeyListener.class.equals (listener))
      return java.awt.event.KeyAdapter.class;
    else if (java.awt.event.MouseListener.class.equals (listener))
      return java.awt.event.MouseAdapter.class;
    else if (java.awt.event.MouseMotionListener.class.equals (listener))
      return java.awt.event.MouseMotionAdapter.class;
    else if (java.awt.event.WindowListener.class.equals (listener))
      return java.awt.event.WindowAdapter.class;
    else return null; // not found
  }

  public static Node.Property [] createEventsProperties (Object beanInstance) {
    BeanInfo beanInfo = createBeanInfo (beanInstance.getClass ());
    EventSetDescriptor[] events = beanInfo.getEventSetDescriptors ();
    ArrayList eventsProps = new ArrayList ();
    for (int i = 0; i < events.length; i++) {
    }

    Node.Property[] np = new Node.Property [eventsProps.size ()];
    eventsProps.toArray (np);

    return np;
  }
  
// -----------------------------------------------------------------------------
// Private methods

  private static Object getSpecialDefaultAWTValue (Object beanObject, String propertyName) {
    if ((beanObject instanceof Frame) ||
        (beanObject instanceof Dialog)) {
      if ("background".equals (propertyName))
        return SystemColor.window;
      else if ("foreground".equals (propertyName))
        return SystemColor.windowText;
      else if ("font".equals (propertyName))
        return new Font ("Dialog", Font.PLAIN, 12);
    }

    if ((beanObject instanceof Label) ||
        (beanObject instanceof Button) ||
        (beanObject instanceof TextField) ||
        (beanObject instanceof TextArea) ||
        (beanObject instanceof Checkbox) ||
        (beanObject instanceof Choice) ||
        (beanObject instanceof List) ||
        (beanObject instanceof Scrollbar) ||
        (beanObject instanceof Panel) ||
        (beanObject instanceof ScrollPane)) {
      if ("background".equals (propertyName))
        return SystemColor.control;
      else if ("foreground".equals (propertyName))
        return SystemColor.controlText;
      else if ("font".equals (propertyName))
        return new Font ("Dialog", Font.PLAIN, 12);
    }

    return null;
  }

}

/*
 * Log
 *  11   Gandalf   1.10        9/24/99  Ian Formanek    getDefaultInstance 
 *       method added
 *  10   Gandalf   1.9         9/6/99   Ian Formanek    Defaults for Window and 
 *       Dialog fixed, default colors for components are taken from SystemColor 
 *       rather than hardcoded colors (was Color.lightGray and Color.black)
 *  9    Gandalf   1.8         9/2/99   Ian Formanek    Fixed bug 3696 - When 
 *       connection is copied and pasted into form, the initialization code of 
 *       the ConnectionSource component is not correctly generated. and 3695 - 
 *       Modified properties with null value are not restored correctly when a 
 *       form is reloaded.
 *  8    Gandalf   1.7         8/1/99   Ian Formanek    OutputLevel changes 
 *       reflected
 *  7    Gandalf   1.6         7/30/99  Ian Formanek    changed comment
 *  6    Gandalf   1.5         7/28/99  Ian Formanek    Fixed bug 2147 - 
 *       horizontalAlignment property displays number instead of text.
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/30/99  Ian Formanek    Removed 
 *       getPropertyEditor
 *  3    Gandalf   1.2         5/5/99   Ian Formanek    
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/29/99  Ian Formanek    
 * $
 */
