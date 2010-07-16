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

package org.netbeans.modules.soa.pojo.ui;

import java.awt.Image;
import java.util.HashMap;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEvent;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEventListener;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEventListenerAdapter;
import org.netbeans.modules.soa.pojo.schema.POJOConsumer;
import org.netbeans.modules.soa.pojo.schema.POJOConsumers;
import org.netbeans.modules.soa.pojo.schema.POJOs;
import org.netbeans.modules.soa.pojo.util.Util;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author sgenipudi
 */
public class POJOConsumersRootNode extends AbstractNode {
    private Project project;
    private static Action[] actions = null;
    
    public POJOConsumersRootNode(Project prj) {
        this(prj, new InstanceContent());
        this.project = prj;
        Children cs = this.getChildren();
        initActions();        
    }
    
    private POJOConsumersRootNode(Project prj, InstanceContent content) {
        super(new POJOConsumersRootNodeChildren(prj), 
                new AbstractLookup(content));
        // adds the node to our own lookup
        content.add (this);
        // adds additional items to the lookup
        content.add (prj);
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(POJOsRootNode.class,
                "LBL_POJOS_CONSUMERS_NODE"); //NOI18N
    }
    
    @Override
    public String getName() {
        return POJONodeFactory.POJOs_NODE_NAME;
    }
    
    @Override
    public Cookie getCookie(Class clz) {
        if  ( clz == Project.class ) {
            if ( project instanceof Cookie )
                return (Cookie)project;
        }
        return super.getCookie(clz);
    }
    
    
    private void initActions() {
        if ( actions == null ) {
            actions = new Action[] {
              //  SystemAction.get(POJOPackageLibrariesInSUAction.class),
              //  null,
              //  SystemAction.get(POJODisablePackageAllAction.class),
            };
        }
    }
    
    @Override
    public Action[] getActions(boolean b) {
        return actions;
    }
        
    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/soa/pojo/resources/soa_pojo_consumer_16.png" ); // No I18N
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/soa/pojo/resources/soa_pojo_consumer_16.png" );// No I18N
    }
    
    
    public static class POJOConsumersRootNodeChildren extends Children.Keys {
        private Project project;
        private POJOConsumers pojoConsumers;
        private java.util.Map<String, POJOConsumer> namePOJOConsumersMap = 
                new HashMap<String, POJOConsumer>();        
        private POJOsEventListener modelListener = new ModelListener();
        
        public POJOConsumersRootNodeChildren(Project prj) {
            super();
            project = prj;
            this.addNodify();    
        }
                
        public void addNodify() {
            POJOs pojos = Util.getPOJOs(project);
            this.namePOJOConsumersMap.clear();
            POJOConsumers pjcs = pojos.getPOJOConsumers();
            if ( pjcs != null)  {
                pojoConsumers = pjcs;
                POJOConsumer[] pjcsArry = pjcs.getPOJOConsumer();
                
                for ( POJOConsumer pjc:pjcsArry) {
                  this.namePOJOConsumersMap.put(pjc.getInterface().toString()+"|"+pjc.getOperation().toString(), pjc);
                }
            }
            updateKeys();
            super.addNotify();
            Util.addModelListener(this.project, this.modelListener);
        }
                
        public void removeNotify() {
            Util.removeModelListener(this.project, this.modelListener);
        }
        
        public void updateKeys() {            
            if (this.pojoConsumers == null){
                return ;
            }            
            this.setKeys(this.namePOJOConsumersMap.keySet());
        }

        private void updateBindingKeys(POJOConsumers ss) {
            this.pojoConsumers = ss;
            this.namePOJOConsumersMap.clear();
            POJOConsumer[] sc = this.pojoConsumers.getPOJOConsumer();
            for (POJOConsumer s: sc){
                this.namePOJOConsumersMap.put(s.getInterface().toString()+"|"+s.getOperation().toString(), s);
            }            

            updateKeys();                
        }
        
        protected Node[] createNodes(Object key) {
            Node[] nodes = null;
            if ( key instanceof String ) {
                POJOConsumer schema = this.namePOJOConsumersMap.get((String)key);                
                POJOConsumerNode bindingNode = null;
                bindingNode = new POJOConsumerNode(project, schema);
                nodes = new Node[] {
                    bindingNode,
                };
            }
            
            return nodes;
        }
        
        private final class ModelListener extends POJOsEventListenerAdapter {
            @Override
            public void pojoAdded(POJOsEvent event) {
                Object eventSrcObj = event.getSource();
                if (eventSrcObj instanceof POJOs){       
                    Object pojoAddObj = event.getNewValue();
                    if ( pojoAddObj instanceof POJOConsumer) {
                        POJOConsumer nCons = (POJOConsumer)pojoAddObj;
                        POJOs pojos = (POJOs) eventSrcObj;
                        POJOConsumers pjcs = pojos.getPOJOConsumers();
                        if ( pjcs == null) {
                             pjcs = new POJOConsumers();
                        }
                        pjcs.addPOJOConsumer(nCons);
                        updateBindingKeys(pjcs);
                    }
                }
            }

            @Override
            public void pojoChanged(POJOsEvent event) {
              
            }
            
            @Override
            public void pojoDeleted(POJOsEvent event) {
            }

            @Override
            public void configFileEdited(POJOsEvent event) {
                Object eventSrcObj = event.getSource();
                if (eventSrcObj instanceof POJOs){
                    POJOs pojos = (POJOs)eventSrcObj;
                    POJOConsumers pojocs = pojos.getPOJOConsumers();
                    if ( pojocs != null) {
                        updateBindingKeys(pojocs);    
                    }
                }
            }
        }
    }      
}
