/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.api.validation;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 * Validation result.
 * <p>
 * This class is not thread safe.
 * @since 1.20
 */
public final class ValidationResult {

    private final List<Message> errors = new ArrayList<Message>();
    private final List<Message> warnings = new ArrayList<Message>();


    /**
     * Return {@code true} if this validation contains any error.
     * @return {@code true} if this validation contains any error, {@code false} otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Get validation errors.
     * @return list of validation errors
     */
    public List<Message> getErrors() {
        return new ArrayList<Message>(errors);
    }

    /**
     * Return {@code true} if this validation contains any warning.
     * @return {@code true} if this validation contains any warning, {@code false} otherwise
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Get validation warnings.
     * @return list of validation warnings
     */
    public List<Message> getWarnings() {
        return new ArrayList<Message>(warnings);

    }

    /**
     * Add validation error.
     * @param error validation error to be added
     */
    public void addError(@NonNull Message error) {
        Parameters.notNull("error", error);
        errors.add(error);
    }

    /**
     * Add validation warning.
     * @param error validation warning to be added
     */
    public void addWarning(@NonNull Message warning) {
        Parameters.notNull("warning", warning);
        warnings.add(warning);
    }

    /**
     * Merge this validation result with another one. In other words,
     * add all errors and warnings from the given validation result
     * to this one.
     * @param otherResult validation result to be merged
     */
    public void merge(@NonNull ValidationResult otherResult) {
        Parameters.notNull("otherResult", otherResult);
        errors.addAll(otherResult.errors);
        warnings.addAll(otherResult.warnings);
    }

    //~ Inner classes

    /**
     * Validation message.
     */
    public static final class Message {

        private final String source;
        private final String message;


        /**
         * Create new validation message.
         * @param source source of the message, e.g. "siteRootFolder"
         * @param message message itself, e.g. "Invalid directory specified."
         */
        public Message(@NonNull String source, @NonNull String message) {
            Parameters.notNull("source", source);
            Parameters.notNull("message", message);
            this.source = source;
            this.message = message;
        }

        /**
         * Get source of the message, e.g. "siteRootFolder".
         * @return source of the message, e.g. "siteRootFolder"
         */
        public String getSource() {
            return source;
        }

        /**
         * Get message itself, e.g. "Invalid directory specified."
         * @return message itself, e.g. "Invalid directory specified.".
         */
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "ValidationMessage{source=" + source + ", message=" + message + '}'; // NOI18N
        }

    }

}
