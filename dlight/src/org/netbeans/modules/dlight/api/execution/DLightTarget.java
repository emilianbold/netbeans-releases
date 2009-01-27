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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.impl.DLightTargetAccessor;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * D-Light Target. It can be anything: starting from shell script to
 *  the whole system.
 * You should implement this interface in case you have your own
 * target type.
 * Default implementation {@link org.netbeans.modules.dlight.api.support.NativeExecutableTarget} can
 * be used.
 */
public abstract class DLightTarget {

  private static final Logger log = DLightLogger.getLogger(DLightTarget.class);
  private List<DLightTargetListener> listeners = Collections.synchronizedList(new ArrayList<DLightTargetListener>());
  private final DLightTargetExecutionService executionService;
  

  static {
    DLightTargetAccessor.setDefault(new DLightTargetAccessorImpl());
  }

  /**
   * Create new target to be d-lighted, as a parameter service which
   * can start and terminated target should be passed
   * @param executionService service to start and terminate target
   */
  protected DLightTarget(DLightTarget.DLightTargetExecutionService executionService) {
    this.executionService = executionService;
  }

  private final DLightTargetExecutionService getExecutionService() {
    return executionService;
  }

  /**
   * Adds target listener, all listeners will be notofied about
   * target state change.
   * @param listener add listener
   */
  public final void addTargetListener(DLightTargetListener listener) {
    if (listener != null && !listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Remove target listener
   * @param listener listener to remove from the list
   */
  public final void removeTargetListener(DLightTargetListener listener) {
    if (listeners.contains(listener)) {
      listeners.remove(listener);
    }
  }

  /**
   * Notifyes listeners target state changed
   * @param oldState state target was
   * @param newState state  target is
   */
  protected final void notifyListeners(DLightTarget.State oldState, DLightTarget.State newState) {
    DLightTargetListener[] ls = listeners.toArray(new DLightTargetListener[0]);
    for (DLightTargetListener l : ls) {
      l.targetStateChanged(this, oldState, newState);
    }
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
   * This service should be implemented to run target
   * @param <T> target to execute
   */
  public interface DLightTargetExecutionService<T extends DLightTarget> {

    /**
     * Start target
     * @param target targeto start
     */
    public void start(T target);

    /**
     * Terminate target
     * @param target target to terminate
     */
    public void terminate(T target);
  }

  private static final class DLightTargetAccessorImpl extends DLightTargetAccessor {

    @Override
    public DLightTargetExecutionService getDLightTargetExecution(DLightTarget target) {
      return target.getExecutionService();
    }
  }
}
