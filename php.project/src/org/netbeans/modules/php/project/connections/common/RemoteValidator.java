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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.common;

import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class RemoteValidator {
    public static final int MINIMUM_PORT = 0;
    public static final int MAXIMUM_PORT = 65535;

    private RemoteValidator() {
    }

    public static String validateHost(String host) {
        assert host != null;
        if (host.trim().length() == 0) {
            return NbBundle.getMessage(RemoteValidator.class, "MSG_NoHostName");
        }
        return null;
    }

    public static String validateUser(String username) {
        if (username.trim().length() == 0) {
            return NbBundle.getMessage(RemoteValidator.class, "MSG_NoUserName");
        }
        return null;
    }

    public static String validatePort(String port) {
        String err = null;
        try {
            int p = Integer.parseInt(port);
            if (p < MINIMUM_PORT || p > MAXIMUM_PORT) { // see InetSocketAddress
                err = NbBundle.getMessage(RemoteValidator.class, "MSG_PortInvalid", String.valueOf(MINIMUM_PORT), String.valueOf(MAXIMUM_PORT));
            }
        } catch (NumberFormatException nfe) {
            err = NbBundle.getMessage(RemoteValidator.class, "MSG_PortNotNumeric");
        }
        return err;
    }

    public static String validateTimeout(String timeout) {
        return validatePositiveNumber(timeout, "MSG_TimeoutNotPositive", "MSG_TimeoutNotNumeric"); // NOI18N
    }

    /**
     * Validate keep-alive interval.
     * @param keepAliveInterval value to be validated
     * @return error message or {@code null} if keep-alive interval is correct
     */
    public static String validateKeepAliveInterval(String keepAliveInterval) {
        return validatePositiveNumber(keepAliveInterval, "MSG_KeepAliveNotPositive", "MSG_KeepAliveNotNumeric"); // NOI18N
    }

    /**
     * Validate input as a positive number.
     * @param number input to be validated
     * @param errorNotPositiveKey error key used if input is not positive number
     * @param errorNotNumericKey error key used if input is not number
     * @return error message or {@code null} if input is positive number
     */
    private static String validatePositiveNumber(String number, String errorNotPositiveKey, String errorNotNumericKey) {
        String err = null;
        try {
            int t = Integer.parseInt(number);
            if (t < 0) {
                err = NbBundle.getMessage(RemoteValidator.class, errorNotPositiveKey);
            }
        } catch (NumberFormatException nfe) {
            err = NbBundle.getMessage(RemoteValidator.class, errorNotNumericKey);
        }
        return err;
    }

}
