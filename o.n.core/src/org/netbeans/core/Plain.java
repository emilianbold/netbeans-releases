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
import java.io.IOException;
import java.net.URL;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;

import org.netbeans.core.modules.ModuleSystem;
import org.netbeans.core.projects.ModuleLayeredFileSystem;
import org.netbeans.core.xml.NbSAXParserFactoryImpl;
import org.netbeans.core.xml.XML;

/** Default implementation of TopManager that is used when 
* the system is used without initialization.
*
* @author Jaroslav Tulach
*/
public class Plain extends NbTopManager implements Runnable {
    
    static {
        NbSAXParserFactoryImpl.install();
    }

  /** Creates new Plain. */
  public Plain() {
  }
  
    private ModuleSystem moduleSystem;
  
    /** Creates defalt file system.
    */
    protected FileSystem createDefaultFileSystem () {
        String systemDir = System.getProperty("system.dir"); // NOI18N

        try {
            File f = systemDir == null ? null : new File (systemDir);
            return org.netbeans.core.projects.SessionManager.getDefault().create(f, f);
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

    /** Creates error logger, just plain that prints to System.err
     */
    protected java.io.PrintWriter createErrorLogger (int minLogSeverity) {
        return new java.io.PrintWriter (System.err);
    }
    
  /** Prints the stack trace of an exception.
  */
  public void notifyException (Throwable t) {
    t.printStackTrace();
  }

  /** */
  public Object notify (org.openide.NotifyDescriptor nd) {
    new Exception("TopManager.notify()").printStackTrace(); // NOI18N
    System.out.println("MSG = " + nd.getMessage()); // NOI18N
    Object[] options = nd.getOptions();
    System.out.print("("); // NOI18N
    for(int i = 0; i < options.length; i++) {
      if (i != 0) System.out.print(", "); // NOI18N
      System.out.print(options[i]);
    }
    System.out.println(")"); // NOI18N
    return new Object();
  }
  
  /** */
  public void setStatusText(String text) {
    System.out.println(text);
  }
  
    /** Initializaton of modules if user directory provided.
     */
    public void run() {
        XML.init();
        String userDir = System.getProperty("modules.dir"); // NOI18N
        FileSystem fs = getRepository().getDefaultFileSystem();
        try {
            moduleSystem = new ModuleSystem(fs, userDir == null ? null : new File(userDir), null);
        } catch (IOException ioe) {
            notifyException(ioe);
            return;
        }
        fireSystemClassLoaderChange();
        moduleSystem.loadBootModules();
        if (! fs.isReadOnly()) {
            moduleSystem.readList();
            moduleSystem.scanForNewAndRestore();
            LoaderPoolNode.installationFinished();
            moduleSystem.installNew();
        } else {
            LoaderPoolNode.installationFinished();
        }
    }
  
    /** Get the module subsystem.  */
    public ModuleSystem getModuleSystem() {
        return moduleSystem;
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
