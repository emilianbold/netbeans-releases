/*
* Plain.java -- synopsis.
*
*
* $Date$
* $Revision$
*
* SUN PROPRIETARY/CONFIDENTIAL:  INTERNAL USE ONLY.
*
* Copyright Š 1997-1999 Sun Microsystems, Inc. All rights reserved.
* Use is subject to license terms.
*/
 
package org.netbeans.core;

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
    
    // set proper versioning
    
    java.util.Properties versions = new java.util.Properties ();
    try {
      versions.load (Main.class.getClassLoader ().getResourceAsStream ("org/netbeans/core/Versioning.properties")); // NOI18N
    } catch (java.io.IOException ioe) {
      ioe.printStackTrace ();
    }
    System.setProperty ("org.openide.specification.version", versions.getProperty ("VERS_Specification_Version")); // NOI18N
    System.setProperty ("org.openide.version", versions.getProperty ("VERS_Implementation_Version")); // NOI18N
    System.setProperty ("org.openide.major.version", versions.getProperty ("VERS_Name")); // NOI18N

    // set NbBundle recognizer
    org.openide.util.NbBundle.setClassLoaderFinder(new ClassLoaderFinder());
    
    
    String userDir = System.getProperty("modules.dir");
    if (userDir != null) {
      java.io.File f = new java.io.File (userDir);
      ModuleInstaller.initialize(f, f);

      // and autoload modules
      ModuleInstaller.autoLoadModules ();
    }
  }
  
  static class ClassLoaderFinder extends SecurityManager implements org.openide.util.NbBundle.ClassLoaderFinder {

    public ClassLoader find() {
      Class[] classes = getClassContext();
      return classes[Math.min(4, classes.length - 1)].getClassLoader();
    }
  }
}

/* 
* $Log: 
*  3    Jaga      1.2         03/24/00 Martin Ryzl     implemented
*       setStatusText(), notify(), added code for setting of version,
*       initialization of NbBundle.loaderFinder
*  2    Jaga      1.1         03/17/00 Jaroslav Tulach Compiles with 1.2 compiler
*  1    Jaga      1.0         03/17/00 Jaroslav Tulach 
* $ 
*/ 
  