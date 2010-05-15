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

package org.netbeans.modules.soa.ldap;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.swing.Icon;
import org.netbeans.modules.soa.ldap.browser.IconPool;
import org.netbeans.modules.soa.ldap.properties.CheckParametersPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class LDAPUtils {
    public static void close(DirContext dirContext) {
        try {
            dirContext.close();
        } catch (Exception ex) {
            // do nothing
        }
    }

    public static void close(NamingEnumeration<?> results) {
        try {
            results.close();
        } catch (Exception ex) {
            // do nothing
        }
    }

    public static void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    public static LdapName createEmptyLdapName() {
        return new LdapName(new ArrayList<Rdn>(0));
    }

    public static String exceptionToString(Exception ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof UnknownHostException) {
            return NbBundle.getMessage(LDAPUtils.class,
                    "LDAPUtils.UnknownHostException",
                    cause.getMessage()); // NOI18N
        }

        if (cause instanceof ConnectException) {
            return cause.getLocalizedMessage();
        }

        return ex.getLocalizedMessage();
    }

    public static Icon getLDAPIcon() {
        return IconPool.loadImageIcon("ldap"); // NOI18N
    }
}
