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

import com.netbeans.developer.impl.IDESettings;
import com.netbeans.ide.modules.ModuleInstall;

/**
* Module installation class for Form Editor
*
* @author Ian Formanek
*/
public class FormEditorModule implements ModuleInstall {



  /** Module installed for the first time. */
  public void installed () {
    System.out.println("FormEditorModule.java:32");
    IDESettings is = new IDESettings ();
    String[] bisp = is.getBeanInfoSearchPath ();
    String[] bisp2 = new String[bisp.length+1];
    System.arraycopy (bisp2, 0, bisp, 0, bisp.length);
    bisp2 [bisp2.length-1] = "com.netbeans.developer.modules.beaninfo.awt";
    is.setBeanInfoSearchPath (bisp2);
  }

  /** Module installed again. */
  public void restored () {
    System.out.println("FormEditorModule.java:43");

    IDESettings is = new IDESettings ();
    String[] bisp = is.getBeanInfoSearchPath ();
    String[] bisp2 = new String[bisp.length+1];
    System.arraycopy (bisp2, 0, bisp, 0, bisp.length);
    bisp2 [bisp2.length-1] = "com.netbeans.developer.modules.beaninfo.awt";
    is.setBeanInfoSearchPath (bisp2);

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
 *  3    Gandalf   1.2         3/26/99  Ian Formanek    
 *  2    Gandalf   1.1         3/22/99  Ian Formanek    
 *  1    Gandalf   1.0         3/22/99  Ian Formanek    
 * $
 */
