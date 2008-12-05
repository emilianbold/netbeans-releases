/*
* PythonBreakpointActionProvider.java
*
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
*/

package org.netbeans.modules.python.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import java.util.Set;
import java.util.Collections;
import org.netbeans.modules.python.debugger.Utils;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;
import org.netbeans.api.debugger.ActionsManager;
import org.openide.text.Line;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Breakpoint;
import org.openide.windows.TopComponent;
import org.openide.util.WeakListeners;




/**
 *
 * @author jean-yves
 */
public class PythonBreakpointActionProvider 
extends ActionsProviderSupport 
implements PropertyChangeListener
{
  private final static Set _ACTIONS_ = Collections.singleton 
    (
        ActionsManager.ACTION_TOGGLE_BREAKPOINT
    );
  
  /**
     * Creates a new instance of PythonBreakpointActionProvider
     */
  public PythonBreakpointActionProvider() 
  {
    setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
    TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(this, TopComponent.getRegistry()));
  }
  
  private void removeFromJpyDbg( PythonBreakpoint bp )
  {
    if ( JpyDbgView.get_debuggerView() != null )
      // if JpyDbgView is active populate info to debugger immediatly
      JpyDbgView.get_debuggerView().clearBreakPoint(bp) ; 
  }
  
  private void addToJpyDbg( PythonBreakpoint bp )
  {
    if ( JpyDbgView.get_debuggerView() != null )
      // if JpyDbgView is active populate info to debugger immediatly
      JpyDbgView.get_debuggerView().setBreakPoint(bp) ; 
    
  }
  
  /**
  * Called when the action is called (action button is pressed).
  *
  * @param action an action which has been called
  */
  public void doAction (Object action) 
  {
    Line line = Utils.getCurrentLine ();
    
    Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints ();
      int i, k = breakpoints.length;
      
      for (i = 0; i < k; i++)
      {	
        if ( breakpoints [i] instanceof PythonBreakpoint &&
             ( ((PythonBreakpoint) breakpoints [i]).getLine () != null ) &&   
             ( (PythonBreakpoint) breakpoints [i]).getLine ().equals (line)
           ) 
	{
	  // assume breakpoint removal
	  removeFromJpyDbg((PythonBreakpoint) breakpoints[i]) ;
          DebuggerManager.getDebuggerManager ().removeBreakpoint(breakpoints [i]);
          break;
        }
      }	
      if (i == k)
      {  
	// not there => assume breakpoint add 
	PythonBreakpoint bp = new PythonBreakpoint (line) ;  
	addToJpyDbg(bp) ;
        DebuggerManager.getDebuggerManager().addBreakpoint (bp) ;
      }  
    }
    
    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    public Set getActions () {
        return _ACTIONS_ ;
    }
  
    public void propertyChange(PropertyChangeEvent evt) 
    {
        boolean enabled = Utils.getCurrentLine() != null;
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
    }
  
}
