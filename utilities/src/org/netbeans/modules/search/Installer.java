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

package com.netbeans.developer.modules.search;

import org.openide.modules.ModuleInstall;


/** 
* During restored() hooks SearchPresenter on FindAction. 
* During uninstalled() frees such hook.
*
* @author  Petr Kuzel
* @version 1.0
*/
public class Installer extends ModuleInstall {

  private SearchHook hook;
  
  public void restored () {
    SearchHook hook = new SearchHook(new SearchPresenter());
    hook.hook();   
  }
  
  public void uninstalled () {
    try {
      hook.unhook();
    } catch (Exception ex) {
      if (Boolean.getBoolean ("netbeans.debug.exceptions"))
        ex.printStackTrace ();         
    }
  }

}

/* 
* Log
*  3    Gandalf   1.2         12/15/99 Petr Kuzel      
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Minor enhancements
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 
