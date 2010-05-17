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


import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.jaxrpc.nodes.WebServiceNode;
import org.netbeans.modules.websvc.project.api.ServiceDescriptor;
import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.api.WebService.Type;
import org.netbeans.modules.websvc.project.spi.WebServiceImplementation;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author mkuchtiak
 */
public class AntJaxRpcService implements WebServiceImplementation {

    private Webservices webservices;
    private WebserviceDescription wsDesc;
    private Project prj;

    /** Constructor.
     *
     * @param service JaxWsService
     * @param prj project
     */
    public AntJaxRpcService(Webservices webservices, WebserviceDescription wsDesc, Project prj) {
        this.webservices = webservices;
        this.wsDesc = wsDesc;
        this.prj = prj;
    }

    public String getIdentifier() {
         return wsDesc.getWebserviceDescriptionName();
    }

    public boolean isServiceProvider() {
        return true;
    }

    public Type getServiceType() {
        return WebService.Type.SOAP;
    }

    public ServiceDescriptor getServiceDescriptor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Node createNode() {
        List<FileObject> roots = new ArrayList<FileObject>();
        SourceGroup[] groups = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (groups != null) {
            for (SourceGroup group : groups) {
                FileObject srcRoot = group.getRootFolder();
                FileObject implBean = getImplBeanClass(srcRoot, wsDesc);
                System.out.println("implBean = "+implBean);
                if (implBean != null) {
                    return new WebServiceNode(webservices, wsDesc, srcRoot, implBean);
                }
            }
        }
        return null;
    }

    private static FileObject getImplBeanClass(FileObject srcRoot, WebserviceDescription webServiceDescription) {
        PortComponent portComponent = webServiceDescription.getPortComponent(0); //assume one port per ws

        ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
        String link = serviceImplBean.getServletLink();
        if (link == null) {
            link = serviceImplBean.getEjbLink();
        }
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(srcRoot);
        String implBean = wsSupport.getImplementationBean(link);
        return srcRoot.getFileObject(implBean.replace('.', '/').concat(".java"));
    }

}
