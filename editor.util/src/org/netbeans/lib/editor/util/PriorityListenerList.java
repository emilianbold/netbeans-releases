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

package org.netbeans.lib.editor.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

/**
 * Listener list that layers the maintained listeners
 * according to the given priority index.
 * <br>
 * Simply said it's an array of listener arrays. The priority index defines
 * the event listeners array holding all the listeners with the given priority index.
 *
 * @author Miloslav Metelka
 * @since 1.4
 */

public class PriorityListenerList implements Serializable {
    
    static final long serialVersionUID = 0L;
    
    private static final EventListener[] EMPTY_LISTENER_ARRAY = new EventListener[0];
    
    private static final EventListener[][] EMPTY_LISTENER_ARRAY_ARRAY = new EventListener[0][];

    private transient EventListener[][] listenersArray = EMPTY_LISTENER_ARRAY_ARRAY;
    
    /**
     * Add listener with the given priority.
     *
     * @param listener listener to be added.
     * @param priority &gt;=0 index defining priority
     *  with which the listener should be fired.
     *  <br>
     *  The higher the priority the sooner the listener will be fired.
     *  <br>
     *  It's guaranteed that all the listeners with higher priority index will be fired
     *  sooner than listeners with lower priority.
     *  <br>
     *  The number of priority levels should be limited to reasonably
     *  low number.
     * @throws IndexOutOfBoundsException when priority &lt; 0
     */
    public synchronized void add(EventListener listener, int priority) {
        EventListener[][] newListenersArray;
        if (priority >= listenersArray.length) {
            newListenersArray = new EventListener[priority + 1][];
            System.arraycopy(listenersArray, 0, newListenersArray, 0, listenersArray.length);
            for (int i = listenersArray.length; i < priority; i++) {
                newListenersArray[i] = EMPTY_LISTENER_ARRAY;
            }
            newListenersArray[priority] = new EventListener[] { listener };

        } else { // Add into existing listeners
            newListenersArray = (EventListener[][])listenersArray.clone();
            EventListener[] listeners = listenersArray[priority];
            EventListener[] newListeners = new EventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, newListeners, 1, listeners.length);
            newListeners[0] = listener;
            newListenersArray[priority] = newListeners;
        }

        listenersArray = newListenersArray;
    }
    
    /**
     * Remove listener with the given priority index.
     *
     * @param listener listener to be removed.
     * @param priority &gt;=0 index defining priority
     *  with which the listener was originally added.
     *  <br>
     *  If the listener was not added or it was added with different
     *  priority then no action happens.
     * @throws IndexOutOfBoundsException when priority &lt; 0
     */
    public synchronized void remove(EventListener listener, int priority) {
        if (priority < listenersArray.length) {
            EventListener[] listeners = listenersArray[priority];
            int index = listeners.length - 1;
            while (index >= 0 && listeners[index] != listener) {
                index--;
            }
            if (index >= 0) {
                EventListener[] newListeners;
                boolean removeHighestPriorityLevel;
                if (listeners.length == 1) {
                    newListeners = EMPTY_LISTENER_ARRAY;
                    removeHighestPriorityLevel = (priority == listenersArray.length - 1);
                } else {
                    newListeners = new EventListener[listeners.length - 1];
                    System.arraycopy(listeners, 0, newListeners, 0, index);
                    System.arraycopy(listeners, index + 1, newListeners, index,
                            newListeners.length - index);
                    removeHighestPriorityLevel = false;
                }
                
                EventListener[][] newListenersArray;
                if (removeHighestPriorityLevel) {
                    newListenersArray = new EventListener[listenersArray.length - 1][];
                    System.arraycopy(listenersArray, 0, newListenersArray, 0, newListenersArray.length);
                } else { // levels count stays the same
                    newListenersArray = (EventListener[][])listenersArray.clone();
                    newListenersArray[priority] = newListeners;
                }

                listenersArray = newListenersArray;
            }
        }
    }

    /**
     * Return the actual array of listeners arrays maintained by this listeners list.
     * <br>
     * <strong>WARNING!</strong>
     * Absolutely NO modification should be done on the contents of the returned
     * data.
     *
     * <p>
     * The higher index means sooner firing. Listeners with the same priority
     * are ordered so that the one added sooner has higher index than the one
     * added later. So the following firing mechanism should be used:<pre>
     *
     *  private void fireMyEvent(MyEvent evt) {
     *    EventListener[][] listenersArray = priorityListenerList.getListenersArray();
     *    for (int priority = listenersArray.length - 1; priority >= 0; priority--) {
     *      EventListener[] listeners = listenersArray[priority];
     *      for (int i = listeners.length - 1; i >= 0; i--) {
     *        ((MyListener)listeners[i]).notify(evt);
     *      }
     *    } 
     *  }
     * </pre>
     */
    public EventListener[][] getListenersArray() {
        return listenersArray;
    }

    // Serialization support.
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        
        // Save serializable event listeners
        int priority = listenersArray.length - 1; // max priority
        s.writeInt(priority); // write max priority
        for (; priority >= 0; priority--) {
            EventListener[] listeners = listenersArray[priority];
            // Write in opposite order of adding 
            for (int i = 0; i < listeners.length; i++) {
                EventListener listener = listeners[i];
                if (listener instanceof Serializable) {
                    s.writeObject(listener);
                }
            }
            s.writeObject(null);
        }
    }
    
    private void readObject(ObjectInputStream s)
    throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        int priority = s.readInt();
        listenersArray = (priority != -1)
            ? new EventListener[priority + 1][]
            : EMPTY_LISTENER_ARRAY_ARRAY;

        for (; priority >= 0; priority--) {
            List listeners = new ArrayList();
            EventListener listenerOrNull;
            while (null != (listenerOrNull = (EventListener)s.readObject())) {
                listeners.add(listenerOrNull);
            }
            listenersArray[priority] = (EventListener[])listeners.toArray(
                    new EventListener[listeners.size()]);
        }
    }

}
