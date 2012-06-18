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
public class MavenJaxWsServicesProvider implements WebServiceDataProvider, PropertyChangeListener {
    private JAXWSLightSupport jaxWsSupport;
    private Project prj;
    private List<WebService> providers = new LinkedList<WebService>();
    private List<WebService> consumers = new LinkedList<WebService>();

    /** Constructor.
     *
     * @param prj project
     * @param jaxWsSupport JAXWSLightSupport
     */
    public MavenJaxWsServicesProvider(Project prj, JAXWSLightSupport jaxWsSupport) {
        this.jaxWsSupport = jaxWsSupport;
        this.prj = prj;
        jaxWsSupport.addPropertyChangeListener(this);
    }

    @Override
    public List<WebService> getServiceProviders() {
        return providers;
    }

    @Override
    public List<WebService> getServiceConsumers() {
        return consumers;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        jaxWsSupport.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        jaxWsSupport.removePropertyChangeListener(pcl);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (JAXWSLightSupport.PROPERTY_SERVICE_ADDED.equals(evt.getPropertyName())) {
            MavenWebService mavenService = new MavenWebService((JaxWsService) evt.getNewValue(), prj);
            WebService webService = WebServiceFactory.createWebService(mavenService);
            if (webService.isServiceProvider()) {
                providers.add(webService);
            } else {
                consumers.add(webService);
            }
        } else if (JAXWSLightSupport.PROPERTY_SERVICE_REMOVED.equals(evt.getPropertyName())) {
            JaxWsService jaxWsService = (JaxWsService) evt.getOldValue();
            if (jaxWsService.isServiceProvider()) {
                String implClass = jaxWsService.getImplementationClass();
                for (WebService service : providers) {
                    if (implClass.equals(service.getIdentifier())) {
                        providers.remove(service);
                        break;
                    }
                }
            } else {
                String clientId = jaxWsService.getId();
                for (WebService client : consumers) {
                    if (clientId != null && clientId.equals(client.getIdentifier())) {
                        consumers.remove(client);
                        break;
                    }
                }
            }
        }
    }

}
