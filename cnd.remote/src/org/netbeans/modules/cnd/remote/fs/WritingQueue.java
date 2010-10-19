/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.fs;

import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;

/**
 * @author Vladimir Kvashin
 */
public class WritingQueue implements ChangeListener {

    private static final Map<ExecutionEnvironment, WritingQueue> instances = new HashMap<ExecutionEnvironment, WritingQueue>();
    private static final Logger LOGGER = Logger.getLogger("cnd.remote.writing.queue.logger");

    private final ExecutionEnvironment execEnv;
    private final Map<Future<Integer>, String> tasks = new HashMap<Future<Integer>, String>();
    private final Set<String> failed = new HashSet<String>();
    private final Object monitor = new Object();

    public WritingQueue(ExecutionEnvironment env) {
        this.execEnv = env;
    }

    public static WritingQueue getInstance(ExecutionEnvironment env) {
        WritingQueue instance;
        synchronized (WritingQueue.class) {
            instance = instances.get(env);
            if (instance == null)  {
                instance = new WritingQueue(env);
                instances.put(env, instance);
            }
        }
        return instance;
    }

    public void add(File srcFile, String dstFileName, int mask, Writer error) {
        CommonTasksSupport.UploadParameters params = new CommonTasksSupport.UploadParameters(
                srcFile, execEnv, dstFileName, mask, error, false, this);
        LOGGER.log(Level.FINEST, "WritingQueue: added file {0}:{2}", new Object[]{execEnv, dstFileName}); //NOI18N
        Future<Integer> task = CommonTasksSupport.uploadFile(params);
        // TODO: synchronizatin flaw
        synchronized (this) {
            // TODO: stop previous taks if any
            tasks.put(task, dstFileName);
        }
    }

    // TODO: persistence! - otherwise after IDE restart we can forget about not synchronized files

    // TODO: where should the storage be? probably somewhere in in rfs caches

    @Override
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (!(source instanceof Future)) {
            CndUtils.assertTrue(false, "Wrong class, should be Future<Integer>: " + (source == null ? "null" : source.getClass())); //NOI18N
            return;
        }
        try {
            Future<Integer> task = (Future<Integer>) e.getSource();
            LOGGER.log(Level.FINEST, "WritingQueue: Task {0} at{1} finished", new Object[]{task, execEnv});
            String dstFileName;
            synchronized (this) {
                dstFileName = tasks.remove(task);
            }
            if (dstFileName == null) {
                LOGGER.warning("got null by upload task"); //NOI18N
            } else {
                try {
                    if (task.get().intValue() != 0) {
                        LOGGER.log(Level.FINEST, "WritingQueue: uploading {0}:{2} succeeded", new Object[] {execEnv, dstFileName});
                        failed.add(dstFileName);
                    } else {
                        LOGGER.log(Level.FINEST, "WritingQueue: uploading {0}:{2} failed", new Object[] {execEnv, dstFileName});
                        failed.remove(dstFileName);
                    }
                } catch (InterruptedException ex) {
                    // don't report InterruptedException
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex); // should never be the case - the task is done
                }
            }
        } finally {
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }

    public boolean waitFinished() throws InterruptedException {
        while (true) {
            synchronized (this) {
                if (tasks.isEmpty()) {
                    break;
                }
            }
            synchronized (monitor) {
                monitor.wait();
            }
        }
        // TODO: how to report user which files failed?
        return failed.isEmpty();
    }
}
