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
package org.netbeans.modules.dlight.api.execution;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.nativeexecution.api.ObservableAction;

public final class ValidationStatus {

    public static ValidationStatus validStatus =
            new ValidationStatus(true, true, "OK", null);
    public static ValidationStatus initialStatus =
            new ValidationStatus(false, false, "Initial", null);


    private boolean validated = false;
    private boolean isValid = false;
    private String reason = "";
    private Collection<ObservableAction> requiredActions = 
            new ArrayList<ObservableAction>();

    private ValidationStatus(
            final boolean validated,
            final boolean isValid,
            final String reason,
            final Collection<ObservableAction> requiredActions) {
        this.isValid = isValid;
        this.validated = validated;
        this.reason = reason == null ? "" : reason; // NOI18N
        if (requiredActions != null) {
            this.requiredActions.addAll(requiredActions);
        }
    }

    public static ValidationStatus unknownStatus(final String reason,
            final Collection<ObservableAction> requiredActions) {
        return new ValidationStatus(false, false, reason, requiredActions);
    }
    public static ValidationStatus unknownStatus(final String reason,
            final ObservableAction requiredAction) {
        ArrayList<ObservableAction> actions = new ArrayList<ObservableAction>();
        actions.add(requiredAction);
        return new ValidationStatus(false, false, reason, actions);

    }

    public static ValidationStatus invalidStatus(final String reason) {
        return new ValidationStatus(true, false, reason, null);
    }

    /**
     *
     * @return
     */
    public String getReason() {
        return reason;
    }

    public ValidationStatus merge(final ValidationStatus status) {
        if (this.isInvalid()) {
            // this was validated and is INVALID -> merged is invalid (just == this)
            // TODO: merge reasons?
            return this;
        }

        if (status.isInvalid()) {
            // status was validated and is INVALID -> merged is invalid (just == status)
            // TODO: merge reasons?
            return status;
        }

        if (this.isValid() && status.isValid()) {
            return this;
        }

        if (this.isValid()) {
            // here status is not validated -> merge is the status
            return status.equals(initialStatus) ? this : status;
        }

        if (status.isValid()) {
            // here this is not validated -> merge is this
            return this.equals(initialStatus) ? status : this;
        }

        // here both are not validated... do merge!
        Collection<ObservableAction> mergedActions =
                new ArrayList<ObservableAction>();
        mergedActions.addAll(this.requiredActions);
        mergedActions.addAll(status.requiredActions);
        
        StringBuffer mergedReason = new StringBuffer();
        
        if (this.equals(initialStatus)) {
            mergedReason.append(status.reason);
        } else if (status.equals(initialStatus)) {
            mergedReason.append(this.reason);
        } else {
            mergedReason.append(this.reason).append("; ").append(status.reason);
        }

        ValidationStatus result = new ValidationStatus(false, false,
                mergedReason.toString(),
                mergedActions);

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValidationStatus)) {
            return false;
        }

        ValidationStatus st = (ValidationStatus) obj;

        if (!st.reason.equals(reason)) {
            return false;
        }

        if (st.validated != validated) {
            return false;
        }

        if (validated == true) {
            if (st.isValid != isValid) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.validated ? 1 : 0);
        hash = 97 * hash + (this.isValid ? 1 : 0);
        hash = 97 * hash + (this.reason != null ? this.reason.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        if (!validated) {
            return "UNKNOWN: " + reason; // NOI18N
        } else {
            if (isValid) {
                return "OK"; // NOI18N
            } else {
                return "INVALID: " + reason; // NOI18N
            }
        }
    }

    public Collection<ObservableAction> getRequiredActions() {
        return requiredActions;
    }

    public boolean isValid() {
        return validated && isValid;
    }

    public boolean isInvalid() {
        return validated && !isValid;
    }

    public boolean isValidated() {
        return validated;
    }
}