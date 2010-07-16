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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEvent;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEventListener;
import org.netbeans.modules.soa.pojo.api.model.event.POJOsEventListenerAdapter;
import org.netbeans.modules.soa.pojo.schema.POJOConsumers;
import org.netbeans.modules.soa.pojo.schema.POJOs;
import org.netbeans.modules.soa.pojo.schema.POJOProvider;
import org.netbeans.modules.soa.pojo.schema.POJOProviders;
import org.netbeans.modules.soa.pojo.ui.actions.RefreshServicesAction;
import org.netbeans.modules.soa.pojo.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author gpatil
 */
public class POJOsRootNode  extends AbstractNode {
    private Project project;
    private static Action[] actions = null;
    
    public POJOsRootNode(Project prj) {
        this(prj, new InstanceContent());
        this.project = prj;
        Children cs = this.getChildren();
        initActions();        
    }
    
    private POJOsRootNode(Project prj, InstanceContent content) {
        super(new POJORootNodeChildren(prj), 
                new AbstractLookup(content));
        // adds the node to our own lookup
        content.add (this);
        // adds additional items to the lookup
        content.add (prj);
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(POJOsRootNode.class, 
                "LBL_POJOS_NODE"); //NOI18N
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
                SystemAction.get(RefreshServicesAction.class)
        // Need better requirement and design for packaging all libraries.
//                SystemAction.get(POJOPackageLibrariesInSUAction.class),
//                null,
//                SystemAction.get(POJODisablePackageAllAction.class),
            };
        }
    }
    
    @Override
    public Action[] getActions(boolean b) {
        return actions;
    }
        
    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/soa/pojo/resources/pojos.png" ); // No I18N
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/soa/pojo/resources/pojosopen.png" );// No I18N
    }
    
    public static class POJORootNodeChildren extends Children.Keys {
        private Project project;
        private POJOs pojos;
        private java.util.Map<String, POJOProvider> namePojoMap =
                new HashMap<String, POJOProvider>();
        private java.util.Map<String, POJOConsumers> namePOJOConsumersMap = 
                new HashMap<String, POJOConsumers>();        
        private POJOsEventListener modelListener = new ModelListener();
        
        public POJORootNodeChildren(Project prj) {
            super();
            project = prj;
            this.addNodify();    
        }

        private String getKey(POJOProvider pojo){
            return pojo.getPackage() + "." + pojo.getClassName();
        }
        
        public void addNodify() {
            this.pojos = Util.getPOJOs(project);
            this.namePojoMap.clear();
            POJOProviders ps = this.pojos.getPOJOProviders();
            if (ps != null){
                POJOProvider[] sc = ps.getPOJOProvider();
                for (POJOProvider s: sc){
                    this.namePojoMap.put(getKey(s), s);
                }
                updateKeys();
                super.addNotify();
                Util.addModelListener(this.project, this.modelListener);
            }
        }
                
        @Override
        public void removeNotify() {
            Util.removeModelListener(this.project, this.modelListener);
        }
        
        public void updateKeys() {            
            if (this.pojos == null){
                return ;
            } 
            HashSet set =new HashSet();
            set.addAll(this.namePojoMap.keySet());
            set.addAll(this.namePOJOConsumersMap.keySet());
            this.setKeys(set);
        }

        private void updateBindingKeys(POJOs ss) {
            this.pojos = ss;
            this.namePojoMap.clear();
            POJOProviders ps = ss.getPOJOProviders();
            if (ps != null){
                POJOProvider[] sc = ps.getPOJOProvider();
                for (POJOProvider s: sc){
                    this.namePojoMap.put(getKey(s), s);
                }
            }
            updateKeys();
        }
        
        protected Node[] createNodes(Object key) {
            Node[] lNodes = null;
            ArrayList<Node> listOfNodes = new ArrayList<Node>();
            if ( key instanceof String ) {
                try {
                    POJOProvider provider = this.namePojoMap.get((String) key);
                    FileObject fo = Util.getFOForJavaClass(project, provider.getPackage() 
                            + "." + provider.getClassName());
                    DataObject dobj = DataObject.find(fo);
                    POJONode bindingNode = new POJONode(project, provider, dobj);
                    listOfNodes.add(bindingNode);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            lNodes = listOfNodes.toArray(new Node[0]);

            
            return lNodes;
        }
        
        private final class ModelListener extends POJOsEventListenerAdapter {
            @Override
            public void pojoAdded(POJOsEvent event) {
                if (event.getSource() instanceof POJOs){                    
                    updateBindingKeys((POJOs) event.getSource());    
                }
            }

            @Override
            public void pojoChanged(POJOsEvent event) {
                if (event.getSource() instanceof POJOs){   
                    POJOProvider provider = (POJOProvider) event.getNewValue();
                    POJOProvider oProvider = (POJOProvider) event.getOldValue();
                    String nName = provider.getClassName();
                    String oName = oProvider.getClassName();
                    if ((nName != null) && (!nName.equals(oName))){
                        // Name change
                        updateBindingKeys((POJOs) event.getSource());    
                    }
                }                
            }
            
            @Override
            public void pojoDeleted(POJOsEvent event) {
                if (event.getSource() instanceof POJOs){
                    updateBindingKeys((POJOs) event.getSource());    
                }
            }

            @Override
            public void configFileEdited(POJOsEvent event) {
                if (event.getSource() instanceof POJOs){
                    updateBindingKeys((POJOs) event.getSource());    
                }
            }
        }
    }    
}