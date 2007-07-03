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
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.actions.JAXBDeleteSchemaAction;
import org.netbeans.modules.xml.jaxb.actions.OpenJAXBCustomizerAction;
import org.netbeans.modules.xml.jaxb.cfg.schema.Bindings;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSource;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.actions.DeleteAction;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author lgao
 * @author $Author$ 
 */
public class JAXBWizardSchemaNode extends AbstractNode {
    private String schemaName;
    private Project project;
    private Schema schema;
    private static Action[] actions = null;
    
    public JAXBWizardSchemaNode(Project project, Schema schema) {
        this(project, schema, new InstanceContent());
        this.schemaName = schema.getName();
        this.project = project;
        this.schema = schema;
        this.initActions();                    
    }

    private JAXBWizardSchemaNode(Project project, Schema schema, InstanceContent content) {
        super(new JAXBWizardSchemaNodeChildren( project, schema ), 
                new AbstractLookup(content));
        // adds the node to our own lookup
        content.add (this);
        // adds additional items to the lookup
        //content.add (...);
    }
    
    public Action[] getActions(boolean b) {
        return actions;
    }
    
    public Schema getSchema(){
        return this.schema;
    }

    public void setSchema(Schema schm){
        this.schema = schm;
        Children c = this.getChildren();
        if (c instanceof JAXBWizardSchemaNodeChildren){
            JAXBWizardSchemaNodeChildren jaxbC = (JAXBWizardSchemaNodeChildren)
                    c;
            jaxbC.setSchema(this.schema);
        }
    }
    
    public Project getProject(){
        return this.project;
    }    
    private void initActions() {
        if ( actions == null ) {
            actions = new Action[] {
                SystemAction.get(OpenJAXBCustomizerAction.class),
                null,
                SystemAction.get(DeleteAction.class)
            };
        }
    }
    
    public String getDisplayName() {
        return schemaName;
    }
    
    public boolean canDestroy(){
        return true;
    }
    
    public void destroy() throws IOException {
        super.destroy();
        // Delete schema
        JAXBDeleteSchemaAction delAction = SystemAction.get(
                                                JAXBDeleteSchemaAction.class);
        delAction.performAction(new Node[] {this});
    }
    
    public Image getIcon(int type) {
        return Utilities.loadImage( "org/netbeans/modules/xml/jaxb/resources/package.gif" ); // No I18N
    }
    
    public Image getOpenedIcon(int type) {
        return Utilities.loadImage( "org/netbeans/modules/xml/jaxb/resources/packageOpen.gif" ); // No I18N
    }
    
    public static class JAXBWizardSchemaNodeChildren extends Children.Keys{
        private Project project;
        private String packageName;
        private Schema schema;
        
        public JAXBWizardSchemaNodeChildren(Project prj, Schema schema) {
            super();
            this.schema = schema;
            this.packageName = schema.getPackage();
            this.project = prj;
            this.addNodify();
        }
        
        private void setKeys(){
            List childrenNodes = new ArrayList();
            SchemaSources sss = this.schema.getSchemaSources();
            SchemaSource[] ss = sss.getSchemaSource();
//            Bindings bs  = this.schema.getBindings();
            
            for (int i = 0; i < ss.length; i++){
                 childrenNodes.add(ss[i]);
            }
            
            super.setKeys(childrenNodes );
        }
        
        public void setSchema(Schema schm){
            this.schema = schm;
            setKeys();
        }
       
        public void refreshChildren(){
            setKeys();
        }
        
        protected Node[] createNodes(Object key) {
            Node[] xsdNodes = null;
            try {
                if ( key instanceof SchemaSource ) {
                    FileObject prjRoot = project.getProjectDirectory();                    
                    SchemaSource ss = (SchemaSource) key;
                    Node xsdNode = null;
                    FileObject fo = null;
                    FileObject xsdFolder = 
                                   ProjectHelper.getFOProjectSchemaDir(project);
                    FileObject locSchemaRoot = 
                            xsdFolder.getFileObject(schema.getName());
                    File projDir = FileUtil.toFile(prjRoot);
                    File tmpFile = null;
                    String originLocType = null;
                    Boolean isURL = Boolean.FALSE;
                    
                    originLocType = ss.getOrigLocationType();
                    if ((originLocType != null) && 
                            ("url".equals(originLocType))){ // NOI18N
                        isURL = Boolean.TRUE;
                    }

                    fo = FileUtil.toFileObject(new File(projDir, 
                            ss.getLocation()));
                    if (fo != null){
                        xsdNode = new JAXBWizardXSDNode(project, fo, 
                                locSchemaRoot, isURL, ss.getOrigLocation());
                    } else {
                        // Log
                        tmpFile = new File(ss.getLocation());
                        fo = xsdFolder.getFileObject(tmpFile.getName());
                        if (fo != null){
                            xsdNode = new JAXBWizardXSDNode(project, fo, 
                                    locSchemaRoot, isURL, ss.getOrigLocation());
                        }
                    }    
                    if (xsdNode != null){
                        xsdNodes = new Node[]{xsdNode};
                    }
                }
                
            } catch ( IntrospectionException inse ) {
                ErrorManager.getDefault().notify( inse );
            }
            
            return xsdNodes;
        }
        
        public void addNodify() {
            setKeys();
            super.addNotify();            
        }        
    }    
}
