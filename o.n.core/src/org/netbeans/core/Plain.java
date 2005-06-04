/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.io.File;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;

import org.netbeans.core.startup.ModuleSystem;
import org.openide.awt.StatusDisplayer;

/** Default implementation of TopManager that is used when 
* the system is used without initialization.
*
* @author Jaroslav Tulach
*/
public class Plain extends NbTopManager implements Runnable, ChangeListener {
    
    private final StatusDisplayer status;

    /** Creates new Plain. */
    public Plain() {
        if (Boolean.getBoolean("org.netbeans.core.Plain.CULPRIT")) Thread.dumpStack(); // NOI18N
        status = StatusDisplayer.getDefault();
        status.addChangeListener(this);
    }
  
    private ModuleSystem moduleSystem;
  

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
  
    /** Create the module system. Subclasses may override. */
    protected ModuleSystem createModuleSystem() throws IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        return new ModuleSystem(fs);
    }
  
    /** Initializaton of modules if user directory provided.
     */
    public void run() {
        try {
            moduleSystem = createModuleSystem();
        } catch (IOException ioe) {
            notifyException(ioe);
            return;
        }
        moduleSystem.loadBootModules();
        if (!Repository.getDefault().getDefaultFileSystem().isReadOnly()) {
            moduleSystem.readList();
            moduleSystem.restore();
            LoaderPoolNode.installationFinished();
        } else {
            LoaderPoolNode.installationFinished();
        }
    }
  
    /** Get the module subsystem.  */
    public ModuleSystem getModuleSystem() {
        return moduleSystem;
    }
  
    public void stateChanged(ChangeEvent e) {
        System.out.println(status.getStatusText());
    }
    
}
