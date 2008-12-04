/*
 * BreakpointAnnotationListener.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*
*/

package org.netbeans.modules.python.debugger.breakpoints;

import org.netbeans.api.debugger.DebuggerManagerAdapter;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Breakpoint;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.python.debugger.DebuggerAnnotation;


/**
 * Netbeans breakpoint semantics
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS} 
 * property and annotates JPDA Debugger line breakpoints in NetBeans editor.
 * @author jean-yves
 */
public class BreakpointAnnotationListener  
extends DebuggerManagerAdapter 
implements PropertyChangeListener 
{
  
  private Map _breakpointToAnnotation = new HashMap ();

  /** Creates a new instance of BreakpointAnnotationListener */
  public BreakpointAnnotationListener() 
  {}
  
  public String[] getProperties () 
  {
    return new String[] {DebuggerManager.PROP_BREAKPOINTS};
  }

  /**
  * Called when some breakpoint is added.
  *
  * @param b breakpoint
  */
  public void breakpointAdded (Breakpoint b) 
  {
    if (! (b instanceof PythonBreakpoint)) return;
      addAnnotation (b);
  }

  /**
  * Called when some breakpoint is removed.
  *
  * @param breakpoint
  */
  public void breakpointRemoved (Breakpoint b) 
  {
    if (! (b instanceof PythonBreakpoint)) return;
      removeAnnotation (b);
  }

  /**
   * This method gets called when a bound property is changed.
   * @param evt A PropertyChangeEvent object describing the event source 
   *   	and the property that has changed.
   */

  public void propertyChange (PropertyChangeEvent evt) 
  {
    if (evt.getPropertyName () != Breakpoint.PROP_ENABLED) return;
    removeAnnotation ((Breakpoint) evt.getSource ());
    addAnnotation ((Breakpoint) evt.getSource ());
  }
    
  private void addAnnotation (Breakpoint b) 
  {
    _breakpointToAnnotation.put (
            b,
            new DebuggerAnnotation (
                b.isEnabled () ? 
                    DebuggerAnnotation.BREAKPOINT_ANNOTATION_TYPE :
                    DebuggerAnnotation.DISABLED_BREAKPOINT_ANNOTATION_TYPE, 
                ((PythonBreakpoint) b).getLine ()
            )
     );
     b.addPropertyChangeListener (
       Breakpoint.PROP_ENABLED, 
       this
     );
  }
    
  private void removeAnnotation (Breakpoint b) 
  {
  DebuggerAnnotation annotation = (DebuggerAnnotation) _breakpointToAnnotation.remove (b);
        if (annotation == null) return;
        annotation.detach ();
        b.removePropertyChangeListener (
            Breakpoint.PROP_ENABLED, 
            this
        );
    }
}
