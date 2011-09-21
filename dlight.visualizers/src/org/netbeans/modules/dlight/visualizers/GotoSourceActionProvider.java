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
package org.netbeans.modules.dlight.visualizers;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.AbstractAction;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.dataprovider.SourceFileInfoDataProvider;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.spi.SourceSupportProvider;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * GoToSource Actions Provider.
 *
 * @author ak119685
 */
public final class GotoSourceActionProvider {

    private final RequestProcessor RP = new RequestProcessor(GotoSourceActionProvider.class.getName(), 1);
    private final SourceFileInfoDataProvider dataprovider;
    private final SourceSupportProvider sourceSupportProvider;
    private final LinkedBlockingQueue<Request> queue = new LinkedBlockingQueue<Request>();
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicBoolean aboutToFinish = new AtomicBoolean();
    private final HashMap<Integer, GotoSourceAction> cache =
            new HashMap<Integer, GotoSourceAction>();
    private Future<?> task = null;

    public GotoSourceActionProvider(SourceSupportProvider sourceSupportProvider, SourceFileInfoDataProvider dataprovider) {
        this.dataprovider = dataprovider;
        this.sourceSupportProvider = sourceSupportProvider;
    }

    /**
     * Returns GoToSource Action for the specified function call.
     * When called for the first time, returns disabled action and starts a task
     * for getting information about source code availability. Once the result
     * of the task is available, changes action's enabled state appropriately.
     *
     * Returnes cached result on subsequent calls.
     *
     * @param functionCall - a function to get GoToSource Action for
     * @return Action that is enabled or disabled depending on source code
     *         availablity
     */
    public synchronized GotoSourceAction getAction(FunctionCallWithMetric functionCall) {
        GotoSourceAction result;
        Integer hashCode = calculateHashCode(functionCall);

        if (!cache.containsKey(hashCode)) {
            result = new GotoSourceAction();
            result.setEnabled(false);
            cache.put(hashCode, result);
            queueRequest(new Request(result, functionCall));
        } else {
            result = cache.get(hashCode);
        }

        return result;
    }

    /**
     * stop processing of all currently running source code availability check
     * tasks
     */
    public synchronized void stopCurrentProcessing() {
        Collection<Request> unprocessedRequests = new ArrayList<Request>();

        lock.lock();

        // Have to remove all unprocessed actions ... 

        try {
            queue.drainTo(unprocessedRequests);
            for (Request r : unprocessedRequests) {
                cache.remove(calculateHashCode(r.functionCall));
            }
            if (task != null && !task.isDone()) {
                task.cancel(true);
            }
        } finally {
            lock.unlock();
        }
    }

    private void queueRequest(Request request) {
        lock.lock();
        try {
            queue.offer(request);

            if (aboutToFinish.compareAndSet(true, false)) {
                try {
                    task.get();
                } catch (InterruptedException ex) {
                    // OK
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } finally {
            lock.unlock();
        }

        if (task == null || task.isDone()) {
            task = RP.submit(new Worker());
        }
    }

    private Integer calculateHashCode(FunctionCallWithMetric functionCall) {
        return (int) (functionCall.getFunction().hashCode() + 7 * functionCall.getLineNumber() + 7 * functionCall.getOffset());
    }

    private final class Worker implements Runnable {

        @Override
        public void run() {
            Request request;

            while (true) {
                lock.lock();
                try {
                    request = queue.poll();
                    if (request == null) {
                        aboutToFinish.set(true);
                        break;
                    }
                } finally {
                    lock.unlock();
                }

                SourceFileInfo sourceFileInfo = dataprovider.getSourceFileInfo(request.functionCall);
                request.action.setSourceInto(sourceFileInfo);
            }
        }
    }

    public final class GotoSourceAction extends AbstractAction {

        private final AtomicReference<SourceFileInfo> sourceInfo = new AtomicReference<SourceFileInfo>();
        private final AtomicReference<Future<Boolean>> taskRef = new AtomicReference<Future<Boolean>>();

        public GotoSourceAction() {
            super(NbBundle.getMessage(GotoSourceActionProvider.class, "GoToSourceActionName")); // NOI18N
        }

        @Override
        public synchronized void actionPerformed(ActionEvent e) {
            Future<Boolean> task = taskRef.get();

            if (task != null && !task.isDone()) {
                // Already in progress...
                return;
            }

            taskRef.set(DLightExecutorService.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() {
                    boolean result;

                    SourceFileInfo source = getSourceInfo();

                    if (source == null || !source.isSourceKnown()) {
                        result = false;
                    } else {
                        sourceSupportProvider.showSource(source);
                        result = true;
                    }

                    taskRef.set(null);
                    return result;
                }
            }, "GotoSourceAction")); // NOI18N
        }

        public SourceFileInfo getSourceInfo() {
            return sourceInfo.get();
        }

        private synchronized void setSourceInto(SourceFileInfo sourceFileInfo) {
            sourceInfo.set(sourceFileInfo);

            if (sourceFileInfo != null && sourceFileInfo.isSourceKnown()) {
                setEnabled(true);
            }
        }
    }

    private static class Request {

        final GotoSourceAction action;
        final FunctionCallWithMetric functionCall;

        private Request(GotoSourceAction action, FunctionCallWithMetric functionCall) {
            this.action = action;
            this.functionCall = functionCall;
        }
    }
}
