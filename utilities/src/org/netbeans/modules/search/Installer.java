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

package src_modules.com.netbeans.developer.modules.search;

import org.openide.modules.ModuleInstall;


/** Hooks on FindAction. 
* @author  Petr Kuzel
* @version 1.0
*/
public class Installer extends ModuleInstall {

  public void restored () {
    new SearchHook(new SearchPresenter()).hook();
  }
  
  public void uninstalled () {
    new SearchHook(null).hook();    
  }

}

/* 
* Log
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 
