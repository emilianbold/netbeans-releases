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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.api.JAXWSView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Milan Kuchtiak
 */
public class WebServicesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of WebServicesNodeFactory */
    public WebServicesNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        assert p != null;
        return new WsNodeList(p);
    }
    
    private static class WsNodeList implements NodeList<String> {
        // Web Services
        private static final String KEY_SERVICES = "web_services"; // NOI18N
        // Web Service Client
        private static final String KEY_SERVICE_REFS = "serviceRefs"; // NOI18N
        
        private Project project;
        
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private final JaxWsChangeListener jaxWsListener;
        private JaxWsModel jaxWsModel;
        
        public WsNodeList(Project proj) {
            project = proj;
            this.jaxWsListener = new JaxWsChangeListener();
        }
        
        public List<String> keys() {
            List<String> result = new ArrayList<String>();
            jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
            FileObject projectDir = project.getProjectDirectory();
            JAXWSSupport jaxwsSupport = JAXWSSupport.getJAXWSSupport(projectDir);
            JAXWSClientSupport jaxwsClientSupport = JAXWSClientSupport.getJaxWsClientSupport(projectDir);
            if (jaxWsModel != null) {
                if ( jaxwsSupport != null && jaxWsModel.getServices().length>0) {
                        result.add(KEY_SERVICES);
                }
                if ( jaxwsClientSupport != null && jaxWsModel.getClients().length>0) {
                        result.add(KEY_SERVICE_REFS);
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
                JAXWSView view = JAXWSView.getJAXWSView();
                return view.createJAXWSView(project);
            } else if (KEY_SERVICE_REFS.equals(key)) {
                JAXWSClientView view = JAXWSClientView.getJAXWSClientView();
                return view.createJAXWSClientView(project);
            }
            return null;
        }
        
        public void addNotify() {
            JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel!=null) jaxWsModel.addPropertyChangeListener(jaxWsListener);
        }
        
        public void removeNotify() {
            JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel!=null) jaxWsModel.removePropertyChangeListener(jaxWsListener);
        }
        
        
        private final class JaxWsChangeListener implements PropertyChangeListener {
            public void propertyChange(PropertyChangeEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        fireChange();
                    }
                });
            }
        }
    }
    
}
