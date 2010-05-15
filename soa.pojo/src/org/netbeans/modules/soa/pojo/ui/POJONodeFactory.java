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

package org.netbeans.modules.soa.pojo.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEvent;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEventListener;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEventListenerAdapter;
import org.netbeans.modules.soa.pojo.schema.POJOProviders;
import org.netbeans.modules.soa.pojo.schema.POJOs;
import org.netbeans.modules.soa.pojo.util.Util;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;

/**
 *
 * @author gpatil
 * @author Sreenivasan Genipudi
 */
//@NodeFactory.Registration(projectType={"org-netbeans-modules-java-j2seproject"} ,position=281)
public class POJONodeFactory  implements NodeFactory {
    public static final String POJOs_NODE_NAME = "POJOs" ; // NOI18N
//  To show consumer node uncomment this code.    
 //   public static final String POJOs_CONSUMER_NODE_NAME = "POJO Consumer" ; // NOI18N    
    
    public POJONodeFactory() {
    }
    
    public synchronized NodeList<String> createNodes(Project project) {
        return new POJORootNodeList(project); 
    }
    
    private class POJORootNodeList  implements NodeList<String> {
        private Project project;
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private POJOsEventListener modelListener = new ModelListener();
        
        private List<String> rootKeys = null;
        
        public POJORootNodeList(Project prj){
            this.project = prj;
            rootKeys = new ArrayList<String>();
            Util.addModelListener(prj, modelListener);
            updateKeys();
        }
        
        private synchronized void updateKeys(){
            POJOs pjs = Util.getPOJOs(project);
            rootKeys.clear();
            POJOProviders ps = pjs.getPOJOProviders();
            if (ps != null && ps.sizePOJOProvider() > 0){
                rootKeys.add(POJOs_NODE_NAME);
            } 
        /*  To show consumer node uncomment this code.  
            if (scs.getPOJOConsumers() != null) {
                rootKeys.add(POJOs_CONSUMER_NODE_NAME);                        
            }*/
        }

        private synchronized void updateKeys(POJOs pojos){
            rootKeys.clear();
            POJOProviders ps = pojos.getPOJOProviders();
            if (ps != null && ps.sizePOJOProvider() > 0){
                rootKeys.add(POJOs_NODE_NAME);
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
            if (POJOs_NODE_NAME.equals(key)){
                ret = new POJOsRootNode(this.project);
            }//  To show consumer node uncomment this code. 
            /*
            else if ( POJOs_CONSUMER_NODE_NAME.equals(key) ) {
                ret = new POJOConsumersRootNode(this.project);
            }*/
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
        
        private final class ModelListener extends POJOsEventListenerAdapter {

            @Override
            public void pojoAdded(POJOsEvent event) {
                POJOs pojos = null;
                if (event.getSource() instanceof POJOs ) {
                    pojos = (POJOs) event.getSource();
                    updateKeys((POJOs) event.getSource());    
                    fireChange();
                }
            }

            @Override
            public void pojoChanged(POJOsEvent event) {
                
            }
            
            @Override
            public void pojoDeleted(POJOsEvent event) {
                if (event.getSource() instanceof POJOs){
                    updateKeys((POJOs) event.getSource());    
                    fireChange();
                }
            }

            @Override
            public void configFileEdited(POJOsEvent event) {
                if (event.getSource() instanceof POJOs){
                    updateKeys((POJOs) event.getSource());    
                    fireChange();
                }
            }            
        }
    }
}