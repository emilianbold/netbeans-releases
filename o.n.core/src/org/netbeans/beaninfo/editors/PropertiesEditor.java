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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditorSupport;
import java.util.Properties;

/** A property editor for Properties class.
* @author   Ian Formanek
*/
public class PropertiesEditor extends PropertyEditorSupport {

  public String getAsText(String s) {
    return org.openide.util.NbBundle.getBundle(PropertiesEditor.class).getString("CTL_TextProperties");
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
 *  3    Gandalf   1.2         1/13/00  Petr Jiricka    i18n
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         6/4/99   Ian Formanek    
 * $
 */
