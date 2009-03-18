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
package org.netbeans.modules.dlight.management.api;

import java.net.ConnectException;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget.ExecutionEnvVariablesProvider;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.management.api.ExecutionContextEvent.Type;
import org.netbeans.modules.dlight.api.impl.DLightToolAccessor;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.spi.impl.DataCollectorProvider;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.openide.util.Exceptions;

final class ExecutionContext {

    private static final Object lock = new Object();
    private static final Logger log = DLightLogger.getLogger(ExecutionContext.class);
    // ***
    private final DLightTarget target;
    private final DLightTargetExecutionEnvProviderCollection envProvider;
    // Immutable
    private final List<DLightTool> tools;
    // @GuardedBy("lock")
    private List<ExecutionContextListener> listeners = null;
    private volatile boolean validationInProgress = false;

    ExecutionContext(final DLightTarget target, DLightConfiguration dlightConfiguration) {
        this.target = target;
        tools = Collections.unmodifiableList(dlightConfiguration.getToolsSet());
        DataCollectorProvider.getInstance().reset();
        envProvider = new DLightTargetExecutionEnvProviderCollection();
    }

    void clear() {
        envProvider.clear();
    }

    DLightTarget getTarget() {
        return target;
    }

    void addDLightTargetExecutionEnviromentProvider(DLightTarget.ExecutionEnvVariablesProvider executionEnvProvider) {
        envProvider.add(executionEnvProvider);
    }

    DLightTarget.ExecutionEnvVariablesProvider getDLightTargetExecutionEnvProvider() {
        return envProvider;
    }

    /**
     * Do not call directly - use DLightSession.addDLightContextListener()
     */
    void setListeners(List<ExecutionContextListener> listeners) {
        synchronized (lock) {
            this.listeners = listeners;
        }
    }

    void validateTools() {
        validateTools(false);
    }

//    static int count = 0;
    void validateTools(boolean performRequiredActions) {
        DLightLogger.assertNonUiThread();

        synchronized (lock) {
            if (validationInProgress) {
                return;
            }
            validationInProgress = true;
        }

        Map<DLightTool, Future<ValidationStatus>> tasks =
                new HashMap<DLightTool, Future<ValidationStatus>>();

        Map<DLightTool, ValidationStatus> states =
                new HashMap<DLightTool, ValidationStatus>();

//        count++;

        for (final DLightTool tool : tools) {
//            System.out.printf("%d: VALIDATING TOOL: %s\n", count, tool.getName());
            ValidationStatus toolCurrentStatus = tool.getValidationStatus();

            states.put(tool, toolCurrentStatus);
//            System.out.printf("%d: CurrentStatus: %s\n", count, toolCurrentStatus.toString());

            tasks.put(tool, DLightExecutorService.submit(new Callable<ValidationStatus>() {

                public ValidationStatus call() throws Exception {
                    return tool.validate(target);
                }
            }, "Tool " + tool.getName() + " validation")); // NOI18N

//            System.out.printf("%d: Future for validation task: %s\n", count, tasks.get(tool).toString());
        }

        boolean changed = false;
        boolean willReiterate = true;

        while (willReiterate) {
            DLightTool[] toolsToValidate = tasks.keySet().toArray(new DLightTool[0]);
            willReiterate = false;

            for (final DLightTool tool : toolsToValidate) {
                Future<ValidationStatus> task = tasks.get(tool);

                try {
                    //TODO: Could use timeouts. Should we?
                    ValidationStatus toolNewStatus = task.get();

//                    System.out.printf("%d: Status of validation task %s: %s\n", count, tasks.toString(), toolNewStatus.toString());

                    boolean thisToolStateChanged = !toolNewStatus.equals(states.get(tool));

                    if (performRequiredActions) {
                        if (!toolNewStatus.isKnown()) {
                            Collection<AsynchronousAction> actions = toolNewStatus.getRequiredActions();

                            if (actions != null) {
                                for (AsynchronousAction a : actions) {
                                    try {
                                        a.invoke();
                                    } catch (Exception ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }

                            task = DLightExecutorService.submit(new Callable<ValidationStatus>() {

                                public ValidationStatus call() throws Exception {
                                    return tool.validate(target);
                                }
                            }, "Tool " + tool.getName() + " validation"); // NOI18N
                            toolNewStatus = task.get();
                            thisToolStateChanged = !toolNewStatus.equals(states.get(tool));
                        }

                        if (!toolNewStatus.isKnown() && thisToolStateChanged) {
                            states.put(tool, toolNewStatus);
                            tasks.put(tool, task);
                            willReiterate = true;
                        } else {
                            tasks.remove(tool);
                            states.remove(tool);
                        }
                    }

                    if (changed == false && thisToolStateChanged) {
                        changed = true;
                    }

                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        if (changed) {
            notifyListeners(new ExecutionContextEvent(this, Type.TOOLS_CHANGED_EVENT, this));
        }

        validationInProgress = false;

    }

    private void notifyListeners(final ExecutionContextEvent event) {
        ExecutionContextListener[] lls = null;

        synchronized (lock) {
            if (listeners == null) {
                return;
            }

            lls = listeners.toArray(new ExecutionContextListener[0]);
        }

        for (ExecutionContextListener l : lls) {
            l.contextChanged(event);
        }
    }

    List<Indicator> getIndicators() {
        ArrayList<Indicator> result = new ArrayList<Indicator>();
        for (DLightTool tool : tools) {
            result.addAll(DLightToolAccessor.getDefault().getIndicators(tool));
        }

        return result;
    }

    List<DLightTool> getTools() {
        return tools;
    }

    final class DLightTargetExecutionEnvProviderCollection implements ExecutionEnvVariablesProvider {

        private List<ExecutionEnvVariablesProvider> providers;

        DLightTargetExecutionEnvProviderCollection() {
            providers = new ArrayList<DLightTarget.ExecutionEnvVariablesProvider>();
        }

        void clear() {
            synchronized (this) {
                providers.clear();
            }
        }

        void add(DLightTarget.ExecutionEnvVariablesProvider provider) {
            synchronized (this) {
                providers.add(provider);
            }
        }

        public Map<String, String> getExecutionEnv(DLightTarget target) throws ConnectException {
            Map<String, String> env = new HashMap<String, String>();
            ExecutionEnvVariablesProvider[] pp = null;

            synchronized (this) {
                pp = providers.toArray(new ExecutionEnvVariablesProvider[0]);
            }

            for (ExecutionEnvVariablesProvider provider : pp) {
                env.putAll(provider.getExecutionEnv(target));
            }

            return env;
        }
    }
}
