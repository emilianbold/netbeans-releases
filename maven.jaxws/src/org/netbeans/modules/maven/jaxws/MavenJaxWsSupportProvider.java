/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
class MavenJaxWsSupportProvider implements JAXWSLightSupportProvider, PropertyChangeListener {

    private static final RequestProcessor MAVEN_WS_RP =
            new RequestProcessor("MavenJaxWsSupportProvider.WS_REQUEST_PROCESSOR"); //NOI18N

    private RequestProcessor.Task pomChangesTask = MAVEN_WS_RP.create(new Runnable() {

        public void run() {
            reactOnPomChanges();
        }
    });

    private JAXWSLightSupport jaxWsSupport;
    private PropertyChangeListener pcl;
    private NbMavenProject mp;
    private Project prj;
    private MetadataModel<WebservicesMetadata> wsModel;

    MavenJaxWsSupportProvider(final Project prj, final JAXWSLightSupport jaxWsSupport) {
        this.prj = prj;
        this.jaxWsSupport = jaxWsSupport;

        MAVEN_WS_RP.post(new Runnable() {

            public void run() {
                registerPCL();
                wsModel = jaxWsSupport.getWebservicesMetadataModel();
                if (wsModel != null) {
                    registerAnnotationListener(wsModel);
                }
            }

        });
    }

    public JAXWSLightSupport findJAXWSSupport() {
        return jaxWsSupport;
    }

    void registerPCL() {
        unregisterPCL();
        mp = prj.getLookup().lookup(NbMavenProject.class);
        mp.addPropertyChangeListener(this);
    }

    void registerAnnotationListener(final MetadataModel<WebservicesMetadata> wsModel) {
        try {
            wsModel.runReadActionWhenReady(new MetadataModelAction<WebservicesMetadata, Void>() {

                public Void run(final WebservicesMetadata metadata) {
                    Webservices webServices = metadata.getRoot();
                    if (pcl != null) {
                        webServices.removePropertyChangeListener(pcl);
                    }
                    pcl = new WebservicesChangeListener(jaxWsSupport, wsModel);
                    webServices.addPropertyChangeListener(pcl);
                    return null;
                }
            });
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    void unregisterPCL() {
        if (mp != null) {
            mp.removePropertyChangeListener(this);
        }
    }

    void unregisterAnnotationListener() {
        if (pcl != null) {
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
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
            pomChangesTask.schedule(1000);
        }
    }

    private void reactOnPomChanges() {
        WSUtils.updateClients(prj, jaxWsSupport);
        List<JaxWsService> services = jaxWsSupport.getServices();
        if (services.size() > 0) {
            MavenModelUtils.reactOnServerChanges(prj);
            if (WSUtils.isWeb(prj)) {
                for (JaxWsService s : services) {
                    if (s.isServiceProvider()) {
                        // add|remove sun-jaxws.xml and WS entries to web.xml file
                        // depending on selected target server
                        WSUtils.checkNonJSR109Entries(prj);
                        break;
                    }
                }
            }
        }
    }

    private class WebservicesChangeListener implements PropertyChangeListener {

        private JAXWSLightSupport jaxWsSupport;

        private MetadataModel<WebservicesMetadata> wsModel;

        private RequestProcessor.Task updateJaxWsTask = MAVEN_WS_RP.create(new Runnable() {

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
                                String implClass = port.getDisplayName();
                                if (WSUtils.isInSourceGroup(prj, implClass)) {
                                    QName portName = port.getWsdlPort();
                                    result.put(implClass,
                                    new ServiceInfo(
                                            wsDesc.getWebserviceDescriptionName(),
                                            (portName == null ? null : portName.getLocalPart()),
                                            implClass,
                                            wsDesc.getWsdlFile()));
                                }
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
                        FileObject wsdlFo = WSUtils.getLocalWsdl(jaxWsSupport, service.getLocalWsdl());
                        if (wsdlFo != null) {
                            service.setId(WSUtils.getUniqueId(wsdlFo.getName(), oldJaxWsServices));
                        }
                        service.setWsdlUrl(WSUtils.getOriginalWsdlUrl(prj, service.getId(), true));
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
