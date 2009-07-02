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

    public BaseQueue(Queue queue) {
        this.queue = queue;
    }

    private static final class Lock {}
    protected final Object lock = new Lock();
    protected StopWatch stopWatch = needsTiming() ? new StopWatch(false) : null;
    protected final Queue queue;
    
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
