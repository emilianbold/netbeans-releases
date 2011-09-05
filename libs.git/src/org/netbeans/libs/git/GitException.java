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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git;

import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class GitException extends Exception {

    public GitException (Throwable t) {
        super(t);
    }

    public GitException (String message) {
        super(message);
    }

    public GitException (String message, Throwable ex) {
        super(message, ex);
    }

    public static class MissingObjectException extends GitException {
        private final String objectName;
        private final GitObjectType objectType;

        public MissingObjectException (String objectName, GitObjectType objectType) {
            super(NbBundle.getMessage(GitException.class, "MSG_Exception_ObjectDoesNotExist", new Object[] { objectType.toString(), objectName })); //NOI18N
            this.objectName = objectName;
            this.objectType = objectType;
        }

        public MissingObjectException (String objectName, GitObjectType objectType, Throwable ex) {
            super(NbBundle.getMessage(GitException.class, "MSG_Exception_ObjectDoesNotExist", new Object[] { objectType.toString(), objectName }), ex); //NOI18N
            this.objectName = objectName;
            this.objectType = objectType;
        }

        public String getObjectName () {
            return objectName;
        }

        public GitObjectType getObjectType () {
            return objectType;
        }
    }
    
    public static class CheckoutConflictException extends GitException {
        private final String[] conflicts;

        public CheckoutConflictException (String[] conflicts, Throwable cause) {
            super(NbBundle.getMessage(GitException.class, "MSG_Exception_CheckoutConflicts"), cause);
            this.conflicts = conflicts;
        }

        public CheckoutConflictException (String[] conflicts) {
            this(conflicts, null);
        }

        public String[] getConflicts () {
            return conflicts;
        }
    }

    public static class AuthorizationException extends GitException {
        private final String repositoryUrl;

        public AuthorizationException (String repositoryUrl, String message, Throwable t) {
            super(message, t);
            this.repositoryUrl = repositoryUrl;
        }

        public AuthorizationException (String message, Throwable t) {
            this(null, message, t);
        }

        /**
         * May be null
         * @return 
         */
        public String getRepositoryUrl () {
            return repositoryUrl;
        }
    }

    public static class RefUpdateException extends GitException {
        private final GitRefUpdateResult result;

        public RefUpdateException (String message, GitRefUpdateResult result) {
            super(message);
            this.result = result;
        }

        public GitRefUpdateResult getResult () {
            return result;
        }
    }

    public static class NotMergedException extends GitException {
        private final String unmergedRevision;

        public NotMergedException (String unmergedRevision) {
            super(unmergedRevision + " has not been fully merged yet");
            this.unmergedRevision = unmergedRevision;
        }
        
        public String getUnmergedRevision () {
            return unmergedRevision;
        }
    }
}
