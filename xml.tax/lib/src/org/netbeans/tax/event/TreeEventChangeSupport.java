/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.event;

import java.lang.reflect.*;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.netbeans.tax.TreeObject;
import java.util.Set;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TreeEventChangeSupport {
    /** Only for debug purposes. */
    private static final boolean DEBUG        = false;
    /** */
    private static final boolean DEBUG_ADD    = false;
    /** */
    private static final boolean DEBUG_REMOVE = false;
    /** */
    private static final boolean DEBUG_FIRE   = false;
    
    /** Utility field used by bound properties. */
    private PropertyChangeSupport propertyChangeSupport;
    
    /** Event source. */
    private TreeObject eventSource;
    
    /** Its event cache. */
    private EventCache eventCache;
    
    //
    // init
    //
    
    /** Creates new TreeEventChangeSupport. */
    public TreeEventChangeSupport (TreeObject eventSource) {
        this.eventSource = eventSource;
        this.eventCache = new EventCache ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public final TreeEvent createEvent (String propertyName, Object oldValue, Object newValue) {
        return new TreeEvent (eventSource, propertyName, oldValue, newValue);
    }
    
    /**
     */
    protected final TreeObject getEventSource () {
        return eventSource;
    }
    
    
    /**
     */
    private final PropertyChangeSupport getPropertyChangeSupport () {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport (eventSource);
        }
        return propertyChangeSupport;
    }
    
    /** Add a PropertyChangeListener to the listener list.
     * @param listener The listener to add.
     */
    public final void addPropertyChangeListener (PropertyChangeListener listener) {
        getPropertyChangeSupport ().addPropertyChangeListener (listener);
        
        if ( DEBUG_ADD ) {
            Util.debug ("-\n- TreeEventChangeSupport::addPropertyChangeListener: listener = " + listener); // NOI18N
            Util.debug ("-                       ::addPropertyChangeListener: propertyChangeSupport = " + listListeners ()); // NOI18N
            Util.debug ("", new RuntimeException ("Request origin."));
            
            if ( listener == null ) {
                Util.debug ("-                       ::addPropertyChangeListener: eventSource = " + eventSource); // NOI18N
                Util.debug ("-\n", new RuntimeException ("TreeEventChangeSupport.addPropertyChangeListener")); // NOI18N
            }
        }
    }
    
    /**
     */
    public final void addPropertyChangeListener (String propertyName, PropertyChangeListener listener) {
        getPropertyChangeSupport ().addPropertyChangeListener (propertyName, listener);
        
        if ( DEBUG_ADD ) {
            Util.debug ("-\n- TreeEventChangeSupport::addPropertyChangeListener: propertyName = " + propertyName); // NOI18N
            Util.debug ("-                       ::addPropertyChangeListener: listener = " + listener); // NOI18N
            Util.debug ("-                       ::addPropertyChangeListener: propertyChangeSupport = " + listListeners ()); // NOI18N
            
            if ( listener == null ) {
                Util.debug ("-                       ::addPropertyChangeListener: eventSource = " + eventSource); // NOI18N
                Util.debug ("-\n", new RuntimeException ("TreeEventChangeSupport.addPropertyChangeListener")); // NOI18N
            }
        }
    }
    
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param listener The listener to remove.
     */
    public final void removePropertyChangeListener (PropertyChangeListener listener) {
        getPropertyChangeSupport ().removePropertyChangeListener (listener);
        
        if ( DEBUG_REMOVE ) {
            Util.debug ("\n- TreeEventChangeSupport::removePropertyChangeListener: listener = " + listener); // NOI18N
            Util.debug ("-                       ::removePropertyChangeListener: propertyChangeSupport = " + listListeners ()); // NOI18N
        }
    }
    
    /**
     */
    public final void removePropertyChangeListener (String propertyName, PropertyChangeListener listener) {
        getPropertyChangeSupport ().removePropertyChangeListener (propertyName, listener);
        
        if ( DEBUG_REMOVE ) {
            Util.debug ("\n- TreeEventChangeSupport::removePropertyChangeListener: propertyName = " + propertyName); // NOI18N
            Util.debug ("-                       ::removePropertyChangeListener: listener = " + listener); // NOI18N
            Util.debug ("-                       ::removePropertyChangeListener: propertyChangeSupport = " + listListeners ()); // NOI18N
        }
    }
    
    /**
     * Check if there are any listeners for a specific property.
     *
     * @param propertyName  the property name.
     * @return true if there are ore or more listeners for the given property
     */
    public final boolean hasPropertyChangeListeners (String propertyName) {
        return getPropertyChangeSupport ().hasListeners (propertyName);
    }
    
    /**
     * Fire an existing PropertyChangeEvent to any registered listeners.
     * No event is fired if the given event's old and new values are
     * equal and non-null.
     * @param evt  The PropertyChangeEvent object.
     */
    public final void firePropertyChange (TreeEvent evt) {
        if ( DEBUG_FIRE ) {
            Util.debug ("- TreeEventChangeSupport::firePropertyChange ( " + evt + " )"); // NOI18N
            Util.debug ("-     eventSource  = " + eventSource); // NOI18N
            Util.debug ("-     EventManager = " + eventSource.getEventManager ()); // NOI18N
        }
        
        if ( eventSource.getEventManager () == null )
            return;
        eventSource.getEventManager ().firePropertyChange (this, evt);
    }
    
    /**
     */
    protected final void firePropertyChangeNow (TreeEvent evt) {
        getPropertyChangeSupport ().firePropertyChange (evt);
    }
    
    /**
     */
    protected final void firePropertyChangeLater (TreeEvent evt) {
        eventCache.addEvent (evt);
    }
    
    /**
     */
    protected final void firePropertyChangeCache () {
        eventCache.firePropertyChange ();
    }
    
    /**
     */
    protected final void clearPropertyChangeCache () {
        eventCache.clear ();
    }
    
    
    //
    // debug
    //
    
    
    /**
     */
    private String listListeners (Object instance) {
        try {
            Class klass = instance.getClass ();
            Field field = klass.getDeclaredField ("listeners"); // NOI18N
            field.setAccessible (true);
            
            return field.get (instance).toString ();
        } catch (Exception ex) {
            return "" + ex.getClass () + " " + ex.getMessage (); // NOI18N
        }
    }
    
    /**
     */
    private String listChildrenListeners (PropertyChangeSupport support) {
        try {
            Object instance = support;
            Class klass = instance.getClass ();
            Field field = klass.getDeclaredField ("children"); // NOI18N
            field.setAccessible (true);
            
            StringBuffer sb = new StringBuffer ();
            Map map = (Map)field.get (instance);
            if (map == null) return "";
            Set keys = map.keySet ();
            Iterator it = keys.iterator ();
            while (it.hasNext ()) {
                Object key = it.next ();
                sb.append ("\n[").append (key).append ("] ").append (listListeners (map.get (key))); // NOI18N
            }
            
            return sb.toString ();
        } catch (Exception ex) {
            ex.printStackTrace ();
            return "<" + ex + ">"; // NOI18N
        }
    }
    
    
    /**
     * For debug purposes list all registered listeners
     */
    public final String listListeners () {
        StringBuffer sb = new StringBuffer ();
        
        sb.append ("[*general*] ").append (listListeners (getPropertyChangeSupport ())); // NOI18N
        sb.append (listChildrenListeners (getPropertyChangeSupport ()));
        
        return sb.toString ();
    }
    
    
    //
    // Event Cache
    //
    
    /**
     * EventCache for later event firing.
     */
    class EventCache {
        
        /** */
        List eventList;
        
        
        //
        // init
        //
        
        /** Creates new EventCache. */
        public EventCache () {
            eventList = new LinkedList ();
        }
        
        
        //
        // itself
        //
        
        /**
         */
        public void clear () {
            synchronized ( eventList ) {
                eventList.clear ();
            }
        }
        
        
        /**
         */
        public void addEvent (TreeEvent event) {
            synchronized ( eventList ) {
                eventList.add (event);
            }
        }
        
        /**
         */
        public void firePropertyChange () {
            List listCopy;
            synchronized ( eventList ) {
                listCopy = new LinkedList (eventList);
                eventList.clear ();
            }
            Iterator it = listCopy.iterator ();
            while ( it.hasNext () ) {
                firePropertyChangeNow ((TreeEvent)it.next ());
            }
        }
        
    } // end: class EventCache
    
}
