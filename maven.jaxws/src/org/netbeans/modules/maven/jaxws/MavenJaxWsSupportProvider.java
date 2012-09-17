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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
class MavenJaxWsSupportProvider implements JAXWSLightSupportProvider, PropertyChangeListener {

    private static final RequestProcessor MAVEN_WS_RP =
            new RequestProcessor("MavenJaxWsSupportProvider.WS_REQUEST_PROCESSOR"); //NOI18N

    private RequestProcessor.Task pomChangesTask = MAVEN_WS_RP.create(new Runnable() {

        @Override
        public void run() {
            reactOnPomChanges();
        }
    });
    
    private static final Logger LOG = Logger.getLogger(MavenJaxWsLookupProvider.class.getName());

    private JAXWSLightSupport jaxWsSupport;
    private PropertyChangeListener pcl;
    private NbMavenProject mp;
    private Project prj;
    private volatile String serverInstance; 
    //private MetadataModel<WebservicesMetadata> wsModel;

    MavenJaxWsSupportProvider(final Project prj, final JAXWSLightSupport jaxWsSupport) {
        this.prj = prj;
        this.jaxWsSupport = jaxWsSupport;
        
        final Lookup lookup = prj.getLookup();
        final Result<J2eeModuleProvider> result = lookup.lookupResult(J2eeModuleProvider.class);
        final LookupListener listener = new LookupListener(){

            @Override
            public void resultChanged( LookupEvent event ) {
                synchronized (result) {
                    LOG.log(Level.INFO, "Maven project lookup is changed"); // NOI18N
                    result.notifyAll();
                }
            }
            
        };
        result.addLookupListener( listener );
        LOG.log(Level.INFO, "Lookup listener is added into the Maven project");// NOI18N

        MAVEN_WS_RP.post(new Runnable() {

            @Override
            public void run() {
                registerPCL();
                LOG.log(Level.INFO, "Inside Maven WS request processor");   // NOI18N
                
                synchronized (result) {
                    while (lookup.lookup(J2eeModuleProvider.class) == null) {
                        try {
                            LOG.log(Level.INFO, 
                                    "Wait in cycle for J2eeModuleProvider instance in Maven lookup");// NOI18N
                            result.wait(1000);
                        }
                        catch( InterruptedException e ){
                            LOG.log(Level.INFO, "Lookup change wait is interrupted", e); //NOI18N
                        }
                    }
                    result.removeLookupListener(listener);
                }
                LOG.log(Level.INFO, "Get out of waiting J2eeModuleProvider instance cycle, listener is removed");// NOI18N
                J2eeModuleProvider provider = lookup.lookup(J2eeModuleProvider.class);
                LOG.log(Level.INFO, "J2eeModuleProvider instance :"+provider);// NOI18N
                
                MetadataModel<WebservicesMetadata> model = 
                        jaxWsSupport.getWebservicesMetadataModel();
                LOG.log(Level.INFO, "WS metadata model : "+model);       // NOI18N
                if (model != null) {
                    registerAnnotationListener(model);
                }
                serverInstance = provider== null ? null : 
                    provider.getServerInstanceID();
                //wsModel = model;
            }

        });
    }

    @Override
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

                @Override
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

    /*void unregisterAnnotationListener() {
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
    }*/

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
            pomChangesTask.schedule(1000);
        }
    }

    private void reactOnPomChanges() {
        WSUtils.updateClients(prj, jaxWsSupport);
        List<JaxWsService> services = jaxWsSupport.getServices();
        if (services.size() > 0) {
            J2eeModuleProvider provider = prj.getLookup().lookup( 
                    J2eeModuleProvider.class);
            String serverInstanceID = provider== null ? null : 
                provider.getServerInstanceID();
            boolean instanceChanged = false;
            if ( serverInstanceID == null ){
                if ( serverInstance != null ){
                    instanceChanged = true;
                }
            }
            else if (!serverInstanceID.equals( serverInstance)){
                instanceChanged = true;
            }
            if ( instanceChanged ){
                serverInstance = serverInstanceID;
                MavenModelUtils.reactOnServerChanges(prj);
            }
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

            @Override
            public void run() {
                updateJaxWs();
            }
        });

        WebservicesChangeListener(JAXWSLightSupport jaxWsSupport, MetadataModel<WebservicesMetadata> wsModel) {
            this.jaxWsSupport = jaxWsSupport;
            this.wsModel = wsModel;
            updateJaxWsTask.schedule(1000);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //requestModelUpdate();
            updateJaxWsTask.schedule(1000);
        }
        
        private void updateJaxWs() {
            try {
                final Map<String, ServiceInfo> newServices = wsModel.runReadAction(
                        new MetadataModelAction<WebservicesMetadata, Map<String, ServiceInfo>>() {

                    @Override
                    public Map<String, ServiceInfo> run(WebservicesMetadata metadata) {
                        Map<String, ServiceInfo> result = new HashMap<String, ServiceInfo>();
                        Webservices webServices = metadata.getRoot();
                        LOG.log(Level.INFO, "Inside update JAX-WS , WS root :"+
                                webServices+" ; number of descriptions :"+
                                webServices.getWebserviceDescription().length);// NOI18N
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
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        doUpdateJaxWs(newServices);        
                    }
                };
                jaxWsSupport.runAtomic(runnable);
                
            } catch (java.io.IOException ioe) {
                ioe.printStackTrace();
            }

        }

        private void doUpdateJaxWs( Map<String, ServiceInfo> newServices ) {
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
