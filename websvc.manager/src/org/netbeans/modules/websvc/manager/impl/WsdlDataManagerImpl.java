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
package org.netbeans.modules.websvc.manager.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlDataManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author nam
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlDataManager.class)
public class WsdlDataManagerImpl implements WsdlDataManager {

    private int precedence;

    public WsdlDataManagerImpl() {
        precedence = 0;
    }

    public WsdlData getWsdlData(String wsdlUrl, String serviceName, boolean synchronuous) {
        return WebServiceListModel.getInstance().getWebServiceData(wsdlUrl, serviceName, synchronuous);
    }

    public WsdlData addWsdlData(String wsdlUrl, String packageName) {
        final WebServiceData wsData = new WebServiceData(wsdlUrl, WebServiceListModel.DEFAULT_GROUP);
        wsData.setPackageName(packageName);
        wsData.setResolved(false);

        // Run the add W/S asynchronously
        Runnable addWsRunnable = new Runnable() {

            public void run() {
                try {
                    WebServiceManager.getInstance().addWebService(wsData, true);
                } catch (IOException ex) {
                    handleException(ex);
                }
            }
        };
        RequestProcessor.getDefault().post(addWsRunnable);
        return wsData;
    }

    public void removeWsdlData(String wsdlUrl, String serviceName) {
        WebServiceData wsData = WebServiceListModel.getInstance().findWebServiceData(wsdlUrl, serviceName, true);
        if (wsData != null) {
            WebServiceManager.getInstance().removeWebService(wsData);
        }
    }

    public WsdlData findWsdlData(String wsdlUrl, String serviceName) {
        return WebServiceListModel.getInstance().findWebServiceData(wsdlUrl, serviceName, true);
    }

    public void refresh(WsdlData wsdlData) {
        if (wsdlData instanceof WebServiceData) {
            final WebServiceData data = (WebServiceData) wsdlData;
            Runnable addWsRunnable = new Runnable() {

                public void run() {
                    try {
                        WebServiceManager.getInstance().refreshWebService(data);
                    } catch (IOException ex) {
                        handleException(ex);
                    }
                }
            };
            RequestProcessor.getDefault().post(addWsRunnable);
        }
    }

    private void handleException(final Exception exception) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (exception instanceof FileNotFoundException) {
                    String errorMessage = NbBundle.getMessage(WebServiceListModel.class, "INVALID_URL");
                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    DialogDisplayer.getDefault().notify(d);
                } else {
                    String cause = (exception != null) ? exception.getLocalizedMessage() : null;
                    String excString = (exception != null) ? exception.getClass().getName() + " - " + cause : null;

                    String errorMessage = NbBundle.getMessage(WebServiceListModel.class, "WS_ADD_ERROR") + "\n\n" + excString; // NOI18N

                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        });
    }

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    public int getPrecedence() {
        return precedence;
    }
}
