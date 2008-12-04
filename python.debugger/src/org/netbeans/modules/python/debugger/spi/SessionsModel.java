/*
 * SessionsModel.java
 *
 * Created on January 26, 2006, 11:30 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.python.debugger.spi;

import java.util.Enumeration;
import java.util.Vector;
import org.netbeans.modules.python.debugger.PythonDebugger;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * 
 * @author jean-yves
 */
public class SessionsModel
implements TableModelFilter 
{
  
  private Vector <ModelListener>        _listeners = new Vector<ModelListener> ();
  
  /** Creates a new instance of SessionsModel */
  public SessionsModel()
  {
	System.out.println("entering Sessions instance creation") ;
  }
  
  
  /** 
  * Registers given listener.
  * 
  * @param l the listener to add
  */
  public void addModelListener (ModelListener l) 
  {
    _listeners.add (l);
  }

  
  /** 
  * Unregisters given listener.
  *
  * @param l the listener to remove
  */
  public void removeModelListener ( ModelListener l) 
  {
    _listeners.remove (l);
  }

  /**
   * Used by Python debugger session to populate it's state back to 
   *session's view
  */
  public void populateNewSessionState( Object source )
  {
  Enumeration lList = _listeners.elements() ;
  ModelEvent evt = new ModelEvent.TableValueChanged(source,source,Constants.SESSION_STATE_COLUMN_ID) ;
    while( lList.hasMoreElements() )
	  ((ModelListener)lList.nextElement()).modelChanged(evt) ;
  
  }
  
  public Object getValueAt( TableModel original , Object node , String columnId )
  throws UnknownTypeException
  {
	if ( ! ( node instanceof  Session )  )
	  throw new UnknownTypeException(node) ;
	PythonDebugger pySession = PythonDebugger.map((Session)node) ; 
	//if ( columnId.equals( Constants.SESSION_STATE_COLUMN_ID  ) )
	//  return ( pySession.getDebuggerState(this) ) ;
	return (original.getValueAt(node,columnId)) ;
  }
  
  public boolean isReadOnly ( TableModel original , Object node , String columnId ) 
  throws UnknownTypeException
  { return original.isReadOnly(node,columnId) ; }
  
  public void setValueAt (TableModel original , Object node , String columnId , Object value ) 
  throws UnknownTypeException
  {
	original.setValueAt(node,columnId,value) ;
  } 
  
}
