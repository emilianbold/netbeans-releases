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

package org.netbeans.modules.websvc.rest.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Lookup;
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
        final RestSupport restSupport = new WebProjectRestSupport(prj);
        
        ProjectOpenedHook openhook = new ProjectOpenedHook() {
            
            PropertyChangeListener pcl;
            
            protected void projectOpened() {
                
                final RestServicesModel wsModel = RestUtils.getRestServicesMetadataModel(prj);
                // note that some sample web project does not have java source root
                if (wsModel != null) { 
                    try {
                        // make sure REST API jar is included in project compile classpath
                        RestUtils.addRestApiJar(prj);
                        new AntFilesHelper(restSupport).initRestBuildExtension();
                                pcl = new RestServicesChangeListener(wsModel, prj);
                                wsModel.addPropertyChangeListener(pcl);
                    } catch (java.io.IOException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
                    }
                }
            }
            
            
            protected void projectClosed() {
                final RestServicesModel wsModel = RestUtils.getRestServicesMetadataModel(prj);
                if (wsModel == null) {
                    return;
                }
                try {
                    wsModel.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                        public Void run(final RestServicesMetadata metadata) {
                            RestServices RestServices = metadata.getRoot();
                            RestServices.removePropertyChangeListener(pcl);
                            return null;
                        }
                    });
                } catch (java.io.IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
        };
        
        PrivilegedTemplates templates = new PrivilegedTemplates() {
            public String[] getPrivilegedTemplates() {
                return new String[] {
                    "Templates/WebServices/RestServicesFromEntities", // NOI18N
                    "Templates/WebServices/RestServicesFromPatterns",  //NOI18N
                    "Templates/WebServices/RestClientStubs"    //NOI18N
                };
            }
        };
        
        //ProjectRestServiceNotifier servicesNotifier = new ProjectRestServiceNotifier(prj);
        return Lookups.fixed(new Object[] {restSupport, openhook, templates});
    }
    
    private class RestServicesChangeListener implements PropertyChangeListener {
        private RestServicesModel wsModel;
        private Project prj;
        private RestSupport support;
        
        RestServicesChangeListener(RestServicesModel wsModel, Project prj) {
            this.wsModel=wsModel;
            this.prj=prj;
            this.support = prj.getLookup().lookup(RestSupport.class);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            //System.out.println("updating rest services");
            try {
                final int[] serviceCount = new int[1];
                wsModel.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                    public Void run(RestServicesMetadata metadata) throws IOException {
                        serviceCount[0] = metadata.getRoot().sizeRestServiceDescription();
                        return null;
                    }
                });
                if (serviceCount[0] > 0) {
                    RestUtils.ensureRestDevelopmentReady(prj);
                } else {
                    RestUtils.removeRestDevelopmentReadiness(prj);
                }
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
            //System.out.println("done updating rest services");
        }
    }
}
