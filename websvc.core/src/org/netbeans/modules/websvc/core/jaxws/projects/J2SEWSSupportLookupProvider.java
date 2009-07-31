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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/** Lookup Provider for WS Support
 *
 * @author mkuchtiak
 */
// XXX could probably be converted to use @ProjectServiceProvider instead
// (would need to do some refactoring first)
@LookupProvider.Registration(projectType="org-netbeans-modules-java-j2seproject")
public class J2SEWSSupportLookupProvider implements LookupProvider {
    
    /** Creates a new instance of JaxWSLookupProvider */
    public J2SEWSSupportLookupProvider() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project project = baseContext.lookup(Project.class);
        JAXWSClientSupportImpl j2seJAXWSClientSupport = new J2SEProjectJAXWSClientSupport(project);
        final JAXWSClientSupport jaxWsClientSupportApi = JAXWSClientSupportFactory.createJAXWSClientSupport(j2seJAXWSClientSupport);
        
        WebServicesClientSupportImpl jaxrpcClientSupport = new J2SEProjectJaxRpcClientSupport(project);
        final WebServicesClientSupport jaxRpcClientSupportApi = WebServicesClientSupportFactory.createWebServicesClientSupport(jaxrpcClientSupport);
        
        ProjectOpenedHook openHook = new ProjectOpenedHook() {
            @Override
            protected void projectOpened() {
                if(jaxRpcClientSupportApi.isBroken(project)) {
                    jaxRpcClientSupportApi.showBrokenAlert(project);
                }
                if (jaxWsClientSupportApi.getServiceClients().size() > 0) {
                    FileObject wsdlFolder = null;
                    try {
                        wsdlFolder = jaxWsClientSupportApi.getWsdlFolder(false);
                    } catch (IOException ex) {}
                    if (wsdlFolder == null || wsdlFolder.getParent().getFileObject("jax-ws-catalog.xml") == null) { //NOI18N
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                try {
                                    JaxWsCatalogPanel.generateJaxWsCatalog(project, jaxWsClientSupportApi);
                                } catch (IOException ex) {
                                    Logger.getLogger(JaxWsCatalogPanel.class.getName()).log(Level.WARNING, "Cannot create jax-ws-catalog.xml", ex);
                                }
                            }
                        });
                    }

                }
            }

            @Override
            protected void projectClosed() {
            }
        };
        return Lookups.fixed(new Object[] {
            jaxWsClientSupportApi,
            jaxRpcClientSupportApi,
            openHook});
    }
}