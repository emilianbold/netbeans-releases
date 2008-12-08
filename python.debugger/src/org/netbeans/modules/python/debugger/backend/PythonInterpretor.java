/**
 * Copyright (C) 2003,2004 Jean-Yves Mengant
 *
 */
package org.netbeans.modules.python.debugger.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import org.netbeans.modules.python.debugger.utils.ExecTerminationEvent;
import org.netbeans.modules.python.debugger.utils.ExecTerminationListener;
import org.netbeans.modules.python.debugger.utils.ProcessLauncher;
import org.netbeans.modules.python.debugger.utils.UtilsError;

/**
 * @author jean-yves
 *
 * implement a local python interpretor launcher utility
 *
 */
public class PythonInterpretor
        extends Thread
        implements ExecTerminationListener
{

  private ProcessLauncher _launcher = new ProcessLauncher();
  private Vector _command = null;
  private PythonDebugEventListener _listener = null;

  public synchronized void addPythonDebugEventListener(PythonDebugEventListener l)
  {
    _listener = l;
  }

  public synchronized void removePythonDebugEventListener()
  {
    _listener = null;
  }

  private void populateInterpretorEvent(int type, String msg)
  {
    if (_listener != null)
    {
      PythonDebugEvent evt = new PythonDebugEvent(type, msg);
      _listener.launcherMessage(evt);
    }
  }

  public void setEnv(String name, String value)
  {
    _launcher.setEnv(name, value);
  }

  public Process getProcess()
  {
    return _launcher.getProcess();
  }

  public PythonInterpretor(String pgm, Vector args)
  {
    // For both CPYTHON an JYTHON we only need the shell execution path location
    _command = args;
    _command.insertElementAt(pgm, 0);
  }

  class _DBG_STREAM_READER_
          extends Thread
  {

    private int _type;
    private InputStream _istream;
    private StringBuffer _resultBuffer;

    public _DBG_STREAM_READER_(int type, InputStream istream)
    {
      _type = type;
      _istream = istream;
      _resultBuffer = new StringBuffer();
    }

    public void run()
    {
      int c;

      try
      {
        while ((_istream != null) && (c = _istream.read()) != -1)
        {
          if (c == '\n')
          {
            populateInterpretorEvent(_type, _resultBuffer.toString());
            _resultBuffer = new StringBuffer();
          } else
          {
            _resultBuffer.append((char) c);
          }

        }
        if ( _istream != null )
          _istream.close();
      } catch (IOException e)
      {
        populateInterpretorEvent(PythonDebugEvent.LAUNCHER_ERR, e.getMessage());
      }
    }
  }

  /**
   * Daemon ending
   */
  public void processHasEnded(ExecTerminationEvent evt)
  {
    int code = evt.get_code();
    String retCode = Integer.toString(code);
    populateInterpretorEvent(PythonDebugEvent.LAUNCHER_ENDING, retCode);
  }

  public void doTheJob()
  {
    try
    {
      _launcher.setCommand(_command);
      _launcher.setExecTerminationListener(this);
      // run debugger through ProcessBuilder thread
      _launcher.go() ;
      _DBG_STREAM_READER_ stdoutReader = new _DBG_STREAM_READER_(PythonDebugEvent.LAUNCHER_MSG, _launcher.getStdout());
      _DBG_STREAM_READER_ stderrReader = new _DBG_STREAM_READER_(PythonDebugEvent.LAUNCHER_MSG, _launcher.getStderr());
      stdoutReader.start();
      stderrReader.start();
      _launcher.waitForCompletion();
    } catch (UtilsError e)
    {
      populateInterpretorEvent(PythonDebugEvent.LAUNCHER_ERR, e.getMessage());
    }
  }

  @Override
  public void run()
  {
    doTheJob();
  }

  public static void main(String[] args)
  {
  }
}
