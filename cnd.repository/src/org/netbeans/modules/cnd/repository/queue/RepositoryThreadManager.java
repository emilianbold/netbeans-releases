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

import java.util.*;
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
    private Set/*<Thread>*/ threads = Collections.synchronizedSet(new HashSet()/*<Thread>*/);
    private int currThread = 0;
    private boolean standalone;
    private RepositoryWriter writer;
    private RepositoryQueue queue;
    private static boolean proceed = true;
    
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
            }
            finally {
                threads.remove(Thread.currentThread());
            }
        }
    }

    public RepositoryThreadManager(RepositoryWriter writer) {
	this.writer = writer;
	standalone = ! RepositoryThreadManager.class.getClassLoader().getClass().getName().startsWith("org.netbeans."); // NOI18N
    }

    public RepositoryQueue startup() {
	if( Stats.queueTrace ) System.err.printf("RepositoryThreadManager.startup\n"); // NOI18N
	int threadCount = Integer.getInteger("cnd.repository.writer.threads", 1).intValue(); // NOI18N
        if (threadCount < 1) {
            threadCount = 1;
        }
        if( ! standalone ) {
            processor = new RequestProcessor(threadNameBase, threadCount);
        }
	queue = new RepositoryQueue();
        for (int i = 0; i < threadCount; i++) {
            Runnable r = new Wrapper(new RepositoryWritingThread(writer, queue));
            if( standalone ) {
                new Thread(r).start();
            }
            else {
                processor.post(r);
            }
        }
	return queue;
    }

	public int getCurrThread() {
		return currThread;
	}

    public void shutdown() {
	if( Stats.queueTrace ) System.err.printf("RepositoryThreadManager.shutdown\n"); // NOI18N
	proceed = false;
	queue.unblock();
//        for (Iterator it = new ArrayList(threads).iterator(); it.hasNext();) {
//            Thread thread = (Thread) it.next();
//            thread.interrupt();
//        }
    }
    
    /*package*/
    static boolean proceed() {
	return proceed;
    }
}
