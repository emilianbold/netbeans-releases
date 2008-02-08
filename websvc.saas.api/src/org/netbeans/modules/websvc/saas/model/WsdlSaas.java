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

package org.netbeans.modules.websvc.saas.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.CodeGen;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author nam
 */
public class WsdlSaas extends Saas {
    //TODO consolidate and remove
    private WebServiceData wsData;

    public WsdlSaas(SaasGroup parentGroup, SaasServices services) {
        super(parentGroup, services);
    }

    public WsdlSaas(SaasGroup parentGroup, String displayName, String url, String packageName) {
        this(parentGroup, new SaasServices());
        this.getDelegate().setDisplayName(displayName);
        this.getDelegate().setUrl(url);
        SaasMetadata m = this.getDelegate().getSaasMetadata();
        if (m == null) {
            m = new SaasMetadata();
            this.getDelegate().setSaasMetadata(m);
        }
        CodeGen cg = m.getCodeGen();
        if (cg == null) {
            cg = new CodeGen();
            m.setCodeGen(cg);
        }
        cg.setPackageName(packageName);
        //wsData = WebServiceListModel.getInstance().findWebServiceData(services.getUrl(), getServiceName());
    }
    
    private WebServiceData findWebServiceData() {
        WebServiceData data = WebServiceListModel.getInstance().findWebServiceData(getUrl(), null , false);
        if (data != null) {
            data.setName(wsData.getWsdlService().getJavaName());
        }
        return data;
    }
    
    public WebServiceData getWsdlData() {
        if (wsData == null) {
            wsData = findWebServiceData();
            if (wsData == null) {
                Task t = WebServiceListModel.getInstance().addWebService(getUrl(), null, null);
                try {
                    t.waitFinished(30000);
                } catch(InterruptedException ex) {
                    //log down there
                }
                wsData = findWebServiceData();
            }
            if (wsData == null) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Failed to add service "+getUrl());
            }
        }
        return wsData;
    }
    
    public WsdlService getWsdlModel() {
        return wsData.getWsdlService();
    }

    public FileObject getLocalWsdlFile() {
        return FileUtil.toFileObject(new File(wsData.getWsdlFile()));
    }
    
    public List<Object> getPortsOrMethods() {
        List<SaasMethod> methods = getMethods();
        if (methods != null && methods.size() > 0) {
            return new ArrayList<Object>(methods);
        }
        return new ArrayList<Object>(getWsdlModel().getPorts());
    }
}
