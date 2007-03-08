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

import java.util.Collection;

/**
 * The most common base class for linked queues.
 *
 * If somebody needed this, it's easy to add an implementation of
 * addFirst(), addLast(), etc.
 *
 * But for the time being the only kind of queues that one needs
 * is the queue that is based on pairs (key, value),
 * so I'll better write such functionality in descendant.
 *
 * So for now it's mostly just a place for base inner classes,
 * Entry and Queue
 *
 * @author Vladimir Kvashin
 */
public class BaseQueue {
    
    /** 
     * A queue entry.
     *
     * Using LinkedList is simpler, 
     * but less efficient from memory point of view
     */
    protected static abstract class AbstractEntry {
	
	/** previous entry */
        private AbstractEntry prev;
	
	/** next entry */
        private AbstractEntry next;
	
    }
    
    
    /**
     * A queue as such
     */
    protected static class Queue {
        
        private AbstractEntry head;
        private AbstractEntry tail;
        
        private void link(AbstractEntry e1, AbstractEntry e2) {
            if( e1 != null ) {
                e1.next = e2;
            }
            if( e2 != null ) {
                e2.prev = e1;
            }
        }
        
        public void addFirst(AbstractEntry e) {
            link(e, head);
            head = e;
            if( tail == null ) {
                tail = head;
            }
            e.prev = null;
        }
        
        public void addLast(AbstractEntry e) {
            if( tail == null ) {
                assert head == null;
                addFirst(e);
            } else {
                link(tail, e);
                tail = e;
            }
            e.next = null;
        }
        
        public void remove(AbstractEntry e) {
            link(e.prev, e.next);
            if( head == e ) {
                head = e.next;
            }
            if( tail == e ) {
                tail = e.prev;
            }
        }
        
        public void clear() {
            head = tail = null;
        }
        
        public AbstractEntry peek() {
            return head;
        }
        
        public boolean isEmpty() {
            return head == null;
        }
        
        public AbstractEntry poll() {
            AbstractEntry ret = head;
            if( head != null ) {
                remove(head);
            }
            return ret;
        }
	
    }
    
    protected Object lock = new Object();
    protected StopWatch stopWatch = needsTiming() ? new StopWatch(false) : null;
    protected Queue queue;
    
    /** Override this to return true in case you need a very simple timing */
    protected boolean needsTiming() {
	return false;
    } 
    
    /** Override this to return true in case you need tracing */
    protected boolean needsTrace() {
	return false;
    }
    
    /** Returns this queue name; used for tracing/debugging purposes */
    protected String getTraceName() {
	return getClass().getName() + '@' + hashCode();
    }    
}
