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

package com.netbeans.developer.modules.loaders.form;

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
//    System.out.println("FormEditorModule: installed");
    addBeanInfos ();
    // 1. create palette
    // bla bla
  }

  /** Module installed again. */
  public void restored () {
//    System.out.println("FormEditorModule: restored");
    addBeanInfos ();
  }

  private void addBeanInfos () {
    // [PENDING] IAN - Highly temporary solution
    IDESettings is = new IDESettings ();
    String[] bisp = is.getBeanInfoSearchPath ();
    String[] bisp2 = new String[bisp.length+1];
    System.arraycopy (bisp2, 0, bisp, 0, bisp.length);
    bisp2 [bisp2.length-1] = "com.netbeans.developer.modules.beaninfo.awt";
    is.setBeanInfoSearchPath (bisp2);
  }

  /** Module was uninstalled. */
  public void uninstalled () {
    // [PENDING - ask and delete ComponentPalette]
  }

  /** Module is being closed. */
  public boolean closing () {
    return true; // agree to close
  }
  
}

/*
 * Log
 *  5    Gandalf   1.4         3/30/99  Ian Formanek    
 *  4    Gandalf   1.3         3/27/99  Ian Formanek    
 *  3    Gandalf   1.2         3/26/99  Ian Formanek    
 *  2    Gandalf   1.1         3/22/99  Ian Formanek    
 *  1    Gandalf   1.0         3/22/99  Ian Formanek    
 * $
 */
