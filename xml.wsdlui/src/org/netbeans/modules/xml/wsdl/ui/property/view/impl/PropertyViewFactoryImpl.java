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
 * PropertyViewFactoryImpl.java
 *
 * Created on January 29, 2007, 6:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.property.view.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.api.property.StringAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.commands.CommonAttributePropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.OtherAttributePropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer;
import org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizerGroupedProperty;
import org.netbeans.modules.xml.wsdl.ui.property.model.DependsOnCustomizer;
import org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties;
import org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty;
import org.netbeans.modules.xml.wsdl.ui.property.model.Property;
import org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup;
import org.netbeans.modules.xml.wsdl.ui.property.model.PropertyModelException;
import org.netbeans.modules.xml.wsdl.ui.property.model.PropertyModelFactory;
import org.netbeans.modules.xml.wsdl.ui.property.model.SimpleCustomizer;
import org.netbeans.modules.xml.wsdl.ui.property.model.StaticCustomizer;
import org.netbeans.modules.xml.wsdl.ui.property.view.BuiltInCustomizerFactory;
import org.netbeans.modules.xml.wsdl.ui.property.view.GroupedBuiltInCustomizerFactory;
import org.netbeans.modules.xml.wsdl.ui.property.view.PropertyViewFactory;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.OptionalAttributeFinderVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaAttributeTypeFinderVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaDocumentationFinderVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaElementAttributeFinderVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.SchemaUtility;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfiguratorFactory;
import org.netbeans.modules.xml.wsdl.ui.spi.NewCustomizerProvider;
import org.netbeans.modules.xml.wsdl.ui.spi.WSDLLookupProviderFactory;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.property.XSDBooleanAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.property.XSDEnumeratedAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.MixedContentFinderVisitor;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class PropertyViewFactoryImpl extends PropertyViewFactory {
    
    /** Creates a new instance of PropertyViewFactoryImpl */
    public PropertyViewFactoryImpl() {
    }

    @Override
    public Sheet.Set[] getPropertySets(ExtensibilityElement exElement, 
                                              QName elementQName, 
                                              Element schemaElement) {
        Set<QName> processAttributeQNames = new HashSet<QName>();
        Sheet.Set defaultPropertiesSheetSet = Sheet.createPropertiesSet();
        ElementProperties elemProperties = null;
        try {
            PropertyModelFactory instance = PropertyModelFactory.getInstance();
            elemProperties = instance.getElementProperties(elementQName);
        } catch (PropertyModelException e) {
            e.printStackTrace();
        }
        Sheet.Set[] nodePropertySet = null;
        if (elemProperties != null) {
            PropertyGroup[] pGroups = elemProperties.getPropertyGroup();
            Map<String, Integer> groupList = new HashMap<String, Integer>();

            nodePropertySet = new Sheet.Set[pGroups.length + 1];
            boolean defaultPropertiesSet = false;
            //Create Property Sheets for each Group
            for (PropertyGroup group  : pGroups) {
                String gName = group.getName();
                int groupNumber = group.getGroupOrder() - 1; //Starts with 1.
                groupList.put(gName, new Integer(groupNumber));
                nodePropertySet[groupNumber] = new Sheet.Set();
                nodePropertySet[groupNumber].setName(gName);
                String displayName = gName; //TODO: get from Bundle
                nodePropertySet[groupNumber].setDisplayName(displayName);
                if (group.isIsDefault()) {
                    defaultPropertiesSheetSet = nodePropertySet[groupNumber];
                    defaultPropertiesSet = true;
                }
            }

            //Add default properties group at the end.
            if (!defaultPropertiesSet) {
                int lastIndex = nodePropertySet.length - 1;
                nodePropertySet[lastIndex] = defaultPropertiesSheetSet;
                groupList.put(null, new Integer(lastIndex));
            }

            //Generate Node.Property for each attribute, and add into each group according to the defined order
            Property[] props = elemProperties.getProperty();
            Map<String, Map<Integer, Node.Property>> map = new HashMap<String, Map<Integer, Node.Property>>();
            Map<Integer, Node.Property> list = null;
            for (Property prop : props) {
                QName attributeQName = new QName(elementQName.getNamespaceURI(), prop.getAttributeName());
                Attribute attribute = getAttribute(attributeQName.getLocalPart(), schemaElement);

                boolean isOptional = isAttributeOptional(attribute, attributeQName.getLocalPart());
                
                //Generate property as per specifications
                Node.Property nodeProperty = getNodeProperty(exElement, elementQName, attributeQName, prop, isOptional);
                
                //if not generated, generate default way.
                if (nodeProperty == null) {
                    
                    nodeProperty = createDefaultNodeProperty(attribute, exElement, attributeQName, isOptional);
                    
                }
                //Mark this attribute as processed
                processAttributeQNames.add(attributeQName);

                if (map.containsKey(prop.getGroupName())) {
                    list = map.get(prop.getGroupName());
                } else {
                    list = new TreeMap<Integer, Node.Property>(); //we dont know in advance how many will be in each group, so assume it to be as long as the number of properties
                    map.put(prop.getGroupName(), list);
                }
                //set Name and description if not done so before.
                setNameAndDescription(nodeProperty, attributeQName.getLocalPart(), null, getAttributeShortDescription(schemaElement, attributeQName));
                list.put(new Integer(prop.getPropertyOrder() - 1), nodeProperty);//PropertyOrder starts with one
            }

            GroupedProperty[] groupedProps = elemProperties.getGroupedProperty();
            for (GroupedProperty groupedProp : groupedProps) {
                String groupedAttrNames = groupedProp.getGroupedAttributeNames();
                String[] attrNames = groupedAttrNames.split(" ");
                Node.Property prop = getGroupedNodeProperty(elementQName.getNamespaceURI(), exElement, groupedProp);
                if (prop != null) {
                    for (String attrName : attrNames) {
                        processAttributeQNames.add(new QName(elementQName.getNamespaceURI(), attrName));
                    }
                } else {
                    // add it individually in the group name at the same property order sequentially
                    
                }
                
                Map<Integer, Node.Property> nodeProps = null;
                if (map.containsKey(groupedProp.getGroupName())) {
                    nodeProps = map.get(groupedProp.getGroupName());
                } else {
                    nodeProps = new TreeMap<Integer, Node.Property>();
                    map.put(groupedProp.getGroupName(), nodeProps);
                }
                setNameAndDescription(prop, groupedProp.getDisplayName(), groupedProp.getDisplayName(), "TODO: Set documentation for Grouped Property.");
                nodeProps.put(new Integer(groupedProp.getPropertyOrder() - 1), prop);
            }
            
            //form return value
            for (String groupName : map.keySet()) {
                Integer i = groupList.get(groupName);
                Map<Integer, Node.Property> properties = map.get(groupName);
                for (Node.Property prop : properties.values()) {
                    if (prop != null) {
                        nodePropertySet[i.intValue()].put(prop);
                    }
                }
            }
        } else {
            nodePropertySet = new Sheet.Set[] {defaultPropertiesSheetSet};
        }
        
        //Add all remaining attributes which are not processed into default properties group.
        populateDefaultPropertySet(defaultPropertiesSheetSet, schemaElement, elementQName, exElement, processAttributeQNames);

        return nodePropertySet;

    }

    private org.openide.nodes.Node.Property getNodeProperty(ExtensibilityElement extensibilityElement, QName elementQName, QName attributeQName, Property prop, boolean isOptional) {
        BuiltInCustomizer bCustomizer = prop.getBuiltInCustomizer();
        if (bCustomizer != null) {
            DependsOnCustomizer dCustomizer = bCustomizer.getDependsOnCustomizer();
            if (dCustomizer != null) {
                String nameOfCustomizer = dCustomizer.getName();
                StaticCustomizer sCustomizer = dCustomizer.getStaticCustomizer();
                if (sCustomizer != null) {
                    QName qname = sCustomizer.getDependsOnAttributeName();
                    //create builtin property adapter with dependson func.\
                    return BuiltInCustomizerFactory.getProperty(extensibilityElement, attributeQName, qname, nameOfCustomizer, isOptional);
                }
                
/*No use case as of now: commented                DynamicCustomizer ddCustomizer = dCustomizer.getDynamicCustomizer();
                if (ddCustomizer != null) {
                    String className = ddCustomizer.getAttributeValueProviderClass();
                    String valueType = ddCustomizer.getDependsOnAttributeValueType();
                    return BuiltInCustomizerFactory.getProperty(extensibilityElement, attributeQName, nameOfCustomizer, className, valueType);
                }*/
                //no customizer defined
                assert false : "Invalid Customizer defined for " + attributeQName.toString(); 
            } else {
                SimpleCustomizer sCustomizer = bCustomizer.getSimpleCustomizer();
                if (sCustomizer != null) {
                    String name = sCustomizer.getName();
                    return BuiltInCustomizerFactory.getProperty(extensibilityElement, attributeQName, name, isOptional);
                }
                assert false : "Invalid Customizer defined for " + attributeQName.toString();
            }
        } else {
            String nCustomizer = prop.getNewCustomizer();
            if (nCustomizer != null) {
                NewCustomizerProvider prov = WSDLLookupProviderFactory.getObject(elementQName.getNamespaceURI(), NewCustomizerProvider.class);
                assert prov != null : "Couldnt create the NewCustomizer";
                Node.Property property = prov.getProperty(extensibilityElement, elementQName, attributeQName, isOptional);
                assert property != null : "Couldnt create the property using this NewCustomizer";
                return property;
            }
        }
        //Probably SchemaCustomizer
        return null;
    }
    
    private Node.Property getGroupedNodeProperty(String namespace, ExtensibilityElement extensibilityElement, GroupedProperty gProp) {
        BuiltInCustomizerGroupedProperty bCustomizer = gProp.getBuiltInCustomizer();
        if (bCustomizer != null) {
            return GroupedBuiltInCustomizerFactory.getProperty(extensibilityElement, bCustomizer);
        }
        
        String nCustomizer = gProp.getNewCustomizer();
        if (nCustomizer != null) {
            
        }
        return null;
    }
    
    private org.openide.nodes.Node.Property createDefaultNodeProperty(Attribute attr, ExtensibilityElement exElement, QName attributeQName, boolean isOptional) {
        Node.Property attrValueProperty = null;
        QName elementQName = new QName(attributeQName.getNamespaceURI(), exElement.getQName().getLocalPart());
        ExtensibilityElementConfigurator configurator = new ExtensibilityElementConfiguratorFactory().getExtensibilityElementConfigurator(
                elementQName);
        if (configurator != null) {
            if (configurator.isHidden(exElement, elementQName, attributeQName.getLocalPart())) {
                return null;
            }
            
            attrValueProperty = configurator.getProperty(exElement, elementQName, attributeQName.getLocalPart());
            if (attrValueProperty != null) return attrValueProperty;
        }
        
        SimpleType gst = findAttributeType(attr);
        
        if(gst != null) {
            try {
                ExtensibilityElementPropertyAdapter adapter = null; 
                if (attr.getDefault() != null) {
                    adapter = new ExtensibilityElementPropertyAdapter(exElement, attributeQName.getLocalPart(), attr.getDefault());
                } else {
                    adapter = new ExtensibilityElementPropertyAdapter(exElement, attributeQName.getLocalPart(), isOptional);
                }
                attrValueProperty = processSimpleType(gst, adapter, isOptional);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (attrValueProperty == null) {
                //create default property
                attrValueProperty = createAttributeProperty(exElement, attributeQName, isOptional, attr.getDefault());
            }
        }
        return attrValueProperty;
    }

    private boolean isAttributeOptional(Attribute attribute, String localPart) {
        OptionalAttributeFinderVisitor visitor = new OptionalAttributeFinderVisitor(localPart);
        attribute.accept(visitor);
        return visitor.isOptional();
    }

    private Attribute getAttribute(String attrName, Element schemaElement) {
        SchemaElementAttributeFinderVisitor seaFinder = new SchemaElementAttributeFinderVisitor(schemaElement);
        schemaElement.accept(seaFinder);

        List<Attribute> attributes = seaFinder.getAttributes();
        Iterator<Attribute> it = attributes.iterator();

        while(it.hasNext()) {
            Attribute attr = it.next();
            if(attr instanceof Nameable) {
                Nameable namedAttr = (Nameable) attr;
                if(attrName.equals(namedAttr.getName()))  {
                    return attr;
                }
            }
        }
        return null;
    }
    
    private SimpleType findAttributeType(Attribute attr) {
        if (attr == null) return null;
        
        SchemaAttributeTypeFinderVisitor typeFinder = new SchemaAttributeTypeFinderVisitor();
        attr.accept(typeFinder);
        return typeFinder.getSimpleType();
    }

    private Node.Property processSimpleType(SimpleType gst, PropertyAdapter propertyAdapter, boolean isOptional) throws NoSuchMethodException {
        Node.Property attrValueProperty = null;  
        //for boolean type we show true/false drop down
        if(gst != null) {
            String simpleTypeName = null;
            if (gst instanceof GlobalSimpleType) {
                simpleTypeName = ((GlobalSimpleType) gst).getName();
            }
            String namesapce = gst.getModel().getSchema().getTargetNamespace();
            SchemaModel primitiveTypesModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            String primitiveTypeNamesapce = primitiveTypesModel.getSchema().getTargetNamespace();
            if(namesapce != null 
                    && namesapce.equals(primitiveTypeNamesapce)
                    && simpleTypeName != null && simpleTypeName.equals("boolean")) {//NOI18N
                attrValueProperty = 
                    new XSDBooleanAttributeProperty(propertyAdapter, String.class, "getValue", "setValue", isOptional);//NOI18N
                
            } else if(gst.getDefinition() instanceof  SimpleTypeRestriction) {
                //if attribute has enumeration facet
                //then use the first enumeration value
                SimpleTypeRestriction sr = (SimpleTypeRestriction) gst.getDefinition();
                Collection enumerations = sr.getEnumerations();
                if(enumerations != null && enumerations.size() > 0) {
                    attrValueProperty = 
                        new XSDEnumeratedAttributeProperty(gst, propertyAdapter, String.class, "getValue", "setValue", isOptional);//NOI18N
                    
                }
            }
        }
        
        
        return attrValueProperty;
    }
    
    private void populateDefaultPropertySet(Sheet.Set propertySet, Element schemaElement, QName elementQName, ExtensibilityElement exElement, Set<QName> processAttributeQNames) {
        SchemaElementAttributeFinderVisitor seaFinder = new SchemaElementAttributeFinderVisitor(schemaElement, true);
        schemaElement.accept(seaFinder);
        List<Attribute> attributes = seaFinder.getAttributes();
        for (Attribute attr : attributes) {
            if(attr instanceof Nameable) {
                Nameable namedAttr = (Nameable) attr;

                QName attrQName = new QName(schemaElement.getModel().getSchema().getTargetNamespace(), namedAttr.getName());
                if (!processAttributeQNames.contains(attrQName)) { 
                    boolean isOptional = isAttributeOptional(attr, namedAttr.getName());
                    Node.Property attrValueProperty = createDefaultNodeProperty(attr, exElement, attrQName, isOptional);
                    setNameAndDescription(attrValueProperty, namedAttr.getName(), null, getAttributeShortDescription(schemaElement, attrQName));
                    propertySet.put(attrValueProperty);
                }
            }
        }

        //find if there needs to be a any element tag, display a simple string property editor.
        MixedContentFinderVisitor aeFinder = new MixedContentFinderVisitor(schemaElement);
        schemaElement.accept(aeFinder);
        if (aeFinder.hasMixedContent()) {
            Node.Property attrValueProperty;
            try {
                attrValueProperty = new BaseAttributeProperty(new AnyElementPropertyAdapter(exElement, elementQName), String.class, "value");
                attrValueProperty.setName(NbBundle.getMessage(PropertyViewFactoryImpl.class, "PROP_NAME_ANY_CONTENT"));
                propertySet.put(attrValueProperty);
            } catch (NoSuchMethodException e) {
                ErrorManager.getDefault().notify(e);
            }
        }

    }
    
    private String getAttributeShortDescription(Element schemaElement, QName attrQName) {
        if (schemaElement != null) {
            Attribute attribute = SchemaUtility.findAttribute(attrQName, schemaElement);
            if(attribute != null) {
                SchemaDocumentationFinderVisitor sdfFinder = new SchemaDocumentationFinderVisitor();
                attribute.accept(sdfFinder);
                return sdfFinder.getDocumentation();
            }
        }
        return null;
    }
    
    private Node.Property createAttributeProperty(ExtensibilityElement exElement, QName attrQName, boolean isOptional, String defaultValue) {
        Node.Property attrValueProperty = null;
        
        try {
            AbstractDocumentComponent adc = (AbstractDocumentComponent) exElement;
            QName elementQName = adc.getQName();
            String namespace = elementQName.getNamespaceURI();
            String ns = attrQName.getNamespaceURI();
            //if attribute are from non wsdl namespace
            //in that case we will have a namspace
            //for wsdl namspace attribute ns is empty string
            if(ns == null || ns.trim().equals("") || ns.equals(namespace)) {
                ExtensibilityElementPropertyAdapter propertyAdapter = 
                    defaultValue != null 
                    ? new ExtensibilityElementPropertyAdapter(exElement, attrQName.getLocalPart(), defaultValue)
                    : new ExtensibilityElementPropertyAdapter(exElement, attrQName.getLocalPart(), isOptional);
                    
                attrValueProperty = getAttributeNodeProperty(propertyAdapter);
                
            } else {
                OtherAttributePropertyAdapter propertyAdapter = 
                    new OtherAttributePropertyAdapter(attrQName, exElement);
                //attributes
                attrValueProperty = getOtherAttributeNodeProperty(attrQName, propertyAdapter);
                
            }
        } catch(Exception ex) {
            // mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
            ErrorManager.getDefault().notify(ex);
        }
        
        return attrValueProperty;
    }
    
    protected Node.Property getAttributeNodeProperty(ExtensibilityElementPropertyAdapter propertyAdapter) throws NoSuchMethodException {
        Node.Property attrValueProperty = new StringAttributeProperty(propertyAdapter, String.class, "getValue", "setValue"); 
        return attrValueProperty;
    }
    
    protected Node.Property getOtherAttributeNodeProperty(QName attrQName, 
            OtherAttributePropertyAdapter propertyAdapter) throws NoSuchMethodException {
        Node.Property attrValueProperty = new BaseAttributeProperty(propertyAdapter, String.class, CommonAttributePropertyAdapter.VALUE);
        attrValueProperty.setName(Utility.fromQNameToString(attrQName));
        return attrValueProperty;
    }
    
    private void setNameAndDescription(Node.Property property, String name, String displayName, String description) {
        if (property == null) return;
        if (isNullOrBlank(property.getName())) {
            assert name != null : "Name cannot be null";
            property.setName(name);
        }
        if (isNullOrBlank(property.getDisplayName()) && displayName != null) {
            property.setDisplayName(displayName);
        }
        if (isNullOrBlank(property.getShortDescription()) || property.getShortDescription().equals(property.getDisplayName())) {
            property.setShortDescription(description);
        }
    }
    
    private boolean isNullOrBlank(String str) {
        return str == null || str.trim().length() == 0;
    }
    
    
    public class AnyElementPropertyAdapter extends PropertyAdapter {
        QName qname;
        
        public AnyElementPropertyAdapter(ExtensibilityElement delegate, QName qname) {
            super(delegate);
            this.qname = qname;
        }
        
        public String getValue() {
            String content = ((ExtensibilityElement)getDelegate()).getContentFragment();
            if (content != null) {
                return content;
            }
            return "";
        }
        
        public void setValue(String string) {
            
            boolean isInTransaction = Utility.startTransaction(getDelegate().getModel());
            try {
                ((ExtensibilityElement)getDelegate()).setContentFragment(string);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            } finally {
                if (!isInTransaction) {
                    Utility.endTransaction(getDelegate().getModel(), isInTransaction);
                }
            }
        }
        
        
    }
    
}
