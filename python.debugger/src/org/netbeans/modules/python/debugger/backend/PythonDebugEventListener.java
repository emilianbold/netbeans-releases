
/**
* Copyright (C) 2003 Jean-Yves Mengant
*
*/


package org.netbeans.modules.python.debugger.backend;

/**
 * @author jean-yves
 *
 * subscriber of this interface will get populated with python event debugging
 * messages
 */
public interface PythonDebugEventListener
{
  public void newDebugEvent( PythonDebugEvent e ) ;
  
  public void launcherMessage( PythonDebugEvent e ) ;
}
 
