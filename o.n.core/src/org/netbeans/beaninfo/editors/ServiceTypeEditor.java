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

package com.netbeans.developer.editors;

import java.beans.*;
import java.util.ArrayList;
import java.util.Enumeration;

import org.openide.TopManager;
import org.openide.ServiceType;
import org.openide.execution.Executor;

/** Support for property editor for Executor.
*
* @author   Jaroslav Tulach
*/
public class ServiceTypeEditor extends java.beans.PropertyEditorSupport {

  /** tagx */
  private String[] tags;
  
  /** class to work on */
  private Class clazz;
  
  /** message to be used in custom editor */
  private String message;

  /** constructs new property editor.
  */
  public ServiceTypeEditor() {
    this (ServiceType.class, "");  
  }

  /** constructs new property editor.
  * @param clazz the class to use 
  * @param message the message for custom editor
  */
  public ServiceTypeEditor(Class clazz, String message) {
    this.clazz = clazz;
    this.message = getString (message);
    update ();
  }
  
  /** Updates the list of executors.
  */
  private void update () {
    java.util.LinkedList names = new java.util.LinkedList ();
    Enumeration ee = TopManager.getDefault ().getServices ().services (clazz);
    while (ee.hasMoreElements()) {
      ServiceType e = (ServiceType) ee.nextElement();
      names.add(e.getName());
    }
    names.toArray(tags = new String[names.size()]);
  }
  
  /** This method is intended for use when generating Java code to set
  * the value of the property.  It should return a fragment of Java code
  * that can be used to initialize a variable with the current property
  * value.
  * <p>
  *
  * @return A fragment of Java code representing an initializer for the
  *    current value.
  */
  public String getJavaInitializationString() {
    return "???";
  }

  
  //----------------------------------------------------------------------

  /**
  * @return The property value as a human editable string.
  * <p>   Returns null if the value can't be expressed as an editable string.
  * <p>   If a non-null value is returned, then the PropertyEditor should
  *       be prepared to parse that string back in setAsText().
  */
  public String getAsText() {
    ServiceType s = (ServiceType)getValue ();
    if (s == null)
      return getString ("LAB_DefaultServiceType");
    else 
      return s.getName();
  }


  /** Set the property value by parsing a given String.  May raise
  * java.lang.IllegalArgumentException if either the String is
  * badly formatted or if this kind of property can't be expressed
  * as text.
  * @param text  The string to be parsed.
  */
  public void setAsText(String text) {
    setValue(TopManager.getDefault ().getServices ().find (text));
  }

  /** @return tags */
  public String[] getTags() {
    update ();
    return tags;
  }
  
  public boolean supportsCustomEditor () {
    return true;
  }
  
  public java.awt.Component getCustomEditor () {
    final ServiceTypePanel s = new ServiceTypePanel (clazz, message);
    
    s.setServiceType ((ServiceType)getValue ());
    s.addPropertyChangeListener (new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent ev) {
        if ("serviceType".equals (ev.getPropertyName ())) {
          setValue (s.getServiceType ());
        }
      }
    });
    
    return s;
  }

  private static String getString (String s) {
    return org.openide.util.NbBundle.getBundle (ServiceTypeEditor.class).getString (s);
  }
}


/*
 * Log
 *  1    Gandalf   1.0         9/15/99  Jaroslav Tulach 
 * $
 */
