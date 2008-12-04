/**
* Copyright (C) 2003 Jean-Yves Mengant
*
*/


package org.netbeans.modules.python.debugger.backend;


/**
 * @author jean-yves
 *
 * used by pluggin implementors interfaces to get populated
 * With source debugging level instance 
 *   
 */
public interface PluginEventListener
{
  /**
   * populate debugging event to the UI plugin interface for
   * sources synchronization process
   * @param e
   * @throws PythonDebugException
   */	
  public void newDebuggingEvent( PluginEvent e )
  throws PythonDebugException ; 
  
}
