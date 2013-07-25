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
package org.netbeans.modules.odcs.ui;

import java.awt.EventQueue;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import javax.swing.JComponent;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.team.server.ui.spi.LoginPanelSupport;
import org.openide.util.NbBundle;
import static org.netbeans.modules.odcs.ui.Bundle.*;
import org.netbeans.modules.odcs.ui.api.OdcsUIUtil;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Ondrej Vrabec
 */
public class LoginPanelSupportImpl implements LoginPanelSupport {
    private final ODCSServer server;
    private LoginPanelDetails component;
    private RequestProcessor rp;
    
    public LoginPanelSupportImpl (ODCSServer server) {
        this.server = server;
    }

    @Override
    @NbBundle.Messages("LBL_AuthenticationFailed=Authentication failed")
    public void startLogin (final LoginPanelCallback loginPanelCallback) {
        final LoginPanelDetails loginPanel = (LoginPanelDetails) getLoginPanelComponent();
        loginPanel.setChildrenEnabled(false);
        getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    OdcsUIUtil.logODCSUsage("LOGIN"); //NOI18N
                    PresenceIndicator.getDefault().init();
                    PasswordAuthentication current = server.getPasswordAuthentication();
                    if (current !=null && !(loginPanel.getUsername().equals(current.getUserName()) && Arrays.equals(loginPanel.getPassword(), current.getPassword()))) {
                        server.logout();
                    }
                    server.login(loginPanel.getUsername(), loginPanel.getPassword());
                    Utilities.savePassword(server, loginPanel.getUsername(), loginPanel.isStorePassword()
                            ? loginPanel.getPassword()
                            : null);
                    loginPanelCallback.successful();
                } catch (final ODCSException ex) {
                    Utils.logException(ex, false);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            String errorMessage = Utils.parseKnownMessage(ex);
                            if (errorMessage == null) {
                                errorMessage = LBL_AuthenticationFailed();
                            }
                            loginPanelCallback.showError(errorMessage, ex.getMessage());
                        }
                    });
                } finally {
                    EventQueue.invokeLater(new Runnable() {
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
    public JComponent getLoginPanelComponent () {
        if (component == null) {
            component = new LoginPanelDetails(server, new Utilities.CredentialsImpl());
            component.initialize();
        }
        return component;
    }

    private synchronized RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("ODCS Login"); // NOI18N
        }
        return rp;
    }
    
}
