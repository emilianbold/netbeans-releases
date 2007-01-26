/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

/*
 * NamespaceNode.java
 *
 * Created on July 21, 2006, 5:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.schema.abe.InstanceUIContext;
import org.netbeans.modules.xml.schema.abe.NamespacePanel;
import org.netbeans.modules.xml.schema.abe.nodes.properties.BaseABENodeProperty;
import org.netbeans.modules.xml.schema.abe.nodes.properties.DesignPatternProperty;
import org.netbeans.modules.xml.schema.abe.nodes.properties.FormPropertyEditor;
import org.netbeans.modules.xml.schema.abe.nodes.properties.NamespaceProperty;
import org.netbeans.modules.xml.schema.abe.nodes.properties.StringEditor;
import org.netbeans.modules.xml.schema.abe.wizard.SchemaTransformPatternSelectionUI;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.actions.NewAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author girix
 */
public class NamespaceNode extends ABEAbstractNode{
    
    /** Creates a new instance of NamespaceNode */
    public NamespaceNode(AXIComponent axiComponent, InstanceUIContext instanceUIContext) {
        super(axiComponent, instanceUIContext);
    }
    
    protected void populateProperties(Sheet sheet) {
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        if(set == null) {
            set = sheet.createPropertiesSet();
        }
        
        try {
            // attribute form property
            Node.Property attrFormProp = new BaseABENodeProperty(
                    getAXIComponent(),
                    Form.class, // Occur.ZeroOne.class as value type
                    AXIDocument.PROP_ATTRIBUTE_FORM_DEFAULT, //property name
                    NbBundle.getMessage(NamespaceNode.class,"PROP_AttributeFormDefault_DisplayName"), // display name
                    NbBundle.getMessage(NamespaceNode.class,"PROP_AttributeFormDefault_ShortDescription"),	// descr
                    FormPropertyEditor.class
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),attrFormProp, getContext()));
            
            // element form property
            Node.Property elementFormProp = new BaseABENodeProperty(
                    getAXIComponent(),
                    Form.class, // Occur.ZeroOne.class as value type
                    AXIDocument.PROP_ELEMENT_FORM_DEFAULT, //property name
                    NbBundle.getMessage(NamespaceNode.class,"PROP_ElementFormDefault_DisplayName"), // display name
                    NbBundle.getMessage(NamespaceNode.class,"PROP_ElementFormDefault_ShortDescription"),	// descr
                    FormPropertyEditor.class
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),elementFormProp, getContext()));
            
            // version property
            Node.Property versionProp = new BaseABENodeProperty(
                    getAXIComponent(),
                    String.class,
                    AXIDocument.PROP_VERSION,
                    NbBundle.getMessage(NamespaceNode.class,"PROP_Version_DisplayName"), // display name
                    NbBundle.getMessage(NamespaceNode.class,"PROP_Version_ShortDescription"),	// descr
                    StringEditor.class
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(), versionProp, getContext()));
            
            //namespace
            Node.Property tnsProp = new NamespaceProperty(
                    getAXIComponent(),
                    AXIDocument.PROP_TARGET_NAMESPACE,
                    NbBundle.getMessage(NamespaceNode.class,"PROP_TargetNamespace_DisplayName"), // display name
                    NbBundle.getMessage(NamespaceNode.class,"PROP_TargetNamespace_ShortDescription"),	// descr
                    NbBundle.getMessage(NamespaceNode.class,"LBL_NamespaceNode_TypeDisplayName") // type display name
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(), tnsProp, getContext()));
            
//            //schema design pattern
//            Node.Property schemaDesignPatternProp = new DesignPatternProperty(
//				getAXIComponent(),
//				AXIDocument.PROP_SCHEMA_DESIGN_PATTERN,
//				NbBundle.getMessage(NamespaceNode.class,"PROP_SchemaDesignPattern_DisplayName"), // display name
//				NbBundle.getMessage(NamespaceNode.class,"PROP_SchemaDesignPattern_ShortDescription")	// descr
//				);
//            set.put(new SchemaModelFlushWrapper(getAXIComponent(), schemaDesignPatternProp));
        } catch (Exception ex) {
        }
        
        sheet.put(set);
    }
    
    public Action[] getActions(boolean b) {
        return ALL_ACTIONS;
    }
    
    public String getName(){
        String namespace = "";
        AXIDocument root = (AXIDocument) super.getAXIComponent();
        if(root != null) {
            namespace = ((AXIDocument) super.getAXIComponent()).
                    getTargetNamespace();
            if(namespace == null)
                namespace = NbBundle.getMessage(
                        NamespacePanel.class, "LBL_NO_NAMESPACE");
        }
        return namespace;
    }
    
    private static final SystemAction[] ALL_ACTIONS=
            new SystemAction[]
    {
        SystemAction.get(NewAction.class),
        null,
        SystemAction.get(GoToAction.class),
        null,
        SystemAction.get(PropertiesAction.class)
    };
    
    public NewType[] getNewTypes() {
        if(!canWrite())
            return new NewType[0];
        List<NewType> ntl = new ArrayList<NewType>();
        //create new GE
        NewType nt = new NewType(){
            public void create() throws IOException {
                getContext().setUserInducedEventMode(true);
                try{
                    getContext().getNamespacePanel().addElement();
                }finally{
                    getContext().setUserInducedEventMode(false);
                }
            }
            public String getName() {
                return NbBundle.getMessage(NamespaceNode.class, "LBL_NEW_GLOBAL_ELEMENT_ACTION");
            }
        };
        ntl.add(nt);
        //create new GCT
        nt = new NewType(){
            public void create() throws IOException {
                getContext().setUserInducedEventMode(true);
                try{
                    getContext().getNamespacePanel().addComplexType();
                }finally{
                    getContext().setUserInducedEventMode(false);
                }
            }
            public String getName() {
                return NbBundle.getMessage(NamespaceNode.class, "LBL_NEW_GLOBAL_COMPLEX_TYPE_ACTION");
            }
        };
        ntl.add(nt);
        return  ntl.toArray(new NewType[ntl.size()]);
    }
    
    protected String getTypeDisplayName() {
        return NbBundle.getMessage(AttributeNode.class,"LBL_Document");
    }
    
    public void destroy() throws IOException {
        //dont let it delete
    }
}
