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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
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
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

/**
 *
 * @author Milan Kuchtiak
 */
public class JaxRpcNodeFactory implements NodeFactory {
    private static final String WSDL_FOLDER = "wsdl"; //NOI18N
    /** Creates a new instance of WebServicesNodeFactory */
    public JaxRpcNodeFactory() {
    }
    
    public NodeList createNodes(Project project) {
        assert project != null;
        return new WsNodeList(project);
    }
    
    private static class WsNodeList implements NodeList<String> {
        
        // Web service client
        private static final String KEY_SERVICE_REFS = "serviceRefs"; // NOI18N
        private Project project;
        
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private final WsdlCreationListener wsdlListener;
        private final MetaInfListener metaInfListener;
        private FileObject wsdlFolder;
        
        public WsNodeList(Project proj) {
            project = proj;
            this.metaInfListener = new MetaInfListener();
            this.wsdlListener = new WsdlCreationListener();
        }
        
        public List keys() {
            List<String> result = new ArrayList<String>();
            WebServicesClientSupport wscs = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
            
            if (wscs  != null ) {
                FileObject wsdlFolder = wscs.getWsdlFolder();
                if( wsdlFolder != null){
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
            if(key.equals(KEY_SERVICE_REFS)){
                FileObject clientRoot = project.getProjectDirectory();
                WebServicesClientView clientView = WebServicesClientView.getWebServicesClientView(clientRoot);
                if (clientView != null) {
                    WebServicesClientSupport wss = WebServicesClientSupport.getWebServicesClientSupport(clientRoot);
                    if (wss!=null) {
                        FileObject wsdlFolder = wss.getWsdlFolder();
                        if (wsdlFolder!=null) {
                            FileObject[] children = wsdlFolder.getChildren();
                            boolean foundWsdl = false;
                            for (int i=0;i<children.length;i++) {
                                if (children[i].getExt().equalsIgnoreCase(WSDL_FOLDER)) { //NOI18N
                                    foundWsdl=true;
                                    break;
                                }
                            }
                            if (foundWsdl) {
                                return clientView.createWebServiceClientView(wsdlFolder);
                            }
                        }
                    }
                }
            }
            return null;
        }
        
        public void addNotify() {
            
            Sources sources = (Sources)project.getLookup().lookup(Sources.class);
            if (sources!=null) {
                SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (groups!=null && groups.length>0) {
                    FileObject srcDir = groups[0].getRootFolder();
                    srcDir.addFileChangeListener(WeakListeners.create(FileChangeListener.class, metaInfListener, srcDir));
                    FileObject metaInf = srcDir.getFileObject("META-INF");
                    if (metaInf!=null) metaInf.addFileChangeListener(WeakListeners.create(FileChangeListener.class, metaInfListener, metaInf));
                }
                
            }
            FileObject projectDir = project.getProjectDirectory();
            FileObject webInf = projectDir.getFileObject("web/WEB-INF");
            if(webInf != null){
                webInf.addFileChangeListener(WeakListeners.create(FileChangeListener.class, metaInfListener, webInf));
            }
            
            //XXX: Not very nice, the wsdlFolder should be hold by this class because it listens on it
            WebServicesClientSupport wsClientSupportImpl = WebServicesClientSupport.getWebServicesClientSupport(project.getProjectDirectory());
            try {
                if (wsClientSupportImpl != null) {
                    wsdlFolder = wsClientSupportImpl.getWsdlFolder(false);
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            if (wsdlFolder != null) {
                wsdlFolder.addFileChangeListener(WeakListeners.create(FileChangeListener.class,wsdlListener, wsdlFolder));
            }
        }
        
        public void removeNotify() {
            Sources sources = (Sources)project.getLookup().lookup(Sources.class);
            if (sources!=null) {
                SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (groups!=null && groups.length>0) {
                    FileObject srcDir = groups[0].getRootFolder();
                    srcDir.removeFileChangeListener(metaInfListener);
                    FileObject metaInf = srcDir.getFileObject("META-INF");
                    if (metaInf!=null) metaInf.removeFileChangeListener(metaInfListener);
                }
                
            }
            
            if (wsdlFolder != null) {
                wsdlFolder.removeFileChangeListener(wsdlListener);
            }
        }
        private final class WsdlCreationListener extends FileChangeAdapter {
            public void fileDataCreated(FileEvent fe) {
                if (WSDL_FOLDER.equalsIgnoreCase(fe.getFile().getExt())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            fireChange();
                        }
                    });
                }
            }
            
            public void fileDeleted(FileEvent fe) {
                if (WSDL_FOLDER.equalsIgnoreCase(fe.getFile().getExt())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            fireChange();
                        }
                    });
                } else if (fe.getFile().isFolder() && WSDL_FOLDER.equals(fe.getFile().getName())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            fireChange();
                        }
                    });
                }
            }
        }
        
        private final class MetaInfListener extends FileChangeAdapter {
            
            public void fileFolderCreated(FileEvent fe) {
                FileObject f = fe.getFile();
                if (f.isFolder() && WSDL_FOLDER.equals(f.getName())) {
                    f.addFileChangeListener(WeakListeners.create(FileChangeListener.class, wsdlListener , f));
                } else if (f.isFolder() && "META-INF".equals(f.getName())) { //NOI18N
                    f.addFileChangeListener(WeakListeners.create(FileChangeListener.class, metaInfListener, f));
                }
            }
            
            public void fileDeleted(FileEvent fe) {
                if (fe.getFile().isFolder() && WSDL_FOLDER.equals(fe.getFile().getName())) {
                    fe.getFile().removeFileChangeListener(wsdlListener);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            fireChange();
                        }
                    });
                } else if (fe.getFile().isFolder() && "META-INF".equals(fe.getFile().getName())) { //NOI18N
                    fe.getFile().removeFileChangeListener(metaInfListener);
                }
            }
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
