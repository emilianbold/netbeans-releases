

package org.netbeans.modules.python.debugger.gui;

import java.util.EventObject;
import javax.swing.* ;

/**
 * @author jean-yves
 *
 *  Techprint debugging event 
 */
public class DebugEvent 
extends EventObject 
{
  public final static String STOP =  "shutdown Python environment" ; 
  public final static String START =  "startup local debugging session" ; 
  public final static String REMOTESTART =  "startup remote debugging session" ; 
  public final static String STEPOVER = "Debug statement step Over"  ; 
  public final static String STEPINTO = "Debug statement step Into" ; 
  public final static String RUN = "Run" ; 
  public final static String SENDCOMMAND = "execute Python Command"  ; 
  public final static String COMMANDFIELD = "Python command field"   ; 
  public final static String TOGGLEJYTHON = "Jython / CPython language switch"   ; 
  public final static String PGMARGS = "Add python programs arguments to args table"   ; 
    
  private String _moduleName ; 
  private Action _action ; 
  private AbstractButton _guiButton ;   
    
  public DebugEvent( Object source , 
                     String moduleName , 
                     Action action , 
                     AbstractButton gui
                   )
  {
    super(source) ;     
    _action = action ; 
    _guiButton = gui ; 
  }

  public String get_moduleName()
  { return _moduleName ; }
  
  public Action get_action()
  { return _action ; }
  
  public AbstractButton get_guiButton()
  { return _guiButton ; }

}
