/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.io.File;
import java.net.URL;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;

import org.netbeans.core.projects.ModuleLayeredFileSystem;

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
        String systemDir = System.getProperty("system.dir");

        try {
            File f = systemDir == null ? null : new File (systemDir);
            return ModuleLayeredFileSystem.create (f, f);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
            throw new InternalError ();
        } catch (java.beans.PropertyVetoException ex) {
            ex.printStackTrace();
            throw new InternalError ();
        }
    }


  /** Test method to check whether some level of interactivity is enabled.
  * @param il mask composed of the constants of IL_XXXX
  * @return true if such level is enabled
  */
  public boolean isInteractive (int il) {
    return (IL_WINDOWS & il) == IL_WINDOWS;
  }

  /** Prints the stack trace of an exception.
  */
  public void notifyException (Throwable t) {
    t.printStackTrace();
  }

  /** */
  public Object notify (org.openide.NotifyDescriptor nd) {
    new Exception("TopManager.notify()").printStackTrace();
    System.out.println("MSG = " + nd.getMessage());
    Object[] options = nd.getOptions();
    System.out.print("(");
    for(int i = 0; i < options.length; i++) {
      if (i != 0) System.out.print(", ");
      System.out.print(options[i]);
    }
    System.out.println(")");
    return new Object();
  }
  
  /** */
  public void setStatusText(String text) {
    System.out.println(text);
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
    } else {
        ModuleInstaller.initialize (null, null);
    }
  }
  
/* JST: not needed anymore.
  static class ClassLoaderFinder extends SecurityManager implements org.openide.util.NbBundle.ClassLoaderFinder {

    public ClassLoader find() {
      Class[] classes = getClassContext();
      return classes[Math.min(4, classes.length - 1)].getClassLoader();
    }
  }
*/

}
