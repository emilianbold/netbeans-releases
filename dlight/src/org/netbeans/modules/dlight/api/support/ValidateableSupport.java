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
package org.netbeans.modules.dlight.api.support;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.dlight.api.execution.Validateable;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.util.AsynchronousAction;
import org.openide.util.Exceptions;

/**
 * This class can be used for example to validate some
 * validatable with invoking all required User actions
 * @param <T> object to validate with
 */
public final class ValidateableSupport<T> {

    private final Validateable<T> validatable;
    private ValidationStatus currentStatus;

    public ValidateableSupport(Validateable<T> validatable) {
        this.validatable = validatable;
        currentStatus = validatable.getValidationStatus();
    }

    public Future<ValidationStatus> asyncValidate(final T target, final boolean performRequiredActions) {
        Future<ValidationStatus> task = DLightExecutorService.submit(new Callable<ValidationStatus>() {

            public ValidationStatus call() throws Exception {
                return syncValidate(target, performRequiredActions);
            }
        }, "ValidateableSupport asyncValidate" +  validatable + " validation"); //NOI18N
        return task;
    }

    /**
     * This method will be invoked in sync
     * @param target
     * @param performRequiredActions
     * @return 
     */
    public ValidationStatus syncValidate(final T target, final boolean performRequiredActions) {
        boolean willReiterate = true;
        while (willReiterate) {
            willReiterate = false;
            try {

                Future<ValidationStatus> task = DLightExecutorService.submit(new Callable<ValidationStatus>() {

                    public ValidationStatus call() throws Exception {
                        return validatable.validate(target);
                    }
                }, validatable + " validation"); // NOI18N
                ValidationStatus vNewStatus = task.get();
                boolean thisValidatableStateChaged = !vNewStatus.equals(currentStatus);
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

                            public ValidationStatus call() throws Exception {
                                return validatable.validate(target);
                            }
                        }, "ValidateableSupport syncValidate" +  validatable + " validation"); // NOI18N
                        vNewStatus = task.get();
                        thisValidatableStateChaged = !vNewStatus.equals(currentStatus);
                    }

                    if (!vNewStatus.isKnown() && thisValidatableStateChaged) {
                        currentStatus = vNewStatus;
                        willReiterate = true;
                    }
                }

            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return currentStatus;
    }
}
