/*
 * PersistenceManager.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*
*/

package org.netbeans.modules.python.debugger.breakpoints;

import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Breakpoint;
import java.util.ArrayList;
import java.beans.PropertyChangeEvent;
import java.util.Vector;


/**
 *
 * @author jean-yves
 */
public class PersistenceManager
implements LazyDebuggerManagerListener
{
  private final static String _PYTHON_ = "python" ; 
  private final static String _DEBUGGER_ = "debugger" ; 
  
  /** Creates a new instance of PersistenceManager */
  public PersistenceManager() {
  }
  
  public String[] getProperties () 
  {
    return new String [] 
    {
      DebuggerManager.PROP_BREAKPOINTS_INIT,
      DebuggerManager.PROP_BREAKPOINTS,
    };
  }
 
  public void breakpointRemoved (Breakpoint breakpoint) 
  {
    Properties p = Properties.getDefault ().getProperties (_DEBUGGER_).
    getProperties (DebuggerManager.PROP_BREAKPOINTS);
    p.setArray (
            _PYTHON_, 
            getBreakpoints ()
        );
    breakpoint.removePropertyChangeListener(this);
  }
  public void sessionAdded (Session session) {}
  public void sessionRemoved (Session session) {}
  public void engineAdded (DebuggerEngine engine) {}
  public void engineRemoved (DebuggerEngine engine) {}
  public void watchAdded (Watch watch) {}
  public void watchRemoved (Watch watch) {}
  public void initWatches () {}
 
  public void propertyChange (PropertyChangeEvent evt) 
  {
    if (evt.getSource() instanceof Breakpoint) 
    {
      Properties.getDefault ().getProperties (_DEBUGGER_).
                getProperties (DebuggerManager.PROP_BREAKPOINTS).setArray (
                    _PYTHON_,
                    getBreakpoints ()
                );
     }
  }
  
  public Breakpoint[] initBreakpoints () 
  {
  Properties p = Properties.getDefault ().getProperties (_DEBUGGER_).
                 getProperties (DebuggerManager.PROP_BREAKPOINTS);
  Breakpoint []  wkArray = (Breakpoint[]) p.getArray (
                            _PYTHON_, 
                            new Breakpoint [0] );
  // chase for null file or line breakpoints and remove them 
  // (Filezilla 150543
  Vector wk = new Vector()  ; 
    for  ( int ii=0 ; ii < wkArray.length ; ii++ )
    {
    PythonBreakpoint cur = (PythonBreakpoint) wkArray[ii] ;
      if (  cur.getLine() != null  )
        wk.add(cur) ; 
    }  

    return (Breakpoint[]) wk.toArray(new Breakpoint [0]) ;
  }

  public void breakpointAdded (Breakpoint breakpoint) 
  {
  Properties p = Properties.getDefault ().getProperties (_DEBUGGER_).
    getProperties (DebuggerManager.PROP_BREAKPOINTS);
    p.setArray (
            _PYTHON_, 
            getBreakpoints ()
        );
    breakpoint.addPropertyChangeListener(this);
  }

  private static Breakpoint[] getBreakpoints () 
  {
  Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints ();
  int i, k = bs.length;
  ArrayList bb = new ArrayList ();
    for (i = 0; i < k; i++)
      // Don't store hidden breakpoints
      if (bs[i] instanceof PythonBreakpoint)
        bb.add (bs [i]);
      bs = new Breakpoint [bb.size ()];
      return (Breakpoint[]) bb.toArray (bs);
  }

}
