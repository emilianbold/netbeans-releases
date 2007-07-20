/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.rest.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/** Lookup Provider for WS Support
 *
 * @author mkuchtiak
 */
public class WebRestSupportLookupProvider implements LookupProvider {
    
    /** Creates a new instance of JaxWSLookupProvider */
    public WebRestSupportLookupProvider() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project prj = baseContext.lookup(Project.class);
        
        ProjectOpenedHook openhook = new ProjectOpenedHook() {
            
            PropertyChangeListener pcl;
            
            protected void projectOpened() {
                System.out.println("REST web project opened");
                RestSupport support = prj.getLookup().lookup(RestSupport.class);
                
                if (support!=null) {
                    final MetadataModel<RestServicesMetadata> wsModel = support.getRestServicesMetadataModel();
                    try {
                        wsModel.runReadActionWhenReady(new MetadataModelAction<RestServicesMetadata, Void>() {
                            public Void run(final RestServicesMetadata metadata) {
                                RestServices restServices = metadata.getRoot();
                                System.out.println("restServices = " + restServices);
                                pcl = new RestServicesChangeListener(wsModel, prj);
                                restServices.addPropertyChangeListener(pcl);
                                
                                for (RestServiceDescription desc : restServices.getRestServiceDescription()) {
                                    System.out.println("desc = " + desc);
                                }
                                return null;
                            }
                        });
                    } catch (java.io.IOException ex) {
                        
                    }
                }
            }
            
            protected void projectClosed() {
                RestSupport support = prj.getLookup().lookup(RestSupport.class);
                
                if (support!=null) {
                    final MetadataModel<RestServicesMetadata> wsModel = support.getRestServicesMetadataModel();
                    try {
                        wsModel.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                            public Void run(final RestServicesMetadata metadata) {
                                RestServices RestServices = metadata.getRoot();
                                RestServices.removePropertyChangeListener(pcl);
                                return null;
                            }
                        });
                    } catch (java.io.IOException ex) {
                        
                    }
                }
            }
        };
        
        //ProjectRestServiceNotifier servicesNotifier = new ProjectRestServiceNotifier(prj);
        return Lookups.fixed(new Object[] {openhook});
    }
    
    private class RestServicesChangeListener implements PropertyChangeListener {
        MetadataModel<RestServicesMetadata> wsModel;
        Project prj;
        
        private RequestProcessor.Task updateRestSvcTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                updateRestServices();
            }
        });
        
        RestServicesChangeListener(MetadataModel<RestServicesMetadata> wsModel, Project prj) {
            this.wsModel=wsModel;
            this.prj=prj;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            //requestModelUpdate();
            updateRestSvcTask.schedule(100);
        }
        
        private void updateRestServices() {
            System.out.println("updating rest services");
            try {
                wsModel.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                    public Void run(RestServicesMetadata metadata) {
                        RestServices root = metadata.getRoot();
                        for (RestServiceDescription desc : root.getRestServiceDescription()) {
                            System.out.println("desc = " + desc);
                           
                        }
                        
                        return null;
                    }
                });
            } catch (IOException ex) {
                
            }
//            try {
//               Map<String,String> newServices = wsModel.runReadAction(new MetadataModelAction<RestServicesMetadata, Map<String,String>>() {
//                    public Map<String,String> run(RestServicesMetadata metadata) {
//                        Map<String,String> result = new HashMap<String,String>();
//                        RestServices restServices = metadata.getRoot();
//                        for (RestServiceDescription wsDesc:restServices.getRestServiceDescription()) {
//                            PortComponent[] ports = wsDesc.getPortComponent();
//                            for (PortComponent port:ports) {
//                                result.put(port.getDisplayName(), wsDesc.getDisplayName());
//                            }
//                            
//                        }
//                        return result;
//                    }
//                });
//                
//                final JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
//                if (jaxWsModel!=null) {
//                    // create list of all existing services (from java)
//                    Map<String,String> oldServices = new HashMap<String,String>();
//                    Service[] allServices = jaxWsModel.getServices();
//                    
//                    for (Service s:allServices) {
//                        // add only services created from java
//                        if (s.getWsdlUrl() == null) {
//                            oldServices.put(s.getImplementationClass(),s.getName());
//                        }
//                    }
//                    // compare new services with existing
//                    
//                    // looking for common services (implementationClass)
//                    Set<String> commonServices = new HashSet<String>();
//                    Set<String> keys1 = oldServices.keySet();
//                    Set<String> keys2 = newServices.keySet();
//                    for(String key:keys1) {
//                        if (keys2.contains(key)) commonServices.add(key);
//                    }
//                    
//                    for (String key:commonServices) {
//                        oldServices.remove(key);
//                        newServices.remove(key);
//                    }
//                    // remove old services
//                    boolean needToSave =false;
//                    for (String key:oldServices.keySet()) {
//                        jaxWsModel.removeService(oldServices.get(key));
//                        needToSave=true;
//                    }
//                    // add new services
//                    for (String key:newServices.keySet()) {
//                        // add only if doesn't exists
//                        if (jaxWsModel.findServiceByImplementationClass(key) == null) {
//                            try {
//                                jaxWsModel.addService(newServices.get(key), key);
//                                needToSave=true;
//                            } catch (ServiceAlreadyExistsExeption ex) {
//                                // TODO: need to handle this
//                            }
//                        }
//                    }
//                    if (needToSave) {
//                        ProjectManager.mutex().writeAccess(new Runnable() {
//                            public void run() {
//                                try {
//                                    jaxWsModel.write();
//                                } catch (IOException ex) {
//                                    ErrorManager.getDefault().notify(ex);
//                                }
//                            }
//                            
//                        });
//                    }
//                }
//            } catch(java.io.IOException ioe) {
//                ErrorManager.getDefault().notify(ioe);
//            }
        }
    }
    
}