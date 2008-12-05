

package org.netbeans.modules.python.debugger.gui;

import org.netbeans.modules.python.debugger.CompositeCallback;


/**
 * implement a compatible interface for netbeans / jedit python shell 
 * containers
 * @author jean-yves
 */
public interface PythonContainer 
{
   public void inspectCompositeCommand( CompositeCallback callBack , String varName ) ;
  
   public void dbgVariableChanged( String name , String value , boolean global ) ;

}
