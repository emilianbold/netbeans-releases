/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.maven.jaxws;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportFactory;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportImpl;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider;
import org.netbeans.modules.websvc.project.spi.WebServiceDataProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * Lookup Provider for WS Support in JavaEE project types
 *
 * @author mkuchtiak
 */
public class MavenJaxWsLookupProvider implements LookupProvider {

    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project prj = baseContext.lookup(Project.class);
        JAXWSLightSupportImpl spiJAXWSSupport = new MavenJAXWSSupportIml(prj);
        final JAXWSLightSupport jaxWsSupport = JAXWSLightSupportFactory.createJAXWSSupport(spiJAXWSSupport);
        
        JAXWSLightSupportProvider jaxWsSupportProvider = new JAXWSLightSupportProvider() {

            public JAXWSLightSupport findJAXWSSupport() {
                return jaxWsSupport;
            }
            
        };
        
        ProjectOpenedHook openhook = new ProjectOpenedHook() {

            PropertyChangeListener pcl;
            FileObject wsdlFolder;
            FileChangeListener wsdlFolderListener;

            protected void projectOpened() {
                final MetadataModel<WebservicesMetadata> wsModel = jaxWsSupport.getWebservicesMetadataModel();
                if (wsModel != null) {
                    try {
                        wsModel.runReadActionWhenReady(new MetadataModelAction<WebservicesMetadata, Void>() {

                            public Void run(final WebservicesMetadata metadata) {
                                Webservices webServices = metadata.getRoot();
                                pcl = new WebservicesChangeListener(jaxWsSupport, wsModel);
                                webServices.addPropertyChangeListener(pcl);
                                return null;
                            }
                        });
                    } catch (java.io.IOException ex) {

                    }
                }

                wsdlFolder = jaxWsSupport.getLocalWsdlFolder(false);
                
                if (wsdlFolder != null) {
                    try {
                        detectWsdlClients(prj, jaxWsSupport, wsdlFolder);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }

            protected void projectClosed() {
                final MetadataModel<WebservicesMetadata> wsModel = jaxWsSupport.getWebservicesMetadataModel();
                if (wsModel != null) { 
                    try {
                        wsModel.runReadActionWhenReady(new MetadataModelAction<WebservicesMetadata, Void>() {

                            public Void run(final WebservicesMetadata metadata) {
                                Webservices webServices = metadata.getRoot();
                                webServices.removePropertyChangeListener(pcl);
                                return null;
                            }
                        });
                    } catch (java.io.IOException ex) {

                    }
                }
            }
        };
        WebServiceDataProvider servicedataProvider = new MavenWebServicesProvider(prj, jaxWsSupport); 
        return Lookups.fixed(openhook, jaxWsSupportProvider, servicedataProvider);
    }

    private class WebservicesChangeListener implements PropertyChangeListener {
        
        JAXWSLightSupport jaxWsSupport;
        
        MetadataModel<WebservicesMetadata> wsModel;
        Project prj;
        private RequestProcessor.Task updateJaxWsTask = RequestProcessor.getDefault().create(new Runnable() {

            public void run() {
                updateJaxWs();
            }
        });

        WebservicesChangeListener(JAXWSLightSupport jaxWsSupport, MetadataModel<WebservicesMetadata> wsModel) {
            this.jaxWsSupport = jaxWsSupport;
            this.wsModel = wsModel;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            //requestModelUpdate();
            updateJaxWsTask.schedule(100);
        }
        
        private void updateJaxWs() {
            
            try {
                Map<String, String> newServices = wsModel.runReadAction(new MetadataModelAction<WebservicesMetadata, Map<String, String>>() {

                    public Map<String, String> run(WebservicesMetadata metadata) {
                        Map<String, String> result = new HashMap<String, String>();
                        Webservices webServices = metadata.getRoot();
                        for (WebserviceDescription wsDesc : webServices.getWebserviceDescription()) {
                            PortComponent[] ports = wsDesc.getPortComponent();
                            for (PortComponent port : ports) {
                                result.put(port.getDisplayName(), port.getPortComponentName());
//                                if ("javax.xml.ws.WebServiceProvider".equals(wsDesc.getDisplayName())) { //NOI18N
//                                    result.put("fromWsdl:"+wsDesc.getWebserviceDescriptionName(), port.getDisplayName()); //NOI18N
//                                } else if (JaxWsUtils.isInSourceGroup(prj, port.getServiceEndpointInterface())) {
//                                    result.put(port.getDisplayName(), port.getPortComponentName());
//                                } else if (wsDesc.getWsdlFile() != null) {
//                                    result.put("fromWsdl:"+wsDesc.getWebserviceDescriptionName(), port.getDisplayName()); //NOI18N
//                                }
                            }

                        }
                        return result;
                    }
                });
                List<JaxWsService> oldJaxWsServices = jaxWsSupport.getServices();
                Map<String, JaxWsService> oldServices = new HashMap<String, JaxWsService>();

                for (JaxWsService s: oldJaxWsServices) {
                    // implementationClass -> Service
                    if (s.isServiceProvider()) {
                        oldServices.put(s.getImplementationClass(), s);
                    }
                }
                // compare new services with existing
                // looking for common services (implementationClass)
                Set<String> commonServices = new HashSet<String>();
                Set<String> keys1 = oldServices.keySet();
                Set<String> keys2 = newServices.keySet();
                for (String key : keys1) {
                    if (keys2.contains(key)) {
                        commonServices.add(key);
                    }
                }
                for (String key : commonServices) {
                    oldServices.remove(key);
                    newServices.remove(key);
                }
                
                // remove old services
                boolean needToSave = false;
                for (String key : oldServices.keySet()) {
                    jaxWsSupport.removeService(oldServices.get(key));
                }
                // add new services
                for (String key : newServices.keySet()) {
                    jaxWsSupport.addService(new JaxWsService(newServices.get(key), key));
                }
            } catch (java.io.IOException ioe) {
                ioe.printStackTrace();
            }

        }
    }
        
    private void detectWsdlClients(Project prj, JAXWSLightSupport jaxWsSupport, FileObject wsdlFolder) throws Exception {
        String[] filepaths = PluginPropertyUtils.getPluginPropertyList(prj,
                "org.codehaus.mojo", //NOI18N
                "jaxws-maven-plugin", //NOI18N
                "wsdlFiles", //NOI18N
                "wsdlFile", //NOI18N
                "wsimport"); //NOI18N
        if (filepaths != null) {
            for (String filePath:filepaths) {
                JaxWsService client = new JaxWsService(filePath, false);
                jaxWsSupport.addService(client);
            }
        }  else {
            // look for wsdl in wsdl folder
//            FileObject[] wsdlCandidates = wsdlFolder.getChildren();
//            for (FileObject wsdlCandidate:wsdlCandidates) {
//                if (wsdlCandidate.isData() && "wsdl".equalsIgnoreCase(wsdlCandidate.getExt())) { //NOI18N
//                    JaxWsService client = new JaxWsService(wsdlCandidate.getNameExt(), false);
//                    jaxWsSupport.addService(client);
//                }
//            }
        }
    }

}
