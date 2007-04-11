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

import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 * @author Sergey Grinev
 */
public class TickingRepositoryQueue extends RepositoryQueue {
    
    protected int currentTick = 0;
    protected final int tickShift;
    
    public static final int queueTickShift = Stats.getInteger("cnd.repository.queue.tickshift", 5); //NOI18N
//    public static final int queueTickDelay = Stats.getInteger("cnd.repository.queue.tickdelay", 20); //NOI18N
    
    
    public TickingRepositoryQueue(int tickShift) {
        this.tickShift = tickShift;
    }
    
    public TickingRepositoryQueue() {
        this(queueTickShift);
    }
    
    protected void doReplaceAddLast(Key key, Persistent value, Entry existent) {
        super.doReplaceAddLast(key, value, existent);
        queue.remove(existent);
        queue.addLast(existent);
        ((TickingEntry)existent).setTick(currentTick);
    }
    
    protected void doPostPoll(Entry polled) {
        super.doPostPoll(polled);
        if (queue.isEmpty()) {
            currentTick = 0;
        }
    }
    
    protected KeyValueQueue.Entry createEntry(Key key, Persistent value) {
        return new TickingEntry(key, value, currentTick);
    }

    public boolean isReady() {
        synchronized ( lock ) {
            if (queue.isEmpty()) {
                return false;
            }
            TickingEntry entry = (TickingEntry)queue.peek();
            return currentTick - entry.getTick() > queueTickShift;
        }
    }
    
//    protected void _waitReady() throws InterruptedException {
//        long time = System.currentTimeMillis();
//        lock.wait(queueTickDelay);
//        long insomniaDelay = System.currentTimeMillis() - time;
//        if (insomniaDelay>0)
//            Thread.sleep(insomniaDelay);
//    }
    
    public void onIdle() {
        currentTick++; // we don't need serialization here
    }

    public class TickingEntry extends Entry {
        
        private int tick;
        
        protected TickingEntry(Key key, Persistent value, int tick) {
            super(key, value);
            this.tick = tick;
        }
        
        public int getTick() {
            return tick;
        }
        
        public void setTick(int tick) {
            this.tick = tick;
        }
    }
}
