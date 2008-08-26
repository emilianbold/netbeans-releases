/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
    private Entry doAddLast(K key, V value) {
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
    
    private Entry doAddFirst(K key, V value) {
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
                lock.wait();
		if( needsTrace() ) System.err.printf("%s: waiting finished\n", getTraceName());
            }
        }
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
