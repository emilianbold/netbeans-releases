/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.impl;

import java.util.*;
import javax.swing.event.*;

import com.netbeans.ide.actions.ToolsAction;
import com.netbeans.ide.modules.ManifestSection;
import com.netbeans.ide.util.actions.SystemAction;

/** Holds list of all actions added by modules.
*
* @author jtulach
*/
class ModuleActions extends Object implements ToolsAction.Model {
  /** array of all actions added by modules */
  private static SystemAction[] array;
  /** map of (ManifestSection.ActionSection, List (SystemAction)) */
  private static HashMap map = new HashMap (3);
  
  /** listeners */
  private static EventListenerList listeners = new EventListenerList ();

  /** instance */
  private static ModuleActions INSTANCE = new ModuleActions ();  
  
  /** Initializes the model.
  */
  public static void initialize () {
    ToolsAction.setModel (INSTANCE);
  }

  /** Array with all activated actions.
  * Can contain null that will be replaced by separators.
  */
  public SystemAction[] getActions () {
    SystemAction[] a = array;
    if (a != null) {
      return a;
    }
    array = a = createActions ();
    return a;
  }
    
  /** Adds change listener to listen on changes of actions
  */
  public void addChangeListener (ChangeListener l) {
    listeners.add (ChangeListener.class, l);
  }
    
  /** Removes change listener to listen on changes of actions
  */
  public void removeChangeListener (javax.swing.event.ChangeListener l) {
    listeners.remove (ChangeListener.class, l);
  }
  
  /** Listens on change of modules and if changed,
  * fires change to all listeners.
  */
  private static void fireChange () {
    if (array != null) {
      // no change
      return;
    }
    
    Object[] obj = listeners.getListenerList ();
    if (obj.length == 0) return;
    
    ChangeEvent ev = new ChangeEvent (INSTANCE);
    for (int i = obj.length - 1; i >= 0; i -= 2) {
      ChangeListener l = (ChangeListener)obj[i];
      l.stateChanged (ev);
    }
  }

  /** Adds new action to the list.
  */
  public synchronized static void add (ManifestSection.ActionSection as) throws InstantiationException {
    List list = (List)map.get (as);
    if (list == null) {
      list = new LinkedList ();
      map.put (as, list);
    }
    list.add (as.getAction ());
    
    array = null;
    fireChange (); // PENDING this is too often
  }
  
  /** Removes new action to the list.
  */
  public synchronized static void remove (ManifestSection.ActionSection as) throws InstantiationException {
    List list = (List)map.get (as);
    if (list == null) {
      return;
    }
    list.remove (as.getAction ());
    
    if (list.isEmpty ()) {
      map.remove (as);
    }
    
    array = null;
    fireChange (); // PENDING this is too often
  }
  
  /** Creates the actions.
  */
  private synchronized static SystemAction[] createActions () {
    java.util.Iterator it = map.values ().iterator ();
    
    LinkedList arr = new LinkedList ();
    
    while (it.hasNext ()) {
      List l = (List)it.next ();
      
      arr.addAll (l);
      
      if (it.hasNext ()) {
        // add separator between modules
        arr.add (null);
      }
    
    }
    
    return (SystemAction[])arr.toArray (new SystemAction[arr.size ()]);
  }
}

/*
* Log
*  2    Gandalf   1.1         5/13/99  Jaroslav Tulach Services changed to 
*       tools.
*  1    Gandalf   1.0         5/13/99  Jaroslav Tulach 
* $
*/