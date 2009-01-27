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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.management.api.ExecutionContextEvent.Type;
import org.netbeans.modules.dlight.management.api.impl.DLightToolAccessor;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ObservableAction;
import org.openide.util.Exceptions;


final class ExecutionContext {
    private static final Object lock = new Object();
    private static final Logger log = DLightLogger.getLogger(ExecutionContext.class);
    private volatile boolean validationInProgress = false;
    private final DLightTarget target;
    private final List<DLightTool> tools = Collections.synchronizedList(new ArrayList<DLightTool>());
    private List<ExecutionContextListener> listeners = null;

    ExecutionContext(final DLightTarget target, final List<DLightTool> tools) {
        this.target = target;
        this.tools.addAll(tools);
    }

    void clear() {
    }

    DLightTarget getTarget() {
        return target;
    }

    /**
     * Do not call directly - use DLightSession.addDLightContextListener()
     */
    void setListeners(List<ExecutionContextListener> listeners) {
        this.listeners = listeners;
    }

    void validateTools() {
        validateTools(false);
    }

    void validateTools(boolean performRequiredActions) {
        DLightLogger.assertNonUiThread();
        
        synchronized (lock) {
            if (validationInProgress) {
                return;
            }
            validationInProgress = true;
        }

        Map<DLightTool, Future<ValidationStatus>> hash = new HashMap<DLightTool, Future<ValidationStatus>>();
        Map<DLightTool, ValidationStatus> shash = new HashMap<DLightTool, ValidationStatus>();

        for (DLightTool tool : tools.toArray(new DLightTool[0])) {
            ValidationStatus toolCurrentStatus = tool.getValidationStatus();
            hash.put(tool, tool.validate(target));
            shash.put(tool, toolCurrentStatus);
        }

        boolean changed = false;
        boolean willReiterate = true;

        while (willReiterate) {
            DLightTool[] toolsToValidate = hash.keySet().toArray(new DLightTool[0]);
            willReiterate = false;

            for (DLightTool tool : toolsToValidate) {
                Future<ValidationStatus> task = hash.get(tool);

                try {
                    //TODO: Could use timeouts. Should we?
                    ValidationStatus toolNewStatus = task.get();

                    boolean thisToolStateChanged = !toolNewStatus.equals(shash.get(tool));

                    if (performRequiredActions) {
                        if (!toolNewStatus.isValidated()) {
                            Collection<ObservableAction> actions =
                                toolNewStatus.getRequiredActions();
                            
                            if (actions != null) {
                                for (ObservableAction a : actions) {
                                    try {
                                        a.call();
                                    } catch (Exception ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }

                            task = tool.validate(target);
                            toolNewStatus = task.get();
                            thisToolStateChanged = !toolNewStatus.equals(shash.get(tool));
                        }

                        if (!toolNewStatus.isValidated() && thisToolStateChanged) {
                            shash.put(tool, toolNewStatus);
                            hash.put(tool, task);
                            willReiterate = true;
                        } else {
                            hash.remove(tool);
                            shash.remove(tool);
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

    private void notifyListeners(ExecutionContextEvent event) {
        if (listeners == null) {
            return;
        }

        for (ExecutionContextListener l : listeners.toArray(new ExecutionContextListener[0])) {
            l.contextChanged(event);
        }
    }

    List<Indicator> getIndicators() {
        ArrayList<Indicator> result = new ArrayList<Indicator>();
        if (tools != null) {
            for (DLightTool tool : tools) {
                result.addAll(DLightToolAccessor.getDefault().getIndicators(tool));
            }
        }

        return result;
    }

    List<DLightTool> getTools() {
        return tools;
    }
}
