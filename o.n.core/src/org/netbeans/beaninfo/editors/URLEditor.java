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
import java.net.URL;
import java.net.MalformedURLException;

/** A property editor for java.net.URL class.
*
* @author   Ian Formanek
*/
public class URLEditor extends PropertyEditorSupport {

  /** sets new value */
  public void setAsText(String s) {
    try {
      URL url = new URL (s);
      setValue(url);
    } catch (MalformedURLException e) {
      // cannot change -> ignore
    }
  }

  /** @return the current value as String */
  public String getAsText() {
    URL url = (URL)getValue ();
    return url.toString ();
  }
  
  public String getJavaInitializationString () {
    URL url = (URL) getValue ();
    return "\""+url.toString ()+"\"";
  }
  
  public boolean supportsCustomEditor () {
    return false;
  }

}

/*
 * Log
 *  2    Gandalf   1.1         5/8/99   Ian Formanek    Fixed to compile
 *  1    Gandalf   1.0         5/8/99   Ian Formanek    
 * $
 */
