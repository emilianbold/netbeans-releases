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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.iep.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPQNames;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.tbls.model.TcgComponentType;
import org.netbeans.modules.tbls.model.TcgModelManager;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 *
 * 
 */
public class ComponentImpl extends IEPComponentBase implements Component {

    private TcgComponentType mType;
    
    public ComponentImpl(IEPModel model,  Element e) {
        super(model, e);
    }

    public ComponentImpl(IEPModel model) {
        this(model, createNewElement(IEPQNames.COMPONENT.getQName(), model));
    }

    public void accept(IEPVisitor visitor) {
        visitor.visitComponent(this);
    }

    public IEPComponent createChild(Element childEl) {
        IEPComponent child = null;
        
        if (childEl != null) {
            String localName = childEl.getLocalName();
            if (localName == null || localName.length() == 0) {
                    localName = childEl.getTagName();
            }
            if (localName.equals(COMPONENT_CHILD)) {
                    child = new ComponentImpl(getModel(), childEl);
            } else if (localName.equals(PROPERTY_CHILD)) {
                    child = new PropertyImpl(getModel(), childEl);
            } else if (localName.equals(IMPORT_CHILD)) {
                    child = new ImportImpl(getModel(), childEl);
            } else if(localName.equals(DOCUMENTATION_CHILD)) {
                child = new DocumentationImpl(getModel(), childEl);
            }
        }
        
        return child;
    }

    public String getName() {
        return getAttribute(ATTR_NAME);
    }

    public void setName(String name) {
        setAttribute(NAME_PROPERTY, ATTR_NAME, name);
    }

    public String getTitle() {
        return getAttribute(ATTR_TITLE);
    }

    public void setTitle(String title) {
        setAttribute(TITLE_PROPERTY, ATTR_TITLE, title);
    }

    public String getType() {
        return getAttribute(ATTR_TYPE);
    }

    public void setType(String type) {
        setAttribute(TYPE_PROPERTY, ATTR_TYPE, type);
    }

    public List<Component> getChildComponents() {
        return getChildren(Component.class);
    }

    public List<Property> getProperties() {
        return getChildren(Property.class);
    }

    public void addChildComponent(Component child) {
        addAfter(COMPONENT_CHILD, child, TypeCollection.FOR_COMPONENT.types());
    }

    public void removeChildComponent(Component child) {
        removeChild(COMPONENT_CHILD, child);
    }

    public void addProperty(Property property) {
        addAfter(PROPERTY_CHILD, property, TypeCollection.FOR_PROPERTY.types());
    }

    public void removeProperty(Property property) {
        removeChild(PROPERTY_CHILD, property);
    }

    public Property getProperty(String name) {
        Property prop = null;
        
        List<Property> properties = getProperties();
        Iterator<Property> it = properties.iterator(); 
        while(it.hasNext()) {
            Property p = it.next();
            String propName = p.getName();
            if(propName.equals(name)) {
                prop = p;
                break;
            }
        }
        
        //if property is null then we can get default value
        //from TcgComponentType
        //This is a sort of migration where if we have old
        //component which does not have a new property which is 
        //added in a later release , then old component still
        //works. In gui we expect component to have this new property
        //and it will fail to work in case old components. so this
        //migration make sure old component still works.
        if(prop == null) {
            TcgComponentType type = getComponentType();
            if(type != null) {
                TcgPropertyType tProp = type.getPropertyType(name);
                if(tProp != null) {
                    if(!tProp.isTransient()) {
                        String propName = tProp.getName();
                        String defaultValue = tProp.getDefaultValueAsString();
                        prop = getModel().getFactory().createProperty(getModel());
                        prop.setName(propName);
                        if(defaultValue != null) {
                            prop.setValue(defaultValue);
                        }
                        
                        //add this property to component
                        getModel().startTransaction();
                        addProperty(prop);
                        getModel().endTransaction();
                        
                    }
                }
            }
        }
        return prop;
    }
    
    public TcgComponentType getComponentType() {
        if(mType == null) {
            this.mType = TcgModelManager.getInstance().getTcgComponentType(getType());
        }
        return mType;
    }
    
    public void setDocumentation(Documentation doc) {
        if(doc != null) {
            Documentation oldDoc = getDocumentation();
            if(oldDoc != null) {
                oldDoc.setTextContent(doc.getTextContent());
            }  else {
                Collection<Class<? extends IEPComponent>> childTypes = new ArrayList<Class<? extends IEPComponent>>();
                childTypes.addAll(TypeCollection.FOR_PROPERTY.types());
                childTypes.addAll(TypeCollection.FOR_COMPONENT.types());
                
                addBefore(COMPONENT_CHILD, doc, childTypes);
                
            }
        } else {
            Documentation oldDoc = getDocumentation();
            if(oldDoc != null) {
                removeChild(oldDoc);
            }
        }
    }
    
    public Documentation getDocumentation() {
        List<Documentation> children = getChildren(Documentation.class);
        if(children.size() != 0) {
            return children.get(0);
        }
        
        return null;
    }

    public boolean hasPropertyDefined(String propName) {
        return getComponentType().hasPropertyType(propName);
    }

    private void throwExceptionIfNull(Property p) {
        if (p == null) {
            String msg = NbBundle.getMessage(ComponentImpl.class, "ComponentImpl.Property_is_not_defined_for_component");
            throw new IllegalArgumentException(msg);
        }
    }

    public String getString(String propName) {
        Property p = getProperty(propName);
        if (p == null) {
            TcgComponentType ct = getComponentType();
            if (ct.hasPropertyType(propName)) {
                return (String) ct.getPropertyType(propName).getDefaultValue();
            } else {
                throwExceptionIfNull(p);
            }
        }
        return p.getValue();
    }
    public void setString(String propName, String value) {
        Property p = getProperty(propName);
        throwExceptionIfNull(p);
        p.setValue(value);
    }

    public int getInt(String propName) {
        Property p = getProperty(propName);
        if (p == null) {
            TcgComponentType ct = getComponentType();
            if (ct.hasPropertyType(propName)) {
                return (Integer) ct.getPropertyType(propName).getDefaultValue();
            } else {
                throwExceptionIfNull(p);
            }
        }    
        return Integer.parseInt(p.getValue());
    }
    public void setInt(String propName, int value) {
        Property p = getProperty(propName);
        throwExceptionIfNull(p);
        p.setValue("" + value);
    }

    public boolean getBoolean(String propName) {
        Property p = getProperty(propName);
        if (p == null) {
            TcgComponentType ct = getComponentType();
            if (ct.hasPropertyType(propName)) {
                return (Boolean) ct.getPropertyType(propName).getDefaultValue();
            } else {
                throwExceptionIfNull(p);
            }
        }    
        return Boolean.parseBoolean(p.getValue());
    }
    public void setBoolean(String propName, boolean value) {
        Property p = getProperty(propName);
        throwExceptionIfNull(p);
        p.setValue("" + value);
    }

    public List<String> getStringList(String propName) {
        Property p = getProperty(propName);
        TcgComponentType ct = getComponentType();
        if (p == null) {
            if (ct.hasPropertyType(propName)) {
                return (List<String>) ct.getPropertyType(propName).getDefaultValue();
            } else {
                throwExceptionIfNull(p);
            }
        }    
        return (List<String>)ct.getPropertyType(propName).getType().parse(p.getValue());
    }
    
    public void setStringList(String propName, List<String> value) {
        Property p = getProperty(propName);
        throwExceptionIfNull(p);
        p.setValue(getComponentType().getPropertyType(propName).getType().format(value));
    }
}
