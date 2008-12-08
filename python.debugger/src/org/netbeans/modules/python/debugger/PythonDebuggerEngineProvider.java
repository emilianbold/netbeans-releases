/*
* PythonDebuggerEngineProvider.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*
*/

package org.netbeans.modules.python.debugger;

import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.api.debugger.DebuggerEngine;

/**
 *
 * @author jean-yves
 */
public class PythonDebuggerEngineProvider
extends DebuggerEngineProvider 
{
  private DebuggerEngine.Destructor _destructor;

  public PythonDebuggerEngineProvider()
  {
    System.out.println("entering PythonDebuggerEngineProvider") ; 
  }
  
  
  public String[] getLanguages () 
  {
    return new String[] { "python" , "jython" };
  }

  public String getEngineTypeID () 
  {
    // System.out.println("returning back PythonDebuggerEngine") ;
    return "PythonDebuggerEngine";
  }
  
  public Object[] getServices () 
  {
    return new Object[] {};
  }
    
  public void setDestructor (DebuggerEngine.Destructor destructor) 
  {
    _destructor = destructor;
  }
    
  public DebuggerEngine.Destructor getDestructor () 
  {
    return _destructor;
  }
}
