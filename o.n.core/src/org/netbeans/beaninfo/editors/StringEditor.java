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
import com.netbeans.ide.util.Utilities;

/** A property editor for String class.
* @author   Ian Formanek
* @version  1.00, 18 Sep, 1998
*/
public class StringEditor extends PropertyEditorSupport {

  /** sets new value */
  public void setAsText(String s) {
    setValue(s);
  }

  public String getJavaInitializationString () {
    String s = (String) getValue ();
    s = Utilities.replaceString (s, "\\", "\\\\");
    s = Utilities.replaceString (s, "\"", "\\\"");
    s = Utilities.replaceString (s, "\n", "\\n");
    return "\""+s+"\"";
  }
  
  public boolean supportsCustomEditor () {
    return true;
  }

  public java.awt.Component getCustomEditor () {
    return new StringCustomEditor (this);
  }

}

/*
 * Log
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
