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

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import org.netbeans.modules.cnd.repository.api.RepositoryException;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;

/**
 *
 * @author Vladimir Kvashin
 */
public class RepositoryWritingThread implements Runnable {
    
    private RepositoryWriter writer;
    private RepositoryQueue queue;
    private ReadWriteLock   rwLock;
    
    public RepositoryWritingThread(RepositoryWriter writer, RepositoryQueue queue, ReadWriteLock rwLock) {
	this.writer = writer;
	this.queue = queue;
        this.rwLock = rwLock;
    }

    private void waitReady() throws InterruptedException {
        if( Stats.maintenanceInterval > 0 ) {
            if( Stats.allowMaintenance && 
                (++numOfSpareCycles >= NUM_SPARE_TIMES_TO_ALLOW_MAINTENANCE)
                && maintenanceIsNeeded) {

                if (Stats.queueTrace) { System.err.printf("%s: maintenance %d ms...\n", getName(), Stats.maintenanceInterval); } // NOI18N
                long time = System.currentTimeMillis();
                
                // there should be no maintenance if the writing is blocked
                try {
                    rwLock.readLock().lock();
                    try {
                        maintenanceIsNeeded = writer.maintenance(Stats.maintenanceInterval);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } finally {
                    rwLock.readLock().unlock();
                }
                
                time = System.currentTimeMillis() - time;
                if( time < Stats.maintenanceInterval ) {
                    Thread.sleep(Stats.maintenanceInterval - time);
                }

            } else {
                if( Stats.queueTrace ) { System.err.printf("%s: sleeping %d ms...\n", getName(), Stats.maintenanceInterval); } // NOI18N
                Thread.sleep(Stats.maintenanceInterval);
            }
            
            queue.onIdle();
        } else {
            if( Stats.queueTrace ) { System.err.printf("%s: waiting...\n", getName()); } // NOI18N
            queue.waitReady();
        }
    }
    
    private static final int NUM_SPARE_TIMES_TO_ALLOW_MAINTENANCE = TickingRepositoryQueue.queueTickShift * 3;
    private int numOfSpareCycles = 0;
    private boolean maintenanceIsNeeded = true;
    
    public void run() {
        if (Stats.queueTrace) { System.err.printf("%s: started.\n", getName()); }
        
        while( true ) {
            RepositoryQueue.Entry<Key, Persistent> entry;
            try {
                try {
                    rwLock.readLock().lock();
                    while (queue.isReady()) {
                        entry = queue.poll();
                        numOfSpareCycles = 0;
                        maintenanceIsNeeded = true;
                        if (Stats.queueTrace) { System.err.printf("%s: writing %s\n", getName(), entry.getKey()); } // NOI18N
                        writer.write(entry.getKey(), entry.getValue());
                    }
                }  finally {
                    rwLock.readLock().unlock();
                }
                
                if( RepositoryThreadManager.proceed() ) {
                    waitReady();
                } else {
                    if (Stats.queueTrace) { System.err.printf("%s: exiting\n", getName()); } // NOI18N
                    break;
                }
            } catch( Throwable e ) {
                RepositoryListenersManager.getInstance().fireAnException(null, 
                        new RepositoryException(e));
            }
        }
    }
    
    private String getName() {
        return Thread.currentThread().getName();
    }
    
}
