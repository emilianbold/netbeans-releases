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

import java.io.IOException;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.WebServicePersistenceManager;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlDataManager;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author nam
 */
public class WsdlDataManagerImpl implements WsdlDataManager {

    public void save(WsdlData data) {
        WebServicePersistenceManager mgr = new WebServicePersistenceManager();
        try {
            WsdlServiceProxyDescriptor desc = data.getJaxWsDescriptor();
            if (desc instanceof WebServiceDescriptor) {
                mgr.saveDescriptor((WebServiceDescriptor)desc);
            }
            desc = data.getJaxRpcDescriptor();
            if (desc instanceof WebServiceDescriptor) {
                mgr.saveDescriptor((WebServiceDescriptor)desc);
            }
        } catch(Exception ex) {
            try {
                mgr.save();
            } catch(Exception e) {
                //at this point, just leave it to save on ide exit 
            }
        }
    }

    public WsdlData getWsdlData(String wsdlUrl, String serviceName, boolean synchronuous) {
        return WebServiceListModel.getInstance().getWebServiceData(wsdlUrl, serviceName, synchronuous);
    }

    public WsdlData addWsdlData(String wsdlUrl, String packageName) {
        return WebServiceListModel.getInstance().addWebService(wsdlUrl, packageName, WebServiceListModel.DEFAULT_GROUP);
    }
    
    public void removeWsdlData(String wsdlUrl, String serviceName) {
        WsdlData data = WebServiceListModel.getInstance().findWebServiceData(wsdlUrl, serviceName, true);
        if (data != null) {
            WebServiceListModel.getInstance().removeWebService(data.getId());
        }
    }

    public WsdlData findWsdlData(String wsdlUrl, String serviceName) {
        return WebServiceListModel.getInstance().findWebServiceData(wsdlUrl, serviceName, true);
    }
    
    public void refresh(WsdlData wsdlData) {
        if (wsdlData instanceof WebServiceData) {
            WebServiceData data = (WebServiceData) wsdlData;
            try {
                WebServiceManager.getInstance().refreshWebService(data);
            } catch(IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
}
