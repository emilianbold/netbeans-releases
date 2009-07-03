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

import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 * @author Sergey Grinev
 */
public class TickingRepositoryQueue extends RepositoryQueue {
    
    protected int currentTick = 0;
    protected final int tickShift;
    
    public static final int queueTickShift = Stats.getInteger("cnd.repository.queue.tickshift", 8); //NOI18N
//    public static final int queueTickDelay = Stats.getInteger("cnd.repository.queue.tickdelay", 20); //NOI18N
    
    
    public TickingRepositoryQueue(int tickShift) {
        this.tickShift = tickShift;
    }
    
    public TickingRepositoryQueue() {
        this(queueTickShift);
    }
    
    @Override
    protected void doReplaceAddLast(Key key, Persistent value, Entry<Key, Persistent> existent) {
        if (existent instanceof TickingEntry) {
            super.doReplaceAddLast(key, value, existent);
            queue.remove(existent);
            queue.addLast(existent);
            ((TickingEntry)existent).setTick(currentTick);
        }
    }
    
    @Override
    protected void doPostPoll(Entry<Key, Persistent> polled) {
        super.doPostPoll(polled);
        if (queue.isEmpty()) {
            currentTick = 0;
        }
    }
    
    @Override
    protected TickingEntry createEntry(Key key, Persistent value) {
        return new TickingEntry(key, value, currentTick);
    }

    @Override
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
    
    @Override
    public void onIdle() {
        currentTick++; // we don't need serialization here
    }

    public class TickingEntry extends Entry<Key, Persistent> {
        
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
