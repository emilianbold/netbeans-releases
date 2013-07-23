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
package org.netbeans.modules.kenai.ui.impl;

import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.collab.chat.KenaiConnection;
import org.netbeans.modules.kenai.collab.chat.PresenceIndicator;
import org.netbeans.modules.kenai.ui.api.KenaiUIUtils;
import org.netbeans.modules.team.server.ui.spi.LoginPanelSupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.kenai.ui.impl.Bundle.*;

/**
 *
 * @author Ondrej Vrabec
 */
public class LoginPanelSupportImpl implements LoginPanelSupport {
    private final Kenai kenai;
    private LoginPanelDetails component;
    private RequestProcessor rp;

    public LoginPanelSupportImpl (Kenai kenai) {
        this.kenai = kenai;
    }

    @Override
    @NbBundle.Messages("LBL_AuthenticationFailed=Authentication failed")
    public void startLogin (final LoginPanelCallback loginPanelCallback) {
        final LoginPanelDetails loginPanel = getLoginPanelComponent();
        loginPanel.setChildrenEnabled(false);
        getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    PresenceIndicator.getDefault().init();
                    KenaiUIUtils.logKenaiUsage("LOGIN"); // NOI18N
                    KenaiConnection.getDefault(kenai);
                    PasswordAuthentication current = kenai.getPasswordAuthentication();
                    if (current !=null && !(loginPanel.getUsername().equals(current.getUserName()) && Arrays.equals(loginPanel.getPassword(), current.getPassword()))) {
                        kenai.logout();
                    }
                    kenai.login(loginPanel.getUsername(), loginPanel.getPassword(), loginPanel.isOnline());
                    LoginUtils.savePassword(kenai, loginPanel.getUsername(), loginPanel.isStorePassword() 
                            ? loginPanel.getPassword()
                            : null);
                    loginPanelCallback.successful();
                } catch (final KenaiException ex) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            String errorMessage = ex.getMessage();
                            if (errorMessage==null || "".equals(errorMessage.trim())) {
                                errorMessage = LBL_AuthenticationFailed();
                                Logger.getLogger(LoginPanelSupportImpl.class.getName()).log(Level.INFO, errorMessage, ex);
                            }
                            Map<String, String> errors = ex.getErrors();
                            String tooltipText = null;
                            if (errors!=null) {
                                tooltipText = errors.get("message"); //NOI18N
                            }
                            loginPanelCallback.showError(errorMessage, tooltipText);
                        }
                    });
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            loginPanel.setChildrenEnabled(true);
                        }
                    });
                }
            }
        });
    }

    @Override
    public LoginPanelDetails getLoginPanelComponent () {
        if (component == null) {
            component = new LoginPanelDetails(kenai, new LoginUtils.CredentialsImpl());
            component.initialize();
        }
        return component;
    }
    
    private synchronized RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Kenai Login"); // NOI18N
        }
        return rp;
    }    
    
}
