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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The queue that is based on key-value pairs
 * @author Vladimir Kvashin
 */
public class KeyValueQueue<K, V> extends BaseQueue {

    public static class Entry<KK, VV> extends BaseQueue.AbstractEntry {
	
	private KK key;
	private VV value;
	
	protected Entry(KK key, VV value) {
	    assert( key != null);
	    assert( value != null);
	    this.key = key;
	    this.value = value;
	}
	
	public KK getKey() {
	    return key;
	}
	
	public VV getValue() {
	    return value;
	}
    }

    protected final Map<K, Entry<K, V>> map = new HashMap<K, Entry<K, V>>();
    private final EventsDispatcher<K, V> dispatcher;
    protected boolean active = true;
	    
    public KeyValueQueue() {
        super(new BaseQueue.Queue());
        dispatcher = new EventsDispatcher<K, V>(this);
        dispatcher.start();
    }
    
    public void addLast(K key, V value) {
	if( needsTrace() ) System.err.printf("%s: addLast %s\n", getTraceName(), key.toString());
        dispatcher.newEvent(createEntry(key, value));
    }

    private void addLastImpl(K key, V value) {
        synchronized (lock) {
            Entry<K, V> entry = map.get(key);
            if (entry == null) {
                doAddLast(key, value);
                if (needsTrace()) {
                    System.err.printf("%s: added last %s\n", getTraceName(), key.toString());
                }
            } else {
                doReplaceAddLast(key, value, entry);
                if (needsTrace()) {
                    System.err.printf("%s: replaced last %s\n", getTraceName(), key.toString());
                }
            }
            lock.notifyAll();
        }
    }

    private final static class EventsDispatcher<KK, VV> extends Thread {
        private final KeyValueQueue<KK, VV> delegate;
        private final BlockingQueue<Entry<KK, VV>> queue = new LinkedBlockingQueue<Entry<KK, VV>>();
        
        public EventsDispatcher(KeyValueQueue<KK, VV> delegate) {
            super("CND Repository Queue Dispatcher"); // NOI18N
            this.delegate = delegate;
        }

        void newEvent(Entry<KK, VV> entry) {
            queue.add(entry);
        }
        
        void handleEvent(KK key, VV value) {
            delegate.addLastImpl(key, value);
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Entry<KK, VV> event;
                    try {
                        event = queue.take();
                    } catch (InterruptedException ex) {
                        // it's ok
                        break;
                    }
                    handleEvent(event.key, event.value);
                } catch (Throwable th) {
                    th.printStackTrace(System.err);
                }
            }
        }
    }

    private Entry<K, V> doAddLast(K key, V value) {
	Entry<K, V> entry = createEntry(key, value);
	map.put(key, entry);
	queue.addLast(entry);
	return entry;
    }

    protected Entry<K, V> createEntry(K key, V value) {
        return new Entry<K, V>(key, value);
    }
    
    protected void doReplaceAddLast(K key, V value, Entry<K, V> existent) {
	existent.value = value;
    }
    
    protected void doReplaceAddFirst(K key, V value, Entry<K, V> existent) {
	existent.value = value;
    }
    
    public Entry<K, V> poll() throws InterruptedException {
	if( needsTrace() ) System.err.printf("%s: Polling...\n", getTraceName());
	synchronized( lock ) {
            try {
                @SuppressWarnings("unchecked")
                Entry<K, V> e = (Entry<K, V>) queue.poll(); // TODO: find out more elegant solution than a stupid cast!
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
    
    protected void doPostPoll(Entry<K, V> polled) {
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
        dispatcher.interrupt();
	synchronized ( lock ) {
	    lock.notifyAll();
	}
    }
    
}
