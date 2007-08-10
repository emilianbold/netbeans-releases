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
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEvent;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEventListener;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEventListenerAdapter;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.nodes.Node;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;

/**
 *
 * @author gpatil
 */
public class JAXBNodeFactory implements NodeFactory {
    public static final String JAXB_NODE_NAME = "JAXB Bindings" ; // NOI18N
    
    public JAXBNodeFactory() {
    }
    
    public synchronized NodeList<String> createNodes(Project project) {
        return new JAXBRootNodeList(project); 
    }
    
    private class JAXBRootNodeList  implements NodeList<String> {
        private Project project;
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private JAXBWizEventListener modelListener = new ModelListener();
        
        private List<String> rootKeys = null;
        
        public JAXBRootNodeList(Project prj){
            this.project = prj;
            rootKeys = new ArrayList<String>();
            ProjectHelper.addModelListener(prj, modelListener);
            updateKeys();
        }
        
        private synchronized void updateKeys(){
            Schemas scs = ProjectHelper.getXMLBindingSchemas(project);        
            rootKeys.clear();
            if (scs != null && scs.sizeSchema() > 0){
                rootKeys.add(JAXB_NODE_NAME);
            }            
        }

        private synchronized void updateKeys(Schemas scs){
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
        }
        
        public void removeNotify() {
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
        
        private final class ModelListener extends JAXBWizEventListenerAdapter {

            @Override
            public void bindingAdded(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateKeys((Schemas) event.getSource());    
                    fireChange();
                }
            }
            
            @Override
            public void bindingDeleted(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateKeys((Schemas) event.getSource());    
                    fireChange();
                }
            }

            @Override
            public void configFileEdited(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateKeys((Schemas) event.getSource());    
                    fireChange();
                }
            }            
        }
    }
}
