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
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider;

/**
 *
 * @author mkuchtiak
 */
class MavenJaxWsSupportProvider implements JAXWSLightSupportProvider {

    private JAXWSLightSupport jaxWsSupport;
    private PropertyChangeListener wsdlFolderListener;
    private NbMavenProject mp;

    MavenJaxWsSupportProvider(Project prj, JAXWSLightSupport jaxWsSupport) {
        this.jaxWsSupport = jaxWsSupport;
        mp = prj.getLookup().lookup(NbMavenProject.class);
        if (mp != null) {
            registerWsdlListener(prj, mp);
        }
    }

    public JAXWSLightSupport findJAXWSSupport() {
        return jaxWsSupport;
    }

    void registerWsdlListener(final Project prj, NbMavenProject mp) {
        if (wsdlFolderListener != null) {
            this.mp.removePropertyChangeListener(wsdlFolderListener);
        }
        this.mp = mp;
        wsdlFolderListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
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
            }
        };
        mp.addPropertyChangeListener(wsdlFolderListener);
    }

    void unregisterWsdlListener(NbMavenProject mp) {
        mp.removePropertyChangeListener(wsdlFolderListener);
    }

}
