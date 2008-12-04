/*
 * StackInfo.java
 *
 * Created on February 19, 2006, 5:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.python.debugger.backend;

/**
 *
 * @author jean-yves
 */
public class StackInfo 
{
    
  private String _name ; 
  private boolean _current ; 
    
      
  /** Creates a new instance of StackInfo */
  public StackInfo( String name , boolean isCurrent ) 
  {
    _name = name ; 
    _current = isCurrent ; 
  }
    
  public String get_name()
  { return _name ;  }
  
  public boolean is_current()
  { return _current ; }
  
}
