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

package org.netbeans.modules.websvc.axis2.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.axis2.Axis2ModelProvider;
import org.netbeans.modules.websvc.axis2.config.model.Axis2;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Model;
import org.netbeans.modules.websvc.axis2.config.model.Service;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;

/**
 *
 * @author Milan Kuchtiak
 */
public class AxisNodeFactory implements NodeFactory {
    
    /** Creates a new instance of WebServicesNodeFactory */
    public AxisNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        assert p != null;
        return new WsNodeList(p);
    }
    
    private static class WsNodeList implements NodeList<String> {
        // Axis2 Services
        private static final String KEY_SERVICES = "axis2_services"; // NOI18N
        
        private Project project;
        
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        
        private Axis2Model axis2Model;
        private PropertyChangeListener axis2ModelProviderListener, axis2ModelListener;
        
        public WsNodeList(Project proj) {
            project = proj;
            axis2ModelProviderListener = new Axis2ModelProviderListener();
            axis2ModelListener = new Axis2ModelListener();
        }
        
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            if (axis2Model != null) {
                if (axis2Model.getRootComponent().getServices().size() > 0) {                   
                    result.add(KEY_SERVICES);
                    return result;
                }
            }
            return result;
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
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
                return new Axis2Node(project);
            }
            return null;
        }
        
        public void addNotify() {
            Axis2ModelProvider axis2ModelProvider = project.getLookup().lookup(Axis2ModelProvider.class);
            if (axis2ModelProvider != null) {
                axis2ModelProvider.addPropertyChangeListener(axis2ModelProviderListener);
                axis2Model = axis2ModelProvider.getAxis2Model();
                if (axis2Model != null) {
                    axis2Model.getRootComponent().addPropertyChangeListener(axis2ModelListener);
                }
            }
        }
        
        public void removeNotify() {
            Axis2ModelProvider axis2ModelProvider = project.getLookup().lookup(Axis2ModelProvider.class);
            if (axis2ModelProvider != null) {
                axis2ModelProvider.removePropertyChangeListener(axis2ModelProviderListener);
            } if (axis2Model != null) {
                axis2Model.getRootComponent().removePropertyChangeListener(axis2ModelListener);
            }
        }
        
        private class Axis2ModelProviderListener implements PropertyChangeListener {

            public void propertyChange(PropertyChangeEvent evt) {
                if (Axis2ModelProvider.PROP_AXIS2.equals(evt.getPropertyName())) {
                    Axis2Model oldModel = (Axis2Model)evt.getOldValue();
                    axis2Model = (Axis2Model)evt.getNewValue();
                    if (oldModel != null) {
                        oldModel.getRootComponent().removePropertyChangeListener(axis2ModelListener);
                    }
                    if (axis2Model != null) {
                        axis2Model.getRootComponent().addPropertyChangeListener(axis2ModelListener);
                    }
                }
            }

        }
        private class Axis2ModelListener implements PropertyChangeListener {

            public void propertyChange(PropertyChangeEvent evt) {
                // refresh keyes only if first service is created or last service is removed
                if (evt.getSource() instanceof Axis2) {
                    Axis2 axis2 = (Axis2)evt.getSource();
                    Object oldValue = evt.getOldValue();
                    Object newValue = evt.getNewValue();
                    if (oldValue == null && newValue instanceof Service && axis2.getServices().size() == 1) {
                        WsNodeList.this.fireChange();
                    } else if (oldValue instanceof Service && newValue == null && axis2.getServices().size() == 0) {
                        WsNodeList.this.fireChange();
                    }
                }
                
            }

        }
    }    
}
