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
import org.openide.util.Utilities;

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
    s = Utilities.replaceString (s, "\\", "\\\\"); // NOI18N
    s = Utilities.replaceString (s, "\"", "\\\""); // NOI18N
    s = Utilities.replaceString (s, "\n", "\\n"); // NOI18N
    s = Utilities.replaceString (s, "\t", "\\t"); // NOI18N
    return "\""+s+"\""; // NOI18N
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
 *  5    Gandalf   1.4         1/13/00  Petr Jiricka    i18n
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/14/99  Ian Formanek    Fixed bug 3875 - The 
 *       code generated for tabs in string properties should be "\t" instead of 
 *       the tab, which is expanded to spaces by the editor.
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
