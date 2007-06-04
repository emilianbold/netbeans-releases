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

package org.netbeans.modules.websvc.jaxrpc.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.webservices.WebServicesView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Milan Kuchtiak
 */
public class JaxRpcWebServiceNodeFactory implements NodeFactory {
    private static final String WS_DD = "webservices.xml"; //NOI18N
    /** Creates a new instance of WebServicesNodeFactory */
    public JaxRpcWebServiceNodeFactory() {
    }
    
    public NodeList createNodes(Project project) {
        assert project != null;
        return new WsNodeList(project);
    }
    
    private static class WsNodeList implements NodeList<String> {
        
        // Web Services
        private static final String KEY_SERVICES = "web_services"; // NOI18N

        private Project project;
        
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

        private final WSDDCreationListener wsdlListener;

        private FileObject wsddFolder;
        
        public WsNodeList(Project proj) {
            project = proj;
            this.wsdlListener = new WSDDCreationListener();
        }
        
        public List keys() {
            List<String> result = new ArrayList<String>();
            WebServicesSupport wss = WebServicesSupport.getWebServicesSupport(project.getProjectDirectory());
            if (wss != null && !wss.getServices().isEmpty()) {
                result.add(KEY_SERVICES);
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
                WebServicesView view = WebServicesView.getWebServicesView(project.getProjectDirectory());
                if(view!=null) {
                    Sources sources = (Sources)project.getLookup().lookup(Sources.class);
                    if (sources!=null) {
                        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                        if (groups!=null && groups.length>0) {
                            FileObject srcDir = groups[0].getRootFolder();
                            return view.createWebServicesView(srcDir);
                        }
                    }
                }
            }
            return null;
        }
        
        public void addNotify() {
            WebServicesSupport wss = WebServicesSupport.getWebServicesSupport(project.getProjectDirectory());
            if(wss!=null) {
                wsddFolder = wss.getWsDDFolder();
                if(wsddFolder !=null) {
                    wsddFolder.addFileChangeListener(wsdlListener);
                }
            }
        }
        
        public void removeNotify() {
            if (wsddFolder != null) {
                wsddFolder.removeFileChangeListener(wsdlListener);
            }
        }
        private final class WSDDCreationListener extends FileChangeAdapter {
            
            public void fileDataCreated(FileEvent fe) {
                if (WS_DD.equalsIgnoreCase(fe.getFile().getNameExt())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            fireChange();
                        }
                    });
                }
            }
            
            public void fileDeleted(FileEvent fe) {
                if (WS_DD.equalsIgnoreCase(fe.getFile().getNameExt())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            fireChange();
                        }
                    });
                }
            }
        }
        
    }
    
}
