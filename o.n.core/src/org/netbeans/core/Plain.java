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

package com.netbeans.developer.impl;

import java.io.File;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;

/** Default implementation of TopManager that is used when 
* the system is used without initialization.
*
* @author Jaroslav Tulach
*/
public class Plain extends NbTopManager implements Runnable {

  /** Creates new Plain. */
  public Plain() {
  }

  
  /** Creates defalt file system.
  */
  protected FileSystem createDefaultFileSystem () {
    LocalFileSystem fs = new LocalFileSystem ();
    try {
      String systemDir = System.getProperty("system.dir");
      if (systemDir != null) {
        File f = new File (systemDir);
        fs.setRootDirectory(f);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    return fs;
  }
  
  /** Prints the stack trace of an exception.
  */
  public void notifyException (Throwable t) {
    t.printStackTrace();
  }
  
  
  
  /** Initializaton of modules if user directory provided.
  */
  public void run() {
    String userDir = System.getProperty("modules.dir");
    if (userDir != null) {
      java.io.File f = new java.io.File (userDir);
      ModuleInstaller.initialize(f, f);

      // and autoload modules
      ModuleInstaller.autoLoadModules ();
    }
  }
}

/* 
* Log
*  2    Jaga      1.1         3/17/00  Jaroslav Tulach Compiles with 1.2 
*       compiler
*  1    Jaga      1.0         3/17/00  Jaroslav Tulach 
* $ 
*/ 
  