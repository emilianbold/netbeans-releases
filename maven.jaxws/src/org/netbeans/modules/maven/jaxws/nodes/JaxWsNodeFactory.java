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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.maven.jaxws.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider;
import org.netbeans.modules.websvc.project.api.WebServiceData;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author Milan Kuchtiak
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=85)
public class JaxWsNodeFactory implements NodeFactory {

    /** Creates a new instance of WebServicesNodeFactory */
    public JaxWsNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        assert p != null;
        return new WsNodeList(p);
    }
    
    private class WsNodeList implements NodeList<String>, PropertyChangeListener, LookupListener {
        // Web Services
        private static final String KEY_SERVICES = "web_services"; // NOI18N
        // Web Service Client
        private static final String KEY_SERVICE_REFS = "serviceRefs"; // NOI18N
        
        private Project project;
        private JAXWSLightSupport jaxwsSupport;
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private Lookup.Result<JAXWSLightSupportProvider> lookupResult;
        
        public WsNodeList(Project proj) {
            project = proj;
        }
        
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            if ( jaxwsSupport != null) {
                List<JaxWsService> services = jaxwsSupport.getServices();
                boolean hasServices = false;
                boolean hasClients = false;
                for (JaxWsService s:services) {
                    if (!hasServices && s.isServiceProvider()) {
                        hasServices = true;
                    } else if (!hasClients && !s.isServiceProvider()) {
                        hasClients = true;
                    }
                    if (hasServices && hasClients) break;
                }
                if (hasServices) result.add(KEY_SERVICES);
                if (hasClients) result.add(KEY_SERVICE_REFS);
            }
            return result;
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
//        
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            ArrayList<ChangeListener> list = new ArrayList<ChangeListener>();
            synchronized (this) {
                list.addAll(listeners);
            }
            Iterator<ChangeListener> it = list.iterator();
            while (it.hasNext()) {
                ChangeListener elem = it.next();
                elem.stateChanged(new ChangeEvent( this ));
            }
        }
        
        public Node node(String key) {
            if (KEY_SERVICES.equals(key)) {
                //JAXWSLightView view = JAXWSLightView.getJAXWSLightView(project.getProjectDirectory());
                //return view.createJAXWSView(project);
                WebServiceData wsData = WebServiceData.getWebServiceData(project);
                return new JaxWsRootNode(project, wsData);
            } else if (KEY_SERVICE_REFS.equals(key)) {
                WebServiceData wsData = WebServiceData.getWebServiceData(project);
                return new JaxWsClientRootNode(project, wsData);
            }
            return null;
        }
        
        public void addNotify() {
            if (jaxwsSupport == null) {
                jaxwsSupport = JAXWSLightSupport.getJAXWSLightSupport(project.getProjectDirectory());
            }
            if (jaxwsSupport != null) {
                jaxwsSupport.addPropertyChangeListener(WeakListeners.propertyChange(this, jaxwsSupport));
            } else {
                lookupResult = project.getLookup().lookupResult(JAXWSLightSupportProvider.class);
                if (lookupResult.allInstances().size() == 0) {
                    lookupResult.addLookupListener(this);
                }
            }
        }
        
        public void removeNotify() {
            if (jaxwsSupport != null) {
                jaxwsSupport.removePropertyChangeListener(WeakListeners.propertyChange(this, jaxwsSupport));
                jaxwsSupport = null;
            }
            if (lookupResult != null) {
                lookupResult.removeLookupListener(this);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fireChange();
        }
        
        public void resultChanged(LookupEvent evt) {
            jaxwsSupport = JAXWSLightSupport.getJAXWSLightSupport(project.getProjectDirectory());
            if (jaxwsSupport != null) {
                jaxwsSupport.addPropertyChangeListener(WeakListeners.propertyChange(WsNodeList.this, jaxwsSupport));
            }
        }
    }
    
}
