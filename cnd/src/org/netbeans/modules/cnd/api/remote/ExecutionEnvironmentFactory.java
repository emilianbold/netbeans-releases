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

package org.netbeans.modules.cnd.api.remote;

import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * A factory for ExecutionEnvironment.
 *
 * The purpose is as follows
 * 1) share a single local execurion environment
 * 2) probably remote executione environments as well
 * 3) during transitional period,
 * transform the user@host string to ExecutionEnvironment
 *
 * I guess the (3) will die some time
 * and the class will be moved to
 * org.netbeans.modules.nativeexecution.api
 *
 * @author Vladimir Kvashin
 */
public class ExecutionEnvironmentFactory {

    private static final ExecutionEnvironment LOCAL = new ExecutionEnvironment();
    public static final int DEFAULT_PORT = Integer.getInteger("cnd.remote.port", 22); //NOI18N

    /** prevents instantiation */
    private ExecutionEnvironmentFactory() {
    }

    public static ExecutionEnvironment getLocalExecutionEnvironment() {
        return LOCAL;
    }

    public static ExecutionEnvironment getExecutionEnvironment(String user, String host) {
        return getExecutionEnvironment(user, host, DEFAULT_PORT);
    }

    public static ExecutionEnvironment getExecutionEnvironment(String user, String host, int port) {
        return new ExecutionEnvironment(user, host, port);
    }

    /**
     * A method that returns a legacy string that contains
     * either user@host or "localhost"
     * TODO: deprecate and remove
     */
    public static String getHostKey(ExecutionEnvironment executionEnvironment) {
        if (executionEnvironment.isLocal()) {
            return CompilerSetManager.LOCALHOST;
        } else if (executionEnvironment.getUser() == null || executionEnvironment.getUser().length() == 0) {
            return executionEnvironment.getHost();
        } else {
            return executionEnvironment.getUser() + '@' + executionEnvironment.getHost();
        }
    }

    /**
     * That's for transition period only
     * TODO: deprecate, then remove as soone as switching to o.n.m.nativeexecution is complete
     * @param hostKey key in the form user@host
     */
    public static ExecutionEnvironment getExecutionEnvironment(String hostKey) {
        // TODO: remove this check and refactor clients to use getLocal() instead
        if ("localhost".equals(hostKey) || "127.0.0.1".equals(hostKey)) { //NOI18N
            return LOCAL;
        }
        String user;
        String host;
        int pos = hostKey.indexOf('@', 0); //NOI18N
        if (pos < 0) {
            user = "";
            host = hostKey;
        } else {
            user = hostKey.substring(0, pos);
            host = hostKey.substring(pos + 1);
        }
        return getExecutionEnvironment(user, host);
    }
}
