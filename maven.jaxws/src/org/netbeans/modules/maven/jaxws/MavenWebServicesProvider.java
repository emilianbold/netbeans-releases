/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.spi.WebServiceDataProvider;
import org.netbeans.modules.websvc.project.spi.WebServiceFactory;

/**
 *
 * @author mkuchtiak
 */
public class MavenWebServicesProvider implements WebServiceDataProvider, PropertyChangeListener {
    private JAXWSLightSupport jaxWsSupport;
    private Project prj;
    private List<WebService> providers = new LinkedList<WebService>();
    private List<WebService> consumers = new LinkedList<WebService>();
    
    public MavenWebServicesProvider(Project prj, JAXWSLightSupport jaxWsSupport) {
        this.jaxWsSupport=jaxWsSupport;
        this.prj = prj;
        jaxWsSupport.addPropertyChangeListener(this);
    }
    
    public List<WebService> getServiceProviders() {
        return providers;
    }

    public List<WebService> getServiceConsumers() {
        return consumers;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        jaxWsSupport.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        jaxWsSupport.removePropertyChangeListener(pcl);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("service-added".equals(evt.getPropertyName())) { //NOI18N
            MavenWebService mavenService = new MavenWebService((JaxWsService)evt.getNewValue(), prj);
            WebService webService = WebServiceFactory.createWebService(mavenService);
            if (webService.isServiceProvider()) {
                providers.add(webService);
            } else {
                consumers.add(webService);
            }
        } else if ("service-removed".equals(evt.getPropertyName())) { //NOI18N
            JaxWsService jaxWsService = (JaxWsService)evt.getOldValue();
            if (jaxWsService.isServiceProvider()) {
                String implClass = jaxWsService.getImplementationClass();
                for (WebService service: providers) {               
                    if (implClass.equals(service.getIdentifier())) {
                        providers.remove(service);
                        break;
                    }
                }
            } else {
                String wsdlFile = jaxWsService.getLocalWsdl();
                for (WebService service: consumers) {               
                    if (wsdlFile.equals(service.getIdentifier())) {
                        consumers.remove(service);
                        break;
                    }
                }
            }
        }
    }

}
