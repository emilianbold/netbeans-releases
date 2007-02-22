
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

package org.netbeans.modules.xml.schema.abe.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.abe.InstanceUIContext;
import org.netbeans.modules.xml.schema.abe.action.AttributeOnElementNewType;
import org.netbeans.modules.xml.schema.abe.action.CompositorOnElementNewType;
import org.netbeans.modules.xml.schema.abe.action.ElementOnElementNewType;
import org.netbeans.modules.xml.schema.abe.nodes.properties.*;
import org.netbeans.modules.xml.schema.model.Form;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class ElementNode extends ABEAbstractNode {
    
    
    /**
     * Creates a new instance of ElementNode
     */
    public ElementNode(AbstractElement element, InstanceUIContext context) {
        super(element, context);
        initialize();
    }
    
    public ElementNode(AbstractElement element) {
        super(element, new ABENodeChildren(element));
        setIconBaseWithExtension(
                "org/netbeans/modules/xml/schema/abe/resources/element.png");
        
    }
    
    private void initialize(){
        getAXIComponent().addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(Element.PROP_NAME)){
                    ElementNode.this.fireNameChange((String) evt.getOldValue(), (String) evt.getNewValue());
                    ElementNode.this.firePropertySetsChange(ElementNode.this.getPropertySets(),
                            ElementNode.this.getPropertySets());
                }
            }
            
        });
    }
    
    protected void populateProperties(Sheet sheet) {
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        if(set == null) {
            set = sheet.createPropertiesSet();
        }
        
        boolean shared = false;
        shared = getAXIComponent().isShared();
        boolean reference = false;
        if(getAXIComponent() instanceof Element)
            reference = ((Element)getAXIComponent()).isReference();
        
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
            Node.Property name = new PropertySupport.Name(
                    this,
                    NbBundle.getMessage(ElementNode.class, "PROP_ElementNode_Name"),
                    NbBundle.getMessage(ElementNode.class, "PROP_ElementNode_NameDesc")
                    );
            sharedSheet.put(name);
            
            // nillable property
            Node.Property nillableProp = new BooleanProperty(
                    getAXIComponent(),
                    Element.PROP_NILLABLE, // property name
                    NbBundle.getMessage(ElementNode.class,"PROP_Nillable_DisplayName"), // display name
                    NbBundle.getMessage(ElementNode.class,"PROP_Nillable_ShortDescription"),	// descr
                    true // default value is false
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),nillableProp, getContext()));
            
            // fixed property
            Node.Property fixedProp = new FixedProperty(
                    getAXIComponent(),
                    NbBundle.getMessage(ElementNode.class,"PROP_Fixed_DisplayName"), // display name
                    NbBundle.getMessage(ElementNode.class,"PROP_Fixed_ShortDescription")	// descr
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),fixedProp, getContext()));
            
            // default property
            Node.Property defaultProp = new DefaultProperty(
                    getAXIComponent(),
                    NbBundle.getMessage(ElementNode.class,"PROP_Default_DisplayName"), // display name
                    NbBundle.getMessage(ElementNode.class,"PROP_Default_ShortDescription")	// descr
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),defaultProp, getContext()));
            
            if(getAXIComponent().supportsCardinality()) {
                // minOccurs
                Node.Property minOccursProp = new MinOccursProperty(
                        getAXIComponent(),
                        String.class,
                        Element.PROP_MINOCCURS,
                        NbBundle.getMessage(ElementNode.class,"PROP_MinOccurs_DisplayName"), // display name
                        NbBundle.getMessage(ElementNode.class,"PROP_MinOccurs_ShortDescription")	// descr
                        );
                set.put(new SchemaModelFlushWrapper(getAXIComponent(), minOccursProp, getContext()));
                
                // maxOccurs
                if (getAXIComponent() instanceof AbstractElement &&
                        ((AbstractElement)getAXIComponent()).allowsFullMultiplicity()) {
                    Property maxOccursProp = new BaseABENodeProperty(
                            getAXIComponent(),
                            String.class,
                            Element.PROP_MAXOCCURS,
                            NbBundle.getMessage(ElementNode.class,"PROP_MaxOccurs_DisplayName"), // display name
                            NbBundle.getMessage(ElementNode.class,"PROP_MaxOccurs_ShortDescription"),	// descr
                            MaxOccursEditor.class
                            );
                    set.put(new SchemaModelFlushWrapper(getAXIComponent(), maxOccursProp, getContext()));
                }
            }
            
            // form property
            Node.Property formProp = new BaseABENodeProperty(
                    getAXIComponent(),
                    Form.class, // Occur.ZeroOne.class as value type
                    Element.PROP_FORM, //property name
                    NbBundle.getMessage(ElementNode.class,"PROP_Form_DisplayName"), // display name
                    NbBundle.getMessage(ElementNode.class,"PROP_Form_ElementShortDescription"),	// descr
                    FormPropertyEditor.class
                    );
            set.put(new SchemaModelFlushWrapper(getAXIComponent(),formProp, getContext()));
            
            // definition property
            List<Class> filterTypes = new ArrayList<Class>();
            filterTypes.add(Datatype.class);
            filterTypes.add(ContentModel.class);
            filterTypes.add(Element.class);
            Node.Property typeProp = new GlobalReferenceProperty(
                    getAXIComponent(),
                    Element.PROP_TYPE,
                    NbBundle.getMessage(ElementNode.class,
                    "PROP_Type_DisplayName"), // display name
                    NbBundle.getMessage(ElementNode.class,
                    "HINT_Type_ShortDesc"),	// descr
                    NbBundle.getMessage(ElementNode.class,
                    "LBL_ElementNode_TypeDisplayName"), // type display name
                    NbBundle.getMessage(ElementNode.class,
                    "LBL_ContentModelNode_TypeDisplayName"),
                    AXIType.class,
                    filterTypes
                    );
            if(!getAXIComponent().isReadOnly())
                sharedSheet.put(new SchemaModelFlushWrapper(getAXIComponent(), typeProp, getContext()));
            
        } catch (Exception ex) {
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
        if((AbstractElement) super.getAXIComponent() != null)
            return ((AbstractElement) super.getAXIComponent()).getName();
        else
            return "";
    }
    
    public NewType[] getNewTypes() {
        if(getAXIComponent().isReadOnly())
            return new NewType[0];
        List<NewType> ntl = new ArrayList<NewType>();
        NewType nt = new ElementOnElementNewType(getContext());
        ntl.add(nt);
        nt = new AttributeOnElementNewType(getContext());
        ntl.add(nt);
        if( ((AXIContainer)getAXIComponent()).getCompositor() == null ){
            nt = new CompositorOnElementNewType(getContext(), Compositor.
                    CompositorType.SEQUENCE);
            ntl.add(nt);
            nt = new CompositorOnElementNewType(getContext(), Compositor.
                    CompositorType.CHOICE);
            ntl.add(nt);
            nt = new CompositorOnElementNewType(getContext(), Compositor.
                    CompositorType.ALL);
            ntl.add(nt);
        }
        return  ntl.toArray(new NewType[ntl.size()]);
    }
    
    public boolean canRename() {
        if(canWrite())
            return true;
        return false;
    }
    
    protected String getTypeDisplayName() {
        return NbBundle.getMessage(AttributeNode.class,"LBL_Element");
    }
    
}
