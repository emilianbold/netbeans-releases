
package org.netbeans.modules.python.debugger;


/**
 * @author jean-yves
 *
 * PyDebugException : exception thrown by PyDebug environment
 */

public class PythonDebugException
extends Exception 
{
  public PythonDebugException()
  {}

  // constructor with String input message
  public PythonDebugException( String Msg  )
  { super(Msg) ; }
}
 
