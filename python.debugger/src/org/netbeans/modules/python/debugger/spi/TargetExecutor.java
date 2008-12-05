/*
 * TargetExecutor.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*
*/
package org.netbeans.modules.python.debugger.spi;

import org.openide.windows.InputOutput;
import org.openide.execution.ExecutorTask;
import org.openide.windows.IOProvider;
import java.io.IOException;
import org.openide.execution.ExecutionEngine;
import org.openide.util.RequestProcessor;
import java.io.OutputStream;
import org.netbeans.modules.python.debugger.actions.JpyDbgView ;
import org.netbeans.modules.python.debugger.DebuggerPythonLogger;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author jean-yves
 */
public class TargetExecutor 
implements Runnable 
{
  private PythonSourceDebuggee _pcookie;
  private InputOutput _io = null ;
  /** used for the tab etc. */
  private String _displayName;
  private boolean _ok = false;
  private OutputStream _outputStream;
  private JpyDbgView _dbgView ;
 
  /** targets may be null to indicate default target */
  public TargetExecutor ( PythonSourceDebuggee pcookie )
  {
    _pcookie = pcookie;  
    _dbgView = pcookie.getDebugView() ;
  }
 
  
  private static class _WRAPPERRUNNABLE_ implements Runnable 
  {
    private final ExecutorTask _task;
    public _WRAPPERRUNNABLE_(ExecutorTask task) 
    {
      _task = task;
    }
    public void run () 
    {
      _task.waitFinished ();
    }
  }
  
  private class _TASKTERMINATIONHANDLER_ 
  implements TaskListener
  {
    public void taskFinished( Task task ) 
    {
      System.out.println("Entering task termination") ;    
      // proceed with debug termination
      // TODO analyze debug termination actions
      if ( _dbgView != null)
        _dbgView.terminateSession(new PythonEvent( PythonEvent.STOP_SESSION, _pcookie.getSession() )) ;
    }
  }
  
  private class _WRAPPEREXECUTORTASK_ extends ExecutorTask 
  {
    private ExecutorTask _task;
    private InputOutput _io;
    public _WRAPPEREXECUTORTASK_(ExecutorTask task, InputOutput io) 
    {
      super(new _WRAPPERRUNNABLE_(task));
      _task = task;
      _io = io;
    }
    public void stop () 
    {
      _task.stop ();
    }
    public int result () 
    {
      return _task.result () + (_ok ? 0 : 1);
    }
    
    public InputOutput getInputOutput () 
    {
      return _io;
    }
  }
  
  
  
  public ExecutorTask execute () throws IOException 
  {
  String fileName;
    // System.out.println("entering targetexecutor execute") ;
    if (_pcookie.getFileObject() != null) 
    {
      fileName = _pcookie.getFileObject().getNameExt();
    } else 
    {
      fileName = _pcookie.getFile().getName();
    }
    _displayName = fileName + "(Debugging)" ; 
    
    if ( _io == null )
      if ( _dbgView == null )
        _io = IOProvider.getDefault().getIO(_displayName, true); 
  
    final ExecutorTask task = ExecutionEngine.getDefault().execute(_displayName, this, InputOutput.NULL);
    // capture termination request event
    task.addTaskListener( new _TASKTERMINATIONHANDLER_() ) ;
    _WRAPPEREXECUTORTASK_ wrapper = new _WRAPPEREXECUTORTASK_(task, _io);

    RequestProcessor.getDefault().post(wrapper);
    // System.out.println("leaving targetexecutor execute") ;
    return wrapper;
  
  }
  
  public ExecutorTask execute(OutputStream outputStream) 
  throws IOException 
  {
    _outputStream = outputStream;
    ExecutorTask task = ExecutionEngine.getDefault().execute(
          "LABEL_execution_name", this, InputOutput.NULL);
    return new _WRAPPEREXECUTORTASK_(task, null);
  }

  
  synchronized public void run () 
  {
    System.out.println("entering targetexecutor THREAD") ;

    PythonSession session = _pcookie.getSession() ; 

    DebuggerPythonLogger dbgTask = DebuggerPythonLogger.getDefault();
    dbgTask.debugFile( session.getOriginatingScript() );

    // Start debugging session
    dbgTask.taskStarted(
                        new PythonEvent( PythonEvent.START_SESSION,
                                         session
                                       )
                       );
    _dbgView.set_logger( dbgTask );
    
    System.out.println("leaving targetexecutor THREAD") ;
  }
  
}
