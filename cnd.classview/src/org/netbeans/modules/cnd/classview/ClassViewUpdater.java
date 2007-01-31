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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.classview;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;

/**
 * Deals with class view model updates
 * @author vk155633
 */
public class ClassViewUpdater implements Runnable {
    
    private static final boolean traceEvents = Boolean.getBoolean("cnd.classview.updater-events"); // NOI18N
   
    private static class BlockingQueue {
        
        private LinkedList data = new LinkedList();
        
        private Object lock = new Object();
        
        public CsmChangeEvent get() throws InterruptedException {
            synchronized( lock ) {
                while( data.isEmpty() ) {
                    lock.wait();
                }
                return (CsmChangeEvent) data.removeFirst();
            }
        }

        public void add(CsmChangeEvent event) {
            synchronized( lock ) {
                data.add(event);
                lock.notify();
            }
        }

        public CsmChangeEvent peek() throws InterruptedException {
            synchronized( lock ) {
                while( data.isEmpty() ) {
                    lock.wait();
                }
                return (CsmChangeEvent) data.peek();
            }
        }

        public boolean isEmpty() throws InterruptedException {
            synchronized( lock ) {
                return data.isEmpty();
            }
        }
    }
    
    private ClassViewModel model;
    private BlockingQueue queue;
    
    public ClassViewUpdater(ClassViewModel model) {
        this.model = model;
        queue = new BlockingQueue();
    }

    private boolean isSkiped(CsmChangeEvent e){
        if (model.isShowLibs()){
            return false;
        }
        if (e.getChangedProjects().size()==1){
            CsmProject project = (CsmProject)e.getChangedProjects().iterator().next();
            if (model.isLibProject(project)){
                return true;
            }
        }
        return false;
    }
    
    /** 
     * delay before class view update.
     */
    private static final int MINIMAL_DELAY = 500;
    
    /**
     * delay before checking queue in batch mode.
     */
    private static final int BATCH_MODE_DELAY = 1000;

    /**
     * stop collect events when batch contains:
     */
    private static final int MAXIMAL_BATCH_SIZE = 50;
    
    /**
     * stop collect events when batch consume time in second:
     */
    private static final int MAXIMAL_BATCH_TIME = 10;
    
    /**
     * delay on user activity.
     */
    private static final int USER_ACTIVITY_DELAY = 1000;
    
    public void run() {
        long start = 0;
        try {
            while( true ) {
                CsmChangeEvent e = queue.get();
                if (isSkiped(e)){
                    continue;
                }
                if (queue.isEmpty()) {
                    Thread.sleep(MINIMAL_DELAY);
                }
                int doWait = 0;
                SmartChangeEvent compose = new SmartChangeEvent(e);
                while(true){
                    while(!queue.isEmpty()){
                        e = queue.peek();
                        if (!isSkiped(e)){
                            if (!compose.addChangeEvent(e)){
                                break;
                            }
                        }
                        queue.get();
                        if (queue.isEmpty() && compose.getCount() < MAXIMAL_BATCH_SIZE && doWait < MAXIMAL_BATCH_TIME) {
                            doWait++;
                            Thread.sleep(BATCH_MODE_DELAY);
                        }
                    }
                    if (model.isUserActivity()){
                        Thread.sleep(USER_ACTIVITY_DELAY);
                        continue;
                    }
                    break;
                }
                if (traceEvents) start = System.nanoTime();
                model.update(compose);
                if (traceEvents) {
                    long end = System.nanoTime();
                    long time = (end-start)/1000000;
                    System.out.println("Compose change event contains "+compose.getCount()+ // NOI18N
                                       " events and +"+compose.getNewDeclarations().size()+ // NOI18N
                                       "-"+compose.getRemovedDeclarations().size()+ // NOI18N
                                       "/"+compose.getChangedDeclarations().size()+ // NOI18N
                                       "*"+compose.getNewNamespaces().size()+ // NOI18N
                                       " declarations. Time = "+((float)(time)/1000.)); // NOI18N
                }
            }
        } catch( InterruptedException e ) {
            return;
        }
    }
    
    public void scheduleUpdate(CsmChangeEvent e) {
        //model.update(e);
        queue.add(e);
    }
}
