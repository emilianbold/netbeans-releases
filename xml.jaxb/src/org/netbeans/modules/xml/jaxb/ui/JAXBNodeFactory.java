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
package org.netbeans.modules.xml.jaxb.ui;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.nodes.Node;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;

/**
 *
 * @author gpatil
 */
public class JAXBNodeFactory implements NodeFactory {
    public static String JAXB_NODE_NAME = "JAXB Bindings" ;
    private static Logger logger = Logger.getLogger(
                                              JAXBNodeFactory.class.getName());
    public JAXBNodeFactory() {
    }
    
    public synchronized NodeList<String> createNodes(Project project) {
        return new JAXBRootNodeList(project); 
    }
    
    private class JAXBRootNodeList  implements NodeList<String> {
        private Project project;
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private JaxbChangeListener jaxbListener = new JaxbChangeListener();
        private List<String> rootKeys = null;
        
        public JAXBRootNodeList(Project prj){
            this.project = prj;
            rootKeys = new ArrayList<String>();
            ProjectHelper.addModelListner(project, jaxbListener);            
            updateKeys();
        }
        
        private synchronized void updateKeys(){
            Schemas scs = ProjectHelper.getXMLBindingSchemas(project);        
            rootKeys.clear();
            if (scs != null && scs.sizeSchema() > 0){
                rootKeys.add(JAXB_NODE_NAME);
            }            
        }
        
        public List<String> keys() {
            List<String> immutable = Collections.unmodifiableList(this.rootKeys);
            return immutable;
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        public void addNotify() {
            //ProjectHelper.addModelListner(project, jaxbListener);
        }
        
        public void removeNotify() {
            //ProjectHelper.removeModelListner(project, jaxbListener);
        }
        
        public synchronized Node node(String key) {
            Node ret = null;
            if (JAXB_NODE_NAME.equals(key)){
                ret = new JAXBWizardRootNode(this.project);
            }
            return ret;
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
        
        private final class JaxbChangeListener extends FileChangeAdapter {
            private void refreshNodes(){
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            JAXBRootNodeList.this.rootKeys.clear();
                            fireChange();
                        } catch (Exception ex ){
                            logger.log(Level.WARNING, "refreshing root nodes.", ex);
                        }
                        
                        try {
                            updateKeys();
                        } catch (Exception ex){
                            logger.log(Level.WARNING, "refreshing root nodes.", ex);
                        }
                        fireChange();
                    }
                });
            }
            
            public void fileChanged(FileEvent fe) {
                refreshNodes();
            }
            
            public void fileRenamed(FileEvent fe) {
                refreshNodes();
            }
            
            public void fileDataCreated(FileEvent fe) {
                // New file is created, check if config file is created.
                FileObject fo = ProjectHelper.getFOForBindingConfigFile(project);
                if ((fo != null) && (fo.isValid())){
                    // Remove listening on folder, add for the file
                    ProjectHelper.removeModelListner(project, jaxbListener);
                    ProjectHelper.addModelListner(project, jaxbListener);
                    refreshNodes();
                } else {
                    logger.log(Level.INFO, "False config create event.");
                }
            }
            
            public void fileDeleted(FileEvent fe) {
                refreshNodes();
            }
        }
    }
}
