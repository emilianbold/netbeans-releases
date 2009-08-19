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
package org.netbeans.modules.nativeexecution.sps.impl;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.acl.NotOwnerException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CancellationException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.sps.impl.RequestPrivilegesTask.RequestPrivilegesTaskParams;
import org.netbeans.modules.nativeexecution.support.Computable;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.ui.GrantPrivilegesDialog;
import org.openide.util.NbBundle;

public final class RequestPrivilegesTask implements Computable<RequestPrivilegesTaskParams, Boolean> {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private WeakReference<GrantPrivilegesDialog> dialogRef = null;

    public Boolean compute(RequestPrivilegesTaskParams args) throws InterruptedException {
        final ProgressHandle ph = ProgressHandleFactory.createHandle(
                loc("TaskPrivilegesSupport_Progress_RequestPrivileges")); // NOI18N

        Process ppriv = null;

        try {
            ph.start();

            // An attempt to grant privileges using pfexec
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(args.support.getExecEnv());
            npb.setExecutable("/bin/pfexec").setArguments("/bin/ppriv", "-s", // NOI18N
                    "I+" + args.privilegesString, args.support.getPID()); // NOI18N

            ppriv = npb.call();
            int result = ppriv.waitFor();

            if (result == 0) {
                // pfexec succeeded ...
                return Boolean.TRUE;
            }

            if (!args.askForPassword) {
                return Boolean.FALSE;
            }

            if (dialogRef == null || dialogRef.get() == null) {
                dialogRef = new WeakReference<GrantPrivilegesDialog>(
                        new GrantPrivilegesDialog());
            }

            GrantPrivilegesDialog dialog = dialogRef.get();

            while (true) {
                if (dialog.askPassword()) {
                    try {
                        char[] clearPassword = dialog.getPassword();
                        args.support.requestPrivileges(args.requestedPrivileges, dialog.getUser(), clearPassword);
                        Arrays.fill(clearPassword, (char) 0);
                        dialog.clearPassword();
                        return Boolean.TRUE;
                    } catch (NotOwnerException ex) {
                        // wrong password or not enough privileges...
                        // Continue with password requests...
                    }
                } else {
                    throw new CancellationException();
                }
            }
        } catch (IOException ex) {
            log.fine("IOException in RequestPrivilegesTask : " + ex.toString()); // NOI18N
        } finally {
            ph.finish();
        }

        return Boolean.FALSE;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(SPSCommonImpl.class, key, params);
    }

    public static class RequestPrivilegesTaskParams {

        final Collection<String> requestedPrivileges;
        final boolean askForPassword;
        final private String privilegesString;
        final SPSCommonImpl support;

        public RequestPrivilegesTaskParams(
                SPSCommonImpl support,
                Collection<String> requestedPrivileges,
                boolean askForPassword) {
            this.support = support;
            this.requestedPrivileges = requestedPrivileges;
            this.askForPassword = askForPassword;

            StringBuffer sb = new StringBuffer();

            for (String priv : requestedPrivileges) {
                sb.append(priv).append(","); // NOI18N
            }

            privilegesString = sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RequestPrivilegesTaskParams)) {
                throw new IllegalArgumentException();
            }

            RequestPrivilegesTaskParams o = (RequestPrivilegesTaskParams) obj;

            return o.askForPassword == askForPassword &&
                    o.support == support &&
                    o.requestedPrivileges.containsAll(requestedPrivileges) &&
                    requestedPrivileges.containsAll(o.requestedPrivileges);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + (this.askForPassword ? 1 : 0);
            hash = 79 * hash + (this.support != null ? this.support.hashCode() : 0);
            return hash;
        }
    }
}
