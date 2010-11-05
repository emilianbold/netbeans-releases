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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.management.api;

import java.net.ConnectException;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget.ExecutionEnvVariablesProvider;
import org.netbeans.modules.dlight.api.execution.Validateable;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.impl.DLightToolAccessor;
import org.netbeans.modules.dlight.management.api.ExecutionContextEvent.Type;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.impl.DataCollectorProvider;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.openide.util.Exceptions;

final class ExecutionContext {

    private final DLightTarget target;
    private final DLightTargetExecutionEnvProviderCollection envProvider;
    private final List<DLightTool> tools;
    private final CopyOnWriteArrayList<ExecutionContextListener> listeners;
    private final AtomicBoolean validationInProgress = new AtomicBoolean(false);
    private final DLightConfiguration dlightConfiguration;

    ExecutionContext(final DLightTarget target, DLightConfiguration dlightConfiguration) {
        this.target = target;
        this.dlightConfiguration = dlightConfiguration;
        listeners = new CopyOnWriteArrayList<ExecutionContextListener>();
        tools = Collections.unmodifiableList(dlightConfiguration.getToolsSet());        
        envProvider = new DLightTargetExecutionEnvProviderCollection();
    }

    void clear() {
        envProvider.clear();
    }

    DLightTarget getTarget() {
        return target;
    }

    DLightConfiguration getDLightConfiguration() {
        return dlightConfiguration;
    }

    void addDLightTargetExecutionEnviromentProvider(DLightTarget.ExecutionEnvVariablesProvider executionEnvProvider) {
        envProvider.add(executionEnvProvider);
    }

    DLightTarget.ExecutionEnvVariablesProvider getDLightTargetExecutionEnvProvider() {
        return envProvider;
    }

    void validateTools() {
        validateTools(false);
    }

    private void validateTools(boolean performRequiredActions, List<DLightTool> toolsToValidate) {
        DLightLogger.assertNonUiThread();

        if (!validationInProgress.compareAndSet(false, true)) {
            return;
        }

        Map<Validateable<DLightTarget>, Future<ValidationStatus>> tasks =
                new HashMap<Validateable<DLightTarget>, Future<ValidationStatus>>();

        Map<Validateable<DLightTarget>, ValidationStatus> states =
                new HashMap<Validateable<DLightTarget>, ValidationStatus>();

        List<DataCollector<?>> collectors = new ArrayList<DataCollector<?>>();
        if (getDLightConfiguration().getConfigurationOptions(false).areCollectorsTurnedOn()) {
            //there is no need to check tools for turned off tools
            for (DLightTool tool : toolsToValidate) {
                List<DataCollector<?>> toolCollectors = getDLightConfiguration().getConfigurationOptions(false).getCollectors(tool);
                //TODO: no algorithm here:) should be better
                for (DataCollector<?> c : toolCollectors) {
                    if (!collectors.contains(c)) {
                        collectors.add(c);
                    }
                }
            }
        }

        List<IndicatorDataProvider<?>> idps = new ArrayList<IndicatorDataProvider<?>>();

        for (DLightTool tool : toolsToValidate) {
            // Try to subscribe every IndicatorDataProvider to every Indicator
            //there can be the situation when IndicatorDataProvider is collector
            //and not attacheble
            List<IndicatorDataProvider<?>> tool_idps = getDLightConfiguration().getConfigurationOptions(false).getIndicatorDataProviders(tool);

            for (IndicatorDataProvider<?> idp : tool_idps) {
                if (idp instanceof DataCollector<?>) {
                    if (collectors.contains((DataCollector<?>) idp)) {
                        // I.e. this provider will be enabled
                        continue;
                    }
                }

                if (idps.contains(idp)) {
                    continue;
                }

                idps.add(idp);
            }
        }

        for (final DataCollector<?> c : collectors) {
            ValidationStatus collectorCurrentStatus = c.getValidationStatus();
            states.put(c, collectorCurrentStatus);
            tasks.put(c, DLightExecutorService.submit(new Callable<ValidationStatus>() {

                @Override
                public ValidationStatus call() throws Exception {
                    return c.validate(target);
                }
            }, "Data Collector " + c.getName() + " validation")); // NOI18N
        }

        for (final IndicatorDataProvider<?> idp : idps) {
            ValidationStatus collectorCurrentStatus = idp.getValidationStatus();
            states.put(idp, collectorCurrentStatus);
            tasks.put(idp, DLightExecutorService.submit(new Callable<ValidationStatus>() {

                @Override
                public ValidationStatus call() throws Exception {
                    return idp.validate(target);
                }
            }, "Indicator Data Provider " + idp.getName() + " validation")); // NOI18N
        }

        boolean changed = false;
        boolean willReiterate = true;

        while (willReiterate) {
            List<Validateable<DLightTarget>> toValidate = new ArrayList<Validateable<DLightTarget>>(tasks.keySet());
            willReiterate = false;

            for (final Validateable<DLightTarget> validatable : toValidate) {
                Future<ValidationStatus> task = tasks.get(validatable);

                try {
                    //TODO: Could use timeouts. Should we?
                    ValidationStatus vNewStatus = task.get();

//                    System.out.printf("%d: Status of validation task %s: %s\n", count, tasks.toString(), toolNewStatus.toString());

                    boolean thisValidatableStateChaged = !vNewStatus.equals(states.get(validatable));

                    if (performRequiredActions) {
                        if (!vNewStatus.isKnown()) {
                            Collection<AsynchronousAction> actions = vNewStatus.getRequiredActions();

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

                                @Override
                                public ValidationStatus call() throws Exception {
                                    return validatable.validate(target);
                                }
                            }, validatable + " validation"); // NOI18N
                            vNewStatus = task.get();
                            thisValidatableStateChaged = !vNewStatus.equals(states.get(validatable));
                        }

                        if (!vNewStatus.isKnown() && thisValidatableStateChaged) {
                            states.put(validatable, vNewStatus);
                            tasks.put(validatable, task);
                            willReiterate = true;
                        } else {
                            tasks.remove(validatable);
                            states.remove(validatable);
                        }
                    }

                    if (changed == false && thisValidatableStateChaged) {
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

        validationInProgress.set(false);
    }

    void validateTools(boolean performRequiredActions) {
        validateTools(performRequiredActions, getTools());
    }

    private void notifyListeners(final ExecutionContextEvent event) {
        for (ExecutionContextListener l : listeners) {
            l.contextChanged(event);
        }
    }

    List<Indicator<?>> getIndicators() {
        ArrayList<Indicator<?>> result = new ArrayList<Indicator<?>>();
        Collection<String> activeToolNames = getDLightConfiguration().getConfigurationOptions(false).getActiveToolNames();
        for (DLightTool tool : tools) {
            if (activeToolNames == null || activeToolNames.contains(tool.getID())) {
                result.addAll(DLightToolAccessor.getDefault().getIndicators(tool));
            }
        }

        return result;
    }

    DLightTool getToolByName(String toolName) {
        Collection<String> activeToolNames = getDLightConfiguration().getConfigurationOptions(false).getActiveToolNames();
        for (DLightTool tool : tools) {
            if (activeToolNames == null || activeToolNames.contains(tool.getID()) && tool.getName().equals(toolName)) {
                return tool;
            }
        }
        return null;
    }

    DLightTool getToolByID(String toolId) {
        Collection<String> activeToolNames = getDLightConfiguration().getConfigurationOptions(false).getActiveToolNames();
        for (DLightTool tool : tools) {
            if (activeToolNames == null || activeToolNames.contains(toolId) && tool.getID().equals(toolId)) {
                return tool;
            }
        }
        return null;
    }

    List<DLightTool> getTools() {
        List<DLightTool> result = new ArrayList<DLightTool>();
        Collection<String> activeToolNames = getDLightConfiguration().getConfigurationOptions(false).getActiveToolNames();
        for (DLightTool tool : tools) {
            if ((tool.isEnabled() && (activeToolNames == null || activeToolNames.contains(tool.getID())))
                    || (!tool.isEnabled() && activeToolNames != null && activeToolNames.contains(tool.getID()))) {
                result.add(tool);
            }
        }

        return result;
    }

    void addListener(ExecutionContextListener listener) {
        listeners.addIfAbsent(listener);
    }

    void removeListener(ExecutionContextListener listener) {
        listeners.remove(listener);
    }

    private static final class DLightTargetExecutionEnvProviderCollection implements ExecutionEnvVariablesProvider {

        private Set<ExecutionEnvVariablesProvider> providers;

        DLightTargetExecutionEnvProviderCollection() {
            providers = new HashSet<DLightTarget.ExecutionEnvVariablesProvider>();
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

        @Override
        public void setupEnvironment(DLightTarget target, MacroMap env) throws ConnectException {
            ExecutionEnvVariablesProvider[] pp = null;

            synchronized (this) {
                pp = providers.toArray(new ExecutionEnvVariablesProvider[0]);
            }

            for (ExecutionEnvVariablesProvider provider : pp) {
                provider.setupEnvironment(target, env);
            }
        }
    }
}
