/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.custom.aip;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A helper class to facilitate supporting delimiter set change notification.
 *
 * @author Jun Xu
 */
public class DelimiterSetChangeNotifier 
        implements DelimiterSetChangeNotificationSupport {
    
    private final List<DelimiterSetChangeListener> delimSetChangeListeners =
            Collections.synchronizedList(new LinkedList<DelimiterSetChangeListener>());
    
    public void addDelimiterSetChangeListener(DelimiterSetChangeListener listeners[]) {
        for (int i = 0; listeners != null && i < listeners.length; i++) {
            delimSetChangeListeners.add(listeners[i]);
        }
    }

    public void addDelimiterSetChangeListener(DelimiterSetChangeListener listener) {
        delimSetChangeListeners.add(listener);
    }

    public DelimiterSetChangeListener[] getDelimiterSetChangeListeners() {
        return delimSetChangeListeners.toArray(new DelimiterSetChangeListener[0]);
    }

    public void removeDelimiterSetChangeListener(DelimiterSetChangeListener listener) {
        delimSetChangeListeners.remove(listener);
    }
    
    public void removeDelimiterSetChangeListener(DelimiterSetChangeListener listeners[]) {
        for (int i = 0; listeners != null && i < listeners.length; i++) {
            delimSetChangeListeners.remove(listeners[i]);
        }
    }

    public void fireDelimiterSetChangeEvent(Object source, String propName,
            Object oldValue, Object newValue) {
        PropertyChangeEvent evt = new PropertyChangeEvent(source, propName, oldValue, newValue);
        DelimiterSetChangeListener[] listeners = getDelimiterSetChangeListeners();
        for (int i = 0; listeners != null && i < listeners.length; i++) {
            listeners[i].propertyChange(evt);
        }
    }
}
