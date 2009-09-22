/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.execution;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.netbeans.modules.dlight.api.impl.DLightTargetAccessor;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.windows.InputOutput;

/**
 * D-Light Target.Target to be d-lighted, it can be anything: starting from shell script to
 *  the whole system.<br>
 * You should implement this interface in case you have your own
 * target type.
 * Default implementation {@link org.netbeans.modules.dlight.api.support.NativeExecutableTarget} can
 * be used.
 */
public abstract class DLightTarget {

    //@GuardedBy("this")
    private final List<DLightTargetListener> listeners;
    private final Info info;
    private final DLightTargetExecutionService<? extends DLightTarget> executionService;


    static {
        DLightTargetAccessor.setDefault(new DLightTargetAccessorImpl());
    }

    /**
     * Create new target to be d-lighted, as a parameter service which
     * can start and terminated target should be passed
     * @param executionService service to start and terminate target
     */
    protected DLightTarget(DLightTarget.DLightTargetExecutionService<? extends DLightTarget> executionService) {
        this.executionService = executionService;
        this.listeners = new ArrayList<DLightTargetListener>();
        this.info = new Info();
    }

    private final DLightTargetExecutionService<? extends DLightTarget> getExecutionService() {
        return executionService;
    }

    /**
     * Adds target listener, all listeners will be notofied about
     * target state change.
     * @param listener add listener
     */
    public final void addTargetListener(DLightTargetListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (this) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    /**
     * Remove target listener
     * @param listener listener to remove from the list
     */
    public final void removeTargetListener(DLightTargetListener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifyes listeners target state changed in separate thread
     * @param oldState state target was
     * @param newState state  target is
     */
    protected final void notifyListeners(final DLightTargetChangeEvent event) {
        DLightTargetListener[] ll;

        synchronized (this) {
            ll = listeners.toArray(new DLightTargetListener[0]);
        }
        
        final CountDownLatch doneFlag = new CountDownLatch(ll.length);

        // Will do notification in parallel, but wait until all listeners
        // finish processing of event.
        for (final DLightTargetListener l : ll) {
            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    try {
                        l.targetStateChanged(event);
                    } finally {
                        doneFlag.countDown();
                    }
                }
            }, "Notifying " + l); // NOI18N
        }

        try {
            doneFlag.await();
        } catch (InterruptedException ex) {
        }

    }

    protected final String putToInfo(String name, String value){
        return info.put(name, value);
    }

    /**
     * Returns {@link org.netbeans.modules.nativeexecution.api.ExecutionEnvironment} this
     * target will be run at
     * @return {@link org.netbeans.modules.nativeexecution.api.ExecutionEnvironment} to run this target at
     */
    public abstract ExecutionEnvironment getExecEnv();

    /**
     * Returns current target state as {@link org.netbeans.modules.dlight.api.execution.DLightTarget.State}
     * @return target current state
     */
    public abstract DLightTarget.State getState();

    /**
     * States target can be at
     */
    public enum State {

        /**
         * Initial state
         */
        INIT,
        /**
         * Starting state
         */
        STARTING,
        /**
         * Running state
         */
        RUNNING,
        /**
         * Target is done
         */
        DONE,
        /**
         * Target is failed
         */
        FAILED,
        /**
         * Target is Stopped
         */
        STOPPED,
        /**
         * Target is terminated
         */
        TERMINATED,
    }

    /**
     * Returns target exit code or <code>-1</code> if exit code is unknown
     * (e.g. target is not started yet, target was terminated manually).
     * If target is still running, waits until target if finished.
     *
     * @return target exit code
     * @throws InterruptedException  if target execution is interrupted
     */
    public abstract int getExitCode() throws InterruptedException;

    /**
     * This service should be implemented to run target along
     * with DLightTarget implementation
     * @param <T> target to execute
     */
    public interface DLightTargetExecutionService<T extends DLightTarget> {

        /**
         * Start target
         * @param target target to start
         * @param executionEnvProvider  execution enviroment provider
         * @return return I/O tab or <code>null</code> which will be used for the inout/output
         */
        public InputOutput  start(T target, ExecutionEnvVariablesProvider executionEnvProvider);

        /**
         * Terminate target
         * @param target target to terminate
         */
        public void terminate(T target);
    }

    /**
     * This provider is supposed to be implemented by the implementator of
     * {@link org.netbeans.modules.dlight.spi.collector.DataCollector} or
     * {@link org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider> if
     * some additional setting up is required before target is stared as an example
     * LD_PRELOAD can be considered
     */
    public interface ExecutionEnvVariablesProvider {

        /**
         * Returns enviroment variables map (name - value) which should
         * be set up before DLightTarget is started
         * @param target  target that is going to start
         * @return enviroment variables map to set up before target is starting
         * @throws ConnectException in case connection to target host is needed,
         *      but the host is not connected yet
         */
        Map<String, String> getExecutionEnv(DLightTarget target) throws ConnectException;
    }

    private static final class DLightTargetAccessorImpl extends DLightTargetAccessor {

        @Override
        public DLightTargetExecutionService<? extends DLightTarget> getDLightTargetExecution(DLightTarget target) {
            return target.getExecutionService();
        }

        @Override
        public Info getDLightTargetInfo(DLightTarget target) {
            return target.info;
        }
    }

    public final class Info{
        private Map<String, String> map;

        Info(){
            map = new ConcurrentHashMap<String, String>();
        }

        public Map<String, String> getInfo(){
            return map;
        }

        String put(String name, String value){
            return map.put(name, value);
        }




    }
}
