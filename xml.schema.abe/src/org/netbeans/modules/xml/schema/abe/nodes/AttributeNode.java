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

/*
 * AttributeNode.java
 *
 * Created on April 17, 2006, 4:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.nodes;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.abe.InstanceUIContext;
import org.netbeans.modules.xml.schema.abe.StartTagPanel;
import org.netbeans.modules.xml.schema.abe.UIUtilities;
import org.netbeans.modules.xml.schema.abe.nodes.properties.BaseABENodeProperty;
import org.netbeans.modules.xml.schema.abe.nodes.properties.DefaultProperty;
import org.netbeans.modules.xml.schema.abe.nodes.properties.FixedProperty;
import org.netbeans.modules.xml.schema.abe.nodes.properties.FormPropertyEditor;
import org.netbeans.modules.xml.schema.abe.nodes.properties.GlobalReferenceProperty;
import org.netbeans.modules.xml.schema.abe.nodes.properties.NameProperty;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AttributeNode extends ABEAbstractNode {
    
    
    /**
     * Creates a new instance of AttributeNode
     */
    public AttributeNode(AbstractAttribute attribute, InstanceUIContext context) {
        super(attribute, context);
    }
    
    
    public AttributeNode(AbstractAttribute attribute) {
        super(attribute, new ABENodeChildren(attribute));
        setIconBaseWithExtension(
                "org/netbeans/modules/xml/schema/abe/resources/attribute.png");
        
    }
    protected void populateProperties(Sheet sheet) {
        if(getAXIComponent() instanceof AnyAttribute)
            //right now there is no props for AnyAttribute
            return;
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        if(set == null) {
            set = sheet.createPropertiesSet();
        }
        
        boolean shared = false;
        shared = getAXIComponent().isShared();
        boolean reference = false;
        if(getAXIComponent() instanceof Attribute)
            reference = ((Attribute)getAXIComponent()).isReference();
        
        Sheet.Set sharedSheet = null;
        if(shared || reference){
            //create shared sheet
            sharedSheet = getSharedSet(sheet);
        }else{
            //use normal sheet for everything
            sharedSheet = set;
        }
        
        if(shared && ! reference){
            //then all props are shared
            set = sharedSheet;
        }
        
        
        try {
            //name property
            Node.Property name = new NameProperty(
                    this,
                    String.class,
                    Attribute.PROP_NAME,
                    NbBundle.getMessage(AttributeNode.class, "PROP_AttributeNode_Name"),
                    NbBundle.getMessage(AttributeNode.class, "PROP_AttributeNode_NameDesc"));
            sharedSheet.put(name);
            
            // fixed property
            Node.Property fixedProp = new FixedProperty(
                    getAXIComponent(),
                    NbBundle.getMessage(AttributeNode.class,"PROP_Fixed_DisplayName"), // display name
                    NbBundle.getMessage(AttributeNode.class,"PROP_Fixed_ShortDescription")	// descr
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),fixedProp, getContext()));
            
            // default property
            Node.Property defaultProp = new DefaultProperty(
                    getAXIComponent(),
                    NbBundle.getMessage(AttributeNode.class,"PROP_Default_DisplayName"), // display name
                    NbBundle.getMessage(AttributeNode.class,"PROP_Default_ShortDescription")	// descr
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),defaultProp, getContext()));
            
            // use property
            Node.Property useProp = new BaseABENodeProperty(
                    getAXIComponent(),
                    Use.class, //as value type
                    Attribute.PROP_USE, //property name
                    NbBundle.getMessage(AttributeNode.class,"PROP_Use_DisplayName"), // display name
                    NbBundle.getMessage(AttributeNode.class,"PROP_Use_ShortDescription"),	// descr
                    UseEditor.class
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),useProp, getContext()));
            
            // form property
            Node.Property formProp = new BaseABENodeProperty(
                    getAXIComponent(),
                    Form.class, // Occur.ZeroOne.class as value type
                    Attribute.PROP_FORM, //property name
                    NbBundle.getMessage(AttributeNode.class,"PROP_Form_DisplayName"), // display name
                    NbBundle.getMessage(AttributeNode.class,"PROP_Form_ElementShortDescription"),	// descr
                    FormPropertyEditor.class
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),formProp, getContext()));
            
            //definition
            List<Class> filterTypes = new ArrayList<Class>();
            filterTypes.add(Datatype.class);
            filterTypes.add(Attribute.class);
            Node.Property typeProp = new GlobalReferenceProperty(
                    getAXIComponent(),
                    Attribute.PROP_TYPE,
                    NbBundle.getMessage(AttributeNode.class,
                    "PROP_Type_DisplayName"), // display name
                    NbBundle.getMessage(AttributeNode.class,
                    "HINT_Type_ShortDesc"),	// descr
                    NbBundle.getMessage(AttributeNode.class,
                    "LBL_AttributeNode_TypeDisplayName"), // type display name
                    NbBundle.getMessage(AttributeNode.class,
                    "LBL_ContentModelNode_TypeDisplayName"),
                    AXIType.class,
                    filterTypes
                    );
            if(!getAXIComponent().isReadOnly())
                sharedSheet.put(new SchemaModelFlushWrapper(getAXIComponent(), typeProp, getContext()));
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        if(shared && !reference){
            //everthing shared
            sheet.put(set);
        }else if(shared && reference){
            //name and defn shared
            sheet.put(set);
            sheet.put(sharedSheet);
        }else{
            sheet.put(set);
        }
    }
    
    public String getName(){
        if((AbstractAttribute) super.getAXIComponent() != null)
            return ((AbstractAttribute) super.getAXIComponent()).getName();
        else
            return "";
    }
    
    protected String getTypeDisplayName() {
        return NbBundle.getMessage(AttributeNode.class,"LBL_Attribute");
    }
    
    public static class UseEditor extends PropertyEditorSupport {
        
        /**
         * Creates a new instance of ProcessContentsEditor
         */
        public UseEditor() {
        }
        
        public String[] getTags() {
            return new String[] {NbBundle.getMessage(AttributeNode.class,"LBL_Empty"),
            NbBundle.getMessage(AttributeNode.class,"LBL_Prohibited"),
            NbBundle.getMessage(AttributeNode.class,"LBL_Optional"),
            NbBundle.getMessage(AttributeNode.class,"LBL_Required")};
        }
        
        public void setAsText(String text) throws IllegalArgumentException {
            if (text.equals(NbBundle.getMessage(AttributeNode.class,"LBL_Empty"))){
                setValue(null);
            } else if (text.equals(NbBundle.getMessage(AttributeNode.class,"LBL_Prohibited"))){
                setValue(Use.PROHIBITED);
            } else if (text.equals(NbBundle.getMessage(AttributeNode.class,"LBL_Optional"))){
                setValue(Use.OPTIONAL);
            } else if (text.equals(NbBundle.getMessage(AttributeNode.class,"LBL_Required"))){
                setValue(Use.REQUIRED);
            }
        }
        
        public String getAsText() {
            Object val = getValue();
            if (val instanceof Use){
                if (Use.PROHIBITED.equals(val)) {
                    return NbBundle.getMessage(AttributeNode.class,"LBL_Prohibited");
                } else if (Use.OPTIONAL.equals(val)) {
                    return NbBundle.getMessage(AttributeNode.class,"LBL_Optional");
                } else if (Use.REQUIRED.equals(val)) {
                    return NbBundle.getMessage(AttributeNode.class,"LBL_Required");
                }
            }
            // TODO how to display invalid values?
            return NbBundle.getMessage(AttributeNode.class,"LBL_Empty");
        }
    }
}
