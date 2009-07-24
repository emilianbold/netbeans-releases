/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.indicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.Validateable;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.support.ValidateableSupport;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.spi.impl.IndicatorRepairActionProviderAccessor;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
public final class IndicatorRepairActionProvider implements ValidationListener {

    private final DLightConfiguration configuration;
    private final DLightTool currentTool;
    private final DLightTarget targetToRepairFor;
    private ValidationStatus currentStatus;
    private final List<IndicatorDataProvider<?>> toReValidate;
    private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
    private final Object listenersLock = new String("IndicatorRepairActionProvider.Listeners"); // NOI18N
    private Future<Boolean> repairTask;

    static {
        IndicatorRepairActionProviderAccessor.setDefault(new IndicatorRepairActionProviderAccessorImpl());
    }

    private IndicatorRepairActionProvider(DLightConfiguration configuration, DLightTool tool, DLightTarget targetToRepairFor) {
        this.configuration = configuration;
        this.currentTool = tool;
        this.targetToRepairFor = targetToRepairFor;
        // TODO: FIXME.
        // hotfix to avoid NPE ...
        this.currentStatus = ValidationStatus.invalidStatus(getMessage("IndicatorDataProviderNotFound")); // NOI18N
        List<IndicatorDataProvider<?>> providers = configuration.getConfigurationOptions(false).getIndicatorDataProviders(currentTool);
        toReValidate = new ArrayList<IndicatorDataProvider<?>>();
        for (IndicatorDataProvider idp : providers) {
            if (!idp.getValidationStatus().isKnown() || (idp.getValidationStatus().isKnown() && idp.getValidationStatus().isInvalid())) {
                idp.addValidationListener(this);
                toReValidate.add(idp);
                currentStatus = idp.getValidationStatus();
            }
        }
    }

    void addChangeListener(ChangeListener l) {
        synchronized (listenersLock) {
            if (!changeListeners.contains(l)) {
                changeListeners.add(l);
            }
        }
    }

    void removeChangeListener(ChangeListener l) {
        synchronized (listenersLock) {
            changeListeners.remove(l);

        }
    }

    private void notifyChangeListeners() {
        synchronized (listenersLock) {
            ChangeEvent evt = new ChangeEvent(this);
            ChangeListener[] listeners = changeListeners.toArray(new ChangeListener[0]);
            for (ChangeListener l : listeners) {
                l.stateChanged(evt);
            }
        }
    }

    public final String getMessage(ValidationStatus status) {
        if (status.isValid()) {
            String message = getMessage("NextRun"); // NOI18N
            if (!configuration.getConfigurationOptions(false).areCollectorsTurnedOn()) {
                message = getMessage("DataCollectorDisabled"); // NOI18N
            }
            return message;

        }
        return status.getReason();
    }

    public boolean isValid() {
        return currentStatus.isValid();
    }

    public boolean needRepair() {
        return !currentStatus.isKnown();
    }

    public String getReason() {
        return currentStatus.getReason();
    }

    public final ValidationStatus getValidationStatus() {
        return currentStatus;
    }

    public Future<Boolean> asyncRepair() {
        synchronized (this) {
            if (repairTask == null || repairTask.isDone()) {
                repairTask = DLightExecutorService.submit(new Callable<Boolean>() {

                    public Boolean call() throws Exception {
                        for (IndicatorDataProvider<?> idp : toReValidate) {
                            final ValidateableSupport<DLightTarget> support = new ValidateableSupport<DLightTarget>(idp);
                            final Future<ValidationStatus> taskStatus = support.asyncValidate(targetToRepairFor, true);
                            Future<Boolean> result = DLightExecutorService.submit(new Callable<Boolean>() {

                                public Boolean call() throws Exception {
                                    final ValidationStatus status = taskStatus.get();
                                    UIThread.invoke(new Runnable() {

                                        public void run() {
                                            currentStatus = status;
                                        //   updateUI(c);
                                        }
                                    });
                                    return status.isKnown();
                                }
                            }, "IndicatorRepairActionProvider task for " + idp.getName());//NOI18N
                            try {
                                //NOI18N
                                if (result.get().booleanValue()) {
                                    return true;
                                }
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        return true;
                    }
                }, "IndicatorRepairActionProvider asyncRepair"); //NOI18N
            }
        }
        return repairTask;
    }

    public void validationStateChanged(Validateable source, ValidationStatus oldStatus, ValidationStatus newStatus) {
        if (newStatus.isKnown()) {
            source.removeValidationListener(this);
            currentStatus = newStatus;
            notifyChangeListeners();
        }
    }

    private static class IndicatorRepairActionProviderAccessorImpl extends IndicatorRepairActionProviderAccessor {

        @Override
        public IndicatorRepairActionProvider createNew(DLightConfiguration configuration, DLightTool tool, DLightTarget targetToRepairFor) {
            return new IndicatorRepairActionProvider(configuration, tool, targetToRepairFor);
        }
    }

    private static String getMessage(String name) {
        return NbBundle.getMessage(IndicatorRepairActionProvider.class, name);
    }
}
