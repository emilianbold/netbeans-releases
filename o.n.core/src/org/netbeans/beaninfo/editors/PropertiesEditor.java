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

import java.beans.PropertyEditorSupport;
import java.util.Properties;

/** A property editor for Properties class.
* @author   Ian Formanek
*/
public class PropertiesEditor extends PropertyEditorSupport {

  public String getAsText(String s) {
    return "<Properties>";
  }

  /** sets new value */
  public void setAsText(String s) {
  }

  public String getJavaInitializationString () {
    return null; // does not generate any code
  }
  
  public boolean supportsCustomEditor () {
    return true;
  }

  public java.awt.Component getCustomEditor () {
    return new PropertiesCustomEditor (this);
  }

}

/*
 * Log
 *  1    Gandalf   1.0         6/4/99   Ian Formanek    
 * $
 */
