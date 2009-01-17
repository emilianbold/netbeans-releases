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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportFactory;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportImpl;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider;
import org.netbeans.modules.websvc.project.spi.LookupMergerSupport;
import org.netbeans.modules.websvc.project.spi.WebServiceDataProvider;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
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

            private PropertyChangeListener pcl;
            private PropertyChangeListener wsdlFolderListener;

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
                        ex.printStackTrace();
                    }
                }

                FileObject wsdlFolder = jaxWsSupport.getLocalWsdlFolder(false);

                if (wsdlFolder != null) {
                    detectWsdlClients(prj, jaxWsSupport, wsdlFolder);
                }
                NbMavenProject mp = prj.getLookup().lookup(NbMavenProject.class);
                if (mp != null) {
                    wsdlFolderListener = new PropertyChangeListener() {

                        public void propertyChange(PropertyChangeEvent evt) {
                            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                                updateClients();
                            }
                        }
                    };
                    mp.addPropertyChangeListener(wsdlFolderListener);
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
                        ex.printStackTrace();
                    }
                }
                NbMavenProject mp = prj.getLookup().lookup(NbMavenProject.class);
                if (mp != null) {
                    mp.removePropertyChangeListener(wsdlFolderListener);
                }
            }

            void updateClients() {
                // get old clients
                List<JaxWsService> oldClients = new ArrayList<JaxWsService>();
                Set<String> oldNames = new HashSet<String>();
                for (JaxWsService s : jaxWsSupport.getServices()) {
                    if (!s.isServiceProvider()) {
                        oldClients.add(s);
                        oldNames.add(s.getLocalWsdl());
                    }
                }
                FileObject wsdlFolder = jaxWsSupport.getLocalWsdlFolder(false);
                if (wsdlFolder != null) {
                    List<JaxWsService> newClients = getJaxWsClients(prj, jaxWsSupport, wsdlFolder);
                    Set<String> commonNames = new HashSet<String>();
                    for (JaxWsService client : newClients) {
                        String localWsdl = client.getLocalWsdl();
                        if (oldNames.contains(localWsdl)) {
                            commonNames.add(localWsdl);
                        }
                    }
                    // removing old clients
                    for (JaxWsService oldClient : oldClients) {
                        if (!commonNames.contains(oldClient.getLocalWsdl())) {
                            jaxWsSupport.removeService(oldClient);
                        }
                    }
                    // add new clients
                    for (JaxWsService newClient : newClients) {
                        if (!commonNames.contains(newClient.getLocalWsdl())) {
                            jaxWsSupport.addService(newClient);
                        }
                    }
                } else {
                    // removing all clients
                    for (JaxWsService client : oldClients) {
                        jaxWsSupport.removeService(client);
                    }
                }

            }
        };
        WebServiceDataProvider jaxWsServiceDataProvider = new MavenJaxWsServicesProvider(prj, jaxWsSupport);
        LookupMerger<WebServiceDataProvider> wsDataProviderMerger =
                LookupMergerSupport.createWebServiceDataProviderMerger();
        return Lookups.fixed(openhook, jaxWsSupportProvider, jaxWsServiceDataProvider, wsDataProviderMerger);
    }

    private class WebservicesChangeListener implements PropertyChangeListener {

        private JAXWSLightSupport jaxWsSupport;

        private MetadataModel<WebservicesMetadata> wsModel;
        private Project prj;
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
                Map<String, ServiceInfo> newServices = wsModel.runReadAction(
                        new MetadataModelAction<WebservicesMetadata, Map<String, ServiceInfo>>() {

                    public Map<String, ServiceInfo> run(WebservicesMetadata metadata) {
                        Map<String, ServiceInfo> result = new HashMap<String, ServiceInfo>();
                        Webservices webServices = metadata.getRoot();
                        for (WebserviceDescription wsDesc : webServices.getWebserviceDescription()) {
                            PortComponent[] ports = wsDesc.getPortComponent();
                            for (PortComponent port : ports) {
                                // key = imlpementation class package name
                                // value = service name
                                QName portName = port.getWsdlPort();
                                result.put(port.getDisplayName(),
                                           new ServiceInfo(wsDesc.getWebserviceDescriptionName(),
                                                        (portName == null ? null : portName.getLocalPart()),
                                                        port.getDisplayName(),
                                                        wsDesc.getWsdlFile()));
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

                for (JaxWsService s : oldJaxWsServices) {
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
                    ServiceInfo serviceInfo = newServices.get(key);
                    if (serviceInfo.getWsdlLocation() == null) {
                        jaxWsSupport.addService(new JaxWsService(serviceInfo.getServiceName(), key));
                    } else {
                        JaxWsService service = new JaxWsService(serviceInfo.getServiceName(), key);
                        String wsdlLocation = serviceInfo.getWsdlLocation();
                        service.setWsdlLocation(wsdlLocation);
                        if (wsdlLocation.startsWith("WEB-INF/wsdl/")) {
                            service.setLocalWsdl(wsdlLocation.substring(13));
                        } else if (wsdlLocation.startsWith("META-INF/wsdl/")) {
                            service.setLocalWsdl(wsdlLocation.substring(14));
                        } else {
                            service.setLocalWsdl(wsdlLocation);
                        }
                        if (serviceInfo.getPortName() != null) {
                            service.setPortName(serviceInfo.getPortName());
                        }
                        jaxWsSupport.addService(service);
                    }
                }
            } catch (java.io.IOException ioe) {
                ioe.printStackTrace();
            }

        }
    }

    private void detectWsdlClients(Project prj, JAXWSLightSupport jaxWsSupport, FileObject wsdlFolder)  {
        List<WsimportPomInfo> candidates = MavenModelUtils.getWsdlFiles(prj);
        if (candidates.size() > 0) {
            for (WsimportPomInfo candidate : candidates) {
                String wsdlPath = candidate.getWsdlPath();
                if (isClient(prj, jaxWsSupport, wsdlPath)) {
                    JaxWsService client = new JaxWsService(wsdlPath, false);
                    if (candidate.getHandlerFile() != null) {
                        client.setHandlerBindingFile(candidate.getHandlerFile());
                    }
                    jaxWsSupport.addService(client);
                }
            }
        } else {
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

    private List<JaxWsService> getJaxWsClients(Project prj, JAXWSLightSupport jaxWsSupport, FileObject wsdlFolder) {
        List<WsimportPomInfo> canditates = MavenModelUtils.getWsdlFiles(prj);
        List<JaxWsService> clients = new ArrayList<JaxWsService>();
        for (WsimportPomInfo candidate : canditates) {
            String wsdlPath = candidate.getWsdlPath();
            if (isClient(prj, jaxWsSupport, wsdlPath)) {
                JaxWsService client = new JaxWsService(wsdlPath, false);
                if (candidate.getHandlerFile() != null) {
                    client.setHandlerBindingFile(candidate.getHandlerFile());
                }
                clients.add(client);
            }
        }
        return clients;
    }

    private boolean isClient(Project prj, JAXWSLightSupport jaxWsSupport, String localWsdlPath) {
        Preferences prefs = ProjectUtils.getPreferences(prj, MavenWebService.class,true);
        if (prefs != null) {
            FileObject wsdlFo = getLocalWsdl(jaxWsSupport, localWsdlPath);
            if (wsdlFo != null) {
                // if client exists return true
                if (prefs.get(MavenWebService.CLIENT_PREFIX+wsdlFo.getName(), null) != null) {
                    return true;
                // if service doesn't exist return true
                } else if (prefs.get(MavenWebService.SERVICE_PREFIX+wsdlFo.getName(), null) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    private FileObject getLocalWsdl(JAXWSLightSupport jaxWsSupport, String localWsdlPath) {
        FileObject localWsdlocalFolder = jaxWsSupport.getLocalWsdlFolder(false);
        if (localWsdlocalFolder!=null) {
            return localWsdlocalFolder.getFileObject(localWsdlPath);
        }
        return null;
    }

    private class ServiceInfo {
        private String serviceName;
        private String portName;
        private String implClass;
        private String wsdlLocation;

        public ServiceInfo(String serviceName, String portName, String implClass, String wsdlLocation) {
            this.serviceName = serviceName;
            this.portName = portName;
            this.implClass = implClass;
            this.wsdlLocation = wsdlLocation;
        }
        
        public String getImplClass() {
            return implClass;
        }

        public void setImplClass(String implClass) {
            this.implClass = implClass;
        }

        public String getPortName() {
            return portName;
        }

        public void setPortName(String portName) {
            this.portName = portName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getWsdlLocation() {
            return wsdlLocation;
        }

        public void setWsdlLocation(String wsdlLocation) {
            this.wsdlLocation = wsdlLocation;
        }

    }

}
