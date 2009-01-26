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
import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ObservableAction;

/**
 *
 */
public interface Validateable<T> {

    public enum ValidationState {

        UNKNOWN,
        NOT_VALID,
        VALID
    }

    public static class ValidationStatus {
        public static final ValidationStatus NOT_VALIDATED = new ValidationStatus(ValidationState.UNKNOWN, "Not yet validated");
        public static final ValidationStatus NOT_VALID = new ValidationStatus(ValidationState.NOT_VALID, null);
        public static final ValidationStatus VALID = new ValidationStatus(ValidationState.VALID, null);
        private ValidationState state;
        private String reason;
        private List<ObservableAction> requiredActions;

        public ValidationStatus(ValidationState state, String reason) {
            this(state, reason, null);
        }

        public ValidationStatus(final ValidationState state, final String reason, final ObservableAction requiredAction) {
            this.state = state;
            this.reason = reason == null ? "-" : reason; // NOI18N
            
            if (requiredAction != null) {
                this.requiredActions = new ArrayList<ObservableAction>();
                this.requiredActions.add(requiredAction);
            }
        }

        public ValidationState getState() {
            return state;
        }

        public String getReason() {
            return reason;
        }

        public ValidationStatus merge(ValidationStatus status) {
            ValidationStatus merged = new ValidationStatus(null, null);

            if (this == NOT_VALIDATED) {
                merged.state = status.state;
                merged.reason = status.reason;
                merged.requiredActions = status.requiredActions;
            } else {
                merged.state = state;
                merged.reason = reason;
                merged.requiredActions = requiredActions;

                switch (state) {
                    case NOT_VALID:
                        switch (status.state) {
                            case VALID:
                            case UNKNOWN:
                                // Just skip ...
                                break;
                            case NOT_VALID:
                                if (status.reason != null) {
                                    merged.reason = reason.concat(status.reason).concat("; ");
                                }
                                break;
                        }
                        break;

                    case VALID:
                    case UNKNOWN:
                        switch (status.state) {
                            case VALID:
                                break;
                            case UNKNOWN:
                            case NOT_VALID:
                                merged.state = status.state;
                                if (status.reason != null && !"-".equals(status.reason)) {
                                    merged.reason = (merged.reason == null) ? status.reason : merged.reason.concat(status.reason).concat("; ");
                                }
                                if (merged.requiredActions == null) {
                                    merged.requiredActions = status.requiredActions;
                                } else {
                                    if (status.requiredActions != null) {
                                        merged.requiredActions.addAll(status.requiredActions);
                                    }
                                }
                                break;
                        }
                        break;
                }
            }

            return merged;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ValidationStatus) {
                ValidationStatus st = (ValidationStatus)obj;
                return st.state.equals(state) && st.reason.equals(reason);
            }

            return false;
        }

        public boolean stateEquals(ValidationStatus obj) {
            return obj.state.equals(state);
        }

        @Override
        public String toString() {
            return "" + (state == null ? "null" : state.toString()) + ": " + (reason == null ? "null" : reason);
        }

        public ObservableAction[] getRequiredActions() {
            return requiredActions == null ? new ObservableAction[0] : requiredActions.toArray(new ObservableAction[0]);
        }

        public boolean isOK() {
            return this.stateEquals(ValidationStatus.VALID);
        }

        public boolean isUnknown() {
            return state.equals(ValidationState.UNKNOWN);
        }

    }

    public Future<ValidationStatus> validate(T objectToValidate);
    public void invalidate();
    public ValidationStatus getValidationStatus();
    public void addValidationListener(ValidationListener listener);
    public void removeValidationListener(ValidationListener listener);
}
