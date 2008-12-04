/*
 * PythonThreadInfos.java
 *
 * Created on February 5, 2006, 7:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.python.debugger.backend;

/**
 * @author jean-yves
 */
public class PythonThreadInfos
{

  /** Thread Name */  
  private String _name ;   
  private boolean _isCurrent = false ; 
  
  /**
   * Creates a new instance of PythonThreadInfos
   */
  public PythonThreadInfos( String name , boolean isCurrent)
  {
    _name = name ; 
    _isCurrent = isCurrent ;
  }
  
  public String get_name()
  { return _name ; }
  
  public boolean isCurrent()
  { return _isCurrent ;  }

}
