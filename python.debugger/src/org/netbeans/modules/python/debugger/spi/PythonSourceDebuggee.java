/*
 * PythonProjectCookie.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*
*/

package org.netbeans.modules.python.debugger.spi;

import java.io.File ;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;
import org.openide.filesystems.FileObject;

/**
 * Node specialization for Python Sources debugging context
 * @author jean-yves
 */
public interface PythonSourceDebuggee
{
  /** Get the disk file for the python script.
  * @return the disk file, or null if none (but must be a file object)
  */
  File getFile ();
  /** Get the file object for the build script.
   * @return the file object, or null if none (but must be a disk file)
   */
  FileObject getFileObject ();
  
  /**
   bind a debug view object
  */
  public void setDebugView( JpyDbgView view ) ; 
  public JpyDbgView getDebugView() ; 
  
  /**
    execute current python shell action
  */ 
  //public void executePython() 
  //throws PythonDebugException ;
  
  /** set current python session */
  public void setSession( PythonSession pythonSession ) ; 
  public PythonSession getSession() ; 
  
}
