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

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReadWriteLock;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.openide.util.RequestProcessor;

/**
 * Manages repository writing threads
 * @author Vladimir Kvashin
 */
public class RepositoryThreadManager {
    
//    private static final RepositoryThreadManager instance = new RepositoryThreadManager();
    
    private static final String threadNameBase = "Repository writer"; // NOI18N
    private RequestProcessor processor;
    
    private Set<Thread> threads = new CopyOnWriteArraySet<Thread>();
    private static final class ThreadsWaitLock {}
    private final Object threadsWaitLock = new ThreadsWaitLock();
    private boolean finished = false;
    
    private int currThread = 0;
    private RepositoryWriter writer;
    private RepositoryQueue queue;
    private static boolean proceed = true;

    private ReadWriteLock rwLock;
    
    private class Wrapper implements Runnable {
        
        private Runnable delegate;
        
        public Wrapper(Runnable delegate) {
            this.delegate = delegate;
        }
        
        public void run() {
            try {
                Thread.currentThread().setName(threadNameBase + ' ' + currThread++);
                threads.add(Thread.currentThread());
                delegate.run();
            } finally {
                threads.remove(Thread.currentThread());
                if( threads.isEmpty() ) {
                    finished = true;
                    synchronized (threadsWaitLock) {
                        threadsWaitLock.notifyAll();
                    }
                }
            }
        }
    }

    public RepositoryThreadManager(RepositoryWriter writer, ReadWriteLock rwLock) {
	this.writer = writer;
        this.rwLock = rwLock;
        queue = Stats.queueUseTicking ? new TickingRepositoryQueue() : new RepositoryQueue();        
    }

    public RepositoryQueue startup() {
	if( Stats.queueTrace ) System.err.printf("RepositoryThreadManager.startup\n"); // NOI18N
	int threadCount = Integer.getInteger("cnd.repository.writer.threads", 1).intValue(); // NOI18N
        if (threadCount < 1) {
            threadCount = 1;
        }

        processor = new RequestProcessor(threadNameBase, threadCount);
        for (int i = 0; i < threadCount; i++) {
            Runnable r = new Wrapper(new RepositoryWritingThread(writer, queue, rwLock));
                processor.post(r);
        }
	return queue;
    }

	public int getCurrThread() {
		return currThread;
	}

    public void shutdown() {
	if( Stats.queueTrace ) System.err.printf("RepositoryThreadManager.shutdown\n"); // NOI18N
	proceed = false;
	queue.shutdown();

        if( Stats.queueTrace ) System.err.printf("RepositoryThreadManager waiting for threads to finish...\n"); // NOI18N
	waitFinished();
	if( Stats.queueTrace ) System.err.printf("RepositoryThreadManager threads have finished.\n"); // NOI18N
    }
    
    private void waitFinished() {
	synchronized( threadsWaitLock ) {
	    while( ! finished ) {
		try {
		    threadsWaitLock.wait();
		} catch (InterruptedException ex) {
		    ex.printStackTrace();
		}
	    }
	}
	
    }
    
    /*package*/
    static boolean proceed() {
	return proceed;
    }
}
