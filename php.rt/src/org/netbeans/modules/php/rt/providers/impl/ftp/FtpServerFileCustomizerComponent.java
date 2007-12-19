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
package org.netbeans.modules.php.rt.providers.impl.ftp;

import java.util.Properties;
import javax.swing.JPanel;
import org.netbeans.modules.php.rt.providers.impl.DefaultServerCustomizer;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.providers.impl.ServerCustomizerComponent;
import org.netbeans.modules.php.rt.spi.providers.Host;

/**
 *
 * @author avk
 */
public class FtpServerFileCustomizerComponent extends FtpServerFilePanelVisual
        implements ServerCustomizerComponent
{

    public FtpServerFileCustomizerComponent(FtpHostImpl host, DefaultServerCustomizer parentDialog) {
        super();
        myHost = host;
        myParentDialog = parentDialog;

    }

    public void readValues(Properties properties) {
        Object obj = properties.get(HOST);
        if (obj != null && obj instanceof FtpHostImpl){
            FtpHostImpl impl = (FtpHostImpl)obj;
            
            String ftpServer = (String) impl.getProperty(FtpHostImpl.FTP_SERVER);
            String ftpUserName = (String) impl.getProperty(FtpHostImpl.FTP_USER_NAME);
            char[] ftpPassword = (char[]) impl.getProperty(FtpHostImpl.FTP_PASSWORD);
            String ftpDirectory = (String) impl.getProperty(FtpHostImpl.FTP_DIRECTORY);

            getFtpServer().setText(ftpServer);
            getFtpUserName().setText(ftpUserName);
            getFtpPassword().setText(String.copyValueOf(ftpPassword));
            getFtpDirectory().setText(ftpDirectory);
        }
        doContentValidation();
    }

    public void storeValues(Properties properties) {
        Object obj = properties.get(HOST);
        if (obj != null && obj instanceof FtpHostImpl){
            FtpHostImpl impl = (FtpHostImpl)obj;
            
            impl.setProperty(FtpHostImpl.FTP_SERVER, getFtpServer().getText());
            impl.setProperty(FtpHostImpl.FTP_USER_NAME, getFtpUserName().getText());
            impl.setProperty(FtpHostImpl.FTP_PASSWORD, getFtpPassword().getPassword());
            impl.setProperty(FtpHostImpl.FTP_DIRECTORY, getFtpDirectory().getText());

            properties.put(HOST, impl);
        }
    }

    public JPanel getPanel() {
        return this;
    }

    protected void setDefaults() {
        // no defaults here
    }

    public void stateChanged() {
        getParentDialog().stateChanged();
    }

    @Override
    protected void setErrorMessage(String message) {
        getParentDialog().setErrorMessage(message);
    }

    protected FtpHostImpl getHost() {
        return myHost;
    }

    private DefaultServerCustomizer getParentDialog() {
        return myParentDialog;
    }

    private DefaultServerCustomizer myParentDialog;

    private FtpHostImpl myHost;

}
