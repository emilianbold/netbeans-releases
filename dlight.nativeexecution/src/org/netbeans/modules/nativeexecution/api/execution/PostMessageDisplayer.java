/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.api.execution;

import java.util.concurrent.TimeUnit;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.ProcessStatusEx;
import org.openide.util.NbBundle;

/**
 *
 * @author ak119685
 */
public interface PostMessageDisplayer {

    public String getPostMessage(NativeProcess process, long time);

    public String getPostStatusString(NativeProcess process);

    public String getPostMessage(NativeProcess.State state, int rc, long time);

    public final static class Default implements PostMessageDisplayer {

        private final String actionName;

        public Default(String actionName) {
            this.actionName = actionName;
        }

        @Override
        public String getPostMessage(State state, int rc, long timeMilliseconds) {
            StringBuilder res = new StringBuilder();

            switch (state) {
                case ERROR:
                    res.append(NbBundle.getMessage(PostMessageDisplayer.class, "FAILED", actionName.toUpperCase()));
                    break;
                case CANCELLED:
                    res.append(NbBundle.getMessage(PostMessageDisplayer.class, "TERMINATED", actionName.toUpperCase()));
                    break;
                case FINISHED:
                    if (rc == 0) {
                        res.append(NbBundle.getMessage(PostMessageDisplayer.class, "SUCCESSFUL", actionName.toUpperCase()));
                    } else {
                        res.append(NbBundle.getMessage(PostMessageDisplayer.class, "FAILED", actionName.toUpperCase()));
                    }
                    break;
                default:
                // should not happen
                }

            res.append(" ("); // NOI18N

            if (rc != 0) {
                res.append(NbBundle.getMessage(PostMessageDisplayer.class, "EXIT_VALUE", rc));
                res.append(", "); // NOI18N
            }

            res.append(NbBundle.getMessage(PostMessageDisplayer.class, "TOTAL_TIME", formatTime(timeMilliseconds)));

            res.append(')');

            return res.toString();
        }

        @Override
        public String getPostStatusString(NativeProcess process) {
            ProcessStatusEx exitStatusEx = process.getExitStatusEx();

            if (exitStatusEx != null) {
                return NbBundle.getMessage(PostMessageDisplayer.class, "MSG_FINISHED", actionName); // NOI18N
            }

            State state = process.getState();
            switch (state) {
                case ERROR:
                    return NbBundle.getMessage(PostMessageDisplayer.class, "MSG_FAILED", actionName); // NOI18N
                case CANCELLED:
                    return NbBundle.getMessage(PostMessageDisplayer.class, "MSG_TERMINATED", actionName); // NOI18N
                case FINISHED:
                    if (process.exitValue() == 0) {
                        return NbBundle.getMessage(PostMessageDisplayer.class, "MSG_SUCCESSFUL", actionName);
                    } else {
                        return NbBundle.getMessage(PostMessageDisplayer.class, "MSG_FAILED", actionName);
                    }
                default:
                    // should not happen
                    return ""; // NOI18N
            }
        }

        private static String formatTime(long millis) {
            if (millis == 0) {
                return " 0" + NbBundle.getMessage(PostMessageDisplayer.class, "MILLISECOND"); // NOI18N
            }

            StringBuilder res = new StringBuilder();
            long seconds = millis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            if (hours > 0) {
                res.append(" ").append(hours).append(NbBundle.getMessage(PostMessageDisplayer.class, "HOUR")); // NOI18N
            }

            if (minutes > 0) {
                res.append(" ").append(minutes - hours * 60).append(NbBundle.getMessage(PostMessageDisplayer.class, "MINUTE")); // NOI18N
            }

            if (seconds > 0) {
                res.append(" ").append(seconds - minutes * 60).append(NbBundle.getMessage(PostMessageDisplayer.class, "SECOND")); // NOI18N
            } else {
                res.append(" ").append(millis).append(NbBundle.getMessage(PostMessageDisplayer.class, "MILLISECOND")); // NOI18N
            }

            return res.toString();
        }

        @Override
        public String getPostMessage(NativeProcess process, long time) {
            State state = process.getState();
            StringBuilder res = new StringBuilder();
            ProcessStatusEx statusEx = process.getExitStatusEx();
            int status = process.exitValue();

            if (statusEx != null) {
                if (state == State.ERROR) {
                    return NbBundle.getMessage(PostMessageDisplayer.class, "FAILED", actionName.toUpperCase()); // NOI18N
                }

                res.append(NbBundle.getMessage(PostMessageDisplayer.class, "FINISHED", actionName.toUpperCase())); // NOI18N

                res.append("; "); // NOI18N
                if (statusEx.ifExited()) {
                    res.append(NbBundle.getMessage(PostMessageDisplayer.class, "EXIT_VALUE", statusEx.getExitCode())); // NOI18N
                } else {
                    res.append(statusEx.termSignal());
                    if (statusEx.ifCoreDump()) {
                        res.append("; "); // NOI18N
                        res.append(NbBundle.getMessage(PostMessageDisplayer.class, "COREDUMPED")); // NOI18N
                    }
                }

                res.append("; "); // NOI18N
                res.append(NbBundle.getMessage(PostMessageDisplayer.class, "TOTAL_TIME_EX", // NOI18N
                        formatTime(statusEx.realTime(TimeUnit.MILLISECONDS)),
                        formatTime(statusEx.sysTime(TimeUnit.MILLISECONDS)),
                        formatTime(statusEx.usrTime(TimeUnit.MILLISECONDS))));
                return res.toString();

            }

            return getPostMessage(state, status, time);
        }
    }
}
