/*
 * PythonSession.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*
*/

package org.netbeans.modules.python.debugger.spi;

import java.io.File ;
import org.netbeans.modules.python.debugger.Debuggee;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;
/**
 * A Python debugging session instance
 * @author jean-yves
 */
public class PythonSession 
{
  
  private File _source ; 
  private JpyDbgView _dbgView ; 
  private String _args = null ;
  private String _scriptArgs = null ;
  private boolean _isRemote = false ; 
  private boolean _isJython = false ;
  private Debuggee _debuggee = null ;

  /** Creates a new instance of PythonSession */
  public PythonSession( Debuggee debuggee ,
                        boolean remote 
                      ) 
  {
    _debuggee = debuggee ;
    _source = _debuggee.getFile() ;
    _dbgView = _debuggee.getDebugView() ;
    _args = _debuggee.getCommandArgs() ;
    _scriptArgs = _debuggee.getScriptArgs() ;
    _isRemote = remote ; 
    _isJython = _debuggee.is_jython() ;
  }
  
  /**
  * Get a display name used for the session as a whole.
  * @return a user-presentable display name appropriate for session-scope messaging
  */
  public String getDisplayName() 
  { return _source.toString() ; }

  /**
   * Get the Python script originally invoked.
   * Note that due to cross modules calls some events may come from other scripts.
   * @return the Python script which was run to start with
   */
  public File getOriginatingScript() 
  { return _source ; }
  
  public String get_args()
  { return _args ; }

  public String get_scriptArgs()
  { return _scriptArgs ; }
  
  public  JpyDbgView get_dbgView()
  { return _dbgView ; }
  
  public boolean isRemote()
  { return _isRemote ; }
  
  public boolean isDebug()
  { return _isRemote ; }
  
  public boolean isJython()
  { return _isJython ; }

  public Debuggee getDebuggee()
  { return _debuggee ; }

}
