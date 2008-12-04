/*
 * DebuggerPythonLogger.java
 *
 * Copyright (C) 2003,2004,2005 Jean-Yves Mengant
 *
 *
 */
package org.netbeans.modules.python.debugger;


import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.spi.debugger.SessionProvider;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


import org.openide.util.Lookup;

import java.io.File;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.python.debugger.spi.PythonEvent;
import org.netbeans.modules.python.debugger.spi.PythonSession;
import org.netbeans.modules.python.debugger.spi.PythonSourceDebuggee;


/**
 * Debugger's tasking entry point class
 *
 * @author jean-yves
 */
public class DebuggerPythonLogger
{
  private final static String _PYTHON_DEBUGGER_INFO_ = "PythonDebuggerInfo";

  /**
   * used by the Netbeans META-INF tree to identify the language type session
   * directory
   */
  private final static String _PYTHONSESSION_ = "PythonSession";

  /** PythonSession => PythonDebugger */
  private Map<PythonSession , PythonDebugger>  _runningDebuggers = new HashMap<PythonSession , PythonDebugger>();

  /** PythonDebugger => PythonSession */
  private Map<PythonDebugger , PythonSession> _runningDebuggers2 = new HashMap<PythonDebugger , PythonSession>();
  private Set<File> _filesToDebug      = new HashSet<File>();

  /**
   * Creates a new instance of DebuggerPythonLogger
   */
  public DebuggerPythonLogger()
  {
    System.out.println( "DebuggerPythonLogger loaded" );
  }

  /**
   * get debugger's instance
   *
   * @return Python debuggers global instance
   */
  public static DebuggerPythonLogger getDefault()
  {
    Iterator it =
      Lookup.getDefault().lookup(
                                 new Lookup.Template( DebuggerPythonLogger.class )
                              ).allInstances().iterator();
    while (it.hasNext())
    {
      DebuggerPythonLogger al = (DebuggerPythonLogger) it.next();
      if (al instanceof DebuggerPythonLogger)
      {
        return  al;
      }
    }
    throw new InternalError();
  }


  /**
   * Fired when a task is started. It is <em>not</em> guaranteed that {@link
   * AntEvent#getTaskName} or {@link AntEvent#getTaskStructure} will be
   * non-null, though they will usually be defined. {@link
   * AntEvent#getTargetName} might also be null. The default implementation does
   * nothing.
   *
   * @param event the associated event object
   */
  public void taskStarted( PythonEvent event )
  {
    // System.out.println( "entering DebuggerPythonLogger task started" );

    PythonDebugger d = getDebugger(
                                   event.getSession(),
                                   event
                                );
    if (d == null)
      return;
    d.taskStarted( event );
    // System.out.println( "leaving DebuggerPythonLogger task started" );
  }


  /**
   * setting debug candidate file
   *
   * @param f debug candidate
   */
  public void debugFile( File f )
  {
    _filesToDebug.add( f );
  }


  private PythonDebugger getDebugger( PythonSession s, PythonEvent pyEvent )
  {
    PythonDebugger d =  _runningDebuggers.get( s );
    if (d != null)
      return d;

    if ( s.getOriginatingScript() == null )
      return null ; 
    if (!_filesToDebug.contains( s.getOriginatingScript() ))
      return null;
    _filesToDebug.remove( s.getOriginatingScript() );

    // start debugging othervise
    FileObject          fo       =
      FileUtil.toFileObject( s.getOriginatingScript() );



    PythonSourceDebuggee pyDebuggee = Debuggee.getDebuggee(fo) ;
    if (pyDebuggee == null)
      throw new NullPointerException();
    d = startDebugging( pyDebuggee, pyEvent );
    _runningDebuggers.put( s, d );
    _runningDebuggers2.put( d, s );

    return d;
  }


  private static PythonDebugger startDebugging(
                                               final PythonSourceDebuggee pyCookie,
                                               final PythonEvent         pyEvent
                                            )
  {
    DebuggerInfo     di =
      DebuggerInfo.create(
                          _PYTHON_DEBUGGER_INFO_,
                          new Object[]
                          {
                            new SessionProvider()
      {
        public String getSessionName()
        {
          // System.out.println( "Debugger Session Name is :" + pyEvent.getSession().getDisplayName() );

          return pyEvent.getSession().getDisplayName();
        }


        public String getLocationName()
        {
          return "localhost";
        }


        public String getTypeID()
        {
          return _PYTHONSESSION_;
        }


        public Object[] getServices()
        {
          return new Object[] {};
        }
      }
      , pyCookie
                          }
                       );
    DebuggerManager  dm = DebuggerManager.getDebuggerManager();
    DebuggerEngine[] es = dm.startDebugging( di );

    return  es[0].lookupFirst( null, PythonDebugger.class );
  }


  private void finishDebugging( PythonDebugger debugger )
  {
    PythonSession session = _runningDebuggers2.remove( debugger );
    _runningDebuggers.remove( session );
  }


  /**
   * Fired once when a build is finished. The default implementation does
   * nothing.
   *
   * @param event the associated event object
   *
   * @see   AntEvent#getException
   */
  public void pythonFinished( PythonEvent event )
  {
    // System.out.println( "entering pythonFinished" );

    PythonDebugger d = getDebugger(
                                   event.getSession(),
                                   event
                                );
    if (d == null)
      return;
    d.pythonFinished( event );
    finishDebugging( d );
  }


}
