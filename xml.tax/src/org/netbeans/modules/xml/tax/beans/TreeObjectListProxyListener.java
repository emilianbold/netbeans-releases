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
package org.netbeans.modules.xml.tax.beans;

import java.beans.*;
import java.util.*;

import org.netbeans.tax.*;

/**
 * This class listens on all members of object list and
 * joins all member events into this one source.
 * <p>
 * <pre>
 *   TreeObjectListProxyListener proxy = 
 *       new TreeObjectListProxyListener(list);
 *   proxy.addPropertyChangeListener(WeakListener.propertyChange(this, proxy));
 * </pre>
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class TreeObjectListProxyListener implements PropertyChangeListener {

    /** */
    private final TreeObjectList list;
    
    /** */
    private final Set listeners = new HashSet();

        
    /** Creates new TreeObjectListListener */
    public TreeObjectListProxyListener(TreeObjectList list) {
        this.list = list;
        list.addPropertyChangeListener(this);
        for (Iterator it = list.iterator(); it.hasNext();) {
            TreeObject next = (TreeObject) it.next();
            if (next != null) next.addPropertyChangeListener(this);
        }
    }

    /*
     * Update listening on list members, forward member events.
     */
    public void propertyChange(final PropertyChangeEvent e) {
        String name = e.getPropertyName();
        Object source = e.getSource();
        
        if (source == list) {
            if (TreeObjectList.PROP_CONTENT_INSERT.equals(name)) {
                TreeObject newObject = (TreeObject)e.getNewValue();
                if (newObject != null) newObject.addPropertyChangeListener(this);
            } else if (TreeObjectList.PROP_CONTENT_REMOVE.equals(name)) {
                TreeObject oldObject = (TreeObject)e.getOldValue();
                if (oldObject != null) oldObject.removePropertyChangeListener(this);
            }
        }
        
        forward(e);
    }    
    
    /**
     */
    private void forward(final PropertyChangeEvent e) {
        // forwarding
        Set peers = new HashSet();
        synchronized (listeners) {
             peers.addAll(listeners);
        }
        for (Iterator it = peers.iterator(); it.hasNext();) {
            PropertyChangeListener next = (PropertyChangeListener) it.next();
            if (next != null) next.propertyChange(e);
        }
    }
    
    /** Be friendly to WeakListener */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (l == null)
            return;
        listeners.remove(l);
    }

    /**
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (l == null)
            return;
        listeners.add(l);
    }

}
