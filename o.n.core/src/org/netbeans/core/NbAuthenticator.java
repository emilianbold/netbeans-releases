/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.core;

import java.net.PasswordAuthentication;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** Global password protected sites Authenticator for IDE
 *
 * @author Jiri Rechtacek
 */
final class NbAuthenticator extends java.net.Authenticator {

    private static final long TIMEOUT = 3000;
    private static long lastTry = 0;

    NbAuthenticator() {
        Preferences proxySettingsNode = NbPreferences.root().node("/org/netbeans/core"); //NOI18N
        assert proxySettingsNode != null;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        Logger.getLogger(NbAuthenticator.class.getName()).log(Level.FINER, "Authenticator.getPasswordAuthentication() with prompt " + this.getRequestingPrompt()); //NOI18N

        if (RequestorType.PROXY == getRequestorType() && ProxySettings.useAuthentication()) {
            Logger.getLogger(NbAuthenticator.class.getName()).log(Level.FINER, "Username set to " + ProxySettings.getAuthenticationUsername() + " while request " + this.getRequestingURL()); //NOI18N
            return new java.net.PasswordAuthentication(ProxySettings.getAuthenticationUsername(), ProxySettings.getAuthenticationPassword());
        } else {
            if (System.currentTimeMillis() - lastTry > TIMEOUT) {
                if (getRequestingProtocol().startsWith("SOCKS")&&(ProxySettings.getAuthenticationUsername().length()>0)) { //NOI18N
                    return new java.net.PasswordAuthentication(ProxySettings.getAuthenticationUsername(), ProxySettings.getAuthenticationPassword());
                }
                NbAuthenticatorPanel ui = new NbAuthenticatorPanel(getRequestingPrompt());
                Object result = DialogDisplayer.getDefault().notify(
                        new DialogDescriptor(ui, NbBundle.getMessage(NbAuthenticator.class, "CTL_Authentication"))); //NOI18N
                if (DialogDescriptor.OK_OPTION == result) {
                    lastTry = 0;
                    return new PasswordAuthentication(ui.getUserName(), ui.getPassword());
                } else {
                    lastTry = System.currentTimeMillis();
                }
            }
        }

        Logger.getLogger(NbAuthenticator.class.getName()).log(Level.WARNING, "No authentication set while requesting " + this.getRequestingURL()); //NOI18N
        return null;
    }

}
