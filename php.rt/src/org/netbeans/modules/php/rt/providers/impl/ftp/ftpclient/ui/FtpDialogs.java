/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.ui;

import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.*;
import javax.swing.JButton;
import org.netbeans.modules.php.rt.WebServerRegistry;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpHostImpl;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpServerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public class FtpDialogs {

    private static final String LOGIN_DETAILS_TITLE = "LBL_Login_Details_Title"; // NOI18N
    private static final String LOGIN_DETAILS_RETRY_BTN = "LBL_Login_Details_Retry_Btn"; // NOI18N

    public static boolean retryLoginDialog(FtpHostImpl host) {
        boolean retry = false;
        String title = NbBundle.getMessage(FtpDialogs.class, LOGIN_DETAILS_TITLE);

        String ftpServer = (String) host.getProperty(FtpHostImpl.FTP_SERVER);
        String ftpUserName = (String) host.getProperty(FtpHostImpl.FTP_USER_NAME);
        char[] ftpPassword = (char[]) host.getProperty(FtpHostImpl.FTP_PASSWORD);

        FtpLoginDetailsPanel panel = new FtpLoginDetailsPanel(ftpServer, ftpUserName, ftpPassword);
        final JButton retryButton = new JButton(NbBundle.getMessage(FtpDialogs.class, LOGIN_DETAILS_RETRY_BTN));
        retryButton.setEnabled(true);
        DialogDescriptor dialog = new DialogDescriptor(
                panel, 
                title, 
                true, 
                new Object[]{retryButton, DialogDescriptor.CANCEL_OPTION}, 
                DialogDescriptor.CANCEL_OPTION, 
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);

        retry = (DialogDisplayer.getDefault().notify(dialog) == retryButton);
        
        host.setProperty(FtpHostImpl.FTP_USER_NAME, panel.getFtpUserName());
        host.setProperty(FtpHostImpl.FTP_PASSWORD, panel.getFtpPassword());
        
        if (panel.getSaveChanges()){
            FtpServerProvider provider = (FtpServerProvider) host.getProvider();
            // may add 
            provider.updateHost(host, host);
            // send notification 
            WebServerRegistry.getInstance().upadateHost(host);
            
        }
        return retry;
    }

}
