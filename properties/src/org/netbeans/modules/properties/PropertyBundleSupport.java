/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.properties;

import java.io.Serializable;
import javax.swing.event.EventListenerList;

/**                    
 * Support for PropertyBundle events, registering listeners, firing events.
 * @author Petr Jiricka
 */
public class PropertyBundleSupport implements Serializable {

    /** 
     * The object to be provided as the "source" for any generated events.
     * @serial
     */
    private Object source;

    /**
     * Constructs a <code>PropertyBundleSupport</code> object.
     *
     * @param sourceBean  The bean to be given as the source for any events.
     */
    public PropertyBundleSupport(Object sourceBean) {
      if (sourceBean == null) {
        throw new NullPointerException();
      }
      source = sourceBean;
    }

    /** List of listeners */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Add a listener to the list that's notified each time a change
     * to the property bundle occurs.
     *
     * @param	l		the PropertyBundleListener
     */
    public void addPropertyBundleListener(PropertyBundleListener l) {
      listenerList.add(PropertyBundleListener.class, l);
    }

    /**
     * Remove a listener from the list that's notified each time a
     * change to the property bundle occurs.
     *
     * @param	l		the PropertyBundleListener
     */
    public void removePropertyBundleListener(PropertyBundleListener l) {
      listenerList.remove(PropertyBundleListener.class, l);
    }
                                     
    /** Fires a global change event - structure may have changed. */
    public void fireBundleStructureChanged() {
      fireBundleChanged(new PropertyBundleEvent(source, PropertyBundleEvent.CHANGE_STRUCT));
    }
                                     
    /** Fires a global change event - any property bundle data may have changed. */
    public void fireBundleDataChanged() {
      fireBundleChanged(new PropertyBundleEvent(source, PropertyBundleEvent.CHANGE_ALL));
    }
                                     
    /** Fires a file change event - one entry has changed. */
    public void fireFileChanged(String entryName) {
      fireBundleChanged(new PropertyBundleEvent(source, entryName));
    }
                                     
    /** Fires an item change event - one item has changed. */
    public void fireItemChanged(String entryName, String itemName) {
      fireBundleChanged(new PropertyBundleEvent(source, entryName, itemName));
    }
                                     
    /**
     * Forward the given notification event to all PropertyBundleListeners that registered
     * themselves as listeners for this property bundle.
     * @see #addPropertyBundleListener
     * @see PropertyBundleEvent
     * @see EventListenerList
     */
    public void fireBundleChanged(PropertyBundleEvent e) {
//System.out.println(e.toString());    
      // Guaranteed to return a non-null array
      Object[] listeners = listenerList.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i]==PropertyBundleListener.class) {
          ((PropertyBundleListener)listeners[i+1]).bundleChanged(e);
        }
      }
    }
                          
} // End of class PropertyBundleSupport

/*
 * <<Log>>
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/10/99  Petr Jiricka    
 *  4    Gandalf   1.3         8/18/99  Petr Jiricka    Nothing
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/14/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/13/99  Petr Jiricka    
 * $
 */
