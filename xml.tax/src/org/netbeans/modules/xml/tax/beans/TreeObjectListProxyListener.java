/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

    private final TreeObjectList list;
    private PropertyChangeSupport changeSupport;
        
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
        if (changeSupport != null)
            changeSupport.firePropertyChange(e);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (changeSupport == null)
                changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (changeSupport != null)
            changeSupport.removePropertyChangeListener(l);
    }
}
