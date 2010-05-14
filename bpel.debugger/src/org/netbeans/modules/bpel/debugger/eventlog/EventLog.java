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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.eventlog;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author Alexander Zgursky
 */
public class EventLog {
    private List<EventRecord> myRecords = new LinkedList<EventRecord>();
    private Map<Listener, Object> myListenerMap =
            new WeakHashMap<Listener, Object>();
    
    /** Creates a new instance of EventLog */
    public EventLog() {
    }
    
    public void addRecord(EventRecord record) {
        synchronized (this) {
            record.setIndex(myRecords.size());
            myRecords.add(record);
        }
        fireRecordAdded(record);
    }
    
    public synchronized EventRecord[] getAllRecords() {
        return myRecords.toArray(new EventRecord[myRecords.size()]);
    }
    
    public synchronized EventRecord[] getRecords(int fromIndex, int toIndex) {
        return myRecords.subList(fromIndex, toIndex).toArray(
                new EventRecord[toIndex - fromIndex]);
    }
    
    public synchronized int getSize() {
        return myRecords.size();
    }
    
    public void addListener(Listener listener) {
        synchronized (myListenerMap) {
            myListenerMap.put(listener, null);
        }
    }
    
    public void removeListener(Listener listener) {
        synchronized (myListenerMap) {
            myListenerMap.remove(listener);
        }
    }
    
    private void fireRecordAdded(EventRecord record) {
        Listener[] listeners;
        synchronized (myListenerMap) {
            listeners = myListenerMap.keySet().toArray(new Listener[0]);
        }
        for (Listener listener : listeners) {
            listener.recordAdded(record);
        }
    }
    
    public interface Listener {
        void recordAdded(EventRecord record);
    }
}
