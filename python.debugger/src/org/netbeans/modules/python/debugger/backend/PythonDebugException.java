/**
* Copyright (C) 2003 Jean-Yves Mengant
*
*/


package org.netbeans.modules.python.debugger.backend;

import java.lang.Exception ;

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
 
