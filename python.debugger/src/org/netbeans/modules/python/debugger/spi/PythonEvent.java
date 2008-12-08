/*
 * PythonEvent.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*/
package org.netbeans.modules.python.debugger.spi;

/**
 * Best effort for collecting Python Debug info for netbeans 
 * debugger
 * @author jean-yves
 */
public class PythonEvent 
{
  
  public final static int UNDEFINED = -1  ;
  public final static int START_SESSION = 0  ;
  public final static int STOP_SESSION  = 1  ;
  public final static int START_RUN  = 2  ;
  
  private int _evtType = UNDEFINED ;           
  private PythonSession _session ; 
  
  /** Creates a new instance of PythonEvent */
  public PythonEvent( int evtType , PythonSession session ) 
  { 
    _evtType = evtType  ; 
    _session = session  ; 
  }

  public boolean isJython()
  { return _session.isJython() ; }
  
  public PythonSession getSession() 
  { return _session ; }
  public int getEvtType() 
  { return _evtType ; }
  
  
}
