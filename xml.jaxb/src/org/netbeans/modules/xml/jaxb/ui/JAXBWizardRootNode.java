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

import java.awt.Image;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.netbeans.modules.xml.jaxb.actions.JAXBRegenerateCodeAction;
import java.util.HashMap;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEvent;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEventListener;
import org.netbeans.modules.xml.jaxb.api.model.events.JAXBWizEventListenerAdapter;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author $Author $
 * @author lgao
 */
public class JAXBWizardRootNode extends AbstractNode {
    private Project project;
    private static Action[] actions = null;
    
    public JAXBWizardRootNode(Project prj) {
        this(prj, new InstanceContent());
        this.project = prj;
        Children cs = this.getChildren();
        initActions();        
    }
    
    private JAXBWizardRootNode(Project prj, InstanceContent content) {
        super(new JAXBWizardRootNodeChildren(prj), 
                new AbstractLookup(content));
        // adds the node to our own lookup
        content.add (this);
        // adds additional items to the lookup
        content.add (prj);
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(JAXBWizardRootNode.class, 
                "LBL_JAXB_Bindings"); //NOI18N
    }
    
    public String getName() {
        return JAXBNodeFactory.JAXB_NODE_NAME;
    }
    
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
                new JAXBRegenerateCodeAction(),
            };
        }
    }
    
    public Action[] getActions(boolean b) {
        return actions;
    }
        
    public Image getIcon(int type) {
        return Utilities.loadImage(
                "org/netbeans/modules/xml/jaxb/resources/packageRoot.gif" ); // No I18N
    }
    
    public Image getOpenedIcon(int type) {
        return Utilities.loadImage(
                "org/netbeans/modules/xml/jaxb/resources/packageRootOpen.gif" );// No I18N
    }
    
    public static class JAXBWizardRootNodeChildren extends Children.Keys {
        private Project project;
        private Schemas schemas;
        private java.util.Map<String, Schema> nameSchemaMap = 
                new HashMap<String, Schema>();
        private JAXBWizEventListener modelListener = new ModelListener();
        
        public JAXBWizardRootNodeChildren(Project prj) {
            super();
            project = prj;
            this.addNodify();    
        }
                
        public void addNodify() {
            this.schemas = ProjectHelper.getXMLBindingSchemas(project);
            this.nameSchemaMap.clear();
            Schema[] sc = this.schemas.getSchema();
            for (Schema s: sc){
                this.nameSchemaMap.put(s.getName(), s);
            }            
            
            updateKeys();
            super.addNotify();
            ProjectHelper.addModelListener(this.project, this.modelListener);
        }
                
        public void removeNotify() {
            ProjectHelper.removeModelListener(this.project, this.modelListener);
        }
        
        public void updateKeys() {            
            if (this.schemas == null){
                return ;
            }            
            this.setKeys(this.nameSchemaMap.keySet());
        }

        private void updateBindingKeys(Schemas ss) {
            this.schemas = ss;
            this.nameSchemaMap.clear();
            Schema[] sc = this.schemas.getSchema();
            for (Schema s: sc){
                this.nameSchemaMap.put(s.getName(), s);
            }            

            updateKeys();                
        }
        
        protected Node[] createNodes(Object key) {
            Node[] nodes = null;
            if ( key instanceof String ) {
                Schema schema = this.nameSchemaMap.get((String)key);                
                JAXBWizardSchemaNode bindingNode = new JAXBWizardSchemaNode(
                        project, schema );
                nodes = new Node[] {
                    bindingNode,
                };
            }
            
            return nodes;
        }
        
        private final class ModelListener extends JAXBWizEventListenerAdapter {
            @Override
            public void bindingAdded(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){                    
                    updateBindingKeys((Schemas) event.getSource());    
                }
            }

            @Override
            public void bindingChanged(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){   
                    Schema schema = (Schema) event.getNewValue();
                    Schema oSchema = (Schema) event.getOldValue();
                    String nName = schema.getName();
                    String oName = oSchema.getName();
                    if ((nName != null) && (!nName.equals(oName))){
                        // Name change
                        updateBindingKeys((Schemas) event.getSource());    
                    }
                }                
            }
            
            @Override
            public void bindingDeleted(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateBindingKeys((Schemas) event.getSource());    
                }
            }

            @Override
            public void configFileEdited(JAXBWizEvent event) {
                if (event.getSource() instanceof Schemas){
                    updateBindingKeys((Schemas) event.getSource());    
                }
            }
        }
    }    
}