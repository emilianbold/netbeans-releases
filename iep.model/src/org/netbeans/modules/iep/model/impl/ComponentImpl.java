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
import org.netbeans.modules.iep.model.lib.TcgComponentType;
import org.netbeans.modules.iep.model.lib.TcgModelManager;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement.StringAttribute;
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
    			removeChild(oldDoc);
    		} else {
    			addAfter(COMPONENT_CHILD, doc, TypeCollection.FOR_COMPONENT.types());
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
}
