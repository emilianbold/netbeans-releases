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

package com.netbeans.developer.modules.loaders.form.palette;

import com.netbeans.ide.cookies.InstanceCookie;
import com.netbeans.developerx.loaders.form.formeditor.layouts.DesignLayout;
import com.netbeans.developerx.loaders.form.formeditor.border.DesignBorder;
import com.netbeans.developerx.loaders.form.formeditor.border.BorderInfo;

import java.lang.ref.WeakReference;

/** The PaletteItem encapsulate all objects that can be used as components in the form editor
*
* @author   Ian Formanek
*/
public class PaletteItem implements java.io.Serializable {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = -2098259549820241091L;

// -----------------------------------------------------------------------------
// Global class variables

  /** Weak reference to shared instance of the JavaBean */
  private WeakReference sharedReference = null;

  /** The JavaBean Class represented by this PaletteItem */
  private Class beanClass;
  
// -----------------------------------------------------------------------------
// Constructors

  /** Creates a new PaletteItem */
  public PaletteItem (InstanceCookie cookie) throws ClassNotFoundException, java.io.IOException {
    this (cookie.instanceClass ());
  }

  /** Creates a new PaletteItem for specified JavaBean class 
  * @param beanClass the string name of the Java Bean's classass
  */
  public PaletteItem (String beanName) throws ClassNotFoundException {
    this (Class.forName (beanName));
  }
  
  /** Creates a new PaletteItem for specified JavaBean class
  * @param beanClass the Java Bean's class
  */
  public PaletteItem (Class beanClass) {
    this.beanClass = beanClass;
  }
  
// -----------------------------------------------------------------------------
// Class Methods

  public Object getSharedInstance () throws InstantiationException, IllegalAccessException {
    Object sharedObject;
    if ((sharedReference == null) || ((sharedObject = sharedReference.get ()) == null)) {
      sharedObject = createInstance ();
      sharedReference = new WeakReference (sharedObject);
    }
    
    return sharedObject;
  }
  
  public Object createInstance () throws InstantiationException, IllegalAccessException {
    if (beanClass == null) return null;
    return beanClass.newInstance ();
  }

  public Class getItemClass () {
    return beanClass;
  }

  public java.beans.BeanInfo getBeanInfo () {
    try {
      return java.beans.Introspector.getBeanInfo (beanClass);
    } catch (java.beans.IntrospectionException e) {
      return null;
    }
  }

  public DesignBorder createBorder () throws InstantiationException, IllegalAccessException {
    return new DesignBorder ((BorderInfo)createInstance ());
  }
  
  public boolean isBorder () {
    return BorderInfo.class.isAssignableFrom (beanClass);
  }

  public boolean isVisual () {
    return java.awt.Component.class.isAssignableFrom (beanClass);
  }

  public boolean isDesignLayout () {
    return DesignLayout.class.isAssignableFrom (beanClass);
  }

  public boolean isContainer () {
    return java.awt.Container.class.isAssignableFrom (beanClass);
  }
}

/*
 * Log
 *  4    Gandalf   1.3         5/15/99  Ian Formanek    
 *  3    Gandalf   1.2         5/14/99  Ian Formanek    
 *  2    Gandalf   1.1         5/14/99  Ian Formanek    
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
