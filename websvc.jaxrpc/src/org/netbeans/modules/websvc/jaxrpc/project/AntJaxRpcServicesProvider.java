/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.jaxrpc.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.DDProvider;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.spi.WebServiceDataProvider;
import org.netbeans.modules.websvc.project.spi.WebServiceFactory;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=WebServiceDataProvider.class, projectType={
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-j2ee-ejbjarproject",
    "org-netbeans-modules-j2ee-clientproject",
    "org-netbeans-modules-java-j2seproject"
})
public class AntJaxRpcServicesProvider implements WebServiceDataProvider {
    private Project prj;

    /** Constructor.
     *
     * @param prj project
     * @param jaxWsSupport JAXWSLightSupport
     */
    public AntJaxRpcServicesProvider(Project prj) {
        this.prj = prj;
    }

    public List<WebService> getServiceProviders() {
        List<WebService> webServices = new ArrayList<WebService>();
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(prj.getProjectDirectory());
        if (wsSupport != null) {
            FileObject wsDD = wsSupport.getWebservicesDD();
            try {
                Webservices ws = DDProvider.getDefault().getDDRoot(wsDD);
                if (ws != null) {
                    for (WebserviceDescription wsDesc : ws.getWebserviceDescription()) {
                        webServices.add(WebServiceFactory.createWebService(new AntJaxRpcService(ws, wsDesc, prj)));
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return webServices;
    }

    public List<WebService> getServiceConsumers() {
        List<WebService> webServices = new ArrayList<WebService>();
        WebServicesClientSupport wscs = WebServicesClientSupport.getWebServicesClientSupport(prj.getProjectDirectory());
        if (wscs != null) {
            FileObject wsdlFolder = wscs.getWsdlFolder();
            if (wsdlFolder != null) {
                FileObject[] wsdls = wsdlFolder.getChildren();
                for (FileObject wsdl : wsdls) {
                    if ("wsdl".equals(wsdl.getExt())) {
                        webServices.add(WebServiceFactory.createWebService(new AntJaxRpcClient(prj, wscs, wsdl)));
                    }
                }
            }
        }
        return webServices;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
    }
}
