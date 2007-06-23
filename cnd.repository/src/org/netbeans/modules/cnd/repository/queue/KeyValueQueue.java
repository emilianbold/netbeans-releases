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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.queue;

import java.util.HashMap;
import java.util.Map;

/**
 * The queue that is based on key-value pairs
 * @author Vladimir Kvashin
 */
public class KeyValueQueue<K, V> extends BaseQueue {

    public class Entry extends BaseQueue.AbstractEntry {
	
	private K key;
	private V value;
	
	protected Entry(K key, V value) {
	    assert( key != null);
	    assert( value != null);
	    this.key = key;
	    this.value = value;
	}
	
	public K getKey() {
	    return key;
	}
	
	public V getValue() {
	    return value;
	}
    }

    protected Map<K, Entry> map = new HashMap<K, Entry>();
    protected boolean active = true;
	    
    public KeyValueQueue() {
	queue = new BaseQueue.Queue();
    }
    
    public void addLast(K key, V value) {
	if( needsTrace() ) System.err.printf("%s: addLast %s\n", getTraceName(), key.toString());
	synchronized ( lock ) {
	    Entry entry = map.get(key);
	    if( entry == null ) {
		doAddLast(key, value);
		if( needsTrace() ) System.err.printf("%s: added last %s\n", getTraceName(), key.toString());
	    }
	    else {
		doReplaceAddLast(key, value, entry);
		if( needsTrace() ) System.err.printf("%s: replaced last %s\n", getTraceName(), key.toString());
	    }
	    lock.notifyAll();
	}
    }
    
    protected Entry doAddLast(K key, V value) {
	Entry entry = createEntry(key, value);
	map.put(key, entry);
	queue.addLast(entry);
	return entry;
    }
    
    protected void doReplaceAddLast(K key, V value, Entry existent) {
	existent.value = value;
    }
    
    public void addFirst(K key, V value) {
	if( needsTrace() ) System.err.printf("%s: addFirst %s\n", getTraceName(), key.toString());
	synchronized ( lock ) {
	    Entry entry = map.get(key);
	    if( entry == null ) {
		doAddFirst(key, value);
		if( needsTrace() ) System.err.printf("%s: added first %s\n", getTraceName(), key.toString());
	    }
	    else {
		doReplaceAddFirst(key, value, entry);
		if( needsTrace() ) System.err.printf("%s: replaced first %s\n", getTraceName(), key.toString());
	    }
	    lock.notifyAll();
	}
    }
    
    protected Entry createEntry(K key, V value) {
        return new Entry(key, value);
    }
    
    protected Entry doAddFirst(K key, V value) {
	Entry entry = createEntry(key, value);
	map.put(key, entry);
	queue.addFirst(entry);
	return entry;
    }
    
    protected void doReplaceAddFirst(K key, V value, Entry existent) {
	existent.value = value;
    }
    
    public Entry poll() throws InterruptedException {
	if( needsTrace() ) System.err.printf("%s: Polling...\n", getTraceName());
	synchronized( lock ) {
            try {
                Entry e = (Entry) queue.poll(); // TODO: find out more elegant solution than a stupid cast!
                if( e != null ) {
                    doPostPoll(e);
                    if( needsTrace() ) System.err.printf("    %s: polling -> %s\n", getTraceName(), e.getKey());
                }
                return e;
            } finally {
                lock.notifyAll();
            }
	}
    }
    
    protected void doPostPoll(Entry polled) {
        map.remove(polled.getKey());
    }
    
    public boolean contains(K key) {
        return map.containsKey(key);
    }
    
    public void remove(K key) {
	if( needsTrace() ) System.err.printf("%s: Removing %s\n", getTraceName(), key);
	synchronized( lock ) {
	    Entry e = map.remove(key);
	    if( e != null ) {
		queue.remove(e);
	    }
            lock.notifyAll();
	}
    }
    
    public void waitReady() throws InterruptedException {
        synchronized ( lock ) {
            while( active && !isReady() ) {
		if( needsTrace() ) System.err.printf("%s: waitReady() ...\n", getTraceName());
                _waitReady();
		if( needsTrace() ) System.err.printf("%s: waiting finished\n", getTraceName());
            }
        }
    }
    
    protected void _waitReady() throws InterruptedException {
        lock.wait();
    }
    
    public boolean isReady()  {
        synchronized ( lock ) {
            return !queue.isEmpty();
        }
    }
    
    public boolean disposable() {
	synchronized ( lock ) {
            return queue.isEmpty();
        }
    }

    public void shutdown() {
	active = false;
	synchronized ( lock ) {
	    lock.notifyAll();
	}
    }
    
}
