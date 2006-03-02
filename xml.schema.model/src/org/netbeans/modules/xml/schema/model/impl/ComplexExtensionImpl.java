/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ComplexExtensionImpl.java
 *
 * Created on October 6, 2005, 9:15 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;

import org.w3c.dom.Element;
/**
 *
 * @author rico
 */
public class ComplexExtensionImpl extends CommonExtensionImpl implements ComplexExtension{
    
    /** Creates a new instance of ComplexExtensionImpl */
    protected ComplexExtensionImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.EXTENSION, model));
    }
    
    public ComplexExtensionImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return ComplexExtension.class;
	}
    
    public void setLocalDefinition(ComplexExtensionDefinition content) {
        Collection<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        setChild(ComplexExtensionDefinition.class, LOCAL_DEFINITION_PROPERTY, content, list);
    }
    
    public ComplexExtensionDefinition getLocalDefinition() {
        Collection<ComplexExtensionDefinition> elements = getChildren(ComplexExtensionDefinition.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        //TODO should we throw exception if there is no definition?
        return null;
    }
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}
