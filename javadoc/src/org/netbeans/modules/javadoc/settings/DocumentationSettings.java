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

package com.netbeans.developer.modules.javadoc.settings;

import java.io.File;

import com.netbeans.ide.options.ContextSystemOption;
import com.netbeans.ide.util.NbBundle;

/** Options for applets - which applet viewer use ...
*
* @author Petr Hrebejk
* @version 0.1, Apr 15, 1999
*/
public class DocumentationSettings extends ContextSystemOption //implements ViewerConstants 
  {
  /** generated Serialized Version UID */
  //static final long serialVersionUID = 605615362662343329L;

  /** generation */
  private static boolean externalJavadoc = false;

  /** searchpath */
  private static String[] searchPath = new String[] {"c:/Jdk1.2/doc" };
  

  // Private attributes for option's children

  private static JavadocSettings javadocSettings;
  private static StdDocletSettings stdDocletSettings;


  static {
    // Create option's children
    javadocSettings  = new JavadocSettings ();
    stdDocletSettings =  new StdDocletSettings ();
  }

  /** Constructor for DocumentationSettings adds optipn's children */
  public DocumentationSettings () {
    addOption( javadocSettings );
    addOption( stdDocletSettings );
  }
    
  
  /** @return human presentable name */
  public String displayName() {
    return NbBundle.getBundle(JavadocSettings.class).getString("CTL_Documentation_settings");
  }

  /** getter for type of generation 
  */
  /*
  public boolean isExternalJavadoc () {
    return externalJavadoc;
  }
  */
  /** setter for viewer */
 
  public void setExternalJavadoc(boolean b) {
    externalJavadoc = b;
    /*
    if (v.equals(INTERNAL_BROWSER) || v.equals(APPLETVIEWER) || v.equals(EXTERNAL))
      viewer = v;
    */
  }
  
  /** Getter for documentation search path
  */
  public String[] getSearchPath() {
    return searchPath;
  }
 
  /** Setter for documentation search path
  */
  public void setSearchPath(String[] s) {
    searchPath = s;
  }
  
}

/*
 * Log
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $
 */
