/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.RequestProcessor;

/**
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-maven")
public class JaxWsOpenHook extends ProjectOpenedHook {

    private static final RequestProcessor METADATA_MODEL_RP =
            new RequestProcessor("JaxWsOpenHook.MAVEN_WS_METADATA_MODEL_RP"); //NOI18N
    
    private final Project prj;
    private PropertyChangeListener pcl;
    private NbMavenProject mavenProject;
    
    public JaxWsOpenHook(Project prj) {
        this.prj = prj;
    }

    protected void projectOpened() {
        final JAXWSLightSupport jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(prj.getProjectDirectory());
        if (jaxWsSupport != null) {
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
            mavenProject = prj.getLookup().lookup(NbMavenProject.class);
            if (mavenProject != null) {
                MavenJaxWsSupportProvider jaxWsSupportProvider = prj.getLookup().lookup(MavenJaxWsSupportProvider.class);
                if (jaxWsSupportProvider != null) {
                    jaxWsSupportProvider.registerWsdlListener(prj, mavenProject);
                }
            }
        }
    }

    protected void projectClosed() {
        JAXWSLightSupport jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(prj.getProjectDirectory());
        if (jaxWsSupport != null) {
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
            if (mavenProject != null) {
                MavenJaxWsSupportProvider jaxWsSupportProvider = prj.getLookup().lookup(MavenJaxWsSupportProvider.class);
                if (jaxWsSupportProvider != null) {
                    jaxWsSupportProvider.unregisterWsdlListener(mavenProject);
                }
            }
        }
    }

    private class WebservicesChangeListener implements PropertyChangeListener {

        private JAXWSLightSupport jaxWsSupport;

        private MetadataModel<WebservicesMetadata> wsModel;

        private RequestProcessor.Task updateJaxWsTask = METADATA_MODEL_RP.create(new Runnable() {

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
            updateJaxWsTask.schedule(1000);
        }

        private synchronized void updateJaxWs() {
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
                    String wsdlLocation = serviceInfo.getWsdlLocation();
                    JaxWsService service = new JaxWsService(serviceInfo.getServiceName(), key);
                    if (wsdlLocation != null && wsdlLocation.length() > 0) {
                        service.setWsdlLocation(wsdlLocation);
                        if (wsdlLocation.startsWith("WEB-INF/wsdl/")) {
                            service.setLocalWsdl(wsdlLocation.substring(13));
                        } else if (wsdlLocation.startsWith("META-INF/wsdl/")) {
                            service.setLocalWsdl(wsdlLocation.substring(14));
                        } else {
                            service.setLocalWsdl(wsdlLocation);
                        }
                        service.setWsdlUrl(WSUtils.getOriginalWsdlUrl(prj, jaxWsSupport, service.getLocalWsdl(), true));
                    }
                    service.setPortName(serviceInfo.getPortName());
                    jaxWsSupport.addService(service);
                }
            } catch (java.io.IOException ioe) {
                ioe.printStackTrace();
            }

        }
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
