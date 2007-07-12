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

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.RepositoryExceptionImpl;
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

                if( Stats.queueTrace ) System.err.printf("%s: maintenance %n ms...\n", getName(), Stats.maintenanceInterval); // NOI18N
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
                    Thread.currentThread().sleep(Stats.maintenanceInterval - time);
                }

            } else {
                if( Stats.queueTrace ) System.err.printf("%s: sleeping %n ms...\n", getName(), Stats.maintenanceInterval); // NOI18N
                Thread.currentThread().sleep(Stats.maintenanceInterval);
            }
            
            queue.onIdle();
        } else {
            if( Stats.queueTrace ) System.err.printf("%s: waiting...\n", getName()); // NOI18N
                queue.waitReady();
        }
    }
    
    private static final int NUM_SPARE_TIMES_TO_ALLOW_MAINTENANCE = TickingRepositoryQueue.queueTickShift * 3;
    private int numOfSpareCycles = 0;
    private boolean maintenanceIsNeeded = true;
    
    public void run() {
        if( Stats.queueTrace ) System.err.printf("%s: started.\n", getName());
        
        while( true ) {
            RepositoryQueue.Entry entry;
            try {
                try {
                    rwLock.readLock().lock();
                    while (queue.isReady()) {
                        entry = queue.poll();
                        numOfSpareCycles = 0;
                        maintenanceIsNeeded = true;
                        if( Stats.queueTrace ) System.err.printf("%s: writing %s\n", getName(), entry.getKey()); // NOI18N
                        writer.write(entry.getKey(), entry.getValue());
                    }
                }  finally {
                    rwLock.readLock().unlock();
                }
                
                if( RepositoryThreadManager.proceed() ) {
                    waitReady();
                } else {
                    if( Stats.queueTrace ) System.err.printf("%s: exiting\n", getName()); // NOI18N
                    break;
                }
            } catch( Throwable e ) {
                RepositoryListenersManager.getInstance().fireAnException(null, 
                        new RepositoryExceptionImpl(e));
            }
        }
    }
    
    private String getName() {
        return Thread.currentThread().getName();
    }
    
}
