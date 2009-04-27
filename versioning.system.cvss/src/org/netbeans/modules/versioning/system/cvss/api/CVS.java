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

package org.netbeans.modules.versioning.system.cvss.api;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.ui.actions.checkout.CheckoutAction;
import org.netbeans.modules.versioning.system.cvss.ui.wizards.RepositoryStep;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Ondra Vrabec
 */
public class CVS {

    /**
     * Opens standard checkout wizard
     * @param url repository url to checkout
     * @throws java.net.MalformedURLException in case the url is invalid
     */
    public static void openCheckoutWizard (final String url) {
        addRecentUrl(url);
        SystemAction.get(CheckoutAction.class).perform();
    }

    /**
     * Tries to determine if the given string is a valid cvs root
     * Currently does not access network and is not time-consuming.
     * @param url repository root
     * @return true if given string is a valid cvs root
     */
    public static boolean isRepository (final String cvsRoot) {
        boolean retval = false;
        
        boolean supportedMethod = false;
        if (cvsRoot != null) {
            supportedMethod |= cvsRoot.startsWith(":pserver:"); // NOI18N
            supportedMethod |= cvsRoot.startsWith(":local:"); // NOI18N
            supportedMethod |= cvsRoot.startsWith(":fork:"); // NOI18N
            supportedMethod |= cvsRoot.startsWith(":ext:"); // NOI18N
        }
        if (supportedMethod) {
            try {
                CVSRoot.parse(cvsRoot);
                retval = true;
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(CVS.class.getName()).log(Level.FINE, "Invalid cvs root: " + cvsRoot, ex);
            }
        }
        return retval;
    }

    /**
     * Adds a remote url for the combos used in Checkout and Import wizard
     *
     * @param url
     * @throws java.net.MalformedURLException
     */
    public static void addRecentUrl(String url) {
        Utils.insert(CvsModuleConfig.getDefault().getPreferences(), RepositoryStep.RECENT_ROOTS, url, 8);
    }
}
