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

package org.netbeans.modules.cnd.modelimpl.platform;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.csm.core.CodeModelRequestProcessor;
import org.openide.util.RequestProcessor;

/**
 * See issue #76034 (When add a lot of source files into the project, then GUI hangs for long time)
 * @author vk155633
 */
public class ProjectListenerThread implements Runnable {

    private static class TaskQueue {

        LinkedList queue = new LinkedList();
    
        public synchronized void addTask(Runnable task) {
            queue.addLast(task);
            notify();
        }
    
        public synchronized Runnable getTask() throws InterruptedException {
            while (queue.isEmpty()) {
                wait();
            }
            return (Runnable) queue.removeFirst();
        }
    }
    
    private static ProjectListenerThread instance =  new ProjectListenerThread();
    
    private static TaskQueue queue = new TaskQueue();
    
    public static ProjectListenerThread instance() {
//        if( instance == null ) {
//            synchronized( ProjectListenerThread.class ) {
//                if( instance == null ) {
//                    instance = new ProjectListenerThread();
//                    //RequestProcessor.getDefault().post(instance);
//                    //CodeModelRequestProcessor.instance().post(instance);
//                }
//            }
//        }
        return instance;
    }

    public void postTask(Runnable task) {
        queue.addTask(task);
    }
    
    public void run() {
        while( true ) {
            Runnable task;
            try {
                task = queue.getTask();
                if( task!= null ) {
                    task.run();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
