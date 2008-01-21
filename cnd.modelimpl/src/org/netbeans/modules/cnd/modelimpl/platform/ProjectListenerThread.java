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

package org.netbeans.modules.cnd.modelimpl.platform;

import java.util.LinkedList;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;

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
		DiagnosticExceptoins.register(ex);

            }
        }
    }
}
