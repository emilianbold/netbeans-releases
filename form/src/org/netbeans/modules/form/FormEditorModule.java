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

package com.netbeans.developer.modules.form;

import com.netbeans.ide.modules.ModuleInstall;

/**
* Module installation class for Form Editor
*
* @author Ian Formanek
*/
public class FormEditorModule implements ModuleInstall {

  /** Module installed for the first time. */
  public void installed () {
  }

  /** Module installed again. */
  public void restored () {
    // 1. create palette
    // bla bla
  }

  /** Module was uninstalled. */
  public void uninstalled () {
  }

  /** Module is being closed. */
  public boolean closing () {
    return true; // agree to close
  }
}

/*
 * Log
 *  1    Gandalf   1.0         3/22/99  Ian Formanek    
 * $
 */
