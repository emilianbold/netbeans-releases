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
package org.netbeans.modules.php.composer.options;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.composer.commands.Composer;
import org.openide.util.NbBundle;

/**
 * Validator for Composer options.
 */
public final class ComposerOptionsValidator {

    private static final Pattern VENDOR_REGEX = Pattern.compile("^[a-z0-9-]+$"); // NOI18N
    private static final Pattern EMAIL_REGEX = Pattern.compile("^\\w+[\\.\\w\\-]*@\\w+[\\.\\w\\-]*\\.[a-z]{2,}$", Pattern.CASE_INSENSITIVE); // NOI18N

    private final List<Message> errors = new LinkedList<Message>();
    private final List<Message> warnings = new LinkedList<Message>();


    public void validate(String composerPath, String vendor, String authorName, String authorEmail) {
        validateComposerPath(composerPath);
        validateVendor(vendor);
        validateAuthorName(authorName);
        validateAuthorEmail(authorEmail);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<Message> getErrors() {
        return new ArrayList<Message>(errors);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<Message> getWarnings() {
        return new ArrayList<Message>(warnings);
    }

    private void validateComposerPath(String composerPath) {
        String warning = Composer.validate(composerPath);
        if (warning != null) {
            warnings.add(new Message("composerPath", warning)); // NOI18N
        }
    }

    @NbBundle.Messages("ComposerOptionsValidator.error.invalidVendor=Vendor is not valid (only lower-cased letters and \"-\" allowed).")
    void validateVendor(String vendor) {
        if (!VENDOR_REGEX.matcher(vendor).matches()) {
            errors.add(new Message("vendor", Bundle.ComposerOptionsValidator_error_invalidVendor())); // NOI18N
        }
    }

    @NbBundle.Messages("ComposerOptionsValidator.error.noAuthorName=Author name cannot be empty.")
    private void validateAuthorName(String authorName) {
        if (!StringUtils.hasText(authorName)) {
            errors.add(new Message("authorName", Bundle.ComposerOptionsValidator_error_noAuthorName())); // NOI18N
        }
    }

    @NbBundle.Messages("ComposerOptionsValidator.error.invalidAuthorEmail=Author e-mail is not valid.")
    void validateAuthorEmail(String authorEmail) {
        if (!StringUtils.hasText(authorEmail)
                || !EMAIL_REGEX.matcher(authorEmail).matches()) {
            errors.add(new Message("authorEmail", Bundle.ComposerOptionsValidator_error_invalidAuthorEmail())); // NOI18N
        }
    }

    //~ Inner classes

    public static final class Message {

        private final String source;
        private final String message;

        public Message(String source, String message) {
            this.source = source;
            this.message = message;
        }

        public String getSource() {
            return source;
        }

        public String getMessage() {
            return message;
        }

    }

}
